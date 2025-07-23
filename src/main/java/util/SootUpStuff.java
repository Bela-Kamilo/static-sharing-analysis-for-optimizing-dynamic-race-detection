package util;

import sootup.core.model.SootMethod;
import sootup.core.signatures.MethodSignature;
import sootup.core.signatures.PackageName;
import sootup.core.types.ClassType;
import sootup.core.types.PrimitiveType;
import sootup.core.types.Type;
import sootup.core.types.VoidType;
import sootup.java.bytecode.inputlocation.JavaClassPathAnalysisInputLocation;
import sootup.java.core.JavaSootMethod;
import sootup.java.core.types.JavaClassType;
import sootup.java.core.views.JavaView;

import java.util.*;
import java.util.stream.Stream;

public final class SootUpStuff {
    private SootUpStuff(){}

    public static JavaView getViewFromPath(String sourcepath){
        return new JavaView( new JavaClassPathAnalysisInputLocation(sourcepath));
    }
    public static SootMethod getMethodFromView2(JavaView view, String className, String methodSignatureString){
        String[] methodSignatureStrings=methodSignatureString.split("\\s");
        String retType= methodSignatureStrings[0];
        String methodName= methodSignatureStrings[1];
        String methodArguments=methodSignatureStrings[2];
        methodArguments=methodArguments.substring(1, methodArguments.length()-1); //remove parentheses
        List<String> methodArgumentsList = methodArguments.isEmpty() ?
                Collections.<String>emptyList()
                : Arrays.stream(methodArguments.split("\\s*,\\s*")).toList();
        JavaClassType classType1= view.getIdentifierFactory().getClassType(className);
        MethodSignature methodSignature1 = view.getIdentifierFactory()
                .getMethodSignature(classType1,
                        methodName,
                        retType,
                        methodArgumentsList);//Arrays.stream(methodArguments.split("\\s*,\\s*")).toList());
        Optional<JavaSootMethod> opt1= view.getMethod(methodSignature1);
        if(!opt1.isPresent()){
            System.err.println("!Error! couldn't get method "+methodSignatureString);
            return null;
        }
        return  opt1.get();
    }

    /**
     *
     * @param view
     * @param methodSignatureString in the format of <DECLARING_CLASS: TYPE NAME(PARAMS)>
     * @return
     */
    public static SootMethod getMethodFromView(JavaView view, String methodSignatureString){
        MethodSignature m = methodSignatureFromString(view,methodSignatureString );
        Optional<JavaSootMethod> opt1= view.getMethod(m);
        if(!opt1.isPresent()){
            System.err.println("!Error! couldn't get method "+methodSignatureString);
            return null;
        }
        return  opt1.get();
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

    /**
     * @param s a primitive type or void or 'String' or class' name
     * @return corresponding PrimitiveType or java.lang.String or default package JavaClassType
     */
    public static Type typeFromString(String s){            //ditch this and use view.getidentifierfactory
        return switch (s) {
            case "int" -> PrimitiveType.getInt();
            case "boolean" -> PrimitiveType.getBoolean();
            case "char" -> PrimitiveType.getChar();
            //TODO
            case "String" -> new JavaClassType("String",new PackageName("java.lang"));
            case "void" -> VoidType.getInstance();
            default -> new JavaClassType(s, PackageName.DEFAULT_PACKAGE);
        };
    }
}
