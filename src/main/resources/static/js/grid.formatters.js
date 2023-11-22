const oss_list_format = {
    ossTypeFormat: function (cellvalue, options, rowObject) {
        var display = "";

        if (cellvalue.includes("M")) {
            display += "<span class=\"badge badge-primary mr-1\">M</span>";
        }
        if (cellvalue.includes("D")) {
            display += "<span class=\"badge badge-info mr-1\">D</span>";
        }
        if (cellvalue.includes("V")) {
            display += "<span class=\"badge badge-success mr-1\">V</span>";
        }

        return display;
    },
    ossNameLinkFormat : function(cellvalue, options, rowObject){
        var display = "";

        if("N" == linkFlag){
            var _frameId = rowObject['ossId'] + "_Opensource";
            var _frameTarget = "#/oss/edit/" + rowObject['ossId'];
            display = "<a class='urlLink' href=\"javascript:;\" onclick=\"createTabInFrame('"+_frameId+"','"+_frameTarget+"')\" >" + cellvalue + "</a>";
        } else {
            var url = '';
            if("${ct:isAdmin()}"){
                url = '<c:url value="/oss/edit/'+rowObject['ossId']+'"/>';
            } else {
                url = '<c:url value="/oss/view/'+rowObject['ossId']+'"/>';
            }
            display = "<a href='" + url + "' class='urlLink' target='_blank'>" + cellvalue + "</a>";
        }

        return display;
    }

}