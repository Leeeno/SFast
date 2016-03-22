package com.cbx.sfast.utilities;

import java.io.File;

import org.apache.commons.lang.StringUtils;
import org.eclipse.ui.IWorkbenchWindow;

import com.cbx.sfast.preferences.PreferenceConstants;

public class AntUtil {

    private static final String ANT_FAILED = "antFailed";
    private static final String COPY_FAILED = "copyFailed";
    private static final String CMD_ANT_JAR_FORMAT = "cmd /c cd %s & ant jar";
    private static final String PREFIX_CORE_JAR = "cbx-core";
    private static final String PREFIX_UI_JAR = "cbx-ui";
    private static final String PREFIX_GENERAL_JAR = "cbx-general";

    public static boolean antGeneral(final IWorkbenchWindow window) {
        return antGeneral(window, false);
    }

    public static boolean antGeneral(final IWorkbenchWindow window, final boolean isForceBuild) {
        try {
            if (!isForceBuild && !CbxUtil.store.getBoolean(PreferenceConstants.P_ALWAYS_ANT_GENERAL)) {
                return true;
            }
            CbxUtil.logln("Ant General");
            if (CbxUtil.PATH_BUSINESS_PROJECT == null || CbxUtil.PATH_GENERAL_PROJECT == null) {
                CbxUtil.showErrorMessageDialog(window.getShell(), "Error", "路径未找到");
                return false;
            }

            final boolean isSmartBuild = CbxUtil.store.getBoolean(PreferenceConstants.P_SMART_BUILD);

            boolean isNeedBuild = true;
            if (isSmartBuild) {
                final String generalChanged = GitUtil.getChangedString(CbxUtil.PATH_GENERAL_PROJECT);
                if (!GitUtil.NOTHING_CHANGED.equals(generalChanged)) {
                    if (CbxUtil.store.getString(PreferenceConstants.P_GENERAL_CHANGED).equals(generalChanged)) {
                        isNeedBuild = false;
                    }
                } else {
                    isNeedBuild = false;
                }
            }
            if (isNeedBuild) {

                final Process ps = Runtime.getRuntime().exec(
                        String.format(CMD_ANT_JAR_FORMAT, CbxUtil.PATH_GENERAL_PROJECT));

                final String msg = CbxUtil.convertInputStreamToString(ps.getInputStream());
                if (msg.contains("BUILD FAILED")) {
                    CbxUtil.errln(CbxUtil.getLineInfo() + msg);
                    CbxUtil.store.setValue(PreferenceConstants.P_GENERAL_CHANGED, ANT_FAILED);
                    return false;
                }
                final String errmsg = CbxUtil.convertInputStreamToString(ps.getErrorStream());
                if (StringUtils.isNotBlank(errmsg)) {
                    CbxUtil.errln(CbxUtil.getLineInfo() + msg + "\n" + errmsg);
                    CbxUtil.store.setValue(PreferenceConstants.P_GENERAL_CHANGED, ANT_FAILED);
                    return false;
                }
            } else {
                CbxUtil.logln("Nothing changed.");
            }
            final File releaseJar = checkReleaseFile(CbxUtil.PATH_GENERAL_PROJECT, PREFIX_GENERAL_JAR);
            if (releaseJar == null) {
                CbxUtil.showErrorMessageDialog(window.getShell(), "Error", "Cannot find release jar.");
                CbxUtil.store.setValue(PreferenceConstants.P_GENERAL_CHANGED, ANT_FAILED);
                return false;
            }

            if (isNeedBuild || CbxUtil.store.getString(PreferenceConstants.P_GENERAL_CHANGED).equals(COPY_FAILED)) {

                final boolean isDelete = deleteFile(CbxUtil.PATH_BUSINESS_PROJECT + CbxUtil.PATH_BUSINESS_LIB,
                        PREFIX_GENERAL_JAR);

                if (!isDelete) {
                    CbxUtil.showErrorMessageDialog(window.getShell(), "Error", "未能删除biz的jar包");
                    CbxUtil.store.setValue(PreferenceConstants.P_GENERAL_CHANGED, COPY_FAILED);
                    return false;
                }
                CbxUtil.copyFile(releaseJar, new File(CbxUtil.PATH_BUSINESS_PROJECT + CbxUtil.PATH_BUSINESS_LIB,
                        releaseJar.getName()));

                CbxUtil.settleBuildPath();
            }
            final String generalChanged = GitUtil.getChangedString(CbxUtil.PATH_GENERAL_PROJECT);
            CbxUtil.store.setValue(PreferenceConstants.P_GENERAL_CHANGED, generalChanged);
        } catch (final Exception e) {
            CbxUtil.errln(CbxUtil.getLineInfo() + e.getMessage());
            return false;
        }
        return true;
    }

