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

package com.marvelution.bamboo.plugins.sonar.tasks.upgrade;

import static com.marvelution.bamboo.plugins.sonar.tasks.configuration.SonarConfigConstants.CFG_SONAR_HOST_PASSWORD;
import static com.marvelution.bamboo.plugins.sonar.tasks.configuration.SonarConfigConstants.CFG_SONAR_JDBC_PASSWORD;
import static com.marvelution.bamboo.plugins.sonar.tasks.configuration.SonarConfigConstants.ENCRYPTOR;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;

import com.atlassian.bamboo.plan.Plan;
import com.atlassian.bamboo.plan.PlanManager;
import com.atlassian.bamboo.task.TaskConfigurationService;
import com.atlassian.bamboo.task.TaskDefinition;
import com.atlassian.sal.api.message.Message;
import com.atlassian.sal.api.upgrade.PluginUpgradeTask;
import com.atlassian.spring.container.ContainerManager;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.Iterables;
import com.google.common.collect.Maps;
import com.marvelution.bamboo.plugins.sonar.tasks.predicates.SonarPredicates;
import com.marvelution.bamboo.plugins.sonar.tasks.utils.PluginHelper;

/**
 * Upgrade task to encrypt all unencrypted passwords
 * 
 * @author <a href="mailto:markrekveld@marvelution.com">Mark Rekveld</a>
 *
 * @since 1.2.0
 */
public class EncryptUnencryptedTaskPasswordsUpgradeTask implements PluginUpgradeTask {

	private final Logger logger = Logger.getLogger(EncryptUnencryptedTaskPasswordsUpgradeTask.class);
	private final PlanManager planManager;
	private TaskConfigurationService taskConfigurationService;

	private static final List<String> PASSWORD_FIELDS = ImmutableList.of(CFG_SONAR_HOST_PASSWORD,
		CFG_SONAR_JDBC_PASSWORD);

	/**
	 * Constructor
	 *
	 * @param planManager the {@link PlanManager} implementation
	 */
	public EncryptUnencryptedTaskPasswordsUpgradeTask(PlanManager planManager) {
		this.planManager = planManager;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public Collection<Message> doUpgrade() throws Exception {
		for (Plan buildable : planManager.getAllPlansUnrestricted()) {
			if (Iterables.any(buildable.getBuildDefinition().getTaskDefinitions(), SonarPredicates.isSonarTask())) {
				logger.info("Check Sonar Tasks for " + buildable.getBuildKey());
				for (TaskDefinition taskDefinition : Iterables.filter(
					buildable.getBuildDefinition().getTaskDefinitions(), SonarPredicates.isSonarTask())) {
					TaskDefinition newTaskDefinition = getTaskConfigurationService().editTask(buildable.getPlanKey(),
						taskDefinition.getId(), taskDefinition.getUserDescription(),
						getTaskConfigurationMap(taskDefinition), taskDefinition.getRootDirectorySelector());
					logger.info("Encrypted task passwords of task [" + newTaskDefinition.getId() + "] of type ["
						+ newTaskDefinition.getPluginKey() + "]");
				}
			} else {
				logger.info(buildable.getBuildKey() + " has no Sonar Tasks to check");
			}
		}
		return null;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public int getBuildNumber() {
		return 2;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public String getPluginKey() {
		return PluginHelper.getPluginKey();
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public String getShortDescription() {
		return "Encrypt all Unencrypted passwords configured in Sonar Tasks.";
	}

	/**
	 * Get the new Task Configuration Map
	 * 
	 * @param taskDefinition the current {@link TaskDefinition}
	 * @return the new Configuration map
	 */
	private Map<String, String> getTaskConfigurationMap(TaskDefinition taskDefinition) {
		Map<String, String> config = Maps.newHashMap();
		for (Entry<String, String> entry : taskDefinition.getConfiguration().entrySet()) {
			if (PASSWORD_FIELDS.contains(entry.getKey()) && StringUtils.isNotBlank(entry.getValue())) {
				// A Password field is set with a value. Encrypt it in the new configuration map
				logger.info("Encrypting field: " + entry.getKey());
				config.put(entry.getKey(), ENCRYPTOR.encrypt(entry.getValue()));
			} else {
				// No need to encrypt this field, just copy it
				config.put(entry.getKey(), entry.getValue());
			}
		}
		return config;
	}

	/**
	 * Getter for taskConfigurationService
	 *
	 * @return the taskConfigurationService
	 */
	private TaskConfigurationService getTaskConfigurationService() {
		if (taskConfigurationService == null) {
			taskConfigurationService =
				(TaskConfigurationService) ContainerManager.getComponent("taskConfigurationService");
		}
		return taskConfigurationService;
	}

}
