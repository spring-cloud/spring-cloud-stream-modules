/*
 * Copyright 2015 the original author or authors.
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
package org.springframework.cloud.stream.module.metrics;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.SpringApplicationConfiguration;
import org.springframework.boot.test.WebIntegrationTest;
import org.springframework.cloud.stream.annotation.Bindings;
import org.springframework.cloud.stream.messaging.Sink;
import org.springframework.data.repository.CrudRepository;
import org.springframework.messaging.Message;
import org.springframework.messaging.support.MessageBuilder;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

/**
 * @author Ilayaperumal Gopinathan
 */
@RunWith(SpringJUnit4ClassRunner.class)
@SpringApplicationConfiguration(classes = FieldValueCounterSinkApplication.class)
@WebIntegrationTest({"server.port:0", "name:FVCounter", "store:redis", "fieldName:test"})
@DirtiesContext
public class FieldValueCounterSinkTests {

	//TODO RedisAvailable Rule

	@Autowired
	@Bindings(FieldValueCounterSink.class)
	private Sink sink;

	@Autowired
	private CrudRepository<FieldValueCounter, String> fieldValueCounterRepository;

	@Before
	public void init() {
		((FieldValueCounterRepository) fieldValueCounterRepository).reset("FVCounter", "Hi");
		((FieldValueCounterRepository) fieldValueCounterRepository).reset("FVCounter", "Hello");
	}

	@After
	public void clear() {
		((FieldValueCounterRepository) fieldValueCounterRepository).reset("FVCounter", "Hi");
		((FieldValueCounterRepository) fieldValueCounterRepository).reset("FVCounter", "Hello");
	}

	@Test
	public void testFieldNameIncrement() throws InterruptedException {
		assertNotNull(this.sink.input());
		Message<String> message = MessageBuilder.withPayload("{\"test\": \"Hi\"}").build();
		sink.input().send(message);
		message = MessageBuilder.withPayload("{\"test\": \"Hello\"}").build();
		sink.input().send(message);
		message = MessageBuilder.withPayload("{\"test\": \"Hi\"}").build();
		sink.input().send(message);
		assertEquals(2, this.fieldValueCounterRepository.findOne("FVCounter").getFieldValueCount().get("Hi").longValue());
		assertEquals(1, this.fieldValueCounterRepository.findOne("FVCounter").getFieldValueCount().get("Hello").longValue());
	}

}
