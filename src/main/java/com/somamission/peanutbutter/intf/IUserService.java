package com.somamission.peanutbutter.intf;

import com.googlecode.jsonrpc4j.JsonRpcParam;
import com.googlecode.jsonrpc4j.JsonRpcService;
import com.somamission.peanutbutter.domain.User;
import com.somamission.peanutbutter.exception.BadRequestException;
import com.somamission.peanutbutter.exception.UserNotFoundException;
import org.springframework.security.core.userdetails.UserDetailsService;

@JsonRpcService("/user")
public interface IUserService extends UserDetailsService {
    User getUserByUsername(@JsonRpcParam(value = "username") String username) throws UserNotFoundException;

    void createNewUser(@JsonRpcParam(value = "username") String username,
                              @JsonRpcParam(value = "email") String email,
                              @JsonRpcParam(value = "password") String password) throws BadRequestException;

    void updatePassword(@JsonRpcParam(value = "username") String username,
                               @JsonRpcParam(value = "password") String password) throws BadRequestException, UserNotFoundException;

    void resetPassword(@JsonRpcParam(value = "username") String username) throws UserNotFoundException, BadRequestException;

    void updateEmail(@JsonRpcParam(value = "username") String username,
                            @JsonRpcParam(value = "email") String email) throws BadRequestException, UserNotFoundException;

    void updateUserInfo(@JsonRpcParam(value = "username") String username,
                               @JsonRpcParam(value = "nameParams") String nameParams,
                               @JsonRpcParam(value = "addressParams") String addressParams) throws UserNotFoundException;
}
