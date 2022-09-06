<%@ page language="java" contentType="text/html; charset=utf-8" pageEncoding="utf-8"%>
<%@ include file="/WEB-INF/constants.jsp"%>
<div id="wrapIframe">
	<!---->
	<div>
		<!----->
		<fieldset class="listSearch">
			<form id="modelListSearch" name="modelListSearch">
				<dl class="basicSearch col2">
					<dt>Basic Search Area</dt>
					<dd>
						<label>Created Date</label>
						<input name="schStartDate" id="schStartDate" type="text" class="cal" title="Search Start Date" maxlength="8"/> ~ 
						<input name="schEndDate" id="schEndDate" type="text" class="cal" title="Search End Date" maxlength="8"/>
						<input id="resetDate" type="button" value="Reset" class="btnColor red" style="width:45px !important;"/>
					</dd>
					<dd class="centerAign">
						<label>Division</label>
						<span class="selectSet">
							<strong title="Status selected value"></strong>
							<select name="prjDivision">
								<option value=""></option>
								${ct:genOption(ct:getConstDef("CD_USER_DIVISION"))}
							</select>
						</span>	
					</dd>
					<dd style="width:100% !important">
						<label style="float:left;">All Model List (Software)</label>
						<div class="uploadGroup" style="float:left;">
							<div class="uploadSet">
								<span class="fileex_back">
									<div id="modelListFile">upload</div>
								</span>
							</div>
						</div>
					</dd>
				</dl>
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
	</div>
	<!---->
</div>
