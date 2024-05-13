package com.cyster.app.sage.root;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class RootController {
    private static final Logger logger = LogManager.getLogger(RootController.class);
    
	@Autowired
	public RootController() {
	}

	@GetMapping("/")
	public String index() {
	    logger.info("get /");
	    
		return "Hello world\n";
	}
}
