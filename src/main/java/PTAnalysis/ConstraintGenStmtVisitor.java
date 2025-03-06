package PTAnalysis;

import sootup.core.jimple.basic.LValue;
import sootup.core.jimple.basic.Value;
import sootup.core.jimple.common.expr.*;
import sootup.core.jimple.common.ref.JInstanceFieldRef;
import sootup.core.jimple.common.ref.JParameterRef;
import sootup.core.jimple.common.ref.JThisRef;
import sootup.core.jimple.common.stmt.JAssignStmt;
import sootup.core.jimple.common.stmt.JIdentityStmt;
import sootup.core.jimple.common.stmt.JInvokeStmt;
import sootup.core.jimple.common.stmt.JReturnStmt;
import sootup.core.jimple.javabytecode.stmt.JRetStmt;
import sootup.core.jimple.visitor.AbstractStmtVisitor;
import sootup.core.jimple.visitor.AbstractValueVisitor;
import sootup.core.signatures.MethodSignature;
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
    private final Set<Constraint> constraints;
    private final int THIS_INDEX=0;

    private final Set<MethodSignature> methodsInvoked; //method invocation will be visited after
    private MethodSignature visitingMethod=null;

    ConstraintGenStmtVisitor(){
        this.constraints= new HashSet<>();
        this.parametersLocationsMap= new HashMap<>();
        this.returnedLocationsMap = new HashMap<>();
        this.methodsInvoked= new HashSet<>();
        this.varsToLocationsMap = new TreeMap<>(new Comparator<Value>() {       // we want to differentiate between same name locals
            @Override                                                           //of different methods
            public int compare(Value o1, Value o2) {
                if(o1==o2) return 0;
                int r =o1.toString().compareTo(o2.toString());
                return r==0? 1 : r;
            }
        });
    }

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

        PointsToSet superset =getOrCreateMappingOf(visitingMethod);
        PointsToSet subset=getOrCreateMappingOf(stmt.getOp());
        constraints.add(new SupersetOfConstraint(superset, subset));
    }
    @Override
    public void caseRetStmt(@Nonnull JRetStmt stmt){System.out.println("VISITED A RET STATEMENT(?)");}
    

    @Override
    public void caseAssignStmt(@Nonnull JAssignStmt stmt) {

        if( !(stmt.getLeftOp().getType() instanceof ReferenceType) ){//we re only interested in refs
            stmt.getRightOp().accept(new ConstraintGenInvokeVisitor());
            return;
        }
        LValue leftOp = stmt.getLeftOp();
        Value rightOp = stmt.getRightOp();

        //'new' rule
        if(rightOp instanceof JNewExpr) {
            MemoryLocation l = new MemoryLocation(stmt.getPositionInfo().getStmtPosition().getFirstLine());
            constraints.add(new ElementOfConstraint(l,getOrCreateMappingOf(leftOp)));
            return;
        }


        if(rightOp instanceof AbstractInvokeExpr) {
            rightOp.accept(new ConstraintGenInvokeVisitor());

            PointsToSet superset =getOrCreateMappingOf(leftOp);
            PointsToSet subset=getOrCreateMappingOf(((AbstractInvokeExpr) rightOp).getMethodSignature());
            constraints.add(new SupersetOfConstraint(superset, subset));
            return;
        }


        copyRule(leftOp, rightOp);

    }
    /**
     *   -------------------
     *  [a=b] ->  a )= b
     */
    void copyRule(LValue leftOp, Value rightOp){

        //checks if field references are a part of this assignement
        AbstractValueVisitor<Tuple<Value,String>> fieldValueVisitor =new AbstractValueVisitor<>() {
            @Override
            public void caseInstanceFieldRef(@Nonnull JInstanceFieldRef ref) {
                setResult(new Tuple<>(ref.getBase(), ref.getFieldSignature().toString()));
            }

            @Override
            public void defaultCaseValue(@Nonnull Value v) {
                setResult(null);
            }
        };
        leftOp.accept(fieldValueVisitor);
        Tuple<Value,String> leftOpBaseAndFieldTuple=fieldValueVisitor.getResult();
        rightOp.accept(fieldValueVisitor);
        Tuple<Value,String> rightOpFieldTuple=fieldValueVisitor.getResult();
        Value supersetBase= leftOp;
        String supersetField=null;
        Value subsetBase= rightOp;
        String subsetField=null;
        if(leftOpBaseAndFieldTuple!=null){
            supersetBase = leftOpBaseAndFieldTuple.getElem1();
            supersetField= leftOpBaseAndFieldTuple.getElem2();
        }
        if(rightOpFieldTuple!=null){
            subsetBase = rightOpFieldTuple.getElem1();
            subsetField= rightOpFieldTuple.getElem2();
        }
        PointsToSet superset =getOrCreateMappingOf(supersetBase);
        PointsToSet subset=getOrCreateMappingOf(subsetBase);
        constraints.add(new SupersetOfConstraint(superset, supersetField, subset, subsetField));

    }


    /** value -> PTSet
     * A mapping of a value to its PTSet*/
    private PointsToSet getOrCreateMappingOf(Value v){
        if(varsToLocationsMap.containsKey(v))
            return varsToLocationsMap.get(v);
        PointsToSet set = new PointsToSet(visitingMethod +":"+v);
        varsToLocationsMap.put(v, set);
        return set;
    }

    /** method-> PTSet    returned locations
     * A mapping of a method to a PTSet of its possibly returned locations
     * */
    private PointsToSet getOrCreateMappingOf(MethodSignature method){
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
    public void setVisitingMethod(MethodSignature method){visitingMethod=method;}


    /** Visits a value, generates constraints for method invocations */
     class ConstraintGenInvokeVisitor extends AbstractValueVisitor{



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

 class Tuple<T1,T2>{
   private T1 elem1;
   private T2 elem2;

    Tuple(T1 e1, T2 e2){
        elem1=e1;
        elem2=e2;
    }
     public void setElem1(T1 elem1) {
         this.elem1 = elem1;
     }

     public void setElem2(T2 elem2) {
         this.elem2 = elem2;
     }

     public T1 getElem1() {
         return elem1;
     }

     public T2 getElem2() {
         return elem2;
     }
 }
