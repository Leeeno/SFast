package com.cbx.sfast.actions;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.util.List;

import org.dom4j.Document;
import org.dom4j.DocumentHelper;
import org.dom4j.Element;
import org.dom4j.io.SAXReader;
import org.dom4j.io.XMLWriter;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.jface.action.IAction;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.IWorkbenchWindowActionDelegate;
import org.eclipse.jface.dialogs.MessageDialog;

/**
 * Our sample action implements workbench action delegate. The action proxy will
 * be created by the workbench and shown in the UI. When the user tries to use
 * the action, this delegate will be created and execution will be delegated to
 * it.
 *
 * @see IWorkbenchWindowActionDelegate
 */
public class ClassPathAction implements IWorkbenchWindowActionDelegate {

	private static String generallibpath = "lib/runtime/";
	private static String bizlibpath = "src/main/webapp/WEB-INF/lib/";
	private static String uilibpath = "lib/runtime/";

	private IWorkbenchWindow window;

	/**
	 * The constructor.
	 */
	public ClassPathAction() {
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
					run(project.getLocationURI().getPath() + "/", generallibpath);
				} else if ("CBX_Business".equals(project.getName())) {
					run(project.getLocationURI().getPath() + "/", bizlibpath);
				} else if ("CBX_UI".equals(project.getName())) {
					run(project.getLocationURI().getPath() + "/", uilibpath);
				}
			}

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

	@SuppressWarnings("unchecked")
	private void run(final String projectPath, final String projectLibPath) {
		final SAXReader reader = new SAXReader();
		InputStream in;
		try {
			final File file = new File(projectPath + ".classpath");
			in = new FileInputStream(file);
			final Document doc = reader.read(in);
			final Element root = doc.getRootElement();
			final List<Element> childNodes = root.elements();
			for (final Element e : childNodes) {
				if ("classpathentry".equals(e.getName())) {
					if ("lib".equals(e.attributeValue("kind"))) {

						final File jar = new File(projectPath
								+ e.attributeValue("path"));
						if (!jar.exists()) {
							root.remove(e);
						}
					}
				}
			}

			final File rootp = new File(projectPath + projectLibPath);
			final File[] files = rootp.listFiles();
			for (final File fi : files) {
				if (!fi.isDirectory()) {
					if (!hasNode(root,
							projectPath + projectLibPath + fi.getName(),
							projectPath)) {
						final Element el = DocumentHelper
								.createElement("classpathentry");
						el.addAttribute("kind", "lib");
						el.addAttribute("path", projectLibPath + fi.getName());
						root.add(el);
					}
				}
			}
			if (projectPath.equals("CBX_Business")) {
				if (!hasNode(root,
						"../CBX_Core/lib/provided/servlet-api-2.5.jar", "")) {
					final Element el = DocumentHelper
							.createElement("classpathentry");
					el.addAttribute("kind", "lib");
					el.addAttribute("path",
							"../CBX_Core/lib/provided/servlet-api-2.5.jar");
					root.add(el);
				}
			}
			write(file, doc);
		} catch (final Exception e) {
			MessageDialog.openInformation(window.getShell(), "Error:",
					e.getMessage());
			e.printStackTrace();
		}
	}

	@SuppressWarnings("unchecked")
	private boolean hasNode(final Element root, final String filePath,
			final String projectPath) {
		final List<Element> childNodes = root.elements();
		for (final Element e : childNodes) {
			if ("classpathentry".equals(e.getName())) {
				if ("lib".equals(e.attributeValue("kind"))) {
					if (filePath.equals(projectPath + e.attributeValue("path"))) {
						return true;
					}

				}
			}
		}
		return false;
	}

	public static void write(final File file, final Document doc) {
		try {
			final XMLWriter out = new XMLWriter(new FileWriter(file));
			out.write(doc);
			out.flush();
			out.close();
		} catch (final IOException e) {
			e.printStackTrace();
		}
	}

	public IProject[] getProjects() {
		IProject[] projects = ResourcesPlugin.getWorkspace().getRoot()
				.getProjects();
		return projects;
	}
}