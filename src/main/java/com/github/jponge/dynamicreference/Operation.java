package com.github.jponge.dynamicreference;

public interface Operation<R, T> {

    public R apply(T reference);
}
