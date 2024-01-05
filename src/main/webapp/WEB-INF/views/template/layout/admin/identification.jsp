<%@ page contentType="text/html; charset=utf-8" pageEncoding="utf-8"%>
<%@ include file="/WEB-INF/constants.jsp"%>
<tiles:insertAttribute name="scripts" />
<tiles:insertAttribute name="common-js" />
<c:if test="${project.androidFlag eq 'N'}">
	<tiles:insertAttribute name="3rd-js" />
	<tiles:insertAttribute name="dep-js" />
	<tiles:insertAttribute name="src-js" />
	<tiles:insertAttribute name="bin-js" />
	<tiles:insertAttribute name="bom-js" />
</c:if>
<c:if test="${project.androidFlag eq 'Y'}">
	<tiles:insertAttribute name="binAndroid-js" />
</c:if>
<div id="loading_wrap" class="loading" style="display:none;">
	<div class="loadingBlind"></div>
	<img src="${ctxPath}/images/loading.gif" alt="loading" />
</div>
<tiles:insertAttribute name="tabs" />