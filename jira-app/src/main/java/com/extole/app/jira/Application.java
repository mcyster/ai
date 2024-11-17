package com.extole.app.jira;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Import;
import org.springframework.core.env.Environment;

import com.cyster.jira.client.JiraClientScan;
import com.cyster.rest.ApplicationServerConfig;
import com.cyster.rest.CysterRestScan;
import com.cyster.scheduler.SchedulerWeaveScan;
import com.cyster.weave.CysterWeaveScan;
import com.cyster.weave.rest.WeaveRestScan;
import com.cyster.web.rest.WebRestScan;
import com.cyster.web.weave.WebWeaveScan;
import com.extole.client.ExtoleClientScan;
import com.extole.jira.ExtoleJiraScan;
import com.extole.rest.ExtoleRestScan;
import com.extole.tickets.rest.ExtoleTicketsScan;
import com.extole.zuper.weave.ExtoleWeaveScan;

@SpringBootApplication
@Import(value = { CysterWeaveScan.class, SchedulerWeaveScan.class, WeaveRestScan.class, WebWeaveScan.class,
        ExtoleClientScan.class, JiraClientScan.class, ExtoleJiraScan.class, ExtoleTicketsScan.class,
        ExtoleWeaveScan.class, WebRestScan.class, ExtoleRestScan.class, CysterRestScan.class })
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
