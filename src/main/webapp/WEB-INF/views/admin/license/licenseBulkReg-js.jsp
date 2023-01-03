<%@ page contentType="text/html; charset=utf-8" pageEncoding="utf-8"%>
<%@ include file="/WEB-INF/constants.jsp"%>
<script>
    var jsonData;
    var withStatus;
    var loadedInfo;
    var licenseData = [];
    var failLicense = [];
    var postData;
    var isClicked = false;
    var stringDataForValid = ['licenseName','licenseType','obligationNotificationYn','obligationDisclosingSrcYn',
        'shortIdentifier','description','licenseText', 'attribution', 'comment'];
    var inputStringDataForValid = ['licenseName','licenseType','obligationNotificationYn','obligationDisclosingSrcYn',
        'shortIdentifier'];
    var listDataForValid = ["licenseNicknames", "webpages"];
    var textAreaStringDataForValid = ['description', 'licenseText', 'attribution', 'comment'];
    var editColList = ['gridId', "licenseNicknames", "webpages", "licenseName", 'licenseType', 'obligationNotificationYn',
        'obligationDisclosingSrcYn', 'shortIdentifier','description','licenseText', 'attribution', 'comment'];
    var selectedIds = new Set()

    function refreshGrid($grid, results) {
        $grid.jqGrid('clearGridData')
            .jqGrid('setGridParam', { data: results })
            .trigger('reloadGrid', [{ page: 1}]);
    }

    /**
     * make rows editable
     * */
    function editable() {
        editColList.forEach((colName) => {
            $("#list").jqGrid('setColProp', colName, {editable: true});
        })
    }

    function showErrorMsg() {

        alertify.error('<spring:message code="msg.common.valid"/>', 0);

        $('.ajax-file-upload-statusbar').fadeOut('slow');
        $('.ajax-file-upload-statusbar').remove();
    }

    /**
     * store seleted id
     * */
    function selectOrUnselect(gridId) {
        if(selectedIds.has(gridId))
            selectedIds.delete(gridId);
        else
            selectedIds.add(gridId);
    }

    /**
     * store all rowId in this page
     * */
    function selectAllPage() {
        getCurPageIds().forEach(id => selectedIds.add(id));
    }

    /**
     * remove all rowId in this page
     * */
    function unselectAllPage() {
        getCurPageIds().forEach(id => selectedIds.delete(id));
    }

    /**
     * return selected rowIds in this page
     * */
    function getCurPageIds() {
        return $("#list").jqGrid("getDataIDs");
    }

    function selectMarkWhenPageChange() {
        for(let id of $("#list").jqGrid("getDataIDs")) {
            if(selectedIds.has(id)) {
                selectedIds.delete(id);
                $("#list").jqGrid("setSelection", id);
            }
        }
    }

    /**
     * data fot seding to server
     * */
    function makePostData() {
        postData = []
        selectedIds.forEach((idx) => {
            postData.push(licenseData[Number(idx)])
        })
    }


    function stubColData(){
        for(var i=0; i<jsonData.length ; i++ ){
            withStatus = jsonData[i]['license'];
            withStatus['status'] = jsonData[i]['status'];
            $('#list').jqGrid('addRowData', i, withStatus);
        }
        $("#list").trigger('reloadGrid');
    }

    function makeLicenseDTOList() {
        for (var i = 0; i < jsonData.length; i++){

            /**
             * String Data NPE CHECK
             * Space Issue CHECK
             * */
            stringDataForValid.forEach((strData) => {
                if (jsonData[i]['license'][strData] == undefined) {
                    jsonData[i]['license'][strData] = ""
                } else {
                    jsonData[i]['license'][strData] = jsonData[i]['license'][strData].trim()
                }
            })

            /**
             * List Data NPE CHECK
             * Space Issue CHECK
             * */
            listDataForValid.forEach((listData) => {
                if (jsonData[i]['license'][listData] == undefined) {
                    jsonData[i]['license'][listData] = []
                } else {
                    checked_list = []
                    for (var j = 0; j < jsonData[i]['license'][listData].length; j++) {
                        if (jsonData[i]['license'][listData][j].trim() != "") {
                            checked_list.push(jsonData[i]['license'][listData][j].trim())
                        }
                    }
                    jsonData[i]['license'][listData] = checked_list
                }
            })

            /**
             * setGridId
             * */
            jsonData[i]['license']['gridId'] = i

            licenseData.push(jsonData[i]['license']);
        }
    }

    /**
     * Remove useless spaces in all rows
     * replace stringData wtih listData in 3 rows (ossNicknames, declaredLicenses, detectedLicenses)
     * */
    function dataValidCheck(mainData) {
        mainData.forEach((row) => {
            inputStringDataForValid.forEach((strData) => {
                licenseData[row["gridId"]][strData] = row[strData].trim();
            })

            textAreaStringDataForValid.forEach((strData) => {
                licenseData[row["gridId"]][strData] = row[strData];
            })

            listDataForValid.forEach((listData) => {
                licenseData[row["gridId"]][listData] = row[listData].trim()
                if (licenseData[row["gridId"]][listData] == "") {
                    licenseData[row["gridId"]][listData] = []
                } else {
                    checked_list = []
                    new_list = licenseData[row["gridId"]][listData].split(",")
                    for (var i = 0; i < new_list.length; i++) {
                        if (new_list[i].trim() != "") {
                            checked_list.push(new_list[i].trim())
                        }
                    }
                    licenseData[row["gridId"]][listData] = checked_list
                }
            })
        })
    }

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
        var target = $("#list");

        if (isClicked == false) {
            isClicked = true;
            $("#btn").click(() => {

                LicenseBulkGridStatusMessageManager.cleanStatusMessages();
                LicenseBulkGridWarningMessageUtil.cleanMessages();
                target.jqGrid('saveRow', _mainLastsel);

                // load all rows in grid
                var mainData = target.jqGrid('getRowData');

                dataValidCheck(mainData)
                makePostData()

                console.log("postdata")
                console.log(postData)

                $.ajax({
                    type: "POST",
                    contentType: 'application/json',
                    url: "/license/bulkRegAjax",
                    data: JSON.stringify(postData),
                    cache : false,
                    dataType: "json",
                    success: (data) => {
                        failLicense = []
                        _mainLastsel = -1;
                        if (data['res'] == true && data['value'] != []) {
                            loadedInfo = data['value'];

                            LicenseBulkGridStatusMessageManager.setStatusMessages(loadedInfo);
                            LicenseBulkGridWarningMessageUtil.call();

                            alertify.alert('<spring:message code="msg.common.success" />', function(){});
                        } else if (data['res'] == false) {
                            showErrorMsg();
                            LicenseBulkGridWarningMessageUtil.setMessagesToCurPage();
                        }
                    },
                    error: (e) => {
                        LicenseBulkGridWarningMessageUtil.setMessagesToCurPage();
                    }
                })
            })
        }
        //,
        $("#list").jqGrid({
            datatype: "local",
            data : jsonData,
            colNames:['gridId', 'License Name','License Type','Notice','Source Code','SPDX Short Identifier','Nickname',
                'Website for the license','User Guide',  'License Text', 'Attribution','Comment', 'Status'],
            colModel: [
                { name: 'gridId', index: 'gridId', width: 75, key:true, hidden: true, editable:false},
                { name: 'licenseName', index: 'License Name', width: 200, align: 'left', editable:false},
                { name: 'licenseType', index: 'License Type', width: 200, align: 'left', editable:false},
                { name: 'obligationNotificationYn', index: 'Notice', width: 75, align: 'left', editable:false},
                { name: 'obligationDisclosingSrcYn', index: 'Source Code', width: 75, align: 'left', editable:false},
                { name: 'shortIdentifier', index: 'SPDX Short Identifier', width: 300, align: 'left', editable:false},
                { name: 'licenseNicknames', index: 'Nickname', width: 300, align: 'left', editable:false},
                { name: 'webpages', index: 'Website for the license', width: 200, align: 'left', editable:false},
                { name: 'description', index:'User Guide', width: 250, align: 'left', editable:false, edittype:"textarea"},
                { name: 'licenseText', index:'License Text', width: 150, align: 'left', editable:false, edittype:"textarea"},
                { name: 'attribution', index:'Attribution', width: 150, align: 'left', editable:false, edittype:"textarea"},
                { name: 'comment', index:'comment', width: 150, align: 'left', editable:false, edittype:"textarea"},
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
            multiselect: true,

            /** check rows seleted when move page*/
            loadComplete: function(data) {
                _mainLastsel = -1;
                selectMarkWhenPageChange();
                makeBackGroundColor();
                LicenseBulkGridWarningMessageUtil.setMessagesToCurPage();
            },
            ondblClickRow: function(rowid,iRow,iCol,e) {
                if(rowid) {
                    if (rowid != _mainLastsel) {
                        $("#list").jqGrid('saveRow', _mainLastsel);
                        LicenseBulkGridWarningMessageUtil.setMessageToRow(_mainLastsel);
                    }
                    LicenseBulkGridWarningMessageUtil.cleanMessageOfRow(rowid);
                    editable()
                    $("#list").jqGrid('editRow', rowid);
                    _mainLastsel = rowid;
                    var nextCol = $("#list").jqGrid('getGridParam', 'colModel')[iCol].name
                    var nextRow = rowid
                    $('#' + nextRow + "_" + nextCol).focus();
                }
            },
            /** select or not each row */
            onSelectRow: function(id){
                selectOrUnselect(id);
            },
            /** select or not all rows */
            onSelectAll: function(aRowids, status) {
                (status) ? selectAllPage() : unselectAllPage();
            },
            /** save data edited when move page */
            onPaging: function() {
                LicenseBulkGridWarningMessageUtil.cleanMessages();

                $("#list").jqGrid('saveRow', _mainLastsel);

                var mainData = $("#list").jqGrid('getRowData');

                dataValidCheck(mainData)
            }
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

                licenseData = []
                if (data['res'] == true){
                    jsonData = data['value'];
                    makeLicenseDTOList();
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
			var logiPath = "/sample/FOSSLight-License-Bulk-Sample.xls";
			var fileName = "FOSSLight-License-Bulk-Sample.xls";

			location.href = '<c:url value="/partner/sampleDownload?fileName='+fileName+'&logiPath='+logiPath+'"/>';
		}
	}
    /**
     * change background color of failed oss row
     * */
    function makeBackGroundColor(){
        for (const failLicenseGridId of failLicense) {
            $("#list").jqGrid("setRowData", failLicenseGridId, false, {'background' : "rgba(255, 0, 0, 0.3)"});
        }
    }

    const LicenseBulkGridStatusMessageManager = {
        cleanStatusMessages: function () {
            $("#list").jqGrid("getGridParam", "data").forEach(row => {
                row.status = ""
            });
        },
        setStatusMessages: function (results) {
            for (const result of results){
                $("#list").jqGrid("getLocalRow", result.gridId).status = result.msg;
                if (!result.status) {
                    failLicense.push(result.gridId);
                }
            }
            $("#list").trigger('reloadGrid', [{ page: 1}]);
        }
    };

    const LicenseBulkGridWarningMessageUtil = (function() {
        let validData = {};
        const gridStr = "list";
        return {
            call: function () {
                this.cleanMessages();
                return this.requestMessages()
                    .then(response => {
                        console.log(response)
                        this.setValidData(response);
                        this.setMessagesToCurPage();
                    });
            },
            requestMessages: function() {
                return $.ajax({
                    type: "POST",
                    contentType: 'application/json',
                    url: "/license/bulkValidation",
                    data: JSON.stringify(this.requestBody()),
                    cache : false,
                    dataType: "json"
                })
            },
            requestBody: function() {
                licenseData.forEach((row) => {
                    stringDataForValid.forEach((strData) => {
                        if(row[strData] === null || row[strData] === undefined)
                            row[strData] = "";
                    })
                    listDataForValid.forEach((listData) => {
                        if(row[listData] === null || row[listData] === undefined)
                            row[listData] = [];
                    })
                })
                return licenseData;
            },
            setValidData: (response) => { validData = response.validData; },
            setMessagesToCurPage: function () {
                gridValidMsgNew(validData, gridStr, "NORMAL");
            },
            cleanMessages: function() { gridCleanErrMsg(gridStr); },
            cleanMessageOfRow: function(rowId) { cleanErrMsg(gridStr, rowId) },
            setMessageToRow: function(rowId) { gridValidMsgRowId(validData, gridStr, rowId); }
        }
    })();
</script>
