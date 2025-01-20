package PTAnalysis;


import java.util.HashSet;

/**
*a set of instances pointed at by a variable (PTSet)
*/
public class PointsToSet extends HashSet<MemoryLocation>{
    private final String varName;
    /** up to solver implementation to set and use */
    public Object constraintSolverSet;

   public PointsToSet(String v){
        this.varName =v;

    }

    public String getVarName() {
        return varName;
    }

    @Override
    public String toString() {
        return varName;
    }

    /** a PointsToSet is equal only to itself */
    public boolean equals(Object o){return this==o;}


    @Override
    public int hashCode() {
        return System.identityHashCode(this);
    }
}
