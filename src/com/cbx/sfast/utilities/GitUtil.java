package com.cbx.sfast.utilities;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.apache.commons.lang.StringUtils;

public class GitUtil {

    public static final String NOTHING_CHANGED = "0";

    private static final String SEPARATOR_PATH = "\t";
    private static final String SEPARATOR_TYPE = ":";
    private static final String SEPARATOR_LINE = "\n";

    private static final String SENTENCE = "(";
    private static final String NO_MESSAGE = "No message.";

    private static final String UNTRACKED_FILE = "Untracked files:";
    private static final String DELETE_FILE = "deleted:";
    private static final String MODIFIED_FILE = "modified:";
    private static final String BLANK_LINE = "#";
    private static final String CMD_ANT_JAR_FORMAT = "cmd /c cd %s & git status";

    private static String getStatus(final String projectPath) throws IOException {
        final Process ps = Runtime.getRuntime().exec(String.format(CMD_ANT_JAR_FORMAT, projectPath));

        final String msg = CbxUtil.convertInputStreamToString(ps.getInputStream());
        if (StringUtils.isBlank(msg)) {
            CbxUtil.errln(NO_MESSAGE);
        }
        final String errmsg = CbxUtil.convertInputStreamToString(ps.getErrorStream());
        if (StringUtils.isNotBlank(errmsg)) {
            CbxUtil.errln(CbxUtil.getLineInfo() + errmsg);
        }
        return msg;
    }

    public static List<File> getChangedFileList(final String projectPath) {
        try {
            final String status = getStatus(projectPath);
            final List<File> fileList = new ArrayList<File>();
            final String[] lines = status.split(SEPARATOR_LINE);

            boolean untrackStart = false;

            for (final String line : lines) {
                if (-1 != line.indexOf(SENTENCE) || BLANK_LINE.equals(line)) {
                    continue;
                }
                if (untrackStart || line.contains(MODIFIED_FILE)) {
                    String filename;
                    if (line.indexOf(SEPARATOR_TYPE) == -1) {
                        filename = line.split(SEPARATOR_PATH)[1].trim();
                    } else {
                        filename = line.split(SEPARATOR_TYPE)[1].trim();
                    }
                    final File file = new File(projectPath + "\\" + filename);
                    fileList.addAll(getFiles(file));
                }
                if (line.contains(UNTRACKED_FILE)) {
                    untrackStart = true;
                }
            }
            return fileList;
        } catch (final IOException e) {
            CbxUtil.errln(CbxUtil.getLineInfo() + e.getMessage());
        }
        return null;
    }

    private static List<File> getFiles(final File file) {
        final List<File> fileList = new ArrayList<File>();
        if (file.isFile()) {
            fileList.add(file);
        } else if (file.isDirectory()) {
            final File[] files = file.listFiles();
            for (final File fi : files) {
                if (fi.isFile()) {
                    fileList.add(fi);
                } else if (fi.isDirectory()) {
                    fileList.addAll(getFiles(fi));
                }
            }
        }
        return fileList;

    }

    public static String getChangedString(final String projectPath) {
        try {
            final String strCL = getStatus(projectPath);
            final String[] lines = strCL.split(SEPARATOR_LINE);

            boolean untrackStart = false;
            Long changedOn = 0L;
            String strDeleted = StringUtils.EMPTY;
            final List<String> deleted = new ArrayList<String>();

            for (final String line : lines) {
                if (-1 != line.indexOf(SENTENCE) || BLANK_LINE.equals(line)) {
                    continue;
                }
                if (untrackStart || line.contains(MODIFIED_FILE)) {
                    final Long co = getFileChangedOn(projectPath, line);
                    if (changedOn < co) {
                        changedOn = co;
                    }
                }
                if (line.contains(DELETE_FILE)) {
                    deleted.add(getFile(line));
                }

                if (line.contains(UNTRACKED_FILE)) {
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
        return NOTHING_CHANGED;
    }

    private static String getFile(final String line) {
        String filename;
        if (line.indexOf(SEPARATOR_TYPE) == -1) {
            filename = line.split(SEPARATOR_PATH)[1].trim();
        } else {
            filename = line.split(SEPARATOR_TYPE)[1].trim();
        }
        return filename;
    }

    private static Long getFileChangedOn(final String projectPath, final String line) {
        String filename;
        if (line.indexOf(SEPARATOR_TYPE) == -1) {
            filename = line.split(SEPARATOR_PATH)[1].trim();
        } else {
            filename = line.split(SEPARATOR_TYPE)[1].trim();
        }
        final File file = new File(projectPath + filename);
        if (file.isFile()) {
            return file.lastModified();
        } else if (file.isDirectory()) {
            return getFileChangedOn(file, 0L);
        }
        return 0L;
    }

    private static Long getFileChangedOn(final File file, Long changedOn) {
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
