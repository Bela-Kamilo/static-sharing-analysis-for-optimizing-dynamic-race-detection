package AliasAnalysis;

import com.ibm.wala.ipa.callgraph.propagation.PointerAnalysis;
import sootup.core.jimple.common.stmt.Stmt;
import sootup.core.model.SootMethod;
import sootup.core.types.ReferenceType;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

//this class will implement a field sensitive Andersen's points to analysis
//on jimple code
//we do not need to deal with dereferencing and such
public class PointsToAnalysis {
    private Map<ReferenceType,PointsToSet> sets;
    private Set<Constraint> constraints;
    private Set<MemoryLocation> locations;
    //private Set<SootMethod>ToAnalizeNext;

    public PointsToAnalysis(){
        this.sets = new HashMap<ReferenceType,PointsToSet>();
        this.constraints= new HashSet<Constraint>();
        this.locations= new HashSet<MemoryLocation>();
        //this.ToAnalizeNext = new HashSet<SootMethod>();
    }

    public void GenerateConstraints(SootMethod method){
        ConstraintGenStmtVisitor stmtVisitor = new ConstraintGenStmtVisitor(constraints);
        for (Stmt stmt : method.getBody().getStmts()) {
            stmt.accept( stmtVisitor);
        }
        PrintConstraints();
    }
    public void PrintConstraints(){
        for (Constraint c : constraints )
            System.out.println( c);
    }

}
