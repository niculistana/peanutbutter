package com.somamission.peanutbutter.intf;

import com.somamission.peanutbutter.domain.User;
import com.somamission.peanutbutter.exception.BadRequestException;
import com.somamission.peanutbutter.exception.UserNotFoundException;
import com.somamission.peanutbutter.param.AddressParams;
import com.somamission.peanutbutter.param.NameParams;
import com.somamission.peanutbutter.param.PhotoParams;
import org.springframework.security.core.userdetails.UserDetailsService;

public interface IUserService extends UserDetailsService {
  User getUserByUsername(String username) throws UserNotFoundException;

  void createNewUser(String username, String email, String password) throws BadRequestException;

  void updatePassword(String username, String password)
      throws BadRequestException, UserNotFoundException;

  void resetPassword(String username) throws UserNotFoundException, BadRequestException;

  void updateEmail(String username, String email) throws BadRequestException, UserNotFoundException;

  void updateUserInfo(String username, NameParams nameParams)
      throws UserNotFoundException, BadRequestException;

  void updateUserInfo(String username, PhotoParams photoParams)
      throws BadRequestException, UserNotFoundException;

  void updateUserInfo(String username, AddressParams addressParams)
      throws UserNotFoundException, BadRequestException;

  void updateUserInfo(
      String username, NameParams nameParams, PhotoParams photoParams, AddressParams addressParams)
      throws UserNotFoundException, BadRequestException;
}
