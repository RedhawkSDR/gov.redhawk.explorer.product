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

import org.eclipse.jface.action.IMenuManager;
import org.eclipse.jface.action.MenuManager;
import org.eclipse.jface.action.Separator;
import org.eclipse.ui.IWorkbenchActionConstants;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.actions.ActionFactory;
import org.eclipse.ui.actions.ActionFactory.IWorkbenchAction;
import org.eclipse.ui.application.ActionBarAdvisor;
import org.eclipse.ui.application.IActionBarConfigurer;

public class ApplicationActionBarAdvisor extends ActionBarAdvisor {
	private IWorkbenchAction exitAction;

	private IWorkbenchAction preferencesAction;

	private IWorkbenchAction helpContentsAction;

	private IWorkbenchAction helpSearchAction;

	private IWorkbenchAction aboutAction;

	public ApplicationActionBarAdvisor(final IActionBarConfigurer configurer) {
		super(configurer);
	}

	@Override
	protected void makeActions(final IWorkbenchWindow window) {
		this.exitAction = ActionFactory.QUIT.create(window);
		register(this.exitAction);

		this.preferencesAction = ActionFactory.PREFERENCES.create(window);
		register(this.preferencesAction);

		this.helpContentsAction = ActionFactory.HELP_CONTENTS.create(window);
		register(this.helpContentsAction);

		this.helpSearchAction = ActionFactory.HELP_SEARCH.create(window);
		register(this.helpSearchAction);

		this.aboutAction = ActionFactory.ABOUT.create(window);
		register(this.aboutAction);
	}

	@Override
	protected void fillMenuBar(final IMenuManager menuBar) {
		final MenuManager fileMenu = new MenuManager("&File", IWorkbenchActionConstants.M_FILE);
		fileMenu.add(new Separator(IWorkbenchActionConstants.MB_ADDITIONS));
		fileMenu.add(this.preferencesAction);
		fileMenu.add(new Separator());
		fileMenu.add(this.exitAction);

		final MenuManager helpMenu = new MenuManager("&Help", IWorkbenchActionConstants.M_HELP);
		helpMenu.add(this.helpContentsAction);
		helpMenu.add(this.helpSearchAction);
		helpMenu.add(new Separator(IWorkbenchActionConstants.MB_ADDITIONS));
		helpMenu.add(new Separator());
		helpMenu.add(this.aboutAction);

		menuBar.add(fileMenu);
		menuBar.add(helpMenu);
	}

}
