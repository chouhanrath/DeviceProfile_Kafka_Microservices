package org.example.kafka.TopicAggregation;

import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.clients.consumer.ConsumerRecords;
import org.apache.kafka.clients.consumer.KafkaConsumer;
import org.example.kafka.ConsumerGroupExercise.ConsumerGroupExample;
import org.example.kafka.ConsumerGroupExercise.ExampleConfig;

import javax.imageio.IIOException;
import java.io.FileWriter;
import java.io.IOException;
import java.time.Duration;
import java.util.Arrays;
import java.util.Properties;
import java.util.TreeSet;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;


public class RunningConsumersWithAggregation {
    public static void main(String[] args) throws Exception {
        int noOfTopics=10;
        int noOfGroups=10;
        int consumerCount=20;
        int [][] groupTopicMatrix= new int[noOfGroups][noOfTopics];
        String[] consumerGroups = new String[noOfGroups];
        for (int i = 0; i < consumerGroups.length; i++) {
            consumerGroups[i] ="consumer-group-"+i;
        }
        ExecutorService executorService = Executors.newFixedThreadPool(consumerCount + 1);

        for (int i=0;i<consumerCount;i++){
            String consumerId = Integer.toString(i);
            int consumerGroupId = (int)Math.floor(Math.random()*(noOfGroups-0+0)+0);
            String topicId="my-topic" + Integer.toString((int)Math.floor(Math.random()*(noOfTopics-0+0)+0));
            System.out.printf("consumer:%s, group:%s, topic:%s%n", consumerId, consumerGroupId, topicId);
            executorService.execute(() -> startConsumer(consumerId, consumerGroups[consumerGroupId], topicId));
        }
    }
    private static void startConsumer(String consumerId, String consumerGroup, String TOPIC_ID){
        //System.out.printf("starting consumer: %s, group: %s, Topic: %s%n", consumerId,consumerGroup, TOPIC_ID);

        Properties consumerPorps = ExampleConfig.getConsumerProps(consumerGroup);
        KafkaConsumer<String, String> consumer = new KafkaConsumer<String, String>(consumerPorps);
        consumer.subscribe(Arrays.asList(TOPIC_ID));
        //consumer.subscribe(Collections.singleton(TOPIC_NAME));
        while (true){
            ConsumerRecords<String,String> records = consumer.poll(Duration.ofSeconds(2));
            for (ConsumerRecord<String, String> record : records){
                System.out.printf("Customer id=%s, Group=%s, Topic=%s, partition id=%s, key=%s,value=%s, offset=%s%n",
                        consumerId, consumerGroup, record.topic(), record.partition(),record.key(),record.value(),record.offset());
            }
            consumer.commitSync();
        }
    }
}
