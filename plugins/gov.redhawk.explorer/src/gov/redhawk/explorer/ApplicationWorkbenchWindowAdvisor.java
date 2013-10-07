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

import gov.redhawk.sca.ui.ScaUiPlugin;

import org.eclipse.jface.action.IContributionItem;
import org.eclipse.jface.action.IMenuManager;
import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.swt.graphics.Point;
import org.eclipse.ui.application.ActionBarAdvisor;
import org.eclipse.ui.application.IActionBarConfigurer;
import org.eclipse.ui.application.IWorkbenchWindowConfigurer;
import org.eclipse.ui.application.WorkbenchWindowAdvisor;

public class ApplicationWorkbenchWindowAdvisor extends WorkbenchWindowAdvisor {

	private static final String KEY_LAST_SCA_EXPLORER_VIEW_ID = "lastScaExplorerViewId";

	public ApplicationWorkbenchWindowAdvisor(final IWorkbenchWindowConfigurer configurer) {
		super(configurer);
	}

	@Override
	public ActionBarAdvisor createActionBarAdvisor(final IActionBarConfigurer configurer) {
		return new ApplicationActionBarAdvisor(configurer);
	}

	@Override
	public void preWindowOpen() {
		final IWorkbenchWindowConfigurer configurer = getWindowConfigurer();
		configurer.setInitialSize(new Point(1024, 768)); // SUPPRESS CHECKSTYLE MagicNumber
		configurer.setShowCoolBar(false);
		configurer.setShowStatusLine(true);
		configurer.setShowFastViewBars(false);
		configurer.setShowProgressIndicator(true);
		configurer.setTitle("SCA Explorer");
	}

	@Override
	public void postWindowOpen() {
		super.postWindowOpen();
		final IMenuManager menuManager = getWindowConfigurer().getActionBarConfigurer().getMenuManager();
		final IContributionItem[] menuItems = menuManager.getItems();

		for (final IContributionItem item : menuItems) {
			if ("org.eclipse.ui.run".equals(item.getId())) {
				menuManager.remove(item);
			}
		}
		menuManager.update(true);
		
		/**
		 * The property that specifies single-domain or multi-domain SCA Explorer view is tested only when the
		 * perspective is initialized. So we reset the perspective if the property has changed since the
		 * last time the application was launched.
		 */
		IPreferenceStore prefs = ScaUiPlugin.getDefault().getScaPreferenceStore();
		boolean singleDomain = Boolean.valueOf(System.getProperty(ScaExplorerPerspective.PROP_SINGLE_DOMAIN_EXPLORER));
		String lastExplorerViewId = prefs.getString(KEY_LAST_SCA_EXPLORER_VIEW_ID);
		String currentExplorerViewId = 
				(singleDomain) ?  ScaExplorerPerspective.SCA_EXPLORER_SD_VIEW_ID :  ScaExplorerPerspective.SCA_EXPLORER_VIEW_ID;
		if (lastExplorerViewId == null) {
			getWindowConfigurer().getWindow().getActivePage().resetPerspective();
			prefs.setValue(KEY_LAST_SCA_EXPLORER_VIEW_ID, currentExplorerViewId);
		} else {
			if (!lastExplorerViewId.equals(currentExplorerViewId)) {
				getWindowConfigurer().getWindow().getActivePage().resetPerspective();
				prefs.setValue(KEY_LAST_SCA_EXPLORER_VIEW_ID, currentExplorerViewId);
			}
		}
	}
}
