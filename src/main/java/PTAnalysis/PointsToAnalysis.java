package PTAnalysis;

import PTAnalysis.ConstraintSolver.Solver;
import sootup.core.jimple.basic.Value;
import sootup.core.jimple.common.stmt.Stmt;
import sootup.core.model.SootMethod;
import sootup.core.signatures.MethodSignature;
import sootup.core.types.Type;
import sootup.core.views.View;

import java.util.*;

//this class will implement a field sensitive Andersen's points to analysis
//on jimple code
//we do not need to deal with dereferencing and such
public class PointsToAnalysis {

    ConstraintGenStmtVisitor ConstraintGenerator;
    Set <MethodSignature> visitedMethods;
    View view;

    public PointsToAnalysis(View view){
        this.ConstraintGenerator = new ConstraintGenStmtVisitor();
        this.visitedMethods = new HashSet<>();
        this.view=view;
    }

    public Map<Value, PointsToSet> analise(SootMethod entryMethod){
        GenerateConstraints(entryMethod);
        Solver solver= new Solver(this.ConstraintGenerator.getConstraints());
        solver.solve();
        Map<Value, PointsToSet> debug = this.ConstraintGenerator.getVarsToLocationsMap();
        return debug;
    }

    //generates constraints for all methods reachable from entryMethod
    //we'll go over each method only once
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
    //generates constraints, notes visited methods
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
