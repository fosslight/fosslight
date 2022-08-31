<%@ page contentType="text/html; charset=utf-8" pageEncoding="utf-8"%>
<%@ include file="/WEB-INF/constants.jsp"%>
<!-- wrap -->
<div id="wrapIframe">
<c:if test="${project.viewOnlyFlag eq 'Y'}">
	<c:if test="${not empty project.prjId}">
		<div class="projdecTop">
			<div class="projectInfo">
				<h2>Project Information</h2>
				<ul>
					<li class="first"><span>Project Name</span>
						<strong>${project.prjName }
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
				<c:if test="${ct:isAdmin() or project.publicYn eq 'Y' or project.viewOnlyFlag ne 'Y'}">
					<span style="right: 0px; position: absolute; bottom: -23px; font-size: 11px;" >
						<input type="button" value="Show Comment History" class="btnColor purple btnCommentHistory" style="width: 160px; height: 18px;"/>
					</span>
				</c:if>
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
	<c:if test="${empty message}">
		<div class="${not empty project.prjId ? 'projectContents' : ''}">
			<!---->
			<div class="tbws1 w1025 mt10">
				<form name="projectForm" id="projectForm" action="" method="post">
					<input type="hidden" name="prjId" style="display: none;"/>
					<input type="hidden" name="prjModelJson" style="display: none;"/>
					<input type="hidden" name="comment" />
					<input type="hidden" name="userComment" />
					<!-- 2018-07-19 choye 추가 -->
					<input type="hidden" name="commId" />
					<input type="hidden" name="statusRequestYn" />
					<table class="dCase">
						<colgroup>
							<col width="188" />
							<col />
						</colgroup>
						<tbody>
							<tr>
								<th class="dCase txStr">Project Name</th>
								<td class="dCase">
									<div class="required">${project.prjName}</div>
									<c:if test="${empty project.prjId}"><a class="right" id="helpLink" style="position:absolute; cursor: pointer; top:38px; left:1060px; display:none;"><img alt="" src="<c:url value="/images/user-guide.png"/>" /></a></c:if>
									<a class="right" id="helpLink" style="position:absolute; cursor: pointer; top:2px; left:1035px; display:none;"><img alt="" src="<c:url value="/images/user-guide.png"/>" /></a>
								</td>
							</tr>
							<tr>
								<th class="dCase">Project Version</th>
								<td class="dCase">${project.prjVersion}</td>
							</tr>
							<tr>
								<th class="dCase">Project Status</th>
								<td class="dCase">
									<div>
										<input type="button" id="identificationStatus" class="w150 mr5"><input type="button" id="verificationStatus" class="w150 mr5"><input type="button" id="destributionStatus" class="w150 mr5"><span id="downloadBtn"></span>
									</div>
								 </td>
							</tr>
							<c:if test="${project.viewOnlyFlag ne 'Y'}">
							<tr>
								<th class="dCase">Permission</th>
								<td class="dCase">
									<span>View : ${not empty project && project.publicYn ne 'N' ? 'Everyone' : 'Creator & Watcher'}</span><br>
									<span>Edit : Creator & Watcher only</span>
								</td>
							</tr>
							</c:if>
							<tr>
								<th class="dCase txStr">Operating System</th>
								<td class="dCase">
									<c:forEach var="code" items="${ct:getCodeValues(ct:getConstDef('CD_OS_TYPE'))}" varStatus="status">
										<c:if test="${code[0] eq project.osType and code[0] ne ct:getConstDef('CD_OS_TYPE_ETC')}">${code[1]}</c:if>
										<c:if test="${ct:getConstDef('CD_OS_TYPE_ETC') eq project.osType and code[0] eq ct:getConstDef('CD_OS_TYPE_ETC')}">${project.osTypeEtc}</c:if>
									</c:forEach>
								</td>
							</tr>
							<tr>
								<th class="dCase txStr"><a class="iconSet help left" id="helpLink_distributionType" style="display: none; position:relative; cursor: pointer; left:10px;"></a>Distribution Type</th>
								<td class="dCase">
									<c:forEach var="code" items="${ct:getCodeValues(ct:getConstDef('CD_DISTRIBUTION_TYPE'))}" varStatus="status">
										<c:if test="${code[0] eq project.distributionType}">${code[1]}</c:if>
									</c:forEach><br>
									● Network service only? ${project.networkServerType eq 'Y' ? 'Yes' : 'No'}
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
														<c:if test="${empty project.distributeTarget or project.distributeTarget eq code[0]}">${code[1]}</c:if>								
													</c:if>
													<c:if test="${status.index ne 0}">
														<c:if test="${project.distributeTarget eq code[0]}">${code[1]}</c:if>								
													</c:if>
												</c:if>
												<c:if test="${empty project.distributeDeployTime and project.destributionStatus ne 'RSV'}">
													<c:if test="${status.index eq 0}">
														<c:if test="${empty project.distributeTarget or project.distributeTarget eq code[0]}">${code[1]}</c:if>								
													</c:if>
													<c:if test="${status.index ne 0}">
														<c:if test="${project.distributeTarget eq code[0]}">${code[1]}</c:if>								
													</c:if>								
												</c:if>
											</c:forEach>
										</td>
									</tr>
									<tr>
										<th class="dCase txStr"><a class="iconSet help left" id="helpLink_ossNotice" style="display: none; position:relative; cursor: pointer; left:10px;"></a>OSS Notice</th>
										<td class="dCase">
											<c:forEach items="${ct:getCodeValues(ct:getConstDef('CD_NOTICE_TYPE'))}" var="code" varStatus="status">
												<c:if test="${code[0] eq project.noticeType}">${code[1]}</c:if>
											</c:forEach>
											<c:if test="${ct:getConstDef('CD_NOTICE_TYPE_PLATFORM_GENERATED') eq project.noticeType}">
												<c:forEach items="${ct:getCodeValues(ct:getConstDef('CD_PLATFORM_GENERATED'))}" var="code" varStatus="status">
													<c:if test="${code[0] eq project.noticeTypeEtc}">${code[1]}</c:if>	
												</c:forEach>
											</c:if>
										</td>
									</tr>
								</c:when>
								<c:otherwise>
									<input type="hidden" name="distributeTarget" 	value="NA">
									<input type="hidden" name="noticeType" 			value="10">
								</c:otherwise>
							</c:choose>
							<tr>
								<th class="dCase txStr"><a class="iconSet help left" id="helpLink_priority" style="display: none; position:relative; cursor: pointer; left:10px;"></a>Priority</th>
								<td class="dCase">
									<c:forEach items="${ct:getCodeValues(ct:getConstDef('CD_PROJECT_PRIORITY'))}" var="code" varStatus="status">
										<c:if test="${code[0] eq project.priority}">${code[1]}</c:if>
									</c:forEach>
								</td>
							</tr>
							<tr id="tr_distribute">
								<th class="dCase">Model Information</th>
								<td class="dCase">
									<div class="jqGridSet miCase" style="margin-top: 5px;">
										<table id="_modelList"><tr><td></td></tr></table>
									</div>
								</td>
							</tr>
							<tr>
								<th class="dCase">Additional Information</th>
								<td class="dCase">${project.comment}</td>
							</tr>
							<tr>
								<th class="dCase">Watcher</th>
								<td class="dCase">
									<div id="multiDiv" class="multiTxtSet2"></div>
								</td>
							</tr>
							<c:if test="${ct:isAdmin() and not empty project.prjId and 'Y' ne project.copyFlag}">

							</c:if>
							<c:if test="${not empty project.prjId and 'Y' ne project.copyFlag}">
							<tr>
								<th class="dCase  txStr">Creator</th>
								<td class="dCase">${project.prjUserName}</td>
							</tr>
							<tr>
								<th class="dCase  txStr">Division</th>
								<td class="dCase">
									<c:forEach items="${ct:getCodeValues(ct:getConstDef('CD_USER_DIVISION'))}" var="code" varStatus="status">
										<c:if test="${code[0] eq project.division}">${code[1]}</c:if>
									</c:forEach>
								</td>
							</tr>
	                        <tr>
	                            <th class="dCase  txStr">Reviewer</th>
	                            <td class="dCase">${project.reviewerName}</td>
	                        </tr>
	                        </c:if>
						</tbody>
					</table>
				</form>
			</div>
			<!---->
			<!---->
		</div>
	</c:if>
	<c:if test="${not empty message}">
		${message}
	</c:if>
</c:if>
	<!---->
</div>
<!-- //wrap -->
