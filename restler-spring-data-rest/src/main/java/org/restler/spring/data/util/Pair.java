package org.restler.spring.data.util;

import java.util.Objects;

public class Pair<T1, T2> {
    private final T1 firstValue;
    private final T2 secondValue;

    public Pair(T1 firstValue, T2 secondValue) {
        this.firstValue = firstValue;
        this.secondValue = secondValue;
    }

    public T1 getFirstValue() {
        return firstValue;
    }

    public T2 getSecondValue() {
        return secondValue;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        Pair<?, ?> pair = (Pair<?, ?>) o;

        if (firstValue != null ? !firstValue.equals(pair.firstValue) : pair.firstValue != null) return false;
        return secondValue != null ? secondValue.equals(pair.secondValue) : pair.secondValue == null;

    }

    @Override
    public int hashCode() {
        int result = firstValue != null ? firstValue.hashCode() : 0;
        result = 31 * result + (secondValue != null ? secondValue.hashCode() : 0);
        return result;
    }

}
