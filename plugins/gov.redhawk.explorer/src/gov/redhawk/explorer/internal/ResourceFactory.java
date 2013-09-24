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
package gov.redhawk.explorer.internal;

import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.IWorkspaceRoot;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.IAdaptable;
import org.eclipse.core.runtime.Path;
import org.eclipse.ui.IElementFactory;
import org.eclipse.ui.IMemento;
import org.eclipse.ui.IPersistableElement;

/**
 * The ResourceFactory is used to save and recreate an IResource object. As
 * such, it implements the IPersistableElement interface for storage and the
 * IElementFactory interface for recreation.
 * 
 * @see IMemento
 * @see IPersistableElement
 * @see IElementFactory
 */
public class ResourceFactory implements IElementFactory, IPersistableElement {

	// These persistence constants are stored in XML.  Do not
	// change them.
	private static final String TAG_PATH = "path"; //$NON-NLS-1$

	private static final String TAG_TYPE = "type"; //$NON-NLS-1$

	private static final String FACTORY_ID = "org.eclipse.ui.internal.model.ResourceFactory"; //$NON-NLS-1$

	// IPersistable data.
	private IResource res;

	/**
	 * Create a ResourceFactory. This constructor is typically used for our
	 * IElementFactory side.
	 */
	public ResourceFactory() {
	}

	/**
	 * Create a ResourceFactory. This constructor is typically used for our
	 * IPersistableElement side.
	 * 
	 * @param input the resource of this factory
	 */
	public ResourceFactory(final IResource input) {
		this.res = input;
	}

	/**
	 * @see IElementFactory
	 */
	@Override
	public IAdaptable createElement(final IMemento memento) {
		// Get the file name.
		final String fileName = memento.getString(ResourceFactory.TAG_PATH);
		if (fileName == null) {
			return null;
		}

		final IWorkspaceRoot root = ResourcesPlugin.getWorkspace().getRoot();
		final String type = memento.getString(ResourceFactory.TAG_TYPE);
		if (type == null) {
			// Old format memento. Create an IResource using findMember.
			// Will return null for resources in closed projects.
			this.res = root.findMember(new Path(fileName));
		} else {
			final int resourceType = Integer.parseInt(type);

			if (resourceType == IResource.ROOT) {
				this.res = root;
			} else if (resourceType == IResource.PROJECT) {
				this.res = root.getProject(fileName);
			} else if (resourceType == IResource.FOLDER) {
				this.res = root.getFolder(new Path(fileName));
			} else if (resourceType == IResource.FILE) {
				this.res = root.getFile(new Path(fileName));
			}
		}
		return this.res;
	}

	/**
	 * @see IPersistableElement
	 */
	@Override
	public String getFactoryId() {
		return ResourceFactory.FACTORY_ID;
	}

	/**
	 * @see IPersistableElement
	 */
	@Override
	public void saveState(final IMemento memento) {
		memento.putString(ResourceFactory.TAG_PATH, this.res.getFullPath().toString());
		memento.putString(ResourceFactory.TAG_TYPE, Integer.toString(this.res.getType()));
	}
}
