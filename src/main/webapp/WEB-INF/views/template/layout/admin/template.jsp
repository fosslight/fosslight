<%@ page contentType="text/html; charset=utf-8" pageEncoding="utf-8"%>
<%@ include file="/WEB-INF/constants.jsp"%>
<%-- Administrator screen template --%>
<!DOCTYPE html>
<html>
	<head>
		<tiles:insertAttribute name="meta" />
		<tiles:insertAttribute name="scripts" />
		<script type="text/javascript" src="/js/ckeditor/ckeditor.js?${jsVersion}"></script>
	</head>
	<body>
		<!-- skip-navigation -->
		<div class="skip-navigation">
			<p><a href="#container" tabindex="0">본문 바로가기</a></p>
		</div>
		<!-- //skip-navigation -->
		<!-- wrap -->
		<div id="wrap">
			<div id="wrapBack">
				<input type="button" value="<" class="headerHandle" />
				<tiles:insertAttribute name="header" />
				<hr/>
				<div class="pop registPop" style="width: 600px;">
                    <div class="popdata" style="padding: 10px 10px 10px;">
                    	<div id="noticeTitle" style="text-align: center; font-size: 12pt; font-weight: bold; padding-bottom:15px;"></div>
                    	<div id="noticeContent"></div>
                    	<div style="text-align: center;"><input type="checkbox" value="checkbox" name="chkbox" id="chkday"/>&nbsp;Do not show this message again</div>
                        <input id="btnNotice" type="button" value="OK" class="okRegister" style="height:40px; cursor: pointer;" />
                    </div>
                </div>
        		<div id="blind_wrap"></div>
				<!-- container -->
				<div id="section">
					<div class="sectionBack">
						<div id="nav-tabs" class="container">
							<!---->
							<div class="contentsFix">
								<div class="contentsFixBack">
									<div class="tabMenu">
										<ul class="nav-tab-menu">
											<li style="display:none;">
												<span></span>
											</li>
										</ul>
									</div>
								</div>
							</div>
							<!---->
							<div class="contents">
							</div>
							<!---->
						</div>
					</div>
				</div>
				<!-- //container -->
			</div>
		</div>
		<!-- //wrap -->
	</body>
<script>

$('#btnNotice').click(function(){
	if($("#chkday").is(":checked")){
		setCookie("noticeYn", "N", 1);
	}
	
	$('.registPop').hide();
	$('#blind_wrap').hide();
});

if(getCookie("noticeYn") != "N"){
	$.ajax({
		url : "/system/notice/getPublishedtNotice",
		type : "GET",
		success : function(data){
			if(data.noticeList){
				var _noticeTitle = "[Notice]";
				
				if(data.noticeList[0].title) {
					_noticeTitle += " " + data.noticeList[0].title;
				}
				
				$("#noticeTitle").text(_noticeTitle);
				$("#noticeContent").append('<div id="noticeEdit" style="width:300px; height:150px;">'+data.noticeList[0].notice+'</div>');

			    var _editor = CKEDITOR.instances.noticeEdit;
			    
				if(_editor) {
					_editor.destroy();
				}
				
				CKEDITOR.replace('noticeEdit', {
					customConfig:'/js/customEditorConf_Comment.js'
				});
				
				$('.registPop').show();
				$('#blind_wrap').show();
			}
		},
		error : function(){}
	});
}
</script>
</html>