<%@ page contentType="text/html; charset=utf-8" pageEncoding="utf-8"%>
<script>
	/*
		* data list
		1. License Name
		2. License Type
		3. Obligation
		4. Short Idetifier
		5. Nick Name
		6. Web page for the license
		7. Description
		8. License Text
		9. Attribution
	*/
	
	$(document).ready(function(){
		'use strict';
		data.init();
	});
	
	var data = {
		detail : ${empty detail ? 'null':detail},
		clone : '',
		typeCodes : [],
		init : function(){
			data.clone = $('.multiTxtSet').clone().html();
			
			if(data.detail){
				$('#webpage a').text(data.detail.webpage);
				$('#webpage a').attr("href",data.detail.webpage);
				$('#licenseText').text(data.detail.licenseText);
				$('#attribution').text(data.detail.attribution);
				
				var obligationFlag = false;
				
				if(data.detail.obligationNotificationYn == 'Y'){
					if(obligationFlag) $('#obligation').append(", ");
					$('#obligation').append("Notice");
					
					obligationFlag = true;
				}
				
				if(data.detail.obligationDisclosingSrcYn == 'Y'){
					if(obligationFlag) $('#obligation').append(", ");
					$('#obligation').append("Source Code");
					
					obligationFlag = true;
				}
				
				if(data.detail.obligationNeedsCheckYn == 'Y'){
					if(obligationFlag) $('#obligation').append(", ");
					$('#obligation').append("Needs Check");
				}
				
				data.detail.licenseNicknames.forEach(function(nickName, index, obj){
					if(nickName!=''){
						if(index > 0) $("#nickNames").append(", ");
						$("#nickNames").append(nickName);
					}					
				});
				
				if(data.detail.restriction){
					$('#restriction').append(data.detail.restriction.replace(/,/gi, ", "));
				}
				
				$('textarea[name=licenseText]').height(240);	
			}
		}
	}

	
	function onError(data, status){
		alertify.error('<spring:message code="msg.common.valid2" />', 0);
    };
</script>