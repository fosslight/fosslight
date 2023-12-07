// license list page grid formatter
const lic_lst_fmt = {
    setGridParam: function () {
        var paramData = $('#licenseSearch').serializeObject();

        if (paramData.restrictions != null) {
            paramData.restrictions = JSON.stringify(paramData.restrictions);
            paramData.restrictions = paramData.restrictions.replace(/\"|\[|\]/gi, "");
        } else {
            paramData.restrictions = "";
        }

        return paramData;
    },

    licenseNameFormatter: function (cellvalue, options, rowObject) {
        const display = "<a class='urlLink' href=\"javascript:;\">" + cellvalue + "</a>";
        return display;
    },
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

// oss list page grid formatter
const oss_lst_fmt = {
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
        const display = "<a href=\"javascript:void(0);\" class='urlLink'>" + cellvalue + "</a>";
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

// oss edit page grid formatter
const oss_edit_fmt = {
    displayButtons : function (cellvalue, options, rowObject) {
        return "<input type=\"button\" value=\"delete\" class=\"btn btn-default\" onclick=\"exeDelete(" + options.rowId + ")\" />";
    },
}

// system code page grid formatter
const sysCode_fmt = {
    numberCheckFormat: function(){
        if(!( (event.keyCode >= 48
                && event.keyCode <= 57)
            || (event.keyCode >= 96
                && event.keyCode <= 105)
            || event.keyCode == 8
            || event.keyCode == 9 ) ) {
            event.returnValue = false;
        }
    }
}

// common used grid formatter
const common_fmt = {
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


