package com.nsv.backend;

import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest
@Disabled("Integration test: requires a running PostgreSQL database — run manually with DB up")
class BackendApplicationTests {

	@Test
	void contextLoads() {
	}

}
