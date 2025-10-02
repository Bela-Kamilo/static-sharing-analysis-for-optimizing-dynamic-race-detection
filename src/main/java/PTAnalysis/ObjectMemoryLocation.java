package PTAnalysis;


import java.util.HashMap;
import java.util.Map;

/** represents the object instances any ref variable may point to */
public class ObjectMemoryLocation {
    static private int  locationCounter=1;  //to be incremented each time
                                            // there s a new object created
                                            //will be used as the id of each ObjectMemoryLocation instance
    final private int lineNumber;
    final private int id;
    final private Map<String,PointsToSet> refFields;

    public ObjectMemoryLocation(int lineNumber){
        this.id=locationCounter++;
        this.lineNumber=lineNumber;
        this.refFields= new HashMap<>();
    }

    public int getId() {return id;}

    @Override
    public String toString() {
        return "m"+lineNumber+" ("+id+")";
    }
    public static int getLocationCounter(){return ObjectMemoryLocation.locationCounter-1;}

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
    public  static void reset(){
        ObjectMemoryLocation.locationCounter=1;}      //only call when PTAnalysis is over
}
