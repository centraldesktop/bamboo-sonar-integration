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

package com.marvelution.bamboo.plugins.sonar.tasks;

import static com.marvelution.bamboo.plugins.sonar.tasks.configuration.SonarRunnerBuildTaskConfigurator.*;

import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.util.Properties;

import com.atlassian.bamboo.build.logger.BuildLogger;
import com.atlassian.bamboo.build.logger.interceptors.ErrorMemorisingInterceptor;
import com.atlassian.bamboo.build.logger.interceptors.LogMemorisingInterceptor;
import com.atlassian.bamboo.build.logger.interceptors.StringMatchingInterceptor;
import com.atlassian.bamboo.process.EnvironmentVariableAccessor;
import com.atlassian.bamboo.process.ExternalProcessBuilder;
import com.atlassian.bamboo.process.ProcessService;
import com.atlassian.bamboo.task.TaskContext;
import com.atlassian.bamboo.task.TaskException;
import com.atlassian.bamboo.task.TaskResult;
import com.atlassian.bamboo.task.TaskResultBuilder;
import com.atlassian.bamboo.task.TaskType;
import com.atlassian.bamboo.utils.SystemProperty;
import com.atlassian.bamboo.v2.build.CurrentBuildResult;
import com.atlassian.bamboo.v2.build.agent.capability.CapabilityContext;
import com.atlassian.utils.process.ExternalProcess;
import com.marvelution.bamboo.plugins.sonar.tasks.configuration.SonarConfigConstants;
import com.marvelution.bamboo.plugins.sonar.tasks.processors.SonarBuildPasswordProcessor;

import org.apache.commons.io.IOUtils;
import org.apache.log4j.Logger;
import org.jetbrains.annotations.NotNull;

/**
 * Sonar Runner {@link TaskType} implementation
 * 
 * @author <a href="mailto:markrekveld@marvelution.com">Mark Rekveld</a>
 */
public class SonarRunnerBuildTask implements TaskType {

	private static final Logger LOGGER = Logger.getLogger(SonarRunnerBuildTask.class);
	private static final String BUILD_SUCCESSFUL_MARKER = "ANALYSIS SUCCESSFUL";
	private static final boolean SEARCH_BUILD_SUCCESS_FAIL_MESSAGE_EVERYWHERE =
		SystemProperty.SEARCH_BUILD_SUCCESS_FAIL_MESSAGE_EVERYWHERE.getValue(false);
	private static final int LINES_TO_PARSE_FOR_ERRORS = 200;
	private static final int FIND_SUCCESS_MESSAGE_IN_LAST = SystemProperty.FIND_SUCCESS_MESSAGE_IN_LAST.getValue(250);

	private final CapabilityContext capabilityContext;
	private final EnvironmentVariableAccessor environmentVariableAccessor;
	private final ProcessService processService;

	/**
	 * Default Constructor
	 * 
	 * @param capabilityContext the {@link CapabilityContext} implementation
	 * @param environmentVariableAccessor the {@link EnvironmentVariableAccessor} implementation
	 * @param processService the {@link ProcessService} implementation
	 */
	public SonarRunnerBuildTask(CapabilityContext capabilityContext,
		EnvironmentVariableAccessor environmentVariableAccessor, ProcessService processService) {
		this.capabilityContext = capabilityContext;
		this.environmentVariableAccessor = environmentVariableAccessor;
		this.processService = processService;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	@NotNull
	public TaskResult execute(@NotNull TaskContext taskContext) throws TaskException {
		final BuildLogger buildLogger = taskContext.getBuildLogger();
		final CurrentBuildResult currentBuildResult = taskContext.getBuildContext().getBuildResult();

		SonarRunnerConfig config = new SonarRunnerConfig(taskContext, capabilityContext, environmentVariableAccessor);

		StringMatchingInterceptor buildSuccessMatcher =
			new StringMatchingInterceptor(BUILD_SUCCESSFUL_MARKER, SEARCH_BUILD_SUCCESS_FAIL_MESSAGE_EVERYWHERE);
		LogMemorisingInterceptor recentLogLines = new LogMemorisingInterceptor(LINES_TO_PARSE_FOR_ERRORS);
		ErrorMemorisingInterceptor errorLines = new ErrorMemorisingInterceptor();

		buildLogger.getInterceptorStack().add(buildSuccessMatcher);
		buildLogger.getInterceptorStack().add(recentLogLines);
		buildLogger.getInterceptorStack().add(errorLines);
		buildLogger.getInterceptorStack().add(new SonarBuildPasswordProcessor());
		
		try {
			ExternalProcess externalProcess = processService.executeProcess(taskContext,
					new ExternalProcessBuilder().workingDirectory(config.getWorkingDirectory())
						.env(config.getExtraEnvironment()).command(config.getCommandline()));
			if (externalProcess.getHandler().isComplete()) {
				TaskResultBuilder taskResultBuilder =
					TaskResultBuilder.create(taskContext).checkReturnCode(externalProcess)
						.checkInterceptorMatches(buildSuccessMatcher, FIND_SUCCESS_MESSAGE_IN_LAST);
				TaskResult result = taskResultBuilder.build();
				String projectKey = null, projectName = null;
				if (!taskContext.getConfigurationMap().getAsBoolean(CFG_SONAR_PROJECT_CONFIGURED)) {
					// We have a projectKey and projectName in the configuration
					projectKey = taskContext.getConfigurationMap().get(CFG_SONAR_PROJECT_KEY);
					projectName = taskContext.getConfigurationMap().get(CFG_SONAR_PROJECT_NAME);
				} else {
					// Get it from the sonar-project.properties file
					InputStream input = null;
					File sonarProjectFile = new File(config.getWorkingDirectory(), "sonar-project.properties");
					try {
						Properties projectPrperties = new Properties();
						input = new FileInputStream(sonarProjectFile);
						projectPrperties.load(input);
						projectKey = projectPrperties.getProperty(CFG_SONAR_PROJECT_KEY);
						projectName = projectPrperties.getProperty(CFG_SONAR_PROJECT_NAME);
					} catch (Exception e) {
						LOGGER.warn("Failed to get the Project Key and Name from the sonar-project.properties file.");
					} finally {
						IOUtils.closeQuietly(input);
					}
				}
				if (projectKey != null) {
					LOGGER.info("Setting the projectKey '" + projectKey + "' in the Build Results");
					currentBuildResult.getCustomBuildData().put(SonarConfigConstants.TRD_SONAR_PROJECT_KEY,
						projectKey);
				}
				if (projectName != null) {
					LOGGER.info("Setting the projectName '" + projectName + "' in the Build Results");
					currentBuildResult.getCustomBuildData().put(SonarConfigConstants.TRD_SONAR_PROJECT_NAME,
						projectName);
				}
				return result;
			}

			throw new TaskException("Failed to execute sonar command, external process not completed?");
		} catch (Exception e) {
			throw new TaskException("Failed to execute sonar task", e);
		} finally {
			currentBuildResult.addBuildErrors(errorLines.getErrorStringList());
		}
	}

}
