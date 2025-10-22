package util;

import sootup.core.model.SootMethod;
import sootup.core.model.SourceType;
import sootup.core.signatures.MethodSignature;
import sootup.core.signatures.PackageName;
import sootup.core.types.ClassType;
import sootup.core.types.PrimitiveType;
import sootup.core.types.Type;
import sootup.core.types.VoidType;
import sootup.core.views.View;
import sootup.java.bytecode.inputlocation.JavaClassPathAnalysisInputLocation;
import sootup.java.bytecode.inputlocation.JrtFileSystemAnalysisInputLocation;
import sootup.java.core.JavaSootMethod;
import sootup.java.core.types.JavaClassType;
import sootup.java.core.views.JavaView;

import java.util.*;
import java.util.stream.Stream;

public final class SootUpStuff {
    private static final JavaView jrtView =new JavaView(new JrtFileSystemAnalysisInputLocation(SourceType.Application));    //doesnt work

    private SootUpStuff(){}

    public static JavaView getViewFromPath(String sourcepath){
        return new JavaView( new JavaClassPathAnalysisInputLocation(sourcepath));
    }

    /**
     *
     * @param view
     * @param methodSignatureString in the format of <DECLARING_CLASS: TYPE NAME(PARAMS)>
     * @return
     */
    public static SootMethod getMethodFromView(JavaView view, String methodSignatureString){
                return getMethodFromView(view,methodSignatureFromString(view,methodSignatureString ));
    }

    /**
     *
     * @param view
     * @param m
     * @return SootMethod of m if it is in view or the view of the JRE
     */
    public static SootMethod getMethodFromView(JavaView view, MethodSignature m){
        Optional<JavaSootMethod> opt;
        Optional<JavaSootMethod> jrtOpt;
        opt= view.getMethod(m);
        if(opt.isEmpty()) {     //maybe m is a JRT Class method
            jrtOpt= jrtView.getMethod(m);
            if(jrtOpt.isEmpty()) {
                System.err.println("!Error! couldn't get method "+m.toString());
                return null;
            }
            return jrtOpt.get();
        }
        return opt.get();
    }
    /**
     * @param path path to the class file of the declaring class
     * @param methodSignatureString in the format of <DECLARING_CLASS: TYPE NAME(PARAMS)>
     *
     */
    public static MethodSignature methodSignatureFromString(String path ,String methodSignatureString) {
        return methodSignatureFromString(getViewFromPath(path),methodSignatureString);
    }
        /**
         * @param methodSignatureString  in the format of <DECLARING_CLASS: TYPE NAME(PARAM_TYPES)>
         *
         */
    public static MethodSignature methodSignatureFromString(JavaView view ,String methodSignatureString){
        String declaringClassPattern="(?<=<)([A-Z][a-zA-Z\\d]*)(?=:)";  //<NAME: but only NAME
        String methodTypePattern="(?<=:\\s)([a-zA-Z]*)(?=\\s)";     //:_TYPE_ but only TYPE '_' is space
        String methodNamePattern="(?<=\\s)([a-zA-Z\\d]*)(?=\\s*\\()";   //_NAME( but only NAME
        Scanner signatureScanner= new Scanner(methodSignatureString);
        String declaringClassName = signatureScanner.findInLine(declaringClassPattern);
        String methodTypeString = signatureScanner.findInLine(methodTypePattern);
        String methodName = signatureScanner.findInLine(methodNamePattern);
        String[] parametersString=signatureScanner.findInLine("(?<=\\()(.*)(?=\\))").split("\\s*,\\s*");
        List<String> parametersList =parametersString[0].isEmpty()? Collections.emptyList() : Arrays.stream(parametersString).toList();
        JavaClassType classType1= view.getIdentifierFactory().getClassType(declaringClassName);
        MethodSignature result = view.getIdentifierFactory()
                .getMethodSignature(classType1,
                        methodName,
                        methodTypeString,
                        parametersList
                );
      return result;
    }
}