    public static boolean antUI(final IWorkbenchWindow window) {
        return antUI(window, false);
    }

    public static boolean antUI(final IWorkbenchWindow window, final boolean isForceBuild) {
        try {
            if (!isForceBuild && !CbxUtil.store.getBoolean(PreferenceConstants.P_ALWAYS_ANT_UI)) {
                return true;
            }
            CbxUtil.logln("Ant UI");
            if (CbxUtil.PATH_BUSINESS_PROJECT == null || CbxUtil.PATH_GENERAL_PROJECT == null
                    || CbxUtil.PATH_UI_PROJECT == null) {
                CbxUtil.showErrorMessageDialog(window.getShell(), "Error", "路径未找到");
                return false;
            }

            final boolean isSmartBuild = CbxUtil.store.getBoolean(PreferenceConstants.P_SMART_BUILD);

            boolean isNeedBuild = true;
            if (isSmartBuild) {
                final String uiChanged = GitUtil.getChangedString(CbxUtil.PATH_UI_PROJECT);
                if (!GitUtil.NOTHING_CHANGED.equals(uiChanged)) {
                    if (CbxUtil.store.getString(PreferenceConstants.P_UI_CHANGED).equals(uiChanged)) {
                        isNeedBuild = false;
                    } else if (CbxUtil.store.getString(PreferenceConstants.P_UI_CHANGED).equals(COPY_FAILED)) {
                        isNeedBuild = false;
                    }
                } else {
                    isNeedBuild = false;
                }
            }
            if (isNeedBuild) {

                final Process ps = Runtime.getRuntime()
                        .exec(String.format(CMD_ANT_JAR_FORMAT, CbxUtil.PATH_UI_PROJECT));

                final String msg = CbxUtil.convertInputStreamToString(ps.getInputStream());
                if (msg.contains("BUILD FAILED")) {
                    CbxUtil.errln(CbxUtil.getLineInfo() + msg);
                    CbxUtil.store.setValue(PreferenceConstants.P_UI_CHANGED, ANT_FAILED);
                    return false;
                }
                final String errmsg = CbxUtil.convertInputStreamToString(ps.getErrorStream());
                if (StringUtils.isNotBlank(errmsg)) {
                    CbxUtil.errln(CbxUtil.getLineInfo() + msg + "\n" + errmsg);
                    CbxUtil.store.setValue(PreferenceConstants.P_UI_CHANGED, ANT_FAILED);
                    return false;
                }
            } else {
                CbxUtil.logln("Nothing changed.");
            }
            final File releaseJar = checkReleaseFile(CbxUtil.PATH_UI_PROJECT, PREFIX_UI_JAR);
            if (releaseJar == null) {
                CbxUtil.showErrorMessageDialog(window.getShell(), "Error", "Cannot find release jar.");
                CbxUtil.store.setValue(PreferenceConstants.P_UI_CHANGED, ANT_FAILED);
                return false;
            }

            if (isNeedBuild || CbxUtil.store.getString(PreferenceConstants.P_UI_CHANGED).equals(COPY_FAILED)) {

                final boolean isDelete1 = deleteFile(CbxUtil.PATH_BUSINESS_PROJECT + CbxUtil.PATH_BUSINESS_LIB,
                        PREFIX_UI_JAR);
                if (!isDelete1) {
                    CbxUtil.showErrorMessageDialog(window.getShell(), "Error", "未能删除biz的jar包");
                    CbxUtil.store.setValue(PreferenceConstants.P_UI_CHANGED, COPY_FAILED);
                    return false;
                }
                if (CbxUtil.store.getBoolean(PreferenceConstants.P_UI_JAR_TO_GENERAL)) {
                    final boolean isDelete2 = deleteFile(CbxUtil.PATH_GENERAL_PROJECT + CbxUtil.PATH_GENERAL_LIB,
                            PREFIX_UI_JAR);
                    if (!isDelete2) {
                        CbxUtil.showErrorMessageDialog(window.getShell(), "Error", "未能删除general的jar包");
                        CbxUtil.store.setValue(PreferenceConstants.P_UI_CHANGED, COPY_FAILED);
                        return false;
                    }
                    CbxUtil.copyFile(releaseJar, new File(CbxUtil.PATH_GENERAL_PROJECT + CbxUtil.PATH_GENERAL_LIB,
                            releaseJar.getName()));
                }
                CbxUtil.copyFile(releaseJar, new File(CbxUtil.PATH_BUSINESS_PROJECT + CbxUtil.PATH_BUSINESS_LIB,
                        releaseJar.getName()));

                CbxUtil.settleBuildPath();
            }
            final String uiChanged = GitUtil.getChangedString(CbxUtil.PATH_UI_PROJECT);
            CbxUtil.store.setValue(PreferenceConstants.P_UI_CHANGED, uiChanged);
        } catch (final Exception e) {
            CbxUtil.errln(CbxUtil.getLineInfo() + e.getMessage());
            return false;
        }
        return true;
    }

