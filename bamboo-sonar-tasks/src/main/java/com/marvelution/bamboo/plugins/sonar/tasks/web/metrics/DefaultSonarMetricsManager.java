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

package com.marvelution.bamboo.plugins.sonar.tasks.web.metrics;

import java.util.List;

import com.atlassian.bamboo.plan.Plan;
import com.atlassian.sal.api.pluginsettings.PluginSettings;
import com.atlassian.sal.api.pluginsettings.PluginSettingsFactory;
import com.atlassian.sal.api.transaction.TransactionCallback;
import com.atlassian.sal.api.transaction.TransactionTemplate;
import com.atlassian.user.User;
import com.google.common.base.Preconditions;
import com.google.common.collect.ImmutableList;

/**
 * Default {@link SonarMetricsManager} implementation
 * 
 * @author <a href="mailto:markrekveld@marvelution.com">Mark Rekveld</a>
 */
public class DefaultSonarMetricsManager implements SonarMetricsManager {

	public static final String TMM_SETTING_KEY = "time.machine.metrics";
	public static final List<String> DEFAULT_METRICS = ImmutableList.of("violations_density", "complexity",
		"coverage");

	private final PluginSettingsFactory settingsFactory;
	private final TransactionTemplate transactionTemplate;

	/**
	 * Constructor
	 * 
	 * @param settingsFactory the {@link PluginSettingsFactory} implementation
	 * @param transactionTemplate the {@link TransactionTemplate} implementation
	 */
	public DefaultSonarMetricsManager(PluginSettingsFactory settingsFactory, TransactionTemplate transactionTemplate) {
		this.settingsFactory = Preconditions.checkNotNull(settingsFactory, "settingsFactory");
		this.transactionTemplate = Preconditions.checkNotNull(transactionTemplate, "transactionTemplate");
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public List<String> getTimeMachineChartMetrics(Plan plan) {
		return getTimeMachineChartMetrics(plan, null);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public List<String> getTimeMachineChartMetrics(final Plan plan, final User user) {
		return transactionTemplate.execute(new TransactionCallback<List<String>>() {
			@SuppressWarnings("unchecked")
			@Override
			public List<String> doInTransaction() {
				List<String> metrics = (List<String>) getPluginSettings(plan).get(getSettingKey(user));
				if (metrics == null || metrics.isEmpty()) {
					return DEFAULT_METRICS;
				}
				return metrics;
			}
		});
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void setTimeMachineMetrics(Plan plan, List<String> metrics) {
		setTimeMachineMetrics(plan, null, metrics);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void setTimeMachineMetrics(final Plan plan, final User user, final List<String> metrics) {
		transactionTemplate.execute(new TransactionCallback<Object>() {
			@Override
			public Object doInTransaction() {
				getPluginSettings(plan).put(getSettingKey(user), metrics);
				return null;
			}
		});
	}

	/**
	 * Getter for the {@link PluginSettings} for the given {@link Plan}
	 * 
	 * @param plan the {@link Plan} to get the {@link PluginSettings} for
	 * @return {@link Plan} Specific {@link PluginSettings} is the Plan is not <code>null</code>, the global
	 *         {@link PluginSettings} are returned otherwise
	 */
	private PluginSettings getPluginSettings(Plan plan) {
		if (plan != null) {
			return settingsFactory.createSettingsForKey(plan.getKey());
		} else {
			return settingsFactory.createGlobalSettings();
		}
	}

	/**
	 * Getter for the Setting key for the metrics
	 * 
	 * @param user the {@link User} to get the key for
	 * @return if the {@link User} is not <code>null</code> that then {@link User} specific key if returned, otherwise
	 *         the global key is returned
	 */
	private String getSettingKey(User user) {
		if (user != null) {
			return TMM_SETTING_KEY + "." + user.getFullName();
		} else {
			return TMM_SETTING_KEY;
		}
	}

}
