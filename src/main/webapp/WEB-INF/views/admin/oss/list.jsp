<%@ page contentType="text/html; charset=utf-8" pageEncoding="utf-8"%>
<%@ include file="/WEB-INF/constants.jsp"%>
<!-- wrap -->
<div id="wrapIframe">
	<!---->
	<div>
		<!---->
		<fieldset class="listSearch">
			<form id="ossSearch" name="ossSearch">
				<input type="hidden" name="type" value="oss">
				<input type="hidden" name="parameter">
				<dl class="basicSearch col2">
					<dt>Basic Search Area</dt>
					<dd>
						<label>OSS Name</label>
						<input name="ossName" type="text" class="autoComOss" value="${searchBean.ossName}" style="width:230px"/>
						<input type="checkbox"id="ossNameAllSearchFlag" ${searchBean.ossNameAllSearchFlag eq 'Y' ? 'checked="checked"' : ''} /><span>&nbsp;Exact Match</span>
						<input type="hidden" name="ossNameAllSearchFlag" value="${searchBean.ossNameAllSearchFlag}"/>
					</dd>
					<dd class="textArea">
						<label>Copyright Text</label>
						<textarea name="copyrights">${searchBean.copyright}</textarea>
					</dd>
					<dd>
						<label>License Name</label>
						<input name="licenseName" type="text" class="autoComLicense" value="${searchBean.licenseName}" style="width:230px"/>
						<input type="checkbox" id="licenseNameAllSearchFlag" ${searchBean.licenseNameAllSearchFlag eq 'Y' ? 'checked="checked"' : ''} /><span>&nbsp;Exact Match</span>
						<input type="hidden" name="licenseNameAllSearchFlag" value="${searchBean.licenseNameAllSearchFlag}"/>
					</dd>
					<dd>
						<label>Description</label>
						<textarea name="summaryDescription">${searchBean.summaryDescription}</textarea>
					</dd>
					<dd>
						<label>Website</label>
						<input name="homepage" type="text" value="${searchBean.homepage}"/>
					</dd>
					<dd>
						<label>Deactivate</label>
						<input type="checkbox" id="deactivateFlag" ${searchBean.deactivateFlag eq 'Y' ? 'checked="checked"' : ''} style="margin:0 5px;"/>
						<input type="hidden" name="deactivateFlag" value="${searchBean.deactivateFlag}"/>
					</dd>
				</dl>
				<c:if test="${ct:isAdmin()}">
				<input type="button" value="Admin Expand apply" class="btnExpand" />
				<dl class="adminSearch" style="display:none; height: 70px;">
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
							</select>
						</span>
					</dd>
					<dd>
						<label>Modified Date</label>
						<input name="mStartDate" id="mStartDate" type="text" class="cal" title="Search Start Date" value="${searchBean.mStartDate}" maxlength="8" autocomplete="off"/> ~ 
						<input name="mEndDate" id="mEndDate" type="text" class="cal" title="Search End Date" value="${searchBean.mEndDate}" maxlength="8" autocomplete="off"/> 
					</dd>
					<dt style="width:20px;"></dt>
					<dd style="padding-top: 6px;">
						<label>License Type</label>
						<span class="selectSet" style="width: 257px;">
							<strong for="licenseType" title="selected value"></strong>
							<select id="licenseType" name="licenseType">
								<option></option>
								${ct:genOption(ct:getConstDef("CD_LICENSE_TYPE"))}
							</select>
						</span>
					</dd>
					<dd style="padding-top: 6px;">
						<label>OSS Type</label>
						<span class="iconSet none" title="None">None</span><input type="checkbox" id="noneLicenseFlag" name="ossTypeSearch" value = "N"/>
						<span class="iconSet multi" title="Multi">Multi</span><input type="checkbox" id="multiLicenseFlag" name="ossTypeSearch" value = "M"/>
						<span class="iconSet dual" title="Dual">Dual</span><input type="checkbox" id="dualLicenseFlag" name="ossTypeSearch" value = "D"/>
						<span class="iconSet vdif" title="Version Difference">v-Diff</span><input type="checkbox" id="versionDiffFlag" name="ossTypeSearch" value = "V"/>
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
				<c:if test="${ct:isAdmin() and project.dropYn ne 'Y'}">
					<input type="button" value="Bulk registration" onclick="createTabInFrame('BulkReg_Oss', '#/oss/ossBulkReg')" class="btnColor red" style="width: 125px;" />
				</c:if>
				<a href="#none" class="btnSet excel" onclick="fn.downloadExcel()"><span>Export</span></a>
				<c:if test="${ct:isAdmin()}">
					<input type="button" value="Add" class="btnColor" onclick="createTabInFrame('New_Opensource', '#<c:url value="/oss/edit"/>')" />
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