package com.cbx.sfast.actions;

import org.eclipse.jface.action.IAction;
import org.eclipse.jface.preference.PreferenceDialog;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.swt.widgets.Display;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.IWorkbenchWindowActionDelegate;
import org.eclipse.ui.dialogs.PreferencesUtil;

/**
 * Our sample action implements workbench action delegate. The action proxy will be created by the workbench and shown
 * in the UI. When the user tries to use the action, this delegate will be created and execution will be delegated to
 * it.
 *
 * @see IWorkbenchWindowActionDelegate
 */
public class OpenPreferenceAction implements IWorkbenchWindowActionDelegate {

    @SuppressWarnings("unused")
    private IWorkbenchWindow window;

    /**
     * The constructor.
     */
    public OpenPreferenceAction() {
    }

    /**
     * The action has been activated. The argument of the method represents the 'real' action sitting in the workbench
     * UI.
     *
     * @see IWorkbenchWindowActionDelegate#run
     */
    @Override
    public void run(final IAction action) {
        final PreferenceDialog dialog = PreferencesUtil.createPreferenceDialogOn(Display.getCurrent().getActiveShell(),
                "com.cbx.sfast.preferences.SFastPreferencePage",
                new String[] {"com.cbx.sfast.preferences.SFastPreferencePage"}, null);
        dialog.open();
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
