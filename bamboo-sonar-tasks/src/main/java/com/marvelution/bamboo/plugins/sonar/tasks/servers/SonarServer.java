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

import net.java.ao.Entity;
import net.java.ao.Implementation;
import net.java.ao.Preload;
import net.java.ao.schema.Ignore;
import net.java.ao.schema.NotNull;

/**
 * Sonar Server {@link Entity}
 * 
 * @author <a href="mailto:markrekveld@marvelution.com">Mark Rekveld</a>
 *
 * @since 1.2.0
 */
@Preload
@Implementation(SonarServerEntity.class)
public interface SonarServer extends Entity {

	/**
	 * Getter for the name of the server
	 * 
	 * @return the name
	 */
	@NotNull
	String getName();

	/**
	 * Setter for the name of the server
	 * 
	 * @param name the name to set
	 */
	void setName(String name);

	/**
	 * Getter for the description of the server
	 * 
	 * @return the description
	 */
	String getDescription();

	/**
	 * Setter for the description of the server
	 * 
	 * @param description the description to set
	 */
	void setDescription(String description);

	/**
	 * Getter for the host of the server
	 * 
	 * @return the host
	 */
	@NotNull
	String getHost();

	/**
	 * Setter for the host of the server
	 * 
	 * @param host the host to set
	 */
	void setHost(String host);

	/**
	 * Getter for the username of the server
	 * 
	 * @return the username
	 */
	String getUsername();

	/**
	 * Setter for the username of the server
	 * 
	 * @param username the username to set
	 */
	void setUsername(String username);

	/**
	 * Getter for the password of the server
	 * 
	 * @return the password
	 */
	String getPassword();

	/**
	 * Setter for the password of the server
	 * 
	 * @param password the password to set
	 */
	void setPassword(String password);

	/**
	 * Getter for the {@link JDBCResource}
	 * 
	 * @return the {@link JDBCResource}
	 */
	JDBCResource getJDBCResource();

	/**
	 * Setter for the {@link JDBCResource}
	 * 
	 * @param resource the {@link JDBCResource} to set
	 */
	void setJDBCResource(JDBCResource resource);

	/**
	 * Custom delete implementation
	 * 
	 * @see SonarServerEntity#delete()
	 */
	@Ignore
	void delete();

}
