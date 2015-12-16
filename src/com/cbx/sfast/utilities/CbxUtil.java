package com.cbx.sfast.utilities;

import java.awt.Toolkit;
import java.awt.datatransfer.Clipboard;
import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.Transferable;
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
import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.console.ConsolePlugin;
import org.eclipse.ui.console.IConsole;
import org.eclipse.ui.console.IConsoleManager;
import org.eclipse.ui.console.MessageConsole;
import org.eclipse.ui.console.MessageConsoleStream;

import sfast.Activator;

import com.cbx.sfast.preferences.PreferenceConstants;

public class CbxUtil {

    public static String generalpath;
    public static String bizpath;
    public static String uipath;
    public static String corepath;

    private static String generallibpath = "lib/runtime/";
    private static String bizlibpath = "src/main/webapp/WEB-INF/lib/";
    private static String uilibpath = "lib/runtime/";
    public static MessageConsole console = findConsole();
    public static MessageConsoleStream errStream = console.newMessageStream();
    public static MessageConsoleStream outStream = console.newMessageStream();
    public static MessageConsoleStream debugStream = console.newMessageStream();

    public static IProject[] projects = getProjects();

    public static IPreferenceStore store = Activator.getDefault().getPreferenceStore();

    static {
        debugStream.setColor(new Color(Display.getDefault(), 0, 255, 0));
        errStream.setColor(new Color(Display.getDefault(), 255, 0, 0));
        getProjectsPath();
    }

    public static void errorLogToConsole() {

    }

    public static void runBiz(final String _bizpath) throws IOException {
        //log("exec:\t" + "cmd /c cd " + _bizpath + " & start jetty-debug.cmd");
        Runtime.getRuntime().exec("cmd /c cd " + _bizpath + " & start jetty-debug.cmd");
    }

    public static void Show(final Shell shell, final String titile, final String message) {
        Display.getDefault().syncExec(new Runnable() {
            @Override
            public void run() {
                MessageDialog.openInformation(shell, titile, message);
            }
        });
    }

    public static void ShowError(final Shell shell, final String titile, final String message) {
        Display.getDefault().syncExec(new Runnable() {
            @Override
            public void run() {
                MessageDialog.openError(shell, titile, message);
            }
        });
    }

    public static boolean antGeneral(final IWorkbenchWindow window) {
        try {

            if (bizpath == null || generalpath == null) {
                ShowError(window.getShell(), "Error", "路径未找到");
                return false;
            }
            final Process ps = Runtime.getRuntime().exec(String.format("cmd /c cd %s & ant jar", generalpath));

            final String msg = loadStream(ps.getInputStream());
            if (msg.contains("BUILD FAILED")) {
                err("util antGeneral Line 68\t" + msg);
                return false;
            }
            final String errmsg = loadStream(ps.getErrorStream());
            if (!"".equals(errmsg) && errmsg != null) {
                err("util antGeneral Line 74\t" + errmsg);
                return false;
            }

            final File releaseJar = ReleaseFile(generalpath, "cbx-general");
            if (releaseJar == null) {
                ShowError(window.getShell(), "Error", "Build failed");
                return false;
            }

            final boolean isDelete = DeleteFile(bizpath + bizlibpath, "cbx-general");

            if (!isDelete) {
                ShowError(window.getShell(), "Error", "未能删除jar包");
                return false;
            }

            CopyTo(releaseJar, new File(bizpath + bizlibpath, releaseJar.getName()));

            SettleBuildPath();
        } catch (final Exception e) {
            err("util antGeneral Line 84\t" + e.getMessage());
            return false;
        }
        return true;
    }

