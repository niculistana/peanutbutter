package com.somamission.peanutbutter.impl;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.somamission.peanutbutter.domain.User;
import com.somamission.peanutbutter.intf.IUserService;
import com.somamission.peanutbutter.constants.ErrorMessageContants;
import com.somamission.peanutbutter.exception.BadRequestException;
import com.somamission.peanutbutter.exception.ObjectNotFoundException;
import com.somamission.peanutbutter.exception.UserNotFoundException;
import com.somamission.peanutbutter.repository.IUserRepository;
import org.apache.commons.lang3.StringUtils;
import org.json.JSONException;
import org.json.JSONObject;
import org.redisson.api.RBucket;
import org.redisson.api.RedissonClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.web.bind.annotation.PathVariable;

@Service
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
    public String getUserById(@PathVariable Long userId) throws JsonProcessingException, UserNotFoundException {
        logger.info("Getting user by id");
        try {
            return objectMapper.writeValueAsString(findUserById(userId));
        } catch (ObjectNotFoundException e) {
            String notFoundMessage = "user id " + userId + " not found";
            logger.info(notFoundMessage);
            throw new UserNotFoundException(notFoundMessage);
        }
    }

    @Override
    public String getUserByUsername(String username) throws JsonProcessingException, UserNotFoundException {
        logger.info("Getting user by username");
        try {
            RBucket<User> bucket = redissonClient.getBucket(username);
            User user = bucket.get();
            if (user != null) {
                return objectMapper.writeValueAsString(user);
            }
            user = userRepository.findByUsername(username).orElseThrow(ObjectNotFoundException::new);
            bucket.set(user);
            return objectMapper.writeValueAsString(user);
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
    public void insertUserDetails(String requestParams) throws BadRequestException, JSONException{
        logger.info("Inserting new user");
        if (StringUtils.isEmpty(requestParams)) {
            logger.info(ErrorMessageContants.EMPTY_PARAMETER_MESSAGE);
            throw new BadRequestException(ErrorMessageContants.EMPTY_PARAMETER_MESSAGE);
        }
        JSONObject userJson = new JSONObject(requestParams);
        User newUser = new User();

        String email = userJson.optString("email");
        String username = userJson.optString("username");
        String password = userJson.optString("password");

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
        newUser.setEmail(email);
        newUser.setUsername(username);
        newUser.setPassword(passwordEncoder.encode(password));
        userRepository.save(newUser);
        RBucket<User> bucket = redissonClient.getBucket(username);
        bucket.set(newUser);
    }

    @Override
    public void updateUser(String requestParams) throws BadRequestException, JSONException, UserNotFoundException {
        logger.info("Updating existing user");
        if (StringUtils.isEmpty(requestParams)) {
            logger.info(ErrorMessageContants.EMPTY_PARAMETER_MESSAGE);
            throw new BadRequestException(ErrorMessageContants.EMPTY_PARAMETER_MESSAGE);
        }
        JSONObject userJson = new JSONObject(requestParams);
        long userId = userJson.optLong("userId");
        User user = null;
        try {
            // use findUserById because you want to get most recent and non-cached data
            user = findUserById(userId);
        } catch (ObjectNotFoundException e) {
            String notFoundMessage = "Cannot update user with user id, reason: user id " + userId + " not found";
            logger.info(notFoundMessage);
            throw new UserNotFoundException(notFoundMessage);
        }

        String email = userJson.optString("email");
        String username = userJson.optString("username");
        String password = userJson.optString("password");
        String firstName = userJson.optString("firstName");
        String lastName = userJson.optString("lastName");


        if (!StringUtils.isEmpty(email) && !email.equals(user.getEmail())) {
            updateEmail(user, email);
        }

        if (!StringUtils.isEmpty(username) && !username.equals(user.getUsername())) {
            updateUsername(user, username);
        }

        // password history checking is in updatePassword method
        if (!StringUtils.isEmpty(password)) {
            updatePassword(user, password);
        }

        if (!StringUtils.isEmpty(firstName) && !firstName.equals(user.getFirstName())) {
            user.setFirstName(firstName);
        }

        if (!StringUtils.isEmpty(lastName) && !lastName.equals(user.getLastName())) {
            user.setLastName(lastName);
        }

        userRepository.save(user);
        RBucket<User> bucket = redissonClient.getBucket(username);
        bucket.set(user);
    }

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

    private void updatePassword(User user, String password) throws BadRequestException {
        boolean isPasswordValid = isPasswordValid(password);
        if (!isPasswordValid) {
            String passwordIsNotSecureEnoughMessage = "password is invalid. Requirements: " + ErrorMessageContants.PASSWORD_FORMAT_REQUIREMENTS;
            logger.info(passwordIsNotSecureEnoughMessage);
            throw new BadRequestException(passwordIsNotSecureEnoughMessage);
        }
        user.setPassword(passwordEncoder.encode(password));
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
        // TODO: implement this
        boolean passwordHasEnoughCharacters = true;
        boolean passwordIsAlphaNumeric = true;
        boolean passwordHasSpecialCharacter = true;
        boolean passwordNotRecentlyUsed = true;
        return passwordHasEnoughCharacters && passwordIsAlphaNumeric && passwordHasSpecialCharacter && passwordNotRecentlyUsed;
    }

    private void addMessageToNotificationQueue(String message) {
        // TODO: implement this
    }
}