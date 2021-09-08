<%@ page contentType="text/html; charset=utf-8" pageEncoding="utf-8"%>
<%@ include file="/WEB-INF/constants.jsp"%>
<script type="text/javascript">
$(document).ready(function() {
	var list = "${syncCheckList}";
	var thLength = $("th.dCase").length;
	var checkList = list.replace("[","").replace("]","").split(",");
	
	$("th.dCase").removeClass("txStr");

	if (checkList.length > 0) {
		for (var i=0; i<checkList.length; i++) {
			for (var j=0; j<thLength; j++) {
				if (checkList[i].trim() == $("th.dCase").eq(j).text().trim()){
					$("th.dCase").eq(j).addClass("txStr");
					break;
				}
			}
		}
	}
});
</script>
		<div class="tbws1">

			<table class="dCase">
				<colgroup>
					<col width="2"/>
					<col width="170"/>
					<col/>
				</colgroup>
				<tbody>
					<tr>
						<th class="dCase"><input type="checkbox" id="Declared License" checked="checked"></th>
						<th class="dCase">Declared License</th>
						<td class="dCase">${ossInfo.licenseName}</td>
					</tr>
					<tr>
						<th class="dCase"><input type="checkbox" id="Detected License" checked="checked"></th>
						<th class="dCase">Detected License</th>
						<td class="dCase">${ossInfo.detectedLicense}</td>
					</tr>
					<tr>
						<th class="dCase" style="height:50px;"><input type="checkbox" id="Copyright"></th>
						<th class="dCase">Copyright</th>
						<td class="dCase">${ossInfo.copyright}</td>
					</tr>
					<tr>
						<th class="dCase"><input type="checkbox" id="Download Location"></th>
						<th class="dCase">Download Location</th>
						<td class="dCase">
							<c:choose>
								<c:when test="${not empty ossInfo.downloadLocations}">
									<c:forEach var="downloadLocation" items="${ossInfo.downloadLocations}" varStatus="g">
										${downloadLocation}<br/>
									</c:forEach> 
								</c:when>
								<c:otherwise></c:otherwise>
							</c:choose>
						</td>
					</tr>
					<tr>
						<th class="dCase"><input type="checkbox" id="Home Page"></th>
						<th class="dCase">Home Page</th>
						<td class="dCase">${ossInfo.homepage}</td>
					</tr>
					<tr>
						<th class="dCase" style="height:50px;"><input type="checkbox" id="Summary Description"></th>
						<th class="dCase">Summary Description</th>
						<td class="dCase">${ossInfo.summaryDescription}</td>
					</tr>
					
					<tr>
						<th class="dCase" style="height:50px;"><input type="checkbox" id="Attribution"></th>
						<th class="dCase">Attribution</th>
						<td class="dCase">${ossInfo.attribution}</td>
					</tr>
				</tbody>
			</table>
	</div>