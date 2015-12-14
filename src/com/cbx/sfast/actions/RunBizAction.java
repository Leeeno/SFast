package com.cbx.sfast.actions;

import java.io.File;

import org.eclipse.jface.action.IAction;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.IWorkbenchWindowActionDelegate;

import com.cbx.sfast.preferences.PreferenceConstants;
import com.cbx.sfast.utilities.CbxUtil;
import com.cbx.sfast.utilities.GitUtil;

/**
 * Our sample action implements workbench action delegate. The action proxy will
 * be created by the workbench and shown in the UI. When the user tries to use
 * the action, this delegate will be created and execution will be delegated to
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
	 * The action has been activated. The argument of the method represents the
	 * 'real' action sitting in the workbench UI.
	 *
	 * @see IWorkbenchWindowActionDelegate#run
	 */
	public void run(IAction action) {
		try {
			if (CbxUtil.bizpath == null) {
				MessageDialog.openInformation(window.getShell(), "Error",
						"未找到biz项目");
				return;
			}
			boolean rewriteJetty = CbxUtil.store
					.getBoolean(PreferenceConstants.P_REWRITE_JETTY_CMD);

			if (rewriteJetty) {
				final String jettyScript = CbxUtil.store
						.getString(PreferenceConstants.P_JETTY_SCRIPT);
				final File file = new File(CbxUtil.bizpath, "jetty-debug.cmd");
				CbxUtil.WriteFile(file, jettyScript);
			}
			WorkThread work = new WorkThread();
			work.start();
		} catch (Exception e) {
			CbxUtil.err(e.getMessage());
		}
	}

	class WorkThread extends Thread {
		@Override
		public void run() {
			synchronized (new Object()) {

				try {
					CbxUtil.log("线程" + Thread.currentThread().getName()
							+ "开始运行");
					boolean antCore = false;
					boolean antUI = false;
					boolean antGeneral = false;

					if (CbxUtil.store
							.getBoolean(PreferenceConstants.P_SMART_BUILD)) {
						String coreChanged = GitUtil
								.GetChangedString(CbxUtil.corepath);
						if (!CbxUtil.store.getString(
								PreferenceConstants.P_CORE_CHANGED).equals(
								coreChanged)) {
							antCore = true;
							CbxUtil.store.setValue(
									PreferenceConstants.P_CORE_CHANGED,
									coreChanged);
						}
					}
					if (CbxUtil.store
							.getBoolean(PreferenceConstants.P_ALWAYS_ANT_CORE)) {
						antCore = true;
					}
					if (antCore) {
						CbxUtil.log("ant core");
						if (!CbxUtil.antCore(window)) {
							CbxUtil.err("RunBizAction Line 84\t"
									+ "ant core failure");
							return;
						}
					}

					if (CbxUtil.store
							.getBoolean(PreferenceConstants.P_SMART_BUILD)) {
						String uiChanged = GitUtil
								.GetChangedString(CbxUtil.uipath);
						if (!CbxUtil.store.getString(
								PreferenceConstants.P_UI_CHANGED).equals(
								uiChanged)) {
							antUI = true;
							CbxUtil.store
									.setValue(PreferenceConstants.P_UI_CHANGED,
											uiChanged);
						}
					}
					if (CbxUtil.store
							.getBoolean(PreferenceConstants.P_ALWAYS_ANT_UI)) {
						antUI = true;
					}
					if (antUI) {
						CbxUtil.log("ant ui");
						if (!CbxUtil.antUI(window)) {
							CbxUtil.err("RunBizAction Line 94\t"
									+ "ant ui failure");
							return;
						}
					}

					if (CbxUtil.store
							.getBoolean(PreferenceConstants.P_SMART_BUILD)) {
						String generalChanged = GitUtil
								.GetChangedString(CbxUtil.generalpath);
						if (!CbxUtil.store.getString(
								PreferenceConstants.P_GENERAL_CHANGED).equals(
								generalChanged)) {
							antGeneral = true;
							CbxUtil.store.setValue(
									PreferenceConstants.P_GENERAL_CHANGED,
									generalChanged);
						}
					}
					if (CbxUtil.store
							.getBoolean(PreferenceConstants.P_ALWAYS_ANT_GENERAL)) {
						antGeneral = true;
					}

					if (antGeneral) {
						CbxUtil.log("ant general");
						if (!CbxUtil.antGeneral(window)) {
							CbxUtil.err("RunBizAction Line 105\t"
									+ "ant general failure");
							return;
						}
					}

					CbxUtil.runBiz(CbxUtil.bizpath);
					CbxUtil.log("线程" + Thread.currentThread().getName()
							+ "运行完毕");
				} catch (Exception e) {
					CbxUtil.err("RunBizAction Line 114\t" + e.getMessage());
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