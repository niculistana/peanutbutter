package com.somamission.peanutbutter.impl;

import com.somamission.peanutbutter.constants.ErrorMessageConstants;
import com.somamission.peanutbutter.domain.User;
import com.somamission.peanutbutter.exception.BadRequestException;
import com.somamission.peanutbutter.exception.ObjectNotFoundException;
import com.somamission.peanutbutter.exception.UserNotFoundException;
import com.somamission.peanutbutter.intf.IUserService;
import com.somamission.peanutbutter.param.UserParams;
import com.somamission.peanutbutter.repository.IUserRepository;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.validator.routines.EmailValidator;
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
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.Arrays;
import java.util.List;
import java.util.concurrent.TimeUnit;

@Service
public class UserService implements IUserService {
    private static final String USERNAME_INVALID_MSG = ErrorMessageConstants.REQUIRED_PARAMETER_NOT_FOUND + ": username";
    private static final String EMAIL_INVALID_MSG = ErrorMessageConstants.REQUIRED_PARAMETER_NOT_FOUND + ": email";
    private static final String PASSWORD_INVALID_MSG = ErrorMessageConstants.REQUIRED_PARAMETER_NOT_FOUND + ": password";
    private static final Logger logger = LoggerFactory.getLogger(UserService.class);

    @Autowired
    private IUserRepository userRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private RedissonClient redissonClient;

    @Autowired
    private CacheManager cacheManager;

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
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        // TODO implement this
        return null;
    }

    @Override
    public void createNewUser(String username, String email, String password) throws BadRequestException {
        logger.info("Inserting new user");

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

        if (!isEmailValid(email)) {
            String emailNotValidMessage = email + " email is invalid. Requirements: " + ErrorMessageConstants.EMAIL_FORMAT_REQUIREMENTS;
            logger.info(emailNotValidMessage);
            throw new BadRequestException(emailNotValidMessage);
        }

        if (!isUsernameValid(username)) {
            String usernameNotValidMessage = username + " username is invalid. Requirements: " + ErrorMessageConstants.USERNAME_FORMAT_REQUIREMENTS;
            logger.info(usernameNotValidMessage);
            throw new BadRequestException(usernameNotValidMessage);
        }

        if (!isPasswordValid(password)) {
            String passwordIsNotSecureEnoughMessage = "password is invalid. Requirements: " + ErrorMessageConstants.PASSWORD_FORMAT_REQUIREMENTS;
            logger.info(passwordIsNotSecureEnoughMessage);
            throw new BadRequestException(passwordIsNotSecureEnoughMessage);
        }

        User user = new User();
        user.setEmail(email);
        user.setUsername(username);
        user.setPassword(passwordEncoder.encode(password));
        userRepository.save(user);
        updateUsersCacheForUsername(username, user);
    }

    @Override
    public void updatePassword(String username, String password) throws BadRequestException, UserNotFoundException {
        logger.info("Updating user password");
        if (StringUtils.isEmpty(username)) {
            logger.info(USERNAME_INVALID_MSG);
            throw new BadRequestException(USERNAME_INVALID_MSG);
        }

        if (!isPasswordValid(password)) {
            String passwordIsNotSecureEnoughMessage = "password is invalid. Requirements: " + ErrorMessageConstants.PASSWORD_FORMAT_REQUIREMENTS;
            logger.info(passwordIsNotSecureEnoughMessage);
            throw new BadRequestException(passwordIsNotSecureEnoughMessage);
        }

        UserParams userParams = new UserParams.Builder().withUsername(username).withPassword(password).build();
        updateUser(userParams);
    }

    @Override
    public void resetPassword(String username) throws UserNotFoundException, BadRequestException {
        logger.info("Resetting user password");
        updatePassword(username, generatePassword());
    }

    @Override
    public void updateEmail(String username, String email) throws BadRequestException, UserNotFoundException {
        logger.info("Updating user email");
        if (StringUtils.isEmpty(username)) {
            logger.info(USERNAME_INVALID_MSG);
            throw new BadRequestException(USERNAME_INVALID_MSG);
        }

        if (!isEmailValid(email)) {
            String emailNotValidMessage = email + " email is invalid. Requirements: " + ErrorMessageConstants.EMAIL_FORMAT_REQUIREMENTS;
            logger.info(emailNotValidMessage);
            throw new BadRequestException(emailNotValidMessage);
        }

        UserParams userParams = new UserParams.Builder().withUsername(username).withEmail(email).build();
        updateUser(userParams);
    }

    private void updateUser(UserParams userParams) throws UserNotFoundException {
        String username = userParams.getUsername();
        User user = null;
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
            if (!StringUtils.isEmpty(userParams.getNameParams().getFirstName())) {
                user.setFirstName(StringUtils.trim(userParams.getNameParams().getFirstName()));
            }

            if (!StringUtils.isEmpty(userParams.getNameParams().getLastName())) {
                user.setLastName(StringUtils.trim(userParams.getNameParams().getLastName()));
            }
        }

        if (null != userParams.getAddressParams() && !StringUtils.isEmpty(userParams.getAddressParams().getFullAddress())) {
            user.setFullAddress(StringUtils.trim(userParams.getAddressParams().getFullAddress()));
        }
        userRepository.save(user);
        updateUsersCacheForUsername(username, user);
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
        // TODO: check how to limit the special characters to a set of common special characters, ie: '!,?,*,...'
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

    private String generatePassword() {
        List<CharacterRule> rules = Arrays.asList(
                new CharacterRule(EnglishCharacterData.UpperCase, 1),
                new CharacterRule(EnglishCharacterData.LowerCase, 1),
                new CharacterRule(EnglishCharacterData.Digit, 1),
                new CharacterRule(EnglishCharacterData.Special, 1));

        PasswordGenerator generator = new PasswordGenerator();
        return generator.generatePassword(12, rules);
    }

    @CachePut(cacheNames = "users", key = "#username")
    private void updateUsersCacheForUsername(String username, User user) {
        RMapCache<String, User> userRMapCache = redissonClient.getMapCache("users");
        userRMapCache.put(username, user, 30, TimeUnit.MINUTES);
    }
}