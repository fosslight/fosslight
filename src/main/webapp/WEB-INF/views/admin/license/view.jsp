<%@ page contentType="text/html; charset=utf-8" pageEncoding="utf-8"%>
<%@ include file="/WEB-INF/constants.jsp"%>
<!-- wrap -->
<div id="wrapIframe">
	<!---->
	<div>
		<!---->
		<div class="tbws1 w1025">
				<table class="dCase">
					<colgroup>
						<col width="188" />
						<col />
					</colgroup>
					<tbody>
						<tr>
							<th class="dCase txStr"><spring:message code="msg.common.field.licenseName" /></th>
							<td class="dCase">
								<div class="required">
									<div id="licenseName" class="viewLicenseTd" ><c:out value="${licenseInfo.licenseName}"/></div>
								</div>
							</td>
						</tr>
						<tr>
							<th class="dCase txStr"><spring:message code="msg.common.field.licenseType" /></th>
							<td class="dCase">
								<div class="required">
									<div id="licenseType" class="viewLicenseTd" >${ct:getCodeString(ct:getConstDef('CD_LICENSE_TYPE') ,licenseInfo.licenseType)}</div>
								</div>
							</td>
						</tr>
						<tr>
							<th class="dCase"><spring:message code="msg.common.field.restriction" /></th>
							<td class="dCase">
								<div id="restriction" class="viewLicenseTd" ></div>
							</td>
						</tr>
						<tr>
							<th class="dCase"><spring:message code="msg.common.field.obligation" /></th>
							<td class="dCase">
								<div id="obligation" class="viewLicenseTd" ></div>
							</td>
						</tr>
						<tr>
							<th class="dCase"><spring:message code="msg.common.field.SPDX" /></th>
							<td class="dCase">
								<div id="shortIdentifier" class="viewLicenseTd" ><c:out value="${licenseInfo.shortIdentifier}"/></div>
							</td>
						</tr>
						<tr>
							<th class="dCase"><spring:message code="msg.common.field.nickname" /></th>
							<td class="dCase">
								<div class="multiTxtSet">	
									<div class="required">						
										<div id="nickNames" class="viewLicenseTd" ></div>	
									</div>
								</div>
							</td>
						</tr>
						<tr>
							<th class="dCase"><spring:message code="msg.common.field.licenseWebsite" /></th>
							<td class="dCase">
								<div id="webpage" class="viewLicenseTd" ><a href="" class="urlLink" target="_blank"></a></div>
							</td>
						</tr>
						<tr>
							<th class="dCase"><spring:message code="msg.common.field.userGuide" /></th>
							<td class="dCase"><div id="description" class="viewLicenseTd viewLicenseTextArea"><c:out value="${licenseInfo.description}"/></div></td>
						</tr>
						<tr>
							<th class="dCase txStr"><spring:message code="msg.common.field.licenseText" /></th>
							<td class="dCase">
								<div class="required">
									<div id="licenseText" class="viewLicenseTd viewLicenseTextArea"></div>
								</div>
							</td>
						</tr>
						<tr>
							<th class="dCase"><spring:message code="msg.common.field.attribution" /></th>
							<td class="dCase"><div id="attribution" class="viewLicenseTd viewLicenseTextArea"></div></td>
						</tr>
					</tbody>
				</table>
		</div>

	</div>
	<!---->
</div>
<!-- //wrap --> 