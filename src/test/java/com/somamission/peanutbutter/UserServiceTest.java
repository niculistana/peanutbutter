package com.somamission.peanutbutter;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.somamission.peanutbutter.constants.ErrorMessageConstants;
import com.somamission.peanutbutter.domain.User;
import com.somamission.peanutbutter.exception.BadRequestException;
import com.somamission.peanutbutter.exception.UserNotFoundException;
import com.somamission.peanutbutter.intf.IUserService;
import com.somamission.peanutbutter.param.AddressParams;
import com.somamission.peanutbutter.param.NameParams;
import com.somamission.peanutbutter.param.PhotoParams;
import org.json.JSONException;
import org.json.JSONObject;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.transaction.annotation.Transactional;

import java.io.IOException;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatExceptionOfType;

@ExtendWith(SpringExtension.class)
@SpringBootTest
@Transactional
public class UserServiceTest {
  @Autowired private IUserService userService;

  @Autowired private PasswordEncoder passwordEncoder;

  @Autowired private ObjectMapper objectMapper;

  private static final String NEW_USER_PATH = "json/happy/new_user.json";
  private static final String WITH_NEW_USERNAME_PASSWORD_PATH =
      "json/happy/existing_user_update_email_password.json";
  private static final String WITH_NEW_INFO_PATH = "json/happy/existing_user_update_info.json";
  private static final String WITH_INSECURE_PASSWORD_PATH =
      "json/edge/user_with_insecure_password.json";
  private static final String WITH_MALFORMED_EMAIL_PATH =
      "json/edge/user_with_malformed_email.json";
  private static final String WITH_MALFORMED_PROFILE_URL_PATH =
      "json/edge/user_with_malformed_profile_url.json";
  private static final String WITH_INVALID_USERNAME = "json/edge/user_with_invalid_username.json";
  private static final String EMAIL_FIELD = "email";
  private static final String USERNAME_FIELD = "username";
  private static final String PASSWORD_FIELD = "password";

  @BeforeEach
  void initEach() throws UserNotFoundException, BadRequestException, JSONException {
    createNewUser();
  }

  @Test
  @DisplayName("Should create a user")
  public void shouldCreateAUser()
      throws IOException, BadRequestException, JSONException, UserNotFoundException {
    String newUserParams = TestUtils.getFileToJson(NEW_USER_PATH);
    User newUserFromRepository = findNewUser();
    User userFromCreateRequest = objectMapper.readValue(newUserParams, User.class);
    assertThat(userFromCreateRequest.getEmail()).isEqualTo(newUserFromRepository.getEmail());
    assertThat(userFromCreateRequest.getUsername()).isEqualTo(newUserFromRepository.getUsername());
    assertThat(
            passwordEncoder.matches(
                userFromCreateRequest.getPassword(), newUserFromRepository.getPassword()))
        .isTrue();
  }

  @Test
  @DisplayName("Should update an existing user's password")
  public void shouldUpdateUserPassword()
      throws BadRequestException, JSONException, UserNotFoundException {
    String existingUserParams = TestUtils.getFileToJson(WITH_NEW_USERNAME_PASSWORD_PATH);
    JSONObject existingUserParamsJson = new JSONObject(existingUserParams);
    String password = existingUserParamsJson.optString(PASSWORD_FIELD);
    String username = existingUserParamsJson.optString(USERNAME_FIELD);
    userService.updatePassword(username, password);
    User updatedUser = userService.getUserByUsername(username);
    assertThat(passwordEncoder.matches(password, updatedUser.getPassword())).isTrue();
  }

  @Test
  @DisplayName("Should reset an existing user's password")
  public void shouldResetUserPassword()
      throws JSONException, BadRequestException, UserNotFoundException {
    String existingUserParams = TestUtils.getFileToJson(WITH_NEW_USERNAME_PASSWORD_PATH);
    JSONObject existingUserParamsJson = new JSONObject(existingUserParams);
    String username = existingUserParamsJson.optString(USERNAME_FIELD);
    User existingUser = userService.getUserByUsername(username);
    String previousPassword = existingUser.getPassword();
    userService.resetPassword(username);
    User updatedUser = userService.getUserByUsername(username);
    assertThat(passwordEncoder.matches(updatedUser.getPassword(), previousPassword)).isFalse();
  }

