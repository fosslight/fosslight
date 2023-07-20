<%@ page contentType="text/html; charset=utf-8" pageEncoding="utf-8"%>
<%@ include file="/WEB-INF/constants.jsp"%>
<tiles:insertAttribute name="scripts" />
<tiles:insertAttribute name="secCommon-js" />
<tiles:insertAttribute name="secTotal-js" />
<tiles:insertAttribute name="secFixed-js" />
<tiles:insertAttribute name="secNotFixed-js" />
<div id="loading_wrap" class="loading" style="display:none;">
	<div class="loadingBlind"></div>
	<img src="<c:url value="/images/loading.gif"/>" alt="loading" />
</div>
<tiles:insertAttribute name="tabs" />