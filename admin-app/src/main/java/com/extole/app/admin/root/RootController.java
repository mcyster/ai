package com.extole.app.admin.root;

import java.io.IOException;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class RootController {
    private static final Logger logger = LogManager.getLogger(RootController.class);

    @Autowired
    public RootController() {
    }

    @GetMapping("/")
    public String index() throws IOException {
        logger.info("get /");

        return "index.html";
    }
}
