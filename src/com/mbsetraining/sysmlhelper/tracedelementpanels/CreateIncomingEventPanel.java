package com.mbsetraining.sysmlhelper.tracedelementpanels;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.Iterator;
import java.util.List;

import javax.swing.AbstractButton;
import javax.swing.BorderFactory;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;

import com.mbsetraining.sysmlhelper.common.GraphNodeInfo;
import com.mbsetraining.sysmlhelper.common.UserInterfaceHelper;
import com.mbsetraining.sysmlhelper.executablembse.PortBasedConnector;
import com.telelogic.rhapsody.core.*;

public class CreateIncomingEventPanel extends CreateTracedElementPanel {

	/**
	 * 
	 */
	private static final long serialVersionUID = 8467350275520692573L;
	
	private JCheckBox _eventActionIsNeededCheckBox;
	private JCheckBox _createSendEventCheckBox;
	private JCheckBox _createSendEventViaPanelCheckBox;
	private JCheckBox _creatAttributeCheckBox;
	private JCheckBox _createCheckOperationCheckBox;
	private String _nameForCheckOperation;
	private JTextField _nameForAttributeTextField;
	private JPanel _attributeNamePanel;
	private JLabel _createAttributeLabel;
	private IRPPackage _packageForEvent;
	private IRPActor _sourceActor;
	private IRPPort _sourceActorPort;
	private JButton _updateButton;

	// for testing only
	public static void main(String[] args) {
		
		IRPApplication theRhpApp = RhapsodyAppServer.getActiveRhapsodyApplication();
		launchThePanel( theRhpApp.getApplicationConnectionString() );	
	}
	
	public CreateIncomingEventPanel(
			String theAppID ){
		
		super( theAppID );
		
		//_context.debug( "CreateIncomingEventPanel invoked" );
		
		IRPClass theBuildingBlock = 
				_context.get_selectedContext().getBuildingBlock();

		if( theBuildingBlock == null ){
			
			buildUnableToRunDialog( 
					"Sorry, this helper is unable to run this command because \n" +
					"there was no execution context or block found in the model. \n " +
					"You need to add the relevant package structure first." );
			
		} else { // theBuildingBlock != null 

			_packageForEvent = _context.get_selectedContext().getPkgThatOwnsEventsAndInterfaces();

			List<IRPModelElement> theCandidateActors = 
					getNonElapsedTimeActorsRelatedTo( theBuildingBlock );

			if( !theCandidateActors.isEmpty() ){

				final IRPModelElement theActor = 
						_context.launchDialogToSelectElement(
								theCandidateActors, "Select Actor", true );

				if( theActor instanceof IRPActor ){

					IRPClass theBlock = _context.get_selectedContext().getBlockUnderDev(
							"Which Block do you want to add the Event to?" );

					if( theBlock != null ){

						final IRPClass theChosenBlock = theBlock;

						IRPPort thePort = _context.getPortThatConnects(
								(IRPActor)theActor, 
								(IRPClassifier)theChosenBlock,
								theBuildingBlock );

						if( thePort == null ){

							buildUnableToRunDialog(
									"Unable to find a port that connects " + _context.elInfo(theActor) + " to the " + 
											_context.elInfo( theChosenBlock ) + ". \n" +
											"You may want to add the necessary ports and connector to the IBD under " + 
											_context.elInfo( theBuildingBlock ) + " \nbefore trying this." );

							theBuildingBlock.highLightElement();

						} else { // Port != null

							_sourceActorPort = thePort;
							_sourceActor = (IRPActor) theActor;
							
							buildContent();
							commonPanelSetup();
						}
					}
				} else { // No actor chosen/available
					
					// This caches the chosen block
					_context.get_selectedContext().getBlockUnderDev(
							"Which Block do you want to add the Event to?" );
					
					buildContent();
					commonPanelSetup();
				}
			}
		}
	}
	
