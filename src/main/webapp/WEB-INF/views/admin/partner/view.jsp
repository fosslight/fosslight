<%@ page contentType="text/html; charset=utf-8" pageEncoding="utf-8"%>
<%@ include file="/WEB-INF/constants.jsp"%>
<jsp:include page="../common/gridCommonFn.jsp" flush="false" />
<!-- wrap -->
<c:set var="isCommited" value="${detail.status eq 'CONF'}"/>
<div id="wrapIframe">
<c:if test="${detail.viewOnlyFlag eq 'Y'}">

	<c:if test="${empty message}">
		<!---->
		<div class="projdecTop" style="height:auto;">
			<div class="projdecTab" style="height:auto;">
				<div class="subTab" style="padding:0px;">
					<div class="tabMenu">
						<a rel="partyDiv">3rd party</a>
	<%--
						<c:if test="${batFlag}">
							<a rel="batDiv">BAT</a>
						</c:if>
	--%>
					</div>
				</div>
				<c:if test="${ct:isAdmin() or detail.viewOnlyFlag ne 'Y'}">
					<c:if test="${not empty detail}">
						<span style="right: 0px; position: absolute; bottom: -23px; font-size: 11px;" >
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
								<th class="dCase txStr">3rd Party Name</th>
								<td class="dCase">${detail.partnerName}</td>
							</tr>
							<tr>
								<th class="dCase txStr">3rd Party Software Name</th>
								<td class="dCase">${detail.softwareName}</td>
							</tr>
							<tr>
								<th class="dCase">3rd Party Software Version</th>
								<td class="dCase">${detail.softwareVersion}</td>
							</tr>
							<tr>
								<th class="dCase">3rd Party Software Status</th>
								<td class="dCase"><input type="button" id="partnerStatus" class="w150 mr5"</td>
							</tr>
							<tr>
								<th class="dCase">Delivery Form</th>
								<td class="dCase">
									<p class="pd5">If you exist in binary form 3rd party software, You can check opensource information using the <a href="#" class="txBlueIt">Binary Analysis</a></p>
									<c:forEach var="code" items="${ct:getCodeValues(ct:getConstDef('CD_PARTNER_DELIVERY_FORM'))}" varStatus="status">
										<c:if test="${code[0] eq detail.deliveryForm}">${code[1]}</c:if>
									</c:forEach>
								</td>
							</tr>
							<c:if test="${project.viewOnlyFlag ne 'Y'}">
							<tr>
								<th class="dCase">Permission</th>
								<td class="dCase">
									<span>View : ${(not empty detail && detail.publicYn ne 'N') || empty detail ? 'EveryOne' : 'Creator & Watcher'}</span><br>
									<span>Edit : Creator & Watcher only</span>
								</td>
							</tr>
							</c:if>
							<tr>
								<th class="dCase">Description</th>
								<td class="dCase">${detail.description}</td>
							</tr>
							<tr>
								<th class="dCase">Open Source Agreement<br/><c:if test="${checkFlag}"><a href="javascript:void(0);" class="sampleDown" onclick="fn.sampleDownload('arg')"><span>Sample</span></a></c:if></th>
								<td class="dCase uploadCase confirmationUpload">
									<c:if test="${not empty confirmationFile}">
										<c:choose>
											<c:when test="${detail.viewOnlyFlag eq 'Y'}">${confirmationFile.origNm} ${confirmationFile.createdDate}</c:when>
											<c:otherwise>
												<a href="<c:url value="/download/${confirmationFile.fileSeq }/${confirmationFile.logiNm}"/>">${confirmationFile.origNm }</a>
												<span style="margin-left:20px;">${confirmationFile.createdDate}</span>
											</c:otherwise>
										</c:choose>
										<input type="hidden" id="confirmationFileId" name="confirmationFileId" value="${confirmationFile.fileSeq}"/>
									</c:if>
								</td>
							</tr>
							<tr>
								<th class="dCase">OSS Checklist (Open Source List)<br/><a href="javascript:void(0);" class="sampleDown" onclick="fn.sampleDownload('chk')"><span>Sample</span></a></th>
								<td class="dCase uploadCase ossUpload">
									<c:if test="${not empty ossFile}">
										<c:choose>
											<c:when test="${detail.viewOnlyFlag eq 'Y'}">${ossFile.origNm} ${ossFile.createdDate}</c:when>
											<c:otherwise>
												<a href="<c:url value="/download/${ossFile.fileSeq }/${ossFile.logiNm}"/>">${ossFile.origNm }</a>
												<span style="margin-left:20px;">${ossFile.createdDate}</span>
											</c:otherwise>
										</c:choose>
										<input type="hidden" id="ossFileId" name="ossFileId" value="${ossFile.fileSeq}"/>
									</c:if>
								</td>
							</tr>
						</tr>
						<tr>
							<th class="dCase">Related Documents</th>
							<td class="dCase uploadCase documentsUpload">
								<ul class="documentsFileArea">
								<c:forEach var="documentsFile" items="${detail.documentsFile }" varStatus="vs">
									<c:if test="${documentsFile.delYn == 'N'}">
										<li>
											<c:choose>
												<c:when test="${detail.viewOnlyFlag eq 'Y'}">${documentsFile.origNm} ${documentsFile.createdDate}</c:when>
												<c:otherwise>
													<a href="<c:url value="/download/${documentsFile.fileSeq}/${documentsFile.logiNm}"/>">${documentsFile.origNm}</a>
													<span style="margin-left:20px;">${documentsFile.createdDate}</span>
												</c:otherwise>
											</c:choose>
											
											<input type="hidden" value="${documentsFile.fileSeq }"/>
										</li>
									</c:if>
								</c:forEach>
								</ul>
								<div class="mt10" style="margin-left:10px;<c:if test="${isCommited}">display:none;</c:if>"><b>The maximum number of files that can be uploaded is 5.</b></div>
								<input type="hidden" id="documentsFileId" name="documentsFileId" value="${detail.documentsFileId}" />
							</td>
						</tr>
						<tr>
							<th class="dCase">Watcher</th>
							<td class="dCase watchCase">
								<div class="multiTxtSet2" id="nameSpace">
								<c:forEach var="watcher" items="${detail.partnerWatcher }" varStatus="status">
									<span>
										<c:if test="${not empty watcher.userId}">
										<input class="watcherTags" type="hidden" name="watchers" value='<c:if test="${ct:getConstDef('CD_USER_DIVISION_EMPTY') ne watcher.division}">${watcher.division}/</c:if>${watcher.userId}' />
										<strong><c:if test="${ct:getConstDef('CD_USER_DIVISION_EMPTY') ne watcher.division}"><b <c:if test="${watcher.deptUseYn eq 'N'}">class="deleteUser"</c:if>>${ct:getCodeString(ct:getConstDef("CD_USER_DIVISION"),watcher.division)}</b>/</c:if><b <c:if test="${watcher.userUseYn eq 'N'}">class="deleteUser"</c:if>>${empty watcher.userName ? watcher.userId : watcher.userName}</b></strong>
										</c:if>
										<c:if test="${empty watcher.userId}">
										<input class="watcherTags" type="hidden" name="watchers" value='${watcher.email}/Email' />
										<strong>${watcher.email}</strong>
										</c:if>
									</span>
								</c:forEach>
								</div>
							</td>
						</tr>
							<tr>
								<th class="dCase txStr">Creator</th>
								<td class="dCase">${detail.creatorName}</td>
							</tr>
							<tr>
								<th class="dCase  txStr">Division</th>
								<td class="dCase">
									<c:forEach items="${ct:getCodeValues(ct:getConstDef('CD_USER_DIVISION'))}" var="code" varStatus="status">
										<c:if test="${code[0] eq detail.division}">${code[1]}</c:if>
									</c:forEach>
								</td>
							</tr>
	                        <tr>
	                            <th class="dCase  txStr">Reviewer</th>
	                            <td class="dCase">${detail.reviewerName}</td>
	                        </tr>
						</tbody>
					</table>
					</form>
				</div>
			</div>
			<!---->
			<c:if test="${detail.viewOnlyFlag eq 'N'}">
				<div class="jqGridSet list2 mt20">
					<table id="list"><tr><td></td></tr></table>
					<div id="pager"></div>
				</div>
			</c:if>
			<!---->
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
									<div>Binary analysis results are used only as reference data. Until the accuracy of the tool is improved, it is not included in the BOM and OSS Notice</div>
									<div>※ Uploaded firmware is only kept for one month.</div>
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
</c:if>
<c:if test="${not empty message}">
	${message}
</c:if>
</c:if>
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

