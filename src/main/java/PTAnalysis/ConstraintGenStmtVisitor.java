package PTAnalysis;

import PTAnalysis.ConstraintSolver.Constraint;
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
import sootup.core.signatures.FieldSignature;
import sootup.core.signatures.MethodSignature;
import sootup.core.types.ArrayType;
import sootup.core.types.ReferenceType;
import sootup.core.types.Type;
import util.Tuple;

import javax.annotation.Nonnull;
import java.util.*;
import java.util.List;

/** Visits a statement, generates appropriate constraints.
 * Holds the constraints themselves
 * Make sure the appropriate visitingMethod has been set before visiting a statement
 * */
public class ConstraintGenStmtVisitor extends AbstractStmtVisitor {

    private final Map<Value,PointsToSet> varsToLocationsMap;
    private final Map<MethodSignature, PointsToSet> returnedLocationsMap;
    private final Map<MethodSignature, Vector<PointsToSet>> parametersLocationsMap;
    private final Set<Constraint> constraints;
    private final int THIS_INDEX=0;

    private final Map<MethodSignature, Set<Tuple<PointsToSet,FieldSignature >>> fieldsRead;
    private final Map<MethodSignature, Set<Tuple<PointsToSet,FieldSignature>>> fieldsWritten;


    private final Set<MethodSignature> methodsInvoked; //method invocations which will be visited after
    private MethodSignature visitingMethod=null;

    ConstraintGenStmtVisitor(){
        this.constraints= new HashSet<>();
        this.parametersLocationsMap= new HashMap<>();
        this.returnedLocationsMap = new HashMap<>();
        this.methodsInvoked= new HashSet<>();
        this.fieldsRead= new HashMap<>();
        this.fieldsWritten= new HashMap<>();
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

    public void setVisitingMethod(MethodSignature method){
        visitingMethod=method;
        this.fieldsRead.put(method, new HashSet<>());
        this.fieldsWritten.put(method, new HashSet<>());
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
            constraints.add(new PTASupersetOfConstraint(superset, subset));
            return;
        }

        if(rightOp instanceof JThisRef){
            //this := @this: Type;     =>   let 'this' as a Local be a superset of all the instances calling this method
            //                              it s ok as long as this as a Local doesnt get assigned (which is illegal anyways)
            subset=getOrCreateMappingOf(visitingMethod,THIS_INDEX);
            superset=getOrCreateMappingOf(leftOp);      //left op is 'this'; should be visitingMethod.this
            constraints.add(new PTASupersetOfConstraint(superset, subset));
            return;
        }

        return;
    }


    // return-statement rule
    @Override
    public void caseReturnStmt(@Nonnull JReturnStmt stmt) {
        if( !(stmt.getOp().getType() instanceof ReferenceType) ) return;

        PointsToSet superset =getOrCreateMappingOfMethod(visitingMethod);
        PointsToSet subset=getOrCreateMappingOf(stmt.getOp());
        constraints.add(new PTASupersetOfConstraint(superset, subset));
    }
    @Override
    public void caseRetStmt(@Nonnull JRetStmt stmt){System.out.println("VISITED A RET STATEMENT(?)");}
    

    @Override
    public void caseAssignStmt(@Nonnull JAssignStmt stmt) {
        LValue leftOp = stmt.getLeftOp();
        Value rightOp = stmt.getRightOp();                          // v find a better way
        if( !(leftOp.getType() instanceof ReferenceType) || leftOp.getType().toString().equals("java.lang.String")){//PTAnalysis is only really interested in refs
            stmt.getRightOp().accept(new ConstraintGenInvokeVisitor());
            //stmt.getLeftOp().accept(new ConstraintGenInvokeVisitor());    //no point
            sideEffectReadStmtRule(stmt);
            sideEffectWriteStmtRule(stmt);
            return;
        }
        if(leftOp.getType().toString().equals("java.lang.String")){
            return;
        }
        if(rightOp instanceof JNewArrayExpr || rightOp instanceof JNewMultiArrayExpr ) return;

        newAssignmentStmtRule(stmt);
        copyStmtRule(stmt);
        methodAssignmentStmtRule(stmt);
        arraysCopyStmtRule(stmt);
        fieldReadAssignmentStmtRule(stmt);
        fieldAssignAssignmentStmtRule(stmt);
        sideEffectReadStmtRule(stmt);
        sideEffectWriteStmtRule(stmt);
        return;
    }

