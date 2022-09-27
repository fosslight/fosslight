$(document).ready(function () {

  // Tutorial Mode = true
  if (getCookie("tutorial") != null) {

  // Create elements to highlight
  let $button1 = $(document.getElementById("btn_project_add"));
  let $button2 = $(document.getElementById("check_create"));
  let $button3 = $(document.getElementById("identification_start"));

  // Array of elements to highlight
  let array_highlights = [$button1, $button2, $button3];

  // Array of titles and content to be displayed by the tooltip
  let array_tooltip_data = [
    {
      title: "Add Button",
      content: "Add 버튼을 클릭합니다.",
    },
    {
      title: "New project",
      content: "add 후 새 프로젝트가 생성된 것을 확인합니다.",
    },
    {
      title: "Identification",
      content: "Identification Column 내 Start 버튼을 클릭합니다",
    },
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

  $("#next_add").append($button1);
  $("#check_create").append($button2);
  $("#identification_start").append($button3);

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

  $("#continue_tutorial").on("click", () => {
    elem_index = 0;
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
    for (const veil of veils) veil.css("display", "block");
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
      case 0:
        $tooltip.css({
          position: "absolute",
          top: `${offset.top + elem_highlight.outerHeight(true) + 10}px`,
          left: `${tooltip_left - 65}px`,
          width: `${tooltip_width}px`,
        });
        break;
      case 1:
        $tooltip.css({
          position: "absolute",
          top: `${offset.top + elem_highlight.outerHeight(true) - 439}px`,
          left: `${tooltip_left - 34.5}px`,
          width: `${tooltip_width}px`,
        });
        break;
      case 2:
        $tooltip.css({
          position: "absolute",
          top: `${offset.top + elem_highlight.outerHeight(true) - 40.227}px`,
          left: `${tooltip_left - 119.25}px`,
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
  // Tutorial Mode = false
  else{
        var tutorial_button1 = (document.getElementById("continue_tutorial"));
        var tutorial_button2 = (document.getElementById("continue_tutorial_25"));
        tutorial_button1.style.display = 'none';
        tutorial_button2.style.display = 'none';
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