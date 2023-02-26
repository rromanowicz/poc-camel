package ex.rr.camel;

import com.fasterxml.jackson.databind.ObjectMapper;
import ex.rr.camel.camel.Routes;
import ex.rr.camel.database.ItemRepository;
import ex.rr.camel.database.Order;
import ex.rr.camel.database.OrderRepository;
import ex.rr.camel.database.ProcessingConfirmationRepository;
import ex.rr.camel.rabbitmq.Config;
import ex.rr.camel.rabbitmq.Listener;
import ex.rr.camel.rabbitmq.Publisher;
import ex.rr.camel.service.OrderService;
import org.apache.camel.EndpointInject;
import org.apache.camel.ProducerTemplate;
import org.apache.camel.component.mock.MockEndpoint;
import org.apache.camel.test.spring.junit5.CamelSpringBootTest;
import org.apache.camel.test.spring.junit5.MockEndpoints;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.io.IOException;
import java.util.Collections;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.*;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@CamelSpringBootTest
@MockEndpoints("direct:findOrderById")
public class RoutesTest {

    @Autowired
    private ProducerTemplate producerTemplate;

    @EndpointInject("mock:direct:findOrderById")
    private MockEndpoint mockEndpoint;
    @Autowired
    protected ObjectMapper objectMapper;

    @Autowired
    private Routes routes;
    @Autowired
    private OrderService orderService;
    @MockBean
    private Publisher publisher;
    @MockBean
    private Listener listener;
    @MockBean
    Config config;
    @MockBean
    private OrderRepository orderRepository;
    @MockBean
    private ItemRepository itemRepository;
    @MockBean
    private ProcessingConfirmationRepository processingConfirmationRepository;
    @Autowired
    private TestRestTemplate restTemplate;


    @Test
    void testGetByIdReturnsOk() {
        when(orderRepository.findById(1L)).thenReturn(Optional.of(new Order()));

        ResponseEntity<Order> response = restTemplate.getForEntity("/order/1", Order.class);
        assertEquals(HttpStatus.OK, response.getStatusCode());
    }

    @Test
    void testGetByIdReturnsNotFound() {
        when(orderRepository.findById(anyLong())).thenReturn(Optional.empty());

        ResponseEntity<Order> response = restTemplate.getForEntity("/order/1", Order.class);
        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
    }

    @Test
    void testGetAllReturnsOk() {
        when(orderRepository.findAll()).thenReturn(Collections.singletonList(new Order()));

        ResponseEntity<List> response = restTemplate.getForEntity("/order", List.class);
        assertEquals(HttpStatus.OK, response.getStatusCode());
    }

    @Test
    void testPostOrderReturnsCreated() throws IOException {
        Order order = objectMapper.readValue(getClass().getClassLoader().getResource("order.json"), Order.class);
        when(orderRepository.save(any(Order.class))).thenReturn(new Order());

        ResponseEntity<Order> response = restTemplate.postForEntity("/order", order, Order.class);
        assertEquals(HttpStatus.CREATED, response.getStatusCode());
    }

    @Test
    void testPostOrderReturnsError() {
        when(orderRepository.save(any(Order.class))).thenReturn(new Order());

        ResponseEntity<String> response = restTemplate.postForEntity("/order", "test", String.class);
        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
    }

    @Test
    void testDeleteOrderReturnsOk() {
        doNothing().when(orderRepository).deleteById(anyLong());

        ResponseEntity<Void> response = restTemplate.exchange("/order/1", HttpMethod.DELETE, HttpEntity.EMPTY, Void.class);
        assertEquals(HttpStatus.OK, response.getStatusCode());
    }

    @Test
    void testDeleteOrderReturnsNotFound() {
        doThrow(new NoSuchElementException()).when(orderRepository).deleteById(anyLong());
        ResponseEntity<Void> response = restTemplate.exchange("/order/2", HttpMethod.DELETE, HttpEntity.EMPTY, Void.class);
        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
    }
}
