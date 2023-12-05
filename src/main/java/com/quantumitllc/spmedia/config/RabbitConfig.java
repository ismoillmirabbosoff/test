package com.quantumitllc.spmedia.config;

import com.quantumitllc.spmedia.messages.constants.BindingKeys;
import com.quantumitllc.spmedia.messages.constants.Exchanges;
import com.quantumitllc.spmedia.messages.constants.Queues;
import org.springframework.amqp.core.AmqpAdmin;
import org.springframework.amqp.core.Binding;
import org.springframework.amqp.core.BindingBuilder;
import org.springframework.amqp.core.DirectExchange;
import org.springframework.amqp.core.Queue;
import org.springframework.amqp.rabbit.annotation.EnableRabbit;
import org.springframework.amqp.rabbit.connection.ConnectionFactory;
import org.springframework.amqp.rabbit.core.RabbitAdmin;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@EnableRabbit
@Configuration
public class RabbitConfig {

  @Bean
  public DirectExchange directExchange() {
    return new DirectExchange(Exchanges.DIRECT_EXCHANGE, true, false);
  }

  @Bean
  public Queue fileDeletionQueue() {
    return new Queue(Queues.FILE_FOR_DELETE_QUEUE, false);
  }

  @Bean
  public Binding fileDeletionBinding(DirectExchange directExchange, Queue fileDeletionQueue) {
    return BindingBuilder.bind(fileDeletionQueue).to(directExchange)
        .with(BindingKeys.FILE_FOR_DELETE_BINDING_ROUTING_KEY);
  }

  @Bean
  public AmqpAdmin amqpAdmin(final ConnectionFactory connectionFactory) {
    return new RabbitAdmin(connectionFactory);
  }

  @Bean
  public Jackson2JsonMessageConverter producerJackson2MessageConverter() {
    return new Jackson2JsonMessageConverter();
  }

  @Bean
  public RabbitTemplate rabbitTemplate(final ConnectionFactory connectionFactory,
      final Jackson2JsonMessageConverter producerJackson2MessageConverter) {
    final var rabbitTemplate = new RabbitTemplate(connectionFactory);
    rabbitTemplate.setMessageConverter(producerJackson2MessageConverter);
    return rabbitTemplate;
  }

}
