package PTAnalysis;

import GenericSolver.GenericConstraint;
import PTAnalysis.ConstraintSolver.Constraint;
import PTAnalysis.ConstraintSolver.Solver;
import RuleApplicator.RuleApplicatorGlobal;
import RuleApplicator.RuleApplicatorStmtVisitor;
import sootup.core.jimple.common.stmt.Stmt;
import sootup.core.model.SootMethod;
import sootup.core.signatures.FieldSignature;
import sootup.core.signatures.MethodSignature;
import sootup.core.views.View;
import sootup.java.core.views.JavaView;
import util.Logging.LeveledLogger;
import util.Logging.LogDetailLevel;
import util.Logging.LoggerFactory;
import util.SootUpStuff;
import util.Tuple;

import java.util.*;

import static util.SootUpStuff.gatherImplementationsOf;

/** This class implements a field sensitive Andersen's-like points to analysis
 *  on jimple code. We do not need to deal with dereferencing and such here.
 *  There s a PointsToSet for every reference local, non-void method, method parameter
 *  and field (of reference type) possibly held by an instance.
 *  Assignment statements and method invocations generate constraints on the PointsToSets.
 *  Every statement (and method) is visited once
*/
public class PointsToAnalysis {
    private final ConstraintManager constraintManager;
    private final RuleApplicatorStmtVisitor constraintGeneratorStmt;
    private final Set <MethodSignature> visitedMethods;
    private final RuleApplicatorGlobal constraintGeneratorGlobal;
    private final View view;
    private final LeveledLogger constraintLogger;
    private boolean hasBeenPerformed=false;
    private final String name;
    private final LogDetailLevel logDetailLevel;

    //public PointsToAnalysis(View view, String analysisName){this(view, analysisName,LogDetailLevel.LOW);}

    public PointsToAnalysis(View view, String analysisName, LogDetailLevel logDetailLevel){
        this.logDetailLevel=logDetailLevel;
        this.constraintManager = new ConstraintManager();
        this.name=analysisName;
        constraintLogger= new LoggerFactory().createLogger("logs/ConstraintGeneration/",name+" ConstraintGeneration", logDetailLevel);
        this.view=view;
        this.constraintGeneratorStmt = new RuleApplicatorStmtVisitor(constraintManager,(JavaView)view);
        this.visitedMethods = new HashSet<>();
        //this.abstractMethods2Implementations = new HashMap<>();
        this.constraintGeneratorGlobal = new RuleApplicatorGlobal(constraintManager);
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
        Solver solver= new Solver(constraintManager.getPTAconstraints(),name,logDetailLevel);
        Map<String,PointsToSet> res= solver.solve();
        hasBeenPerformed=true;
        //SOLVE FOR SIDE EFFECTS
        ObjectMemoryLocation.reset();
        return res;
    }

