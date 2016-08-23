package functionalanalysisplugin;

import generalhelpers.GeneralHelpers;
import generalhelpers.Logger;
import generalhelpers.UserInterfaceHelpers;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import javax.swing.AbstractButton;
import javax.swing.BorderFactory;
import javax.swing.BoxLayout;
import javax.swing.JCheckBox;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;

import com.telelogic.rhapsody.core.*;

public class CreateIncomingEventPanel extends CreateTracedElementPanel {
	
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	private JCheckBox m_EventActionIsNeededCheckBox;
	private JCheckBox m_CreateSendEvent;
	private JCheckBox m_CreateSendEventViaPanel;
	private JCheckBox m_AttributeCheckBox;
	private JCheckBox m_CheckOperationCheckBox;
	private String m_CheckOpName;
	private JTextField m_AttributeNameTextField;
	private JPanel m_AttributeNamePanel;
	private JLabel m_CreateAttributeLabel;
	private IRPPackage m_PackageForEvent;
	private IRPActor m_SourceActor;
	private IRPPort m_SourceActorPort;
	
	public static void createIncomingEventsFor(
			IRPProject theActiveProject,
			List<IRPGraphElement> theSelectedGraphEls) {
		
		Set<IRPModelElement> theMatchingEls = 
				GeneralHelpers.findModelElementsIn( theSelectedGraphEls, "Requirement" );
		
		// cast to IRPRequirement
		@SuppressWarnings("unchecked")
		Set<IRPRequirement> theSelectedReqts = (Set<IRPRequirement>)(Set<?>) theMatchingEls;
		
		if (GeneralHelpers.doUnderlyingModelElementsIn( theSelectedGraphEls, "Requirement" )){
			
			// only requirements are selected hence assume only a single operation is needed
			launchThePanel( theSelectedGraphEls.get(0), theSelectedReqts, theActiveProject );
			
		} else {
			
			// launch a dialog for each selected element that is not a requirement
			for (IRPGraphElement theGraphEl : theSelectedGraphEls) {
				
				IRPModelElement theModelObject = theGraphEl.getModelObject();
				
				if (theModelObject != null && !(theModelObject instanceof IRPRequirement)){
					
					// only launch a dialog for non requirement elements
					launchThePanel(	theGraphEl, theSelectedReqts, theActiveProject );
				}		
			}
		}
	}
	
