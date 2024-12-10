package AliasAnalysis;

import sootup.core.jimple.basic.Value;
import sootup.core.jimple.common.stmt.Stmt;
import sootup.core.model.SootMethod;
import sootup.core.signatures.MethodSignature;
import sootup.core.types.ReferenceType;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

//this class will implement a field sensitive Andersen's points to analysis
//on jimple code
//we do not need to deal with dereferencing and such
public class PointsToAnalysis {

    //private Set<SootMethod>ToAnaliseNext;
    ConstraintGenStmtVisitor ConstraintGenerator;

    public PointsToAnalysis(){
        this.ConstraintGenerator = new ConstraintGenStmtVisitor();
        //this.ToAnaliseNext = new HashSet<SootMethod>();
    }

    public void GenerateConstraints(SootMethod entryMethod){
        ConstraintGenerator.setVisitingMethod(entryMethod.getSignature());
        for (Stmt stmt : entryMethod.getBody().getStmts()) {
            stmt.accept( ConstraintGenerator);
        }
        PrintConstraints();
        System.out.println("------------\nMethods invoked:\n"+ConstraintGenerator.getMethodsInvoked());
        System.out.println("------------");
    }
    public void PrintConstraints(){
        System.out.println("---------\nConstraints:");
        for (Constraint c : ConstraintGenerator.getConstraints() )
            System.out.println( c);
        System.out.println("---------");
    }

}
