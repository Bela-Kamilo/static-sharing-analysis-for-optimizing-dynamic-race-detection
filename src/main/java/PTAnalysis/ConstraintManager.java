package PTAnalysis;

import GenericSolver.GenericConstraint;
import PTAnalysis.ConstraintSolver.Constraint;
import sootup.core.jimple.basic.Value;
import sootup.core.jimple.common.expr.AbstractInvokeExpr;
import sootup.core.jimple.common.ref.JArrayRef;
import sootup.core.jimple.common.ref.JInstanceFieldRef;
import sootup.core.jimple.common.ref.JStaticFieldRef;
import sootup.core.signatures.FieldSignature;
import sootup.core.signatures.MethodSignature;
import sootup.core.types.ReferenceType;
import sootup.core.types.Type;
import util.Tuple;

import java.util.*;

/**
 * Holds all Points-to and Side effects constraints and provides the necessary methods to work with them
 * intended use is ... TODO structure this so the accessibleHeap... maps make sense
 */
public class ConstraintManager {
    //PTA data structures
    private final Map<Value, PointsToSet> varsToLocationsMap;
    private final Map<MethodSignature, PointsToSet> returnedLocationsMap;
    private final Map<MethodSignature, Vector<PointsToSet>> parametersLocationsMap;
    private final Set<Constraint> PTAconstraints;
    private final int THIS_INDEX=0;
    //Side Effects data structures
    private final Set<GenericConstraint<AccessibleHeapLocation>> sideEffectsConstraints;

    //someMethod -> (PTSet , someField)
    private final Map<MethodSignature, Set<Tuple<PointsToSet, FieldSignature>>> fieldsRead;
    private final Map<MethodSignature, Set<Tuple<PointsToSet,FieldSignature>>> fieldsWritten;
    //someMethod-> 1.someField
    private final Map<MethodSignature,Set<AccessibleHeapLocation>> readSets;
    private final Map<MethodSignature,Set<AccessibleHeapLocation>> writeSets;

    public ConstraintManager(){
        this.PTAconstraints = new HashSet<>();
        this.parametersLocationsMap= new HashMap<>();
        this.returnedLocationsMap = new HashMap<>();
        this.fieldsRead= new HashMap<>();
        this.fieldsWritten= new HashMap<>();        // v maybe we could use IdentityMap for this
        this.varsToLocationsMap = new TreeMap<>(new Comparator<Value>() {       // we want to differentiate between same name locals
            @Override                                                           //of different methods
            public int compare(Value o1, Value o2) {
                if(o1==o2) return 0;
                if(o1 instanceof JStaticFieldRef && o2 instanceof JStaticFieldRef)//static field values however are different for the same field
                    return ((JStaticFieldRef) o1).getFieldSignature().compareTo(((JStaticFieldRef) o2).getFieldSignature());
                int r =o1.toString().compareTo(o2.toString());
                return r==0? 1 : r;
            }
        });
        this.sideEffectsConstraints= new HashSet<>();
        this.readSets= new HashMap<>();
        this.writeSets= new HashMap<>();
    }

    //                      PTA stuff
    //--------------------------------------------------------------------

    public void addPTA(Constraint c){
          this.PTAconstraints.add(c);
    }

    public void addSE(GenericConstraint<AccessibleHeapLocation> c){
        this.sideEffectsConstraints.add(c);
    }

    public Map<Value, PointsToSet> getVarsToLocationsMap() {
        return varsToLocationsMap;
    }
    public Set<Constraint> getPTAconstraints() {
        return Collections.unmodifiableSet(PTAconstraints);
    }

    /** value -> PTSet
     * A mapping of a value to its PTSet*/
    public PointsToSet getOrCreateMappingOf(Value v, MethodSignature visitingMethod){
        if(v instanceof AbstractInvokeExpr){
            return getOrCreateMappingOfMethod( ( (AbstractInvokeExpr) v).getMethodSignature());
        }
        Value v2;
        if(v instanceof JInstanceFieldRef) v2= ((JInstanceFieldRef) v).getBase();
        else if(v instanceof JArrayRef) v2= ((JArrayRef) v).getBase() ;
        else v2=v;
        if(varsToLocationsMap.containsKey(v2))
            return varsToLocationsMap.get(v2);
        String name = v2 instanceof JStaticFieldRef ? v2.toString() : visitingMethod +":"+v2;  //what else might be visible from outside visitingMethod?
        PointsToSet set = new PointsToSet(name);
        varsToLocationsMap.put(v2, set);
        return set;
    }

    /** method-> PTSet    returned locations
     * A mapping of a method to a PTSet of its possibly returned locations
     * */
    public PointsToSet getOrCreateMappingOfMethod(MethodSignature method){
        if(returnedLocationsMap.containsKey(method))
            return returnedLocationsMap.get(method);
        PointsToSet set = new PointsToSet(method.toString());
        returnedLocationsMap.put(method, set);
        return set;
    }

