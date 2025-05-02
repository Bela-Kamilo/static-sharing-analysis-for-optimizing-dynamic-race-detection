package PTAnalysis;

import sootup.core.jimple.basic.LValue;
import sootup.core.jimple.basic.Local;
import sootup.core.jimple.basic.Value;
import sootup.core.jimple.common.expr.*;
import sootup.core.jimple.common.ref.*;
import sootup.core.jimple.common.stmt.JAssignStmt;
import sootup.core.jimple.common.stmt.JIdentityStmt;
import sootup.core.jimple.common.stmt.JInvokeStmt;
import sootup.core.jimple.common.stmt.JReturnStmt;
import sootup.core.jimple.javabytecode.stmt.JRetStmt;
import sootup.core.jimple.visitor.AbstractStmtVisitor;
import sootup.core.jimple.visitor.AbstractValueVisitor;
import sootup.core.signatures.MethodSignature;
import sootup.core.types.ArrayType;
import sootup.core.types.ReferenceType;
import sootup.core.types.Type;

import javax.annotation.Nonnull;
import java.util.*;
import java.util.List;

/** visits a statement, generates appropriate constraints */
public class ConstraintGenStmtVisitor extends AbstractStmtVisitor {

    private final Map<Value,PointsToSet> varsToLocationsMap;
    private final Map<MethodSignature, PointsToSet> returnedLocationsMap;
    private final Map<MethodSignature, Vector<PointsToSet>> parametersLocationsMap;
    private final Map<Local, PointsToSet> arraysMap;
    private final Set<Constraint> constraints;
    private final int THIS_INDEX=0;

    private final Set<MethodSignature> methodsInvoked; //method invocation will be visited after
    private MethodSignature visitingMethod=null;

    ConstraintGenStmtVisitor(){
        this.constraints= new HashSet<>();
        this.parametersLocationsMap= new HashMap<>();
        this.returnedLocationsMap = new HashMap<>();
        this.methodsInvoked= new HashSet<>();
        this.arraysMap= new HashMap<>();
        this.varsToLocationsMap = new TreeMap<>(new Comparator<Value>() {       // we want to differentiate between same name locals
            @Override                                                           //of different methods
            public int compare(Value o1, Value o2) {
                if(o1==o2) return 0;
                if(o1 instanceof JStaticFieldRef && o2 instanceof JStaticFieldRef)//static field values however are different for the same field
                    return ((JStaticFieldRef) o1).getFieldSignature().compareTo(((JStaticFieldRef) o2).getFieldSignature());
                int r =o1.toString().compareTo(o2.toString());
                return r==0? 1 : r;
            }
        });
    }

    public void setVisitingMethod(MethodSignature method){visitingMethod=method;}

    public Map<Value, PointsToSet> getVarsToLocationsMap() {
        return varsToLocationsMap;
    }

    public Set<MethodSignature> getMethodsInvoked() {
        return methodsInvoked;
    }

    public Set<Constraint> getConstraints() {
        return constraints;
    }

    //<< x.f(a1,a2...an); >> treat as f(x,a1,a2...an) >>  f.params = args and add f in MethodsInvoked
    @Override
    public void caseInvokeStmt(@Nonnull JInvokeStmt stmt) {
        stmt.getInvokeExpr().accept(new ConstraintGenInvokeVisitor() );

    }

    public void caseIdentityStmt(@Nonnull JIdentityStmt stmt) {
        Value leftOp= stmt.getLeftOp();
        Value rightOp= stmt.getRightOp();
        PointsToSet subset;
        PointsToSet superset;
        if(!(rightOp.getType() instanceof ReferenceType)) return;

        if(rightOp instanceof JParameterRef){
            subset=getOrCreateMappingOf(visitingMethod,((JParameterRef) rightOp).getIndex()+1);
            superset=getOrCreateMappingOf(leftOp);
            constraints.add(new SupersetOfConstraint(superset, subset));
            return;
        }



        if(rightOp instanceof JThisRef){
            //this := @this: Type;     =>   let 'this' as a Local be a superset of all the instances calling this method
            //                              it s ok as long as this as a Local doesnt get assigned (which is illegal anyways)
            subset=getOrCreateMappingOf(visitingMethod,THIS_INDEX);
            superset=getOrCreateMappingOf(leftOp);      //left op is 'this'; should be visitingMethod.this
            constraints.add(new SupersetOfConstraint(superset, subset));
            return;
        }

        return;
    }


