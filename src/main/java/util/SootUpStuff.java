package util;

import sootup.core.model.SootMethod;
import sootup.core.signatures.MethodSignature;
import sootup.java.bytecode.inputlocation.JavaClassPathAnalysisInputLocation;
import sootup.java.core.JavaSootMethod;
import sootup.java.core.types.JavaClassType;
import sootup.java.core.views.JavaView;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

public final class SootUpStuff {
    private SootUpStuff(){}

    public static JavaView getViewFromPath(String sourcepath){
        return new JavaView( new JavaClassPathAnalysisInputLocation(sourcepath));
    }
    public static SootMethod getMethodFromView(JavaView view, String className, String methodSignatureString){
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
}
