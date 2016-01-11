package com.cbx.sfast.actions;

import org.eclipse.jface.action.IAction;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.IWorkbenchWindowActionDelegate;

import com.cbx.sfast.utilities.AntUtil;
import com.cbx.sfast.utilities.CbxUtil;

/**
 * Our sample action implements workbench action delegate. The action proxy will be created by the workbench and shown
 * in the UI. When the user tries to use the action, this delegate will be created and execution will be delegated to
 * it.
 *
 * @see IWorkbenchWindowActionDelegate
 */
public class AntCoreAction implements IWorkbenchWindowActionDelegate {

    private IWorkbenchWindow window;

    /**
     * The constructor.
     */
    public AntCoreAction() {
    }

    /**
     * The action has been activated. The argument of the method represents the 'real' action sitting in the workbench
     * UI.
     *
     * @see IWorkbenchWindowActionDelegate#run
     */
    @Override
    public void run(final IAction action) {
        final WorkThread work = new WorkThread();
        work.start();
    }

    class WorkThread extends Thread {
        @Override
        public void run() {
            synchronized (new Object()) {

                try {
                    CbxUtil.logln("线程" + Thread.currentThread().getName() + "开始运行");
                    AntUtil.antCore(window);

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
