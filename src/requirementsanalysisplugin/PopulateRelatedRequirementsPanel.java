package requirementsanalysisplugin;

import functionalanalysisplugin.GraphEdgeInfo;
import functionalanalysisplugin.RequirementSelectionPanel;
import generalhelpers.CreateStructuralElementPanel;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.ComponentOrientation;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Window;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import javax.swing.BorderFactory;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.SwingUtilities;

import com.telelogic.rhapsody.core.*;

public class PopulateRelatedRequirementsPanel extends CreateStructuralElementPanel {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	protected RequirementSelectionPanel m_RequirementsPanel = null;
	protected IRPSequenceDiagram m_SequenceDiagram = null;
	protected Set<IRPRequirement> m_ReqtsForTable;
	protected JCheckBox m_RemoveFromViewCheckBox;

	public static void launchThePanel(
			final String theAppID ){
	
		javax.swing.SwingUtilities.invokeLater(new Runnable() {

			@Override
			public void run() {
				
				JFrame.setDefaultLookAndFeelDecorated( true );
				
				JFrame frame = new JFrame(
						"Populate requirements on diagram" );
				
				frame.setDefaultCloseOperation( JFrame.DISPOSE_ON_CLOSE );
				
				PopulateRelatedRequirementsPanel thePanel = 
						new PopulateRelatedRequirementsPanel( theAppID );

				frame.setContentPane( thePanel );
				frame.pack();
				frame.setLocationRelativeTo( null );
				frame.setVisible( true );
			}
		});
	}
	
	public PopulateRelatedRequirementsPanel(
			String theAppID ) {
		
		super( theAppID );
		
		m_SequenceDiagram = (IRPSequenceDiagram) _context.getSelectedElement();
		m_ReqtsForTable = getReqtsRelatedToInterfaceItemsOn( m_SequenceDiagram );
		
		setLayout( new BorderLayout(10,10) );
		setBorder( BorderFactory.createEmptyBorder( 10, 10, 10, 10 ) );
		
		JPanel thePageStartPanel = new JPanel();
		thePageStartPanel.setLayout( new BoxLayout( thePageStartPanel, BoxLayout.X_AXIS ) );
		
		JButton theSelectAllButton = new JButton( "Select All" );
		theSelectAllButton.setPreferredSize( new Dimension( 75,25 ) );

		theSelectAllButton.addActionListener( new ActionListener() {
			
			@Override
			public void actionPerformed( ActionEvent e ) {
				
				try {
					m_RequirementsPanel.selectedRequirementsIn( m_ReqtsForTable );
												
				} catch (Exception e2) {
					
					_context.error("Error, unhandled exception in PopulateRelatedRequirementsPanel " + 
							"constructor on Select All button action listener");
				}
			}
		});
		
		JButton theDeselectAllButton = new JButton( "De-select All" );
		theDeselectAllButton.setPreferredSize( new Dimension( 75,25 ) );

		theDeselectAllButton.addActionListener( new ActionListener() {
			
			@Override
			public void actionPerformed( ActionEvent e ) {
				
				try {
					m_RequirementsPanel.deselectedRequirementsIn( m_ReqtsForTable );
												
				} catch (Exception e2) {
					
					_context.error("Error, unhandled exception in PopulateRelatedRequirementsPanel " + 
							"constructor on De-select All button action listener");
				}
			}
		});
		
		JButton theAddExistingButton = new JButton( "Select Present" );
		theAddExistingButton.setPreferredSize( new Dimension( 75,25 ) );

		theAddExistingButton.addActionListener( new ActionListener() {
			
			@Override
			public void actionPerformed( ActionEvent e ) {
				
				try {
					m_RequirementsPanel.selectedRequirementsIn( 
							getExistingReqtsOn( m_SequenceDiagram ) );
												
				} catch (Exception e2) {
					
					_context.error("Error, unhandled exception in PopulateRelatedRequirementsPanel " + 
							"constructor on Add Existing button action listener");
				}
			}
		});
		
		JButton theSelectVerificationsButton = new JButton( "Select Verifications" );
		theSelectVerificationsButton.setPreferredSize( new Dimension( 75,25 ) );

		theSelectVerificationsButton.addActionListener( new ActionListener() {
			
			@Override
			public void actionPerformed( ActionEvent e ) {
				
				try {
					
					Set<IRPModelElement> theEls = 
							_context.getElementsThatHaveStereotypedDependenciesFrom( 
									m_SequenceDiagram, "verify" );
					
					Set<IRPRequirement> theReqts = new HashSet<>();
					
					for( IRPModelElement theEl : theEls ){
						
						if( theEl instanceof IRPRequirement ){
							theReqts.add( (IRPRequirement) theEl );
						}
					}
					
					m_RequirementsPanel.selectedRequirementsIn( theReqts );
												
				} catch (Exception e2) {
					
					_context.error("Error, unhandled exception in PopulateRelatedRequirementsPanel " + 
							"constructor on Add Existing button action listener");
				}
			}
		});
		
		m_RemoveFromViewCheckBox = new JCheckBox(
				"Remove existing & re-add the below");
		
		thePageStartPanel.add( theSelectAllButton );
		thePageStartPanel.add( new JLabel("  ") );
		thePageStartPanel.add( theDeselectAllButton );
		thePageStartPanel.add( new JLabel("  ") );
		thePageStartPanel.add( theAddExistingButton );
		thePageStartPanel.add( new JLabel("  ") );
		thePageStartPanel.add( theSelectVerificationsButton );
		thePageStartPanel.add( new JLabel("  ") );		
		thePageStartPanel.add( m_RemoveFromViewCheckBox );
		
		m_RequirementsPanel = new RequirementSelectionPanel( 
				"Requirements related to the events/operations are:",
				m_ReqtsForTable, 
				getExistingReqtsOn( m_SequenceDiagram ) );
		
		add( thePageStartPanel, BorderLayout.PAGE_START );
		add( m_RequirementsPanel, BorderLayout.WEST );
		add( createOKCancelPanel(), BorderLayout.PAGE_END );
	}
		
