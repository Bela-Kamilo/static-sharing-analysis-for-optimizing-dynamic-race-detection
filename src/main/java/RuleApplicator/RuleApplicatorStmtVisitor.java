package RuleApplicator;

import GenericSolver.GenericConstraint;
import GenericSolver.SupersetOfConstraint;
import PTAnalysis.*;
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

/**Implements statement and value rules for points-to and side effect analyses.
 * Visits a statement, generates appropriate points-to and side effect constraints.
 * Make sure the appropriate visitingMethod has been set before visiting a statement
 * */
public class RuleApplicatorStmtVisitor extends AbstractStmtVisitor {
    private final ConstraintManager constraintManager;
    private final Set<MethodSignature> methodsInvoked; //method invocations which will be visited after
    private MethodSignature visitingMethod=null;

   public RuleApplicatorStmtVisitor(ConstraintManager constraintManager){
       this.constraintManager=constraintManager;
       this.methodsInvoked= new HashSet<>();
    }

    public void setVisitingMethod(MethodSignature method){
        visitingMethod=method;
        constraintManager.initFieldsReadAndWritten(method);

    }

    public Set<MethodSignature> getMethodsInvoked() {
        return methodsInvoked;
    }


    public static boolean isLocationHolder(Value v){
        //PTAnalysis is only really interested in refs                                  v i should find a better way
        return (v.getType() instanceof ReferenceType) && ! v.getType().toString().equals("java.lang.String");
    }

    //<< x.f(a1,a2...an); >> treat as  f.this=x , f.params = args and add f in MethodsInvoked
    @Override
    public void caseInvokeStmt(@Nonnull JInvokeStmt stmt) {
        stmt.getInvokeExpr().accept(new ConstraintGenInvokeVisitor() );
        sideEffectsInvocationValueRule(stmt.getInvokeExpr());
    }

    public void caseIdentityStmt(@Nonnull JIdentityStmt stmt) {
        Value leftOp= stmt.getLeftOp();
        Value rightOp= stmt.getRightOp();
        PointsToSet subset;
        PointsToSet superset;
        if(!(isLocationHolder(rightOp))) return;

        if(rightOp instanceof JParameterRef){
            subset=constraintManager.getOrCreateMappingOf(visitingMethod,((JParameterRef) rightOp).getIndex()+1);
            superset=constraintManager.getOrCreateMappingOf(leftOp, visitingMethod);
            constraintManager.addPTA(new PTASupersetOfConstraint(superset, subset));
            return;
        }

        if(rightOp instanceof JThisRef){
            //this := @this: Type;     =>   let 'this' as a Local be a superset of all the instances calling this method
            //                              it s ok as long as this as a Local doesnt get assigned (which is illegal anyways)
            subset=constraintManager.getOrCreateMappingOf(visitingMethod,constraintManager.getTHIS_INDEX());
            superset=constraintManager.getOrCreateMappingOf(leftOp, visitingMethod);      //left op is 'this'; should be visitingMethod.this
            constraintManager.addPTA(new PTASupersetOfConstraint(superset, subset));
            return;
        }

        return;
    }


    // return-statement rule
    @Override
    public void caseReturnStmt(@Nonnull JReturnStmt stmt) {
        if( !(isLocationHolder(stmt.getOp())) ) return;

        PointsToSet superset =constraintManager.getOrCreateMappingOfMethod(visitingMethod);
        PointsToSet subset=constraintManager.getOrCreateMappingOf(stmt.getOp(), visitingMethod);
        constraintManager.addPTA(new PTASupersetOfConstraint(superset, subset));
    }
    @Override
    public void caseRetStmt(@Nonnull JRetStmt stmt){System.out.println("VISITED A RET STATEMENT(?)");}
    

