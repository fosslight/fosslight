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
					<dd class="lastAign">
						<label>Created Date</label>
						<input type="text" class="cal" name="createdDate1" id="createdDate1" title="Search Start Date" value="${searchBean.createdDate1}" maxlength="8" autocomplete="off" style="width:77px;"/> ~
						<input type="text" class="cal" name="createdDate2" id="createdDate2" title="Search End Date" value="${searchBean.createdDate2}" maxlength="8" autocomplete="off" style="width:77px;"/>
					</dd>
					<dd>
						<label style="width:100px;">3rd Party<br/>Software Name</label>
						<input type="text" name="softwareName" class="autoComSwNm" value="${searchBean.softwareName}" style="width:150px;"/>
					</dd>
					<dd class="centerAign">
						<label>3rd Party<br/>Software Version</label>
						<input type="text" name="softwareVersion" value="${searchBean.softwareVersion}"/>
					</dd>
					<dd class="lastAign">
						<label><br/>Division</label>
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
					<dd>
						<label style="width:100px;">Creator</label>
						<input type="text" name="creator" class="autoComCreatorDivision" value="${searchBean.creator}" style="width:150px;"/>
					</dd>
					<dd class="centerAign">
						<label>Reviewer</label>
						<input type="text" name="reviewer" class="autoComReviewer" value="${searchBean.reviewer}"/>
					</dd>
					<dd class="lastAign">
						<label>Watcher</label>
						<input type="text" name="watchers"  class=""  value="${searchBean.watchers[0]}"/>
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
					<dd class="lastAign">
						<label style="width:100px;">Description</label>
						<textarea name="description" style="margin: 0px; width: 150px; height: 54px;">${searchBean.description}</textarea>
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
						<label>Division</label>
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
											<strong for="parDivision" title="Watcher part selected value">Select Division</strong>
											<select id="parDivision" name="parDivision" onchange="fn.selectDivision()">
												<option value="">Select Division</option>
												${ct:genOption(ct:getConstDef("CD_USER_DIVISION"))}
											</select>
										</span>
										<span class="selectSet w500">
											<strong for="parUserId" title="Watcher name selected value">Select User</strong>
											<select id="parUserId" name="parUserId">
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
												<option value="par">3rd Party List</option>
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
				<div id="ChangeContainer" class="inblock" style="vertical-align:top; position: relative;">
					<input type="button" value="Change" class="btnColor" onclick="fn.change(this);" />
					<div id="ChangeList" class="w100 tright" style="display: none; position: absolute; z-index: 1; left: 0; text-align:left;">
						<a onclick="fn.changeDivision()" style="display: block;">Division</a>
						<a onclick="fn.changeWatcher()" style="display: block;">Watcher</a>
					</div>
				</div>
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
