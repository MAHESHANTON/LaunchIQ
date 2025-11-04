package com.launchiq.controller;

import com.launchiq.util.AESEncryption;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
@RequestMapping("/admin/crypto")
public class AdminCryptoController {

    @GetMapping("/encrypt")
    public Map<String, String> encrypt(@RequestParam String value) {
        String enc = AESEncryption.encrypt(value);
        return Map.of("cipher", enc);
    }
}


