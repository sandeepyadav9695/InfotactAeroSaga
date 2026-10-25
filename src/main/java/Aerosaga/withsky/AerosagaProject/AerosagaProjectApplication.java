package Aerosaga.withsky.AerosagaProject;

import Aerosaga.withsky.AerosagaProject.config.AeroSagaProperties;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
@EnableConfigurationProperties(AeroSagaProperties.class)
public class AerosagaProjectApplication {

	public static void main(String[] args) {
		SpringApplication.run(AerosagaProjectApplication.class, args);

		System.out.println("hello Aerosaga");
	}

}
