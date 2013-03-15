/**
 * REDHAWK HEADER
 *
 * Identification: $Revision: 7080 $
 */
package gov.redhawk.explorer.rcp.wizard;

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
