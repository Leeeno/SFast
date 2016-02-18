package com.cbx.sfast.utilities;

import java.io.File;

import org.eclipse.ui.IWorkbenchWindow;

import com.cbx.sfast.preferences.PreferenceConstants;

public class AntUtil {

    private static final String CMD_ANT_JAR_FORMAT = "cmd /c cd %s & ant jar";

    public static boolean antGeneral(final IWorkbenchWindow window) {
        try {

            if (CbxUtil.PATH_BUSINESS_PROJECT == null || CbxUtil.PATH_GENERAL_PROJECT == null) {
                CbxUtil.showErrorMessageDialog(window.getShell(), "Error", "路径未找到");
                return false;
            }
            final Process ps = Runtime.getRuntime().exec(
                    String.format(CMD_ANT_JAR_FORMAT, CbxUtil.PATH_GENERAL_PROJECT));

            final String msg = CbxUtil.convertInputStreamToString(ps.getInputStream());
            if (msg.contains("BUILD FAILED")) {
                CbxUtil.errln(CbxUtil.getLineInfo() + msg);
                return false;
            }
            final String errmsg = CbxUtil.convertInputStreamToString(ps.getErrorStream());
            if (!"".equals(errmsg) && errmsg != null) {
                CbxUtil.errln(CbxUtil.getLineInfo() + msg + "\n" + errmsg);
                return false;
            }

            final File releaseJar = checkReleaseFile(CbxUtil.PATH_GENERAL_PROJECT, "cbx-general");
            if (releaseJar == null) {
                CbxUtil.showErrorMessageDialog(window.getShell(), "Error", "Build failed");
                return false;
            }

            final boolean isDelete = deleteFile(CbxUtil.PATH_BUSINESS_PROJECT + CbxUtil.PATH_BUSINESS_LIB,
                    "cbx-general");

            if (!isDelete) {
                CbxUtil.showErrorMessageDialog(window.getShell(), "Error", "未能删除biz的jar包");
                return false;
            }

            CbxUtil.copyFile(releaseJar,
                    new File(CbxUtil.PATH_BUSINESS_PROJECT + CbxUtil.PATH_BUSINESS_LIB, releaseJar.getName()));

            CbxUtil.settleBuildPath();
        } catch (final Exception e) {
            CbxUtil.errln(CbxUtil.getLineInfo() + e.getMessage());
            return false;
        }
        return true;
    }

    public static boolean antUI(final IWorkbenchWindow window) {
        try {

            if (CbxUtil.PATH_BUSINESS_PROJECT == null || CbxUtil.PATH_GENERAL_PROJECT == null
                    || CbxUtil.PATH_UI_PROJECT == null) {
                CbxUtil.showErrorMessageDialog(window.getShell(), "Error", "路径未找到");
                return false;
            }
            final Process ps = Runtime.getRuntime().exec(String.format(CMD_ANT_JAR_FORMAT, CbxUtil.PATH_UI_PROJECT));

            final String msg = CbxUtil.convertInputStreamToString(ps.getInputStream());
            if (msg.contains("BUILD FAILED")) {
                CbxUtil.errln(CbxUtil.getLineInfo() + msg);
                return false;
            }
            final String errmsg = CbxUtil.convertInputStreamToString(ps.getErrorStream());
            if (!"".equals(errmsg) && errmsg != null) {
                CbxUtil.errln(CbxUtil.getLineInfo() + msg + "\n" + errmsg);
                return false;
            }

            final File releaseJar = checkReleaseFile(CbxUtil.PATH_UI_PROJECT, "cbx-ui");
            if (releaseJar == null) {
                CbxUtil.showErrorMessageDialog(window.getShell(), "Error", "Build failed");
                return false;
            }

            final boolean isDelete1 = deleteFile(CbxUtil.PATH_BUSINESS_PROJECT + CbxUtil.PATH_BUSINESS_LIB, "cbx-ui");
            if (!isDelete1) {
                CbxUtil.showErrorMessageDialog(window.getShell(), "Error", "未能删除biz的jar包");
                return false;
            }
            if (CbxUtil.store.getBoolean(PreferenceConstants.P_UI_JAR_TO_GENERAL)) {
                final boolean isDelete2 = deleteFile(CbxUtil.PATH_GENERAL_PROJECT + CbxUtil.PATH_GENERAL_LIB, "cbx-ui");
                if (!isDelete2) {
                    CbxUtil.showErrorMessageDialog(window.getShell(), "Error", "未能删除general的jar包");
                    return false;
                }
                CbxUtil.copyFile(releaseJar, new File(CbxUtil.PATH_GENERAL_PROJECT + CbxUtil.PATH_GENERAL_LIB,
                        releaseJar.getName()));
            }
            CbxUtil.copyFile(releaseJar,
                    new File(CbxUtil.PATH_BUSINESS_PROJECT + CbxUtil.PATH_BUSINESS_LIB, releaseJar.getName()));

            CbxUtil.settleBuildPath();
        } catch (final Exception e) {
            CbxUtil.errln(CbxUtil.getLineInfo() + e.getMessage());
            return false;
        }
        return true;
    }

