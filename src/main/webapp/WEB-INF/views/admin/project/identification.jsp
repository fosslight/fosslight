<%@ page contentType="text/html; charset=utf-8" pageEncoding="utf-8"%>
<%@ include file="/WEB-INF/constants.jsp"%>
<script type="text/javascript" src="${ctxPath}/js/tutorial/tutorial-identification.js?${jsVersion}"></script>
<jsp:include page="../common/gridCommonFn.jsp" flush="false" />
<!-- wrap -->
<c:set var="isCommited" value="${project.verificationStatus eq 'CONF'}"/>
<div id="wrapIframe">
	<div class="projdecTop">
		<div class="projectInfo">
			<h2>Project Information</h2>
			<ul>
				<li class="first"><span>Project Name</span><strong><label id="vPrjName"></label>
				    <input type="button" value=" 📢 Continue Tutorial " id="continue_tutorial" />
					<span id="editTab" class="btnIcon basic" style="display:inline-block;width:16px;padding:0;margin-left:3px;">Basic Info</span>
					<c:if test="${project.verificationStatus ne 'NA' and (not empty project.verificationStatus or project.identificationStatus eq 'CONF')}">
					<span id="packagingTab" class="btnIcon packag" style="display:inline-block;width:16px;padding:0;margin-left:3px;">Packaging</span>
					</c:if>
					<c:if test="${distributionFlag and project.destributionStatus ne 'NA' and (not empty project.destributionStatus or project.verificationStatus eq 'CONF')}">
					<span id="distributionTab" class="btnIcon distr" style="display:inline-block;width:16px;padding:0;margin-left:3px;">Distribution</span>
					</c:if>
					</strong>
				</li>
				<li><span>Created</span><strong><label id="vCreated"></label></strong></li>
			</ul>
			<a class="right" id="helpLink" style="position:relative; cursor: pointer; top:-37px; right:-75px; display: none;"><img alt="" src="${ctxPath}/images/user-guide.png" /></a>
		</div>
		<!---->	
		<div class="projdecTab">
			<div class="subTab">
			<div class="tabMenu">
				<a rel="partyDiv" id="third_party">3rd party</a>
				<a rel="srcDiv" id="src_tab">SRC</a>
				<a rel="binDiv">BIN</a>
				<a rel="binAndroidDiv">BIN (Android)</a>
				<a rel="bomDiv">BOM</a>
				<c:if test="${batFlag}">
					<a rel="batDiv">BAT</a>
				</c:if>
			</div>
			</div>
			<div class="projdecBtn" style="display: none;">
				<c:if test="${ct:isAdmin()}">
					<c:if test="${not empty project.srcAndroidResultFileId or not empty project.binBinaryFileId}">
						<div style="position:absolute;right:154px;top:5px;width:175px;display:none;" id="binaryDB">
							<input type="checkbox" id="ignoreBinaryDbFlag" name="ignoreBinaryDbFlag" value="N" style="margin-right:10px;"/>Do not register in binary DB
						</div>
					</c:if>
				</c:if>
				<c:choose>
					<c:when test="${not ct:isAdmin() and (project.completeYn eq 'Y' or project.distributeDeployYn eq 'Y' or project.identificationStatus eq 'REV')}">
						<input type="button" id="btnRejectNotice" value="Reject" class="gray btnColor" title="If you need modify, please leave a comment on FOSSLight team." onclick="return false;"/>
					</c:when>
					<c:when test="${project.completeYn ne 'Y' and project.dropYn ne 'Y' and project.distributeDeployYn ne 'Y'}">
						<a class="btnSet confirm"><span id="bomConfirm">Confirm</span></a>
						<a class="btnSet reject"><span id="bomReject">Reject</span></a>
						<a class="btnSet review"><span id="bomRequest">Request</span></a>
						<a class="btnSet restart"><span id="bomReviewStart">Review Start</span></a>
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
		<input type="button" value="Save & Send comment" class="btnCLight saveEditor" onclick="com_fn.sendEditor('WR');"/>
		<input type="button" value="Save draft" class="btnCLight" onclick="com_fn.saveEditor();"/>
	</div>
	<div class="grid-container">
		<div class="grid-width-100">
			<div id="editor">${project.userComment}</div>
		</div>
	</div>
	<script>
		initSample();
		showHelpLink("Project_List_Identification");
	</script>
	</div>
	<div>
	
