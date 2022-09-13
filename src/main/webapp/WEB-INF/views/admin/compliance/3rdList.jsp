<%@ page language="java" contentType="text/html; charset=utf-8" pageEncoding="utf-8"%>
<%@ include file="/WEB-INF/constants.jsp"%>
<div id="wrapIframe">
	<!---->
	<div>
		<!----->
		<fieldset class="listSearch">
			<form id="3rdPartySearch" name="3rdPartySearch">
				<dl class="basicSearch">
					<dt>Basic Search Area</dt>
					<dd>
						<label>Created Date</label>
						<input name="createdDate1" id="createdDate1" type="text" class="cal" title="Search Start Date" maxlength="8"/> ~ 
						<input name="createdDate2" id="createdDate2" type="text" class="cal" title="Search End Date" maxlength="8"/>
						<input id="resetDate" type="button" value="Reset" class="btnColor red" style="width:45px !important;margin-right:30px;"/> 
					</dd>
					<dd class="centerAign">
						<label>Division</label>
						<span class="selectSet">
							<strong title="Status selected value"></strong>
							<select name="division">
								<option value=""></option>
								${ct:genOption(ct:getConstDef("CD_USER_DIVISION"))}
							</select>
						</span>	
					</dd>
				</dl>
				<input id="search" type="button" value="Search" class="btnColor search" />
				<a class="right" id="helpLink" style="position:absolute; cursor: pointer; top:10px; right:-60px; display:none;"><img alt="" src="${ctxPath}/images/user-guide.png" /></a>
			</form>
		</fieldset>
		<div class="btnLayout mt20" style="padding:5px 0 !important">
			<span class="right">
				<input name="export" type="button" value="Export" class="btnColor"/>
			</span>
		</div>
		<div class="jqGridSet">
			<table id="list"><tr><td></td></tr></table>
			<div id="pager"></div>
		</div>
		<!----->
	</div>
	<!---->
</div>
