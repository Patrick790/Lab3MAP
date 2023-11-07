package org.example.domain.validators;

import org.example.domain.User;

public class UserValidator implements Validator<User> {
    @Override
    public void validate(User entity) throws ValidationException{
        if(entity.getLastName().length() < 3 || entity.getFirstName().length() < 3)
            throw new ValidationException("Name must contain at least 3 characters\n");
        if(entity.getLastName().contains("1234567890") || entity.getFirstName().contains("1234567890"))
            throw new ValidationException("Name must not contain numbers\n");

    }
}