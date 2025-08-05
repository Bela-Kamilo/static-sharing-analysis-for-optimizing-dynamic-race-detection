package PTAnalysis;

import PTAnalysis.ConstraintSolver.Constraint;

import java.util.Objects;
import java.util.Optional;

/** superset ⊇ subset
 * superset.superSetField ⊇ subset
 * superset ⊇ subset.subSetField
 * superset.superSetField ⊇ subset.subSetField
 * */
public class PTASupersetOfConstraint implements Constraint {
    private final PointsToSet superSet;
    private final PointsToSet subSet;
    private final String superSetField;
    private final String subSetField;

    public PTASupersetOfConstraint(PointsToSet SuperSet, PointsToSet SubSet){
        this(SuperSet, null, SubSet, null);
    }
    public PTASupersetOfConstraint(PointsToSet SuperSet, String superSetField, PointsToSet SubSet){
        this(SuperSet, superSetField, SubSet, null);
    }
    public PTASupersetOfConstraint(PointsToSet SuperSet, PointsToSet SubSet, String subSetField){
        this(SuperSet, null, SubSet, subSetField);
    }
    public PTASupersetOfConstraint(PointsToSet SuperSet, String superSetField, PointsToSet SubSet, String subSetField){
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
    public PTASuperSetOfConstraintType getSuperSetConstraintType(){
        if(getSuperSetField().isPresent()){
            return getSubSetField().isPresent()?
                    PTASuperSetOfConstraintType.FIELD_SUPERSETOF_FIELD
                    : PTASuperSetOfConstraintType.SUPERSET_FIELD;
        }
        return getSubSetField().isPresent()?
                PTASuperSetOfConstraintType.SUBSET_FIELD
                : PTASuperSetOfConstraintType.FIELDLESS;
    }
    @Override
    public boolean equals(Object obj) {
        if (obj instanceof PTASupersetOfConstraint)
            return subSet.equals(((PTASupersetOfConstraint)obj).getSubSet())
                    && superSet.equals(((PTASupersetOfConstraint)obj).getSuperSet())
                    && getSubSetField().equals(((PTASupersetOfConstraint)obj).getSubSetField())
                    && getSuperSetField().equals(((PTASupersetOfConstraint)obj).getSuperSetField());
        return false;
    }

    @Override
    public int hashCode() {
        return Objects.hash(superSet, subSet);
    }
}
