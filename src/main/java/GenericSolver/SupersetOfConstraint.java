package PTAnalysis.ConstraintSolver.GenericSolver;

import PTAnalysis.ConstraintSolver.Constraint;

import java.util.Objects;
import java.util.Set;

/** superset ⊇ subset
 * */
public class SupersetOfConstraint<T> implements GenericConstraint<T> {
    private final Set<T> superSet;
    private final Set<T> subSet;
    private final String superSetName;
    private final String subSetName;

    public SupersetOfConstraint(Set<T> SuperSet,String superSetName, Set<T> SubSet ,String subSetName){
        this.superSet =SuperSet;
        this.subSet =SubSet;
        this.superSetName=superSetName;
        this.subSetName=subSetName;
    }


    public String toString(){
      return superSetName+" is a superset of "+ subSetName;
    }

    public Set<T> getSubSet() {
        return subSet;
    }
    public Set<T> getSuperSet() {
        return superSet;
    }
    public String getSubSetName() {
        return subSetName;
    }
    public String getSuperSetName() {
        return superSetName;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj instanceof SupersetOfConstraint)
            return subSet ==((SupersetOfConstraint<?>)obj).getSubSet()
                    && superSet ==((SupersetOfConstraint<?>)obj).getSuperSet();
       return false;
    }

    @Override
    public int hashCode() {
        return Objects.hash(superSet, subSet);
    }
}
