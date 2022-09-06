<%@ page contentType="text/html; charset=utf-8" pageEncoding="utf-8"%>
<%@ include file="/WEB-INF/constants.jsp"%>
<!-- wrap -->
<div id="wrapIframe">
	<!---->
	<div>
		<!---->
		<fieldset class="listSearch">
			<form id="licenseSearch" name="licenseSearch">
				<dl class="basicSearch col2">
					<dt>Basic Search Area</dt>
					<dd>
						<label>License Name</label>
						<input type="text" name="licenseName" class="autoComLicense" value="${searchBean.licenseName}" style="width:230px"/>
						<input type="checkbox" id="licenseNameAllSearchFlag" value="Y" ${searchBean.licenseNameAllSearchFlag eq 'Y' ? 'checked="checked"' : ''} /><span>&nbsp;Exact Match</span>
						<input type="hidden" name="licenseNameAllSearchFlag" value="${searchBean.licenseNameAllSearchFlag}" />
					</dd>
					<dd class="textArea">
						<label>License Text</label>
						<textarea name="licenseText">${searchBean.licenseText}</textarea>
					</dd>
					<dd>
						<label>License Type</label>
						<span class="selectSet">
							<strong for="licenseType" title="selected value"></strong>
							<select id="licenseType" name="licenseType">	
								<option></option>
								${ct:genOption(ct:getConstDef("CD_LICENSE_TYPE"))}
							</select>
						</span>
					</dd>
					<c:if test="${not empty ct:getCodeValues(ct:getConstDef('CD_LICENSE_RESTRICTION'))}">
						<dd style="width:100%;">
							<label>Restriction</label>
							<span class='checkSet'>
							${ct:genCheckbox(ct:getConstDef("CD_LICENSE_RESTRICTION"), searchBean.restrictions, 'list')}
	            			</span>
						</dd>
					</c:if>
					<dd>
						<label>User Guide</label>
						<textarea name="description" >${searchBean.description}</textarea>
					</dd>
					<dd>
						<label>Website</label>
						<input name="webpage" type="text" value="${searchBean.webpage}"/>
					</dd>
					<dd>
						<label>Obligation Type</label>
						<span class="selectSet">
							<strong for="obligationType" title="selected value"></strong>
							<select id="obligationType" name="obligationType">	
								<option></option>
								${ct:genOption(ct:getConstDef("CD_OBLIGATION_TYPE"))}
								<option value="NONE">None</option>
							</select>
						</span>
					</dd>
				</dl>
				<c:if test="${ct:isAdmin()}">
				<input type="button" value="Admin Expand apply" class="btnExpand" />
				<dl class="adminSearch" style="display:none;">
					<dt style="width:20px;"></dt>
					<dd>
						<label>Creator</label>
						<span class="selectSet">
							<strong for="creator" title="Creator selected value"></strong>
							<select id="creator" name="creator">
								<option></option>
								${ct:genOptionUsers("ROLE_ADMIN")}
							</select>
						</span>
					</dd>
					<dd>
						<label>Created Date</label>
						<input name="cStartDate" id="cStartDate" type="text" class="cal" title="Search Start Date" value="${searchBean.cStartDate}" maxlength="8" autocomplete="off"/> ~ 
						<input name="cEndDate" id="cEndDate" type="text" class="cal" title="Search End Date" value="${searchBean.cEndDate}" maxlength="8" autocomplete="off"/> 
					</dd>
					<dd>
						<label>Modifier</label>
						<span class="selectSet">
							<strong for="modifier" title="Modifier selected value"></strong>
							<select id="modifier" name="modifier">
								<option></option>
								${ct:genOptionUsers("ROLE_ADMIN")}
								<option value="NONE">None</option>
							</select>
						</span>
					</dd>
					<dd>
						<label>Modified Date</label>
						<input name="mStartDate" id="mStartDate" type="text" class="cal" title="Search Start Date" value="${searchBean.mStartDate}" maxlength="8" autocomplete="off"/> ~ 
						<input name="mEndDate" id="mEndDate" type="text" class="cal" title="Search End Date" value="${searchBean.mEndDate}" maxlength="8" autocomplete="off"/> 
					</dd>
				</dl>
				</c:if>
				<input name="act" type="hidden" value="search"/>
				<input id="search" type="submit" value="Search" class="btnColor search" />
				<a class="right" id="helpLink" style="position:absolute; cursor: pointer; top:10px; right:-60px; display:none;"><img alt="" src="${ctxPath}/images/user-guide.png" /></a>
			</form>
		</fieldset>
		<!---->
		<div class="btnLayout">
			<span class="right">
				<a href="#none" class="btnSet excel" onclick="fn.downloadExcel()"><span>Export</span></a>
				<c:if test="${ct:isAdmin()}">
					<input type="button" value="Add" class="btnColor" onclick="createTabInFrame('New_License', '#<c:url value="/license/edit"/>')" />
				</c:if>
			</span>
		</div>
		<!---->
		<div class="jqGridSet">
			<table id="list"><tr><td></td></tr></table>
			<div id="pager"></div>
		</div>
	</div>
	<!---->
</div>
<!-- //wrap --> 