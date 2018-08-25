package com.somamission.peanutbutter.intf;

import com.googlecode.jsonrpc4j.JsonRpcParam;
import com.googlecode.jsonrpc4j.JsonRpcService;
import com.somamission.peanutbutter.domain.User;
import com.somamission.peanutbutter.exception.BadRequestException;
import com.somamission.peanutbutter.exception.UserNotFoundException;
import org.springframework.security.core.userdetails.UserDetailsService;

import java.io.IOException;

@JsonRpcService("/user")
public interface IUserService extends UserDetailsService {
    public User getUserByUsername(@JsonRpcParam(value = "username") String username) throws UserNotFoundException;

    public void createNewUser(@JsonRpcParam(value = "email") String email,
                              @JsonRpcParam(value = "username") String username,
                              @JsonRpcParam(value = "password") String password) throws BadRequestException;

    public void updatePassword(@JsonRpcParam(value = "password") String password,
                               @JsonRpcParam(value = "username") String username) throws BadRequestException, UserNotFoundException;

    public void resetPassword(@JsonRpcParam(value = "username") String username) throws UserNotFoundException, BadRequestException;

    public void updateEmail(@JsonRpcParam(value = "email") String email,
                            @JsonRpcParam(value = "username") String username) throws BadRequestException, UserNotFoundException;

    public void updateUserInfo(String userParamString) throws UserNotFoundException;
}
