package functionalanalysisplugin;

import java.awt.BorderLayout;
import java.util.List;
import java.util.Set;

import javax.swing.BorderFactory;
import javax.swing.BoxLayout;
import javax.swing.JCheckBox;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;

import com.mbsetraining.sysmlhelper.common.UserInterfaceHelper;
import com.mbsetraining.sysmlhelper.executablembse.PortBasedConnector;
import com.telelogic.rhapsody.core.*;

public class CreateOutgoingEventPanel extends CreateTracedElementPanel {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	private JCheckBox m_ActionOnDiagramIsNeededCheckBox;
	private IRPActor m_DestinationActor;
	private IRPPort m_DestinationActorPort;
	private IRPPackage m_PackageForEvent;
	private JCheckBox m_SendOperationIsNeededCheckBox;
	private JCheckBox m_ActiveAgumentNeededCheckBox;
	
	public static void main(String[] args) {

		IRPApplication rhpApp = RhapsodyAppServer.getActiveRhapsodyApplication();
		launchThePanel( rhpApp.getApplicationConnectionString() );
	}

	public static void launchThePanel(
			final String theAppID ){

		javax.swing.SwingUtilities.invokeLater(new Runnable() {

			@Override
			public void run() {

				JFrame.setDefaultLookAndFeelDecorated( true );

				JFrame frame = new JFrame( "Create an outgoing event" );

				frame.setDefaultCloseOperation( JFrame.DISPOSE_ON_CLOSE );

				CreateOutgoingEventPanel thePanel = 
						new CreateOutgoingEventPanel(
								theAppID );

				frame.setContentPane( thePanel );

				frame.pack();
				frame.setLocationRelativeTo( null );
				frame.setVisible( true );
			}
		});
	}

	public CreateOutgoingEventPanel(
			String theAppID ){

		super( theAppID );
		
		IRPClass theBuildingBlock = 
				_selectionContext.getBuildingBlock();

		if( theBuildingBlock == null ){
			
			buildUnableToRunDialog( 
					"Sorry, this helper is unable to run this command because \n" +
					"there was no execution context or block found in the model. \n " +
					"You need to add the relevant package structure first." );
			
		} else { // theBuildingBlock != null

			m_PackageForEvent = _selectionContext.getPkgThatOwnsEventsAndInterfaces();

			List<IRPModelElement> theCandidateActors = 
					getNonElapsedTimeActorsRelatedTo( theBuildingBlock );

			if( theCandidateActors.isEmpty() ){

				UserInterfaceHelper.showWarningDialog( "There are no actor parts to send events to in the " + 
						_context.elInfo( theBuildingBlock ) + ". \n\n" +
						"If you want to send an event then add an actor to the BDD and connect its part using ports. These steps are automated \n" +
						"by the '" + _context.getString( "functionalanalysisplugin.AddNewActorToPackageMenu" ) + "' command. ");

			} else {

				final IRPModelElement theActorEl = 
						_context.launchDialogToSelectElement(
								theCandidateActors, 
								"Select Actor to send Event to", 
								true);

				if( theActorEl != null && 
						theActorEl instanceof IRPActor ){

					IRPActor theActor = (IRPActor)theActorEl;

					List<IRPModelElement> theCandidates = 
							_context.getNonActorOrTestingClassifiersConnectedTo( 
									theActor, 
									theBuildingBlock );

					if( theCandidates.isEmpty() ){

						UserInterfaceHelper.showWarningDialog(
								"The " + _context.elInfo( theBuildingBlock ) + 
								" does not have any connectors that connect the " + _context.elInfo(theActor) + 
								" with Blocks.\n\n" + "Fix this and then try again");

					} else {

						IRPClass theChosenBlock = 
								_selectionContext.getBlockUnderDev( 
										"Which Block is sending the event?" );

						if( theChosenBlock != null ){

							final IRPPort thePort = _context.getPortThatConnects(
									(IRPClassifier)theChosenBlock,
									theActor, 
									theBuildingBlock );

							if( thePort == null ){

								UserInterfaceHelper.showWarningDialog(
										"Unable to find a port that connects " + _context.elInfo( theActor ) + " to the " + 
												_context.elInfo( theChosenBlock ) + ". \n" +
												"You may want to add the necessary ports and connector to the IBD under " + 
												_context.elInfo( theBuildingBlock ) + " \nbefore trying this." );

								theBuildingBlock.highLightElement();

							} else { // thePort != null

								String theSourceText = _context.getActionTextFrom( _selectionContext.getSelectedEl() );		

								if( theSourceText.isEmpty() ){
									theSourceText = _tbd;
								}

								createCommonContent(
										theSourceText, 
										_selectionContext.getChosenBlock(), 
										_selectionContext.getSelectedReqts(), 
										theActor, 
										thePort,
										m_PackageForEvent );
							}
						}
					}
				}
			}
		}

		_context.debug( "CreateOutgoingEventPanel constructor called for " + 
				_context.elInfo( _selectionContext.getSelectedEl() ) );

	}

