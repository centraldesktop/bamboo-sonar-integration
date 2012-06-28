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

package com.marvelution.bamboo.plugins.sonar.tasks.web.contextproviders;

import static com.marvelution.bamboo.plugins.sonar.tasks.configuration.SonarConfigConstants.*;

import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.apache.log4j.Logger;
import org.sonar.wsclient.Sonar;
import org.sonar.wsclient.services.Server;
import org.sonar.wsclient.services.ServerQuery;

import com.atlassian.bamboo.build.Job;
import com.atlassian.bamboo.plan.Plan;
import com.atlassian.bamboo.resultsummary.ResultsSummary;
import com.atlassian.bamboo.resultsummary.ResultsSummaryCriteria;
import com.atlassian.bamboo.resultsummary.ResultsSummaryManager;
import com.atlassian.bamboo.task.TaskDefinition;
import com.atlassian.plugin.PluginParseException;
import com.atlassian.plugin.web.ContextProvider;
import com.google.common.collect.Iterables;
import com.marvelution.bamboo.plugins.sonar.tasks.predicates.SonarPredicates;
import com.marvelution.bamboo.plugins.sonar.tasks.servers.SonarClientFactory;
import com.marvelution.bamboo.plugins.sonar.tasks.utils.SonarTaskUtils;
import com.marvelution.bamboo.plugins.sonar.tasks.web.SonarConfiguration;

/**
 * {@link ContextProvider} implementation to add the {@link SonarConfiguration} objects to the context
 * 
 * @author <a href="mailto:markrekveld@marvelution.com">Mark Rekveld</a>
 */
public class SonarConfigurationContextProvider implements ContextProvider {

	private static final Logger LOGGER = Logger.getLogger(SonarConfigurationContextProvider.class);

	public static final String SONAR_CONFIGURATION_CONTEXT_KEY = "sonarConfiguration";
	public static final String SONAR_SERVER_INFO_CONTEXT_KEY = "sonarServerInfo";

	private ResultsSummaryManager summaryManager;
	private SonarClientFactory clientFactory;

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void init(Map<String, String> params) throws PluginParseException {
		// Not required by this Context Provider
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public Map<String, Object> getContextMap(Map<String, Object> context) {
		if (LOGGER.isDebugEnabled()) {
			for (Entry<String, Object> entry : context.entrySet()) {
				LOGGER.debug("Context contains entry " + entry.getKey() + " of type: "
					+ entry.getValue().getClass().getName());
			}
		}
		SonarConfiguration config = getSonarConfigurationFromJobs(SonarTaskUtils.getJobsWithSonarTasks((Plan) context.get("plan")));
		context.put(SONAR_CONFIGURATION_CONTEXT_KEY, config);
		try {
			Sonar sonar = clientFactory.create(config.getSonarHost());
			context.put(SONAR_SERVER_INFO_CONTEXT_KEY, sonar.find(new ServerQuery()));
		} catch (Exception e) {
			LOGGER.debug(e);
			context.put(SONAR_SERVER_INFO_CONTEXT_KEY, new Server().setVersion("0.0"));
		}
		return context;
	}

	/**
	 * Getter for summaryManager
	 *
	 * @return the summaryManager
	 */
	public ResultsSummaryManager getSummaryManager() {
		return summaryManager;
	}

	/**
	 * Setter for the {@link ResultsSummaryManager}
	 * 
	 * @param summaryManager the {@link ResultsSummaryManager} to set
	 */
	public void setResultsSummaryManager(ResultsSummaryManager summaryManager) {
		this.summaryManager = summaryManager;
	}

	/**
	 * Getter for clientFactory
	 *
	 * @return the clientFactory
	 */
	public SonarClientFactory getClientFactory() {
		return clientFactory;
	}

	/**
	 * Setter for clientFactory
	 *
	 * @param clientFactory the clientFactory to set
	 */
	public void setClientFactory(SonarClientFactory clientFactory) {
		this.clientFactory = clientFactory;
	}

	/**
	 * Get the {@link SonarConfiguration} object from the given {@link Job}s {@link List}
	 * 
	 * @param jobs the {@link Job} {@link List} to get the {@link SonarConfiguration} from
	 * @return the {@link SonarConfiguration}
	 */
	private SonarConfiguration getSonarConfigurationFromJobs(List<Job> jobs) {
		SonarConfiguration config = new SonarConfiguration();
		for (Job job : jobs) {
			TaskDefinition taskDefinition = Iterables.find(job.getBuildDefinition().getTaskDefinitions(),
				SonarPredicates.isSonarTask());
			if (taskDefinition != null) {
				// Copy the Sonar Host configuration form the task definition
				config.setHost(taskDefinition.getConfiguration().get(CFG_SONAR_HOST_URL));
				config.setUsername(taskDefinition.getConfiguration().get(CFG_SONAR_HOST_USERNAME));
				config.setPassword(ENCRYPTOR.decrypt(taskDefinition.getConfiguration().get(CFG_SONAR_HOST_PASSWORD)));
				// And get the Sonar project key and name form the job build results
				for (ResultsSummary buildResult : summaryManager.getResultSummaries(new ResultsSummaryCriteria(job
					.getKey(), false))) {
					LOGGER.debug("Checking result of build: " + buildResult.getBuildKey() + " #"
						+ buildResult.getBuildNumber());
					if (buildResult.getCustomBuildData().containsKey(TRD_SONAR_PROJECT_KEY)) {
						config.setProjectKey(buildResult.getCustomBuildData().get(TRD_SONAR_PROJECT_KEY));
						config.setProjectName(buildResult.getCustomBuildData().get(TRD_SONAR_PROJECT_NAME));
						break;
					}
				}
				LOGGER.debug("Found Configuration " + config.toString());
				break;
			}
		}
		return config;
	}

}
