package PTAnalysis;

import GenericSolver.GenericConstraint;
import PTAnalysis.ConstraintSolver.Constraint;
import PTAnalysis.ConstraintSolver.Solver;
import RuleApplicator.RuleApplicatorStmtVisitor;
import sootup.core.jimple.common.stmt.Stmt;
import sootup.core.model.SootMethod;
import sootup.core.signatures.FieldSignature;
import sootup.core.signatures.MethodSignature;
import sootup.core.types.Type;
import sootup.core.views.View;
import sootup.java.core.views.JavaView;
import util.LoggerFactory;
import util.SootUpStuff;
import util.Tuple;

import java.util.*;
import java.util.logging.Logger;

/** This class implements a field sensitive Andersen's-like points to analysis
 *  on jimple code. We do not need to deal with dereferencing and such here.
 *  There s a PointsToSet for every reference local, non-void method, method parameter
 *  and field (of reference type) possibly held by an instance.
 *  Assignment statements and method invocations generate constraints on the PointsToSets.
 *  Every statement (and method) is visited once
*/
public class PointsToAnalysis {
    RuleApplicatorStmtVisitor ConstraintGenerator;
    Set <MethodSignature> visitedMethods;
    View view;
    private final Logger constraintLogger;
    private boolean hasBeenPerformed=false;
    public PointsToAnalysis(View view){
        constraintLogger= new LoggerFactory().createLogger("logs/ConstraintGeneration/","ConstraintGeneration");
        this.ConstraintGenerator = new RuleApplicatorStmtVisitor();
        this.visitedMethods = new HashSet<>();
        this.view=view;
    }

    /** performs the analysis on reachable code from entryMethod
     * @return A mapping of jimple value holders to sets of memory locations representing allocated objects
     * value holders are in the form of :
     *
     * {@literal <Class: MethodSignature>:local   -   for locals } <br>
     * {@literal <Class: StaticField>             -   for static fields } <br>
     * {@literal X.<Class: Type field>            -   for instance fields, where X is an integer representing
     *                                                  an abstract memory location of an object
     * } <br
     * {@literal <Class: MethodSignature>         -   for the possible locations the method might return} <br>
     *}
     */
    public Map<String, PointsToSet> analise(SootMethod entryMethod){
        GenerateConstraints(entryMethod);
        Solver solver= new Solver(this.ConstraintGenerator.getPTAconstraints());
        Map<String,PointsToSet> res= solver.solve();
        hasBeenPerformed=true;
        //SOLVE FOR SIDE EFFECTS
        ObjectMemoryLocation.reset();
        return res;
    }

    /** passes all methods reachable from entryMethod.
    * We go over each method only once
    */
     private void GenerateConstraints(SootMethod entryMethod){
         SootMethod nextMethod=null;
        //pass entry method
        generateConstraintsForSingleMethod(entryMethod);
        //pass every other method
        Queue<MethodSignature> everyOtherMethod= new LinkedList<>(ConstraintGenerator.getMethodsInvoked());
        while(!everyOtherMethod.isEmpty()){
            MethodSignature method = everyOtherMethod.remove();
            //if ( visitedMethods.contains(method)) continue;
            if ( Type.isObjectLikeType(method.getDeclClassType()) || visitedMethods.contains(method)) continue;

           // Optional<? extends SootMethod> opt = view.getMethod(method);
            //if(!opt.isPresent()) { System.err.println("!Coulnt get SootMethod of "+ method+"!"); continue;}
            nextMethod= SootUpStuff.getMethodFromView((JavaView) view,method);
            if(nextMethod==null) continue;
            generateConstraintsForSingleMethod(nextMethod);
            //generateConstraintsForSingleMethod(opt.get());
            //note every other method to be passed over
            ConstraintGenerator.getMethodsInvoked().stream().
                        filter(m -> !visitedMethods.contains(m)).
                        forEach(everyOtherMethod::add);

        }
        LoggerFactory.closeHandlerls(constraintLogger);
    }
    /** passes a single method, notes other visited methods */
    public void generateConstraintsForSingleMethod(SootMethod method){
        constraintLogger.info("+++Visiting "+ method+"+++");
        constraintLogger.info(method.getBody().toString());
        constraintLogger.info("+++++++++++++++");
        ConstraintGenerator.setVisitingMethod(method.getSignature());
        for (Stmt stmt : method.getBody().getStmts()) {
            stmt.accept( ConstraintGenerator);
        }
        visitedMethods.add(method.getSignature());
        PrintConstraintsToLog();
        constraintLogger.info("------------\nMethods invoked:\n"+ConstraintGenerator.getMethodsInvoked());
        constraintLogger.info("------------");
    }

    public void PrintConstraintsToLog(){
        constraintLogger.info("---------\nPTConstraints:");
        int i=1;
        for (Constraint c : ConstraintGenerator.getPTAconstraints() )
            constraintLogger.info((i++) + " "+ c);
        constraintLogger.info("---------");
        constraintLogger.info("---------\nSEConstraints:");
        i=1;
        for (GenericConstraint c : ConstraintGenerator.getSEConstraints() )
            constraintLogger.info((i++) + " "+ c);
        constraintLogger.info("---------");
    }

    public Set<GenericConstraint<AccessibleHeapLocation>> getSEConstraints(){return ConstraintGenerator.getSEConstraints();}

    public Map<MethodSignature , Set<Tuple<PointsToSet, FieldSignature>>> getFieldsRead(){
        if(!hasBeenPerformed) throw new IllegalStateException("analise() need first be called to yield results");
        return ConstraintGenerator.getFieldsRead();
    }

    public Map<MethodSignature , Set<Tuple<PointsToSet, FieldSignature>>> getFieldsWritten(){
        if(!hasBeenPerformed) throw new IllegalStateException("analise() need first be called to yield results");
       return ConstraintGenerator.getFieldsWritten();
    }

    public Map<MethodSignature,Set<AccessibleHeapLocation>>  getReads(){
        if(!hasBeenPerformed) throw new IllegalStateException("analise() need first be called to yield results");
        return ConstraintGenerator.getReads();
    }
    public Map<MethodSignature,Set<AccessibleHeapLocation>>  getWrites(){
        if(!hasBeenPerformed) throw new IllegalStateException("analise() need first be called to yield results");
        return ConstraintGenerator.getWrites();
    }
    public Map<MethodSignature , Set<AccessibleHeapLocation>> getReadSets(){
        return ConstraintGenerator.getReadSets();
    }
    public Map<MethodSignature , Set<AccessibleHeapLocation>> getWriteSets(){
        return ConstraintGenerator.getWriteSets();
    }

public boolean hasBeenPerformed(){return hasBeenPerformed;}

}
