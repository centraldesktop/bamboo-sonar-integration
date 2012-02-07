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

package com.marvelution.bamboo.plugins.sonar.tasks.actions.admin;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.util.Collection;

import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;

import com.atlassian.bamboo.ww2.BambooActionSupport;
import com.atlassian.bamboo.ww2.aware.permissions.GlobalAdminSecurityAware;
import com.marvelution.bamboo.plugins.sonar.tasks.configuration.SonarServerTaskConfigurationService;
import com.marvelution.bamboo.plugins.sonar.tasks.servers.JDBCResource;
import com.marvelution.bamboo.plugins.sonar.tasks.servers.SonarServer;
import com.marvelution.bamboo.plugins.sonar.tasks.servers.SonarServerManager;

/**
 * @author <a href="mailto:markrekveld@marvelution.com">Mark Rekveld</a>
 *
 * @since 1.2.0
 */
@SuppressWarnings("unchecked")
public class ConfigureSonarServers extends BambooActionSupport implements GlobalAdminSecurityAware, InvocationHandler {

	/**
	 * ADD Mode type string value
	 */
	public static final String ADD = "add";

	/**
	 * EDIT Mode type string value
	 */
	public static final String EDIT = "edit";

	private static final long serialVersionUID = 1L;

	private final Logger logger = Logger.getLogger(ConfigureSonarServers.class);

	private SonarServerManager serverManager;
	private SonarServerTaskConfigurationService configrationService;
	private String action;
	private String mode;
	private int serverId;
	private String serverName;
	private String serverDescription;
	private String serverHost;
	private String serverUsername;
	private String serverPassword;
	private String jdbcUrl;
	private String jdbcDriver;
	private String jdbcUsername;
	private String jdbcPassword;
	

	/**
	 * Setter for {@link SonarServerManager}
	 *
	 * @param serverManager the {@link SonarServerManager} to set
	 */
	public void setServerManager(SonarServerManager serverManager) {
		this.serverManager = serverManager;
	}

