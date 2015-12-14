package com.cbx.sfast.actions;

import java.io.BufferedInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;

import org.eclipse.jface.action.IAction;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.IWorkbenchWindowActionDelegate;

import com.cbx.sfast.utilities.CbxUtil;

/**
 * Our sample action implements workbench action delegate. The action proxy will
 * be created by the workbench and shown in the UI. When the user tries to use
 * the action, this delegate will be created and execution will be delegated to
 * it.
 *
 * @see IWorkbenchWindowActionDelegate
 */
public class PrintLogToConsoleAction implements IWorkbenchWindowActionDelegate {

	@SuppressWarnings("unused")
	private IWorkbenchWindow window;

	/**
	 * The constructor.
	 */
	public PrintLogToConsoleAction() {
	}

	/**
	 * The action has been activated. The argument of the method represents the
	 * 'real' action sitting in the workbench UI.
	 *
	 * @see IWorkbenchWindowActionDelegate#run
	 */
	public void run(IAction action) {
		WorkThread work = new WorkThread();
		work.start();
	}

	class WorkThread extends Thread {
		@Override
		public void run() {
			synchronized (new Object()) {

				try {
					CbxUtil.log("线程" + Thread.currentThread().getName()
							+ "开始运行");
					String logPath = CbxUtil.bizpath + "logs/error/error.log";
					File f = new File(logPath);
					String result = null;
					BufferedInputStream is = null;
					try {
						is = new BufferedInputStream(new FileInputStream(f));
						long contentLength = f.length();
						ByteArrayOutputStream outstream = new ByteArrayOutputStream(
								contentLength > 0 ? (int) contentLength : 1024);
						byte[] buffer = new byte[4096];
						int len;
						while ((len = is.read(buffer)) > 0) {
							outstream.write(buffer, 0, len);
						}
						outstream.close();
						result = outstream.toString();
					} finally {
						if (is != null) {
							try {
								is.close();
							} catch (Exception e) {
							}
						}
					}

					CbxUtil.log("线程" + Thread.currentThread().getName()
							+ "结束运行");
					CbxUtil.log(result);
				} catch (Exception e) {
					CbxUtil.err("PrintLogToConsoleAction Line 52\t"
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