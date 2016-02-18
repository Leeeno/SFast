package com.cbx.sfast.utilities;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.util.HashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.lang.StringUtils;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IFolder;
import org.eclipse.core.resources.IProject;
import org.eclipse.jface.text.BadLocationException;
import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.IRegion;
import org.eclipse.swt.widgets.Display;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.ide.IDE;
import org.eclipse.ui.texteditor.ITextEditor;

public class CSSUtil {

    public static final String CSS_PATH = "src/main/webapp/css/";

    private static final String BIZ_PROJECT_NAME = "CBX_Business";
    private static final String PATTERN_CSS = ".%s[\\s\\{:,]+";
    private static final String PRINT_FORMAT = "%s\t(%s:%s)";

    public static void FindCSS(final String classname, final IWorkbenchPage page) throws PartInitException {
        if (StringUtils.isBlank(classname)) {
            return;
        }
        CbxUtil.clearConsole();
        final IProject project = CbxUtil.getProjects(BIZ_PROJECT_NAME);
        final IFolder folder = project.getFolder(CSS_PATH);

        final File rootp = new File(CbxUtil.PATH_BUSINESS_PROJECT + CSS_PATH);
        final File[] files = rootp.listFiles();
        final Map<String, Object[]> cssMap = new HashMap<String, Object[]>();
        if (files != null) {
            for (final File fi : files) {
                if (fi.isDirectory()) {
                    // CbxUtil.log("Skip directory----------------" + fi.getAbsolutePath());
                } else {
                    try {
                        final FileReader fr = new FileReader(fi);
                        @SuppressWarnings("resource")
                        final BufferedReader br = new BufferedReader(fr);
                        String str = null;
                        int line = 0;
                        while ((str = br.readLine()) != null) {
                            line++;
                            final Pattern pattern = Pattern.compile(String.format(PATTERN_CSS, classname));
                            final Matcher matcher = pattern.matcher(str);

                            if (matcher.find()) {
                                CbxUtil.logln(String.format(PRINT_FORMAT, str, fi.getName(), line));
                                cssMap.put(str, new Object[] {fi.getName(), line});
                            }
                        }
                    } catch (final Exception e) {
                        CbxUtil.errln(CbxUtil.getLineInfo() + e.getMessage());
                    }
                }
            }
            String shortestCSS = null;
            for (final String css : cssMap.keySet()) {
                if (shortestCSS == null || css.length() < shortestCSS.length()) {
                    shortestCSS = css;
                }
            }
            if (shortestCSS != null) {
                // final IFileStore fileStore = EFS.getLocalFileSystem().getStore(cssMap.get(shortestCSS).toURI());
                final IFile file = folder.getFile((String) cssMap.get(shortestCSS)[0]);
                final int line = (int) cssMap.get(shortestCSS)[1];
                Display.getDefault().asyncExec(new Runnable() {
                    @Override
                    public void run() {
                        try {
                            final IEditorPart editor = IDE.openEditor(page, file, true);
                            goToLine(editor, line);
                        } catch (final PartInitException e) {
                            CbxUtil.errln(CbxUtil.getLineInfo() + e.getMessage());
                        }
                    }
                });
            }
        }
    }

    public static void goToLine(final IEditorPart editorPart, final int lineNumber) {
        if (!(editorPart instanceof ITextEditor) || lineNumber <= 0) {
            return;
        }
        final ITextEditor editor = (ITextEditor) editorPart;
        final IDocument document = editor.getDocumentProvider().getDocument(editor.getEditorInput());
        if (document != null) {
            IRegion lineInfo = null;
            try {
                lineInfo = document.getLineInformation(lineNumber - 1);
            } catch (final BadLocationException e) {
                CbxUtil.errln(CbxUtil.getLineInfo() + e.getMessage());
            }
            if (lineInfo != null) {
                editor.selectAndReveal(lineInfo.getOffset(), lineInfo.getLength());
            }
        }
    }
}
