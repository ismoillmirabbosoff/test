package com.quantumitllc.spmedia.messages.producers;

import com.quantumitllc.spmedia.messages.constants.Exchanges;
import java.io.Serializable;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Slf4j
public class EventProducer {

  private final RabbitTemplate rabbitTemplate;

  public void sendFanout(Serializable message) {
    rabbitTemplate.convertAndSend(Exchanges.FANOUT_EXCHANGE, "", message);
    log.info("Sending event: {}", message);
  }

  public void sendTopic(Serializable message, String key) {
    rabbitTemplate.convertAndSend(Exchanges.TOPIC_EXCHANGE, key, message);
    log.info("Sending event with routing [ key: {} ] : {}", key, message);
  }

}
