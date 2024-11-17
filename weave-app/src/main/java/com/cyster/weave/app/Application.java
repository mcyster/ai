package com.cyster.weave.app;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Import;
import org.springframework.core.env.Environment;

import com.cyster.rest.ApplicationServerConfig;
import com.cyster.rest.CysterRestScan;
import com.cyster.scheduler.SchedulerWeaveScan;
import com.cyster.weave.CysterWeaveScan;
import com.cyster.weave.rest.WeaveRestScan;
import com.cyster.web.rest.WebRestScan;
import com.cyster.web.weave.WebWeaveScan;

@SpringBootApplication
@Import(value = { CysterWeaveScan.class, SchedulerWeaveScan.class, WeaveRestScan.class, WebWeaveScan.class,
        WebRestScan.class, CysterRestScan.class })
public class Application {

    private static final Logger logger = LoggerFactory.getLogger(Application.class);

    public static void main(String[] args) {
        SpringApplication.run(Application.class, args);
    }

    @Bean
    public CommandLineRunner commandLineRunner(ApplicationContext context) {
        return args -> {
            Environment environment = context.getEnvironment();
            if (!environment.containsProperty("OPENAI_API_KEY")) {
                logger.warn("Warning: Environment variable OPENAI_API_KEY not defined!");
            } else {
                var key = environment.getProperty("OPENAI_API_KEY");
                if (key.length() < 4) {
                    logger.warn("Warning: Environment variable OPENAI_API_KEY is invalid!");
                } else {
                    logger.info("OPENAI_API_KEY: ..." + key.substring(key.length() - 4));
                }
            }

            var config = new ApplicationServerConfig(context);
            logger.info(config.getDescription());
        };
    }

}
