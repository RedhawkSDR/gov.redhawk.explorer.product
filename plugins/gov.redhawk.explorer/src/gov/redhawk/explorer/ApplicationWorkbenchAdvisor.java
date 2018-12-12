/*******************************************************************************
 * This file is protected by Copyright. 
 * Please refer to the COPYRIGHT file distributed with this source distribution.
 *
 * This file is part of REDHAWK IDE.
 *
 * All rights reserved.  This program and the accompanying materials are made available under 
 * the terms of the Eclipse Public License v1.0 which accompanies this distribution, and is available at 
 * http://www.eclipse.org/legal/epl-v10.html
 *******************************************************************************/
package gov.redhawk.explorer;

import gov.redhawk.explorer.internal.ResourceFactory;
import gov.redhawk.explorer.wizard.StartupWizard;
import gov.redhawk.sca.ui.ScaUiPlugin;

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

	/* This class was essentially copied from org.eclipse.ide.internal */
	class WorkbenchAdapterFactory implements IAdapterFactory {
		private final Object resourceFactory = new ResourceFactory();

		@Override
		public <T> T getAdapter(Object adaptableObject, Class<T> adapterType) {
			if (adapterType.isInstance(adaptableObject)) {
				return adapterType.cast(adaptableObject);
			}
			if (adapterType == IPersistableElement.class) {
				return adapterType.cast(getPersistableElement(adaptableObject));
			}
			if (adapterType == IElementFactory.class) {
				return adapterType.cast(getElementFactory(adaptableObject));
			}

			return null;
		}

		@Override
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
		if (Boolean.valueOf(System.getProperty(ScaUiPlugin.PROP_SINGLE_DOMAIN))) {
			return ScaExplorerSingleDomainPerspective.PERSPECTIVE_ID;
		}
		return ScaExplorerPerspective.PERSPECTIVE_ID;
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
