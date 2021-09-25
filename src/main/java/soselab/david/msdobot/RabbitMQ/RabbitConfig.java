package soselab.david.msdobot.RabbitMQ;

import org.springframework.amqp.core.*;
import org.springframework.amqp.rabbit.connection.ConnectionFactory;
import org.springframework.amqp.rabbit.listener.SimpleMessageListenerContainer;
import org.springframework.amqp.rabbit.listener.adapter.MessageListenerAdapter;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.amqp.support.converter.MessageConverter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.env.Environment;
import soselab.david.msdobot.RabbitMQ.Consumer.RabbitMessageHandler;
import soselab.david.msdobot.Service.JDAConnect;

/**
 * this class controls connection with rabbitMQ server
 *
 * to add new consumer connection, you need to add create a new exchange and queue, you can also use existing ones
 * 1. create queue
 * 2. create exchange
 * 3. bind exchange and queue, note that you can set routing key in this step
 * 4. define which class and which function will handle incoming message from your queue
 * 5. create message listener container
 *
 * use existing code below as template
 */
@Configuration
public class RabbitConfig {

    private final JDAConnect jdaService;
    private final RabbitMessageHandler rabbitMessageHandler;

    public RabbitConfig(JDAConnect jdaConnect, RabbitMessageHandler rabbitMessageHandler, Environment env){
        this.jdaService = jdaConnect;
        this.rabbitMessageHandler = rabbitMessageHandler;
    }

    public static final String JENKINS_EXCHANGE = "jenkins";
    public static final String JENKINS_QUEUE = "jChannel";


    @Bean
    public MessageConverter jsonMessageConverter(){
        return new Jackson2JsonMessageConverter();
    }

    // bind: myExchange - <"dog.#"> --> (myQueue)
    //         exchange   routingKey    queue(channel)
//    @Bean
//    Binding binding(Queue q, TopicExchange topicExchange){
//        return BindingBuilder.bind(q).to(topicExchange).with("dog.#");
//    }
    
    @Bean
    Queue createJenkinsQueue(){
        return new Queue(JENKINS_QUEUE, true);
    }
    @Bean
    TopicExchange exchangeJenkins(){
        return new TopicExchange(JENKINS_EXCHANGE);
    }
    @Bean
    Binding bindJenkins(){
        return BindingBuilder.bind(createJenkinsQueue()).to(exchangeJenkins()).with("jenkins.*");
    }

    /* consumer message function settings */

    /**
     * bind rabbitmq consumer with assigned class and method
     * let assigned method to handle incoming message
     * note that received message type will be byte array
     * @param handler message handler
     * @return instance of message listener adapter
     */
    @Bean
    MessageListenerAdapter jenkinsListener(RabbitMessageHandler handler){
        return new MessageListenerAdapter(handler, "handleJenkinsMessage");
    }

    /* consumer settings */
    /**
     * bind exchange, queue, message handler together
     * @param connectionFactory
     * @return consumer container
     */
    @Bean
    SimpleMessageListenerContainer jenkinsContainer(ConnectionFactory connectionFactory){
        SimpleMessageListenerContainer container = new SimpleMessageListenerContainer();
        container.setConnectionFactory(connectionFactory);
        container.setQueueNames(JENKINS_QUEUE);
        container.setMessageListener(jenkinsListener(rabbitMessageHandler));

        return container;
    }
}
