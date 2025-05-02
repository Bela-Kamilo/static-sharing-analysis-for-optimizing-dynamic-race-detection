package PTAnalysis;


import java.awt.*;
import java.util.HashSet;

/**
*a set of instances pointed at by a variable (PTSet)
*/
public class PointsToSet extends HashSet<Integer>{
    private final String varName;
    /** up to solver implementation to set and use */
    public Object constraintSolverSet;

   public PointsToSet(String v){
        this.varName =v;

    }

   public String getVarName() {return varName;}

    @Override
//   public String toString() {return varName;}

    /** a PointsToSet is equal only to itself */
    public boolean equals(Object o){return this==o;}
   /** a PointsToSet is equivalent to another if they contain exactly the same elements*/
    public boolean isEquiv(PointsToSet p){return super.equals(p);}

    @Override
    public int hashCode() {
        return System.identityHashCode(this);
    }


}
