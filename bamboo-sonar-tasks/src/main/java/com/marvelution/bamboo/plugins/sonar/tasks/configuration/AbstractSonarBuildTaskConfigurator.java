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

package com.marvelution.bamboo.plugins.sonar.tasks.configuration;

import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import com.atlassian.bamboo.build.Buildable;
import com.atlassian.bamboo.collections.ActionParametersMap;
import com.atlassian.bamboo.task.AbstractTaskConfigurator;
import com.atlassian.bamboo.task.TaskConfigConstants;
import com.atlassian.bamboo.task.TaskDefinition;
import com.atlassian.bamboo.task.BuildTaskRequirementSupport;
import com.atlassian.bamboo.utils.error.ErrorCollection;
import com.atlassian.bamboo.v2.build.agent.capability.Requirement;
import com.atlassian.bamboo.ww2.actions.build.admin.create.UIConfigSupport;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.Iterables;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import com.marvelution.bamboo.plugins.sonar.tasks.servers.SonarServer;
import com.marvelution.bamboo.plugins.sonar.tasks.servers.SonarServerManager;

/**
 * Base implementation of the {@link AbstractTaskConfigurator} for all the Sonar Tasks
 * 
 * @author <a href="mailto:markrekveld@marvelution.com">Mark Rekveld</a>
 */
