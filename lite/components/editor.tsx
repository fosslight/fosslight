import { useEffect, useRef, useState } from 'react';

export default function Editor({
  value,
  setValue
}: {
  value: string;
  setValue: (value: string) => void;
}) {
  const [editorLoaded, setEditorLoaded] = useState(false);
  const editorRef = useRef<any>();
  const { CKEditor, ClassicEditor } = editorRef.current || {};

  useEffect(() => {
    editorRef.current = {
      CKEditor: require('@ckeditor/ckeditor5-react').CKEditor,
      ClassicEditor: require('@ckeditor/ckeditor5-build-classic')
    };
    setEditorLoaded(true);
    // eslint-disable-next-line react-hooks/exhaustive-deps
  }, []);

  return (
    editorLoaded && (
      <CKEditor
        editor={ClassicEditor}
        data={value}
        onChange={(_: any, editor: any) => setValue(editor.getData())}
      />
    )
  );
}
