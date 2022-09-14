$(document).ready(function () {
  // add tutorial code

  // Create elements to highlight
  let $button1 = $(document.getElementById("proj_list"));

  // Array of elements to highlight
  let array_highlights = [$button1];

  // Array of titles and content to be displayed by the tooltip
  let array_tooltip_data = [
    { title: "Project list 이동", content: "Project list 로 이동합니다." },
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
    </div>
  </div>`
  );

  $("#proj_list").prepend($button1);

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

  //Make all veils visible
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

    //Make all veils visible
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

      $tooltip.css({
        position: "absolute",
        top: `${offset.top + elem_highlight.outerHeight(true) + 10}px`,
        left: `${tooltip_left}px`,
        width: `${tooltip_width}px`,
      });
    }

    // Make the tooltip visible on the screen
    function show_tooltip() {
      $tooltip.css({display: "flex"});
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
});