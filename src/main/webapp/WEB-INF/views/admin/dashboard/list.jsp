<%@ page contentType="text/html; charset=utf-8" pageEncoding="utf-8"%>
<%@ include file="/WEB-INF/constants.jsp"%>

<style>
.grdrow {
    overflow:hidden;
}
.grdcol {
    float: left;
    width: 50%;
    padding-left: 10px;
    padding-right: 10px;
    -webkit-box-sizing: border-box;
    -moz-box-sizing: border-box;
    box-sizing: border-box;
    height: auto;
    min-height: 38px;
    overflow: hidden;
}
.grdcol h4 {padding-left:5px;}
.grdcol span{color: #ff5a00;}
.grdcol .reLoad{
	color: #ff5a00;
	font-size: 8px;
	cursor: pointer;
}
</style>

<!-- wrap -->
<div id="wrapIframe">
	<fieldset class="listSearch" style="width: 80%;">
		<div class="grdrow mt10">
     			<ul>
					<c:forEach var="code" items="${ct:getAllValues(ct:getConstDef('CD_DASHBOARD_DETAIL'))}" varStatus="status">
						<li><strong>${code[4]}</strong></li>
					</c:forEach>
				</ul>
		</div>
	</fieldset>
    <div>
      	<div class="grdSet mt20">
			<div class="grdrow mt10">
				<c:if test="${projectFlag || partnerFlag}">
					<div class="grdcol">
						<h4><span style="font-size: 14px;">Opened Jobs</span>&nbsp;&nbsp;<img alt="reload" src="${ctxPath}/images/reload_arrow.png" onclick="reLode('jobsList');" height="22px" width="22px"  style="cursor:pointer; position: relative; top: 5px;" /><c:if test="${ct:isAdmin()}"><a style="float:right; position: relative; top: 5px;"><input type="checkbox" id="reviewerOnly" />&nbsp;Reviewing Projects Only</a></c:if></h4>
						<div class="jqGridSet2 mt5">
				            <table id="jobsList"><tr><td></td></tr></table>
				            <div id="jobsPager"></div>
						</div>
					</div>
					<div class="grdcol">
						<!-- 2018-08-17 choye 변경 -->
						<h4><span style="font-size: 14px;">Recent Comments</span>&nbsp;&nbsp;<img alt="reload" src="${ctxPath}/images/reload_arrow.png" onclick="reLode('commentsList');" height="22px" width="22px"  style="cursor:pointer;position: relative;top: 5px;" /><span style="float:right;position: relative; top: 5px;"><a><input type="checkbox" id="readYnFilter" />&nbsp;Show Unread Comments Only</a> <input class="btnColor purple btnConfirmedHistory" style="width: 130px; height: 18px;" type="button" value="Mark All as Read"></span></h4>
						<div class="jqGridSet2 mt5">
				            <table id="commentsList"><tr><td></td></tr></table>
				            <div id="commentsPager"></div>
						</div>
					</div>
				</c:if>
			</div>
			<div class="grdrow mt10">
				<div class="grdcol">
					<h4><span style="font-size: 14px;">Updated OSS</span>&nbsp;&nbsp;<img alt="reload" src="${ctxPath}/images/reload_arrow.png" onclick="reLode('ossList');" height="22px" width="22px"  style="cursor:pointer;position: relative;top: 5px;" /></h4>
					<div class="jqGridSet2 mt5">
			            <table id="ossList"><tr><td></td></tr></table>
			            <div id="ossPager"></div>
					</div>
				</div>
				<div class="grdcol">
					<h4><span style="font-size: 14px;">Updated License</span>&nbsp;&nbsp;<img alt="reload" src="${ctxPath}/images/reload_arrow.png" onclick="reLode('licenseList');" height="22px" width="22px"  style="cursor:pointer;position: relative;top: 5px;" /></h4>
					<div class="jqGridSet2 mt5">
			            <table id="licenseList"><tr><td></td></tr></table>
			            <div id="licensePager"></div>
					</div>
				</div>
			</div>
		</div>
    </div>
</div>
<!-- //wrap -->