<%@ page contentType="text/html; charset=utf-8" pageEncoding="utf-8"%>
<%@ include file="/WEB-INF/constants.jsp"%>
<jsp:include page="../common/gridCommonFn.jsp" flush="false" />
<!-- wrap -->
<div id="wrapIframe">
	<!---->
	<div class="projectContents"  style="top:0px;">
	<div id="divViewMode">
	</div>
	<div id="divEditMode">
		<!---->
		<div class="tbws1 w1025">
			<form name="projectForm" id="projectForm" action="" method="post">
				<input type="hidden" name="prjId" style="display: none;"/>
				<input type="hidden" name="prjModelJson" style="display: none;"/>
				<input type="hidden" name="comment" />
				<input type="hidden" name="deleteMemo" id="deleteMemo" />
				<table class="dCase">
					<colgroup>
						<col width="188" />
						<col />
					</colgroup>
					<tbody>
						<tr>
							<th class="dCase txStr"><spring:message code="msg.common.field.project.name" /></th>
							<td class="dCase">
								<div class="required">
									<input name="prjName" type="text" class="w100P"/>
									<span class="retxt prjName">This field is required.</span>
								</div>
							</td>
						</tr>
						<tr>
							<th class="dCase"><spring:message code="msg.common.field.version" /></th>
							<td class="dCase">
								<input name="prjVersion" type="text" class="w100P"/>
							</td>
						</tr>
						<tr>
							<th class="dCase"><spring:message code="msg.common.field.comment" /></th>
							<td class="dCase">
								<div class="grid-container">
									<div class="grid-width-100">
										<div id="editor">${project.comment}</div>
									</div>
								</div>
							</td>
						</tr>
						<tr style="display:none">
							<th class="dCase  txStr">Creator</th>
							<td class="dCase">
								<input type="text" name="creatorNm" class="autoComCreatorDivision" value=""/>
								<input type="hidden" name="creator" <c:if test="${not empty project }">value='${project.creator}'</c:if>/>
							</td>
						</tr>
					</tbody>
				</table>
			</form>
		</div>
		<!---->
		<div class="btnLayout w1025">
			<span class="right">
				<c:if test="${not empty project.prjId}">
					<input id="cancel" type="button" value="Cancel" class="btnColor red" />
				</c:if>
				<input id="save" type="button" value="Save" class="btnColor red" />
			</span>
		</div>
		<!---->
	</div><!-- //end of edit mode -->
