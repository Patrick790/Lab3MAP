package com.example.lab7v3.controllers;

import com.example.lab7v3.domain.User;
import com.example.lab7v3.observer.Observer;
import com.example.lab7v3.observer.UserChangeEvent;
import com.example.lab7v3.repository.paging.Page;
import com.example.lab7v3.repository.paging.Pageable;
import com.example.lab7v3.service.UserService;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import javafx.scene.control.cell.PropertyValueFactory;

import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

public class UsersController implements Observer<UserChangeEvent> {

    private UserService userService;

    @FXML
    TableView<User> userTableView;

    @FXML
    TableColumn<User, Long> idUser;

    @FXML
    TableColumn<User, String> firstName;

    @FXML
    TableColumn<User, String> lastName;

    @FXML
    TextField firstNameField;

    @FXML
    TextField lastNameField;

    @FXML
    Button prevButton;

    @FXML
    Button nextButton;

    private int pageSize = 5;

    private int currentPage = 0;

    private int totalNrOfElems = 0;

    ObservableList<User> usersModel = FXCollections.observableArrayList();

    public void setUserService(UserService userService){
        this.userService = userService;
        userService.addObserver(this);
        initModel();
    }

    private void initModel(){
        Page<User> page = userService.findAllOnPage(new Pageable(currentPage, pageSize));

        int maxPage = (int) Math.ceil((double) page.getTotalNrOfElems() / pageSize) - 1;

        if(currentPage > maxPage){
            currentPage = maxPage;

            page = userService.findAllOnPage(new Pageable(currentPage, pageSize));
        }

        usersModel.setAll(StreamSupport.stream(page.getElementsOnPage().spliterator(),
                false).collect(Collectors.toList()));

        totalNrOfElems = page.getTotalNrOfElems();

        prevButton.setDisable(currentPage == 0);
        nextButton.setDisable((currentPage + 1) * pageSize >= totalNrOfElems);

    }

    public void initialize(){
        userTableView.setItems(usersModel);

        idUser.setCellValueFactory(new PropertyValueFactory<>("id"));
        firstName.setCellValueFactory(new PropertyValueFactory<>("firstName"));
        lastName.setCellValueFactory(new PropertyValueFactory<>("lastName"));
    }

    public void onPressDelete(ActionEvent actionEvent){
        User selectedUser = userTableView.getSelectionModel().getSelectedItem();

        if(selectedUser != null){
            userService.removeEntity(selectedUser.getId());
        }
        else {
            MessageAlert.showErrorMessage(null, "No user selected");
        }
    }

    public void onPressAdd(ActionEvent actionEvent) {
        String firstName = firstNameField.getText();
        String lastName = lastNameField.getText();

        userService.addUser(new User(firstName, lastName ));
    }

    public void onPressUpdate(ActionEvent actionEvent){
        User selectedUser = userTableView.getSelectionModel().getSelectedItem();

        if(selectedUser != null){
            // Retrieve updated data from the text fields or any other input fields
            String updatedFirstName = firstNameField.getText();
            String updatedLastName = lastNameField.getText();

            // Set the updated data to the selected user
            selectedUser.setFirstName(updatedFirstName);
            selectedUser.setLastName(updatedLastName);

            // Update the user using the UserService
            userService.update(selectedUser);
            initModel();
        } else {
            MessageAlert.showErrorMessage(null, "No user selected");
        }
    }


    @Override
    public void update(UserChangeEvent userChangeEvent) {
        initModel();
    }

    public void onPressPrev(ActionEvent actionEvent){
        currentPage--;
        initModel();
    }

    public void onPressNext(ActionEvent actionEvent){
        currentPage++;
        initModel();
    }
}
