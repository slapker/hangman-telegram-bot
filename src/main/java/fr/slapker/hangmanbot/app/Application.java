package fr.slapker.hangmanbot.app;


import org.springframework.boot.SpringApplication;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;

@ComponentScan(basePackages = "fr.slapker.hangmanbot")
@EnableJpaRepositories("fr.slapker.hangmanbot.repository")
public class Application {

    public static void main(String[] args) {
        SpringApplication.run(Application.class, args);
    }
}

