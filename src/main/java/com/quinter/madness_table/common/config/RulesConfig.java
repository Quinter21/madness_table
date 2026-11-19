package com.quinter.madness_table.common.config;

import com.quinter.madness_table.rules.JavaRandomSource;
import com.quinter.madness_table.rules.RandomSource;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class RulesConfig {

    @Bean
    public RandomSource randomSource() {
        return new JavaRandomSource();
    }
}
