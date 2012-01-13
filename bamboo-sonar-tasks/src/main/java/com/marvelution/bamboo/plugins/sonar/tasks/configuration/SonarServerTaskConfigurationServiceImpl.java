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

import static com.marvelution.bamboo.plugins.sonar.tasks.configuration.SonarConfigConstants.CTX_ADMIN_ACTION;
import static com.marvelution.bamboo.plugins.sonar.tasks.configuration.SonarConfigConstants.CFG_SONAR_ID;
import static com.marvelution.bamboo.plugins.sonar.tasks.configuration.SonarConfigConstants.CFG_SONAR_HOST_URL;
import static com.marvelution.bamboo.plugins.sonar.tasks.configuration.SonarConfigConstants.CFG_SONAR_HOST_USERNAME;
import static com.marvelution.bamboo.plugins.sonar.tasks.configuration.SonarConfigConstants.CFG_SONAR_HOST_PASSWORD;
import static com.marvelution.bamboo.plugins.sonar.tasks.configuration.SonarConfigConstants.CFG_SONAR_JDBC_URL;
import static com.marvelution.bamboo.plugins.sonar.tasks.configuration.SonarConfigConstants.CFG_SONAR_JDBC_DRIVER;
import static com.marvelution.bamboo.plugins.sonar.tasks.configuration.SonarConfigConstants.CFG_SONAR_JDBC_USERNAME;
import static com.marvelution.bamboo.plugins.sonar.tasks.configuration.SonarConfigConstants.CFG_SONAR_JDBC_PASSWORD;

import java.util.Collection;
import java.util.List;
import java.util.Map;

import org.apache.log4j.Logger;

import com.atlassian.bamboo.build.Job;
import com.atlassian.bamboo.collections.ActionParametersMap;
import com.atlassian.bamboo.event.BuildConfigurationUpdatedEvent;
import com.atlassian.bamboo.plan.Plan;
import com.atlassian.bamboo.plan.PlanKey;
import com.atlassian.bamboo.plan.PlanKeys;
import com.atlassian.bamboo.plan.PlanManager;
import com.atlassian.bamboo.task.ImmutableTaskDefinition;
import com.atlassian.bamboo.task.TaskConfigurationService;
import com.atlassian.bamboo.task.TaskConfigurator;
import com.atlassian.bamboo.task.TaskDefinition;
import com.atlassian.bamboo.task.TaskManager;
import com.atlassian.bamboo.task.TaskModuleDescriptor;
import com.atlassian.bamboo.utils.error.SimpleErrorCollection;
import com.atlassian.bamboo.webwork.util.ActionParametersMapImpl;
import com.atlassian.event.EventManager;
import com.atlassian.spring.container.ContainerManager;
import com.google.common.collect.Iterables;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.marvelution.bamboo.plugins.sonar.tasks.predicates.SonarPredicates;
import com.marvelution.bamboo.plugins.sonar.tasks.servers.SonarServer;
import com.marvelution.bamboo.plugins.sonar.tasks.utils.SonarTaskUtils;

/**
 * Default implementation of the {@link SonarServerTaskConfigurationService} interface
 * 
 * @author <a href="mailto:markrekveld@marvelution.com">Mark Rekveld</a>
 *
 * @since 1.0.0
 */
@SuppressWarnings("deprecation")
public class SonarServerTaskConfigurationServiceImpl implements SonarServerTaskConfigurationService {

	private final Logger logger = Logger.getLogger(SonarServerTaskConfigurationServiceImpl.class);
	private final PlanManager planManager;
	private final EventManager eventManager;
	private TaskManager taskManager;
	private TaskConfigurationService taskConfigurationService;
	private Map<PlanKey, List<TaskDefinition>> validTaskDefinitions;