    public static boolean antUI(final IWorkbenchWindow window) {
        try {

            if (bizpath == null || generalpath == null || uipath == null) {
                ShowError(window.getShell(), "Error", "路径未找到");
                return false;
            }
            final Process ps = Runtime.getRuntime().exec(String.format("cmd /c cd %s & ant jar", uipath));

            final String msg = loadStream(ps.getInputStream());
            if (msg.contains("BUILD FAILED")) {
                err("util antUI Line 117\t" + msg);
                return false;
            }
            final String errmsg = loadStream(ps.getErrorStream());
            if (!"".equals(errmsg) && errmsg != null) {
                err("util antUI Line 122\t" + errmsg);
                return false;
            }

            final File releaseJar = ReleaseFile(uipath, "cbx-ui");
            if (releaseJar == null) {
                ShowError(window.getShell(), "Error", "Build failed");
                return false;
            }

            final boolean isDelete1 = DeleteFile(bizpath + bizlibpath, "cbx-ui");
            final boolean isDelete2 = DeleteFile(generalpath + generallibpath, "cbx-ui");
            if (!(isDelete1 && isDelete2)) {
                ShowError(window.getShell(), "Error", "未能删除jar包");
                return false;
            }
            if (store.getBoolean(PreferenceConstants.P_UI_JAR_TO_GENERAL)) {
                CopyTo(releaseJar, new File(generalpath + generallibpath, releaseJar.getName()));
            }
            CopyTo(releaseJar, new File(bizpath + bizlibpath, releaseJar.getName()));

            SettleBuildPath();
        } catch (final Exception e) {
            err("util antUI Line 133\t" + e.getMessage());
            return false;
        }
        return true;
    }

    public static boolean antCore(final IWorkbenchWindow window) {
        try {

            if (bizpath == null || generalpath == null || uipath == null || corepath == null) {
                ShowError(window.getShell(), "Error", "路径未找到");
                return false;
            }
            final Process ps = Runtime.getRuntime().exec(String.format("cmd /c cd %s & ant jar", corepath));

            final String msg = loadStream(ps.getInputStream());
            if (msg.contains("BUILD FAILED") && !msg.contains(" Directory does not exist")) {
                err("util antCore Line 186\t" + msg);
                return false;
            }
            final String errmsg = loadStream(ps.getErrorStream());
            if (!"".equals(errmsg) && errmsg != null) {
                err("util antCore Line 192\t" + errmsg);
                return false;
            }

            final File releaseJar = ReleaseFile(corepath, "cbx-core");
            if (releaseJar == null) {
                ShowError(window.getShell(), "Error", "Build failed");
                return false;
            }

            final boolean isDelete1 = DeleteFile(bizpath + bizlibpath, "cbx-core");
            final boolean isDelete2 = DeleteFile(generalpath + generallibpath, "cbx-core");
            final boolean isDelete3 = DeleteFile(uipath + uilibpath, "cbx-core");
            if (!(isDelete1 && isDelete2 && isDelete3)) {
                ShowError(window.getShell(), "Error", "未能删除jar包");
                return false;
            }

            if (store.getBoolean(PreferenceConstants.P_CORE_JAR_TO_UI)) {
                CopyTo(releaseJar, new File(uipath + uilibpath, releaseJar.getName()));
            }

            if (store.getBoolean(PreferenceConstants.P_CORE_JAR_TO_GENERAL)) {
                CopyTo(releaseJar, new File(generalpath + generallibpath, releaseJar.getName()));
            }
            CopyTo(releaseJar, new File(bizpath + bizlibpath, releaseJar.getName()));

            SettleBuildPath();
        } catch (final Exception e) {
            err("util antCore Line 204\t" + e.getMessage());
            return false;
        }
        return true;
    }

    public static void err(final String message) {
        Display.getDefault().syncExec(new Runnable() {
            @Override
            public void run() {
                errStream.println("[" + getTime() + "]" + message);
            }
        });
    }

    public static void log(final String message) {
        Display.getDefault().syncExec(new Runnable() {
            @Override
            public void run() {
                outStream.println("[" + getTime() + "]" + message);
            }
        });
    }

    public static void debug(final String message) {
        Display.getDefault().syncExec(new Runnable() {
            @Override
            public void run() {
                debugStream.println("[" + getTime() + "]" + message);
            }
        });
    }

    public static String getTime() {
        final Date date = new Date();
        final DateFormat format = new SimpleDateFormat("HH:mm:ss");
        final String time = format.format(date);
        return time;
    }

