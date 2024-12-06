package AliasAnalysis;

import sootup.core.signatures.MethodSignature;

import java.util.*;

//MethodManager holds info about a method's return locations
//and a methods parameter's locations
public class MethodManager {
    //holds a vector of PointsToSets for each method
    //there has to be a better way tbh
    private final Map<MethodSignature , Vector<Set<PointsToSet>>> methodParametersMap;
    private final Map<MethodSignature, PointsToSet> methodReturnMap;

    MethodManager(){
        methodParametersMap = new HashMap<>();
        methodReturnMap = new HashMap<>();}

    //note  parameter=argument for method f
    public void argumentIsSubsetOf(MethodSignature f , int argumentAndParameterOrdinal, PointsToSet argumentsPTSet){
        if(!methodParametersMap.containsKey(f)){
            System.out.println("!method "+ f+" hasn't been noted!");
            return;
        }
        Vector<Set<PointsToSet>> parameterVector= methodParametersMap.get(f);
        Set<PointsToSet> parameterPTSets= parameterVector.get(argumentAndParameterOrdinal);
        parameterPTSets.add(argumentsPTSet);

    }

    public void noteMethod(MethodSignature methodSignature){

        if(isNoted(methodSignature))return;

        PointsToSet returnPTSet = new PointsToSet(methodSignature.toString());
        methodReturnMap.put(methodSignature, returnPTSet);
        Vector<Set<PointsToSet>> parameterVector= new Vector<>();
        for(int i =0 ; i < methodSignature.getParameterTypes().size();i++){
            parameterVector.add(new HashSet<>());
        }
        methodParametersMap.put(methodSignature, parameterVector);
    }

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

    public void printParameterMappings(){System.out.println(methodParametersMap);}
}
