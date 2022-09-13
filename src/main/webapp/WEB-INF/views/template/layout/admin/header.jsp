<%@ page contentType="text/html; charset=utf-8" pageEncoding="utf-8"%>
<script type="text/javascript" src="${ctxPath}/js/tutorial/tutorial-header.js?${jsVersion}"></script>
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

		if(${sessUserInfo.division} === ${ct:getConstDef('CD_USER_DIVISION_EMPTY')}) {
			alertify.alert('<spring:message code="msg.configuration.notice.devision" />', function (e) {
				if (e) {
					$('.configurationSpan .add-tab').trigger("click");
				}
			});
		}
	});
</script>
<!-- header -->
<div id="header">
	<div class="back">
		<div class="logo">
			<img src="${ctxPath}/images/img_logo.png" alt="FOSSLight Hub"/>
		</div>
		<c:set var="projectVersion" value="${ct:getProperty('project.version')}"></c:set>
		<div class="version">
			<strong><a style="color: #AC1E35;" href="https://github.com/fosslight/fosslight/blob/develop/RELEASE_NOTES.md" target='_blank'>v${projectVersion}</a></strong>
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
			<span class="configurationSpan"><a href="#<c:url value="/configuration/edit"/>" class="add-tab" title="User Settings"><span><img src="${ctxPath}/images/settings.png" alt="FOSSLight Hub" width="14" height="14" /></span>&nbsp;&nbsp;${sessUserInfo.userName}</a></span>
			<span class="userLogoutSpan"><a href="<c:url value="/session/logout-proc"/>" class="userLogoutA">Logout</a></span>
			<input type="button" value=" 📢 Continue Tutorial " id="continue_tutorial" />
			<p style="margin-top: 20px;"><marquee behavior="scroll" direction="left">${ct:getCodeExpString(ct:getConstDef('CD_MARQUEE'), ct:getConstDef('CD_DTL_CONTENTS'))}</marquee></p>
		</div>
		<div class="gnb">
			<div class="scrl">
				<ul>
					<c:if test="${dashboardFlag}">
					    <li><a href="#<c:url value="/dashboard/list"/>" class="add-tab">Dashboard</a></li>
				    </c:if>
				    <c:if test="${statisticsFlag}">
							<li><a href="#<c:url value="/statistics/view"/>" class="add-tab">Statistics</a></li>
					</c:if>
					<li><a href="#<c:url value="/license/list"/>" class="add-tab">License List</a></li>
					<li id="before_proj_list"><a href="#<c:url value="/oss/list"/>" class="add-tab">OSS List</a></li>
					<c:if test="${projectFlag}">
						<li id="proj_list"><a href="#<c:url value="/project/list"/>" class="add-tab" id="proj_list">Project List</a></li>
					</c:if>
					<c:if test="${partnerFlag}">
						<li><a href="#<c:url value="/partner/list"/>" class="add-tab">3rd Party List</a></li>
					</c:if>
					<c:if test="${batFlag}">
						<li><a href="#<c:url value="/bat/list"/>" class="add-tab">BAT List</a></li>
					</c:if>
					<c:if test="${binarydbFlag}">
						<li><a href="#<c:url value="/system/bat"/>" class="add-tab">Binary DB</a></li>
					</c:if>
					<li><a href="#<c:url value="/vulnerability/list"/>" class="add-tab">Vulnerability</a></li>
					<li><a href="#<c:url value="/selfCheck/list"/>" class="add-tab">Self-Check List</a></li>
					<c:if test="${complianceStatusFlag and (projectFlag or partnerFlag)}">
						<li class="gnbOpen">
							<strong>Compliance Status</strong>
							<ol>
								<c:if test="${projectFlag}">
									<li><a href="#<c:url value="/compliance/modelStatus"/>" class="add-tab">Model(S/W) OSC Status</a></li>
								</c:if>
								<c:if test="${partnerFlag}">
									<li><a href="#<c:url value="/compliance/3rdList"/>" class="add-tab">3rd Party Status</a></li>
								</c:if>
							</ol>
						</li>
					</c:if>
					<c:if test="${externalLinkFlag and not empty ct:getCodeValues(ct:getConstDef('CD_EXTERNAL_LINK'))}">
						<li class="gnbOpen">
							<a href="#<c:url value="/external/external"/>" class="add-tab">External Link</a>
							<ol>
								<c:forEach var="code" items="${ct:getCodeValues(ct:getConstDef('CD_EXTERNAL_LINK'))}" varStatus="status">
									<li><a href="${fn:split(ct:getCodeExpString(ct:getConstDef('CD_EXTERNAL_LINK'),code[0]),'|')[1]}" target="_blank">${code[1]}</a></li>
								</c:forEach>
							</ol>
						</li>
					</c:if>
					<c:if test="${ct:isAdmin()}">
						<li><a href="#<c:url value="/system/configuration"/>" class="add-tab">Configuration</a></li>
						<li class="gnbOpen">
							<strong>System</strong>
							<ol>
								<li><a href="#<c:url value="/system/code"/>" class="add-tab">Code management</a></li>
								<li><a href="#<c:url value="/system/user"/>" class="add-tab">User management</a></li>
								<li><a href="#<c:url value="/system/history"/>" class="add-tab">History List</a></li>
								<li><a href="#<c:url value="/system/notice"/>" class="add-tab">Notification</a></li>
								<li><a href="#<c:url value="/system/sentMail"/>" class="add-tab">Sent Mail List</a></li>
								<c:if test="${binarydbFlag}">
								<li><a href="#<c:url value="/system/processGuide"/>" class="add-tab">Help & Guide</a></li>
								</c:if>
								<li><a href="#<c:url value="/system/vulnerabilityHistory"/>" class="add-tab">Vulnerability Log</a></li>
								<c:if test="${binarydbFlag}">
								<li><a href="#<c:url value="/system/binaryDataHistory"/>" class="add-tab">BinaryDB Log</a></li>
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

