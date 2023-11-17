
function ajaxRequest(url, method, dataType, successCallback, errorCallback) {
    $.ajax({
        url: url,
        type: method,
        dataType: dataType,
        success: successCallback,
        error: errorCallback
    });
}
