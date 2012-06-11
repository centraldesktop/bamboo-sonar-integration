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

import java.util.Collection;
import java.util.List;

import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;

import com.atlassian.bamboo.bandana.PlanAwareBandanaContext;
import com.atlassian.bamboo.plan.Plan;
import com.atlassian.bamboo.plan.PlanManager;
import com.atlassian.bamboo.user.BambooUserManager;
import com.atlassian.bandana.BandanaManager;
import com.atlassian.sal.api.message.Message;
import com.atlassian.sal.api.pluginsettings.PluginSettings;
import com.atlassian.sal.api.upgrade.PluginUpgradeTask;
import com.atlassian.user.User;
import com.google.common.base.Preconditions;
import com.marvelution.bamboo.plugins.sonar.tasks.utils.PluginHelper;
import com.marvelution.bamboo.plugins.sonar.tasks.web.metrics.SonarMetricsManager;

/**
 * {@link PluginUpgradeTask} to migrate the Time Machine Metrics to {@link PluginSettings}
 * 
 * @author <a href="mailto:markrekveld@marvelution.com">Mark Rekveld</a>
 *
 * @since 1.4.0
 */
public class TimeMachineMetricsUpgradeTask implements PluginUpgradeTask {

	public static final String BASE_BANDANA_KEY = PluginHelper.getPluginKey() + ":";
	public static final String TIME_MACHINE_BASE_BANDANA_KEY = BASE_BANDANA_KEY + "SonarTimeMachineMetrics:";
	public static final String GLOBAL_TIME_MACHINE_BANDANA_KEY = TIME_MACHINE_BASE_BANDANA_KEY + "Global";

	private static final Logger LOGGER = Logger.getLogger(TimeMachineMetricsUpgradeTask.class);

	private final BandanaManager bandanaManager;
	private final BambooUserManager userManager;
	private final PlanManager planManager;
	private final SonarMetricsManager metricsManager;

	/**
	 * Constructor
	 * 
	 * @param bandanaManager the {@link BandanaManager} implementation
	 * @param userManager the {@link BambooUserManager} implementation
	 * @param planManager the {@link PlanManager} implementation
	 * @param metricsManager the {@link SonarMetricsManager} implementation
	 */
	public TimeMachineMetricsUpgradeTask(BandanaManager bandanaManager, BambooUserManager userManager,
			PlanManager planManager, SonarMetricsManager metricsManager) {
		this.bandanaManager = Preconditions.checkNotNull(bandanaManager, "badanaManager");
		this.metricsManager = Preconditions.checkNotNull(metricsManager, "metricsManager");
		this.userManager = Preconditions.checkNotNull(userManager, "userManager");
		this.planManager = Preconditions.checkNotNull(planManager, "planManager");
	}

	/**
	 * {@inheritDoc}
	 */
	@SuppressWarnings("unchecked")
	@Override
	public Collection<Message> doUpgrade() throws Exception {
		for (Plan plan : planManager.getAllPlansUnrestricted()) {
			PlanAwareBandanaContext context = PlanAwareBandanaContext.forPlan(plan);
			for (String key : bandanaManager.getKeys(context)) {
				if (key.startsWith(TIME_MACHINE_BASE_BANDANA_KEY)) {
					LOGGER.info("Migrating Time Machine Metrics under key " + key + " to the PluginSettigns");
					List<String> metrics = (List<String>) bandanaManager.getValue(context, key);
					User user = null;
					if (!GLOBAL_TIME_MACHINE_BANDANA_KEY.equals(key)) {
						user = userManager.getUser(StringUtils.substringAfter(key, TIME_MACHINE_BASE_BANDANA_KEY));
					}
					metricsManager.setTimeMachineMetrics(plan, user, metrics);
					bandanaManager.removeValue(context, key);
				}
			}
		}
		return null;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public int getBuildNumber() {
		return 4;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public String getShortDescription() {
		return "Upgrade the Time Machine Metrics from the Bandana storage to the PluginSettings";
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public String getPluginKey() {
		return PluginHelper.getPluginKey();
	}

}
