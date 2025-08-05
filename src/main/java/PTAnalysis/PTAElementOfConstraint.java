package PTAnalysis;
/** element ∈ set */
public class ElementOfConstraint implements Constraint{
    private final ObjectMemoryLocation element;
    private final PointsToSet set;
   public  ElementOfConstraint(ObjectMemoryLocation e , PointsToSet set ) {
        this.set= set;
        this.element=e;
    }

    public String toString(){ return element +" is an element of "+ set.getVarName();}

    public ObjectMemoryLocation getElement() {
        return element;
    }

    public PointsToSet getSet() {
        return set;
    }
}
