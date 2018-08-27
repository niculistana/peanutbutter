package com.somamission.peanutbutter.impl;

import com.google.gson.Gson;
import com.googlecode.jsonrpc4j.spring.AutoJsonRpcServiceImpl;
import com.somamission.peanutbutter.constants.ErrorMessageContants;
import com.somamission.peanutbutter.domain.User;
import com.somamission.peanutbutter.exception.BadRequestException;
import com.somamission.peanutbutter.exception.ObjectNotFoundException;
import com.somamission.peanutbutter.exception.UserNotFoundException;
import com.somamission.peanutbutter.intf.IUserService;
import com.somamission.peanutbutter.param.AddressParams;
import com.somamission.peanutbutter.param.NameParams;
import com.somamission.peanutbutter.param.UserParams;
import com.somamission.peanutbutter.repository.IUserRepository;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.validator.routines.EmailValidator;
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

import java.io.IOException;
import java.util.Arrays;
import java.util.List;

@Service
@AutoJsonRpcServiceImpl
public class UserService implements IUserService {
    private static Logger logger = LoggerFactory.getLogger(UserService.class);

    @Autowired
    IUserRepository userRepository;

    @Autowired
    PasswordEncoder passwordEncoder;

    @Autowired
    RedissonClient redissonClient;

    @Override
    public User getUserByUsername(String username) throws UserNotFoundException {
        logger.info("Getting user by username");
        try {
            if (StringUtils.isEmpty(username)) {
                throw new ObjectNotFoundException();
            }
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
    public void createNewUser(String username, String email, String password) throws BadRequestException {
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

        if (!isEmailValid(email)) {
            String emailNotValidMessage = email + " email is invalid. Requirements: " + ErrorMessageContants.EMAIL_FORMAT_REQUIREMENTS;
            logger.info(emailNotValidMessage);
            throw new BadRequestException(emailNotValidMessage);
        }

        if (!isUsernameValid(username)) {
            String usernameNotValidMessage = username + " username is invalid. Requirements: " + ErrorMessageContants.USERNAME_FORMAT_REQUIREMENTS;
            logger.info(usernameNotValidMessage);
            throw new BadRequestException(usernameNotValidMessage);
        }

        if (!isPasswordValid(password)) {
            String passwordIsNotSecureEnoughMessage = "password is invalid. Requirements: " + ErrorMessageContants.PASSWORD_FORMAT_REQUIREMENTS;
            logger.info(passwordIsNotSecureEnoughMessage);
            throw new BadRequestException(passwordIsNotSecureEnoughMessage);
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
    public void updatePassword(String username, String password) throws BadRequestException, UserNotFoundException {
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
    public void resetPassword(String username) throws UserNotFoundException, BadRequestException {
        logger.info("Resetting user password");
        updatePassword(generatePassword(), username);
    }

    @Override
    public void updateEmail(String username, String email) throws BadRequestException, UserNotFoundException {
        logger.info("Updating user email");
        if (StringUtils.isEmpty(username)) {
            logger.info(ErrorMessageContants.REQUIRED_PARAMETER_NOT_FOUND + ": username");
            throw new BadRequestException(ErrorMessageContants.REQUIRED_PARAMETER_NOT_FOUND + ": username");
        }

        if (!isEmailValid(email)) {
            String emailNotValidMessage = email + " email is invalid. Requirements: " + ErrorMessageContants.EMAIL_FORMAT_REQUIREMENTS;
            logger.info(emailNotValidMessage);
            throw new BadRequestException(emailNotValidMessage);
        }

        User user = getUserByUsername(username);
        UserParams userParams = new UserParams.Builder().withEmail(email).build();
        updateUser(user, userParams);
    }

    @Override
    public void updateUserInfo(String username, String nameParamString, String addressParamString) throws UserNotFoundException {
        logger.info("Updating user info");
        Gson gson = new Gson();
        NameParams nameParams = gson.fromJson(nameParamString, NameParams.class);
        AddressParams addressParams = gson.fromJson(addressParamString, AddressParams.class);
        User user = getUserByUsername(username);
        UserParams userParams = new UserParams.Builder().withNameParams(nameParams).withAddressParams(addressParams).build();
        updateUser(user, userParams);
    }

    private void updateUser(User user, UserParams userParams) {
        if (!StringUtils.isEmpty(userParams.getEmail())) {
            user.setEmail(StringUtils.trim(userParams.getEmail()));
        }

        if (!StringUtils.isEmpty(userParams.getPassword())) {
            user.setPassword(passwordEncoder.encode(StringUtils.trim(userParams.getPassword())));
        }

        if (!StringUtils.isEmpty(userParams.getNameParams().getFirstName())) {
            user.setFirstName(StringUtils.trim(userParams.getNameParams().getFirstName()));
        }

        if (!StringUtils.isEmpty(userParams.getNameParams().getLastName())) {
            user.setLastName(StringUtils.trim(userParams.getNameParams().getLastName()));
        }

        if (!StringUtils.isEmpty(userParams.getAddressParams().getFullAddress())) {
            user.setFullAddress(StringUtils.trim(userParams.getAddressParams().getFullAddress()));
        }

        userRepository.save(user);
        RBucket<User> bucket = redissonClient.getBucket(user.getUsername());
        bucket.set(user);
    }

    private User findUserById(Long userId) throws ObjectNotFoundException {
        return userRepository.findById(userId).orElseThrow(ObjectNotFoundException::new);
    }

    private boolean isEmailValid(String email) {
        // TODO: check if email is unique
        return EmailValidator.getInstance().isValid(email);
    }

    private boolean isUsernameValid(String username) {
        // TODO: implement this
        boolean isUsernameUnique = true;
        boolean usernameHasEnoughCharacters = true;
        boolean isUsernameIsNotAbusive = true;
        return isUsernameUnique && usernameHasEnoughCharacters;
    }

    private boolean isPasswordValid(String password) {
        PasswordValidator validator = new PasswordValidator(
                new LengthRule(8, 128),
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