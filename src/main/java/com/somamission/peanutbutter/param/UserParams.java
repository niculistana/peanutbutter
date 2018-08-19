package com.somamission.peanutbutter.param;

public class UserParams {
    public static class Builder {
        private String email;
        private String password;

        public Builder() {
        }

        public Builder withEmail(String email) {
            this.email = email;
            return this;
        }

        public Builder withPassword(String password) {
            this.password = password;
            return this;
        }

        public UserParams build() {
            UserParams userParams = new UserParams();
            userParams.email = this.email;
            userParams.password = this.password;
            return userParams;
        }
    }

    private String email;
    private String password;

    private UserParams() {

    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }
}
