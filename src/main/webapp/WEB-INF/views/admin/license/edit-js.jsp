<%@ page contentType="text/html; charset=utf-8" pageEncoding="utf-8"%>
<%@ include file="/WEB-INF/constants.jsp"%>
<script>
	
	$(document).ready(function(){
		'use strict';
		initSample();
		data.init();
		evt.init();	

		if($('input[name=licenseId]').val()) {
			fn_commemt.getCommentList();
		}

		$('.clear.example ').on('click', function() {
			    $('.clear.example .ui.dropdown').dropdown('clear');
		});
	});
	
	var commentTemp = '';
	var data = {
		detail : ${empty detail ? 'null':detail},
		clone : '',
		typeCodes : [],
		init : function(){
			commentTemp = $('<div>').append($('dl[name=commentClone]').clone());
			$('dl[name=commentClone]').remove();
			data.clone = $('.multiTxtSet').clone().html();
			data.cloneWebPage = $('.multiWebPageSet').clone().html();
			
			if(data.detail){
				$('input[name=licenseId]').val(data.detail.licenseId);
				$('select[name=licenseType]').val(data.detail.licenseType).trigger('change');
				$('input[name=licenseName]').val(data.detail.licenseName);
				$('input[name=shortIdentifier]').val(data.detail.shortIdentifier);
				//$('input[name=webpage]').val(data.detail.webpage);
				$('textarea[name=description]').val(data.detail.description);
				$('textarea[name=licenseText]').val(data.detail.licenseText);
				$('textarea[name=attribution]').val(data.detail.attribution);
				
				if(data.detail.obligationDisclosingSrcYn == 'Y'){
					$('input[name=obligationDisclosingSrcYn]').trigger('click');
				}
				
				if(data.detail.obligationNotificationYn == 'Y'){
					$('input[name=obligationNotificationYn]').trigger('click');
				}
				
				if(data.detail.obligationNeedsCheckYn == 'Y'){
					$('input[name=obligationNeedsCheckYn]').trigger('click');
				}
				
				data.detail.licenseNicknames.forEach(function(nickName, index, obj){
					if(index == 0){
						$('.multiTxtSet span:first').remove();
					}
					
					if(nickName!=''){
						$(data.clone).appendTo('.multiTxtSet');
						$('.multiTxtSet input[type=text]:last').val(nickName);
						$('.smallDelete').on('click', function(){
							$(this).parent().remove();
						});
					}					
				});

				if(data.detail.webpages.length <= 0){
					$('input[name=webpages]').val(data.detail.webpage);
				}else{
					data.detail.webpages.forEach(function(webpage, index, obj){
						if(index == 0){
							$('.multiWebPageSet span:first').remove();
						}
											
						if(webpage !=''){
							$(data.cloneWebPage).appendTo('.multiWebPageSet');
							$('.multiWebPageSet input[type=text]:last').val(webpage);
							$('.smallDelete').on('click', function(){
								$(this).parent().remove();
							});
						}					
					});
				}
				
				$('textarea[name=licenseText]').height(240);
				
				// 삭제 버튼 보이기
				$('#delete').show();
			}
		},
	}
	var evt = {
		init : function(){
			//닉네임 인풋 추가
			$('#nickAdd').on('click', function(){
				$(data.clone).prependTo('.multiTxtSet');
				$('.smallDelete').on('click', function(){
					$(this).parent().parent().remove();
				});
			});

			// add web page
			$('#webpageAdd').on('click', function(){
				$(data.cloneWebPage).appendTo('.multiWebPageSet');
				$('.smallDelete').on('click', function(){
					$(this).parent().parent().remove();
				});
			});
			
			// 'Needs Check' check box event
			$("#needsChk").on('click', function(){
				// If this check box is checked, make the others UNCHECKED & READ-ONLY
				if($(this).is(":checked")) {
					$(".oblicationChk").prop("checked", false).attr("disabled", true);
				} else {
					$(".oblicationChk").attr("disabled", false);
					setObligationCheckBox($("#licenseType").val());
				}
			});
			
			//라이센스 등록
			$("#save").on('click',function(){
				alertify.confirm('<spring:message code="msg.common.confirm.save" />', function (e) {
					if (e) {
						registSubmit();
					} else {
						return false;
					}
				});
			});
			
			//라이센스 삭제
			$('#delete').on('click', function(){
				var editorVal = CKEDITOR.instances.editor.getData();
				
				if(editorVal == "") {
					alertify.alert('<spring:message code="msg.license.confirm.delete.required.comment" />', function(){});
					return false;
				}
				
				alertify.confirm('<spring:message code="msg.license.confirm.delete" />', function (e) {
					if (e) {
						$('input[name=comment]').val(editorVal);
						deleteSubmit();
					} else {
						return false;
					}
				});
			});
			
			//고지서 체크 안한상태에서 소스공개 체크 시 고지서 자동체크
			$('input[name=obligationDisclosingSrcYn]').on('click', function(){
				var checked1 = $(this).attr('checked');
				var checked2 = $('input[name=obligationNotificationYn]').attr('checked');
				if(checked1 && !checked2){
					$('input[name=obligationNotificationYn]').trigger('click');
				}else{
					
				}
			});
			
			$('select[name=licenseType]').on('change', function(){
				// If 'Needs Check' check box is checked, make it UNCHECKED.
				$("#needsChk").prop("checked", false);
				// The others be set to usable.
				$(".oblicationChk").attr("disabled", false);
				
				// Extracted function for 'Needs Check' check box event. hk-cho
				setObligationCheckBox($(this).val());
			});
		}			
	};
	
	// Set status of Obligation checkboxes by license type.
	function setObligationCheckBox(type){
		if(type == "${ct:getCodes(ct:getConstDef('CD_LICENSE_TYPE'))[2]}"){
			$('input[name=obligationNotificationYn]').prop('checked', true);
			$('input[name=obligationDisclosingSrcYn]').prop('checked', true);
		} else if(type == "${ct:getCodes(ct:getConstDef('CD_LICENSE_TYPE'))[1]}"){
			$('input[name=obligationNotificationYn]').prop('checked', true);
			$('input[name=obligationDisclosingSrcYn]').prop('checked', true);
		} else if(type == "${ct:getCodes(ct:getConstDef('CD_LICENSE_TYPE'))[0]}"){
			$('input[name=obligationNotificationYn]').prop('checked', true);
			$('input[name=obligationDisclosingSrcYn]').prop('checked', false);
		}
	};
	
	function registSubmit(){
	    $("#licenseForm").ajaxForm({
	    	url :'<c:url value="/license/validation"/>',
            type : 'POST',
            dataType:"json",
            cache : false,
	        success: onValidSuccess,
            error : onError
	    }).submit();
	}
	
	function deleteSubmit(){
		$("#licenseForm").ajaxForm({
			url :'<c:url value="/license/delAjax"/>',
            type : 'POST',
            dataType:"json",
            cache : false,
	        success:onDeleteSuccess,
            error : onError
	    }).submit();
	}
	
	function onValidSuccess(json, status){
		if(json.isValid == 'false') {
			createValidMsgComplex(json);
			
			alertify.error('<spring:message code="msg.common.valid" />', 0);
		} else if(json.isValid == 'true') {
			loading.show();
			
			var editorVal = CKEDITOR.instances.editor.getData();
			$('input[name=comment]').val(replaceWithLink(editorVal));

			var pData = $('#licenseForm').serializeObject();
			
			if(pData.restrictions != null){
				pData.restrictions = JSON.stringify(pData.restrictions);
				$('input[name=restriction]').val(pData.restrictions.replace(/\"|\[|\]/gi, ""));
			}else{
				$('input[name=restriction]').val('');
			}
			
			$("#licenseForm").ajaxForm({
				url :'<c:url value="/license/saveAjax"/>',
	            type : 'POST',
	            dataType:"json",
	            cache : false,
		        success : onRegistSuccess,
	            error : onError
		    }).submit();
		}
	}
	
	function onRegistSuccess(json, status){
		loading.hide();
		
		if(json.resCd == '10') {
			alertify.alert('<spring:message code="msg.common.success" />', function(){
				deleteTabInFrame('#<c:url value="/license/edit/'+json.licenseId+'"/>');
				reloadTabInframe('<c:url value="/license/list"/>');
			}); 
		} else {
			alertify.error('<spring:message code="msg.common.valid2" />', 0);
		}
	};
	function onDeleteSuccess(json, status){
		var licenseId = $('input[name=licenseId]').val();
		
		if(json.isValid == 'false') {
			alertify.alert(json.validMsg, function(){});
		} else {
			alertify.alert('<spring:message code="msg.common.success" />',function(){
				if(licenseId){
					deleteTabInFrame('#<c:url value="/license/edit/'+licenseId+'"/>');			
				}else{
					deleteTabInFrame('#<c:url value="/license/edit"/>');
				}
				reloadTabInframe('<c:url value="/license/list"/>');
			});
		}
	}
	
	function onError(data, status){
		alertify.error('<spring:message code="msg.common.valid2" />', 0);
    }
    
    function deleteComment(obj){
		if(!confirm("Are you sure you want to delete this comment?")) return;
		var commId = $(obj).next().val();
		$.ajax({
			url : '<c:url value="/license/deleteComment"/>',
			type : 'POST',
			dataType : 'json',
			cache : false,
			data : {'commId' : commId},
			success : function(){},
			error : function(){
				alertify.error('<spring:message code="msg.common.valid2" />', 0);
			}
		})
	}
	
	function modifyComment(obj){
		$('.commModifyPop').show();
		$('#blind_wrap').show();
		var commId = $(obj).next().next().val();
		modifyCommentId = commId;
		var contents = $(obj).parent().parent().next().html();
		CKEDITOR.instances.editor3.setData(contents);
		//코멘트 수정
		$('.closeModComment').off("click").on("click", function(){
			$('.commModifyPop').hide();
			$('#blind_wrap').hide();	
		});
		
		$('.modifyComment').off("click").on("click", function(){
			var editorVal = CKEDITOR.instances.editor3.getData();
			var register = '${sessUserInfo.userId}';
			var param = {commId : modifyCommentId, referenceId : data.detail.licenseId, referenceDiv :'30', contents : replaceWithLink(editorVal)};
			$.ajax({
				url : '<c:url value="/license/saveComment"/>',
				type : 'POST',
				dataType : 'json',
				cache : false,
				data : param,
				success : function(json){
					if(json.isValid){
						if(json.isValid == 'false') {
							createValidMsgComplex(json);
							alertify.error('<spring:message code="msg.common.valid" />', 0);
							
						} else if(json.isValid == 'true') {
							alertify.alert('<spring:message code="msg.common.success" />', function(){
							});
						}	
					}else{
						alertify.alert('<spring:message code="msg.common.success" />', function(){
						});
					}
					$('.commModifyPop').hide();
					$('#blind_wrap').hide();
				},
				error : function(){
					alertify.error('<spring:message code="msg.common.valid2" />', 0);
				}
			})
		});
	}
	
var fn_commemt = {
    getCommentList : function(){
        $.ajax({
        	url : '<c:url value="/comment/getCommentList"/>',
            type : 'GET',
            dataType : 'html',
            cache : false,
            data : {
            	referenceId : data.detail.licenseId,
            	referenceDiv : 'license'
            },
            success : function(data){
                $('#commentListArea').html(replaceWithLink(data));
            },
            error : function(xhr, ajaxOptions, thrownError){
                alertify.error('<spring:message code="msg.common.valid2" />', 0);
            }
        });
    },
    deleteComment : function(_commId){
        if(!confirm("Are you sure you want to delete this comment?")) {
            return;
        }
        
        $.ajax({
        	url : '<c:url value="/comment/deleteComment"/>',
            type : 'POST',
            dataType : 'json',
            cache : false,
            data : {'commId' : _commId},
            success : function(data){
                alertify.success('<spring:message code="msg.common.success" />');
                fn_commemt.getCommentList();
            },
            error : function(){
                alertify.error('<spring:message code="msg.common.valid2" />', 0);
            }
        });
    },
    editComment : function(_commId){
        if(CKEDITOR.instances['comm_editor_'+_commId]) {
            var _editor = CKEDITOR.instances['comm_editor_'+_commId];
            _editor.destroy();
        }
        
        _editor = CKEDITOR.replace('comm_editor_'+_commId);

        $("#spanBtnArea_"+_commId+" > .btnViewMode").hide();
        $("#spanBtnArea_"+_commId+" > .btnEditMode").show();
        $("#spanBtnArea_"+_commId+" > .closeModComment").off("click").on("click", function(e){
            e.preventDefault();
            fn_commemt.createNonToolbarEditor(_commId);
            $("#spanBtnArea_"+_commId+" > .btnViewMode").show();
            $("#spanBtnArea_"+_commId+" > .btnEditMode").hide();
        });
        
        $("#spanBtnArea_"+_commId+" > .modifyComment").off("click").on("click", function(e){
            e.preventDefault();
            var _referenceId = $('input[name=licenseId]').val();
            var param = {commId : _commId, contents : replaceWithLink(_editor.getData()), referenceDiv: '30', referenceId: _referenceId};

            $.ajax({
            	url : '<c:url value="/comment/updateComment"/>',
                type : 'POST',
                dataType : 'json',
                cache : false,
                data : param,
                success : function(json){
                    fn_commemt.createNonToolbarEditor(_commId);
                    $("#spanBtnArea_"+_commId+" > .btnViewMode").show();
                    $("#spanBtnArea_"+_commId+" > .btnEditMode").hide();
                    alertify.success('<spring:message code="msg.common.success" />');
                },
                error : function(){
                    alertify.error('<spring:message code="msg.common.valid2" />', 0);
                },
                complete : function(){
                	fn_commemt.getCommentList();
	            }
            });
            
            return false;
        });
    },
    createNonToolbarEditor : function(_commId) {
        var _editor = CKEDITOR.instances['comm_editor_'+_commId];
        
        if(_editor) {
            _editor.destroy();
        }

        if($('#comm_editor_'+_commId).html().length > 0) {
            CKEDITOR.replace('comm_editor_'+_commId, {customConfig:'<c:url value="/js/customEditorConf_Comment.js"/>'});
        }
    }
}
</script>