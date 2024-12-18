package AliasAnalysis;

import sootup.core.jimple.basic.LValue;
import sootup.core.jimple.basic.Local;
import sootup.core.jimple.basic.StmtPositionInfo;
import sootup.core.jimple.basic.Value;
import sootup.core.jimple.common.expr.*;
import sootup.core.jimple.common.ref.JParameterRef;
import sootup.core.jimple.common.stmt.JAssignStmt;
import sootup.core.jimple.common.stmt.JIdentityStmt;
import sootup.core.jimple.common.stmt.JInvokeStmt;
import sootup.core.jimple.common.stmt.JReturnStmt;
import sootup.core.jimple.javabytecode.stmt.JRetStmt;
import sootup.core.jimple.visitor.AbstractStmtVisitor;
import sootup.core.jimple.visitor.AbstractValueVisitor;
import sootup.core.signatures.MethodSignature;
import sootup.core.types.ReferenceType;

import javax.annotation.Nonnull;
import java.awt.*;
import java.util.*;

//visits a statement, generates constraints
public class ConstraintGenStmtVisitor extends AbstractStmtVisitor {

    private Map<Value,PointsToSet> varsToLocationsMap;
    private Map<MethodSignature, PointsToSet> returnedLocationsMap;
    private Map<MethodSignature, Vector<PointsToSet>> parametersLocationsMap;
    private Set<Constraint> constraints;

    private Set<MethodSignature> methodsInvoked; //method invocation will be visited after
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

    //<< f(a1,a2...an); >> treat as f.args = ai and add f in MethodsInvoked
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
            subset=getOrCreateMappingOf(visitingMethod,((JParameterRef) rightOp).getIndex());
            superset=getOrCreateMappingOf(leftOp);
            constraints.add(new SubsetOfConstraint( subset, superset ));
            return;
        }

        //figure out what happens with 'this' identity
        return;
    }


    // treat as f(...) = stmt.getOp()
    @Override
    public void caseReturnStmt(@Nonnull JReturnStmt stmt) {
        if( !(stmt.getOp().getType() instanceof ReferenceType) ) return;

        PointsToSet superset =getOrCreateMappingOf(visitingMethod);
        PointsToSet subset=getOrCreateMappingOf(stmt.getOp());
        constraints.add(new SubsetOfConstraint( subset, superset ));
    }
    @Override
    public void caseRetStmt(@Nonnull JRetStmt stmt){System.out.println("VISITED A RET STATEMENT");}
    

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


        if(rightOp instanceof AbstractInvokeExpr) {
            rightOp.accept(new ConstraintGenInvokeVisitor());

            PointsToSet superset =getOrCreateMappingOf(leftOp);
            PointsToSet subset=getOrCreateMappingOf(((AbstractInvokeExpr) rightOp).getMethodSignature());
            constraints.add(new SubsetOfConstraint( subset, superset ));
            return;
        }

        //copy rule
        PointsToSet superset =getOrCreateMappingOf(leftOp);
        PointsToSet subset=getOrCreateMappingOf(rightOp);
        constraints.add(new SubsetOfConstraint( subset, superset ));

    }



    // value -> PTSet
    private PointsToSet getOrCreateMappingOf(Value v){
        if(varsToLocationsMap.containsKey(v))
            return varsToLocationsMap.get(v);
        PointsToSet set = new PointsToSet(visitingMethod.getName() +":"+v);
        varsToLocationsMap.put(v, set);
        return set;
    }
    //method-> PTSet    ,returned locations
    private PointsToSet getOrCreateMappingOf(MethodSignature method){
        if(returnedLocationsMap.containsKey(method))
            return returnedLocationsMap.get(method);
        PointsToSet set = new PointsToSet(method.toString());
        returnedLocationsMap.put(method, set);
        return set;
    }

    //method -> parametersPTSet
    private PointsToSet getOrCreateMappingOf(MethodSignature method,int paramOrdinal){

        try {
            if (parametersLocationsMap.containsKey(method))
                return parametersLocationsMap.get(method).get(paramOrdinal);
        } catch (Exception e) {
            System.out.println("!failed to get "+method+" 's parameter"+paramOrdinal);
        }

        Vector<PointsToSet> paramVector= new Vector<>();
        for(int i=0; i< method.getParameterTypes().size();i++)
            paramVector.add(new PointsToSet(method+"."+i));
        parametersLocationsMap.put(method, paramVector);
        return paramVector.get(paramOrdinal);
    }
    public void setVisitingMethod(MethodSignature method){visitingMethod=method;}


    //generates constraints for a single value
    //we only need this class for invocations
     class ConstraintGenInvokeVisitor extends AbstractValueVisitor{


        //method invocations
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

             methodsInvoked.add(invokeExpr.getMethodSignature());

            int i=0;
            for(Value arg : invokeExpr.getArgs()) {
                PointsToSet superset =getOrCreateMappingOf(invokeExpr.getMethodSignature(), i);
                PointsToSet subset=getOrCreateMappingOf(arg);
                constraints.add(new SubsetOfConstraint( subset, superset ));
                i++;
            }
        }

    }

}

