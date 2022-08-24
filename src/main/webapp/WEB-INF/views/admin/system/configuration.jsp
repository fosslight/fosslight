<%@ page language="java" contentType="text/html; charset=utf-8" pageEncoding="utf-8"%>
<%@ include file="/WEB-INF/constants.jsp"%>
<div id="wrapIframe">
	<!---->
	<div>
		<fieldset class="listSearch mt20">
			<form action="/configurationForm" method="post">
				<dl class="basicSearch col1">
					<dt>Settings</dt>
					<div><spring:message code="msg.configuration.guide.comment" /></div>
					<dd>
						<span class="checkSet"><input type="checkbox" id="loginFlag" name="loginFlag" class="mainCategory" <c:if test="${ct:getCodeExpString(ct:getConstDef('CD_SYSTEM_SETTING'), ct:getConstDef('CD_LDAP_USED_FLAG')) eq 'Y'}">checked</c:if> /><label for="loginFlag">Authentication using LDAP</label></span>
						<div class="detailArea">
							<dl class="col1">
								<dt>detail Area</dt>
								<c:forEach var="code" items="${ct:getCodeValues(ct:getConstDef('CD_LOGIN_SETTING'))}" varStatus="status">
									<c:choose>
										<c:when test="${fn:contains(code[1], 'Flag')}">
											<dd><label>${code[1]}</label><span class="checkSet"><input type="checkbox" id="ldap${code[0]}" <c:if test="${code[3] eq 'Y'}">checked</c:if> /></dd>
										</c:when>
										<c:otherwise>
											<dd><label>${code[1]}</label><input type="text" id="ldap${code[0]}" value="${code[3]}"/></dd>
										</c:otherwise>
									</c:choose>
								</c:forEach>
							</dl>
						</div>
					</dd>
					<dd>
						<span class="checkSet"><input type="checkbox" id="smtpFlag" name="smtpFlag" class="mainCategory" <c:if test="${ct:getCodeExpString(ct:getConstDef('CD_SYSTEM_SETTING'), ct:getConstDef('CD_SMTP_USED_FLAG')) eq 'Y'}">checked</c:if> /><label for="smtpFlag">SMTP Setting</label></span>
						<div class="detailArea">
							<dl class="col1">
								<dt>detail Area</dt>
								<c:forEach var="code" items="${ct:getCodeValues(ct:getConstDef('CD_SMTP_SETTING'))}" varStatus="status">
									<c:choose>
										<c:when test="${fn:contains(code[1], 'Flag')}">
											<dd><label>${code[1]}</label><span class="checkSet"><input type="checkbox" id="smtp${code[0]}" <c:if test="${code[3] eq 'Y'}">checked</c:if> /></dd>
										</c:when>
										<c:otherwise>
											<dd><label>${code[1]}</label><input type="text" id="smtp${code[0]}" value="${code[0] eq '401' ? '' : code[3]}"/></dd>
										</c:otherwise>
									</c:choose>
								</c:forEach>
							</dl>
						</div>
					</dd>
					<dd>
						<span class="checkSet"><input type="checkbox" id="externalServiceFlag" name="externalServiceFlag" class="mainCategory" <c:if test="${ct:getCodeExpString(ct:getConstDef('CD_SYSTEM_SETTING'), ct:getConstDef('CD_EXTERNAL_SERVICE_USED_FLAG')) eq 'Y'}">checked</c:if> /><label>External Service Setting</label></span>
						<div class="detailArea">
							<dl class="col1">
								<dt>detail Area</dt>
								<c:forEach var="code" items="${ct:getCodeValues(ct:getConstDef('CD_EXTERNAL_SERVICE_SETTING'))}" varStatus="status">
									<c:choose>
										<c:when test="${fn:contains(code[1], 'Flag')}">
											<dd><label>${code[1]}</label><span class="checkSet"><input type="checkbox" id="external${code[0]}" <c:if test="${code[3] eq 'Y'}">checked</c:if> /></dd>
										</c:when>
										<c:otherwise>
											<dd><label>${code[1]}</label><input type="text" id="external${code[0]}" value="${code[0] eq '100' ? '' : code[3]}"/></dd>
										</c:otherwise>
									</c:choose>
								</c:forEach>
							</dl>
						</div>
					</dd>
					<dd id="projectConfig">
						<span class="checkSet"><label>Notice Setting</label></span>
						<div class="detailArea">
							<dl class="col1">
								<dt>detail Area</dt>
								<c:forEach var="code" items="${ct:getCodeValues('918')}" varStatus="status">
									<c:choose>
										<c:when test="${fn:contains(code[1], 'Flag')}">
											<c:set var="title" value="${code[1].replaceAll(' ', '')}" />
<%--
											<dd><label>${code[1]}</label><span class="checkSet"><input type="checkbox" id="${title}" name="${title}" <c:if test="${code[3] eq 'Y'}">checked</c:if> <c:if test="${fn:contains(title, 'NoticeFlag')}">disabled</c:if> /></dd>
--%>
											<c:if test="${not empty ct:getCodeValues(code[2])}">
												<c:choose>
													<c:when test="${fn:contains(title, 'NoticeFlag')}">
														<dd><label>Notice Type</label>${ct:genCheckbox(code[2], '', '')}</dd>
													</c:when>
												</c:choose>
											</c:if>

										</c:when>
										<c:otherwise>
											<dd><label>${code[1]}</label><input type="text" id="${code[0]}" value="${code[3]}" /></dd>
										</c:otherwise>
									</c:choose>
								</c:forEach>
                                <dd><label>License Link</label><input type="checkbox" id="licenceLink" name="licenseLink"></dd>
							</dl>
						</div>
					</dd>
					<dd>
						<span class="checkSet"><input type="checkbox" id="externalAnalysisFlag" name="externalAnalysisFlag" class="mainCategory" <c:if test="${ct:getCodeExpString(ct:getConstDef('CD_SYSTEM_SETTING'), ct:getConstDef('CD_EXTERNAL_ANALYSIS_USED_FLAG')) eq 'Y'}">checked</c:if> /><label for="externalAnalysisFlag">External Analysis Setting</label></span>
						<div class="detailArea">
							<dl class="col1">
								<dt>detail Area</dt>
								<c:forEach var="code" items="${ct:getCodeValues(ct:getConstDef('CD_EXTERNAL_ANALYSIS_SETTING'))}" varStatus="status">
									<c:choose>
										<c:when test="${fn:contains(code[1], 'Flag')}">
											<dd><label>${code[1]}</label><span class="checkSet"><input type="checkbox" id="externalAnalysis${code[0]}" <c:if test="${code[3] eq 'Y'}">checked</c:if> /></dd>
										</c:when>
										<c:otherwise>
											<dd><label>${code[1]}</label><input type="${code[1] eq "Admin Token" ? "password" : "text"}" id="externalAnalysis${code[0]}" value="${code[0] eq '401' ? '' : code[3]}"/></dd>
										</c:otherwise>
									</c:choose>
								</c:forEach>
							</dl>
						</div>
					</dd>
				</dl>
			</form>
		</fieldset>
		<div class="btnLayout w1025">
			<input id="save" type="button" value="Save" class="btnColor red right">
		</div>
	</div>
	<!---->
</div>
