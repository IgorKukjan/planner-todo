package ru.javabegin.micro.planner.todo.mq.func;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.messaging.Message;
import ru.javabegin.micro.planner.todo.service.TestDataService;

import java.util.function.Consumer;

@Configuration
public class MessageFunc {
    private TestDataService testDataService;

    public MessageFunc(TestDataService testDataService) {
        this.testDataService = testDataService;
    }

    @Bean
    public Consumer<Message<Long>> newUserActionConsume() {
        Consumer<Message<Long>> consumer = message -> {
//            System.out.println("test dlq");
//            int a = 10, b = 0;
//            System.out.println(a/b);

            testDataService.initTestData(message.getPayload());
        };
        return consumer;
    }
}
