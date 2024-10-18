package analysis;

// /*
import sootup.core.*;
//import sootup.analysis.*;
import sootup.callgraph.*;
import sootup.core.graph.StmtGraph;
import sootup.core.inputlocation.AnalysisInputLocation;
import sootup.core.util.DotExporter;
import sootup.java.bytecode.inputlocation.JavaClassPathAnalysisInputLocation;
import sootup.java.core.*;
import sootup.java.core.types.JavaClassType;
//import sootup.java.bytecode.*;
import sootup.java.core.views.JavaView;
import sootup.jimple.parser.*;

import sootup.core.inputlocation.AnalysisInputLocation;
import sootup.core.jimple.common.expr.JVirtualInvokeExpr;
import sootup.core.jimple.common.stmt.JInvokeStmt;
import sootup.core.model.SootClass;
import sootup.core.model.SootMethod;
import sootup.core.model.SourceType;
import sootup.core.signatures.MethodSignature;
import sootup.core.types.ClassType;
import sootup.core.views.View;
//import sootup.java.bytecode.frontend.inputlocation.PathBasedAnalysisInputLocation;
import sootup.java.core.language.JavaJimple;
import sootup.java.core.views.JavaView;

import java.util.Collections;
import java.util.Optional;
//import sootup.qilin.*;

public class Main {
    private static AnalysisInputLocation inputLocation;
    private static JavaView view;
    private static JavaClassType classType;
    private static JavaSootClass sootClass;
    private static MethodSignature methodSignature;
    private static Optional<JavaSootMethod> opt;
    private static SootMethod method;

    //instantiates the above fields
    private static void init(){
        //location->view->classType->SootClass->method->SootMethod->cfg
        inputLocation =
                new JavaClassPathAnalysisInputLocation("subject_of_analysis/bytecode");
        view = new JavaView( inputLocation);
        classType =
                view.getIdentifierFactory().getClassType("A");
        sootClass = view.getClass(classType).get();

        methodSignature =
                view
                        .getIdentifierFactory()
                        .getMethodSignature(
                                classType,
                                "a", // method name
                                "int", // return type
                                Collections.singletonList("int")); // args

        opt = view.getMethod(methodSignature);

        if(!opt.isPresent()){
            return;
        }
        method = opt.get();


    }

    public static void main(String[] args) {

        init();
        System.out.println(method.getModifiers());
        StmtGraph<?> stmt_graph = method.getBody().getStmtGraph();

        /*
        String urlToWebeditor = DotExporter.createUrlToWebeditor(stmt_graph);
        System.out.println(urlToWebeditor);
        */
        /*
        CallGraphAlgorithm cha = new ClassHierarchyAnalysisAlgorithm(view);
        CallGraph cg = cha.initialize(Collections.singletonList(methodSignature));


        System.out.println("--");
        cg.callsFrom(methodSignature).stream()
                .forEach(tgt -> System.out.println(methodSignature + " may call " + tgt));
        */

        System.out.println("--");
        System.out.println("sootclass of " +sootClass);
        System.out.println("sootmethod of " +method);
        method.getBody().getStmts().stream().forEach( x -> System.out.println(x));
        //System.out.println("method body : \n" +method.getBody());
    }
}
//*/
