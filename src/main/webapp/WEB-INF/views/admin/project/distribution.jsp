<%@ page contentType="text/html; charset=utf-8" pageEncoding="utf-8"%>
<%@ include file="/WEB-INF/constants.jsp"%>
<!-- wrap -->
<div id="wrapIframe">
	<!---->
	<div class="projdecTop">
		<div class="projectInfo">
			<h2>Project Information</h2>
			<ul>
				<li class="first"><span>Project Name</span><strong><c:out value="${project.prjName }"/>
					<span id="editTab" class="btnIcon basic" style="display:inline-block;width:16px;padding:0;margin-left:3px;">Basic Info</span>
					<c:if test="${not empty project.identificationStatus}">
					<span id="identificationTab" class="btnIcon identi" style="display:inline-block;width:16px;padding:0;margin-left:3px;">Identification</span>
					</c:if>
					<c:if test="${project.verificationStatus ne 'NA' and (not empty project.verificationStatus or project.identificationStatus eq 'CONF')}">
					<span id="packagingTab" class="btnIcon packag" style="display:inline-block;width:16px;padding:0;margin-left:3px;">Packaging</span>
					</c:if>
					</strong>
				</li>
				<li><span>Created</span><strong><c:out value="${project.prjUserName }"/>&nbsp;<c:out value="${project.prjDivisionName }"/> (${ct:formatDateSimple(project.createdDate)})</strong></li>
			</ul>
			<a class="right" id="helpLink" style="position:relative; cursor: pointer; top:-37px; right:-75px; display: none;"><img alt="" src="/images/user-guide.png" /></a>
		</div>
		<!---->
		<div class="projdecTab">
			<div class="subTab">
			<div>
				<span>Distribution</span>
			</div>
			<input type="button" value="Comment Edit" class="btnColor commentBtn" />
			<span style="right: 125px; position: absolute; bottom: -23px; font-size: 11px;" >
				<input type="button" value="Show Comment History" class="btnColor purple btnCommentHistory" style="width: 160px; height: 18px;"/>
			</span>
			</div>
		</div>
	</div>
	<div class="commentEditor" style="display:none;">
		<div class="cBtn">
		<input type="button" value="Save & Send comment" class="btnCLight saveEditor" onclick="fn.editorDialog();"/>
		<input type="button" value="Save draft" class="btnCLight" onclick="fn.saveEditor();"/>
		</div>
		<div class="grid-container">
			<div class="grid-width-100">
				<div id="editor"><c:out value="${project.userComment}"/></div>
			</div>
		</div>
	</div>
	<div class="projectContents">
	
		<!---->
		<div class="tbws1 w1025">
			<form id="distributionForm">
			<input type="hidden" name="beforeDistributeName" value=""/>
			<input type="hidden" name="beforeDistributeSoftwareType" value=""/>
			<input type="hidden" name="prjId" value="${project.prjId}"/>
			<input type="hidden" name="noticeFileId" value="${project.noticeFileId}"/>
			<input type="hidden" name="packageFileId" value="${project.packageFileId}"/>
			<input type="hidden" name="packageFileId2" value="${project.packageFileId2}"/>
			<input type="hidden" name="packageFileId3" value="${project.packageFileId3}"/>
			<input type="hidden" name="prjModelJson" value=""/>
			<input type="hidden" name="prjDeleteModelJson" value=""/>
			<input type="hidden" name="distributeDeployTime" value="${project.distributeDeployTime}"/>
			<input type="hidden" name="lastModifiedTime"/>
			<input type="hidden" name="saveFlag" />
			<input type="hidden" name="userComment" id="userComment" />
			<input type="hidden" name="verifyYn" id="verifyYn" value="N"/>
			<input type="hidden" name="distributeName" value="${project.distributeName}"/>
			<input type="hidden" name="distributeSoftwareType" value="${project.distributeSoftwareType}"/>
			<input type="hidden" name="destributionStatus" value="${project.destributionStatus}"/>
			<input type="hidden" name="statusVerifyYn" value="${project.statusVerifyYn}"/>
			<input type="hidden" name="chagnedNoticeYn" value="${project.changedNoticeYn}"/>
			<c:if test="${not empty project.distributeDeployTime}">
				<input type="hidden" name="distributeTarget" value="${project.distributeTarget}"/>
				<input type="hidden" name="distributeMasterCategory" value="${project.distributeMasterCategory}"/>
				<c:set var="disabledFlag" value="disabled='disabled'"  />
			</c:if>
			
			<table class="dCase">
				<colgroup>
					<col width="188" />
					<col />
				</colgroup>
				<tbody>
					<tr>
						<th class="dCase txStr">Description</th>
						<td class="dCase">
							<div class="required">
								<input id="distributeName" type="text" class="w100P" <c:if test="${not empty project.distributeDeployTime}">value="${project.distributeName}"</c:if>/>
								<span class="retxt">This field is required.</span>
							</div>
						</td>
					</tr>
					
					<tr> <!-- 161024 수정 -->
						<th class="dCase txStr">Master Category</th>
						<td class="dCase">
							<div class="required">
								<span class="">
									<select id="distributeMasterCategory" name="distributeMasterCategory" ${disabledFlag}>	
										<option></option>
									</select>
								</span>
								<span class="retxt">This field is required.</span>
							</div>
						</td>
					</tr>
					<tr>
						<th class="dCase txStr">Software / Model</th>
						<td class="dCase">
							<div class="required">
								<span class="">
									<select id="distributeSoftwareType" <c:if test="${not empty project.distributeDeployTime}">value="${project.distributeSoftwareType}"</c:if>>
										<option value=""></option>
										<c:forEach items="${ct:getCodeValues(ct:getConstDef('CD_NOTICE_DEFAULT_SOFTWARE_TYPE'))}" var="code" varStatus="status">
											<option value="${code[0]}" ${code[0] eq project.distributeSoftwareType ? 'selected="selected"' : ''} >${code[1]}</option>
										</c:forEach>
									</select>
								</span>
								<span class="retxt">This field is required.</span>
							</div>
						</td>
					</tr>
					<tr> <!-- 161024 추가 -->
						<th class="dCase">Distribution Site</th>
						<td class="dCase">
							<c:forEach items="${ct:getCodeValues(ct:getConstDef('CD_DISTRIBUTE_CODE'))}" var="code" varStatus="status">
								<c:if test="${code[0] ne 'NA'}">
									<span class="radioSet"><input type="radio" name="distributeTarget" value="${code[0]}" id="radio${status.index+3}" ${empty project.distributeTarget or project.distributeTarget eq code[0] ? 'checked="checked"' : ''} ${disabledFlag}/><label for="radio${status.index+3}">${code[1]}</label></span>								
								</c:if>
							</c:forEach>
						</td>
					</tr>
					<tr> <!-- 161024 수정 -->
						<th class="dCase">OSS Notice</th>
						<td class="dCase licenseFile"><input type="hidden" name="licenseFileName" /></td>
					</tr>
					<tr> <!-- 161024 수정 -->
						<th class="dCase">OSS Package</th>
						<td class="dCase openSourceFile">
							<input type="hidden" name="openSourceFileName" />
							<input type="hidden" name="openSourceFileName2" />
							<input type="hidden" name="openSourceFileName3" />
						</td>
					</tr>
					<tr>
						<th class="dCase">Model Information</th>
						<td class="dCase">
							<span class="fileex_back vtop">
								<div id="modelFile"></div>
							</span>
							<a href="javascript:;" class="btnCLight download right" onclick="fn.downloadModelList()"><span>Download</span></a>
							
							<div class="jqGridSet" style="margin-top: 5px;">
								<span id="validMsgModelList" class="retxt"></span>
								<table id="_modelList"><tr><td></td></tr></table>
								<div id="pagerModel"></div>
							</div>
							<div class="mt5">
								<!-- <input id="rowAdd"type="button" value="Add Row" class="btnCLight gray left" /> --> 
								<input id="allDelete"type='button' value='Delete All' class='btnCLight darkgray right' />
							</div>
							<div class="jqGridSet" style="margin-top: 5px;">
								<table id="_modelDeleteList"><tr><td></td></tr></table>
							</div>
						</td>
					</tr>
				</tbody>
			</table>
			</form>
		</div>
		<!---->
		<div class="btnLayout w1025">
            <span class="left">
				<c:if test="${(not empty project.distributeDeployTime and ct:isAdmin() and project.completeYn ne 'Y') and project.viewOnlyFlag eq 'N'}">
	                <input id="resetDistribution" type="button" value="Reject" class="btnColor brown saveClick wauto" />
	            </c:if>
            </span>
            <c:if test="${project.viewOnlyFlag eq 'N' and project.dropYn ne 'Y'}">
				<span class="right">
					<input id="save" type="button" value="Save & Check" class="btnColor red saveClick wauto" /> <!-- 161024 수정 -->
				</span>
			</c:if>
		</div>
		<!---->	
		<div class="lastStep" style="display:none;"> <!-- 161024 수정 -->
			<div class="editSearchUp grayBox">
				<h3>Distribution Information (To be Updated)</h3>
				<ul class="distributeList"></ul>
				<!---->
				<div class="distSearch mt20">
					<div class="tRStit">
						<h3>Category Permission</h3>
					</div>
					<div class="jqGridSet">
						<table id="_list2"><tr><td></td></tr></table>
						<div id="pagerModel"></div>
					</div>
				</div>
			</div>
			<!---->
			<div class="btnLayout w1025">
				<span class="right">
					<c:choose>
						<c:when test="${project.destributionStatus eq ct:getConstDef('CD_DTL_DISTRIBUTE_STATUS_RESERVE')}">
							<input id="cancel" type="button" value="Cancel Distribute" class="btnColor red wauto" onclick="fn.cancelDistributeReserve();" />
						</c:when>
						<c:otherwise>
							<c:if test="${not empty project.distributeDeployTime and ct:getConstDef('CD_DTL_DISTRIBUTE_STATUS_DEPLOIDED') ne project.destributionStatus}">
								<input id="only" type="button" value="Distribute Model Only" class="btnColor red wauto" onclick="fn.distributeModelOnly();" />
							</c:if>
							<input id="distribute" type="button" value="Distribute" class="btnColor red wauto" onclick="fn.distributeAll();" />
						</c:otherwise>
					</c:choose>
				</span>

			</div>
		</div>	
		<!---->			
	</div>
	<!---->
</div>
<!-- //wrap -->