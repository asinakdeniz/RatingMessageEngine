package rating.engine;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableAsync;

@EnableAsync
@SpringBootApplication
public class RatingMessageEngineApplication {

	public static void main(String[] args) {
		SpringApplication.run(RatingMessageEngineApplication.class, args);
	}

}
