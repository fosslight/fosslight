<%@ page contentType="text/html; charset=utf-8" pageEncoding="utf-8"%>
<%@ include file="/WEB-INF/constants.jsp"%>
<script type="text/javascript" src="${ctxPath}/js/basic.js"></script>
<script type="text/javascript">
$(document).ready(function() {
	var selectElArr = ["obligationType", "licenseType", "prjDivision", "distributionType", "priority", "networkServerType", "division"];
	$.each(selectElArr, function(index, item){ 
		var selectEl = $('#searchConditionForm select[name="'+item+'"]');
		if(selectEl.length > 0) {
			$(selectEl).trigger('change');
		}
	});
});
</script>
<div style="padding-top: 10px;">
	<fieldset class="listSearch">
		<form id="searchConditionForm" name="searchConditionForm">
			<input type="hidden" name="defaultSearchType" value="${defaultSearchType}" />
			<c:choose>
				<c:when test="${defaultSearchType eq 'LICENSE'}">
					<dl class="basicSearch col2">
						<dt>Basic Search Area</dt>
						<dd>
							<label>License Name</label>
							<input type="text" name="licenseName" class="autoComLicense" value="${searchBean.licenseName}" style="width:230px"/>
							<input type="checkbox" name="licenseNameAllSearchFlag" value="Y" ${searchBean.licenseNameAllSearchFlag eq 'Y' ? 'checked="checked"' : ''}/><span>&nbsp;Exact Match</span>
						</dd>
						<dd class="textArea">
							<label>License Text</label>
							<textarea name="licenseText">${searchBean.licenseText}</textarea>
						</dd>
						<dd>
							<label>License Type</label>
							<span class="selectSet">
								<strong for="licenseType" title="selected value"></strong>
								<select name="licenseType">	
									<option></option>
									${ct:genOptionSelected(ct:getConstDef("CD_LICENSE_TYPE"), searchBean.licenseType)}
								</select>
							</span>
						</dd>
						<c:if test="${not empty ct:getCodeValues(ct:getConstDef('CD_LICENSE_RESTRICTION'))}">
							<dd style="width:100%;">
								<label>Restriction</label>
								<span class='checkSet'>
								${ct:genCheckbox(ct:getConstDef("CD_LICENSE_RESTRICTION"), searchBean.restrictions, 'list')}
		            			</span>
							</dd>
						</c:if>
						<dd>
							<label>User Guide</label>
							<textarea name="description" >${searchBean.description}</textarea>
						</dd>
						<dd>
							<label>Website</label>
							<input name="webpage" type="text" value="${searchBean.webpage}"/>
						</dd>
						<dd>
							<label>Obligation Type</label>
							<span class="selectSet">
								<strong for="obligationType" title="selected value"></strong>
								<select id="obligationType" name="obligationType">	
									<option></option>
									${ct:genOptionSelected(ct:getConstDef("CD_OBLIGATION_TYPE"), searchBean.obligationType)}
									<option value="NONE" ${searchBean.obligationType eq 'NONE' ? 'selected="selected"' : ''}>None</option>
								</select>
							</span>
						</dd>
					</dl>
				</c:when>
				<c:when test="${defaultSearchType eq 'OSS'}">
					<dl class="basicSearch col2">
						<dt>Basic Search Area</dt>
						<dd>
							<label>OSS Name</label>
							<input name="ossName" type="text" class="autoComOss" value="${searchBean.ossName}" style="width:230px"/>
							<input type="checkbox" name="ossNameAllSearchFlag" value="Y" ${searchBean.ossNameAllSearchFlag eq 'Y' ? 'checked="checked"' : ''} /><span>&nbsp;Exact Match</span>
						</dd>
						<dd class="textArea">
							<label>Copyright Text</label>
							<textarea name="copyright">${searchBean.copyright}</textarea>
						</dd>
						<dd>
							<label>License Name</label>
							<input name="licenseName" type="text" class="autoComLicense" value="${searchBean.licenseName}" style="width:230px"/>
							<input type="checkbox" name="licenseNameAllSearchFlag" value="Y" ${searchBean.licenseNameAllSearchFlag eq 'Y' ? 'checked="checked"' : ''}/><span>&nbsp;Exact Match</span>
						</dd>
						<dd>
							<label>Description</label>
							<textarea name="summaryDescription">${searchBean.summaryDescription}</textarea>
						</dd>
						<dd>
							<label>Website</label>
							<input name="homepage" type="text" value="${searchBean.homepage}"/>
						</dd>
						<dd>
							<label>Deactivate</label>
							<input type="checkbox" name="deactivateFlag" value="Y" ${searchBean.deactivateFlag eq 'Y' ? 'checked="checked"' : ''} style="margin:0 5px;"/>
						</dd>
					</dl>
				</c:when>
				<c:when test="${defaultSearchType eq 'PROJECT'}">
					<dl class="basicSearch col3">
						<dt>Basic Search Area</dt>
						<dd>
							<label>ID</label>
							<input type="text" name="prjId" value="${searchBean.prjId}"/>
						</dd>
						<dd class="centerAign">
							<label>Project Name</label>
							<input type="text" name="prjName" class="autoComProjectNm" value="${searchBean.prjName}"/>
						</dd>
						<dd class="lastAign">
							<label>Created Date</label>
							<input name="schStartDate" type="text" class="cal" title="Search Start Date" value="${searchBean.schStartDate}" maxlength="8" autocomplete="off" style="width:77px;"/> ~ 
							<input name="schEndDate" type="text" class="cal" title="Search End Date" value="${searchBean.schEndDate}" maxlength="8" autocomplete="off" style="width:77px;"/> 
						</dd>
						<dd>
							<label>Division</label>
							<span class="selectSet">
								<strong title="Division selected value"></strong>
								<select name="prjDivision">
									<option value=""></option>
									${ct:genOptionSelected(ct:getConstDef("CD_USER_DIVISION"), searchBean.prjDivision)}
								</select>
							</span>						
						</dd>
						<dd class="centerAign">
							<label>Creator</label>
							<input type="text" name="creator" class="autoComCreatorDivision" value="${searchBean.creator}"/>
						</dd>
						<dd class="lastAign">
							<label>Reviewer</label>
							<input type="text" name="reviewer" class="autoComReviewer" value="${searchBean.reviewer}"/>
						</dd>
						<dd>
							<label>Distribution Type</label>
							<span class="selectSet overHidden">
								<strong title="Distribution type selected value"></strong>
								<select name="distributionType">
									<option value=""></option>
									${ct:genOptionSelected(ct:getConstDef("CD_DISTRIBUTION_TYPE"), searchBean.distributionType)}
								</select>
							</span>
						</dd>
						<dd class="centerAign">
							<label>Network Service</label>
							<span class="selectSet overHidden vmiddle">
								<strong title="Network servvice Type selected value"></strong>
								<select name="networkServerType">
									<option value=""></option>
									<option value="Y" ${searchBean.networkServerType eq 'Y' ? 'selected="selected"' : ''}>Yes</option>
									<option value="N" ${searchBean.networkServerType eq 'N' ? 'selected="selected"' : ''}>No</option>
								</select>
							</span>
						</dd>
						<dd class="lastAign">
							<label>Model Name</label>
							<input type="text" name="modelName" class="autoComProjectModel" value="${searchBean.modelName}"/>
						</dd>
						<dd style="width:100%;">
							<label>Status</label>
							<span class='checkSet'>
							${ct:genCheckbox(ct:getConstDef("CD_PROJECT_STATUS"), searchBean.statuses, '')}
	            			</span>
						</dd>
						<dd>
							<label>Priority</label>
							<span class="selectSet overHidden vmiddle">
								<strong title="priority selected value"></strong>
								<select name="priority">
									<option value=""></option>
									${ct:genOptionSelected(ct:getConstDef("CD_PROJECT_PRIORITY"), searchBean.priority)}
								</select>
							</span>
						</dd>
					</dl>
				</c:when>
				<c:when test="${defaultSearchType eq 'THIRD_PARTY'}">
					<dl class="basicSearch col3">
						<dt>Basic Search Area</dt>
						<dd>
							<label style="width:100px;">ID</label>
							<input type="text" name="partnerId" value="${searchBean.partnerId}" style="width:150px;"/>
						</dd>
						<dd class="centerAign">
							<label>3rd Party Name</label>
							<input type="text" name="partnerName" class="autoComParty" value="${searchBean.partnerName}"/>
						</dd>
						<dd class="lastAign"></dd>
						<dd>
							<label style="width:100px;">3rd Party<br/>Software Name</label>
							<input type="text" name="softwareName" class="autoComSwNm" value="${searchBean.softwareName}" style="width:150px;"/>
						</dd>
						<dd class="centerAign">
							<label>3rd Party<br/>Software Version</label>
							<input type="text" name="softwareVersion" value="${searchBean.softwareVersion}"/>
						</dd>
						<dd class="lastAign">
							<label>Division</label>
							<span class="selectSet">
								<strong title="Division selected value"></strong>
								<select name="division">
									<option value=""></option>
									${ct:genOptionSelected(ct:getConstDef("CD_USER_DIVISION"), searchBean.division)}
								</select>
							</span>			
						</dd>
						<dd style="width:100%;">
							<label style="width:100px;">Status</label>
							<span class='checkSet'>
							${ct:genCommonCheckbox(ct:getConstDef("CD_IDENTIFICATION_STATUS"), "status", searchBean.status, false)}
	            			</span>
						</dd>
						<dd class="">
							<label style="width:100px;">Created Date</label>
							<input type="text" class="cal" name="createdDate1" id="createdDate1" title="Search Start Date" value="${searchBean.createdDate1}" style="width:70px;" maxlength="8"/> ~ 
							<input type="text" class="cal" name="createdDate2" id="createdDate2" title="Search End Date" value="${searchBean.createdDate2}" style="width:70px;" maxlength="8"/> 			
						</dd>
						<dd class="centerAign">
							<label>Creator</label>
							<input type="text" name="creator" class="autoComCreatorDivision" value="${searchBean.creator}"/>
						</dd>
						<dd class="lastAign">
							<label>Reviewer</label>
							<input type="text" name="reviewer" class="autoComReviewer" value="${searchBean.reviewer}"/>
						</dd>
					</dl>
				</c:when>

				<c:when test="${defaultSearchType eq 'SELF_CHECK'}">
					<dl class="basicSearch col3">
						<dt>Basic Search Area</dt>
						<dd>
							<label>ID</label>
							<input type="text" name="prjId" value="${searchBean.prjId}"/>
						</dd>
						<dd class="centerAign">
							<label>Project Name</label>
							<input type="text" name="prjName" value="${searchBean.prjName}"/>
						</dd>
						<dd class="lastAign">
							<label>OSS Name</label>
							<input type="text" name="ossName" class="autoComOss" value="${searchBean.ossName}"/>
						</dd>
						<dd>
							<label>Created Date</label>
							<input name="schStartDate" id="schStartDate" type="text" class="cal" title="Search Start Date" value="${searchBean.schStartDate}" maxlength="8" autocomplete="off"/> ~ 
							<input name="schEndDate" id="schEndDate" type="text" class="cal" title="Search End Date" value="${searchBean.schEndDate}" maxlength="8" autocomplete="off"/> 
						</dd>
						<dd class="centerAign">
							<label>License Name</label>
							<input type="text" name="licenseName" class="autoComLicense" value="${searchBean.licenseName}"/>
						</dd>
						<dd class="lastAign ">
							<label>Creator</label>
							<input type="text" name="creator" class="autoComCreatorDivision" value="${searchBean.creator}"/>
						</dd>
					</dl>
				</c:when>
			</c:choose>
		</form>
	</fieldset>
</div>
<!---->