    /*              POINTS-TO ANALYSIS RULES
    ------------------------------------------------------------------------
     */

    private boolean newAssignmentStmtRule(JAssignStmt stmt){
        if(stmt.getRightOp() instanceof JNewExpr) {
            ObjectMemoryLocation l = new ObjectMemoryLocation(stmt.getPositionInfo().getStmtPosition().getFirstLine());
            constraints.add(new PTAElementOfConstraint(l,getOrCreateMappingOf(stmt.getLeftOp())));
            return true;
        }
        return false;
    }
    private boolean copyStmtRule(JAssignStmt stmt){
        LValue leftOp= stmt.getLeftOp();
        Value rightOp= stmt.getRightOp();
        if(copyRuleApplies(leftOp , rightOp)){
            PointsToSet superset =getOrCreateMappingOf(leftOp);
            PointsToSet subset=getOrCreateMappingOf(rightOp);
            constraints.add(new PTASupersetOfConstraint(superset,  subset));
            return true;
        }
        return false;
    }
    /**
     * true for non array type locals,
     * non array type array refs,
     * or static fields
     */
    private boolean copyRuleApplies(LValue leftOp, Value rightOp){
        boolean leftOpOK = leftOp instanceof Local && ! (leftOp.getType() instanceof ArrayType)
                || (leftOp instanceof JArrayRef && leftOp.getType() instanceof ArrayType &&  ((ArrayType) leftOp.getType()).getDimension()==1)
                || leftOp instanceof JStaticFieldRef;
        boolean rightOpOK = rightOp instanceof Local && ! (rightOp.getType() instanceof ArrayType)
                || (rightOp instanceof JArrayRef && rightOp.getType() instanceof ArrayType &&  ((ArrayType) rightOp.getType()).getDimension()==1)
                || rightOp instanceof JStaticFieldRef;
        return leftOpOK &&rightOpOK ;
    }


   private boolean methodAssignmentStmtRule(JAssignStmt stmt){
        if(stmt.getRightOp() instanceof AbstractInvokeExpr) {
            Value rightOp = stmt.getRightOp();
            LValue leftOp = stmt.getLeftOp();
            rightOp.accept(new ConstraintGenInvokeVisitor());

            PointsToSet superset =getOrCreateMappingOf(leftOp);
            PointsToSet subset=getOrCreateMappingOf(rightOp);
            constraints.add(new PTASupersetOfConstraint(superset, subset));
            return true;
        }
        return false;
    }
    /**
     * We equate 2 different array locals' points to sets.
     * We do this for 2 reasons :
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
    private boolean arraysCopyStmtRule(JAssignStmt stmt){
        LValue lvalue = stmt.getLeftOp();
        Value rvalue=stmt.getRightOp();
        if(lvalue.getType() instanceof ArrayType && rvalue.getType() instanceof  ArrayType){
            PointsToSet rvalueSet= getOrCreateMappingOf(rvalue);
            PointsToSet lvalueSet= getOrCreateMappingOf(lvalue);
            constraints.add(new PTASupersetOfConstraint(lvalueSet,rvalueSet));
            constraints.add(new PTASupersetOfConstraint(rvalueSet,lvalueSet));
            return true;
        }
        return false;
    }
    private boolean fieldReadAssignmentStmtRule(JAssignStmt stmt){
        if(stmt.getRightOp() instanceof JInstanceFieldRef){

            //checks if field references are a part of this assignment
            AbstractValueVisitor<String> fieldValueVisitor =new AbstractValueVisitor<>() {
                @Override
                public void caseInstanceFieldRef(@Nonnull JInstanceFieldRef ref) {
                    setResult( ref.getFieldSignature().toString());
                }

                @Override
                public void defaultCaseValue(@Nonnull Value v) {setResult(null);}
            };
            LValue leftOp= stmt.getLeftOp();
            Value rightOp= stmt.getRightOp();
            rightOp.accept(fieldValueVisitor);
            String subsetField=fieldValueVisitor.getResult();
            if(subsetField == null) throw new RuntimeException("Field-Read-Assignment-Statement rule applied on no field assignment");
            PointsToSet superset =getOrCreateMappingOf(leftOp);
            PointsToSet subset=getOrCreateMappingOf(rightOp);
            constraints.add(new PTASupersetOfConstraint(superset,  subset, subsetField));
            return true;
        }
        return false;
    }
    private boolean fieldAssignAssignmentStmtRule(JAssignStmt stmt){
        if(stmt.getLeftOp() instanceof JInstanceFieldRef){

            //checks if field references are a part of this assignment
            AbstractValueVisitor<String> fieldValueVisitor =new AbstractValueVisitor<>() {
                @Override
                public void caseInstanceFieldRef(@Nonnull JInstanceFieldRef ref) {
                    setResult( ref.getFieldSignature().toString());
                }

                @Override
                public void defaultCaseValue(@Nonnull Value v) {setResult(null);}
            };
            LValue leftOp= stmt.getLeftOp();
            Value rightOp= stmt.getRightOp();
            leftOp.accept(fieldValueVisitor);
            String superSetField=fieldValueVisitor.getResult();
            if(superSetField == null) throw new RuntimeException("Field-Assign-Assignment-Statement rule applied on no field assignment");
            PointsToSet superset =getOrCreateMappingOf(leftOp);
            PointsToSet subset=getOrCreateMappingOf(rightOp);
            constraints.add(new PTASupersetOfConstraint(superset,superSetField,  subset));
            return true;
        }
        return false;
    }

    /*              SIDE EFFECTS RULES
    ------------------------------------------------------------------------
     */
    private void sideEffectReadStmtRule(JAssignStmt stmt){
       Value rightOp=stmt.getRightOp();

        if(rightOp instanceof JInstanceFieldRef){
           FieldSignature field = ((JInstanceFieldRef) rightOp).getFieldSignature();
           PointsToSet baseSet = getOrCreateMappingOf(((JInstanceFieldRef) rightOp).getBase());
           fieldsRead.get(visitingMethod).add(new Tuple<>(baseSet,field));
        }
    }

