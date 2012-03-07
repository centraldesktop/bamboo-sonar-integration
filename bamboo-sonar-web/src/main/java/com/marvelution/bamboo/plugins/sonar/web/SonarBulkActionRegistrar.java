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

package com.marvelution.bamboo.plugins.sonar.web;

import java.util.List;

import org.apache.log4j.Logger;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;

import com.atlassian.bamboo.ww2.actions.admin.bulk.BulkAction;
import com.atlassian.spring.container.ContainerContext;
import com.atlassian.spring.container.ContainerManager;
import com.marvelution.bamboo.plugins.sonar.web.actions.admin.bulk.UpdateSonarHostBulkAction;
import com.marvelution.bamboo.plugins.sonar.web.actions.admin.bulk.UpdateSonarMavenJDBCBulkAction;
import com.marvelution.bamboo.plugins.sonar.web.actions.admin.bulk.UpdateSonarMavenPluginBulkAction;
import com.marvelution.bamboo.plugins.sonar.web.actions.admin.bulk.UpdateSonarRunnerJDBCBulkAction;

/**
 * Utility component to register the Sonar Bulk Actions
 * 
 * @author <a href="mailto:markrekveld@marvelution.com">Mark Rekveld</a>
 *
 * @since 1.3.0
 */
public class SonarBulkActionRegistrar implements InitializingBean, ApplicationContextAware {

	private static final Logger LOGGER = Logger.getLogger(SonarBulkActionRegistrar.class);

	private ApplicationContext context;

	/**
	 * {@inheritDoc}
	 */
	@SuppressWarnings("unchecked")
	@Override
	public void afterPropertiesSet() throws Exception {
		LOGGER.info("Registering the Sonar Bulk Actions in the Bamboo Application Context");
		try {
			List<BulkAction> availableBulkActions = (List<BulkAction>) getAvailableBulkActionsBean();
			if (availableBulkActions != null) {
				LOGGER.info("Found the 'availableBulkActions' bean...");
				availableBulkActions.add(new UpdateSonarHostBulkAction());
				availableBulkActions.add(new UpdateSonarMavenJDBCBulkAction());
				availableBulkActions.add(new UpdateSonarMavenPluginBulkAction());
				availableBulkActions.add(new UpdateSonarRunnerJDBCBulkAction());
			} else {
				LOGGER.warn("Unable to find the 'availableBulkActions' bean. Sonar Bulk Actions will not be available");
			}
		} catch (Exception e) {
			LOGGER.error("Failed to register the Sonar Bulk Actions: " + e.getMessage(), e);
		}
	}

	/**
	 * Get the Available Bulk Actions bean from the application context available
	 * 
	 * @return the bulk actions bean
	 */
	protected Object getAvailableBulkActionsBean() {
		if (context != null && context.containsBean("availableBulkActions")) {
			LOGGER.info("Located the 'availableBulkActions' bean in the application context");
			return context.getBean("availableBulkActions");
		} else {
			ContainerContext containerContext = ContainerManager.getInstance().getContainerContext();
			try {
				LOGGER.info("Located the 'availableBulkActions' bean in the container context");
				return containerContext.getComponent("availableBulkActions");
			} catch (Exception e) {
				return null;
			}
		}
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void setApplicationContext(ApplicationContext context) throws BeansException {
		this.context = context;
	}

}
