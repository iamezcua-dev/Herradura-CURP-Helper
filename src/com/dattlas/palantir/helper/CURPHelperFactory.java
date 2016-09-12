package com.dattlas.palantir.helper;

/*
 * All source code and information in this file is made
 * available under the following licensing terms:
 *
 * Copyright (c) 2009, Palantir Technologies, Inc.
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are
 * met:
 *
 *     * Redistributions of source code must retain the above copyright
 *       notice, this list of conditions and the following disclaimer.
 *
 *     * Redistributions in binary form must reproduce the above
 *       copyright notice, this list of conditions and the following
 *       disclaimer in the documentation and/or other materials provided
 *       with the distribution.
 *
 *     * Neither the name of Palantir Technologies, Inc. nor the names of its
 *       contributors may be used to endorse or promote products derived
 *       from this software without specific prior written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS
 * "AS IS" AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT
 * LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR
 * A PARTICULAR PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT
 * OWNER OR CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL,
 * SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT
 * LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE,
 * DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY
 * THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
 * (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE
 * OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 *
 *
 */

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.Image;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

import javax.imageio.ImageIO;
import javax.swing.Icon;
import javax.swing.ImageIcon;
import javax.swing.JComponent;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.SwingConstants;
import javax.swing.SwingUtilities;

import org.apache.commons.lang.StringUtils;
import org.jdesktop.swingx.util.SwingWorker;

import com.dattlas.palantir.helper.panel.MainPanel;
import com.dattlas.palantir.helper.panel.ResultPanel;
import com.dattlas.palantir.helper.util.Constants;
import com.palantir.api.dataevent.PalantirDataEventType;
import com.palantir.api.ui.component.dialog.OptionDialogs.OptionMode;
import com.palantir.api.workspace.ApplicationContext;
import com.palantir.api.workspace.ApplicationInterface;
import com.palantir.api.workspace.HelperInterface;
import com.palantir.api.workspace.PalantirConnection;
import com.palantir.api.workspace.PalantirFrame;
import com.palantir.api.workspace.PalantirWorkspaceContext;
import com.palantir.api.workspace.SafeAbstractHelperFactory;
import com.palantir.api.workspace.SafeHelperFactory;
import com.palantir.api.workspace.applications.BrowserApplicationInterface;
import com.palantir.api.workspace.applications.ObjectExplorerApplicationInterface;
import com.palantir.client.search.SearchOperator;
import com.palantir.exception.PalantirSearchException;
import com.palantir.services.OperatorType;
import com.palantir.services.impl.search.ISearchQuery;
import com.palantir.services.ptobject.DataSourceRecord;
import com.palantir.services.ptobject.PTObject.SetterStyle;
import com.palantir.services.ptobject.PTObjectContainer;
import com.palantir.services.ptobject.PTObjectContainers;
import com.palantir.services.ptobject.PTObjectType;
import com.palantir.services.ptobject.Property;
import com.palantir.services.ptobject.PropertyType;
import com.palantir.services.ptobject.Role;
import com.palantir.services.search.SearchResultsPager;
import com.palantir.util.Locatables;
import com.palantir.util.paging.ResultsPage;

/**
 * This CURPHelperFactory is a factory which generates the CURPHelper.
 *
 */
public class CURPHelperFactory extends SafeAbstractHelperFactory {
	
	public CURPHelperFactory() {
		super(	"CURPHelper", 												//Title
				new String[] { 
					BrowserApplicationInterface.APPLICATION_URI, 
					ObjectExplorerApplicationInterface.APPLICATION_URI},	//Workspace Applications this helper must be paired with...
				new Integer [] { SwingConstants.VERTICAL },					//Helper orientation
				new Dimension(522, 583),									//Minimum acceptable helper size
				null,														//Keyboard shortcut
				"com.dattlas.CURPHelper");									//The URI for the helper
	}

