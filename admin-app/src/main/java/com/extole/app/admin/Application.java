package com.extole.app.admin;

import org.slf4j.LoggerFactory;
import org.slf4j.Logger;
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
import com.extole.client.ExtoleClientScan;
import com.extole.admin.weave.ExtoleAdminWeaveScan;

@SpringBootApplication
@Import(value = {
    CysterRestScan.class,
    CysterWeaveScan.class,
    SchedulerWeaveScan.class,
    WeaveRestScan.class, 
    ExtoleClientScan.class, 
    ExtoleAdminWeaveScan.class, 
})
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
            }

            var config = new ApplicationServerConfig(context);
            logger.info(config.getDescription());
        };
    }
    


}
