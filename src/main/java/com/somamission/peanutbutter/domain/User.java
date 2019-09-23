package com.somamission.peanutbutter.domain;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.somamission.peanutbutter.constants.ErrorMessageConstants;
import lombok.Data;
import org.hibernate.validator.constraints.Length;
import org.hibernate.validator.constraints.URL;

import javax.persistence.*;
import javax.validation.constraints.Email;
import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;
import java.io.Serializable;

@Entity
@Table(name = "users")
@Data
public class User implements Serializable {

  private static final long serialVersionUID = 1L;

  @Id
  @GeneratedValue(strategy = GenerationType.AUTO)
  @Column(name = "user_id")
  private Long userId;

  @NotNull
  @Length(min = 3, max = 60)
  @Column(unique = true)
  private String username;

  @NotEmpty
  @Email(message = ErrorMessageConstants.EMAIL_FORMAT_REQUIREMENTS)
  @Column(unique = true)
  private String email;

  @NotNull
  @JsonProperty(access = JsonProperty.Access.WRITE_ONLY)
  private String password;

  @URL(message = ErrorMessageConstants.URL_FORMAT_REQUIREMENTS)
  @Column(name = "profile_url")
  private String profilePhotoUrl;

  @URL(message = ErrorMessageConstants.URL_FORMAT_REQUIREMENTS)
  @Column(name = "cover_url")
  private String coverPhotoUrl;

  @Length(max = 60)
  @Column(name = "name_first")
  private String firstName;

  @Length(max = 60)
  @Column(length = 60, name = "name_last")
  private String lastName;

  @Column(name = "address_full")
  private String fullAddress;
}