	public JPanel createOKCancelPanel(){
		
		JPanel thePanel = new JPanel();
		thePanel.setLayout( new FlowLayout() );
		
		JButton theOKButton = new JButton("OK");
		theOKButton.setPreferredSize(new Dimension(75,25));

		theOKButton.addActionListener( new ActionListener() {
			
			@Override
			public void actionPerformed(ActionEvent e) {
				
				try {
					boolean isValid = checkValidity( true );
					
					if (isValid){
						performAction();
						Window dialog = SwingUtilities.windowForComponent( (Component) e.getSource() );
						dialog.dispose();
					}		
												
				} catch (Exception e2) {
					_context.error("Error, unhandled exception in CreateOperationPanel.createOKCancelPanel on OK button action listener");
				}
			}
		});
		
		JButton theCancelButton = new JButton("Cancel");
		theCancelButton.setPreferredSize(new Dimension(75,25));
		
		theCancelButton.addActionListener( new ActionListener() {
			
			@Override
			public void actionPerformed(ActionEvent e) {
				
				try {
					Window dialog = SwingUtilities.windowForComponent( (Component) e.getSource() );
					dialog.dispose();
												
				} catch (Exception e2) {
					_context.error("Error, unhandled exception in CreateOperationPanel.createOKCancelPanel on Cancel button action listener");
				}
			}	
		});
		
		thePanel.setComponentOrientation(ComponentOrientation.LEFT_TO_RIGHT);
		thePanel.add( theOKButton );
		thePanel.add( theCancelButton );
		
		return thePanel;
	}
	
	private static Set<IRPRequirement> getExistingReqtsOn(
			IRPSequenceDiagram theSD ){
								
		Set<IRPRequirement> theExistingReqtsOnSD = new HashSet<IRPRequirement>();
		
		IRPDiagram theDiagram = (IRPDiagram) theSD; //theCollaboration.getMainDiagram();
		
		@SuppressWarnings("unchecked")
		List<IRPGraphElement> theGraphEls = theDiagram.getGraphicalElements().toList();
		
		for( IRPGraphElement theGraphEl : theGraphEls ){
			
			IRPModelElement theModelObject = theGraphEl.getModelObject();
			
			if( theModelObject != null && 
				theModelObject instanceof IRPRequirement ){
				
				theExistingReqtsOnSD.add( (IRPRequirement) theModelObject );
			}
		}
		
		return theExistingReqtsOnSD;
	}
	
