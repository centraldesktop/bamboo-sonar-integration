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

[#-- @ftlvariable name="uiConfigBean" type="com.atlassian.bamboo.ww2.actions.build.admin.create.UIConfigSupport" --]

[#import "/com/marvelution/bamboo/plugins/sonar/tasks/configuration/sonarBuildTask.ftl" as sbt /]

[@ww.label labelKey='executable.type' name='label' /]
[@ui.displayJdk jdkLabel=buildJdk isJdkValid=uiConfigBean.isJdkLabelValid(buildJdk) /]
[@ww.label labelKey='builder.common.env' name='environmentVariables' hideOnNull='true'/]
[@sbt.showSonarHostViewer serverConfigured /]
[#if serverConfigured ]
	[@ww.label labelKey='sonar.runner.server.setup' value='<i>Yes</i>' escape="false" /]
[/#if]
[#if !projectConfigured ]
	[@ww.label labelKey='sonar.project.key' name='sonarProjectKey' /]
	[@ww.label labelKey='sonar.project.name' name='sonarProjectName' /]
	[@ww.label labelKey='sonar.project.version' name='sonarProjectVersion' /]
	[@ww.label labelKey='sonar.sources' name='sonarSources' /]
	[@ww.label labelKey='sonar.tests' name='sonarTests' hideOnNull='true' /]
	[@ww.label labelKey='sonar.binaries' name='sonarBinaries' hideOnNull='true' /]
	[@ww.label labelKey='sonar.libraries' name='sonarLibraries' hideOnNull='true' /]
	[#include "commonSonarBuildTaskView.ftl"]
[#else]
	[@ww.label labelKey='sonar.runner.project.setup' value='<i>Yes</i>' escape="false" /]
[/#if]
