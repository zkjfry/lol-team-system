package com.example.lolteambackend;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
@MapperScan("com.example.lolteambackend.dao")
public class LolTeamBackendApplication {

    public static void main(String[] args) {
        SpringApplication.run(LolTeamBackendApplication.class, args);
    }
}