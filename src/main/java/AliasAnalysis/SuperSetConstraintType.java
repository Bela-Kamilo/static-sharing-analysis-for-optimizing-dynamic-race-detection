package AliasAnalysis;

public enum SuperSetConstraintType {
    FIELDLESS,
    SUPERSET_FIELD,
    SUBSET_FIELD,
    FIELD_SUPERSET_OF_FIELD     //this is not legal jimple i believe
}
