<%@ page contentType="text/html; charset=utf-8" pageEncoding="utf-8"%>
<%@ include file="/WEB-INF/constants.jsp"%>
<style>
.grdSet .grdrow:first-child .grdcol {padding:0;border: 1px solid #aaaaaa;background: #f5f4f3;}
.grdSet .grdrow:first-child .grdcol:not(:first-child) {border-left: 0px none;}
.grdSet .grdrow:first-child {margin:0;}
.grdrow {
    margin-left: -10px;
    margin-right: -10px;
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
.grdcol h3 {position:relative;text-align:center;line-height:38px;font-size:16px;}
.grdcol h4 {padding-left:5px;}
</style>

<!-- wrap -->
<div id="wrapIframe">

	<!---->
	<div>
		<div class="historyInfo">
			<h2>History Information</h2>
			<ul>
				<li><span>ID    </span><strong>${basicInfo.idx}</strong></li>
				<li><span>Name    </span><strong>${basicInfo.hTitle}</strong></li>				
				<li><span>Type    </span><strong>${basicInfo.hTypeNm}</strong></li>
				<li><span>Action    </span><strong>${basicInfo.hAction}</strong></li>
				<li><span>Modifier</span><strong>${basicInfo.modifier}(<fmt:parseDate value="${basicInfo.modifiedDate}" var="dateFmt" pattern="yyyy-MM-dd HH:mm:ss"/><fmt:formatDate value="${dateFmt}" pattern="yyyy-MM-dd HH:mm"/>)</strong></li>
				<%-- <li><span>ETC </span><strong>${basicInfo.hEtc}</strong></li> --%>
			</ul>
		</div>
		<!---->
		<div class="jqGridSet mt20">
			<table id="list"><tr><td></td></tr></table>
		</div>
		<c:if test="${fn:length(history.sub) > 0}">
			<div class="grdSet mt20">
				<div class="grdrow">
					<div class="grdcol"><h3>Before</h3></div>
					<div class="grdcol"><h3>After</h3></div>
				</div>
				<c:forEach var="sub" items="${history.sub}" varStatus="status">
					<!-- sub list loof -->
					<div class="grdrow mt10">
						<div class="grdcol">
							<h4><span>${sub.type}</span></h4>
							<div class="jqGridSet2 mt5">
								<table id="subAsList${status.count}" ><tr><td></td></tr></table>
							</div>
						</div>
						<div class="grdcol">
							<h4><span>${sub.type}</span></h4>
							<div class="jqGridSet2 mt5">
								<table id="subBeList${status.count}"><tr><td></td></tr></table>
							</div>
						</div>
					</div>
					<!---->
				</c:forEach>
			</div>
			<!---->
		</c:if>
	</div>
	<!---->

</div>
<!-- //wrap -->