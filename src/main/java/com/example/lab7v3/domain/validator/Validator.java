package com.example.lab7v3.domain.validator;

public interface Validator<T> {
    void validate(T entity) throws ValidationException;
}
