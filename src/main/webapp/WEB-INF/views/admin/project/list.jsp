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
						<input type="text" name="prjName" class="autoComProjectNm" value="${searchBean.prjName}"/>
					</dd>
					<dd class="lastAign">
						<label>Created Date</label>
						<input name="schStartDate" id="schStartDate" type="text" class="cal" title="Search Start Date" value="${searchBean.schStartDate}" maxlength="8" autocomplete="off" style="width:77px;"/> ~ 
						<input name="schEndDate" id="schEndDate" type="text" class="cal" title="Search End Date" value="${searchBean.schEndDate}" maxlength="8" autocomplete="off" style="width:77px;"/> 
					</dd>
					<dd>
						<label>Creator</label>
						<input type="text" name="creator" class="autoComCreatorDivision" value="${searchBean.creator}"/>
					</dd>
					<dd class="centerAign">
						<label>Reviewer</label>
						<input type="text" name="reviewer" class="autoComReviewer" value="${searchBean.reviewer}"/>
					</dd>
					<dd class="lastAign">
						<label>Watcher</label>
						<input type="text" name="watchers" class="" value="${searchBean.watchers[0]}"/>
					</dd>
					<dd>
						<label>Distribution Type</label>
						<span class="selectSet overHidden">
							<strong title="Distribution type selected value"></strong>
							<select name="distributionType">
								<option value=""></option>
								${ct:genOption(ct:getConstDef("CD_DISTRIBUTION_TYPE"))}
							</select>
						</span>
					</dd>
					<dd class="centerAign">
						<label>Network Service</label>
						<span class="selectSet overHidden vmiddle">
							<strong title="Network servvice Type selected value"></strong>
							<select name="networkServerType">
								<option value=""></option>
								<option value="Y">Yes</option>
								<option value="N">No</option>
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
							<strong title="Network servvice Type selected value"></strong>
							<select name="priority">
								<option value=""></option>
								${ct:genOption(ct:getConstDef("CD_PROJECT_PRIORITY"))}
							</select>
						</span>
					</dd>
					<dd class="centerAign">
						<label>Division</label>
						<span class="selectSet">
							<strong title="Status selected value"></strong>
							<select name="prjDivision">
								<option value=""></option>
								${ct:genOption(ct:getConstDef("CD_USER_DIVISION"))}
							</select>
						</span>
					</dd>
					<c:if test="${!ct:isAdmin()}">
					<dd class="">
						<label class="vmiddle" style="width: 50%;">View My Projects Only</label>
						<input type="checkbox" id="checkbox3" name="publicYn" ${searchBean.publicYn eq 'N' ? 'checked="checked"' : '' }/>
					</dd>
					</c:if>
				</dl>
				<input type="button" value="Admin Expand apply" class="btnHiddenExpand" />
				<dl class="hiddenSearch" style="display:none;">
					<dd>
						<label>OSS Name</label>
						<input type="text" name="ossName" class="autoComOss" value="${searchBean.ossName}"/>
					</dd>
					<dd class="centerAign">
						<label>OSS Version</label>
						<input type="text" name="ossVersion" class="autoComOssVersion" value="${searchBean.ossVersion}"/>
					</dd>
					<dd class="lastAign">
						<label>License Name</label>
						<input type="text" name="licenseName" class="autoComLicense" value="${searchBean.licenseName}"/>
					</dd>
					<dd>
						<label>Additional Information</label>
						<textarea name="comment" style="margin: 0px; width: 201px; height: 54px;">${searchBean.comment}</textarea>
					</dd>
					<dd class="centerAign w600">
						<label>Comment</label>
						<textarea name="userComment" style="margin: 0px; width: 180px; height: 54px;">${searchBean.userComment}</textarea>					
					</dd>
						<dd>
							<label>Binary Name</label>
							<input type="text" name="schBinaryName"  value="${searchBean.schBinaryName}"/>
						</dd>
					<c:if test="${partnerFlag}">
						<dd class="centerAign">
							<label>3rd party</label>
							<input type="text" name="refPartnerId" class="autoComConfParty" value="${searchBean.refPartnerId}"/>
						</dd>
					</c:if>
				</dl>
				<input name="act" type="hidden" value="search"/>
				<input id="search" type="submit" value="Search" class="btnColor search" />
				<a class="right" id="helpLink" style="position:absolute; cursor: pointer; top:10px; right:-60px; display:none;"><img alt="" src="${ctxPath}/images/user-guide.png" /></a>
			</form>
		</fieldset>
		<!---->
		<div class="btnLayout">
			<input type="button" value="Reject" class="btnReject btnColor left" style="display: none;"/>
			
			<!-- Popup -->
			<div id="changeStatusPop" class="pop changeStatusPop">
				<h1 class="orange">Change Status</h1>
				<div class="popdata">
					<input type="hidden" id="identificationStatus" />
					<input type="hidden" id="verificationStatus" />
					<input type="hidden" id="distributionStatus" />
					<input type="hidden" id="completeFlag" />
					<input type="hidden" id="dropFlag" />
					<input type="hidden" id="commId"/>
					<div class="radioSet" id="CSIdentification"><input type="radio" id="r1" name="radioName" value="1"><label for="r1">Restart Identification</label></div>
					<div class="radioSet mt10" id="CSDrop"><input type="radio" id="r2" name="radioName" value="2"><label for="r2">Drop</label></div>
					<div class="radioSet mt10" id="CSDelete"><input type="radio" id="r3" name="radioName" value="3"><label for="r3">Delete</label></div>
					<c:if test="${ct:isAdmin()}">
						<div class="radioSet mt10" id="CSComplete"><input type="radio" id="r4" name="radioName" value="4"><label for="r4">Complete</label></div>
					</c:if>
					<div class="taSet required mt20">
						<label for="t1">Caused by</label>
						<textarea id="reason" rows="8"></textarea>
						<div class="retxt">This field is required.</div>
					</div>
				</div>
				<div class="pbtn">
					<input id="popCancel" type="button" value="Cancel" class="btnCancel btnColor" />
					<input id="popChangeStatus" type="button" value="OK" class="btnColor red" />
				</div>
			</div>
			<!-- //Popup -->
			
			<!-- Popup -->
			<div id="changeDivisionPop" class="pop changeDivisionPop">
				<h1 class="orange">Change Division</h1>
				<div class="popdata">
					<div class="mtb20">
						<label>Division</label>
						<span id="changeDivisionSelect" class="selectSet" style="width: 200px;">
							<strong title="Division selected value"></strong>
							<select name="division">
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
			
			<!-- Popup -->
			<div id="changeWatcherPop" class="pop changeWatcherPop" style="left:35%;">
				<h1 class="orange">Change Watcher</h1>
				<div class="popdata">
					<div class="tbws1 w900 mt10">
						<table class="dCase">
							<tr>
								<th class="dCase"><spring:message code="msg.common.field.watcher" /></th>
								<td class="dCase">
									<div class="pb5">
										<span class="selectSet w200">
											<strong for="prjDivision" title="Watcher part selected value">Select Division</strong>
											<select id="prjDivision" name="prjDivision" onchange="fn.selectDivision()">
												<option value="">Select Division</option>
												${ct:genOption(ct:getConstDef("CD_USER_DIVISION"))}
											</select>
										</span>
										<span class="selectSet w500">
											<strong for="prjUserId" title="Watcher name selected value">Select User</strong>
											<select id="prjUserId" name="prjUserId">
											</select>
										</span>
										<input type="button" value="+ Add" class="btnCLight gray" onclick="fn.addWatcherClick();"/>
									</div>
									<div class="pb5">
										<span><input type="text" id="adId" name="adId" style="width:200px" placeholder="Input AD ID" onKeypress="fn.checkChar()" /></span>
										<c:set var="useDomainFlag" value="${ct:genOption(ct:getConstDef('CD_REGIST_DOMAIN'))}" />
										<c:choose>
											<c:when test="${not empty useDomainFlag}">
												<span class="selectSet w220">
													<strong for="domain" title="Watcher domain selected value">${ct:getCodeExpString(ct:getConstDef('CD_REGIST_DOMAIN'), ct:getConstDef('CD_DTL_DEFAULT_DOMAIN'))}</strong>
													<select id="domain" name="domain">
														${ct:genOption(ct:getConstDef("CD_REGIST_DOMAIN"))}
													</select>
												</span>
												<input type="text" id="emailTemp" class="w220" <c:if test="${not empty useDomainFlag}">style="display:none;" value="${ct:getCodeExpString(ct:getConstDef('CD_REGIST_DOMAIN'), ct:getConstDef('CD_DTL_DEFAULT_DOMAIN'))}"</c:if> onKeypress="fn.CheckChar()"  placeholder="Input your Email Domain" />
											</c:when>
											<c:otherwise>
												<span class="pd5">@</span>
												<input type="text" id="emailTemp" style="width:326px !important" value="" onKeypress="fn.checkChar()"  placeholder="Input your Email Domain" />
											</c:otherwise>
										</c:choose>
										<input type="button" value="+ Add" class="btnCLight gray" onclick="fn.addEmail();"/>
									</div>
									<div class="pb5">
										<span class="selectSet w200">
											<strong for="listKind" title="selected value">Select List</strong>
											<select id="listKind" name="listKind">
												<option value="">Select List</option>
												<option value="prj">Project List</option>
											</select>
										</span>
										<span><input type="text" id="listId" name="listId" style="width:500px" placeholder="Input ID you want to copy"/></span>
										<input type="button" value="+ Add" class="btnCLight gray" onclick="fn.addList();"/>
									</div>
									<div id="multiDiv" class="multiTxtSet2">
									</div>
								</td>
							</tr>
						</table>
					</div>
				</div>
				<div class="pbtn" style="text-align:right;">
					<input type="button" value="Add" class="btnColor red" onclick="fn.changeWatcherAdd();"/>
					<input type="button" value="Delete" class="btnColor red" onclick="fn.changeWatcherDelete();"/>
					<input type="button" value="Cancel" class="btnCancel btnColor" onclick="fn.changeWatcherCanCel();"/>
				</div>
			</div>
			<!-- //Popup -->
			
			<span class="left">
				<input id="copy" type="button" value="Copy" class="btnColor" onclick="fn.copy();"/>
				<div id="ChangeContainer" class="inblock" style="vertical-align:top; position: relative;">
					<input type="button" value="Change" class="btnColor" onclick="fn.change(this);" />
					<div id="ChangeList" class="w100 tright" style="display: none; position: absolute; z-index: 1; left: 0; text-align:left;">
						<a onclick="fn.checkProjectStatus()" style="display: block;">Status</a>
						<a onclick="fn.changeDivision()" style="display: block;">Division</a>
						<a onclick="fn.changeWatcher()" style="display: block;">Watcher</a>
					</div>
				</div>
				<input type="button" value="BOM Compare" class="btnColor blue w120" onclick="fn.bomCompare();" />
			</span>
			
			<span class="right">
				<a href="#none" class="btnSet excel" onclick="fn.downloadExcel()"><span>Export</span></a>
				<input type="button" value="Add" class="btnColor btnAdd" onclick="createTabInFrame('New_Project', '#<c:url value="/project/edit"/>')" />
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