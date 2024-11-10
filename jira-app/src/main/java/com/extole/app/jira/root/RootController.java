package com.extole.app.jira.root;

import java.io.IOException;

import org.slf4j.LoggerFactory;
import org.slf4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.servlet.view.RedirectView;

@Controller
public class RootController {
    private static final Logger logger = LoggerFactory.getLogger(RootController.class);

    @Autowired
    public RootController() {
    }

    @GetMapping("/")
    public RedirectView index() throws IOException {
        logger.info("get /");

        return new RedirectView("/sites/managed/sites/index.html?tags=managed,curated,start");
    }
}
