package ru.feech.conf;

import org.springframework.amqp.core.AmqpAdmin;
import org.springframework.amqp.core.Queue;
import org.springframework.amqp.rabbit.annotation.EnableRabbit;
import org.springframework.amqp.rabbit.connection.CachingConnectionFactory;
import org.springframework.amqp.rabbit.connection.ConnectionFactory;
import org.springframework.amqp.rabbit.core.RabbitAdmin;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.stereotype.Component;

import java.util.logging.Logger;

/**
 * Created by feech on 01.10.15.
 */

@EnableRabbit
@Configuration
@Component
//@ConfigurationProperties(prefix = "rabbit")
public class RabbitConfiguration {
    final private Logger l = Logger.getLogger("+++"+RabbitConfiguration.class.getName());

//    @Value("${test.name}")
//    private String info;


    @Value("${rabbit.host}")
    String host;
    @Value("${rabbit.port}")
    Integer port;
    @Value("${rabbit.user}")
    String user;
    @Value("${rabbit.password}")
    String password;
    @Value("${rabbit.queue_on_split}")
    String queue_on_split;
//    @Value("${rabbit.queue_splited}")
//    String queue_splited;
//    @Value("${rabbit.info_out}")
//    String exchange;

//    @Bean
//    public String test()
//    {
//        l.info("bean test^ " + info);
//        return null;
//    }

    @Bean
    public ConnectionFactory connectionFactory() {
        CachingConnectionFactory connectionFactory =
                new CachingConnectionFactory(host, port);
        connectionFactory.setUsername(user);
        connectionFactory.setPassword(password);
        return connectionFactory;
    }

    @Bean
    public AmqpAdmin amqpAdmin() {
        final RabbitAdmin rabbitAdmin = new RabbitAdmin(connectionFactory());
        rabbitAdmin.setAutoStartup(true);
        return rabbitAdmin;
    }

    @Bean
    public RabbitTemplate rabbitTemplate() {
        return new RabbitTemplate(connectionFactory());
    }

    @Bean
    public Queue to_split() {
        l.info("init: " + queue_on_split);
        return new Queue(queue_on_split, false, false, false);
    }

//    @Bean
//    public FanoutExchange myExchange() {
//        return new FanoutExchange(exchange, false, false);
//    }
}
