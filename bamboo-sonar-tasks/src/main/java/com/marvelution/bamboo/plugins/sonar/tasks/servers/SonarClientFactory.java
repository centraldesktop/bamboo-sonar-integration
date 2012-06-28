/*
 * Licensed to Marvelution under one or more contributor license 
 * agreements.  See the NOTICE file distributed with this work 
 * for additional information regarding copyright ownership.
 * Marvelution licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License.
 * You may obtain a copy of the License at
 *
 *  http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package com.marvelution.bamboo.plugins.sonar.tasks.servers;

import org.sonar.wsclient.Host;
import org.sonar.wsclient.Sonar;

/**
 * {@link Sonar} factory interface
 * 
 * @author <a href="mailto:markrekveld@marvelution.com">Mark Rekveld</a>
 */
public interface SonarClientFactory {

	/**
	 * Create a {@link Sonar} for the given {@link Host}
	 * 
	 * @param host the {@link Host} to get the {@link Sonar} for
	 * @return the {@link Sonar}
	 */
	Sonar create(Host host);

	/**
	 * Create a {@link Sonar} for the given {@link SonarServer}
	 * 
	 * @param server the {@link SonarServer} to get the {@link Sonar} for
	 * @return the {@link Sonar}
	 */
	Sonar create(SonarServer server);

}