	public Set<IRPRequirement> getReqtsRelatedToInterfaceItemsOn(
			IRPSequenceDiagram theSD ){
		
		_context.debug("getReqtsRelatedToInterfaceItemsOn invoked for " + _context.elInfo( theSD ) );
				
		Set<IRPRequirement> theReqtsRelatedToSD = new HashSet<IRPRequirement>();
		
		IRPCollaboration theCollaboration = theSD.getLogicalCollaboration();
		
		@SuppressWarnings("unchecked")
		List<IRPModelElement> theMessagePointEls = theCollaboration.getMessagePoints().toList();
		
		for( IRPModelElement theMessagePointEl : theMessagePointEls ){
			
			IRPMessagePoint theMessagePoint = (IRPMessagePoint) theMessagePointEl;
			IRPMessage theMessage = theMessagePoint.getMessage();
			IRPInterfaceItem theInterfaceItem = theMessage.getFormalInterfaceItem();

			if( theInterfaceItem instanceof IRPEvent || 
				theInterfaceItem instanceof IRPOperation ){
				
				Set<IRPRequirement> theReqtsThatTraceFrom = 
						_context.getRequirementsThatTraceFrom( 
								theInterfaceItem, true );
				
				theReqtsRelatedToSD.addAll( theReqtsThatTraceFrom );
			}
		}
		
		return theReqtsRelatedToSD;
	}
	
	private int getStartXForReqtsOn(
			IRPSequenceDiagram theSD ){
		
		int theStartX = 50;
								
		IRPCollaboration theCollaboration = theSD.getLogicalCollaboration();
		
		@SuppressWarnings("unchecked")
		List<IRPModelElement> theMessagePointEls = 
			theCollaboration.getMessagePoints().toList();
		
		for( IRPModelElement theMessagePointEl : theMessagePointEls ){
			
			IRPMessagePoint theMessagePoint = (IRPMessagePoint)theMessagePointEl;
			IRPMessage theMessage = theMessagePoint.getMessage();
			IRPInterfaceItem theInterfaceItem = theMessage.getFormalInterfaceItem();

			if( theInterfaceItem instanceof IRPEvent || 
				theInterfaceItem instanceof IRPOperation ){
				
				@SuppressWarnings("unchecked")
				List<IRPGraphElement> theGraphEls = 
					theSD.getCorrespondingGraphicElements( theMessage ).toList();

				for( IRPGraphElement theGraphEl : theGraphEls ){
					
					if( theGraphEl instanceof IRPGraphEdge ){
											
						GraphEdgeInfo theEdgeInfo = new GraphEdgeInfo( (IRPGraphEdge) theGraphEl );
						
						int top_left_x = theEdgeInfo.getEndX();
						
						if( top_left_x > theStartX ){
							theStartX = top_left_x;
						}
					}
				}
			}
		}
		
		return theStartX;
	}
	