    /** passes all methods reachable from entryMethod (except jrt packages)
    * We go over each method only once
    */
     private void GenerateConstraints(SootMethod entryMethod){
         SootMethod nextMethod=null;
        //pass entry method
        generateConstraintsForSingleMethod(entryMethod);
        //pass every other method
        Queue<MethodSignature> everyOtherMethod= new LinkedList<>(constraintGeneratorStmt.getMethodsInvoked());
        while(!everyOtherMethod.isEmpty()){
            MethodSignature method = everyOtherMethod.remove();
            if ( visitedMethods.contains(method)) continue;

            nextMethod= SootUpStuff.getMethodFromView((JavaView) view,method);
            if(nextMethod==null) {
                System.err.println("! FAILED TO GET METHOD " + method + "!");
                visitedMethods.add(method);
                continue;
            }  //method is inherited and seems like another method of nextMethod
            if(!nextMethod.getSignature().equals(method) ) {
        //        constraintGeneratorGlobal.inheritedMethodsRule(nextMethod, method);
            }
            if(visitedMethods.contains(nextMethod.getSignature())) {
                visitedMethods.add(method);
                continue;
            }

            generateConstraintsForSingleMethod(nextMethod);
            //note every other method to be passed over
            constraintGeneratorStmt.getMethodsInvoked().stream().
                        filter(m -> !visitedMethods.contains(m)).
                        forEach(everyOtherMethod::add);
        }
        constraintLogger.info("FINISHED GENERATING CONSTRAINTS",LogDetailLevel.LOW);
        constraintLogger.closeHandlers();
    }
    /** passes over a single method, notes other visited methods
     * @throws IllegalStateException if a method has been already visited
     *  */
    public void generateConstraintsForSingleMethod(SootMethod method){
            constraintLogger.info("+++Visiting " + method + "+++",LogDetailLevel.LOW);
            if(method.isAbstract()){
                generateConstraintsForAbsMethod(method);
                return;
            }
            constraintLogger.info(method.getBody().toString(),LogDetailLevel.MEDIUM);
            constraintLogger.info("+++++++++++++++",LogDetailLevel.LOW);
            constraintGeneratorStmt.setVisitingMethod(method.getSignature());
            for (Stmt stmt : method.getBody().getStmts()) {
                stmt.accept(constraintGeneratorStmt);
            }
            visitedMethods.add(method.getSignature());
            PrintConstraintsToLog();
            constraintLogger.info("------------\nMethods invoked:\n" + constraintGeneratorStmt.getMethodsInvoked(),LogDetailLevel.MEDIUM);
            constraintLogger.info("------------",LogDetailLevel.MEDIUM);
    }
    //supposing an abstract method is called, we assume it can be any one of its implementations
    //so we will visit them all
    public void generateConstraintsForAbsMethod(SootMethod method){
        if(method.isConcrete()) return;
        constraintLogger.info(method +" is abstract, visiting implementations",LogDetailLevel.LOW);
        constraintLogger.info("+++++++++++++++",LogDetailLevel.LOW);
        Set<SootMethod> methodImplementations = gatherImplementationsOf(method, view);
        int i=1;
        for(SootMethod implementation : methodImplementations){
            constraintLogger.info("v       implementation "+ i++ +" of "+method+"    v",LogDetailLevel.LOW);
            //The abstract method rules should apply to all methods in the view
            //but we only need those in our possible execution path

            //impl(m).this )= m.this
            constraintGeneratorGlobal.abstractThisGlobalRule(method,implementation);
            //impl(m).params= m.params
            constraintGeneratorGlobal.abstractParamGlobalRule(method,implementation);
            //m.return )= impl(m).return     <- contravariant like
            constraintGeneratorGlobal.abstractReturnGlobalRule(method,implementation);

            if(!visitedMethods.contains(implementation.getSignature()))
                generateConstraintsForSingleMethod(implementation);
        }
        visitedMethods.add(method.getSignature());
        return;
    }

    public void PrintConstraintsToLog(){
        constraintLogger.info("---------\nPTConstraints:",LogDetailLevel.HIGH);
        int i=1;
        for (Constraint c : constraintManager.getPTAconstraints() )
            constraintLogger.info((i++) + " "+ c,LogDetailLevel.HIGH);
        constraintLogger.info("---------",LogDetailLevel.HIGH);
        constraintLogger.info("---------\nSEConstraints:",LogDetailLevel.HIGH);
        i=1;
        for (GenericConstraint<?> c : constraintManager.getSEConstraints() )
            constraintLogger.info((i++) + " "+ c,LogDetailLevel.HIGH);
        constraintLogger.info("---------",LogDetailLevel.HIGH);
    }

    public Set<GenericConstraint<AccessibleHeapLocation>> getSEConstraints(){return constraintManager.getSEConstraints();}

    public Map<MethodSignature , Set<Tuple<PointsToSet, FieldSignature>>> getFieldsRead(){
        if(!hasBeenPerformed) throw new IllegalStateException("analise() need first be called to yield results");
        return constraintManager.getFieldsRead();
    }

    public Map<MethodSignature , Set<Tuple<PointsToSet, FieldSignature>>> getFieldsWritten(){
        if(!hasBeenPerformed) throw new IllegalStateException("analise() need first be called to yield results");
       return constraintManager.getFieldsWritten();
    }

    public Map<MethodSignature,Set<AccessibleHeapLocation>>  getReads(){
        if(!hasBeenPerformed) throw new IllegalStateException("analise() need first be called to yield results");
        return constraintManager.getReads();
    }
    public Map<MethodSignature,Set<AccessibleHeapLocation>>  getWrites(){
        if(!hasBeenPerformed) throw new IllegalStateException("analise() need first be called to yield results");
        return constraintManager.getWrites();
    }
    public Map<MethodSignature , Set<AccessibleHeapLocation>> getReadSets(){
        return constraintManager.getReadSets();
    }
    public Map<MethodSignature , Set<AccessibleHeapLocation>> getWriteSets(){
        return constraintManager.getWriteSets();
    }

    public boolean hasBeenPerformed(){return hasBeenPerformed;}

    public String getName(){return this.name;}
    public LogDetailLevel getLogDetailLevel(){return this.logDetailLevel;}
}
