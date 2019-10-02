package com.somamission.peanutbutter.impl;

import com.somamission.peanutbutter.constants.ErrorMessageConstants;
import com.somamission.peanutbutter.domain.ReservedWord;
import com.somamission.peanutbutter.domain.User;
import com.somamission.peanutbutter.exception.BadRequestException;
import com.somamission.peanutbutter.exception.ObjectNotFoundException;
import com.somamission.peanutbutter.exception.UserFoundException;
import com.somamission.peanutbutter.exception.UserNotFoundException;
import com.somamission.peanutbutter.intf.IReservedWordService;
import com.somamission.peanutbutter.intf.IUserService;
import com.somamission.peanutbutter.param.AddressParams;
import com.somamission.peanutbutter.param.NameParams;
import com.somamission.peanutbutter.param.PhotoParams;
import com.somamission.peanutbutter.param.UserParams;
import com.somamission.peanutbutter.repository.IUserRepository;
import org.apache.commons.lang3.StringUtils;
import org.passay.*;
import org.redisson.api.RMapCache;
import org.redisson.api.RedissonClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.CacheManager;
import org.springframework.cache.annotation.CachePut;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import javax.validation.ConstraintViolation;
import javax.validation.Validator;
import java.util.Arrays;
import java.util.List;
import java.util.Set;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

@Service
public class UserService implements IUserService {
  private static final String USERNAME_INVALID_MSG =
      ErrorMessageConstants.REQUIRED_PARAMETER_NOT_FOUND + ": username";
  private static final String EMAIL_INVALID_MSG =
      ErrorMessageConstants.REQUIRED_PARAMETER_NOT_FOUND + ": email";
  private static final String PASSWORD_INVALID_MSG =
      ErrorMessageConstants.REQUIRED_PARAMETER_NOT_FOUND + ": password";
  private static final String USER_ALREADY_EXISTS_MSG = "User already exists";
  private static final Logger logger = LoggerFactory.getLogger(UserService.class);

  @Autowired private IUserRepository userRepository;

  @Autowired private IReservedWordService reservedWordService;

  @Autowired private PasswordEncoder passwordEncoder;

  @Autowired private RedissonClient redissonClient;

  @Autowired private CacheManager cacheManager;

  @Autowired private Validator validator;

  @Override
  @Cacheable(cacheNames = "users", key = "#username")
  public User getUserByUsername(String username) throws UserNotFoundException {
    logger.info("Getting user by username");
    try {
      if (StringUtils.isEmpty(username)) {
        throw new ObjectNotFoundException();
      }
      return userRepository.findByUsername(username).orElseThrow(ObjectNotFoundException::new);
    } catch (ObjectNotFoundException e) {
      String notFoundMessage = "username " + username + " not found";
      logger.info(notFoundMessage);
      throw new UserNotFoundException(notFoundMessage);
    }
  }

  @Override
  public UserDetails loadUserByUsername(String username) {
    throw new UnsupportedOperationException();
  }

  @Override
  public void createNewUser(String username, String email, String password)
      throws BadRequestException, UserFoundException {
    logger.info("Inserting new user");

    if (userRepository.findByUsername(username).isPresent()) {
      String conflictMessage = USER_ALREADY_EXISTS_MSG + ": " + username;
      logger.info(conflictMessage);
      throw new UserFoundException(conflictMessage);
    }

    if (StringUtils.isEmpty(email)) {
      logger.info(EMAIL_INVALID_MSG);
      throw new BadRequestException(EMAIL_INVALID_MSG);
    }
    if (StringUtils.isEmpty(username)) {
      logger.info(USERNAME_INVALID_MSG);
      throw new BadRequestException(USERNAME_INVALID_MSG);
    }
    if (StringUtils.isEmpty(password)) {
      logger.info(PASSWORD_INVALID_MSG);
      throw new BadRequestException(PASSWORD_INVALID_MSG);
    }

    if (!isUsernameValid(username)) {
      String usernameNotValidMessage =
          username
              + " username is invalid. Requirements: "
              + ErrorMessageConstants.USERNAME_FORMAT_REQUIREMENTS;
      logger.info(usernameNotValidMessage);
      throw new BadRequestException(usernameNotValidMessage);
    }

    if (isPasswordValid(password)) {
      String notSecureEnoughMessage =
          "password is invalid. Requirements: "
              + ErrorMessageConstants.PASSWORD_FORMAT_REQUIREMENTS;
      logger.info(notSecureEnoughMessage);
      throw new BadRequestException(notSecureEnoughMessage);
    }

    User user = new User();
    user.setEmail(email);
    user.setUsername(username);
    user.setPassword(passwordEncoder.encode(password));

    Set<ConstraintViolation<User>> errors = validator.validate(user);
    if (!errors.isEmpty()) {
      handleValidationErrors(errors);
    }

    userRepository.save(user);
    updateUsersCacheForUsername(username, user);
  }

  @Override
  public void updatePassword(String username, String password)
      throws BadRequestException, UserNotFoundException {
    logger.info("Updating user password");
    if (StringUtils.isEmpty(username)) {
      logger.info(USERNAME_INVALID_MSG);
      throw new BadRequestException(USERNAME_INVALID_MSG);
    }

    if (isPasswordValid(password)) {
      String passwordIsNotSecureEnoughMessage =
          "password is invalid. Requirements: "
              + ErrorMessageConstants.PASSWORD_FORMAT_REQUIREMENTS;
      logger.info(passwordIsNotSecureEnoughMessage);
      throw new BadRequestException(passwordIsNotSecureEnoughMessage);
    }

    UserParams userParams =
        new UserParams.Builder().withUsername(username).withPassword(password).build();
    updateUser(userParams);
  }

