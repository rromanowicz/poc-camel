package ex.rr.camel.rabbitmq;

import org.springframework.amqp.core.Binding;
import org.springframework.amqp.core.BindingBuilder;
import org.springframework.amqp.core.DirectExchange;
import org.springframework.amqp.core.Queue;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class Config {

    @Value("${rabbit.order.exchange}")
    private String exchange;
    @Value("${rabbit.order.state.pending.queue}")
    private String pendingQueue;
    @Value("${rabbit.order.state.processed.queue}")
    private String processedQueue;
    @Value("${rabbit.order.state.processed.routing-key}")
    private String processedRoutingKey;

    @Bean
    public Queue pendingQueue() {
        return new Queue(pendingQueue, false);
    }

    @Bean
    public Queue processedQueue() {
        return new Queue(processedQueue, false);
    }

    @Bean
    DirectExchange exchange() {
        return new DirectExchange(exchange);
    }

    @Bean
    Binding marketingBinding(Queue processedQueue, DirectExchange exchange) {
        return BindingBuilder.bind(processedQueue).to(exchange).with(processedRoutingKey);
    }
}