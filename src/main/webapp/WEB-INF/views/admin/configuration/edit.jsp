<%@ page contentType="text/html; charset=utf-8" pageEncoding="utf-8"%>
<%@ include file="/WEB-INF/constants.jsp"%>
<!-- wrap -->
<div id="wrapIframe">
	<!---->
	<div>
		<div class="tbws1 w1025" id="userInfoArea">
			<table class="dCase">
				<colgroup>
					<col width="188" />
					<col />
				</colgroup>
				<tbody>
					<tr>
						<th class="dCase txStr">User Name</th>
						<td class="dCase">
							<input type="text" name="userName" value="${userInfo.userName}" style="width: 200px;"/>
						</td>
					</tr>
					<tr>
						<th class="dCase txStr">Division</th>
						<td class="dCase">
							<div>
								<span class="selectSet" style="width: 200px;">
									<strong title="Division selected value"></strong>
									<select name="division">
										${ct:genOptionSelected(ct:getConstDef("CD_USER_DIVISION"), userInfo.division)}
									</select>
								</span>	
							</div>
						</td>
					</tr>
					<c:if test="${ct:getCodeExpString(ct:getConstDef('CD_SYSTEM_SETTING'), ct:getConstDef('CD_LDAP_USED_FLAG')) eq 'N'}">
						<tr>
							<th class="dCase"><span class="radioSet">
								<c:choose>
									<c:when test="${userInfo.userId eq 'admin' and 'Y' eq adminLockFlag}">
										<input type="checkbox" id="passwordEnabled" onclick="alert('Changing admin information is blocked.'); return false;"/>
									</c:when>
									<c:otherwise>
										<input type="checkbox" id="passwordEnabled"/>
									</c:otherwise>
								</c:choose>
								<label for="single">Password</label></span></th>
							<td class="dCase">
								<input type="password" name="password" id="password" value="" style="width: 200px;" disabled="disabled" />
							</td>
						</tr>
					</c:if>
				</tbody>
			</table>
		</div>
		<!---->
		<div class="btnLayout w1025">
			<input type="button" value="change" class="btnColor red right" onclick="fn.changePassword()"/>
		</div>
		<!---->
		
		<!---->
		<div class="tbws1 w1025">
			<form name="ConfigurationForm" id="ConfigurationForm">
				<table class="dCase">
					<colgroup>
						<col width="188" />
						<col />
					</colgroup>
					<tbody>
						<tr>
							<th class="dCase txStr" rowspan="2">Setting</th>
							<td class="dCase">
								<span class="selectSet" style="width: 200px;">
									<strong title="DefaultLocale selected value"></strong>
									<select name="defaultLocale">
										<c:forEach var="code" items="${ct:getCodeValues(ct:getConstDef('CD_DEFAULT_LOCALE'))}" varStatus="status">
											<option value="${code[3]}" ${code[3] eq sessUserInfo.defaultLocale ? 'selected="selected"' : '' }>${code[1]}</option>
										</c:forEach>
									</select>
								</span>
							</td>
						</tr>
						<tr>
							<td class="dCase">
								<div class="required"><br/>
									<c:forEach var="code" items="${ct:getCodeValues(ct:getConstDef('CD_DEFAULT_TAB'))}" varStatus="status">
										<span class="radioSet"><input type="checkbox" name="defaultTab" id="defaultTab${code[0]}" value="${code[0]}"/><label for="single">${code[1]}</label></span><br/><br/>
									</c:forEach>
								</div>
							</td>
						</tr>
					</tbody>
				</table>
			</form>
		</div>
		<!---->
		<div class="btnLayout w1025">
			<input id="save" type="button" value="Save" class="btnColor red right" />
		</div>
		
		<div class="tbws1 w1025">
			<table class="dCase">
				<colgroup>
					<col width="188" />
					<col />
				</colgroup>
				<tbody>
					<tr>
						<th class="dCase txStr">Default Search Conditions</th>
						<td class="dCase">
							<span class="selectSet" style="width: 200px;">
								<strong title="DefaultSearch selected value"></strong>
								<select id="defaultSearch" name="defaultSearch" onchange="fn.loadSearchCondition();">
									<option value=""></option>
									<option value="LICENSE">License List</option>
									<option value="OSS">OSS List</option>
									<option value="PROJECT">Project List</option>
									<option value="THIRD_PARTY">3rd Party List</option>
									<option value="SELF_CHECK">Self-check List</option>
								</select>
							</span>

						</td>
					</tr>
				</tbody>
			</table>
		</div>
		<div id="searchConditionArea" style="display: none;"></div>
		<div class="btnLayout w1025" id="searchConditionBtnArea" style="display: none;">
			<input id="saveSearchCondition" type="button" value="Save" onclick="fn.updateSearchCondition();" class="btnColor red right" />
		</div>
		
		<!---->

	</div>
	<!-- //wrap --> 

</div>