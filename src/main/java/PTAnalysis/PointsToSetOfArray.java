package PTAnalysis;

import java.util.LinkedList;
import java.util.List;

/**
 *This class exists because we want to treat arrays as aggregates,
 * that is lump all their elements together.
 * Different locals will map to the same PointsToSet for 2 reasons
 *
 * 1) In cases of multidimensional arrays arr[i][j] and arr[k] will map to the same
 * PointsToSet
 *
 * 2) array instances passed around through assignments
 * that is a case were
 * local1= arr
 * local1[i]= new //memory location l
 *
 * arr should also hold l in its PointsToSet
 */
public class PointsToSetOfArray extends PointsToSet{
    private final List<String> aliases;
    public PointsToSetOfArray(String v) {
        super(v);
        aliases = new LinkedList<>();
    }
    public void addAlias(String valueName){
        this.aliases.add(valueName);
    }

    public List<String> getAliases() {
        return aliases;
    }
}
