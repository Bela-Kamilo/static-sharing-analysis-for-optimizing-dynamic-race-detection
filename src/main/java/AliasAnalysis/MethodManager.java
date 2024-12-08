package AliasAnalysis;

import sootup.core.signatures.MethodSignature;

import java.util.*;

//MethodManager holds info about a method's return locations
//and a methods parameter's locations
public class MethodManager {
    //holds a vector of PointsToSets for each method
    //there has to be a better way tbh
    private final Map<MethodSignature , MethodNote> notedMethods;


    MethodManager(){this.notedMethods= new HashMap<>(); }


    //note  parameter=argument for method f
    public void argumentIsSubsetOf(MethodSignature f , int argumentAndParameterOrdinal, PointsToSet argumentsPTSet){
        if(!notedMethods.containsKey(f)){
            System.out.println("!method "+ f+" hasn't been noted!");
            return;
        }
        notedMethods.get(f).addArgument(argumentsPTSet,argumentAndParameterOrdinal);
        /*
        Vector<Set<PointsToSet>> parameterVector= methodParametersMap.get(f);
        Set<PointsToSet> parameterPTSets= parameterVector.get(argumentAndParameterOrdinal);
        parameterPTSets.add(argumentsPTSet);
*/
    }

    public void noteMethod(MethodSignature methodSignature) {

        if (notedMethods.containsKey(methodSignature)) return;    //method already noted
        MethodNote methodNote = new MethodNote(methodSignature);
        notedMethods.put(methodSignature, methodNote);
    }
    /*

    private boolean isNoted(MethodSignature methodSignature){
        boolean res=false;

        if(methodParametersMap.containsKey(methodSignature)){
            System.out.println("method " + methodSignature+ "is already noted in the Parameter's Map");
            res=true;
        }    //for debugging
        if(methodReturnMap.containsKey(methodSignature)){
            System.out.println("method " + methodSignature+ "is already noted in the Return Map");
            res=true;
        }
        return res;
    }
*/
    public String toString(){return notedMethods.toString();}
}
