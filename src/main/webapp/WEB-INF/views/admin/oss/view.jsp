<%@ page contentType="text/html; charset=utf-8" pageEncoding="utf-8"%>
<%@ include file="/WEB-INF/constants.jsp"%>
<!-- wrap -->
<div id="wrapIframe">
	<div>
		<div class="tbws1 w1025">
			<!-- Main Table [S] -->
			<table class="dCase">
				<colgroup>
					<col width="188"/>
					<col/>
				</colgroup>
				<tbody>
					<tr>
						<th class="dCase txStr"><spring:message code="msg.common.field.OSS.name" /></th>
						<td class="dCase">
							<div class="required">
								<div id="ossName" class="viewOssTd"></div>
							</div>
						</td>
					</tr>
					<tr>
						<th class="dCase"><spring:message code="msg.common.field.nickname" /></th>
						<td class="dCase">
							<div class="multiTxtSet">
								<div class="required">
									<div id="nickNames" class="viewOssTd" ></div>
								</div>
							</div>
						</td>
					</tr>
					<tr>
						<th class="dCase"><spring:message code="msg.common.field.OSS.version" /></th>
						<td class="dCase">
							<div class="required">
								<div id="ossVersion" class="viewOssTd" ></div>
							</div>
						</td>
					</tr>
					<tr>
						<th class="dCase txStr"><spring:message code="msg.common.field.declaredLicense" /><br><input type="button" id="btnShowLicenseText" value="Show license text" class="btnCLight gray"></th>
						<td class="dCase">
							<div class="required">
								<div class="licenseMulti">
									<div class="mark"></div>
									<div class="mt5"><table id="_licenseChoice"><tr><td></td></tr></table></div>
								</div>
							</div>
							<input type="hidden" id="licenseName" />
							<div id="disp_licenseText" style="display: none;"></div>
						</td>
					</tr>
					<tr>
						<th class="dCase"><spring:message code="msg.common.field.detectedLicense" /></th>
						<td class="dCase">
							<div class="multiItemSet multiDetectedLicenseSet">
								<div class="required">
									<div name="detectedLicenses" class="viewOssTd" ></div>
								</div>
							</div>
						</td>
					</tr>
					<tr>
						<th class="dCase"><spring:message code="msg.common.field.Copyright" /></th>
						<td class="dCase">
							<div id="Copyright" class="viewOssTd viewOssTextArea"><c:out value="${copyright}"/></div>
						</td>
					</tr>
					<tr id="lt">
						<th class="dCase"><spring:message code="msg.common.field.licenseType" /></th>
						<td class="dCase"><div id="licenseType" class="viewOssTd" ></div></td>
					</tr>
					<tr id="ob">
						<th class="dCase"><spring:message code="msg.common.field.obligation" /></th>
						<td class="dCase"><div id="obligation" class="viewOssTd" ></div></td>
					</tr>
					<tr>
						<th class="dCase"><spring:message code="msg.common.field.downloadLocation" /></th>
						<td class="dCase">
							<div class="multiItemSet multiDownloadLocationSet">
								<div class="required">
									<div name="downloadLocations" class="viewOssTd" ><a href="" class="urlLink" target="_blank"></a></div>
								</div>
							</div>
						</td>
					</tr>
					<tr>
						<th class="dCase"><spring:message code="msg.common.field.homepage" /></th>
						<td class="dCase">
							<div id="homepage" class="viewOssTd"><a href="" class="urlLink" target="_blank"></a></div>
						</td>
					</tr>
					<tr>
						<th class="dCase"><spring:message code="msg.common.field.summaryDescription" /></th>
						<td class="dCase"><div id="summaryDescription" class="viewOssTd viewOssTextArea" ></div></td>
					</tr>
					<tr>
						<th class="dCase"><spring:message code="msg.common.field.attribution" /></th>
						<td class="dCase"><div id="attribution" class="viewOssTd viewOssTextArea" ></div></td>
					</tr>
					<c:if test="${!empty ossId}">
					<tr>
						<th class="dCase"><spring:message code="msg.common.field.project" /><br/><input id="listMore" type="button" value="List more" class="btnCLight gray" /></th>
						<td class="dCase">
							<table id="_projectList"><tr><td></td></tr></table>
						</td>
					</tr>
						<c:if test="${not empty vulnInfoList}">
							<tr>
								<th class="dCase"><spring:message code="msg.common.field.vulnerability" /></th>
								<td class="dCase">
									<table id="_vulnInfoList"><tr><td></td></tr></table>
								</td>
							</tr>
						</c:if>
					</c:if>
				</tbody>
			</table>
			<!-- Main Table [E] -->
		</div>
	</div>
	<!---->
</div>
<!-- //wrap -->