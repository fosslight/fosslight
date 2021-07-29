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
						<th class="dCase txStr">OSS Name</th>
						<td class="dCase">${ossInfo.ossName}</td>
					</tr>
					<tr>
						<th class="dCase">Nick Name</th>
						<td class="dCase">${ossInfo.ossNickname}</td>
					</tr>
					<tr>
						<th class="dCase">OSS Version</th>
						<td class="dCase">${ossInfo.ossVersion}<c:if test="${ossInfo.ossType eq 'V'}"> / <span class="iconSet vdif">v-Diff</span></c:if></td>
					</tr>
					<tr>
						<th class="dCase txStr">Declared License</th>
						<td class="dCase" id="td_declaredLicenseName">
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
						<th class="dCase">Detected License</th>
						<td class="dCase" id="td_detectedLicenseName">
							${ossInfo.detectedLicense}
						</td>
					</tr>
					<tr>
						<th class="dCase">Copyright</th>
						<td class="dCase">${ossInfo.copyright}</td>
					</tr>
					<tr>
						<th class="dCase">License Type</th>
						<td class="dCase">${ct:getCodeString(ct:getConstDef('CD_LICENSE_TYPE'), ossInfo.licenseType)}</td>
					</tr>
					<tr>
						<th class="dCase">Obligation</th>
						<td class="dCase">
							<c:choose>
								<c:when test="${ossInfo.obligationType eq ct:getConstDef('CD_DTL_OBLIGATION_NOTICE')}"><span class="iconSet ops" title="Notice"></span></c:when>
								<c:when test="${ossInfo.obligationType eq ct:getConstDef('CD_DTL_OBLIGATION_DISCLOSURE')}"><span class="iconSet ops" title="Notice"></span><span class="iconSet man" title="Source Code"></span></c:when>
								<c:otherwise><span></span></c:otherwise>
							</c:choose>
						</td>
					</tr>
					<tr>
						<th class="dCase">Download Location</th>
						<td class="dCase">
							<c:choose>
								<c:when test="${empty ossInfo.downloadLocationGroup and not empty ossInfo.downloadLocation}"><a href="${ossInfo.downloadLocation}" class="urlLink" target="_blank">${ossInfo.downloadLocation}</a></c:when>
								<c:when test="${not empty ossInfo.downloadLocationGroup}">
									<c:set var="downloadLocationGroup" value="${fn:split(ossInfo.downloadLocationGroup,',')}" />
									<c:forEach var="downloadLocation" items="${downloadLocationGroup}" varStatus="g">
										<a href="${downloadLocation}" class="urlLink" target="_blank">${downloadLocation}</a>
									</c:forEach> 
								</c:when>
								<c:otherwise></c:otherwise>
							</c:choose>
						</td>
					</tr>
					<tr>
						<th class="dCase">Home Page</th>
						<td class="dCase">
							<c:choose>
								<c:when test="${not empty ossInfo.homepage}"><a href="${ossInfo.homepage}" class="urlLink" target="_blank">${ossInfo.homepage}</a></c:when>
								<c:otherwise></c:otherwise>
							</c:choose>
						</td>
					</tr>
					<tr>
						<th class="dCase">Summary Description</th>
						<td class="dCase">${ossInfo.summaryDescription}</td>
					</tr>
					<tr>
						<th class="dCase">Attribution</th>
						<td class="dCase">${ossInfo.attribution}</td>
					</tr>
					<tr>
						<th class="dCase">Creator</th>
						<td class="dCase">${ossInfo.creator}</td>
					</tr>
					<tr>
						<th class="dCase">Modifier</th>
						<td class="dCase">${ossInfo.modifier}</td>
					</tr>
				</tbody>
			</table>
	</div>