	private void buildContent() {
		
		String theSourceText = _context.getActionTextFrom( 
				_context.get_selectedContext().getSelectedEl() );	

		if( theSourceText.isEmpty() ){
			theSourceText = _tbd;
		}

		//_context.debug( "CreateIncomingEventPanel constructor (1) called with text '" + theSourceText + "'" );

		String theProposedName = determineBestEventName( 
				_context.get_selectedContext().getChosenBlock(), 
				theSourceText );									

		//_context.debug( "The proposed name is '" + theProposedName + "'" );

		setLayout( new BorderLayout(10,10) );
		setBorder( BorderFactory.createEmptyBorder( 10, 10, 10, 10 ) );

		_eventActionIsNeededCheckBox = new JCheckBox( "Populate on diagram?" );
		setupPopulateCheckbox( _eventActionIsNeededCheckBox );

		JPanel thePageStartPanel = new JPanel();
		thePageStartPanel.setLayout( new BoxLayout( thePageStartPanel, BoxLayout.X_AXIS ) );
		thePageStartPanel.add( createChosenNamePanelWith( "Create an event called:  ", theProposedName ) );
		thePageStartPanel.add( _eventActionIsNeededCheckBox );

		add( thePageStartPanel, BorderLayout.PAGE_START );

		JPanel theCenterPanel = new JPanel();
		theCenterPanel.setLayout( new BoxLayout( theCenterPanel, BoxLayout.Y_AXIS ) );

		_requirementSelectionPanel.setAlignmentX( LEFT_ALIGNMENT );
		theCenterPanel.add( _requirementSelectionPanel );

		_createSendEventCheckBox = new JCheckBox();
		_createSendEventCheckBox.setSelected( _sourceActor != null );
		_createSendEventCheckBox.setEnabled( _sourceActor != null );
		_createSendEventCheckBox.setVisible( _sourceActor != null );
		theCenterPanel.add( _createSendEventCheckBox );

		_creatAttributeCheckBox = new JCheckBox(
				"Add a value argument to change a corresponding attribute on the block");

		_creatAttributeCheckBox.addActionListener( new ActionListener() {

			public void actionPerformed(ActionEvent actionEvent) {

				AbstractButton abstractButton = (AbstractButton) actionEvent.getSource();

				boolean selected = abstractButton.getModel().isSelected();

				_createCheckOperationCheckBox.setEnabled( selected );
				_updateButton.setEnabled( selected );
				_createAttributeLabel.setEnabled( selected);
				_nameForAttributeTextField.setEnabled( selected );

				setupSendEventViaPanelCheckbox( _sourceActor );

				_createCheckOperationCheckBox.setSelected( selected );

				if( selected==false ){
					_createSendEventViaPanelCheckBox.setSelected( false );			        	
				}

				updateNames();

			}} );

		theCenterPanel.add( _creatAttributeCheckBox );

		_attributeNamePanel = createAttributeNamePanel( 
				_context.determineUniqueNameBasedOn( 
						_context.toMethodName( theSourceText, 40 ), 
						"Attribute", 
						_context.get_selectedContext().getChosenBlock() ) );

		_attributeNamePanel.setAlignmentX( LEFT_ALIGNMENT );

		theCenterPanel.add( _attributeNamePanel );

		_createCheckOperationCheckBox = new JCheckBox();
		_createCheckOperationCheckBox.setSelected( false );
		_createCheckOperationCheckBox.setEnabled( false );
		_updateButton.setEnabled( false );
		theCenterPanel.add( _createCheckOperationCheckBox );

		_createSendEventViaPanelCheckBox = new JCheckBox();
		setupSendEventViaPanelCheckbox( _sourceActor );

		theCenterPanel.add( _createSendEventViaPanelCheckBox );	

		add( theCenterPanel, BorderLayout.WEST );
	}

	private void setupSendEventViaPanelCheckbox(
			IRPActor theSourceActor ) {

		if( theSourceActor != null ){

			boolean isAttributeForEvent = 
					_creatAttributeCheckBox.isSelected();

			boolean isSendEventViaPanelOptionEnabled = 
					_context.getIsSendEventViaPanelOptionEnabled();

			boolean isSendEventViaPanelWantedByDefault = 
					_context.getIsSendEventViaPanelWantedByDefault( );

			_createSendEventViaPanelCheckBox.setSelected( 
					isAttributeForEvent && 
					isSendEventViaPanelWantedByDefault &&
					theSourceActor != null );

			_createSendEventViaPanelCheckBox.setEnabled( 
					isAttributeForEvent && 
					isSendEventViaPanelOptionEnabled &&
					theSourceActor != null );

			_createSendEventViaPanelCheckBox.setVisible( 
					isSendEventViaPanelOptionEnabled &&
					theSourceActor != null );

		} else {
			
			_createSendEventViaPanelCheckBox.setSelected( false );
			_createSendEventViaPanelCheckBox.setEnabled( false );
			_createSendEventViaPanelCheckBox.setVisible( false );
		}
	}

