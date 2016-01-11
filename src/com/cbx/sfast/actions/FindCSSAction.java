package com.cbx.sfast.actions;

import org.eclipse.jface.action.IAction;
import org.eclipse.jface.text.TextSelection;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.IWorkbenchWindowActionDelegate;
import org.eclipse.ui.texteditor.ITextEditor;

import com.cbx.sfast.utilities.CSSUtil;
import com.cbx.sfast.utilities.CbxUtil;

/**
 * Our sample action implements workbench action delegate. The action proxy will be created by the workbench and shown
 * in the UI. When the user tries to use the action, this delegate will be created and execution will be delegated to
 * it.
 *
 * @see IWorkbenchWindowActionDelegate
 */
public class FindCSSAction implements IWorkbenchWindowActionDelegate {

    private IWorkbenchWindow window;
    public static String selectionText = null;

    /**
     * The constructor.
     */
    public FindCSSAction() {
    }

    /**
     * The action has been activated. The argument of the method represents the 'real' action sitting in the workbench
     * UI.
     *
     * @see IWorkbenchWindowActionDelegate#run
     */
    @Override
    public void run(final IAction action) {
        selectionText = getCurrentSelection();
        final WorkThread work = new WorkThread();
        work.start();
    }

    class WorkThread extends Thread {
        @Override
        public void run() {
            synchronized (new Object()) {

                try {
                    CbxUtil.logln("线程" + Thread.currentThread().getName() + "开始运行");

                    CSSUtil.FindCSS(selectionText, window.getActivePage());

                    CbxUtil.logln("线程" + Thread.currentThread().getName() + "结束运行");
                } catch (final Exception e) {
                    CbxUtil.errln(CbxUtil.getLineInfo() + e.getMessage());
                }

            }
        }
    }

    /**
     * Selection in the workbench has been changed. We can change the state of the 'real' action here if we want, but
     * this can only happen after the delegate has been created.
     *
     * @see IWorkbenchWindowActionDelegate#selectionChanged
     */
    @Override
    public void selectionChanged(final IAction action, final ISelection selection) {
    }

    public String getCurrentSelection() {
        try {
            final IEditorPart part = window.getActivePage().getActiveEditor();
            if (part instanceof ITextEditor) {
                final ITextEditor editor = (ITextEditor) part;
                // final IDocumentProvider prov = editor.getDocumentProvider();
                // final IDocument doc = prov.getDocument(editor.getEditorInput());
                final ISelection sel = editor.getSelectionProvider().getSelection();
                if (sel instanceof TextSelection) {
                    final TextSelection textSel = (TextSelection) sel;
                    return textSel.getText();
                }
            }
        } catch (final Exception ex) {
            CbxUtil.errln(CbxUtil.getLineInfo() + ex.getMessage());
        }
        return null;
    }

    /**
     * We can use this method to dispose of any system resources we previously allocated.
     *
     * @see IWorkbenchWindowActionDelegate#dispose
     */
    @Override
    public void dispose() {
    }

    /**
     * We will cache window object in order to be able to provide parent shell for the message dialog.
     *
     * @see IWorkbenchWindowActionDelegate#init
     */
    @Override
    public void init(final IWorkbenchWindow window) {
        this.window = window;
    }

}
