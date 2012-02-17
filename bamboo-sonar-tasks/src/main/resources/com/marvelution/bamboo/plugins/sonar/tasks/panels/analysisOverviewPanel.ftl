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

<div class="module">
	<div class="mod-header"><h3>[@ww.text name="sonar.host.configuration" /]</h3></div>
	<div class="mod-content">
	[#assign host]<a href="${sonarConfiguration.host}">${sonarConfiguration.host}</a>[/#assign]
	[@ww.label labelKey='sonar.host.url' value='${host}' escape='false' /]
	[#if sonarConfiguration.isSecured()]
		[@ww.label labelKey='sonar.host.username' value='${sonarConfiguration.username}' /]
	[/#if]
	</div>
</div>
<div class="module">
	<div class="mod-header"><h3>[@ww.text name="sonar.project.configuration" /]</h3></div>
	<div class="mod-content">
	[#if sonarConfiguration.isAnalyzed()]
		[#assign resource]<a href="${sonarConfiguration.projectKey}">${sonarConfiguration.projectName}</a>[/#assign]
		[@ww.label labelKey='sonar.project.name' value='${resource}' escape='false' /]
	[#else]
		[@ww.text name='sonar.web.panel.project.not.analyzed' /]
	[/#if]
	</div>
</div>
