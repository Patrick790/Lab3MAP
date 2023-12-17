package com.example.lab7v3.observer;

public interface Observer<E extends Event> {
    void update(E e);
}
