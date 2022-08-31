<%@ page contentType="text/html; charset=utf-8" pageEncoding="utf-8"%>
<%@ include file="/WEB-INF/constants.jsp"%>
<!-- <style type="text/css">
    .cke_top
    {
        display: none !important;
    }
</style> -->
<script type="text/javascript">
	$(document).ready(function() {
		$( "div[id^='comm_editor_']" ).each(function (){
			if($(this).html().length > 0 && !this.hasAttribute('contenteditable')) {
				this.setAttribute( 'contenteditable', 'true' );
				
				var _editor = CKEDITOR.replace(this.id, 
						{
					customConfig:'<c:url value="/js/customEditorConf_Comment.js"/>'
						});
			}
			if(CKEDITOR.instances[this.id]) {
				var text = CKEDITOR.instances[this.id].getData();
				var linkText;
				if(opener == null) {
					linkText = replaceWithLink(text);
				} else {
					linkText = opener.replaceWithLink(text);
				}
				CKEDITOR.instances[this.id].setData(linkText);
			}
		});
		
		CKEDITOR.on('instanceReady', function (ev) {
			$('iframe').contents().unbind("click").bind("click",function(e){
	            var url = e.target.href;
	            
	            if (url !== undefined && url !== "") {
	                window.open(url);
	            }
	        });
		});
		
		if(${commentListCnt} > 5 && !${moreYn}){ // 5건 이상이고 최초 화면 로드일 경우 (More버튼을 눌러 추가 리스트를 로드한 경우는 X)
			$("#moreListDiv").show();
		}else{
			$("#moreListDiv").hide();
		}
	});
</script>
<c:choose>
	<c:when test="${fn:length(commentList) > 0}">
		<c:forEach var="item" items="${commentList}" varStatus="varstatus">
			<dt>
				<span class="left">
					<strong class="nameArea" >
						<strong style="color:${ct:getCommentColor(item.referenceDiv)};">${ct:getCodeString(ct:getConstDef('CD_COMMENT_DIVISION'), item.referenceDiv)}</strong>
						<c:if test="${not empty item.expansion1}"> (${item.expansion1})</c:if>
						<c:if test="${not empty item.status}"> > ${item.status}</c:if><br/>${item.creator}</strong> | <span class="dateArea">${ct:formatDate(item.createdDate)}</span>
				</span>
				<span class="right" id="spanBtnArea_${item.commId}">
				<c:if test="${item.creator eq item.loginUserName}">
					<input type="button" value="editModify" class="editModify btnViewMode" onclick="fn_commemt.editComment('${item.commId}', '${item.referenceDiv}', '${item.referenceId}');"/>
					<input type="button" value="editDelete" class="editDelete btnViewMode" onclick="fn_commemt.deleteComment('${item.commId}');"/>
				</c:if>
					<input type="button" value="Cancel" class="btnCancel btnColor closeModComment btnEditMode" style="display: none;" />
					<input type="button" value="Save" class="btnColor red modifyComment btnEditMode" style="display: none;" />
					<input type="hidden" name="commId"/> 
				</span>
			</dt>
			<div class="grid-container">
				<div class="grid-width-100">
					<div id="comm_editor_${item.commId}">${item.contents}</div>
				</div>
			</div>
		</c:forEach>
	</c:when>
	<c:otherwise><p class="noneTxt">No comments were registered.</p></c:otherwise>
</c:choose>

