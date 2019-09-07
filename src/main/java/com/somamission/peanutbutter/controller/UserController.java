package com.somamission.peanutbutter.controller;

import com.somamission.peanutbutter.domain.User;
import com.somamission.peanutbutter.exception.BadRequestException;
import com.somamission.peanutbutter.exception.UserNotFoundException;
import com.somamission.peanutbutter.intf.IUserService;
import com.somamission.peanutbutter.param.UserParams;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
public class UserController {
    @Autowired
    private
    IUserService userService;

    @GetMapping("/user/{username}")
    public User getUser(@PathVariable String username) throws UserNotFoundException {
        return userService.getUserByUsername(username);
    }

    @PostMapping("/user/create")
    public ResponseEntity<User> createUser(@RequestBody UserParams userParams) throws UserNotFoundException, BadRequestException {
        String username = userParams.getUsername();
        userService.createNewUser(username, userParams.getEmail(), userParams.getPassword());
        User user = userService.getUserByUsername(username);
        if (null == user) throw new UserNotFoundException(username);
        return new ResponseEntity<>(HttpStatus.OK);
    }
}
