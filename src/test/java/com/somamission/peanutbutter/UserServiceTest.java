package com.somamission.peanutbutter;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.google.gson.Gson;
import com.somamission.peanutbutter.intf.IUserService;
import com.somamission.peanutbutter.constants.ErrorMessageContants;
import com.somamission.peanutbutter.domain.User;
import com.somamission.peanutbutter.exception.BadRequestException;
import com.somamission.peanutbutter.exception.UserNotFoundException;
import org.json.JSONException;
import org.json.JSONObject;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.junit4.SpringRunner;

import java.io.FileNotFoundException;
import java.io.IOException;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatExceptionOfType;

@RunWith(SpringRunner.class)
@SpringBootTest
public class UserServiceTest {
    @Autowired
    IUserService userService;

    @Autowired
    private PasswordEncoder passwordEncoder;

    private Gson gson = new Gson();;

    @Test
    public void should_create_a_user() throws IOException, BadRequestException, JSONException, UserNotFoundException {
        String newUserParams = TestUtils.getFileToJson("json/happy/new_user.json");
        JSONObject newUserParamsJson = new JSONObject(newUserParams);
        String email = newUserParamsJson.optString("email");
        String username = newUserParamsJson.optString("username");
        String password = newUserParamsJson.optString("password");
        userService.createNewUser(email, username, password);
        User userFromCreateRequest = gson.fromJson(newUserParams, User.class);
        User newUser = userService.getUserByUsername(userFromCreateRequest.getUsername());
        assertThat(userFromCreateRequest.getEmail()).isEqualTo(newUser.getEmail());
        assertThat(userFromCreateRequest.getUsername()).isEqualTo(newUser.getUsername());
        assertThat(passwordEncoder.matches(userFromCreateRequest.getPassword(), newUser.getPassword())).isTrue();
    }

    @Test
    public void should_update_user_password() throws BadRequestException, JSONException, FileNotFoundException, UserNotFoundException, JsonProcessingException {
        String newUserParams = TestUtils.getFileToJson("json/happy/new_user.json");
        JSONObject newUserParamsJson = new JSONObject(newUserParams);
        String email = newUserParamsJson.optString("email");
        String username = newUserParamsJson.optString("username");
        String password = newUserParamsJson.optString("password");
        userService.createNewUser(email, username, password);
        String updateUserParams = TestUtils.getFileToJson("json/happy/existing_user.json");
        JSONObject updateUserParamsJson = new JSONObject(updateUserParams);
        String updatePassword = updateUserParamsJson.optString("password");
        userService.updatePassword(updatePassword, username);
        User updatedUser = userService.getUserByUsername(username);
        assertThat(passwordEncoder.matches(updatePassword, updatedUser.getPassword())).isTrue();
    }

    @Test
    public void should_reset_user_password() throws FileNotFoundException, JSONException, BadRequestException, JsonProcessingException, UserNotFoundException {
        String newUserParams = TestUtils.getFileToJson("json/happy/new_user.json");
        JSONObject newUserParamsJson = new JSONObject(newUserParams);
        String email = newUserParamsJson.optString("email");
        String username = newUserParamsJson.optString("username");
        String password = newUserParamsJson.optString("password");
        userService.createNewUser(email, username, password);
        userService.resetPassword(username);
        User updatedUser = userService.getUserByUsername(username);
        assertThat(passwordEncoder.matches(updatedUser.getPassword(), password)).isFalse();
    }

    @Test
    public void should_update_email() throws FileNotFoundException, JSONException, BadRequestException, JsonProcessingException, UserNotFoundException {
        String newUserParams = TestUtils.getFileToJson("json/happy/new_user.json");
        JSONObject newUserParamsJson = new JSONObject(newUserParams);
        String email = newUserParamsJson.optString("email");
        String username = newUserParamsJson.optString("username");
        String password = newUserParamsJson.optString("password");
        userService.createNewUser(email, username, password);
        String updateUserParams = TestUtils.getFileToJson("json/happy/existing_user.json");
        JSONObject updateUserParamsJson = new JSONObject(updateUserParams);
        String updateEmail = updateUserParamsJson.optString("email");
        userService.updateEmail(updateEmail, username);
        User updatedUser = userService.getUserByUsername(username);
        assertThat(updateEmail.equals(updatedUser.getEmail())).isTrue();
    }