	private void populateRequirementsFor(
			IRPSequenceDiagram theSD, 
			List<IRPRequirement> theReqtsToPopulate ){
		
		int top_left_x = getStartXForReqtsOn( theSD );
		
		// Get default width and height from the SD
		String theDefaultSize = theSD.getPropertyValue("Format.Requirement.DefaultSize");
		String[] theSplit = theDefaultSize.split(",");
		int theWidth = Integer.parseInt( theSplit[2] );
		int theHeight = Integer.parseInt( theSplit[3] );
		int theXGap = 10;
		
		_context.debug( "Populate requirements invoked for " + _context.elInfo( theSD ) );
		
		Set<IRPRequirement> theReqtsOnDiagram = getExistingReqtsOn( theSD );

		if( m_RemoveFromViewCheckBox.isSelected() ){
			
			IRPDiagram theDiagram = (IRPDiagram)theSD;
			
			for( Iterator<IRPRequirement> iterator = theReqtsOnDiagram.iterator(); iterator.hasNext(); ){
				
				IRPRequirement theReqtOnDiagram = iterator.next();
				
				IRPCollection theGraphElementsToRemove = 
						theDiagram.getCorrespondingGraphicElements( theReqtOnDiagram );
				
				_context.debug("Removing x" + theGraphElementsToRemove.getCount() + " related to " +
						_context.elInfo( theReqtOnDiagram ) + " from " + _context.elInfo( theDiagram ) );
				
				theDiagram.removeGraphElements( theGraphElementsToRemove );
			}
			
			theReqtsOnDiagram = new HashSet<>();
		}
		
		IRPCollaboration theCollaboration = theSD.getLogicalCollaboration();
		
		@SuppressWarnings("unchecked")
		List<IRPModelElement> theMessagePoints = theCollaboration.getMessagePoints().toList();
		
		for( IRPModelElement irpModelElement : theMessagePoints ){
			
			IRPMessagePoint theMessagePoint = (IRPMessagePoint) irpModelElement;
			IRPMessage theMessage = theMessagePoint.getMessage();
			IRPInterfaceItem theInterfaceItem = theMessage.getFormalInterfaceItem();

			if( theInterfaceItem instanceof IRPEvent || 
				theInterfaceItem instanceof IRPOperation ){
				
				@SuppressWarnings("unchecked")
				List<IRPGraphElement> theGraphEls = 
					theSD.getCorrespondingGraphicElements( theMessage ).toList();

				for( IRPGraphElement irpGraphElement : theGraphEls ){
					
					if( irpGraphElement instanceof IRPGraphEdge ){
																	
						GraphEdgeInfo theEdgeInfo = new GraphEdgeInfo( (IRPGraphEdge) irpGraphElement );
						
						int top_left_y = theEdgeInfo.getStartY();
						
						int x = top_left_x + 100;
						
						Set<IRPRequirement> theReqtsThatTraceFrom = 
								_context.getRequirementsThatTraceFrom( theInterfaceItem, true );
						
						for( IRPRequirement theReqt : theReqtsThatTraceFrom ){
							
							// only populate once per diagram, i.e. first instance only
							if( !theReqtsOnDiagram.contains( theReqt ) && 
								 theReqtsToPopulate.contains( theReqt ) ){
								
								_context.debug( "Adding " + _context.elInfo( theReqt ) + " to " + _context.elInfo( theSD ) );
								
								IRPGraphNode grElement = theSD.addNewNodeForElement(
										theReqt, x, top_left_y-20, 10, 10 );
																
		                        grElement.setGraphicalProperty( "Width", String.valueOf( theWidth ) );  
		                        grElement.setGraphicalProperty( "Height", String.valueOf( theHeight ) );
	 
								theReqtsOnDiagram.add( theReqt );
								x = x + theWidth + theXGap;
							}
						}
					}
				}	
			}
		}
	}
	
	protected boolean checkValidity(
			boolean isMessageEnabled ){
		
		return true;
	}

	protected void performAction() {
		
		// do silent check first
		if( checkValidity( false ) ){
			
			List<IRPRequirement> theReqts = m_RequirementsPanel.getSelectedRequirementsList();
			
			populateRequirementsFor(m_SequenceDiagram, theReqts);
			
		} else {
			_context.error("Error in PopulateRelatedRequirementsPanel.performAction, checkValidity returned false");
		}	
	}
}

/**
 * Copyright (C) 2017-2021  MBSE Training and Consulting Limited (www.executablembse.com)

    This file is part of SysMLHelperPlugin.

    SysMLHelperPlugin is free software: you can redistribute it and/or modify
    it under the terms of the GNU General Public License as published by
    the Free Software Foundation, either version 3 of the License, or
    (at your option) any later version.

    SysMLHelperPlugin is distributed in the hope that it will be useful,
    but WITHOUT ANY WARRANTY; without even the implied warranty of
    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
    GNU General Public License for more details.

    You should have received a copy of the GNU General Public License
    along with SysMLHelperPlugin.  If not, see <http://www.gnu.org/licenses/>.
*/
