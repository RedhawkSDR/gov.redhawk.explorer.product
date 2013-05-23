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
package gov.redhawk.explorer.wizard;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.parsers.SAXParserFactory;

import org.eclipse.jface.viewers.ArrayContentProvider;
import org.eclipse.jface.viewers.ComboViewer;
import org.eclipse.jface.viewers.ISelectionChangedListener;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.jface.viewers.ListViewer;
import org.eclipse.jface.viewers.SelectionChangedEvent;
import org.eclipse.jface.wizard.WizardPage;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;
import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;

/**
 * The Start Waveform page allows the user to select a waveform type and the 
 * specific waveform to launch to initiate processing.
 */

public class StartWaveformPage extends WizardPage {
	private static final String[] NO_WAVEFORM_INPUT = new String[] { "Select Type" };
	private static final String TYPE = "type";
	private static final String WAVEFORM = "waveform";
	private static final String PATH = "path";
	private static final String NAME = "name";

	private ComboViewer waveformType;

	private ListViewer waveformList;

	private final Map<String, List<WaveMapping>> waveMap;

	/**
	 * Constructor for StartWaveformPage.
	 * 
	 * @param pageName name of this WizardPage
	 */
	public StartWaveformPage(final String pageName) {
		super(pageName);
		setTitle(pageName);
		setDescription("Select the Waveform to launch");
		setPageComplete(false);
		this.waveMap = new HashMap<String, List<WaveMapping>>();
	}

	/**
	 * {@inheritDoc}
	 */
	public void createControl(final Composite parent) {
		final Composite container = new Composite(parent, SWT.NULL);
		final GridLayout layout = new GridLayout();
		container.setLayout(layout);
		layout.numColumns = 2;
		Label label = new Label(container, SWT.NULL);
		label.setText("Type:");

		this.waveformType = new ComboViewer(container, SWT.BORDER | SWT.READ_ONLY);
		GridData gd = new GridData(GridData.FILL_HORIZONTAL);
		this.waveformType.getControl().setLayoutData(gd);
		this.waveformType.setContentProvider(new ArrayContentProvider());
		this.waveformType.setLabelProvider(new LabelProvider());
		this.waveformType.addSelectionChangedListener(new ISelectionChangedListener() {
			public void selectionChanged(final SelectionChangedEvent event) {
				// If there is something selected, load the list of available waveforms
				if (!event.getSelection().isEmpty()) {
					final String type = (String) ((IStructuredSelection) event.getSelection()).getFirstElement();
					StartWaveformPage.this.waveformList.setInput(StartWaveformPage.this.waveMap.get(type).toArray());
					StartWaveformPage.this.waveformList.getControl().setEnabled(true);
				} else {
					// Otherwise, remove everything and show nothing
					clearWaveformList();
				}
			}
		});

		label = new Label(container, SWT.NULL);
		label.setText("Waveform:");
		label.setLayoutData(new GridData(SWT.LEFT, SWT.TOP, false, true));

		this.waveformList = new ListViewer(container, SWT.BORDER | SWT.V_SCROLL);
		gd = new GridData(GridData.FILL_BOTH);
		this.waveformList.getControl().setLayoutData(gd);
		this.waveformList.setContentProvider(new ArrayContentProvider());
		this.waveformList.setLabelProvider(new LabelProvider() {
			@Override
			public String getText(final Object element) {
				// Show either a string, or the waveform name
				if (element instanceof String) {
					return element.toString();
				} else if (element instanceof WaveMapping) {
					return ((WaveMapping) element).getWaveformName();
				}
				return super.getText(element);
			}
		});
		this.waveformList.addSelectionChangedListener(new ISelectionChangedListener() {
			public void selectionChanged(final SelectionChangedEvent event) {
				// Selecting a waveform indicates completability of the wizard
				StartWaveformPage.this.setPageComplete(!event.getSelection().isEmpty());
			}
		});
		clearWaveformList();
		setControl(container);
	}

