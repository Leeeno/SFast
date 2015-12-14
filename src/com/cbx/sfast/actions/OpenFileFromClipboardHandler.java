package com.cbx.sfast.actions;

import java.awt.Toolkit;
import java.awt.datatransfer.Clipboard;
import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.Transferable;
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
	 * the command has been executed, so extract extract the needed information
	 * from the application context.
	 */
	public Object execute(ExecutionEvent event) throws ExecutionException {
		IWorkbenchWindow window = HandlerUtil
				.getActiveWorkbenchWindowChecked(event);
		try {
			String filePath = getClipboardText();
			String[] filePaths = filePath.trim().replace("\n", "").split("\t");
			File fileToOpen = null;
			if (filePaths.length == 1) {
				fileToOpen = new File(filePath);
			} else if (filePaths.length >= 2) {
				IProject[] projects = getProjects();
				IFile file = null;
				for (IProject project : projects) {
					file = project.getFile(filePaths[1].replace("\t", "") + "/"
							+ filePaths[0].replace("\t", ""));
					fileToOpen = new File(file.getLocationURI().getPath());
					if (!(fileToOpen.exists() && fileToOpen.isFile())) {
						fileToOpen = null;
						continue;
					} else {
						break;
					}
				}
			} else {
				CbxUtil.err("OpenFileFromClipboardHandler Line 66\t"
						+ "Can not open file:" + getClipboardText()
						+ "\n内容格式不正确");
				MessageDialog.openInformation(window.getShell(),
						"Can not open file:", getClipboardText() + "\n内容格式不正确");
				return null;
			}
			if (fileToOpen.exists() && fileToOpen.isFile()) {
				IFileStore fileStore = EFS.getLocalFileSystem().getStore(
						fileToOpen.toURI());
				IWorkbenchPage page = window.getActivePage();

				IDE.openEditorOnFileStore(page, fileStore);
			} else {
				CbxUtil.err("OpenFileFromClipboardHandler Line 80\t"
						+ "Can not open file:" + getClipboardText()
						+ "\n找不到文件\n" + fileToOpen.getPath());
				MessageDialog.openInformation(window.getShell(),
						"Can not open file:", getClipboardText() + "\n找不到文件\n"
								+ fileToOpen.getPath());
				return null;
			}

		} catch (Exception e) {
			CbxUtil.err("OpenFileFromClipboardHandler Line 90\t"
					+ e.getMessage());
		}

		return null;
	}

	public IProject[] getProjects() {
		IProject[] projects = ResourcesPlugin.getWorkspace().getRoot()
				.getProjects();
		return projects;
	}

	protected String getClipboardText() {
		Clipboard clip = Toolkit.getDefaultToolkit().getSystemClipboard();// 获取系统剪贴板
		// 获取剪切板中的内容
		Transferable clipT = clip.getContents(null);
		if (clipT != null) {
			// 检查内容是否是文本类型
			if (clipT.isDataFlavorSupported(DataFlavor.stringFlavor))
				try {
					return (String) clipT
							.getTransferData(DataFlavor.stringFlavor);

				} catch (Exception e) {
					CbxUtil.err("OpenFileFromClipboardHandler Line 116\t"
							+ e.getMessage());
					return null;
				}
		}
		return null;
	}
}
