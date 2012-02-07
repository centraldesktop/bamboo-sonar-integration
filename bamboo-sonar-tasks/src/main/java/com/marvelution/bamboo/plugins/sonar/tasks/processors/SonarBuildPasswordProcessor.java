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

package com.marvelution.bamboo.plugins.sonar.tasks.processors;

import java.lang.reflect.Field;
import java.util.Map.Entry;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;

import com.atlassian.bamboo.build.CustomBuildProcessorServer;
import com.atlassian.bamboo.build.LogEntry;
import com.atlassian.bamboo.build.SimpleLogEntry;
import com.atlassian.bamboo.build.logger.LogInterceptor;
import com.atlassian.bamboo.v2.build.BuildContext;
import com.marvelution.bamboo.plugins.sonar.tasks.utils.PluginHelper;

/**
 * {@link CustomBuildProcessorServer} implementation to filter the Build Mata Data
 * 
 * @author <a href="mailto:markrekveld@marvelution.com">Mark Rekveld</a>
 *
 * @since 1.2.0
 */
public class SonarBuildPasswordProcessor implements CustomBuildProcessorServer, LogInterceptor {

	private static final long serialVersionUID = 1L;
	private static final Logger LOGGER = Logger.getLogger(SonarBuildPasswordProcessor.class);
	private static final Pattern PATTERN = Pattern.compile("(sonar\\.jdbc\\.password\\=(.*?))(\\s|<|$)");

	private volatile BuildContext buildContext;

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void init(BuildContext buildContext) {
		this.buildContext = buildContext;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public BuildContext call() throws InterruptedException, Exception {
		for (Entry<String, String> entry : buildContext.getBuildResult().getCustomBuildData().entrySet()) {
			if (entry.getKey().startsWith("build.commandline." + PluginHelper.getPluginKey())) {
				buildContext.getBuildResult().getCustomBuildData()
					.put(entry.getKey(), filterPasswords(entry.getValue()));
			}
		}
		return buildContext;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void intercept(LogEntry logEntry) {
		if (logEntry instanceof SimpleLogEntry) {
			String filtered = filterPasswords(logEntry.getUnstyledLog());
			if (!filtered.equals(logEntry.getUnstyledLog())) {
				// The log entry line was changed update the LogEntry...
				try {
					Field logField = getField(logEntry);
					if (logField != null) {
						logField.setAccessible(true);
						logField.set(logEntry, filtered);
					}
				} catch (Exception e) {
					LOGGER.debug("Failed to filter the Sonar password from LogEntry: " + e.getMessage(), e);
				}
			}
		}
	}

	/**
	 * Getter for the 'log' field of the {@link LogEntry} given
	 * 
	 * @param entry the {@link LogEntry} to get the 'log' field from
	 * @return the log {@link Field}, may be <code>null</code>
	 */
	private Field getField(LogEntry entry) {
		Field field;
		try {
			field = entry.getClass().getDeclaredField("log");
		} catch (Exception e) {
			LOGGER.debug("Field to get the log field from the entry itself... trying the super class...");
			try {
				field = entry.getClass().getSuperclass().getDeclaredField("log");
			} catch (Exception e1) {
				LOGGER.debug("Field to get the log field from the entry superclass");
				field = null;
			}
		}
		return field;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void interceptError(LogEntry logEntry) {
		intercept(logEntry);
	}

	/**
	 * Search and replace the Sonar password property
	 * 
	 * @param entry the {@link String} to search in
	 * @return the filtered {@link String}
	 */
	private String filterPasswords(String entry) {
		Matcher matcher = PATTERN.matcher(entry);
		if (matcher.find()) {
			return StringUtils.replace(entry, matcher.group(1), "sonar.jdbc.password=******");
		}
		return entry;
	}

}
