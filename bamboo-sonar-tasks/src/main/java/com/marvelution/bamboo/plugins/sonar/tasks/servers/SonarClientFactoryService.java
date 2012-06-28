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
import org.sonar.wsclient.connectors.HttpClient4Connector;

/**
 * Default {@link SonarClientFactory}
 * 
 * @author <a href="mailto:markrekveld@marvelution.com">Mark Rekveld</a>
 */
public class SonarClientFactoryService implements SonarClientFactory {

	/**
	 * Constructor
	 */
	public SonarClientFactoryService() {
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public Sonar create(Host host) {
		return new Sonar(new HttpClient4Connector(host));
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public Sonar create(SonarServer server) {
		return create(new Host(server.getHost(), server.getUsername(), server.getPassword()));
	}

}
