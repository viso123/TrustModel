package com.tsw.blockchain.common.controller;

import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class TransactionController {

    @RequestMapping("/")
    public String index() {
        return "This is the correct site";
    }
}
