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
    unformatter: function (cellvalue, options, rowObject) {
        return cellvalue;
    },
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


