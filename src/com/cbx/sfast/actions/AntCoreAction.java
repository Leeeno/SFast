package com.cbx.sfast.actions;

import java.io.File;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.jface.action.IAction;
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
public class AntCoreAction implements IWorkbenchWindowActionDelegate {

	private static String generalpath;
	private static String bizpath;
	private static String uipath;
	private static String corepath;

	private static String generallibpath = "lib/runtime/";
	private static String bizlibpath = "src/main/webapp/WEB-INF/lib/";
	private static String uilibpath = "lib/runtime/";
	private IWorkbenchWindow window;

	/**
	 * The constructor.
	 */
	public AntCoreAction() {
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
				if ("CBX_General".equals(project.getName())) {
					generalpath = project.getLocationURI().getPath() + "/";
				} else if ("CBX_Business".equals(project.getName())) {
					bizpath = project.getLocationURI().getPath() + "/";
				} else if ("CBX_UI".equals(project.getName())) {
					uipath = project.getLocationURI().getPath() + "/";
				} else if ("CBX_Core".equals(project.getName())) {
					corepath = project.getLocationURI().getPath() + "/";
				}
			}

			if (bizpath == null || generalpath == null || uipath == null || corepath == null) {
				CbxCommand.Show(window.getShell(), "Error", "路径未找到");
				return;
			}

			final Process ps = Runtime.getRuntime().exec(
					String.format("cmd /c cd %s & ant jar", corepath));
			System.out.print(CbxCommand.loadStream(ps.getInputStream()));
			System.err.print(CbxCommand.loadStream(ps.getErrorStream()));

			final File releaseJar = CbxCommand.ReleaseFile(corepath, "cbx-core");
			if (releaseJar == null) {
				CbxCommand.Show(window.getShell(), "Error", "Build failed");
				return;
			}

			final boolean isDelete1 = CbxCommand.DeleteFile(bizpath + bizlibpath,
					"cbx-core");
			final boolean isDelete2 = CbxCommand.DeleteFile(generalpath + generallibpath,
					"cbx-core");
			final boolean isDelete3 = CbxCommand.DeleteFile(uipath + uilibpath, "cbx-core");
			if (!(isDelete1 && isDelete2 && isDelete3)) {
				CbxCommand.Show(window.getShell(), "Error", "未能删除jar包----------------");
				return;
			}

			CbxCommand.CopyTo(releaseJar, new File(generalpath + generallibpath,
					releaseJar.getName()));
			CbxCommand.CopyTo(releaseJar,
					new File(uipath + uilibpath, releaseJar.getName()));
			CbxCommand.CopyTo(releaseJar,
					new File(bizpath + bizlibpath, releaseJar.getName()));

		} catch (Exception e) {

			CbxCommand.Show(window.getShell(), "Error", e.getMessage());
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