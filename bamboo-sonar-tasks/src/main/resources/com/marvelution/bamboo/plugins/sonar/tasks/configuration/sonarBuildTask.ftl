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

[#macro showSonarHostEditor ]
	[@ui.bambooSection titleKey='sonar.host.configuration']
		[@ww.select labelKey='sonar.server.options' name='sonarId'
					listKey='key' listValue='value' toggle='true'
					list=sonarServers /]
		[@ui.bambooSection dependsOn='sonarId' showOn='0']
			[@ww.textfield labelKey='sonar.host.url' name='sonarHostUrl' required='true' cssClass="long-field" /]
			[@ww.textfield labelKey='sonar.host.username' name='sonarHostUsername' cssClass="long-field" /]
			[@ww.password labelKey='sonar.host.password' name='sonarHostPassword' cssClass="long-field" showPassword='true' /]
			[#nested /]
		[/@ui.bambooSection]
	[/@ui.bambooSection]
[/#macro]

[#macro showSonarJDBCEditor ]
	[@ww.textfield labelKey='sonar.jdbc.url' name='sonarJdbcUrl' cssClass="long-field" /]
	[@ww.textfield labelKey='sonar.jdbc.username' name='sonarJdbcUsername' cssClass="long-field" /]
	[@ww.password labelKey='sonar.jdbc.password' name='sonarJdbcPassword' cssClass="long-field" showPassword='true' /]
	[@ww.textfield labelKey='sonar.jdbc.driver' name='sonarJdbcDriver' cssClass="long-field" /]
[/#macro]

[#macro showSonarHostViewer hideJDBC ]
	[#if sonarId != "0" && sonarServer?has_content ]
		[@ww.label labelKey='sonar.server.global' escape="false"]
			[@ww.param name='value' ]${sonarServer.name}[/@ww.param]
		[/@ww.label]
		[@ww.label labelKey='sonar.host.url' escape="false"]
			[@ww.param name='value' ]<i>${sonarServer.host}</i>[/@ww.param]
		[/@ww.label]
		[#if sonarServer.username!?has_content ]
			[@ww.label labelKey='sonar.host.username' escape="false"]
				[@ww.param name='value' ]<i>${sonarServer.username}</i>[/@ww.param]
			[/@ww.label]
			[@ww.label labelKey='sonar.host.password' name='sonarHostPassword' /]
		[/#if]
		[#if !hideJDBC && sonarServer.getJDBCResource()?has_content ]
			[#assign resource = sonarServer.getJDBCResource() /]
			[@ww.label labelKey='sonar.jdbc.url' escape="false"]
				[@ww.param name='value' ]<i>${resource.url}</i>[/@ww.param]
			[/@ww.label]
			[#if resource.username!?has_content ]
				[@ww.label labelKey='sonar.jdbc.username' escape="false"]
					[@ww.param name='value' ]<i>${resource.username}</i>[/@ww.param]
				[/@ww.label]
				[@ww.label labelKey='sonar.jdbc.password' name='sonarJdbcPassword' /]
			[/#if]
			[@ww.label labelKey='sonar.jdbc.driver' escape="false"]
				[@ww.param name='value' ]<i>${resource.driver}</i>[/@ww.param]
			[/@ww.label]
		[/#if]
	[#else]
		[#if deletedServer?has_content ]
			[@ww.label labelKey='sonar.server.global' escape="false"]
				[@ww.param name='value' ]<b>[@ww.text name="sonar.server.global.deleted" /]</b>[/@ww.param]
			[/@ww.label]
		[/#if]
		[@ww.label labelKey='sonar.host.url' name='sonarHostUrl' /]
		[#if sonarHostUsername?has_content ]
			[@ww.label labelKey='sonar.host.username' name='sonarHostUsername' /]
			[@ww.label labelKey='sonar.host.password' name='sonarHostPassword' /]
		[/#if]
		[#if !hideJDBC ]
			[@ww.label labelKey='sonar.jdbc.url' name='sonarJdbcUrl' hideOnNull='true' /]
			[@ww.label labelKey='sonar.jdbc.username' name='sonarJdbcUsername' hideOnNull='true' /]
			[@ww.label labelKey='sonar.jdbc.password' name='sonarJdbcPassword' hideOnNull='true' /]
			[@ww.label labelKey='sonar.jdbc.driver' name='sonarJdbcDriver' hideOnNull='true' /]
		[/#if]
	[/#if]
[/#macro]
