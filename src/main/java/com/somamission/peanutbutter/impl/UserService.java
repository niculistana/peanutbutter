package com.somamission.peanutbutter.impl;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.googlecode.jsonrpc4j.spring.AutoJsonRpcServiceImpl;
import com.somamission.peanutbutter.domain.User;
import com.somamission.peanutbutter.intf.IUserService;
import com.somamission.peanutbutter.constants.ErrorMessageContants;
import com.somamission.peanutbutter.exception.BadRequestException;
import com.somamission.peanutbutter.exception.ObjectNotFoundException;
import com.somamission.peanutbutter.exception.UserNotFoundException;
import com.somamission.peanutbutter.param.UserParams;
import com.somamission.peanutbutter.repository.IUserRepository;
import org.apache.commons.lang3.StringUtils;
import org.passay.*;
import org.redisson.api.RBucket;
import org.redisson.api.RedissonClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

@Service
@AutoJsonRpcServiceImpl
public class UserService implements IUserService {
    private static Logger logger = LoggerFactory.getLogger(UserService.class);

    @Autowired
    IUserRepository userRepository;

    @Autowired
    ObjectMapper objectMapper;

    @Autowired
    PasswordEncoder passwordEncoder;

    @Autowired
    RedissonClient redissonClient;

    @Override
    public User getUserByUsername(String username) throws JsonProcessingException, UserNotFoundException {
        logger.info("Getting user by username");
        try {
            RBucket<User> bucket = redissonClient.getBucket(username);
            User user = bucket.get();
            if (user != null) {
                return user;
            }
            user = userRepository.findByUsername(username).orElseThrow(ObjectNotFoundException::new);
            bucket.set(user);
            return user;
        } catch (ObjectNotFoundException e) {
            String notFoundMessage = "username " + username + " not found";
            logger.info(notFoundMessage);
            throw new UserNotFoundException(notFoundMessage);
        }
    }

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        // TODO implement this
        return null;
    }

    @Override
    public void createNewUser(String email, String username, String password) throws BadRequestException {
        logger.info("Inserting new user");

        if (StringUtils.isEmpty(email)) {
            logger.info(ErrorMessageContants.REQUIRED_PARAMETER_NOT_FOUND + ": email");
            throw new BadRequestException(ErrorMessageContants.REQUIRED_PARAMETER_NOT_FOUND + ": email");
        }
        if (StringUtils.isEmpty(username)) {
            logger.info(ErrorMessageContants.REQUIRED_PARAMETER_NOT_FOUND + ": username");
            throw new BadRequestException(ErrorMessageContants.REQUIRED_PARAMETER_NOT_FOUND + ": username");
        }
        if (StringUtils.isEmpty(password)) {
            logger.info(ErrorMessageContants.REQUIRED_PARAMETER_NOT_FOUND + ": password");
            throw new BadRequestException(ErrorMessageContants.REQUIRED_PARAMETER_NOT_FOUND + ": password");
        }

        User newUser = new User();
        newUser.setEmail(email);
        newUser.setUsername(username);
        newUser.setPassword(passwordEncoder.encode(password));
        userRepository.save(newUser);
        RBucket<User> bucket = redissonClient.getBucket(username);
        bucket.set(newUser);
    }

    @Override
    public void updatePassword(String password, String username) throws BadRequestException, JsonProcessingException, UserNotFoundException {
        logger.info("Updating user password");
        if (StringUtils.isEmpty(username)) {
            logger.info(ErrorMessageContants.REQUIRED_PARAMETER_NOT_FOUND + ": username");
            throw new BadRequestException(ErrorMessageContants.REQUIRED_PARAMETER_NOT_FOUND + ": username");
        }

        if (!isPasswordValid(password)) {
            String passwordIsNotSecureEnoughMessage = "password is invalid. Requirements: " + ErrorMessageContants.PASSWORD_FORMAT_REQUIREMENTS;
            logger.info(passwordIsNotSecureEnoughMessage);
            throw new BadRequestException(passwordIsNotSecureEnoughMessage);
        }

        User user = getUserByUsername(username);
        UserParams userParams = new UserParams.Builder().withPassword(password).build();
        updateUser(user, userParams);
    }

    @Override
    public void resetPassword(String username) throws UserNotFoundException, BadRequestException, JsonProcessingException {
        logger.info("Resetting user password");
        updatePassword(generatePassword(), username);
    }

    private void updateUser(User user, UserParams userParams) {
        if (!StringUtils.isEmpty(userParams.getEmail())) {
            user.setEmail(userParams.getEmail());
        }

        if (!StringUtils.isEmpty(userParams.getPassword())) {
            user.setPassword(passwordEncoder.encode(userParams.getPassword()));
        }

        userRepository.save(user);
        RBucket<User> bucket = redissonClient.getBucket(user.getUsername());
        bucket.set(user);
    }

