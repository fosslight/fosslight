<%@ page contentType="text/html; charset=utf-8" pageEncoding="utf-8"%>
<%@ include file="/WEB-INF/constants.jsp"%>
<!-- wrap -->
<div id="wrapIframe">
	<!---->
	<div>
		<!---->
		<div class="tbws1 w1025">
			<form name="licenseForm" id="licenseForm" action="" method="post">
				<input type="hidden" name="licenseId" />
				<input type="hidden" name="comment" />
				<input type="hidden" name="restriction" />
				<table class="dCase">
					<colgroup>
						<col width="188" />
						<col />
					</colgroup>
					<tbody>
						<tr>
							<th class="dCase txStr"><spring:message code="msg.common.field.licenseName" /></th>
							<td class="dCase">
								<div class="required">
									<input name="licenseName" type="text" class="autoComLicenseLong w100P"/>
									<span class="retxt"></span>
								</div>
							</td>
						</tr>
						<tr>
							<th class="dCase txStr"><spring:message code="msg.common.field.licenseType" /></th>
							<td class="dCase">
								<div class="required">
									<span class="selectSet w100P">
										<strong for="licenseType" title="selected value"></strong>
										<select id="licenseType" name="licenseType">
											<option></option>
											${ct:genOption(ct:getConstDef("CD_LICENSE_TYPE"))}
										</select>
									</span>
									<span class="retxt"></span>
								</div>
							</td>
						</tr>
						<c:if test="${not empty ct:getCodeValues(ct:getConstDef('CD_LICENSE_RESTRICTION'))}">
						<tr>
							<th class="dCase"><spring:message code="msg.common.field.restriction" /></th>
							<td class="dCase">
								<span class='checkSet'>
								${ct:genCheckbox(ct:getConstDef("CD_LICENSE_RESTRICTION"), licenseInfo.restriction, 'edit')}
		            			</span>
							</td>
						</tr>
						</c:if>
						<tr>
							<th class="dCase"><spring:message code="msg.common.field.obligation" /></th>
							<td class="dCase">
								<span class="checkSet">
									<input name="obligationNotificationYn" class="oblicationChk" type="checkbox" id="noticeChk" value="Y" /><label for="noticeChk">Notice</label>
								</span>
								<span class="checkSet">
									<input name="obligationDisclosingSrcYn" class="oblicationChk" type="checkbox" id="sourceChk" value="Y" /><label for="sourceChk">Source Code</label>
								</span>
							</td>
						</tr>
						<tr>
							<th class="dCase"><spring:message code="msg.common.field.SPDX" /></th>
							<td class="dCase">
								<div class="required">
									<input name="shortIdentifier" type="text" class="w100P" />
									<span class="retxt"></span>
								</div>
							</td>
						</tr>
						<tr>
							<th class="dCase"><spring:message code="msg.common.field.nickname" /></th>
							<td class="dCase">
								<div class="multiTxtSet">	
									<div class="required">								
										<span><input type="text" name="licenseNicknames" style="width: 356px;"/><input type="button" value="Delete" class="smallDelete" /></span>
										<span class="retxt"></span>
									</div>
								</div>
								<input id="nickAdd" type="button" value="+ Add" class="btnCLight gray" />
							</td>
						</tr>
						<tr>
							<th class="dCase"><spring:message code="msg.common.field.licenseWebsite" /></th>
							<td class="dCase">
								<input name="webpage" type="text" class="w100P" placeholder="http://" />
							</td>
						</tr>
						<c:if test="${not empty licenseInfo.internalUrl}">
						<tr>
							<th class="dCase"><spring:message code="msg.common.field.internalURL" /></th>
							<td class="dCase"><a href="${licenseInfo.internalUrl}" target="_blank"><c:out value="${licenseInfo.internalUrl}"/></a></td>
						</tr>
						</c:if>
						<tr>
							<th class="dCase"><spring:message code="msg.common.field.userGuide" /></th>
							<td class="dCase"><textarea name="description" class="w100P h150"></textarea></td>
						</tr>
						<tr>
							<th class="dCase txStr"><spring:message code="msg.common.field.licenseText" /></th>
							<td class="dCase">
								<div class="required">
									<textarea name="licenseText" class="w100P h150"></textarea>
									<span class="retxt"></span>
								</div>
							</td>
						</tr>
						<tr>
							<th class="dCase"><spring:message code="msg.common.field.attribution" /></th>
							<td class="dCase"><textarea name="attribution" class="w100P h150"></textarea></td>
						</tr>
						<tr>
							<th class="dCase">Comment</th>
							<td class="dCase">
								<div class="grid-container">
									<div class="grid-width-100">
										<div id="editor"></div>
									</div>
								</div>
							</td>
						</tr>
					</tbody>
				</table>
			</form>
		</div>
		<!---->
		<div class="btnLayout w1025">
			<input id="delete" type="button" value="Delete" class="btnColor left" style="display: none;" />
			<input id="save" type="button" value="Save" class="btnColor red right" />
		</div>
		<!---->
		<c:if test="${not empty licenseInfo.licenseId }">	
		<div class="tabContent">
	        <div class="commentList" style="width:986px">
	            <strong class="tit">Comments</strong>
	            <div class="commentBack" id="commentListArea"></div>
	        </div>
        </div>
        </c:if>
	</div>
	<!---->
	<!-- //wrap --> 
</div>