	/**
	 * Setter for configrationService
	 *
	 * @param configrationService the configrationService to set
	 */
	public void setConfigrationService(SonarServerTaskConfigurationService configrationService) {
		this.configrationService = configrationService;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public String doDefault() throws Exception {
		if (StringUtils.isNotBlank(getAction())) {
			addActionMessage(getText("sonar.global.messages.server.action",
				new String[] {getText("sonar.global.messages.server.action." + getAction())}));
		}
		return SUCCESS;
	}

	/**
	 * Add a {@link SonarServer}
	 * 
	 * @return the view to show to add the {@link SonarServer}
	 * @throws Exception in case of errors
	 */
	public String doAdd() throws Exception {
		setMode(ADD);
		logger.debug("Adding a new Sonar Server, return the Add form view");
		return INPUT;
	}

	/**
	 * Create a {@link SonarServer}
	 * 
	 * @return the view to show when the {@link SonarServer} is created
	 * @throws Exception in case of errors when creating the server
	 */
	public String doCreate() throws Exception {
		setMode(ADD);
		if (!isValidServer()) {
			return ERROR;
		}
		SonarServer server = serverManager.addServer(getServerName(), getServerDescription(), getServerHost(),
			getServerUsername(), getServerPassword(), getJdbcUrl(), getJdbcDriver(), getJdbcUsername(),
			getJdbcPassword());
		logger.debug("Added server with name " + server.getName() + " under ID " + server.getID());
		return SUCCESS;
	}

	/**
	 * Edit a {@link SonarServer}
	 * 
	 * @return the view to show to edit the {@link SonarServer}
	 * @throws Exception in case of errors
	 */
	public String doEdit() throws Exception {
		setMode(EDIT);
		logger.debug("Editing a Sonar Server, return the Edit form view");
		if (!serverManager.hasServer(getServerId())) {
			addActionError(getText("sonar.global.errors.no.server.id", new String[] {"update"}));
			return ERROR;
		} else {
			SonarServer server = serverManager.getServer(getServerId());
			setServerName(server.getName());
			setServerDescription(server.getDescription());
			setServerHost(server.getHost());
			setServerUsername(server.getUsername());
			setServerPassword(server.getPassword());
			if (server.getJDBCResource() != null) {
				setJdbcUrl(server.getJDBCResource().getUrl());
				setJdbcDriver(server.getJDBCResource().getDriver());
				setJdbcUsername(server.getJDBCResource().getUsername());
				setJdbcPassword(server.getJDBCResource().getPassword());
			}
		}
		return INPUT;
	}

	/**
	 * Update a {@link SonarServer}
	 * 
	 * @return the view to show when the {@link SonarServer} is updated
	 * @throws Exception in case of errors when updating the server
	 */
	public String doUpdate() throws Exception {
		setMode(EDIT);
		if (!isValidServer()) {
			return ERROR;
		} else if (!configrationService.updateSonarServerTaskConfiguration(getSonarServer())) {
			addActionError(getText("sonar.global.errors.task.validation.errors"));
			return ERROR;
		}
		SonarServer server = serverManager.updateServer(getServerId(), getServerName(), getServerDescription(),
			getServerHost(), getServerUsername(), getServerPassword(), getJdbcUrl(), getJdbcDriver(),
			getJdbcUsername(), getJdbcPassword());
		logger.debug("Updated server with ID " + server.getID());
		return SUCCESS;
	}

	/**
	 * Delete a {@link SonarServer}
	 * 
	 * @return the view to show to delete the {@link SonarServer}
	 * @throws Exception in case of errors
	 */
	public String doDelete() throws Exception {
		setMode(EDIT);
		logger.debug("Deleting a Sonar Server, return the Delete form view");
		if (!serverManager.hasServer(getServerId())) {
			addActionError(getText("sonar.global.errors.no.server.id", new String[] {"delete"}));
			return ERROR;
		} else {
			SonarServer server = serverManager.getServer(getServerId());
			setServerName(server.getName());
			setServerDescription(server.getDescription());
			setServerHost(server.getHost());
		}
		return INPUT;
	}

	/**
	 * Remove a {@link SonarServer}
	 * 
	 * @return the view to show when the {@link SonarServer} is removed
	 * @throws Exception in case of errors when removing the server
	 */
	public String doRemove() throws Exception {
		if (serverManager.hasServer(getServerId())) {
			SonarServer server = serverManager.getServer(serverId);
			configrationService.disableSonarServerTaskJobs(server);
			serverManager.removeServer(serverId);
			return SUCCESS;
		} else {
			addActionError(getText("sonar.global.errors.no.server.id", new String[] {"delete"}));
			return ERROR;
		}
	}

	/**
	 * Internal method to valid the Sonar Server settings
	 * 
	 * @return <code>true</code> if valid, <code>false</code> otherwise
	 * @throws Exception
	 */
	private boolean isValidServer() throws Exception {
		if (StringUtils.isNotBlank(getServerName())) {
			SonarServer server = serverManager.getServer(getServerName());
			if (server != null && server.getID() != getServerId()) {
				addFieldError("serverName", getText("sonar.server.name.no.duplicates"));
			}
		} else if (StringUtils.isBlank(getServerName())) {
			addFieldError("serverName", getText("sonar.server.name.mandatory"));
		}
		if (StringUtils.isBlank(getServerHost())) {
			addFieldError("serverHost", getText("sonar.host.url.mandatory"));
		} else {
			if (!getServerHost().startsWith("http://") && !getServerHost().startsWith("https://")) {
				addFieldError("serverHost", getText("sonar.host.url.invalid"));
			}
		}
		// TODO Validate JDBC Resource
		if (StringUtils.isNotBlank(getJdbcUrl()) || StringUtils.isNotBlank(getJdbcDriver())
			|| StringUtils.isNotBlank(getJdbcUsername()) || StringUtils.isNotBlank(getJdbcPassword())) {
			
		}
		return !(hasFieldErrors() || hasActionErrors());
	}

	/**
	 * Get all the {@link SonarServer} instances
	 * 
	 * @return {@link Collection} of {@link SonarServer} instances
	 */
	public Collection<SonarServer> getSonarServers() {
		return serverManager.getServers();
	}

	/**
	 * Getter for action
	 *
	 * @return the action
	 */
	public String getAction() {
		return action;
	}

	/**
	 * Setter for action
	 *
	 * @param action the action to set
	 */
	public void setAction(String action) {
		this.action = action;
	}

	/**
	 * Getter for mode
	 *
	 * @return the mode
	 */
	public String getMode() {
		return mode;
	}

	/**
	 * Setter for mode
	 *
	 * @param mode the mode to set
	 */
	public void setMode(String mode) {
		this.mode = mode;
	}

	/**
	 * Getter for serverId
	 *
	 * @return the serverId
	 */
	public int getServerId() {
		return serverId;
	}

	/**
	 * Setter for serverId
	 *
	 * @param serverId the serverId to set
	 */
	public void setServerId(int serverId) {
		this.serverId = serverId;
	}

	/**
	 * Getter for serverName
	 *
	 * @return the serverName
	 */
	public String getServerName() {
		return serverName;
	}

	/**
	 * Setter for serverName
	 *
	 * @param serverName the serverName to set
	 */
	public void setServerName(String serverName) {
		this.serverName = serverName;
	}

	/**
	 * Getter for serverDescription
	 *
	 * @return the serverDescription
	 */
	public String getServerDescription() {
		return serverDescription;
	}

	/**
	 * Setter for serverDescription
	 *
	 * @param serverDescription the serverDescription to set
	 */
	public void setServerDescription(String serverDescription) {
		this.serverDescription = serverDescription;
	}

	/**
	 * Getter for serverHost
	 *
	 * @return the serverHost
	 */
	public String getServerHost() {
		return serverHost;
	}

	/**
	 * Setter for serverHost
	 *
	 * @param serverHost the serverHost to set
	 */
	public void setServerHost(String serverHost) {
		this.serverHost = serverHost;
	}

	/**
	 * Getter for serverUsername
	 *
	 * @return the serverUsername
	 */
	public String getServerUsername() {
		return serverUsername;
	}

	/**
	 * Setter for serverUsername
	 *
	 * @param serverUsername the serverUsername to set
	 */
	public void setServerUsername(String serverUsername) {
		this.serverUsername = serverUsername;
	}

	/**
	 * Getter for serverPassword
	 *
	 * @return the serverPassword
	 */
	public String getServerPassword() {
		return serverPassword;
	}

	/**
	 * Setter for serverPassword
	 *
	 * @param serverPassword the serverPassword to set
	 */
	public void setServerPassword(String serverPassword) {
		this.serverPassword = serverPassword;
	}

	/**
	 * Getter for anonymousHost
	 * 
	 * @return <code>true</code> if {@link #serverUsername} and {@link #serverPassword} are blank
	 */
	public boolean isAnonymousHost() {
		return StringUtils.isBlank(serverUsername) && StringUtils.isBlank(serverPassword);
	}

	/**
	 * Getter for defaultJdbc
	 * 
	 * @return <code>true</code> if {@link #jdbcUrl}, {@link #jdbcDriver}, {@link #jdbcUsername} and
	 *         {@link #jdbcPassword} are blank
	 */
	public boolean isDefaultJdbc() {
		return StringUtils.isBlank(jdbcUrl) && StringUtils.isBlank(jdbcDriver) && StringUtils.isBlank(jdbcUsername)
			&& StringUtils.isBlank(jdbcPassword);
	}

	/**
	 * Getter for jdbcUrl
	 *
	 * @return the jdbcUrl
	 */
	public String getJdbcUrl() {
		return jdbcUrl;
	}

	/**
	 * Setter for jdbcUrl
	 *
	 * @param jdbcUrl the jdbcUrl to set
	 */
	public void setJdbcUrl(String jdbcUrl) {
		this.jdbcUrl = jdbcUrl;
	}

	/**
	 * Getter for jdbcDriver
	 *
	 * @return the jdbcDriver
	 */
	public String getJdbcDriver() {
		return jdbcDriver;
	}

	/**
	 * Setter for jdbcDriver
	 *
	 * @param jdbcDriver the jdbcDriver to set
	 */
	public void setJdbcDriver(String jdbcDriver) {
		this.jdbcDriver = jdbcDriver;
	}

	/**
	 * Getter for jdbcUsername
	 *
	 * @return the jdbcUsername
	 */
	public String getJdbcUsername() {
		return jdbcUsername;
	}

	/**
	 * Setter for jdbcUsername
	 *
	 * @param jdbcUsername the jdbcUsername to set
	 */
	public void setJdbcUsername(String jdbcUsername) {
		this.jdbcUsername = jdbcUsername;
	}

	/**
	 * Getter for jdbcPassword
	 *
	 * @return the jdbcPassword
	 */
	public String getJdbcPassword() {
		return jdbcPassword;
	}

	/**
	 * Setter for jdbcPassword
	 *
	 * @param jdbcPassword the jdbcPassword to set
	 */
	public void setJdbcPassword(String jdbcPassword) {
		this.jdbcPassword = jdbcPassword;
	}

	/**
	 * Internal method to get a {@link Proxy} for the {@link SonarServer}
	 * This {@link Proxy} has the {@link ConfigureSonarServers} object as {@link InvocationHandler}
	 * 
	 * @return the {@link Proxy}
	 */
	private SonarServer getSonarServer() {
		return (SonarServer) Proxy.newProxyInstance(SonarServer.class.getClassLoader(),
			new Class[] {SonarServer.class}, this);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
		logger.debug("Got a " + proxy.getClass().getName() + " Proxy Invocation for " + method.getName());
		// Server methods
		if (proxy instanceof SonarServer) {
			if (method.getName().equals("getID")) {
				return getServerId();
			} else if (method.getName().equals("getName")) {
				return getServerName();
			} else if (method.getName().equals("getDescription")) {
				return getServerDescription();
			} else if (method.getName().equals("getHost")) {
				return getServerHost();
			} else if (method.getName().equals("getUsername")) {
				return getServerUsername();
			} else if (method.getName().equals("getPassword")) {
				return getServerPassword();
			} else if (method.getName().equals("getJDBCResource")) {
				return (JDBCResource) Proxy.newProxyInstance(JDBCResource.class.getClassLoader(),
					new Class[] {JDBCResource.class}, this);
			}
		// JDBC Resource methods
		} else if (proxy instanceof JDBCResource) {
			if (method.getName().equals("getUrl")) {
				return getJdbcUrl();
			} else if (method.getName().equals("getDriver")) {
				return getJdbcDriver();
			} else if (method.getName().equals("getUsername")) {
				return getJdbcUsername();
			} else if (method.getName().equals("getPassword")) {
				return getJdbcPassword();
			}
		}
		return null;
	}

}
