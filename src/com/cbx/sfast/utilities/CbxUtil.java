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
import org.dom4j.io.OutputFormat;
import org.dom4j.io.SAXReader;
import org.dom4j.io.XMLWriter;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.console.ConsolePlugin;
import org.eclipse.ui.console.IConsole;
import org.eclipse.ui.console.IConsoleManager;
import org.eclipse.ui.console.MessageConsole;
import org.eclipse.ui.console.MessageConsoleStream;

import sfast.Activator;

public class CbxUtil {

    public static String PATH_GENERAL_PROJECT;
    public static String PATH_BUSINESS_PROJECT;
    public static String PATH_UI_PROJECT;
    public static String PATH_CORE_PROJECT;

    public static final String PATH_GENERAL_LIB = "lib/runtime/";
    public static final String PATH_BUSINESS_LIB = "src/main/webapp/WEB-INF/lib/";
    public static final String PATH_UI_LIB = "lib/runtime/";
    public static final String PATH_CORE_LIB = "lib/runtime/";

    private static MessageConsole console = findConsole();
    private static MessageConsoleStream errStream = console.newMessageStream();
    private static MessageConsoleStream outStream = console.newMessageStream();
    private static MessageConsoleStream debugStream = console.newMessageStream();

    public static IProject[] projects = getProjects();

    public static IPreferenceStore store = Activator.getDefault().getPreferenceStore();

    static {
        debugStream.setColor(new Color(Display.getDefault(), 0, 255, 0));
        errStream.setColor(new Color(Display.getDefault(), 255, 0, 0));
        getProjectsPath();
    }

    private static final String CMD_JETTY_WITH_CONEMU = "cmd /c cd " + PATH_BUSINESS_PROJECT
            + " & D:/Software/ConEmu/ConEmu64.exe jetty-debug.cmd";

    public static void runBiz() throws IOException {
        // log("exec:\t" + "cmd /c cd " + _bizpath + " & start jetty-debug.cmd");
        Runtime.getRuntime().exec(CMD_JETTY_WITH_CONEMU);
    }

    public static void showMessageDialog(final Shell shell, final String titile, final String message) {
        Display.getDefault().syncExec(new Runnable() {
            @Override
            public void run() {
                MessageDialog.openInformation(shell, titile, message);
            }
        });
    }

    public static void showErrorMessageDialog(final Shell shell, final String titile, final String message) {
        Display.getDefault().syncExec(new Runnable() {
            @Override
            public void run() {
                MessageDialog.openError(shell, titile, message);
            }
        });
    }

