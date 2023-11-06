package org.example.service;

public interface Service<E> {
    String getEntities();
    E removeEntity(Long id);
}
