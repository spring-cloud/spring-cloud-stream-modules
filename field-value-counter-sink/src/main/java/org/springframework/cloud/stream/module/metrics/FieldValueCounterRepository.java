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

/**
 * A repository to save, delete and find FieldValueCounter instances. Uses the Spring Data Repository marker interface
 * and conventions for method names and behavior.
 * 
 * The name is the id and should be unique.
 * 
 * @author Mark Pollack
 * @author Ilayaperumal Gopinathan
 */
public interface FieldValueCounterRepository {

	/**
	 * Increment the FieldValueCounter for a given field name by score, creating missing counters.
	 * 
	 * @param name the FieldValueCounter name
	 * @param fieldName the name of the field
	 * @param score the incremental value
	 * @throws IllegalArgumentException in case the given name is null
	 */
	void increment(String name, String fieldName, double score);

	/**
	 * Decrement the FieldValueCounter for a given field name by score, creating missing counters.
	 * 
	 * @param name the FieldValueCounter name
	 * @param fieldName the name of the field
	 * @param score the decremental value
	 * @throws IllegalArgumentException in case the given name is null
	 */
	void decrement(String name, String fieldName, double score);

	/**
	 * Reset the FieldValueCounter to zero for the given field name, creating missing counters.
	 * 
	 * @param name the FieldValueCounter name
	 * @param fieldName the name of the field
	 * @throws IllegalArgumentException in case the given name is null
	 */
	void reset(String name, String fieldName);
}
