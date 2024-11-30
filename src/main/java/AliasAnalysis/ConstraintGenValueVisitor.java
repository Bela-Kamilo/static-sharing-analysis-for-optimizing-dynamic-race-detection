package AliasAnalysis;

import sootup.core.jimple.common.expr.JNewExpr;
import sootup.core.jimple.visitor.AbstractValueVisitor;

import javax.annotation.Nonnull;
import java.util.HashSet;
import java.util.Set;

public class ConstraintGenValueVisitor extends AbstractValueVisitor {
    Set<Constraint> Constraints;

    ConstraintGenValueVisitor(Set<Constraint> Constraints){
        this.Constraints = Constraints;
    }

    @Override
    public void caseNewExpr(@Nonnull JNewExpr expr) {
        defaultCaseExpr(expr);
    }

}