	private void createCommonContent(
			String theSourceText,
			IRPClassifier onTargetBlock,
			Set<IRPRequirement> withReqtsAlsoAdded,
			IRPActor toDestinationActor,
			IRPPort toDestinationActorPort,
			IRPPackage thePackageForEvent ) {	

		m_DestinationActor = toDestinationActor;
		m_DestinationActorPort = toDestinationActorPort;
		m_PackageForEvent = thePackageForEvent;

		String[] splitActorName = m_DestinationActor.getName().split("_");
		String theActorName = splitActorName[0];
		String theSourceMinusActor = theSourceText.replaceFirst( "^" + theActorName, "" );

		_context.debug("The source minus actor is '" + theSourceMinusActor + "'");	

		String theProposedName = _context.determineUniqueNameBasedOn( 
				_context.toMethodName( "reqInform" + theActorName + theSourceMinusActor, 40 ), 
				"Event", 
				onTargetBlock.getProject() );	

		m_SendOperationIsNeededCheckBox = new JCheckBox();
		m_SendOperationIsNeededCheckBox.setSelected(true);

		m_ActiveAgumentNeededCheckBox = new JCheckBox(
				"Add an 'active' argument to the event (e.g. for on/off conditions)");

		m_ActiveAgumentNeededCheckBox.setSelected(false);

		setLayout( new BorderLayout(10,10) );
		setBorder( BorderFactory.createEmptyBorder( 10, 10, 10, 10 ) );

		m_ActionOnDiagramIsNeededCheckBox = new JCheckBox("Populate on diagram?");
		setupPopulateCheckbox( m_ActionOnDiagramIsNeededCheckBox );

		JPanel thePageStartPanel = new JPanel();
		thePageStartPanel.setLayout( new BoxLayout( thePageStartPanel, BoxLayout.X_AXIS ) );
		thePageStartPanel.add( createChosenNamePanelWith( "Create an event called:  ", theProposedName ) );
		thePageStartPanel.add( m_ActionOnDiagramIsNeededCheckBox );

		_requirementSelectionPanel.setAlignmentX(LEFT_ALIGNMENT);

		JPanel theCenterPanel = new JPanel();
		theCenterPanel.setLayout( new BoxLayout( theCenterPanel, BoxLayout.Y_AXIS ) );
		theCenterPanel.add( _requirementSelectionPanel );
		theCenterPanel.add( m_SendOperationIsNeededCheckBox );
		theCenterPanel.add( m_ActiveAgumentNeededCheckBox );

		_chosenNameTextField.getDocument().addDocumentListener(
				new DocumentListener() {

					@Override
					public void changedUpdate(DocumentEvent arg0) {
						updateNames();					
					}

					@Override
					public void insertUpdate(DocumentEvent arg0) {
						updateNames();
					}

					@Override
					public void removeUpdate(DocumentEvent arg0) {
						updateNames();
					}	
				});

		updateNames();

		add( thePageStartPanel, BorderLayout.PAGE_START );
		add( theCenterPanel, BorderLayout.WEST );
		add( createOKCancelPanel(), BorderLayout.PAGE_END );
	}

	private void updateNames(){
		m_SendOperationIsNeededCheckBox.setText(
				"Add an '" + determineBestInformNameFor(
						_selectionContext.getChosenBlock(), 
						_chosenNameTextField.getText() ) 
						+ "' operation that sends the event");
	}

	private String determineBestInformNameFor(
			IRPClassifier onTargetBlock,
			String theEventName){

		String theProposedName = _context.determineUniqueNameBasedOn( 
				_context.toMethodName( _context.decapitalize( theEventName.replace("req", "") ), 40 ), 
				"Operation", 
				onTargetBlock );

		return theProposedName;
	}

