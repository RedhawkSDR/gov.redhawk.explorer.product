/**
 * REDHAWK HEADER
 *
 * Identification: $Revision: 2231 $
 */
package gov.redhawk.explorer.rcp;

import gov.redhawk.sca.ui.views.ScaExplorer;
import gov.redhawk.ui.views.namebrowser.view.NameBrowserView;

import org.eclipse.ui.IFolderLayout;
import org.eclipse.ui.IPageLayout;
import org.eclipse.ui.IPerspectiveFactory;

/**
 * This perspective is used to provide a simple browser interface to an SCA
 * domain. It should strive for simplicity as the most important characteristic.
 * The UI should be heavily filtered using Activities to present the fewest
 * number of menu items as possible. However, the perspective is not fixed, as
 * this prevents users from maximizing views and such. Instead, all of the views
 * should be set to not be closeable.
 */
public class ScaExplorerPerspective implements IPerspectiveFactory {

	private static final String SCA_EXPLORER_VIEW_ID = ScaExplorer.VIEW_ID;

	private static final String NAMEBROWSER_VIEW_ID = NameBrowserView.ID;

	/** The PDE Error Log view ID. */
	private static final String PDE_ERROR_LOG_VIEW_ID = "org.eclipse.pde.runtime.LogView";

	public void createInitialLayout(final IPageLayout layout) {
		// Editors are placed for free.
		final String editorArea = layout.getEditorArea();

		final IFolderLayout left = layout.createFolder("left", IPageLayout.LEFT, (float) 0.35, editorArea);

		left.addView(ScaExplorerPerspective.SCA_EXPLORER_VIEW_ID);
		left.addView(ScaExplorerPerspective.NAMEBROWSER_VIEW_ID);

		final IFolderLayout bottom = layout.createFolder("bottom", IPageLayout.BOTTOM, (float) 0.60, editorArea);

		bottom.addView(IPageLayout.ID_PROP_SHEET);
		bottom.addView(ScaExplorerPerspective.PDE_ERROR_LOG_VIEW_ID);

		// DON'T USE A FIXED PERSPECTIVE BECAUSE IT DOESN'T ALLOW A VIEW
		// TO BE MAXIMIZED/MINIMIZED.

		// These are so important, don't let the operator close them.
		layout.getViewLayout(ScaExplorerPerspective.SCA_EXPLORER_VIEW_ID).setCloseable(false);
		layout.getViewLayout(ScaExplorerPerspective.NAMEBROWSER_VIEW_ID).setCloseable(false);
		layout.getViewLayout(IPageLayout.ID_PROP_SHEET).setCloseable(false);
		layout.getViewLayout(ScaExplorerPerspective.PDE_ERROR_LOG_VIEW_ID).setCloseable(false);
		// Don't let anything move
		layout.getViewLayout(ScaExplorerPerspective.SCA_EXPLORER_VIEW_ID).setMoveable(false);
		layout.getViewLayout(ScaExplorerPerspective.NAMEBROWSER_VIEW_ID).setMoveable(false);
		layout.getViewLayout(IPageLayout.ID_PROP_SHEET).setMoveable(false);
		layout.getViewLayout(ScaExplorerPerspective.PDE_ERROR_LOG_VIEW_ID).setMoveable(false);
	}
}
