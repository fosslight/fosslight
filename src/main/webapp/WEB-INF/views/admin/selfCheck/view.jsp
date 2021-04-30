<%@ page contentType="text/html; charset=utf-8" pageEncoding="utf-8"%>
<jsp:include page="../common/gridCommonFn.jsp" flush="false" />
<!-- wrap -->
<div id="wrapIframe">
	<div class="projdecTop">
		<div class="projectInfo">
			<h2>Project Information</h2>
			<ul>
				<li class="first"><span>Project Name</span><strong><label id="vPrjName"></label></strong></li>
				<li><span>Created</span><strong><label id="vCreated"></label></strong></li>
				<li class="first"><span>Comment</span><strong><label></label></strong></li>
			</ul>
			<div style="padding:0 0 30px 130px;">
				<div id="editor" style="overflow:auto; height:150px;">${project.comment}</div>
				<div style="padding-top:10px;"><input type="button" value="Edit" onclick="src_fn.mvEdit()" class="btnColor btnExpor srcBtn right" /></div>
			</div>
		</div>
		
		<div id="srcDivBtn" class="mt20">
			<span class="right" style="margin-right:50px;">
				<input type="button" value="Export" onclick="src_fn.downloadExcel()" class="btnColor red btnExpor srcBtn" />
				<input id="srcResetUp" type="button" value="Reset" class="btnColor btnReset srcBtn idenReset" />
				<input id="srcSaveUp" type="button" value="Save" class="btnSave btnColor red idenSave"/>
			</span>
		</div>
	</div>
	
	<div class="projectContents" style="top:250px;">
				<fieldset class="editSearchUp grayBox singleLine" style="margin-top:80px;">
					<div class="sukind">
						<span class="radioSet srcBtn"><input type="radio" id="srcR1" name="srcSelectOption" onchange="src_fn.changeSelectOption()" value="1" checked/><label for="srcR1">Upload Analysis Result</label></span>
					</div>
						<form id="srcUploadForm" class="srcBtn">
							<input type="hidden" id="srcCsvFileId" value="${project.srcCsvFileId }">
							<dl class="uploadCase" id="srcUploadSearch">
								<dt>Upload Analysis Result</dt>
								<dd>
									<div class="basicCase">
										<div class="uploadTit">
											<span class="checkSet"><label for="2">Please select a file to upload</label></span>	
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
																	<a href="/download/${csvFile.fileSeq }/${csvFile.logiNm}">${csvFile.origNm }</a>
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
				<div class="jqGridSet mt20 srcBtn">
					<table id="srcList"><tr><td></td></tr></table>
					<div id="srcPager"></div>
				</div>
				<!---->
				<div class="btnLayout">
					<span class="right">
						<input type="button" value="Export" onclick="src_fn.downloadExcel()" class="btnColor red btnExpor srcBtn" />
						<input id="srcReset" type="button" value="Reset" class="btnColor btnReset srcBtn idenReset" />
						<input id="srcSave" type="button" value="Save" class="btnSave btnColor red idenSave"/>
					</span>
				</div>
				<!---->
			</div>
	</div>
	<div>
	</div>
</div>
<!-- //wrap -->
<div id="blind_wrap"></div>

<div class="pop ossSelectPop">
	<h1><input type="checkbox" onchange="checkAll('ossSelectPop', this)" class="sheetNum"> Select Sheet</h1>
	<div class="popdata sheetNameArea">
	</div>
	<div class="pbtn">
		<input type="button" value="Cancel" class="btnCancel btnColor" onclick="src_fn.closeAndroidPop()"/>
		<input type="button" value="OK" class="btnColor red sheetApply" onclick="src_fn.getAndroidData()"/>
	</div>
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
		<input type="button" value="Cancel" class="btnCancel btnColor" onclick="src_fn.closePop()">
		<input type="button" value="OK" class="btnColor red sheetApply" onclick="src_fn.getSheetData()">
	</div>
</div>