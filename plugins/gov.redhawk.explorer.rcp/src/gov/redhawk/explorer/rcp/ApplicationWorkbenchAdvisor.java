/**
 * REDHAWK HEADER
 *
 * Identification: $Revision: 8286 $
 */
package gov.redhawk.explorer.rcp;

import gov.redhawk.explorer.rcp.internal.ResourceFactory;
import gov.redhawk.explorer.rcp.wizard.StartupWizard;

import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.IWorkspace;
import org.eclipse.core.resources.IWorkspaceRoot;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.IAdaptable;
import org.eclipse.core.runtime.IAdapterFactory;
import org.eclipse.core.runtime.IAdapterManager;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Platform;
import org.eclipse.core.runtime.Status;
import org.eclipse.jface.window.Window;
import org.eclipse.jface.wizard.WizardDialog;
import org.eclipse.ui.IElementFactory;
import org.eclipse.ui.IPersistableElement;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.application.IWorkbenchConfigurer;
import org.eclipse.ui.application.IWorkbenchWindowConfigurer;
import org.eclipse.ui.application.WorkbenchAdvisor;
import org.eclipse.ui.application.WorkbenchWindowAdvisor;
import org.eclipse.ui.progress.WorkbenchJob;

public class ApplicationWorkbenchAdvisor extends WorkbenchAdvisor {

	private static final String PERSPECTIVE_ID = "gov.redhawk.explorer.rcp.perspective";
	
	private static final String PERSPECTIVE_ID_SD = "gov.redhawk.sca.ui.singledomain.perspective";
	
	private static final String PROP_SINGLE_DOMAIN = "gov.redhawk.sca.singledomain";

	/* This class was essentially copied from org.eclipse.ide.internal */
	class WorkbenchAdapterFactory implements IAdapterFactory {
		private final Object resourceFactory = new ResourceFactory();

		public Object getAdapter(final Object o, @SuppressWarnings("rawtypes") final Class adapterType) {
			if (adapterType.isInstance(o)) {
				return o;
			}
			if (adapterType == IPersistableElement.class) {
				return getPersistableElement(o);
			}
			if (adapterType == IElementFactory.class) {
				return getElementFactory(o);
			}

			return null;
		}

		public Class< ? >[] getAdapterList() {
			return new Class[] { IElementFactory.class, IPersistableElement.class };
		}

		/**
		 * Returns an object which is an instance of IPersistableElement
		 * associated with the given object. Returns <code>null</code> if no
		 * such object can be found.
		 */
		protected Object getPersistableElement(final Object o) {
			if (o instanceof IResource) {
				return new ResourceFactory((IResource) o);
			}
			return null;
		}

		/**
		 * Returns an object which is an instance of IElementFactory associated
		 * with the given object. Returns <code>null</code> if no such object
		 * can be found.
		 */
		protected Object getElementFactory(final Object o) {
			if (o instanceof IResource) {
				return this.resourceFactory;
			}
			return null;
		}

	};

	@Override
	public WorkbenchWindowAdvisor createWorkbenchWindowAdvisor(final IWorkbenchWindowConfigurer configurer) {
		return new ApplicationWorkbenchWindowAdvisor(configurer);
	}

	@Override
	public String getInitialWindowPerspectiveId() {
		if (System.getProperty(PROP_SINGLE_DOMAIN) != null) {
		//if (Activator.getDefault().getBundle().getBundleContext().getProperty("osgi." + PROP_SINGLE_DOMAIN) != null) {
			return ApplicationWorkbenchAdvisor.PERSPECTIVE_ID_SD;
		} else {
			return ApplicationWorkbenchAdvisor.PERSPECTIVE_ID;
		}
	}

	@Override
	public IAdaptable getDefaultPageInput() {
		final IWorkspace workspace = ResourcesPlugin.getWorkspace();
		return workspace.getRoot();
	}

	@Override
	public void initialize(final IWorkbenchConfigurer configurer) {
		super.initialize(configurer);

		configurer.setSaveAndRestore(true);

		// This is important when setSaveAndRestore is true
		// without this, the getDefaultPageInput() class will not
		// be persistable and so the navigator content adapters will
		// not work after the first load to the workspace
		final IAdapterManager manager = Platform.getAdapterManager();
		manager.registerAdapters(new WorkbenchAdapterFactory(), IWorkspaceRoot.class);
	}

	@Override
	public void postStartup() {
	    super.postStartup();

		if (Activator.getDefault().isShowStartupWizard()) {
			final WorkbenchJob job = new WorkbenchJob("Startup Wizard launcher") {
				@Override
				public IStatus runInUIThread(final IProgressMonitor monitor) {
					// Create the wizard
					final StartupWizard wiz = new StartupWizard();
					// Load the dialog
					final WizardDialog dialog = new WizardDialog(PlatformUI.getWorkbench().getDisplay().getActiveShell(), wiz);
					// Listen to page changes in the wizard
					dialog.addPageChangingListener(wiz);

					// Show the wizard and watch what happens
					if (dialog.open() != Window.OK) {
						return new Status(IStatus.CANCEL, Activator.PLUGIN_ID, "Startup Canceled");
					}
					return new Status(IStatus.OK, Activator.PLUGIN_ID, "");
				}
			};
			job.setSystem(true);
			job.schedule();
		}
	}
}
