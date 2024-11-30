package AliasAnalysis;

import sootup.core.jimple.basic.LValue;
import sootup.core.jimple.basic.Local;
import sootup.core.jimple.basic.Value;
import sootup.core.jimple.common.expr.AbstractInvokeExpr;
import sootup.core.jimple.common.expr.JNewExpr;
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
    private Set<Constraint> constraints;
    private ConstraintGenValueVisitor valueVisitor;
    private Map<MethodSignature, PointsToSet> returnedLocationsMap;
    private Set<MemoryLocation> locations;

    private Set<MethodSignature> methodsInvoked; //method invocation will be visited after


    ConstraintGenStmtVisitor(){
        this.varsToLocationsMap = new HashMap<Value,PointsToSet>();
        this.constraints= new HashSet<Constraint>();
        this.locations= new HashSet<MemoryLocation>();
        this.returnedLocationsMap = new HashMap<MethodSignature, PointsToSet>();
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
        AbstractInvokeExpr invokeExpr= stmt.getInvokeExpr();
       // MethodsInvoked.add(invokeExpr.getMethodSignature());

        JAssignStmt newAssignStmt;

        int i=0;                                //fix this, only for testing
        for(Value arg : stmt.getInvokeExpr().getArgs()) {
            LValue newLValue = new Local("argument" + i, invokeExpr.getMethodSignature().getParameterTypes().get(i));
             newAssignStmt=new JAssignStmt(newLValue, arg, stmt.getPositionInfo());
             newAssignStmt.accept(this);
            i++;
        }

        System.out.println("okokok");

    }
    @Override
    public void caseReturnStmt(@Nonnull JReturnStmt stmt) {
        defaultCaseStmt(stmt);
    }


    @Override
    public void caseAssignStmt(@Nonnull JAssignStmt stmt) {

        if( !(stmt.getLeftOp().getType() instanceof ReferenceType) ) return; //we re only interested in refs

        //malloc rule
        if(stmt.getRightOp() instanceof JNewExpr) {
            MemoryLocation l = new MemoryLocation(stmt.getPositionInfo().getStmtPosition().getFirstLine());
            constraints.add(new ElementOfConstraint(l,getOrCreateMappingOf(stmt.getLeftOp())));
            return;
        }

        //copy rule
        PointsToSet superset =getOrCreateMappingOf(stmt.getLeftOp());
        PointsToSet subset=getOrCreateMappingOf(stmt.getRightOp());
        constraints.add(new SubsetOfConstraint( subset, superset ));

    }

    @Override
    public void caseIdentityStmt(@Nonnull JIdentityStmt stmt) {
        defaultCaseStmt(stmt);  //figure out what happens with 'this' and function arguments initialization
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

    class constraintGenValueVisitor extends AbstractValueVisitor{


    }

}

