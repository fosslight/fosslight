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
?>
<!doctype html>
<html>
<head>
<meta charset="utf-8">
<meta name="viewport" content="width=device-width, initial-scale=1.0, user-scalable=no">
<title><?php echo $imagebrowser1; ?> :: Fujana Solutions</title>

<!-- Jquery -->
<link rel="stylesheet" href="http://code.jquery.com/mobile/1.4.5/jquery.mobile-1.4.5.min.css" />
<script src="http://code.jquery.com/jquery-1.11.1.min.js"></script>

<script>
</script>

<style>
	body {
		background-color:#F18273;
		max-width:400px;
		margin:auto;
		padding:12px;
		margin-top:6%;
		margin-bottom:6%;
		font-family:Verdana, Geneva, sans-serif;
	}

	.container {
		background-color:#FFF;
		padding:30px;
		border-radius:0px;
		-webkit-box-shadow: 0px 0px 5px 0px rgba(0,0,0,0.75);
		-moz-box-shadow: 0px 0px 5px 0px rgba(0,0,0,0.75);
		box-shadow: 0px 0px 5px 0px rgba(0,0,0,0.75);
		text-align:center;
	}

	.logo {
		width:70%;
	}

	input {
		border-radius:0px;
		width:100%;
		border:solid;
		border-width:thin;
		border-color:#CCC;
		padding:4px;
		margin-bottom:10px;
		font-size:24px;
		font-weight:lighter;
		transition: all .25s ease-in-out;
		-moz-transition: all .25s ease-in-out;
		-webkit-transition: all .25s ease-in-out;

		-webkit-box-sizing: border-box;
		   -moz-box-sizing: border-box;
				box-sizing: border-box;

		-webkit-appearance: none;
		-webkit-border-radius:0px;


	}

	input:focus {
		box-shadow: 0 0 3px #64C6E7;
		opacity:1;
	}

	.nameOfInput {
		font-size:14px;
		margin-bottom:1px;
		text-align:left;
		font-weight:bold;
		color:#666;
	}

	.login_btn {
		margin-top:14px;
		background-color:#F9F9F9;
		border-radius:2px;
		color:#666;
		font-weight:normal;
		font-size:16px;
		width:auto;
		padding:5px;
		border:none !important;
		cursor:pointer;
	}

	.hrNews {
		border-top:solid;
		border-top-width:thin;
		border-top-color:#CCC;
		margin:auto;
		margin-top:7px;
	}

	.hrNews2 {
		margin-top:20px;
	}
    h1 {
        color: #1862A8;
        font-family: 'Open Sans', sans-serif;
        font-size: 34px;
        font-weight: 300;
        line-height: 40px;
        margin: 0 0 16px;
    }

    h2 {
        color: #39424D;
        font-family: 'Open Sans', sans-serif;
        font-size: 20px;
        font-weight: 300;
        line-height: 32px;
        margin: -10px 0px 14px;
    }
</style>
</head>
<body>
<div class="container">
	<div class="login">
    <h1><?php echo $loginsite1; ?></h1>
    <h2><?php echo $loginsite2; ?></h2>
	<form name="form2" action="login.php" method="post">
    <p class="nameOfInput"><?php echo $loginsite3; ?></p>
    <input type="text" name="username" class="login_form">
    <p class="nameOfInput"><?php echo $loginsite4; ?></p>
    <input type="password" name="password">
    <div style="text-align:right;">
    <input class="login_btn" type="submit" value="<?php echo $loginsite5; ?>">
    </div>
    </form>
    </div>
    <br />
    <div class="hrNews"></div>
    <p style="text-align:left; font-size:13px; font-family:Verdana, Geneva, sans-serif;">
    <a href="https://twitter.com/fujanasolutions" style="font-weight:bolder; color:#1862A8; text-decoration:none; font-family:Georgia, 'Times New Roman', Times, serif; font-size:14px;">2015 Image Uploader for CKEditor</a><br />
    <a href="http://imageuploaderforckeditor.altervista.org/" style="text-decoration:underline; color:#484947;">Documentation</a>  <a href="http://imageuploaderforckeditor.altervista.org/support/" style="text-decoration:underline; color:#484947;">FAQ</a></p>
    <div class="hrNews hrNews2"></div>
</div>
</body>
</html>
