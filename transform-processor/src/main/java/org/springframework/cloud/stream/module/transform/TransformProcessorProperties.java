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

package org.springframework.cloud.stream.module.transform;

import javax.validation.constraints.AssertTrue;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.core.io.Resource;
import org.springframework.expression.Expression;
import org.springframework.expression.ExpressionParser;
import org.springframework.expression.spel.standard.SpelExpressionParser;
import org.springframework.integration.config.TransformerFactoryBean;
import org.springframework.integration.groovy.GroovyScriptExecutingMessageProcessor;
import org.springframework.integration.scripting.ScriptVariableGenerator;
import org.springframework.scripting.support.ResourceScriptSource;

/**
 * Holds configuration properties for the Transform Processor module.
 *
 * @author Eric Bottard
 */
@ConfigurationProperties
public class TransformProcessorProperties {

	private static final ExpressionParser PARSER = new SpelExpressionParser();

	private static final Expression DEFAULT_EXPRESSION = PARSER.parseExpression("payload");

	/**
	 * Reference to a script used to process messages.
	 */
	private Resource script;

	private Expression expression = DEFAULT_EXPRESSION;

	public Resource getScript() {
		return script;
	}

	public void setScript(Resource script) {
		this.script = script;
	}

	public void setExpression(String expression) {
		this.expression = PARSER.parseExpression(expression);
	}

	public Expression getExpression() {
		return expression;
	}

	@AssertTrue(message = "Exactly one of 'expression' or 'script' must be set")
	public boolean isMutuallyExclusive() {
		return DEFAULT_EXPRESSION.getExpressionString().equals(expression.getExpressionString())
				|| script == null;
	}

}
