
package org.example;

import org.example.domain.User;
import org.example.domain.validator.UserValidator;
import org.example.repository.InMemoryRepository;
import org.example.service.UserService;
import org.example.ui.UserInterface;

public class Main {
    public static void main(String[] args) {
//        User u1=new User("u1FirstName", "u1LastName");
//        u1.setId(1l);
//        User u2=new User("u2FirstName", "u2LastName"); u2.setId(1l);
//        User u3=new User("u3FirstName", "u3LastName"); u3.setId(1l);
//        User u4=new User("u4FirstName", "u4LastName"); u4.setId(1l);
//        User u5=new User("u5FirstName", "u5LastName"); u5.setId(1l);
//        User u6=new User("u6FirstName", "u6LastName"); u6.setId(1l);
//        User u7=new User("u7FirstName", "u7LastName"); u7.setId(1l);
//
//        InMemoryRepository<Long, User> repo=new InMemoryRepository<>(new UserValidator());
//        repo.save(u1);
//        repo.save(u2);
//        repo.save(u3);
//        repo.save(u4);
//        repo.save(u5);
//        repo.save(u6);
//        repo.save(u7);
//
//        System.out.println("ok");

        InMemoryRepository<Long, User> repository = new InMemoryRepository<>(new UserValidator());
        UserService userService = new UserService(repository);
        UserInterface ui = new UserInterface(userService);

        ui.run();
    }

}