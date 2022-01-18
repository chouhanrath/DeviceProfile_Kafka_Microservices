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


public class RunningConsumersWithAggregation2 {
    public static void main(String[] args) throws Exception {
        int noOfTopics=15;
        int noOfGroups=noOfTopics;
        int consumerCount=200;
        int frequency=0; int aggregatedFrequency=0;
        int [][] groupTopicMatrix= new int[consumerCount][noOfGroups];
        int [][] aggregateMatrix= new int[consumerCount][noOfGroups];
        String[] consumerGroups = new String[noOfGroups];
        long startTime=0, endTime, totalTime, aggregatedStartTime=0, aggregatedEndTime, aggregatedTotalTime;
        for (int i = 0; i < consumerGroups.length; i++) {
            consumerGroups[i] ="consumer-group-"+i;
        }
        ExecutorService executorService = Executors.newFixedThreadPool(consumerCount + 1);

        for (int i=0;i<(noOfTopics*consumerCount);i++){
            startTime= System.nanoTime();
            int consumerIdi = (int)Math.floor(Math.random()*(consumerCount-0+0)+0);
            //if (consumerIdi!=0){frequency++;}
            String consumerId = Integer.toString(consumerIdi);
            int consumerGroupId = (int)Math.floor(Math.random()*(noOfGroups-0+0)+0);
            String topicId="my-topic"+consumerGroupId; //"my-topic" + Integer.toString((int)Math.floor(Math.random()*(noOfTopics-0+0)+0));
            //System.out.printf("consumer:%s, group:%s, topic:%s%n", consumerId, consumerGroupId, topicId);
            groupTopicMatrix[consumerIdi][consumerGroupId]=1;
            //if (consumerGroupId != 0){beforeRedudancyMatrix[consumerGroupId]=beforeRedudancyMatrix[consumerGroupId]+1;}
            executorService.execute(() -> startConsumer(consumerId, consumerGroups[consumerGroupId], topicId));
        }

        //for (int brd=0;brd<noOfTopics;brd++){brdCount=brdCount+beforeRedudancyMatrix[brd];}
        //System.out.println("RedudancyBefore:"+brdCount);
        for (int i1=0;i1<consumerCount;i1++)
        {
            int columnIdx=0;
            for (int j1=0;j1<noOfGroups;j1++){
                //System.out.print(groupTopicMatrix[i1][j1]);
                if (groupTopicMatrix[i1][j1]==1){
                    aggregateMatrix[i1][columnIdx]=j1;
                    columnIdx++;frequency++;
                }
            }
            //System.out.println();
        }
        endTime= System.nanoTime();
        totalTime= endTime - startTime;
        System.out.println("Before Aggregation:"+totalTime);
        System.out.println("Frequency:"+frequency);
        for (int c=0;c<consumerCount;c++){
            if (aggregateMatrix[c][0]==0){aggregatedFrequency++;}
        }
        for (int i2=0;i2<consumerCount;i2++)
        {
            aggregatedStartTime=System.nanoTime();
            //System.out.print("user:"+i+"=");
            for (int j2=0;j2<noOfGroups;j2++) {
                //System.out.print(aggregateMatrix[i][j]+",");
                int aggregatedConsumerIdi = i2;
                String aggregatedConsumerId = Integer.toString(aggregatedConsumerIdi);
                int aggregatedConsumerGroupId = aggregateMatrix[i2][j2];
                String aggregatedTopicId="my-topic"+aggregatedConsumerGroupId;
                //if (aggregatedConsumerGroupId != 0){redudancyMatrix[aggregatedConsumerGroupId]=redudancyMatrix[aggregatedConsumerGroupId]+1;}
                executorService.execute(() -> startConsumer(aggregatedConsumerId, consumerGroups[aggregatedConsumerGroupId], aggregatedTopicId));
            }
            //System.out.println();
        }

        //for (int rd=0;rd<noOfTopics;rd++){rdCount=rdCount+redudancyMatrix[rd];}
        //System.out.println("RedudancyAfter:"+rdCount);
        System.out.println("Aggregated Frequency:"+(frequency-aggregatedFrequency));

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
