<?php
$filenames = array(
    "imgbrowser.php",
    "imgdelete.php",
    "imgupload.php",
    "pluginconfig.php",
    "uploads/",
);
if ($username == "" and $password == "") {
    $filenames[] = "create.php";
}
foreach($filenames as $filename){
    if (!is_writable($filename)){
        $check_permission = false;
    } else {
        $check_permission = true;
    }
}
if(!$check_permission):
?>
    <!DOCTYPE html>
    <html lang="en">
    <head>

        <meta charset="utf-8">
        <meta name="viewport" content="width=device-width, initial-scale=1.0, user-scalable=no">
        <title><?php echo $imagebrowser1; ?> :: Fujana Solutions</title>
        <meta name="author" content="Moritz Maleck">
        <link rel="icon" href="img/cd-ico-browser.ico">
        <link rel="stylesheet" href="styles.css">
        <script src="https://ajax.googleapis.com/ajax/libs/jquery/1.11.3/jquery.min.js"></script>
        <script src="http://www.maleck.org/imageuploader/plugininfo.js"></script>
        <script src="dist/jquery.lazyload.min.js"></script>
        <script src="function.js"></script>
        <style>
            #folderError a {
                color: #55ACEE;
            }
        </style>
    </head>
    <body ontouchstart="">

    <div id="header">
        <a class="headerA" href="http://imageuploaderforckeditor.altervista.org/" target="_blank"><?php echo $imagebrowser1; ?></a><br>
    </div>

    <div id="folderError">
        <b><?php echo $alerts1; ?></b><br><br>
        <?php echo $alerts2; ?> <a href="http://ow.ly/RE7wC" target="_blank"><?php echo $alerts3; ?></a><br><br>
        <?php echo $alerts4; ?>
    </div>

    </body>
    </html>
<?php
exit;
endif;
?>