    @Test
    public void should_update_user_info() throws IOException, BadRequestException, JSONException, UserNotFoundException {
        String newUserParams = TestUtils.getFileToJson("json/happy/new_user.json");
        JSONObject newUserParamsJson = new JSONObject(newUserParams);
        String email = newUserParamsJson.optString("email");
        String username = newUserParamsJson.optString("username");
        String password = newUserParamsJson.optString("password");
        userService.createNewUser(email, username, password);
        String updateUserParams = TestUtils.getFileToJson("json/happy/existing_user.json");
        userService.updateUserInfo(updateUserParams);
        JSONObject updateUserParamsJson = new JSONObject(updateUserParams);
        String usernameParam = updateUserParamsJson.optString("username");
        User updateUser = userService.getUserByUsername(usernameParam);
        User userFromUpdateRequest = gson.fromJson(updateUserParams, User.class);
        assertThat(userFromUpdateRequest.getFirstName()).isEqualTo(updateUser.getFirstName());
        assertThat(userFromUpdateRequest.getLastName()).isEqualTo(updateUser.getLastName());
    }

    @Test
    public void should_not_update_a_user_password() throws FileNotFoundException, JSONException, UserNotFoundException, JsonProcessingException {
        assertThatExceptionOfType(BadRequestException.class).isThrownBy(() -> {
            String newUserParams = TestUtils.getFileToJson("json/happy/new_user.json");
            JSONObject newUserParamsJson = new JSONObject(newUserParams);
            String email = newUserParamsJson.optString("email");
            String username = newUserParamsJson.optString("username");
            String password = newUserParamsJson.optString("password");
            userService.createNewUser(email, username, password);
            String updateUserParams = TestUtils.getFileToJson("json/edge/user_with_insecure_password.json");
            JSONObject updateUserParamsJson = new JSONObject(updateUserParams);
            String updatePassword = updateUserParamsJson.optString("password");
            userService.updatePassword(updatePassword, username);
        }).withMessageContaining(ErrorMessageContants.PASSWORD_FORMAT_REQUIREMENTS);
    }

    @Test
    public void should_not_update_a_user_email() throws FileNotFoundException, JSONException, UserNotFoundException, JsonProcessingException {
        assertThatExceptionOfType(BadRequestException.class).isThrownBy(() -> {
            String newUserParams = TestUtils.getFileToJson("json/happy/new_user.json");
            JSONObject newUserParamsJson = new JSONObject(newUserParams);
            String email = newUserParamsJson.optString("email");
            String username = newUserParamsJson.optString("username");
            String password = newUserParamsJson.optString("password");
            userService.createNewUser(email, username, password);
            String updateUserParams = TestUtils.getFileToJson("json/edge/user_with_malformed_email.json");
            JSONObject updateUserParamsJson = new JSONObject(updateUserParams);
            String updateEmail = updateUserParamsJson.optString("email");
            userService.updateEmail(updateEmail, username);
        }).withMessageContaining(ErrorMessageContants.EMAIL_FORMAT_REQUIREMENTS);
    }

    @Test
    public void should_return_user_by_username() throws IOException, BadRequestException, JSONException, UserNotFoundException {
        String newUserParams = TestUtils.getFileToJson("json/happy/new_user.json");
        JSONObject newUserParamsJson = new JSONObject(newUserParams);
        String email = newUserParamsJson.optString("email");
        String username = newUserParamsJson.optString("username");
        String password = newUserParamsJson.optString("password");
        userService.createNewUser(email, username, password);
        User foundUser = userService.getUserByUsername(username);
        User userFromUpdateRequest = gson.fromJson(newUserParams, User.class);
        assertThat(userFromUpdateRequest.getEmail()).isEqualTo(foundUser.getEmail());
        assertThat(userFromUpdateRequest.getFirstName()).isEqualTo(foundUser.getFirstName());
        assertThat(userFromUpdateRequest.getLastName()).isEqualTo(foundUser.getLastName());
    }

    private void createNewUser() throws BadRequestException, FileNotFoundException, JSONException {
        String newUserParams = TestUtils.getFileToJson("json/happy/new_user.json");
        JSONObject newUserParamsJson = new JSONObject(newUserParams);
        String email = newUserParamsJson.optString("email");
        String username = newUserParamsJson.optString("username");
        String password = newUserParamsJson.optString("password");
        userService.createNewUser(email, username, password);
    }
}
