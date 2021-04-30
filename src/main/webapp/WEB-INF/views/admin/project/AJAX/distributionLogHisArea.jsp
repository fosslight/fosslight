<%@ page contentType="text/html; charset=utf-8" pageEncoding="utf-8"%>
<%@ include file="/WEB-INF/constants.jsp"%>
<c:forEach var="item" items="${distributionActionLogs}">
  <dl>
	<dt>
		<span class="left">
			<strong class="nameArea">${ct:getCodeString(ct:getConstDef('CD_DISTRIBUTION_HIS_TYPE'), item.actType)}</strong> | <span class="dateArea">${item.creator} ( ${ct:formatDate(item.createdDate)} )</span>
		</span>
	</dt>
	<dd class="commentContentsArea">${item.actCont}</dd>
  </dl>
</c:forEach>
