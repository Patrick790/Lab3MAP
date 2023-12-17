package com.example.lab7v3.repository.memory;

import com.example.lab7v3.domain.Entity;
import com.example.lab7v3.domain.validator.Validator;
import com.example.lab7v3.repository.Repository;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

public class InMemoryRepository<ID, E extends Entity<ID>> implements Repository<ID,E> {
    private final Validator<E> validator;
    Map<ID,E> entities;

    public InMemoryRepository(Validator<E> validator) {
        this.validator = validator;
        entities=new HashMap<ID,E>();
    }

    @Override
    public Optional<E> findOne(ID id){
        if (id==null)
            throw new IllegalArgumentException("id must be not null");
        return Optional.ofNullable(entities.get(id));
    }

    @Override
    public Iterable<E> findAll() {
        return entities.values();
    }


    @Override
    public Optional<E> save(E entity) {
        if (entity==null)
            throw new IllegalArgumentException("entity must be not null");
        validator.validate(entity);
        return Optional.ofNullable(entities.putIfAbsent(entity.getId(), entity));
    }

    @Override
    public Optional<E> delete(ID id) {
        if( id == null)
            throw new IllegalArgumentException("id must not be null");
        return Optional.ofNullable(entities.remove(id));
    }

    @Override
    public Optional<E> update(E entity) {

        if(entity == null)
            throw new IllegalArgumentException("entity must be not null!");
        validator.validate(entity);

        delete(entity.getId());
        return save(entity);

    }
}
