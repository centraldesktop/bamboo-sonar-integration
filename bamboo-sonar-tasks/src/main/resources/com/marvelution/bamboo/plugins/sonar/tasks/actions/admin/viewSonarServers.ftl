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

<html>
<head>
	[@ui.header pageKey="sonar.global.servers.title" title=true /]
	<meta name="decorator" content="adminpage">
</head>
<body>
	[#include "serverRunningWarning.ftl"]
	<div class="toolbar">
		<div class="aui-toolbar inline">
			<ul class="toolbar-group">
				<li class="toolbar-item">
					<a class="toolbar-trigger" href="[@ww.url action='addSonarServer' namespace='/admin/sonar' /]">[@ww.text name='sonar.global.add.server' /]</a>
				</li>
			</ul>
		</div>
	</div>
	[@ui.header pageKey="sonar.global.servers.heading" /]
	<p>[@ww.text name='sonar.global.servers.description' /]</p>
	[@ww.actionmessage /]
	[@ui.clear/]
	[@ui.bambooPanel titleKey='sonar.global.servers.list.heading']
		[#if sonarServers!?size > 0]
		<table id="sonar-servers" class="aui" width="100%">
			<thead><tr>
				<th class="labelPrefixCell">[@ww.text name='sonar.global.servers.list.heading.server' /]</th>
				<th class="valueCell">[@ww.text name='sonar.global.servers.list.heading.configuration' /]</th>
				<th class="operations">[@ww.text name='sonar.global.servers.list.heading.operations' /]</th>
			</tr></thead>
			<tbody>
				[#foreach server in sonarServers]
					<tr>
						<td class="labelPrefixCell">
							<a href="${server.host}">${server.name?html}</a><br />
							[#if server.description?has_content]
								<span class="subGrey">${server.description}</span>
							[/#if]
						</td>
						<td class="valueCell">
							<b>[@ww.text name='sonar.global.servers.list.host' /]:</b> <a href="${server.host}">${server.host}</a><br />
							[#if server.username?has_content]<b>[@ww.text name='sonar.global.servers.list.username' /]:</b> ${server.username}[#else]<b>[@ww.text name='sonar.global.servers.list.anonymous' /]</b>[/#if]<br />
							[#if server.getJDBCResource()!?has_content]
								[#assign resource = server.getJDBCResource() /]
								<b>[@ww.text name='sonar.global.servers.list.jdbc.details' /]:</b><br />
								<b>[@ww.text name='sonar.global.servers.list.jdbc.url' /]:</b> ${resource.url}<br />
								<b>[@ww.text name='sonar.global.servers.list.jdbc.driver' /]:</b> ${resource.driver}<br />
								<b>[@ww.text name='sonar.global.servers.list.jdbc.username' /]:</b> ${resource.username}<br />
							[#else]
								<b>[@ww.text name='sonar.global.servers.list.jdbc.default' /]</b><br />
							[/#if]
						</td>
						<td class="operations">
							<a href="${req.contextPath}/admin/sonar/editSonarServer.action?serverId=${server.ID}">[@ww.text name='sonar.global.edit.server' /]</a>
							| <a href="${req.contextPath}/admin/sonar/deleteSonarServer.action?serverId=${server.ID}">[@ww.text name='sonar.global.delete.server' /]</a>
						</td>
					</tr>
				[/#foreach]
			</tbody>
		</table>
		[#else]
			[@ui.messageBox type='warning' titleKey='sonar.global.servers.none']
				[@ww.text name='sonar.global.servers.none.help' /]
			[/@ui.messageBox]
		[/#if]
	[/@ui.bambooPanel]
</body>
</html>
