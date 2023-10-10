package intraprocedural;

import soot.Unit;
import soot.Value;

public class TaintValue {
    private final Value value;
    private final Unit source;

    public TaintValue(Value value, Unit source) {
        this.value = value;
        this.source = source;
    }

    public Value getValue() {
        return value;
    }

    public Unit getSource() {
        return source;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        TaintValue that = (TaintValue) o;
        return value.equals(that.value) && source.equals(that.source);
    }

    @Override
    public int hashCode() {
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
