$(document).ready(function() {

  // Tutorial Mode = true
  if (getCookie("tutorial") != null) {

  // Create elements to highlight
  let $button1 = $(document.getElementById("third_party"));
  let $button2 = $(document.getElementById("applicableParty"));
  let $button3 = $(document.getElementById("btnPartnerSaveUp"));
  let $button4 = $(document.getElementById("src_tab"));
  let $button5 = $(document.getElementById("srcCsvFile"));
  let $button6 = $(document.getElementById("srcSaveUp"));
  let $button7 = $(document.getElementById("bin_tab"));
  let $button8 = $(document.getElementById("jqGrid"));
  let $button9 = $(document.getElementById("jqGrid"));
  let $button10 = $(document.getElementById("binSave"));
  let $button11 = $(document.getElementById("bom_tab"));
  let $button12 = $(document.getElementById("bomSaveUp"));
  let $button13 = $(document.getElementById("request"));
  let $button14 = $(document.getElementById("reviewStart"));
  let $button15 = $(document.getElementById("confirm"));

  // Array of elements to highlight
  let array_highlights = [
    $button1, $button2, $button3,
    $button4, $button5, $button6,
    $button7, $button8, $button9,
    $button10, $button11, $button12,
    $button13, $button14, $button15];

  // Array of titles and content to be displayed by the tooltip
  let array_tooltip_data = [
    { title: "3rd Party", content: "3rd Party Tab 으로 이동합니다." },
    { title: "Not applicable", content: "Not applicable 을 체크합니다." },
    { title: "Save", content: "Save 를 클릭합니다" },
    { title: "SRC", content: "SRC Tab 으로 이동합니다." },
    { title: "Upload Report", content: "OSS List가 쓰여진 Sample FOSSLight Report(https://fosslight.org/fosslight-guide/tutorial/result_files/sample_src.tar.gz)를 다운로드 후, Upload 합니다. 이때 Load 할 Sheet로 SRC Sheet를 선택합니다." },
    { title: "Save", content: "Save 버튼을 클릭합니다." },
    { title: "BIN Tab", content: "BIN Tab으로 이동합니다." },
    { title: "Plus Button", content: "+ 버튼을 클릭합니다." },
    { title: "Fill Out Informations", content: "OSS 정보를 기입합니다. *필수입력: OSS Name, License" },
    { title: "Save", content: "Save 버튼을 클릭합니다." },
    { title: "BOM Tab", content: "BOM Tab으로 이동합니다." },
    { title: "Merge And Save", content: "Merge And Save 버튼을 클릭합니다." },
    { title: "Request", content: "Request 버튼을 클릭하여 리뷰를 요청합니다." },
    { title: "Review Start", content: "Review Start 버튼을 클릭합니다." },
    { title: "Confirm", content: "Confirm 버튼을 클릭합니다." },
  ];

  // create veil
  let $veilUp = $('<div id="veil_up"></div>');
  let $veilDown = $('<div id="veil_down"></div>');
  let $veilLeft = $('<div id="veil_left"></div>');
  let $veilRight = $('<div id="veil_right"></div>');

  // create tooltip
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

  $(".tabMenu").prepend($button1);
  $("#applicableParty").prepend($button2);
  $("#btnPartnerSaveUp").append($button3);
  $("#src_tab").append($button4);

  // Put veil and tooltips in body tag
  $("body").append($veilUp);
  $("body").append($veilDown);
  $("body").append($veilLeft);
  $("body").append($veilRight);
  $("body").append($tooltip);

  // veil, tooltip default style setting
  const veils = [$veilUp, $veilDown, $veilLeft, $veilRight];
  const property_veil = {
    display: "none",
    background: "rgba(0, 0, 0, 0.2)",
    position: "absolute",
    "z-index": 100,
  };
  for (const veil of veils) veil.css(property_veil);
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

  // Set the part to highlight
  let elem_index = 0;
  let elem_highlight = array_highlights[elem_index];
  let clickedReview = 0;

  $("#continue_tutorial").on("click", () => {
    elem_index = 0;
    if(clickedReview === 1) {
      elem_index = 13;
    }
    elem_highlight = array_highlights[elem_index];
    locate_veils();
    show_veils();
    locate_tooltip();
    show_tooltip();
  });
  // Changing the browser window size
  // veil, tooltip default style setting
  $(window).resize(() => {
    locate_veils();
    locate_tooltip();
  });

  // Tutorial - Proper handling of previous, next, and close buttons
  $("#button_prev").on("click", handle_click_prev);
  $("#button_next").on("click", handle_click_next);
  $("#button_close_tooltip").on("click", hide_veils_and_tooltips);

  //////////////////////////////// FUNCTIONS ////////////////////////////////

  // Set the position and size of each of the 4 veils
  function locate_veils() {
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

  // veil make all visible
  function show_veils() {
    for (const veil of veils) {
      veil.css("display", "block");
    }
  }

  // Position the tooltip appropriately
  function locate_tooltip() {
    if (!elem_highlight) return;
    let offset = elem_highlight.offset();

  // Edit the title and content to the nth title and content
    $("#tooltip_title").text(array_tooltip_data[elem_index].title);
    $("#tooltip_content").text(array_tooltip_data[elem_index].content);

    // Adjust the position to match the highlight element
    const tooltip_width = 150;
    let tooltip_left =
        offset.left - (tooltip_width - elem_highlight.outerWidth(true)) / 2;
    if (tooltip_left < 0) tooltip_left = offset.left;

    switch (elem_index) {
      case 4:
        $tooltip.css({
          position: "absolute",
          top: `${offset.top + elem_highlight.outerHeight(true) + 10}px`,
          left: `${tooltip_left}px`,
          width: `${tooltip_width + 150}px`,
        });
        break;
      case 7:
        $tooltip.css({
          position: "absolute",
          top: `${offset.top + elem_highlight.outerHeight(true) - 729}px`,
          left: `${tooltip_left - 318.5}px`,
          width: `${tooltip_width}px`,
        });
        break;
      case 8:
        $tooltip.css({
          position: "absolute",
          top: `${offset.top + elem_highlight.outerHeight(true) -669}px`,
          left: `${tooltip_left}px`,
          width: `${tooltip_width + 150}px`,
        });
        break;
      case 2: case 5: case 9:
        $tooltip.css({
          position: "absolute",
          top: `${offset.top + elem_highlight.outerHeight(true) + 10}px`,
          left: `${tooltip_left - 100}px`,
          width: `${tooltip_width}px`,
        });
        break;
      case 11:
        $tooltip.css({
          position: "absolute",
          top: `${offset.top + elem_highlight.outerHeight(true) + 10}px`,
          left: `${tooltip_left - 62}px`,
          width: `${tooltip_width}px`,
        });
        break;
      default:
        $tooltip.css({
          position: "absolute",
          top: `${offset.top + elem_highlight.outerHeight(true) + 10}px`,
          left: `${tooltip_left}px`,
          width: `${tooltip_width}px`,
        });
        break;
    }
  }

  // Make the tooltip visible on the screen
  function show_tooltip() {
    $tooltip.css({ display: "flex" });
  }

  // Make all veil and tooltip invisible on the screen
  function hide_veils_and_tooltips() {
    $veilUp.css("display", "none");
    $veilDown.css("display", "none");
    $veilLeft.css("display", "none");
    $veilRight.css("display", "none");
    $tooltip.css("display", "none");
  }

  // Function executed when 'back' of tooltip is clicked
  function handle_click_prev() {
    if (elem_index === 0) return;
    elem_index -= 1;
    elem_highlight = array_highlights[elem_index];
    locate_veils();
    show_veils();
    locate_tooltip();
    show_tooltip();
  }

  // Function executed when 'Next' of tooltip is clicked
  function handle_click_next() {
    clickedReview = 1;
    if (elem_index === array_highlights.length - 1) {
      hide_veils_and_tooltips();
      return;
    }
    elem_index += 1;
    elem_highlight = array_highlights[elem_index];
    locate_veils();
    show_veils();
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