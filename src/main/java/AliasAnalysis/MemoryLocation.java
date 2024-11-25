package AliasAnalysis;

//represents the object instances any variable may point to
public class MemoryLocation {
    static private int locationCounter=0;  //to be incremented each time
                                            // there s a new object created
                                            //will be used the id of each MemoryLocation instance
    final private int lineNumber;
    final private int id;

    MemoryLocation(int lineNumber){
        this.id=locationCounter++;
        this.lineNumber=lineNumber;
    }

}
