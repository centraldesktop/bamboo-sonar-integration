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

import com.atlassian.bamboo.collections.ActionParametersMap;
import com.atlassian.bamboo.plugins.maven.task.AbstractMavenConfig;
import com.atlassian.bamboo.task.AbstractTaskConfigurator;
import com.atlassian.bamboo.task.TaskDefinition;
import com.atlassian.bamboo.utils.error.ErrorCollection;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;

import org.apache.commons.lang.StringUtils;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.Map;

/**
 * {@link AbstractTaskConfigurator} implementation for the Sonar Maven builders
 * 
 * @author <a href="mailto:markrekveld@marvelution.com">Mark Rekveld</a>
 */
public abstract class AbstractSonarMavenBuildTaskConfigurator extends AbstractSonarBuildTaskConfigurator {

	/**
	 * Maven specific configuration options
	 */
	public static final String CFG_SONAR_JDBC_PROFILE = "sonarJdbcProfile";
	public static final String CFG_SONAR_JDBC_OPTION = "sonarJdbcOption";
	public static final String CFG_SONAR_JDBC_USE_PROFILE = "sonarJdbcUseProfile";
	public static final String CFG_SONAR_JDBC_USE_FORM = "sonarJdbcUseForm";

	/**
	 * Sonar plugin groupId, artifactId and goal. The version comes from the sub-implementation
	 */
	private static final String SONAR_PLUGIN_GROUPID = "org.codehaus.mojo";
	private static final String SONAR_PLUGIN_ARTIFACTID = "sonar-maven-plugin";
	private static final String SONAR_PLUGIN_GOAL = "sonar";

	private static final String CTX_SONAR_JDBC_OPTIONS = "sonarJdbcOptions";
	private static final Map<String, String> CFG_SONAR_JDBC_OPTIONS = ImmutableMap.of(
		CFG_SONAR_JDBC_USE_FORM, "Specify the configuration below.",
		CFG_SONAR_JDBC_USE_PROFILE, "Get configuration from a Maven Profile."
	);

	private static final String CFG_GOALS = AbstractMavenConfig.CFG_GOALS;
	private static final List<String> FIELDS_TO_COPY = ImmutableList.of(CFG_GOALS, CFG_SONAR_JDBC_PROFILE,
		CFG_SONAR_JDBC_OPTION);

	/**
	 * {@inheritDoc}
	 */
	@NotNull
	@Override
	public Map<String, String> generateTaskConfigMap(@NotNull final ActionParametersMap params,
					@Nullable final TaskDefinition previousTaskDefinition) {
		Map<String, String> config = super.generateTaskConfigMap(params, previousTaskDefinition);
		taskConfiguratorHelper.populateTaskConfigMapWithActionParameters(config, params, FIELDS_TO_COPY);
		config.put(CFG_GOALS, SONAR_PLUGIN_GROUPID + ":" + SONAR_PLUGIN_ARTIFACTID + ":"
			+ getSonarMavenPluginVersion() + ":" + SONAR_PLUGIN_GOAL);
		return config;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void populateContextForCreate(Map<String, Object> context) {
		super.populateContextForCreate(context);
		context.put(CFG_SONAR_JDBC_OPTION, CFG_SONAR_JDBC_USE_FORM);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void populateContextForEdit(@NotNull Map<String, Object> context, @NotNull TaskDefinition taskDefinition) {
		super.populateContextForEdit(context, taskDefinition);
		taskConfiguratorHelper.populateContextWithConfiguration(context, taskDefinition, FIELDS_TO_COPY);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void populateContextForView(@NotNull Map<String, Object> context, @NotNull TaskDefinition taskDefinition) {
		super.populateContextForView(context, taskDefinition);
		taskConfiguratorHelper.populateContextWithConfiguration(context, taskDefinition, FIELDS_TO_COPY);
		context.put("usesProfile",
			CFG_SONAR_JDBC_USE_PROFILE.equals(taskDefinition.getConfiguration().get(CFG_SONAR_JDBC_OPTION)));
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	protected void populateContextForAllOperations(Map<String, Object> context) {
		super.populateContextForAllOperations(context);
		context.put(CTX_SONAR_JDBC_OPTIONS, CFG_SONAR_JDBC_OPTIONS);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void validate(@NotNull ActionParametersMap params, @NotNull ErrorCollection errorCollection) {
		super.validate(params, errorCollection);
		if (CFG_SONAR_JDBC_USE_PROFILE.equals(params.getString(CFG_SONAR_JDBC_OPTION))) {
			if (StringUtils.isBlank(params.getString(CFG_SONAR_JDBC_PROFILE))) {
				errorCollection.addError(CFG_SONAR_JDBC_PROFILE, textProvider.getText("sonar.jdbc.profile.mandatory"));
			}
		} else {
			validateSonarServer(params, errorCollection);
		}
	}

	/**
	 * Getter for the Maven plugin org.codehaus.mojo:sonar-maven-plugin
	 * 
	 * @return the version string
	 */
	protected abstract String getSonarMavenPluginVersion();

}
