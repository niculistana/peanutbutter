package com.somamission.peanutbutter;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.somamission.peanutbutter.constants.ErrorMessageConstants;
import com.somamission.peanutbutter.domain.User;
import com.somamission.peanutbutter.exception.BadRequestException;
import com.somamission.peanutbutter.exception.UserNotFoundException;
import com.somamission.peanutbutter.intf.IUserService;
import org.json.JSONException;
import org.json.JSONObject;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.transaction.annotation.Transactional;

import java.io.IOException;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatExceptionOfType;

@RunWith(SpringRunner.class)
@SpringBootTest
@Transactional
public class UserServiceTest {
    @Autowired
    private IUserService userService;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private ObjectMapper objectMapper;

    private static final String NEW_USER_PATH = "json/happy/new_user.json";
    private static final String EXISTING_USER_PATH = "json/happy/existing_user_update_email_password.json";
    private static final String WITH_INSECURE_PASSWORD_PATH = "json/edge/user_with_insecure_password.json";
    private static final String WITH_MALFORMED_EMAIL_PATH = "json/edge/user_with_malformed_email.json";
    private static final String EMAIL_FIELD = "email";
    private static final String USERNAME_FIELD = "username";
    private static final String PASSWORD_FIELD = "password";

    @Test
    public void shouldCreateAUser() throws IOException, BadRequestException, JSONException, UserNotFoundException {
        String newUserParams = TestUtils.getFileToJson(NEW_USER_PATH);
        JSONObject newUserParamsJson = new JSONObject(newUserParams);
        User newUserFromRepository = createAndFindNewUser(newUserParamsJson);
        User userFromCreateRequest = objectMapper.readValue(newUserParams, User.class);
        assertThat(userFromCreateRequest.getEmail()).isEqualTo(newUserFromRepository.getEmail());
        assertThat(userFromCreateRequest.getUsername()).isEqualTo(newUserFromRepository.getUsername());
        assertThat(passwordEncoder.matches(userFromCreateRequest.getPassword(), newUserFromRepository.getPassword())).isTrue();
    }

    @Test
    public void shouldUpdateUserPassword() throws BadRequestException, JSONException, UserNotFoundException {
        String newUserParams = TestUtils.getFileToJson(NEW_USER_PATH);
        JSONObject newUserParamsJson = new JSONObject(newUserParams);
        createNewUser(newUserParamsJson);
        String updateUserParams = TestUtils.getFileToJson(EXISTING_USER_PATH);
        JSONObject updateUserParamsJson = new JSONObject(updateUserParams);
        String updatePassword = updateUserParamsJson.optString(PASSWORD_FIELD);
        String username = updateUserParamsJson.optString(USERNAME_FIELD);
        userService.updatePassword(username, updatePassword);
        User updatedUser = userService.getUserByUsername(username);
        assertThat(passwordEncoder.matches(updatePassword, updatedUser.getPassword())).isTrue();
    }

    @Test
    public void shouldResetUserPassword() throws JSONException, BadRequestException, UserNotFoundException {
        String newUserParams = TestUtils.getFileToJson(NEW_USER_PATH);
        JSONObject newUserParamsJson = new JSONObject(newUserParams);
        String email = newUserParamsJson.optString(EMAIL_FIELD);
        String username = newUserParamsJson.optString(USERNAME_FIELD);
        String password = newUserParamsJson.optString(PASSWORD_FIELD);
        userService.createNewUser(username, email, password);
        userService.resetPassword(username);
        User updatedUser = userService.getUserByUsername(username);
        assertThat(passwordEncoder.matches(updatedUser.getPassword(), password)).isFalse();
    }