	/**
	 * <p>Creates an instance of the helper.</p>
	 *
	 * @param palantirContext the Palantir context for this helper
	 * @param application the application content
	 * @return a new helper
	 */
	public HelperInterface createHelper(PalantirWorkspaceContext palantirContext, ApplicationInterface application) {
		return new CURPHelper(this, palantirContext, application);
	}

	protected static class CURPHelper implements HelperInterface, ActionListener, PropertyChangeListener {

		private SafeHelperFactory factory;
		private JTextField nameField;
		
		private JPanel panel;				// Main panel
		private MainPanel panelMain;		// Main helper panel
		private ResultPanel panelResult;	// Results panel
		
		private PalantirWorkspaceContext palantirContext;
		private PalantirFrame palantirFrame;
			
		@Override
		public void propertyChange(PropertyChangeEvent evt) {
			System.out.println("Object: " + evt.getSource().toString() + ", has fired this property: " + evt.getPropertyName());
			switch( evt.getPropertyName() ){
				case MainPanel.SHOW_CURP_DATA:
					SwingUtilities.invokeLater(new Runnable() {
						@Override
						public void run() {
							panel.removeAll();
							panelResult.setData(panelMain.getInformationMap());
							panelResult.setCurpImage(panelMain.getCurpImage());
							panel.add(panelResult, BorderLayout.CENTER);
							SwingUtilities.updateComponentTreeUI(getDisplayComponent());
						}
					});
					
					break;
					
				case ResultPanel.CLEAR_CURP_CONTENTS:
					SwingUtilities.invokeLater(new Runnable() {
						@Override
						public void run() {
							panel.removeAll();
							panelMain.setDropAreaText(MainPanel.DROP_FILE_HERE);
							panel.add(panelMain, BorderLayout.CENTER);
							SwingUtilities.updateComponentTreeUI(getDisplayComponent());
						}
					});
					
					break;
					
			}
		}
		
		public CURPHelper(SafeHelperFactory factory, PalantirWorkspaceContext palantirContext, ApplicationInterface application) {
						
			this.factory = factory;
			this.palantirContext = palantirContext;
	        			
			//Configuring Main panel...
			panel = new JPanel(new BorderLayout());
			panel.setBackground(palantirContext.getColors().getPrimaryBackgroundColor());
									
			panelMain = new MainPanel( palantirContext );
			panelResult = new ResultPanel( palantirContext );
			
			panelMain.addPropertyChangeListener(this);
			panelResult.addPropertyChangeListener(this);
	        
//			//Adding the drag-and-drop enabled *.pdf curp JPanel.
//			final JPanel panelCurp = palantirContext.getUiBuilders().panelBuilder().build();
//			panelCurp.setBorder(BorderFactory.createTitledBorder("CURP Screenshot"));
//			panelCurp.setLayout(new BorderLayout(10, 10));
//			panelCurp.add(labelCurpImage, BorderLayout.CENTER);
//			panelCurp.setSize(251, 133);
//			panelCurp.setPreferredSize( panelCurp.getSize() );
//			panelCurp.addMouseListener(mouseAdapter);
//			topPanel.add(panelCurp, "0,1");
//	        
//			//Adding some buttons with their functionality: CREATE and SEARCH ...
//			JButton buttonCreate = palantirContext.getUiBuilders().buttonBuilder().build();
//			buttonCreate.setText("Create object on Graph...");
//			buttonCreate.setActionCommand("CREATE");
//			buttonCreate.setSize(157, 23);
//			buttonCreate.setPreferredSize(buttonCreate.getSize());
//			topPanel.add(buttonCreate, "1,1");
//			
//			//Adding the CURP info container
//			final JPanel panelInfo = palantirContext.getUiBuilders().panelBuilder().build();
//			panelInfo.setBorder(BorderFactory.createTitledBorder("Información"));
//			panelInfo.setSize(500, 279);
//			panelInfo.setPreferredSize(panelInfo.getSize());
//			panelInfo.addMouseListener(mouseAdapter);
//			initFields();
//			topPanel.add(panelInfo, "0,2,2,2");
			
	        //Adding blank padding components to allow the container to show their specified gap
			panel.add(panelMain, BorderLayout.CENTER);
			panel.add(palantirContext.getUiBuilders().panelBuilder().build() , BorderLayout.NORTH);
			panel.add(palantirContext.getUiBuilders().panelBuilder().build() , BorderLayout.SOUTH);
			panel.add(palantirContext.getUiBuilders().panelBuilder().build() , BorderLayout.WEST);
			panel.add(palantirContext.getUiBuilders().panelBuilder().build() , BorderLayout.EAST);			
			
		}
		
