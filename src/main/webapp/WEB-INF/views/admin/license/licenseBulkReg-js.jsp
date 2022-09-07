<%@ page contentType="text/html; charset=utf-8" pageEncoding="utf-8"%>
<%@ include file="/WEB-INF/constants.jsp"%>
<script>
    // var postData = []
    var jsonData;
    var withStatus;
    var loadedInfo;
    var licenceData = [];
    var isClicked = false;
    // var stringDataForValid = ["licenseName", "licenseType", "obligationNotificationYn",
    //     "obligationDisclosingSrcYn", "shortIdentifier", "description", "licenseText", 'attribution', 'comment']
    // var listDataForValid = ["licenseNicknames", "webpages"]
    // var editColList = ['id', "licenseName", "licenseType", "obligationNotificationYn",
    //     "obligationDisclosingSrcYn", "shortIdentifier", "licenseNicknames", "webpages", "description",
    //     "licenseText", 'attribution', 'comment']

    function refreshGrid($grid, results) {
        $grid.jqGrid('clearGridData')
            .jqGrid('setGridParam', { data: results })
            .trigger('reloadGrid', [{ page: 1}]);
    }

    // function editable() {
    //     editColList.forEach((colName) => {
    //         $("#list").jqGrid('setColProp', colName, {editable: true});
    //     })
    // }

    function showErrorMsg() {

        alertify.error('<spring:message code="msg.common.valid"/>', 0);

        $('.ajax-file-upload-statusbar').fadeOut('slow');
        $('.ajax-file-upload-statusbar').remove();
    }

    function stubColData(){
        //데이터 추가
        for(var i=0; i<jsonData.length ; i++ ){
            withStatus = jsonData[i]['license'];
            withStatus['status'] = jsonData[i]['status'];
            $('#list').jqGrid('addRowData', i, withStatus);
            console.log(withStatus)
        }
        console.log($('#list').jqGrid('getRowData'));
        //다시 로드
        $("#list").trigger('reloadGrid');
    }

    function makeLicenseDTOList() {
        for (var i = 0; i < jsonData.length; i++){
            licenceData.push(jsonData[i]['license']);
        }
    }

    // function dataValidCheck(mainData) {
    //     mainData.forEach((row, index) => {
    //         stringDataForValid.forEach((strData) => {
    //             licenceData[index][strData] = row[strData].trim();
    //         })
    //         listDataForValid.forEach((listData) => {
    //             ossData[index][listData] = row[listData].trim()
    //             if (licenceData[index][listData] == "") {
    //                 licenceData[index][listData] = []
    //             } else {
    //                 licenceData[index][listData] = licenceData[index][listData].split(",")
    //             }
    //         })
    //     })}

    // function makePostData(rowIds) {
    //     rowIds.forEach((idx) => {
    //         postData.push(licenceData[Number(idx)])
    //     })
    // }

    function checkLoaded(){

        $("#list").jqGrid('clearGridData');

        for (var i=0; i < jsonData.length; i++){
            var addedOSS = false;
            for (var j = 0; j < loadedInfo.length; j++){
                if (jsonData[i]['license']['licenseName'] === loadedInfo[j]['licenseName']){
                    jsonData[i]['status'] = 'Added';
                    addedOSS = true;
                    break;
                }
            }
            if (!addedOSS) {
                jsonData[i]['status'] = 'Failed';
            }
        }
        stubColData();
        $("#list").trigger('reloadGrid');
    }

    $(document).ready(function()
    {
        // var target = $("#list");

        if (isClicked == false) {
            isClicked = true;
            $("#btn").click(() => {

                // target.jqGrid('saveRow', _mainLastsel);
                // var rowIds = target.jqGrid("getGridParam", "selarrrow");
                // var mainData = target.jqGrid('getRowData');
                //
                // dataValidCheck(mainData)
                // postData = []
                // makePostData(rowIds)

                $.ajax({
                    type: "POST",
                    contentType: 'application/json',
                    url: "/oss/bulkRegAjax",
                    data: JSON.stringify(licenceData),
                    cache : false,
                    dataType: "json",
                    success: (data) => {
                        _mainLastsel = -1;
                        if (data['res'] == true && data['value'] != []) {
                            loadedInfo = data['value'];
                            checkLoaded();
                            alertify.alert('<spring:message code="msg.common.success" />', function(){});
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
            colNames:['id', 'License Name','License Type','Notice','Source Code','SPDX Short Identifier','Nickname',
                'Website for the license','User Guide',  'License Text', 'Attribution','Comment', 'Status'],
            colModel: [
                { name: 'id', 	index: 'id', width: 75, key:true, hidden: true, editable:false},
                {name: 'licenseName', index: 'License Name', width: 200, align: 'left', editable:false},
                {name: 'licenseType', index: 'License Type', width: 200, align: 'left', editable:false},
                {name: 'obligationNotificationYn', index: 'Notice', width: 75, align: 'left', editable:false},
                {name: 'obligationDisclosingSrcYn', index: 'Source Code', width: 75, align: 'left', editable:false},
                { name: 'shortIdentifier', index: 'SPDX Short Identifier', width: 300, align: 'left', editable:false},
                { name: 'licenseNicknames', index: 'Nickname', width: 300, align: 'left', editable:false},
                { name: 'webpages', index: 'Website for the license', width: 200, align: 'left', editable:false},
                { name: 'description', index:'User Guide', width: 250, align: 'left', editable:false},
                { name: 'licenseText', index:'License Text', width: 150, align: 'left', editable:false},
                { name: 'attribution', index:'Attribution', width: 150, align: 'left', editable:false},
                { name: 'comment', index:'comment', width: 150, align: 'left', editable:false},
                { name: 'status', index:'status', width: 150, align: 'left'}
            ],
            viewrecords: true,
            rowNum: ${ct:getConstDef("DISP_PAGENATION_DEFAULT")},
            rowList: [${ct:getConstDef("DISP_PAGENATION_LIST_STR")}],
            autowidth: true,
            gridview: true,
            height: 'auto',
            pager: '#pager',

            autoencode: true,
            editurl:'clientArray',
            recordpos:'right',
            toppager:true,
            loadonce:false,
            cellsubmit : 'clientArray',
            ignoreCase: true,
            // multiselect: true,

            loadComplete: function(data) {
                _mainLastsel = -1;
            },
            // ondblClickRow: function(rowid,iRow,iCol,e) {
            //     if(rowid) {
            //         if (rowid != _mainLastsel) {
            //             $("#list").jqGrid('saveRow', _mainLastsel);
            //         }
            //
            //         editable()
            //
            //         $("#list").jqGrid('editRow', rowid);
            //         _mainLastsel = rowid;
            //
            //         var nextCol = $("#list").jqGrid('getGridParam', 'colModel')[iCol].name
            //         var nextRow = rowid
            //         $('#' + nextRow + "_" + nextCol).focus();
            // }
        });

        var accept1 = '';

        //checking for allowed extensions (xlsx, xls, xlsm)
        <c:forEach var="file" items="${ct:getCodes(ct:getConstDef('CD_FILE_ACCEPT'))}" varStatus="fileStatus">
            <c:if test="${file eq '11'}">
                accept1 = '${ct:getCodeExpString(ct:getConstDef("CD_FILE_ACCEPT"), file)}';
            </c:if>
        </c:forEach>

        $("#csvFile").uploadFile({
            url:'/license/csvFile',
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

                $("#list").jqGrid('clearGridData');

                licenceData = []
                if (data['res'] == true){
                    jsonData = data['value'];
                    makeLicenseDTOList();
                    console.log(licenceData)
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
    var fn = {
        downloadBulkSample : function(type){
			var logiPath = "/sample/FOSSLight-OSS-Bulk-Sample.xls";
			var fileName = "FOSSLight-OSS-Bulk-Sample.xls";

			location.href = '<c:url value="/partner/sampleDownload?fileName='+fileName+'&logiPath='+logiPath+'"/>';
		}
	}
</script>