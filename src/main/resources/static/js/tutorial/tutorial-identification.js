$(document).ready(function () {

  // Tutorial Mode = true
  if (getCookie("tutorial") != null) {

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
  let $vailUp = $('<div id="vail_up"></div>');
  let $vailDown = $('<div id="vail_down"></div>');
  let $vailLeft = $('<div id="vail_left"></div>');
  let $vailRight = $('<div id="vail_right"></div>');

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

  $("#login_space").append($button1);

  // Put vail and tooltips in body tag
  $("body").append($vailUp);
  $("body").append($vailDown);
  $("body").append($vailLeft);
  $("body").append($vailRight);
  $("body").append($tooltip);

  // vail, tooltip default style setting
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

  // Set the part to highlight
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
  // Changing the browser window size
  // Adjust the position of the vail and tooltip
  $(window).resize(() => {
    locate_vails();
    locate_tooltip();
  });

  // Tutorial - Proper handling of previous, next, and close buttons
  $("#button_prev").on("click", handle_click_prev);
  $("#button_next").on("click", handle_click_next);
  $("#button_close_tooltip").on("click", hide_vails_and_tooltips);

  //////////////////////////////// FUNCTIONS ////////////////////////////////

  // Set the position and size of each of the 4 vails
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

  // Make all vail visible
  function show_vails() {
    for (const vail of vails) vail.css("display", "block");
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
    $tooltip.css({ display: "flex" });
  }

  // Make both vail and tooltip invisible on the screen
  function hide_vails_and_tooltips() {
    $vailUp.css("display", "none");
    $vailDown.css("display", "none");
    $vailLeft.css("display", "none");
    $vailRight.css("display", "none");
    $tooltip.css("display", "none");
  }

  // Function executed when 'back' of tooltip is clicked
  function handle_click_prev() {
    if (elem_index === 0) return;
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

  // Tutorial Mode = false
  else{
        var tutorial_button1 = (document.getElementById("continue_tutorial"));
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