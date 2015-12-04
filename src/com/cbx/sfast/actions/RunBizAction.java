package com.cbx.sfast.actions;

import java.io.File;

import org.eclipse.jface.action.IAction;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.IWorkbenchWindowActionDelegate;

import sfast.Activator;

import com.cbx.sfast.preferences.PreferenceConstants;
import com.cbx.sfast.utilities.CbxUtil;

/**
 * Our sample action implements workbench action delegate. The action proxy will
 * be created by the workbench and shown in the UI. When the user tries to use
 * the action, this delegate will be created and execution will be delegated to
 * it.
 *
 * @see IWorkbenchWindowActionDelegate
 */
public class RunBizAction implements IWorkbenchWindowActionDelegate {

	private static String bizpath;

	private IWorkbenchWindow window;

	IPreferenceStore store = Activator.getDefault().getPreferenceStore();

	/**
	 * The constructor.
	 */
	public RunBizAction() {
	}

	/**
	 * The action has been activated. The argument of the method represents the
	 * 'real' action sitting in the workbench UI.
	 *
	 * @see IWorkbenchWindowActionDelegate#run
	 */
	public void run(IAction action) {
		try {
			bizpath = CbxUtil.bizpath;
			if (bizpath == null) {
				MessageDialog.openInformation(window.getShell(), "Error",
						"未找到biz项目");
				return;
			}
			boolean rewriteJetty = store
					.getBoolean(PreferenceConstants.P_REWRITE_JETTY_CMD);

			if (rewriteJetty) {
				final String jettyScript = store
						.getString(PreferenceConstants.P_JETTY_SCRIPT);
				final File file = new File(bizpath, "jetty-debug.cmd");
				CbxUtil.WriteFile(file, jettyScript);
			}
			WorkThread work = new WorkThread();
			work.start();
		} catch (Exception e) {
			CbxUtil.log("RunBizAction Line 65\t" + "err:---"
					+ e.getMessage());
		}
	}

	class WorkThread extends Thread {
		@Override
		public void run() {
			synchronized (new Object()) {

				try {
					CbxUtil.log("RunBizAction Line 76\t" + "线程"
							+ Thread.currentThread().getName() + "开始运行");
					// Thread.currentThread().sleep(100);

					if (store.getBoolean(PreferenceConstants.P_ALWAYS_ANT_CORE)) {
						CbxUtil.log("RunBizAction Line 81\t"
								+ "ant core");
						if (!CbxUtil.antCore(window)) {
							CbxUtil.log("RunBizAction Line 84\t"
									+ "ant core failure");
							return;
						}
					}

					if (store.getBoolean(PreferenceConstants.P_ALWAYS_ANT_UI)) {
						CbxUtil.out
								.println("RunBizAction Line 92\t" + "ant ui");
						if (!CbxUtil.antUI(window)) {
							CbxUtil.log("RunBizAction Line 94\t"
									+ "ant ui failure");
							return;
						}
					}

					if (store
							.getBoolean(PreferenceConstants.P_ALWAYS_ANT_GENERAL)) {
						CbxUtil.log("RunBizAction Line 102\t"
								+ "ant general");
						if (!CbxUtil.antGeneral(window)) {
							CbxUtil.log("RunBizAction Line 105\t"
									+ "ant general failure");
							return;
						}
					}
					CbxUtil.runBiz(bizpath);
					CbxUtil.log("RunBizAction Line 111\t" + "线程"
							+ Thread.currentThread().getName() + "运行完毕");
				} catch (Exception e) {
					CbxUtil.log("RunBizAction Line 114\t" + "err:---"
							+ e.getMessage());
				}

			}
		}
	}

	/**
	 * Selection in the workbench has been changed. We can change the state of
	 * the 'real' action here if we want, but this can only happen after the
	 * delegate has been created.
	 *
	 * @see IWorkbenchWindowActionDelegate#selectionChanged
	 */
	public void selectionChanged(IAction action, ISelection selection) {
	}

	/**
	 * We can use this method to dispose of any system resources we previously
	 * allocated.
	 *
	 * @see IWorkbenchWindowActionDelegate#dispose
	 */
	public void dispose() {
	}

	/**
	 * We will cache window object in order to be able to provide parent shell
	 * for the message dialog.
	 *
	 * @see IWorkbenchWindowActionDelegate#init
	 */
	public void init(IWorkbenchWindow window) {
		this.window = window;
	}

}