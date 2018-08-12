package com.somamission.peanutbutter.controller;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.somamission.peanutbutter.intf.IUserService;
import com.somamission.peanutbutter.exception.BadRequestException;
import com.somamission.peanutbutter.exception.UserNotFoundException;
import org.json.JSONException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping(path = "users")
public class UserController {
    @Autowired
    IUserService userService;

    @GetMapping(path = "{username}", produces = MediaType.APPLICATION_JSON_VALUE)
    private ResponseEntity getUserByUsername(
            @PathVariable String username
    ) {
        try {
            return ResponseEntity.ok(userService.getUserByUsername(username));
        } catch (JsonProcessingException e) {
            return ResponseEntity.badRequest().build();
        } catch (UserNotFoundException unfe) {
            return ResponseEntity.badRequest().body(unfe.getMessage());
        }
    }

    @PostMapping(path = "create")
    private ResponseEntity createUser(@RequestBody String requestParams) {
        try {
            userService.insertUserDetails(requestParams);
            return ResponseEntity.status(HttpStatus.CREATED).build();
        } catch (BadRequestException | JSONException e) {
            return ResponseEntity.badRequest().build();
        }
    }

    @PutMapping(path = "update")
    private ResponseEntity updateUser(@RequestBody String requestParams) {
        try {
            userService.updateUser(requestParams);
            return ResponseEntity.status(HttpStatus.NO_CONTENT).build();
        } catch (BadRequestException | JSONException e) {
            return ResponseEntity.badRequest().build();
        } catch (UserNotFoundException unfe) {
            return ResponseEntity.badRequest().body(unfe.getMessage());
        }
    }
}
