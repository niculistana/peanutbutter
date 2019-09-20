package com.somamission.peanutbutter.controller;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.somamission.peanutbutter.domain.User;
import com.somamission.peanutbutter.exception.BadRequestException;
import com.somamission.peanutbutter.exception.UserNotFoundException;
import com.somamission.peanutbutter.intf.IUserService;
import com.somamission.peanutbutter.param.UserParams;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
public class UserController {
    @Autowired
    private
    IUserService userService;

    @Autowired
    ObjectMapper objectMapper;

    @GetMapping("/user/{username}")
    public ResponseEntity<String> getUser(@PathVariable String username) throws UserNotFoundException, JsonProcessingException {
        return ResponseEntity.ok(objectMapper.writeValueAsString(userService.getUserByUsername(username)));
    }

    @PostMapping("/user/create")
    public ResponseEntity<String> createUser(@RequestBody UserParams userParams) throws UserNotFoundException, BadRequestException, JsonProcessingException {
        String username = userParams.getUsername();
        userService.createNewUser(username, userParams.getEmail(), userParams.getPassword());
        User user = userService.getUserByUsername(username);
        if (null == user) throw new UserNotFoundException(username);
        return ResponseEntity.ok(objectMapper.writeValueAsString(user));
    }

    @PutMapping("/user/update")
    public ResponseEntity<String> update(@RequestBody UserParams userParams) throws UserNotFoundException, BadRequestException, JsonProcessingException {
        String username = userParams.getUsername();
        String password = userParams.getPassword();
        if (!StringUtils.isEmpty(password)) {
            userService.updatePassword(username, password);
        }
        User user = userService.getUserByUsername(username);
        if (null == user) throw new UserNotFoundException(username);
        return ResponseEntity.ok(objectMapper.writeValueAsString(user));
    }
}
