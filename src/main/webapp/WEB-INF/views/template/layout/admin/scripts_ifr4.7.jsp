<%@ page contentType="text/html; charset=utf-8" pageEncoding="utf-8" %>
<%@ include file="/WEB-INF/constants.jsp"%>
<script>
var CTX_PATH = "${ctxPath}";
</script>
<%-- Add script --%>
<link rel="stylesheet" href="//code.jquery.com/ui/1.11.4/themes/smoothness/jquery-ui.css">
<link rel="stylesheet" href="${ctxPath}/css/jqgrid4.7/ui.jqgrid.css" />
<link rel="stylesheet" href="${ctxPath}/css/commonIframe.css?${cssVersion}" />
<link rel="stylesheet" href="${ctxPath}/css/uploadFile/uploadfile.css" />
<link rel="stylesheet" href="//cdnjs.cloudflare.com/ajax/libs/qtip2/3.0.3/jquery.qtip.css">

<script src="//code.jquery.com/jquery-1.11.3.min.js"></script>
<script src="//code.jquery.com/jquery-migrate-1.2.1.min.js"></script>
<script src="//code.jquery.com/ui/1.11.3/jquery-ui.min.js"></script>
<script src="${ctxPath}/js/jqgrid4.7/js/i18n/grid.locale-en.js"></script>
<script src="${ctxPath}/js/uploadFile/jquery.uploadfile.min.js"></script>
<script src="//cdnjs.cloudflare.com/ajax/libs/qtip2/3.0.3/jquery.qtip.min.js"></script>

<script src="//cdnjs.cloudflare.com/ajax/libs/jquery.form/4.3.0/jquery.form.min.js"></script>
<script src="${ctxPath}/js/basic.js?${jsVersion}"></script>
<script src="${ctxPath}/js/jqgrid4.7/js/jquery.jqGrid.js?${jsVersion}"></script>
<script src="${ctxPath}/js/ckeditor/ckeditor.js?${jsVersion}"></script>
<script src="${ctxPath}/js/basic_editor.js?${jsVersion}"></script>

<!-- alertify -->
<script src="//cdn.jsdelivr.net/npm/alertifyjs@1.11.0/build/alertify.min.js"></script>
<link rel="stylesheet" href="//cdn.jsdelivr.net/npm/alertifyjs@1.11.0/build/css/alertify.min.css"/>
<link rel="stylesheet" href="//cdn.jsdelivr.net/npm/alertifyjs@1.11.0/build/css/themes/default.min.css"/>

<script type="text/javascript">
	$.jgrid.no_legacy_api = true;
	$.jgrid.useJSON = true;
</script>


