package com.gmail.andrewandy.ascendancy.lib.util;

import java.util.Objects;

public class MutablePair<K, V> {

    private final K key;
    private V value;

    public MutablePair(final K key, final V value) {
        this.key = key;
        this.value = value;
    }

    public K getKey() {
        return key;
    }

    public V getValue() {
        return value;
    }

    public void setValue(V value) {
        this.value = value;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }

        MutablePair<?, ?> pair = (MutablePair<?, ?>) o;

        if (!Objects.equals(key, pair.key)) {
            return false;
        }
        return Objects.equals(value, pair.value);
    }

    @Override
    public int hashCode() {
        int result = key != null ? key.hashCode() : 0;
        result = 31 * result + (value != null ? value.hashCode() : 0);
        return result;
    }

}
