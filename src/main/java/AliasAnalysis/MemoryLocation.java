package AliasAnalysis;

import AliasAnalysis.ConstraintSolver.SolverElement;

//represents the object instances any variable may point to
public class MemoryLocation {
    static private int  locationCounter=1;  //to be incremented each time
                                            // there s a new object created
                                            //will be used as the id of each MemoryLocation instance
    final private int lineNumber;
    final private int id;
    public SolverElement<?> constraintSolverElement;


    MemoryLocation(int lineNumber){
        this.id=locationCounter++;
        this.lineNumber=lineNumber;
        this.constraintSolverElement=null;
    }

    public int getId() {
        return id;
    }

    @Override
    public String toString() {
        return "m"+lineNumber+" ("+id+")";
    }
    public static int getLocationCounter(){return MemoryLocation.locationCounter;}

}
