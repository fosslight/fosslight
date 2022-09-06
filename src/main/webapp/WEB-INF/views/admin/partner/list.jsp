<%@ page contentType="text/html; charset=utf-8" pageEncoding="utf-8"%>
<%@ include file="/WEB-INF/constants.jsp"%>
<!-- wrap -->
<div id="wrapIframe">
	<!---->
	<div>
		<!---->
		<fieldset class="listSearch">
			<form id="3rdSearch" name="3rdSearch">
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
							<strong title="Status selected value"></strong>
							<select name="division">
								<option value=""></option>
								${ct:genOption(ct:getConstDef("CD_USER_DIVISION"))}
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
						<input type="text" class="cal" name="createdDate1" id="createdDate1" title="Search Start Date" value="${searchBean.createdDate1}" style="width:70px;" maxlength="8" autocomplete="off"/> ~ 
						<input type="text" class="cal" name="createdDate2" id="createdDate2" title="Search End Date" value="${searchBean.createdDate2}" style="width:70px;" maxlength="8" autocomplete="off"/> 			
					</dd>
					<dd class="centerAign">
						<label>Creator</label>
						<input type="text" name="creator" class="autoComCreatorDivision" value="${searchBean.creator}"/>
					</dd>
					<dd class="lastAign">
						<label>Reviewer</label>
						<input type="text" name="reviewer" class="autoComReviewer" value="${searchBean.reviewer}"/>
					</dd>
					<c:if test="${!ct:isAdmin()}">
					<dd class="lastAign" >
						<label style="width:150px;">View My 3rd Parties Only</label>
						<input type="checkbox" id="checkbox3" name="publicYn" ${searchBean.publicYn eq 'N' ? 'checked="checked"' : '' }/>
					</dd>
					</c:if>
				</dl>
				<input type="button" value="Admin Expand apply" class="btnHiddenExpand" />
				<dl class="hiddenSearch" style="display:none;">
					<dd>
						<label style="width:100px;">OSS Name</label>
						<input type="text" name="ossName" class="autoComOss" value="${searchBean.ossName}" style="width:150px;"/>
					</dd>
					<dd class="centerAign">
						<label>OSS Version</label>
						<input type="text" name="ossVersion" class="autoComOss" value="${searchBean.ossVersion}"/>
					</dd>
					<dd class="lastAign">
						<label style="width:100px;">License Name</label>
						<input type="text" name="licenseName" class="autoComLicense" value="${searchBean.licenseName}" style="width:150px;"/>
					</dd>
					<dd>
						<label style="width:100px;">Binary Name</label>
						<input type="text" name="binaryName" class="" value="${searchBean.binaryName}" style="width:150px;"/>
					</dd>
					<dd class="centerAign">
						<label>Comment</label>
						<textarea name="userComment" style="margin: 0px; width: 180px; height: 54px;">${searchBean.comment}</textarea>					
					</dd>
				</dl>
				<input name="act" type="hidden" value="search"/> 
				<input type="submit" id="search" value="Search" class="btnColor search" />
				<a class="right" id="helpLink" style="position:absolute; cursor: pointer; top:10px; right:-60px; display:none;"><img alt="" src="${ctxPath}/images/user-guide.png" /></a>
			</form>
		</fieldset>
		<!---->
		<div class="btnLayout">
			<!-- Popup -->
			<div id="partnerChangeDivisionPop" class="pop changeDivisionPop">
				<h1 class="orange">Change Division</h1>
				<div class="popdata">
					<div class="mtb20">
						<span>Division</label>
						<span id="partnerChangeDivisionSelect" class="selectSet" style="width: 200px;">
							<strong title="Division selected value"></strong>
							<select name="partnerDivision">
								${ct:genOption(ct:getConstDef("CD_USER_DIVISION"))}
							</select>
						</span>	
					</div>
				</div>
				<div class="pbtn">
					<input type="button" value="Cancel" class="btnCancel btnColor" onclick="fn.changeDivisionCancel();"/>
					<input type="button" value="OK" class="btnColor red" onclick="fn.changeDivisionSave();"/>
				</div>
			</div>
			<!-- //Popup -->
		
			<span class="left">
				<input type="button" value="Change Division" class="btnColor w120" onclick="fn.changeDivision();" />
			</span>
			<span class="right">
				<a href="#none" class="btnSet excel" onclick="fn.downloadExcel()"><span>Export</span></a>
				<input type="button" value="Add" class="btnColor btnAdd" onclick="createTabInFrame('New_3rdParty', '#<c:url value="/partner/edit"/>')" />
			</span>
		</div>
		<!---->
		<div class="jqGridSet">
			<table id="list"><tr><td></td></tr></table>
			<div id="pager"></div>
		</div>
	</div>
	<!---->

</div>
<!-- //wrap -->