    @Test
    public void shouldUpdateEmail() throws JSONException, BadRequestException, UserNotFoundException {
        String newUserParams = TestUtils.getFileToJson(NEW_USER_PATH);
        JSONObject newUserParamsJson = new JSONObject(newUserParams);
        createNewUser(newUserParamsJson);
        String updateUserParams = TestUtils.getFileToJson(EXISTING_USER_PATH);
        JSONObject updateUserParamsJson = new JSONObject(updateUserParams);
        String updateEmail = updateUserParamsJson.optString(EMAIL_FIELD);
        String username = updateUserParamsJson.optString(USERNAME_FIELD);
        userService.updateEmail(username, updateEmail);
        User updatedUser = userService.getUserByUsername(username);
        assertThat(updateEmail.equals(updatedUser.getEmail())).isTrue();
    }

    @Test
    public void shouldUpdateUserInfoNames() throws IOException, BadRequestException, JSONException, UserNotFoundException {
        // TODO
    }

    @Test
    public void shouldNotUpdateUserPassword() {
        assertThatExceptionOfType(BadRequestException.class).isThrownBy(() -> {
            String newUserParams = TestUtils.getFileToJson(NEW_USER_PATH);
            JSONObject newUserParamsJson = new JSONObject(newUserParams);
            String email = newUserParamsJson.optString(EMAIL_FIELD);
            String username = newUserParamsJson.optString(USERNAME_FIELD);
            String password = newUserParamsJson.optString(PASSWORD_FIELD);
            userService.createNewUser(username, email, password);
            String updateUserParams = TestUtils.getFileToJson(WITH_INSECURE_PASSWORD_PATH);
            JSONObject updateUserParamsJson = new JSONObject(updateUserParams);
            String updatePassword = updateUserParamsJson.optString(PASSWORD_FIELD);
            userService.updatePassword(username, updatePassword);
        }).withMessageContaining(ErrorMessageConstants.PASSWORD_FORMAT_REQUIREMENTS);
    }

    @Test
    public void shouldNotUpdateUserEmail() {
        assertThatExceptionOfType(BadRequestException.class).isThrownBy(() -> {
            String newUserParams = TestUtils.getFileToJson(NEW_USER_PATH);
            JSONObject newUserParamsJson = new JSONObject(newUserParams);
            createNewUser(newUserParamsJson);
            String updateUserParams = TestUtils.getFileToJson(WITH_MALFORMED_EMAIL_PATH);
            JSONObject updateUserParamsJson = new JSONObject(updateUserParams);
            String updateEmail = updateUserParamsJson.optString(EMAIL_FIELD);
            String username = updateUserParamsJson.optString(USERNAME_FIELD);
            userService.updateEmail(username, updateEmail);
        }).withMessageContaining(ErrorMessageConstants.EMAIL_FORMAT_REQUIREMENTS);
    }

    @Test
    public void shouldNotCreateUser() throws IOException, BadRequestException, JSONException, UserNotFoundException {
        // TODO
    }

    @Test
    public void shouldReturnUserByUsername() throws IOException, BadRequestException, JSONException, UserNotFoundException {
        String newUserParams = TestUtils.getFileToJson(NEW_USER_PATH);
        JSONObject newUserParamsJson = new JSONObject(newUserParams);
        User newUserFromRepository = createAndFindNewUser(newUserParamsJson);
        User userFromUpdateRequest = objectMapper.readValue(newUserParams, User.class);
        assertThat(userFromUpdateRequest.getEmail()).isEqualTo(newUserFromRepository.getEmail());
        assertThat(userFromUpdateRequest.getFirstName()).isEqualTo(newUserFromRepository.getFirstName());
        assertThat(userFromUpdateRequest.getLastName()).isEqualTo(newUserFromRepository.getLastName());
    }

    private void createNewUser(JSONObject params) throws JSONException, BadRequestException, UserNotFoundException {
        String email = params.optString(EMAIL_FIELD);
        String username = params.optString(USERNAME_FIELD);
        String password = params.optString(PASSWORD_FIELD);
        userService.createNewUser(username, email, password);
    }

    private User createAndFindNewUser(JSONObject params) throws JSONException, BadRequestException, UserNotFoundException {
        createNewUser(params);
        String username = params.optString(USERNAME_FIELD);
        return userService.getUserByUsername(username);
    }
}