<!-- 3rd party ****************************************************************************************************** -->
	<c:if test="${partnerFlag}">
		<div id="partyDiv" class="tabContent">
			<div class="projectContents">
				<!---->
				<div class="orangeBox">
					<input type="button" value="btnToggle" class="btnToggle">
					<fieldset class="editSearchUp">
							<div class="sukind">
								<span class="radioSet partyBtn"><input type="radio" id="1" name="selectOption" onchange="party_evt.changeSelectOption()" value="1" checked <c:if test="${isCommited}">disabled</c:if> /><label for="1">3rd party Search</label></span>
								<span class="radioSet partyBtn"><input type="radio" id="2" name="selectOption" onchange="party_evt.changeSelectOption()" value="2" <c:if test="${isCommited}">disabled</c:if> /><label for="2">Project Search</label></span>
								<span class="checkSet"><input type="checkbox" id="applicableParty" value="N" onchange="com_fn.checkAplicable(this, 'partyBtn')"/><label for="applicableParty">Not Applicable</label></span>
							</div>
							<form id="3rdSearchForm" class="partyBtn">
							<input type="hidden" name="publicYn" value="Y">
							<dl class="fideCase" id="3rdSearch">
								<dt>3rd party Search Area</dt>
								<dd>
									<label>3rd party Name</label>
									<input type="text" class="autoComConfParty" name="partnerName"/>
								</dd>
								<dd>
									<label>Software Name</label>
									<input type="text" class="autoComConfSwNm" name="softwareName"/>
								</dd>
								<dd>
									<label>Software Version</label>
									<input type="text" class="autoComConfSwVer" name="softwareVersion"/>
								</dd>
								<dd class="sBtnArea"><input type="button" value="Search" class="btnColor red wauto partyBtn" id="3rdSearchBtn"/></dd>
							</dl>
							</form>
							<form id="projectSearchForm" class="partyBtn">
							<input type="hidden" name="publicYn" value="Y">
							<dl class="fideCase" id="projectSearch" style="display:none;">
								<dt>Project Search Area</dt>
								<dd>
									<label>Project Name</label>
									<input type="text" class="autoComProjectNmConf" name="prjName"/>
								</dd>
								<dd>
									<label>Project Version</label>
									<input type="text" name="prjVersion"/>
								</dd>
								<dd class="sBtnArea"><input type="button" value="Search" class="btnColor black wauto partyBtn" id="3rdProjectSearchBtn"/></dd>
							</dl>
							</form>
					</fieldset>
					<!---->
					<div class="3rdProjectSearch editSearchUp partyBtn">
						<div class="three1list" style="display:none;">
							<div class="tRStit">
								<h3>Search Results</h3>
								<br/>
							</div>
							<div class="jqGridSet">
								<table id="_list"><tr><td></td></tr></table>
								<div id="pager"></div>
							</div>
						</div>
						<!---->
						<br/>
						<div class="three2list" style="display:none;">
							<div class="tRStit">
								<h3>Detail Preview</h3>
								<span class="right"><input type="button" value="Load" class="btnCLight" onclick="party_evt.loadToList()"/></span>
							</div>
							<div class="jqGridSet">
								<table id="_list2"><tr><td></td></tr></table>
								<div id="pager2"></div>
							</div>
						</div>
					</div>
					<div class="3rdProjectSearch2 editSearchUp partyBtn">
						<div class="three3list" style="display:none;">
							<div class="tRStit">
								<h3>Search Results</h3>
								<br/>
							</div>
							<div class="jqGridSet">
								<table id="_list-1"><tr><td></td></tr></table>
								<div id="pager-1"></div>
							</div>
						</div>
						<!---->
						<br/>
						<div class="three4list" style="display:none;">
							<div class="tRStit">
								<h3>Detail Preview</h3>
								<span class="right"><input type="button" value="Load" class="btnCLight" onclick="party_evt.projectToAddList()"/></span>
							</div>
							<div class="jqGridSet">
								<table id="_list-2"><tr><td></td></tr></table>
								<div id="pager2-2"></div>
							</div>
						</div>
					</div>
					<div class="partyBtn">
						<div class="three5list">
							<div class="tRStit">
								<h3>Loaded List</h3>
								<br/>
							</div>
							<div class="jqGridSet">
								<table id="_3rdAddList"><tr><td></td></tr></table>
							</div>
						</div>
					</div>
				</div>
				<!---->
				<!---->
				<div class="btnLayout">
		            <span class="right">
			            <c:if test="${project.dropYn ne 'Y'}">
			                <input type="button" value="Reset" class="btnColor btnReset partyBtn idenReset" onclick="party_evt.reset()"/>
			                <input type="button" id="btnPartnerSaveUp"  value="Save" class="btnColor red btnSave idenSave" onclick="party_evt.save()"/>
		            	</c:if>
	            	</span>
		        </div>
				<div class="jqGridSet partyBtn">
					<table id="list3"><tr><td></td></tr></table>
					<div id="pager3"></div>
				</div>
				<!---->
				<div class="btnLayout">
					<span class="right">
						<c:if test="${project.dropYn ne 'Y'}">
							<input type="button" value="Reset" class="btnColor btnReset partyBtn idenReset" onclick="party_evt.reset()"/>
							<input type="button" value="Save" class="btnColor red btnSave idenSave" onclick="party_evt.save()"/>
						</c:if>
					</span>
				</div>
				<!---->

			</div>
		</div>
	</c:if>
