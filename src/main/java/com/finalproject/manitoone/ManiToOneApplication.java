package com.finalproject.manitoone;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableAsync;

@EnableAsync
@SpringBootApplication
public class ManiToOneApplication {

    public static void main(String[] args) {
        System.out.println("JASYPT_ENCRYPTOR_PASSWORD: " + System.getenv("JASYPT_ENCRYPTOR_PASSWORD"));
        System.out.println("JASYPT_ENCRYPTOR_PASSWORD: " + System.getProperty("JASYPT_ENCRYPTOR_PASSWORD"));
        SpringApplication.run(ManiToOneApplication.class, args);
    }

}
