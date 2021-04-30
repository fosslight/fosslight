<%@ page contentType="text/html; charset=utf-8" pageEncoding="utf-8"%>
<%@ include file="/WEB-INF/constants.jsp"%>
<tiles:insertAttribute name="scripts" />
<tiles:insertAttribute name="edit-js" />
<div id="loading_wrap" class="loading" style="display:none;">
	<div class="loadingBlind"></div>
	<img src="/images/loading.gif" alt="loading" />
</div>
<tiles:insertAttribute name="tabs" />