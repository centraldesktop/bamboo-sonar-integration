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

[#import "/com/marvelution/bamboo/plugins/sonar/tasks/configuration/sonarBuildTask.ftl" as sbt /]

[@ww.label labelKey='builder.maven2.projectFile' name='projectFile' hideOnNull='true' /]
[@ui.displayJdk jdkLabel=buildJdk isJdkValid=uiConfigBean.isJdkLabelValid(buildJdk) /]
[@ww.label labelKey='builder.common.env' name='environmentVariables' hideOnNull='true'/]
[@ww.label labelKey='builder.common.sub' name='workingSubDirectory' hideOnNull='true' /]
[@ww.label labelKey='sonar.plugin.preinstalled' escape="false"]
    [@ww.param name='value' ]
        [@ui.displayYesOrNo sonarPluginPreInstalled /]
    [/@ww.param]
[/@ww.label]

[@sbt.showSonarHostViewer usesProfile /]
[#if usesProfile ]
	[@ww.label labelKey='sonar.jdbc.profile' name='sonarJdbcProfile' /]
[/#if]

[#include "commonSonarBuildTaskView.ftl"]
