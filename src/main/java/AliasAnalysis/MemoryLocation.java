package AliasAnalysis;

//represents the object instances any variable may point to
public class MemoryLocation {
    static private long  locationCounter=1;  //to be incremented each time
                                            // there s a new object created
                                            //will be used the id of each MemoryLocation instance
    final private int lineNumber;
    final private long id;

    MemoryLocation(int lineNumber){
        this.id=locationCounter++;
        this.lineNumber=lineNumber;
    }

    @Override
    public String toString() {
        return "m"+lineNumber+" ("+id+")";
    }
}