  @Test
  @DisplayName("Should update an existing user's email")
  public void shouldUpdateEmail() throws JSONException, BadRequestException, UserNotFoundException {
    String existingUserParams = TestUtils.getFileToJson(WITH_NEW_USERNAME_PASSWORD_PATH);
    JSONObject existingUserParamsJson = new JSONObject(existingUserParams);
    String updateEmail = existingUserParamsJson.optString(EMAIL_FIELD);
    String username = existingUserParamsJson.optString(USERNAME_FIELD);
    userService.updateEmail(username, updateEmail);
    User updatedUser = userService.getUserByUsername(username);
    assertThat(updateEmail.equals(updatedUser.getEmail())).isTrue();
  }

  @Test
  @DisplayName("Should update an existing user's first name and last name")
  public void shouldUpdateUserInfo()
      throws IOException, BadRequestException, JSONException, UserNotFoundException {
    String existingUserParams = TestUtils.getFileToJson(WITH_NEW_INFO_PATH);
    JSONObject existingUserParamsJson = new JSONObject(existingUserParams);
    String username = existingUserParamsJson.optString(USERNAME_FIELD);
    // Update names
    JSONObject nameParamsJson = existingUserParamsJson.optJSONObject("nameParams");
    String firstName = nameParamsJson.optString("firstName");
    String lastName = nameParamsJson.optString("lastName");
    NameParams nameParams =
        new NameParams.Builder().withFirstName(firstName).withLastName(lastName).build();
    userService.updateUserInfo(username, nameParams);
    User updatedUser = userService.getUserByUsername(username);
    assertThat(updatedUser.getFirstName().equals(firstName)).isTrue();
    assertThat(updatedUser.getLastName().equals(lastName)).isTrue();
    JSONObject photoParamsJson = existingUserParamsJson.optJSONObject("photoParams");
    String profileUrl = photoParamsJson.optString("profileUrl");
    String coverUrl = photoParamsJson.optString("coverUrl");
    PhotoParams photoParams =
        new PhotoParams.Builder().withProfileUrl(profileUrl).withCoverUrl(coverUrl).build();
    // Update photos
    userService.updateUserInfo(username, photoParams);
    updatedUser = userService.getUserByUsername(username);
    assertThat(updatedUser.getProfilePhotoUrl().equals(photoParams.getProfileUrl())).isTrue();
    assertThat(updatedUser.getCoverPhotoUrl().equals(photoParams.getCoverUrl())).isTrue();
    JSONObject addressParamsJson = existingUserParamsJson.optJSONObject("addressParams");
    String addressLineOne = addressParamsJson.optString("addressLineOne");
    String addressLineTwo = addressParamsJson.optString("lastName");
    String city = addressParamsJson.optString("city");
    String state = addressParamsJson.optString("state");
    String zip = addressParamsJson.optString("zip");
    String country = addressParamsJson.optString("country");
    // Update address
    AddressParams addressParams =
        new AddressParams.Builder()
            .withAddressLineOne(addressLineOne)
            .withAddressLineTwo(addressLineTwo)
            .withCity(city)
            .withState(state)
            .withZip(zip)
            .withCountry(country)
            .build();
    userService.updateUserInfo(username, addressParams);
    updatedUser = userService.getUserByUsername(username);
    assertThat(updatedUser.getFullAddress().equals(addressParams.getFullAddress())).isTrue();
  }

  @Test
  @DisplayName("Should not update an existing user's password if the password is not secure")
  public void shouldNotUpdateUserPassword() {
    assertThatExceptionOfType(BadRequestException.class)
        .isThrownBy(
            () -> {
              String existingUserParams = TestUtils.getFileToJson(WITH_INSECURE_PASSWORD_PATH);
              JSONObject existingUserParamsJson = new JSONObject(existingUserParams);
              String username = existingUserParamsJson.optString(USERNAME_FIELD);
              String password = existingUserParamsJson.optString(PASSWORD_FIELD);
              userService.updatePassword(username, password);
            })
        .withMessageContaining(ErrorMessageConstants.PASSWORD_FORMAT_REQUIREMENTS);
  }

