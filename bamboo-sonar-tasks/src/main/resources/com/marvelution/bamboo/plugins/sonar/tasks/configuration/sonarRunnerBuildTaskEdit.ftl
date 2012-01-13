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

[#assign addExecutableLink][@ui.displayAddExecutableInline executableKey='snr' /][/#assign]
[@ww.select cssClass="builderSelectWidget" labelKey='executable.type' name='label'
            list=uiConfigBean.getExecutableLabels('snr')
            extraUtility=addExecutableLink /]

[#assign addJdkLink][@ui.displayAddJdkInline /][/#assign]
[@ww.select cssClass="jdkSelectWidget"
            labelKey='builder.common.jdk' name='buildJdk'
            list=uiConfigBean.jdkLabels required='true'
            extraUtility=addJdkLink /]

[@ww.textfield labelKey='builder.common.env' name='environmentVariables' cssClass="long-field" /]

[@sbt.showSonarHostEditor]
	[@ww.checkbox labelKey='sonar.runner.server.setup' name='serverConfigured' toggle='true'/]
	[@ui.bambooSection titleKey='sonar.jdbc.configuration' dependsOn='serverConfigured' showOn='false']
		[@sbt.showSonarJDBCEditor /]
	[/@ui.bambooSection]
[/@sbt.showSonarHostEditor]
[@ww.checkbox labelKey='sonar.runner.project.setup' name='projectConfigured' toggle='true'/]
[@ui.bambooSection titleKey='sonar.project.configuration' dependsOn='projectConfigured' showOn='false']
	[@ww.textfield labelKey='sonar.project.key' name='sonarProjectKey' required='true' cssClass="long-field" /]
	[@ww.textfield labelKey='sonar.project.name' name='sonarProjectName' required='true' cssClass="long-field" /]
	[@ww.textfield labelKey='sonar.project.version' name='sonarProjectVersion' required='true' cssClass="long-field" /]
	[@ww.textfield labelKey='sonar.sources' name='sonarSources' required='true' cssClass="long-field" /]
	[@ww.textfield labelKey='sonar.tests' name='sonarTests' cssClass="long-field" /]
	[@ww.textfield labelKey='sonar.binaries' name='sonarBinaries' cssClass="long-field" /]
	[@ww.textfield labelKey='sonar.libraries' name='sonarLibraries' cssClass="long-field" /]
	[#include "commonSonarBuildTaskEdit.ftl"]
[/@ui.bambooSection]
