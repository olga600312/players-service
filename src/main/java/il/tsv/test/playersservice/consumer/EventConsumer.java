package il.tsv.test.playersservice.consumer;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import il.tsv.test.playersservice.dto.PlayerDTO;
import il.tsv.test.playersservice.repository.PlayerRepository;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.pulsar.client.api.SubscriptionType;
import org.apache.pulsar.common.schema.SchemaType;
import org.springframework.messaging.handler.annotation.Header;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.pulsar.annotation.PulsarListener;
import org.springframework.pulsar.support.PulsarHeaders;
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
    @PulsarListener(
            topics = "null_value_topic",
            subscriptionName = "${spring.pulsar.consumer.subscription.name}",
            schemaType = SchemaType.BYTES,
            subscriptionType = SubscriptionType.Shared
    )
    public void consumeNullEvent( @Payload(required = false) PlayerDTO playerDTO,
                                  @Header(PulsarHeaders.KEY) String key) throws JsonProcessingException {
        log.info("Consumed null playerDTOEvent {} from key {}"
                , playerDTO!=null?new ObjectMapper().writeValueAsString(playerDTO):"null",
                key);
    }
}
