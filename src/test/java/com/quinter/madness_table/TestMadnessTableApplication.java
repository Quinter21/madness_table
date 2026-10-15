package com.quinter.madness_table;

import org.springframework.boot.SpringApplication;

public class TestMadnessTableApplication {

	public static void main(String[] args) {
		SpringApplication.from(MadnessTableApplication::main).with(TestcontainersConfiguration.class).run(args);
	}

}