	private void commonPanelSetup(){
		add( createOKCancelPanel(), BorderLayout.PAGE_END );

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
	}

	public static void launchThePanel(
			String theAppID ){

		javax.swing.SwingUtilities.invokeLater(new Runnable() {

			@Override
			public void run() {

				JFrame.setDefaultLookAndFeelDecorated( true );

				JFrame frame = new JFrame( "Create an incoming event " );

				frame.setDefaultCloseOperation( JFrame.DISPOSE_ON_CLOSE );

				CreateIncomingEventPanel thePanel = 
						new CreateIncomingEventPanel(
								theAppID );

				frame.setContentPane( thePanel );

				frame.pack();
				frame.setLocationRelativeTo( null );
				frame.setVisible( true );
			}
		});
	}

	private String determineBestEventName(
			IRPClassifier onTargetBlock,
			final String theSourceText ){

		String theProposedName = null;

		if( _sourceActor != null ){
			String[] splitActorName = _sourceActor.getName().split("_");
			String theActorName = splitActorName[0];
			String theSourceMinusActor = theSourceText.replaceFirst( "^" + theActorName, "" );

			theProposedName = _context.determineUniqueNameBasedOn( 
					_context.toMethodName(
							"req" + theActorName + _context.capitalize( theSourceMinusActor ), 40 ), 
					"Event", 
					onTargetBlock.getProject() );
		} else {
			theProposedName = _context.determineUniqueNameBasedOn( 
					_context.toMethodName( 
							"ev" + _context.capitalize( theSourceText ), 40 ), 
					"Event", 
					onTargetBlock.getProject() );
		}

		return theProposedName;
	}

	private void updateNames(){

		if( _creatAttributeCheckBox.isSelected() ){
			_createSendEventCheckBox.setText(
					"Add a '" + determineSendEventNameFor( _chosenNameTextField.getText() ) 
					+ "' event with 'value' argument to the actor for webify/test creation usage" );
		} else {
			_createSendEventCheckBox.setText(
					"Add a '" + determineSendEventNameFor( _chosenNameTextField.getText() ) 
					+ "' event to the actor" );
		}

		_createSendEventViaPanelCheckBox.setText(
				"Add a '" + determineSendEventViaPanelNameFor( _chosenNameTextField.getText() ) 
				+ "' event and '" + _nameForAttributeTextField.getText() + "' attribute to the actor to ease panel creation" );

		_nameForCheckOperation = _context.determineBestCheckOperationNameFor(
				_context.get_selectedContext().getChosenBlock(), 
				_nameForAttributeTextField.getText(),
				40 );

		_createCheckOperationCheckBox.setText(
				"Add a '" + _nameForCheckOperation + "' operation to the block that returns the attribute value" );
	}

