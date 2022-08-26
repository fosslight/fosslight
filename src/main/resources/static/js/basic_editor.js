/* exported initSample */

if ( CKEDITOR.env.ie && CKEDITOR.env.version < 9 )
	CKEDITOR.tools.enableHtml5Elements( document );

// The trick to keep the editor in the sample quite small
// unless user specified own height.
CKEDITOR.config.height = 150;
CKEDITOR.config.width = 'auto';

var initSample = ( function() {
	var wysiwygareaAvailable = isWysiwygareaAvailable(),
		isBBCodeBuiltIn = !!CKEDITOR.plugins.get( 'bbcode' );

	return function() {
		var editorElement = CKEDITOR.document.getById( 'editor' );

		// :(((
		if ( isBBCodeBuiltIn ) {
			editorElement.setHtml();
		}

		// Depending on the wysiwygare plugin availability initialize classic or inline editor.
		if ( wysiwygareaAvailable ) {
			CKEDITOR.replace( 'editor' );
		} else {
			editorElement.setAttribute( 'contenteditable', 'true' );
			CKEDITOR.inline( 'editor' );

			// TODO we can consider displaying some info box that
			// without wysiwygarea the classic editor may not work.
		}
		var linkText = replaceWithLink(CKEDITOR.instances.editor.getData());
		CKEDITOR.instances.editor.setData(linkText);
	};

	function isWysiwygareaAvailable() {
		// If in development mode, then the wysiwygarea must be available.
		// Split REV into two strings so builder does not replace it :D.
		if ( CKEDITOR.revision == ( '%RE' + 'V%' ) ) {
			return true;
		}

		return !!CKEDITOR.plugins.get( 'wysiwygarea' );
	}
} )();

var initSample2 = ( function() {
	var wysiwygareaAvailable = isWysiwygareaAvailable(),
		isBBCodeBuiltIn = !!CKEDITOR.plugins.get( 'bbcode' );

	return function() {
		var editorElement = CKEDITOR.document.getById( 'editor2' );

		// :(((
		if ( isBBCodeBuiltIn ) {
			editorElement.setHtml();
		}

		// Depending on the wysiwygare plugin availability initialize classic or inline editor.
		if ( wysiwygareaAvailable ) {
			CKEDITOR.replace( 'editor2' );
		} else {
			editorElement.setAttribute( 'contenteditable', 'true' );
			CKEDITOR.inline( 'editor2' );

			// TODO we can consider displaying some info box that
			// without wysiwygarea the classic editor may not work.
		}
		var linkText = replaceWithLink(CKEDITOR.instances.editor2.getData());
		CKEDITOR.instances.editor2.setData(linkText);
	};

	function isWysiwygareaAvailable() {
		// If in development mode, then the wysiwygarea must be available.
		// Split REV into two strings so builder does not replace it :D.
		if ( CKEDITOR.revision == ( '%RE' + 'V%' ) ) {
			return true;
		}

		return !!CKEDITOR.plugins.get( 'wysiwygarea' );
	}
} )();

var initSample3 = ( function() {
	var wysiwygareaAvailable = isWysiwygareaAvailable(),
		isBBCodeBuiltIn = !!CKEDITOR.plugins.get( 'bbcode' );

	return function() {
		var editorElement = CKEDITOR.document.getById( 'editor3' );

		// :(((
		if ( isBBCodeBuiltIn ) {
			editorElement.setHtml();
		}

		// Depending on the wysiwygare plugin availability initialize classic or inline editor.
		if ( wysiwygareaAvailable ) {
			CKEDITOR.replace( 'editor3' );
		} else {
			editorElement.setAttribute( 'contenteditable', 'true' );
			CKEDITOR.inline( 'editor3' );

			// TODO we can consider displaying some info box that
			// without wysiwygarea the classic editor may not work.
		}
		var linkText = replaceWithLink(CKEDITOR.instances.editor3.getData());
		CKEDITOR.instances.editor3.setData(linkText);
	};

	function isWysiwygareaAvailable() {
		// If in development mode, then the wysiwygarea must be available.
		// Split REV into two strings so builder does not replace it :D.
		if ( CKEDITOR.revision == ( '%RE' + 'V%' ) ) {
			return true;
		}

		return !!CKEDITOR.plugins.get( 'wysiwygarea' );
	}
} )();

function resetEditor(instance) {
	instance.updateElement();
	instance.setData('');
}

var initCKEditorNoToolbar = function (id, readOnly) {
	if(CKEDITOR.instances[id]) {
		var _editor = CKEDITOR.instances[id];
		_editor.destroy();
	}
	CKEDITOR.replace(id, {customConfig:"/js/customEditorConf_Comment.js", readOnly : readOnly});
	var linkText = replaceWithLink(CKEDITOR.instances[id].getData());
	CKEDITOR.instances[id].setData(linkText);
	return linkText;
}

var initCKEditorToolbar = function (id) {
	if(CKEDITOR.instances[id]) {
		var _editor = CKEDITOR.instances.editor;
		_editor.destroy();
	}
	CKEDITOR.replace(id);
	var linkText = replaceWithLink(CKEDITOR.instances[id].getData());
	CKEDITOR.instances[id].setData(linkText);
	return linkText;
}

var activeLink = function(){
	CKEDITOR.on('instanceReady', function (ev) {
		$('iframe').contents().unbind("click").bind("click",function(e){
			var url = e.target.href;
			if (url !== undefined && url !== "") {
				window.open(url);
			}
		});
	});
}