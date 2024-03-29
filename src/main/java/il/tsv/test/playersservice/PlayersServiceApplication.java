package il.tsv.test.playersservice;

import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.binder.MeterBinder;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;

@SpringBootApplication
public class PlayersServiceApplication  {

	public static void main(String[] args) {
		SpringApplication.run(PlayersServiceApplication.class, args);
	}


	@Bean
	MeterBinder meterBinderTotal() {
		return meterRegistry -> Counter.builder("get_player_all")
				.description("Get Player counter")
				.register(meterRegistry);
	}

}
