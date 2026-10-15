package com.quinter.madness_table;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;

@Import(TestcontainersConfiguration.class)
@SpringBootTest
class MadnessTableApplicationTests {

	@Test
	void contextLoads() {
	}

}
