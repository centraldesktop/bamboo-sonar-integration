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

import java.util.Collection;

/**
 * {@link SonarServer} manager interface
 * 
 * @author <a href="mailto:markrekveld@marvelution.com">Mark Rekveld</a>
 *
 * @since 1.2.0
 */
public interface SonarServerManager {

	/**
	 * Get if at least one {@link SonarServer} is configured
	 * 
	 * @return <code>true</code> if at least one {@link SonarServer} is configured, <code>false</code> otherwise
	 * @see #hasServers()
	 */
	boolean isConfigured();

	/**
	 * Check if there are any {@link SonarServer} objects available
	 * 
	 * @return <code>true</code> if there are {@link SonarServer} available, <code>false</code> otherwise
	 */
	boolean hasServers();

	/**
	 * Check if there is a {@link SonarServer} with the given Id
	 * 
	 * @param serverId the {@link SonarServer} Id to check
	 * @return <code>true</code> if the server with the given Id exists, <code>false</code> otherwise
	 */
	boolean hasServer(int serverId);

	/**
	 * Check if there is a {@link SonarServer} with the given Name
	 * 
	 * @param name the {@link SonarServer} Name to check
	 * @return <code>true</code> if the server with the given Name exists, <code>false</code> otherwise
	 */
	boolean hasServer(String name);

	/**
	 * Getter for the {@link Collection} of all configured {@link SonarServer} objects
	 * 
	 * @return the {@link Collection} of {@link SonarServer} objects
	 */
	Collection<SonarServer> getServers();

	/**
	 * Getter for a {@link SonarServer} by its Id
	 * 
	 * @param serverId the {@link SonarServer} Id
	 * @return the {@link SonarServer}, may be <code>null</code>
	 */
	SonarServer getServer(int serverId);

	/**
	 * Getter for a {@link SonarServer} by its name
	 * 
	 * @param name the {@link SonarServer} name
	 * @return the {@link SonarServer}, may be <code>null</code>
	 */
	SonarServer getServer(String name);

	/**
	 * Add a {@link SonarServer}
	 *
	 * @param name the name of the {@link SonarServer}
	 * @param description the description of the {@link SonarServer}
	 * @param host the base host of the {@link SonarServer} 
	 * @param username the username to use for authentication
	 * @param password the password to use for authentication
	 * @return the new {@link SonarServer}
	 */
	SonarServer addServer(String name, String description, String host, String username, String password);

	/**
	 * Add a {@link SonarServer}
	 *
	 * @param name the name of the {@link SonarServer}
	 * @param description the description of the {@link SonarServer}
	 * @param host the base host of the {@link SonarServer} 
	 * @param username the username to use for authentication
	 * @param password the password to use for authentication
	 * @param jdbcUrl the JDBC Resource URL
	 * @param jdbcDriver the JDBC Resource Driver
	 * @param jdbcUsername the username to use for JDBC Resource authentication
	 * @param jdbcPassword the password to use for JDBC Resource authentication
	 * @return the new {@link SonarServer}
	 */
	SonarServer addServer(String name, String description, String host, String username, String password,
					String jdbcUrl, String jdbcDriver, String jdbcUsername, String jdbcPassword);

	/**
	 * Add a copy of the given {@link SonarServer}
	 * 
	 * @param server the {@link SonarServer} to copy
	 * @return the new {@link SonarServer}
	 */
	SonarServer addServer(SonarServer server);

	/**
	 * Update a {@link SonarServer}
	 * 
	 * @param serverId the Id of the server to update
	 * @param name the name of the {@link SonarServer}
	 * @param description the description of the {@link SonarServer}
	 * @param host the base host of the {@link SonarServer}
	 * @param username the username to use for authentication
	 * @param password the password to use for authentication
	 * @param jdbcUrl the JDBC Resource URL
	 * @param jdbcDriver the JDBC Resource Driver
	 * @param jdbcUsername the username to use for JDBC Resource authentication
	 * @param jdbcPassword the password to use for JDBC Resource authentication
	 * @return the new {@link SonarServer}
	 */
	SonarServer updateServer(int serverId, String name, String description, String host, String username,
					String password, String jdbcUrl, String jdbcDriver, String jdbcUsername, String jdbcPassword);

	/**
	 * Update a {@link SonarServer}
	 * 
	 * @param server the {@link SonarServer} to update
	 * @return the new {@link SonarServer}
	 */
	SonarServer updateServer(SonarServer server);

	/**
	 * Remove a {@link SonarServer} by its server Id
	 * 
	 * @param serverId the Id of the {@link SonarServer} to remove
	 */
	void removeServer(int serverId);

	/**
	 * Remove a {@link SonarServer}
	 * 
	 * @param server the {@link SonarServer} to remove
	 * @see #removeServer(int)
	 */
	void removeServer(SonarServer server);

}
