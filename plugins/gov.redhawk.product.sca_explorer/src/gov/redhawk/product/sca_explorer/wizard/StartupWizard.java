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
package gov.redhawk.product.sca_explorer.wizard;

import gov.redhawk.model.sca.ScaDomainManager;
import gov.redhawk.model.sca.ScaWaveform;
import gov.redhawk.model.sca.util.LaunchWaveformJob;
import gov.redhawk.product.sca_explorer.Activator;
import gov.redhawk.sca.ui.ScaUI;

import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.List;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Path;
import org.eclipse.core.runtime.Status;
import org.eclipse.emf.common.util.EList;
import org.eclipse.jface.dialogs.IPageChangingListener;
import org.eclipse.jface.dialogs.PageChangingEvent;
import org.eclipse.jface.operation.IRunnableWithProgress;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.wizard.IWizardPage;
import org.eclipse.jface.wizard.Wizard;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Display;
import org.eclipse.ui.INewWizard;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.statushandlers.StatusManager;

import CF.File;
import CF.FileException;
import CF.InvalidFileName;
import CF.OctetSequenceHolder;

/**
 * This wizard allows the user to select a domain to connect to and then launch
 * any waveform specified in the dom/<StartupWizard.STARTUP_FILE_NAME> file.  If a
 * waveform is already running on the selected domain, the editor for it will be
 * opened on finish.  Otherwise, the available waveforms in the 
 * dom/<StartupWizard.STARTUP_FILE_NAME> file are listed to let the user choose
 * which to launch.
 */

public class StartupWizard extends Wizard implements INewWizard, IPageChangingListener {
	public static final String STARTUP_FILE_NAME = "/cfg/startup.xml";
	private SelectDomainPage domainPage;
	private StartWaveformPage waveformPage;
	private ScaDomainManager mgr;
	private boolean finishable;

	/**
	 * Constructor for StartupWizard.
	 */
	public StartupWizard() {
		super();
		setNeedsProgressMonitor(true);
	}

	/**
	 * Add the pages to the wizard.
	 */
	@Override
	public void addPages() {
		this.domainPage = new SelectDomainPage("Select Domain", this);
		this.waveformPage = new StartWaveformPage("Select Waveform");
		addPage(this.domainPage);
		addPage(this.waveformPage);
	}

	/**
	 * This method is called when 'Finish' button is pressed in the wizard. We
	 * will create an operation and run it using wizard as execution context.
	 */
	@Override
	public boolean performFinish() {
		// Find the selected waveform
		final WaveMapping waveform = this.waveformPage.getSelectedWave();
		if (waveform == null) {
			// If the waveform is null, we're finished from the domain page
			// perform domain specific finishing.
			return finishDomain();
		}
		// Otherwise, launch the selected waveform
		return finishLaunchWaveform(waveform);
	}

