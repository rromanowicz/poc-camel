package ex.rr.camel.rabbitmq;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import ex.rr.camel.database.Order;
import ex.rr.camel.database.ProcessingConfirmation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Slf4j
@Component
public class Publisher {

    @Value("${rabbit.order.exchange}")
    private String exchange;
    @Value("${rabbit.order.state.pending.queue}")
    private String pendingQueue;
    @Value("${rabbit.order.state.processed.queue}")
    private String processedQueue;
    @Value("${rabbit.order.state.processed.routing-key}")
    private String processedRoutingKey;

    @Autowired
    private RabbitTemplate rabbitTemplate;

    @Autowired
    private ObjectMapper objectMapper;

    public void publish(Order order) throws JsonProcessingException {
        String json = objectMapper.writeValueAsString(order);
        rabbitTemplate.convertAndSend(exchange, processedRoutingKey, json);
        log.info("Order with ID: [{}] published on [{}] routingKey", order.getId(), processedRoutingKey);
    }

    public void publish(ProcessingConfirmation processingConfirmation) throws JsonProcessingException {
        String json = objectMapper.writeValueAsString(processingConfirmation);
        rabbitTemplate.convertAndSend(exchange, processedRoutingKey, json);
        log.info("OrderConfirmation published on [{}] routingKey/ [{}]", processedRoutingKey, processingConfirmation);
    }

}
