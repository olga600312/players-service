package il.tsv.test.playersservice.config;

import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.Timer;
import io.micrometer.core.instrument.binder.MeterBinder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class WSConfiguration {
    @Bean
    MeterBinder meterBinderTotal() {
        return meterRegistry -> {
            Counter.builder("get_player_all")
                    .description("Get Player counter")
                    .register(meterRegistry);
            Counter.builder("get_player_by_id")
                    .description("Get Player By Id counter")
                    .tag("id","")
                    .register(meterRegistry);
            Timer.builder("timer.players.all")
                    .description("Meter Timing")
                   // .publishPercentileHistogram(true)
                    .register(meterRegistry);
        };
    }
}
