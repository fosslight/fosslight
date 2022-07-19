<%@ page contentType="text/html; charset=utf-8" pageEncoding="utf-8"%>
<%@ include file="/WEB-INF/constants.jsp"%>
<script type="text/javascript">
$(document).ready(function() {
	CKEDITOR.inline( 'editorView' );
	if ( CKEDITOR.instances.editorView ) {
		CKEDITOR.instances.editorView.destroy();
	}
});
</script>

		<div class="projectInfo">
			<h2>Project Information</h2>
			<ul>
				<li class="first">
					<span>Project Name</span><strong><label>${project.prjName} <c:if test="${not empty project.prjVersion}">(<c:out value="${project.prjVersion}"/>)</c:if></label></strong>
				</li>
				<li>
					<span>Created</span><strong><label>${project.prjUserName} ${project.prjDivisionName } (${ct:formatDateSimple(project.createdDate)})</label></strong>
				</li>
				<li class="first">
					<span>Comment</span><strong><label></label></strong>
				</li>
			</ul>
			<div style="padding:0 0 30px 130px;">
				<div id="editorView" style="overflow:auto; height:130px;">${project.comment}</div>
				<div style="padding-top:10px;"><input type="button" value="Edit" onclick="fn_self.modeChange('${project.prjId}', 'e');" class="btnColor btnExpor srcBtn right" /></div>
			</div>
		</div>