package com.somamission.peanutbutter.intf;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.googlecode.jsonrpc4j.JsonRpcError;
import com.googlecode.jsonrpc4j.JsonRpcErrors;
import com.googlecode.jsonrpc4j.JsonRpcParam;
import com.googlecode.jsonrpc4j.JsonRpcService;
import com.somamission.peanutbutter.domain.User;
import com.somamission.peanutbutter.exception.BadRequestException;
import com.somamission.peanutbutter.exception.UserNotFoundException;
import org.json.JSONException;
import org.springframework.security.core.userdetails.UserDetailsService;

@JsonRpcService("/user")
public interface IUserService extends UserDetailsService {
    public User getUserByUsername(@JsonRpcParam(value = "username") String username) throws JsonProcessingException, UserNotFoundException;

    public void createNewUser(@JsonRpcParam(value = "email") String email,
                              @JsonRpcParam(value = "username") String username,
                              @JsonRpcParam(value = "password") String password) throws BadRequestException;

    public void updatePassword(@JsonRpcParam(value = "password") String password,
                               @JsonRpcParam(value = "username") String username) throws BadRequestException, JsonProcessingException, UserNotFoundException;

    public void resetPassword(@JsonRpcParam(value = "username") String username) throws UserNotFoundException, BadRequestException, JsonProcessingException;

    public void updateEmail(@JsonRpcParam(value = "email") String email,
                            @JsonRpcParam(value = "username") String username) throws BadRequestException, JsonProcessingException, UserNotFoundException;
}
