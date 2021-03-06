<%@ page language="java" contentType="text/html; charset=utf-8" pageEncoding="utf-8"%>
<!DOCTYPE html>
<html>
	<head>
<!-- 		<meta http-equiv="content-type" content="text/html; charset=UTF-8"> -->
		<meta charset="utf-8" />
		<meta http-equiv="X-UA-Compatible" content="IE=edge" />
		<title>FOSSLight Hub</title>
		<%@ include file="/WEB-INF/constants.jsp"%>
<%-- Add script --%>
<link rel="stylesheet" type="text/css" href="${ctxPath}/css/jquery-ui.css">
<link rel="stylesheet" type="text/css" href="${ctxPath}/css/common.css?${cssVersion}" />
<script type="text/javascript" src="${ctxPath}/js/jquery-1.11.0.min.js"></script>
<script type="text/javascript" src="${ctxPath}/js/jquery-migrate-1.2.1.min.js"></script>
<script type="text/javascript" src="${ctxPath}/js/jquery-ui.min.js"></script>

<script type="text/javascript" src="${ctxPath}/js/jquery.form.min.js"></script>
<script type="text/javascript" src="${ctxPath}/js/basic.js?${jsVersion}"></script>

<!-- alertify -->
<script type="text/javascript" src="${ctxPath}/js/alertifyjs/alertify.min.js"></script>

