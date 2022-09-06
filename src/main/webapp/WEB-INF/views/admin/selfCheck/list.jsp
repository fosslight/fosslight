<%@ page contentType="text/html; charset=utf-8" pageEncoding="utf-8"%>
<%@ include file="/WEB-INF/constants.jsp"%>
<!-- wrap -->
<div id="wrapIframe">
	<!---->
	<div>
		<!---->
		<fieldset class="listSearch">
			<form id="projectSearch" name="projectSearch">
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
				<input name="act" type="hidden" value="search"/>
				<input id="search" type="submit" value="Search" class="btnColor search" />
				<a class="right" id="helpLink" style="position:absolute; cursor: pointer; top:10px; right:-60px; display:none;"><img alt="" src="${ctxPath}/images/user-guide.png" /></a>
			</form>
		</fieldset>
		<!---->
		<div class="btnLayout">
			<!-- Popup -->
			<div id="rejectPop" class="pop rejectPop">
				<h1>RequestUpdate</h1>
				<div class="popdata">
					<div class="radioSet"><input type="radio" id="r1" name="radioName" value="0"><label for="r1">Identification</label></div>
					<div class="radioSet mt10"><input type="radio" id="r2" name="radioName" value="1"><label for="r2">Verification</label></div>
					<div class="taSet required mt20">
						<label for="t1">Caused by</label>
						<textarea id="reason" rows="8"></textarea>
						<div class="retxt">This field is required.</div>
					</div>
				</div>
				<div class="pbtn">
					<input id="popCancel" type="button" value="Cancel" class="btnCancel btnColor" />
					<input id="popReject" type="button" value="Reject" class="btnColor red" />
				</div>
			</div>
			<!-- //Popup -->
			<div class="btnLayout">
				<span class="right">
					<a href="#none" class="btnSet excel" onclick="fn.downloadExcel()"><span>Export</span></a>
					<input type="button" value="Add" class="btnColor" onclick="createTabInFrame('New_SelfCheck', '#<c:url value="/selfCheck/edit"/>')" />
				</span>
			</div>
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