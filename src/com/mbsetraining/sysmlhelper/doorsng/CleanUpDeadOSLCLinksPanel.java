package com.mbsetraining.sysmlhelper.doorsng;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JOptionPane;
import javax.swing.JScrollPane;
import javax.swing.ListSelectionModel;
import javax.swing.ScrollPaneConstants;

import com.mbsetraining.sysmlhelper.common.NamedElementMap;
import com.mbsetraining.sysmlhelper.executablembse.ExecutableMBSEBasePanel;
import com.telelogic.rhapsody.core.*;

public class CleanUpDeadOSLCLinksPanel extends ExecutableMBSEBasePanel{


	/**
	 * 
	 */
	private static final long serialVersionUID = -5148201310026260659L;
	private List<IRPDependency> _foundDependencies = new ArrayList<IRPDependency>();
	
	public static void main(String[] args) {
		
		String theRhpAppID = RhapsodyAppServer.getActiveRhapsodyApplication().getApplicationConnectionString();
		CleanUpDeadOSLCLinksPanel.launchThePanel( theRhpAppID );
	}
	
	public static void launchThePanel(
			final String theAppID ){

		javax.swing.SwingUtilities.invokeLater(new Runnable() {

			@Override
			public void run() {
				
				JFrame.setDefaultLookAndFeelDecorated( true );

				String theCaption = "Find and delete OSLC links to unresolved requirements?";
				
				JFrame frame = new JFrame( theCaption );
				
				frame.setDefaultCloseOperation( JFrame.DISPOSE_ON_CLOSE );

				CleanUpDeadOSLCLinksPanel thePanel = 
						new CleanUpDeadOSLCLinksPanel( theAppID );

				frame.setContentPane( thePanel );
				frame.pack();
				frame.setLocationRelativeTo( null );
				frame.setVisible( true );
			}
		});
	}
	
	public CleanUpDeadOSLCLinksPanel(
			String theAppID ){
		
		super( theAppID );
		
		List<IRPModelElement> theSelectedEls = _context.getSelectedElements();
		
		setLayout( new BorderLayout(10,10) );
		setBorder( BorderFactory.createEmptyBorder( 10, 10, 10, 10 ) );
		
		Box theBox = Box.createVerticalBox();

		Set<IRPDependency> theCandidateReqts = 
				buildUnresolvedOSLCLinks( theSelectedEls ); 
		
		_foundDependencies = new ArrayList<>( theCandidateReqts );
		
		JLabel theLabel;
		
		if( _foundDependencies.isEmpty() ){
			theLabel = new JLabel("There are no unresolved OSLC links");
			theLabel.setAlignmentX(Component.LEFT_ALIGNMENT);
			theBox.add( theLabel );
			
		} else {
			
			List<IRPModelElement> theFoundEls = new ArrayList<>( _foundDependencies );			
			final NamedElementMap theNamedElMap = new NamedElementMap( theFoundEls );
			
			Object[] dataList = theNamedElMap.getFullNamesIn();

			JList<Object> list = new JList<>( dataList );
			list.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);

			list.addMouseListener(new MouseAdapter() {
			    public void mouseClicked(MouseEvent evt) {
			        @SuppressWarnings("rawtypes")
					JList list = (JList)evt.getSource();
			        
			        if (evt.getClickCount() >= 2) {

			            // Double-click detected
			            int index = list.locationToIndex(evt.getPoint());
			            
			            IRPModelElement theElement = theNamedElMap.getElementAt(index);
			            
			            if( theElement == null ){
			            	
			    			JDialog.setDefaultLookAndFeelDecorated(true);
			    			
			    			JOptionPane.showMessageDialog(
			    					null, 
			    					"Element no longer exists", 
			    					"Warning",
			    					JOptionPane.WARNING_MESSAGE);
			            	
			            } else {
			            	theElement.highLightElement();
			            }   
			             
			            _context.debug( _context.elInfo( theElement ) + " was double-clicked" );
			        }
			    }
			});
			
			JScrollPane theScrollPane = new JScrollPane(list);
			theScrollPane.setBounds(1,1,16,58);
			
		    theScrollPane.setVerticalScrollBarPolicy( ScrollPaneConstants.VERTICAL_SCROLLBAR_ALWAYS);
		    
		    JLabel theStartLabel = new JLabel("The following " + _foundDependencies.size() + 
		    		" dependencies are unresolved:\n");
		    
		    theStartLabel.setAlignmentX(Component.LEFT_ALIGNMENT);
			theBox.add( theStartLabel );
			theBox.add( theScrollPane );
			
			 JLabel theEndLabel = new JLabel( "Do you want to delete them from the project?\n" );
			    theEndLabel.setAlignmentX(Component.LEFT_ALIGNMENT);
				theBox.add( theEndLabel );
		}
		
