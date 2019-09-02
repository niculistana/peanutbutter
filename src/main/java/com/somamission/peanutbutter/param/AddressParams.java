package com.somamission.peanutbutter.param;

import org.apache.commons.lang3.StringUtils;

public class AddressParams {
    private static class Builder {
        private String addressLineOne;
        private String addressLineTwo;
        private String city;
        private String state;
        private String country;
        private String zip;

        private Builder withAddressLineOne(String addressLineOne) {
            this.addressLineOne = addressLineOne;
            return this;
        }

        private Builder withAddressLineTwo(String addressLineTwo) {
            this.addressLineTwo = addressLineTwo;
            return this;
        }

        private Builder withCity(String city) {
            this.city = city;
            return this;
        }

        private Builder withState(String state) {
            this.state = state;
            return this;
        }

        private Builder withCountry(String country) {
            this.country = country;
            return this;
        }

        private Builder withZip(String zip) {
            this.zip = zip;
            return this;
        }

        private AddressParams build() {
            AddressParams addressParams = new AddressParams();
            addressParams.addressLineOne = this.addressLineOne;
            addressParams.addressLineTwo = this.addressLineTwo;
            addressParams.city = this.city;
            addressParams.state = this.state;
            addressParams.country = this.country;
            addressParams.zip = this.zip;
            return addressParams;
        }
    }

    private String addressLineOne;
    private String addressLineTwo;
    private String city;
    private String state;
    private String country;
    private String zip;

    public String getAddressLineOne() {
        return addressLineOne;
    }

    public String getAddressLineTwo() {
        return addressLineTwo;
    }

    public String getCity() {
        return city;
    }

    public String getState() {
        return state;
    }

    public String getCountry() {
        return country;
    }

    public String getZip() {
        return zip;
    }

    public String getFullAddress() {
        String[] fullAddressFields = {this.addressLineOne, this.addressLineTwo, this.city, this.state, this.country, this.zip};
        return StringUtils.join(fullAddressFields, ",");
    }
}
