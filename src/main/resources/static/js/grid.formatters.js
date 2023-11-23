// license grid formatter
const license_grid_format = {
    displayLicenseRestriction: function (cellvalue, options, rowObject) {
        var display = "";

        if (cellvalue != "" && cellvalue != undefined) {
            display =
                '<span class="badge badge-warning text-xm" data-toggle="tooltip" data-placement="top" title="' +
                cellvalue +
                '" onclick="src_fn_com.showLicenseRestrictionViewPage(\'' +
                options.gid +
                "','" +
                options.rowId +
                "')\">R</span>";
        }

        return display;
    },
    unformatter: function (cellvalue, options, rowObject) {
        return cellvalue;
    },
}

let linkFlag;
// oss grid formatter
const oss_grid_format = {
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
    },
    obligationTypeFormat : function (cellvalue, options, rowObject) {
        var display = "";
        switch(cellvalue) {
            case "10" :
                display = "<i class=\"far fa-file-alt fa-lg\" title=\"Notice\"></i>";

                break;
            case "11" :
                display = "<i class=\"far fa-file-alt fa-lg\" title=\"Notice\"></i><i class=\"far fa-file-code fa-lg ml-1\" title=\"Source Code\"></i>";

                break;
            default:
                display = '';

                break;
        }
        return display;
    }
}

const common_grid_format = {
    truncateText: function (cellValue, options, rowObject) {
        var maxLength = options.colModel.maxlength;
        var firstLine = cellValue.split('\n')[0];

        if (firstLine.length > maxLength) {
            return cellValue.substring(0, maxLength) + "...";
        } else {
            cellValue = firstLine;
        }
        return cellValue;
    }
}