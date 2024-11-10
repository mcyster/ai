package com.extole.app.admin.root;

import java.io.IOException;

import org.slf4j.LoggerFactory;
import org.slf4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class RootController {
    private static final Logger logger = LoggerFactory.getLogger(RootController.class);

    @Autowired
    public RootController() {
    }

    @GetMapping("/")
    public String index() throws IOException {
        logger.info("get /");
       
        return "index.html";
    }
}
