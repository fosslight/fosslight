<%@ page contentType="text/html; charset=utf-8" pageEncoding="utf-8"%>
<%@ include file="/WEB-INF/constants.jsp"%>
<script type="text/javascript">
	$(document).ready(function() {
		let params = new URLSearchParams(window.location.search);

		if(params.has('lang')) {
			let lang = params.get('lang');
			$("#selectLang").val(lang).prop("selected", true);
		}

		$("#selectLang").change(function () {
			let selectedOption = $('#selectLang').val();
			window.location.replace('?lang=' + selectedOption);
		});
	});
</script>
<!-- header -->
<div id="header">
	<div class="back">
		<div class="logo">
			<img src="images/img_logo.png" alt="FOSSLight System"/>
		</div>
		<spring:eval expression="@environment.getProperty('project.version')" var="projectVersion"/>
		<div class="version">
			<strong><a style="color: #AC1E35;" href="https://github.com/fosslight/fosslight/releases/tag/v${projectVersion}" target='_blank'>v${projectVersion}</a></strong>
		</div>
		<div class="userLang">
			<select id="selectLang">
				<c:forEach var="code" items="${ct:getCodeValues(ct:getConstDef('CD_DEFAULT_LOCALE'))}" varStatus="status">
					<option value="${code[3]}">${code[1]}</option>
				</c:forEach>
			</select>
		</div>
		<div class="userLogout">
			<input type="hidden" id="defaultTabAnchorArr" value="${sessUserInfo.defaultTabAnchor}" />
			<span class="configurationSpan"><a href="#/configuration/edit" class="add-tab" title="default tab">${sessUserInfo.userName}</a></span>
			<span class="userLogoutSpan"><a href="/session/logout-proc" class="userLogoutA">Logout</a></span>
			<p style="margin-top: 20px;"><marquee behavior="scroll" direction="left">${ct:getCodeExpString(ct:getConstDef('CD_MARQUEE'), ct:getConstDef('CD_DTL_CONTENTS'))}</marquee></p>
		</div>
		<div class="gnb">
			<div class="scrl">
				<ul>
					<c:if test="${dashboardFlag}">
					    <li><a href="#/dashboard/list" class="add-tab">Dashboard</a></li>
				    </c:if>
				    <c:if test="${statisticsFlag and ct:isAdmin()}">
							<li><a href="#/statistics/view" class="add-tab">Statistics</a></li>
					</c:if>
					<li><a href="#/license/list" class="add-tab">License List</a></li>
					<li><a href="#/oss/list" class="add-tab">OSS List</a></li>
					<c:if test="${projectFlag}">
						<li><a href="#/project/list" class="add-tab">Project List</a></li>
					</c:if>
					<c:if test="${partnerFlag}">
						<li><a href="#/partner/list" class="add-tab">3rd Party List</a></li>
					</c:if>
					<c:if test="${batFlag}">
						<li><a href="#/bat/list" class="add-tab">BAT List</a></li>
					</c:if>
					<c:if test="${binarydbFlag}">
						<li><a href="#/system/bat" class="add-tab">Binary DB</a></li>
					</c:if>
					<li><a href="#/vulnerability/list" class="add-tab">Vulnerability</a></li>
					<li><a href="#/selfCheck/list" class="add-tab">Self-Check List</a></li>
					<c:if test="${complianceStatusFlag and (projectFlag or partnerFlag)}">
						<li class="gnbOpen">
							<strong>Compliance Status</strong>
							<ol>
								<c:if test="${projectFlag}">
									<li><a href="#/compliance/modelStatus" class="add-tab">Model(S/W) OSC Status</a></li>
								</c:if>
								<c:if test="${partnerFlag}">
									<li><a href="#/compliance/3rdList" class="add-tab">3rd Party Status</a></li>
								</c:if>
							</ol>
						</li>
					</c:if>
					<c:if test="${externalLinkFlag and not empty ct:getCodeValues(ct:getConstDef('CD_EXTERNAL_LINK'))}">
						<li class="gnbOpen">
							<a href="#/external/external" class="add-tab">External Link</a>
							<ol>
								<c:forEach var="code" items="${ct:getCodeValues(ct:getConstDef('CD_EXTERNAL_LINK'))}" varStatus="status">
									<li><a href="${fn:split(ct:getCodeExpString(ct:getConstDef('CD_EXTERNAL_LINK'),code[0]),'|')[1]}" target="_blank">${code[1]}</a></li>
								</c:forEach>
							</ol>
						</li>
					</c:if>
					<c:if test="${ct:isAdmin()}">
						<li><a href="#/system/configuration" class="add-tab">Configuration</a></li>
						<li class="gnbOpen">
							<strong>System</strong>
							<ol>
								<li><a href="#/system/code" class="add-tab">Code management</a></li>
								<li><a href="#/system/user" class="add-tab">User management</a></li>
								<li><a href="#/system/history" class="add-tab">History List</a></li>
								<li><a href="#/system/notice" class="add-tab">Notification</a></li>
								<li><a href="#/system/sentMail" class="add-tab">Sent Mail List</a></li>
								<c:if test="${binarydbFlag}">
								<li><a href="#/system/processGuide" class="add-tab">Help & Guide</a></li>
								</c:if>
								<li><a href="#/system/vulnerabilityHistory" class="add-tab">Vulnerability Log</a></li>
								<c:if test="${binarydbFlag}">
								<li><a href="#/system/binaryDataHistory" class="add-tab">BinaryDB Log</a></li>
								</c:if>
							</ol>
						</li>
					</c:if>
				</ul>	
			</div>
		</div>
		<div class="footer"><p>Copyright © 2021 LG Electronics. <br/>All Rights Reserved. <span>PRIVACY LEGAL</span></p></div>
	</div>
</div>
<!-- //header -->