	private void populateSendActionOnDiagram(
			IRPEvent theEvent ){

		IRPApplication theRhpApp = RhapsodyAppServer.getActiveRhapsodyApplication();

		IRPDiagram theSourceDiagram = _selectionContext.getSourceDiagram();
		IRPGraphElement theGraphEl = _selectionContext.getSelectedGraphEl();

		if( theSourceDiagram != null ){

			if( theSourceDiagram instanceof IRPActivityDiagram ){

				IRPActivityDiagram theAD = (IRPActivityDiagram)theSourceDiagram;

				IRPFlowchart theFlowchart = theAD.getFlowchart();

				IRPState theState = 
						(IRPState) theFlowchart.addNewAggr(
								"State", theEvent.getName() );

				theState.setStateType("EventState");

				if( theState != null ){

					IRPSendAction theSendAction = theState.getSendAction();
					theSendAction.setEvent(theEvent);
				}

				theFlowchart.addNewNodeForElement(
						theState, getSourceElementX(), getSourceElementY(), 300, 40 );

				theState.highLightElement();

			} else if( theSourceDiagram instanceof IRPObjectModelDiagram ){				

				IRPObjectModelDiagram theOMD = (IRPObjectModelDiagram)theSourceDiagram;

				IRPGraphNode theEventNode = theOMD.addNewNodeForElement(
						theEvent, getSourceElementX() + 50, getSourceElementY() + 50, 300, 40 );	

				if( theGraphEl != null ){
					IRPCollection theGraphElsToDraw = theRhpApp.createNewCollection();
					theGraphElsToDraw.addGraphicalItem( theGraphEl );
					theGraphElsToDraw.addGraphicalItem( theEventNode );

					theOMD.completeRelations( theGraphElsToDraw, 1 );
				}

				theEvent.highLightElement();

			} else {
				_context.error( "Error in populateSendActionOnDiagram " + 
						_context.elInfo( theSourceDiagram ) + 
						" is not supported for populating on" );
			}

		} else {	
			_context.error( "Error in populateSendActionOnDiagram, m_SourceGraphElement is null when value was expected" );
		}
	}

	@Override
	protected boolean checkValidity(
			boolean isMessageEnabled ){

		String errorMessage = null;
		boolean isValid = true;

		String theChosenName = _chosenNameTextField.getText();
		IRPClass theChosenBlock = _selectionContext.getChosenBlock();

		boolean isLegalName = _context.isLegalName( 
				theChosenName, 
				theChosenBlock );

		if( !isLegalName ){

			errorMessage += theChosenName + " is not legal as an identifier representing an executable event\n";				
			isValid = false;

		} else if( !_context.isElementNameUnique(
				theChosenName, 
				"Event", 
				m_PackageForEvent.getProject(), 
				1 ) ){

			errorMessage = "Unable to proceed as the event name '" + theChosenName + "' is not unique";
			isValid = false;
		}		

		if( m_SendOperationIsNeededCheckBox.isSelected() ){

			String theProposedName = determineBestInformNameFor(
					theChosenBlock, 
					theChosenName );

			if( !_context.isElementNameUnique(
					theProposedName, 
					"Operation", 
					theChosenBlock, 
					0 ) ){

				if( errorMessage != null ){
					errorMessage += "\nand the operation name  '" + theProposedName + "' is not unique";
				} else {
					errorMessage = "Unable to proceed as the operation name '" + theProposedName + "' is not unique";
				}
				isValid = false;
			}
		}

		if( isMessageEnabled && !isValid && errorMessage != null ){

			UserInterfaceHelper.showWarningDialog( errorMessage );
		}

		return isValid;
	}

	@Override
	protected void performAction() {

		IRPClass theChosenBlock = _selectionContext.getChosenBlock();

		String theEventName = _chosenNameTextField.getText(); 

		if( !theEventName.isEmpty() ){

			IRPEvent theEvent = m_PackageForEvent.addEvent( theEventName );

			List<IRPRequirement> selectedReqtsList = _requirementSelectionPanel.getSelectedRequirementsList();

			addTraceabilityDependenciesTo( theEvent, selectedReqtsList );

			if( m_ActiveAgumentNeededCheckBox.isSelected() ){
				theEvent.addArgument( "active" );
			}

			theEvent.highLightElement();

			PortBasedConnector theExistingConnector = 
					new PortBasedConnector( 
							theChosenBlock, 
							m_DestinationActor,
							_context );

			theExistingConnector.addEvent( theEvent );

			IRPModelElement theReception = m_DestinationActor.addNewAggr("Reception", theEventName);
			addTraceabilityDependenciesTo( theReception, selectedReqtsList );

			theReception.highLightElement();

			if (m_SendOperationIsNeededCheckBox.isSelected()){

				_context.debug("Adding an inform Operation");		

				IRPOperation informOp =
						theChosenBlock.addOperation(
								determineBestInformNameFor(
										theChosenBlock, 
										theEventName ) );

				informOp.highLightElement();

				addTraceabilityDependenciesTo( informOp, selectedReqtsList );

				String thePortName = m_DestinationActorPort.getName();

				if (m_ActiveAgumentNeededCheckBox.isSelected()){

					informOp.addArgument("active");
					informOp.setBody("OPORT( " + thePortName + " )->GEN( " + theEventName + "( active ) );");
				} else {
					informOp.setBody("OPORT( " + thePortName + " )->GEN( " + theEventName + " );");
				}			
			}	

			if( m_ActionOnDiagramIsNeededCheckBox.isSelected() ){
				populateSendActionOnDiagram( theEvent );
			}

			_selectionContext.bleedColorToElementsRelatedTo( selectedReqtsList );
		}
	}
}

/**
 * Copyright (C) 2016-2021  MBSE Training and Consulting Limited (www.executablembse.com)

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