    // treat as f(...) = stmt.getOp()
    @Override
    public void caseReturnStmt(@Nonnull JReturnStmt stmt) {
        if( !(stmt.getOp().getType() instanceof ReferenceType) ) return;

        PointsToSet superset =getOrCreateMappingOfMethod(visitingMethod);
        PointsToSet subset=getOrCreateMappingOf(stmt.getOp());
        constraints.add(new SupersetOfConstraint(superset, subset));
    }
    @Override
    public void caseRetStmt(@Nonnull JRetStmt stmt){System.out.println("VISITED A RET STATEMENT(?)");}
    

    @Override
    public void caseAssignStmt(@Nonnull JAssignStmt stmt) {
        LValue leftOp = stmt.getLeftOp();
        Value rightOp = stmt.getRightOp();
        if( !(leftOp.getType() instanceof ReferenceType)  ){//we re only interested in refs
            stmt.getRightOp().accept(new ConstraintGenInvokeVisitor());
            //stmt.getLeftOp().accept(new ConstraintGenInvokeVisitor());
            return;
        }
       //'new' rule
        if(rightOp instanceof JNewExpr) {
            MemoryLocation l = new MemoryLocation(stmt.getPositionInfo().getStmtPosition().getFirstLine());
            constraints.add(new ElementOfConstraint(l,getOrCreateMappingOf(leftOp)));
            return;
        }
        if(rightOp instanceof JNewArrayExpr || rightOp instanceof JNewMultiArrayExpr ){ return;}
        if(rightOp instanceof JArrayRef && leftOp.getType() instanceof ArrayType) {
            aliasArrays(leftOp, rightOp);
        }
        if(rightOp instanceof AbstractInvokeExpr) {
            rightOp.accept(new ConstraintGenInvokeVisitor());

            PointsToSet superset =getOrCreateMappingOf(leftOp);
            PointsToSet subset=getOrCreateMappingOf(rightOp);
            constraints.add(new SupersetOfConstraint(superset, subset));
            return;
        }
        //checks if field references are a part of this assignment
        AbstractValueVisitor<String> fieldValueVisitor =new AbstractValueVisitor<>() {
            @Override
            public void caseInstanceFieldRef(@Nonnull JInstanceFieldRef ref) {
                setResult( ref.getFieldSignature().toString());
            }

            @Override
            public void defaultCaseValue(@Nonnull Value v) {setResult(null);}
        };
        leftOp.accept(fieldValueVisitor);
        String supersetField=fieldValueVisitor.getResult();
        rightOp.accept(fieldValueVisitor);
        String subsetField=fieldValueVisitor.getResult();
        PointsToSet superset =getOrCreateMappingOf(leftOp);
        PointsToSet subset=getOrCreateMappingOf(rightOp);
        constraints.add(new SupersetOfConstraint(superset, supersetField, subset, subsetField));

    }
    /**
     * We make 2 different locals map to the same PointsToSet
     * We want this for 2 reasons :
     * 1) In cases of multidimensional arrays arr[i][j] and arr[k]
     * both refer to the contents of arr
     *
     * 2) array instances can be passed around through assignments
     *  as such
     * local1= arr
     * local1[i]= new //memory location l
     *
     * arr should also hold l in its PointsToSet
     */
    private void aliasArrays(Value lvalue, Value rvalue) {
        if(!(lvalue.getType() instanceof ArrayType && rvalue.getType() instanceof ArrayType))
            throw new RuntimeException("expected ArrayType, ArrayType. Got "+lvalue.getType()+" , "+rvalue.getType());
        if(varsToLocationsMap.containsKey(lvalue)) //think this is WRONG    - figure out what happens w/ case 2)
            throw new RuntimeException("expected "+lvalue + "not to be mapped to a PointsToSet");
        Value lvalueBase= lvalue instanceof JArrayRef ? ((JArrayRef) lvalue).getBase() : lvalue;
        Value rvalueBase= rvalue instanceof JArrayRef ? ((JArrayRef) rvalue).getBase() : rvalue;
        PointsToSet rvalueSet= getOrCreateMappingOf(rvalue);
       ///*
       if(!(rvalueSet instanceof PointsToSetOfArray ))
            throw new RuntimeException("An array local is mapped to a non PointsToSetOFArray PointsToSet");
        //((PointsToSetOfArray)rvalueSet).addAlias(rvalueBase.toString());
        //*/
        ((PointsToSetOfArray) rvalueSet).addAlias(visitingMethod +":"+lvalueBase.toString());
        varsToLocationsMap.put(lvalueBase, rvalueSet);//to change this w/ addAlias();


    }
    /** value -> PTSet
     * A mapping of a value to its PTSet*/
    private PointsToSet getOrCreateMappingOf(Value v){
        if(v instanceof AbstractInvokeExpr){
            return getOrCreateMappingOfMethod( ( (AbstractInvokeExpr) v).getMethodSignature());
        }
        if(v instanceof JArrayRef) return getOrCreateMappingOfArray((JArrayRef) v);
        Value v2;
        if(v instanceof JInstanceFieldRef) v2= ((JInstanceFieldRef) v).getBase();
        else if(v instanceof JArrayRef) v2= ((JArrayRef) v).getBase() ;
        else v2=v;
        if(varsToLocationsMap.containsKey(v2))
            return varsToLocationsMap.get(v2);
        String name = v2 instanceof JStaticFieldRef ? v2.toString() : visitingMethod +":"+v2;  //what else might be visible from outside visitingMethod?
        PointsToSet set = new PointsToSet(name);
        varsToLocationsMap.put(v2, set);
        return set;
    }