    /** method -> parametersPTSet
     * A mapping of a method to PTSets of its parameters
     * m.this index is 0
     * parameters index start from 1
     * */
    public PointsToSet getOrCreateMappingOf(MethodSignature method,int paramOrdinal){

        try {
            if (parametersLocationsMap.containsKey(method))
                return parametersLocationsMap.get(method).get(paramOrdinal);
        } catch (Exception e) {
            System.err.println("!failed to get "+method+" 's parameter"+paramOrdinal);
        }

        Vector<PointsToSet> paramVector= new Vector<>();
        paramVector.add(new PointsToSet(method+".this"));
        List<Type> types = method.getParameterTypes();
        for(int i=1; i< types.size()+1;i++) {
            if(types.get(i-1) instanceof ReferenceType)
                paramVector.add(new PointsToSet(method + "." + i));
            else
                paramVector.add(null);
        }
        parametersLocationsMap.put(method, paramVector);
        return paramVector.get(paramOrdinal);
    }

    public int getTHIS_INDEX() {
        return THIS_INDEX;
    }


    //                      Side effects stuff
    //--------------------------------------------------------------------


    public void methodReads(MethodSignature m, PointsToSet set, FieldSignature field){
        Set<Tuple<PointsToSet, FieldSignature>> readsOfm=fieldsRead.get(m);
        readsOfm.add(new Tuple<>(set,field));
    }

    public void methodWrites(MethodSignature m, PointsToSet set, FieldSignature field){
        Set<Tuple<PointsToSet, FieldSignature>> writesOfm=fieldsWritten.get(m);
        writesOfm.add(new Tuple<>(set,field));
    }

    /**
     * @throws IllegalStateException if it has already been called with the same {@param method}
     */
    public void initFieldsReadAndWritten(MethodSignature method){
       ;
        if(fieldsRead.containsKey(method))
            throw new IllegalStateException("Initialization of fields read by "+method+" has already happened");
        if(fieldsWritten.containsKey(method))
            throw new IllegalStateException("Initialization of fields written by "+method+" has already happened");
        this.fieldsRead.put(method, new HashSet<>());
        this.fieldsWritten.put(method, new HashSet<>());
    }


    public Set<AccessibleHeapLocation> getOrCreateMethodReadSet(MethodSignature m){
        if(readSets.containsKey(m)) return readSets.get(m);
        Set<AccessibleHeapLocation> toBe = new HashSet<>();
        readSets.put(m,toBe);
        return toBe;
    }

    public Set<AccessibleHeapLocation> getOrCreateMethodWriteSet(MethodSignature m){
        if(writeSets.containsKey(m)) return writeSets.get(m);
        Set<AccessibleHeapLocation> toBe = new HashSet<>();
        writeSets.put(m,toBe);
        return toBe;
    }

    public Map<MethodSignature, Set<AccessibleHeapLocation>> getReadSets() {
        return Collections.unmodifiableMap(readSets);
    }

    public Map<MethodSignature, Set<AccessibleHeapLocation>> getWriteSets() {
        return Collections.unmodifiableMap(writeSets);
    }

    public Map<MethodSignature, Set<Tuple<PointsToSet, FieldSignature>>> getFieldsRead() {
        return Collections.unmodifiableMap(fieldsRead);
    }

    public Map<MethodSignature, Set<Tuple<PointsToSet, FieldSignature>>> getFieldsWritten() {
        return Collections.unmodifiableMap(fieldsWritten);
    }

    public Set<GenericConstraint<AccessibleHeapLocation>> getSEConstraints(){
        return Collections.unmodifiableSet(sideEffectsConstraints);
    }

    public Set<AccessibleHeapLocation> getWritesOf(MethodSignature m){
        Set<AccessibleHeapLocation> res= new HashSet<>();

        Set<Tuple<PointsToSet,FieldSignature>> PTSetsAndFields =fieldsWritten.get(m);
        for(Tuple<PointsToSet,FieldSignature> PTSetAndField : PTSetsAndFields)
            for(int i : PTSetAndField.getElem1())
                res.add(new AccessibleHeapLocation(i,PTSetAndField.getElem2()));

        return res;
    }

    public Set<AccessibleHeapLocation> getReadsOf(MethodSignature m){
        Set<AccessibleHeapLocation> res= new HashSet<>();

        Set<Tuple<PointsToSet,FieldSignature>> PTSetsAndFields =fieldsRead.get(m);
        for(Tuple<PointsToSet,FieldSignature> PTSetAndField : PTSetsAndFields)
            for(int i : PTSetAndField.getElem1())
                res.add(new AccessibleHeapLocation(i,PTSetAndField.getElem2()));

        return res;
    }

    // maybe store this in the future so we dont recompute it each time we call getWrites or getReads

    public Map<MethodSignature,Set<AccessibleHeapLocation>> getWrites(){
        Map<MethodSignature, Set<AccessibleHeapLocation>> res= new HashMap<>();
        fieldsWritten.forEach((method,_)->{res.put(method,getWritesOf(method));});

        return res;
    }

    public Map<MethodSignature,Set<AccessibleHeapLocation>> getReads(){
        Map<MethodSignature, Set<AccessibleHeapLocation>> res= new HashMap<>();
        fieldsRead.forEach((method,_)->{res.put(method,getReadsOf(method));});

        return res;
    }


}
