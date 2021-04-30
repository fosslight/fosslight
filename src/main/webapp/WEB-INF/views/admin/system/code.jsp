<%@ page language="java" contentType="text/html; charset=utf-8" pageEncoding="utf-8"%>
<%@ include file="/WEB-INF/constants.jsp"%>
<div id="wrapIframe">
	<!---->
	<div>
		<!----->
		<fieldset class="listSearch">
			<form id="codeSearch" >
				<dl class="basicSearch col2">
					<dt>Basic Search Area</dt>
					<dd>
						<label>Code No</label>
						<input id="codeNo" name="cdNo" type="text" class="autoComCodeNo"/>
					</dd>
					<dd>
						<label>Code Name</label>
						<input id="codeName" name="cdNm" type="text" class="autoComCodeNm"/>
					</dd>
				</dl>
				<input id="search" type="button" value="Search" class="btnColor search" />
			</form>
		</fieldset>
		<div class="btnLayout"></div>
		<div class="jqGridSet">
			<table id="list"><tr><td></td></tr></table>
			<div id="pager"></div>
		</div>
		<!----->
		<div class="btnLayout">
			<span class="right">
				<a id="btnSaveCodeDetail" href="javascript:;" class="btnColor red" style="display: none;">Save</a>
				<!-- <a id="btnAddCodeDetail" href="javascript:;" class="btnColor">Add</a> -->
			</span>
		</div>
		<!----->
		<div class="jqGridSet">
			<table id="list2"><tr><td></td></tr></table>
			<div id="pager2"></div>
		</div>
		<!----->
		<div class="btnLayout"></div>
		<!----->
	</div>
	<!---->
</div>
