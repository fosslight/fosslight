import { useEffect, useRef, useState } from 'react';
import { UseFormReturn } from 'react-hook-form';

export default function Editor({ form, name }: { form: UseFormReturn<any>; name: string }) {
  const [editorLoaded, setEditorLoaded] = useState(false);
  const editorRef = useRef<any>();
  const { CKEditor, ClassicEditor } = editorRef.current || {};

  useEffect(() => {
    editorRef.current = {
      CKEditor: require('@ckeditor/ckeditor5-react').CKEditor,
      ClassicEditor: require('@ckeditor/ckeditor5-build-classic')
    };
    setEditorLoaded(true);

    form.register(name);
    // eslint-disable-next-line react-hooks/exhaustive-deps
  }, []);

  return (
    editorLoaded && (
      <CKEditor
        editor={ClassicEditor}
        data={form.watch(name)}
        onChange={(_: any, editor: any) => form.setValue(name, editor.getData())}
      />
    )
  );
}
