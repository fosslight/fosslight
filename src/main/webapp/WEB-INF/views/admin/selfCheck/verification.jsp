<%@ page contentType="text/html; charset=utf-8" pageEncoding="utf-8"%>
<%@ include file="/WEB-INF/constants.jsp"%>
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
			<a class="right" id="helpLink" style="position:relative; cursor: pointer; top:-37px; right:-75px; display: none;"><img alt="" src="<c:url value="/images/user-guide.png"/>" /></a>
		</div>
		<!---->
		<div class="projdecTab">
			<div class="subTab">
				<div class="tabMenu">
					<a rel="notice">Notice</a>
				</div>
			</div>
		</div>
	</div>
	<div class="commentEditor" style="display:none;">
		<div class="cBtn">
		<input type="button" value="Save & Send comment" class="btnCLight saveEditor"/>
		<input type="button" value="Save draft" class="btnCLight"/>
		</div>
		<div class="grid-container">
			<div class="grid-width-100">
				<div id="editor">${project.userComment}</div>
			</div>
		</div>
	</div>
	<div>
		<c:set var="isAllowDownload" value="${project.allowDownloadBitFlag > 0}"/>
		<c:set var="isProgOrConfStat" value="${project.verificationStatus eq 'REV' or project.verificationStatus eq 'CONF'}"/>
		<div id="notice" class="tabContent">
			<div class="projectContents">
				<div class="btnLayout2 w1025 mt20">
					<span><input type="radio" id="r1" name="btnEditOssNotice" value="N" checked/><label for="r1"><strong>Request to generate a default OSS Notice. (Select this in most cases.)</strong></label></span>
				</div>
				<div class="btnLayout2 w1025 mt10">
					<span><input type="radio" id="r2" name="btnEditOssNotice" value="Y" /><label for="r2"><strong>Request to generate a modified OSS Notice. (Select this only in exceptional cases.)</strong></label></span>
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
								<input type="hidden" id="isSimpleNotice" name="isSimpleNotice">
								<input type="hidden" name="previewOnly" id="previewOnly" value="N"/>

								<dl class="uploadCase">
									<dd class="mt10">
										<div class="basicCase">
											<div class="uploadTit">
												<input type="checkbox" id="companyName" value="${ossNotice.editCompanyYn}" checked><label for="companyName">Company Name</label>											
											</div>
											<div class="uploadSet">
												<input type="text" id="editCompanyName" name="companyNameFull" value="" style="width:269px;" disabled><span style="padding-left:20px">* Deselect if the company name must be removed.</span>
												<div class="retxt"></div>
											</div>
										</div>
									</dd>
									<dd class="mt10">	
										<div class="basicCase">
											<div class="uploadTit">
												<input type="checkbox" id="ossDistributionSite" value="${ossNotice.editDistributionSiteUrlYn}" checked><label for="ossDistributionSite">OSS Distribution Site</label>
											</div>
											<div class="uploadSet">
												<input type="text" id="editOssDistributionSite" name="distributionSiteUrl" value="" style="width:269px;" disabled><span style="padding-left:20px">* Deselect if both OSS Package and OSS Notice are not registered on the OSS Distribution site.</span>
												<div class="retxt"></div>
											</div>
										</div>
									</dd>
									<dd class="mt10">
										<div class="basicCase">
											<div class="uploadTit">
												<input type="checkbox" id="email" value="${ossNotice.editEmailYn}" checked><label for="email">Email (Written Offer)</label>
											</div>
											<div class="uploadSet">
												<input type="text" id="editEmail" style="width:269px;" name="email" value="" disabled><span style="padding-left:20px">* Deselect if the written offer is not required(OSS Package is delivered directly to the recipient).</span>
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
												</c:if>
											</div>
										</div>
									</dd>
									<!-- <dd class="mt10">
										<div class="basicCase">
											<span class="right">
												<c:if test="${project.verificationStatus ne 'CONF' and project.dropYn ne 'Y' and (ct:isAdmin() or project.viewOnlyFlag eq 'N')}">
													<input type="button" id="save" value="Save" class="btnColor red"/>
												</c:if>
											</span>
										</div>
									</dd> -->
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
					</span>
				</div>
				</c:if>
			</div>
		</div>
	</div>
</div>
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
