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

import com.marvelution.bamboo.plugins.sonar.tasks.servers.SonarServer;

/**
 * Interface for the {@link SonarServer} dependent Tasks configuration service
 * 
 * @author <a href="mailto:markrekveld@marvelution.com">Mark Rekveld</a>
 *
 * @since 1.0.0
 */
public interface SonarServerTaskConfigurationService {

	/**
	 * Update all the Tasks on the Bamboo instance that use the updated {@link SonarServer}
	 * 
	 * @param server the {@link SonarServer} that was updated
	 * @return <code>true</code> if all Tasks are updated successfully, <code>false</code> if any task fails to
	 *         validate with the new server settings
	 */
	boolean updateSonarServerTaskConfiguration(SonarServer server);

	/**
	 * Disable all the Jobs that use the delete {@link SonarServer}
	 * 
	 * @param server the {@link SonarServer} that is about to be deleted
	 */
	void disableSonarServerTaskJobs(SonarServer server);

}
