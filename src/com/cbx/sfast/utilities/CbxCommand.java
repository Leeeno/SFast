package com.cbx.sfast.utilities;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;

import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.swt.widgets.Shell;

public class CbxCommand {

	private static String generalpath;
	private static String bizpath;
	private static String uipath;
	private static String corepath;

	private static String generallibpath = "lib/runtime/";
	private static String bizlibpath = "src/main/webapp/WEB-INF/lib/";
	private static String uilibpath = "lib/runtime/";

	public static void runBiz() {
		final String debugString = "@echo off\r\nsetlocal\r\n"
				+ "set ANT_OPTS=-Xdebug -Xmx1024m -XX:MaxPermSize=3072m -Xnoagent -Djava.compiler=NONE -Xrunjdwp:transport=dt_socket,address=4000,server=y,suspend=n"
				+ "\r\nant jetty.run 2> error.log\r\nendlocal";
		final File file = new File(bizpath, "jetty-debug.cmd");
		WriteFile(file, debugString);
		try {
			Runtime.getRuntime().exec(
					String.format("cmd /c cd %s & start jetty-debug.cmd",
							bizpath));
		} catch (final Exception e) {
			e.printStackTrace();
		}
	}

	public static void Show(Shell shell, String titile, String message) {

		MessageDialog.openInformation(shell, titile, message);
	}

	public static void antGeneral() {
		try {
			final Process ps = Runtime.getRuntime().exec(
					String.format("cmd /c cd %s & ant jar", generalpath));
			System.out.print(loadStream(ps.getInputStream()));
			System.err.print(loadStream(ps.getErrorStream()));

			final File releaseJar = ReleaseFile(generalpath, "cbx-general");
			if (releaseJar == null) {
				System.err.println("Build failed");
				return;
			}

			final boolean isDelete = DeleteFile(bizpath + bizlibpath,
					"cbx-core");

			if (!isDelete) {
				System.err.println("未能删除jar包----------------");
				return;
			}

			CopyTo(releaseJar,
					new File(bizpath + bizlibpath, releaseJar.getName()));
		} catch (final Exception e) {
			e.printStackTrace();
		}
	}

	public static void antUI() {
		try {
			final Process ps = Runtime.getRuntime().exec(
					String.format("cmd /c cd %s & ant jar", uipath));
			System.out.print(loadStream(ps.getInputStream()));
			System.err.print(loadStream(ps.getErrorStream()));

			final File releaseJar = ReleaseFile(uipath, "cbx-ui");
			if (releaseJar == null) {
				System.err.println("Build failed");
				return;
			}

			final boolean isDelete1 = DeleteFile(bizpath + bizlibpath, "cbx-ui");
			final boolean isDelete2 = DeleteFile(generalpath + generallibpath,
					"cbx-ui");
			if (!(isDelete1 && isDelete2)) {
				System.err.println("未能删除jar包----------------");
				return;
			}

			CopyTo(releaseJar, new File(generalpath + generallibpath,
					releaseJar.getName()));
			CopyTo(releaseJar,
					new File(bizpath + bizlibpath, releaseJar.getName()));
		} catch (final Exception e) {
			e.printStackTrace();
		}
	}

	public static void antCore() {
		try {
			final Process ps = Runtime.getRuntime().exec(
					String.format("cmd /c cd %s & ant jar", corepath));
			System.out.print(loadStream(ps.getInputStream()));
			System.err.print(loadStream(ps.getErrorStream()));

			final File releaseJar = ReleaseFile(corepath, "cbx-core");
			if (releaseJar == null) {
				System.err.println("Build failed");
				return;
			}

			final boolean isDelete1 = DeleteFile(bizpath + bizlibpath,
					"cbx-core");
			final boolean isDelete2 = DeleteFile(generalpath + generallibpath,
					"cbx-core");
			final boolean isDelete3 = DeleteFile(uipath + uilibpath, "cbx-core");
			if (!(isDelete1 && isDelete2 && isDelete3)) {
				System.err.println("未能删除jar包----------------");
				return;
			}

			CopyTo(releaseJar, new File(generalpath + generallibpath,
					releaseJar.getName()));
			CopyTo(releaseJar,
					new File(uipath + uilibpath, releaseJar.getName()));
			CopyTo(releaseJar,
					new File(bizpath + bizlibpath, releaseJar.getName()));
		} catch (final Exception e) {
			e.printStackTrace();
		}
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
}
