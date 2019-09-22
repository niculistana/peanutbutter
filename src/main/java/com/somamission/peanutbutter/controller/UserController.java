package com.somamission.peanutbutter.controller;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.somamission.peanutbutter.constants.ErrorMessageConstants;
import com.somamission.peanutbutter.domain.User;
import com.somamission.peanutbutter.exception.BadRequestException;
import com.somamission.peanutbutter.exception.UserNotFoundException;
import com.somamission.peanutbutter.intf.IUserService;
import com.somamission.peanutbutter.param.AddressParams;
import com.somamission.peanutbutter.param.NameParams;
import com.somamission.peanutbutter.param.UserParams;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
public class UserController {
  @Autowired private IUserService userService;

  @Autowired ObjectMapper objectMapper;

  @GetMapping("/user/{username}")
  public ResponseEntity<String> getUser(@PathVariable String username)
      throws UserNotFoundException, JsonProcessingException {
    return ResponseEntity.ok(
        objectMapper.writeValueAsString(userService.getUserByUsername(username)));
  }

  @PostMapping("/user/create")
  public ResponseEntity<String> createUser(@RequestBody UserParams userParams)
      throws UserNotFoundException, BadRequestException, JsonProcessingException {
    String username = userParams.getUsername();
    String email = userParams.getEmail();
    String password = userParams.getPassword();
    if (StringUtils.isEmpty(email) || StringUtils.isEmpty(password)) {
      throw new BadRequestException(ErrorMessageConstants.REQUIRED_PARAMETER_NOT_FOUND);
    }
    userService.createNewUser(username, email, password);
    User user = userService.getUserByUsername(username);
    if (null == user) throw new UserNotFoundException(username);
    return ResponseEntity.ok(objectMapper.writeValueAsString(user));
  }

  @PutMapping("/user/{username}/updatePassword")
  public ResponseEntity<String> updatePassword(@RequestBody UserParams userParams)
      throws UserNotFoundException, BadRequestException, JsonProcessingException {
    String username = userParams.getUsername();
    String password = userParams.getPassword();
    if (StringUtils.isEmpty(password)) {
      throw new BadRequestException(ErrorMessageConstants.REQUIRED_PARAMETER_NOT_FOUND);
    }
    userService.updatePassword(username, password);
    User user = userService.getUserByUsername(username);
    if (null == user) throw new UserNotFoundException(username);
    return ResponseEntity.ok(objectMapper.writeValueAsString(user));
  }

  @PutMapping("/user/{username}/resetPassword")
  public ResponseEntity<String> resetPassword(@RequestBody UserParams userParams)
      throws UserNotFoundException, BadRequestException, JsonProcessingException {
    String username = userParams.getUsername();
    userService.resetPassword(username);
    User user = userService.getUserByUsername(username);
    if (null == user) throw new UserNotFoundException(username);
    return ResponseEntity.ok(objectMapper.writeValueAsString(user));
  }

  @PutMapping("/user/{username}/updateEmail")
  public ResponseEntity<String> updateEmail(@RequestBody UserParams userParams)
      throws UserNotFoundException, BadRequestException, JsonProcessingException {
    String username = userParams.getUsername();
    String email = userParams.getEmail();
    if (StringUtils.isEmpty(email)) {
      throw new BadRequestException(ErrorMessageConstants.REQUIRED_PARAMETER_NOT_FOUND);
    }
    userService.updateEmail(username, email);
    User user = userService.getUserByUsername(username);
    if (null == user) throw new UserNotFoundException(username);
    return ResponseEntity.ok(objectMapper.writeValueAsString(user));
  }

  @PutMapping("/user/{username}/updateInfo")
  public ResponseEntity<String> updateUserInfo(
      @PathVariable String username, @RequestBody UserParams userParams)
      throws UserNotFoundException, BadRequestException, JsonProcessingException {
    NameParams nameParams = userParams.getNameParams();
    AddressParams addressParams = userParams.getAddressParams();
    if (null != nameParams && null != addressParams) {
      userService.updateUserInfo(username, nameParams, addressParams);
    } else if (null != nameParams) {
      userService.updateUserInfo(username, nameParams);
    } else {
      userService.updateUserInfo(username, addressParams);
    }
    User user = userService.getUserByUsername(username);
    if (null == user) throw new UserNotFoundException(username);
    return ResponseEntity.ok(objectMapper.writeValueAsString(user));
  }
}