  @Override
  public void resetPassword(String username) throws UserNotFoundException, BadRequestException {
    logger.info("Resetting user password");
    updatePassword(username, generatePassword());
  }

  @Override
  public void updateEmail(String username, String email)
      throws BadRequestException, UserNotFoundException {
    logger.info("Updating user email");
    if (StringUtils.isEmpty(username)) {
      logger.info(USERNAME_INVALID_MSG);
      throw new BadRequestException(USERNAME_INVALID_MSG);
    }

    UserParams userParams =
        new UserParams.Builder().withUsername(username).withEmail(email).build();
    updateUser(userParams);
  }

  @Override
  public void updateUserInfo(String username, NameParams nameParams)
      throws UserNotFoundException, BadRequestException {
    UserParams userParams =
        new UserParams.Builder().withUsername(username).withNameParams(nameParams).build();
    this.updateUser(userParams);
  }

  @Override
  public void updateUserInfo(String username, PhotoParams photoParams)
      throws BadRequestException, UserNotFoundException {
    UserParams userParams =
        new UserParams.Builder().withUsername(username).withPhotoParams(photoParams).build();
    this.updateUser(userParams);
  }

  @Override
  public void updateUserInfo(String username, AddressParams addressParams)
      throws UserNotFoundException, BadRequestException {
    UserParams userParams =
        new UserParams.Builder().withUsername(username).withAddressParams(addressParams).build();
    this.updateUser(userParams);
  }

  @Override
  public void updateUserInfo(
      String username, NameParams nameParams, PhotoParams photoParams, AddressParams addressParams)
      throws UserNotFoundException, BadRequestException {
    UserParams userParams =
        new UserParams.Builder()
            .withUsername(username)
            .withNameParams(nameParams)
            .withPhotoParams(photoParams)
            .withAddressParams(addressParams)
            .build();
    this.updateUser(userParams);
  }

  private void updateUser(UserParams userParams) throws UserNotFoundException, BadRequestException {
    String username = userParams.getUsername();
    User user;
    try {
      user = userRepository.findByUsername(username).orElseThrow(ObjectNotFoundException::new);
    } catch (ObjectNotFoundException e) {
      throw new UserNotFoundException(username);
    }

    if (!StringUtils.isEmpty(userParams.getEmail())) {
      user.setEmail(StringUtils.trim(userParams.getEmail()));
    }

    if (!StringUtils.isEmpty(userParams.getPassword())) {
      user.setPassword(passwordEncoder.encode(StringUtils.trim(userParams.getPassword())));
    }

    if (null != userParams.getNameParams()) {
      NameParams nameParams = userParams.getNameParams();
      if (!StringUtils.isEmpty(nameParams.getFirstName())) {
        user.setFirstName(StringUtils.trim(nameParams.getFirstName()));
      }

      if (!StringUtils.isEmpty(nameParams.getLastName())) {
        user.setLastName(StringUtils.trim(nameParams.getLastName()));
      }
    }

    if (null != userParams.getPhotoParams()) {
      PhotoParams photoParams = userParams.getPhotoParams();
      if (!StringUtils.isEmpty(photoParams.getProfileUrl())) {
        user.setProfilePhotoUrl(photoParams.getProfileUrl());
      }

      if (!StringUtils.isEmpty(photoParams.getCoverUrl())) {
        user.setCoverPhotoUrl(photoParams.getCoverUrl());
      }
    }

    if (null != userParams.getAddressParams()
        && !StringUtils.isEmpty(userParams.getAddressParams().getFullAddress())) {
      user.setFullAddress(StringUtils.trim(userParams.getAddressParams().getFullAddress()));
    }

    Set<ConstraintViolation<User>> errors = validator.validate(user);
    if (!errors.isEmpty()) {
      handleValidationErrors(errors);
    }

    userRepository.save(user);
    updateUsersCacheForUsername(username, user);
  }

  // can this be part of hibernate validator?
  private boolean isUsernameValid(String username) {
    List<String> reservedWords =
        reservedWordService.getAllReservedWords().stream()
            .map(ReservedWord::getWord)
            .collect(Collectors.toList());
    return !reservedWords.contains(username);
  }

  // can this be part of hibernate validator?
  private boolean isPasswordValid(String password) {
    // should we have a special character rule?
    PasswordValidator passwordValidator =
        new PasswordValidator(
            new LengthRule(8, 128),
            new CharacterRule(EnglishCharacterData.UpperCase, 1),
            new CharacterRule(EnglishCharacterData.LowerCase, 1),
            new CharacterRule(EnglishCharacterData.Digit, 1),
            new WhitespaceRule());

    RuleResult result = passwordValidator.validate(new PasswordData(password));
    return !result.isValid();
  }

  private String generatePassword() {
    List<CharacterRule> rules =
        Arrays.asList(
            new CharacterRule(EnglishCharacterData.UpperCase, 1),
            new CharacterRule(EnglishCharacterData.LowerCase, 1),
            new CharacterRule(EnglishCharacterData.Digit, 1),
            new CharacterRule(EnglishCharacterData.Special, 1));

    PasswordGenerator generator = new PasswordGenerator();
    return generator.generatePassword(12, rules);
  }

  private void handleValidationErrors(Set<ConstraintViolation<User>> errors)
      throws BadRequestException {
    String errorMessage =
        StringUtils.join(errors.stream().map(ConstraintViolation::getMessage).toArray(), "\n");
    logger.info(errorMessage);
    throw new BadRequestException(errorMessage);
  }

  @CachePut(cacheNames = "users", key = "#username")
  private void updateUsersCacheForUsername(String username, User user) {
    RMapCache<String, User> userRMapCache = redissonClient.getMapCache("users");
    userRMapCache.put(username, user, 30, TimeUnit.MINUTES);
  }
}
