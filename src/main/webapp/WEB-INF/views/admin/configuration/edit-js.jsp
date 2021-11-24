<%@ page contentType="text/html; charset=utf-8" pageEncoding="utf-8"%>
<%@ include file="/WEB-INF/constants.jsp"%>
<script>
	var defaultLocale = '${sessUserInfo.defaultLocale}';
	var defaultTab = '${sessUserInfo.defaultTab}';
	$(document).ready(function(){
		'use strict';
		data.init();
		evt.init();	
	});
	var commentTemp = '';
	var data = {
		init : function(){
			if(!defaultTab) {
				$("[name='defaultTab']:eq(0)").attr("checked", true); // 첫번째 항목을 선택하도록 변경
			} else {
				$.each(defaultTab.split(","), function(idx, val){
					$("#defaultTab"+val).attr('checked', true);
				});
			}

			if(!defaultLocale) {
				$("#selectLang").val("1").prop("selected", true);
			} else {
				$("#selectLang").val(defaultLocale).prop("selected", true);
			}
		},
	};
	
	var evt = {
		init : function(){
			$("#save").on('click',function(){
				//var radioVal = $('input[name="defaultTab"]:checked').val();
				if($('input:checkbox[name="defaultTab"]:checked').length == 0){
					alertify.alert('<spring:message code="msg.configuration.required.selectDefaultTab" />', function(){}); 
					return false;
				}
				
				alertify.confirm('<spring:message code="msg.common.confirm.save" />', function (e) {
					if (e) {
						fn.updateSubmit();
					} else {
						return false;
					}
				});
			});

			$("#saveDefaultLocale").on('click',function(){
				alertify.confirm('<spring:message code="msg.common.confirm.save" />', function (e) {
					if (e) {
						fn.updateDefaultLocale();
					} else {
						return false;
					}
				});
			});
		}			
	};

	var fn = {
		updateDefaultLocale : function (){
			$("#localeConfigurationForm").ajaxForm({
				url :'/configuration/saveDefaultLocaleAjax',
				type : 'POST',
				dataType:"json",
				cache : false,
				success: fn.onUpdateSuccess,
				error : fn.onError
			}).submit();
		},
		updateSubmit : function(){
		    $("#ConfigurationForm").ajaxForm({
	            url :'/configuration/saveAjax',
	            type : 'POST',
	            dataType:"json",
	            cache : false,
		        success: fn.onUpdateSuccess,
	            error : fn.onError
		    }).submit();
		},
		onUpdateSuccess : function(json, status){
			loading.hide();
			if(json.resCd == '10'){
				alertify.alert('<spring:message code="msg.common.success" />', function(){
					top.location.reload();
				}); 
			}else{
				alertify.error('<spring:message code="msg.common.valid2" />', 0);
			}
		},
		onError : function(data, status){
			alertify.error('<spring:message code="msg.common.valid2" />', 0);
	    },
	    changePassword : function(){
			var params = {};
			var password = $("#password").val();

			if(fn.checkPassword(password)){
				$.ajax({
					type: 'POST',
					url: '/system/user/changePassword',
					data: JSON.stringify({'password': password}),
					contentType : 'application/json',
			        success: fn.onUpdateSuccess,
		            error : fn.onError
				});
			}
		},
	    checkPassword : function(password){		    
			// TODO - Password에대해 기준이 정해지면 추가할 예정
			
		    return true;
		}
	};
	
	
</script>