<link rel="stylesheet" type="text/css" href="${ctxPath}/js/alertifyjs/css/alertify.min.css" />
<link rel="stylesheet" type="text/css" href="${ctxPath}/js/alertifyjs/css/themes/default.min.css" />
		<script>
			if (top.location!= self.location) {
			   top.location = self.location.href;
			}
			
			var etcDomain = "${ct:getConstDef('CD_DTL_ECT_DOMAIN')}";
			
			$(document).ready(function() {
				$("#okRegister, #btnCancel, #btn_login, #btnRegist").css("cursor", "pointer");
				$(".registArea").css("display", "none");
				
				$('#btnRegist').click(function(){
					$(".loginArea").hide();
					$('.registArea').show();
				});// registArea show
				
				$('#btnCancel').click(function(){
					$('.registArea').hide();
					$(".loginArea").show();
				}); // registArea hide
				
				$("#btn_login").click(function() {
					excSubmit();
				});
				
				$('#okRegister').click(function(){
					var test = '${ct:getCodeExpString(ct:getConstDef("CD_REGIST_DOMAIN"), ct:getConstDef("CD_DTL_DEFAULT_DOMAIN"))}';
					if(test) {
						makeEmail();
					} else {
						$("#email").val($("#emailTemp").val());
					}
					registSubmit();
				});
				
				// error message hide ??????
				$("input[name='un'], input[name='up']").focus(function() {
					hideErrMsg();
				}).keydown(function (key) {
					if(key.keyCode == 13){excSubmit();} // Enter Key Evnet
				});
				
				saveLastId();
			});
			
			//save id
			function saveLastId(){
				 // ????????? ???????????? ???????????? ID ?????? ????????????. ????????? ???????????? ?????????.
			    var userInputId = getCookie("userInputId");
			    
			    $("input[name=un]").val(userInputId); 
			     
			    if($("input[name=un]").val() != ""){ // ??? ?????? ID??? ???????????? ?????? ????????? ?????? ???, ?????? ?????? ????????? ID??? ????????? ????????????,
			        $("#saveID").attr("checked", true); // ID ??????????????? ?????? ????????? ??????.
			    }
			    
			    $("#saveID").change(function(){ // ??????????????? ????????? ?????????,
			        if($("#saveID").is(":checked")){ // ID ???????????? ???????????? ???,
			            var userInputId = $("input[name=un]").val();
			            
			            setCookie("userInputId", userInputId, 7); // 7??? ?????? ?????? ??????
			        } else { // ID ???????????? ?????? ?????? ???,
			            deleteCookie("userInputId");
			        }
			    });
			     
			    // ID ??????????????? ????????? ???????????? ID??? ???????????? ??????, ?????? ?????? ?????? ??????.
			    $("input[name=un]").keyup(function(){ // ID ?????? ?????? ID??? ????????? ???,
			        if($("#saveID").is(":checked")){ // ID ??????????????? ????????? ????????????,
			            var userInputId = $("input[name=un]").val();

			            setCookie("userInputId", userInputId, 180); // 180??? ?????? ?????? ??????
			        }
			    });
			}
			
			function setCookie(cookieName, value, exdays){
			    var exdate = new Date();
			    exdate.setDate(exdate.getDate() + exdays);

			    var cookieValue = escape(value) + ((exdays==null) ? "" : "; expires=" + exdate.toGMTString());
			    document.cookie = cookieName + "=" + cookieValue;
			}
			 
			function deleteCookie(cookieName){
			    var expireDate = new Date();
			    expireDate.setDate(expireDate.getDate() - 1);

			    document.cookie = cookieName + "= " + "; expires=" + expireDate.toGMTString();
			}
			 
			function getCookie(cookieName) {
			    cookieName = cookieName + '=';
			    var cookieData = document.cookie;
			    var start = cookieData.indexOf(cookieName);
			    var cookieValue = '';
			    
			    if(start != -1){
			        start += cookieName.length;

			        var end = cookieData.indexOf(';', start);
			        if(end == -1) {
				        end = cookieData.length;
			        }

			        cookieValue = cookieData.substring(start, end);
			    }
			    
			    return unescape(cookieValue);
			}
			
			function registSubmit(){
			    $("#registForm").ajaxForm({
			    	url :'<c:url value="/system/user/saveAjax"/>',
                    type : 'POST',
                    dataType:"json",
                    cache : false,
			        success: onRegistSuccess,
                    error : onError
			     }).submit();
			};
			
			function excSubmit() {
				hideErrMsg();
				
				var formData = $("#loginForm").serialize();
				
				$.ajax({
					url : '<c:url value="/session/login-proc"/>',
					type : "POST",
					data : formData,
					cache : false,
					beforeSend: function(xhr) {
						xhr.setRequestHeader("Accept", "application/json");
					},
					success : onSuccess,
					error : onError
				});
			};
			
            function onSuccess(json, status){
                if(json.response.error) {
                    alertify.error(json.response.message, 0);
                    
                    $("#loginForm div.retxt").html(json.response.message).css("color","red");
                    
                    showErrMsg();
                } else {
                	location.href = '<c:url value="/index?lang=' + json.response.locale + '"/>';

                    return; 
                }
            };
            
  			function onRegistSuccess(json, status){
  				hideErrMsg();
  				
  				if("false" == json.isValid) {
  					alertify.error('<spring:message code="msg.common.valid" />', 0);

  					$('.retxt').text('');

  					showErrMsg();

  					if(json.userId){
  						$('.userId').text(json.userId);
  					}
  					
  					if(json.userPw){
  						$('.userPw').text(json.userPw);
  					}
  					
  					if(json.email){
  						$('.email').text(json.email);
  					}
  					
  					if(json.division){
  						$('.division').text(json.division);
  					}
  					
  					if(json.userName){
  						$('.userName').text(json.userName);
  					}
  				} else {
  					alertify.alert('<spring:message code="msg.common.success" />', function(){
  						location.reload();
  	  				});
  				}
  			};

			function onError(data, status){
                alertify.error('<spring:message code="msg.common.valid2" />', 0);
            }
            
            function showErrMsg() {
                $("div.retxt").show();
            }
            
            function hideErrMsg() {
                $("div.retxt").hide();
            }
            
            function showValidMsg(msgData) {
                hideErrMsg();
                
                $.each(msgData,function(key,value) {
                    if("isValid" != key && "validMsg" != key) {
                        if($('input[name='+key+']').length > 0) {
                            $('input[name='+key+']').next("div.retxt").html(value).show();
                        } else if($('textarea[name='+key+']').length > 0) {
                            $('textarea[name='+key+']').next("div.retxt").html(value).show();
                        } else if($('select[name='+key+']').length > 0) {
                            $('select[name='+key+']').parent().next("div.retxt").html(value).show();
                        } 
                    }
            	});
            }
            
            function emailChange(){
                var value = $("#emailCombo option:selected").val();
                var text = $("#emailCombo option:selected").text();

                if(value == etcDomain){
                	$("#emailTemp").val("").show();
                } else {
                	$("#emailTemp").val(text).hide();   
                }
            }
            
            function divisionChange(){
            	var value = $("#s3 option:selected").text();

            	$('strong[for=s3]').text(value);
            }
            
            function CheckChar() {
            	if(event.keyCode == 64){//@ Special character check
                    alertify.alert('<spring:message code="msg.login.check.char" />', function(){});
            		event.returnValue = false;
            	}
            }
            
           	function makeEmail(){
        		var domain = $("#emailTemp").val();
        		var userId = $("input[name=userId]").val();
        		
        		if(userId != ""){
        			$("#email").val(userId+"@"+domain);
        		}
        	}
        </script>
    </head>
    <body id="login_before">
    	<!-- Login -->
		<div id="login" class="loginArea">
			<div class="back">
				<div class="box">
					<fieldset>
						<div>
							<h1><img src="../images/img_login_logo1.png" alt="FOSSLIGHT" /></h1>
							<form name="loginForm" id="loginForm" action="<c:url value="/session/login-proc"/>">
								<dl>
									<dt><label for="accountInput">ID</label></dt>
									<dd class="required">
										<input type="text" name="un" placeholder="foss.kim" />
									</dd>
									<dt><label for="passwordInput">Password</label></dt>
									<dd class="required">
										<input type="password" name="up"/>
									</dd>
								</dl>
								<input type="button" value="LOGIN" class="btnlogin" id="btn_login" />
								<span class="joinGo">
									<span class="checkSet"><input type="checkbox" id="saveID" /><label for="saveID">SAVE ID</label></span>
									<strong><a class="btnRegist" id="btnRegist">SignUp</a></strong>
								</span>
							</form>
						</div>
					</fieldset>
					<!------------>
					<p><spring:message code="msg.login.description.forgot.pw" /></p>
				</div>
			</div>
		</div>
		<!-- //Login -->
		<!-- Login -->
		<div id="login" class="registArea">
			<div class="back">
				<div class="box joinCase">
					<fieldset>
						<div>
							<h1><img src="../images/img_login_logo2.png" alt="FOSSLIGHT" /><br/>REGISTER</h1>
							<form id="registForm">
								<dl>
									<dt><label>ID</label></dt>
									<dd class="required">
										<input type="text" name="userId" placeholder="foss.kim" onKeypress="CheckChar()"/><div class="retxt userId"></div>
									</dd>
									<dt><label>Password</label></dt>
									<dd class="required">
										<input type="password" name="userPw" /><div class="retxt userPw"></div>
									</dd>
									<dt><label>e-mail</label></dt>
									<dd class="required">
										<c:set var="useDomainFlag" value="${ct:genOption(ct:getConstDef('CD_REGIST_DOMAIN'))}" />
										<c:if test="${not empty useDomainFlag}">
											<span class="selectSet">
												<strong for="emailCombo">${ct:getCodeExpString(ct:getConstDef('CD_REGIST_DOMAIN'), ct:getConstDef('CD_DTL_DEFAULT_DOMAIN'))}</strong>
												<select name="registDomain" id="emailCombo" onchange="emailChange()">
													<option></option>
													${ct:genOption(ct:getConstDef("CD_REGIST_DOMAIN"))}
												</select>
											</span>
										</c:if>
										<input type="text" id="emailTemp" <c:if test="${not empty useDomainFlag}">style="display:none;"</c:if> value="<c:if test="${not empty useDomainFlag}">${ct:getCodeExpString(ct:getConstDef('CD_REGIST_DOMAIN'), ct:getConstDef('CD_DTL_DEFAULT_DOMAIN'))}</c:if>"/>
										<input type="hidden" id="email" name="email" value=""/>
										<div class="retxt email"></div>
									</dd>
									<dt><label>Name</label></dt>
									<dd class="required">
										<input type="text" name="userName" /><div class="retxt userName"></div>
									</dd>
									<dt><label>Division</label></dt>
									<dd class="required">
										<span class="selectSet">
											<strong for="s3">select division</strong>
											<select name="division" id="s3" onchange="divisionChange()">
											<option></option>
											${ct:genOption(ct:getConstDef("CD_USER_DIVISION"))}
											</select>
										</span>
										<div class="retxt division"></div>
									</dd>
								</dl>
								<span class="joinBtn">
									<input type="button" value="SIGN UP" class="btnlogin" id="okRegister" />
									<input type="button" value="CANCEL" class="btnJoinCanel" id="btnCancel" />
								</span>
							</form>
						</div>
					</fieldset>
					<!------------>
				</div>
			</div>
		</div>
		<!-- //Login -->
        <div id="blind_wrap"></div>
    </body>
</html>