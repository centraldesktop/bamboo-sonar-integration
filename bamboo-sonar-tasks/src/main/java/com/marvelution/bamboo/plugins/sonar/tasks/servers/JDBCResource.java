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
 * JDBC Resource {@link Entity}
 * 
 * @author <a href="mailto:markrekveld@marvelution.com">Mark Rekveld</a>
 *
 * @since 1.2.0
 */
@Preload
@Implementation(JDBCResourceEntity.class)
public interface JDBCResource extends Entity {

	/**
	 * Getter for the {@link SonarServer}
	 * 
	 * @return the {@link SonarServer}
	 */
	@NotNull
	SonarServer getSonarServer();

	/**
	 * Setter for the {@link SonarServer}
	 * 
	 * @param server the {@link SonarServer} to set
	 */
	void setSonarServer(SonarServer server);

	/**
	 * Getter for the url of the server
	 * 
	 * @return the url
	 */
	@NotNull
	String getUrl();

	/**
	 * Setter for the url of the server
	 * 
	 * @param url the url to set
	 */
	void setUrl(String url);

	/**
	 * Getter for the driver of the server
	 * 
	 * @return the driver
	 */
	@NotNull
	String getDriver();

	/**
	 * Setter for the driver of the server
	 * 
	 * @param driver the driver to set
	 */
	void setDriver(String driver);

	/**
	 * Getter for the username of the server
	 * 
	 * @return the username
	 */
	@NotNull
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
	// @NotNull Removed this NotNull so the password can be encrypted before its added to the database
	String getPassword();

	/**
	 * Setter for the password of the server
	 * 
	 * @param password the password to set
	 */
	void setPassword(String password);

	/**
	 * Custom delete implementation
	 * 
	 * @see JDBCResourceEntity#delete()
	 */
	@Ignore
	void delete();

}
