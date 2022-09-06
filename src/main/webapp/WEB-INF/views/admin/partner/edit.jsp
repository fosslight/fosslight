<%@ page contentType="text/html; charset=utf-8" pageEncoding="utf-8"%>
<%@ include file="/WEB-INF/constants.jsp"%>
<jsp:include page="../common/gridCommonFn.jsp" flush="false" />
<!-- wrap -->
<c:set var="isCommited" value="${detail.status eq 'CONF'}"/>
<div id="wrapIframe">
	<!---->
	<div class="projdecTop" style="height:auto;">
		<div class="projdecTab" style="height:auto;">
			<div class="subTab" style="padding:0px;">
				<div class="tabMenu">
					<a rel="partyDiv">3rd party</a>
					<c:if test="${batFlag}">
						<a rel="batDiv">BAT</a>
					</c:if>
				</div>
			</div>
				<c:if test="${detail.viewOnlyFlag ne 'Y'}">
					<c:if test="${detail.loginUserRole ne 'ROLE_VIEWER'}">
						<div class="projdecBtn" style="top:0px;">
						<c:if test="${detail.status eq 'CONF'}">
							<input id="createProject" type="button" value="Create Project for OSS Notice" class="btnColor red w200" />
						</c:if>
						<c:if test="${detail.loginUserRole eq 'ROLE_ADMIN'}">
							<c:if test="${detail.status eq 'REV' }">
							<a href="javascript:void(0);" class="btnSet confirm" onclick="fn.confirm()"><span>Confirm</span></a>
							</c:if>
							<c:if test="${detail.status eq 'REV' || detail.status eq 'CONF'}">
							<a href="javascript:void(0);" class="btnSet reject" onclick="fn.reject()"><span>Reject</span></a>
							</c:if>
							<c:if test="${detail.status eq 'REQ' }">
							<a href="javascript:void(0);" class="btnSet restart" onclick="fn.reviewStart()"><span>Review Start</span></a>
							</c:if>
								<c:if test="${detail.status eq 'PROG' }">
							<a href="javascript:void(0);" class="btnSet review" onclick="fn.requestReview()"><span>Request</span></a>
							</c:if> 
						</c:if>
						<c:if test="${detail.loginUserRole ne 'ROLE_ADMIN'}">
							<c:if test="${detail.status eq 'REQ' || detail.status eq 'CONF'}">
							<a href="javascript:void(0);" class="btnSet reject" onclick="fn.reject()"><span>Reject</span></a>
							</c:if>
							<c:if test="${detail.status eq 'PROG' }">
							<a href="javascript:void(0);" class="btnSet review" onclick="fn.requestReview()"><span>Request</span></a>
							</c:if> 
						</c:if>
						</div>
					</c:if>
					<c:if test="${not empty detail}">
						<input type="button" value="Comment Edit" class="btnColor commentBtn" />
						<span style="right: 125px; position: absolute; bottom: -23px; font-size: 11px;" >
							<input type="button" value="Show Comment History" class="btnColor purple btnCommentHistory" style="width: 160px; height: 18px;"/>
						</span>
					</c:if>
				</c:if>
			</div>
		</div>
		<div class="commentEditor" style="display:none; top:85px;">
			<div class="cBtn">
				<input type="button" value="Save & Send comment" class="btnCLight saveEditor" onclick="fn.sendEditor('WR');"/>
				<input type="button" value="Save draft" class="btnCLight saveEditorDraft" />
			</div>
			<div class="grid-container" style="height:240px;">
				<div class="grid-width-100">
					<div id="editor">${detail.userComment }</div>
				</div>
			</div>
			<script>
				initSample();
			</script>
		</div>
	<div class="third">
	
	<!-- 3rd party ****************************************************************************************************** -->
		<div id="partyDiv" class="tabContent">
		<!---->
		<div class="projectContents">
			<div class="tbws1 w1025" style="margin-top:20px;">
			<c:set var="confirmStatusDisabled"><c:if test="${detail.status eq 'CONF'}">disabled="disabled"</c:if></c:set>
			<form id="partnerForm">
				<input type="hidden" id="partnerId" name="partnerId" value="${detail.partnerId }"/>
				<input type="hidden" id="status" name="status"
				<c:if test="${not empty detail }">value="${detail.status }"</c:if>
				<c:if test="${empty detail }">value="PROG"</c:if>/>
				<input type="hidden" id="ossComponentsStr" name="ossComponentsStr"/>
				<input type="hidden" id="ossComponentsLicenseStr" name="ossComponentsLicenseStr"/>
				<input type="hidden" id="userComment" name="userComment"/>
				<input type="hidden" id="delDocumentsFile" name="delDocumentsFile"/>
				<table class="dCase">
					<colgroup>
						<col width="188" />
						<col />
					</colgroup>
					<tbody>
						<tr>
							<th class="dCase txStr"><spring:message code="msg.common.field.3rdParty.name" /></th>
							<td class="dCase">
								<div class="required">
									<input type="text" id="partnerName" name="partnerName" class="autoComParty w100P" value='' ${confirmStatusDisabled}/>
									<div class="retxt"></div>
								</div>
							</td>
						</tr>
						<tr>
							<th class="dCase txStr"><spring:message code="msg.common.field.3rdParty.softwareName" /></th>
							<td class="dCase">
								<div class="required">
									<input type="text" id="softwareName" name="softwareName" class="autoComSwNm w100P" value='' ${confirmStatusDisabled}/>
									<div class="retxt">This field is required.</div>
								</div>
							</td>
						</tr>
						<tr>
							<th class="dCase"><spring:message code="msg.common.field.3rdParty.softwareVersion" /></th>
							<td class="dCase"><input type="text" id="softwareVersion" name="softwareVersion" value="${not empty detail ? detail.softwareVersion : ''}" ${confirmStatusDisabled} />
							<div class="retxt" style="display:none;">This field is required.</div></td>
						</tr>
						<tr>
							<th class="dCase"><spring:message code="msg.common.field.deliveryForm" /></th>
							<td class="dCase">
								<p class="pd5"><spring:message code="msg.partner.deliveryForm.notice" /></p>
								<span class="selectSet w150">
									<strong for="deliveryForm" title="selected value">Source Code Form</strong>
									<select id="deliveryForm" name="deliveryForm" ${confirmStatusDisabled}>
										${ct:genOption(ct:getConstDef("CD_PARTNER_DELIVERY_FORM"))}
									</select>
								</span>
							</td>
						</tr>
						<c:if test="${project.viewOnlyFlag ne 'Y'}">
						<tr>
							<th class="dCase"><spring:message code="msg.common.field.permission" /></th>
							<td class="dCase">
								<span>View : </span>
								<span class="radioSet">
									<input type="radio" name="publicYn" value="Y" id="permissionRadio1" ${(not empty detail && detail.publicYn ne 'N') || empty detail ? 'checked="checked"' : ''} ${confirmStatusDisabled} /><label for="permissionRadio1">EveryOne</label>
									<input type="radio" name="publicYn" value="N" id="permissionRadio2" ${not empty detail && detail.publicYn eq 'N' ? 'checked="checked"' : ''} ${confirmStatusDisabled} /><label for="permissionRadio2">Creator & Watcher</label>
								</span><br>
								<span>Edit : Creator & Watcher only</span>
							</td>
						</tr>
						</c:if>
						<tr>
							<th class="dCase"><spring:message code="msg.common.field.description" /></th>
							<td class="dCase">
								<div class="grid-container">
									<div class="grid-width-100">
										<div id="editor4"><c:if test="${not empty detail }">${detail.description }</c:if></div>
									</div>
								</div>
								<c:if test="${(detail.viewOnlyFlag ne 'Y') and (not empty detail.partnerId) }">
									<div class="right mt5">
										<input id="saveBtn" type='button' value='Save' class='btnCLight red right' style="margin-left:5px;" onclick="fn.editDescription();"/>
									</div>
								</c:if>
							</td>
						</tr>
						<tr>
							<th class="dCase"><spring:message code="msg.common.field.Agreement" /><br/><c:if test="${checkFlag}"><a href="javascript:void(0);" class="sampleDown" onclick="fn.sampleDownload('arg')"><span>Sample</span></a></c:if></th>
							<td class="dCase uploadCase confirmationUpload">
								<c:if test="${empty confirmationFile}">
									<span class="fileex_back">
										<div id="confirmationFile" <c:if test="${isCommited}">style="display:none;"</c:if>>+ Add file</div>
										<input type="hidden" id="confirmationFileId" name="confirmationFileId"/>
									</span>
								</c:if>
								<c:if test="${not empty confirmationFile}">
									<a href="<c:url value="/download/${confirmationFile.fileSeq }/${confirmationFile.logiNm}"/>">${confirmationFile.origNm }</a><span style="margin-left:20px;">${confirmationFile.createdDate}</span>
									<c:if test="${detail.viewOnlyFlag ne 'Y'}">
									<span><input type="button" value="Delete" class="smallDelete" onclick="fn.deleteConfirmationFile(this)" style="vertical-align:super;" <c:if test="${isCommited}">style="display:none;"</c:if>/></span>
									</c:if>
									<input type="hidden" id="confirmationFileId" name="confirmationFileId" value="${confirmationFile.fileSeq}"/>
								</c:if>
								<div class="required">
									<div class="retxt confirmationFile">This field is required.</div>
								</div>
							</td>
						</tr>
						<tr>
							<th class="dCase"><spring:message code="msg.common.field.OSSChecklist" />(Open Source List)<br/><a href="javascript:void(0);" class="sampleDown" onclick="fn.sampleDownload('chk')"><span>Sample</span></a></th>
							<td class="dCase uploadCase ossUpload">
								<c:if test="${empty ossFile}">
									<span class="fileex_back">
										<div id="ossFile" <c:if test="${isCommited}">style="display:none;"</c:if>>+ Add file</div>
										<input type="hidden" id="ossFileId" name="ossFileId"/>
									</span>
								</c:if>
								<c:if test="${not empty ossFile}">
									<a href="<c:url value="/download/${ossFile.fileSeq }/${ossFile.logiNm}"/>">${ossFile.origNm }</a><span style="margin-left:20px;">${ossFile.createdDate}</span>
									<c:if test="${detail.viewOnlyFlag ne 'Y'}">
									<span><input type="button" value="Delete" class="smallDelete" onclick="fn.deleteOssFile(this)" style="vertical-align:super;"/></span>
									</c:if>
									<input type="hidden" id="ossFileId" name="ossFileId" value="${ossFile.fileSeq}"/>
								</c:if>
								<div class="required">
									<div class="retxt ossFileId">This field is required.</div>
								</div>
							</td>
						</tr>
					</tr>
					<tr>
						<th class="dCase"><spring:message code="msg.common.field.relatedDocuments" /></th>
						<td class="dCase uploadCase documentsUpload">
							<span class="fileex_back">
								<div id="documentsFile" <c:if test="${isCommited or detail.documentsFileCnt eq '5'}">style="display:none;"</c:if>>+ Add file</div>
							</span>
							<ul class="documentsFileArea">
							<c:forEach var="documentsFile" items="${detail.documentsFile }" varStatus="vs">
								<c:if test="${documentsFile.delYn == 'N'}">
									<li>
										<a href="<c:url value="/download/${documentsFile.fileSeq}/${documentsFile.logiNm}"/>">${documentsFile.origNm }</a>
										<span style="margin-left:20px;">${documentsFile.createdDate}</span>
										<span>
											<input type="button" value="Delete" class="smallDelete" onclick="fn.deleteDocumentsFile(this)" style="vertical-align:super;margin-left: 5px;<c:if test="${isCommited}">display:none;</c:if>"/>
										</span>
										<input type="hidden" value="${documentsFile.fileSeq }"/>
									</li>
								</c:if>
							</c:forEach>
							</ul>
							<div class="mt10" style="margin-left:10px;<c:if test="${isCommited}">display:none;</c:if>"><b>The maximum number of files that can be uploaded is 5.</b></div>
							<input type="hidden" id="documentsFileId" name="documentsFileId" value="${detail.documentsFileId}" />
						</td>
					</tr>
					<c:if test="${detail.viewOnlyFlag ne 'Y'}">
					<c:if test="${not empty prjList}">
						<tr>
							<th class="dCase"><spring:message code="msg.common.field.project" /><br/><input id="listMore" type="button" value="List more" class="btnCLight gray" /></th>
							<td class="dCase">
								<table id="_projectList"><tr><td></td></tr></table>
							</td>
						</tr>
					</c:if>
					</c:if>
					<tr>
						<th class="dCase"><spring:message code="msg.common.field.watcher" /></th>
						<td class="dCase watchCase">
							<c:if test="${detail.viewOnlyFlag ne 'Y'}">
							<div class="pb5">
								<span class="selectSet w150">
									<strong for="userDivision" title="selected value">Select Division</strong>
									<select id="userDivision" onchange="fn.selectDivision()">
										<option value="">Select Division</option>
										${ct:genOption(ct:getConstDef("CD_USER_DIVISION"))}
									</select>
								</span>
								<span class="selectSet w350">
									<strong for="userName" title="selected value">Select User</strong>
									<select id="userName">
									</select>
								</span>
								<input type="button" value="+ Add" id="addWatcher" class="btnCLight gray" />
							</div>
							<div class="pb5">
								<span><input type="text" id="adId" name="adId" style="width:150px" placeholder="Input email" onKeypress="fn.CheckChar()" /></span>
								<c:set var="useDomainFlag" value="${ct:genOption(ct:getConstDef('CD_REGIST_DOMAIN'))}" />
								<c:choose>
									<c:when test="${not empty useDomainFlag}">
										<span class="selectSet w220">
											<strong for="domain" title="Watcher domain selected value">${ct:getCodeExpString(ct:getConstDef('CD_REGIST_DOMAIN'), ct:getConstDef('CD_DTL_DEFAULT_DOMAIN'))}</strong>
											<select id="domain" name="domain">
												${ct:genOption(ct:getConstDef("CD_REGIST_DOMAIN"))}
											</select>
										</span>
										<input type="text" id="emailTemp" class="w220" style="display:none;" value="${ct:getCodeExpString(ct:getConstDef('CD_REGIST_DOMAIN'), ct:getConstDef('CD_DTL_DEFAULT_DOMAIN'))}" onKeypress="fn.CheckChar()"  placeholder="Input your Email Domain" />
									</c:when>
									<c:otherwise>
										<span class="pd5">@</span>
										<input type="text" id="emailTemp" style="width:326px !important" value="" onKeypress="fn.CheckChar()"  placeholder="Input your Email Domain" />
									</c:otherwise>
								</c:choose>
								<input id="addEmail" type="button" value="+ Add" class="btnCLight gray" />
							</div>
							<div class="pb5">
								<span class="selectSet w150">
									<strong for="listKind" title="selected value">Select List</strong>
									<select id="listKind">
										<option value="">Select List</option>
										<c:if test="${projectFlag}">
											<option value="prj">Project List</option>
										</c:if>
										<option value="par">3rd Party List</option>
										<c:if test="${batFlag}">
											<option value="bat">BAT List</option>
										</c:if>
									</select>
								</span>
								<span><input type="text" id="listId" name="listId" style="width:350px" placeholder="Input ID you want to copy"/></span>
								<input id="addList" type="button" value="+ Add" class="btnCLight gray" />
							</div>
							</c:if>
							<div class="multiTxtSet2" id="nameSpace">
							<c:forEach var="watcher" items="${detail.partnerWatcher }" varStatus="status">
								<span>
									<c:if test="${not empty watcher.userId}">
									<input class="watcherTags" type="hidden" name="watchers" value='<c:if test="${ct:getConstDef('CD_USER_DIVISION_EMPTY') ne watcher.division}">${watcher.division}/</c:if>${watcher.userId}' />
									<strong><c:if test="${ct:getConstDef('CD_USER_DIVISION_EMPTY') ne watcher.division}"><b <c:if test="${watcher.deptUseYn eq 'N'}">class="deleteUser"</c:if>>${ct:getCodeString(ct:getConstDef("CD_USER_DIVISION"),watcher.division)}</b>/</c:if><b <c:if test="${watcher.userUseYn eq 'N'}">class="deleteUser"</c:if>>${empty watcher.userName ? watcher.userId : watcher.userName}</b></strong>
									<c:if test="${detail.viewOnlyFlag ne 'Y'}">
									<input type="button" value="Delete" class="smallDelete" onclick="fn.removeWatcher('${watcher.division}','${watcher.userId}');"/>
									</c:if>
									</c:if>
									<c:if test="${empty watcher.userId}">
									<input class="watcherTags" type="hidden" name="watchers" value='${watcher.email}/Email' />
									<strong>${watcher.email}</strong>
									<c:if test="${detail.viewOnlyFlag ne 'Y'}">
									<input type="button" value="Delete" class="smallDelete" onclick="fn.removeWatcher('${watcher.email}','Email');"/>
									</c:if>
									</c:if>
								</span>
							</c:forEach>
							</div>
						</td>
					</tr>
						<c:if test="${not empty detail.partnerId}">
						<tr>
							<th class="dCase txStr"><spring:message code="msg.common.field.creator" /></th>
							<td class="dCase">
								<div class="required">
									<input type="text" name="creatorNm" class="autoComCreatorDivision w600" value="" ${(ct:isAdmin() and detail.status ne 'CONF') ? '' : 'disabled="disabled"'} />
									<input type="hidden" name="creator" <c:if test="${not empty detail }">value='${detail.creator}'</c:if>/>
									<span class="retxt">This field is required.</span>
								</div>
							</td>
						</tr>
						<tr>
							<th class="dCase txStr"><spring:message code="msg.common.field.division" /></th>
							<td class="dCase">
								<div class="pb5">
									<span class="selectSet w600">
										<strong for="division" title="Watcher part selected value">Select Division</strong>
										<select id="division" name="division" ${ct:isAdmin() ? '' : 'disabled="disabled"'} >
											<option value=""></option>
											${ct:genOptionSelected(ct:getConstDef('CD_USER_DIVISION'), detail.division)}
										</select>
									</span>
								</div>
							</td>
						</tr>
                        <tr>
                            <th class="dCase  txStr"><spring:message code="msg.common.field.reviewer" /></th>
                            <td class="dCase">
                                <div class="required">
                                    <input type="text" name="reviewerName" class="w600" value="${detail.reviewerName}" disabled="disabled"/>
                                </div>
                            </td>
                        </tr>
                        </c:if>
					</tbody>
				</table>
				</form>
			</div>
		</div>
		<div class="boxLine mt10" style="display:none;">
			<div class="fileupload-progress">
				<!-- The global progress bar -->
				<div class="progress mt10" role="progressbar" aria-valuemin="0" aria-valuemax="100"></div>
				<!-- The extended global progress state -->
				<div class="progress-extended mt10">&nbsp;</div>
			</div>
		</div>
		<!---->
		<div class="btnLayout">
            <span class="right">
				<input id="copyUrl" type="text" style="width:1px; height:1px; margin:0; padding:0; border: 0;">
				<c:if test="${not empty detail.partnerId}">
					<input type="button" value="Share URL" class="btnColor red" onclick="fn.shareUrl();" />
				</c:if>
                <c:if test="${not empty detail.partnerId and detail.viewOnlyFlag ne 'Y'}">
                    <input type="button" value="Export" class="btnColor red btnExport" onclick="fn.downloadExcel()"/>
					<input type="button" value="Yaml" class="btnColor red btnExport" onclick="fn.downloadYaml()"/
                </c:if>
                <c:if test="${detail.status ne 'REQ' and detail.status ne 'CONF' and  (detail.loginUserRole eq 'ROLE_ADMIN'  or (detail.loginUserRole ne 'ROLE_ADMIN' and detail.status ne 'REV')) and detail.viewOnlyFlag ne 'Y'}">
                    <input type="button" value="Check OSS Name" onclick="fn.CheckOssViewPage('PARTNER')" class="btnColor red srcBtn" style="width: 115px;" />
                    <input type="button" value="Check License" onclick="fn.CheckOssLicenseViewPage('PARTNER')" class="btnColor red srcBtn" style="width: 100px;" />
                    <input id="partyReset" type="button" value="Reset" class="btnColor" onclick="fn.reset()"/>
                    <input id="partySave" type="button" value="Save" onclick="fn.save()" class="btnColor red" />
                </c:if>
            </span>
            <span class="left">
	            <c:if test="${not empty detail and detail.status ne 'REQ' and detail.status ne 'CONF' and (detail.loginUserRole eq 'ROLE_ADMIN'  or (detail.loginUserRole ne 'ROLE_ADMIN' and detail.status ne 'REV')) and detail.viewOnlyFlag ne 'Y'}">
	                <input id="partyDelete" type="button" value="Delete" class="btnColor red" onclick="fn.delete()"/>
	                <input id="partyBulkEdit" type="button" value="Bulk Edit" class="btnColor red" onclick="fn.bulkEdit()"/>
	            </c:if>
            </span>
        </div>
		<div class="jqGridSet list2">
			<table id="list"><tr><td></td></tr></table>
			<div id="pager"></div>
		</div>
	</div>
	<!---->
	<!-- BAT ************************************************************************************************************ -->
	<c:if test="${batFlag}">
		<div id="batDiv" class="tabContent">
			<div class="projectContents">
				<!---->
				<div class="orangeBox">
					<input type="button" value="btnToggle" class="btnToggle">
					<fieldset class="editSearchUp">
						<form>
							<div class="uploadBox batFileUpload batBtn">
								<dl class="uploadCase">
									<dt>Upload Analysis Result</dt>
									<dd>
										<div class="basicCase">
											<div class="uploadSet">
												<span class="fileex_back">
													<span><input type="radio" id="1" name="batselectOption" onchange="bat_fn.changeSelectOption()" value="1" checked /><label for="1">Upload </label></span>
													<div id="binaryFile">+ Add file</div>
													<input type="hidden" id="binaryFileId" value="${project.srcCsvFileId }"> 
													<span><input type="radio" id="2" name="batselectOption" onchange="bat_fn.changeSelectOption()" value="2" /><label for="2">URL </label></span>
													<div id="wgetUrl" style="width: 500px;"><input type="text" class="autoComConfParty" style="width:70%" id="sendWgetUrl" name="sendWgetUrl"/><input id="send" type="button" value="send" class="btnColor" /></div>  
												</span>
											</div>
										</div>
									</dd>
								</dl>
								<div class="projectSearch">
									<div class="jqGridSet firstResult">
										<table id="_binaryFileList"><tr><td></td></tr></table>
									</div>
								</div>
								<div><spring:message code="msg.partner.notice"/></div>
								<div>※<spring:message code="msg.partner.notice.term"/></div>
							</div>
						</form>
					</fieldset>
				</div>
				<!---->
				<div class="btnLayout">
	                   <span class="right"></span>
	               </div>
				<div class="jqGridSet batBtn">
					<table id="batList"><tr><td></td></tr></table>
					<div id="batPager"></div>
				</div>
				<!---->
				<div class="btnLayout">
					<span class="right"></span>
				</div>
				<!---->
			</div>
		</div>
	</c:if>
