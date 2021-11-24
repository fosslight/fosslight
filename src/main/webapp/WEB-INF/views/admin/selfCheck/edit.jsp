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
		<div class="btnLayout">
            <input id="delete" type="button" value="Delete" class="btnColor left selfCheckDelete" /><!-- 2018-07-19 choye 추가 class에  selfCheckDelete -->
            <span class="right">
            <a class="iconSet help left" id="helpLink_vulerabiityExport" style="display: none; position:relative; cursor: pointer; right:10px;"></a>
				<input type="button" value="OSS Notice" onclick="src_fn.createNoticeTab()" class="btnColor red btnExpor srcBtn"  style="width: 80px;" />
				<input type="button" value="Export" onclick="src_fn.downloadExcel()" class="btnColor red btnExpor srcBtn" />
                <input type="button" value="Check OSS Name" onclick="src_fn.CheckOssViewPage()" class="btnColor red btnExpor srcBtn" style="width: 115px;" />
                <input id="srcResetUp" type="button" value="Reset" class="btnColor btnReset srcBtn idenReset" />
                <input id="srcSaveUp" type="button" value="Save" class="btnSave btnColor red idenSave"/>
            </span>
        </div>
		<div class="jqGridSet srcBtn">
			<table id="srcList"><tr><td></td></tr></table>
			<div id="srcPager"></div>
		</div>
		<!---->
		<div class="btnLayout">
			<input id="delete" type="button" value="Delete" class="btnColor left selfCheckDelete" /><!-- 2018-07-19 choye 추가 class에  selfCheckDelete -->
			<span class="right">
				<input type="button" value="OSS Notice" onclick="createTabInFrame('Notice','#/selfCheck/verification/'+'${project.prjId}')" class="btnColor red btnNotice srcBtn"/>
				<input type="button" value="Export" onclick="src_fn.downloadExcel()" class="btnColor red btnExpor srcBtn" />
				<input type="button" value="Check OSS Name" onclick="src_fn.CheckOssViewPage()" class="btnColor red btnExpor srcBtn" style="width: 115px;" />
				<input id="srcReset" type="button" value="Reset" class="btnColor btnReset srcBtn idenReset" />
				<input id="srcSave" type="button" value="Save" class="btnSave btnColor red idenSave"/>
			</span>
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