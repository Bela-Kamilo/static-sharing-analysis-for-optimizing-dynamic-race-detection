package AliasAnalysis;

import sootup.core.jimple.visitor.AbstractValueVisitor;

import java.util.HashSet;
import java.util.Set;

public class ConstraintGenValueVisitor extends AbstractValueVisitor {
    Set<Constraint> Constraints;

    ConstraintGenValueVisitor(Set<Constraint> Constraints){
        this.Constraints = Constraints;
    }

}
