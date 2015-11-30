package com.cbx.sfast.actions;

import java.io.File;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.jface.action.IAction;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.IWorkbenchWindowActionDelegate;

import com.cbx.sfast.utilities.CbxCommand;

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
			IProject[] projects = getProjects();
			for (IProject project : projects) {
				if ("CBX_Business".equals(project.getName())) {
					bizpath = project.getLocationURI().getPath() + "/";
				}
			}
			if (bizpath == null) {
				return;
			}
			final String debugString = "@echo off\r\nsetlocal\r\n"
					+ "set ANT_OPTS=-Xdebug -Xmx1024m -XX:MaxPermSize=3072m -Xnoagent -Djava.compiler=NONE -Xrunjdwp:transport=dt_socket,address=4000,server=y,suspend=n"
					+ "\r\nant jetty.run 2> error.log\r\nendlocal";
			final File file = new File(bizpath, "jetty-debug.cmd");
			CbxCommand.WriteFile(file, debugString);

			Runtime.getRuntime().exec(
					"cmd /c cd " + bizpath + " & start jetty-debug.cmd");

		} catch (Exception e) {

			MessageDialog.openInformation(window.getShell(), "Error",
					e.getMessage());
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

	public IProject[] getProjects() {
		IProject[] projects = ResourcesPlugin.getWorkspace().getRoot()
				.getProjects();
		return projects;
	}
}