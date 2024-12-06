package AliasAnalysis;

import sootup.core.jimple.basic.LValue;
import sootup.core.jimple.basic.Local;
import sootup.core.jimple.basic.StmtPositionInfo;
import sootup.core.jimple.basic.Value;
import sootup.core.jimple.common.expr.*;
import sootup.core.jimple.common.stmt.JAssignStmt;
import sootup.core.jimple.common.stmt.JIdentityStmt;
import sootup.core.jimple.common.stmt.JInvokeStmt;
import sootup.core.jimple.common.stmt.JReturnStmt;
import sootup.core.jimple.visitor.AbstractStmtVisitor;
import sootup.core.jimple.visitor.AbstractValueVisitor;
import sootup.core.signatures.MethodSignature;
import sootup.core.types.ReferenceType;

import javax.annotation.Nonnull;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

//visits a statement, generates constraints
public class ConstraintGenStmtVisitor extends AbstractStmtVisitor {

    private Map<Value,PointsToSet> varsToLocationsMap;
    private Map<MethodSignature, PointsToSet> returnedLocationsMap;
    private Set<Constraint> constraints;
    private Set<MemoryLocation> locations;
    private Set<MethodSignature> methodsInvoked; //method invocation will be visited after
    private StmtPositionInfo visitingStmtPositionInfo;
    private MethodManager methodManager;

    ConstraintGenStmtVisitor(){
        this.varsToLocationsMap = new HashMap<Value,PointsToSet>();
        this.constraints= new HashSet<Constraint>();
        this.locations= new HashSet<MemoryLocation>();
        this.returnedLocationsMap = new HashMap<MethodSignature, PointsToSet>();
        this.methodManager= new MethodManager();
    }

    public Set<MethodSignature> getMethodsInvoked() {
        return methodsInvoked;
    }

    public Set<Constraint> getConstraints() {
        return constraints;
    }

    //<< f(a1,a2...an); >> treat as f.args = ai and add f in MethodsInvoked
    @Override
    public void caseInvokeStmt(@Nonnull JInvokeStmt stmt) {
        visitingStmtPositionInfo =stmt.getPositionInfo();
        methodManager.noteMethod(stmt.getInvokeExpr().getMethodSignature());
        stmt.getInvokeExpr().accept(new ConstraintGenInvokeVisitor() );

    }
    public void caseIdentityStmt(@Nonnull JIdentityStmt stmt) {
        defaultCaseStmt(stmt);  //figure out what happens with 'this' and function arguments initialization
    }


    // treat as f(...) = stmt.getOp()
    @Override
    public void caseReturnStmt(@Nonnull JReturnStmt stmt) {
        defaultCaseStmt(stmt);
    }
    

    @Override
    public void caseAssignStmt(@Nonnull JAssignStmt stmt) {

        if( !(stmt.getLeftOp().getType() instanceof ReferenceType) ) return; //we re only interested in refs
        LValue leftOp = stmt.getLeftOp();
        Value rightOp = stmt.getRightOp();

        //malloc rule
        if(rightOp instanceof JNewExpr) {
            MemoryLocation l = new MemoryLocation(stmt.getPositionInfo().getStmtPosition().getFirstLine());
            constraints.add(new ElementOfConstraint(l,getOrCreateMappingOf(leftOp)));
            return;
        }

        visitingStmtPositionInfo =stmt.getPositionInfo();

        if(rightOp instanceof AbstractInvokeExpr)
            rightOp.accept(new ConstraintGenInvokeVisitor() );

        //copy rule
        PointsToSet superset =getOrCreateMappingOf(leftOp);
        PointsToSet subset=getOrCreateMappingOf(rightOp);
        constraints.add(new SubsetOfConstraint( subset, superset ));

    }




    private PointsToSet getOrCreateMappingOf(Value v){
        if(varsToLocationsMap.containsKey(v))
            return varsToLocationsMap.get(v);
        PointsToSet set = new PointsToSet(v.toString());
        varsToLocationsMap.put(v, set);
        return set;
    }

    private PointsToSet getOrCreateMappingOf(MethodSignature method){
        if(returnedLocationsMap.containsKey(method))
            return returnedLocationsMap.get(method);
        PointsToSet set = new PointsToSet(method.toString());
        returnedLocationsMap.put(method, set);
        return set;
    }

    public void printMethodsNoted(){
        methodManager.printParameterMappings();
    }

    //generates constraints for a single value
    //we only need this class for invocations
     class ConstraintGenInvokeVisitor extends AbstractValueVisitor{


        //method invokations
        @Override
        public void caseSpecialInvokeExpr(@Nonnull JSpecialInvokeExpr expr) {
            defaultInvokeExpr(expr);
        }

        @Override
        public void caseVirtualInvokeExpr(@Nonnull JVirtualInvokeExpr expr) {
            defaultInvokeExpr(expr);
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

            // MethodsInvoked.add(invokeExpr.getMethodSignature());



            JAssignStmt newAssignStmt;

            int i=0;                                //fix this, only for testing
            for(Value arg : invokeExpr.getArgs()) { //i think we can assume that argumenti will be locali in functino body
                LValue newLValue = new Local("argument" + i, invokeExpr.getMethodSignature().getParameterTypes().get(i));
                newAssignStmt=new JAssignStmt(newLValue, arg, visitingStmtPositionInfo);
                newAssignStmt.accept(ConstraintGenStmtVisitor.this);
                methodManager.argumentIsSubsetOf(invokeExpr.getMethodSignature(),i,getOrCreateMappingOf(arg));
                i++;
            }

            System.out.println("okokok");
        }

    }

}

