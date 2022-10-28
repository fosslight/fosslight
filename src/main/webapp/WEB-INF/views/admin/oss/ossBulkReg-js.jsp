<%@ page contentType="text/html; charset=utf-8" pageEncoding="utf-8"%>
<%@ include file="/WEB-INF/constants.jsp"%>
<jsp:useBean id="T2CoValidationConfig" class="oss.fosslight.validation.T2CoValidationConfig" scope="page" />
<script>
    var jsonData;
    var withStatus;
    var loadedInfo;
    var ossData = [];
    var failOss = [];
    var isClicked = false;
    var postData; /**list data for sending to server*/
    var stringDataForValid = ['ossName','ossVersion','copyright','homepage','downloadLocation'
        ,'summaryDescription','attribution', 'comment'];
    var listDataForValid = ["ossNicknames", "declaredLicenses", "detectedLicenses"];
    var editColList = ['id', "ossNicknames", "declaredLicenses", "detectedLicenses", 'ossName','ossVersion','copyright','homepage','downloadLocation'
        ,'summaryDescription','attribution', 'comment'];
    var selectedIds = new Set() /**set for selected rowids*/
    var referenceId = '${projectData.prjId}';
    var referenceDiv = '${projectData.referenceDiv}';

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

    /**
     *
     * */
    function selectMarkWhenPageChange() {
        for(let id of $("#list").jqGrid("getDataIDs")) {
            if(selectedIds.has(id)) {
                selectedIds.delete(id);
                $("#list").jqGrid("setSelection", id);
            }
        }
    }

    /**
     * load rows in grid
     * */
    function stubColData(){
        for(var i=0; i<jsonData.length ; i++ ){
            withStatus = jsonData[i]['oss'];
            withStatus.gridId = i;
            $('#list').jqGrid('addRowData', i, withStatus);
        }
        $("#list").trigger('reloadGrid');
    }

    /**
     * make ossData
     * */
    function makeOssDTOList() {
        for (var i = 0; i < jsonData.length; i++){
            ossData.push(jsonData[i]['oss']);
        }
    }

    /**
     * Remove useless spaces in all rows
     * replace stringData wtih listData in 3 rows (ossNicknames, declaredLicenses, detectedLicenses)
     * */
    function dataValidCheck(mainData) {
        mainData.forEach((row) => {
            stringDataForValid.forEach((strData) => {
                ossData[row["gridId"]][strData] = row[strData].trim();
            })

            listDataForValid.forEach((listData) => {
                ossData[row["gridId"]][listData] = row[listData].trim()
                if (ossData[row["gridId"]][listData] == "") {
                    ossData[row["gridId"]][listData] = []
                } else {
                    ossData[row["gridId"]][listData] = ossData[row["gridId"]][listData].split(",")
                        .map(item => item.trim()).filter(item => item.length);
                }
            })
        })
    }

    /**
     * make final data
     * postdata will be sent to server
     * */
    function makePostData() {
        postData = []
        selectedIds.forEach((idx) => {
            postData.push(ossData[Number(idx)])
        })
    }

    function checkLoaded(){

        $("#list").jqGrid('clearGridData');

        for (var i=0; i < jsonData.length; i++){
            var addedOSS = false;
            for (var j = 0; j < loadedInfo.length; j++){
                if (jsonData[i]['oss']['ossName'] === loadedInfo[j]['ossName']){
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
                OssBulkGridStatusMessageManager.cleanStatusMessages();
                OssBulkGridWarningMessageUtil.cleanMessages();
                target.jqGrid('saveRow', _mainLastsel);

                // load all rowIds checked in grid
                var rowIds = target.jqGrid("getGridParam", "selarrrow");

                // load all rows in grid
                var mainData = target.jqGrid('getRowData');

                //data Check and make postData
                dataValidCheck(mainData)
                makePostData()

                $.ajax({
                    type: "POST",
                    contentType: 'application/json',
                    url: "/oss/bulkRegAjax",
                    data: JSON.stringify(postData),
                    cache : false,
                    dataType: "json",
                    success: (data) => {
                        failOss = []
                        _mainLastsel = -1;
                        if (data['res'] == true && data['value'] != []) {
                            loadedInfo = data['value'];
                            OssBulkGridStatusMessageManager.setStatusMessages(loadedInfo);
                            OssBulkGridWarningMessageUtil.call();
                            alertify.alert('<spring:message code="msg.common.success" />', function(){});
                        } else if (data['res'] == false) {
                            showErrorMsg();
                            OssBulkGridWarningMessageUtil.setMessagesToCurPage();
                        }
                    },
                    error: (e) => {
                        OssBulkGridWarningMessageUtil.setMessagesToCurPage();
                    }
                })
            })
        }
        $("#list").jqGrid({
            datatype: "local",
            data : jsonData,
            colNames:['gridId', 'OSS Name','Nickname','Version','Declared License','Detected License','Copyright',
                'Homepage','Download URL',  'Summary Description', 'Attribution','Comment', 'Status'],
            colModel: [
                { name: 'gridId', index: 'gridId', width: 75, key:true, hidden: true, editable:false},
                { name: 'ossName', index: 'ossName', width: 200, align: 'left', editable:false},
                { name: 'ossNicknames', index: 'ossNickNames', width: 200, align: 'left', editable:false},
                { name: 'ossVersion', index: 'ossVersion', width: 75, align: 'left', editable:false},
                { name: 'declaredLicenses', index: 'declaredLicenses', width: 300, align: 'left', editable:false},
                { name: 'detectedLicenses', index: 'detectedLicenses', width: 300, align: 'left', editable:false},
                { name: 'copyright', index: 'copyright', width: 200, align: 'left', editable:false},
                { name: 'homepage', index:'homepage', width: 250, align: 'left', editable:false},
                { name: 'downloadLocation', index:'downloadLocation', width: 150, align: 'left', editable:false},
                { name: 'summaryDescription', index:'summaryDescription', width: 150, align: 'left', editable:false},
                { name: 'attribution', index:'attribution', width: 150, align: 'left', editable:false},
                { name: 'comment', index:'comment', width: 150, align: 'left', editable:false},
                { name: 'status', index:'status', width: 500, align: 'left'}
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
                OssBulkGridWarningMessageUtil.setMessagesToCurPage();
            },
            ondblClickRow: function(rowid,iRow,iCol,e) {
                if(rowid) {
                    if (rowid != _mainLastsel) {
                        $("#list").jqGrid('saveRow', _mainLastsel);
                        OssBulkGridWarningMessageUtil.setMessageToRow(_mainLastsel);
                    }
                    OssBulkGridWarningMessageUtil.cleanMessageOfRow(rowid);
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
                OssBulkGridWarningMessageUtil.cleanMessages();

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
                $("#list").jqGrid('clearGridData');
                selectedIds = new Set()
                ossData = []
                failOss = []
                if (data['res'] == true){
                    jsonData = data['value'];
                    makeOssDTOList();
                    stubColData();
                    OssBulkGridWarningMessageUtil.call();
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
        IdentificationGridDataExtractor.call();
    });
    var fn = {
        downloadBulkSample : function(type){
			var logiPath = "/sample/FOSSLight-OSS-Bulk-Sample.xls";
			var fileName = "FOSSLight-OSS-Bulk-Sample.xls";

			location.href = '<c:url value="/partner/sampleDownload?fileName='+fileName+'&logiPath='+logiPath+'"/>';
		}
	}

    const IdentificationGridDataExtractor = {
        call : function (){
            if(this.isFromProjectIdentification()) {
                $.ajax({
                    contentType: 'application/json',
                    url: `/project/identificationGrid/\${referenceId}/\${referenceDiv}?referenceId=\${referenceId}&merge=N`,
                    dataType: "json",
                    success: (data) => {
                        if (data.validData) {
                            this.addRows(data);
                            OssBulkGridWarningMessageUtil.call();
                        }
                    },
                    error: (e) => {
                        console.log(e);
                    }
                })
            }
        },
        isFromProjectIdentification : function (){
            return referenceId && referenceDiv;
        },
        addRows(data) {
            const mainDataMap = this.getMapOfMainData(data.rows);
            const validData = data.validData;

            jsonData = [];
            delete validData.isValid;

            const ids = this.getInValidGridIds(validData);

            for(let i = 0; i < ids.length; ++i) {
                const newRow = this.toOssInfo(mainDataMap[ids[i]]);
                newRow.gridId = i;
                $("#list").jqGrid('addRowData', i, newRow);
                ossData.push(newRow);
                jsonData.push({oss: newRow});
            }
            $("#list").trigger('reloadGrid');
        },
        getInValidGridIds(validData) {
            let inValidGridId = new Set();

            let ossVersionUnconfirmedMsg = '<%=T2CoValidationConfig.getInstance().getRuleAllMap().get("OSS_VERSION.UNCONFIRMED.MSG")%>';
            let ossNameUnconfirmedMsg = '<%=T2CoValidationConfig.getInstance().getRuleAllMap().get("OSS_NAME.UNCONFIRMED.MSG")%>'

            for (let key in validData){
                if (validData[key] == ossNameUnconfirmedMsg || validData[key] == ossVersionUnconfirmedMsg){
                    inValidGridId.add(key.split(".")[1])
                }
            }
            return Array.from(inValidGridId);
        },
        getMapOfMainData(mainData) {
            return mainData.reduce((obj, x) => {
                obj[x.componentId] = x;
                return obj;
            }, {});
        },
        toOssInfo(data) {
            return {
                //tab datas
                comment: "",
                copyright: data.copyrightText,
                downloadLocation: data.downloadLocation,
                homepage: data.homepage,
                declaredLicenses: data.licenseName.trim() ? data.licenseName.trim().split(",") : [],
                ossName: data.ossName,
                ossVersion: data.ossVersion,
                //oss datas
                attribution: "",
                detectedLicenses: [],
                summaryDescription: "",
                ossNicknames: []
            }
        }
    }
    /**
     * change background color of failed oss row
     * */
    function makeBackGroundColor(){
        for (const failOssGridId of failOss) {
            $("#list").jqGrid("setRowData", failOssGridId, false, {'background' : "rgba(255, 0, 0, 0.3)"});
        }
    }

    const OssBulkGridStatusMessageManager = {
        cleanStatusMessages: function () {
            $("#list").jqGrid("getGridParam", "data").forEach(row => {
                row.status = ""
            });
        },
        setStatusMessages: function (results) {
            for (const result of results){
                $("#list").jqGrid("getLocalRow", result.gridId).status = result.msg;
                if (!result.status) {
                    failOss.push(result.gridId);
                }
            }
            $("#list").trigger('reloadGrid', [{ page: 1}]);
        }
    };

    const OssBulkGridWarningMessageUtil = (function() {
        let validData = {};
        const gridStr = "list";
        return {
            call: function () {
                this.cleanMessages();
                return this.requestMessages()
                    .then(response => {
                        this.setValidData(response);
                        this.setMessagesToCurPage();
                    });
            },
            requestMessages: function() {
                return $.ajax({
                    type: "POST",
                    contentType: 'application/json',
                    url: "/oss/bulkValidation",
                    data: JSON.stringify(this.requestBody()),
                    cache : false,
                    dataType: "json"
                })
            },
            requestBody: function() {
                ossData.forEach((row) => {
                    stringDataForValid.forEach((strData) => {
                        if(row[strData] === null || row[strData] === undefined)
                            row[strData] = "";
                    })
                    listDataForValid.forEach((listData) => {
                        if(row[listData] === null || row[listData] === undefined)
                            row[listData] = [];
                    })
                })
                return ossData;
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