	/**
	 * Open the editor for the running waveform.
	 * @return true if the editor was able to be opened, false otherwise
	 */
	private boolean finishDomain() {
		final boolean[] retVal = new boolean[1];
		retVal[0] = false;
		// Have the waveform page load the available waveforms
		initializeWavePage();

		final IWorkbenchPage activePage = PlatformUI.getWorkbench().getActiveWorkbenchWindow().getActivePage();
		final Display display = activePage.getWorkbenchWindow().getShell().getDisplay();
		final WaveMapping[] mappings = this.waveformPage.getAllMappings();
		final List<String> profiles = new ArrayList<String>();

		// Loop through all the mappings and add the profile paths to a list of good waveforms
		for (final WaveMapping map : mappings) {
			profiles.add(map.getWaveformPath());
		}

		try {
			getContainer().run(true, true, new IRunnableWithProgress() {
				public void run(final IProgressMonitor monitor) throws InvocationTargetException, InterruptedException {
					final int RETRY_COUNT = 5;
					monitor.beginTask("Loading Waveform", RETRY_COUNT + 1);
					try {
						// Wait for the factory to be installed
						final List<ScaWaveform> waves = new ArrayList<ScaWaveform>();
						int count = 0;

						// There may not be anything running, so don't wait forever
						while (waves.isEmpty() && (count < RETRY_COUNT)) {
							final EList<ScaWaveform> waveforms = getDomMgr().getWaveforms();
							for (final ScaWaveform w : waveforms) {
								if (profiles.contains(w.getProfile())) {
									waves.add(w);
									break;
								}
							}

							monitor.worked(1);
							count++;
							if (waves.size() == 0) {
								Thread.sleep(500); // SUPPRESS CHECKSTYLE MagicNumber
								if (monitor.isCanceled()) {
									throw new InterruptedException();
								}
							}
						}
						monitor.worked(RETRY_COUNT - count);

						// If there aren't any waves, either nothing was running, or something
						// was running, but we're not supposed to start it.
						if (waves.isEmpty()) {
							final String message;
							if (count == RETRY_COUNT) {
								message = "A non-standard waveform is already running on the domain";
							} else {
								message = "Unable to find waveforms to run on the domain";
							}
							final IStatus status = new Status(IStatus.ERROR, Activator.PLUGIN_ID, message);
							StatusManager.getManager().handle(status);
							display.asyncExec(new Runnable() {
								public void run() {
									StartupWizard.this.domainPage.setErrorMessage(message);
								}
							});
							return;
						}

						final ScaWaveform waveform = waves.get(0);
						// Open the editor on the first waveform that matches in the StartupWizard.STARTUP_FILE_NAME
						display.asyncExec(new Runnable() {
							public void run() {
								try {
									final boolean useUri = !SWT.getPlatform().startsWith("rap");
									ScaUI.openEditorOnEObject(activePage, waveform, useUri);
								} catch (final CoreException e) {
									StatusManager.getManager().handle(e, Activator.PLUGIN_ID);
								}
							}
						});
						retVal[0] = true;
					} finally {
						monitor.done();
					}
				}
			});
		} catch (final InvocationTargetException e) {
			String msg = e.getMessage();
			if (e.getCause() != null) {
				msg = e.getCause().getMessage();
			}
			if (msg == null || msg.length() == 0) {
				msg = "Unknown Error.";
			}
			final IStatus status = new Status(IStatus.ERROR, Activator.PLUGIN_ID, msg, e.getCause());
			StatusManager.getManager().handle(status);
			display.asyncExec(new Runnable() {
				public void run() {
					StartupWizard.this.domainPage.setErrorMessage("Unable to open waveform");
				}
			});
			return false;
		} catch (final InterruptedException e) {
			// PASS
		}
		return retVal[0];
	}

	private boolean finishLaunchWaveform(final WaveMapping waveform) {
		final IWorkbenchPage activePage = PlatformUI.getWorkbench().getActiveWorkbenchWindow().getActivePage();
		final Display display = activePage.getWorkbenchWindow().getShell().getDisplay();
		final boolean autoStart = this.waveformPage.getAutoStart();
		final boolean[] retVal = new boolean[1];
		retVal[0] = false;
		final Object waitLock = new Object();

		final IPath wavePath = new Path(waveform.getWaveformPath());
		final LaunchWaveformJob launchJob = new LaunchWaveformJob(getDomMgr(), waveform.getWaveformName(), wavePath, null, null, autoStart, waitLock);
		try {
			getContainer().run(true, true, new IRunnableWithProgress() {

				public void run(final IProgressMonitor monitor) throws InvocationTargetException, InterruptedException {
					final int WAIT_TIME = 1000;
					monitor.beginTask("Launching waveform " + waitLock, IProgressMonitor.UNKNOWN);
					try {
						launchJob.schedule();

						synchronized (waitLock) {
							while (launchJob.getResult() == null) {
								if (monitor.isCanceled()) {
									launchJob.cancel();
									throw new InterruptedException();
								} else {
									waitLock.wait(WAIT_TIME);
								}
							}
						}

						if (launchJob.getWaveform() != null) {
							display.asyncExec(new Runnable() {

								public void run() {
									try {
										final boolean useUri = !SWT.getPlatform().startsWith("rap");
										ScaUI.openEditorOnEObject(activePage, launchJob.getWaveform(), useUri);
									} catch (final CoreException e) {
										StatusManager.getManager().handle(e, Activator.PLUGIN_ID);
									}
								}

							});
						} else {
							StartupWizard.this.waveformPage.setErrorMessage("Unable to launch selected waveform.");
							StartupWizard.this.waveformPage.setPageComplete(false);
						}
					} finally {
						monitor.done();
					}
				}
			});
		} catch (final InvocationTargetException e) {
			String msg = e.getMessage();
			if (e.getCause() != null) {
				msg = e.getCause().getMessage();
			}

			if (msg == null || msg.length() == 0) {
				msg = "Unknown Error.";
			}
			final IStatus status = new Status(IStatus.ERROR, Activator.PLUGIN_ID, msg, e.getCause());
			StatusManager.getManager().handle(status, StatusManager.SHOW);
			display.asyncExec(new Runnable() {
				public void run() {
					StartupWizard.this.waveformPage.setErrorMessage("Unable to launch selected waveform.");
				}
			});
			return false;
		} catch (final InterruptedException e) {
			return false;
		}

		return retVal[0];
	}

