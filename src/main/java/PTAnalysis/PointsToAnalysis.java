package PTAnalysis;

import PTAnalysis.ConstraintSolver.Solver;
import util.EmptyFormatter;
import sootup.core.jimple.common.stmt.Stmt;
import sootup.core.model.SootMethod;
import sootup.core.signatures.MethodSignature;
import sootup.core.types.Type;
import sootup.core.views.View;
import util.LoggerFactory;

import java.io.IOException;
import java.util.*;
import java.util.logging.FileHandler;
import java.util.logging.Logger;

/** This class implements a field sensitive Andersen's-like points to analysis
 *  on jimple code. We do not need to deal with dereferencing and such here.
 *  There s a PointsToSet for every reference local, non-null method, method parameter
 *  and field (of reference type) possibly held by an instance.
 *  Assignment statements and method invocations generate constraints on the PointsToSets.
 *  Every statement (and method) is visited once
*/
public class PointsToAnalysis {


    ConstraintGenStmtVisitor ConstraintGenerator;
    Set <MethodSignature> visitedMethods;
    View view;
    private final Logger constraintLogger;
    private boolean hasBeenPerformed=false;
    public PointsToAnalysis(View view){
        constraintLogger= new LoggerFactory().createLogger("ConstraintGeneration");
        this.ConstraintGenerator = new ConstraintGenStmtVisitor();
        this.visitedMethods = new HashSet<>();
        this.view=view;
    }

    /** performs the analysis on reachable code from entryMethod
     * @return A mapping of jimple value holders to sets of memory locations
     * value holders are in the form of :
     *
     * <Class: MethodSignature>:local   -   for locals
     * <Class: StaticField>             -   for static fields
     * X.<Class: Type field>            -   for instance fields, where X is an integer representing
     *                                      an abstract memory location
     * <Class: MethodSignature>         -   for the possible locations the method might return
     *
     */
    public Map<String, PointsToSet> analise(SootMethod entryMethod){
        GenerateConstraints(entryMethod);
        Solver solver= new Solver(this.ConstraintGenerator.getConstraints());
        Map<String,PointsToSet> res= solver.solve();
        hasBeenPerformed=true;
        MemoryLocation.reset();
        return res;
    }

    /** passes all methods reachable from entryMethod.
    * We go over each method only once
    */
     public void GenerateConstraints(SootMethod entryMethod){
        //pass entry method
        generateConstraintsForSingleMethod(entryMethod);
        //pass every other method
        Queue<MethodSignature> everyOtherMethod= new LinkedList<>(ConstraintGenerator.getMethodsInvoked());
        while(!everyOtherMethod.isEmpty()){
            MethodSignature method = everyOtherMethod.remove();
            if ( Type.isObjectLikeType(method.getDeclClassType()) || visitedMethods.contains(method)) continue;

            Optional<? extends SootMethod> opt = view.getMethod(method);
            if(!opt.isPresent()) { System.err.println("!Coulnt get SootMethod of "+ method+"!"); continue;}
            generateConstraintsForSingleMethod(opt.get());
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
        constraintLogger.info("---------\nConstraints:");
        int i=1;
        for (Constraint c : ConstraintGenerator.getConstraints() )
            constraintLogger.info((i++) + " "+ c);
        constraintLogger.info("---------");
    }

    public Set<Constraint> getConstraints(){return ConstraintGenerator.getConstraints();}

}
