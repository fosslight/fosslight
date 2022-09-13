<%@ page contentType="text/html; charset=utf-8" pageEncoding="utf-8"%>
<%@ include file="/WEB-INF/constants.jsp"%>
<script type="text/javascript" src="${ctxPath}/js/tutorial/tutorial-packaging.js?${jsVersion}"></script>
<!-- wrap -->
<c:set var="isCommited" value="${project.verificationStatus eq 'CONF'}"/>
<div id="wrapIframe">
	<!---->
	<div class="projdecTop">
		<div class="projectInfo">
			<h2>Project Information</h2>
			<ul>
				<li class="first"><span>Project Name</span><strong>${project.prjName }
					<span id="editTab" class="btnIcon basic" style="display:inline-block;width:16px;padding:0;margin-left:3px;">Basic Info</span>
					<c:if test="${not empty project.identificationStatus}">
					<span id="identificationTab" class="btnIcon identi" style="display:inline-block;width:16px;padding:0;margin-left:3px;">Identification</span>
					</c:if>
					<c:if test="${distributionFlag and project.destributionStatus ne 'NA' and (not empty project.destributionStatus or project.verificationStatus eq 'CONF')}">
					<span id="distributionTab" class="btnIcon distr" style="display:inline-block;width:16px;padding:0;margin-left:3px;">Distribution</span>
					</c:if></strong>
				</li>
				<li><span>Created</span><strong>${project.prjUserName }&nbsp;${project.prjDivisionName } (${ct:formatDateSimple(project.createdDate)})</strong></li>
			</ul>
			<a class="right" id="helpLink" style="position:relative; cursor: pointer; top:-37px; right:-75px; display: none;"><img alt="" src="<c:url value="${ctxPath}/images/user-guide.png"/>" /></a>
		</div>
		<!---->
		<div class="projdecTab">
			<div class="subTab">
			<div class="tabMenu">
				<a rel="packaging">Packaging</a>
				<a id="tutorial_28" rel="notice">Notice</a>
			</div>
			</div>
		  	<c:if test="${ct:isAdmin()}">
				<div style="position:absolute;right:160px;top:28px;" id="div_approve">
					<input type="checkbox" id="approve" name="approve" value="Y" style="margin-right:5px;"/>Approve without verification
				</div>
			</c:if>
			<div class="projdecBtn" style="display: none;">

				<c:choose>
					<c:when test="${not ct:isAdmin() and (project.completeYn eq 'Y' or project.verificationStatus eq 'NA' or project.distributeDeployYn eq 'Y' or project.verificationStatus eq 'REV')}">
						<input type="button" id="btnRejectNotice" value="Reject" class="gray btnColor" title="If you need modify, please leave a comment on FOSSLight team." onclick="return false;"/>
					</c:when>
					<c:when test="${project.completeYn ne 'Y' and project.dropYn ne 'Y' and project.verificationStatus ne 'NA' and project.distributeDeployYn ne 'Y'}">
						<a class="btnSet confirm"><span id="verConfirm">Confirm</span></a>
						<a class="btnSet reject"><span id="verReject">Reject</span></a>
						<a class="btnSet review"><span id="verRequest">Request</span></a>
						<a class="btnSet restart"><span id="verReviewStart">Review Start</span></a>
					</c:when>
					<c:otherwise>
					</c:otherwise>
				</c:choose>
			</div>
			<input type="button" value="Comment Edit" class="btnColor commentBtn" />
			<span style="right: 125px; position: absolute; bottom: -23px; font-size: 11px;" >
				<input type="button" value="Show Comment History" class="btnColor purple btnCommentHistory" style="width: 160px; height: 18px;"/>
			</span>
		</div>
	</div>
	<div class="commentEditor" style="display:none;">
		<div class="cBtn">
		<input type="button" value="Save & Send comment" class="btnCLight saveEditor" onclick="fn.sendEditor('WR');"/>
		<input type="button" value="Save draft" class="btnCLight" onclick="fn.saveEditor();"/>
		</div>
		<div class="grid-container">
			<div class="grid-width-100">
				<div id="editor">${project.userComment}</div>
			</div>
		</div>
	</div>
	<div>
		<div id="packaging" class="tabContent">
			<div class="projectContents">
				<div class="btnLayout" style="padding:5px 0 !important">
					<span><b>Upload OSS Package</b></span>
					<span style="padding-left:30px;"><input type="checkbox" id="autoVerify" name="autoVerify" checked="checked"/><span style="padding-left:3px;"><b>Verify when file is uploaded</b></span></span>
				</div>
				<!-- 161116 upload 추가 -->
				<div class="multiSet">
					<c:forEach var="i" begin="1" end="3" step="1" varStatus="status">
						<div id="fileUploader_${i}">
						<fieldset class="editSearchUp grayBox singleLine mt10">
							<form>				
								<dl class="uploadCase">
									<dt>Upload Analysis Result</dt>
									<dd>
										<div class="basicCase">
											<div class="uploadTit">
												<span class="checkSet"><label for="2">Package File ${i}</label></span>	
											</div>
											<div class="uploadSet">
									<c:if test="${not empty files }">
										<c:forEach items="${files}" var="file" varStatus="stat">
											<c:if test="${stat.count == i}">
												<span class="fileex_back verifyFile_${i}"<c:if test="${not empty file }">style="display:none;"</c:if>>
											</c:if>
										</c:forEach>
									</c:if>
												<div id="tutorial_26">
													<span><input type="radio" id="1" name="selectOption_${i}" onchange="fn.changeSelectOption(this)" value="1" checked  <c:if test="${isCommited}">disabled</c:if> /><label for="1">Upload </label></span>
													<div id="registFile_${i}" <c:if test="${isCommited}">style="display:none;"</c:if>>upload</div>
												</div>
													<span><input type="radio" id="2" name="selectOption_${i}" onchange="fn.changeSelectOption(this)" value="2" <c:if test="${isCommited}">disabled</c:if> /><label for="2">URL </label></span>
													<div id="wgetUrl_${i}" style="width: 500px;"><input type="text" class="autoComConfParty" style="width:70%" id="sendWgetUrl_${i}" name="sendWgetUrl_${i}"/><input id="send_${i}" type="button" value="send" class="btnColor" /></div>
													<div class="mt10">
														<span><input type="radio" id="3" name="selectOption_${i}" onchange="fn.changeSelectOption(this)" value="3" <c:if test="${isCommited}">disabled</c:if> /><label for="3">Project Search </label></span>
													</div>
													<div id="projectSearch_${i}" style="width: 770px;">
														<span style="padding-right:10px;">Project Name/<br>Model/Creator</span>
														<input type="text" style="width:612px" class="verifyProjectSearch"/>
														<input type="button" value="Search" class="btnColor black wauto partyBtn verifyProjectSearch" style="right:0;" id="verificationProjectSearch_${i}" onclick="fn.reuseSearch(this)"/>
														<h3 class="mt10">Search Results</h3>
														<div class="jqGridSet mt10">
															<table id="projectList_${i}"><tr><td></td></tr></table>
															<div id="pager_${i}"></div>
														</div>
														<div id="packaging_${i}" style="display:none;">
															<h3 class="mt10">detail preview</h3>
															<div class="jqGridSet mt10">
																<table id="packagingList_${i}"><tr><td></td></tr></table>
															</div>
														</div>
													</div>
												</span>
												<div class="uploadList_${i}">
													<ul>
														<c:if test="${not empty files }">
															<c:forEach items="${files}" var="file" varStatus="stat2">
																<c:if test="${stat2.count == i}">
																	<input type="hidden" name="fileSeq_${i}" value="${file.fileSeq}" />
																	<c:if test="${not empty file }">
																		<c:choose>
																			<c:when test="${empty project.verificationStatus or project.verificationStatus eq '' or project.verificationStatus eq 'PROG' or (ct:isAdmin() and project.verificationStatus eq 'REV')}">
																				<li>
																					<span>
																						<strong><a class="urlLink" href="<c:url value="/download/${file.fileSeq}/${file.logiNm}"/>">${file.origNm}<span style="margin-left:20px;">${file.createdDate}</span></a></strong>
																						<input type="button" value="Delete" class="smallDelete" onclick="fn.deleteFile(this,'${file.fileSeq}', '${i}' )">
																					</span>
																				</li>
																			</c:when>
																			<c:otherwise>
																				<li>
																					<span>
																						<strong><a class="urlLink" href="<c:url value="/download/${file.fileSeq}/${file.logiNm}"/>">${file.origNm}<span style="margin-left:20px;">${file.createdDate}</span></a></strong>
																					</span>
																				</li>
																			</c:otherwise>
																		</c:choose>
																	</c:if>
																</c:if>
															</c:forEach>
														</c:if>
													</ul>
												</div>
											</div>
										</div>
										
										<c:if test="${(empty project.verificationStatus or project.verificationStatus eq 'PROG' or (ct:isAdmin() and project.verificationStatus eq 'REV' )) and project.viewOnlyFlag eq 'N'}">
											<div class="basicCase mt10">
												<div class="uploadTit">
													<span class="checkSet"></span>	
												</div>
												<div class="uploadSet" id="fileUplWarnMessage_${i}" style="display:none;">
													<span>* The maximum file size for uploading is 4GB.</span>
												</div>
											</div>
										</c:if>
										<span class="right" id="BtnSetDynamicCreate_${i}">
											<c:if test="${!isCommited}">
												<c:if test="${i < 3}">
													<input id="uploadAdd_${i}" type="button" value="+ Add" class="btnCLight gray" style="display:none;"/>
												</c:if>
												<input id="uploadRemove_${i}" type="button" value="- Remove" class="btnCLight gray" style="display:none;"/>
											</c:if>
										</span>
									</dd>
								</dl>
							</form>
						</fieldset>
						</div>
					</c:forEach>
				</div>
				<div class="btnLayout">
					<div id="changePathPop" class="pop changePathPop" style="position:fixed;top:40%;left:20%;">
						<fieldset class="editSearchUp grayBox singleLine mt10" >
							<form>
								<dl class="uploadCase">
									<dt>Upload Analysis Result</dt>
									<dd>
										<div class="basicCase">
											<div class="uploadTit">
												<span class="checkSet"><label for="2">Change Path</label></span>	
											</div>
											<div class="uploadSet">
												<span class="fileex_back" style="">
													<div id="verificationFile"><div class="ajax-upload-dragdrop" style="vertical-align: top; width: 400px;"><div class="ajax-file-upload" style="position: relative; overflow: hidden; cursor: default;">Upload<form method="POST" action="<c:url value="/project/verification/registFile?prjId=120"/>" enctype="multipart/form-data" style="margin: 0px; padding: 0px;"><input type="file" id="ajax-upload-id-1479276429067" name="myfile" accept="*" style="position: absolute; cursor: pointer; top: 0px; width: 100%; height: 100%; left: 0px; z-index: 100; opacity: 0;"></form></div><span><b>Drag &amp; Drop Files</b></span></div><div></div></div><div class="ajax-file-upload-container"></div><div class="ajax-file-upload-container"></div>
												</span>
											</div>
										</div>
									</dd>
								</dl>
							</form>
						</fieldset>
						<span class="right">
							<input id="btnChangePathCancel" type="button" value="cancel" class="btnColor tright" style="margin:10px 0;">
						</span>
					</div>
				</div>
				<!---->
				<div class="btnLayout mt20" style="padding:5px 0 !important">
					<span style="padding:0 20px 0 0"><b>Verify OSS Package</b></span><span>* Enter the path info of source code for each OSS and click the Verify button</span>
					<span class="right">
						<input name="export_path" type="button" value="Export Path" class="btnColor" style="width:90px;"/>
						<input name="upload_path" type="button" value="Upload Path" class="btnColor" style="width:90px;" />
						<c:if test="${project.dropYn ne 'Y'}">
							<input name="btnSavePath" type="button" value="Save" class="btnColor red" />
							<c:if test="${(empty project.verificationStatus or project.verificationStatus eq 'PROG' or (ct:isAdmin() and project.verificationStatus eq 'REV' )) and project.viewOnlyFlag eq 'N'}">
								<input id="tutorial_27" name="verify" type="button" value="Verify" class="btnColor red" />
							</c:if>
						</c:if>
					</span>
				</div>
				<div class="jqGridSet">
					<table id="list"><tr><td></td></tr></table>
					<div id="pager"></div>
				</div>
				<div class="btnLayout" style="padding:5px 0 !important">
					<c:set var="verifyFlag">style="display: none;"</c:set>
					<c:if test="${project.statusVerifyYn ne 'N'}"><c:set var="verifyFlag" /></c:if>
					<input type="hidden" id="verifyFlag" value="${project.statusVerifyYn}"/>
					<input type="hidden" id="readmeFile" value="${project.readmeFileName}" />
					<input type="hidden" id="exceptFile" value="${ct:getConstDef('PACKAGING_VERIFY_FILENAME_PROPRIETARY')}" />
					<input type="hidden" id="verifyFile" value="${ct:getConstDef('PACKAGING_VERIFY_FILENAME_FILE_LIST')}" />
					<span id="verifyBtnSet" ${verifyFlag}>
						<input id="btnReadme" type="button" value="README" class="btnColor <c:if test="${not empty project.readmeFileName}">green</c:if>" />
						<input id="btnVerifyFileContent" type="button" value="File List" class="btnColor <c:if test="${not empty project.verifyFileContent}">green</c:if>" style="width:90px;" />
						<input id="btnProprietary" type="button" value="Banned List" class="btnColor <c:if test="${not empty project.exceptFileContent}">green</c:if>" style="width:90px;" />
					</span>
				</div>
			</div>
			<!---->
		</div>
		<!-- 다운로드 허용 플래그 -->
		<c:set var="isAllowDownload" value="${project.allowDownloadBitFlag > 0}"/>
		<c:set var="isProgOrConfStat" value="${project.verificationStatus eq 'REV' or project.verificationStatus eq 'CONF'}"/>
		<div id="notice" class="tabContent">
			<div class="projectContents">
				<div class="btnLayout2 w1025 mt20">
					<span><input type="radio" id="r1" name="btnEditOssNotice" value="N" <c:if test="${ossNotice.editNoticeYn eq 'N'}">checked</c:if> <c:if test="${project.verificationStatus eq 'CONF'}"> disabled</c:if> /><label for="r1"><strong>Request to generate a default OSS Notice. (Select this in most cases.)</strong></label></span>
				</div>
				<div class="btnLayout2 w1025 mt10">
					<span id="tutorial_29"><input type="radio" id="r2" name="btnEditOssNotice" value="Y" <c:if test="${ossNotice.editNoticeYn eq 'Y'}">checked</c:if> <c:if test="${project.verificationStatus eq 'CONF'}"> disabled</c:if>/><label for="r2"><strong>Request to generate a modified OSS Notice. (Select this only in exceptional cases.)</strong></label></span>
				</div>
				<div class="boxLine mt10">
					<div class="noticeEdit2">
						<div class="nEKind">
							<form id="noticeForm">
								<input type="hidden" id="editNoticeYn" name="editNoticeYn" value="${ossNotice.editNoticeYn}"/>
								<input type="hidden" id="editCompanyYn" name="editCompanyYn" value="${ossNotice.editCompanyYn}"/>
								<input type="hidden" id="editDistributionSiteUrlYn" name="editDistributionSiteUrlYn" value="${ossNotice.editDistributionSiteUrlYn}"/>
								<input type="hidden" id="editEmailYn" name="editEmailYn" value="${ossNotice.editEmailYn}"/>
								<input type="hidden" id="hideOssVersionYn" name="hideOssVersionYn" value="${ossNotice.hideOssVersionYn}"/>
								<input type="hidden" id="editAppendedYn" name="editAppendedYn" value="${ossNotice.editAppendedYn}"/>
								<input type="hidden" name="prjId" value="${project.prjId}"/>
								<input type="hidden" id="useCustomNoticeYn" name="useCustomNoticeYn" value="${project.useCustomNoticeYn}"/>
								<input type="hidden" id="noticeHtml" name="noticeHtml" value=""/>
								<input type="hidden" name="noticeType" value="${project.noticeType}" />
								<input type="hidden" name="packageJson"/>
								<input type="hidden" name="packageFileId" id="packageFileId"/>
								<input type="hidden" name="userComment" />
								<input type="hidden" name="withoutVerifyYn" value="${project.withoutVerifyYn}"/>
								<input type="hidden" name="ignoreBinaryDbFlag" value=""/>
								<input type="hidden" id="appended" name="appended" value="" />
								<input type="hidden" id="appendedTEXT" name="appendedTEXT" value="" />
								<!-- 다운로드 허용 플래그 -->
								<input type="hidden" id="allowDownloadNoticeHTMLYn" name="allowDownloadNoticeHTMLYn" value="${project.allowDownloadNoticeHTMLYn}" />
								<input type="hidden" id="allowDownloadNoticeTextYn" name="allowDownloadNoticeTextYn" value="${project.allowDownloadNoticeTextYn}" />
								<input type="hidden" id="allowDownloadSimpleHTMLYn" name="allowDownloadSimpleHTMLYn" value="${project.allowDownloadSimpleHTMLYn}" />
								<input type="hidden" id="allowDownloadSimpleTextYn" name="allowDownloadSimpleTextYn" value="${project.allowDownloadSimpleTextYn}" />
								<input type="hidden" id="allowDownloadSPDXSheetYn" name="allowDownloadSPDXSheetYn" value="${project.allowDownloadSPDXSheetYn}" />
								<input type="hidden" id="allowDownloadSPDXRdfYn" name="allowDownloadSPDXRdfYn" value="${project.allowDownloadSPDXRdfYn}" />
								<input type="hidden" id="allowDownloadSPDXTagYn" name="allowDownloadSPDXTagYn" value="${project.allowDownloadSPDXTagYn}" />
								<input type="hidden" id="allowDownloadSPDXJsonYn" name="allowDownloadSPDXJsonYn" value="${project.allowDownloadSPDXJsonYn}" />
								<input type="hidden" id="allowDownloadSPDXYamlYn" name="allowDownloadSPDXYamlYn" value="${project.allowDownloadSPDXYamlYn}" />
								<input type="hidden" id="isSimpleNotice" name="isSimpleNotice">
								<input type="hidden" name="previewOnly" id="previewOnly" value="N"/>

								<dl class="uploadCase">
									<dd class="mt10">
										<div id="tutorial_30" class="basicCase">
											<div class="uploadTit">
												<input type="checkbox" id="companyName" value="${ossNotice.editCompanyYn}"><label for="companyName">Company Name</label>											
											</div>
											<div class="uploadSet">
												<input type="text" id="editCompanyName" name="companyNameFull" value="${ossNotice.companyNameFull}" style="width:269px;" disabled><span style="padding-left:20px">* Deselect if the company name must be removed.</span>
												<div class="retxt"></div>
											</div>
										</div>
									</dd>
									<dd class="mt10">	
										<div class="basicCase">
											<div class="uploadTit">
												<input type="checkbox" id="ossDistributionSite" value="${ossNotice.editDistributionSiteUrlYn}"><label for="ossDistributionSite">OSS Distribution Site</label>
											</div>
											<div class="uploadSet">
												<input type="text" id="editOssDistributionSite" name="distributionSiteUrl" value="${ossNotice.distributionSiteUrl}" style="width:269px;" disabled><span style="padding-left:20px">* Deselect if both OSS Package and OSS Notice are not registered on the OSS Distribution site.</span>
												<div class="retxt"></div>
											</div>
										</div>
									</dd>
									<dd class="mt10">
										<div class="basicCase">
											<div class="uploadTit">
												<input type="checkbox" id="email" value="${ossNotice.editEmailYn}"><label for="email">Email (Written Offer)</label>
											</div>
											<div class="uploadSet">
												<input type="text" id="editEmail" style="width:269px;" name="email" value="${ossNotice.email}" disabled><span style="padding-left:20px">* Deselect if the written offer is not required(OSS Package is delivered directly to the recipient).</span>
												<div class="retxt"></div>
											</div>
										</div>
									</dd>
									<dd class="mt10">
										<div class="basicCase">
											<div class="uploadTit">
												<input type="checkbox" id="hideOssVersion" value="${ossNotice.hideOssVersionYn}"><label for="hideOssVersion">Hide OSS Version</label>
											</div>
											<div class="uploadSet">
												<span>* Select if all OSS version information must be removed.</span>	
											</div>
										</div>
									</dd>
									<dd class="mt20">
										<div class="basicCase">
											<div class="uploadTit">
												<input type="checkbox" id="append" value="${ossNotice.editAppendedYn}"><label for="append">Append</label>
											</div>
											<div class="uploadSet">
												<span>* Select if there is something to add to the OSS Notice.</span>
												<div class="nEEitor mt10" id="editAppend">
													<div class="grid-container">
														<div class="grid-width-100">
															<c:set var="appended" value="${ossNotice.appended}"/>
															<div id="editor2" class="packagingAppend">${appended}</div>
														</div>
													</div>
												</div>
											</div>
										</div>
									</dd>
									<dd class="mt20">
										<div class="basicCase">
											<div class="uploadTit">
												<label>OSS Notice File Format</label>
											</div>
											<div class="uploadSet">
												<span class="checkSet">
													<input type="checkbox" id="chkAllowDownloadNoticeHTML" name="chkAllowDownloadNoticeHTML" 
														data-targetid="allowDownloadNoticeHTMLYn" <c:if test="${project.allowDownloadNoticeHTMLYn eq 'Y'}">checked</c:if> disabled >
													<label for=chkAllowDownloadNoticeHTML>Notice HTML</label>
												</span>
												<c:if test="${ct:getCodeExpString(ct:getConstDef('CD_NOTICE_INFO'), ct:getConstDef('CD_DTL_NOTICE_TEXT')) eq 'Y'}">
													<span class="checkSet">
														<input type="checkbox" id="chkAllowDownloadNoticeText" name="chkAllowDownloadNoticeText" 
															data-targetid="allowDownloadNoticeTextYn" <c:if test="${project.allowDownloadNoticeTextYn eq 'Y'}">checked</c:if> <c:if test="${ossNotice.editNoticeYn eq 'N' or project.verificationStatus eq 'CONF'}"> disabled</c:if> >
														<label for="chkAllowDownloadNoticeText">Notice Text</label>
													</span>
												</c:if>
												<span class="checkSet">
													<input type="checkbox" id="chkAllowDownloadSimpleHTML" name="chkAllowDownloadSimpleHTML" 
														data-targetid="allowDownloadSimpleHTMLYn" <c:if test="${project.allowDownloadSimpleHTMLYn eq 'Y'}">checked</c:if> <c:if test="${ossNotice.editNoticeYn eq 'N' or project.verificationStatus eq 'CONF'}"> disabled</c:if> >
													<label for="chkAllowDownloadSimpleHTML">Simple HTML</label>
												</span>
												<c:if test="${ct:getCodeExpString(ct:getConstDef('CD_NOTICE_INFO'), ct:getConstDef('CD_DTL_NOTICE_TEXT')) eq 'Y'}">
													<span class="checkSet">
														<input type="checkbox" id="chkAllowDownloadSimpleText" name="chkAllowDownloadSimpleText" 
															data-targetid="allowDownloadSimpleTextYn" <c:if test="${project.allowDownloadSimpleTextYn eq 'Y'}">checked</c:if> <c:if test="${ossNotice.editNoticeYn eq 'N' or project.verificationStatus eq 'CONF'}"> disabled</c:if> >
														<label for="chkAllowDownloadSimpleText">Simple Text</label>
													</span>
												</c:if>
												<div class="mt10">
													<c:if test="${ct:getCodeExpString(ct:getConstDef('CD_NOTICE_INFO'), ct:getConstDef('CD_DTL_NOTICE_SPDX')) eq 'Y'}">
														<span class="checkSet">
															<input type="checkbox" id="chkAllowDownloadSPDXSheet" name="chkAllowDownloadSPDXSheet"
																data-targetid="allowDownloadSPDXSheetYn" <c:if test="${project.allowDownloadSPDXSheetYn eq 'Y'}">checked</c:if> <c:if test="${ossNotice.editNoticeYn eq 'N' or project.verificationStatus eq 'CONF'}"> disabled</c:if> >
															<label for="chkAllowDownloadSPDXSheet">SPDX(SpreadSheet)</label>
														</span>
														<span class="checkSet">
															<input type="checkbox" id="chkAllowDownloadSPDXRdf" name="chkAllowDownloadSPDXRdf"
																data-targetid="allowDownloadSPDXRdfYn" <c:if test="${project.allowDownloadSPDXRdfYn eq 'Y'}">checked</c:if> <c:if test="${ossNotice.editNoticeYn eq 'N' or project.verificationStatus eq 'CONF'}"> disabled</c:if> >
															<label for="chkAllowDownloadSPDXRdf">SPDX(RDF)</label>
														</span>
														<span class="checkSet">
															<input type="checkbox" id="chkAllowDownloadSPDXTag" name="chkAllowDownloadSPDXTag"
																data-targetid="allowDownloadSPDXTagYn" <c:if test="${project.allowDownloadSPDXTagYn eq 'Y'}">checked</c:if> <c:if test="${ossNotice.editNoticeYn eq 'N' or project.verificationStatus eq 'CONF'}"> disabled</c:if> >
															<label for="chkAllowDownloadSPDXTag">SPDX(TAG)</label>
														</span>
														<span class="checkSet">
															<input type="checkbox" id="chkAllowDownloadSPDXJson" name="chkAllowDownloadSPDXJson"
																   data-targetid="allowDownloadSPDXJsonYn" <c:if test="${project.allowDownloadSPDXJsonYn eq 'Y'}">checked</c:if> <c:if test="${ossNotice.editNoticeYn eq 'N' or project.verificationStatus eq 'CONF'}"> disabled</c:if> >
															<label for="chkAllowDownloadSPDXJson">SPDX(JSON)</label>
														</span>
														<span class="checkSet">
															<input type="checkbox" id="chkAllowDownloadSPDXYaml" name="chkAllowDownloadSPDXYaml"
																   data-targetid="allowDownloadSPDXYamlYn" <c:if test="${project.allowDownloadSPDXYamlYn eq 'Y'}">checked</c:if> <c:if test="${ossNotice.editNoticeYn eq 'N' or project.verificationStatus eq 'CONF'}"> disabled</c:if> >
															<label for="chkAllowDownloadSPDXYaml">SPDX(YAML)</label>
														</span>
													</c:if>
												</div>
											</div>
										</div>
									</dd>
									<c:if test="${project.useCustomNoticeYn eq 'Y'}">
									<dd class="mt20">
										<span class="useCustomNotice"><spring:message code="msg.project.verification.use.custom.notice"/></span>
									</dd>
									</c:if>
									<dd class="mt10">
										<div class="basicCase">
											<span class="right">
												<c:if test="${project.verificationStatus ne 'CONF' and project.dropYn ne 'Y' and (ct:isAdmin() or project.viewOnlyFlag eq 'N')}">
													<input type="button" id="save" value="Save" class="btnColor red"/>
												</c:if>
											</span>
										</div>
									</dd>
								</dl>
							</form>
						</div>
					</div>
				</div>
				<div class="btnLayout w1025">
				<c:if test="${ct:isAdmin()}">
					<input type="button" id="noticePreview" value="Preview" class="btnColor" style="width: 100px;"/>
					<span class="selectSet" style="width:160px">
						<strong for="docType" title="selected value"></strong>
						<select id="docType" name="docType">
							<!-- <option value="noticePreview">Notice Preview</option> -->
							<option value="noticeDownload">Default (html)</option>
							<c:if test="${ct:getCodeExpString(ct:getConstDef('CD_NOTICE_INFO'), ct:getConstDef('CD_DTL_NOTICE_TEXT')) eq 'Y'}">
								<option value="noticeTextDownload">Default (text)</option>
							</c:if>
							<option value="noticeSimpleDownload">Simple (html)</option>
							<c:if test="${ct:getCodeExpString(ct:getConstDef('CD_NOTICE_INFO'), ct:getConstDef('CD_DTL_NOTICE_TEXT')) eq 'Y'}">
								<option value="noticeTextSimpleDownload">Simple (text)</option>
							</c:if>
							<c:if test="${ct:getCodeExpString(ct:getConstDef('CD_NOTICE_INFO'), ct:getConstDef('CD_DTL_NOTICE_SPDX')) eq 'Y'}">
								<option value="spdxSpreadSheet">SPDX (spreadsheet)</option>
								<option value="spdxRdf">SPDX (RDF)</option>
								<option value="spdxTag">SPDX (TAG)</option>
								<option value="spdxJson">SPDX (JSON)</option>
								<option value="spdxYaml">SPDX (YAML)</option>
							</c:if>
						</select>
					</span>
					<input type="button" id="packageDocDownload" value="download" class="btnColor" style="width: 100px;"/>
					<c:if test="${isProgOrConfStat}">
						<c:set var="fullCustomFlag" value="${project.useCustomNoticeYn eq 'Y'}"/>
						<input type="checkbox" id="chkUseCustomNotice" style="margin:0 5px;" <c:if test="${fullCustomFlag}">checked</c:if>/>Use the html editor
						<input type="button" id="noticeEditor" value="Edit" class="btnColor" style="margin-left: 5px;width: 100px;<c:if test="${!fullCustomFlag}">opacity: 0.5</c:if>" <c:if test="${!fullCustomFlag}">disabled="disabled"</c:if>/>
					</c:if>
				</c:if>
				</div>
				<c:if test="${ct:isAdmin() eq false and isAllowDownload and project.verificationStatus eq 'CONF'}">
				<div class="btnLayout">
					<span class="boxLine" style="padding-top: 5px;padding-bottom: 5px;">
						<label><strong>Download: </strong></label>
						<c:if test="${project.allowDownloadNoticeHTMLYn eq 'Y'}">
						<span>
							<a href="javascript:fn.downloadNotice();" style="color: rgb(0, 112, 192);text-decoration: underline !important;margin-left: 5px;">Notice HTML</a>
						</span>
						</c:if>
						<c:if test="${project.allowDownloadNoticeTextYn eq 'Y'}">
						<span>
							<a href="javascript:fn.downloadNoticeText();" style="color: rgb(0, 112, 192);text-decoration: underline !important;margin-left: 5px;">Notice Text</a>
						</span>
						</c:if>
						<c:if test="${project.allowDownloadSimpleHTMLYn eq 'Y'}">
						<span>
							<a href="javascript:fn.downloadNoticeSimple();" style="color: rgb(0, 112, 192);text-decoration: underline !important;margin-left: 5px;">Simple HTML</a>
						</span>
						</c:if>
						<c:if test="${project.allowDownloadSimpleTextYn eq 'Y'}">
						<span>
							<a href="javascript:fn.downloadNoticeTextSimple();" style="color: rgb(0, 112, 192);text-decoration: underline !important;margin-left: 5px;">Simple Text</a>
						</span>
						</c:if>
						<c:if test="${project.allowDownloadSPDXSheetYn eq 'Y'}">
						<span>
							<a href="javascript:fn.downloadSpdxSpreadSheetExcel();" style="color: rgb(0, 112, 192);text-decoration: underline !important;margin-left: 5px;">SPDX(SpreadSheet)</a>
						</span>
						</c:if>
						<c:if test="${project.allowDownloadSPDXRdfYn eq 'Y'}">
						<span>
							<a href="javascript:fn.downloadSpdxRdf();" style="color: rgb(0, 112, 192);text-decoration: underline !important;margin-left: 5px;">SPDX(RDF)</a>
						</span>
						</c:if>
						<c:if test="${project.allowDownloadSPDXTagYn eq 'Y'}">
						<span>
							<a href="javascript:fn.downloadSpdxTag();" style="color: rgb(0, 112, 192);text-decoration: underline !important;margin-left: 5px;">SPDX(TAG)</a>
						</span>
						</c:if>
						<c:if test="${project.allowDownloadSPDXJsonYn eq 'Y'}">
						<span>
							<a href="javascript:fn.downloadSpdxJson();" style="color: rgb(0, 112, 192);text-decoration: underline !important;margin-left: 5px;">SPDX(JSON)</a>
						</span>
						</c:if>
						<c:if test="${project.allowDownloadSPDXYamlYn eq 'Y'}">
						<span>
							<a href="javascript:fn.downloadSpdxYaml();" style="color: rgb(0, 112, 192);text-decoration: underline !important;margin-left: 5px;">SPDX(YAML)</a>
						</span>
						</c:if>
					</span>
				</div>
				</c:if>
			</div>
		</div>
	</div>
</div>
<button id='continue_tutorial_26' style="position: fixed; bottom: 30px; left: 30px; font-size: 30px; padding: 5px;">Continue tutorial (packaging)</button>

<!-- //wrap -->
<div id="blind_wrap"></div>

<c:if test="${not empty userGuideLicenseList}">
<div class="pop warningPop">
	<div class="popdata">
		<p><b><spring:message code="msg.project.packaging.verify.userguide" /></b></p><br/>
		<c:forEach items="${userGuideLicenseList}" var="userGuide">
			<p style="font-style: italic;"><b>${userGuide.licenseName}</b></p>
			<p style="margin-left: 30px;">${userGuide.descriptionHtml}</p><br/>
		</c:forEach>
	</div>
	<div class="pbtn">
		<input type="button" value="OK" class="btnColor red OKcolse" />
	</div>
</div>
</c:if>
