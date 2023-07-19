package com.mbsetraining.sysmlhelper.doorsng;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.ArrayList;
import java.util.List;
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

public class UpdateSurrogateRequirementsPanel extends ExecutableMBSEBasePanel{

	/**
	 * 
	 */
	private static final long serialVersionUID = -2463303852702389912L;

	private List<IRPRequirement> _requirementsToUpdate = new ArrayList<IRPRequirement>();
	private List<IRPRequirement> _requirementsThatMatch = new ArrayList<IRPRequirement>();
	private List<IRPRequirement> _requirementsWithNoLinks = new ArrayList<IRPRequirement>();
	private List<IRPRequirement> _requirementsWithUnloadedHyperlinks = new ArrayList<IRPRequirement>();

	public static void main(String[] args) {

		String theRhpAppID = RhapsodyAppServer.getActiveRhapsodyApplication().getApplicationConnectionString();
		UpdateSurrogateRequirementsPanel.launchThePanel( theRhpAppID );
	}

	public static void launchThePanel(
			final String theAppID ){

		javax.swing.SwingUtilities.invokeLater(new Runnable() {

			@Override
			public void run() {

				JFrame.setDefaultLookAndFeelDecorated( true );

				String theCaption = "Update surrogate requirement(s) based on remote requirement changes";

				JFrame frame = new JFrame( theCaption );

				frame.setDefaultCloseOperation( JFrame.DISPOSE_ON_CLOSE );

				UpdateSurrogateRequirementsPanel thePanel = 
						new UpdateSurrogateRequirementsPanel( theAppID );

				frame.setContentPane( thePanel );
				frame.pack();
				frame.setLocationRelativeTo( null );
				frame.setVisible( true );
			}
		});
	}

	public UpdateSurrogateRequirementsPanel(
			String theAppID ){

		super( theAppID );

		List<IRPModelElement> theSelectedEls = _context.getSelectedElements();

		setLayout( new BorderLayout(10,10) );
		setBorder( BorderFactory.createEmptyBorder( 10, 10, 10, 10 ) );

		Box theBox = Box.createVerticalBox();

		determineRequirementsToUpdate( theSelectedEls ); 

		JLabel theLabel;

		if( _requirementsToUpdate.isEmpty() ){

			String msg = "No requirements to update (" +_requirementsThatMatch.size() + " matched, ";
			msg += _requirementsWithUnloadedHyperlinks.size() + " traced to unloaded links, ";
			msg += _requirementsWithNoLinks.size() + " had no links )";

			theLabel = new JLabel( msg );
			theLabel.setAlignmentX(Component.LEFT_ALIGNMENT);
			theBox.add( theLabel );

		} else {

			List<IRPModelElement> theFoundEls = new ArrayList<>( _requirementsToUpdate );			
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

			JLabel theStartLabel = new JLabel("The following " + _requirementsToUpdate.size() + 
					" requirements need updating:\n");

			theStartLabel.setAlignmentX(Component.LEFT_ALIGNMENT);
			theBox.add( theStartLabel );
			theBox.add( theScrollPane );

			JLabel theEndLabel = new JLabel( "Do you want to update their specification text based on remote requirements?\n" );
			theEndLabel.setAlignmentX(Component.LEFT_ALIGNMENT);
			theBox.add( theEndLabel );
		}

		add( theBox, BorderLayout.CENTER );
		add( createOKCancelPanel(), BorderLayout.PAGE_END );
	}

	public void determineRequirementsToUpdate(
			List<IRPModelElement> theSelectedEls ) {

		for( IRPModelElement theSelectedEl : theSelectedEls ){

			@SuppressWarnings("unchecked")
			List<IRPRequirement> theRequirements = theSelectedEl.getNestedElementsByMetaClass( "Requirement", 1 ).toList();

			for( IRPRequirement theRequirement : theRequirements ){
				determineRequirementsToUpdateBasedOn(theRequirement);
			}			
		}
	}

	private void determineRequirementsToUpdateBasedOn(
			IRPRequirement theRequirement ){

		List<IRPModelElement> theRemoteDependsOns = _context.getRemoteDependsOnFor( theRequirement );

		if( theRemoteDependsOns.isEmpty() ) {
			
			if( !_requirementsWithNoLinks.contains( theRequirement ) ) {	
				_context.debug( "Found " + _context.elInfo( theRequirement ) + "'s got no oslc links");
				_requirementsWithNoLinks.add( theRequirement );
			}
		} else {
			
			for (IRPModelElement theRemoteDependsOn : theRemoteDependsOns) {
				
				_context.info( _context.elInfo (theRequirement) + " traces to " + _context.elInfo( theRemoteDependsOn ) + 
						" owned by " + _context.elInfo( theRemoteDependsOn.getOwner() ) );

				if( theRemoteDependsOn instanceof IRPRequirement ){

					IRPRequirement theOSLCRequirement = (IRPRequirement)theRemoteDependsOn;

					String theRemoteSpec = theOSLCRequirement.getSpecification();
					String theSpec = theRequirement.getSpecification();

					if( theSpec.equals( theRemoteSpec ) ){

						if( !_requirementsThatMatch.contains( theRequirement ) ) {	
							_context.debug( "Found " + _context.elInfo( theRequirement ) + "'s spec matches");
							_requirementsThatMatch.add( theRequirement );
						}
					} else {
						
						if( !_requirementsToUpdate.contains( theRequirement ) ) {		
							_context.debug( "Found " + _context.elInfo( theRequirement ) + "'s spec needs updating");
							_requirementsToUpdate.add( theRequirement );
						}
					}

				} else if( theRemoteDependsOn instanceof IRPHyperLink ) {

					if( !_requirementsWithUnloadedHyperlinks.contains( theRequirement ) ) {					
						_context.debug( "Found " + _context.elInfo( theRequirement ) + "'s oslc link is unloaded");
						_requirementsWithUnloadedHyperlinks.add( theRequirement );
					}
				}
			}
		}
	}

	@Override
	protected boolean checkValidity(boolean isMessageEnabled) {
		return true;
	}

	@Override
	protected void performAction() {

		if( checkValidity( false ) ){

			for( IRPRequirement theRequirement : _requirementsToUpdate ){

				List<IRPRequirement> theRemoteRequirements = _context.getRemoteRequirementsFor( theRequirement );

				if( theRemoteRequirements.size()!=1 ) {

					_context.error( "Found " +  theRemoteRequirements.size() + " remote requirements for " + 
							_context.elInfo(theRequirement) + " when expecting 1");

				} else if( theRemoteRequirements.size()==1 ) {

					IRPRequirement theOSLCRequirement = (IRPRequirement)theRemoteRequirements.get(0);

					String theRemoteSpec = theOSLCRequirement.getSpecification();
					String theSpec = theRequirement.getSpecification();

					if( theSpec.equals( theRemoteSpec ) ){

						_context.warning( "Found " + _context.elInfo( theRequirement ) + 
								"'s spec already matches hence no action needed");

					} else {
						_context.info( "Updating " + _context.elInfo( theRequirement ) + "'s spec to '" + theRemoteSpec + "'" );

						theRequirement.setSpecification( theRemoteSpec );
					}
				}
			}
		}
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