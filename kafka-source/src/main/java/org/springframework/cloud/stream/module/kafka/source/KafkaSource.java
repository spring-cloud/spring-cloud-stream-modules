/*
 * Copyright 2016 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.springframework.cloud.stream.module.kafka.source;

import kafka.serializer.Decoder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cloud.stream.annotation.Bindings;
import org.springframework.cloud.stream.annotation.EnableBinding;
import org.springframework.cloud.stream.messaging.Source;
import org.springframework.context.annotation.Bean;
import org.springframework.integration.dsl.IntegrationFlow;
import org.springframework.integration.dsl.IntegrationFlows;
import org.springframework.integration.dsl.kafka.Kafka;
import org.springframework.integration.kafka.core.*;
import org.springframework.integration.kafka.listener.KafkaMessageListenerContainer;
import org.springframework.integration.kafka.listener.KafkaNativeOffsetManager;
import org.springframework.integration.kafka.listener.OffsetManager;
import org.springframework.integration.kafka.support.ZookeeperConnect;
import org.springframework.util.Assert;
import org.springframework.util.ObjectUtils;
import org.springframework.util.StringUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * Source module that receives data from Kafka.
 *
 * @author Soby Chacko
 */
@EnableBinding(Source.class)
public class KafkaSource {

    @Autowired
    @Bindings(KafkaSource.class)
    private Source source;

    @Autowired
    private KafkaConfigurationProperties properties;

    @Bean
    public KafkaMessageListenerContainer container() {

        Assert.isTrue(ObjectUtils.isEmpty(properties.getTopics()) && ObjectUtils.isEmpty(properties.getPartitions()),
                "Either a list of topics OR a topic=<comma separated partitions> must be provided");

        ZookeeperConnect zookeeperConnect = new ZookeeperConnect();
        zookeeperConnect.setZkConnect(properties.getZkConnect());
        zookeeperConnect.setZkConnectionTimeout(properties.getZkConnectionTimeout());
        zookeeperConnect.setZkSessionTimeout(properties.getZkSessionTimeout());
        zookeeperConnect.setZkSyncTime(properties.getZkSyncTime());

        Configuration zookeeperConfiguration = new ZookeeperConfiguration(zookeeperConnect);
        ConnectionFactory kafkaConnectionFactory = new DefaultConnectionFactory(zookeeperConfiguration);

        Partition[] partitions = getPartitions();

        OffsetManager kafkaNativeOffsetManager = new KafkaNativeOffsetManager(kafkaConnectionFactory, zookeeperConnect);

        KafkaMessageListenerContainer container = ObjectUtils.isEmpty(partitions) ?
                new KafkaMessageListenerContainer(kafkaConnectionFactory, properties.getTopics()) :
                new KafkaMessageListenerContainer(kafkaConnectionFactory, partitions);

        container.setOffsetManager(kafkaNativeOffsetManager);

        return container;
    }

    @Bean
    public IntegrationFlow fromKafka() throws IllegalAccessException, InstantiationException {

        Class keyDecoderClass = properties.getKeyDecoder();
        Class payloadDecoderClass = properties.getPayloadDecoder();

        //TODO: Address if decoder implementations have constructor args?
        //TODO: For ex if the StringDecoder needs to use a different encoding etc.
        Decoder keyDeocder = (Decoder)keyDecoderClass.newInstance();
        Decoder payloadDeocder = (Decoder) payloadDecoderClass.newInstance();

        return IntegrationFlows.from(Kafka.messageDriverChannelAdapter(container())
                                    .keyDecoder(keyDeocder)
                                    .payloadDecoder(payloadDeocder))
                .channel(source.output())
                .get();
    }


    public Partition[] getPartitions() {
        Map<String, String> partitionsMap = properties.getPartitions();
        List<Partition> partitions = new ArrayList<>();

        if (!partitionsMap.isEmpty()) {
            for (Map.Entry<String, String> entry : partitionsMap.entrySet()) {

                String[] topicPartitions = StringUtils.commaDelimitedListToStringArray(entry.getValue());

                for (String part : topicPartitions) {
                    partitions.add(new Partition(entry.getKey(), Integer.valueOf(part)));
                }
            }
        }

        return (Partition[])partitions.toArray();
    }
}
