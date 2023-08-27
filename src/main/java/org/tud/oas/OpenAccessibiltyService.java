package org.tud.oas;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.slf4j.LoggerFactory;
import org.slf4j.Logger;

@SpringBootApplication
public class OpenAccessibiltyService {
	static final Logger logger = LoggerFactory.getLogger(OpenAccessibiltyService.class);

	public static void main(String[] args) throws Exception {
		SpringApplication.run(OpenAccessibiltyService.class, args);
	}
}