    /** method-> PTSet    returned locations
     * A mapping of a method to a PTSet of its possibly returned locations
     * */
    private PointsToSet getOrCreateMappingOfMethod(MethodSignature method){
        if(returnedLocationsMap.containsKey(method))
            return returnedLocationsMap.get(method);
        PointsToSet set = new PointsToSet(method.toString());
        returnedLocationsMap.put(method, set);
        return set;
    }

    /** method -> parametersPTSet
     * A mapping of a method to PTSets of its parameters
     * */
    private PointsToSet getOrCreateMappingOf(MethodSignature method,int paramOrdinal){

        try {
            if (parametersLocationsMap.containsKey(method))
                return parametersLocationsMap.get(method).get(paramOrdinal);
        } catch (Exception e) {
            System.err.println("!failed to get "+method+" 's parameter"+paramOrdinal);
        }

        Vector<PointsToSet> paramVector= new Vector<>();
        paramVector.add(new PointsToSet(method+".this"));
        List<Type> types = method.getParameterTypes();
        for(int i=1; i< types.size()+1;i++) {
            if(types.get(i-1) instanceof ReferenceType)
                paramVector.add(new PointsToSet(method + "." + i));
            else
                paramVector.add(null);
        }
        parametersLocationsMap.put(method, paramVector);
        return paramVector.get(paramOrdinal);
    }
    /** Array access-> PTSet
     *
     * */
    private PointsToSet getOrCreateMappingOfArray(JArrayRef ar){
        Local arrayBase=ar.getBase();
        if(varsToLocationsMap.containsKey(arrayBase))
            return varsToLocationsMap.get(arrayBase);
        PointsToSet set = new PointsToSetOfArray(visitingMethod +":"+arrayBase);
        varsToLocationsMap.put(arrayBase, set);
        return set;
    }


    /** Visits a value, generates constraints for method invocations */
     class ConstraintGenInvokeVisitor extends AbstractValueVisitor{

         /*
        public void caseArrayRef(@Nonnull JArrayRef ref) {
            Local base = ref.getBase();
        }*/

        @Override
        public void caseSpecialInvokeExpr(@Nonnull JSpecialInvokeExpr expr) {
            defaultInvokeExpr(expr);
        }

        @Override
        public void caseVirtualInvokeExpr(@Nonnull JVirtualInvokeExpr expr) {
            defaultInvokeExpr(expr);

            //x.f(a); >> f.this=x
            PointsToSet superset =getOrCreateMappingOf(expr.getMethodSignature(), THIS_INDEX);
            PointsToSet subset=getOrCreateMappingOf(expr.getBase());
            constraints.add(new SupersetOfConstraint(superset, subset));
        }

        @Override
        public void caseInterfaceInvokeExpr(@Nonnull JInterfaceInvokeExpr expr) {
            defaultInvokeExpr(expr);
        }

        @Override
        public void caseStaticInvokeExpr(@Nonnull JStaticInvokeExpr expr) {
            defaultInvokeExpr(expr);
        }

        @Override
        public void caseDynamicInvokeExpr(@Nonnull JDynamicInvokeExpr expr) {
            defaultInvokeExpr(expr);
        }


        private  void defaultInvokeExpr(AbstractInvokeExpr invokeExpr ){
             methodsInvoked.add(invokeExpr.getMethodSignature());
            int i=THIS_INDEX+1;
            for(Value arg : invokeExpr.getArgs()) {
                if (! (arg.getType() instanceof  ReferenceType))
                    continue;
                PointsToSet superset =getOrCreateMappingOf(invokeExpr.getMethodSignature(), i);
                PointsToSet subset=getOrCreateMappingOf(arg);
                constraints.add(new SupersetOfConstraint(superset, subset));
                i++;
            }
        }

    }

}
