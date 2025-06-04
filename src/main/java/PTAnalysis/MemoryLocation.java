package PTAnalysis;


import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

/** represents the object instances any ref variable may point to */
public class MemoryLocation {
    static private int  locationCounter=1;  //to be incremented each time
                                            // there s a new object created
                                            //will be used as the id of each MemoryLocation instance
    final private int lineNumber;
    final private int id;
    final private Map<String,PointsToSet> refFields;

    MemoryLocation(int lineNumber){
        this.id=locationCounter++;
        this.lineNumber=lineNumber;
        this.refFields= new HashMap<>();
    }

    public int getId() {return id;}

    @Override
    public String toString() {
        return "m"+lineNumber+" ("+id+")";
    }
    public static int getLocationCounter(){return MemoryLocation.locationCounter-1;}

    public void setField(String field , PointsToSet fieldPTSet){
        refFields.put(field, fieldPTSet);
    }
    public PointsToSet getField(String field){
        return refFields.get(field);
    }
    /**@return true if this memory location holds field 'field'
     * @return false otherwise
     * */
    public boolean existsField(String field){ return refFields.containsKey(field);}
    public Iterable<PointsToSet> getAllFields(){return refFields.values();}
    public  static void reset(){MemoryLocation.locationCounter=1;}      //only call when PTAnalysis is over
}
