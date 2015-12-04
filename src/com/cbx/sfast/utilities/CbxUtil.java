package com.cbx.sfast.utilities;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

import org.dom4j.Document;
import org.dom4j.DocumentHelper;
import org.dom4j.Element;
import org.dom4j.io.SAXReader;
import org.dom4j.io.XMLWriter;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.console.ConsolePlugin;
import org.eclipse.ui.console.IConsole;
import org.eclipse.ui.console.IConsoleManager;
import org.eclipse.ui.console.MessageConsole;
import org.eclipse.ui.console.MessageConsoleStream;

public class CbxUtil {

	private static String generalpath;
	public static String bizpath;
	private static String uipath;
	private static String corepath;

	private static String generallibpath = "lib/runtime/";
	private static String bizlibpath = "src/main/webapp/WEB-INF/lib/";
	private static String uilibpath = "lib/runtime/";

	public static MessageConsoleStream out = findConsole().newMessageStream();
	public static MessageConsoleStream err = findConsole().newMessageStream();

	public static IProject[] projects = getProjects();

	static {
		err.setColor(new Color(Display.getDefault(), 255, 0, 0));
		getProjectsPath();
	}

	public static void runBiz(String _bizpath) throws IOException {
		log("util runBiz Line 34\t" + "exec:\t" + "cmd /c cd " + _bizpath
				+ " & start jetty-debug.cmd");
		Runtime.getRuntime().exec(
				"cmd /c cd " + _bizpath + " & start jetty-debug.cmd");
	}

	public static void Show(final Shell shell, final String titile,
			final String message) {
		Display.getDefault().syncExec(new Runnable() {
			public void run() {
				MessageDialog.openInformation(shell, titile, message);
			}
		});
	}

	public static void ShowError(final Shell shell, final String titile,
			final String message) {
		Display.getDefault().syncExec(new Runnable() {
			public void run() {
				MessageDialog.openError(shell, titile, message);
			}
		});
	}

	public static boolean antGeneral(IWorkbenchWindow window) {
		try {

			if (bizpath == null || generalpath == null) {
				ShowError(window.getShell(), "Error", "路径未找到");
				return false;
			}
			final Process ps = Runtime.getRuntime().exec(
					String.format("cmd /c cd %s & ant jar", generalpath));

			String msg = loadStream(ps.getInputStream());
			if (msg.contains("BUILD FAILED")) {
				err("util antGeneral Line 68\t"
						+ loadStream(ps.getInputStream()));
				return false;
			}
			String errmsg = loadStream(ps.getErrorStream());
			if (errmsg != "" || errmsg != null) {
				err("util antGeneral Line 74\t"
						+ loadStream(ps.getErrorStream()));
			}

			final File releaseJar = ReleaseFile(generalpath, "cbx-general");
			if (releaseJar == null) {
				ShowError(window.getShell(), "Error", "Build failed");
				return false;
			}

			final boolean isDelete = DeleteFile(bizpath + bizlibpath,
					"cbx-general");

			if (!isDelete) {
				ShowError(window.getShell(), "Error", "未能删除jar包");
				return false;
			}

			CopyTo(releaseJar,
					new File(bizpath + bizlibpath, releaseJar.getName()));
		} catch (Exception e) {
			err("util antGeneral Line 84\t" + "err:---" + e.getMessage());
			return false;
		}
		return true;
	}

	public static boolean antUI(IWorkbenchWindow window) {
		try {

			if (bizpath == null || generalpath == null || uipath == null) {
				ShowError(window.getShell(), "Error", "路径未找到");
				return false;
			}
			final Process ps = Runtime.getRuntime().exec(
					String.format("cmd /c cd %s & ant jar", uipath));

			String msg = loadStream(ps.getInputStream());
			if (msg.contains("BUILD FAILED")) {
				err("util antUI Line 117\t" + loadStream(ps.getInputStream()));
				return false;
			}
			String errmsg = loadStream(ps.getErrorStream());
			if (errmsg != "" || errmsg != null) {
				err("util antUI Line 122\t" + "err:---"
						+ loadStream(ps.getErrorStream()));
			}

			final File releaseJar = ReleaseFile(uipath, "cbx-ui");
			if (releaseJar == null) {
				ShowError(window.getShell(), "Error", "Build failed");
				return false;
			}

			final boolean isDelete1 = DeleteFile(bizpath + bizlibpath, "cbx-ui");
			final boolean isDelete2 = DeleteFile(generalpath + generallibpath,
					"cbx-ui");
			if (!(isDelete1 && isDelete2)) {
				ShowError(window.getShell(), "Error", "未能删除jar包");
				return false;
			}

			CopyTo(releaseJar, new File(generalpath + generallibpath,
					releaseJar.getName()));
			CopyTo(releaseJar,
					new File(bizpath + bizlibpath, releaseJar.getName()));

		} catch (Exception e) {
			err("util antUI Line 133\t" + "err:---" + e.getMessage());
			return false;
		}
		return true;
	}

