package intraprocedural;

import soot.Unit;
import soot.Value;

public class TaintValue {
    private final Value value;  // The actual value being tainted.
    private final Unit source;  // The source statement of the taint.

    // Constructor to initialize the TaintValue.
    public TaintValue(Value value, Unit source) {
        this.value = value;
        this.source = source;
    }

    // Getter for value.
    public Value getValue() {
        return value;
    }

    // Getter for source.
    public Unit getSource() {
        return source;
    }

    // Overriding equals and hashCode is crucial to ensure that TaintValue objects can be used
    // effectively in collections like sets and maps.

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        TaintValue that = (TaintValue) o;
        return value.equals(that.value) && source.equals(that.source);
    }

    @Override
    public int hashCode() {
        // Consider both value and source in hash code calculation.
        int result = value.hashCode();
        result = 31 * result + source.hashCode();
        return result;
    }

    @Override
    public String toString() {
        return "TaintValue{" +
                "value=" + value +
                ", source=" + source +
                '}';
    }
}
