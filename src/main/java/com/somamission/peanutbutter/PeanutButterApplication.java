package com.somamission.peanutbutter;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.builder.SpringApplicationBuilder;

@SpringBootApplication
public class PeanutButterApplication {

    protected SpringApplicationBuilder configure(SpringApplicationBuilder application) {
        return application.sources(PeanutButterApplication.class);
    }

    public static void main(String[] args) throws Exception {
        SpringApplication.run(PeanutButterApplication.class, args);
    }
}
