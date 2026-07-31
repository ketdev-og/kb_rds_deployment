package com.ops.kbspring;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cache.annotation.EnableCaching;

@SpringBootApplication
@EnableCaching
public class KbSpringApplication {

    public static void main(String[] args) {
        SpringApplication.run(KbSpringApplication.class, args);
    }

}
