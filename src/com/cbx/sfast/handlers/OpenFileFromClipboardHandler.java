package com.cbx.sfast.handlers;

import java.io.File;

import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.core.filesystem.EFS;
import org.eclipse.core.filesystem.IFileStore;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.handlers.HandlerUtil;
import org.eclipse.ui.ide.IDE;

import com.cbx.sfast.utilities.CbxUtil;

/**
 * Our sample handler extends AbstractHandler, an IHandler base class.
 *
 * @see org.eclipse.core.commands.IHandler
 * @see org.eclipse.core.commands.AbstractHandler
 */
public class OpenFileFromClipboardHandler extends AbstractHandler {
    /**
     * The constructor.
     */
    public OpenFileFromClipboardHandler() {
    }

    /**
     * the command has been executed, so extract extract the needed information from the application context.
     */
    @Override
    public Object execute(final ExecutionEvent event) throws ExecutionException {
        final IWorkbenchWindow window = HandlerUtil.getActiveWorkbenchWindowChecked(event);
        try {
            final String filePath = CbxUtil.getClipboardText();
            final String[] filePaths = filePath.trim().replace("\n", "").split("\t");
            File fileToOpen = null;
            if (filePaths.length == 1) {
                fileToOpen = new File(filePath);
            } else if (filePaths.length >= 2) {
                final IProject[] projects = getProjects();
                IFile file = null;
                for (final IProject project : projects) {
                    file = project.getFile(filePaths[1].replace("\t", "") + "/" + filePaths[0].replace("\t", ""));
                    fileToOpen = new File(file.getLocationURI().getPath());
                    if (!(fileToOpen.exists() && fileToOpen.isFile())) {
                        fileToOpen = null;
                        continue;
                    } else {
                        break;
                    }
                }
            } else {
                CbxUtil.errln(CbxUtil.getLineInfo() + "Can not open file:" + filePath + "\n内容格式不正确");
                MessageDialog.openInformation(window.getShell(), "Can not open file:", filePath + "\n内容格式不正确");
                return null;
            }
            if (fileToOpen.exists() && fileToOpen.isFile()) {
                final IFileStore fileStore = EFS.getLocalFileSystem().getStore(fileToOpen.toURI());
                final IWorkbenchPage page = window.getActivePage();

                IDE.openEditorOnFileStore(page, fileStore);
            } else {
                CbxUtil.errln(CbxUtil.getLineInfo() + "Can not open file:" + filePath + "\n找不到文件\n"
                        + fileToOpen.getPath());
                MessageDialog.openInformation(window.getShell(), "Can not open file:", filePath + "\n找不到文件\n"
                        + fileToOpen.getPath());
                return null;
            }

        } catch (final Exception e) {
            CbxUtil.errln(CbxUtil.getLineInfo() + e.getMessage());
        }

        return null;
    }

    public IProject[] getProjects() {
        final IProject[] projects = ResourcesPlugin.getWorkspace().getRoot().getProjects();
        return projects;
    }

}
