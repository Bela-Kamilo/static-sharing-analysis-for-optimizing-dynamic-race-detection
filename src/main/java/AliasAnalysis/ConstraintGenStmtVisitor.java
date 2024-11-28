package AliasAnalysis;

import sootup.core.jimple.common.stmt.JAssignStmt;
import sootup.core.jimple.common.stmt.JIdentityStmt;
import sootup.core.jimple.common.stmt.JInvokeStmt;
import sootup.core.jimple.common.stmt.JReturnStmt;
import sootup.core.jimple.javabytecode.stmt.JRetStmt;
import sootup.core.jimple.visitor.AbstractStmtVisitor;
import sootup.core.model.SootMethod;

import javax.annotation.Nonnull;
import java.util.Set;

//visits a statement, generates constraints
public class ConstraintGenVisitor extends AbstractStmtVisitor {
    Set<SootMethod> MethodsInvoked; //if there s a method invocation it will need to be
                                    //visited after
    Set<Constraint> Constraints;
    @Override
    public void caseInvokeStmt(@Nonnull JInvokeStmt stmt) {
        return;     // treat as args = args and then we ll do another pass on invoked methods
    }
    @Override
    public void caseReturnStmt(@Nonnull JReturnStmt stmt) {
        defaultCaseStmt(stmt);
    }


    @Override
    public void caseAssignStmt(@Nonnull JAssignStmt stmt) {
        defaultCaseStmt(stmt);
    }

    @Override
    public void caseIdentityStmt(@Nonnull JIdentityStmt stmt) {
        defaultCaseStmt(stmt);  //figure out what happens with 'this'
    }


}

