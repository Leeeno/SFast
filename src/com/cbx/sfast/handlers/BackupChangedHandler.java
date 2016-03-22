package com.cbx.sfast.handlers;

import java.io.File;
import java.util.List;

import org.apache.commons.lang.StringUtils;
import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.core.resources.IResource;
import org.eclipse.jdt.internal.core.JavaProject;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.swt.widgets.DirectoryDialog;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.handlers.HandlerUtil;

import com.cbx.sfast.preferences.PreferenceConstants;
import com.cbx.sfast.utilities.CbxUtil;
import com.cbx.sfast.utilities.GitUtil;

@SuppressWarnings("restriction")
public class BackupChangedHandler extends AbstractHandler {
    /**
     * The constructor.
     */
    public BackupChangedHandler() {
    }

    /**
     * the command has been executed, so extract extract the needed information from the application context.
     */
    @Override
    public Object execute(final ExecutionEvent event) throws ExecutionException {
        final IWorkbenchWindow window = HandlerUtil.getActiveWorkbenchWindowChecked(event);
        final ISelection sel = window.getSelectionService().getSelection();
        if (sel instanceof IStructuredSelection) {
            final Object obj = ((IStructuredSelection) sel).getFirstElement();
            IResource resource = null;
            String path = null;
            if (obj instanceof IResource) {
                resource = (IResource) obj;
                path = resource.getLocation().toOSString();
            } else if (obj instanceof JavaProject) {
                resource = ((JavaProject) obj).getResource();
                path = resource.getLocation().toOSString();
            }
            CbxUtil.logln("Backup path:\t" + path);
            if (StringUtils.isNotBlank(path)) {
                try {
                    final List<File> changedFileList = GitUtil.getChangedFileList(path);
                    final String lastPath = CbxUtil.store.getString(PreferenceConstants.P_BACKUP_PATH);
                    final Shell activeShell = PlatformUI.getWorkbench().getActiveWorkbenchWindow().getShell();
                    final DirectoryDialog directory = new DirectoryDialog(activeShell);
                    directory.setFilterPath(lastPath);
                    final String savePath = directory.open();
                    if (savePath != null) {
                        CbxUtil.store.setValue(PreferenceConstants.P_BACKUP_PATH, savePath);

                        for (final File file : changedFileList) {
                            if (isExclude(file)) {
                                continue;
                            }
                            final File toFile = new File(savePath + "\\" + file.getName());
                            final File toFile2 = new File(savePath + "\\" + getRelativePath(path, file));
                            backupOldFile(toFile);
                            backupOldFile(toFile2);
                            CbxUtil.copyFile(file, toFile);
                            CbxUtil.copyFile(file, toFile2);
                        }
                    }
                } catch (final Exception e) {
                    CbxUtil.errln(CbxUtil.getLineInfo() + e.getMessage());
                }
            }

        }
        return null;
    }

    /**
     * @param toFile
     * @throws Exception
     */
    private void backupOldFile(final File file) throws Exception {
        if (file.exists()) {
            CbxUtil.copyFile(file, new File(file.getAbsolutePath() + "-bak-" + System.currentTimeMillis()));
        }
    }

    /**
     * @param file
     * @return
     */
    private boolean isExclude(final File file) {
        final String fileName = file.getName();
        final String strRules = CbxUtil.store.getString(PreferenceConstants.P_BACKUP_EXCLUDE);
        final String[] rules = strRules.split("\\|");
        if (rules.length > 0) {
            for (final String rule : rules) {
                if (rule.length() < 2) {
                    continue;
                }
                final String ru = rule.substring(1);
                if (rule.startsWith("<") && StringUtils.startsWithIgnoreCase(fileName, ru)) {
                    return true;
                } else if (rule.startsWith(">") && StringUtils.endsWithIgnoreCase(fileName, ru)) {
                    return true;
                } else if (rule.startsWith(":") && StringUtils.equalsIgnoreCase(fileName, ru)) {
                    return true;
                } else if (rule.startsWith("?") && StringUtils.containsIgnoreCase(fileName, ru)) {
                    return true;
                }
            }
        }
        return false;
    }

    /**
     * @param path
     * @param file
     * @return
     */
    private String getRelativePath(final String path, final File file) {
        final String[] projectPath = path.split("\\\\");
        final String projectName = projectPath[projectPath.length - 1];
        final String[] paths = file.getAbsolutePath().split(projectName);
        if (paths.length > 1) {
            return projectName + paths[1];
        }
        return file.getAbsolutePath().split(":")[1];
    }
}
