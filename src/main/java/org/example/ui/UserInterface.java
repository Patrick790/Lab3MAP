package org.example.ui;

import org.example.domain.User;
import org.example.domain.validator.ValidationException;
import org.example.service.UserService;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.Objects;

public class UserInterface {
    private final UserService userService;

    public UserInterface(UserService userService) {
        this.userService = userService;
    }

    private void executeInput(int option) {
        switch (option) {
            case 1:
                addUser();
                return;
            case 2:
                removeUser();
                return;
            case 3:
                System.out.println(userService.getEntities());
                return;
            case 4:
                addFriendship();
                return;
            case 5:
                removeFriendship();
                return;
            case 6:
                String result = userService.getFriendships();
                System.out.println(!result.isEmpty() ? result : "Nu exista utilizatori cu prieteni");
                return;
            case 7:
                getNumberOfCommunities();
                return;
            case 8:
                getMostSociableCommunity();
                return;
            default:
                return;
        }
    }

    private void removeFriendship() {
        System.out.println("Remove friendship\n" + userService.getEntities() + "\nSelect two ids.\nid1=");
        BufferedReader reader = new BufferedReader(new InputStreamReader(System.in));
        try {
            String input = reader.readLine();
            Long id1 = Long.parseLong(input);
            System.out.println("id2=");
            input = reader.readLine();
            Long id2 = Long.parseLong(input);
            userService.removeFriendship(id1, id2);
            System.out.println("Friendship removed successfully.\n");
        } catch (IOException | IllegalArgumentException e) {
            System.out.println("Error reading input: \n" + e.getMessage());
        }
    }

    private void addFriendship(){
        System.out.println("Add friendship\n" + userService.getEntities() + "\nSelect two ids.\nid1=");
        BufferedReader reader = new BufferedReader(new InputStreamReader(System.in));
        try{
            String input = reader.readLine();
            Long id1 = Long.parseLong(input);
            System.out.println("id2=");
            input = reader.readLine();
            Long id2 = Long.parseLong(input);
            userService.addFriendship(id1, id2);
            System.out.println("Friendships added successfully.\n");
        }catch (IOException | IllegalArgumentException e){
            System.out.println("Error reading input: \n" + e.getMessage());
        }
    }

    private void getMostSociableCommunity(){
        System.out.println("Most sociable community consists of: " + userService.getMostSocialCommunity() + '\n');
    }

    private void getNumberOfCommunities(){
        System.out.println("Number of communities = " + userService.getNumberOfCommunities() + "\n");
    }

    private void removeUser(){
        System.out.println("Remove User\nid=");
        BufferedReader reader = new BufferedReader(new InputStreamReader(System.in));
        try {
            String input = reader.readLine();
            long inputValue = Long.parseLong(input);
            String name = userService.removeEntity(inputValue).toString();
            if (Objects.equals(name, null))
                System.out.printf("Could not find user with id %d", inputValue);
            else
                System.out.printf("%s was removed successfully.\n", name);
        } catch (IOException | IllegalArgumentException e) {
            System.out.println("Error reading input: \n" + e.getMessage());
        }
    }

    private void addUser() {
        System.out.println("Add User\nfirst name: ");
        BufferedReader reader = new BufferedReader(new InputStreamReader(System.in));
        try {
            String firstName = reader.readLine();
            System.out.println("last name: ");
            String lastName = reader.readLine();
            User user = new User(firstName, lastName);
            userService.addUser(user);
            System.out.println("User was added successfully.\n");
        } catch (IOException | IllegalArgumentException | ValidationException e) {
            System.out.println("Error reading input:\n" + e.getMessage());
        }
    }

    private void showOptions() {
        System.out.println("1. add user\n2. remove user\n3. view users\n4. add friendship\n5. remove friendship\n" +
                "6. view friendships\n7. view number of communities\n8. view most sociable community\n0. exit\n");
    }

    private Integer readUserOption() {
        BufferedReader reader = new BufferedReader(new InputStreamReader(System.in));
        int inputValue = -1;
        try {
            String input = reader.readLine();
            inputValue = Integer.parseInt(input);
        } catch (IOException | NumberFormatException e) {
            System.out.println("Error reading input!\n");
        }
        return inputValue;
    }

    public void run() {
        boolean stop = false;
        while (!stop) {
            showOptions();
            Integer option = readUserOption();
            if (option == 0)
                stop = true;
            else
                executeInput(option);
        }
    }
}