	private JPanel createAttributeNamePanel(
			String theProposedName ){

		JPanel thePanel = new JPanel();
		thePanel.setLayout( new BoxLayout( thePanel, BoxLayout.X_AXIS ) );	

		_createAttributeLabel = new JLabel( "Create an attribute called:  " );
		_createAttributeLabel.setEnabled( false );
		thePanel.add( _createAttributeLabel );

		_nameForAttributeTextField = new JTextField( theProposedName );
		_nameForAttributeTextField.setMaximumSize( new Dimension( 300,20 ) );
		_nameForAttributeTextField.setEnabled(false);

		_nameForAttributeTextField.getDocument().addDocumentListener(
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

		_updateButton = new JButton( "Update" );
		_updateButton.setPreferredSize( new Dimension( 75,20 ) );

		_updateButton.addActionListener( new ActionListener() {
			
			@Override
			public void actionPerformed( ActionEvent e ) {
				
				try {
					String theAttributeName = _nameForAttributeTextField.getText();
					
					if( !theAttributeName.isEmpty() ){
						
						String theDecapitalized = _context.decapitalize( theAttributeName );
						
						_nameForAttributeTextField.setText( theDecapitalized );

						String theCapitalized = _context.capitalize( theAttributeName );
						
						String newEventName = determineBestEventName(
								_context.get_selectedContext().getChosenBlock(),
								theCapitalized );
						
						_chosenNameTextField.setText( newEventName );
					}
												
				} catch( Exception e2 ){
					_context.error( "Unhandled exception in when update button pressed, e2=" + e2.getMessage() );
				}
			}
		});
		
		thePanel.add( _nameForAttributeTextField );
		thePanel.add( new JLabel( " " ) );
		thePanel.add( _updateButton );

		return thePanel;
	}

	private void removeDependenciesToRequirementsFrom(
			IRPModelElement theEl ){

		@SuppressWarnings("unchecked")
		List<IRPDependency> theDependencies = theEl.getDependencies().toList();

		for (Iterator<IRPDependency> iter = theDependencies.listIterator(); iter.hasNext(); ) {

			IRPDependency theDependency = iter.next();

			IRPModelElement theDependsOn = theDependency.getDependsOn();

			if( theDependsOn != null && 
					theDependsOn instanceof IRPRequirement ){

				theDependency.deleteFromProject();
			}
		}
	}

	private IRPModelElement createTestBenchSendFor(
			IRPEvent theEvent, 
			IRPActor onTheActor, 
			String withSendEventName){

		IRPEvent sendEvent = null;

		IRPStatechart theStatechart = onTheActor.getStatechart();

		if (theStatechart != null){

			IRPState theReadyState = 
					_context.getStateCalled(
							"Ready", 
							theStatechart, 
							onTheActor );

			if (theReadyState != null){

				_context.debug( "Creating event called " + withSendEventName 
						+ " on actor called " + onTheActor.getName() );

				sendEvent = (IRPEvent) theEvent.clone(withSendEventName, onTheActor.getOwner());

				removeDependenciesToRequirementsFrom( sendEvent );

				_context.debug("The state called " + theReadyState.getFullPathName() + 
						" is owned by " + theReadyState.getOwner().getFullPathName());

				IRPTransition theTransition = theReadyState.addInternalTransition( sendEvent );

				String actionText = "OPORT( " + _sourceActorPort.getName() + 
						" )->GEN(" + theEvent.getName() + "(";

				@SuppressWarnings("unchecked")
				List<IRPArgument> theArguments = theEvent.getArguments().toList();

				for (Iterator<IRPArgument> iter = theArguments.iterator(); iter.hasNext();) {
					IRPArgument theArgument = (IRPArgument) iter.next();
					actionText += "params->" + theArgument.getName();

					if (iter.hasNext()) actionText += ",";
				}

				actionText += "));";

				theTransition.setItsAction(actionText);

				sendEvent.addStereotype("Web Managed", "Event");

			} else {
				_context.error( "Error in createTestBenchSendFor, the actor called " 
						+ onTheActor.getFullPathName() + "'s statechart does not have a Ready state" );
			}
		} else {
			_context.error( "Unable to proceed as actor called " 
					+ onTheActor.getFullPathName() + " does not have a statechart" );
		}

		return sendEvent;
	}

	private IRPModelElement createTestBenchSendViaPanelFor(
			IRPEvent theEvent,
			IRPAttribute theAttribute, 
			IRPActor onTheActor, 
			String withSendEventName){

		IRPEvent sendEvent = null;

		IRPStatechart theStatechart = onTheActor.getStatechart();

		if (theStatechart != null){

			IRPState theReadyState = 
					_context.getStateCalled(
							"Ready", 
							theStatechart, 
							onTheActor );

			if (theReadyState != null){

				_context.debug("Creating event called " + withSendEventName 
						+ " on actor called " + onTheActor.getName());

				sendEvent = (IRPEvent) onTheActor.getOwner().addNewAggr("Event", withSendEventName);

				_context.debug("The state called " + theReadyState.getFullPathName() + 
						" is owned by " + theReadyState.getOwner().getFullPathName());

				IRPTransition theTransition = theReadyState.addInternalTransition( sendEvent );

				IRPModelElement existingEl = 
						_context.findElementWithMetaClassAndName(
								"Attribute", theAttribute.getName(), onTheActor );

				IRPAttribute theAttributeOwnedByActor;

				if( existingEl != null && existingEl instanceof IRPAttribute ){

					theAttributeOwnedByActor = (IRPAttribute)existingEl;
				} else {
					theAttributeOwnedByActor = 
							(IRPAttribute) theAttribute.clone(
									theAttribute.getName(), onTheActor );
				}

				String actionText = "OPORT( " + _sourceActorPort.getName() + " )->GEN( " + 
						theEvent.getName() + "( " + theAttributeOwnedByActor.getName() + " ) );" ;

				theTransition.setItsAction(actionText);

			} else {
				_context.error( "Error in createTestBenchSendViaPanelFor, the actor called " 
						+ onTheActor.getFullPathName() + "'s statechart does not have a Ready state" );
			}
		} else {
			_context.error( "Unable to proceed as actor called " 
					+ onTheActor.getFullPathName() + " does not have a statechart" );
		}

		return sendEvent;
	}

	private void addSetAttributeTransitionToMonitoringStateFor(
			IRPAttribute theAttribute, 
			String triggeredByTheEventName, 
			IRPClassifier theOwnerOfStatechart) {

		IRPStatechart theStatechart = theOwnerOfStatechart.getStatechart();

		IRPState theMonitoringState = 
				_context.getStateCalled("MonitoringConditions", theStatechart, theOwnerOfStatechart);

		if (theMonitoringState != null){
			//_context.debug( _context.elInfo( theMonitoringState ) + "found");

			IRPTransition theTransition = theMonitoringState.addTransition(theMonitoringState);

			theTransition.setItsTrigger(triggeredByTheEventName);
			theTransition.setItsAction("set" + _context.capitalize(theAttribute.getName()) + "(params->value);");

			//_context.debug( _context.elInfo( theTransition ) + " was added");	

			IRPGraphElement theGraphEl = 
					_context.findGraphEl(
							theOwnerOfStatechart, 
							"MonitoringConditions" );

			if (theGraphEl != null){
				IRPDiagram theGraphElDiagram = theGraphEl.getDiagram();
				
				//_context.debug( _context.elInfo( theGraphElDiagram ) + "related to " 
				//		+ _context.elInfo(theGraphEl.getModelObject()) 
				//		+ " is the diagram for the GraphEl");

				IRPGraphNode theGraphNode = (IRPGraphNode)theGraphEl;

				GraphNodeInfo theNodeInfo = new GraphNodeInfo( theGraphNode, _context );

				theGraphElDiagram.addNewEdgeForElement(
						theTransition, 
						theGraphNode, 
						theNodeInfo.getTopRightX(), 
						theNodeInfo.getMiddleY(), 
						theGraphNode, 
						theNodeInfo.getMiddleX(), 
						theNodeInfo.getBottomLeftY());

				//_context.debug( "Added edge to " + theEdge.getModelObject().getFullPathName() );
			} else {
				_context.error( "Error in addAnAttributeToMonitoringStateWith, " +
						"unable to find the MonitoringConditions state" );
			}

		} else {
			_context.error("Error did not find MonitoringConditions state");
		}
	}

	@Override
	protected boolean checkValidity(
			boolean isMessageEnabled) {

		String errorMessage = null;
		boolean isValid = true;

		String theChosenName = _chosenNameTextField.getText();
		String theAttributeName = _nameForAttributeTextField.getText();

		
		IRPClassifier theClassifier = (IRPClassifier)_context.get_selectedContext().getChosenBlock();
		IRPModelElement theSourceModelEl = _context.get_selectedContext().getSelectedEl();
		
		boolean isLegalName = _context.isLegalName( theChosenName, theClassifier );

		if( !isLegalName ){

			errorMessage = theChosenName + " is not legal as an identifier representing an executable event\n";				
			isValid = false;

		} else if( !_context.isElementNameUnique(
				theChosenName, 
				"Event", 
				_packageForEvent.getProject(), 
				1 ) ){

			errorMessage = "Unable to proceed as the event name '" + theChosenName + "' is not unique";
			isValid = false;

		} else if( _creatAttributeCheckBox.isSelected() ){

			_context.debug("An attribute is needed, chosen name was " + theAttributeName);			
			_context.debug( _context.elInfo( theClassifier ) + " is the target owning element" );

			IRPStatechart theStatechart = theClassifier.getStatechart();

			if (theStatechart != null){
				IRPState theMonitoringState = 
						_context.getStateCalled( 
								"MonitoringConditions", 
								theStatechart, 
								theClassifier );

				if( theMonitoringState==null ){
					errorMessage = "You selected to update an attribute with an event. The Block has a statechart but for this to work the owning block needs to have a statechart with a MonitoringConditions \n" +
							"state. To do this make sure the chosen Block has a generalization to the BasePkg.TimeElapsedBlock";
					isValid = false;
					_creatAttributeCheckBox.setSelected( false );
					_createCheckOperationCheckBox.setSelected( false );
				}
			}

			if( theStatechart == null ){

				errorMessage = "You selected to update an attribute with an event. The Block has no statechart. For this to work the owning block needs to have a statechart with a MonitoringConditions \n" +
						"state. To do this make sure the chosen Block has a generalization to the BasePkg.TimeElapsedBlock";
				isValid = false;	

				_creatAttributeCheckBox.setSelected( false );
				_createCheckOperationCheckBox.setSelected( false );

			} else if( !_context.isLegalName( theAttributeName, theClassifier ) ){

				errorMessage = theChosenName + " is not legal as an identifier representing an executable attribute\n";				
				isValid = false;

			} else if ( (theSourceModelEl == null || 
					!( theSourceModelEl instanceof IRPAttribute ) ) &&
					!_context.isElementNameUnique(
							theAttributeName, 
							"Attribute", 
							theClassifier, 
							0 ) ){

				errorMessage = "Unable to proceed as the attribute name '" + theAttributeName + "' is not unique";
				isValid = false;
			}
			
		} else if( _createCheckOperationCheckBox.isSelected() && 
				!_context.isElementNameUnique(
						_nameForCheckOperation,
						"Operation",
						theClassifier, 
						1 ) ){

			errorMessage = "Unable to proceed as the check operation name '" + _nameForCheckOperation + "' is not unique";
			isValid = false;
		}

		if( isMessageEnabled && !isValid && errorMessage != null ){

			UserInterfaceHelper.showWarningDialog( errorMessage );
		}

		return isValid;
	}

	private String determineSendEventNameFor(
			String theSourceEventName ){

		return "send_" + theSourceEventName.replaceFirst( "req", "" );
	}

	private String determineSendEventViaPanelNameFor(
			String theSourceEventName ){

		return "send_" + theSourceEventName.replaceFirst( "req","" ) + "ViaPanel";
	}

	@Override
	protected void performAction() {

		// checkValidity is assumed to return true

		IRPEvent theEvent = _packageForEvent.addEvent( _chosenNameTextField.getText() );

		IRPClassifier theClassifier = (IRPClassifier)_context.get_selectedContext().getChosenBlock();
		IRPModelElement theSourceModelEl = _context.get_selectedContext().getSelectedEl();

		// add value argument before cloning the event to create the test-bench send
		if( _creatAttributeCheckBox.isSelected() ){
			theEvent.addArgument( "value" );
		}

		List<IRPRequirement> selectedReqtsList = 
				_requirementSelectionPanel.getSelectedRequirementsList();

		addTraceabilityDependenciesTo( theEvent, selectedReqtsList );

		// was an actor selected?
		if( _sourceActor instanceof IRPClassifier ){
			
			PortBasedConnector theExistingConnector = 
					new PortBasedConnector( 
							_sourceActor, 
							theClassifier,
							_context );

			theExistingConnector.addEvent( theEvent );
		}

		IRPModelElement theReception = 
				theClassifier.addNewAggr( 
						"Reception", 
						_chosenNameTextField.getText() );

		addTraceabilityDependenciesTo( theReception, selectedReqtsList );

		if ( _createSendEventCheckBox.isSelected() ) {

			String theSendEventName = determineSendEventNameFor( theEvent.getName() );

			//_context.debug( "Send event option was enabled, create event called " + 
			//		theSendEventName );

			IRPModelElement theTestbenchReception = 
					createTestBenchSendFor( 
							theEvent, 
							(IRPActor) _sourceActor, 
							theSendEventName );

			theTestbenchReception.highLightElement();

		} else {

			//_context.debug( "Send event option was not enabled, so skipping this" );
			theReception.highLightElement();

			// If no send event and no actor then assume you want to webify the event directly on the block
			if( _sourceActor == null ){
				theEvent.addStereotype( "Web Managed", "Event" );
			}
		}

		IRPAttribute theAttribute = null;

		if( _creatAttributeCheckBox.isSelected() && 
				theClassifier instanceof IRPClassifier ){

			if( theSourceModelEl != null && 
					theSourceModelEl instanceof IRPAttribute ){

				theAttribute = (IRPAttribute)theSourceModelEl;

			} else {
				theAttribute = addAttributeTo(
						theClassifier, 
						_nameForAttributeTextField.getText(), 
						"0", 
						selectedReqtsList );
			}

			_context.addAutoRippleDependencyIfOneDoesntExist(
					theAttribute, theReception );

			if( _createSendEventViaPanelCheckBox.isSelected() ){

				String theSendEventViaPanelName = 
						determineSendEventViaPanelNameFor( theEvent.getName() );

				//String theSendEventName = 
				//		determineSendEventNameFor( theEvent.getName() );

				//_context.debug( "Send event option was enabled, create event called " + theSendEventName );

				IRPModelElement theTestbenchReceptionViaPanel = createTestBenchSendViaPanelFor( 
						theEvent, 
						theAttribute,
						(IRPActor) _sourceActor, 
						theSendEventViaPanelName );

				theTestbenchReceptionViaPanel.highLightElement();
			}

			if ( _createCheckOperationCheckBox.isSelected() ){

				IRPOperation theCheckOp = 
						addCheckOperationFor( theAttribute, _nameForCheckOperation );
				
				addTraceabilityDependenciesTo( theCheckOp, selectedReqtsList );	
				theCheckOp.highLightElement();
			}

			addSetAttributeTransitionToMonitoringStateFor(
					theAttribute, 
					theEvent.getName(), 
					theClassifier );
		}	

		_context.bleedColorToElementsRelatedTo( selectedReqtsList );

		if( _eventActionIsNeededCheckBox.isSelected() ){
			populateReceiveEventActionOnDiagram( theEvent );
		}

		if( theAttribute != null ){

			// highlight the attribute so that it can be made a 
			// publish flow-port next, if needed
			theAttribute.highLightElement();
		} else {
			theEvent.highLightElement();			
		}
	}

	private void populateReceiveEventActionOnDiagram(
			IRPEvent theEvent ){
		
		IRPDiagram theSourceDiagram = _context.get_selectedContext().getSourceDiagram();
		
		if( theSourceDiagram != null ){

			if( theSourceDiagram instanceof IRPActivityDiagram ){
				IRPActivityDiagram theAD = (IRPActivityDiagram)theSourceDiagram;

				IRPFlowchart theFlowchart = theAD.getFlowchart();

				IRPAcceptEventAction theAcceptEvent = 
						(IRPAcceptEventAction) theFlowchart.addNewAggr(
								"AcceptEventAction", theEvent.getName() );

				theAcceptEvent.setEvent( theEvent );			

				theFlowchart.addNewNodeForElement( 
						theAcceptEvent, 
						getSourceElementX(), 
						getSourceElementY(), 
						300, 
						40 );

				theAcceptEvent.highLightElement();

			} else if( theSourceDiagram instanceof IRPObjectModelDiagram ){				

				IRPObjectModelDiagram theOMD = (IRPObjectModelDiagram)theSourceDiagram;

				IRPGraphNode theEventNode = theOMD.addNewNodeForElement(
						theEvent, 
						getSourceElementX() + 50, 
						getSourceElementY() + 50, 
						300, 
						40 );	

				IRPGraphElement theSourceGraphEl = _context.get_selectedContext().getSelectedGraphEl();
				
				if( theSourceGraphEl != null ){

					IRPCollection theGraphElsToDraw = 
							_context.get_rhpApp().createNewCollection();

					theGraphElsToDraw.addGraphicalItem( theSourceGraphEl );
					theGraphElsToDraw.addGraphicalItem( theEventNode );

					theOMD.completeRelations( theGraphElsToDraw, 1 );
				}

				theEvent.highLightElement();

			} else {
				_context.error( "Error in populateReceiveEventActionOnDiagram, m_SourceGraphElement is not a supported diagram" );
			}

		} else {

			_context.error( "Error in populateReceiveEventActionOnDiagram, m_SourceGraphElement is null when value was expected" );
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