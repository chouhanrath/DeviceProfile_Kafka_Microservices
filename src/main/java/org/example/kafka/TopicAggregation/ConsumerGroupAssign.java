package org.example.kafka.TopicAggregation;

import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.clients.consumer.ConsumerRecords;
import org.apache.kafka.clients.consumer.KafkaConsumer;
import org.apache.kafka.clients.producer.KafkaProducer;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.example.kafka.ConsumerGroupExercise.ExampleConfig;

import java.io.FileWriter;
import java.io.IOException;
import java.time.Duration;
import java.util.*;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicInteger;

public class ConsumerGroupAssign {
    private final static int PARTITION_COUNT = 3;
    private static int TOPIC_COUNT = 0;//give no of topics here
    private static int GROUP_COUNT = 0;//give no of GROUP here
    private final static String TOPIC_NAME = "example-topic";
    private final static int MSG_COUNT = 4;
    private static int totalMsgToSend;
    private static AtomicInteger msg_received_counter = new AtomicInteger(0);
    public static Random random =new Random();
    public static int [][] groupTopicMatrix= new int[30][30];
    public static List<Integer> list = new ArrayList<Integer>();

    public ConsumerGroupAssign(int i, int i1) {
        TOPIC_COUNT=i;
        GROUP_COUNT=i1;
    }


    public static void run (int consumerCount, String[] consumerGroups) throws Exception{
        int distinctGroups =new TreeSet<>(Arrays.asList(consumerGroups)).size();

        totalMsgToSend = MSG_COUNT * PARTITION_COUNT * distinctGroups;
        ExecutorService executorService = Executors.newFixedThreadPool(consumerCount + 1);

        for (int consumerGroupNo=0; consumerGroupNo < consumerGroups.length; consumerGroupNo++){
            //System.out.print(consumerGroupNo+":");
            for (int i = 0; i < consumerCount; i++) {
                String consumerId = Integer.toString(i + 1);
                int finalI = (int)Math.floor(Math.random()*(GROUP_COUNT-0+0)+0);//random.nextInt(consumerGroups.length);
                int topicFinalI=(int)Math.floor(Math.random()*(TOPIC_COUNT-0+0)+0);
                groupTopicMatrix[finalI][topicFinalI]=1;
                //System.out.printf("Group no:%d, topic no:%d%n", finalI, topicFinalI);
                String TOPIC_ID = "my-topic" + Integer.toString(topicFinalI);
                executorService.execute(() -> startConsumer(consumerId, consumerGroups[finalI], TOPIC_ID));
            }

        }
        System.out.println("matrix is:");
        for (int gc = 0; gc < GROUP_COUNT; gc++) {
            //System.out.print(gc+":");
            for (int tc = 0; tc < TOPIC_COUNT; tc++) {
                System.out.print(groupTopicMatrix[gc][tc] + "  ");
            }
            System.out.println();
        }

        //System.out.println("Printing List: "+list);
        System.out.println("MS to be composed:");
        FileWriter fw = new FileWriter("out.txt");
        try {
            for (int tc2 = 0; tc2 < GROUP_COUNT; tc2++) {
                fw.write(tc2 + "-");
                for (int gc2 = 0; gc2 < TOPIC_COUNT; gc2++) {
                    if (groupTopicMatrix[tc2][gc2] == 1) {
                        System.out.printf("ms-%d/", gc2);
                        fw.write(gc2 + ",");
                    }
                }
                System.out.println();
                fw.write(":");
            }
            fw.close();
        }
        catch(IOException e){e.printStackTrace();}


        //executorService.execute(ConsumerGroupExample::sendMessages);
        //executorService.shutdown();
        //executorService.awaitTermination(10, TimeUnit.MINUTES);
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
                msg_received_counter.incrementAndGet();
                System.out.printf("Customer id=%s, Topic=%s, partition id=%s, key=%s,value=%s, offset=%s%n",
                        consumerId, record.topic(), record.partition(),record.key(),record.value(),record.offset());
            }
            consumer.commitSync();
            if(msg_received_counter.get()==totalMsgToSend){
                break;
            }
        }
    }


    private static void sendMessages(){
        Properties producerProps = ExampleConfig.getProducerProps();
        KafkaProducer producer = new KafkaProducer<>(producerProps);
        int key=0;
        for (int i=0; i<MSG_COUNT;i++){
            for (int partitionId=0;partitionId< PARTITION_COUNT; partitionId++){
                String value="chouhan-"+i;
                key++;
                System.out.printf("sending msg topic:%s, key=%s,value=%s,partition id:%s%n",
                        TOPIC_NAME ,key, value, partitionId);
                producer.send(new ProducerRecord<>(TOPIC_NAME, partitionId, Integer.toString(key),value));
            }
        }
    }
}
