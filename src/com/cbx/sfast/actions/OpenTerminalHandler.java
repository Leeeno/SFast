package com.cbx.sfast.actions;

import java.io.File;
import java.io.IOException;

import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IResource;
import org.eclipse.jdt.core.IJavaElement;
import org.eclipse.jdt.core.IPackageFragment;
import org.eclipse.jdt.core.IPackageFragmentRoot;
import org.eclipse.jdt.internal.core.JarPackageFragmentRoot;
import org.eclipse.jdt.internal.core.JavaProject;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.team.ui.synchronize.ISynchronizeModelElement;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.handlers.HandlerUtil;

import com.cbx.sfast.utilities.CbxUtil;

/**
 * Our sample handler extends AbstractHandler, an IHandler base class.
 *
 * @see org.eclipse.core.commands.IHandler
 * @see org.eclipse.core.commands.AbstractHandler
 */
@SuppressWarnings("restriction")
public class OpenTerminalHandler extends AbstractHandler {
    /**
     * The constructor.
     */
    public OpenTerminalHandler() {
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
            // common resource file
            if (obj instanceof IFile) {
                resource = (IResource) obj;
                path = resource.getLocation().toOSString();
                path = path.substring(0, path.lastIndexOf(File.separator));
            }
            // other resource such as folder,project
            else if (obj instanceof IResource) {
                resource = (IResource) obj;
                path = resource.getLocation().toOSString();
            }
            // explorer java element, contain field,method,package
            else if (obj instanceof IJavaElement) {
                // jar resource is null
                if (obj instanceof JarPackageFragmentRoot) {
                    path = ((IPackageFragmentRoot) obj).getPath().toOSString();
                    // get folder
                    path = path.substring(0, path.lastIndexOf(File.separator));
                } else if (obj instanceof JavaProject) {
                    resource = ((JavaProject) obj).getResource();
                    path = resource.getLocation().toOSString();
                } else if (obj instanceof IPackageFragmentRoot) {
                    // src folder
                    final String prjPath = ((IPackageFragmentRoot) obj).getJavaProject().getProject().getParent()
                            .getLocation().toOSString();
                    path = prjPath + ((IPackageFragmentRoot) obj).getPath().toOSString();
                } else if (obj instanceof IPackageFragment) {// other : package
                    resource = ((IPackageFragment) obj).getResource();
                    path = resource.getLocation().toOSString();
                } else {// member:filed:
                    resource = ((IJavaElement) obj).getResource();
                    path = resource.getLocation().toOSString();
                    // get folder
                    path = path.substring(0, path.lastIndexOf(File.separator));
                }

            }
            // explorer team ui resource
            else if (obj instanceof ISynchronizeModelElement) {
                resource = ((ISynchronizeModelElement) obj).getResource();
            }
            if (path != null) {
                try {
                    final File conemu = new File("D:\\Software\\ConEmu\\ConEmu.exe1");
                    if (conemu.exists()) {
                        Runtime.getRuntime().exec("D:\\Software\\ConEmu\\ConEmu.exe /cmd -cur_console:d:" + path);
                        Runtime.getRuntime().exec("D:\\Software\\ConEmu\\ConEmu.exe /Dir " + path);
                    } else {

                        Runtime.getRuntime().exec("cmd /k start cd " + path);
                    }
                } catch (final IOException e) {
                    CbxUtil.errln(CbxUtil.getLineInfo() + e.getMessage());
                }
            }

        }
        return null;
    }
}
