package AliasAnalysis;
import sootup.core.jimple.basic.Value;


import java.util.HashSet;

//a set of instances pointed ot by a variable
public class PointsToSet extends HashSet<MemoryLocation>{
    private String var;
    PointsToSet(String v){
        this.var=v;
    }

    public String getVar() {
        return var;
    }
}
