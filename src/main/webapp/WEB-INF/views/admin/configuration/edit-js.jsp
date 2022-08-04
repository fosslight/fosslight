<%@ page contentType="text/html; charset=utf-8" pageEncoding="utf-8"%>
<%@ include file="/WEB-INF/constants.jsp"%>
<script>
	//var defaultLocale = '${sessUserInfo.defaultLocale}';
	var defaultTab = '${sessUserInfo.defaultTab}';
	$(document).ready(function(){
		'use strict';
		data.init();
		evt.init();
        var userDivision = $('#userInfoArea select[name="division"]');
        for(var i=0;i<userDivision.children().length;i++){
            if(userDivision.children()[i].value == ${ct:getConstDef('CD_USER_DIVISION_EMPTY')} ) {
                break;
            }
            if(userDivision.children().length - 1 == i ) {
                userDivision.append("<option value='${ct:getConstDef('CD_USER_DIVISION_EMPTY')}' ></option>");
                if(${sessUserInfo.division} == ${ct:getConstDef('CD_USER_DIVISION_EMPTY')}) {
                    $('#userInfoArea select[name="division"] option:last').attr("selected", "selected");
                    $('#userInfoArea select[name="division"] option:last').change();
                }
            }
        }
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
			$('#ConfigurationForm select[name="defaultLocale"]').trigger('change');
			$('#userInfoArea select[name="division"]').trigger('change');
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
			
			$("#passwordEnabled").on('change', function(){
				var isChecked = $('#passwordEnabled').is(":checked");
				$("#password").attr("disabled", !isChecked);
				$("#password").val('');
			});
		}			
	};

	var fn = {
		loadSearchCondition: function(){
			var searchAreaFlag = $("#defaultSearch option:selected").val();
			$("#searchConditionArea").fadeOut(300);
			$("#searchConditionBtnArea").fadeOut(300);
			
			if(searchAreaFlag != "") {
				$.ajax({
					url : '<c:url value="/configuration/loadDefaultSearchCondition"/>',
					dataType : 'html',
					cache : false,
					data : {'defaultSearchType' : searchAreaFlag},
					success : function(detailResult){
						$("#searchConditionArea").html(detailResult);
						$("#searchConditionArea").fadeIn(300);
						$("#searchConditionBtnArea").fadeIn(300);
					},
					error : function(request,status,error){
						alertify.error('<spring:message code="msg.common.valid2" />', 0);
						$("#defaultSearch").val("");
					}
				});
				
			}
			
		},
		updateSearchCondition: function(){
			// multiple checkbox values to comma separate
			var chkElArr = ["restrictions", "statuses", "status"];
			$.each(chkElArr, function(index, item){ 
				var selectEl = $('#searchConditionForm input[name="'+item+'"]');
				if(selectEl.length > 0) {
					fn.appendFormCheckboxValuesEl(item);
				}
			});
			
		    $("#searchConditionForm").ajaxForm({
		    	url : '<c:url value="/configuration/updateDefaultSearchCondition"/>',
	            type : 'POST',
	            dataType:"json",
	            cache : false,
		        success: fn.onUpdateSuccess,
	            error : fn.onError
		    }).submit();
		},
		appendFormCheckboxValuesEl(target) {
			$('#searchConditionForm input[name="chk_'+target+'"]').remove();
			var addEl = '<input type="hidden" name="chk_'+target+'" value="'+ $('input[name="'+target+'"]:checked').map(function () {return this.value;}).get().join(",") +'" />';
			$("#searchConditionForm").append(addEl);
		},
		updateSubmit : function(){
		    $("#ConfigurationForm").ajaxForm({
		    	url :'<c:url value="/configuration/saveAjax"/>',
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
				alertify.success('<spring:message code="msg.common.success" />');
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
			var userName = $('#userInfoArea input[name="userName"]').val();
			var division = $.trim($('#userInfoArea select[name="division"] option:selected').val());
			var confirmMsg = "";
			if(division === "${ct:getConstDef('CD_USER_DIVISION_EMPTY')}"){
			    confirmMsg = '<spring:message code="msg.configuration.confirm.save"/>';
			} else {
                confirmMsg = '<spring:message code="msg.common.confirm.save"/>'
			}
			alertify.confirm(confirmMsg, function (e) {
				if (e) {
					$.ajax({
						type: 'POST',
						url :'<c:url value="/system/user/updateUserNameAndDivision"/>',
						data: JSON.stringify({'userName':userName, 'division':division, 'password': password}),
						contentType : 'application/json',
						success: function (data) {
							if("true" != data.isValid) {
								if(data.validMsg) {
									alertify.alert(data.validMsg);
								} else {
									alertify.error('<spring:message code="msg.common.valid2" />', 0);
								}
							} else {
								alertify.success('<spring:message code="msg.common.success" />');
							}
						},
			            error : fn.onError
					});
				} else {
					return false;
				}
			});
		}
	};
	
	
</script>