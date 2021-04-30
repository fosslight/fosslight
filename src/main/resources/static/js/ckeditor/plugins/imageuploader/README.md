# Image Uploader and Browser for CKEditor
[Image Uploader and Browser for CKEditor](http://imageuploaderforckeditor.altervista.org/) is a plugin that allows you to **upload images** easily to your server and add automatically to CKEditor. Since **version 2.0** you can **browse and manage** your uploaded files online right in your browser - without using a FTP Client. The Image Browser is **responsive** and looks great on every device width.

## Download
You can download the Image Uploader and Browser for CKEditor [here](http://ckeditor.com/addon/imageuploader).

## Features
* Functionality: Upload, delete, download and view your PNG, JPG & GIF files.
* Secure: Only you can access the image browser by a password protection since version 4.0.
* Flexible: Do you already have your own upload folder? You can easily switch and create folders in the image browser.
* Modern UI: The Image Browser is responsive and looks great on every device width.
* Support: Regular updates and an always up to date documentation make it easy for you to install and use the browser.

## Installation and Configuration
First extract the downloaded file into the CKEditor’s *plugins* folder. Then enable the plugin by changing or adding the extraPlugins line in your configuration (config.js):

### Defining Configuration In-Page
```
CKEDITOR.replace( 'editor1', {
  extraPlugins: 'imageuploader'
});
```

### Using the config.js File
```
CKEDITOR.editorConfig = function( config ) {
  config.extraPlugins = 'imageuploader';
};
```

Don't forget to set `CHMOD writable permission (0777)` to the **imageuploader** folder on your server.

Please read the [Plugin FAQ](http://imageuploaderforckeditor.altervista.org/support/) for more information.

## How to use

### Browse and manage files
Open the **Image info** tab and click **Browse server**. A new window will open where you see all your uploaded images. Open the preview of a picture by tapping on the image. To use the file click **Use**. To upload a new image open the upload panel in the image browser.

### Change the upload path
Open the **Image info** tab and click Browse server. A new window will open where you see all your uploaded images. Open the **Settings** to choose another upload path.

### Further questions?
Please read the [Plugin FAQ](http://imageuploaderforckeditor.altervista.org/support/).

## Demo
The demo can be found [here](http://imageuploaderforckeditor.altervista.org/demo.php).

## Share
[Tweet](http://twitter.com/share?url=http://imageuploaderforckeditor.altervista.org&text=Use%20the%20Image%20Uploader%20for%20CKEditor%20for%20free%20now!%20&hashtags=imageuploaderforckeditor) or [Share on Facebook](http://www.facebook.com/sharer.php?u=http://imageuploaderforckeditor.altervista.org).

## Support
The support site can be found [here](http://ibm.bplaced.com/contact/index.php?cdproject=Image%20Uploader%20and%20Browser%20for%20CKEditor). Before submitting a support ticket please read the [FAQ](http://imageuploaderforckeditor.altervista.org/support/).

## License
Image Uploader and Browser for CKEditor is licensed under the MIT license:
[http://en.wikipedia.org/wiki/MIT_License](http://en.wikipedia.org/wiki/MIT_License)

Copyright © 2015 by Moritz Maleck