	public static boolean antCore(IWorkbenchWindow window) {
		try {

			if (bizpath == null || generalpath == null || uipath == null
					|| corepath == null) {
				ShowError(window.getShell(), "Error", "路径未找到");
				return false;
			}
			final Process ps = Runtime.getRuntime().exec(
					String.format("cmd /c cd %s & ant jar", corepath));

			String msg = loadStream(ps.getInputStream());
			if (msg.contains("BUILD FAILED")
					&& !msg.contains(" Directory does not exist")) {
				err("util antCore Line 186\t" + loadStream(ps.getInputStream()));
				return false;
			}
			String errmsg = loadStream(ps.getErrorStream());
			if (errmsg != "" || errmsg != null) {
				err("util antCore Line 192\t" + "err:---"
						+ loadStream(ps.getErrorStream()));
			}

			final File releaseJar = ReleaseFile(corepath, "cbx-core");
			if (releaseJar == null) {
				ShowError(window.getShell(), "Error", "Build failed");
				return false;
			}

			final boolean isDelete1 = DeleteFile(bizpath + bizlibpath,
					"cbx-core");
			final boolean isDelete2 = DeleteFile(generalpath + generallibpath,
					"cbx-core");
			final boolean isDelete3 = DeleteFile(uipath + uilibpath, "cbx-core");
			if (!(isDelete1 && isDelete2 && isDelete3)) {
				ShowError(window.getShell(), "Error", "未能删除jar包");
				return false;
			}

			CopyTo(releaseJar,
					new File(uipath + uilibpath, releaseJar.getName()));
			CopyTo(releaseJar, new File(generalpath + generallibpath,
					releaseJar.getName()));
			CopyTo(releaseJar,
					new File(bizpath + bizlibpath, releaseJar.getName()));

		} catch (Exception e) {
			err("util antCore Line 204\t" + e.getMessage());
			return false;
		}
		return true;
	}

	public static void err(final String message) {
		Display.getDefault().syncExec(new Runnable() {
			public void run() {
				err.println("[" + getTime() + "]" + message);
			}
		});
	}

	public static void log(final String message) {
		Display.getDefault().syncExec(new Runnable() {
			public void run() {
				out.println("[" + getTime() + "]" + message);
			}
		});
	}


	public static String getTime() {
		Date date = new Date();
		DateFormat format = new SimpleDateFormat("HH:mm:ss");
		String time = format.format(date);
		return time;
	}

	public static MessageConsole findConsole() {
		ConsolePlugin plugin = ConsolePlugin.getDefault();
		IConsoleManager conMan = plugin.getConsoleManager();
		IConsole[] existing = conMan.getConsoles();
		for (int i = 0; i < existing.length; i++)
			if ("SFast console".equals(existing[i].getName()))
				return (MessageConsole) existing[i];

		MessageConsole myConsole = new MessageConsole("SFast console", null);
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
					log("Skip directory----------------" + fi.getAbsolutePath());
				} else {
					if (fi.getName().contains(filename)
							&& fi.getName().contains(".jar")) {
						log("util ReleaseFile Line 244\tfound: "
								+ fi.getAbsolutePath());
						return fi;
					} else {
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
					log("Skip directory----------------" + fi.getAbsolutePath());
				} else {
					if (fi.getName().contains(filename)) {

						if (!fi.delete()) {
							err("util DeleteFile Line 267\tcannot delete "
									+ fi.getAbsolutePath());
							return false;
						} else {
							if (fi.getName().contains("alpha")) {
								SettleBuildPath();
							}
							log("util DeleteFile Line 272\tdeleting "
									+ fi.getAbsolutePath());
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

		log("util CopyTo Line 286\tcopy file: " + f1.getAbsolutePath() + " to "
				+ f2.getAbsolutePath());
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

			log("util WriteFile Line 305\twrite file: "
					+ file.getAbsolutePath());

			final byte[] contentInBytes = content.getBytes();

			fop.write(contentInBytes);
			fop.flush();
			fop.close();

		} catch (final IOException e) {
			e.printStackTrace();
		}
	}

	public static void getProjectsPath() {
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
	}

	public static void SettleBuildPath() {
		if (bizpath != null) {
			SettleBuildPath(bizpath, bizlibpath);
		}
		if (generalpath != null) {
			SettleBuildPath(generalpath, generallibpath);
		}
		if (uipath != null) {
			SettleBuildPath(uipath, uilibpath);
		}
	}

	@SuppressWarnings("unchecked")
	public static void SettleBuildPath(final String projectPath,
			final String projectLibPath) {
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
			writeXml(file, doc);
		} catch (final Exception e) {

			err("util SettleBuildPath Line 383:\t " + e.getMessage());
		}
	}

	@SuppressWarnings("unchecked")
	private static boolean hasNode(final Element root, final String filePath,
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

	public static void writeXml(final File file, final Document doc) {
		try {
			final XMLWriter out = new XMLWriter(new FileWriter(file));
			out.write(doc);
			out.flush();
			out.close();
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