    public static boolean antCore(final IWorkbenchWindow window) {
        try {

            if (CbxUtil.PATH_BUSINESS_PROJECT == null || CbxUtil.PATH_GENERAL_PROJECT == null
                    || CbxUtil.PATH_UI_PROJECT == null || CbxUtil.PATH_CORE_PROJECT == null) {
                CbxUtil.showErrorMessageDialog(window.getShell(), "Error", "路径未找到");
                return false;
            }
            final Process ps = Runtime.getRuntime().exec(String.format(CMD_ANT_JAR_FORMAT, CbxUtil.PATH_CORE_PROJECT));

            final String msg = CbxUtil.convertInputStreamToString(ps.getInputStream());
            if (msg.contains("BUILD FAILED") && !msg.contains(" Directory does not exist")) {
                CbxUtil.errln(CbxUtil.getLineInfo() + msg);
                return false;
            }
            final String errmsg = CbxUtil.convertInputStreamToString(ps.getErrorStream());
            if (!"".equals(errmsg) && errmsg != null) {
                CbxUtil.errln(CbxUtil.getLineInfo() + msg + "\n" + errmsg);
                return false;
            }

            final File releaseJar = checkReleaseFile(CbxUtil.PATH_CORE_PROJECT, "cbx-core");
            if (releaseJar == null) {
                CbxUtil.showErrorMessageDialog(window.getShell(), "Error", "Build failed");
                return false;
            }

            final boolean isDelete1 = deleteFile(CbxUtil.PATH_BUSINESS_PROJECT + CbxUtil.PATH_BUSINESS_LIB, "cbx-core");
            if (!isDelete1) {
                CbxUtil.showErrorMessageDialog(window.getShell(), "Error", "未能删除biz的jar包");
                return false;
            }

            if (CbxUtil.store.getBoolean(PreferenceConstants.P_CORE_JAR_TO_UI)) {
                final boolean isDelete2 = deleteFile(CbxUtil.PATH_UI_PROJECT + CbxUtil.PATH_UI_LIB, "cbx-core");
                if (!isDelete2) {
                    CbxUtil.showErrorMessageDialog(window.getShell(), "Error", "未能删除ui的jar包");
                    return false;
                }
                CbxUtil.copyFile(releaseJar,
                        new File(CbxUtil.PATH_UI_PROJECT + CbxUtil.PATH_UI_LIB, releaseJar.getName()));
            }

            if (CbxUtil.store.getBoolean(PreferenceConstants.P_CORE_JAR_TO_GENERAL)) {
                final boolean isDelete3 = deleteFile(CbxUtil.PATH_GENERAL_PROJECT + CbxUtil.PATH_GENERAL_LIB,
                        "cbx-core");
                if (!isDelete3) {
                    CbxUtil.showErrorMessageDialog(window.getShell(), "Error", "未能删除general的jar包");
                    return false;
                }
                CbxUtil.copyFile(releaseJar, new File(CbxUtil.PATH_GENERAL_PROJECT + CbxUtil.PATH_GENERAL_LIB,
                        releaseJar.getName()));
            }

            CbxUtil.copyFile(releaseJar,
                    new File(CbxUtil.PATH_BUSINESS_PROJECT + CbxUtil.PATH_BUSINESS_LIB, releaseJar.getName()));

            CbxUtil.settleBuildPath();
        } catch (final Exception e) {
            CbxUtil.errln(CbxUtil.getLineInfo() + e.getMessage());
            return false;
        }
        return true;
    }

    public static File checkReleaseFile(final String path, final String filename) throws Exception {
        final File release = new File(path + "release");
        final File[] files = release.listFiles();
        if (files != null) {
            for (final File fi : files) {
                if (fi.isDirectory()) {
                    CbxUtil.logln("Skip directory----------------" + fi.getAbsolutePath());
                } else {
                    if (fi.getName().contains(filename) && fi.getName().contains(".jar")) {
                        CbxUtil.logln("found: " + fi.getAbsolutePath());
                        return fi;
                    } else {
                    }
                }
            }
        }
        return null;
    }

    public static boolean deleteFile(final String path, final String contains) throws Exception {
        final File rootp = new File(path);
        final File[] files = rootp.listFiles();
        if (files != null) {
            for (final File fi : files) {
                if (fi.isDirectory()) {
                    CbxUtil.logln("Skip directory----------------" + fi.getAbsolutePath());
                } else {
                    if (fi.getName().contains(contains)) {

                        if (!fi.delete()) {
                            CbxUtil.errln(CbxUtil.getLineInfo() + fi.getAbsolutePath());
                            return false;
                        } else {
                            CbxUtil.logln("deleting " + fi.getAbsolutePath());
                        }
                    }
                }
            }
        }
        return true;
    }

}
