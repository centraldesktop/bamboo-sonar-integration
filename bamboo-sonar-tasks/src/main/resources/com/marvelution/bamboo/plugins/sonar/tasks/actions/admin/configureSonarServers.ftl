[#--
 ~ Licensed to Marvelution under one or more contributor license
 ~ agreements.  See the NOTICE file distributed with this work
 ~ for additional information regarding copyright ownership.
 ~ Marvelution licenses this file to you under the Apache License,
 ~ Version 2.0 (the "License"); you may not use this file except
 ~ in compliance with the License.
 ~ You may obtain a copy of the License at
 ~
 ~  http://www.apache.org/licenses/LICENSE-2.0
 ~
 ~ Unless required by applicable law or agreed to in writing,
 ~ software distributed under the License is distributed on an
 ~ "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 ~ KIND, either express or implied. See the License for the
 ~ specific language governing permissions and limitations
 ~ under the License.
 --]

[#-- @ftlvariable name="action" type="com.marvelution.bamboo.plugins.sonar.tasks.actions.admin.ConfigureSonarServers" --]
[#-- @ftlvariable name="" type="com.marvelution.bamboo.plugins.sonar.tasks.actions.admin.ConfigureSonarServers" --]

[#if mode == 'edit' ]
	[#assign targetAction = "/admin/sonar/updateSonarServer.action"]
[#else]
	[#assign targetAction = "/admin/sonar/createSonarServer.action"]
[/#if]

<html>
<head>
	[@ui.header pageKey="sonar.global.${mode}.server.title" title=true /]
	<meta name="decorator" content="adminpage">
</head>
<body>
	[#include "serverRunningWarning.ftl"]
	[@ui.header pageKey="sonar.global.${mode}.server.heading" /]
	<p>[@ww.text name='sonar.global.${mode}.server.description' /]</p>
	[@ui.clear /]
	[@ww.form action=targetAction
			submitLabelKey='sonar.global.${mode}.server.button'
			titleKey='sonar.global.${mode}.server.form.title'
			cancelUri='/admin/sonar/viewSonarServers.action'
			showActionErrors='true'
			descriptionKey='sonar.global.${mode}.server.form.description']
		[@ww.hidden name='mode' /]
		[#if mode == 'edit']
			[@ww.hidden name='serverId' /]
		[/#if]
		[@ww.textfield name='serverName' labelKey='sonar.server.name' descriptionKey='sonar.server.name.description' required='true' /]
		[@ww.textfield name='serverDescription' labelKey='sonar.server.description' descriptionKey='sonar.server.description.description' /]
		[@ww.textfield name='serverHost' labelKey='sonar.host.url' descriptionKey='sonar.host.url.description' required='true' /]
		[@ww.checkbox labelKey='sonar.host.anonymous' name='anonymousHost' toggle='true'/]
		[@ui.bambooSection titleKey='sonar.host.authentication' dependsOn='anonymousHost' showOn='false']
			[@ww.textfield name='serverUsername' labelKey='sonar.host.username' descriptionKey='sonar.host.username.description' required='true' /]
			[@ww.password name='serverPassword' labelKey='sonar.host.password' descriptionKey='sonar.host.password.description' required='true' showPassword='true' /]
		[/@ui.bambooSection]
		[@ww.checkbox labelKey='sonar.jdbc.default.jdbc' name='defaultJdbc' toggle='true'/]
		[@ui.bambooSection titleKey='sonar.jdbc.configuration' dependsOn='defaultJdbc' showOn='false']
			[@ww.textfield name='jdbcUrl' labelKey='sonar.jdbc.url' descriptionKey='sonar.jdbc.url.description' required='true' /]
			[@ww.textfield name='jdbcDriver' labelKey='sonar.jdbc.driver' descriptionKey='sonar.jdbc.driver.description' required='true' /]
			[@ww.textfield name='jdbcUsername' labelKey='sonar.jdbc.username' descriptionKey='sonar.jdbc.username.description' required='true' /]
			[@ww.password name='jdbcPassword' labelKey='sonar.jdbc.password' descriptionKey='sonar.jdbc.password.description' required='true' showPassword='true' /]
		[/@ui.bambooSection]
	[/@ww.form]
</body>
</html>
