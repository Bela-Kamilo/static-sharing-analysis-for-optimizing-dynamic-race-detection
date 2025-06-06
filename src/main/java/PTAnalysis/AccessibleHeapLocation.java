package PTAnalysis;

import sootup.core.signatures.FieldSignature;
import util.Tuple;

/**
 * Represents a heap location that takes part in a memory access, that is a read or a write.
 * A field of an object instance. ex. 1.SomeField
 */
public class AccessibleHeapLocation {
    private final int memoryLocation;
    private final FieldSignature field;
    public AccessibleHeapLocation(int memoryLocation, FieldSignature f){
        this.memoryLocation=memoryLocation;
        this.field=f;
    }
    @Override
    public String toString() {
        return memoryLocation+"."+field.toString();
    }

    public int getMemoryLocation() {
        return memoryLocation;
    }

    public FieldSignature getField() {
        return field;
    }
}