    private void sideEffectWriteStmtRule(JAssignStmt stmt){
        LValue leftOp=stmt.getLeftOp();

        if(leftOp instanceof JInstanceFieldRef){
            FieldSignature field  = ((JInstanceFieldRef) leftOp).getFieldSignature();
            PointsToSet baseSet = getOrCreateMappingOf(((JInstanceFieldRef) leftOp).getBase());
            fieldsWritten.get(visitingMethod).add(new Tuple<>(baseSet,field));
        }
    }
    //--------------------------------------------------------------------

    public Set<AccessibleHeapLocation> getWritesOf(MethodSignature m){
        Set<AccessibleHeapLocation> res= new HashSet<>();

        Set<Tuple<PointsToSet,FieldSignature>> PTSetsAndFields =fieldsWritten.get(m);
        for(Tuple<PointsToSet,FieldSignature> PTSetAndField : PTSetsAndFields)
            for(int i : PTSetAndField.getElem1())
                res.add(new AccessibleHeapLocation(i,PTSetAndField.getElem2()));

        return res;
    }

    public Set<AccessibleHeapLocation> getReadsOf(MethodSignature m){
        Set<AccessibleHeapLocation> res= new HashSet<>();

        Set<Tuple<PointsToSet,FieldSignature>> PTSetsAndFields =fieldsRead.get(m);
        for(Tuple<PointsToSet,FieldSignature> PTSetAndField : PTSetsAndFields)
            for(int i : PTSetAndField.getElem1())
                res.add(new AccessibleHeapLocation(i,PTSetAndField.getElem2()));

        return res;
    }

    // maybe store this in the future so we dont recompute it each time we call getWrites or getReads

    public Map<MethodSignature,Set<AccessibleHeapLocation>> getWrites(){
        Map<MethodSignature, Set<AccessibleHeapLocation>> res= new HashMap<>();
        fieldsWritten.forEach((method,_)->{res.put(method,getWritesOf(method));});

        return res;
    }

    public Map<MethodSignature,Set<AccessibleHeapLocation>> getReads(){
        Map<MethodSignature, Set<AccessibleHeapLocation>> res= new HashMap<>();
        fieldsRead.forEach((method,_)->{res.put(method,getReadsOf(method));});

        return res;
    }
    /** value -> PTSet
     * A mapping of a value to its PTSet*/
    private PointsToSet getOrCreateMappingOf(Value v){
        if(v instanceof AbstractInvokeExpr){
            return getOrCreateMappingOfMethod( ( (AbstractInvokeExpr) v).getMethodSignature());
        }
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


    /** Visits a value, generates constraints for method invocations
     * method-invocation-value rule is implemented here
     * */
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
            constraints.add(new PTASupersetOfConstraint(superset, subset));
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
                constraints.add(new PTASupersetOfConstraint(superset, subset));
                i++;
            }
        }

    }

}
