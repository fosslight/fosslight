<<<<<<< HEAD
$(document).ready(function () {
  // Create elements to highlight
  let $button1 = $(document.getElementById("login_space"));

  // Array of elements to highlight (button 1, 2, 3, 4)
  let array_highlights = [$button1]; //, $button3, $button4, $button5
  // Array of titles and content to be displayed by the tooltip
  let array_tooltip_data = [
    {
      title: "Login",
      content:
        " ID : admin, Password : admin 으로 입력 후 login 버튼을 클릭합니다.",
    },
  ];

  // create vail
=======
// 한지영
$(document).ready(function () {
  // 하이라이트할 요소들 생성
  let $button1 = $(document.getElementById("srcCsvFile"));
  let $button2 = $(document.getElementById("srcSaveUp"));
  let $button3 = $(document.getElementById("bin_tab"));

  // let $button4 = $(document.getElementById("bList"));
  let $button5 = $(document.getElementById("bom_tab"));

  let $button6 = $(document.getElementById("bomSaveUp"));
  let $button7 = $(document.getElementById("projdBtn"));
  let $button8 = $(document.getElementById("btnReview"));

  // 하이라이트할 요소들의 배열
  let array_highlights = [
    $button1,
    $button2,
    $button3,
    $button5,
    $button6,
    $button7,
    $button8,
  ];

  // 툴팁이 보여줄 제목 및 내용의 배열
  let array_tooltip_data = [
    {
      title: "upload",
      content:
        "OSS List가 쓰여진 Sample FOSSLight Report(https://fosslight.org/fosslight-guide/tutorial/result_files/sample_src.tar.gz)를 다운로드 후, Upload 합니다. 이때 Load 할 Sheet로 SRC Sheet를 선택합니다.",
    },
    { title: "Save", content: "Save 버튼을 클릭합니다." },
    { title: "BIN Tab", content: "BIN Tab으로 이동합니다." },

    // { title: "plus", content: "+ 버튼을 클릭합니다." },
    { title: "BOM Tab", content: "BOM Tab으로 이동합니다." },

    { title: "Merge And Save", content: "Merge And Save 버튼을 클릭합니다." },
    { title: "Request", content: "Request 버튼을 클릭하여 리뷰를 요청합니다." },
    { title: "Review", content: "Review Start 버튼을 클릭합니다." },
  ];

  // vail 생성
>>>>>>> eb2b8fe8 (Match indent)
  let $vailUp = $('<div id="vail_up"></div>');
  let $vailDown = $('<div id="vail_down"></div>');
  let $vailLeft = $('<div id="vail_left"></div>');
  let $vailRight = $('<div id="vail_right"></div>');

<<<<<<< HEAD
  // create tooltip
=======
  // tooltip 생성
>>>>>>> eb2b8fe8 (Match indent)
  let $tooltip = $(
    `<div id="tooltip">
            <div id="tooltip_title_container">
              <div id="tooltip_title"></div>
              <button id="button_close_tooltip">X</button>
            </div>
            <p id="tooltip_content"></p>
            <div>
            </div>
          </div>`
  );

<<<<<<< HEAD
  $("#login_space").append($button1);

  // Put vail and tooltips in body tag
=======
  // body 태그 내에 vail, tooltip들 넣기
>>>>>>> eb2b8fe8 (Match indent)
  $("body").append($vailUp);
  $("body").append($vailDown);
  $("body").append($vailLeft);
  $("body").append($vailRight);
  $("body").append($tooltip);

<<<<<<< HEAD
  // vail, tooltip default style setting
=======
  // vail, tooltip 기본 style 설정
>>>>>>> eb2b8fe8 (Match indent)
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

<<<<<<< HEAD
  // Set the part to highlight
=======
  // 하이라이트할 부분 설정
>>>>>>> eb2b8fe8 (Match indent)
  let elem_index = 0;
  let elem_highlight = array_highlights[elem_index];

  $("#start_tutorial").on("click", () => {
    elem_index = 0;
    elem_highlight = array_highlights[elem_index];
    locate_vails();
    show_vails();
    locate_tooltip();
    show_tooltip();
  });
<<<<<<< HEAD
  // Changing the browser window size
  // Adjust the position of the vail and tooltip
=======

  // 브라우저 창 크기를 바꿀 시
  // vail, tooltip의 위치 조절
>>>>>>> eb2b8fe8 (Match indent)
  $(window).resize(() => {
    locate_vails();
    locate_tooltip();
  });

<<<<<<< HEAD
  // Tutorial - Proper handling of previous, next, and close buttons
=======
  // 튜토리얼 - 이전, 다음, 닫기 버튼을 눌렀을 때 적절히 처리
>>>>>>> eb2b8fe8 (Match indent)
  $("#button_prev").on("click", handle_click_prev);
  $("#button_next").on("click", handle_click_next);
  $("#button_close_tooltip").on("click", hide_vails_and_tooltips);

  //////////////////////////////// FUNCTIONS ////////////////////////////////

<<<<<<< HEAD
  // Set the position and size of each of the 4 vails
=======
  // 4개 vail 각각의 위치, 크기 설정
>>>>>>> eb2b8fe8 (Match indent)
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

<<<<<<< HEAD
  // Make all vail visible
=======
  // vail 모두 보이게 설정
>>>>>>> eb2b8fe8 (Match indent)
  function show_vails() {
    for (const vail of vails) vail.css("display", "block");
  }

<<<<<<< HEAD
  // Position the tooltip appropriately
=======
  // tooltip의 위치를 적절히 설정
>>>>>>> eb2b8fe8 (Match indent)
  function locate_tooltip() {
    if (!elem_highlight) return;
    let offset = elem_highlight.offset();

<<<<<<< HEAD
    // Edit the title and content to the nth title and content
    $("#tooltip_title").text(array_tooltip_data[elem_index].title);
    $("#tooltip_content").text(array_tooltip_data[elem_index].content);

    // Adjust the position to match the highlight element
=======
    // 제목, 내용을 n번째 제목, 내용으로 수정
    $("#tooltip_title").text(array_tooltip_data[elem_index].title);
    $("#tooltip_content").text(array_tooltip_data[elem_index].content);

    // 위치를 highlight element에 맞게 수정
>>>>>>> eb2b8fe8 (Match indent)
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

<<<<<<< HEAD
  // Make the tooltip visible on the screen
=======
  // tooltip을 화면에 보이게 함
>>>>>>> eb2b8fe8 (Match indent)
  function show_tooltip() {
    $tooltip.css({ display: "flex" });
  }

<<<<<<< HEAD
  // Make both vail and tooltip invisible on the screen
=======
  // vail, tooltip을 모두 화면에 보이지 않게 함
>>>>>>> eb2b8fe8 (Match indent)
  function hide_vails_and_tooltips() {
    $vailUp.css("display", "none");
    $vailDown.css("display", "none");
    $vailLeft.css("display", "none");
    $vailRight.css("display", "none");
    $tooltip.css("display", "none");
  }

<<<<<<< HEAD
  // Function executed when 'back' of tooltip is clicked
=======
  // tooltip의 '이전' 클릭시 실행되는 함수
>>>>>>> eb2b8fe8 (Match indent)
  function handle_click_prev() {
    if (elem_index === 0) return;
    elem_index -= 1;
    elem_highlight = array_highlights[elem_index];
    locate_vails();
    show_vails();
    locate_tooltip();
    show_tooltip();
  }

<<<<<<< HEAD
  // Function executed when 'Next' of tooltip is clicked
=======
  // tooltip의 '다음' 클릭시 실행되는 함수
>>>>>>> eb2b8fe8 (Match indent)
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
<<<<<<< HEAD
});
=======
});
>>>>>>> eb2b8fe8 (Match indent)
