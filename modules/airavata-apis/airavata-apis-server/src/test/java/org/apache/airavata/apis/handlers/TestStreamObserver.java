package org.apache.airavata.apis.handlers;

import io.grpc.stub.StreamObserver;

public final class TestStreamObserver<T> implements StreamObserver<T> {
    T next;
    Throwable error;
    boolean completed = false;

    @Override
    public void onNext(T value) {
        this.next = value;
    }

    @Override
    public void onError(Throwable t) {
        this.error = t;
    }

    @Override
    public void onCompleted() {
        this.completed = true;
    }

    public T getNext() {
        return next;
    }

    public Throwable getError() {
        return error;
    }

    public boolean isCompleted() {
        return completed;
    }
}
