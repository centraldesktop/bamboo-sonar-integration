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

<div id="sonar-server-running-warning" class="aui-message warning" style="display: block; ">
	<div id="server-running-message-title"><span><strong>[@ww.text name='sonar.bamboo.running' /]</strong></span></div>
	<div id="server-running-message"><span>[@ww.text name='sonar.update.delete.adversely.affect.running.builds' /]</span></div>
	<div id="sonar-pause-button-container">
		<div class="aui-toolbar sonar-pause-button">
			<ul class="toolbar-group">
				<li class="toolbar-item">
					<a id="sonar-pause-server-button" class="toolbar-trigger">
						<span>[@ww.text name='sonar.bamboo.pause.server' /]</span>
					</a>
				</li>
			</ul>
		</div>
	</div>
	<span class="aui-icon icon-warning"></span>
</div>
<script type="text/javascript">
AJS.$(function($) {
	var $sonarPauseButton = $("#sonar-pause-server-button").click(function(event) {
		BAMBOO.ADMIN.SERVERSTATE.serverStateUpdater.pause();
	});
	BAMBOO.ADMIN.SERVERSTATE.serverStateUpdater.onServerStatusUpdated(function(event, oldState, newState, info) {
		var $warning = $("#sonar-server-running-warning");
		if (newState == BAMBOO.ADMIN.STATUS_PAUSED || newState == BAMBOO.ADMIN.STATUS_ERROR) {
			$warning.hide();
		} else {
			$warning.show();
		}
		$sonarPauseButton.toggleClass("disabled", (newState != BAMBOO.ADMIN.STATUS_RUNNING));
	});
	BAMBOO.ADMIN.SERVERSTATE.serverStateUpdater.onServerStatusAction(function(event, action) {
		$sonarPauseButton.addClass("disabled");
	});
});
</script>
