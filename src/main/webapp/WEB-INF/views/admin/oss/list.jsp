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
						<input type="checkbox"id="ossNameAllSearchFlag"/><span>&nbsp;Exact Match</span>
						<input type="hidden" name="ossNameAllSearchFlag" value=""/>
					</dd>
					<dd class="textArea">
						<label>Copyright Text</label>
						<textarea name="copyrights">${searchBean.copyright}</textarea>
					</dd>
					<dd>
						<label>License Name</label>
						<input name="licenseName" type="text" class="autoComLicense" value="${searchBean.licenseName}" style="width:230px"/>
						<input type="checkbox" id="licenseNameAllSearchFlag"/><span>&nbsp;Exact Match</span>
						<input type="hidden" name="licenseNameAllSearchFlag" value=""/>
					</dd>
					<dd>
						<label>Description</label>
						<textarea name="summaryDescription">${searchBean.summaryDescription}</textarea>
					</dd>
					<dd>
						<label>WebSite</label>
						<input name="homepage" type="text" value="${searchBean.homepage}"/>
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
						<input name="cStartDate" id="cStartDate" type="text" class="cal" title="Search Start Date" value="${searchBean.cStartDate}" maxlength="8"/> ~ 
						<input name="cEndDate" id="cEndDate" type="text" class="cal" title="Search End Date" value="${searchBean.cEndDate}" maxlength="8"/> 
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
						<input name="mStartDate" id="mStartDate" type="text" class="cal" title="Search Start Date" value="${searchBean.mStartDate}" maxlength="8"/> ~ 
						<input name="mEndDate" id="mEndDate" type="text" class="cal" title="Search End Date" value="${searchBean.mEndDate}" maxlength="8"/> 
					</dd>
				</dl>
				</c:if>
				<input name="act" type="hidden" value="search"/> 
				<input id="search" type="submit" value="Search" class="btnColor search" />
				<a class="right" id="helpLink" style="position:absolute; cursor: pointer; top:10px; right:-60px; display:none;"><img alt="" src="/images/user-guide.png" /></a>
			</form>
		</fieldset>
		<!---->
		<div class="btnLayout">
			<span class="right">
				<a href="#none" class="btnSet excel" onclick="fn.downloadExcel()"><span>Excel download</span></a>
				<c:if test="${ct:isAdmin()}">
					<input type="button" value="Add" class="btnColor" onclick="createTabInFrame('New_Opensource', '#/oss/edit')" />
				</c:if>
			</span>
		</div>
		<!---->
		<div class="jqGridSet">
			<table id="list"><tr><td></td></tr></table>
			<div id="pager"></div>
		</div>
		<!---->
		<div class="btnLayout">
			<span class="right">
				<a href="#none" class="btnSet excel" onclick="fn.downloadExcel()"><span>Excel download</span></a>
				<c:if test="${ct:isAdmin()}">
					<input type="button" value="Add" class="btnColor" onclick="createTabInFrame('New_Opensource', '#/oss/edit')" />
				</c:if>
			</span>
		</div>
		<!---->
	</div>
	<!---->
</div>
<!-- //wrap -->