	/**
	 * Constructor
	 *
	 * @param planManager the {@link PlanManager} implementation
	 * @param eventManager the {@link EventManager} implementation
	 */
	public SonarServerTaskConfigurationServiceImpl(PlanManager planManager, EventManager eventManager) {
		this.planManager = planManager;
		this.eventManager = eventManager;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public boolean updateSonarServerTaskConfiguration(SonarServer server) {
		validTaskDefinitions = Maps.newHashMap();
		for (Job job : getSonarServerDependingJobs(server)) {
			logger.debug("Checking Job [" + job.getKey() + "] for Sonar Server dependent tasks");
			for (TaskDefinition taskDefinition : Iterables.filter(job.getBuildDefinition().getTaskDefinitions(),
				SonarPredicates.isSonarServerDependingTask(server))) {
				logger.debug("Checking TaskDefinition " + taskDefinition.getId() + " of type ["
					+ taskDefinition.getPluginKey() + "]");
				TaskDefinition currectTaskDefinition = new ImmutableTaskDefinition(taskDefinition);
				if (isUpdatedTaskConfigurationValid(currectTaskDefinition, server)) {
					logger.debug("Updated Server is valid for this Sonar Task");
					PlanKey key = PlanKeys.getPlanKey(job.getKey());
					if (validTaskDefinitions.containsKey(key)) {
						validTaskDefinitions.get(key).add(currectTaskDefinition);
					} else {
						validTaskDefinitions.put(key, Lists.newArrayList(currectTaskDefinition));
					}
				} else {
					logger.error("Failed to validate the updated Sonar Server [" + server.getID() + "] for task "
						+ taskDefinition.getId() + ". Cancelling the update of the task definitions.");
					return false;
				}
			}
		}
		// Still here? Then all the task definitions checked are valid and the update can start.
		logger.debug("All tasks are valid with the new Server configuration. Start the update of all the tasks.");
		for (Map.Entry<PlanKey, List<TaskDefinition>> entry : validTaskDefinitions.entrySet()) {
			for (TaskDefinition taskDefinition : entry.getValue()) {
				TaskDefinition newTaskDefinition = getTaskConfigurationService().editTask(entry.getKey(),
					taskDefinition.getId(), taskDefinition.getUserDescription(),
					getTaskConfigurationMap(taskDefinition, server), taskDefinition.getRootDirectorySelector());
				logger.info("Updated task [" + newTaskDefinition.getId() + "] of type ["
					+ newTaskDefinition.getPluginKey() + "]");
			}
			publishBuildConfigurationUpdatedEventForPlan(entry.getKey());
		}
		validTaskDefinitions = null;
		return true;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void disableSonarServerTaskJobs(SonarServer server) {
		for (Job job : getSonarServerDependingJobs(server)) {
			logger.info("Suspending all build activity for job " + job.getKey());
			job.setSuspendedFromBuilding(true);
			planManager.setPlanSuspendedState(job, true);
			publishBuildConfigurationUpdatedEventForPlan(PlanKeys.getPlanKey(job.getKey()));
		}
	}

	/**
	 * Getter for taskManager
	 *
	 * @return the taskManager
	 */
	public TaskManager getTaskManager() {
		logger.debug("Getting the TaskManager implementation");
		if (taskManager == null) {
			logger.debug("Getting the TaskManager from the Spring Container Manager");
			taskManager = (TaskManager) ContainerManager.getComponent("taskManager");
		}
		return taskManager;
	}

	/**
	 * Getter for taskConfigurationService
	 *
	 * @return the taskConfigurationService
	 */
	public TaskConfigurationService getTaskConfigurationService() {
		logger.debug("Getting the TaskConfigurationService implementation");
		if (taskConfigurationService == null) {
			logger.debug("Getting the TaskConfigurationService from the Spring Container Manager");
			taskConfigurationService =
				(TaskConfigurationService) ContainerManager.getComponent("taskConfigurationService");
		}
		return taskConfigurationService;
	}

	/**
	 * Internal method to publish the {@link BuildConfigurationUpdatedEvent} for the given {@link PlanKey}
	 * 
	 * @param planKey the {@link PlanKey} to publish the {@link BuildConfigurationUpdatedEvent} for
	 */
	private void publishBuildConfigurationUpdatedEventForPlan(PlanKey planKey) {
		logger.debug("Publishing the BuildConfigrationUpdatedEvent for plan " + planKey.getKey());
		eventManager.publishEvent(new BuildConfigurationUpdatedEvent(this, planKey));
	}

	/**
	 * Internal method get all the {@link Job} instances that have a Sonar Task that depend on the given
	 * {@link SonarServer}
	 * 
	 * @param server the {@link SonarServer} to check for
	 * @return the Collection of {@link Job} instances that depend on the given {@link SonarServer}
	 */
	private Collection<Job> getSonarServerDependingJobs(SonarServer server) {
		List<Job> jobs = Lists.newArrayList();
		for (Plan plan : planManager.getAllPlans()) {
			jobs.addAll(SonarTaskUtils.getJobsWithSonarTasks(plan,
				SonarPredicates.isSonarServerDependingTask(server)));
		}
		return jobs;
	}

	/**
	 * Internal method to validate the new configuration using the task configurator
	 * 
	 * @param currectTaskDefinition the current {@link TaskDefinition}
	 * @param server the new {@link SonarServer}
	 * @return <code>true</code> if the new configuration validates, <code>false</code> otherwise
	 */
	private boolean isUpdatedTaskConfigurationValid(TaskDefinition currectTaskDefinition, SonarServer server) {
		TaskModuleDescriptor descriptor = getTaskManager().getTaskDescriptor(currectTaskDefinition.getPluginKey());
		if (descriptor.getTaskConfigurator() != null) {
			SimpleErrorCollection errorCollection = new SimpleErrorCollection();
			descriptor.getTaskConfigurator().validate(getActionParametersMap(currectTaskDefinition, server),
				errorCollection);
			return !errorCollection.hasAnyErrors();
		}
		return true;
	}

	/**
	 * Get the Task Configuration {@link Map} with the updated {@link SonarServer} configuration
	 * 
	 * @param taskDefinition the current {@link TaskDefinition}
	 * @param server the new {@link SonarServer}
	 * @return the {@link Map<String, String>} task configuration map
	 */
	private Map<String, String> getTaskConfigurationMap(TaskDefinition taskDefinition, SonarServer server) {
		TaskModuleDescriptor descriptor = getTaskManager().getTaskDescriptor(taskDefinition.getPluginKey());
		if (descriptor.getTaskConfigurator() != null) {
			TaskConfigurator configurator = descriptor.getTaskConfigurator();
			return configurator.generateTaskConfigMap(getActionParametersMap(taskDefinition, server), taskDefinition);
		} else {
			return getConfigurationMap(taskDefinition.getConfiguration(), server);
		}
	}

	/**
	 * Internal method to the configuration parameters as an {@link ActionParametersMap} implementation
	 * 
	 * @param taskDefinition the {@link TaskDefinition}
	 * @param server the {@link SonarServer}
	 * @return the {@link ActionParametersMap}
	 */
	private ActionParametersMap getActionParametersMap(TaskDefinition taskDefinition, SonarServer server) {
		return new ActionParametersMapImpl(getConfigurationMap(taskDefinition.getConfiguration(), server));
	}

	/**
	 * Get the {@link Map<String, String>} for the Task Configuration including the new Server settings
	 * 
	 * @param currentConfiguration the current configuration {@link Map}
	 * @param server the new {@link SonarServer}
	 * @return the {@link Map<String, String>}
	 */
	private Map<String, String> getConfigurationMap(Map<String, String> currentConfiguration,
					SonarServer server) {
		Map<String, String> config = Maps.newHashMap(currentConfiguration);
		config.put(CTX_ADMIN_ACTION, "true");
		config.put(CFG_SONAR_ID, String.valueOf(server.getID()));
		config.put(CFG_SONAR_HOST_URL, server.getHost());
		config.put(CFG_SONAR_HOST_USERNAME, server.getUsername());
		config.put(CFG_SONAR_HOST_PASSWORD, server.getPassword());
		if (server.getJDBCResource() != null) {
			config.put(CFG_SONAR_JDBC_URL, server.getJDBCResource().getUrl());
			config.put(CFG_SONAR_JDBC_DRIVER, server.getJDBCResource().getDriver());
			config.put(CFG_SONAR_JDBC_USERNAME, server.getJDBCResource().getUsername());
			config.put(CFG_SONAR_JDBC_PASSWORD, server.getJDBCResource().getPassword());
		}
		return config;
	}

}
