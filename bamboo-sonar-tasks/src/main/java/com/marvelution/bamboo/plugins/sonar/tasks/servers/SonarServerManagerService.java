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

import static com.google.common.base.Preconditions.checkNotNull;

import java.util.Collection;

import org.apache.log4j.Logger;
import org.tuckey.web.filters.urlrewrite.utils.StringUtils;

import net.java.ao.DBParam;
import net.java.ao.Query;

import com.atlassian.activeobjects.external.ActiveObjects;
import com.atlassian.activeobjects.tx.Transactional;
import com.google.common.collect.Lists;

/**
 * Default {@link SonarServerManager} implementation
 * 
 * @author <a href="mailto:markrekveld@marvelution.com">Mark Rekveld</a>
 *
 * @since 1.2.0
 */
@Transactional
public class SonarServerManagerService implements SonarServerManager {

	private final Logger logger = Logger.getLogger(SonarServerManagerService.class);
	private final ActiveObjects objects;

	/**
	 * Constructor
	 *
	 * @param objects the {@link ActiveObjects} implementation
	 */
	public SonarServerManagerService(ActiveObjects objects) {
		this.objects = checkNotNull(objects, "active objects");
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public boolean isConfigured() {
		return hasServers();
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public boolean hasServers() {
		return objects.count(SonarServer.class) > 0;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public boolean hasServer(int serverId) {
		return getServer(serverId) != null;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public boolean hasServer(String name) {
		return getServer(name) != null;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public Collection<SonarServer> getServers() {
		return Lists.newArrayList(objects.find(SonarServer.class));
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public SonarServer getServer(int serverId) {
		return objects.get(SonarServer.class, serverId);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public SonarServer getServer(String name) {
		try {
			return objects.find(SonarServer.class, Query.select().where("NAME = ?", name).limit(1))[0];
		} catch (IndexOutOfBoundsException e) {
			return null;
		}
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public SonarServer addServer(String name, String description, String host, String username, String password) {
		return addServer(name, description, host, username, password, null, null, null, null);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public SonarServer addServer(String name, String description, String host, String username, String password,
					String jdbcUrl, String jdbcDriver, String jdbcUsername, String jdbcPassword) {
		logger.debug("Adding a new server with name " + name);
		SonarServer server = objects.create(SonarServer.class, new DBParam("NAME", name), new DBParam("HOST", host));
		server.setDescription(description);
		server.setUsername(username);
		server.setPassword(password);
		server.setJDBCResource(getJdbcResource(null, jdbcUrl, jdbcDriver, jdbcUsername, jdbcPassword));
		server.save();
		return server;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public SonarServer addServer(SonarServer server) {
		checkNotNull(server, "server argument may NOT be null");
		logger.debug("Adding (copy) a new server with name " + server.getName());
		SonarServer newServer = addServer(server.getName(), server.getDescription(), server.getHost(),
			server.getUsername(), server.getPassword());
		if (server.getJDBCResource() != null) {
			newServer.setJDBCResource(copyJdbcResource(server.getJDBCResource()));
		}
		return newServer;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public SonarServer updateServer(int serverId, String name, String description, String host, String username,
					String password, String jdbcUrl, String jdbcDriver, String jdbcUsername, String jdbcPassword) {
		SonarServer server = getServer(serverId);
		logger.debug("Updating server with ID " + serverId);
		checkNotNull(server, "server not found");
		server.setName(name);
		server.setDescription(description);
		server.setHost(host);
		server.setUsername(username);
		server.setPassword(password);
		JDBCResource resource = null;
		if (server.getJDBCResource() != null) {
			resource = server.getJDBCResource();
		}
		server.setJDBCResource(getJdbcResource(resource, jdbcUrl, jdbcDriver, jdbcUsername, jdbcPassword));
		server.save();
		return server;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public SonarServer updateServer(SonarServer server) {
		checkNotNull(server, "server argument may NOT be null");
		if (server.getJDBCResource() != null){
			server.getJDBCResource().save();
		}
		server.save();
		return server;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void removeServer(int serverId) {
		removeServer(objects.get(SonarServer.class, serverId));
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void removeServer(SonarServer server) {
		checkNotNull(server, "server argument may NOT be null");
		logger.debug("Deleting server with ID: " + server.getID());
		JDBCResource resource = server.getJDBCResource();
		server.delete();
		if (resource != null) {
			resource.delete();
		}
	}

	/**
	 * Copy a {@link JDBCResource}
	 * 
	 * @param otherResource the {@link JDBCResource} to copy
	 * @return the copy
	 */
	private JDBCResource copyJdbcResource(JDBCResource otherResource) {
		if (otherResource == null) {
			return null;
		}
		return getJdbcResource(null, otherResource.getUrl(), otherResource.getDriver(), otherResource.getUsername(),
			otherResource.getPassword());
	}

	/**
	 * Get a new or update the existing {@link JDBCResource}
	 * 
	 * @param resource the {@link JDBCResource} to update, may be <code>null</code> to get a new {@link JDBCResource}
	 * @param url the JDBC URL, may not be <code>null</code>
	 * @param driver the JDBC Driver, may not be <code>null</code>
	 * @param username the JDBC username, may not be <code>null</code>
	 * @param password the JDBC password, may not be <code>null</code>
	 * @return
	 */
	private JDBCResource getJdbcResource(JDBCResource resource, String url, String driver, String username,
					String password) {
		if (StringUtils.isBlank(url) && StringUtils.isBlank(driver) && StringUtils.isBlank(username)
			&& StringUtils.isBlank(password)) {
			if (resource != null) {
				resource.delete();
			}
			return null;
		}
		if (resource == null) {
			resource = objects.create(JDBCResource.class, new DBParam("URL", url), new DBParam("DRIVER", driver),
				new DBParam("USERNAME", username));
		} else {
			resource.setUrl(url);
			resource.setDriver(driver);
			resource.setUsername(username);
		}
		resource.setPassword(password);
		resource.save();
		return resource;
	}

}
