$(document).ready(function () {

  // Tutorial Mode = true
  if (getCookie("tutorial") != null) {

  // Array of elements to highlight
  let array_highlights = [];
  // Array of titles and content to be displayed by the tooltip
  let array_tooltip_data = [
    { title: "Start", content: "Packaging Column 내 start 버튼을 클릭합니다." },
  ];

  // create vail
  let $veilUp = $('<div id="veil_up"></div>');
  let $veilDown = $('<div id="veil_down"></div>');
  let $veilLeft = $('<div id="veil_left"></div>');
  let $veilRight = $('<div id="veil_right"></div>');

  // create tooltip
  let $tooltip = $(
    `<div id="tooltip">
            <div id="p_tooltip_title_container">
              <div id="p_tooltip_title"></div>
              <button id="p_button_close_tooltip">X</button>
            </div>
            <p id="p_tooltip_content"></p>
            <div>
              <button id="p_button_prev">이전</button>
              <button id="p_button_next">다음</button>
            </div>
          </div>`
  );

  // Put vail and tooltips in body tag
  $("body").append($veilUp);
  $("body").append($veilDown);
  $("body").append($veilLeft);
  $("body").append($veilRight);
  $("body").append($tooltip);

  // vail, tooltip default style setting
  const vails = [$veilUp, $veilDown, $veilLeft, $veilRight];
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
  $("#p_tooltip_title_container").css({
    display: "flex",
    "justify-content": "space-between",
    "align-items": "center",
  });
  $("#p_tooltip_title").css({
    display: "inline-block",
    "font-weight": 600,
  });
  $("#p_tooltip_content").css({
    "margin-top": "5px",
    "margin-bottom": "15px",
  });

  // Set the part to highlight
  let elem_index = 0;
  let elem_highlight = array_highlights[elem_index];

  // When 'continue tutorial' is clicked in the project list
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
  // When 'continue tutorial' is clicked in the packaging
  $("#continue_tutorial_26").on("click", () => {
    let $element26 = $(document.getElementById("tutorial_26"));
    let $element27 = $(document.getElementById("tutorial_27"));
    let $element28 = $(document.getElementById("tutorial_28"));
    let $element29 = $(document.getElementById("tutorial_29"));
    let $element30 = $(document.getElementById("tutorial_30"));
    let $element31 = $(document.getElementById("noticePreview"));
    let $element32 = $(document.getElementById("packageDocDownload"));
    let $element33 = $(document.getElementById("save"));
    array_highlights.push(
      $element26,
      $element27,
      $element28,
      $element29,
      $element30,
      $element31,
      $element32,
      $element33
    );
    array_tooltip_data = [
      {
        title: "Upload",
        content: "Sample Source Code를 다운로드 후, Upload 합니다.",
      },
      {
        title: "Verify",
        content:
          "Verify를 클릭하면 README, File List, Banned List 버튼이 활성화된 것을 확인할 수 있습니다.",
      },
      { title: "Notice", content: "Notice Tab으로 이동합니다." },
      {
        title: "Notice",
        content: `"Request to generate a modified OSS Notice.를 체크하여 OSS Notice를 변경할 수 있습니다.`,
      },
      {
        title: "Notice File Format",
        content: `OSS Notice File Format을 추가적으로 체크할 수 있습니다.`,
      },
      {
        title: "Preview",
        content: `Preview 버튼을 클릭하여 OSS Notice를 확인한 후 우측 하단 OK 버튼을 클릭합니다.`,
      },
      {
        title: "Download",
        content: `Download 버튼을 클릭하면 OSS Notice 파일을 미리 다운로드할 수 있습니다.`,
      },
      { title: "Save", content: `Save 버튼을 클릭합니다.` },
    ];
    elem_index = 0;
    elem_highlight = array_highlights[elem_index];
    locate_vails();
    show_vails();
    locate_tooltip();
    show_tooltip();
  });

  // Changing the browser window size Adjust the position of the vail and tooltip
  $(window).resize(() => {
    locate_vails();
    locate_tooltip();
  });

  // Tutorial - Proper handling of previous, next, and close buttons
  $("#p_button_prev").on("click", handle_click_prev);
  $("#p_button_next").on("click", handle_click_next);
  $("#p_button_close_tooltip").on("click", hide_vails_and_tooltips);

  //////////////////////////////// FUNCTIONS ////////////////////////////////

  // Set the position and size of each of the 4 vails
  function locate_vails() {
    let offset = elem_highlight.offset();

    $veilUp.css({
      top: 0,
      left: 0,
      width: "100%",
      height: `${offset.top}`,
    });
    $veilDown.css({
      top: `${offset.top + elem_highlight.outerHeight(true)}px`,
      left: 0,
      width: "100%",
      height: `calc(100% - ${offset.top + elem_highlight.outerHeight(true)}px)`,
    });
    $veilLeft.css({
      top: `${offset.top}px`,
      left: 0,
      width: `${offset.left}px`,
      height: `${elem_highlight.outerHeight(true)}`,
    });
    $veilRight.css({
      top: `${offset.top}px`,
      left: `${offset.left + elem_highlight.outerWidth(true)}px`,
      width: `calc(100% - ${offset.left + elem_highlight.outerWidth(true)}px)`,
      height: `${elem_highlight.outerHeight(true)}`,
    });
  }

  // Make all vail visible
  function show_vails() {
    for (const vail of vails) vail.css("display", "block");
  }

  // Position the tooltip appropriately
  function locate_tooltip() {
    if (!elem_highlight) return;
    let offset = elem_highlight.offset();

    // Edit the title and content to the nth title and content
    $("#p_tooltip_title").text(array_tooltip_data[elem_index].title);
    $("#p_tooltip_content").text(array_tooltip_data[elem_index].content);

    // Adjust the position to match the highlight element
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

  // Make the tooltip visible on the screen
  function show_tooltip() {
    $tooltip.css({ display: "flex" });
  }

  // Make both vail and tooltip invisible on the screen
  function hide_vails_and_tooltips() {
    $veilUp.css("display", "none");
    $veilDown.css("display", "none");
    $veilLeft.css("display", "none");
    $veilRight.css("display", "none");
    $tooltip.css("display", "none");
  }

  // Function executed when 'back' of tooltip is clicked
  function handle_click_prev() {
    if (elem_index === 0) {
      return;
    }
    elem_index -= 1;
    elem_highlight = array_highlights[elem_index];
    locate_vails();
    show_vails();
    locate_tooltip();
    show_tooltip();
  }

  // Function executed when 'Next' of tooltip is clicked
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
  }
  else{
        var tutorial_button25 = (document.getElementById("continue_tutorial_25"));
        tutorial_button25.style.display = 'none';
  }

  function getCookie(cookieName) {
            var cookieValue = null;
            if (document.cookie) {
                var array = document.cookie.split((escape(cookieName) + '='));
                if (array.length >= 2) {
                    var arraySub = array[1].split(';');
                    cookieValue = unescape(arraySub[0]);
                }
            }
            return cookieValue;
         }

});