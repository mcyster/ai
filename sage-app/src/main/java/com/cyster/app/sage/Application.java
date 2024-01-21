package com.cyster.app.sage;

import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Import;
import org.springframework.core.env.Environment;

import com.cyster.rest.ApplicationServerConfig;
import com.cyster.sage.CysterSageScan;
import com.cyster.store.CysterStoreScan;
import com.extole.sage.ExtoleSageScan;

@SpringBootApplication
@Import(value = { CysterSageScan.class, CysterStoreScan.class, ExtoleSageScan.class })
public class Application {

    public static void main(String[] args) {
        SpringApplication.run(Application.class, args);
    }

    @Bean
    public CommandLineRunner commandLineRunner(ApplicationContext context) {
        return args -> {
            Environment environment = context.getEnvironment();
            if (!environment.containsProperty("OPENAI_API_KEY")) {
                System.out.println("Warning: Environment variable OPENAI_API_KEY not defined!");
            }

            var config = new ApplicationServerConfig(context);
            System.out.println(config.getDescription());
        };
    }

}