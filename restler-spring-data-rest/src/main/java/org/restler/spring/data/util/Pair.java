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
    public boolean equals(Object obj) {
        if(obj instanceof Pair) {
            Pair objPair = (Pair)obj;

            return getFirstValue().equals(objPair.getFirstValue()) &&
                    getSecondValue().equals(objPair.getSecondValue());
        }
        return false;
    }

    @Override
    public int hashCode() {
        return Objects.hash(firstValue, secondValue);
    }
}
