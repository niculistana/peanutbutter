package com.somamission.peanutbutter.param;

public class NameParams {
  public static class Builder {
    private String firstName;
    private String lastName;

    public Builder withFirstName(String firstName) {
      this.firstName = firstName;
      return this;
    }

    public Builder withLastName(String lastName) {
      this.lastName = lastName;
      return this;
    }

    public NameParams build() {
      NameParams nameParams = new NameParams();
      nameParams.firstName = this.firstName;
      nameParams.lastName = this.lastName;

      return nameParams;
    }
  }

  private String firstName;
  private String lastName;

  public String getFirstName() {
    return firstName;
  }

  public String getLastName() {
    return lastName;
  }
}
