package com.cyster.app.store.root;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class RootController {

    @Autowired
    public RootController() {
    }

    @GetMapping("/")
    public String index() {
        return "Hello world\n";
    }

}
