package il.tsv.test.playersservice.consumer;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import il.tsv.test.playersservice.dto.PlayerDTO;
import il.tsv.test.playersservice.repository.PlayerRepository;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.pulsar.client.api.SubscriptionType;
import org.apache.pulsar.common.schema.SchemaType;
import org.springframework.pulsar.annotation.PulsarListener;
import org.springframework.stereotype.Service;

@Service
@Slf4j
@AllArgsConstructor
public class EventConsumer {
    private PlayerRepository repository;



   /* @PulsarListener(
            topics = "${spring.pulsar.producer.topic-name}",
            subscriptionName = "${spring.pulsar.consumer.subscription.name}",
            subscriptionType = SubscriptionType.Shared
    )
    public void consumeTextEvent(String msg) {
        log.info("EventConsumer:: consumeTextEvent consumed events {}", msg);
    }*/


    @PulsarListener(
            topics = "${spring.pulsar.producer.topic-name}",
            subscriptionName = "${spring.pulsar.consumer.subscription.name}",
            schemaType = SchemaType.JSON,
            subscriptionType = SubscriptionType.Shared
    )
    public void consumeRawEvent(PlayerDTO playerDTO) throws JsonProcessingException {
        log.info("Consumed playerDTOEvent  {}", new ObjectMapper().writeValueAsString(playerDTO));
    }
}
