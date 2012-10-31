/**
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package org.apache.ctakes.utils.env;

import org.apache.uima.UimaContext;

public class EnvironmentVariable {

	/**
	 * @param name
	 * @param context
	 * @return String value Get Environment Variable values will attempt to get
	 *         it from System Proerties, then Environment, then uima context
	 */
	public static String getEnv(String name, UimaContext context) {
		String value = null;
		if (name != null && name.trim().length() > 0) {
			//Attempt to get it from system properites, env variables
			value = getEnv(name);
			if (value == null) {
				// Attempt to get it from UIMA Context
				value = (String) context.getConfigParameterValue(name);
			}
		}
		return value;
	}

	/**
	 * @param name
	 * @return value Get Environment Variable values will attempt to get it from
	 *         System Proerties, then Environment
	 */
	public static String getEnv(String name) {
		String value = null;
		if (name != null && name.trim().length() > 0) {
			// Attempt to get it from System Properties
			value = System.getProperty(name);
			if (value == null) {
				// Attempt ot get it from Env Variables
				value = System.getenv(name);
			}
		}
		return value;
	}
}
