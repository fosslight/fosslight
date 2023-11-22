$(window).load(function () {
    resizingJqGidSet();
    resizingInnerJqGidSet();
    resizingOuterJqGidSet();
    /* 브라우저 창 크기에 따라 jqGrid Width 자동 조절 */
    $(window)
        .bind("resize", function () {
            resizingJqGidSet();
            resizingInnerJqGidSet();
            resizingOuterJqGidSet();
        })
        .trigger("resize");
});

function resizingJqGidSet() {
    if ($(".jqGridSet table").length > 0) {
        $(".jqGridSet table").jqGrid("setGridWidth", 0, true);
        $(".jqGridSet table").jqGrid("setGridWidth", $(".jqGridSet").width(), true);
    }
}

function resizingInnerJqGidSet() {
    if ($(".innerJqGridSet table").length > 0) {
        $(".innerJqGridSet table").jqGrid("setGridWidth", 0, true);
        $(".innerJqGridSet table").jqGrid(
            "setGridWidth",
            $(window).innerWidth() - 380,
            true
        );
    }
}

function resizingOuterJqGidSet() {
    if ($(".outerJqGridSet table").length > 0) {
        $(".outerJqGridSet table").jqGrid("setGridWidth", 0, true);
        $(".outerJqGridSet table").jqGrid(
            "setGridWidth",
            $(window).innerWidth() - 50,
            true
        );
    }
}

var searchStringOptions = {
    searchoptions: { sopt: ["cn", "eq", "ne", "bw", "bn", "ew", "en", "nc"] },
};
var searchNumberOptions = {
    searchoptions: { sopt: ["ge", "le", "gt", "lt", "eq"] },
};
var searchDateOptions = {
    searchoptions: { sopt: ["eq", "lt", "le", "gt", "ge"] },
};

var numberCommonOptions = {
    align: "center",
    resizable: true,
    editable: true,
    template: searchNumberOptions,
    edittype: "text",
    editoptions: { size: 1, maxlength: 15 },
};

var stringCommonOptions = {
    align: "left",
    resizable: true,
    editable: true,
    template: searchStringOptions,
    edittype: "text",
};

function displayUrl(cellvalue) {
    var icon1 =
        '<a href="https://opensource.org/licenses/BSD-2-Clause" class="urlLink" target="_blank">https://opensource.org/licenses/BSD-2-Clause</a>';
    return icon1;
}

function displayButtons(cellvalue, options, rowObject) {
    var deleted = "<input type='button' value='delete' class='btn-secondary' />";
    return deleted;
}

function escapeHtml(str) {
    var map = {
        "&": "&amp;",
        "<": "&lt;",
        ">": "&gt;",
        '"': "&quot;",
        "'": "&#039;",
    };

    return str.replace(/[&<>"']/g, function (m) {
        return map[m];
    });
}

function appendMultiTag(e, elId_01, elId_02, tgId) {
    e.preventDefault();

    const fword = $("#" + elId_01).val();
    const lword = $("#" + elId_02).val();
    const word = fword + " / " + lword;

    if (!fword || !lword || fword.length === 0 || lword.length === 0) {
        return;
    }

    const el = $("<div/>", {
        class: "external-event",
        text: word,
    }).append(
        '<i class="fas fa-times float-right mt-1" name="deleteTagButton"></i>'
    );

    $("#" + tgId).prepend(el);
    $("#" + elId_01 + ", #" + elId_02)
        .val(null)
        .trigger("change");
}

function appendSingleTag(e, id) {
    e.preventDefault();
    const word = $("#input_" + id).val();

    if (!word || word.length === 0) {
        return;
    }

    const el = $("<div/>", {
        class: "external-event",
        name: id,
        text: word
    }).append(
        '<i class="fas fa-times text-blue-gray float-right mt-1" name="deleteTagButton"></i>'
    );

    $("#appendArea_" + id).prepend(el);
    $("#input_" + id)
        .val(null)
        .trigger("change");
}