</div>
<div class="pop sheetSelectPop">
	<h1><input type="checkbox" onchange="checkAll('sheetSelectPop', this)" class="sheetNum"> Select Sheet</h1>
	<div class="popdata">
		<ol class="sheetNameArea">
			<li>
				<input type="checkbox" value="0" id="sheet0" class="sheetNum">
				<label for="sheet0">sheet 1</label>
			</li>
			<li>
				<input type="checkbox" value="1" id="sheet1" class="sheetNum">
				<label for="sheet1">sheet 2</label>
			</li>
		</ol>
	</div>
	<div class="pbtn">
		<input type="button" value="Cancel" class="btnCancel btnColor" onclick="fn.closePop()">
		<input type="button" value="OK" class="btnColor red" onclick="fn.getSheetData()">
	</div>
</div>
</div>
<div id="blind_wrap"></div>
<dl name="commentClone">
	<dt>
		<span class="left">
			<strong class="nameArea"></strong> | <span class="dateArea"></span>
		</span>
		<span class="right">
			<input type="button" value="editModify" class="editModify" onclick="fn.modifyComment(this)"/>
			<input type="button" value="editDelete" class="editDelete" onclick="fn.deleteComment(this)"/>
			<input type="hidden" name="commId"/> 
		</span>
	</dt>
	<dd class="commentContentsArea"></dd>
</dl>
<div class="pop commModifyPop">
	<h1>Comment</h1>
	<div class="popdata">
		<div class="grid-container">
			<div class="grid-width-100">
				<div id="editor3">
					<h1>Hello FOSSLight Hub</h1>
				</div>
			</div>
		</div>
		<script>
			initSample3();
		</script>
	</div>
	<div class="pbtn">
		<input type="button" value="Cancel" class="btnCancel btnColor closeModComment" />
		<input type="button" value="OK" class="btnColor red modifyComment" />
	</div>
</div>