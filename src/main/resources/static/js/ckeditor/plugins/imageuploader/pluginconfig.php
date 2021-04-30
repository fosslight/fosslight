<?php

// checking lang value
if(isset($_COOKIE['sy_lang'])) {
    $load_lang_code = $_COOKIE['sy_lang'];
} else {
    $load_lang_code = "en";
}

// including lang files
switch ($load_lang_code) {
    case "en":
        require(__DIR__ . '/lang/en.php');
        break;
    case "pl":
        require(__DIR__ . '/lang/pl.php');
        break;
}

if(isset($_POST["newpath"]) or isset($_POST["extension"]) or isset($_GET["file_style"])){
    session_start();
}

if(isset($_SESSION['username'])){

    if(isset($_POST["newpath"])){
        $newpath = filter_input(INPUT_POST, 'newpath', FILTER_SANITIZE_STRING);
        $root = $_SERVER['DOCUMENT_ROOT'];
        $data = '
    $useruploadfolder = "'.$newpath.'";
    $useruploadpath = $usersiteroot."$useruploadfolder/";
    $foldershistory[] = "'.$newpath.'";
        '.PHP_EOL;
        $fp = fopen(__DIR__ . '/pluginconfig.php', 'a');
        fwrite($fp, $data);
    }

    if(isset($_POST["extension"])){
        $extension_setting = filter_input(INPUT_POST, 'extension', FILTER_SANITIZE_STRING);
        if($extension_setting == "no" or $extension_setting == "yes"){
            setcookie(
                "file_extens",
                $extension_setting,
                time() + (10 * 365 * 24 * 60 * 60)
            );
        } else {
            echo '
                <script>
                alert("'.$dltimageerrors1.'\r\n\r\n'.$configerrors1.'");
                history.back();
                </script>
            ';
        }
    }
    if(isset($_GET["file_style"])){
        $file_style = filter_input(INPUT_GET, 'file_style', FILTER_SANITIZE_STRING);
        if($file_style == "block" or $file_style == "list"){
            setcookie(
                "file_style",
                $file_style,
                time() + (10 * 365 * 24 * 60 * 60)
            );
            header('Location: ' . $_SERVER['HTTP_REFERER']);
        } else {
            echo '
                <script>
                alert("'.$dltimageerrors1.'\r\n\r\n'.$configerrors2.'");
                history.back();
                </script>
            ';
        }
    }

}

// Version of the plugin
$currentpluginver = "4.1.8";

// Show/Hide the settings button
$show_settings = true;

// username and password
$username = "";
$password = "";

// ststem icons
$sy_icons = array(
    "cd-ico-browser.ico",
    "cd-icon-block.png",
    "cd-icon-browser.png",
    "cd-icon-bug.png",
    "cd-icon-close-black.png",
    "cd-icon-close-grey.png",
    "cd-icon-close.png",
    "cd-icon-coffee.png",
    "cd-icon-credits.png",
    "cd-icon-delete.png",
    "cd-icon-disable.png",
    "cd-icon-done.png",
    "cd-icon-download.png",
    "cd-icon-edit.png",
    "cd-icon-english.png",
    "cd-icon-faq.png",
    "cd-icon-german.png",
    "cd-icon-hideext.png",
    "cd-icon-image.png",
    "cd-icon-images.png",
    "cd-icon-list.png",
    "cd-icon-logout.png",
    "cd-icon-password.png",
    "cd-icon-polish.png",
    "cd-icon-qedit.png",
    "cd-icon-qtrash.png",
    "cd-icon-refresh.png",
    "cd-icon-select.png",
    "cd-icon-settings.png",
    "cd-icon-showext.png",
    "cd-icon-translate.png",
    "cd-icon-updates.png",
    "cd-icon-upload-big.png",
    "cd-icon-upload-grey.png",
    "cd-icon-upload.png",
    "cd-icon-use.png",
    "cd-icon-version.png",
    "cd-icon-warning.png",
);

// show/hide file extension
if(!isset($_COOKIE["file_extens"])){
    $file_extens = "no";
} else {
    $file_extens = $_COOKIE["file_extens"];
}

// show/hide news section
if(!isset($_COOKIE["show_news"])){
    $news_sction = "yes";
} else {
    $news_sction = "no";
}

// file_style
if(!isset($_COOKIE["file_style"])){
    $file_style = "block";
} else {
    $file_style = $_COOKIE["file_style"];
}

// Path to the upload folder, please set the path using the Image Browser Settings menu.

$foldershistory = array();
$useruploadroot = "http://$_SERVER[HTTP_HOST]";
$browserfolder = pathinfo("$_SERVER[REQUEST_URI]");
$browserfolder = ltrim($browserfolder["dirname"], '/');
$usersiteroot = substr($_SERVER["SCRIPT_FILENAME"], 0, (stripos($_SERVER["SCRIPT_FILENAME"], $_SERVER["SCRIPT_NAME"])+1));

$useruploadfolder = "$browserfolder/uploads";
$useruploadpath = $usersiteroot."$useruploadfolder/";
$foldershistory[] = $useruploadfolder;
