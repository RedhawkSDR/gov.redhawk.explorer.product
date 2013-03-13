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

class WaveMapping {
	private final String type;
	private final String waveformName;
	private final String waveformPath;

	public WaveMapping(final String type, final String name, final String path) {
		this.type = type;
		this.waveformName = name;
		this.waveformPath = path;
	}

	public String getType() {
		return this.type;
	}

	public String getWaveformName() {
		return this.waveformName;
	}

	public String getWaveformPath() {
		return this.waveformPath;
	}
}
