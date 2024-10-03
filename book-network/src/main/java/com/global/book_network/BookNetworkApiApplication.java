package com.global.book_network;

import com.global.book_network.role.Role;
import com.global.book_network.role.RoleRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;
import org.springframework.scheduling.annotation.EnableAsync;

@SpringBootApplication
@EnableAsync
public class BookNetworkApiApplication {

    public static void main(String[] args) {
        SpringApplication.run(BookNetworkApiApplication.class, args);
    }

    @Bean
    public CommandLineRunner runner(RoleRepository roleRepo) {
        return args -> {
            if (roleRepo.findByName("USER").isEmpty()) {
                roleRepo.save(
                        Role.builder()
                                .name("USER")
                                .build()
                );
            }
        };
    }
}
