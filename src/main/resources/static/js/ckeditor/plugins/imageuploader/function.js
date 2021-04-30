// Copyright (c) 2015, Fujana Solutions - Moritz Maleck. All rights reserved.
// For licensing, see LICENSE.md

// Open image modal
function showImage(imgSrc, imgStyle) {
    $("#imageFSimg").attr('src', imgSrc);
    $("#imageFSimg").attr('style', 'max-width:' + imgStyle + 'px');

    $("#imageFullSreen").show();
    $("#background").slideDown(250, "swing");

    $("#imgActionUse").attr("onclick","useImage('" + imgSrc + "')");
    $("#imgActionDownload").attr("href", imgSrc);

}

// Open editbar
function showEditBar(imgSrc, imgStyle, imgID, imgName) {
    var imgSrc = imgSrc;
    var imgStyle = imgStyle;
    var imgID = imgID;
    var imgName = imgName;

    $("#editbar").slideUp(100);
    $("#editbar").slideDown(100);

    $(".fileDiv,.fullWidthFileDiv").removeClass( "selected" );
    $("div[data-imgid='" + imgID +"']").addClass( "selected" );

    $("#updates").css("visibility", "hidden");
    $("#updates").slideUp(150);

    $("#editbarDelete").attr("onclick","deleteImg('" + imgName + "', '" + imgID + "');");
    $("#editbarUse").attr("onclick","useImage('" + imgSrc + "')");
    $("#editbarView").attr("onclick","showImage('" + imgSrc + "','" + imgStyle + "')");
    $("#editbarDownload").attr("href", imgSrc);
}

// hide editbar if user clicks outside of element
$(document).mouseup(function (e) {
    var container = $(".fileDiv,.fullWidthFileDiv,#editbar");

    if (!container.is(e.target) // if the target of the click isn't the container...
        && container.has(e.target).length === 0) // ... nor a descendant of the container
    {
        hideEditBar();
    }
});

// hide editbar function
function hideEditBar() {
    $("#editbar").slideUp(100);

    $(".fileDiv,.fullWidthFileDiv").removeClass( "selected" );

    if (currentpluginver != pluginversion) {
        $("#updates").slideDown(150);
        $("#updates").css("visibility", "visible");
    };
}

// Use image and overgive image src to ckeditor
function useImage(imgSrc) {
    function getUrlParam( paramName ) {
        var reParam = new RegExp( '(?:[\?&]|&)' + paramName + '=([^&]+)', 'i' ) ;
        var match = window.location.search.match(reParam) ;

        return ( match && match.length > 1 ) ? match[ 1 ] : null ;
    }
    var funcNum = getUrlParam( 'CKEditorFuncNum' );
    var imgSrc = imgSrc;
    var fileUrl = imgSrc;
    window.opener.CKEDITOR.tools.callFunction( funcNum, fileUrl );
    window.close();
}

// open upload image modal
function uploadImg() {

    $("#uploadImgDiv").show();
    $("#background2").slideDown(250, "swing");

}

// open settings modal
function pluginSettings() {

    $("#settingsDiv").show();
    $("#background3").slideDown(250, "swing");

}

// check if new version is available
$( document ).ready(function() {
    if (currentpluginver != pluginversion) {
        $("#updates").show();
        $('#updates').html("A new version of "+ pluginname +" ("+ pluginversion +") is available. <a target=\"_blank\" href=\""+ plugindwonload +"\">Download it now!</a>");
    };
});

// call jquery lazy load
$(function() {
    $("img.lazy").lazyload();
});

$( document ).ready(function() {
    var elem = '#uploadpathEditable';
    var text = '.saveUploadPathP';
    var btn = '.saveUploadPathA';
    var btnCancel = '#pathCancel';
    $( elem ).attr('contenteditable','true');
    $( elem ).click(function() {
        $( this ).addClass("editableActive");
        $( btn ).fadeIn();
        $( text ).show();
        $( '.pathHistory' ).fadeIn();
    });
    $( btnCancel ).click(function() {
        $( elem ).removeClass('editableActive');
        $( btn ).hide();
        $( text ).hide();
        $( '.pathHistory' ).hide();
    });
});

function updateImagePath(){
    var name = $("#uploadpathEditable").text();
    $.ajax({
      method: "POST",
      url: "pluginconfig.php",
      data: { newpath: name, }
    }).done(function( msg ) {
        $('#settingsDiv').hide();
        $('#background3').slideUp(250, 'swing');

        setTimeout(function(){
            location.reload();
        }, 250);
    });
}

function useHistoryPath(path){
    var path = path;
    $.ajax({
      method: "POST",
      url: "pluginconfig.php",
      data: { newpath: path, }
    }).done(function( msg ) {
        $('#settingsDiv').hide();
        $('#background3').slideUp(250, 'swing');

        setTimeout(function(){
            location.reload();
        }, 250);
    });
}

// open pluginconfig.php to change the extension settings
function extensionSettings(setting){
    var setting = setting;
    $.ajax({
      method: "POST",
      url: "pluginconfig.php",
      data: {
          extension: setting,
      }
    }).done(function( msg ) {
        $('#settingsDiv').hide();
        $('#background3').slideUp(250, 'swing');

        setTimeout(function(){
            location.reload();
        }, 250);
    });
}