public abstract class AbstractSonarBuildTaskConfigurator extends AbstractTaskConfigurator implements
		BuildTaskRequirementSupport, SonarConfigConstants, SonarTaskConfigurator {

	private static final Logger LOGGER = Logger.getLogger(AbstractSonarBuildTaskConfigurator.class);

	private static final List<String> FIELDS_TO_COPY = ImmutableList.of(CFG_SONAR_HOST_URL, CFG_SONAR_HOST_USERNAME,
		CFG_SONAR_JDBC_URL, CFG_SONAR_JDBC_USERNAME, CFG_SONAR_JDBC_DRIVER, CFG_SONAR_LANGUAGE, CFG_SONAR_ID,
		CFG_SONAR_JAVA_SOURCE, CFG_SONAR_JAVA_TARGET, CFG_SONAR_EXTRA_CUSTOM_PARAMETERS, CFG_SONAR_PROFILE);

	private static final List<String> PASSWORD_FIELDS = ImmutableList.of(CFG_SONAR_HOST_PASSWORD,
		CFG_SONAR_JDBC_PASSWORD);

	protected UIConfigSupport uiConfigBean;
	protected SonarServerManager serverManager;

	/**
	 * {@inheritDoc}
	 */
	@Override
	public Set<Requirement> calculateRequirements(TaskDefinition taskDefinition, Buildable buildable) {
		final Set<Requirement> requirements = Sets.newHashSet();
		taskConfiguratorHelper.addJdkRequirement(requirements, taskDefinition, TaskConfigConstants.CFG_JDK_LABEL);
		return requirements;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void validate(@NotNull ActionParametersMap params, @NotNull ErrorCollection errorCollection) {
		taskConfiguratorHelper.validateBuilderLabel(params, errorCollection);
		taskConfiguratorHelper.validateJdk(params, errorCollection);
		validateSonarHost(params, errorCollection);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void validateSonarHost(@NotNull ActionParametersMap params, @NotNull ErrorCollection errorCollection) {
		LOGGER.debug("Validating Sonar Host Properties");
		if (params.containsKey(CFG_SONAR_ID) && params.getInt(CFG_SONAR_ID, 0) != 0) {
			copySonarServerToConfiguration(params);
			if (!serverManager.hasServer(params.getInt(CFG_SONAR_ID, 0))) {
				errorCollection.addError("sonarId", getI18nBean().getText("sonar.server.unknown"));
			}
		} else {
			if (StringUtils.isBlank(params.getString(CFG_SONAR_HOST_URL))) {
				errorCollection.addError(CFG_SONAR_HOST_URL, getI18nBean().getText("sonar.host.url.mandatory"));
			} else if (!params.getString(CFG_SONAR_HOST_URL).startsWith("http://")
				&& !params.getString(CFG_SONAR_HOST_URL).startsWith("https://")) {
				errorCollection.addError(CFG_SONAR_HOST_URL, getI18nBean().getText("sonar.host.url.invalid"));
			}
		}
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void validateSonarServer(@NotNull ActionParametersMap params, @NotNull ErrorCollection errorCollection) {
		LOGGER.debug("Validating Sonar JDBC Properties");
		// TODO Validate the JDBC settings if non default
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void validateAdvancedProperties(ActionParametersMap params, ErrorCollection errorCollection) {
		LOGGER.debug("Validating Advanced Sonar Properties");
		// Not needed yet
	}

	/**
	 * {@inheritDoc}
	 */
	@NotNull
	@Override
	public Map<String, String> generateTaskConfigMap(@NotNull final ActionParametersMap params,
					@Nullable final TaskDefinition previousTaskDefinition) {
		Map<String, String> config = super.generateTaskConfigMap(params, previousTaskDefinition);
		taskConfiguratorHelper.populateTaskConfigMapWithActionParameters(config, params,
			Iterables.concat(TaskConfigConstants.DEFAULT_BUILDER_CONFIGURATION_KEYS, FIELDS_TO_COPY));
		// Copy the Sonar server to the ActionParametersMap in case it is set
		copySonarServerToConfiguration(params);
		// Encrypt the password before adding it to the configuration
		for (String field : PASSWORD_FIELDS) {
			config.put(field, ENCRYPTOR.encrypt(params.getString(field)));
		}
		return config;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void populateContextForCreate(@NotNull Map<String, Object> context) {
		super.populateContextForCreate(context);
		populateContextForAllOperations(context);
		context.put(CFG_SONAR_ID, "0");
		context.put(TaskConfigConstants.CFG_JDK_LABEL, uiConfigBean.getDefaultJdkLabel());
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void populateContextForEdit(@NotNull Map<String, Object> context, @NotNull TaskDefinition taskDefinition) {
		super.populateContextForEdit(context, taskDefinition);
		populateContextForAllOperations(context);
		taskConfiguratorHelper.populateContextWithConfiguration(context, taskDefinition,
			Iterables.concat(TaskConfigConstants.DEFAULT_BUILDER_CONFIGURATION_KEYS, FIELDS_TO_COPY));
		// Decrypt the password before adding it to the edit context
		for (String field : PASSWORD_FIELDS) {
			context.put(field, ENCRYPTOR.decrypt(taskDefinition.getConfiguration().get(field)));
		}
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void populateContextForView(@NotNull Map<String, Object> context, @NotNull TaskDefinition taskDefinition) {
		super.populateContextForView(context, taskDefinition);
		populateContextForAllOperations(context);
		taskConfiguratorHelper.populateContextWithConfiguration(context, taskDefinition,
			Iterables.concat(TaskConfigConstants.DEFAULT_BUILDER_CONFIGURATION_KEYS, FIELDS_TO_COPY));
		try {
			int sonarId = Integer.parseInt(taskDefinition.getConfiguration().get(CFG_SONAR_ID));
			if (serverManager.hasServer(sonarId)) {
				context.put(CTX_SONAR_SERVER, serverManager.getServer(sonarId));
			} else if (sonarId > 0) {
				context.put(CTX_DELETED_SERVER, true);
			}
		} catch (Exception e) {
			context.put(CFG_SONAR_ID, "0");
		}
		// Add a fake password context variable to display the password
		if (StringUtils.isNotBlank((String) context.get(CFG_SONAR_HOST_PASSWORD))) {
			context.put(CFG_SONAR_HOST_PASSWORD, SONAR_FAKE_PASSWORD);
		}
		// Add a fake password context variable to display the password
		if (StringUtils.isNotBlank((String) context.get(CFG_SONAR_JDBC_PASSWORD))) {
			context.put(CFG_SONAR_JDBC_PASSWORD, SONAR_FAKE_PASSWORD);
		}
	}

	/**
	 * Setter for the {@link UIConfigSupport}
	 * 
	 * @param uiConfigBean the {@link UIConfigSupport}
	 */
	public void setUiConfigBean(UIConfigSupport uiConfigBean) {
		this.uiConfigBean = uiConfigBean;
	}

	/**
	 * Setter for serverManager
	 *
	 * @param serverManager the serverManager to set
	 */
	public void setServerManager(SonarServerManager serverManager) {
		this.serverManager = serverManager;
	}

	/**
	 * Setter for the context that is applicable for both view and edit
	 * 
	 * @param context the {@link Map} context to set
	 */
	protected void populateContextForAllOperations(@NotNull Map<String, Object> context) {
		context.put(CTX_UI_CONFIG_BEAN, uiConfigBean);
		Map<String, String> servers = Maps.newHashMap();
		servers.put("0", getI18nBean().getText("sonar.server.specify"));
		for (SonarServer server : serverManager.getServers()) {
			servers.put(String.valueOf(server.getID()), server.getName());
		}
		context.put(CTX_SONAR_SERVERS, servers);
	}

	/**
	 * Helper method to copy a {@link NexusServer} to the configuration Action parameters map given
	 * 
	 * @param params the {@link ActionParametersMap} to add the server to
	 */
	protected void copySonarServerToConfiguration(ActionParametersMap params) {
		if (params.containsKey(CTX_ADMIN_ACTION)) {
			LOGGER.debug("No need to copy the server configuration, we are coming from the admin action.");
		} else if (params.containsKey(CFG_SONAR_ID) && serverManager.hasServer(params.getInt(CFG_SONAR_ID, 0))) {
			SonarServer server = serverManager.getServer(params.getInt(CFG_SONAR_ID, 0));
			params.put(CFG_SONAR_HOST_URL, server.getHost());
			params.put(CFG_SONAR_HOST_USERNAME, server.getUsername());
			params.put(CFG_SONAR_HOST_PASSWORD, server.getPassword());
			if (server.getJDBCResource() != null) {
				params.put(CFG_SONAR_JDBC_URL, server.getJDBCResource().getUrl());
				params.put(CFG_SONAR_JDBC_DRIVER, server.getJDBCResource().getDriver());
				params.put(CFG_SONAR_JDBC_USERNAME, server.getJDBCResource().getUsername());
				params.put(CFG_SONAR_JDBC_PASSWORD, server.getJDBCResource().getPassword());
			}
		} else {
			LOGGER.debug("Nothing to copy, no Global Sonar server used");
		}
	}

}
