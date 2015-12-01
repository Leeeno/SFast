package com.cbx.sfast.utilities;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.console.ConsolePlugin;
import org.eclipse.ui.console.IConsole;
import org.eclipse.ui.console.IConsoleManager;
import org.eclipse.ui.console.MessageConsole;
import org.eclipse.ui.console.MessageConsoleStream;

public class CbxUtil {

	private static String generalpath;
	private static String bizpath;
	private static String uipath;
	private static String corepath;

	private static String generallibpath = generalpath + "lib/runtime/";
	private static String bizlibpath = bizpath + "src/main/webapp/WEB-INF/lib/";
	private static String uilibpath = uipath + "lib/runtime/";

	public static MessageConsoleStream out = findConsole().newMessageStream();

	public static IProject[] projects = getProjects();

	public static void runBiz(String _bizpath) throws IOException {
		out.println("util runBiz Line 34\t" + "exec:\t" + "cmd /c cd "
				+ _bizpath + " & start jetty-debug.cmd");
		Runtime.getRuntime().exec(
				"cmd /c cd " + _bizpath + " & start jetty-debug.cmd");
	}

	public static void Show(Shell shell, String titile, String message) {

		MessageDialog.openInformation(shell, titile, message);
	}

	public static void antGeneral(IWorkbenchWindow window) {
		try {
			for (IProject project : projects) {
				if ("CBX_General".equals(project.getName())) {
					generalpath = project.getLocationURI().getPath() + "/";
					generalpath = generalpath.substring(1);
				} else if ("CBX_Business".equals(project.getName())) {
					bizpath = project.getLocationURI().getPath() + "/";
					bizpath = bizpath.substring(1);
				}
			}

			if (bizpath == null || generalpath == null) {
				CbxUtil.Show(window.getShell(), "Error", "路径未找到");
				return;
			}
			final Process ps = Runtime.getRuntime().exec(
					String.format("cmd /c cd %s & ant jar", generalpath));
			out.println("util antCore Line 63\t"
					+ loadStream(ps.getInputStream()));
			out.println("util antCore Line 64\t" + "err:---"
					+ loadStream(ps.getErrorStream()));

			final File releaseJar = CbxUtil.ReleaseFile(generalpath,
					"cbx-general");
			if (releaseJar == null) {
				CbxUtil.Show(window.getShell(), "Error", "Build failed");
				return;
			}

			final boolean isDelete = CbxUtil.DeleteFile(bizlibpath, "cbx-core");

			if (!isDelete) {
				CbxUtil.Show(window.getShell(), "Error",
						"未能删除jar包----------------");
				return;
			}

			CbxUtil.CopyTo(releaseJar,
					new File(bizlibpath, releaseJar.getName()));
		} catch (Exception e) {
			out.println("util antCore Line 84\t" + "err:---" + e.getMessage());
		}
	}

	public static void antUI(IWorkbenchWindow window) {
		try {
			for (IProject project : projects) {
				if ("CBX_General".equals(project.getName())) {
					generalpath = project.getLocationURI().getPath() + "/";
					generalpath = generalpath.substring(1);
				} else if ("CBX_Business".equals(project.getName())) {
					bizpath = project.getLocationURI().getPath() + "/";
					bizpath = bizpath.substring(1);
				} else if ("CBX_UI".equals(project.getName())) {
					uipath = project.getLocationURI().getPath() + "/";
					uipath = uipath.substring(1);
				}
			}

			if (bizpath == null || generalpath == null || uipath == null) {
				Show(window.getShell(), "Error", "路径未找到");
				return;
			}
			final Process ps = Runtime.getRuntime().exec(
					String.format("cmd /c cd %s & ant jar", uipath));
			out.println("util antCore Line 111\t"
					+ loadStream(ps.getInputStream()));
			out.println("util antCore Line 113\t" + "err:---"
					+ loadStream(ps.getErrorStream()));

			final File releaseJar = ReleaseFile(uipath, "cbx-ui");
			if (releaseJar == null) {
				Show(window.getShell(), "Error", "Build failed");
				return;
			}

			final boolean isDelete1 = DeleteFile(bizlibpath, "cbx-ui");
			final boolean isDelete2 = DeleteFile(generallibpath, "cbx-ui");
			if (!(isDelete1 && isDelete2)) {
				Show(window.getShell(), "Error", "未能删除jar包----------------");
				return;
			}

			CopyTo(releaseJar, new File(generallibpath, releaseJar.getName()));
			CopyTo(releaseJar, new File(bizlibpath, releaseJar.getName()));

		} catch (Exception e) {
			out.println("util antCore Line 133\t" + "err:---" + e.getMessage());
		}
	}

