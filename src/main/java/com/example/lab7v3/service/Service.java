package com.example.lab7v3.service;

public interface Service<E> {
    public String getEntities();
    public E removeEntity(Long id);
}
