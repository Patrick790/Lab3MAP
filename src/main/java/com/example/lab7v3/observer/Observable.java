package com.example.lab7v3.observer;

public interface Observable<E extends Event> {
    void notifyAll(E e);

    void addObserver(Observer<E> obs);

    void removeObserver(Observer<E> obs);
}
