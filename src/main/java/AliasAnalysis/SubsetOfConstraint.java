package AliasAnalysis;

import sootup.core.jimple.basic.Value;
import sootup.core.types.ReferenceType;

public class SubsetOfConstraint implements  Constraint{
    private final PointsToSet SuperSet;
    private final PointsToSet SubSet;
    SubsetOfConstraint(PointsToSet SubSet , PointsToSet SuperSet){
        this.SuperSet=SuperSet;
        this.SubSet=SubSet;
    }

    public String toString(){ return SubSet.getVar() +" is a subset of "+ SuperSet.getVar();}

    public PointsToSet getSubSet() {
        return SubSet;
    }

    public PointsToSet getSuperSet() {
        return SuperSet;
    }
}
