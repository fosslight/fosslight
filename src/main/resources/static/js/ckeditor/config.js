/**
 * @license Copyright (c) 2003-2017, CKSource - Frederico Knabben. All rights reserved.
 * For licensing, see LICENSE.md or http://ckeditor.com/license
 */

CKEDITOR.editorConfig = function( config ) {
	// Define changes to default configuration here.
	// For complete reference see:
	// http://docs.ckeditor.com/#!/api/CKEDITOR.config

	// The toolbar groups arrangement, optimized for two toolbar rows.
	config.toolbarGroups = [
		{ name: 'clipboard',   groups: [ 'clipboard', 'undo' ] },
		{ name: 'editing',     groups: [ 'find', 'selection', 'spellchecker' ] },
		{ name: 'links' },
		{ name: 'insert' },
		{ name: 'forms' },
		{ name: 'tools' },
		{ name: 'document',	   groups: [ 'mode', 'document', 'doctools' ] },
		{ name: 'others' },
		'/',
		{ name: 'basicstyles', groups: [ 'basicstyles', 'cleanup' ] },
		{ name: 'paragraph',   groups: [ 'list', 'indent', 'blocks', 'align', 'bidi' ] },
		{ name: 'styles' },
		{ name: 'colors' }
		//{ name: 'about' }
	];
	
	config.extraPlugins = 'autolink';
	/*
	config.extraPlugins = 'link';
	config.extraPlugins = 'dialog';
	config.extraPlugins = 'dialogui';
	config.extraPlugins = 'fakeobjects';
	*/
	// preview를 추가하면 autolink가 동작하지 않은 
	// TODO ckeditor 최신 버전으로 재구성 해야함
	//config.extraPlugins = 'preview';

	// Remove some buttons provided by the standard plugins, which are
	// not needed in the Standard(s) toolbar.
	config.removeButtons = 'Underline,Subscript,Superscript';

	// Set the most common block elements.
	config.format_tags = 'p;h1;h2;h3;pre';

	// Simplify the dialog windows.
	config.removeDialogTabs = 'image:advanced;link:advanced';
	
	config.uploadUrl  = '/imageupload/upload2';
	config.filebrowserUploadUrl = '/imageupload/upload';
	config.image_previewText = 'In publishing and graphic design, lorem ipsum is placeholder text (filler text) commonly used to demonstrate the graphic elements of a document or visual presentation';
	config.pasteFromWordRemoveFontStyles = false; 
	config.pasteFromWordRemoveStyles = false;
};