	public CreateIncomingEventPanel(
			IRPGraphElement forSourceGraphElement, 
			IRPClassifier onTargetBlock,
			Set<IRPRequirement> withReqtsAlsoAdded,
			IRPActor theSourceActor, 
			IRPPort theSourceActorPort,
			IRPPackage thePackageForEvent ){
		
		super( forSourceGraphElement, withReqtsAlsoAdded, onTargetBlock );
		
		m_SourceActor = theSourceActor;
		m_SourceActorPort = theSourceActorPort;
		m_PackageForEvent = thePackageForEvent;
		
		final String theSourceText = GeneralHelpers.getActionTextFrom( m_SourceGraphElement.getModelObject() );	
		
		Logger.writeLine("CreateIncomingEventPanel constructor called with text '" + theSourceText + "'");
		
		String theProposedName = determineBestEventName( onTargetBlock, theSourceText );									
		
		Logger.writeLine("The proposed name is '" + theProposedName + "'");
		
		setLayout( new BorderLayout(10,10) );
		setBorder( BorderFactory.createEmptyBorder( 10, 10, 10, 10 ) );
		
		m_EventActionIsNeededCheckBox = new JCheckBox("Populate on diagram?");

		boolean isPopulateOptionHidden = 
				FunctionalAnalysisSettings.getIsPopulateOptionHidden(
						thePackageForEvent.getProject() );
		
		boolean isPopulate = 
				FunctionalAnalysisSettings.getIsPopulateWantedByDefault(
						thePackageForEvent.getProject() );
		
		m_EventActionIsNeededCheckBox.setVisible( !isPopulateOptionHidden );
		m_EventActionIsNeededCheckBox.setSelected( isPopulate );
		
		JPanel thePageStartPanel = new JPanel();
		thePageStartPanel.setLayout( new BoxLayout( thePageStartPanel, BoxLayout.X_AXIS ) );
		thePageStartPanel.add( createChosenNamePanelWith( "Create an event called:  ", theProposedName ) );
		thePageStartPanel.add( m_EventActionIsNeededCheckBox );
				
		add( thePageStartPanel, BorderLayout.PAGE_START );
		
		JPanel theCenterPanel = new JPanel();
		theCenterPanel.setLayout( new BoxLayout( theCenterPanel, BoxLayout.Y_AXIS ) );
		
		m_RequirementsPanel.setAlignmentX(LEFT_ALIGNMENT);
		theCenterPanel.add( m_RequirementsPanel );
		
		m_CreateSendEvent = new JCheckBox();
		m_CreateSendEvent.setSelected(true);
		theCenterPanel.add( m_CreateSendEvent );
	
		m_AttributeCheckBox = new JCheckBox("Add a value argument to change a corresponding attribute on the block");
		
		m_AttributeCheckBox.addActionListener( new ActionListener() {
			
		      public void actionPerformed(ActionEvent actionEvent) {
		    	  
			        AbstractButton abstractButton = (AbstractButton) actionEvent.getSource();
			        
			        boolean selected = abstractButton.getModel().isSelected();
			        
			        m_CheckOperationCheckBox.setEnabled(selected);
			        m_CreateAttributeLabel.setEnabled(selected);
			        m_AttributeNameTextField.setEnabled(selected);
			        m_CreateSendEventViaPanel.setEnabled(selected);
			        
			        m_CheckOperationCheckBox.setSelected(selected);
			        
			        if( selected==false ){
				        m_CreateSendEventViaPanel.setSelected(false);			        	
			        }
			        
			        updateNames();
			        
			      }} );
			   
		theCenterPanel.add( m_AttributeCheckBox );
		
		m_AttributeNamePanel = createAttributeNamePanel( 
				determineBestAttributeName( onTargetBlock, theSourceText ) );
		
		m_AttributeNamePanel.setAlignmentX(LEFT_ALIGNMENT);
	
		theCenterPanel.add( m_AttributeNamePanel );
		
		m_CheckOperationCheckBox = new JCheckBox();
		m_CheckOperationCheckBox.setSelected(false);
		m_CheckOperationCheckBox.setEnabled(false);
		theCenterPanel.add( m_CheckOperationCheckBox );
		
		m_CreateSendEventViaPanel = new JCheckBox();
		m_CreateSendEventViaPanel.setSelected(false);
		m_CreateSendEventViaPanel.setEnabled(false);
		theCenterPanel.add( m_CreateSendEventViaPanel );	
		
		add( theCenterPanel, BorderLayout.WEST );
		add( createOKCancelPanel(), BorderLayout.PAGE_END );

		m_ChosenNameTextField.getDocument().addDocumentListener(
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

	public CreateIncomingEventPanel(
			IRPAttribute forSelectedAttribute, 
			IRPClassifier onTargetBlock,
			Set<IRPRequirement> withReqtsAlsoAdded,
			IRPActor theSourceActor, 
			IRPPort theSourceActorPort,
			IRPPackage thePackageForEvent ){
		
		super( forSelectedAttribute, withReqtsAlsoAdded, onTargetBlock );
		
		m_SourceActor = theSourceActor;
		m_SourceActorPort = theSourceActorPort;
		m_PackageForEvent = thePackageForEvent;
		
		final String theSourceText = forSelectedAttribute.getName();
		
		Logger.writeLine("CreateIncomingEventPanel constructor called with text '" + theSourceText + "'");
		
		String theProposedName = determineBestEventName( onTargetBlock, theSourceText );									
		
		Logger.writeLine("The proposed name is '" + theProposedName + "'");
		
		setLayout( new BorderLayout(10,10) );
		setBorder( BorderFactory.createEmptyBorder( 10, 10, 10, 10 ) );
		
		m_EventActionIsNeededCheckBox = new JCheckBox("Populate on diagram?");
		m_EventActionIsNeededCheckBox.setSelected( false );
		m_EventActionIsNeededCheckBox.setVisible( false );
		
		JPanel thePageStartPanel = new JPanel();
		thePageStartPanel.setLayout( new BoxLayout( thePageStartPanel, BoxLayout.X_AXIS ) );
		thePageStartPanel.add( createChosenNamePanelWith( "Create an event called:  ", theProposedName ) );
		thePageStartPanel.add( m_EventActionIsNeededCheckBox );
				
		add( thePageStartPanel, BorderLayout.PAGE_START );
		
		JPanel theCenterPanel = new JPanel();
		theCenterPanel.setLayout( new BoxLayout( theCenterPanel, BoxLayout.Y_AXIS ) );
		
		m_RequirementsPanel.setAlignmentX(LEFT_ALIGNMENT);
		theCenterPanel.add( m_RequirementsPanel );
		
		m_CreateSendEvent = new JCheckBox();
		m_CreateSendEvent.setSelected(true);
		theCenterPanel.add( m_CreateSendEvent );

		m_CreateSendEventViaPanel = new JCheckBox();
		m_CreateSendEventViaPanel.setSelected(true);
		theCenterPanel.add( m_CreateSendEventViaPanel );
		
		m_AttributeCheckBox = new JCheckBox("Add a value argument to change a corresponding attribute on the block");
		m_AttributeCheckBox.setSelected(true);
		m_AttributeCheckBox.setEnabled(false);
			   
		theCenterPanel.add( m_AttributeCheckBox );
		
		m_AttributeNamePanel = new JPanel();
		m_AttributeNamePanel.setLayout( new BoxLayout(m_AttributeNamePanel, BoxLayout.X_AXIS ) );	
		
		m_CreateAttributeLabel = new JLabel("Use the attribute called:  ");
		m_CreateAttributeLabel.setEnabled(false);
		m_AttributeNamePanel.add( m_CreateAttributeLabel );
		
		m_AttributeNameTextField = new JTextField( forSelectedAttribute.getName() );
		m_AttributeNameTextField.setMaximumSize( new Dimension( 300,20 ) );
		m_AttributeNameTextField.setEnabled(false);
		
		m_AttributeNamePanel.add( m_AttributeNameTextField );
		m_AttributeNamePanel.setAlignmentX(LEFT_ALIGNMENT);
	
		theCenterPanel.add( m_AttributeNamePanel );
		
		m_CheckOperationCheckBox = new JCheckBox();
		m_CheckOperationCheckBox.setSelected(false);
		m_CheckOperationCheckBox.setVisible(false);
		
		theCenterPanel.add( m_CheckOperationCheckBox );
		
		add( theCenterPanel, BorderLayout.WEST );
		add( createOKCancelPanel(), BorderLayout.PAGE_END );

		m_ChosenNameTextField.getDocument().addDocumentListener(
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
			final IRPGraphElement theSourceGraphElement, 
			final Set<IRPRequirement> withReqtsAlsoAdded,
			final IRPProject inProject){
		
		IRPPackage thePackageUnderDev =
				FunctionalAnalysisSettings.getPackageUnderDev( inProject );
		
		if( thePackageUnderDev != null ){

			IRPClass theBuildingBlock = 
					FunctionalAnalysisSettings.getBuildingBlock( thePackageUnderDev );
			
			if( theBuildingBlock != null ){
				
				final IRPPackage thePackageForEvent = 
						FunctionalAnalysisSettings.getPkgThatOwnsEventsAndInterfaces( inProject );
				
				final IRPModelElement theActor = 
						GeneralHelpers.launchDialogToSelectElement(
								getActorsRelatedTo( theBuildingBlock ), "Select Actor", true);
	
				if( theActor != null && theActor instanceof IRPActor ){
					
					final IRPClass theChosenBlock = selectBlockBasedOn(
							(IRPActor) theActor,
							theBuildingBlock,
							"Select Block to send event to:",
							true );
					
					if( theChosenBlock != null ){
						
						IRPPort thePort = GeneralHelpers.getPortThatConnects(
								(IRPActor)theActor, 
								(IRPClassifier)theChosenBlock,
								theBuildingBlock );
						
						if( thePort != null ){
							
							final IRPPort theSourceActorPort = thePort;
							
							javax.swing.SwingUtilities.invokeLater(new Runnable() {

								@Override
								public void run() {
									
									JFrame.setDefaultLookAndFeelDecorated( true );
									
									JFrame frame = new JFrame( "Create an incoming event on " + Logger.elementInfo( theChosenBlock ) + 
											" from " + Logger.elementInfo( theActor ) );
									
									frame.setDefaultCloseOperation( JFrame.DISPOSE_ON_CLOSE );

									CreateIncomingEventPanel thePanel = 
											new CreateIncomingEventPanel(
													theSourceGraphElement, 
													theChosenBlock, 
													withReqtsAlsoAdded,
													(IRPActor)theActor, 
													theSourceActorPort,
													thePackageForEvent );

									frame.setContentPane( thePanel );

									frame.pack();
									frame.setLocationRelativeTo( null );
									frame.setVisible( true );
								}
							});
						}
					}
				}
			}
		}
	}

	public static void launchThePanel(
			final IRPAttribute forSelectedAttribute,
			final Set<IRPRequirement> withReqtsAlsoAdded,
			final IRPProject inProject){
		
		IRPPackage thePackageUnderDev =
				FunctionalAnalysisSettings.getPackageUnderDev( inProject );
		
		if( thePackageUnderDev != null ){

			IRPClass theBuildingBlock = 
					FunctionalAnalysisSettings.getBuildingBlock( thePackageUnderDev );
			
			if( theBuildingBlock != null ){
				
				final IRPPackage thePackageForEvent = 
						FunctionalAnalysisSettings.getPkgThatOwnsEventsAndInterfaces( inProject );
				
				final IRPModelElement theActor = 
						GeneralHelpers.launchDialogToSelectElement(
								getActorsRelatedTo( theBuildingBlock ), "Select Actor", true);
	
				if( theActor != null && theActor instanceof IRPActor ){
					
					List<IRPModelElement> theCandidates = 
							GeneralHelpers.getNonActorOrTestingClassifiersConnectedTo( 
									(IRPActor)theActor, theBuildingBlock );
					
					final IRPModelElement theChosenBlockEl = 
							forSelectedAttribute.getOwner();
					
					if( !theCandidates.contains(theChosenBlockEl) ){
					
						UserInterfaceHelpers.showWarningDialog("The " + Logger.elementInfo( theChosenBlockEl ) + 
								" does not have a port that connects with " + Logger.elementInfo(theActor) + "\n\n" +
								"Fix this and then try again");
						
					} else if( theChosenBlockEl != null && theChosenBlockEl instanceof IRPClassifier ){
										
						final IRPClassifier theChosenBlock = (IRPClassifier)theChosenBlockEl;
						
						IRPPort thePort = GeneralHelpers.getPortThatConnects(
								(IRPActor)theActor, 
								(IRPClassifier)theChosenBlock,
								theBuildingBlock );
						
						if( thePort != null ){
							
							final IRPPort theSourceActorPort = thePort;
							
							javax.swing.SwingUtilities.invokeLater(new Runnable() {

								@Override
								public void run() {
									
									JFrame.setDefaultLookAndFeelDecorated( true );
									
									JFrame frame = new JFrame( "Create an incoming event from " + Logger.elementInfo( theActor ));
									
									frame.setDefaultCloseOperation( JFrame.DISPOSE_ON_CLOSE );

									CreateIncomingEventPanel thePanel = 
											new CreateIncomingEventPanel(
													forSelectedAttribute, 
													theChosenBlock, 
													withReqtsAlsoAdded,
													(IRPActor)theActor,
													theSourceActorPort,
													thePackageForEvent );
										
									frame.setContentPane( thePanel );

									frame.pack();
									frame.setLocationRelativeTo( null );
									frame.setVisible( true );
								}
							});
						}
					}
				}
			}
		}
	}
	
	private String determineBestEventName(
			IRPClassifier onTargetBlock,
			final String theSourceText) {
		
		String[] splitActorName = m_SourceActor.getName().split("_");
		String theActorName = splitActorName[0];
		String theSourceMinusActor = theSourceText.replaceFirst( "^" + theActorName, "" );
		
		String theProposedName = GeneralHelpers.determineUniqueNameBasedOn( 
				GeneralHelpers.toMethodName("req" + theActorName + GeneralHelpers.capitalize(theSourceMinusActor) ), 
				"Event", 
				onTargetBlock.getProject());
		
		return theProposedName;
	}
	
	private String determineBestAttributeName(
			IRPClassifier onTargetBlock,
			final String theSourceText) {
		
		String theProposedName = GeneralHelpers.determineUniqueNameBasedOn( 
				GeneralHelpers.toMethodName( theSourceText ), 
				"Attribute", 
				onTargetBlock );
		
		return theProposedName;
	}
		
	private void updateNames(){
		
		if( m_AttributeCheckBox.isSelected() ){
			m_CreateSendEvent.setText(
					"Add a '" + determineSendEventNameFor( m_ChosenNameTextField.getText() ) 
					+ "' event with 'value' argument to the actor");
		} else {
			m_CreateSendEvent.setText(
					"Add a '" + determineSendEventNameFor( m_ChosenNameTextField.getText() ) 
					+ "' event to the actor");
		}

		m_CreateSendEventViaPanel.setText(
				"Add a '" + determineSendEventViaPanelNameFor( m_ChosenNameTextField.getText() ) 
				+ "' event and '" + m_AttributeNameTextField.getText() + "' attribute to the actor");
		
		m_CheckOpName = determineBestCheckOperationNameFor(
				(IRPClassifier)m_TargetOwningElement, 
				m_AttributeNameTextField.getText() );
		
		m_CheckOperationCheckBox.setText(
				"Add a '" + m_CheckOpName + "' operation to the block that returns the attribute value");
	}
	
	private JPanel createAttributeNamePanel(
			String theProposedName ){
		
		JPanel thePanel = new JPanel();
		thePanel.setLayout( new BoxLayout(thePanel, BoxLayout.X_AXIS ) );	
		
		m_CreateAttributeLabel = new JLabel("Create an attribute called:  ");
		m_CreateAttributeLabel.setEnabled(false);
		thePanel.add( m_CreateAttributeLabel );
		
		m_AttributeNameTextField = new JTextField( theProposedName );
		m_AttributeNameTextField.setMaximumSize( new Dimension( 300,20 ) );
		m_AttributeNameTextField.setEnabled(false);
		
		m_AttributeNameTextField.getDocument().addDocumentListener(
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
		
		thePanel.add( m_AttributeNameTextField );
	
		return thePanel;
	}
	
	private IRPModelElement createTestBenchSendFor(
			IRPEvent theEvent, 
			IRPActor onTheActor, 
			String withSendEventName){
		
		IRPEvent sendEvent = null;
		
		IRPStatechart theStatechart = onTheActor.getStatechart();
		
		if (theStatechart != null){
			
			IRPState theReadyState = getStateCalled("Ready", theStatechart, onTheActor);
			
			if (theReadyState != null){
				
				Logger.writeLine("Creating event called " + withSendEventName 
						+ " on actor called " + onTheActor.getName());
				
				sendEvent = (IRPEvent) theEvent.clone(withSendEventName, onTheActor.getOwner());
				
				Logger.writeLine("The state called " + theReadyState.getFullPathName() + 
						" is owned by " + theReadyState.getOwner().getFullPathName());
				
				IRPTransition theTransition = theReadyState.addInternalTransition( sendEvent );
				
				String actionText = "OPORT( " + m_SourceActorPort.getName() + " )->GEN(" + theEvent.getName() + "(";
				
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
				Logger.writeLine("Error in createTestBenchSendFor, the actor called " 
						+ onTheActor.getFullPathName() + "'s statechart does not have a Ready state");
			}
		} else {
			Logger.writeLine("Unable to proceed as actor called " 
					+ onTheActor.getFullPathName() + " does not have a statechart");
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
			
			IRPState theReadyState = getStateCalled("Ready", theStatechart, onTheActor);
			
			if (theReadyState != null){
				
				Logger.writeLine("Creating event called " + withSendEventName 
						+ " on actor called " + onTheActor.getName());
				
				sendEvent = (IRPEvent) onTheActor.getOwner().addNewAggr("Event", withSendEventName);
				
				Logger.writeLine("The state called " + theReadyState.getFullPathName() + 
						" is owned by " + theReadyState.getOwner().getFullPathName());
				
				IRPTransition theTransition = theReadyState.addInternalTransition( sendEvent );
				
				IRPModelElement existingEl = 
						GeneralHelpers.findElementWithMetaClassAndName(
								"Attribute", theAttribute.getName(), onTheActor );
				
				IRPAttribute theAttributeOwnedByActor;
				
				if( existingEl != null && existingEl instanceof IRPAttribute ){
					
					theAttributeOwnedByActor = (IRPAttribute)existingEl;
				} else {
					theAttributeOwnedByActor = 
							(IRPAttribute) theAttribute.clone(
									theAttribute.getName(), onTheActor );
				}

				String actionText = "OPORT( " + m_SourceActorPort.getName() + " )->GEN( " + 
						theEvent.getName() + "( " + theAttributeOwnedByActor.getName() + " ) );" ;
				
				theTransition.setItsAction(actionText);
						
			} else {
				Logger.writeLine("Error in createTestBenchSendViaPanelFor, the actor called " 
						+ onTheActor.getFullPathName() + "'s statechart does not have a Ready state");
			}
		} else {
			Logger.writeLine("Unable to proceed as actor called " 
					+ onTheActor.getFullPathName() + " does not have a statechart");
		}
		
		return sendEvent;
	}
	
	private IRPState getStateCalled(
			String theName, 
			IRPStatechart inTheDiagram, 
			IRPModelElement ownedByEl){
		
		@SuppressWarnings("unchecked")
		List<IRPModelElement> theElsInDiagram = inTheDiagram.getElementsInDiagram().toList();
		
		IRPState theState = null;
		
		int count = 0;
		
		for (IRPModelElement theEl : theElsInDiagram) {
			
			if (theEl instanceof IRPState 
					&& theEl.getName().equals(theName)
					&& getOwningClassifierFor(theEl).equals(ownedByEl)){
				
				Logger.writeLine("Found state called " + theEl.getName() + " owned by " + theEl.getOwner().getFullPathName());
				
				theState = (IRPState) theEl;
				count++;
			}
		}
		
		if (count != 1){
			Logger.writeLine("Warning in getStateCalled (" + count + ") states called " + theName + " were found");
		}
		
		return theState;
	}
	
	private IRPModelElement getOwningClassifierFor(
			IRPModelElement theState){
		
		IRPModelElement theOwner = theState.getOwner();
		
		while (theOwner.getMetaClass().equals("State") || theOwner.getMetaClass().equals("Statechart")){
			theOwner = theOwner.getOwner();
		}
		
		Logger.writeLine("The owner for " + Logger.elementInfo(theState) + " is " + Logger.elementInfo(theOwner));
			
		return theOwner;
	}	
	
	private void addSetAttributeTransitionToMonitoringStateFor(
			IRPAttribute theAttribute, 
			String triggeredByTheEventName, 
			IRPClassifier theOwnerOfStatechart) {

		IRPStatechart theStatechart = theOwnerOfStatechart.getStatechart();

		IRPState theMonitoringState = 
				getStateCalled("MonitoringConditions", theStatechart, theOwnerOfStatechart);

		if (theMonitoringState != null){
			Logger.writeLine(theMonitoringState, "found");

			IRPTransition theTransition = theMonitoringState.addTransition(theMonitoringState);

			theTransition.setItsTrigger(triggeredByTheEventName);
			theTransition.setItsAction("set" + GeneralHelpers.capitalize(theAttribute.getName()) + "(params->value);");

			Logger.writeLine(theTransition, "was added");	

			IRPGraphElement theGraphEl = findGraphEl(theOwnerOfStatechart, "MonitoringConditions");

			if (theGraphEl != null){
				IRPDiagram theGraphElDiagram = theGraphEl.getDiagram();
				Logger.writeLine(theGraphElDiagram, "related to " 
						+ Logger.elementInfo(theGraphEl.getModelObject()) 
						+ " is the diagram for the GraphEl");

				IRPGraphNode theGraphNode = (IRPGraphNode)theGraphEl;
				
				GraphNodeInfo theNodeInfo = new GraphNodeInfo( theGraphNode );

				IRPGraphEdge theEdge = theGraphElDiagram.addNewEdgeForElement(
						theTransition, 
						theGraphNode, 
						theNodeInfo.getTopRightX(), 
						theNodeInfo.getMiddleY(), 
						theGraphNode, 
						theNodeInfo.getMiddleX(), 
						theNodeInfo.getBottomLeftY());
				
				Logger.writeLine("Added edge to " + theEdge.getModelObject().getFullPathName());
			} else {
				Logger.writeLine("Error in addAnAttributeToMonitoringStateWith, unable to find the MonitoringConditions state");
			}

		} else {
			Logger.writeLine("Error did not find MonitoringConditions state");
		}
	}
	
	private IRPGraphElement findGraphEl(
			IRPClassifier theClassifier, 
			String withTheName) {
		
		IRPGraphElement theFoundGraphEl = null;
		
		@SuppressWarnings("unchecked")
		List<IRPStatechartDiagram> theStatechartDiagrams = 
				theClassifier.getStatechart().getNestedElementsByMetaClass("StatechartDiagram", 1).toList();
		
		for (IRPStatechartDiagram theStatechartDiagram : theStatechartDiagrams) {
			
			Logger.writeLine(theStatechartDiagram, "was found owned by " + Logger.elementInfo(theClassifier));
			
			@SuppressWarnings("unchecked")
			List<IRPGraphElement> theGraphEls = theStatechartDiagram.getGraphicalElements().toList();
			
			for (IRPGraphElement theGraphEl : theGraphEls) {
				
				IRPModelElement theEl = theGraphEl.getModelObject();
				
				if (theEl != null){
					Logger.writeLine("Found " + theEl.getMetaClass() + " called " + theEl.getName());
					
					if (theEl.getName().equals(withTheName)){
						
						Logger.writeLine("Success, found GraphEl called " + withTheName + " in statechart for " + Logger.elementInfo(theClassifier));
						theFoundGraphEl = theGraphEl;
						break;
					}
				}
			}
		}
		
		return theFoundGraphEl;
	}

	@Override
	protected boolean checkValidity(
			boolean isMessageEnabled) {

		String errorMessage = null;
		boolean isValid = true;
		
		String theChosenName = m_ChosenNameTextField.getText();
		String theAttributeName = m_AttributeNameTextField.getText();
		
		boolean isLegalName = GeneralHelpers.isLegalName( theChosenName );
		
		if (!isLegalName){
			
			errorMessage = theChosenName + " is not legal as an identifier representing an executable event\n";				
			isValid = false;
			
		} else if (!GeneralHelpers.isElementNameUnique(
				theChosenName, 
				"Event", 
				m_PackageForEvent.getProject(), 
				1)){

			errorMessage = "Unable to proceed as the event name '" + theChosenName + "' is not unique";
			isValid = false;
			
		} else if (m_AttributeCheckBox.isSelected()){
			
			Logger.writeLine("An attribute is needed, chosen name was " + theAttributeName);
			
			boolean isLegalAttributeName = GeneralHelpers.isLegalName( theAttributeName );
			
			if (!isLegalAttributeName){
				
				errorMessage = theChosenName + " is not legal as an identifier representing an executable attribute\n";				
				isValid = false;

			} else if ( (m_SourceModelElement == null || !(m_SourceModelElement instanceof IRPAttribute)) &&
					!GeneralHelpers.isElementNameUnique(
					theAttributeName, 
					"Attribute", 
					m_TargetOwningElement, 
					0)){

				errorMessage = "Unable to proceed as the attribute name '" + theAttributeName + "' is not unique";
				isValid = false;
			}
		} else if( m_CheckOperationCheckBox.isSelected() && 
				   !GeneralHelpers.isElementNameUnique(
						   m_CheckOpName,
						   "Operation",
						   m_TargetOwningElement, 
						   1 ) ){
			
			errorMessage = "Unable to proceed as the check operation name '" + m_CheckOpName + "' is not unique";
			isValid = false;
		}

		if (isMessageEnabled && !isValid && errorMessage != null){

			UserInterfaceHelpers.showWarningDialog( errorMessage );
		}
		
		return isValid;
	}

	private String determineSendEventNameFor(
			String theSourceEventName ){
		
		return "send_" + theSourceEventName.replaceFirst("req","");
	}

	private String determineSendEventViaPanelNameFor(
			String theSourceEventName ){
		
		return "send_" + theSourceEventName.replaceFirst("req","") + "ViaPanel";
	}
	
	@Override
	protected void performAction() {
		
		try {
			// do silent check first
			if( checkValidity( false ) ){
				
				IRPEvent theEvent = m_PackageForEvent.addEvent( m_ChosenNameTextField.getText() );

				// add value argument before cloning the event to create the test-bench send
				if (m_AttributeCheckBox.isSelected()){
					theEvent.addArgument( "value" );
				}
				
				List<IRPRequirement> selectedReqtsList = m_RequirementsPanel.getSelectedRequirementsList();

				addTraceabilityDependenciesTo( theEvent, selectedReqtsList );

				IRPModelElement theReception = 
						m_TargetOwningElement.addNewAggr( 
								"Reception", m_ChosenNameTextField.getText() );
				
				addTraceabilityDependenciesTo( theReception, selectedReqtsList );		
				
				if ( m_CreateSendEvent.isSelected() ) {

					String theSendEventName = determineSendEventNameFor( theEvent.getName() );
					
					Logger.writeLine( "Send event option was enabled, create event called " + theSendEventName );

					IRPModelElement theTestbenchReception = 
							createTestBenchSendFor( theEvent, (IRPActor) m_SourceActor, theSendEventName );

					theTestbenchReception.highLightElement();
					
				} else {

					Logger.writeLine( "Send event option was not enabled, so skipping this" );
					theReception.highLightElement();
				}

				if ( m_AttributeCheckBox.isSelected() && 
						m_TargetOwningElement instanceof IRPClassifier){
					
					IRPClassifier theClassifier = (IRPClassifier) m_TargetOwningElement;
					
					IRPAttribute theAttribute;
					
					if (m_SourceModelElement != null && m_SourceModelElement instanceof IRPAttribute){
						
						theAttribute = (IRPAttribute)m_SourceModelElement;
						
					} else {
						theAttribute = theClassifier.addAttribute(
								m_AttributeNameTextField.getText() );
						
						theAttribute.setDefaultValue("0");
						addTraceabilityDependenciesTo( theAttribute, selectedReqtsList );
						theAttribute.highLightElement();
					}
					
					IRPDependency theAutoRippleDependency = theAttribute.addDependencyTo( theReception );
					theAutoRippleDependency.addStereotype("AutoRipple", "Dependency");

					if( m_CreateSendEventViaPanel.isSelected() ){
						
						String theSendEventViaPanelName = determineSendEventViaPanelNameFor( theEvent.getName() );
						
						String theSendEventName = determineSendEventNameFor( theEvent.getName() );
						
						Logger.writeLine( "Send event option was enabled, create event called " + theSendEventName );

						IRPModelElement theTestbenchReceptionViaPanel = createTestBenchSendViaPanelFor( 
								theEvent, 
								theAttribute,
								(IRPActor) m_SourceActor, 
								theSendEventViaPanelName );
												
						theTestbenchReceptionViaPanel.highLightElement();
					}
					
					if ( m_CheckOperationCheckBox.isSelected() ){
						
						IRPOperation theCheckOp = addCheckOperationFor( theAttribute, m_CheckOpName );		
						addTraceabilityDependenciesTo( theCheckOp, selectedReqtsList);	
						theCheckOp.highLightElement();
					}

					addSetAttributeTransitionToMonitoringStateFor(
							theAttribute, theEvent.getName(), theClassifier );
				}	
				
				if( m_SourceGraphElement != null ){
					bleedColorToElementsRelatedTo( m_SourceGraphElement );
				}
				
				if( m_EventActionIsNeededCheckBox.isSelected() ){
					populateReceiveEventActionOnDiagram( theEvent );
				}
				
				theEvent.highLightElement();
				
			} else {
				Logger.writeLine( "Error in CreateIncomingEventPanel.performAction, checkValidity returned false" );
			}	
		} catch (Exception e) {
			Logger.writeLine( "Error, unhandled exception detected in CreateIncomingEventPanel.performAction" );
		}
	}
	
	private void populateReceiveEventActionOnDiagram(
			IRPEvent theEvent ){
		
		IRPApplication theRhpApp = FunctionalAnalysisPlugin.getRhapsodyApp();		

		IRPDiagram theDiagram = m_SourceGraphElement.getDiagram();

		if( theDiagram instanceof IRPActivityDiagram ){

			IRPActivityDiagram theAD = (IRPActivityDiagram)theDiagram;

			IRPFlowchart theFlowchart = theAD.getFlowchart();

			IRPAcceptEventAction theAcceptEvent = 
					(IRPAcceptEventAction) theFlowchart.addNewAggr(
							"AcceptEventAction", theEvent.getName() );

			theAcceptEvent.setEvent( theEvent );			
			theFlowchart.addNewNodeForElement( theAcceptEvent, getSourceElementX(), getSourceElementY(), 300, 40 );

			theRhpApp.highLightElement( theAcceptEvent );

		} else if( theDiagram instanceof IRPObjectModelDiagram ){				

			IRPObjectModelDiagram theOMD = (IRPObjectModelDiagram)theDiagram;

			IRPGraphNode theEventNode = theOMD.addNewNodeForElement(theEvent, getSourceElementX() + 50, getSourceElementY() + 50, 300, 40);	

			IRPCollection theGraphElsToDraw = theRhpApp.createNewCollection();
			theGraphElsToDraw.addGraphicalItem( m_SourceGraphElement );
			theGraphElsToDraw.addGraphicalItem( theEventNode );

			theOMD.completeRelations( theGraphElsToDraw, 1 );

			theRhpApp.highLightElement( theEvent );

		} else {
			Logger.writeLine( "Error in CreateOperationPanel.performAction, expected an IRPActivityDiagram" );
		}
	}
}

/**
 * Copyright (C) 2016  MBSE Training and Consulting Limited (www.executablembse.com)

    Change history:
    #022 30-MAY-2016: Improved handling and validation of event/operation creation by adding new forms (F.J.Chadburn)
    #024 30-MAY-2016: Check box to allow user to choose whether to add the check operation (F.J.Chadburn) 
    #029 01-JUN-2016: Add Warning Dialog helper to UserInterfaceHelpers (F.J.Chadburn)
    #030 01-JUN-2016: Improve legal name checking across helpers (F.J.Chadburn)
    #032 05-JUN-2016: Populate call operation/event actions on diagram check-box added (F.J.Chadburn)
    #033 05-JUN-2016: Add support for creation of operations and events from raw requirement selection (F.J.Chadburn)
    #034 05-JUN-2016: Re-factored design to move static constructors into appropriate panel class (F.J.Chadburn)
    #040 17-JUN-2016: Extend populate event/ops to work on OMD, i.e., REQ diagrams (F.J.Chadburn)
    #042 29-JUN-2016: launchThePanel renaming to improve Panel class design consistency (F.J.Chadburn)
    #043 03-JUL-2016: Add Derive downstream reqt for CallOps, InterfaceItems and Event Actions (F.J.Chadburn)
    #044 03-JUL-2016: Minor re-factoring/code corrections (F.J.Chadburn)
    #054 13-JUL-2016: Create a nested BlockPkg package to contain the Block and events (F.J.Chadburn)
    #062 17-JUL-2016: Create InterfacesPkg and correct build issues by adding a Usage dependency (F.J.Chadburn)
    #069 20-JUL-2016: Fix population of events/ops on diagram when creating from a transition (F.J.Chadburn)
    #078 28-JUL-2016: Added isPopulateWantedByDefault tag to FunctionalAnalysisPkg to give user option (F.J.Chadburn)
    #082 09-AUG-2016: Add a check operation check box added to the create attribute dialog (F.J.Chadburn)
    #088 09-AUG-2016: Added a create send event via Panel to the incoming event dialog (F.J.Chadburn)
    #089 15-AUG-2016: Add a pull-down list to select Block when adding events/ops in white box (F.J.Chadburn)
    #090 15-AUG-2016: Fix check operation name issue introduced in fixes #083 and #084 (F.J.Chadburn)
    #093 23-AUG-2016: Added isPopulateOptionHidden tag to allow hiding of the populate check-box on dialogs (F.J.Chadburn)

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