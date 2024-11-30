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

    //private Set<SootMethod>ToAnalizeNext;
    ConstraintGenStmtVisitor ConstraintGenerator;

    public PointsToAnalysis(){
        this.ConstraintGenerator = new ConstraintGenStmtVisitor();
        //this.ToAnalizeNext = new HashSet<SootMethod>();
    }

    public void GenerateConstraints(SootMethod method){

        for (Stmt stmt : method.getBody().getStmts()) {
            stmt.accept( ConstraintGenerator);
        }
        PrintConstraints();
    }
    public void PrintConstraints(){
        for (Constraint c : ConstraintGenerator.getConstraints() )
            System.out.println( c);
    }

}
