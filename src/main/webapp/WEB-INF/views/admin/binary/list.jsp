<%@ page language="java" contentType="text/html; charset=utf-8" pageEncoding="utf-8"%>
<%@ include file="/WEB-INF/constants.jsp"%>
<div id="wrapIframe">
	<!---->
	<div>
		<!----->
		<fieldset class="listSearch">
			<form id="batSearch" name="batSearch">
				<dl class="basicSearch col3">
					<dt>Basic Search Area</dt>
					<dd>
						<label>Binary Name</label>
						<input name="fileName" type="text" />
					</dd>
					<dd class="centerAign">
						<label>Project Name</label>
						<input type="text" name="parentName"/>
					</dd>
					<dd class="lastAign">
						<label>OSS Name</label>
						<input name="ossName" type="text" class="autoComOss" />
					</dd>
					<dd>
						<label>Updated Date</label>
						<input name="schStartDate" type="text" class="cal" title="Search Start Date" maxlength="8"/> ~ 
						<input name="schEndDate" type="text" class="cal" title="Search End Date" maxlength="8"/> 
					</dd>
					<dd class="centerAign">
						<label>Platform Name</label>
						<input type="text" name="platformName"/>
					</dd>
					<dd class="lastAign">
						<label>OSS Version</label>
						<input type="text" name="ossVersion" />
					</dd>
					<dd>
						<label>TLSH</label>
						<input type="text" name="tlshCheckSum" />
					</dd>
					<dd class="centerAign">
						<label>Platform Version</label>
						<input type="text" name="platformVersion"/>
					</dd>
					<dd class="lastAign">
						<label>License Name</label>
						<input type="text" name="license" />
					</dd>
					<dd>
						<label>Checksum</label>
						<input type="text" name="checkSum" />
					</dd>
				</dl>
				<input id="search" type="button" value="Search" class="btnColor search" />
				<a class="right" id="helpLink" style="position:absolute; cursor: pointer; top:10px; right:-60px; display:none;"><img alt="" src="<c:url value="/images/user-guide.png"/>" /></a>
				<input type="hidden" id="filters" name="filters"/>
			</form>
		</fieldset>
		<div class="btnLayout">
		</div>
		<div class="jqGridSet">
			<table id="list"><tr><td></td></tr></table>
			<div id="pager"></div>
		</div>
		<!----->
		
		<div class="btnLayout">
			<span class="right">
				<a href="#none" class="btnSet excel" onclick="fn.downloadExcel()"><span>Excel download</span></a>
				<c:if test="${ct:isAdmin()}">
				<a id="btnSaveBat" href="javascript:;" class="btnColor red">Save</a>
				</c:if>
			</span>
		</div>
		
	</div>
</div>