    private static MessageConsole findConsole() {
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

    public static void clearConsole() {
        console.clearConsole();
    }

    public static void err(final String message) {
        Display.getDefault().syncExec(new Runnable() {
            @Override
            public void run() {
                errStream.print(message);
            }
        });
    }

    public static void log(final String message) {
        Display.getDefault().syncExec(new Runnable() {
            @Override
            public void run() {
                outStream.print(message);
            }
        });
    }

    public static void debug(final String message) {
        Display.getDefault().syncExec(new Runnable() {
            @Override
            public void run() {
                debugStream.print(message);
            }
        });
    }

    public static void errln(final String message) {
        Display.getDefault().syncExec(new Runnable() {
            @Override
            public void run() {
                errStream.println("[" + getTime() + "]" + message);
            }
        });
    }

    public static void logln(final String message) {
        Display.getDefault().syncExec(new Runnable() {
            @Override
            public void run() {
                outStream.println("[" + getTime() + "]" + message);
            }
        });
    }

    public static void debugln(final String message) {
        Display.getDefault().syncExec(new Runnable() {
            @Override
            public void run() {
                debugStream.println("[" + getTime() + "]" + message);
            }
        });
    }

    public static String getLineInfo() {
        final StackTraceElement ste = new Throwable().getStackTrace()[1];
        return "(" + ste.getFileName() + ":" + ste.getLineNumber() + "):\t";
    }

    public static String getTime() {
        final Date date = new Date();
        final DateFormat format = new SimpleDateFormat("HH:mm:ss");
        final String time = format.format(date);
        return time;
    }

    public static String convertInputStreamToString(InputStream in) throws IOException {
        int ptr = 0;
        in = new BufferedInputStream(in);
        final StringBuffer buffer = new StringBuffer();
        while ((ptr = in.read()) != -1) {
            buffer.append((char) ptr);
        }
        return buffer.toString();
    }

    @SuppressWarnings("resource")
    public static void copyFile(final File f1, final File f2) throws Exception {
        int byteread = 0;

        logln("copy file: " + f1.getAbsolutePath() + " to " + f2.getAbsolutePath());
        final InputStream inStream = new FileInputStream(f1);
        final FileOutputStream fs = new FileOutputStream(f2);
        final byte[] buffer = new byte[1444];
        while ((byteread = inStream.read(buffer)) != -1) {
            fs.write(buffer, 0, byteread);
        }
        inStream.close();
    }

    public static void writeFile(final File file, final String content) {
        try (FileOutputStream fop = new FileOutputStream(file)) {

            if (!file.exists()) {
                file.createNewFile();
            }

            logln("write file: " + file.getAbsolutePath());

            final byte[] contentInBytes = content.getBytes();

            fop.write(contentInBytes);
            fop.flush();
            fop.close();

        } catch (final IOException e) {
            e.printStackTrace();
        }
    }

    public static void settleBuildPath() {
        if (PATH_BUSINESS_PROJECT != null) {
            settleBuildPath(PATH_BUSINESS_PROJECT, PATH_BUSINESS_LIB);
        }
        if (PATH_GENERAL_PROJECT != null) {
            settleBuildPath(PATH_GENERAL_PROJECT, PATH_GENERAL_LIB);
        }
        if (PATH_UI_PROJECT != null) {
            settleBuildPath(PATH_UI_PROJECT, PATH_UI_LIB);
        }
        if (PATH_CORE_PROJECT != null) {
            settleBuildPath(PATH_CORE_PROJECT, PATH_CORE_LIB);
        }
    }

    @SuppressWarnings("unchecked")
    private static void settleBuildPath(final String projectPath, final String projectLibPath) {
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

            errln(getLineInfo() + e.getMessage());
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
            final OutputFormat xmlFormat = new OutputFormat();
            xmlFormat.setEncoding("UTF-8");
            // 设置换行
            xmlFormat.setNewlines(true);
            // 生成缩进
            xmlFormat.setIndent(true);
            // 使用4个空格进行缩进, 可以兼容文本编辑器
            xmlFormat.setIndent("    ");
            xmlFormat.setLineSeparator("\r\n");
            xmlFormat.setExpandEmptyElements(true);

            final XMLWriter out = new XMLWriter(new FileWriter(file), xmlFormat);
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

    public static IProject getProjects(final String projectName) {
        for (final IProject iProject : projects) {
            if (projectName.equals(iProject.getName())) {
                return iProject;
            }
        }
        return null;
    }

    public static void getProjectsPath() {
        for (final IProject project : projects) {
            if ("CBX_General".equals(project.getName())) {
                PATH_GENERAL_PROJECT = project.getLocationURI().getPath() + "/";
                PATH_GENERAL_PROJECT = PATH_GENERAL_PROJECT.substring(1);
            } else if ("CBX_Business".equals(project.getName())) {
                PATH_BUSINESS_PROJECT = project.getLocationURI().getPath() + "/";
                PATH_BUSINESS_PROJECT = PATH_BUSINESS_PROJECT.substring(1);
            } else if ("CBX_UI".equals(project.getName())) {
                PATH_UI_PROJECT = project.getLocationURI().getPath() + "/";
                PATH_UI_PROJECT = PATH_UI_PROJECT.substring(1);
            } else if ("CBX_Core".equals(project.getName())) {
                PATH_CORE_PROJECT = project.getLocationURI().getPath() + "/";
                PATH_CORE_PROJECT = PATH_CORE_PROJECT.substring(1);
            }
        }
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
                    errln(getLineInfo() + e.getMessage());
                    return null;
                }
            }
        }
        return null;
    }
}
