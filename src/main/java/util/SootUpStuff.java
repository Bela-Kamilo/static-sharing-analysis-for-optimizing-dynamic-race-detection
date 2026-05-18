package util;

import sootup.core.model.SootClass;
import sootup.core.model.SootMethod;
import sootup.core.model.SourceType;
import sootup.core.signatures.MethodSignature;
import sootup.core.signatures.MethodSubSignature;
import sootup.core.typehierarchy.TypeHierarchy;
import sootup.core.types.ClassType;
import sootup.core.views.View;
import sootup.java.bytecode.inputlocation.JavaClassPathAnalysisInputLocation;
import sootup.java.bytecode.inputlocation.JrtFileSystemAnalysisInputLocation;
import sootup.java.core.JavaSootClass;
import sootup.java.core.JavaSootMethod;
import sootup.java.core.types.JavaClassType;
import sootup.java.core.views.JavaView;

import java.util.*;

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
     * @return SootMethod of m if it is in view
     */
    //WATCH OUT. IF GIVEN A METHOD DEFINED IN A SUPERCLASS THIS WILL RETURN THE SOOTMETHOD OF THE SUPERCLASS
    // WHICHJ WILLCAUSE AN ERROR WHEN THAT ACTUAL METHOD IS TO BE VISITED (AND INITED)
    public static SootMethod getMethodFromView(JavaView view, MethodSignature m){
        Optional<JavaSootMethod> opt;
        //Optional<JavaSootMethod> jrtOpt;
        //jrtOpt= jrtView.getMethod(m);
        opt= view.getMethod(m);
        if(opt.isEmpty()) {     //m could be defined in a superclass
            ClassType callerClassType = m.getDeclClassType();
            Optional<JavaSootClass> callerClass = view.getClass(callerClassType);
            if(callerClass.isEmpty())
                throw new RuntimeException("couldn't get caller class of "+m);
            SootMethod mSootMethod = getMethodDefinedInParentClass(view,callerClass.get(),m.getSubSignature());
            if(mSootMethod == null){
                System.err.println("!ERROR couldnt get method "+m+" !");
                return null;
            }
            return mSootMethod;
        }
        return opt.get();
    }

    public static SootMethod getMethodDefinedInParentClass(JavaView view,JavaSootClass callerClass, MethodSubSignature m){
        Optional<JavaSootMethod> opt = callerClass.getMethod(m);
        //opt is empty if m is defined in super class
        if(opt.isPresent()) return opt.get();
       //get callerClass's super class
        Optional<JavaClassType> optSuperClass= callerClass.getSuperclass();
        if(optSuperClass.isEmpty())
            return null;
        //get super class' SootClass
        Optional<JavaSootClass> optSuperClassSootClass= view.getClass(optSuperClass.get());
        if(optSuperClassSootClass.isEmpty())
            return null;
        //try to get m
        JavaSootClass superClass= optSuperClassSootClass.get();
        Optional<JavaSootMethod> result = superClass.getMethod(m);
        if(result.isPresent()) return result.get();
        //repeat
        return getMethodDefinedInParentClass(view,superClass,m);
    }

    /**
     * @param path path to the class file of the declaring class
     * @param methodSignatureString in the format of <DECLARING_CLASS: TYPE NAME(PARAMS)>
     *
     */
    public static MethodSignature methodSignatureFromString(String path ,String methodSignatureString) {
        return methodSignatureFromString(getViewFromPath(path),methodSignatureString);
    }


    public static Set<SootMethod> gatherImplementationsOf( SootMethod abstractMethod, View view){
        Set<SootMethod> res = new TreeSet<>(new Comparator<SootMethod>() {  //implementations appearing in the same order
                                                                            // every time helps with testing
            @Override
            public int compare(SootMethod o1, SootMethod o2) {
                return o1.toString().compareTo(o2.toString());
            }
        });
        if(abstractMethod.isConcrete()) {
            res.add(abstractMethod);
            return res;
        }
        SootClass classOfAbstractMethod = view.getClass(abstractMethod.getDeclaringClassType()).orElseThrow();
        //find all subclasses of the abstract method's class
        for(SootClass classInView : view.getClasses() ){
            if(isSubclass(classInView,classOfAbstractMethod,view.getTypeHierarchy())) {
                Optional<? extends SootMethod> opt=classInView.
                        getMethod(abstractMethod.getSignature().getSubSignature());

                if(opt.isEmpty()) continue;
                SootMethod implementation= opt.get();
                res.add(implementation);
            }
        }
        return res;
    }

    public static boolean isSubclass(SootClass subClass, SootClass superClass, TypeHierarchy hierarchy) {
        return hierarchy.isSubtype(superClass.getType(), subClass.getType());
    }
     /*   //if subclass directly extends superClass

        //or if some subClass'superclass directly exntends SuperClass
        subClass.
        return  false;
    }*/

        /**
         * @param methodSignatureString  in the format of <DECLARING_CLASS: TYPE NAME(PARAM_TYPES)>
         *  reimplementation of JavaIdentifierFactory.parseMethodSignature -> refactor me out
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
