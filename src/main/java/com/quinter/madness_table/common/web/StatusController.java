package com.quinter.madness_table.common.web;


import java.time.Instant;
import java.util.Map;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api")
public class StatusController {

    @GetMapping("/status")
    public Map<String, Object> status() {
        return Map.of(
                "app", "madness-table",
                "status", "The stars are right",
                "timestamp", Instant.now().toString()
        );
    }
}
