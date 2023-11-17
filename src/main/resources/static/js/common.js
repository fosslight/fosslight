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

// $(window)
//   .bind("resize", function () {
//     if ($(".jqGridSet table").length > 0) {
//       // 그리드의 width 초기화
//       $(".jqGridSet table").jqGrid("setGridWidth", 0, true);
//       // 그리드의 width를 div 에 맞춰서 적용
//       $(".jqGridSet table").jqGrid(
//         "setGridWidth",
//         $(".jqGridSet").width(),
//         true
//       );
//     }
//   })
//   .trigger("resize");

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

// nav link 따라 main grids / bom grids 를 전환
// nav link 가 3rd party, src, bin 이면 main grids 를 표시하고, bom 이면 bom grids 표시
function toggleMainGridsOnNavLink() {
    $(".nav-link").on("click", function (event) {
        const elementId = $(this).attr("id");

        $("[name='mainGrids-area']").hide();

        if (elementId === "tab-menu-tParty") $("#mainGrids-tParty").show();
        else if (elementId === "tab-menu-dep") $("#mainGrids-dep").show();
        else if (elementId === "tab-menu-src") $("#mainGrids-src").show();
        else if (elementId === "tab-menu-bin") $("#mainGrids-bin").show();
        else if (elementId === "tab-menu-bom") $("#mainGrids-bom").show();
        else if (elementId === "tab-menu-yocto") $("#mainGrids-yocto").show();
        else if (elementId === "tab-menu-bat") $("#mainGrids-bat").show();
    });
}

// radio check 에 대응하는 content 를 표시
function showRadioCheckedContent() {
    $('input[type="radio"]').each(function () {
        if (this.checked) {
            console.log(this.id);
            const elementId = this.id.split("_")[1];
            const categoryId = elementId.split("-")[0];

            $('[name^="content_' + categoryId + '"]').hide();

            console.log(elementId);
            $("#content_" + elementId).show();
        }
    });
}

// radio check 여부에 따라 content를 전환
function toggleRadioContentEvent() {
    $('input[type="radio"]').on("change", function () {
        const elementId = this.id.split("_")[1];
        const categoryId = elementId.split("-")[0];

        $('[name^="content_' + categoryId + '"]').hide();
        createSubGrids(categoryId);

        if (this.checked) {
            $("#content_" + elementId).show();
        }
    });
}

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
