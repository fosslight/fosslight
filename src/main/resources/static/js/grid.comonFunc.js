// 화면과 공통 그리드 함수 연결을 확인하기 위한 임시 commonFunc
// ajax 와 관련된 함수는 제외됨

var _mainLastsel = -1;
var _subLastsel = -1;
var _popup = null;
var _popupLicense = null;
var _popupOss = null;
var _popupBulkOssRef = null;
var _popupVuln = null;

var fn_grid_com = {
  // 공통 언포메터
  unformatter: function (cellvalue, options, rowObject) {
    return cellvalue;
  },
  // url 포메터
  displayUrl: function (cellvalue, options, rowObject) {
    var url = "";

    if (cellvalue != null) {
      var httpVal = cellvalue;
      if (
        !(
          cellvalue.toLowerCase().startsWith("http://") ||
          cellvalue.toLowerCase().startsWith("https://") ||
          cellvalue.toLowerCase().startsWith("ftp://") ||
          cellvalue.toLowerCase().startsWith("git://")
        )
      ) {
        httpVal = "http://" + cellvalue;
      }
      var _href = '<c:url value="' + httpVal + '"/>';
      url =
        '<a href="' +
        _href +
        '" class="urlLink" target="_blank">' +
        cellvalue +
        "</a>";
    }

    return url;
  },
  unDisplayUrl: function (cellvalue, options, rowObject) {
    return cellvalue;
  },
  // license 포메터
  displayLicense: function (cellvalue, options, rowObject) {
    var newVal = "";

    if (cellvalue) {
      var licenseArr = cellvalue.split(",");
      for (var i = 0; i < licenseArr.length; i++) {
        if (licenseArr[i] != "") {
          if (newVal == "") {
            newVal += licenseArr[i];
          } else {
            newVal += "," + licenseArr[i];
          }
        }
      }
    }

    return newVal;
  },
  // vulnerability 포메터
  displayVulnerability: function (cellvalue, options, rowObject) {
    var display = "";
    var _url = "";
    var prjId = "${project.prjId}";

    if (prjId) {
      _url =
        '<c:url value="/vulnerability/vulnpopup?prjId=' +
        prjId +
        "&ossName=" +
        rowObject.ossName +
        "&ossVersion=" +
        rowObject.ossVersion +
        '&vulnType="/>';
    } else {
      _url =
        '<c:url value="/vulnerability/vulnpopup?ossName=' +
        rowObject.ossName +
        "&ossVersion=" +
        rowObject.ossVersion +
        '&vulnType="/>';
    }

    if (parseInt(cellvalue) >= 9.0) {
      display =
        '<span class="iconSet vulCritical" onclick="openNVD2(\'' +
        rowObject.ossName +
        "','" +
        _url +
        "')\">" +
        cellvalue +
        "</span>";
    } else if (parseInt(cellvalue) >= 7.0) {
      display =
        '<span class="iconSet vulHigh" onclick="openNVD2(\'' +
        rowObject.ossName +
        "','" +
        _url +
        "')\">" +
        cellvalue +
        "</span>";
    } else if (parseInt(cellvalue) >= 4.0) {
      display =
        '<span class="iconSet vulMiddle" onclick="openNVD2(\'' +
        rowObject.ossName +
        "','" +
        _url +
        "')\">" +
        cellvalue +
        "</span>";
    } else if (parseInt(cellvalue) > 0) {
      display =
        '<span class="iconSet vulLow" onclick="openNVD2(\'' +
        rowObject.ossName +
        "','" +
        _url +
        "')\">" +
        cellvalue +
        "</span>";
    } else if (parseInt(cellvalue) == 0 || cellvalue == undefined) {
      display = '<span style="font-size:0;"></span>';
    } else {
      display = cellvalue;
    }

    return display;
  },
  displayBatGuiReport: function (cellvalue, options, rowObject) {
    var display = "";

    if (
      cellvalue == "Y" &&
      rowObject.ossName &&
      "-" != rowObject.ossName &&
      rowObject.batStringMatchPercentage != "Binary DB matched"
    ) {
      var url =
        '<c:url value="/download/batGuiReport/' +
        rowObject.referenceId +
        "/" +
        rowObject.batChecksum +
        '"/>';
      display =
        "<a class='iconReport' href='" +
        url +
        "'>" +
        rowObject.batChecksum +
        "</a>";
    }

    return display;
  },
  displayLicenseName: function (cellvalue, options, rowObject) {
    const display = "<a class='urlLink' href=\"javascript:;\">" + cellvalue + "</a>";
    return display;
  },
  // License Restriction 포메터
  displayLicenseRestriction: function (cellvalue, options, rowObject) {
    var display = "";

    if (cellvalue != "" && cellvalue != undefined) {
      display =
        '<span class="badge badge-warning text-xm" data-placement="top" title="' +
        cellvalue +
        '" onclick="src_fn_com.showLicenseRestrictionViewPage(\'' +
        options.gid +
        "','" +
        options.rowId +
        "')\">R</span>";
    }

    return display;
  },
  // comment 포메터
  displayComment: function (cellvalue, options, rowObject) {
    var display = "";

    if (cellvalue != "" && cellvalue != undefined) {
      display =
        '<div class="commentDiv" style="height : 29px; overflow: hidden;">' +
        cellvalue +
        "</div>";
    }

    return display;
  },
  // 메인 그리드 체크박스 포메터
  cboxFormatter: function (cellvalue, options, rowObject) {
    return (
      '<input id="' +
      options.rowId +
      '_excludeYn" type="checkbox"' +
      (cellvalue == "Y" ? ' value ="Y" checked="checked"' : ' value ="N"') +
      "onclick=\"fn_grid_com.onCboxClick('" +
      options.rowId +
      "','main')\"/>"
    );
  },
  cboxUnFormatter: function (cellvalue, options, rowObject) {
    console.log(options);
    var cboxValue = $("#" + options.rowId + "_excludeYn").val();

    return cboxValue;
  },
  // 서브 그리드 체크박스 포메터
  cboxSubFormatter: function (cellvalue, options, rowObject) {
    return (
      '<input id="' +
      options.rowId +
      '_excludeYn" type="checkbox"' +
      (cellvalue == "Y" ? ' value ="Y" checked="checked"' : ' value ="N"') +
      "onclick=\"fn_grid_com.onCboxClick('" +
      options.rowId +
      "','sub')\"/>"
    );
  },
  cboxSubUnFormatter: function (cellvalue, options, rowObject) {
    var cboxValue = $("#" + options.rowId + "_excludeYn").val();

    return cboxValue;
  },
  // 체크박스 클릭시 excludeYn 값 및 음영 처리
  onCboxClick: function (rowId, sub) {
    var id = "";
    typeof rowId == "object" ? (id = rowId.id) : (id = rowId);
    var target = "";

    if (sub == "sub") {
      target = $("#" + id + "_excludeYn")
        .closest("table")
        .attr("id");
    } else {
      target = $("#" + id + "_excludeYn")
        .parent()
        .attr("aria-describedby")
        .split("_")[0];
    }

    var _tr = $("#" + target).jqGrid("getInd", rowId, true);
    var value = "N";

    if ($("#" + id + "_excludeYn").is(":checked")) {
      $("#" + id + "_excludeYn").attr("value", "Y");
      $(_tr).addClass("excludeRow");
      value = "Y";
    } else {
      $("#" + id + "_excludeYn").attr("value", "N");
      $(_tr).removeClass("excludeRow");
    }

    fn_grid_com.saveCellData(target, rowId, "excludeYn", value);
  },

  onCboxClickAll: function (allChk, target) {
    console.log(allChk);
    console.log(target);
    if (event.stopPropagation) {
      event.stopPropagation(); //MOZILLA
    } else {
      event.cancelBubble = true; //IE
    }

    var dataArray = $("#" + target).jqGrid("getRowData");

    if ($(allChk).is(":checked")) {
      $("#" + target + " input[id*='_excludeYn']").each(function (idx) {
        $(this).attr("value", "Y");
        $(this).prop("checked", true);
        $(this).parent().parent().addClass("excludeRow");
        // fn_grid_com.saveCellData(
        //   target,
        //   dataArray[idx].gridId,
        //   "excludeYn",
        //   "Y"
        // );
      });
    } else {
      $("#" + target + " input[id*='_excludeYn']").each(function (idx) {
        $(this).attr("value", "N");
        $(this).prop("checked", false);
        $(this).parent().parent().removeClass("excludeRow");
        // fn_grid_com.saveCellData(
        //   target,
        //   dataArray[idx].gridId,
        //   "excludeYn",
        //   "N"
        // );
      });
    }
  },
  // 전체 excludeYn 에 대한  음영 처리
  checkExclude: function (data, target) {
    var arr = [];
    arr = $("#" + target).jqGrid("getDataIDs");

    for (var i in arr) {
      if ($("#" + target).jqGrid("getCell", arr[i], "excludeYn") == "Y") {
        $("#" + target).jqGrid("setRowData", arr[i], false, {
          "background-color": "#ada9a9",
        });
      }
    }
  },
  // 그리드 셀 경고 클래스 설정
  setWarningClass: function (target, rowid, colnames) {
    for (var c in colnames) {
      var idx = -1;
      var cm = target.jqGrid("getGridParam", "colModel");

      for (var i in cm) {
        if (cm[i].name == colnames[c]) {
          idx = i;

          break;
        }
      }

      var td = target[0].rows.namedItem(rowid).cells[idx];

      if (target.jqGrid("getCell", rowid, idx) == "") {
        $(td).addClass("warningStyle");
      } else {
        $(td).removeClass("warningStyle");
      }
    }
  },
  // 서브 그리드 라이센스 배경색 설정
  makeBackGroundColor: function (target) {
    var lastColor = colors[0];
    // 배경색 설정
    var arr = [];
    arr = target.jqGrid("getDataIDs");
    var comb = "";

    for (var i in arr) {
      comb = target.jqGrid("getCell", arr[i], "ossLicenseComb");

      if (comb == "AND" || comb == "") {
        target.jqGrid("setRowData", arr[i], false, {
          "background-color": lastColor,
        });
      } else if (comb == "OR") {
        var flag = false;

        for (var j = 0; j < colors.length; j++) {
          if (lastColor == colors[j]) {
            lastColor = colors[j + 1];
            target.jqGrid("setRowData", arr[i], false, {
              "background-color": lastColor,
            });

            flag = true;
          } else if (lastColor == colors[colors.length - 1]) {
            lastColor = colors[0];
            target.jqGrid("setRowData", arr[i], false, {
              "background-color": lastColor,
            });

            flag = true;
          }

          if (flag) {
            break;
          }
        }
      }
    }
  },
  // 행추가 메인 & 서브
  rowAdd: function (div, target, flag, rowid, callbackFunc) {
    if (flag == "main") {
      // 서브 그리드 url 전송 설정(false 전송 안함)
      subGridUrl = false;

      // 이전 로우 세이브
      if (_mainLastsel != -1) {
        var licenseName = callbackFunc(target.getRowData(_mainLastsel));
        target.jqGrid("setCell", _mainLastsel, "licenseName", licenseName);
        fn_grid_com.saveCellData(
          target.attr("id"),
          _mainLastsel,
          "licenseName",
          licenseName,
          null,
          null
        );
        target.jqGrid("saveRow", _mainLastsel);
      } else {
        target.jqGrid("saveRow", _mainLastsel);
      }

      // 로우 추가
      var _tempRandId = $.jgrid.randId();
      target.jqGrid(
        "addRowData",
        _tempRandId,
        {
          gridId: _tempRandId,
          licenseDiv: "S",
          excludeYn: "N",
          customBinaryYn: "Y",
        },
        "last"
      );
      // 경고 클래스 설정
      fn_grid_com.setWarningClass(target, _tempRandId, [
        "ossName",
        "licenseName",
      ]);
      // 추가된 로우 에디트 설정
      fn_grid_com.setMainEditMode(target, _tempRandId);

      // 만약 client array에도 추가한다.
      var dataArray = $("#" + div).jqGrid("getRowData", _tempRandId);
      if ("binAndroidList" == div) {
        binAndroidMainData[binAndroidMainData.length - 1] = dataArray;
      } else if ("srcList" == div) {
        srcMainData[srcMainData.length - 1] = dataArray;
      } else if ("binList" == div) {
        binMainData[binMainData.length - 1] = dataArray;
      } else if ("list" == div) {
        partyMainData[partyMainData.length - 1] = dataArray;
      } else if ("batList" == div) {
        batMainData[batMainData.length - 1] = dataArray;
      } else if ("depList" == div) {
        depMainData[depMainData.length - 1] = dataArray;
      }

      $.each(dataArray, function (_key, _val) {
        fn_grid_com.saveCellData(div, _tempRandId, _key, _val, null, null);
      });

      target.jqGrid("editRow", _tempRandId);
      target.jqGrid("setSelection", _tempRandId);

      // 서브 그리드 확장 및 숨기기
      $("#" + div + " #" + _tempRandId)
        .find("td:first")
        .removeClass("sgcollapsed")
        .find("a")
        .hide();
      $("#" + div + " #" + _tempRandId)
        .next()
        .hide();

      // 라스트 셀 설정
      _mainLastsel = _tempRandId;

      // 서브 그리드 url 전송 설정(true 전송)
      subGridUrl = true;

      $("#" + _tempRandId + "_licenseName").addClass("autoCom");
      $("#" + _tempRandId + "_licenseName").css({ width: "60px" });
    } else if (flag == "sub") {
      var mainGridId = $("#" + div).jqGrid("getCell", rowid, "gridId");
      var _tempRandId = mainGridId + "-" + $.jgrid.randId();
      target.jqGrid(
        "addRowData",
        _tempRandId,
        {
          gridId: _tempRandId,
          componentId: mainGridId,
          excludeYn: "N",
          editable: "Y",
        },
        "last"
      );
      target.jqGrid("setColProp", "licenseId", { editable: true });
      target.jqGrid("setColProp", "licenseName", { editable: true });
      target.jqGrid("setColProp", "licenseText", { editable: true });
      target.jqGrid("setColProp", "copyrightText", { editable: true });

      // client array에도 추가한다.
      var dataArray = target.jqGrid("getRowData", _tempRandId);

      if ("binAndroidList" == div) {
        binAndroidSubData[binAndroidSubData.length - 1] = dataArray;
      } else if ("srcList" == div) {
        srcSubData[srcSubData.length - 1] = dataArray;
      } else if ("binList" == div) {
        binSubData[binSubData.length - 1] = dataArray;
      } else if ("list" == div) {
        partySubData[partySubData.length - 1] = dataArray;
      } else if ("batList" == div) {
        batSubData[batSubData.length - 1] = dataArray;
      }

      $.each(dataArray, function (_key, _val) {
        fn_grid_com.saveCellData(div, _tempRandId, _key, _val, null, null);
      });

      target.jqGrid("editRow", _tempRandId);
      target.jqGrid("setSelection", _tempRandId);
    }
  },
  // 행추가 메인 & 서브 (new)
  rowAddNew: function (div, target, flag, rowid, callbackFunc) {
    if (flag == "main") {
      // 서브 그리드 url 전송 설정(false 전송 안함)
      subGridUrl = false;

      // 이전 로우 세이브
      if (_mainLastsel != -1) {
        var licenseName = callbackFunc(target.getRowData(_mainLastsel));
        target.jqGrid("setCell", _mainLastsel, "licenseName", licenseName);
        fn_grid_com.saveCellData(
          target.attr("id"),
          _mainLastsel,
          "licenseName",
          licenseName,
          null,
          null
        );
        target.jqGrid("saveRow", _mainLastsel);
      } else {
        target.jqGrid("saveRow", _mainLastsel);
      }

      // 로우 추가
      var _tempRandId = $.jgrid.randId();
      target.jqGrid(
        "addRowData",
        _tempRandId,
        {
          gridId: _tempRandId,
          licenseDiv: "S",
          excludeYn: "N",
          customBinaryYn: "Y",
        },
        "first"
      );
      // 경고 클래스 설정
      fn_grid_com.setWarningClass(target, _tempRandId, [
        "ossName",
        "licenseName",
      ]);
      // 추가된 로우 에디트 설정
      fn_grid_com.setMainEditMode(target, _tempRandId);

      // 만약 client array에도 추가한다.
      var dataArray = $("#" + div).jqGrid("getRowData", _tempRandId);
      if ("binAndroidList" == div) {
        binAndroidMainData[binAndroidMainData.length - 1] = dataArray;
      } else if ("srcList" == div) {
        srcMainData[srcMainData.length - 1] = dataArray;
      } else if ("binList" == div) {
        binMainData[binMainData.length - 1] = dataArray;
      } else if ("list" == div) {
        partyMainData[partyMainData.length - 1] = dataArray;
      } else if ("batList" == div) {
        batMainData[batMainData.length - 1] = dataArray;
      } else if ("depList" == div) {
        depMainData[depMainData.length - 1] = dataArray;
      }

      $.each(dataArray, function (_key, _val) {
        fn_grid_com.saveCellData(div, _tempRandId, _key, _val, null, null);
      });

      target.jqGrid("editRow", _tempRandId);
      target.jqGrid("setSelection", _tempRandId);

      // 서브 그리드 확장 및 숨기기
      $("#" + div + " #" + _tempRandId)
        .find("td:first")
        .removeClass("sgcollapsed")
        .find("a")
        .hide();

      // 라스트 셀 설정
      _mainLastsel = _tempRandId;

      // 서브 그리드 url 전송 설정(true 전송)
      subGridUrl = true;

      $("#" + _tempRandId + "_licenseName").addClass("autoCom");
      $("#" + _tempRandId + "_licenseName").css({ width: "60px" });

      // OSS TABLE_ADD_checkbox attr, class edit
      $("#" + _tempRandId)
        .find("input[type=checkbox]")
        .removeAttr("checked");
      $("#" + _tempRandId)
        .find("input[type=checkbox]")
        .removeClass("cbox");
    }
  },
  // 행삭제 메인 & 서브
  rowDel: function (target, flag) {
    var selrow = target.jqGrid("getGridParam", "selrow");
    // 메인 그리드 로우 삭제시 서브 그리드 동시 삭제
    if (flag == "main") {
      var targetDataObj = target.selector;

      //bat 일 경우
      if (targetDataObj == "#batList" || targetDataObj == "#binAndroidList") {
        if (target.jqGrid("getCell", selrow, "customBinaryYn") == "Y") {
          target.jqGrid("collapseSubGridRow", selrow);
          target.jqGrid("delRowData", selrow);

          if ("#binAndroidList" == targetDataObj) {
            fn_grid_com.deleteLocalDataAfterDelRow(target, selrow, flag);
          } else if ("#batList" == targetDataObj) {
            fn_grid_com.deleteLocalDataAfterDelRow(target, selrow, flag);
          }
        } else {
          alertify.error(
            '<spring:message code="msg.common.cannot.delete" />',
            0
          );
        }
      } else {
        target.jqGrid("collapseSubGridRow", selrow);
        target.jqGrid("delRowData", selrow);
        fn_grid_com.deleteLocalDataAfterDelRow(target, selrow, flag);
      }
      // 서브 그리드 로우만 삭제
    } else if (flag == "sub") {
      var editableFlag = target.jqGrid("getCell", selrow, "editable");

      if (editableFlag == "Y") {
        target.jqGrid("delRowData", selrow);
        fn_grid_com.deleteLocalDataAfterDelRow(target, selrow, flag);
      } else {
        alertify.error(
          '<spring:message code="msg.common.cannot.registered.delete" />',
          0
        );
      }
    }
  },
  // 행삭제 메인 & 서브 (new)
  rowDelNew: function (target, flag) {
    $("#loading_wrap").show();

    setTimeout(function () {
      try {
        var selarrrow = target.jqGrid("getGridParam", "selarrrow");

        if (flag == "main") {
          var targetDataObj = target.selector;
          onAjaxLoadingHide = false;

          var dataArray = target.jqGrid("getGridParam", "data");

          for (var i = selarrrow.length - 1; i >= 0; i--) {
            var selrow = selarrrow[i];
            dataArray = fn_grid_com.deleteLocalDataAfterDelRowData(
              dataArray,
              selrow
            );
          }
          //bat 일 경우
          if (
            targetDataObj == "#batList" ||
            targetDataObj == "#binAndroidList"
          ) {
            if (target.jqGrid("getCell", selrow, "customBinaryYn") == "Y") {
              for (var i = selarrrow.length - 1; i >= 0; i--) {
                var selrow = selarrrow[i];
                target.jqGrid("collapseSubGridRow", selrow);
                target.jqGrid("delRowData", selrow);
              }

              if ("#binAndroidList" == targetDataObj) {
                fn_grid_com.deleteLocalDataAfterDelRowNew(
                  target,
                  dataArray,
                  flag
                );
              } else if ("#batList" == targetDataObj) {
                fn_grid_com.deleteLocalDataAfterDelRowNew(
                  target,
                  dataArray,
                  flag
                );
              }
            } else {
              alertify.error(
                '<spring:message code="msg.common.cannot.delete" />',
                0
              );
            }
          } else {
            for (var i = selarrrow.length - 1; i >= 0; i--) {
              var selrow = selarrrow[i];
              target.jqGrid("collapseSubGridRow", selrow);
              target.jqGrid("delRowData", selrow);
            }

            fn_grid_com.deleteLocalDataAfterDelRowNew(target, dataArray, flag);
          }
        }
      } catch (e) {
        alertify.error('<spring:message code="msg.common.cannot.delete" />', 0);
      } finally {
        $("#loading_wrap").hide();
      }
    }, 300);
  },
  deleteLocalDataAfterDelRowData: function (dataArray, selrow) {
    var reMakeArrObj = [];
    var newIdx = 0;

    for (var idx = 0; idx < dataArray.length; idx++) {
      if (dataArray[idx].gridId != selrow) {
        reMakeArrObj[newIdx++] = dataArray[idx];
      }
    }

    return reMakeArrObj;
  },
  deleteLocalDataAfterDelRowNew: function (target, dataArray, flag) {
    // client array에서 삭제
    var targetDataObj = target.selector;
    var reMakeArrObj = dataArray;

    if (flag == "main") {
      target.jqGrid("GridUnload");

      if ("#binAndroidList" == targetDataObj) {
        binAndroidMainData = reMakeArrObj;
        binAndroid_grid.load();
        // total record 표시
        $("#binAndroidList_toppager_right, #binAndroidPager_right").html(
          '<div dir="ltr" style="text-align:right" class="ui-paging-info">Total : ' +
            binAndroidMainData.length +
            "</div>"
        );
      } else if ("#srcList" == targetDataObj) {
        srcMainData = reMakeArrObj;
        src_grid.load();
        // total record 표시
        $("#srcList_toppager_right, #srcPager_right").html(
          '<div dir="ltr" style="text-align:right" class="ui-paging-info">Total : ' +
            srcMainData.length +
            "</div>"
        );
      } else if ("#binList" == targetDataObj) {
        binMainData = reMakeArrObj;
        bin_grid.load();
        // total record 표시
        $("#binList_toppager_right, #binPager_right").html(
          '<div dir="ltr" style="text-align:right" class="ui-paging-info">Total : ' +
            binMainData.length +
            "</div>"
        );
      } else if ("#list" == targetDataObj) {
        partyMainData = reMakeArrObj;
        grid.init();
        // total record 표시
        $("#list_toppager_right, #pager_right").html(
          '<div dir="ltr" style="text-align:right" class="ui-paging-info">Total : ' +
            partyMainData.length +
            "</div>"
        );
      } else if ("#batList" == targetDataObj) {
        batMainData = reMakeArrObj;
        bat_grid_list.load();
        // total record 표시
        $("#batList_toppager_right, #batPager_right").html(
          '<div dir="ltr" style="text-align:right" class="ui-paging-info">Total : ' +
            batMainData.length +
            "</div>"
        );
      } else if ("#depList" == targetDataObj) {
        depMainData = reMakeArrObj;
        dep_grid.load();
        // total record 표시
        $("#depList_toppager_right, #depPager_right").html(
          '<div dir="ltr" style="text-align:right" class="ui-paging-info">Total : ' +
            depMainData.length +
            "</div>"
        );
      }
    }
  },
  deleteLocalDataAfterDelRow: function (target, selrow, flag) {
    // client array에서 삭제
    var targetDataObj = target.selector;
    var dataArray = $("" + targetDataObj).jqGrid("getGridParam", "data");

    var reMakeArrObj = [];
    var newIdx = 0;

    for (var idx = 0; idx < dataArray.length; ++idx) {
      if (dataArray[idx].gridId != selrow) {
        reMakeArrObj[newIdx++] = dataArray[idx];
      }
    }

    if (flag == "main") {
      target.jqGrid("GridUnload");

      if ("#binAndroidList" == targetDataObj) {
        binAndroidMainData = reMakeArrObj;
        binAndroid_grid.load();
      } else if ("#srcList" == targetDataObj) {
        srcMainData = reMakeArrObj;
        src_grid.load();
      } else if ("#binList" == targetDataObj) {
        binMainData = reMakeArrObj;
        bin_grid.load();
      } else if ("#list" == targetDataObj) {
        partyMainData = reMakeArrObj;
        grid.init();
      } else if ("#batList" == targetDataObj) {
        batMainData = reMakeArrObj;
        bat_grid_list.load();
      } else if ("#depList" == targetDataObj) {
        depMainData = reMakeArrObj;
        dep_grid.load();
      }
    } else {
      // sub
      var targetSubDataObj = targetDataObj.split("_")[0];

      if ("#binAndroidList" == targetSubDataObj) {
        binAndroidSubData[selrow.split("-")[0]] = reMakeArrObj;
        target
          .jqGrid("setGridParam", { data: binAndroidSubData })
          .trigger("reloadGrid");
      } else if ("#srcList" == targetSubDataObj) {
        srcSubData[selrow.split("-")[0]] = reMakeArrObj;
        target
          .jqGrid("setGridParam", { data: srcSubData })
          .trigger("reloadGrid");
      } else if ("#binList" == targetSubDataObj) {
        binSubData[selrow.split("-")[0]] = reMakeArrObj;
        target
          .jqGrid("setGridParam", { data: binSubData })
          .trigger("reloadGrid");
      } else if ("#list" == targetSubDataObj) {
        partySubData[selrow.split("-")[0]] = reMakeArrObj;
        target
          .jqGrid("setGridParam", { data: partySubData })
          .trigger("reloadGrid");
      } else if ("#batList" == targetSubDataObj) {
        batSubData[selrow.split("-")[0]] = reMakeArrObj;
        target
          .jqGrid("setGridParam", { data: batSubData })
          .trigger("reloadGrid");
      } else if ("#depList" == targetSubDataObj) {
        depSubData[selrow.split("-")[0]] = reMakeArrObj;
        target
          .jqGrid("setGridParam", { data: depSubData })
          .trigger("reloadGrid");
      }
    }
  },
  // 멀티라이센스 펼치기 및 싱글 라이센스 서브 그리드 숨기기
  multyExpand: function (target) {
    var arr = [];
    arr = $("#" + target).jqGrid("getDataIDs");

    for (var i in arr) {
      $("#" + target).jqGrid("expandSubGridRow", arr[i]);
      if ($("#" + target).jqGrid("getCell", arr[i], "licenseDiv") != "M") {
        $("#" + target + " #" + arr[i])
          .find("td:first")
          .removeClass("sgcollapsed")
          .find("a")
          .hide();
        $("#" + target + " #" + arr[i])
          .next()
          .hide();
      }
    }
  },
  // 메인 그리드 excludeYn 에디트 모드
  setCellEdit: function (
    target,
    rowid,
    msgData,
    diffMsgData,
    infoMsgData,
    callbackFunc
  ) {
    if (rowid) {
      if (rowid != _mainLastsel) {
        if (_mainLastsel != -1) {
          var licenseName = callbackFunc(target.getRowData(_mainLastsel));
          target.jqGrid("setCell", _mainLastsel, "licenseName", licenseName);
          fn_grid_com.saveCellData(
            target.attr("id"),
            _mainLastsel,
            "licenseName",
            licenseName,
            null,
            null
          );
          target.jqGrid("saveRow", _mainLastsel);
        } else {
          target.jqGrid("saveRow", _mainLastsel);
        }

        gridValidMsgRowId(
          msgData,
          target.selector.replace("#", ""),
          _mainLastsel
        );

        if (diffMsgData) {
          gridDiffMsgRowId(
            diffMsgData,
            target.selector.replace("#", ""),
            _mainLastsel
          );
        }

        if (infoMsgData) {
          gridInfoMsgRowId(
            infoMsgData,
            target.selector.replace("#", ""),
            _mainLastsel
          );
        }
      }

      fn_grid_com.setMainEditMode(target, rowid);
      target.jqGrid("editRow", rowid);
      _mainLastsel = rowid;
    }
  },
  // 메인 그리드 에디트 모드 설정
  setMainEditMode: function (target, rowid) {
    target.jqGrid("setColProp", "gridId", { editable: true });
    target.jqGrid("setColProp", "licenseId", { editable: true });
    target.jqGrid("setColProp", "licenseName", { editable: true });
    target.jqGrid("setColProp", "licenseText", { editable: true });
    target.jqGrid("setColProp", "copyrightText", { editable: true });
    target.jqGrid("setColProp", "filePath", { editable: true });
    target.jqGrid("setColProp", "ossName", { editable: true });
    target.jqGrid("setColProp", "ossVersion", { editable: true });
    target.jqGrid("setColProp", "downloadLocation", { editable: true });
    target.jqGrid("setColProp", "homepage", { editable: true });

    var customBinary = target.jqGrid("getCell", rowid, "customBinaryYn");

    if (customBinary == "Y") {
      target.jqGrid("setColProp", "binaryName", { editable: true });
    } else {
      target.jqGrid("setColProp", "binaryName", { editable: false });
    }
  },

  // [MC요청] 2. Project List – OSS List에서 Binary DB 검색 결과 제공
  showBinaryViewPage: function (
    target,
    rowid,
    restoreYn,
    _validMsgData,
    _diffMsgData,
    _infoMsgData
  ) {
    var _targetId = target.selector.replace("#", "");
    cleanErrMsg(_targetId, rowid);
    target.jqGrid("saveRow", rowid);
    var path = target.jqGrid("getCell", rowid, "binaryName").split("/");
    var filename = path[path.length - 1];

    if (!restoreYn || restoreYn == undefined) target.jqGrid("editRow", rowid);

    if (_validMsgData) {
      gridValidMsgRowId(_validMsgData, _targetId, rowid);
    }
    if (_diffMsgData) {
      gridDiffMsgRowId(_diffMsgData, _targetId, rowid);
    }
    if (_infoMsgData) {
      gridInfoMsgRowId(_infoMsgData, _targetId, rowid);
    }

    if (filename != "") {
      onAjaxLoadingHide = true;
      $.ajax({
        url: '<c:url value="/system/bat/existBinaryName"/>',
        type: "GET",
        dataType: "json",
        cache: false,
        async: false,
        data: { filename: filename },
        contentType: "application/json",
        success: function (data) {
          if (data.isValid) {
            var _encUrl =
              "filename=" + fn_grid_com.replaceGetParamChar(filename);

            if (_popup == null || _popup.closed) {
              _popup = window.open(
                "<c:url value='/system/bat/binarypopup?" + _encUrl + "'/>",
                "binaryViewPopup_" + filename,
                "width=1450, height=650, toolbar=no, location=no, left=100, top=100"
              );
              if (
                !_popup ||
                _popup.closed ||
                typeof _popup.closed == "undefined"
              ) {
                alertify.alert(
                  '<spring:message code="msg.common.window.allowpopup" />',
                  function () {}
                );
              }
            } else {
              _popup.close();
              _popup = window.open(
                "<c:url value='/system/bat/binarypopup?" + _encUrl + "'/>",
                "binaryViewPopup_" + filename,
                "width=1450, height=650, toolbar=no, location=no, left=100, top=100"
              );
            }
          }
        },
        error: function () {
          alertify.error('<spring:message code="msg.common.valid2" />', 0);
        },
      });
    }
  },
};
