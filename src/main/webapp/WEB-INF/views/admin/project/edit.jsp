<%@ page contentType="text/html; charset=utf-8" pageEncoding="utf-8"%>
<script type="text/javascript" src="${ctxPath}/js/tutorial/tutorial-proj-edit.js?${jsVersion}"></script>
<%@ include file="/WEB-INF/constants.jsp"%>
<!-- wrap -->
<div id="wrapIframe">
	<c:if test="${not empty project.prjId}">
	<div class="projdecTop">
		<div class="projectInfo">
			<h2>Project Information</h2>
			<ul>
				<li class="first"><span>Project Name</span>
					<strong>${project.prjName }
						<c:if test="${not empty project.prjId}">
						<span id="identificationTab" class="btnIcon identi" style="display:inline-block;width:16px;padding:0;margin-left:3px;">Identification</span>
						</c:if>
						<c:if test="${project.verificationStatus ne 'NA' and (not empty project.verificationStatus or project.identificationStatus eq 'CONF')}">
						<span id="packagingTab" class="btnIcon packag" style="display:inline-block;width:16px;padding:0;margin-left:3px;">Packaging</span>
						</c:if>
						<c:if test="${distributionFlag and project.destributionStatus ne 'NA' and (not empty project.destributionStatus or project.verificationStatus eq 'CONF')}">
						<span id="distributionTab" class="btnIcon distr" style="display:inline-block;width:16px;padding:0;margin-left:3px;">Distribution</span>
						</c:if>
					</strong>
				</li>
				<li><span>Created</span><strong>${project.prjUserName }&nbsp;${project.prjDivisionName } (${ct:formatDateSimple(project.createdDate)})</strong></li>
			</ul>
		</div>
		<!---->
		<div class="projdecTab">
			<div class="subTab">
			<div>
				<span>Basic Information</span>
			</div>
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
				<div id="editor2">${project.userComment}</div>
			</div>
		</div>
	</div>
	</c:if>
	<input type="button" value=" 📢 Continue Tutorial " id="continue_tutorial" />
	<div class="${not empty project.prjId ? 'projectContents' : ''}">
		<!---->
		<div class="tbws1 w1025 mt10">
			<form name="projectForm" id="projectForm" action="" method="post">
				<input type="hidden" name="prjId" style="display: none;"/>
				<input type="hidden" name="prjModelJson" style="display: none;"/>
				<input type="hidden" name="comment" />
				<input type="hidden" name="userComment" />
				<input type="hidden" name="commId" />
				<input type="hidden" name="statusRequestYn" />
				<input type="hidden" id="refPartnerId" name="refPartnerId" value="${project.refPartnerId}" />
				<input type="hidden" id="identificationStatusConfFlag" value="${project.identificationStatusConfFlag}" />
				<input type="hidden" id="verificationStatusConfFlag" value="${project.verificationStatusConfFlag}" />
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
									<input name="prjName" type="text" value="${project.prjName}" class="autoComProjectNm w100P"/>
									<span class="retxt">This field is required.</span>
								</div>
								<c:if test="${empty project.prjId}"><a class="right" id="helpLink" style="position:absolute; cursor: pointer; top:38px; left:1060px; display:none;"><img alt="" src="${ctxPath}/images/user-guide.png" /></a></c:if>
								<a class="right" id="helpLink" style="position:absolute; cursor: pointer; top:2px; left:1035px; display:none;"><img alt="" src="${ctxPath}/images/user-guide.png" /></a>
							</td>
						</tr>
						<tr>
							<th class="dCase"><spring:message code="msg.common.field.project.version" /></th>
							<td class="dCase">
								<div class="required">
									<input name="prjVersion" value="${project.prjVersion}" type="text" class="w100P"/>
									<span class="retxt">This field is required.</span>
								</div>
							</td>
						</tr>
						<c:if test="${project.viewOnlyFlag ne 'Y'}">
						<tr>
							<th class="dCase"><spring:message code="msg.common.field.permission" /></th>
							<td class="dCase">
								<span>View : </span>
								<span class="radioSet">
									<input type="radio" name="publicYn" value="Y" id="permissionRadio1" ${not empty project && project.publicYn ne 'N' ? 'checked="checked"' : ''} /><label for="permissionRadio1">Everyone</label>
									<input type="radio" name="publicYn" value="N" id="permissionRadio2" ${not empty project && project.publicYn eq 'N' ? 'checked="checked"' : ''} /><label for="permissionRadio2">Creator & Watcher</label>
								</span><br>
								<span>Edit : Creator & Watcher only</span>
							</td>
						</tr>
						</c:if>
						<tr>
							<th class="dCase txStr"><spring:message code="msg.common.field.OS" /></th>
							<td class="dCase">
								<div class="required">
									<span class="selectSet writeSelect">
										<strong title="Operating system selected value"></strong>
										<select id="osType" name="osType" style="width: 300px;">
											<option value=""></option>
											${ct:genOption(ct:getConstDef("CD_OS_TYPE"))}
										</select>
									</span>
									<input id="osTypeEtc" name="osTypeEtc" type="text" disabled="disabled"/>
									<span class="retxt">This field is required.</span>
								</div>
							</td>
						</tr>
						<tr>
							<th class="dCase txStr"><a class="iconSet help left" id="helpLink_distributionType" style="display: none; position:relative; cursor: pointer; left:10px;"></a><spring:message code="msg.common.field.distributionType" /></th>
							<td class="dCase">
								${ct:genRadio(ct:getConstDef("CD_DISTRIBUTION_TYPE"), project.distributionType, project.networkServerType)}
							</td>
						</tr>
						<c:choose>
							<c:when test="${distributionFlag}">

								<tr> <!-- 161024 추가 -->
									<th class="dCase txStr"><a class="iconSet help left" id="helpLink_distributionSite" style="display: none; position:relative; cursor: pointer; left:10px;"></a>Distribution Site</th>
									<td class="dCase">
										<c:forEach items="${ct:getCodeValues(ct:getConstDef('CD_DISTRIBUTE_CODE'))}" var="code" varStatus="status">
											<c:if test="${!empty project.distributeDeployTime or project.destributionStatus eq 'RSV'}">
												<c:if test="${status.index eq 0}">
													<span class="radioSet"><input type="radio" name="distributeTarget" value="${code[0]}" id="radio${status.index+3}" ${empty project.distributeTarget or project.distributeTarget eq code[0] ? 'checked="checked"' : ''} disabled/><label for="radio${status.index+3}">${code[1]}</label></span>								
												</c:if>
												<c:if test="${status.index ne 0}">
													<span class="radioSet"><input type="radio" name="distributeTarget" value="${code[0]}" id="radio${status.index+3}" ${project.distributeTarget eq code[0] ? 'checked="checked"' : ''} disabled/><label for="radio${status.index+3}">${code[1]}</label></span>								
												</c:if>
											</c:if>
											<c:if test="${empty project.distributeDeployTime and project.destributionStatus ne 'RSV'}">
												<c:if test="${status.index eq 0}">
													<span class="radioSet"><input type="radio" name="distributeTarget" value="${code[0]}" id="radio${status.index+3}" ${empty project.distributeTarget or project.distributeTarget eq code[0] ? 'checked="checked"' : ''} /><label for="radio${status.index+3}">${code[1]}</label></span>								
												</c:if>
												<c:if test="${status.index ne 0}">
													<span class="radioSet"><input type="radio" name="distributeTarget" value="${code[0]}" id="radio${status.index+3}" ${project.distributeTarget eq code[0] ? 'checked="checked"' : ''} /><label for="radio${status.index+3}">${code[1]}</label></span>								
												</c:if>								
											</c:if>
										</c:forEach>
									</td>
								</tr>
								<tr>
									<th class="dCase txStr"><a class="iconSet help left" id="helpLink_ossNotice" style="display: none; position:relative; cursor: pointer; left:10px;"></a>OSS Notice</th>
									<td class="dCase">
										<div class="required">
											<c:forEach items="${ct:getCodeValues(ct:getConstDef('CD_NOTICE_TYPE'))}" var="code" varStatus="status">
												<c:choose>
													<c:when test="${fn:toUpperCase(code[0]) eq '80'}">
														<span class="radioSet"><input type="radio" name="noticeType" value="${code[0]}" id="noticeType${status.index}" <c:if test="${code[0] eq project.noticeType}">checked</c:if> style="margin:5px 5px 0 0;"/><label for="noticeType${status.index}">${code[1]}</label></span>
														<span class="selectSet">
															<strong for="noticeTypeEtc" title="selected value"></strong>
															<select id="noticeTypeEtc" name="noticeTypeEtc" style="width: 130px;">
																<option value=""></option>
																${ct:genOptionSelected(ct:getConstDef("CD_PLATFORM_GENERATED"), project.noticeTypeEtc)}
															</select>
														</span>
													</c:when>
													<c:otherwise>
														<span class="radioSet"><input type="radio" name="noticeType" value="${code[0]}" id="noticeType${status.index}" <c:if test="${code[0] eq project.noticeType}">checked</c:if> style="margin:5px 5px 0 0;"/><label for="noticeType${status.index}">${code[1]}</label></span>	
													</c:otherwise>
												</c:choose>
											</c:forEach>
											<span class="retxt">This field is required.</span>
										</div>
									</td>
								</tr>
							</c:when>
							<c:otherwise>
							<!-- 
								<input type="hidden" name="distributionType" 	value="10">
								<input type="hidden" name="networkServerType" 	value="N">
								 -->
								<input type="hidden" name="distributeTarget" 	value="NA">
								<input type="hidden" name="noticeType" 			value="10">
							</c:otherwise>
						</c:choose>
						<tr>
							<th class="dCase txStr"><a class="iconSet help left" id="helpLink_priority" style="display: none; position:relative; cursor: pointer; left:10px;"></a><spring:message code="msg.common.field.priority" /></th>
							<td class="dCase">
								<div class="required">
									<span class="selectSet w150">
										<strong for="priority" title="Watcher part selected value"></strong>
										<select id="priority" name="priority">
											<option value=""></option>
											${ct:genOptionSelected(ct:getConstDef("CD_PROJECT_PRIORITY"), project.priority)}
										</select>
									</span>
									<span class="retxt">This field is required.</span>
								</div>
							</td>
						</tr>
						<tr id="tr_distribute">
							<th class="dCase">Model Information</th>
							<td class="dCase">
								<span class="fileex_back vtop">
									<div id="modelFile"></div>
								</span>
								<a href="javascript:;" class="btnCLight download right" onclick="fn.downloadModelList()"><span>Download</span></a>
								<div class="jqGridSet miCase" style="margin-top: 5px;">
									<table id="_modelList"><tr><td></td></tr></table>
									<div id="pagerModel"></div>
								</div>
								<div class="mt5">
									<!-- <input id="rowAdd"type="button" value="Add Row" class="btnCLight gray left" /> --> 
									<c:if test="${project.viewOnlyFlag eq 'N'}"><input id="saveModel"type='button' value='Save' class='btnCLight red right' style='margin-left:5px; display: none;' /></c:if>
									<input id="allDelete"type='button' value='Delete All' class='btnCLight darkgray right' />
								</div>
								<div id="unabledChangeModelTxt" style="display: none;">
									<c:if test="${project.destributionStatus ne 'NA' and (not empty project.destributionStatus or project.verificationStatus eq 'CONF')}">
										<b><spring:message code="msg.project.unabled.changemodel" /></b><span id="distributionTabModel" class="btnIcon distr">Distribution</span>
									</c:if>	
								</div>
							</td>
						</tr>
						<tr>
							<th class="dCase"><spring:message code="msg.common.field.additionalInformation" /></th>
							<td class="dCase">
								<div class="grid-container">
									<div class="grid-width-100">
										<div id="editor"></div>
									</div>
								</div>
								<c:if test="${(project.viewOnlyFlag ne 'Y') and (project.copyFlag ne 'Y') and (not empty project.prjId) }">
									<div class="right mt5">
										<input id="saveBtn" type='button' value='Save' class='btnCLight red right' style="margin-left:5px;" onclick="fn.editComment();"/>
									</div>
								</c:if>
							</td>
						</tr>
						<c:if test="${project.viewOnlyFlag ne 'Y'}">
						<tr>
							<th class="dCase"><spring:message code="msg.common.field.watcher" /></th>
							<td class="dCase">
								<div class="pb5">
									<span class="selectSet w150">
										<strong for="prjDivision" title="Watcher part selected value">Select Division</strong>
										<select id="prjDivision" name="prjDivision" onchange="fn.selectDivision()">
											<option value="">Select Division</option>
											${ct:genOption(ct:getConstDef("CD_USER_DIVISION"))}
										</select>
									</span>
									<span class="selectSet w350">
										<strong for="prjUserId" title="Watcher name selected value">Select User</strong>
										<select id="prjUserId" name="prjUserId">
										</select>
									</span>
									<input id="addWatcher" type="button" value="+ Add" class="btnCLight gray" />
								</div>
								<div class="pb5">
									<span><input type="text" id="adId" name="adId" style="width:150px" placeholder="Input email" onKeypress="fn.CheckChar()" /></span>
									<c:set var="useDomainFlag" value="${ct:genOption(ct:getConstDef('CD_REGIST_DOMAIN'))}" />
									<c:set var="useDomainFlag" value="${ct:genOption(ct:getConstDef('CD_REGIST_DOMAIN'))}" />
									<c:choose>
										<c:when test="${not empty useDomainFlag}">
											<span class="selectSet w220">
												<strong for="domain" title="Watcher domain selected value">${ct:getCodeExpString(ct:getConstDef('CD_REGIST_DOMAIN'), ct:getConstDef('CD_DTL_DEFAULT_DOMAIN'))}</strong>
												<select id="domain" name="domain">
													${ct:genOption(ct:getConstDef("CD_REGIST_DOMAIN"))}
												</select>
											</span>
											<input type="text" id="emailTemp" class="w220" <c:if test="${not empty useDomainFlag}">style="display:none;" value="${ct:getCodeExpString(ct:getConstDef('CD_REGIST_DOMAIN'), ct:getConstDef('CD_DTL_DEFAULT_DOMAIN'))}"</c:if> onKeypress="fn.CheckChar()"  placeholder="Input your Email Domain" />
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
											<option value="prj">Project List</option>
											<c:if test="${partnerFlag}">
												<option value="par">3rd Party List</option>
											</c:if>
											<c:if test="${batFlag}">
												<option value="bat">BAT List</option>
											</c:if>											
										</select>
									</span>
									<span><input type="text" id="listId" name="listId" style="width:350px" placeholder="Input ID you want to copy"/></span>
									<input id="addList" type="button" value="+ Add" class="btnCLight gray" />
								</div>
								<div id="multiDiv" class="multiTxtSet2">
								</div>
							</td>
						</tr>
						</c:if>
						<c:if test="${project.viewOnlyFlag eq 'Y'}">
						<tr>
							<th class="dCase"><spring:message code="msg.common.field.watcher" /></th>
							<td class="dCase">
								<div id="multiDiv" class="multiTxtSet2">
							</td>
						</tr>
						</c:if>
						<c:if test="${not empty project.prjId and 'Y' ne project.copyFlag}">
						<tr>
							<th class="dCase  txStr"><spring:message code="msg.common.field.creator" /></th>
							<td class="dCase">
								<div class="required">
									<input type="text" name="creatorNm" class="autoComCreatorDivision w600" value="" ${ct:isAdmin() ? '' : 'disabled="disabled"'} />
									<span class="retxt">This field is required.</span>
									<input type="hidden" name="creator" <c:if test="${not empty project }">value='${project.creator}'</c:if>/>
								</div>
							</td>
						</tr>
						<tr>
							<th class="dCase txStr"><spring:message code="msg.common.field.division" /></th>
							<td class="dCase">
								<div class="pb5">
									<span class="selectSet w350">
										<strong for="division" title="Watcher part selected value">Select Division</strong>
										<select id="division" name="division" ${ct:isAdmin() ? '' : 'disabled="disabled"'} >
											${ct:genOptionSelected(ct:getConstDef('CD_USER_DIVISION'), project.division)}
										</select>
									</span>
								</div>
							</td>
						</tr>
                        <tr>
                            <th class="dCase  txStr"><spring:message code="msg.common.field.reviewer" /></th>
                            <td class="dCase">
                                <div class="required">
                                    <input type="text" name="reviewer" class="w600" value="${project.reviewerName}" disabled="disabled"/>
                                </div>
                            </td>
                        </tr>
                        </c:if>
                        <c:if test="${empty project.prjId}">
						<tr>
							<th class="dCase">Comment</th>
							<td class="dCase">
								<div class="grid-container">
									<div class="grid-width-100">
										<div id="editor2"></div>
									</div>
								</div>
							</td>
						</tr>
						</c:if>
					</tbody>
				</table>
			</form>
		</div>
		<!---->
		<div class="btnLayout w1025">
			<c:if test="${not empty project.prjId}">
				<span class="left">
					<c:if test="${project.completeYn ne 'Y' and project.viewOnlyFlag eq 'N'}"><input id="delete" type="button" value="Delete" class="btnColor" /></c:if>
					<c:if test="${project.viewOnlyFlag eq 'N'}"><input id="drop" type="button" value="Drop" class="btnColor wauto" style="display: none;"/></c:if>
				</span>
			</c:if>
			<span class="right">
				<input id="copyUrl" type="text" style="width:1px; height:1px; margin:0; padding:0; border: 0;">
				<c:if test="${not empty project.prjId}">
					<input type="button" value="Share URL" class="btnColor red" onclick="fn.shareUrl();" />
				</c:if>
				<c:if test="${project.viewOnlyFlag eq 'N'}">
					<input id="complete" type="button" value="Complete" class="btnColor wauto" style="display: none;"/>
				</c:if>
				<input id="copy" type="button" value="Copy" class="btnColor" onclick="fn.copy();" style="display: none;"/>
				<c:choose>
					<c:when test="${project.dropYn eq 'Y' and project.statusRequestYn eq 'N'}"><input id="reopen" type="button" value="Open" class="btnColor red" /></c:when>
					<c:when test="${project.completeYn eq 'Y' and project.statusRequestYn eq 'N' and ct:isAdmin()}"><input id="reopen" type="button" value="Open" class="btnColor red" /></c:when>
					<c:when test="${project.completeYn eq 'Y' and project.statusRequestYn eq 'Y' and ct:isAdmin()}"><input id="reqToOpen" type="button" value="Requested to Open" class="btnColor red" style="width:130px"/></c:when>
					<c:when test="${project.completeYn eq 'Y' and ct:isAdmin() eq false and project.viewOnlyFlag eq 'N'}"><input id="reqToOpen" type="button" value="Request to Open" class="btnColor red" style="width:110px"/></c:when>
					<c:otherwise>
						<c:if test="${(project.completeYn ne 'Y' or project.dropYn ne 'Y') and project.viewOnlyFlag eq 'N' or empty project.prjId or project.copyFlag eq 'Y'}"><input id="save" type="button" value="Save" class="btnColor red" /></c:if>
					</c:otherwise>
				</c:choose>
			</span>
		</div>
		<!---->
	</div>
	<!---->
</div>
<!-- //wrap -->
<!-- Popup -->
<div id="copyConfirmPopup" class="pop changeStatusPop">
	<h1 class="orange">What status do you want after copying project?</h1>
	<div class="popdata">
		<div class="radioSet" id="CSIdentificationProgress"><input type="radio" id="r1" name="confirmStatusCopyRadio" value="IdentificationProg"><label for="r1">Identification Progress</label></div>
		<div class="radioSet mt10" id="CSIdentificationConfirm"><input type="radio" id="r2" name="confirmStatusCopyRadio" value="IdentificationConf"><label for="r2">Identification Confirm</label></div>
		<div class="radioSet mt10" id="CSVerificationConfirm"><input type="radio" id="r3" name="confirmStatusCopyRadio" value="verificationConf"><label for="r3">Packaging Confirm</label></div>
	</div>
	<div class="pbtn">
		<input id="popCopyConfirmCancel" type="button" value="Cancel" class="btnCancel btnColor" />
		<input id="popCopyConfirmSave" type="button" value="OK" class="btnColor red" />
	</div>
</div>
<!-- //Popup -->