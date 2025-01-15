package ru.javabegin.micro.planner.todo.mq.func

import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.messaging.Message
import ru.javabegin.micro.planner.todo.service.TestDataService
import java.util.function.Consumer

@Configuration
class MessageFunc(private val testDataService: TestDataService) {
    @Bean
    fun newUserActionConsume(): Consumer<Message<Long>> {
        val consumer =
            Consumer { message: Message<Long> ->
//            System.out.println("test dlq");
//            int a = 10, b = 0;
//            System.out.println(a/b);
                testDataService.initTestData(message.payload)
            }
        return consumer
    }
}