	/**
	 * This returns the connected Domain Manager.
	 * 
	 * @return the DomainManager that's currently connected
	 */
	protected ScaDomainManager getDomMgr() {
		return this.mgr;
	}

	/**
	 * Initializes the Select Waveform page.
	 * 
	 * @return true if initialization was successful
	 */
	public boolean initializeWavePage() {
		final OctetSequenceHolder dataHolder = new OctetSequenceHolder();
		boolean complete = false;
		this.mgr = StartupWizard.this.domainPage.getDomainManager();
		if ((this.mgr != null) && (this.mgr.getFileManager() != null)) {
			IStatus status = null;
			StartupWizard.this.domainPage.setErrorMessage(null);
			try {
				final File cfg = this.mgr.getFileManager().open(StartupWizard.STARTUP_FILE_NAME, true);
				cfg.read(dataHolder, cfg.sizeOf());
				complete = true;
			} catch (final InvalidFileName e) {
				status = new Status(IStatus.ERROR, Activator.PLUGIN_ID, "Unable to find configuration file: " + StartupWizard.STARTUP_FILE_NAME, e);
			} catch (final FileException e) {
				status = new Status(IStatus.ERROR, Activator.PLUGIN_ID, "Unable to open configuration file: " + StartupWizard.STARTUP_FILE_NAME, e);
			} catch (final CF.FilePackage.IOException e) {
				status = new Status(IStatus.ERROR, Activator.PLUGIN_ID, "Unable to read configuration file: " + StartupWizard.STARTUP_FILE_NAME, e);
			}
			if (status != null) {
				StatusManager.getManager().handle(status, StatusManager.SHOW);
				StartupWizard.this.domainPage.setErrorMessage(status.getMessage());
			}
		} else {
			String stat;
			if (this.mgr == null) {
				stat = "No Domain Manager available.";
			} else {
				stat = "Unable to find File Manager for domain '" + this.mgr.getName() + "'";
			}
			StartupWizard.this.domainPage.setErrorMessage(stat);
		}

		this.waveformPage.initialize(dataHolder.value);
		this.domainPage.setPageComplete(complete);

		return complete;
	}

	public void init(final IWorkbench workbench, final IStructuredSelection selection) {
		// Pass
	}

	@Override
	public IWizardPage getNextPage(final IWizardPage page) {
		if ((page == this.domainPage) && this.finishable) {
			return null;
		}
		return super.getNextPage(page);
	}

	@Override
	public boolean canFinish() {
		if (this.finishable) {
			return true;
		}
		return super.canFinish();
	}

	/**
	 * This is used to toggle between the finish and next buttons. This is called after checking if any
	 * applications are installed on the domain
	 * @param running boolean indicating true if there are applications in the domain
	 */
	public void appsRunning(final boolean running) {
		this.finishable = running;
		this.getContainer().updateButtons();
	}

	/**
	 * Callback used to initialize the Start Waveform page before showing.
	 * {@inheritDoc}
	 */
	public void handlePageChanging(final PageChangingEvent event) {
		if (event.getTargetPage() == this.waveformPage) {
			event.doit = initializeWavePage();
		}
	}
}
