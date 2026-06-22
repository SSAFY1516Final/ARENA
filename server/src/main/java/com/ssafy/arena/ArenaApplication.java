package com.ssafy.arena;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.mybatis.spring.annotation.MapperScan;

@MapperScan("com.ssafy.arena.mapper")
@SpringBootApplication
public class ArenaApplication {

	public static void main(String[] args) {
		SpringApplication.run(ArenaApplication.class, args);
	}

}
