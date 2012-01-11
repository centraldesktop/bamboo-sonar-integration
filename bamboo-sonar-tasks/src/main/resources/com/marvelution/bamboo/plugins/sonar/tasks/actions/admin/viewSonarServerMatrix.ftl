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

[#-- @ftlvariable name="action" type="com.marvelution.bamboo.plugins.sonar.tasks.actions.admin.ViewSonarServerMatrix" --]
[#-- @ftlvariable name="" type="com.marvelution.bamboo.plugins.sonar.tasks.actions.admin.ViewSonarServerMatrix" --]

<html>
<head>
	[@ui.header pageKey="sonar.global.server.matrix.title" title=true /]
	<meta name="decorator" content="adminpage">
</head>
<body>
	[@ui.header pageKey="sonar.global.server.matrix.heading" /]
	<p>[@ww.text name='sonar.global.server.matrix.description' /]</p>
	[@ww.actionmessage /]
	[@ui.clear/]
	[@ui.bambooPanel]
		[#if servers!?size > 0]
		<table id="sonar-server-matrix" class="aui serverPlanMatrix" width="100%">
			<thead><tr>
				<th></th>
				[#list servers as server]
				<th class="serverHeading">
					${server_index + 1}
					<a href="${req.contextPath}/admin/sonar/viewSonarServers.action">${server.name}</a>
					[#if server.description?has_content]
						<br /><span class="subGrey">${server.description}</span>
					[/#if]
				</th>
				[/#list]
			</tr></thead>
			<tbody>
				[#list buildables as build]
				<tr>
					<th class="planHeading">
						${build_index + 1}
						[@ww.url id='editBuildConfigurationUrl' action='editBuildConfiguration' namespace='/build/admin/edit' buildKey='${build.key}'/]
						[#if build.type.equals("JOB")]
							[@ww.url id='chainLink'  value='/browse/${build.parent.key}'/]
							<a title="${build.key}" href="${editBuildConfigurationUrl}">${build.buildName}</a> [@ww.text name='build.partof'/] <a title="${build.parent.name}" href="${chainLink}">${build.parent.name}</a>
						[#else]
							<a title="${build.key}" href="${editBuildConfigurationUrl}">${build.name}</a>
						[/#if]
					</th>
					[#assign serversForPlan = action.getServerMatrix().get(build.key) /]
					[#list servers as server]
					<td class="checkCell">
						[#if serversForPlan.get(server.ID)]
							<img src="[@cp.getStaticResourcePrefix /]/images/icons/icon_tick.gif" alt="">
						[/#if]
					</td>
					[/#list]
				</tr>
				[/#list]
			</tbody>
		</table>
		[#else]
			[@ui.messageBox type='warning' titleKey='sonar.global.servers.none']
				[@ww.text name='sonar.global.server.matrix.none.help' /]
			[/@ui.messageBox]
		[/#if]
	[/@ui.bambooPanel]
</body>
</html>
