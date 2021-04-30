<?php
session_start();
header('content-type: text/html; charset=utf-8');

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

require(__DIR__ . "/pluginconfig.php");

$tmpusername = filter_input(INPUT_POST, 'username', FILTER_SANITIZE_STRING);
$tmppassword = md5(filter_input(INPUT_POST, 'password', FILTER_SANITIZE_STRING));

if($tmpusername == $username and $password == $tmppassword) {
    $_SESSION['username'] = $tmpusername;
    header("Location: imgbrowser.php");
} else {
    echo '
        <script>
        alert("'.$loginerrors1.'");
        history.back();
        </script>
    ';
}
