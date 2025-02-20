package PTAnalysis;

import PTAnalysis.ConstraintSolver.Solver;
import sootup.core.jimple.basic.Value;
import sootup.core.jimple.common.stmt.Stmt;
import sootup.core.model.SootMethod;
import sootup.core.signatures.MethodSignature;
import sootup.core.types.Type;
import sootup.core.views.View;

import java.util.*;

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

    public PointsToAnalysis(View view){
        this.ConstraintGenerator = new ConstraintGenStmtVisitor();
        this.visitedMethods = new HashSet<>();
        this.view=view;
    }

    /** performs the analysis on reachable code from entryMethod  */
    public Map<String, Set<Integer>> analise(SootMethod entryMethod){
        GenerateConstraints(entryMethod);
        Solver solver= new Solver(this.ConstraintGenerator.getConstraints());
        return solver.solve();
      //  Map<Value, PointsToSet> debug = this.ConstraintGenerator.getVarsToLocationsMap();
       // return debug;
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
            if(!opt.isPresent()) { System.out.println("!Coulnt get SootMethod of "+ method+"!"); continue;}
            generateConstraintsForSingleMethod(opt.get());
            //note every other method to be passed over
            ConstraintGenerator.getMethodsInvoked().stream().
                        filter(m -> !visitedMethods.contains(m)).
                        forEach(everyOtherMethod::add);

        }
    }
    /** passes a single method, notes other visited methods */
    public void generateConstraintsForSingleMethod(SootMethod method){
        System.out.println("+++Visiting "+ method+"+++");
        System.out.println(method.getBody());
        System.out.println("+++++++++++++++");
        ConstraintGenerator.setVisitingMethod(method.getSignature());
        for (Stmt stmt : method.getBody().getStmts()) {
            stmt.accept( ConstraintGenerator);
        }
        visitedMethods.add(method.getSignature());
        PrintConstraints();
        System.out.println("------------\nMethods invoked:\n"+ConstraintGenerator.getMethodsInvoked());
        System.out.println("------------");
    }

    public void PrintConstraints(){
        System.out.println("---------\nConstraints:");
        int i=1;
        for (Constraint c : ConstraintGenerator.getConstraints() )
            System.out.println((i++) + " "+ c);
        System.out.println("---------");
    }

    public Set<Constraint> getConstraints(){return ConstraintGenerator.getConstraints();}
}
