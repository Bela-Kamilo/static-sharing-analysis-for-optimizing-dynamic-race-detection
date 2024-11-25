package AliasAnalysis;
import sootup.core.types.ReferenceType;

import java.util.HashSet;

public class PointsToSet extends HashSet<MemoryLocation>{
    ReferenceType var;

    PointsToSet(ReferenceType var){
        super();
        this.var=var;
    }

    @Override
    public boolean add(MemoryLocation memoryLocation) {
        return super.add(memoryLocation);
    }
}
