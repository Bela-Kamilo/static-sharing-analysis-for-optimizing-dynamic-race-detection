package PTAnalysis;

import java.util.Objects;
import java.util.Optional;

public class SupersetOfConstraint implements  Constraint{
    private final PointsToSet superSet;
    private final PointsToSet subSet;
    private final String superSetField;
    private final String subSetField;

    public SupersetOfConstraint(PointsToSet SuperSet, PointsToSet SubSet){
        this(SuperSet, null, SubSet, null);
    }
    public SupersetOfConstraint(PointsToSet SuperSet, String superSetField, PointsToSet SubSet){
        this(SuperSet, superSetField, SubSet, null);
    }
    public SupersetOfConstraint(PointsToSet SuperSet, PointsToSet SubSet, String subSetField){
        this(SuperSet, null, SubSet, subSetField);
    }
    public SupersetOfConstraint(PointsToSet SuperSet, String superSetField, PointsToSet SubSet, String subSetField){
        this.superSet =SuperSet;
        this.subSet =SubSet;
        this.subSetField=subSetField ;
        this.superSetField=superSetField;
    }


    public String toString(){
       String toreturn = superSet.getVarName().toString();
       if( superSetField!=null) toreturn+= "."+superSetField;
       toreturn+=" is a superset of "+ subSet.getVarName();
        if( subSetField!=null) toreturn+= "."+subSetField;
       return toreturn;
    }

    public PointsToSet getSubSet() {
        return subSet;
    }

    public PointsToSet getSuperSet() {
        return superSet;
    }
    public Optional<String> getSubSetField() {
        return subSetField == null? Optional.empty() : Optional.of(subSetField);
    }

    public Optional<String> getSuperSetField() {
        return superSetField == null? Optional.empty() : Optional.of(superSetField);
    }
    public SuperSetConstraintType getSuperSetConstraintType(){
        if(getSuperSetField().isPresent()){
            return getSubSetField().isPresent()?
                    SuperSetConstraintType.FIELD_SUPERSET_OF_FIELD
                    : SuperSetConstraintType.SUPERSET_FIELD;
        }
        return getSubSetField().isPresent()?
                SuperSetConstraintType.SUBSET_FIELD
                : SuperSetConstraintType.FIELDLESS;
    }
    @Override
    public boolean equals(Object obj) {
        if (obj instanceof SupersetOfConstraint)
            return subSet.equals(((SupersetOfConstraint)obj).getSubSet())
                    && superSet.equals(((SupersetOfConstraint)obj).getSuperSet())
                    && getSubSetField().equals(((SupersetOfConstraint)obj).getSubSetField())
                    && getSuperSetField().equals(((SupersetOfConstraint)obj).getSuperSetField());
        return false;
    }

    @Override
    public int hashCode() {
        return Objects.hash(superSet, subSet);
    }
}
