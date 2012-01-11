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

import java.util.Collection;
import java.util.Map;

import com.atlassian.bamboo.build.Buildable;
import com.atlassian.bamboo.plan.PlanPredicates;
import com.atlassian.bamboo.ww2.BambooActionSupport;
import com.atlassian.bamboo.ww2.aware.permissions.GlobalAdminSecurityAware;
import com.google.common.base.Predicates;
import com.google.common.collect.Collections2;
import com.google.common.collect.Iterables;
import com.google.common.collect.Maps;
import com.marvelution.bamboo.plugins.sonar.tasks.predicates.SonarPredicates;
import com.marvelution.bamboo.plugins.sonar.tasks.servers.SonarServer;
import com.marvelution.bamboo.plugins.sonar.tasks.servers.SonarServerManager;

/**
 * @author <a href="mailto:markrekveld@marvelution.com">Mark Rekveld</a>
 *
 * @since 1.0.0
 */
@SuppressWarnings("unchecked")
public class ViewSonarServerMatrix extends BambooActionSupport implements GlobalAdminSecurityAware {

	private static final long serialVersionUID = 1L;

	private SonarServerManager serverManager;
	private Collection<SonarServer> servers;
	private Collection<Buildable> buildables;
	private Map<String, Map<Integer, Boolean>> matrix;

	public Map<String, Map<Integer, Boolean>> getServerMatrix() {
		if (matrix == null) {
			matrix = Maps.newHashMap();
			for (Buildable buildable : getBuildables()) {
				Map<Integer, Boolean> serverMatches = Maps.newHashMap();
				for (SonarServer server : getServers()) {
					serverMatches.put(server.getID(),
						Iterables.any(buildable.getBuildDefinition().getTaskDefinitions(),
							SonarPredicates.isSonarServerDependingTask(server)));
				}
				matrix.put(buildable.getKey(), serverMatches);
			}
		}
		return matrix;
	}

	/**
	 * Getter for servers
	 *
	 * @return the servers
	 */
	public Collection<SonarServer> getServers() {
		if (servers == null) {
			servers = serverManager.getServers();
		}
		return servers;
	}

	/**
	 * Getter for buildables
	 *
	 * @return the buildables
	 */
	public Collection<Buildable> getBuildables() {
		if (buildables == null) {
			buildables = Collections2.filter(planManager.getAllPlans(Buildable.class),
				Predicates.and(Predicates.not(PlanPredicates.isSuspendedFromBuilding()),
					SonarPredicates.hasSonarTasks()));
		}
		return buildables;
	}

	/**
	 * Setter for {@link SonarServerManager}
	 *
	 * @param serverManager the {@link SonarServerManager} to set
	 */
	public void setServerManager(SonarServerManager serverManager) {
		this.serverManager = serverManager;
	}

}
