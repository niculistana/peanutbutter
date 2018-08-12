package com.somamission.peanutbutter.intf;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.somamission.peanutbutter.exception.BadRequestException;
import com.somamission.peanutbutter.exception.UserNotFoundException;
import org.json.JSONException;
import org.springframework.security.core.userdetails.UserDetailsService;

public interface IUserService extends UserDetailsService {
    public String getUserById(Long userId) throws JsonProcessingException, UserNotFoundException;
    public String getUserByUsername(String username) throws JsonProcessingException, UserNotFoundException;
    public void insertUserDetails(String userRequestParams) throws BadRequestException, JSONException;
    public void updateUser(String requestParams) throws BadRequestException, JSONException, UserNotFoundException;
}