<!-- SRC ************************************************************************************************************ -->
		<div id="srcDiv" class="tabContent">
			<div class="projectContents">
				<div class="orangeBox">
					<input type="button" value="btnToggle" class="btnToggle">
					<fieldset class="editSearchUp">
						<div class="sukind">
							<span class="radioSet srcBtn"><input type="radio" id="srcR1" name="srcSelectOption" onchange="src_fn.changeSelectOption()" value="1" checked <c:if test="${isCommited}">disabled</c:if> /><label for="srcR1">Upload Analysis Result</label></span>
							<span class="radioSet srcBtn"><input type="radio" id="srcR2" name="srcSelectOption" onchange="src_fn.changeSelectOption()" value="2" <c:if test="${isCommited}">disabled</c:if> /><label for="srcR2">Project Search</label></span>
							<span class="checkSet"><input type="checkbox" id="applicableSrc" value="N" onchange="com_fn.checkAplicable(this, 'srcBtn')" /><label for="applicableSrc">Not Applicable</label></span>
						</div>
						<form id="srcUploadForm" class="srcBtn">
							<input type="hidden" id="srcCsvFileId" value="${project.srcCsvFileId }">
							<dl class="uploadCase" id="srcUploadSearch">
								<dt>Upload Analysis Result</dt>
								<dd>
									<div class="basicCase">
										<div class="uploadTit">
											<span class="checkSet"><label for="2">FOSSLight Report</label></span>	
										</div>
										<div class="uploadGroup">
											<div class="uploadSet">
												<span class="fileex_back" <c:if test="${isCommited}">style="display:none;"</c:if>>
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
																	<input type="button" value="Delete" class="smallDelete" onclick="src_fn.deleteCsv(this, '1')" <c:if test="${isCommited}">style="display:none;"</c:if>/>
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
						<form id="srcProjectForm" class="srcBtn">
							<input type="hidden" name="publicYn" value="Y">
							<dl class="fideCase" id="srcProjectSearch" style="display:none;">
								<dt>Project Search Area</dt>
								<dd>
									<label>Project Name</label>
									<input type="text" class="autoComProjectNmConf" name="prjName"/>
								</dd>
								<dd>
									<label>Project Version</label>
									<input type="text" name="prjVersion"/>
								</dd>
								<dd class="sBtnArea"><input id="srcProjectSearchBtn" type="button" value="Search" class="btnColor black wauto srcBtn" /></dd>
							</dl>
						</form>
					</fieldset>
					<div class="srcProjectSearch editSearchUp srcBtn">
						<div class="srcProject1" style="display:none;">
							<div class="tRStit">
								<h3>Search Results</h3>
								<br/>
							</div>
							<div class="jqGridSet">
								<table id="_srcProjectList1"><tr><td></td></tr></table>
								<div id="srcProjectPager1"></div>
							</div>
						</div>
						<!---->
						<br/>
						<div class="srcProject2" style="display:none;">
							<div class="tRStit">
								<h3>Detail Preview</h3>
								<span class="right"><input type="button" value="Load" class="btnCLight" onclick="src_fn.showDialog()"/></span>
							</div>
							<div class="jqGridSet">
								<table id="_srcProjectList2"><tr><td></td></tr></table>
							</div>
						</div>
					</div>
					<br>
					<div class="srcBtn">
						<div class="srcProject3">
							<div class="tRStit">
								<h3>Loaded List</h3>
								<br/>
							</div>
							<div class="jqGridSet">
								<table id="_srcAddList"><tr><td></td></tr></table>
							</div>
						</div>
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
				<div class="btnLayout">
                    <span class="left">
                    	<c:if test="${ct:isAdmin() and project.dropYn ne 'Y'}">
	                        <input type="button" value="OSS bulk registration" onclick="fn_grid_com.ossBulkReg('${project.prjId}','11')" class="btnColor red" style="width: 145px;" />
                    	</c:if>
                    	<c:if test="${project.dropYn ne 'Y' and (ct:isAdmin() or project.viewOnlyFlag eq 'N')}">
                    		<input type="button" value="Check OSS Name" onclick="com_fn.CheckOssViewPage('SRC')" class="btnColor red srcBtn btnCheck" style="width: 115px;" />
                    		<input type="button" value="Check License" onclick="com_fn.CheckOssLicenseViewPage('SRC')" class="btnColor red srcBtn btnCheck" style="width: 100px;" />
                    		<input type="button" value="Bulk Edit" onclick="com_fn.bulkEdit('SRC')" class="btnColor btnColor red idenEdit" />
                    	</c:if>
                    </span>
                    <span class="right">
                        <input type="button" value="Export" onclick="src_fn.downloadExcel()" class="btnColor red btnExpor srcBtn" />
                        <input type="button" value="Yaml" class="btnColor red btnExport" onclick="com_fn.downloadYaml('SRC')"/>
                        <c:if test="${project.dropYn ne 'Y'}">
	                        <input id="srcResetUp" type="button" value="Reset" class="btnColor btnReset srcBtn idenReset" />
	                        <input id="srcSaveUp" type="button" value="Save" class="btnSave btnColor red idenSave"/>
                        </c:if>
						<div class="pop savePop">
							<div class="popdata">
								<p>The following open source and license names will be changed to names registered on the system for efficient management.</p>
								<dl class="openSourceArea">
								</dl>
								<dl class="licenseArea">
								</dl>
							</div>
							<div class="pbtn">
								<input type="button" value="Cancel" class="btnCancel btnColor" id="nicknameCancel"/>
								<input type="button" value="OK" class="btnColor red" id="nicknameOk"/>
							</div>
						</div>
                    </span>
                </div>
				<!---->
				<div class="jqGridSet srcBtn">
					<table id="srcList"><tr><td></td></tr></table>
					<div id="srcPager"></div>
				</div>
			</div>
		</div>
