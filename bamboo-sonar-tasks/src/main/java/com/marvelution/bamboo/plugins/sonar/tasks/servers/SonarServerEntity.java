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

package com.marvelution.bamboo.plugins.sonar.tasks.servers;

import net.java.ao.EntityManager;

import org.apache.log4j.Logger;

import com.marvelution.security.crypto.SimpleStringEncryptor;
import com.marvelution.security.crypto.StringEncryptor;

/**
 * Entity implementation for the {@link SonarServer} entity
 * 
 * @author <a href="mailto:markrekveld@marvelution.com">Mark Rekveld</a>
 *
 * @since 1.2.0
 */
public class SonarServerEntity {

	private static final StringEncryptor ENCRYPTOR = new SimpleStringEncryptor("qE5ejuTUzAxEPUguTratrafretruPE");

	private final Logger logger = Logger.getLogger(SonarServerEntity.class);
	private final SonarServer server;
	private final EntityManager entityManager;

	/**
	 * Constructor
	 *
	 * @param server the {@link SonarServer} entity
	 */
	public SonarServerEntity(SonarServer server) {
		this.server = server;
		entityManager = server.getEntityManager();
	}

	/**
	 * Decrypt the password from the database
	 * 
	 * @return the decrypted password
	 * @see SonarServer#getPassword()
	 */
	public String getPassword() {
		logger.debug("Decrypting the password for user: " + server.getUsername());
		return ENCRYPTOR.decrypt(server.getPassword());
	}

	/**
	 * Encrypt the password before storing it in the database
	 * 
	 * @param password the password to encrypt
	 * @see SonarServer#setPassword(java.lang.String)
	 */
	public void setPassword(String password) {
		logger.debug("Encrypting the password for user: " + server.getUsername());
		server.setPassword(ENCRYPTOR.encrypt(password));
	}

	/**
	 * Custom save implementation to make sure the entity is saved even if auto-commit is off.
	 * 
	 * @see net.java.ao.RawEntity#save()
	 */
	public void save() {
		server.save();
		checkAutoCommit();
	}

	/**
	 * Custom delete implementation to make sure the entity is deleted even if auto-commit is off.
	 * 
	 * @see SonarServer#delete()
	 */
	public void delete() {
		try {
			entityManager.delete(server);
			checkAutoCommit();
		} catch (Exception e) {
			logger.error("Failed to delete the server", e);
		}
	}

	/**
	 * Checker for the auto-commit status, if auto-commit is off then commit() is called on the {@link EntityManager}
	 */
	private void checkAutoCommit() {
		try {
			// I found that during testing the commit is not always executed leaving the check to be only in the cache
			// So calling the commit method manually in case auto-commit is off
			if (!entityManager.getProvider().getConnection().getAutoCommit()) {
				logger.debug("Auto-commit is off, calling commit() manually...");
				entityManager.getProvider().getConnection().commit();
			}
		} catch (Exception e) {
			logger.error("Failed to check auto-commit status and/or manually commiting the server changes", e);
		}
	}

}