	public static void antCore(IWorkbenchWindow window) {
		try {
			for (IProject project : projects) {
				if ("CBX_General".equals(project.getName())) {
					generalpath = project.getLocationURI().getPath() + "/";
					generalpath = generalpath.substring(1);
				} else if ("CBX_Business".equals(project.getName())) {
					bizpath = project.getLocationURI().getPath() + "/";
					bizpath = bizpath.substring(1);
				} else if ("CBX_UI".equals(project.getName())) {
					uipath = project.getLocationURI().getPath() + "/";
					uipath = uipath.substring(1);
				} else if ("CBX_Core".equals(project.getName())) {
					corepath = project.getLocationURI().getPath() + "/";
					corepath = corepath.substring(1);
				}
			}

			out.println("util antCore Line 155\t" + "bizpath:\t" + bizpath);
			out.println("util antCore Line 156\t" + "generalpath:\t"
					+ generalpath);
			out.println("util antCore Line 157\t" + "uipath:\t" + uipath);
			out.println("util antCore Line 158\t" + "corepath:\t" + corepath);
			if (bizpath == null || generalpath == null || uipath == null
					|| corepath == null) {
				Show(window.getShell(), "Error", "路径未找到");
				return;
			}
			final Process ps = Runtime.getRuntime().exec(
					String.format("cmd /c cd %s & ant jar", corepath));
			out.println("util antCore Line 167\t"
					+ loadStream(ps.getInputStream()));
			out.println("util antCore Line 168\t" + "err:---"
					+ loadStream(ps.getErrorStream()));

			final File releaseJar = ReleaseFile(corepath, "cbx-core");
			if (releaseJar == null) {
				Show(window.getShell(), "Error", "Build failed");
				return;
			}

			final boolean isDelete1 = DeleteFile(bizlibpath, "cbx-core");
			final boolean isDelete2 = DeleteFile(generallibpath, "cbx-core");
			final boolean isDelete3 = DeleteFile(uilibpath, "cbx-core");
			if (!(isDelete1 && isDelete2 && isDelete3)) {
				Show(window.getShell(), "Error", "未能删除jar包");
				return;
			}

			CopyTo(releaseJar, new File(generallibpath, releaseJar.getName()));
			CopyTo(releaseJar, new File(uilibpath, releaseJar.getName()));
			CopyTo(releaseJar, new File(bizlibpath, releaseJar.getName()));

		} catch (Exception e) {
			out.println("util antCore Line 191\t" + "err:---" + e.getMessage());
		}
	}

	public static MessageConsole findConsole() {
		ConsolePlugin plugin = ConsolePlugin.getDefault();
		IConsoleManager conMan = plugin.getConsoleManager();
		IConsole[] existing = conMan.getConsoles();
		for (int i = 0; i < existing.length; i++)
			if ("Console".equals(existing[i].getName()))
				return (MessageConsole) existing[i];
		// no console found, so create a new one
		MessageConsole myConsole = new MessageConsole("Console", null);
		conMan.addConsoles(new IConsole[] { myConsole });
		return myConsole;
	}

	public static String loadStream(InputStream in) throws IOException {
		int ptr = 0;
		in = new BufferedInputStream(in);
		final StringBuffer buffer = new StringBuffer();
		while ((ptr = in.read()) != -1) {
			buffer.append((char) ptr);
		}
		return buffer.toString();
	}

	public static File ReleaseFile(final String path, final String filename)
			throws Exception {

		final File release = new File(path + "release");
		final File[] files = release.listFiles();
		if (files != null) {
			for (final File fi : files) {
				if (fi.isDirectory()) {
					System.out.println("Skip directory----------------"
							+ fi.getAbsolutePath());
				} else {
					if (fi.getName().contains(filename)
							&& fi.getName().contains(".jar")) {
						return fi;
					}
				}
			}
		}
		return null;
	}

	public static boolean DeleteFile(final String path, final String filename)
			throws Exception {
		final File rootp = new File(path);
		final File[] files = rootp.listFiles();
		if (files != null) {
			for (final File fi : files) {
				if (fi.isDirectory()) {
					System.out.println("Skip directory----------------"
							+ fi.getAbsolutePath());
				} else {
					if (fi.getName().contains(filename)) {
						if (!fi.delete()) {
							return false;
						}
					}
				}
			}
		}
		return true;
	}

	@SuppressWarnings("resource")
	public static void CopyTo(final File f1, final File f2) throws Exception {
		int byteread = 0;
		final InputStream inStream = new FileInputStream(f1);
		final FileOutputStream fs = new FileOutputStream(f2);
		final byte[] buffer = new byte[1444];
		while ((byteread = inStream.read(buffer)) != -1) {
			fs.write(buffer, 0, byteread);
		}
		inStream.close();
	}

	public static void WriteFile(final File file, final String content) {

		try (FileOutputStream fop = new FileOutputStream(file)) {

			if (!file.exists()) {
				file.createNewFile();
			}

			// get the content in bytes
			final byte[] contentInBytes = content.getBytes();

			fop.write(contentInBytes);
			fop.flush();
			fop.close();

		} catch (final IOException e) {
			e.printStackTrace();
		}
	}

	public static IProject[] getProjects() {
		IProject[] projects = ResourcesPlugin.getWorkspace().getRoot()
				.getProjects();
		return projects;
	}
}
