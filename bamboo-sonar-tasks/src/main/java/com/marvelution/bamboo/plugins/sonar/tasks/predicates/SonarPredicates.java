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

package com.marvelution.bamboo.plugins.sonar.tasks.predicates;

import static com.marvelution.bamboo.plugins.sonar.tasks.configuration.SonarConfigConstants.CFG_SONAR_ID;

import org.apache.commons.lang.StringUtils;
import org.jetbrains.annotations.Nullable;

import com.atlassian.bamboo.build.Buildable;
import com.atlassian.bamboo.task.TaskDefinition;
import com.atlassian.bamboo.task.TaskIdentifier;
import com.google.common.base.Preconditions;
import com.google.common.base.Predicate;
import com.google.common.base.Predicates;
import com.google.common.collect.Iterables;
import com.marvelution.bamboo.plugins.sonar.tasks.servers.SonarServer;
import com.marvelution.bamboo.plugins.sonar.tasks.utils.PluginHelper;


/**
 * Sonar related {@link Predicate} helper
 * 
 * @author <a href="mailto:markrekveld@marvelution.com">Mark Rekveld</a>
 */
public class SonarPredicates {

	/**
	 * Get the Has Sonar Tasks {@link Predicate}
	 * 
	 * @return the {@link Predicate}
	 */
	public static Predicate<Buildable> hasSonarTasks() {
		return new Predicate<Buildable>() {
			@Override
			public boolean apply(Buildable buildable) {
				return Iterables.any(buildable.getBuildDefinition().getTaskDefinitions(), isSonarTask());
			}
		};
	}

	/**
	 * Get the Is Sonar Task {@link Predicate}
	 * 
	 * @return the {@link Predicate}
	 */
	public static IsSonarTaskPredicate<TaskDefinition> isSonarTask() {
		return new IsSonarTaskPredicate<TaskDefinition>();
	}

	/**
	 * Get the Is Sonar Runner task {@link Predicate}
	 * 
	 * @return the {@link Predicate}
	 */
	public static IsSonarTaskTypePredicate<TaskDefinition> isSonarRunnerTask() {
		return new IsSonarTaskTypePredicate<TaskDefinition>("task.builder.sonar");
	}

	/**
	 * Get the is Sonar Maven 2 task {@link Predicate}
	 * 
	 * @return the {@link Predicate}
	 */
	public static IsSonarTaskTypePredicate<TaskDefinition> isSonarMaven2Task() {
		return new IsSonarTaskTypePredicate<TaskDefinition>("task.builder.sonar2");
	}

	/**
	 * Get the is Sonar Maven 3 task {@link Predicate}
	 * 
	 * @return the {@link Predicate}
	 */
	public static IsSonarTaskTypePredicate<TaskDefinition> isSonarMaven3Task() {
		return new IsSonarTaskTypePredicate<TaskDefinition>("task.builder.sonar3");
	}

	/**
	 * Get the is Sonar Maven task {@link Predicate}
	 * 
	 * @return the {@link Predicate}
	 */
	public static Predicate<TaskDefinition> isSonarMavenTask() {
		return Predicates.or(isSonarMaven2Task(), isSonarMaven3Task());
	}

	/**
	 * Get the Is Sonar Server dependency task {@link Predicate}
	 * 
	 * @param server
	 * @return {@link Predicates#and(Predicate, Predicate)} implementation combining the {@link #isSonarTask()} and
	 *         the {@link IsSonarConfigurationSettingDependentPredicate} predicates
	 */
	public static Predicate<TaskDefinition> isSonarServerDependingTask(final SonarServer server) {
		return Predicates.and(isSonarTask(),
			new IsSonarConfigurationSettingDependentPredicate(CFG_SONAR_ID, String.valueOf(server.getID())));
	}

	/**
	 * Specifci Sonar Task {@link Predicate} implementation
	 * 
	 * @author <a href="mailto:markrekveld@marvelution.com">Mark Rekveld</a>
	 *
	 * @param <TASKDEF>
	 */
	private static class IsSonarTaskTypePredicate<TASKDEF extends TaskIdentifier> implements Predicate<TASKDEF> {

		private final String taskTypeKey;

		/**
		 * Default Constructor
		 * 
		 * @param taskTypeKey the Sonar TaskType key
		 */
		protected IsSonarTaskTypePredicate(String taskTypeKey) {
			this.taskTypeKey = taskTypeKey;
		}

		/**
		 * {@inheritDoc}
		 */
		@Override
		public boolean apply(@Nullable TaskIdentifier taskIdentifier) {
			return ((TaskIdentifier) Preconditions.checkNotNull(taskIdentifier)).getPluginKey().equals(
				PluginHelper.getPluginKey() + ":" + taskTypeKey);
		}

	}

	/**
	 * Generic "Is Sonar" {@link Predicate} implementation
	 * 
	 * @author <a href="mailto:markrekveld@marvelution.com">Mark Rekveld</a>
	 *
	 * @param <TASKDEF>
	 */
	private static class IsSonarTaskPredicate<TASKDEF extends TaskIdentifier> implements Predicate<TASKDEF> {

		/**
		 * {@inheritDoc}
		 */
		@Override
		public boolean apply(@Nullable TASKDEF taskIdentifier) {
			return ((TaskIdentifier) Preconditions.checkNotNull(taskIdentifier)).getPluginKey().startsWith(
				PluginHelper.getPluginKey() + ":task.builder.sonar");
		}

	}

	/**
	 * {@link Predicate} implementation to check on specific task configuration field value
	 * 
	 * @author <a href="mailto:markrekveld@marvelution.com">Mark Rekveld</a>
	 *
	 * @since 1.2.0
	 */
	private static class IsSonarConfigurationSettingDependentPredicate implements Predicate<TaskDefinition> {

		private final String name;
		private final String value;

		/**
		 * Constructor
		 *
		 * @param name the configuration field name
		 * @param value the configuration field value
		 */
		protected IsSonarConfigurationSettingDependentPredicate(String name, String value) {
			this.name = Preconditions.checkNotNull(name);
			this.value = Preconditions.checkNotNull(value);
		}

		/**
		 * {@inheritDoc}
		 */
		@Override
		public boolean apply(@Nullable TaskDefinition taskDefinition) {
			Preconditions.checkNotNull(taskDefinition);
			return taskDefinition.getConfiguration().containsKey(name)
				&& StringUtils.equals(taskDefinition.getConfiguration().get(name), value);
		}

	}

}
