package org.example.kafka.TopicAggregation;

import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.clients.consumer.ConsumerRecords;
import org.apache.kafka.clients.consumer.KafkaConsumer;
import org.example.kafka.ConsumerGroupExercise.ExampleConfig;

import java.time.Duration;
import java.util.Arrays;
import java.util.Properties;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;


public class RunningConsumersWithAggregation1 {
    public static void main(String[] args) throws Exception {
        int noOfTopics=50;
        int noOfGroups=noOfTopics;
        int consumerCount=100;
        int [][] groupTopicMatrix= new int[consumerCount][noOfGroups];
        int [][] aggregateMatrix= new int[consumerCount][noOfGroups];
        String[] consumerGroups = new String[noOfGroups];
        long startTime=0, endTime, totalTime, aggregatedStartTime=0, aggregatedEndTime, aggregatedTotalTime;
        for (int i = 0; i < consumerGroups.length; i++) {
            consumerGroups[i] ="consumer-group-"+i;
        }
        ExecutorService executorService = Executors.newFixedThreadPool(consumerCount + 1);

        for (int i=0;i<consumerCount;i++){
            startTime= System.nanoTime();
            int consumerIdi = (int)Math.floor(Math.random()*(consumerCount-0+0)+0);
            String consumerId = Integer.toString(consumerIdi);
            int consumerGroupId = (int)Math.floor(Math.random()*(noOfGroups-0+0)+0);
            String topicId="my-topic"+consumerGroupId; //"my-topic" + Integer.toString((int)Math.floor(Math.random()*(noOfTopics-0+0)+0));
            //System.out.printf("consumer:%s, group:%s, topic:%s%n", consumerId, consumerGroupId, topicId);
            groupTopicMatrix[consumerIdi][consumerGroupId]=1;
            executorService.execute(() -> startConsumer(consumerId, consumerGroups[consumerGroupId], topicId));
        }

        for (int i=0;i<consumerCount;i++)
        {
            int columnIdx=0;
            for (int j=0;j<noOfGroups;j++){
                //System.out.print(groupTopicMatrix[i][j]);
                if (groupTopicMatrix[i][j]==1){
                    aggregateMatrix[i][columnIdx]=j;
                    columnIdx++;
                }
            }
            //System.out.println();
        }
        endTime= System.nanoTime();
        totalTime= endTime - startTime;
        System.out.println("Before Aggregation:"+totalTime);

        for (int i=0;i<consumerCount;i++)
        {
            aggregatedStartTime=System.nanoTime();
            //System.out.print("user:"+i+"=");
            for (int j=0;j<noOfGroups;j++) {
                //System.out.print(aggregateMatrix[i][j]+",");
                int aggregatedConsumerIdi = i;
                String aggregatedConsumerId = Integer.toString(aggregatedConsumerIdi);
                int aggregatedConsumerGroupId = aggregateMatrix[i][j];
                String aggregatedTopicId="my-topic"+aggregatedConsumerGroupId;
                executorService.execute(() -> startConsumer(aggregatedConsumerId, consumerGroups[aggregatedConsumerGroupId], aggregatedTopicId));
            }
            //System.out.println();
        }

        aggregatedEndTime=System.nanoTime();
        aggregatedTotalTime=aggregatedEndTime-aggregatedStartTime;
        System.out.println("\nAfter Aggregation:"+aggregatedTotalTime);
    }
    private static void startConsumer(String consumerId, String consumerGroup, String TOPIC_ID){
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
