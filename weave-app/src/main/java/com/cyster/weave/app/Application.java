package com.cyster.weave.app;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Import;
import org.springframework.core.env.Environment;

import com.cyster.rest.ApplicationServerConfig;
import com.cyster.rest.CysterRestScan;
import com.cyster.store.CysterStoreScan;
import com.cyster.weave.CysterSageScan;
import com.cyster.weave.rest.WeaveRestScan;
import com.cyster.web.rest.WebSiteScan;
import com.cyster.web.weave.WebDeveloperScan;
import com.extole.client.ExtoleClientScan;
import com.extole.tickets.rest.ExtoleTicketsScan;
import com.extole.weave.ExtoleSageScan;

@SpringBootApplication
@Import(value = { CysterSageScan.class, WeaveRestScan.class, WebDeveloperScan.class, ExtoleClientScan.class, ExtoleTicketsScan.class, CysterStoreScan.class, ExtoleSageScan.class, WebSiteScan.class, CysterRestScan.class })
public class Application {

    private static final Logger logger = LogManager.getLogger(Application.class);

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
