package com.example.bookshop_gateway.controller;

import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class TestController {
    // This is a test endpoint to verify that the gateway is working
    @RequestMapping("/test")
    public String test() {
        return "Gateway is working!";
    }
}
