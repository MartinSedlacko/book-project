package com.karankumar.bookproject;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.context.annotation.ComponentScan;


@EnableAutoConfiguration
@ComponentScan
public class EmailService
{
    public static void main(String[] args) {
        SpringApplication.run(EmailService.class, args);
    }
}