		add( theBox, BorderLayout.CENTER );
		add( createOKCancelPanel(), BorderLayout.PAGE_END );
	}
	
	public Set<IRPDependency> buildUnresolvedOSLCLinks(
			List<IRPModelElement> theSelectedEls ) {
		
		Set<IRPDependency> theReqts = new HashSet<IRPDependency>();
		
		for (IRPModelElement theSelectedEl : theSelectedEls) {
			
			IRPModelElement theElementToSearchUnder = theSelectedEl;
			
			if( theSelectedEl instanceof IRPActivityDiagram ){	
				theElementToSearchUnder = theElementToSearchUnder.getOwner();
			}
			
			@SuppressWarnings("unchecked")
			List<IRPDependency> theDependencies = theElementToSearchUnder.getNestedElementsByMetaClass( "Dependency", 1 ).toList();
								
			for( IRPDependency theDependency : theDependencies ){
					
				IRPModelElement theDependsOn = theDependency.getDependsOn();
				
				//_context.info( "theDependent is " + theDependent );
				//_context.info( "theDependsOn is " + theDependsOn );

				if( theDependsOn instanceof IRPHyperLink ) {
					
					IRPHyperLink theHyperLink = (IRPHyperLink)theDependsOn;
						
					_context.info( "Dependency to Hyperlink: " + theHyperLink.getURL() );
					theReqts.add( theDependency );
				}
			}			
		}

		return theReqts;
	}

	@Override
	protected boolean checkValidity(boolean isMessageEnabled) {
		return true;
	}

	@Override
	protected void performAction() {
		if( checkValidity( false ) ){
		
			for( IRPDependency theDependency : _foundDependencies ){
				
				try {
					deleteDependency( theDependency );	
					
				} catch( Exception e ){
					_context.info( "CleanUpDeadOSLCLinksPanel, unexpected exception while deleting, e=" + e.getMessage() );
				}			
			}
		}
	}

	private void deleteDependency( 
			IRPDependency theDependency ){
		
		_context.info( "Deleting " + _context.elInfo( theDependency ) + " from project" );
		
		IRPModelElement theDependsOn = theDependency.getDependsOn();
		
		@SuppressWarnings("unchecked")
		List<IRPModelElement> theReferences = theDependsOn.getReferences().toList();
		
		for (IRPModelElement theReference : theReferences) {
			
			if( theReference instanceof IRPDiagram ) {
				
				IRPDiagram theDiagram = (IRPDiagram)theReference;

				Set<IRPGraphElement> theSetOfGraphEls = 
						_context.getGraphElementsFor(theDependsOn, theDiagram);
				
				IRPCollection theElsToRemove = _context.createNewCollection();
				
				for (IRPGraphElement theGraphEl : theSetOfGraphEls) {
					theElsToRemove.addGraphicalItem( theGraphEl );
				}
				
				theDiagram.removeGraphElements(theElsToRemove);
			}
		}
		
		theDependency.deleteFromProject();
	}
}

/**
 * Copyright (C) 2023  MBSE Training and Consulting Limited (www.executablembse.com)

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