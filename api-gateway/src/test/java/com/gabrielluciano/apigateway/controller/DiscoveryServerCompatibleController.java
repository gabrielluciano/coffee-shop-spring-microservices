package com.gabrielluciano.apigateway.controller;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/eureka")
public class DiscoveryServerCompatibleController {

    @GetMapping
    public Boolean home() {
        return true;
    }

    @GetMapping("/**")
    public Boolean staticResources() {
        return true;
    }
}