		public String getDefaultPosition() {
			return BorderLayout.EAST;
		}

		public JComponent getDisplayComponent() {
			return panel;
		}

		public Image getFrameIcon() {
			return null;
		}

		public void setOwners(PalantirFrame pFrame, ApplicationContext appContext) {
			this.palantirFrame = pFrame;
		}

		public Icon getIcon() {
			return new ImageIcon(retrieveImage("/curp.png"));
		}

		public SafeHelperFactory getFactory() {
			return factory;
		}

		public String getTitle() {
			return "CURPHelper Helper";
		}

		public void initialize(ApplicationInterface app) {
			// do nothing
		}

		public void dispose(ApplicationInterface arg0) {
			// do nothing
		}

		public void setConstraint(String constraint) {
			// do nothing
		}

		private Image retrieveImage(String name) {
			Image theImage=null;
			try {
				theImage = new ImageIcon(ImageIO.read(CURPHelperFactory.class.getResource(name))).getImage().getScaledInstance(16, 16, Image.SCALE_SMOOTH);
				return theImage;
			} catch (Exception e) {
				e.printStackTrace();
			}
			return theImage;
		}

		public void actionPerformed(ActionEvent e) {
			if(e.getActionCommand().equals("SEARCH")) {
				doSearch();
			}
		}

		/**
		 * Search for a person with the name specified in the text field. Add
		 * results to the Graph.
		 */
		private void doSearch() {

			final String name = this.nameField.getText();
			if (StringUtils.isEmpty(name)) {
				palantirContext.getUiBuilders().optionDialogBuilder(palantirFrame.getFrame(), OptionMode.OK)
					.message("No name specified")
					.show();
				return;
			}

			final PalantirConnection conn = this.palantirContext.getPalantirConnection();

			SwingWorker<Void, Void> worker = new SwingWorker<Void, Void>() {

				@Override
				protected Void doInBackground() throws Exception {

					// new search
					ISearchQuery sq = palantirContext.getSearchFactory().getNewSearchQuery(OperatorType.INTERSECT);

					// by person
					sq.addObjectTypeTerm(palantirContext.getOntology().getObjectTypeByUri(PTObjectType.PERSON_URI), true);

					PropertyType nameType = palantirContext.getOntology().getPropertyTypeByUri(PropertyType.NAME_URI);
					sq.addPropertyTerm(nameType, name, SearchOperator.EQUALS);

					// run the query (get pages of results)
					SearchResultsPager pager = conn.search(sq);

					ResultsPage<PTObjectContainer,PalantirSearchException> currentPage = pager;
					Collection<PTObjectContainer> ptocs = new ArrayList<PTObjectContainer>();
					while (currentPage.moreResultsAvailable()) {
						currentPage = currentPage.getNextPage();
						ptocs.addAll(currentPage.getResults());
					}

					// We don't need to load the ptocs because we're adding to graph by locator

					// if no results, notify user
					if( ptocs.isEmpty() ) {
						palantirContext.getUiBuilders().optionDialogBuilder(palantirFrame.getFrame(), OptionMode.OK)
							.message("No objects found with name: " + name)
							.show();
						return null;
					}

					// add the results to the graph
					palantirContext.getGraph().addObjects(Locatables.getLocatorList(ptocs));

					return null;
				}

			};

			palantirContext.getMonitoredExecutorService().execute(worker);
		}

	    
	}
}
