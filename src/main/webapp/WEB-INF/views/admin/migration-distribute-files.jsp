<%@ page contentType="text/html; charset=utf-8" pageEncoding="utf-8"%>
<%-- 관리자 화면 템플릿 --%>
<!DOCTYPE html>
<html>
	<head>
		<tiles:insertAttribute name="meta" />
		<tiles:insertAttribute name="scripts" />
		<script type="text/javascript">
		
		</script>
	</head>
	<body>
		<div id="wrap" style="padding-top: 10px;">
			<div  align="center" >
				<table>
					<th>ID</th>
					<th>Project Name</th>
					<th>Project Version</th>
					<th>Last Distribution Time</th>
					<th>OSDD Key</th>
					<th>Notice File</th>
					<th>Notice Result</th>
					<th>Notice Msg</th>
					<th>Source File</th>
					<th>Source Result</th>
					<th>Source Msg</th>
				<c:forEach var="item" items="${prjList}" varStatus="status">
					<tr>
						<td>${item.prjId}</td>
						<td>${item.prjName}</td>
						<td>${item.prjVersion}</td>
						<td>${item.distributeDeployTime}</td>
						<td>${item.distributeOsdKey}</td>
						<td></td>
						<td></td>
						<td></td>
						<td></td>
						<td></td>
						<td></td>
					</tr>
				</c:forEach>
				</table>
			</div>
		</div>
	</body>
</html>