<!-- BIN start ************************************************************************************************************ -->
		<div id="binDiv" class="tabContent">
			<div class="projectContents">
				<div class="orangeBox">
					<input type="button" value="btnToggle" class="btnToggle">
					<fieldset class="editSearchUp">
						<div class="sukind">
							<span class="radioSet binBtn"><input type="radio" id="binR1" name="binSelectOption" onchange="bin_fn.changeSelectOption()" value="1" checked <c:if test="${isCommited}">disabled</c:if> /><label for="binR1">Upload Analysis Result</label></span>
							<span class="radioSet binBtn"><input type="radio" id="binR2" name="binSelectOption" onchange="bin_fn.changeSelectOption()" value="2" <c:if test="${isCommited}">disabled</c:if> /><label for="binR2">Project Search</label></span>
							<span class="checkSet"><input type="checkbox" id="applicableBin" value="N" onchange="com_fn.checkAplicable(this, 'binBtn')"/><label for="applicableBin">Not Applicable</label></span>
						</div>
						<form id="binUploadForm" class="binBtn">
							<input type="hidden" id="binCsvFileId" value="${project.binCsvFileId }"/>
							<input type="hidden" id="binBinaryFileId" value="${project.binBinaryFileId }"/>
							<dl class="uploadCase" id="binUploadSearch">
								<dt>Upload Binary Analysis Result</dt>
								<dd>
									<div class="androidCase" style="border-top: none;">
										<div class="uploadTit">
											<span class="checkSet"><label for="2">Binary Analysis Result</label></span>	
										</div>
										<div class="uploadGroup">
											<div class="uploadSet">
												<label>FOSSLight Report :</label>
												<span class="fileex_back" <c:if test="${isCommited}">style="display:none;"</c:if>>
													<div id="binCsvFile">upload</div>
												</span>
												<div class="uploadList">
													<ul class="binFileArea">
													<c:forEach var="csvFile" items="${project.binCsvFile }" varStatus="vs">
														<li>
															<span>
																<strong>
																	<a href="<c:url value="/download/${csvFile.fileSeq }/${csvFile.logiNm}"/>">${csvFile.origNm }</a>
																	<br>
																	${csvFile.createdDate}
																	<input type="hidden" value="${csvFile.fileSeq }"/>
																	<input type="button" value="Delete" class="smallDelete" onclick="bin_fn.deleteCsv(this, '1')" <c:if test="${isCommited}">style="display:none;"</c:if>/>
																</strong>
															</span>
														</li>
													</c:forEach>
													</ul>
												</div>
											</div>
											<div class="uploadSet">
												<label>binary.txt :</label>
												<span class="fileex_back" <c:if test="${isCommited}">style="display:none;"</c:if>>
													<div id="binBinaryFile">upload</div>
												</span>
												<div class="uploadList">
													<ul class="binBinaryFileArea">
													<c:forEach var="csvFile" items="${project.binBinaryFile }" varStatus="vs">
														<li>
															<span>
																<strong>
																	<a href="<c:url value="/download/${csvFile.fileSeq}/${csvFile.logiNm}"/>">${csvFile.origNm }</a>
																	<br>
																	${csvFile.createdDate}
																	<input type="hidden" value="${csvFile.fileSeq }"/>
																	<input type="button" value="Delete" class="smallDelete" onclick="bin_fn.deleteCsv(this, '2')" <c:if test="${isCommited}">style="display:none;"</c:if>/>
																</strong>
															</span>
														</li>
													</c:forEach>
													</ul>
												</div>
											</div>
										</div>
									</div>
								</dd>
							</dl>
						</form>
						<form id="binProjectForm" class="binBtn">
							<input type="hidden" name="publicYn" value="Y">
							<dl class="fideCase" id="binProjectSearch" style="display:none;">
								<dt>Project Search Area</dt>
								<dd>
									<label>Project Name</label>
									<input type="text" class="autoComProjectNmConf" name="prjName"/>
								</dd>
								<dd>
									<label>Project Version</label>
									<input type="text" name="prjVersion"/>
								</dd>
								<dd class="sBtnArea"><input id="binProjectSearchBtn" type="button" value="Search" class="btnColor black wauto binBtn" /></dd>
							</dl>
						</form>
					</fieldset>
					<div class="binProjectSearch editSearchUp binBtn">
						<div class="binProject1" style="display:none;">
							<div class="tRStit">
								<h3>Search Results</h3>
								<br/>
							</div>
							<div class="jqGridSet">
								<table id="_binProjectList1"><tr><td></td></tr></table>
								<div id="binProjectPager1"></div>
							</div>
						</div>
						<!---->
						<br/>
						<div class="binProject2" style="display:none;">
							<div class="tRStit">
								<h3>Detail Preview</h3>
								<span class="right"><input type="button" value="Load" class="btnCLight" onclick="bin_fn.showDialog()"/></span>
							</div>
							<div class="jqGridSet">
								<table id="_binProjectList2"><tr><td></td></tr></table>
							</div>
						</div>
					</div>
					<br>
					<div class="binBtn">
						<div class="binProject3">
							<div class="tRStit">
								<h3>Loaded List</h3>
								<br/>
							</div>
							<div class="jqGridSet">
								<table id="_binAddList"><tr><td></td></tr></table>
							</div>
						</div>
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
				<div class="btnLayout">
                    <span class="left">
                    	<c:if test="${ct:isAdmin() and project.dropYn ne 'Y'}">
	                       <input type="button" value="OSS bulk registration" onclick="fn_grid_com.ossBulkReg('${project.prjId}','15')" class="btnColor red" style="width: 145px;" />
                    	</c:if>
                    	<c:if test="${project.dropYn ne 'Y' and (ct:isAdmin() or project.viewOnlyFlag eq 'N')}">
                    		<input type="button" value="Check OSS Name" onclick="com_fn.CheckOssViewPage('BIN')" class="btnColor red binBtn btnCheck" style="width: 115px;" />
                    		<input type="button" value="Check License" onclick="com_fn.CheckOssLicenseViewPage('BIN')" class="btnColor red binBtn btnCheck" style="width: 100px;" />
                    		<input type="button" value="Bulk Edit" onclick="com_fn.bulkEdit('BIN')" class="btnColor btnColor red idenEdit" />
                    	</c:if>
                    </span>
                    <span class="right">
                        <input type="button" value="Export" onclick="bin_fn.downloadExcel()" class="btnColor red btnExpor binBtn" />
                        <input type="button" value="Yaml" class="btnColor red btnExport" onclick="com_fn.downloadYaml('BIN')"/>
                        <c:if test="${project.dropYn ne 'Y'}">
	                        <input id="binReset" type="button" value="Reset" class="btnColor btnReset binBtn idenReset" />
	                        <input id="binSave" type="button" value="Save" class="btnSave btnColor red idenSave"/>
                        </c:if>
						<div class="pop savePop">
							<div class="popdata">
								<p>The following open source and license names will be changed to names registered on the system for efficient management.</p>
								<dl class="openSourceArea">
								</dl>
								<dl class="licenseArea">
								</dl>
							</div>
							<div class="pbtn">
								<input type="button" value="Cancel" class="btnCancel btnColor" id="binNicknameCancel"/>
								<input type="button" value="OK" class="btnColor red" id="binNicknameOk"/>
							</div>
						</div>
                    </span>
                </div>
				<div class="jqGridSet binBtn">
					<table id="binList"><tr><td></td></tr></table>
					<div id="binPager"></div>
				</div>
			</div>
		</div>
