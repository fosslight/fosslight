$(document).ready(function (){
    // 하이라이트할 요소들의 배열
    let array_highlights = [];
    // 툴팁이 보여줄 제목 및 내용의 배열
    let array_tooltip_data = [
        { title: "Start", content: "Packaging Column 내 start 버튼을 클릭합니다." },
    ];

    // vail 생성
    let $vailUp = $('<div id="vail_up"></div>');
    let $vailDown = $('<div id="vail_down"></div>');
    let $vailLeft = $('<div id="vail_left"></div>');
    let $vailRight = $('<div id="vail_right"></div>');

    // tooltip 생성
    let $tooltip = $(
        `<div id="tooltip">
            <div id="tooltip_title_container">
              <div id="tooltip_title"></div>
              <button id="button_close_tooltip">X</button>
            </div>
            <p id="tooltip_content"></p>
            <div>
              <button id="button_prev">이전</button>
              <button id="button_next">다음</button>
            </div>
          </div>`
    );

    // body 태그 내에 vail, tooltip들 넣기
    $("body").append($vailUp);
    $("body").append($vailDown);
    $("body").append($vailLeft);
    $("body").append($vailRight);
    $("body").append($tooltip);

    // vail, tooltip 기본 style 설정
    const vails = [$vailUp, $vailDown, $vailLeft, $vailRight];
    const property_vail = {
        display: "none",
        background: "rgba(0, 0, 0, 0.2)",
        position: "absolute",
        "z-index": 100,
    };
    for (const vail of vails) vail.css(property_vail);
    $tooltip.css({
        display: "none",
        background: "white",
        padding: "10px",
        "z-index": 110,
        "border-radius": "10px",
        "box-shadow": "rgb(0 0 0 / 25%) 0px 0px 6px 2px",
        "flex-direction": "column",
    });
    $("#tooltip_title_container").css({
        display: "flex",
        "justify-content": "space-between",
        "align-items": "center",
    });
    $("#tooltip_title").css({
        display: "inline-block",
        "font-weight": 600,
    });
    $("#tooltip_content").css({
        "margin-top": "5px",
        "margin-bottom": "15px",
    });

    // 하이라이트할 부분 설정
    let elem_index = 0;
    let elem_highlight = array_highlights[elem_index];

    // project list에서 'continue tutorial' 눌렀을 때
    $("#continue_tutorial_25").on("click", () => {
        let $element25 = $(document.getElementById("tutorial_25"));
        array_highlights.push($element25);

        elem_index = 0;
        elem_highlight = array_highlights[elem_index];
        locate_vails();
        show_vails();
        locate_tooltip();
        show_tooltip();
    });
    // packaging에서 'continue tutorial' 눌렀을 때
    $("#continue_tutorial_26").on("click", () => {
        let $element26 = $(document.getElementById("tutorial_26"));
        let $element27 = $(document.getElementById("tutorial_27"));
        let $element28 = $(document.getElementById("tutorial_28"));
        let $element29 = $(document.getElementById("tutorial_29"));
        let $element30 = $(document.getElementById("tutorial_30"));
        let $element31 = $(document.getElementById("noticePreview"));
        let $element32 = $(document.getElementById("packageDocDownload"));
        let $element33 = $(document.getElementById("save"));
        array_highlights.push($element26, $element27, $element28, $element29, $element30, $element31, $element32, $element33);
        array_tooltip_data = [
            { title: "Upload", content: "Sample Source Code를 다운로드 후, Upload 합니다." },
            { title: "Verify", content: "Verify를 클릭하면 README, File List, Banned List 버튼이 활성화된 것을 확인할 수 있습니다." },
            { title: "Notice", content: "Notice Tab으로 이동합니다." },
            { title: "Notice", content: `"Request to generate a modified OSS Notice.를 체크하여 OSS Notice를 변경할 수 있습니다.` },
            { title: "Notice File Format", content: `OSS Notice File Format을 추가적으로 체크할 수 있습니다.` },
            { title: "Preview", content: `Preview 버튼을 클릭하여 OSS Notice를 확인한 후 우측 하단 OK 버튼을 클릭합니다.` },
            { title: "Download", content: `Download 버튼을 클릭하면 OSS Notice 파일을 미리 다운로드할 수 있습니다.` },
            { title: "Save", content: `Save 버튼을 클릭합니다.` },
        ];
        elem_index = 0;
        elem_highlight = array_highlights[elem_index];
        locate_vails();
        show_vails();
        locate_tooltip();
        show_tooltip();
    });


    // 브라우저 창 크기를 바꿀 시 vail, tooltip의 위치 조절
    $(window).resize(() => {
        locate_vails();
        locate_tooltip();
    });

    // 튜토리얼 - 이전, 다음, 닫기 버튼을 눌렀을 때 적절히 처리
    $("#button_prev").on("click", handle_click_prev);
    $("#button_next").on("click", handle_click_next);
    $("#button_close_tooltip").on("click", hide_vails_and_tooltips);

    //////////////////////////////// FUNCTIONS ////////////////////////////////

    // 4개 vail 각각의 위치, 크기 설정
    function locate_vails() {
        let offset = elem_highlight.offset();

        $vailUp.css({
            top: 0,
            left: 0,
            width: "100%",
            height: `${offset.top}`,
        });
        $vailDown.css({
            top: `${offset.top + elem_highlight.outerHeight(true)}px`,
            left: 0,
            width: "100%",
            height: `calc(100% - ${offset.top + elem_highlight.outerHeight(true)}px)`,
        });
        $vailLeft.css({
            top: `${offset.top}px`,
            left: 0,
            width: `${offset.left}px`,
            height: `${elem_highlight.outerHeight(true)}`,
        });
        $vailRight.css({
            top: `${offset.top}px`,
            left: `${offset.left + elem_highlight.outerWidth(true)}px`,
            width: `calc(100% - ${offset.left + elem_highlight.outerWidth(true)}px)`,
            height: `${elem_highlight.outerHeight(true)}`,
        });
    }

    // vail 모두 보이게 설정
    function show_vails() {
        for (const vail of vails) vail.css("display", "block");
    }

    // tooltip의 위치를 적절히 설정
    function locate_tooltip() {
        if (!elem_highlight) return;
        let offset = elem_highlight.offset();

        // 제목, 내용을 n번째 제목, 내용으로 수정
        $("#tooltip_title").text(array_tooltip_data[elem_index].title);
        $("#tooltip_content").text(array_tooltip_data[elem_index].content);

        // 위치를 highlight element에 맞게 수정
        const tooltip_width = 150;
        let tooltip_left =
            offset.left - (tooltip_width - elem_highlight.outerWidth(true)) / 2;
        if (tooltip_left < 0) tooltip_left = offset.left;

        $tooltip.css({
            position: "absolute",
            top: `${offset.top + elem_highlight.outerHeight(true) + 10}px`,
            left: `${tooltip_left}px`,
            width: `${tooltip_width}px`,
        });
    }

    // tooltip을 화면에 보이게 함
    function show_tooltip() {
        $tooltip.css({ display: "flex" });
    }

    // vail, tooltip을 모두 화면에 보이지 않게 함
    function hide_vails_and_tooltips() {
        $vailUp.css("display", "none");
        $vailDown.css("display", "none");
        $vailLeft.css("display", "none");
        $vailRight.css("display", "none");
        $tooltip.css("display", "none");
    }

    // tooltip의 '이전' 클릭시 실행되는 함수
    function handle_click_prev() {
        if (elem_index === 0) return;
        elem_index -= 1;
        elem_highlight = array_highlights[elem_index];
        locate_vails();
        show_vails();
        locate_tooltip();
        show_tooltip();
    }

    // tooltip의 '다음' 클릭시 실행되는 함수
    function handle_click_next() {
        if (elem_index === array_highlights.length - 1) {
            hide_vails_and_tooltips();
            return;
        }
        elem_index += 1;
        elem_highlight = array_highlights[elem_index];
        locate_vails();
        show_vails();
        locate_tooltip();
        show_tooltip();
    }
});