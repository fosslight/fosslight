<%@ page contentType="text/html; charset=utf-8" pageEncoding="utf-8" %>
<!DOCTYPE HTML>
<html>
	<%@ include file="/WEB-INF/constants.jsp"%>
	<head>
		<meta charset="utf-8" />
		<meta http-equiv="X-UA-Compatible" content="IE=edge" />
		<title>${ct:getConstDef('APP_NAME')}</title>
		<link rel="stylesheet" type="text/css" href="/css/common.css" />
	</head>
	<body id="login_before">
		<!-- Error : 20150302 -->
		<div class="error_layout"> 
			<strong>${ct:getConstDef('APP_NAME')} - 서비스 이용에 불편을 드려 죄송합니다.</strong>
			<div><strong>찾으시려는 페이지는 존재하지 않거나, 현재 사용할 수 없는 페이지 입니다.</strong></div>
			입력하신 페이지 주소가 정확한지 다시 한번 확인해주시기 바랍니다.<br/>
			주소가 정확하다면 브라우저의 "새로고침" 버튼을 눌러 확인 하시거나,<br/>
			잠시 후에 다시 접속을 시도해 주시기 바랍니다.
			<p>
			</p>  
			<br/>
		</div>
	</body>
</html>