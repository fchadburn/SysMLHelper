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
import javax.swing.JList;
import javax.swing.JOptionPane;
import javax.swing.JScrollPane;
import javax.swing.JTabbedPane;
import javax.swing.ListSelectionModel;
import javax.swing.ScrollPaneConstants;

import com.mbsetraining.sysmlhelper.common.NamedElementMap;
import com.mbsetraining.sysmlhelper.common.UserInterfaceHelper;
import com.mbsetraining.sysmlhelper.executablembse.ExecutableMBSEBasePanel;
import com.telelogic.rhapsody.core.*;

public class UpdateSurrogateRequirementsPanel extends ExecutableMBSEBasePanel{

	/**
	 * 
	 */
	private static final long serialVersionUID = -2463303852702389912L;


	private IRPPackage _owningPkg;
	private RemoteRequirementAssessment _assessment;
	private int _requirementNameMaxLength;
	
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

				String theCaption = "Update requirement(s) in model based on remote requirement links ";

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
		
		_requirementNameMaxLength = _context.getRequirementNameLengthMax();

		List<IRPModelElement> theSelectedEls = _context.getSelectedElements();

		setLayout( new BorderLayout(10,10) );
		setBorder( BorderFactory.createEmptyBorder( 10, 10, 10, 10 ) );

		_assessment = new RemoteRequirementAssessment( _context );
		_assessment.determineRequirementsToUpdate( theSelectedEls ); 

		if( theSelectedEls.size()==1 ) {
			
			IRPModelElement theSelectedEl = theSelectedEls.get( 0 );
			
			if( theSelectedEl instanceof IRPPackage ) {
				_owningPkg = (IRPPackage)theSelectedEl;
			}
		}
		
		JTabbedPane theTabbedPane = new JTabbedPane();

		theTabbedPane.addTab(
				"Update model spec (" + _assessment._requirementsToUpdateSpec.size() + ")", 
				null, 
				createRequirementsToUpdateSpecBox(), 
				"Requirements to update specification based on the specification text of remote");
		//updatePane.setMnemonicAt(0, KeyEvent.VK_1);
		
		if( !_assessment._requirementsToUpdateName.isEmpty() ) {

			theTabbedPane.addTab(
					"Update model reqt's name (" + _assessment._requirementsToUpdateName.size() + ")", 
					null, 
					createRequirementsToUpdateNameBox(), 
					"Requirements to update name based on remote counterpart's name");
		}
		
		if( !_assessment._requirementOwnersToUpdateName.isEmpty() ) {

			theTabbedPane.addTab(
					"Update reqt owner's name (" + _assessment._requirementOwnersToUpdateName.size() + ")", 
					null, 
					createRequirementOwnersToUpdateNameBox(), 
					"Requirement owners to update name of based on remote counterpart's name");
		}
		
		if( !_assessment._remoteRequirementsThatDontTrace.isEmpty() ) {

			theTabbedPane.addTab(
					"New requirements (" + _assessment._remoteRequirementsThatDontTrace.size() + ")", 
					null, 
					createRemoteRequirementsThatDontTraceBox(), 
					"Requirements linked to package but not to a requirement");
		}
		//updatePane.setMnemonicAt(0, KeyEvent.VK_1);

		// Only show this if not empty, as a warning
		if( !_assessment._requirementsWithNoMatchOrLinks.isEmpty() ) {

			theTabbedPane.addTab(
					"Info-only: No-links (" + _assessment._requirementsWithNoMatchOrLinks.size() + ")", 
					null, 
					createRequirementsWithNoLinksBox(), 
					"Requirements without links to remote requirements");
			//updatePane.setMnemonicAt(0, KeyEvent.VK_1);
		}

		if( !_assessment._remoteRequirementsToEstablishTraceTo.isEmpty() ) {

			theTabbedPane.addTab(
					"Info-only: Match with existing (" + _assessment._remoteRequirementsToEstablishTraceTo.size() + ")", 
					null, 
					createRequirementsToEstablishTraceToBox(), 
					"Requirements in model that can be matched with external requirements");
			//updatePane.setMnemonicAt(0, KeyEvent.VK_1);
		}
		
		if( !_assessment._requirementsExceedingNameLengthMax.isEmpty() ) {

			theTabbedPane.addTab(
					"Info-only: Names too long (" + _assessment._requirementsExceedingNameLengthMax.size() + ")", 
					null, 
					createRequirementsExceedingNameLengthBox(), 
					"Requirements or owners with names exceeding the " + _context.getRequirementNameLengthMax() + " chars roundtrip max");
			//updatePane.setMnemonicAt(0, KeyEvent.VK_1);
		}