<c:if test="${not empty project.prjId}">
	<div class="projdecTab">
		<div class="subTab">
			<div class="tabMenu">
				<a rel="ossList">OSS List</a>
				<a rel="notice">Notice</a>
			</div>
		</div>
	</div>
	<div>
		<div id="ossList" class="tabContent">
			<fieldset class="editSearchUp grayBox singleLine mt20">
				<div class="sukind">
					<span class="radioSet srcBtn"><input type="radio" id="srcR1" name="srcSelectOption" onchange="fn.changeSelectOption(this)" value="1" checked/><label for="srcR1">Upload Analysis Result</label></span>
					<span class="radioSet srcBtn"><input type="radio" id="2" name="selectOption_${i}" onchange="fn.changeSelectOption(this)" value="2" <c:if test="${ct:getCodeExpString(ct:getConstDef('CD_SYSTEM_SETTING'), ct:getConstDef('CD_EXTERNAL_ANALYSIS_USED_FLAG')) eq 'N'}"></c:if>/><label for="2">URL </label></span>
				</div>
					<form id="srcUploadForm" class="srcBtn">
						<input type="hidden" id="srcCsvFileId" value="${project.srcCsvFileId }">
						<dl class="uploadCase" id="srcUploadSearch">
							<dt>Upload Analysis Result</dt>
							<dd>
								<div class="basicCase">
									<div class="uploadTitCheckSet">
										<span class="checkSet"><label class="checksrcR1">Please select a file to upload</label></span>
										<span class="checkSet" style="display:none;"><label class="check2">Enter the link of the source to be analyzed</label></span>
									</div>
									<div class="uploadGroup">
										<div class="uploadSet">
											<span class="fileex_back">
												<div id="srcCsvFile">+ Add file</div>
											</span>
											<div class="uploadList">
												<ul class="csvFileArea">
												<c:forEach var="csvFile" items="${project.csvFile }" varStatus="vs">
													<c:if test="${csvFile.delYn == 'N'}">
														<li>
															<span>
																<strong>
																	<a href="<c:url value="/download/${csvFile.fileSeq }/${csvFile.logiNm}"/>">${csvFile.origNm }</a>
																	<br>
																	${csvFile.createdDate}
																	<input type="hidden" value="${csvFile.fileSeq }"/>
																	<input type="button" value="Delete" class="smallDelete" onclick="src_fn.deleteCsv(this, '1')"/>
																</strong>
															</span>
														</li>
													</c:if>
												</c:forEach>
												</ul>
											</div>
										</div>
										<br/>
										<div id="wgetUrl_${i}" class="wgetUrl" style="width: 500px; display: none;"><input type="text" style="width:70%" id="sendWgetUrl" name="sendWgetUrl" /><input type="button" value="send" class="btnColor red btnExpor srcBtn" onclick=src_fn.uploadOSSByUrl() /></div>
									</div>
								</div>
							</dd>
						</dl>
					</form>
			</fieldset>
			<div class="boxLine mt10" style="display:none;">
				<div class="fileupload-progress">
					<!-- The global progress bar -->
					<div class="progress mt10" role="progressbar" aria-valuemin="0" aria-valuemax="100"></div>
					<!-- The extended global progress state -->
					<div class="progress-extended mt10">&nbsp;</div>
				</div>
			</div>
			<div class="btnLayout">
	            <input id="delete" type="button" value="Delete" class="btnColor left selfCheckDelete" /><!-- 2018-07-19 choye 추가 class에  selfCheckDelete -->
	            <span class="right">
	            <a class="iconSet help left" id="helpLink_vulerabiityExport" style="display: none; position:relative; cursor: pointer; right:10px;"></a>
	                <input type="button" value="Export" onclick="src_fn.downloadExcel()" class="btnColor red btnExpor srcBtn" />
	                <input type="button" value="Bulk Edit" onclick="fn.bulkEdit()" class="btnColor red"/>
	                <input type="button" value="Yaml" class="btnColor red btnExport" onclick="fn.downloadYaml()"/>
	                <input type="button" value="Check OSS Name" onclick="src_fn.CheckOssViewPage()" class="btnColor red btnExpor srcBtn" style="width: 115px;" />
	                <input type="button" value="Check License" onclick="src_fn.CheckOssLicenseViewPage()" class="btnColor red btnExpor srcBtn" style="width: 100px;" />
	                <input id="srcResetUp" type="button" value="Reset" class="btnColor btnReset srcBtn idenReset" />
	                <input id="srcSaveUp" type="button" value="Save" class="btnSave btnColor red idenSave"/>
	            </span>
	        </div>
			<div class="jqGridSet srcBtn">
				<table id="srcList"><tr><td></td></tr></table>
				<div id="srcPager"></div>
			</div>
		</div>
		<div id="notice" class="tabContent">
			<div class="mt10">
				<div class="btnLayout2 w1025 mt20">
					<span><strong>고지문은 참고용으로만 제공되며, FOSSLight Hub는 고지문의 내용, 신뢰도, 정확성 등에 대해 어떠한 보증도 하지 않습니다.<br>FOSSLight Hub와 고지문 사용에 따라 발생하는 모든 책임은 전적으로 사용자에게 있으며, FOSSLight Hub는 사용자 또는 제3자에 대해 어떠한 책임도 지지 않습니다.</strong></span>
				</div>
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
								<input type="hidden" id="allowDownloadSPDXJsonYn" name="allowDownloadSPDXJsonYn" value="${project.allowDownloadSPDXJsonYn}" />
								<input type="hidden" id="allowDownloadSPDXYamlYn" name="allowDownloadSPDXYamlYn" value="${project.allowDownloadSPDXYamlYn}" />
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
			</div>
		</div>
	</div>
	<!---->
</c:if>
	</div>
</div>

<div class="pop sheetSelectPop">
	<h1>Select Sheet</h1>
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
		<input type="button" value="Cancel" class="btnCancel btnColor" onclick="src_fn.closePop()">
		<input type="button" value="OK" class="btnColor red sheetApply" onclick="src_fn.getSheetData()">
	</div>
</div>
<!-- //wrap -->