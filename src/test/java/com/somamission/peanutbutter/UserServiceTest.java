package com.somamission.peanutbutter;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.google.gson.Gson;
import com.somamission.peanutbutter.intf.IUserService;
import com.somamission.peanutbutter.constants.ErrorMessageContants;
import com.somamission.peanutbutter.domain.User;
import com.somamission.peanutbutter.exception.BadRequestException;
import com.somamission.peanutbutter.exception.UserNotFoundException;
import org.json.JSONException;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
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

    private Gson gson = new Gson();

    @Test
    public void should_create_a_user() throws IOException, BadRequestException, JSONException, UserNotFoundException {
        String newUserParams = TestUtils.getFileToJson("json/happy/new_user.json");
        userService.insertUserDetails(newUserParams);
        User userFromCreateRequest = gson.fromJson(newUserParams, User.class);
        String newUserJsonStr = userService.getUserByUsername(userFromCreateRequest.getUsername());
        User userCreatedFromRequest = gson.fromJson(newUserJsonStr, User.class);
        assertThat(userFromCreateRequest.getEmail()).isEqualTo(userCreatedFromRequest.getEmail());
        assertThat(userFromCreateRequest.getUsername()).isEqualTo(userCreatedFromRequest.getUsername());
    }

    @Test
    public void should_update_a_user() throws FileNotFoundException, BadRequestException, JSONException, UserNotFoundException, JsonProcessingException {
        String newUserParams = TestUtils.getFileToJson("json/happy/new_user.json");
        userService.insertUserDetails(newUserParams);
        String updateUserParams = TestUtils.getFileToJson("json/happy/existing_user.json");
        userService.updateUser(updateUserParams);
        String updateUserJsonStr = userService.getUserById(1L);
        User userFromUpdateRequest = gson.fromJson(updateUserParams, User.class);
        User userUpdateFromRequest = gson.fromJson(updateUserJsonStr, User.class);
        assertThat(userFromUpdateRequest.getFirstName()).isEqualTo(userUpdateFromRequest.getFirstName());
        assertThat(userFromUpdateRequest.getLastName()).isEqualTo(userUpdateFromRequest.getLastName());
    }

    @Test
    public void should_not_update_a_user_username() throws FileNotFoundException, JSONException, UserNotFoundException, JsonProcessingException {
        assertThatExceptionOfType(BadRequestException.class).isThrownBy(() -> {
            String newUserParams = TestUtils.getFileToJson("json/happy/new_user.json");
            userService.insertUserDetails(newUserParams);
            String updateUserParams = TestUtils.getFileToJson("json/edge/user_with_short_username.json");
            userService.updateUser(updateUserParams);
        }).withMessageContaining(ErrorMessageContants.USERNAME_FORMAT_REQUIREMENTS);
    }


    @Test
    public void should_not_update_a_user_password() throws FileNotFoundException, JSONException, UserNotFoundException, JsonProcessingException {
        assertThatExceptionOfType(BadRequestException.class).isThrownBy(() -> {
            String newUserParams = TestUtils.getFileToJson("json/happy/new_user.json");
            userService.insertUserDetails(newUserParams);
            String updateUserParams = TestUtils.getFileToJson("json/edge/user_with_insecure_password.json");
            userService.updateUser(updateUserParams);
        }).withMessageContaining(ErrorMessageContants.PASSWORD_FORMAT_REQUIREMENTS);
    }

    @Test
    public void should_not_update_a_user_email() throws FileNotFoundException, JSONException, UserNotFoundException, JsonProcessingException {
        assertThatExceptionOfType(BadRequestException.class).isThrownBy(() -> {
            String newUserParams = TestUtils.getFileToJson("json/happy/new_user.json");
            userService.insertUserDetails(newUserParams);
            String updateUserParams = TestUtils.getFileToJson("json/edge/user_with_unsupported_email.json");
            userService.updateUser(updateUserParams);
        }).withMessageContaining(ErrorMessageContants.EMAIL_FORMAT_REQUIREMENTS);
    }

    @Test
    public void should_return_user_by_username() throws IOException, BadRequestException, JSONException, UserNotFoundException {
        String newUserParams = TestUtils.getFileToJson("json/happy/new_user.json");
        userService.insertUserDetails(newUserParams);
        String foundUserStr = userService.getUserByUsername("fbar");
        User foundUser = gson.fromJson(foundUserStr, User.class);
        User userFromUpdateRequest = gson.fromJson(newUserParams, User.class);
        assertThat(userFromUpdateRequest.getEmail()).isEqualTo(foundUser.getEmail());
        assertThat(userFromUpdateRequest.getFirstName()).isEqualTo(foundUser.getFirstName());
        assertThat(userFromUpdateRequest.getLastName()).isEqualTo(foundUser.getLastName());
    }
}
