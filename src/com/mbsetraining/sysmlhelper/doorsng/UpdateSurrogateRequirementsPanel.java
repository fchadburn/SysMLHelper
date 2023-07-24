package com.mbsetraining.sysmlhelper.doorsng;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JList;
import javax.swing.JOptionPane;
import javax.swing.JScrollPane;
import javax.swing.JTabbedPane;
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

	private List<IRPRequirement> _requirementsInScope = new ArrayList<IRPRequirement>();
	private List<IRPRequirement> _requirementsToUpdate = new ArrayList<IRPRequirement>();
	private List<IRPRequirement> _requirementsThatMatch = new ArrayList<IRPRequirement>();
	private List<IRPRequirement> _requirementsWithNoLinks = new ArrayList<IRPRequirement>();
	private List<IRPRequirement> _requirementsWithUnloadedHyperlinks = new ArrayList<IRPRequirement>();
	private List<IRPRequirement> _remoteRequirementsThatTrace = new ArrayList<IRPRequirement>();
	private List<IRPRequirement> _remoteRequirementsThatDontTrace = new ArrayList<IRPRequirement>();
	private Map<IRPRequirement,IRPRequirement> _remoteRequirementsToEstablishTraceTo = new HashMap<>();  
	private List<IRPRequirement> _requirementsWithNoMatchOrLinks = new ArrayList<IRPRequirement>();

	private IRPPackage _owningPkg;

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

				String theCaption = "Update requirement(s) in model based on remote requirement links";

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

		determineRequirementsToUpdate( theSelectedEls ); 

		if( theSelectedEls.size()==1 ) {

			IRPModelElement theSelectedEl = theSelectedEls.get(0);

			if( theSelectedEl instanceof IRPPackage ) {
				_owningPkg = (IRPPackage)theSelectedEl;
				determineNewRequirementsNeeded( (IRPPackage) theSelectedEl );
			}
		}

		JTabbedPane theTabbedPane = new JTabbedPane();

		theTabbedPane.addTab(
				"Update model (" + _requirementsToUpdate.size() + ")", 
				null, 
				createRequirementsToUpdateBox(), 
				"Requirements to update based on text of remote specification");
		//updatePane.setMnemonicAt(0, KeyEvent.VK_1);

		if( !_remoteRequirementsThatDontTrace.isEmpty() ) {

			theTabbedPane.addTab(
					"New requirements (" + _remoteRequirementsThatDontTrace.size() + ")", 
					null, 
					createRemoteRequirementsThatDontTraceBox(), 
					"Requirements linked to package but not to a requirement");
		}
		//updatePane.setMnemonicAt(0, KeyEvent.VK_1);

		// Only show this if not empty, as a warning
		if( !_requirementsWithNoMatchOrLinks.isEmpty() ) {

			theTabbedPane.addTab(
					"Info-only: No-links (" + _requirementsWithNoMatchOrLinks.size() + ")", 
					null, 
					createRequirementsWithNoLinksBox(), 
					"Requirements without links to remote requirements");
			//updatePane.setMnemonicAt(0, KeyEvent.VK_1);
		}
		
		if( !_remoteRequirementsToEstablishTraceTo.isEmpty() ) {

			theTabbedPane.addTab(
					"Match with existing (" + _remoteRequirementsToEstablishTraceTo.size() + ")", 
					null, 
					createRequirementsToEstablishTraceToBox(), 
					"Requirements in model that can be matched with external requirements");
			//updatePane.setMnemonicAt(0, KeyEvent.VK_1);
		}

		if( !_requirementsWithUnloadedHyperlinks.isEmpty() ) {

			theTabbedPane.addTab(
					"Unloaded links (" + _requirementsWithUnloadedHyperlinks.size() + ")", 
					null, 
					createBoxBasedOn( _requirementsWithUnloadedHyperlinks ), 
					"Requirements with HyperLinks to unloaded requirements");
		}

		add( theTabbedPane, BorderLayout.CENTER );
		
		int updateCount = _requirementsToUpdate.size() + _remoteRequirementsThatDontTrace.size() + _remoteRequirementsToEstablishTraceTo.size();
		
		if( updateCount == 0 ) {
			
			add( createOKCancelPanel(), BorderLayout.PAGE_END );

		} else {			
			add( createUpdateCancelPanelWith( "Update model (" + updateCount + ")" ), BorderLayout.PAGE_END );
		}
	}

	private Box createRequirementsToUpdateBox() {

		Box theBox = Box.createVerticalBox();

		JScrollPane theScrollPane = createDoubleClickableScrollPaneFor( new ArrayList<>( _requirementsToUpdate ) );

		Component theStartLabel;

		String msg = "";

		if( _requirementsToUpdate.size() == 0 ) {
			msg += "None of the " + _requirementsInScope.size() +" requirements in scope require their specification text \n" + 
					"to be updated due to changes to their remote counterpart. \n\nOf these:\n" + 
					_requirementsThatMatch.size() + " requirements have matching specification text\n";
			
			if( _requirementsWithNoLinks.size() > 0 ) {				
				msg += _requirementsWithNoLinks.size() + " have no remote requirement links";
			}

		} else if( _requirementsToUpdate.size() == 1 ) {
			msg += "There is 1 requirement that needs updating because its specification text doesn't match \n(double-click to locate in browser). ";

		} else {
			msg += "There are " + _requirementsToUpdate.size() + " requirements that need updating because their specification text doesn't match. ";
			msg += "Double-click to locate in browser. ";

		}

		int unloadedCount = _requirementsWithUnloadedHyperlinks.size();

		if( unloadedCount > 0 ) {
			msg += "\n\nHowever, there are " + unloadedCount + " unloaded requirements. \n";
			msg += "It is strongly recommended to resolve these before proceeding";
		}

		theStartLabel = createPanelWithTextCentered( msg );

		theBox.add( theStartLabel );

		if( _requirementsToUpdate.size() > 0 ) {
			theBox.add( theScrollPane );
		}

		return theBox;
	}
	
	private Box createRequirementsWithNoLinksBox() {

		Box theBox = Box.createVerticalBox();

		JScrollPane theScrollPane = createDoubleClickableScrollPaneFor( new ArrayList<>( _requirementsWithNoMatchOrLinks ) );

		Component theStartLabel;

		String msg = "";
		
		int count = _requirementsWithNoMatchOrLinks.size();

		if( count == 0 ) {
			msg += "There are no requirements without remote requirement links.";

		} else if( count == 1 ) {
			
			if( _owningPkg instanceof IRPPackage ) {
				msg += "There is 1 requirement in " + _owningPkg.getName() + "\n without a remote requirement link ";
			} else {
				msg += "There is 1 requirement without a remote requirement link \n";
			}
			
			msg += "nor matching remote specification \n";
			msg += "(double-click to locate in browser).";

		} else { // plural
			
			if( _owningPkg instanceof IRPPackage ) {

				msg += "There are " + count + " requirements in " + _owningPkg.getName() + "\n without remote requirement links ";
			} else {
				msg += "There are " + count + " requirements without remote requirement links \n";

			}

			msg += "nor matching remote specification \n";
			msg += "(double-click to locate in browser).";
		}

		theStartLabel = createPanelWithTextCentered( msg );

		theBox.add( theStartLabel );

		if( count > 0 ) {
			theBox.add( theScrollPane );
		}

		return theBox;
	}
	
	private Box createRequirementsToEstablishTraceToBox() {

		Box theBox = Box.createVerticalBox();
		
		List<IRPRequirement> theReqts = new ArrayList<>( _remoteRequirementsToEstablishTraceTo.keySet() );

		JScrollPane theScrollPane = createDoubleClickableScrollPaneFor( new ArrayList<>( theReqts ) );

		Component theStartLabel;

		String msg = "";
		
		int count = theReqts.size();

		if( count == 0 ) {
			msg += "There are no requirements to establish trace relations to.";

		} else if( count == 1 ) {
			
			if( _owningPkg instanceof IRPPackage ) {
				msg += "There is 1 requirement in " + _owningPkg.getName() + "\nto establish a trace relation to ";
			} else {
				msg += "There is 1 requirement to establish a trace relation to ";
			}
			
			msg += "because it has a matching specification text \n";
			msg += "(double-click to locate in browser).";

		} else { // plural
			
			if( _owningPkg instanceof IRPPackage ) {

				msg += "There are " + count + " requirements in " + _owningPkg.getName() + " to establish a trace relations to ";
			} else {
				msg += "There are " + count + " requirements to establish trace relations to ";
			}

			msg += "because it has a matching specification text \n";
			msg += "(double-click to locate in browser).";
		}

		theStartLabel = createPanelWithTextCentered( msg );

		theBox.add( theStartLabel );

		if( count > 0 ) {
			theBox.add( theScrollPane );
		}

		return theBox;
	}

	private Box createRemoteRequirementsThatDontTraceBox() {

		Box theBox = Box.createVerticalBox();

		JScrollPane theScrollPane = createDoubleClickableScrollPaneFor( new ArrayList<>( _remoteRequirementsThatDontTrace ) );

		Component theStartLabel;

		String msg = "";
		
		int count = _remoteRequirementsThatDontTrace.size();

		if( _owningPkg == null ) {
			msg += "The remote requirements check works by looking for links from the package and \n";
			msg += "seeing if there are any requirements that are not linked in it, hence only works \n";
			msg += "if a package is selected and has remote requirement links";

		} else if( count == 0 ) {
			msg += "There are no remote requirements traced to " + _owningPkg.getName() + " that don't trace to requirement in it.";

		} else if( count == 1 ) {
			
			msg += _owningPkg.getName() + " has a link to 1 remote requirement that has no corresponding \n" + 
						"requirement in the package and hence will be created ";

			msg += "(double-click to locate in browser).";

		} else { // plural
			
			msg += _owningPkg.getName() + " has a link to " + count + " remote requirements that have no corresponding \n" + 
					"requirements in the package and hence will be created";

			msg += "(double-click to locate in browser).";
		}

		theStartLabel = createPanelWithTextCentered( msg );

		theBox.add( theStartLabel );

		if( count > 0 ) {
			theBox.add( theScrollPane );
		}

		return theBox;
	}
	
	private Box createBoxBasedOn( 
			List<IRPRequirement> theRequirements ) {

		Box theBox = Box.createVerticalBox();

		List<IRPModelElement> theFoundEls = new ArrayList<>( theRequirements );			
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

		theBox.add( theScrollPane );

		return theBox;
	}

	public void determineRequirementsToUpdate(
			List<IRPModelElement> theSelectedEls ) {

		_requirementsInScope = getNestedRequirementsFor( theSelectedEls );

		for( IRPRequirement theRequirement : _requirementsInScope ){
			determineRequirementsToUpdateBasedOn(theRequirement);
		}
	}

	@SuppressWarnings("unchecked")
	private List<IRPRequirement> getNestedRequirementsFor(
			List<IRPModelElement> theSelectedEls ){

		Set<IRPRequirement> theNestedRequirements = new LinkedHashSet<IRPRequirement>();

		for( IRPModelElement theSelectedEl : theSelectedEls ){

			theNestedRequirements.addAll( theSelectedEl.
					getNestedElementsByMetaClass( "Requirement", 1 ).toList() );
		}

		return new ArrayList<>( theNestedRequirements );
	}

	public void determineNewRequirementsNeeded(
			IRPPackage theRequirementPkg ) {

		List<IRPRequirement> theMissingRemoteReqts = new ArrayList<>();
		
		Set<IRPRequirement> theExpectedRemoteReqts = _context.
				getRemoteRequirementsFor( theRequirementPkg );

		for( IRPRequirement theExpectedRemoteReqt : theExpectedRemoteReqts ){

			if( !_remoteRequirementsThatTrace.contains( theExpectedRemoteReqt )) {
				_context.debug( "Found that remote " + _context.elInfo( theExpectedRemoteReqt ) + " doesn't have traceability yet" );
				theMissingRemoteReqts.add( theExpectedRemoteReqt );
			}
		}

		for( IRPRequirement theReqt : _requirementsWithNoLinks ){
			
			String theSpec = theReqt.getSpecification();
			List<IRPRequirement> theMatchedReqts = new ArrayList<>(); 
			
			theMatchedReqts.addAll( _context.getRequirementsThatMatch( theSpec, theExpectedRemoteReqts ) );

			if( theMatchedReqts.size() == 1 ) {
				
				IRPRequirement theMatchedReqt = theMatchedReqts.get( 0 );
				
				_context.debug( "Found that spec of unlinked " + _context.elInfo( theReqt ) + " matches remote " + _context.elInfo( theMatchedReqt ) );
				
				if( _requirementsWithNoLinks.contains( theReqt ) ){
					
					_remoteRequirementsToEstablishTraceTo.put( theReqt,  theMatchedReqt );			

				} else {
					_context.warning( "Match found " + _context.elInfo( theReqt ) + " has match to remote but is already linked to a different requirement" );
				}
				
			} else if( theMatchedReqts.size() > 1 ) {

				_context.warning( "Match found " + _context.elInfo( theReqt ) + " has match to " + theMatchedReqts.size() + " hence don't know which one to choose" );

			} else {

				_requirementsWithNoMatchOrLinks.add( theReqt );
			}
		}
		
		
		for( IRPRequirement theMissingRemoteReqt : theMissingRemoteReqts ){
			
			if( !_remoteRequirementsToEstablishTraceTo.containsValue( theMissingRemoteReqt ) ) {
				_remoteRequirementsThatDontTrace.add( theMissingRemoteReqt );
			}
		}

		int size = _remoteRequirementsToEstablishTraceTo.keySet().size();
		
		_context.debug ( "determineNewRequirementsNeeded found " + size + " unlinked remote reqts have matches " + 
				"to requirements with same spec text and " +  _remoteRequirementsThatDontTrace.size() + " requirements do not" );
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

			for( IRPModelElement theRemoteDependsOn : theRemoteDependsOns ){

				_context.info( _context.elInfo ( theRequirement ) + " traces to " + _context.elInfo( theRemoteDependsOn ) + 
						" owned by " + _context.elInfo( theRemoteDependsOn.getOwner() ) );

				if( theRemoteDependsOn instanceof IRPRequirement ){

					_remoteRequirementsThatTrace.add( (IRPRequirement) theRemoteDependsOn );

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
				updateRequirementBasedOnRemoteSpecification( theRequirement );
			}
			
			for( IRPRequirement theRequirement : _remoteRequirementsThatDontTrace ){
				createRequirementBasedOn( theRequirement );
			}
			
		    for (Map.Entry<IRPRequirement, IRPRequirement> entry : _remoteRequirementsToEstablishTraceTo.entrySet()) {

		    	IRPRequirement fromReqt = entry.getKey();
		    	IRPRequirement toReqt = entry.getValue();
		    	
		    	_context.establishTraceRelationFrom( fromReqt, toReqt );
		    }
		}
	}

	private void updateRequirementBasedOnRemoteSpecification(
			IRPRequirement theRequirement ){
		
		_context.debug( "updateRequirementBasedOnRemoteSpecification was invoked for " + _context.elInfo( theRequirement ) );

		List<IRPRequirement> theRemoteRequirements = new ArrayList<>( _context.getRemoteRequirementsFor( theRequirement ) );

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
	
	private void createRequirementBasedOn(
			IRPRequirement theRemoteRequirement ){
		
		//_context.info( "createRequirementBasedOnRemoteLink was invoked for " + _context.elInfo( theRemoteRequirement ) );
		
		String theRemoteName = theRemoteRequirement.getName();
		String theRemoteSpec = theRemoteRequirement.getSpecification();
		String theRemoteID = theRemoteRequirement.getRequirementID();

		//_context.info( "theRemoteName = " + theRemoteName);
		//_context.info( "theRemoteSpec = " + theRemoteSpec);
		//_context.info( "theRemoteID = " + theRemoteID);
		
		String theProposedName = theRemoteName.replaceAll( theRemoteID + ": ", "" );
		theProposedName = theRemoteID + " " + theProposedName;
		theProposedName = theProposedName.replaceAll( "\\.", "" );

		int maxLength = 50;

		if (theProposedName.length() <= maxLength) {
			theProposedName = theProposedName.trim();
		} else {
			theProposedName = theProposedName.substring(0, maxLength).trim();
		}
		
		//_context.info( "theProposedName = " + theProposedName);

		theProposedName = _context.determineUniqueNameBasedOn(theProposedName, "Requirement", _owningPkg);
		
		_context.info( "Creating requirement called " + theProposedName + " with spec '" + theRemoteSpec + "'" );

		IRPRequirement theRequirement = (IRPRequirement) _owningPkg.addNewAggr( "Requirement", theProposedName );
		theRequirement.setSpecification( theRemoteSpec );
		theRequirement.setRequirementID( theRemoteID );
		
		if( _owningPkg.getUserDefinedMetaClass().equals( _context.REQTS_ANALYSIS_REQUIREMENT_PACKAGE ) ) {
			
			List<IRPStereotype> theStereotypes = _context.getMoveToStereotypes( _owningPkg );
			
			for( IRPStereotype theStereotype : theStereotypes ){
				theRequirement.addSpecificStereotype( theStereotype );							
			}
			
			// Save required by 9.0.1 SR1 to get GUI to update
			//modelElement.getProject().save();
		}
		
		theRequirement.addRemoteDependencyTo( theRemoteRequirement, "Trace" ); 
		

		//_context.info( "------------------");
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