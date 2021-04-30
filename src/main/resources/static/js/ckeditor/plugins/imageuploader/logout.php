<?php
session_start();
if(!isset($_SESSION['username'])) {
    exit;
}

session_destroy();
header("Location: imgbrowser.php");
