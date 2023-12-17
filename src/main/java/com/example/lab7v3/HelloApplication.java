package com.example.lab7v3;

import com.example.lab7v3.controllers.UsersController;
import com.example.lab7v3.domain.User;
import com.example.lab7v3.repository.FriendshipDBRepository;
import com.example.lab7v3.repository.UserDBRepository;
import com.example.lab7v3.repository.paging.PagingRepository;
import com.example.lab7v3.service.UserService;
import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.layout.AnchorPane;
import javafx.stage.Stage;

import java.io.IOException;

public class HelloApplication extends Application {
    @Override
    public void start(Stage stage) throws IOException {
        PagingRepository<Long, User> repository = new UserDBRepository("jdbc:postgresql://localhost:5432/socialnetwork", "postgres", "Tocilarule#7");
        FriendshipDBRepository friendshipDBRepository = new FriendshipDBRepository("jdbc:postgresql://localhost:5432/socialnetwork", "postgres", "Tocilarule#7");

        UserService userService = new UserService((UserDBRepository) repository, friendshipDBRepository);

        FXMLLoader fxmlLoader = new FXMLLoader(HelloApplication.class.getResource("views/users-view.fxml"));

        //FXMLLoader fxmlLoader1 = new FXMLLoader();
        //fxmlLoader1.setLocation(getClass().getResource("views/users-view.fxml"));
//        AnchorPane layout = fxmlLoader1.load();
        Scene scene = new Scene(fxmlLoader.load());
//        Scene scene = new Scene(layout);
        UsersController usersController = fxmlLoader.getController();
        usersController.setUserService(userService);

        stage.setTitle("Users");
        stage.setScene(scene);
        stage.show();
    }

    public static void main(String[] args) {
        launch();
    }
}