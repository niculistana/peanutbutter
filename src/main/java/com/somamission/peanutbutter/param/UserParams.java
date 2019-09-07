package com.somamission.peanutbutter.param;

public class UserParams {
    public static class Builder {
        private String email;
        private String username;
        private String password;
        private NameParams nameParams;
        private AddressParams addressParams;

        public Builder withEmail(String email) {
            this.email = email;
            return this;
        }

        public Builder withUsername(String username) {
            this.username = username;
            return this;
        }

        public Builder withPassword(String password) {
            this.password = password;
            return this;
        }

        public Builder withNameParams(NameParams nameParams) {
            this.nameParams = nameParams;
            return this;
        }

        public Builder withAddressParams(AddressParams addressParams) {
            this.addressParams = addressParams;
            return this;
        }

        public UserParams build() {
            UserParams userParams = new UserParams();
            userParams.email = this.email;
            userParams.password = this.password;
            userParams.nameParams = this.nameParams;
            userParams.addressParams = this.addressParams;
            return userParams;
        }
    }

    private String email;
    private String password;
    private String username;
    private NameParams nameParams;
    private AddressParams addressParams;

    private UserParams() {

    }

    public String getEmail() {
        return email;
    }

    public String getUsername() {
        return username;
    }

    public String getPassword() {
        return password;
    }

    public NameParams getNameParams() {
        return nameParams;
    }

    public AddressParams getAddressParams() {
        return addressParams;
    }
}