    @Override
    public void caseAssignStmt(@Nonnull JAssignStmt stmt) {
        LValue leftOp = stmt.getLeftOp();
        Value rightOp = stmt.getRightOp();
        if( !(isLocationHolder(rightOp))){//PTAnalysis is only really interested in refs
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
        castStmtRule(stmt);
        methodAssignmentStmtRule(stmt);
        arraysCopyStmtRule(stmt);
        fieldReadAssignmentStmtRule(stmt);
        fieldAssignAssignmentStmtRule(stmt);
        sideEffectReadStmtRule(stmt);
        sideEffectWriteStmtRule(stmt);
        sideEffectsInvocationValueRule(stmt.getRightOp());
        return;
    }

    /*              POINTS-TO ANALYSIS RULES
    ------------------------------------------------------------------------
     */

    private boolean newAssignmentStmtRule(JAssignStmt stmt){
        if(stmt.getRightOp() instanceof JNewExpr) {
            ObjectMemoryLocation l = new ObjectMemoryLocation(stmt.getPositionInfo().getStmtPosition().getFirstLine());
            PointsToSet set =constraintManager.getOrCreateMappingOf(stmt.getLeftOp(), visitingMethod);
            constraintManager.addPTA(new PTAElementOfConstraint(l, set));
            return true;
        }
        return false;
    }
    private boolean copyStmtRule(JAssignStmt stmt){
        LValue leftOp= stmt.getLeftOp();
        Value rightOp= stmt.getRightOp();
        if(copyRuleApplies(leftOp , rightOp)){
            PointsToSet superset =constraintManager.getOrCreateMappingOf(leftOp, visitingMethod);
            PointsToSet subset=constraintManager.getOrCreateMappingOf(rightOp, visitingMethod);
            constraintManager.addPTA(new PTASupersetOfConstraint(superset,  subset));
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

    private boolean castStmtRule(JAssignStmt stmt){
        LValue leftOp= stmt.getLeftOp();
        Value rightOp= stmt.getRightOp();
        if(rightOp instanceof JCastExpr){
            Value castedRightOp =((JCastExpr) rightOp).getOp();
            PointsToSet superset =constraintManager.getOrCreateMappingOf(leftOp, visitingMethod);
            PointsToSet subset=constraintManager.getOrCreateMappingOf(castedRightOp, visitingMethod);
            constraintManager.addPTA(new PTASupersetOfConstraint(superset,  subset));
            return true;
        }
        return false;
    }

   private boolean methodAssignmentStmtRule(JAssignStmt stmt){
        if(stmt.getRightOp() instanceof AbstractInvokeExpr) {
            Value rightOp = stmt.getRightOp();
            LValue leftOp = stmt.getLeftOp();
            rightOp.accept(new ConstraintGenInvokeVisitor());

            PointsToSet superset =constraintManager.getOrCreateMappingOf(leftOp, visitingMethod);
            PointsToSet subset=constraintManager.getOrCreateMappingOf(rightOp, visitingMethod);
            constraintManager.addPTA(new PTASupersetOfConstraint(superset, subset));
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
            PointsToSet rvalueSet= constraintManager.getOrCreateMappingOf(rvalue, visitingMethod);
            PointsToSet lvalueSet= constraintManager.getOrCreateMappingOf(lvalue, visitingMethod);
            constraintManager.addPTA(new PTASupersetOfConstraint(lvalueSet,rvalueSet));
            constraintManager.addPTA(new PTASupersetOfConstraint(rvalueSet,lvalueSet));
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
            PointsToSet superset =constraintManager.getOrCreateMappingOf(leftOp, visitingMethod);
            PointsToSet subset=constraintManager.getOrCreateMappingOf(rightOp, visitingMethod);
            constraintManager.addPTA(new PTASupersetOfConstraint(superset,  subset, subsetField));
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
            PointsToSet superset =constraintManager.getOrCreateMappingOf(leftOp, visitingMethod);
            PointsToSet subset=constraintManager.getOrCreateMappingOf(rightOp, visitingMethod);
            constraintManager.addPTA(new PTASupersetOfConstraint(superset,superSetField,  subset));
            return true;
        }
        return false;
    }


    /**
     * Implements method-invocation-value rule
     * */
     class ConstraintGenInvokeVisitor extends AbstractValueVisitor{

        @Override
        public void caseSpecialInvokeExpr(@Nonnull JSpecialInvokeExpr expr) {
            defaultInvokeExpr(expr);
            //x.f(a); >> f.this=x
            PointsToSet superset =constraintManager.getOrCreateMappingOf(expr.getMethodSignature(), constraintManager.getTHIS_INDEX());
            PointsToSet subset=constraintManager.getOrCreateMappingOf(expr.getBase(), visitingMethod);
            constraintManager.addPTA(new PTASupersetOfConstraint(superset, subset));
        }


        @Override
        public void caseVirtualInvokeExpr(@Nonnull JVirtualInvokeExpr expr) {
            defaultInvokeExpr(expr);
            //x.f(a); >> f.this=x
            PointsToSet superset =constraintManager.getOrCreateMappingOf(expr.getMethodSignature(), constraintManager.getTHIS_INDEX());
            PointsToSet subset=constraintManager.getOrCreateMappingOf(expr.getBase(), visitingMethod);
            constraintManager.addPTA(new PTASupersetOfConstraint(superset, subset));
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
            int i=constraintManager.getTHIS_INDEX()+1;
            for(Value arg : invokeExpr.getArgs()) {
                if (!(isLocationHolder(arg))) {
                    i++;
                    continue;
                }
                PointsToSet superset =constraintManager.getOrCreateMappingOf(invokeExpr.getMethodSignature(), i);
                PointsToSet subset=constraintManager.getOrCreateMappingOf(arg, visitingMethod);
                constraintManager.addPTA(new PTASupersetOfConstraint(superset, subset));
                i++;
            }
        }
    }

/*              SIDE EFFECTS RULES
------------------------------------------------------------------------
*/
    private boolean sideEffectReadStmtRule(JAssignStmt stmt){
        Value rightOp=stmt.getRightOp();

        if(rightOp instanceof JInstanceFieldRef){
            FieldSignature field = ((JInstanceFieldRef) rightOp).getFieldSignature();
            PointsToSet baseSet = constraintManager.getOrCreateMappingOf(((JInstanceFieldRef) rightOp).getBase(), visitingMethod);
            constraintManager.methodReads(visitingMethod,baseSet,field);
            return true;
        }
        return false;
    }

    private boolean sideEffectWriteStmtRule(JAssignStmt stmt){
        LValue leftOp=stmt.getLeftOp();

        if(leftOp instanceof JInstanceFieldRef){
            FieldSignature field  = ((JInstanceFieldRef) leftOp).getFieldSignature();
            PointsToSet baseSet = constraintManager.getOrCreateMappingOf(((JInstanceFieldRef) leftOp).getBase(), visitingMethod);
            constraintManager.methodWrites(visitingMethod,baseSet,field);
            return true;
        }
        return false;
    }

    private boolean sideEffectsInvocationValueRule(Value v){
        if(v instanceof AbstractInvokeExpr){
            MethodSignature m = ((AbstractInvokeExpr) v).getMethodSignature();
            if(isRunMethod(m)) return false;
            constraintManager.addSE(new SupersetOfConstraint<>(
                    constraintManager.getOrCreateMethodReadSet(visitingMethod),visitingMethod+"._READS",
                    constraintManager.getOrCreateMethodReadSet(m), m+"._READS"));
            constraintManager.addSE(new SupersetOfConstraint<>(
                    constraintManager.getOrCreateMethodWriteSet(visitingMethod),visitingMethod+"._WRITES",
                    constraintManager.getOrCreateMethodWriteSet(m), m+"._WRITES"));
            return true;
        }
        return false;
    }

    private boolean isRunMethod(MethodSignature m){
        String runMethodPattern = ".*void\\s+run\\s*\\(\\s*\\).*";
        return m.toString().matches(runMethodPattern);
    }

}