  @Test
  @DisplayName(
      "Should not update an existing user's password if the email is not correctly formatted")
  public void shouldNotUpdateUserEmail() {
    assertThatExceptionOfType(BadRequestException.class)
        .isThrownBy(
            () -> {
              String existingUserParams = TestUtils.getFileToJson(WITH_MALFORMED_EMAIL_PATH);
              JSONObject existingUserParamsJson = new JSONObject(existingUserParams);
              String email = existingUserParamsJson.optString(EMAIL_FIELD);
              String username = existingUserParamsJson.optString(USERNAME_FIELD);
              userService.updateEmail(username, email);
            })
        .withMessageContaining(ErrorMessageConstants.EMAIL_FORMAT_REQUIREMENTS);
  }

  @Test
  @DisplayName(
      "Should not update an existing user's profile photo if url is not correctly formatted")
  public void shouldNotUpdateUserProfilePhotoUrl() {
    assertThatExceptionOfType(BadRequestException.class)
        .isThrownBy(
            () -> {
              String existingUserParams = TestUtils.getFileToJson(WITH_MALFORMED_PROFILE_URL_PATH);
              JSONObject existingUserParamsJson = new JSONObject(existingUserParams);
              String username = existingUserParamsJson.optString(USERNAME_FIELD);
              JSONObject photoParamsJson = existingUserParamsJson.optJSONObject("photoParams");
              String profileUrl = photoParamsJson.optString("profileUrl");
              PhotoParams photoParams =
                  new PhotoParams.Builder().withProfileUrl(profileUrl).build();
              userService.updateUserInfo(username, photoParams);
            })
        .withMessageContaining(ErrorMessageConstants.URL_FORMAT_REQUIREMENTS);
  }

  @Test
  @DisplayName("Should not update an existing user's password if the username is invalid")
  public void shouldNotCreateUser() {
    assertThatExceptionOfType(BadRequestException.class)
        .isThrownBy(
            () -> {
              String newUserParams = TestUtils.getFileToJson(WITH_INVALID_USERNAME);
              JSONObject params = new JSONObject(newUserParams);
              String email = params.optString(EMAIL_FIELD);
              String username = params.optString(USERNAME_FIELD);
              String password = params.optString(PASSWORD_FIELD);
              userService.createNewUser(username, email, password);
            });
  }

  @Test
  @DisplayName("Should get an existing user by username")
  public void shouldGetUserByUsername()
      throws IOException, BadRequestException, JSONException, UserNotFoundException {
    String newUserParams = TestUtils.getFileToJson(NEW_USER_PATH);
    User newUserFromRepository = findNewUser();
    User userFromUpdateRequest = objectMapper.readValue(newUserParams, User.class);
    assertThat(userFromUpdateRequest.getEmail()).isEqualTo(newUserFromRepository.getEmail());
    assertThat(userFromUpdateRequest.getFirstName())
        .isEqualTo(newUserFromRepository.getFirstName());
    assertThat(userFromUpdateRequest.getLastName()).isEqualTo(newUserFromRepository.getLastName());
  }

  /** Creates a new user using correct params */
  private void createNewUser() throws JSONException, BadRequestException, UserNotFoundException {
    String newUserParams = TestUtils.getFileToJson(NEW_USER_PATH);
    JSONObject params = new JSONObject(newUserParams);
    String email = params.optString(EMAIL_FIELD);
    String username = params.optString(USERNAME_FIELD);
    String password = params.optString(PASSWORD_FIELD);
    userService.createNewUser(username, email, password);
  }

  /**
   * Finds the new user created
   *
   * @return the user found
   */
  private User findNewUser() throws JSONException, BadRequestException, UserNotFoundException {
    String newUserParams = TestUtils.getFileToJson(NEW_USER_PATH);
    JSONObject params = new JSONObject(newUserParams);
    String username = params.optString(USERNAME_FIELD);
    return userService.getUserByUsername(username);
  }
}
