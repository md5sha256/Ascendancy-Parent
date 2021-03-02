package com.gmail.andrewandy.ascendancy.lib.util;

import java.util.Objects;

public class ImmutablePair<K, V> {

    private final K key;
    private final V value;

    public ImmutablePair(final K key, final V value) {
        this.key = key;
        this.value = value;
    }

    public K getKey() {
        return key;
    }

    public V getValue() {
        return value;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }

        ImmutablePair<?, ?> immutablePair = (ImmutablePair<?, ?>) o;

        if (!Objects.equals(key, immutablePair.key)) {
            return false;
        }
        return Objects.equals(value, immutablePair.value);
    }

    @Override
    public int hashCode() {
        int result = key != null ? key.hashCode() : 0;
        result = 31 * result + (value != null ? value.hashCode() : 0);
        return result;
    }

}
