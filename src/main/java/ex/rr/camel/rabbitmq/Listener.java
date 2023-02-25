package ex.rr.camel.rabbitmq;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import ex.rr.camel.database.ProcessingConfirmation;
import ex.rr.camel.service.OrderService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Slf4j
@Component
public class Listener {

    @Autowired
    ObjectMapper objectMapper;

    @Autowired
    private OrderService orderService;

    @RabbitListener(queues = "${rabbit.order.state.processed.queue}")
    void listener(String message) {
        try {
            ProcessingConfirmation order = objectMapper.readValue(message, ProcessingConfirmation.class);
            log.info("Order processed [{}]", order);
        } catch (JsonProcessingException e) {
            log.error("Failed to process message. [{}]", message);
        }
    }
}