<!-- BIN Android start ************************************************************************************************************ -->
		<div id="binAndroidDiv" class="tabContent">
			<div class="projectContents">
				<!---->
				<div class="orangeBox">
					<input type="button" value="btnToggle" class="btnToggle">
					<fieldset class="editSearchUp">
						<div class="sukind">
							<span class="radioSet binAndroidBtn"><input type="radio" id="binAndroidR1" name="binAndroidSelectOption" onchange="binAndroid_fn.changeSelectOption()" value="1" checked/><label for="binAndroidR1">Upload Analysis Result</label></span>
							<span class="radioSet binAndroidBtn"><input type="radio" id="binAndroidR2" name="binAndroidSelectOption" onchange="binAndroid_fn.changeSelectOption()" value="2"/><label for="binAndroidR2">Project Search</label></span>
							<span class="checkSet"><input type="checkbox" id="applicableBinAndroid" value="N" onchange="com_fn.checkAplicable(this, 'binAndroidBtn')"/><label for="applicableBinAndroid">Not Applicable</label></span>
						</div>
						<form id="binAndroidUploadForm" class="binAndroidBtn">
							<input type="hidden" id="srcAndroidCsvFileId" value="${project.srcAndroidCsvFileId }"/>
							<input type="hidden" id="srcAndroidNoticeFileId" value="${project.srcAndroidNoticeFileId }"/>
							<input type="hidden" id="srcAndroidNoticeXmlId" value="${project.srcAndroidNoticeXmlId }"/>
							<input type="hidden" id="srcAndroidResultFileId" value="${project.srcAndroidResultFileId }"/>
							<dl class="uploadCase" id="binAndroidUploadSearch">
								<dt>Upload Analysis Result</dt>
								<dd>
									<div class="androidCase" style="border-top: none;">
										<div class="uploadTit">
											<span class="checkSet"><label for="2">Android Model</label></span>	
										</div>
										<span class="sampleDownSet"></span>
										<div class="uploadGroup">
											<div class="uploadSet">
												<label>FOSSLight Report :</label>
												<span class="fileex_back">
													<div id="androidCsvFile">upload</div>
												</span>
												<div class="uploadList">
													<ul class="androidCsvFileArea">
													<c:forEach var="csvFile" items="${project.androidCsvFile }" varStatus="vs">
														<li>
															<span>
																<strong style="max-width:752px;">
																	<a href="<c:url value="/download/${csvFile.fileSeq}/${csvFile.logiNm}"/>">${csvFile.origNm }</a>
																	&nbsp;&nbsp;${csvFile.createdDate}
																	<input type="hidden" value="${csvFile.fileSeq }"/>
																	<input type="button" value="Delete" class="smallDelete" onclick="binAndroid_fn.deleteCsv(this, '2')"/>
																</strong>
															</span>
														</li>
													</c:forEach>
													</ul>
												</div>
											</div>
											<div class="uploadSet">
												<label>NOTICE :</label>
												<span class="fileex_back">
													<div id="androidNoticeFile">upload</div>
												</span>
												<div class="uploadList">
													<ul class="androidNoticeFileArea">
													<c:forEach var="csvFile" items="${project.androidNoticeFile }" varStatus="vs">
														<li>
															<span>
																<strong style="max-width:752px;">
																	<a href="<c:url value="/download/${csvFile.fileSeq}/${csvFile.logiNm}"/>">${csvFile.origNm }</a>
																	&nbsp;&nbsp;${csvFile.createdDate}
																	<input type="hidden" value="${csvFile.fileSeq }"/>
																	<!-- 여기에 xml,tar.gz,zip file의 경우 변환된 html file인지 확인하고 해당 될경우 delete button을 제거함. -->
																	<c:if test="${vs.index eq '0'}"> 
																		<input type="button" value="Delete" class="smallDelete" onclick="binAndroid_fn.deleteCsv(this, '3')"/>
																	</c:if>
																</strong>
															</span>
														</li>
													</c:forEach>
													</ul>
												</div>
											</div>
											<div class="uploadSet">
												<label>result.txt :</label>
												<span class="fileex_back">
													<div id="androidResultFile">upload</div>
												</span>
												<div class="uploadList">
													<ul class="androidResultFileArea">
													<c:forEach var="csvFile" items="${project.androidResultFile }" varStatus="vs">
														<li>
															<span>
																<strong style="max-width:752px;">
																	<a href="<c:url value="/download/${csvFile.fileSeq}/${csvFile.logiNm}"/>">${csvFile.origNm }</a>
																	&nbsp;&nbsp;${csvFile.createdDate}
																	<input type="hidden" value="${csvFile.fileSeq }"/>
																	<input type="button" value="Delete" class="smallDelete" onclick="binAndroid_fn.deleteCsv(this, '4')"/>
																</strong>
															</span>
														</li>
													</c:forEach>
													</ul>
												</div>
											</div>
										</div>
									</div>
								</dd>
								<dd class="androidCase"><input type="button" value="Apply" class="btnColor black wauto" onclick="binAndroid_fn.showDialog('APPLY')"/></dd> <!-- sBtnArea -->
							</dl>
						</form>
						<form id="binAndroidProjectForm" class="binAndroidBtn">
							<input type="hidden" name="publicYn" value="Y">
							<dl class="fideCase" id="binAndroidProjectSearch" style="display:none;">
								<dt>Project Search Area</dt>
								<dd>
									<label>Project Name</label>
									<input type="text" class="autoComProjectNmConf" name="prjName"/>
								</dd>
								<dd>
									<label>Project Version</label>
									<input type="text" name="prjVersion"/>
								</dd>
								<dd class="sBtnArea"><input id="binAndroidProjectSearchBtn" type="button" value="Search" class="btnColor black wauto binAndroidBtn" /></dd>
							</dl>
						</form>
					</fieldset>
					<div class="binAndroidProjectSearch editSearchUp binAndroidBtn">
						<div class="binAndroidProject1" style="display:none;">
							<div class="tRStit">
								<h3>Search Results</h3>
								<br/>
							</div>
							<div class="jqGridSet">
								<table id="_binAndroidProjectList1"><tr><td></td></tr></table>
								<div id="binAndroidProjectPager1"></div>
							</div>
						</div>
						<!---->
						<br/>
						<div class="binAndroidProject2" style="display:none;">
							<div class="tRStit">
								<h3>Detail Preview</h3>
								<span class="right"><input type="button" value="Load" class="btnCLight" onclick="binAndroid_fn.loadToList();"/></span>
							</div>

							<div style="padding-top: 20px;">
								<dl class="uploadCase">
									<dd>
										<div class="androidCase">
											<div class="uploadGroup">
												<div class="uploadSet inblock">
													<label class="left">NOTICE :</label>
													<span class="fileex_back">
														<div id="androidNoticeFileDummy">upload</div>
													</span>
													<div class="uploadList">
														<ul class="androidNoticeFileAreaDummy">
														</ul>
													</div>
												</div>
												<div class="uploadSet inblock">
													<label class="left">result.txt :</label>
													<span class="fileex_back">
														<div id="androidResultFileDummy">upload</div>
													</span>
													<div class="uploadList">
														<ul class="androidResultFileAreaDummy">
														</ul>
													</div>
												</div>
											</div>
										</div>
	
									</dd>
								</dl>
							</div>
							
							<div class="jqGridSet">
								<table id="_binAndroidProjectList2"><tr><td></td></tr></table>
							</div>
						</div>
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
				<div class="btnLayout">
					<span class="left">
						<c:if test="${project.dropYn ne 'Y'}">
	                    	<input type="button" value="Supplement NOTICE.html" onclick="binAndroid_fn.showDialog('NOTICE')" class="btnColor red binAndroidBtn supplementNotice" style="width: 165px;" />
                    	</c:if>
                    	<c:if test="${ct:isAdmin()}">
	                        <input type="button" value="OSS bulk registration" onclick="fn_grid_com.ossBulkReg('${project.prjId}','14')" class="btnColor red" style="width: 145px;" />
	                        <input type="button" value="Check License Text" onclick="fn_grid_com.checkLicenseTextValidation('${project.prjId}')" class="btnColor red" style="width: 130px;" />
                    	</c:if>
                    	<c:if test="${project.dropYn ne 'Y'}">
	                    	<input type="button" value="check License Text" class="downSet btnPackage" id="checkLicenseTextFile" onclick="binAndroid_fn.downloadFile()" style="display:none;float:right;">
	                    	<input type="button" value="Check OSS Name" onclick="com_fn.CheckOssViewPage('ANDROID')" class="btnColor red binAndroidBtn btnCheck" style="width: 115px;" />
	                    	<input type="button" value="Check License" onclick="com_fn.CheckOssLicenseViewPage('ANDROID')" class="btnColor red binAndroidBtn btnCheck" style="width: 100px;" />
	                    	<input type="button" value="Bulk Edit" onclick="com_fn.bulkEdit('BINANDROID')" class="btnColor btnColor red idenEdit" />
                    	</c:if>
                    </span>
                    <span class="right">
                        <input type="button" value="Export" onclick="binAndroid_fn.downloadExcel()" class="btnColor red btnExpor binAndroidBtn" />
                        <input type="button" value="Yaml" class="btnColor red btnExport" onclick="com_fn.downloadYaml('ANDROID')"/>
                        <c:if test="${project.dropYn ne 'Y'}">
	                        <input id="binAndroidReset" type="button" value="Reset" class="btnColor btnReset binAndroidBtn idenReset" />
	                        <input id="binAndroidSave" type="button" value="Save" class="btnSave btnColor red idenSave"/>
                        </c:if>
						<div class="pop savePop">
							<div class="popdata">
								<p>The following open source and license names will be changed to names registered on the system for efficient management.</p>
								<dl class="openSourceArea">
								</dl>
								<dl class="licenseArea">
								</dl>
							</div>
							<div class="pbtn">
								<input type="button" value="Cancel" class="btnCancel btnColor" id="androidNicknameCancel"/>
								<input type="button" value="OK" class="btnColor red" id="androidNicknameOk"/>
							</div>
						</div>
                    </span>
                </div>
				<div class="jqGridSet binAndroidBtn">
					<table id="binAndroidList"><tr><td></td></tr></table>
					<div id="binAndroidPager"></div>
				</div>
			</div>
		</div>
