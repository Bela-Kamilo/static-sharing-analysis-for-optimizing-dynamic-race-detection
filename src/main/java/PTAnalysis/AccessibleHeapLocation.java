package PTAnalysis;

import sootup.core.signatures.FieldSignature;
import util.Tuple;

import java.util.Objects;

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

    @Override
    public boolean equals(Object obj) {
        if (! (obj instanceof AccessibleHeapLocation)) return false;
        return memoryLocation == ((AccessibleHeapLocation) obj).getMemoryLocation() && field.equals(((AccessibleHeapLocation) obj).getField());
    }

    @Override
    public int hashCode() {
        return Objects.hash(memoryLocation, field);
    }
}
