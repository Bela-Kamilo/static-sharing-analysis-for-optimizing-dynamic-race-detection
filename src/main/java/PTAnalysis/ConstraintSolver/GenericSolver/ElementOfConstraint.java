package PTAnalysis.ConstraintSolver.GenericSolver;

import PTAnalysis.ConstraintSolver.Constraint;

import java.util.Objects;
import java.util.Set;

/** element ∈ set */
public class ElementOfConstraint<T> implements GenericConstraint<T> {
    private final T element;
    private final Set<T> set;
    private final String setName;
   public ElementOfConstraint(T e , Set<T> set, String setName ) {
        this.set= set;
        this.element=e;
        this.setName= setName;
    }

    public String toString(){ return element +" is an element of "+setName;}

    public T getElement() {
        return element;
    }

    public Set<T> getSet() {
        return set;
    }

    public String getSetName() {
        return setName;
    }

    @Override
    public boolean equals(Object obj) {
       if(obj instanceof ElementOfConstraint<?>)
          return element.equals(((ElementOfConstraint<?>) obj).getElement()) && set == ((ElementOfConstraint<?>) obj).set;

       return false;
    }

    @Override
    public int hashCode() {
        return Objects.hash(element , set);
    }
}