    public static MessageConsole findConsole() {
        final ConsolePlugin plugin = ConsolePlugin.getDefault();
        final IConsoleManager conMan = plugin.getConsoleManager();
        final IConsole[] existing = conMan.getConsoles();
        for (int i = 0; i < existing.length; i++) {
            if ("SFast console".equals(existing[i].getName())) {
                return (MessageConsole) existing[i];
            }
        }

        final MessageConsole myConsole = new MessageConsole("SFast console", null);
        conMan.addConsoles(new IConsole[] {myConsole});
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

    public static File ReleaseFile(final String path, final String filename) throws Exception {
        final File release = new File(path + "release");
        final File[] files = release.listFiles();
        if (files != null) {
            for (final File fi : files) {
                if (fi.isDirectory()) {
                    log("Skip directory----------------" + fi.getAbsolutePath());
                } else {
                    if (fi.getName().contains(filename) && fi.getName().contains(".jar")) {
                        log("found: " + fi.getAbsolutePath());
                        return fi;
                    } else {
                    }
                }
            }
        }
        return null;
    }

    public static boolean DeleteFile(final String path, final String filename) throws Exception {
        final File rootp = new File(path);
        final File[] files = rootp.listFiles();
        if (files != null) {
            for (final File fi : files) {
                if (fi.isDirectory()) {
                    log("Skip directory----------------" + fi.getAbsolutePath());
                } else {
                    if (fi.getName().contains(filename)) {

                        if (!fi.delete()) {
                            err("util DeleteFile Line 267\tcannot delete " + fi.getAbsolutePath());
                            return false;
                        } else {
                            log("deleting " + fi.getAbsolutePath());
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

        log("copy file: " + f1.getAbsolutePath() + " to " + f2.getAbsolutePath());
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

            log("write file: " + file.getAbsolutePath());

            final byte[] contentInBytes = content.getBytes();

            fop.write(contentInBytes);
            fop.flush();
            fop.close();

        } catch (final IOException e) {
            e.printStackTrace();
        }
    }

    public static void getProjectsPath() {
        for (final IProject project : projects) {
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
    public static void SettleBuildPath(final String projectPath, final String projectLibPath) {
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

                        final File jar = new File(projectPath + e.attributeValue("path"));
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
                    if (!hasNode(root, projectPath + projectLibPath + fi.getName(), projectPath)) {
                        final Element el = DocumentHelper.createElement("classpathentry");
                        el.addAttribute("kind", "lib");
                        el.addAttribute("path", projectLibPath + fi.getName());
                        root.add(el);
                    }
                }
            }
            if (projectPath.equals("CBX_Business")) {
                if (!hasNode(root, "../CBX_Core/lib/provided/servlet-api-2.5.jar", "")) {
                    final Element el = DocumentHelper.createElement("classpathentry");
                    el.addAttribute("kind", "lib");
                    el.addAttribute("path", "../CBX_Core/lib/provided/servlet-api-2.5.jar");
                    root.add(el);
                }
            }
            writeXml(file, doc);
        } catch (final Exception e) {

            err("util SettleBuildPath Line 383:\t " + e.getMessage());
        }
    }

    @SuppressWarnings("unchecked")
    private static boolean hasNode(final Element root, final String filePath, final String projectPath) {
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
        final IProject[] projects = ResourcesPlugin.getWorkspace().getRoot().getProjects();
        return projects;
    }

    public static String getClipboardText() {
        final Clipboard clip = Toolkit.getDefaultToolkit().getSystemClipboard();// 获取系统剪贴板
        // 获取剪切板中的内容
        final Transferable clipT = clip.getContents(null);
        if (clipT != null) {
            // 检查内容是否是文本类型
            if (clipT.isDataFlavorSupported(DataFlavor.stringFlavor)) {
                try {
                    return (String) clipT.getTransferData(DataFlavor.stringFlavor);

                } catch (final Exception e) {
                    CbxUtil.err("OpenFileFromClipboardHandler Line 116\t" + e.getMessage());
                    return null;
                }
            }
        }
        return null;
    }
}
