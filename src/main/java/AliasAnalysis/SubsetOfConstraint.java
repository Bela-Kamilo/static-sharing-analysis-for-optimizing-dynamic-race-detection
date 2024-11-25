package AliasAnalysis;

import sootup.core.types.ReferenceType;

public class SubsetOfConstraint {
    private ReferenceType SuperSet;
    private ReferenceType SubSet;
    SubsetOfConstraint(ReferenceType SubSet , ReferenceType SuperSet){
        this.SuperSet=SuperSet;
        this.SubSet=SubSet;
    }
}
