package com.cbx.sfast.utilities;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.apache.commons.lang.StringUtils;

public class GitUtil {

    public static String getChangedString(final String projectPath) {
        try {
            final String strCL = getStatus(projectPath);
            final String[] lines = strCL.split("\n");

            boolean untrackStart = false;
            Long changedOn = 0L;
            String strDeleted = "";
            final List<String> deleted = new ArrayList<String>();

            for (final String line : lines) {
                if (-1 != line.indexOf("(")) {
                    continue;
                }
                if ("#".equals(line)) {
                    continue;
                }
                if (untrackStart || line.contains("modified:")) {
                    final Long co = getFileChangedOn(projectPath, line);
                    if (changedOn < co) {
                        changedOn = co;
                    }
                }
                if (line.contains("deleted:")) {
                    deleted.add(getFile(line));
                }

                if (line.contains("Untracked files:")) {
                    untrackStart = true;
                }
            }
            Collections.sort(deleted);
            for (final String str : deleted) {
                strDeleted += str;
            }
            return changedOn + strDeleted;
        } catch (final IOException e) {
            CbxUtil.errln(CbxUtil.getLineInfo() + e.getMessage());
        }
        return "0";
    }

    public static List<String> getChangedFileList(final String projectPath) {
        try {
            final String status = getStatus(projectPath);
            final List<String> fileList = new ArrayList<String>();
            final String[] lines = status.split("\n");

            boolean untrackStart = false;

            for (final String line : lines) {
                if (-1 != line.indexOf("(") || "#".equals(line)) {
                    continue;
                }
                if (untrackStart || line.contains("modified:")) {
                    String filename;
                    if (line.indexOf(":") == -1) {
                        filename = line.split("\t")[1].trim();
                    } else {
                        filename = line.split(":")[1].trim();
                    }
                    final File file = new File(projectPath + filename);
                    fileList.addAll(getFilePath(file));
                }
                if (line.contains("Untracked files:")) {
                    untrackStart = true;
                }
            }
            return fileList;
        } catch (final IOException e) {
            CbxUtil.errln(CbxUtil.getLineInfo() + e.getMessage());
        }
        return null;
    }

    public static List<String> getFilePath(final File file) {
        final List<String> fileList = new ArrayList<String>();
        final File[] files = file.listFiles();
        for (final File fi : files) {
            if (fi.isFile()) {
                fileList.add(fi.getAbsolutePath());
            } else if (fi.isDirectory()) {
                fileList.addAll(getFilePath(fi));
            }
        }
        return fileList;
    }

    public static String getStatus(final String projectPath) throws IOException {
        // CbxUtil.log(String.format("cmd /c cd %s & git status", projectPath));
        final Process ps = Runtime.getRuntime().exec(String.format("cmd /c cd %s & git status", projectPath));

        final String msg = CbxUtil.convertInputStreamToString(ps.getInputStream());
        if (StringUtils.isBlank(msg)) {
            CbxUtil.errln(CbxUtil.getLineInfo() + "No message.");
        }
        final String errmsg = CbxUtil.convertInputStreamToString(ps.getErrorStream());
        if (StringUtils.isNotBlank(errmsg)) {
            CbxUtil.errln(CbxUtil.getLineInfo() + errmsg);
        }
        return msg;
    }

    public static String getFile(final String line) {
        String filename;
        if (line.indexOf(":") == -1) {
            filename = line.split("\t")[1].trim();
        } else {
            filename = line.split(":")[1].trim();
        }
        return filename;
    }

    public static Long getFileChangedOn(final String projectPath, final String line) {
        String filename;
        if (line.indexOf(":") == -1) {
            filename = line.split("\t")[1].trim();
        } else {
            filename = line.split(":")[1].trim();
        }
        final File file = new File(projectPath + filename);
        if (file.isFile()) {
            return file.lastModified();
        } else if (file.isDirectory()) {
            return getFileChangedOn(file, 0L);
        }
        return 0L;
    }

    public static Long getFileChangedOn(final File file, Long changedOn) {
        final File[] files = file.listFiles();
        for (final File fi : files) {
            if (fi.isFile()) {
                if (changedOn < fi.lastModified()) {
                    changedOn = fi.lastModified();
                }
            } else if (fi.isDirectory()) {
                final Long co = getFileChangedOn(fi, changedOn);
                if (changedOn < co) {
                    changedOn = co;
                }
            }
        }
        return changedOn;
    }
}
