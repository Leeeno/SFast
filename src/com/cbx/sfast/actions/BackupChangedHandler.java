package com.cbx.sfast.actions;

import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.core.resources.IResource;
import org.eclipse.jdt.internal.core.JavaProject;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.handlers.HandlerUtil;

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
            if (path != null) {
                try {
                    CbxUtil.debugln(GitUtil.getChangedFileList(path).toString());
                } catch (final Exception e) {
                    CbxUtil.errln(e.getMessage());
                }
            }

        }
        return null;
    }
}
