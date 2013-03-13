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
package gov.redhawk.product.sca_explorer;

import org.eclipse.jface.action.IContributionItem;
import org.eclipse.jface.action.IMenuManager;
import org.eclipse.swt.graphics.Point;
import org.eclipse.ui.application.ActionBarAdvisor;
import org.eclipse.ui.application.IActionBarConfigurer;
import org.eclipse.ui.application.IWorkbenchWindowConfigurer;
import org.eclipse.ui.application.WorkbenchWindowAdvisor;

public class ApplicationWorkbenchWindowAdvisor extends WorkbenchWindowAdvisor {

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
	}
}
