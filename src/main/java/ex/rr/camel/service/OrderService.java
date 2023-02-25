package ex.rr.camel.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import ex.rr.camel.database.*;
import ex.rr.camel.rabbitmq.Publisher;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Slf4j
@Service
public class OrderService {

    @Autowired
    private Publisher publisher;

    @Autowired
    private OrderRepository orderRepository;

    @Autowired
    private ItemRepository itemRepository;

    @Autowired
    private ProcessingConfirmationRepository processingConfirmationRepository;


    Order findOrderById(Long id) {
        return orderRepository.findById(id).orElseThrow();
    }

    List<Order> findAllOrders() {
        return orderRepository.findAll();
    }

    public Order saveOrder(Order order) {
       return  orderRepository.save(order);
    }

    void removeOrder(Long id) {
        orderRepository.deleteById(id);
    }

    String test(Order order) {
        return String.format("{id:%s, items:[{id:%s}]", order.getId(), order.getItems().get(0).getId());
    }

    ProcessingConfirmation processOrder(Order order) {
        ProcessingConfirmation confirmation = null;
        Item item = order.getItems().get(0);
        try {
            item.setStatus(Status.COMPLETED);
            confirmation = buildProcessingConfirmation(order);
            publisher.publish(confirmation);
        } catch (JsonProcessingException e) {
            item.setStatus(Status.FAILED);
            log.error(e.getMessage());
        } finally {
            itemRepository.save(item);
        }
        return confirmation;
    }

    private ProcessingConfirmation buildProcessingConfirmation(Order order) {
        Item item = order.getItems().get(0);
        return processingConfirmationRepository.save(
                new ProcessingConfirmation(null, order.getId(), item.getId(), item.getAction(), Status.COMPLETED)
        );
    }


}