// check if a file to upload is selected
function checkUpload(){
    if( document.getElementById("upload").files.length == 0 ){
        alert("Please select a file to upload.");
        return false;
    }
}

// toggle the edit icons
function toggleQEditIcons(){
    $( '.fullWidthlastChild, .qEditIconsDiv' ).toggle();
}

// toggle the edit mode
function toogleQEditMode(){
    if($('#qEditBtnOpen').is(':visible')){
        Cookies.set('qEditMode', 'yes');
    } else {
        Cookies.remove('qEditMode');
    }
    toggleQEditIcons();
    $( '#qEditBtnDone, #qEditBtnOpen' ).slideToggle();
}

// check if qEditMode is activated
$( document ).ready(function() {
    if(Cookies.get('qEditMode') == "yes"){
        toogleQEditMode();
    }
});

// drag n' drop
function drop(e) {
    e.preventDefault();

    var file = e.dataTransfer.files[0];

    if(file && file.type.match("image/*")) {

        var formdata = new FormData();
        formdata.append('upload', file);

        var xhr = new XMLHttpRequest();
        xhr.open('POST', 'imgupload.php');
        xhr.send(formdata);

        xhr.onreadystatechange = function() {
            if (xhr.readyState == 4 && xhr.status == 200) {
                hideEditBar();
                reloadImages();
                setTimeout(function(){
                    $("#dropzone").slideUp(450, "swing");
                }, 50);
            }
        }
    }
}

// toggle dropzone
function toggleDropzone(s) {
    var elem = $("#dropzone");
    if(s == "show") {
        elem.show();
    } else {
        elem.hide();
    }
}

// hide dropzone if user clicks close
$( document ).ready(function() {
    $( "#dropzone" ).click(function() {
        setTimeout(function(){
            toggleDropzone('hide');
        }, 500);
    });
});

// delete image
function deleteImg(src, imgid) {
    $.ajax({
       method: "GET",
        url: "imgdelete.php",
        data: {
            img: src,
        }
    }).done(function() {
        var imgDiv = $( "div[data-imgid='" + imgid +"']" );
        if(Cookies.get('file_style') == "block"){
            imgDiv.addClass("deleteAnimationBlock");
        } else {
            imgDiv.slideUp(250, "swing");
        }
        setTimeout(function(){
            imgDiv.hide();
            hideEditBar();
            reloadImages();
        }, 320);
    });
}


// reload images
function reloadImages() {
    $.ajaxSetup({cache:false});
    $('#files').load('function.php?f=loadImages', function(response, status, xhr) {
         $("img.lazy").lazyload();
     });
}

// select language
function selectLang(lang) {
    Cookies.set('sy_lang', lang, { expires: 1095 });

    $('#setLangDiv').hide();
    $('#background4').slideUp(250, 'swing');

    setTimeout(function(){
        location.reload();
    }, 250);
}

// open lang panel
function openLangPanel() {
    $('#settingsDiv').hide();
    $('#background3').slideUp(250, 'swing');

    setTimeout(function(){
        $("#setLangDiv").show();
        $("#background4").slideDown(250, "swing");
    }, 350);
}

// enable news
function enableNews() {
    Cookies.remove('show_news');

    $('#settingsDiv').hide();
    $('#background3').slideUp(250, 'swing');

    setTimeout(function(){
        location.reload();
    }, 250);
}

// disable news
function disableNews() {
    Cookies.set('show_news', 'no', { expires: 7 });

    $('#settingsDiv').hide();
    $('#background3').slideUp(250, 'swing');

    setTimeout(function(){
        location.reload();
    }, 250);
}

function logOut(){
    $.ajax({
      method: "POST",
      url: "logout.php",
    }).done(function( msg ) {
        $('#settingsDiv').hide();
        $('#background3').slideUp(250, 'swing');

        setTimeout(function(){
            location.reload();
        }, 300);
    });
}

// keyboard shortcuts
$(document).keyup(function (e){
    // left arrow and top arrow
    if (e.keyCode == 37 || e.keyCode == 38) {
        var imgID = $(".selected").data("imgid");
        if(typeof imgID === 'undefined'){
            var imgID = 2;
        };
        var next = --imgID;
        $( "div[data-imgid='" + next +"']" ).trigger( "click" );
    }
    // right arrow and bottom arrow
    if (e.keyCode == 39 || e.keyCode == 40) {
        var imgID = $(".selected").data("imgid");
        if(typeof imgID === 'undefined'){
            var imgID = 0;
        };
        var next = ++imgID;
        $( "div[data-imgid='" + next +"']" ).trigger( "click" );
    }
    // space
    if (e.keyCode == 32) {
        if($('#imageFullSreen').is(':visible')){
            $( "#imageFullSreenClose" ).trigger( "click" );
        } else {
            var imgID = $(".selected").data("imgid");
            if(typeof imgID !== 'undefined'){
                $( "#editbarView" ).trigger( "click" );
            }
        }
    }
})
