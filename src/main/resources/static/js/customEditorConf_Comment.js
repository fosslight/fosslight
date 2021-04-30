CKEDITOR.editorConfig = function(config) {
	// Define changes to default configuration here.
	// For complete reference see:
	// http://docs.ckeditor.com/#!/api/CKEDITOR.config

	// The toolbar groups arrangement, optimized for two toolbar rows.
	config.toolbarGroups = [
		//{ name: 'clipboard',   groups: [ 'clipboard', 'undo' ] },
		//{ name: 'editing',     groups: [ 'find', 'selection', 'spellchecker' ] },
		//{ name: 'links' }
		//{ name: 'insert' } // 이게 없으면 이미지가 표시되지 않음
		//{ name: 'forms' },
		//{ name: 'tools' },
		//{ name: 'document',	   groups: [ 'mode', 'document', 'doctools' ] },
		//{ name: 'others' },
		//'/',
		//{ name: 'basicstyles', groups: [ 'basicstyles', 'cleanup' ] } 
		//{ name: 'paragraph',   groups: [ 'list', 'indent', 'blocks', 'align', 'bidi' ] },
		//{ name: 'styles' },
		//{ name: 'colors' },
		//{ name: 'about' }
	];

	//config.toolbarGroups = [];
	// Remove some buttons provided by the standard plugins, which are
	// not needed in the Standard(s) toolbar.
	config.removeButtons = 'Underline,Subscript,Superscript';

	// Set the most common block elements.
	//config.format_tags = 'p;h1;h2;h3;pre';

	// Simplify the dialog windows.
	config.removeDialogTabs = 'image:advanced;link:advanced';
	
	config.uploadUrl  = '/imageupload/upload2';
	config.filebrowserUploadUrl = '/imageupload/upload';
	config.image_previewText = 'In publishing and graphic design, lorem ipsum is placeholder text (filler text) commonly used to demonstrate the graphic elements of a document or visual presentation';
	config.pasteFromWordRemoveFontStyles = false; 
	config.pasteFromWordRemoveStyles = false;
	config.allowedContent = true;
	// config.js end
    // config.height = 'auto';
    config.width = '100%';
    config.extraPlugins = 'autogrow';
    config.autoGrow_minHeight = 100;
    config.autoGrow_maxHeight = 450;
    config.autoGrow_bottomSpace = 50;
    config.readOnly = true;
 };