<!-- BOM Start ************************************************************************************************************ -->
		<div id="bomDiv" class="tabContent">
			<div class="projectContents">
				<!---->
				<br/>
				<div class="btnLayout">
					<c:if test="${autoAnalysisFlag}">
						<span class="left">
							<c:if test="${ct:isAdmin() and project.dropYn ne 'Y'}">
								 <input type="button" value="Auto Analysis" class="btnColor red idenAnalysis" onclick="bom_fn.analysisValidation()" style="width:120px;"/>
								 <input type="button" value="Analysis Result" class="btnColor red idenAnalysisResult" onclick="bom_fn.showAnalysisResult()" style="display:none;width:120px;"/>
							 </c:if>
						</span>
					</c:if>
					<input type="hidden" id="mergeYn"  style="display: none;"/>
                    <span class="right">
                        <input type="button" value="Export" class="btnColor red btnExport" onclick="bom_fn.downloadExcel()"/>
                        <input type="button" value="Yaml" class="btnColor red btnExport" onclick="com_fn.downloadYaml('BOM')"/>
                        <c:if test="${project.dropYn ne 'Y'}">
	                        <input id="bomResetUp" type="button" value="Reset" class="btnColor btnReset idenReset" />
	                        <input id="bomSaveUp" type="button" value="Merge And Save" class="btnColor red btnSave idenSave" style="width:120px;"/>
                        </c:if>
                    </span>
                </div>
				<div class="jqGridSet">
					<table id="bomList"><tr><td></td></tr></table>
					<div id="bomPager"></div>
				</div>
			</div>
		</div>
		
<!-- BAT ************************************************************************************************************ -->
		<c:if test="${batFlag}">
			<div id="batDiv" class="tabContent">
				<div class="projectContents">
					<!---->
					<div class="orangeBox">
						<input type="button" value="btnToggle" class="btnToggle">
						<fieldset class="editSearchUp">
							<form>
								<!---->
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
	
</div>
<!-- //wrap -->
<div id="blind_wrap"></div>
<div class="pop ossSelectPop">
	<h1><input type="checkbox" onchange="checkAll('ossSelectPop', this);" class="sheetNum"> Select Sheet</h1>
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

