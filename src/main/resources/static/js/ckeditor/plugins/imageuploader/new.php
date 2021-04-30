<!doctype html>
<html>
<head>
<meta charset="utf-8">
<meta name="viewport" content="width=device-width, initial-scale=1.0, user-scalable=no">
<title>Image Browser for CKEditor :: Fujana Solutions</title>

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
    .disable {
        color: #39424D;
        font-family: 'Open Sans', sans-serif;
        font-size: 15px;
        font-weight: 500;
        line-height: 17px;
        margin: 0px 0px 30px;
        cursor: pointer;
        text-decoration: underline;
    }
    .disable:hover {
        color: #1862A8;
    }
    .description {
        color: #39424D;
        font-family: 'Open Sans', sans-serif;
        font-size: 13px;
        font-weight: 300;
        line-height: 17px;
        margin: 10px 0px -10px;
        text-align: left;
    }
</style>
</head>
<body>
<div class="container">
	<div class="login">
    <h1>Welcome!</h1>
    <h2>Please create a new (local) account to prevent others to view and manage your images.</h2>
    <h3 class="disable" onclick="window.open('http://imageuploaderforckeditor.altervista.org/disable_pw.html','about:blank', 'toolbar=no, scrollbars=yes, resizable=no, width=900, height=600');">How can I disable the password protection? (external link)</h3>
	<form name="form2" action="create.php" method="post">
    <p class="nameOfInput">Username</p>
    <input type="text" name="username" class="login_form">
    <p class="nameOfInput">Password</p>
    <input type="password" name="password">
    <div style="text-align:right;">
    <input class="login_btn" type="submit" value="Create account">
    </div>
    </form>
    </div>
    <br />
    <div class="hrNews"></div>
    <p class="description">
        The default upload folder is <b>ckeditor/plugins/imageuploader/uploads</b>. You can change it in the <b>settings</b> panel.
    </p>
    <div class="hrNews hrNews2"></div>
</div>
</body>
</html>
