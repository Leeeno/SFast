package com.cbx.sfast.actions;

import java.io.File;

import org.eclipse.jface.action.IAction;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.IWorkbenchWindowActionDelegate;

import com.cbx.sfast.preferences.PreferenceConstants;
import com.cbx.sfast.utilities.AntUtil;
import com.cbx.sfast.utilities.CbxUtil;

/**
 * Our sample action implements workbench action delegate. The action proxy will be created by the workbench and shown
 * in the UI. When the user tries to use the action, this delegate will be created and execution will be delegated to
 * it.
 *
 * @see IWorkbenchWindowActionDelegate
 */
public class RunBizAction implements IWorkbenchWindowActionDelegate {

    private IWorkbenchWindow window;

    /**
     * The constructor.
     */
    public RunBizAction() {
    }

    /**
     * The action has been activated. The argument of the method represents the 'real' action sitting in the workbench
     * UI.
     *
     * @see IWorkbenchWindowActionDelegate#run
     */
    @Override
    public void run(final IAction action) {
        try {
            if (CbxUtil.PATH_BUSINESS_PROJECT == null) {
                MessageDialog.openInformation(window.getShell(), "Error", "未找到biz项目");
                return;
            }
            final boolean rewriteJetty = CbxUtil.store.getBoolean(PreferenceConstants.P_REWRITE_JETTY_CMD);

            if (rewriteJetty) {
                final String jettyScript = CbxUtil.store.getString(PreferenceConstants.P_JETTY_SCRIPT);
                final File file = new File(CbxUtil.PATH_BUSINESS_PROJECT, "jetty-debug.cmd");
                CbxUtil.writeFile(file, jettyScript);
            }
            final WorkThread work = new WorkThread();
            work.start();
        } catch (final Exception e) {
            CbxUtil.errln(CbxUtil.getLineInfo() + e.getMessage());
        }
    }

    class WorkThread extends Thread {

        @Override
        public void run() {
            synchronized (new Object()) {

                try {
                    CbxUtil.logln("线程" + Thread.currentThread().getName() + "开始运行");

                    if (!AntUtil.antCore(window)) {
                        CbxUtil.errln(CbxUtil.getLineInfo() + "ant core failed");
                        return;
                    }

                    if (!AntUtil.antUI(window)) {
                        CbxUtil.errln(CbxUtil.getLineInfo() + "ant ui failed");
                        return;
                    }

                    if (!AntUtil.antGeneral(window)) {
                        CbxUtil.errln(CbxUtil.getLineInfo() + "ant general failed");
                        return;
                    }

                    CbxUtil.runBiz();
                    CbxUtil.logln("线程" + Thread.currentThread().getName() + "运行完毕");
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