//    private void updateUser(String requestParams) throws BadRequestException, UserNotFoundException {
//        logger.info("Updating existing user");
//        if (StringUtils.isEmpty(requestParams)) {
//            logger.info(ErrorMessageContants.EMPTY_PARAMETER_MESSAGE);
//            throw new BadRequestException(ErrorMessageContants.EMPTY_PARAMETER_MESSAGE);
//        }
//        JSONObject userJson = new JSONObject(requestParams);
//        long userId = userJson.optLong("userId");
//        User user = null;
//        try {
//            // use findUserById because you want to get most recent and non-cached data
//            user = findUserById(userId);
//        } catch (ObjectNotFoundException e) {
//            String notFoundMessage = "Cannot update user with user id, reason: user id " + userId + " not found";
//            logger.info(notFoundMessage);
//            throw new UserNotFoundException(notFoundMessage);
//        }
//
//        String email = userJson.optString("email");
//        String username = userJson.optString("username");
//        String password = userJson.optString("password");
//        String firstName = userJson.optString("firstName");
//        String lastName = userJson.optString("lastName");
//
//
//        if (!StringUtils.isEmpty(email) && !email.equals(user.getEmail())) {
//            updateEmail(user, email);
//        }
//
//        if (!StringUtils.isEmpty(username) && !username.equals(user.getUsername())) {
//            updateUsername(user, username);
//        }
//
//        // password history checking is in updatePassword method
//        if (!StringUtils.isEmpty(password)) {
//            updatePassword(user, password);
//        }
//
//        if (!StringUtils.isEmpty(firstName) && !firstName.equals(user.getFirstName())) {
//            user.setFirstName(firstName);
//        }
//
//        if (!StringUtils.isEmpty(lastName) && !lastName.equals(user.getLastName())) {
//            user.setLastName(lastName);
//        }
//
//        userRepository.save(user);
//        RBucket<User> bucket = redissonClient.getBucket(username);
//        bucket.set(user);
//    }

    private User findUserById(Long userId) throws ObjectNotFoundException {
        return userRepository.findById(userId).orElseThrow(ObjectNotFoundException::new);
    }

    private void updateEmail(User user, String email) throws BadRequestException {
        boolean isEmailValid = isEmailValid(email);
        if (!isEmailValid) {
            String emailAlreadyExistsMessage = "email: " + email + " is invalid. Requirements: " + ErrorMessageContants.EMAIL_FORMAT_REQUIREMENTS;
            logger.info(emailAlreadyExistsMessage);
            throw new BadRequestException(emailAlreadyExistsMessage);
        }
        user.setEmail(email);
    }


    private void updateUsername(User user, String username) throws BadRequestException {
        boolean isUsernameValid = isUsernameValid(username);
        if (!isUsernameValid) {
            String usernameAlreadyExistsMessage = "username: " + username + " is invalid. Requirements: " + ErrorMessageContants.USERNAME_FORMAT_REQUIREMENTS;
            logger.info(usernameAlreadyExistsMessage);
            throw new BadRequestException(usernameAlreadyExistsMessage);
        }
        user.setUsername(username);
    }

    private boolean isEmailValid(String username) {
        // TODO: implement this
        boolean isEmailUnique = true;
        boolean emailHasCorrectHost = true;
        return isEmailUnique && emailHasCorrectHost;
    }

    private boolean isUsernameValid(String username) {
        // TODO: implement this
        boolean isUsernameUnique = true;
        boolean usernameHasEnoughCharacters = true;
        return isUsernameUnique && usernameHasEnoughCharacters;
    }

    private boolean isPasswordValid(String password) {
        PasswordValidator validator = new PasswordValidator(
                new LengthRule(8, 16),
                new CharacterRule(EnglishCharacterData.UpperCase, 1),
                new CharacterRule(EnglishCharacterData.LowerCase, 1),
                new CharacterRule(EnglishCharacterData.Digit, 1),
                new CharacterRule(EnglishCharacterData.Special, 1),
                new WhitespaceRule());

        RuleResult result = validator.validate(new PasswordData(password));
        return result.isValid();
    }

    private void addMessageToNotificationQueue(String message) {
        // TODO: implement this
    }

    private String generatePassword() {
        List<CharacterRule> rules = Arrays.asList(
                new CharacterRule(EnglishCharacterData.UpperCase, 1),
                new CharacterRule(EnglishCharacterData.LowerCase, 1),
                new CharacterRule(EnglishCharacterData.Digit, 1),
                new CharacterRule(EnglishCharacterData.Special, 1));

        PasswordGenerator generator = new PasswordGenerator();
        return generator.generatePassword(12, rules);
    }
}