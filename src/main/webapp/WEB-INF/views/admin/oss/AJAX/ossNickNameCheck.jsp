<%@ page contentType="text/html; charset=utf-8" pageEncoding="utf-8"%>
<%@ include file="/WEB-INF/constants.jsp"%>
<script type="text/javascript">
$(document).ready(function() {
	
});
function showLicenseText(target) {
	var obj = $("#license_"+target);
	
	if(obj.css("display") != "none"){
		obj.hide();
	} else {
		$(".classLicenseText").hide();

		obj.show();
	}
}
</script>
		<div class="tbws1">

			<table class="dCase">
				<colgroup>
					<col width="188"/>
					<col/>
				</colgroup>
				<tbody>
					<tr>
						<th class="dCase txStr"><spring:message code="msg.common.field.OSS.name" /></th>
						<td class="dCase">${ossInfo.ossName}</td>
					</tr>
					<tr>
						<th class="dCase"><spring:message code="msg.common.field.nickname" /></th>
						<td class="dCase">${ossInfo.ossNickname}</td>
					</tr>
					<tr>
						<th class="dCase"><spring:message code="msg.common.field.OSS.version" /></th>
						<td class="dCase">${ossInfo.ossVersion}</td>
					</tr>
					<tr>
						<th class="dCase txStr">License</th>
						<td class="dCase" id="td_licenseName">
							<c:choose>
								<c:when test="${ossInfo.licenseDiv eq ct:getConstDef('LICENSE_DIV_MULTI')}">
								${ossInfo.licenseName}
								</c:when>
								<c:otherwise>${ossInfo.licenseName}</c:otherwise>
							</c:choose>
							<c:forEach var="license" items="${ossInfo.ossLicenses}">
								<div id="license_${license.licenseId}" class="classLicenseText" style="display: none;">${license.ossLicenseText}</div>
							</c:forEach>
						</td>
					</tr>
					<tr>
						<th class="dCase"><spring:message code="msg.common.field.Copyright" /></th>
						<td class="dCase">${ossInfo.copyright}</td>
					</tr>
					<tr>
						<th class="dCase"><spring:message code="msg.common.field.licenseType" /></th>
						<td class="dCase">${ct:getCodeString(ct:getConstDef('CD_LICENSE_TYPE'), ossInfo.licenseType)}</td>
					</tr>
					<tr>
						<th class="dCase"><spring:message code="msg.common.field.obligation" /></th>
						<td class="dCase">
							<c:choose>
								<c:when test="${ossInfo.obligationType eq ct:getConstDef('CD_DTL_OBLIGATION_NOTICE')}"><span class="iconSet ops" title="Notice"></span></c:when>
								<c:when test="${ossInfo.obligationType eq ct:getConstDef('CD_DTL_OBLIGATION_DISCLOSURE')}"><span class="iconSet ops" title="Notice"></span><span class="iconSet man" title="Source Code"></span></c:when>
								<c:otherwise><span></span></c:otherwise>
							</c:choose>
						</td>
					</tr>
					<tr>
						<th class="dCase"><spring:message code="msg.common.field.downloadLocation" /></th>
						<td class="dCase">
							<c:choose>
								<c:when test="${not empty ossInfo.downloadLocation}"><a href="${ossInfo.downloadLocation}" class="urlLink" target="_blank">${ossInfo.downloadLocation}</a></c:when>
								<c:otherwise></c:otherwise>
							</c:choose>
						</td>
					</tr>
					<tr>
						<th class="dCase"><spring:message code="msg.common.field.homepage" /></th>
						<td class="dCase">
							<c:choose>
								<c:when test="${not empty ossInfo.homepage}"><a href="${ossInfo.homepage}" class="urlLink" target="_blank">${ossInfo.homepage}</a></c:when>
								<c:otherwise></c:otherwise>
							</c:choose>
						</td>
					</tr>
					<tr>
						<th class="dCase"><spring:message code="msg.common.field.summaryDescription" /></th>
						<td class="dCase">${ossInfo.summaryDescription}</td>
					</tr>
				</tbody>
			</table>
	</div>