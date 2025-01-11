package PTAnalysis;


import java.util.HashSet;

/**
*a set of instances pointed at by a variable
*/
public class PointsToSet extends HashSet<MemoryLocation>{
    private final String varName;
    public Object constraintSolverSet;    //this is up to solver implementation to set and use
   // private final Set<Field> fieldSet;//
   public PointsToSet(String v){
        this.varName =v;
    //    fieldSet= new HashSet<>();//
    }

    public String getVarName() {
        return varName;
    }

    @Override
    public String toString() {
        return varName;
    }

    //a PointsToSet is equal only to itself
    public boolean equals(Object o){return this==o;}

    //let PointsToSet hash as a set though

    @Override
    public int hashCode() {
        return System.identityHashCode(this);
    }
}