    public static boolean antCore(final IWorkbenchWindow window) {
        return antCore(window, false);
    }

    public static boolean antCore(final IWorkbenchWindow window, final boolean isForceBuild) {
        try {
            if (!isForceBuild && !CbxUtil.store.getBoolean(PreferenceConstants.P_ALWAYS_ANT_CORE)) {
                return true;
            }
            CbxUtil.logln("Ant Core");
            if (CbxUtil.PATH_BUSINESS_PROJECT == null || CbxUtil.PATH_GENERAL_PROJECT == null
                    || CbxUtil.PATH_UI_PROJECT == null || CbxUtil.PATH_CORE_PROJECT == null) {
                CbxUtil.showErrorMessageDialog(window.getShell(), "Error", "路径未找到");
                CbxUtil.store.setValue(PreferenceConstants.P_CORE_CHANGED, ANT_FAILED);
                return false;
            }
            final boolean isSmartBuild = CbxUtil.store.getBoolean(PreferenceConstants.P_SMART_BUILD);

            boolean isNeedBuild = true;
            if (isSmartBuild) {
                final String coreChanged = GitUtil.getChangedString(CbxUtil.PATH_CORE_PROJECT);
                if (!GitUtil.NOTHING_CHANGED.equals(coreChanged)) {
                    if (CbxUtil.store.getString(PreferenceConstants.P_CORE_CHANGED).equals(coreChanged)) {
                        isNeedBuild = false;
                    } else if (CbxUtil.store.getString(PreferenceConstants.P_CORE_CHANGED).equals(COPY_FAILED)) {
                        isNeedBuild = false;
                    }
                } else {
                    isNeedBuild = false;
                }
            }
            if (isNeedBuild) {

                final Process ps = Runtime.getRuntime().exec(
                        String.format(CMD_ANT_JAR_FORMAT, CbxUtil.PATH_CORE_PROJECT));

                final String msg = CbxUtil.convertInputStreamToString(ps.getInputStream());
                if (msg.contains("BUILD FAILED") && !msg.contains(" Directory does not exist")) {
                    CbxUtil.errln(CbxUtil.getLineInfo() + msg);
                    CbxUtil.store.setValue(PreferenceConstants.P_CORE_CHANGED, ANT_FAILED);
                    return false;
                }
                final String errmsg = CbxUtil.convertInputStreamToString(ps.getErrorStream());
                if (StringUtils.isNotBlank(errmsg)) {
                    CbxUtil.errln(CbxUtil.getLineInfo() + msg + "\n" + errmsg);
                    CbxUtil.store.setValue(PreferenceConstants.P_CORE_CHANGED, ANT_FAILED);
                    return false;
                }
            } else {
                CbxUtil.logln("Nothing changed.");
            }
            final File releaseJar = checkReleaseFile(CbxUtil.PATH_CORE_PROJECT, PREFIX_CORE_JAR);
            if (releaseJar == null) {
                CbxUtil.showErrorMessageDialog(window.getShell(), "Error", "Cannot find release jar.");
                CbxUtil.store.setValue(PreferenceConstants.P_CORE_CHANGED, ANT_FAILED);
                return false;
            }
            if (isNeedBuild || CbxUtil.store.getString(PreferenceConstants.P_CORE_CHANGED).equals(COPY_FAILED)) {

                final boolean isDelete1 = deleteFile(CbxUtil.PATH_BUSINESS_PROJECT + CbxUtil.PATH_BUSINESS_LIB,
                        PREFIX_CORE_JAR);
                if (!isDelete1) {
                    CbxUtil.showErrorMessageDialog(window.getShell(), "Ereror", "未能删除biz的jar包");
                    CbxUtil.store.setValue(PreferenceConstants.P_CORE_CHANGED, COPY_FAILED);
                    return false;
                }

                if (CbxUtil.store.getBoolean(PreferenceConstants.P_CORE_JAR_TO_UI)) {
                    final boolean isDelete2 = deleteFile(CbxUtil.PATH_UI_PROJECT + CbxUtil.PATH_UI_LIB, PREFIX_CORE_JAR);
                    if (!isDelete2) {
                        CbxUtil.showErrorMessageDialog(window.getShell(), "Error", "未能删除ui的jar包");
                        CbxUtil.store.setValue(PreferenceConstants.P_CORE_CHANGED, COPY_FAILED);
                        return false;
                    }
                    CbxUtil.copyFile(releaseJar,
                            new File(CbxUtil.PATH_UI_PROJECT + CbxUtil.PATH_UI_LIB, releaseJar.getName()));
                }

                if (CbxUtil.store.getBoolean(PreferenceConstants.P_CORE_JAR_TO_GENERAL)) {
                    final boolean isDelete3 = deleteFile(CbxUtil.PATH_GENERAL_PROJECT + CbxUtil.PATH_GENERAL_LIB,
                            PREFIX_CORE_JAR);
                    if (!isDelete3) {
                        CbxUtil.showErrorMessageDialog(window.getShell(), "Error", "未能删除general的jar包");
                        CbxUtil.store.setValue(PreferenceConstants.P_CORE_CHANGED, COPY_FAILED);
                        return false;
                    }
                    CbxUtil.copyFile(releaseJar, new File(CbxUtil.PATH_GENERAL_PROJECT + CbxUtil.PATH_GENERAL_LIB,
                            releaseJar.getName()));
                }

                CbxUtil.copyFile(releaseJar, new File(CbxUtil.PATH_BUSINESS_PROJECT + CbxUtil.PATH_BUSINESS_LIB,
                        releaseJar.getName()));
                CbxUtil.settleBuildPath();
            }
            final String coreChanged = GitUtil.getChangedString(CbxUtil.PATH_CORE_PROJECT);
            CbxUtil.store.setValue(PreferenceConstants.P_CORE_CHANGED, coreChanged);

        } catch (final Exception e) {
            CbxUtil.errln(CbxUtil.getLineInfo() + e.getMessage());
            return false;
        }
        return true;
    }

    private static File checkReleaseFile(final String path, final String filename) throws Exception {
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

    private static boolean deleteFile(final String path, final String contains) throws Exception {
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
