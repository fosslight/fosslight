<%@ page contentType="text/html; charset=utf-8" pageEncoding="utf-8"%>
<%@ include file="/WEB-INF/constants.jsp"%>
<!-- wrap -->
<div id="wrapIframe">
	<div>
		<div class="tbws1 w1025">
			<form name="ossForm" id="ossForm">
				<input type="hidden" name="ossLicensesJson"/>
				<input type="hidden" name="ossId"/>
				<input type="hidden" name="licenseType" id="licenseType" />
				<input type="hidden" name="obligationType" id="obligationType" />
				<input type="hidden" name="comment"/>
				<input type="hidden" name="ossType" value="${ossType}"/>
				<input type="hidden" name="validationType"/>
				<input type="hidden" name="downloadLocation"/>
				<input type="hidden" name="licenseId" value=""/>
				<input type="hidden" name="deactivateFlag" value="N"/>
				<!-- Main Table [S] -->
				<table class="dCase">
					<colgroup>
						<col width="188"/>
						<col/>
					</colgroup>
					<tbody>
						<tr>
							<th class="dCase txStr">OSS Name</th>
							<td class="dCase">
								<div class="required">
									<input name="ossName" type="text" class="autoComOss w350" value="${ossName}"/><c:if test="${!empty ossId}"><input type="checkbox" id="deactivateFlag" name="deactivateFlag" value="N" style="margin:0 5px;"/>Deactivate</c:if>
									<span class="retxt"></span>
								</div>
							</td>
						</tr>
						<tr>
							<th class="dCase">Nick Name</th>
							<td class="dCase">
								<div class="multiTxtSet">
									<div class="required">
										<span><input type="text" name="ossNicknames" class="w350" onblur="fn.checkNickName(this)"/><input type="button" value="Delete" class="smallDelete"/></span>
										<span class="retxt"></span>
									</div>
								</div>
								<input id="nickAdd" type="button" value="+ Add" class="btnCLight gray"/>
							</td>
						</tr>
						<tr>
							<th class="dCase">OSS Version</th>
							<td class="dCase">
								<div class="required">	
									<input name="ossVersion" type="text" class="w350" value="${ossVersion}"/><span name="ossType"></span>
									<span class="retxt"></span>
								</div>
							</td>
						</tr>
						<tr>
							<th class="dCase txStr">Declared License<br><input type="button" id="btnShowLicenseText" value="Show license text" class="btnCLight gray"></th>
							<td class="dCase">
								<div class="required">
									<input type="hidden" name="licenseDiv"/>
									<div class="licenseMulti">
										<div class="mark"></div>
										<div class="mt5"><table id="_licenseChoice"><tr><td></td></tr></table></div>
										<div id="_licenseChoicePager"></div>
									</div>
								</div>
								<div id="disp_licenseText" style="display: none;"></div>
							</td>
						</tr>
						<tr>
							<th class="dCase">Detected License</th>
							<td class="dCase">
								<div class="multiItemSet multiDetectedLicenseSet">
									<div class="required">
										<span><input type="text" name="detectedLicenses" class="autoComOssLicense w725"/><input type="button" value="Delete" class="smallDelete"/></span>
										<span class="retxt"></span>
									</div>
								</div>
								<input id="detectedLicenseAdd" type="button" value="+ Add" class="btnCLight gray"/>
							</td>
						</tr>
						<tr>
							<th class="dCase">Copyright</th>
							<td class="dCase">
								<textarea name="copyright" class="w100P h150">${copyright}</textarea>
							</td>
						</tr>
						<tr id="lt">
							<th class="dCase">License Type</th>
							<td class="dCase"></td>
						</tr>
						<tr id="ob">
							<th class="dCase">Obligation</th>
							<td class="dCase"></td>
						</tr>
						<tr>
							<th class="dCase">Download Location</th>
							<td class="dCase">
								<div class="multiItemSet multiDownloadLocationSet">
									<div class="required">
										<span><input type="text" name="downloadLocations" class="w725"/><input type="button" value="Delete" class="smallDelete"/></span>
										<span class="urltxt"></span>
									</div>
								</div>
								<input id="downloadLocationAdd" type="button" value="+ Add" class="btnCLight gray"/>
							</td>
						</tr>
						<tr>
							<th class="dCase">Home Page</th>
							<td class="dCase">
								<div class="required">
									<input name="homepage" type="text" class="w100P" placeholder="http://" value="${homepage }"/>
									<span class="urltxt"></span>
								</div>
							</td>
						</tr>
						<tr>
							<th class="dCase">Summary Description</th>
							<td class="dCase"><textarea name="summaryDescription" class="w100P h150">${summaryDescription }</textarea></td>
						</tr>
						<tr>
							<th class="dCase">Attribution</th>
							<td class="dCase"><textarea name="attribution" class="w100P h150">${attribution }</textarea></td>
						</tr>
						<c:if test="${!empty ossId}">
							<c:if test="${projectListFlag}">
								<tr>
									<th class="dCase">Project<br/><input id="listMore" type="button" value="List more" class="btnCLight gray" /></th>
									<td class="dCase">
										<table id="_projectList"><tr><td></td></tr></table>
									</td>
								</tr>
								<tr>
									<th class="dCase">3rd Party<br/><input id="listMore3rd" type="button" value="List more" class="btnCLight gray" /></th>
									<td class="dCase">
										<table id="_partnerList"><tr><td></td></tr></table>
									</td>
								</tr>
							</c:if>
							<c:if test="${not empty vulnInfoList}">
								<tr>
									<th class="dCase">Vulnerability</th>
									<td class="dCase">
										<table id="_vulnInfoList"><tr><td></td></tr></table>
									</td>
								</tr>
							</c:if>
						</c:if>
						<tr>
							<th class="dCase">Comment</th>
							<td class="dCase">
								<div class="grid-container">
									<div class="grid-width-100">
										<div id="editor">${project.comment }</div>
									</div>
								</div>
							</td>
						</tr>
					</tbody>
				</table>
				<!-- Main Table [E] -->
			</form>
		</div>
		<!-- Button Set[S] -->
		<div class="btnLayout w1025">
			<c:if test="${!empty ossId}">
			<input id="delete" type="button" value="Delete" class="btnOssSelect btnColor left" />
			</c:if> 
			<span class="right">
				<c:if test="${!empty ossId}">
				<input id="copy" type="button" value="Copy" class="btnColor" />
				</c:if>
				<input id="save" type="button" value="Save" class="btnColor red" />
			</span>
		</div>
		<c:if test="${not empty ossId}">    
        <div class="tabContent">
            <div class="commentList" style="width:986px">
                <strong class="tit">Comments</strong>
                <div class="commentBack" id="commentListArea"></div>
            </div>
        </div>
		</c:if>
		<!-- Button Set[E] -->
		<!-- Popup -->
	    <div class="pop ossSelectPop" style="position: absolute;">
	        <h1>Select OSS</h1>
	            <div class="red"><strong class="red">This OSS is already in use and can not be just deleted.</strong></div>
	            <div>Please select another OSS to replace this OSS.<br>The name of this OSS will be a nickname of selected OSS.</div>
	        <div class="popdata">
	            <!-- 검색 박스 -->
	            <fieldset class="listSearch2">
	                <form id="listSearch" name="listSearch">
	                    <dl class="basicSearch">
	                        <dt>Basic Search Area</dt>
	                        <dd>
	                            <label>OSS Name</label>
	                            <input name="schOssName" type="text" class="autoCom ui-autocomplete-input"/>
	                        </dd>
	                    </dl>
	                    <input type="button" value="Search" class="btnColor search">
	                </form>
	            </fieldset>
	            <!-- //검색 박스 -->
	            <!-- 검색 리스트 -->
	            <div class="mt20">
	                <table id="_ossSelectList"><tr><td></td></tr></table>
	                <div id="ossSelectListPager"></div>
	            </div>
	            <!-- //검색 리스트 -->
	        </div>
	        <div class="pbtn">
	            <input type="button" value="Cancel" class="btnCancel btnColor" />
	            <input type="button" value="OK" class="btnColor red" />
	        </div>
	    </div>
	    <!-- //Popup -->
	    
		<!-- Popup -->
	    <div class="pop mergeOssCheckPop" style="position: absolute;">
	        <h1>Preview OSS Version Merge</h1>
	        <div class="red"><strong class="red">This OSS has multiple versions. </strong></div>
	        <div>All versions are deleted or merged. Please Check Again.</div>
	        <div style="font-size: 25px;" id="div_mergeOssCheckPop_change"></div>
	        <div class="popdata">
	            <!-- 검색 리스트 -->
	            <div class="mt20">
	                <table id="_ossMergeCheckList"><tr><td></td></tr></table>
	            </div>
	            <!-- //검색 리스트 -->
	        </div>
	        <div class="pbtn">
	            <input type="button" value="Cancel" class="btnCancel btnColor" />
	            <input type="button" value="OK" class="btnColor red" />
	        </div>
	    </div>
	    <!-- //Popup -->
	</div>
	<!---->
</div>
<!-- //wrap -->
<dl name="commentClone">
	<dt>
		<span class="left">
			<strong class="nameArea"></strong> | <span class="dateArea"></span>
		</span>
		<span class="right">
			<input type="button" value="editModify" class="editModify" onclick="modifyComment(this)"/>
			<input type="button" value="editDelete" class="editDelete" onclick="deleteComment(this)"/>
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
				<div id="editor3"></div>
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