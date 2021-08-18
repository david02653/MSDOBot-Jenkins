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

import java.util.Arrays;

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

    public static final String EXCHANGE_NAME = "myExchange";
    public static final String QUEUE_NAME = "myQueue";
    public static final String TEST_EXCHANGE = "topic_logs";
    public static final String TEST_QUEUE = "testQ";
    public static final String JENKINS_EXCHANGE = "jenkins";
    public static final String JENKINS_QUEUE = "jChannel";
    public static final String EUREKA_EXCHANGE = "eurekaserver";
    public static final String EUREKA_QUEUE = "eureka";

    public static final String MISCELLANEOUS = "MISCELLANEOUS";
    public static final String MASTER_QUEUE = "master";



    @Bean
    public MessageConverter jsonMessageConverter(){
        return new Jackson2JsonMessageConverter();
    }

    // bind: myExchange - <"dog.#"> --> (myQueue)
    //         exchange   routingKey    queue(channel)
    @Bean
    Queue createQueue(){
        return new Queue(QUEUE_NAME, true);
    }
    @Bean
    TopicExchange exchange(){
        return new TopicExchange(EXCHANGE_NAME);
    }
//    @Bean
//    Binding binding(Queue q, TopicExchange topicExchange){
//        return BindingBuilder.bind(q).to(topicExchange).with("dog.#");
//    }
    @Bean
    Binding binding(){
        return BindingBuilder.bind(createQueue()).to(exchange()).with("dog.#");
    }

    @Bean
    Queue createQueueA(){
        return new Queue(TEST_QUEUE, true);
    }
    @Bean
    TopicExchange exchangeA(){
        return new TopicExchange(TEST_EXCHANGE);
    }
    @Bean
    Binding bindingA(){
        return BindingBuilder.bind(createQueueA()).to(exchangeA()).with("rat.#");
    }
    
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

    @Bean
    Queue createEurekaQueue(){
        return new Queue(EUREKA_QUEUE, true);
    }
    @Bean
    TopicExchange exchangeEureka(){
        return new TopicExchange(EUREKA_EXCHANGE);
    }
    @Bean
    Binding bindEureka(){
        return BindingBuilder.bind(createEurekaQueue()).to(exchangeEureka()).with("eureka.*");
    }

    @Bean
    Queue createMiscellaneousQueue(){
        return new Queue(MASTER_QUEUE, true);
    }
    @Bean
    TopicExchange exchangeMiscellaneous(){
        return new TopicExchange(MISCELLANEOUS);
    }
    @Bean
    Binding bindMiscellaneous(){
        return BindingBuilder.bind(createMiscellaneousQueue()).to(exchangeMiscellaneous()).with("#");
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
    MessageListenerAdapter listenerAdapter(RabbitMessageHandler handler){
        return new MessageListenerAdapter(handler, "handleMessage");
    }
    @Bean
    MessageListenerAdapter jenkinsListener(RabbitMessageHandler handler){
        return new MessageListenerAdapter(handler, "handleJenkinsMessage");
    }
    @Bean
    MessageListenerAdapter eurekaListener(RabbitMessageHandler handler){
        return new MessageListenerAdapter(handler, "handleEurekaMessage");
    }

    /**
     * bind exchange, queue, message handler together
     * @param connectionFactory
     * @return consumer container
     */
    /* consumer settings */
    @Bean
    SimpleMessageListenerContainer container(ConnectionFactory connectionFactory/*, MessageListenerAdapter adapter*/){
        SimpleMessageListenerContainer container = new SimpleMessageListenerContainer();
        container.setConnectionFactory(connectionFactory);
        container.setQueueNames(QUEUE_NAME);
//        container.setMessageListener(adapter);
        container.setMessageListener(listenerAdapter(rabbitMessageHandler));

        return container;
    }

    @Bean
    SimpleMessageListenerContainer jenkinsContainer(ConnectionFactory connectionFactory){
        SimpleMessageListenerContainer container = new SimpleMessageListenerContainer();
        container.setConnectionFactory(connectionFactory);
        container.setQueueNames(JENKINS_QUEUE);
        container.setMessageListener(jenkinsListener(rabbitMessageHandler));

        return container;
    }

    @Bean
    SimpleMessageListenerContainer eurekaContainer(ConnectionFactory connectionFactory){
        SimpleMessageListenerContainer container = new SimpleMessageListenerContainer();
        container.setConnectionFactory(connectionFactory);
        container.setQueueNames(EUREKA_QUEUE);
        container.setMessageListener(eurekaListener(rabbitMessageHandler));
        return container;
    }

    @Bean
    SimpleMessageListenerContainer miscellaneousContainer(ConnectionFactory connectionFactory){
        SimpleMessageListenerContainer container = new SimpleMessageListenerContainer();
        container.setConnectionFactory(connectionFactory);
        container.setQueueNames(MASTER_QUEUE);
        container.setMessageListener((MessageListener) message -> {
            System.out.println(message.getMessageProperties());
            System.out.println(message.getMessageProperties().getContentType());
            System.out.println(message.getMessageProperties().getTimestamp());
            System.out.println(Arrays.toString(message.getBody()));
            String msg = new String(message.getBody());
            System.out.println(msg);
            try {
                jdaService.getJda().awaitReady();
                jdaService.send("text2", " [x] from '" + message.getMessageProperties().getReceivedRoutingKey() + "' : " + msg);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        });
        return container;
    }
}
