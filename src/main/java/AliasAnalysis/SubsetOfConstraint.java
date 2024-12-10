package AliasAnalysis;

import java.util.Objects;

public class SubsetOfConstraint implements  Constraint{
    private final PointsToSet superSet;
    private final PointsToSet subSet;
    SubsetOfConstraint(PointsToSet SubSet , PointsToSet SuperSet){
        this.superSet =SuperSet;
        this.subSet =SubSet;
    }

    public String toString(){ return subSet.getVar() +" is a subset of "+ superSet.getVar();}

    public PointsToSet getSubSet() {
        return subSet;
    }

    public PointsToSet getSuperSet() {
        return superSet;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj instanceof SubsetOfConstraint)
            return subSet.equals(((SubsetOfConstraint)obj).getSubSet())
                    && superSet.equals(((SubsetOfConstraint)obj).getSuperSet());
        return false;
    }

    @Override
    public int hashCode() {
        return Objects.hash(superSet, subSet);
    }
}
