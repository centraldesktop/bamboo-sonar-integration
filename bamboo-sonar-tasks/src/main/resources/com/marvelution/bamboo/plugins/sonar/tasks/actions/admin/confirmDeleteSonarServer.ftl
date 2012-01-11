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

<html>
<head>
	[@ui.header pageKey="sonar.global.delete.server.title" title=true /]
	<meta name="decorator" content="adminpage">
</head>
<body>
	[#include "serverRunningWarning.ftl"]
	[@ui.header pageKey="sonar.global.delete.server.heading" /]
	<p>[@ww.text name='sonar.global.delete.server.description' /]</p>
	[@ui.clear /]
	[@ww.form action='/admin/sonar/removeSonarServer.action'
			submitLabelKey='sonar.global.delete.server.button'
			titleKey='sonar.global.delete.server.form.title'
			cancelUri='/admin/sonar/viewSonarServers.action'
			showActionErrors='true'
			descriptionKey='sonar.global.delete.server.form.description']
		[@ww.hidden name='serverId' /]
		[@ui.messageBox type='warning' titleKey='sonar.global.delete.server.confirmation']
			[@ww.text name='sonar.global.delete.server.confirmation.text' /]
		[/@ui.messageBox]
	[/@ww.form]
</body>
</html>
