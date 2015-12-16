package com.cbx.sfast.utilities;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class GitUtil {

    public static String GetChangedString(final String projectPath) {
        try {
            final String strCL = GetStatus(projectPath);
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
                    final Long co = GetFileChangedOn(projectPath, line);
                    if (changedOn < co) {
                        changedOn = co;
                    }
                }
                if (line.contains("deleted:")) {
                    deleted.add(GetFile(line));
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
            CbxUtil.err("GitUtil GetChangedString Line 49\t" + e.getMessage());
        }
        return "0";
    }

    public static String GetStatus(final String projectPath) throws IOException {
        // CbxUtil.log(String.format("cmd /c cd %s & git status", projectPath));
        final Process ps = Runtime.getRuntime().exec(String.format("cmd /c cd %s & git status", projectPath));

        final String msg = CbxUtil.loadStream(ps.getInputStream());
        if (msg == null) {
            CbxUtil.err("GitUtil GetStatus Line 60\t" + msg);
        }
        final String errmsg = CbxUtil.loadStream(ps.getErrorStream());
        if (!"".equals(errmsg) && errmsg != null) {
            CbxUtil.err("GitUtil GetStatus Line 64\t" + errmsg);
        }
        return msg;
    }

    public static String GetFile(final String line) {
        String filename;
        if (line.indexOf(":") == -1) {
            filename = line.split("\t")[1].trim();
        } else {
            filename = line.split(":")[1].trim();
        }
        return filename;
    }

    public static Long GetFileChangedOn(final String projectPath, final String line) {
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
            return GetFileChangedOn(file, 0L);
        }
        return 0L;
    }

    public static Long GetFileChangedOn(final File file, Long changedOn) {
        final File[] files = file.listFiles();
        for (final File fi : files) {
            if (fi.isFile()) {
                if (changedOn < fi.lastModified()) {
                    changedOn = fi.lastModified();
                }
            } else if (fi.isDirectory()) {
                final Long co = GetFileChangedOn(fi, changedOn);
                if (changedOn < co) {
                    changedOn = co;
                }
            }
        }
        return changedOn;
    }
}