		if( !_assessment._requirementsWithUnloadedHyperlinks.isEmpty() ) {

			theTabbedPane.addTab(
					"Unloaded links (" + _assessment._requirementsWithUnloadedHyperlinks.size() + ")", 
					null, 
					createBoxBasedOn( _assessment._requirementsWithUnloadedHyperlinks ), 
					"Requirements with HyperLinks to unloaded requirements");
		}

		add( theTabbedPane, BorderLayout.CENTER );

		int updateCount = _assessment._requirementsToUpdateSpec.size() + 
				_assessment._requirementOwnersToUpdateName.size() +
				_assessment._remoteRequirementsThatDontTrace.size() +
				_assessment._requirementsToUpdateName.size();

		if( updateCount == 0 ) {

			add( createOKCancelPanel(), BorderLayout.PAGE_END );

		} else {			
			add( createUpdateCancelPanelWith( "Update model (" + updateCount + ")" ), BorderLayout.PAGE_END );
		}
	}

	private Box createRequirementsToUpdateSpecBox() {

		Box theBox = Box.createVerticalBox();

		JScrollPane theScrollPane = createDoubleClickableScrollPaneFor( new ArrayList<>( _assessment._requirementsToUpdateSpec ) );

		Component theStartLabel;

		String msg = createUpdateMessage();

		theStartLabel = createPanelWithTextCentered( msg );

		theBox.add( theStartLabel );

		if( _assessment._requirementsToUpdateSpec.size() > 0 ) {
			theBox.add( theScrollPane );
		}

		return theBox;
	}

	private String createUpdateMessage() {
		
		String msg = "";

		if( _assessment._requirementsToUpdateSpec.size() == 0 ) {
			msg += "None of the " + _assessment._requirementsInScope.size() +" requirements in scope require their specification text \n" + 
					"to be updated due to changes to their remote counterpart. \n\nOf these:\n" + 
					_assessment._requirementsThatMatch.size() + " requirements have matching specification text\n";

			if( _assessment._requirementsThatDontTrace.size() > 0 ) {				
				msg += _assessment._requirementsThatDontTrace.size() + " have no remote requirement links";
			}

		} else if( _assessment._requirementsToUpdateSpec.size() == 1 ) {
			msg += "There is 1 requirement that needs updating because its specification text doesn't match \n(double-click to locate in browser). ";

		} else {
			msg += "There are " + _assessment._requirementsToUpdateSpec.size() + " requirements that need updating because their specification text doesn't match. ";
			msg += "Double-click to locate in browser. ";

		}

		int unloadedReqtsCount = _assessment._requirementsWithUnloadedHyperlinks.size();

		if( _owningPkg instanceof IRPPackage ) {
			unloadedReqtsCount += _context.getUnloadedLinksFor( _owningPkg ).size();
		}

		if( unloadedReqtsCount > 0 ) {
			msg += "\n\nHowever, there are " + unloadedReqtsCount + " unloaded links on the requirements or the package. \n";
			msg += "It is strongly recommended to resolve these before proceeding";
		}
		
		return msg;
	}
	
	private Box createRequirementsToUpdateNameBox() {

		Box theBox = Box.createVerticalBox();

		JScrollPane theScrollPane = createDoubleClickableScrollPaneFor( new ArrayList<>( _assessment._requirementsToUpdateName ) );

		Component theStartLabel;

		String msg = "";

		if( _assessment._requirementsToUpdateName.size() == 0 ) {
			msg += "None of the " + _assessment._requirementsInScope.size() +" requirements in scope require their name \n" + 
					"to be updated due to changes to their remote counterpart. ";

			if( _assessment._requirementsThatDontTrace.size() > 0 ) {				
				msg += _assessment._requirementsThatDontTrace.size() + " have no remote requirement links";
			}

		} else if( _assessment._requirementsToUpdateName.size() == 1 ) {
			msg += "There is 1 requirement that needs its name updating to match the remote name \n(double-click to locate in browser). ";

		} else {
			msg += "There are " + _assessment._requirementsToUpdateName.size() + " name's of requirements to update to match the remote names. ";
			msg += "Double-click to locate in browser. ";

		}

		theStartLabel = createPanelWithTextCentered( msg );

		theBox.add( theStartLabel );

		if( _assessment._requirementsToUpdateName.size() > 0 ) {
			theBox.add( theScrollPane );
		}

		return theBox;
	}

	private Box createRequirementOwnersToUpdateNameBox() {

		Box theBox = Box.createVerticalBox();

		JScrollPane theScrollPane = createDoubleClickableScrollPaneFor( new ArrayList<>( _assessment._requirementOwnersToUpdateName ) );

		Component theStartLabel;

		String msg = "";

		if( _assessment._requirementOwnersToUpdateName.size() == 0 ) {
			msg += "None of the " + _assessment._requirementOwnersInScope.size() +" requirement owners in scope require their name \n" + 
					"to be updated due to changes to their remote counterpart. ";

			if( _assessment._requirementOwnersThatDontTrace.size() > 0 ) {				
				msg += _assessment._requirementOwnersThatDontTrace.size() + " have no remote requirement links";
			}

		} else if( _assessment._requirementOwnersToUpdateName.size() == 1 ) {
			msg += "There is 1 requirement owner that needs its name updating to match the remote name \n(double-click to locate in browser). ";

		} else {
			msg += "There are " + _assessment._requirementOwnersToUpdateName.size() + " name's of requirement owners to update to match the remote names. ";
			msg += "Double-click to locate in browser. ";

		}

		theStartLabel = createPanelWithTextCentered( msg );

		theBox.add( theStartLabel );

		if( _assessment._requirementOwnersToUpdateName.size() > 0 ) {
			theBox.add( theScrollPane );
		}

		return theBox;
	}

	private Box createRequirementsWithNoLinksBox() {

		Box theBox = Box.createVerticalBox();

		JScrollPane theScrollPane = createDoubleClickableScrollPaneFor( new ArrayList<>( _assessment._requirementsWithNoMatchOrLinks ) );

		Component theStartLabel;

		String msg = "";

		int count = _assessment._requirementsWithNoMatchOrLinks.size();

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
	
	private Box createRequirementsExceedingNameLengthBox() {

		Box theBox = Box.createVerticalBox();

		JScrollPane theScrollPane = createDoubleClickableScrollPaneFor( new ArrayList<>( _assessment._requirementsExceedingNameLengthMax ) );

		Component theStartLabel;

		String msg = "";
		
		String postMsg = " exceeding the max length of " + _context.getRequirementNameLengthMax() + " for a name\n";

		int count = _assessment._requirementsExceedingNameLengthMax.size();

		if( count == 0 ) {
			msg += "There are no requirements " + postMsg;

		} else if( count == 1 ) {

			if( _owningPkg instanceof IRPPackage ) {
				msg += "There is 1 requirement in " + _owningPkg.getName() + postMsg;
			} else {
				msg += "There is 1 requirement " + postMsg;
			}

			msg += "(double-click to locate in browser).";

		} else { // plural

			if( _owningPkg instanceof IRPPackage ) {

				msg += "There are " + count + " requirements in " + _owningPkg.getName() + "\n "+ postMsg;
			} else {
				msg += "There are " + count + " requirements " + postMsg;

			}

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

		List<IRPModelElement> theReqts = new ArrayList<>( _assessment._remoteRequirementsToEstablishTraceTo.keySet() );

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
			msg += "A separate 'Establish trace relations to...' right-click menu command on the package does this \n";
			msg += "(double-click to locate in browser).";

		} else { // plural

			if( _owningPkg instanceof IRPPackage ) {

				msg += "There are " + count + " requirements in " + _owningPkg.getName() + " to establish a trace relations to ";
			} else {
				msg += "There are " + count + " requirements to establish trace relations to ";
			}

			msg += "because they have matching specification text \n";
			msg += "A separate 'Establish trace relations to...' right-click menu command on the package does this \n";
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

		JScrollPane theScrollPane = createDoubleClickableScrollPaneFor( new ArrayList<>( _assessment._remoteRequirementsThatDontTrace ) );

		Component theStartLabel;

		String msg = "";

		int count = _assessment._remoteRequirementsThatDontTrace.size();

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
					"requirements in the package and hence will be created ";

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



	private void updateRequirementBasedOnRemoteSpecification(
			IRPRequirement theRequirement ){

		_context.debug( "updateRequirementBasedOnRemoteSpecification was invoked for " + _context.elInfo( theRequirement ) );

		List<IRPRequirement> theRemoteRequirements = _context.getRemoteRequirementsFor( theRequirement );

		if( theRemoteRequirements.size()!=1 ) {

			_context.error( "Found " +  theRemoteRequirements.size() + " remote requirements for " + 
					_context.elInfo(theRequirement) + " when expecting 1");

		} else if( theRemoteRequirements.size()==1 ) {

			IRPRequirement theOSLCRequirement = (IRPRequirement)theRemoteRequirements.get(0);
			
			if( _context.isRequirementSpecificationMatchingFor(
					theRequirement, theOSLCRequirement ) ){

				_context.warning( "Found " + _context.elInfo( theRequirement ) + 
						"'s spec already matches hence no action needed");

			} else {
				
				String theRemoteSpec = theOSLCRequirement.getSpecification();

				_context.info( "Updating " + _context.elInfo( theRequirement ) + "'s spec to '" + theRemoteSpec + "'" );

				theRequirement.setSpecification( theRemoteSpec );
			}
		}
	}
	
	private void updateLocalNameBasedOnRemoteName(
			IRPModelElement theEl ){

		_context.debug( "updateLocalNameBasedOnRemoteName was invoked for " + _context.elInfo( theEl ) );

		List<IRPRequirement> theRemoteRequirements = _context.getRemoteRequirementsFor( theEl );

		if( theRemoteRequirements.size()!=1 ) {

			_context.error( "Found " +  theRemoteRequirements.size() + " remote requirements for " + 
					_context.elInfo( theEl ) + " when expecting 1");

		} else if( theRemoteRequirements.size()==1 ) {

			IRPRequirement theOSLCRequirement = (IRPRequirement)theRemoteRequirements.get(0);

			String theRemoteName = theOSLCRequirement.getName();
			String theRemoteID = theOSLCRequirement.getRequirementID();
			
			String theProposedName = _context.
					determineRequirementNameBasedOn( theRemoteName, theRemoteID, _requirementNameMaxLength );
			
			String theName = theEl.getName();

			if( theName.equals( theProposedName ) ){

				_context.warning( "Found " + _context.elInfo( theEl ) + 
						"'s name already matches remote hence no action needed");

			} else {
				_context.info( "Updating " + _context.elInfo( theEl ) + "'s name to '" + theProposedName + "'" );

				theEl.setName( theProposedName );
			}
		}
	}

	private void createRequirementBasedOn(
			IRPRequirement theRemoteRequirement ){

		//_context.info( "createRequirementBasedOnRemoteLink was invoked for " + _context.elInfo( theRemoteRequirement ) );

		String theRemoteName = theRemoteRequirement.getName();
		String theRemoteSpec = theRemoteRequirement.getSpecification();
		String theRemoteID = theRemoteRequirement.getRequirementID();

		String theProposedName = _context.
				determineRequirementNameBasedOn( 
						theRemoteName, theRemoteID, _requirementNameMaxLength );

		//_context.info( "theProposedName = " + theProposedName);

		theProposedName = _context.determineUniqueNameBasedOn(theProposedName, "Requirement", _owningPkg);

		_context.info( "Creating requirement called " + theProposedName + " with spec '" + theRemoteSpec + "'" );

		IRPRequirement theRequirement = (IRPRequirement) _owningPkg.addNewAggr( "Requirement", theProposedName );

		theRequirement.setSpecification( theRemoteSpec );
		theRequirement.setRequirementID( theRemoteID );

		if( _owningPkg.getUserDefinedMetaClass().equals(
				_context.REQTS_ANALYSIS_REQUIREMENT_PACKAGE ) ) {

			List<IRPStereotype> theStereotypes = _context.getMoveToStereotypes(
					_owningPkg );

			for( IRPStereotype theStereotype : theStereotypes ){
				theRequirement.addSpecificStereotype( theStereotype ); 
			}

			List<IRPRequirement> theRemoteReqts = new ArrayList<>();
			theRemoteReqts.add( theRemoteRequirement );
			
			_assessment._remoteRequirementsToEstablishTraceTo.put( theRequirement, theRemoteReqts );
		}

		//theRequirement.addRemoteDependencyTo( theRemoteRequirement, "Trace" );

		//_context.get_rhpPrj().save();

		//_context.info( "------------------");
	}

	@Override
	protected boolean checkValidity(boolean isMessageEnabled) {
		return true;
	}

	@Override
	protected void performAction() {

		try {

			if( checkValidity( false ) ){

				for( IRPRequirement theRequirement : _assessment._requirementsToUpdateSpec ){
					updateRequirementBasedOnRemoteSpecification( theRequirement );
				}
				
				for( IRPRequirement theRequirement : _assessment._requirementsToUpdateName ){
					updateLocalNameBasedOnRemoteName( theRequirement );
				}
				
				for( IRPModelElement theOwner : _assessment._requirementOwnersToUpdateName ){
					updateLocalNameBasedOnRemoteName( theOwner );
				}

				for( IRPRequirement theRequirement : _assessment._remoteRequirementsThatDontTrace ){
					createRequirementBasedOn( theRequirement );
				}

				/*
				for (Map.Entry<IRPRequirement, List<IRPRequirement>> entry : _remoteRequirementsToEstablishTraceTo.entrySet()) {

					IRPRequirement fromReqt = entry.getKey();
					List<IRPRequirement> toReqts = entry.getValue();
					
					for (IRPRequirement toReqt : toReqts) {
						_context.establishTraceRelationFrom( fromReqt, toReqt );
						_context.get_rhpPrj().save();
					}

					//_context.get_rhpPrj().save();
				}*/

				//_context.get_rhpPrj().save();

				// Save required by 9.0.1 SR1 to get GUI to update
				//				theRemoteRequirement.getProject().save();
			}
		} catch( Exception e ){
			UserInterfaceHelper.showWarningDialog( "Exception, e=" + e.getMessage() );
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