	/**
	 * Tests parses the given data to load the available waveforms.
	 * 
	 * @param data the contents of the $SDRROOT/dom/<StartupWizard.STARTUP_FILE_NAME> file to parse 
	 */
	public void initialize(final byte[] data) {
		this.waveMap.clear();
		if (data != null) {
			try {
				final SAXParserFactory factory = SAXParserFactory.newInstance();
				// The next line parses the file and creates WaveMappings by hooking into the SAX callbacks
				factory.newSAXParser().parse(new ByteArrayInputStream(data), new WaveMappingXMLContentHandler());
				setErrorMessage(null);
			} catch (final ParserConfigurationException e) {
				setErrorMessage("Unable to load all available waveforms");
			} catch (final SAXException e) {
				setErrorMessage("Unable to parse all available waveforms");
			} catch (final IOException e) {
				setErrorMessage("Unable to load all available waveforms");
			}
		}
		// Set the input, show any that were loaded before the exception
		this.waveformType.setInput(StartWaveformPage.this.waveMap.keySet().toArray());
		clearWaveformList();
	}

	/**
	 * Implements the SAX ContentHandler interface to define callback
	 * behavior to parse the XML document and generate WaveMapping's.
	 */
	class WaveMappingXMLContentHandler extends DefaultHandler {
		private String type;
		private String name;
		private String path;

		/**
		 * This reports the occurrence of an actual element.
		 *
		 * @param uri namespace URI this element is associated with, or an empty String
		 * @param localName name of element (with no namespace prefix, if one is present)
		 * @param qName XML 1.0 version of element name:[namespace prefix]:[localName]
		 * @param attributes Attributes list for this element
		 * @throws SAXException when things go wrong
		 */
		@Override
		public void startElement(final String uri, final String localName, final String qName, final Attributes attributes) throws SAXException {
			if (StartWaveformPage.TYPE.equals(qName)) {
				this.type = attributes.getValue(StartWaveformPage.NAME);
				StartWaveformPage.this.waveMap.put(this.type, new ArrayList<WaveMapping>());
				this.name = null;
				this.path = null;
			} else if (StartWaveformPage.WAVEFORM.equals(qName)) {
				this.name = attributes.getValue(StartWaveformPage.NAME);
				this.path = attributes.getValue(StartWaveformPage.PATH);
			}
			super.startElement(uri, localName, qName, attributes);
		}

		/**
		 * Indicates the end of an element
		 *
		 * @param uri <code>String</code> URI of namespace this element is associated with
		 * @param localName <code>String</code> name of element without prefix
		 * @param qName <code>String</code> name of element in XML 1.0 form
		 * @throws <code>SAXException</code> when things go wrong
		 */
		@Override
		public void endElement(final String uri, final String localName, final String qName) throws SAXException {
			if (StartWaveformPage.WAVEFORM.equals(qName)) {
				if (this.type != null && this.name != null && this.path != null) {
					StartWaveformPage.this.waveMap.get(this.type).add(new WaveMapping(this.type, this.name, this.path));
				}
			}
			super.endElement(uri, localName, qName);
		}
	}

	private void clearWaveformList() {
		this.waveformList.getControl().setEnabled(false);
		this.waveformList.setInput(StartWaveformPage.NO_WAVEFORM_INPUT);
		setPageComplete(false);
	}

	/**
	 * This returns the selected WaveMapping object from the list
	 * @return the WaveMapping object that is currently selected
	 */
	public WaveMapping getSelectedWave() {
		return (WaveMapping) ((IStructuredSelection) this.waveformList.getSelection()).getFirstElement();
	}

	/**
	 * Returns whether or not to auto-start the waveform
	 * @return currently always true
	 */
	public boolean getAutoStart() {
		return true;
	}

	/**
	 * This returns all the WaveMapping objects from the {@link StartupWizard.STARTUP_FILE_NAME} file
	 * @return all the WaveMapping objects
	 */
	public WaveMapping[] getAllMappings() {
		final List<WaveMapping> allMaps = new ArrayList<WaveMapping>();
		for (final List<WaveMapping> maps : this.waveMap.values()) {
			allMaps.addAll(maps);
		}
		return allMaps.toArray(new WaveMapping[allMaps.size()]);
	}
}
