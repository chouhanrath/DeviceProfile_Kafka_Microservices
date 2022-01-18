package org.example.kafka.TopicAggregation;

import java.util.Properties;

public class ProducerConsumerProperties {
    public static final String BROKERS = "localhost:9092";

    public static Properties getProducerProps() {
        Properties props = new Properties();
        props.put("bootstrap.servers", BROKERS);
        props.put("acks", "all");
        props.put("key.serializer", "org.apache.kafka.common.serialization.StringSerializer");
        props.put("value.serializer", "org.apache.kafka.common.serialization.StringSerializer");
        return props;
    }

    public static Properties getConsumerProps(String consumerGroupp) {
        Properties props = new Properties();
        props.setProperty("bootstrap.servers", BROKERS);
        props.setProperty("group.id", consumerGroupp);
        props.setProperty("enable.auto.commit", "false");
        props.setProperty("key.deserializer", "org.apache.kafka.common.serialization.StringDeserializer");
        props.setProperty("value.deserializer", "org.apache.kafka.common.serialization.StringDeserializer");
        return props;
    }

}
