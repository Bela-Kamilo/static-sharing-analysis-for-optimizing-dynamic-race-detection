package AliasAnalysis;

import sootup.core.signatures.MethodSignature;

import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.Vector;

//this class is a note for some method
//noted are arguments passed to it in invocations
//and location sets the method returns
public class MethodNote {
    private final MethodSignature methodSignature;
    private final Vector<Set<PointsToSet>> argumentsVector;
    private final Set<PointsToSet> methodReturnSets;

    MethodNote(MethodSignature f){
        this.methodSignature=f;
        this.methodReturnSets= new HashSet<>();
        Vector<Set<PointsToSet>> newargVector= new Vector<>();
        for(int i=0; i< f.getParameterTypes().size();i++)
            newargVector.add(new HashSet<>());
        this.argumentsVector= newargVector;
    }

    public void addArgument(PointsToSet PTSet, int ordinal){
        try {
            argumentsVector.get(ordinal).add(PTSet);

        } catch (Exception e) {
            System.out.println("!failed to add "+PTSet+"as argument "+ordinal);
        }
    }

    public void addReturnSet(PointsToSet returned){
        methodReturnSets.add(returned);
    }

    public Vector<Set<PointsToSet>> getArgumentsVector() {
        return argumentsVector;
    }

    @Override
    public String toString() {
        return argumentsVector.toString();
    }
}
