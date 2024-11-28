package AliasAnalysis;

import sootup.core.jimple.basic.Value;
import sootup.core.types.ReferenceType;

public class SubsetOfConstraint implements  Constraint{
    private final Value SuperSet;
    private final Value SubSet;
    SubsetOfConstraint(Value SubSet , Value SuperSet){
        this.SuperSet=SuperSet;
        this.SubSet=SubSet;
    }

    public String toString(){ return SubSet +" is a subset of "+ SuperSet;}

    public Value getSubSet() {
        return SubSet;
    }

    public Value getSuperSet() {
        return SuperSet;
    }
}
