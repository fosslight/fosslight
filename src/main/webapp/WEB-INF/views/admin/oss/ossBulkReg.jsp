<%@ page contentType="text/html; charset=utf-8" pageEncoding="utf-8"%>
<%@ include file="/WEB-INF/constants.jsp"%>
<head>
    <tiles:insertAttribute name="scripts"></tiles:insertAttribute>
</head>
<div id="wrapIframe">
    <body>
    <div>
         <span class="fileex_back">
                <div id="csvFile">+ Add file</div>
         </span>
         <div>
            <a href="javascript:void(0);" class="sampleDown" onclick="fn.downloadBulkSample()"><span>Sample</span></a>
         </div>
    </div>
    <div class="jqGridSet">
        <table id="list"><tr><td></td></tr></table>
        <div id="pager"></div>
    </div>
    <div class="btnLayout">
        <span class="left">
            <input type="button" id="btn" value="Save" class="btnColor red" style="width: 125px;" />
        </span>
    </div>
    </body>
</div>
<tiles:insertAttribute name="ossBulkReg-js"></tiles:insertAttribute>