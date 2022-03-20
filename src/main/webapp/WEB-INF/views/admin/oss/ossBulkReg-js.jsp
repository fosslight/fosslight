<%@ page contentType="text/html; charset=utf-8" pageEncoding="utf-8"%>
<%@ include file="/WEB-INF/constants.jsp"%>
<script>
    var jsonData;
    var withStatus;
    var loadedInfo;
    var ossData = [];
    var isClicked = false;

    function refreshGrid($grid, results) {
        $grid.jqGrid('clearGridData')
            .jqGrid('setGridParam', { data: results })
            .trigger('reloadGrid', [{ page: 1}]);
    }

    function showErrorMsg() {

        alertify.error('<spring:message code="msg.common.valid"/>', 0);

        $('.ajax-file-upload-statusbar').fadeOut('slow');
        $('.ajax-file-upload-statusbar').remove();
    }
    function stubColData(){
        //데이터 추가
        for(var i=0; i<jsonData.length ; i++ ){
            withStatus = jsonData[i]['oss'];
            withStatus['status'] = jsonData[i]['status'];
            $('#list').jqGrid('addRowData', i, withStatus);
        }
        //다시 로드
        $("#list").trigger('reloadGrid');
    }

    function makeOssDTOList() {
        for (var i = 0; i < jsonData.length; i++){
            ossData.push(jsonData[i]['oss']);
        }
        console.log(ossData);
    }
    function checkLoaded(){
        $("#list").jqGrid('clearGridData');
        for (var i=0; i < loadedInfo.length; i++){
            for (var j = 0; j < jsonData.length; j++){
                if (jsonData[j]['oss']['ossName'] === loadedInfo[i]['ossName']){
                    jsonData[j]['status'] = 'loaded';
                    break;
                }
            }
        }
        stubColData();
        $("#list").trigger('reloadGrid');
    }
    $(document).ready(function()
    {
        var target = $("#list");

        if (isClicked == false) {
            isClicked = true;
            $("#btn").click(() => {
                $.ajax({
                    type: "POST",
                    contentType: 'application/json',
                    url: "/oss/bulkRegAjax",
                    data: JSON.stringify(ossData),
                    dataType: "json",
                    success: (data) => {
                        if (data['res'] == true && data['value'] != []) {
                            loadedInfo = data['value'];
                            console.log(data);
                            checkLoaded();
                        } else if (data['res'] == false) {
                            showErrorMsg();
                        }
                    },
                    error: (e) => {
                        console.log(e);
                    }
                })
            })
        }
        //,
        $("#list").jqGrid({
            datatype: "local",
            data : jsonData,
            colNames:['id', 'OSS Name','Nickname','Version','Declared License','Detected License','Copyright',
                'Homepage','Download URL',  'Summary Description', 'Attribution','Comment', 'Status'],
            colModel: [
                { name: 'id', 	index: 'id', width: 75, key:true, hidden: true},
                {name: 'ossName', index: 'ossName', width: 200, align: 'left'},
                {name: 'ossNicknames', index: 'ossNickNames', width: 200, align: 'left'},
                {name: 'ossVersion', index: 'ossVersion', width: 75, align: 'left'},
                { name: 'declaredLicenses', index: 'declaredLicenses', width: 300, align: 'left'},
                { name: 'detectedLicenses', index: 'detectedLicenses', width: 300, align: 'left'},
                { name: 'ossCopyright', index: 'copyright', width: 200, align: 'left'},
                { name: 'homepage', index:'homepage', width: 250, align: 'left'},
                { name: 'downloadLocation', index:'downloadLocation', width: 150, align: 'left'},
                { name: 'summaryDescription', index:'summaryDescription', width: 150, align: 'left'},
                { name: 'attribution', index:'attribution', width: 150, align: 'left'},
                { name: 'comment', index:'comment', width: 150, align: 'left'},
                { name: 'status', index:'status', width: 150, align: 'left'}
            ],
            viewrecords: true,
            rowNum: 10,
            rowList:[20,40,60],
            autowidth: true,
            gridview: true,
            height: 'auto',
            pager: "#jqGridPager"
        });

        var accept1 = '';

        //checking for allowed extensions (xlsx, xls, xlsm)
        <c:forEach var="file" items="${ct:getCodes(ct:getConstDef('CD_FILE_ACCEPT'))}" varStatus="fileStatus">
            <c:if test="${file eq '11'}">
                accept1 = '${ct:getCodeExpString(ct:getConstDef("CD_FILE_ACCEPT"), file)}';
            </c:if>
        </c:forEach>

        $("#csvFile").uploadFile({
            url:'/oss/csvFile',
            multiple:false,
            dragDrop:true,
            fileName:'myfile',
            allowedTypes: accept1,
            sequential:true,
            sequentialCount:1,
            dynamicFormData: function(){
                var data ={ "registFileId" :$('#csvFileId').val(), "tabNm" : "BULK"};
                return data;
            },
            onSuccess:function(files,data,xhr,pd) {
                if (data['res'] == true){
                    jsonData = data['value'];
                    makeOssDTOList();
                    console.log(jsonData);
                    stubColData();
                    loading.hide();
                }
                else if(data['res'] == false){
                    showErrorMsg();
                } else {
                    if (data['limitCheck'] == null) {
                        showErrorMsg();
                    }
                }
            }
        });
    });
</script>
