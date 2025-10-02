package SideEffects;

import GenericSolver.ElementOfConstraint;
import GenericSolver.GenericConstraint;
import GenericSolver.GenericSolver;
import PTAnalysis.AccessibleHeapLocation;
import PTAnalysis.PointsToAnalysis;
import PTAnalysis.PointsToSet;
import sootup.core.model.SootMethod;
import sootup.core.signatures.FieldSignature;
import sootup.core.signatures.MethodSignature;
import sootup.core.views.View;
import util.Tuple;

import java.util.*;

/**
 * Class to calculate every method's reads and writes
 * Uses results from {@link PointsToAnalysis}
 */
public class SideEffectsTracker {
    private final PointsToAnalysis PTA;
    private final Map<MethodSignature, Set<AccessibleHeapLocation>> READS;
    private final HashMap<MethodSignature, Set<AccessibleHeapLocation>> WRITES;
    private final Map<MethodSignature, Set<AccessibleHeapLocation>> RUN_METHOD_READS;
    private final HashMap<MethodSignature, Set<AccessibleHeapLocation>> RUN_METHOD_WRITES;
    private final SootMethod entryMethod;
    private boolean runMethodsSECollected;
    public SideEffectsTracker(View view , SootMethod entryMethod){
       this(entryMethod, new PointsToAnalysis(view));
    }

    public SideEffectsTracker(SootMethod entryMethod , PointsToAnalysis PTA){
        this.PTA = PTA;
        this.READS= new HashMap<>();
        this.WRITES = new HashMap<>();
        this.entryMethod=entryMethod;
        this.RUN_METHOD_READS= new LinkedHashMap<>();
        this.RUN_METHOD_WRITES= new LinkedHashMap<>();
        this.runMethodsSECollected=false;
    }

    private void analise(){
        PTA.analise(entryMethod);
        READS.putAll(PTA.getReadSets());
        WRITES.putAll(PTA.getWriteSets());
        Set<GenericConstraint<AccessibleHeapLocation>> SE_Constraints = new HashSet<>(PTA.getSEConstraints());
        Map<MethodSignature, Set<Tuple<PointsToSet, FieldSignature>>> fieldreads =PTA.getFieldsRead();
        Map<MethodSignature, Set<Tuple<PointsToSet, FieldSignature>>> fieldwrites =PTA.getFieldsWritten();
        //CONSTRUCT CONSTRAINTS
        //ugly , ik
        //                      SIDE EFFECT READ CONSTRAINTS
        //for every method
        for(Map.Entry<MethodSignature, Set<Tuple<PointsToSet,FieldSignature>>> entry : fieldreads.entrySet()){
            MethodSignature m = entry.getKey();
            Set<Tuple<PointsToSet,FieldSignature>> fieldAccesses = entry.getValue();
            //get field read accesses
            for(Tuple<PointsToSet,FieldSignature> fieldRead : fieldAccesses){
                FieldSignature field = fieldRead.getElem2();
                //for every possible Object Location that may be the base
                for( Integer l : fieldRead.getElem1()){
                    Set<AccessibleHeapLocation> readsOfm ;
                    if ( READS.containsKey(m) )
                        readsOfm =READS.get(m);
                    else {
                        readsOfm= new HashSet<>();
                        READS.put(m,readsOfm);
                    }
                    SE_Constraints.add( new ElementOfConstraint<>(new AccessibleHeapLocation(l,field), readsOfm , m.toString()+"._READS") );
                }
            }
        }
        //                      SIDE EFFECT WRITE CONSTRAINTS
        //for every method
        for(Map.Entry<MethodSignature, Set<Tuple<PointsToSet,FieldSignature>>> entry : fieldwrites.entrySet()){
            MethodSignature m = entry.getKey();
            Set<Tuple<PointsToSet,FieldSignature>> fieldAccesses = entry.getValue();
            //get field read accesses
            for(Tuple<PointsToSet,FieldSignature> fieldWrite : entry.getValue()){
                FieldSignature field = fieldWrite.getElem2();
                //for every possible Object Location that may be the base
                for( Integer l : fieldWrite.getElem1()){
                    Set<AccessibleHeapLocation> writesOfm ;
                    if ( WRITES.containsKey(m) )
                         writesOfm =WRITES.get(m);
                    else {
                         writesOfm= new HashSet<>();
                         WRITES.put(m, writesOfm);
                    }
                    SE_Constraints.add( new ElementOfConstraint<>(new AccessibleHeapLocation(l,field),  writesOfm , m.toString()+"._WRITES") );
                }
            }
        }

        //SOLVE THEM
        String className=entryMethod.toString().substring(1,entryMethod.toString().indexOf(':'));
        new GenericSolver<AccessibleHeapLocation>(SE_Constraints, className+" SideEffects").solve();
        return;
    }



    public Set< AccessibleHeapLocation> getReadsOf(MethodSignature method){
        if(!PTA.hasBeenPerformed()) analise();
        return Collections.unmodifiableSet(READS.get(method));

    }
    public Set<AccessibleHeapLocation> getWritesOf(MethodSignature method){
        if(!PTA.hasBeenPerformed()) analise();
        return Collections.unmodifiableSet(WRITES.get(method));
    }
    public Map<MethodSignature, Set<AccessibleHeapLocation>>  getReads(){
        if(!PTA.hasBeenPerformed()) analise();
        return Collections.unmodifiableMap(READS);
    }
    public Map<MethodSignature,Set<AccessibleHeapLocation>>  getWrites(){
        if(!PTA.hasBeenPerformed()) analise();
        return Collections.unmodifiableMap(WRITES);
    }

    public Map<MethodSignature,Set<AccessibleHeapLocation>> getThreadWrites(){
        if(!runMethodsSECollected) collectThreadSE();
        return RUN_METHOD_WRITES;
    }
    public Map<MethodSignature,Set<AccessibleHeapLocation>> getThreadReads(){
        if(!runMethodsSECollected) collectThreadSE();
        return RUN_METHOD_READS;
    }
    private void collectThreadSE(){
        if(runMethodsSECollected) return;
        String runMethodPattern = ".*void\\s+run\\s*\\(\\s*\\).*";
        String mainMethodPattern= ".*void\\s+main\\(java\\.lang\\.String\\[]\\).*";
        for(Map.Entry<MethodSignature ,Set<AccessibleHeapLocation>> mWrites : getWrites().entrySet()){
            if(mWrites.getKey().toString().matches(runMethodPattern+"|"+mainMethodPattern))
                RUN_METHOD_WRITES.put(mWrites.getKey(),mWrites.getValue());
        }
        for(Map.Entry<MethodSignature ,Set<AccessibleHeapLocation>> mReads : getReads().entrySet()){
            if(mReads.getKey().toString().matches(runMethodPattern+"|"+mainMethodPattern))
                RUN_METHOD_READS.put(mReads.getKey(),mReads.getValue());
        }
        runMethodsSECollected=true;
    }
}
