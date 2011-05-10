package com.github.jponge.dynamicreference;

import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

public class DynamicReference<T> {

    private final ReadWriteLock lock = new ReentrantReadWriteLock();

    private T currentReference;

    public DynamicReference(T reference) {
        this.currentReference = reference;
    }

    public DynamicReference() {
        this(null);
    }

    public DynamicReference<T> set(T reference) {
        try {
            lock.writeLock().lock();
            this.currentReference = reference;
        } finally {
            lock.writeLock().unlock();
        }
        return this;
    }

    public DynamicReference<T> discard() {
        return this.set(null);
    }

    public <R> R perform(Operation<R, T> operation) throws IllegalStateException {
        try {
            lock.readLock().lock();
            if (currentReference == null) {
                throw new IllegalStateException("The dynamic reference is currently null");
            }
            return operation.apply(currentReference);
        } finally {
            lock.readLock().unlock();
        }
    }

    public <R> R perform(Operation<R, T> operation, R defaultResult) {
        try {
            return this.perform(operation);
        } catch (IllegalStateException ignored) {
            return defaultResult;
        }
    }
}
