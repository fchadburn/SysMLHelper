package functionalanalysisplugin;

import generalhelpers.GeneralHelpers;
import generalhelpers.Logger;
import generalhelpers.StereotypeAndPropertySettings;
import generalhelpers.TraceabilityHelper;

import java.awt.Component;
import java.awt.ComponentOrientation;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Window;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.SwingUtilities;

import com.telelogic.rhapsody.core.*;

public abstract class CreateTracedElementPanel extends JPanel {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	protected RequirementSelectionPanel m_RequirementsPanel = null;
	protected IRPModelElement m_TargetOwningElement = null;
	protected JTextField m_ChosenNameTextField = null;
	protected IRPGraphElement m_SourceGraphElement = null;
	protected IRPModelElement m_SourceModelElement = null;
	protected IRPDiagram m_SourceGraphElementDiagram = null;
	protected IRPProject m_Project = null;
	protected final String m_Tbd = "Tbd";
	
	SelectedElementContext m_ElementContext;

	public CreateTracedElementPanel(
			IRPGraphElement forSourceGraphElement, 
			Set<IRPRequirement> withReqtsAlsoAdded,
			IRPClassifier onTargetClassifier,
		    IRPProject inProject ) {
		
		super();

		m_TargetOwningElement = onTargetClassifier;		
		m_SourceGraphElement = forSourceGraphElement;
		m_SourceModelElement = m_SourceGraphElement.getModelObject();
		m_Project = inProject;
		
		if( m_SourceModelElement instanceof IRPDiagram ){
			 
			m_SourceGraphElementDiagram = (IRPDiagram) m_SourceModelElement;
		} else {
			m_SourceGraphElementDiagram = m_SourceGraphElement.getDiagram();
		}
		
		setupRequirementsPanel( withReqtsAlsoAdded );
	}

	public CreateTracedElementPanel(
			IRPModelElement forSourceModelElement, 
			Set<IRPRequirement> withReqtsAlsoAdded,
			IRPClassifier onTargetClassifier,
			IRPProject inProject ) {
		
		super();

		m_TargetOwningElement = onTargetClassifier;		
		m_SourceGraphElement = null;
		m_SourceModelElement = forSourceModelElement;
		m_Project = inProject;

		if( m_SourceModelElement instanceof IRPDiagram ){
			m_SourceGraphElementDiagram = (IRPDiagram) m_SourceModelElement;
		}
		
		setupRequirementsPanel( withReqtsAlsoAdded );
	}
	
	protected void setupPopulateCheckbox(
			JCheckBox theCheckbox ) {
		
		// is the diagram an AD or RD?
		if( m_SourceGraphElementDiagram != null && ( 
				m_SourceGraphElementDiagram instanceof IRPActivityDiagram ||
				m_SourceGraphElementDiagram instanceof IRPObjectModelDiagram ) ){

			boolean isPopulateOptionHidden = 
					StereotypeAndPropertySettings.getIsPopulateOptionHidden(
							m_SourceGraphElementDiagram );
			
			boolean isPopulate = 
					StereotypeAndPropertySettings.getIsPopulateWantedByDefault(
							m_SourceGraphElementDiagram );
			
			theCheckbox.setVisible( !isPopulateOptionHidden );
			theCheckbox.setSelected( isPopulate );
			
		} else {
			
			// Not supported
			theCheckbox.setVisible( false );
			theCheckbox.setSelected( false );
		}
	}
	
	private void setupRequirementsPanel(
			Set<IRPRequirement> withReqtsAlsoAdded ){
		
		Set<IRPRequirement> tracedToReqts = 
				TraceabilityHelper.getRequirementsThatTraceFrom( 
						m_SourceModelElement, true );
		
		tracedToReqts.addAll( withReqtsAlsoAdded );
		
		if (tracedToReqts.isEmpty()){	
			m_RequirementsPanel = new RequirementSelectionPanel( 
					"There are no requirements to establish �satisfy� dependencies to",
					tracedToReqts, 
					tracedToReqts );
		} else {
			m_RequirementsPanel = new RequirementSelectionPanel( 
					"With �satisfy� dependencies to:",
					tracedToReqts, 
					tracedToReqts );
		}
		
	}
	
	public JPanel createChosenNamePanelWith(
			String theLabelText,
			String andInitialChosenName ){
		
		JPanel thePanel = new JPanel();
		thePanel.setLayout( new BoxLayout(thePanel, BoxLayout.X_AXIS ) );	
		
		JLabel theLabel =  new JLabel( theLabelText );
		thePanel.add( theLabel );
		
		m_ChosenNameTextField = new JTextField();
		m_ChosenNameTextField.setText( andInitialChosenName );
		m_ChosenNameTextField.setMinimumSize( new Dimension( 350,20 ) );
		m_ChosenNameTextField.setPreferredSize( new Dimension( 350,20 ) );
		m_ChosenNameTextField.setMaximumSize( new Dimension( 350,20 ) );
		
		thePanel.add( m_ChosenNameTextField );
		
		return thePanel;
	}
	
	protected IRPOperation addCheckOperationFor(
			IRPAttribute theAttribute,
			String withTheName ){
		
		IRPOperation theOperation = null;
		
		IRPModelElement theOwner = theAttribute.getOwner();
		
		if( theOwner instanceof IRPClassifier ){
			IRPClassifier theClassifier = (IRPClassifier)theOwner;
			String theAttributeName = theAttribute.getName();
			
			theOperation = theClassifier.addOperation( withTheName );
			
			theOperation.setBody( "OM_RETURN( " + theAttributeName + " );" );
			
			TraceabilityHelper.addAutoRippleDependencyIfOneDoesntExist( 
					theAttribute, theOperation );

			IRPClassifier theType = findTypeCalled( "int" );
			
			if( theType != null ){
				theOperation.setReturns( theType );
			}
		} else {
			Logger.writeLine( "Error in addCheckOperationFor, owner of " + 
					Logger.elementInfo( theAttribute ) + " is not a Classifier" );
		}
		
		return theOperation;
	}
	
	private IRPClassifier findTypeCalled(String theName){
		
		IRPClassifier theTypeFound = null;
		int count = 0;
		
		@SuppressWarnings("unchecked")
		List<IRPModelElement> theTypes = 
				FunctionalAnalysisPlugin.getActiveProject().getNestedElementsByMetaClass("Type", 1).toList();
		
		for (IRPModelElement irpModelElement : theTypes) {			
			
			if (irpModelElement.getName().equals(theName) 
					&& irpModelElement instanceof IRPClassifier){
				theTypeFound = (IRPClassifier) irpModelElement;
				Logger.writeLine(irpModelElement, "was found in findTypeCalled");
				count++;
			}
		}
		
		if (theTypeFound==null){
			Logger.writeLine("Error in findTypeCalled, unable to find type called '" + theName + "'");
		}
		
		if (count>1){
			Logger.writeLine("Warning in findTypeCalled, unexpectedly " + count + " types called '" + theName + "' were found");
		}
		
		return theTypeFound;
	}

	
	// implementation specific provided by parent
	protected abstract boolean checkValidity(boolean isMessageEnabled);
	
	// implementation specific provided by parent
	protected abstract void performAction();
		
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
					Logger.writeLine("Error, unhandled exception in CreateOperationPanel.createOKCancelPanel on OK button action listener");
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
					Logger.writeLine("Error, unhandled exception in CreateOperationPanel.createOKCancelPanel on Cancel button action listener");
				}
			}	
		});
		
		thePanel.setComponentOrientation(ComponentOrientation.LEFT_TO_RIGHT);
		thePanel.add( theOKButton );
		thePanel.add( theCancelButton );
		
		return thePanel;
	}
	
	protected void bleedColorToElementsRelatedTo( 
			IRPGraphElement theGraphEl ){
		
		// only bleed on activity diagrams		
		if (theGraphEl.getDiagram() instanceof IRPActivityDiagram){
			
			String theColorSetting = "255,0,0";
			IRPDiagram theDiagram = theGraphEl.getDiagram();
			IRPModelElement theEl = theGraphEl.getModelObject();
			
			if (theEl != null){
				
				List<IRPRequirement> theSelectedReqts = m_RequirementsPanel.getSelectedRequirementsList();
				
				Logger.writeLine("Setting color to red for " + theEl.getName());
				theGraphEl.setGraphicalProperty("ForegroundColor", theColorSetting);
				
				@SuppressWarnings("unchecked")
				List<IRPDependency> theExistingDeps = theEl.getDependencies().toList();
				
				for (IRPDependency theDependency : theExistingDeps) {
					
					IRPModelElement theDependsOn = theDependency.getDependsOn();
					
					if (theDependsOn != null && 
						theDependsOn instanceof IRPRequirement && 
						theSelectedReqts.contains( theDependsOn )){	
						
						bleedColorToGraphElsRelatedTo( theDependsOn, theColorSetting, theDiagram );
						bleedColorToGraphElsRelatedTo( theDependency, theColorSetting, theDiagram );
					}
				}
			}
		}
	}

	private static void bleedColorToGraphElsRelatedTo(
			IRPModelElement theEl, 
			String theColorSetting, 
			IRPDiagram onDiagram){

		@SuppressWarnings("unchecked")
		List<IRPGraphElement> theGraphElsRelatedToElement = 
				onDiagram.getCorrespondingGraphicElements( theEl ).toList();
		
		for (IRPGraphElement irpGraphElement : theGraphElsRelatedToElement) {
			
			irpGraphElement.setGraphicalProperty("ForegroundColor", theColorSetting);
			
			IRPModelElement theModelObject = irpGraphElement.getModelObject();
			
			if (theModelObject != null){
				Logger.writeLine("Setting color to red for " + theModelObject.getName());
			}
		}
	}
	
	protected void addTraceabilityDependenciesTo(
			IRPModelElement theElement, 
			List<IRPRequirement> theReqtsToAdd ){

		IRPStereotype theDependencyStereotype =
				StereotypeAndPropertySettings.getStereotypeToUseForFunctions( m_TargetOwningElement );
				
		if( theDependencyStereotype != null ){
			
			String theStereotypeName = theDependencyStereotype.getName();
			
			Set<IRPModelElement> theExistingTracedReqts = 
					TraceabilityHelper.getElementsThatHaveStereotypedDependenciesFrom( 
							theElement, theStereotypeName );
			
			for( IRPRequirement theReqt : theReqtsToAdd ) {
				
				if( theExistingTracedReqts.contains( theReqt ) ){
					Logger.writeLine( theElement, "already has a �" + theStereotypeName + 
							"� dependency to " + Logger.elementInfo( theReqt ) + ", so doing nothing" );
				} else {					
					Logger.writeLine( theElement, "does not have a �" + theStereotypeName + 
							"� dependency to " + Logger.elementInfo( theReqt ) + ", so adding one" );
					
					IRPDependency theDep = theElement.addDependencyTo( theReqt );
					theDep.setStereotype( theDependencyStereotype );						
				}
			}
			
		} else {
			Logger.writeLine("Error in addTraceabilityDependenciesTo, unable to find stereotype to apply to dependencies");
		}
	}

	protected static List<IRPModelElement> getNonElapsedTimeActorsRelatedTo(
			 IRPClassifier theBuildingBlock ){
		
		List<IRPModelElement> theActors = new ArrayList<IRPModelElement>();
		
		// get the logical system part and block
		@SuppressWarnings("unchecked")
		List<IRPInstance> theParts = 
 			theBuildingBlock.getNestedElementsByMetaClass("Part", 0).toList();
		
		IRPStereotype theTestbenchStereotype =
				StereotypeAndPropertySettings.getStereotypeForTestbench(
						theBuildingBlock );

		for (IRPInstance thePart : theParts) {
			
			IRPClassifier theOtherClass = thePart.getOtherClass();
			
			if (theOtherClass instanceof IRPActor && 
					GeneralHelpers.hasStereotypeCalled( 
							theTestbenchStereotype.getName(), theOtherClass ) ){
//					!theOtherClass.getName().equals("ElapsedTime") ){
				
				theActors.add((IRPActor) theOtherClass);					
			}
		}
		
		return theActors;
	}
	
	protected int getSourceElementX(){
		
		int x = 10;
		
		if( m_SourceGraphElement != null ){

			if (m_SourceGraphElement instanceof IRPGraphNode){
				GraphNodeInfo theNodeInfo = new GraphNodeInfo( (IRPGraphNode) m_SourceGraphElement );
				
				x = theNodeInfo.getTopLeftX() + 20;
				
			} else if (m_SourceGraphElement instanceof IRPGraphEdge){
				GraphEdgeInfo theNodeInfo = new GraphEdgeInfo( (IRPGraphEdge) m_SourceGraphElement );
				
				x = theNodeInfo.getMidX();
			}
		} else {
			x = 20; // default is top right
		}
		
		return x;
	}
	
	protected int getSourceElementY(){
		
		int y = 10;
		
		if( m_SourceGraphElement != null ){

			if (m_SourceGraphElement instanceof IRPGraphNode){
				GraphNodeInfo theNodeInfo = new GraphNodeInfo( (IRPGraphNode) m_SourceGraphElement );
				
				y = theNodeInfo.getTopLeftY() + 20;
				
			} else if (m_SourceGraphElement instanceof IRPGraphEdge){
				GraphEdgeInfo theNodeInfo = new GraphEdgeInfo( (IRPGraphEdge) m_SourceGraphElement );
				
				y = theNodeInfo.getMidY();
			}
		} else {
			y = 20; // default is top right
		}
		
		return y;
	}

	protected void populateCallOperationActionOnDiagram(
			IRPOperation theOperation ){

		try {
			IRPApplication theRhpApp = FunctionalAnalysisPlugin.getRhapsodyApp();

			if( m_SourceGraphElementDiagram != null ){

				if( m_SourceGraphElementDiagram instanceof IRPActivityDiagram ){
					
					IRPActivityDiagram theAD = (IRPActivityDiagram)m_SourceGraphElementDiagram;

					IRPFlowchart theFlowchart = theAD.getFlowchart();

					if( m_SourceGraphElement != null && 
						m_SourceGraphElement.getModelObject() instanceof IRPCallOperation ){

						IRPCallOperation theCallOp = (IRPCallOperation) m_SourceGraphElement.getModelObject();
						theCallOp.setOperation(theOperation);

					} else {
						IRPCallOperation theCallOp = 
								(IRPCallOperation) theFlowchart.addNewAggr(
										"CallOperation", theOperation.getName() );

						theCallOp.setOperation(theOperation);

						theFlowchart.addNewNodeForElement(
								theCallOp, getSourceElementX(), getSourceElementY(), 300, 40 );

						theCallOp.highLightElement();
					}

				} else if( m_SourceGraphElementDiagram instanceof IRPObjectModelDiagram ){

					IRPObjectModelDiagram theOMD = (IRPObjectModelDiagram)m_SourceGraphElementDiagram;

					IRPGraphNode theEventNode = theOMD.addNewNodeForElement( 
							theOperation, getSourceElementX() + 50, getSourceElementY() + 50, 300, 40 );	

					if( m_SourceGraphElement != null ){
						IRPCollection theGraphElsToDraw = theRhpApp.createNewCollection();
						theGraphElsToDraw.addGraphicalItem( m_SourceGraphElement );
						theGraphElsToDraw.addGraphicalItem( theEventNode );

						theOMD.completeRelations( theGraphElsToDraw, 1 );
					}
					
					theOperation.highLightElement();

				} else {
					Logger.writeLine( "Error in populateCallOperationActionOnDiagram " + Logger.elementInfo( m_SourceGraphElementDiagram ) + 
							" is not supported for populating on");
				}

			} else {	
				Logger.writeLine( "Error in populateCallOperationActionOnDiagram, m_SourceGraphElementDiagram is null when value was expected" );
			}

		} catch (Exception e) {
			Logger.writeLine( "Error in populateCallOperationActionOnDiagram, unhandled exception was detected ");
		}
	}
	
	protected static IRPClass getBlock(
			final IRPGraphElement theSourceGraphElement,
			final IRPModelElement orTheModelElement, 
			final String theMsg ){
		
		IRPClass theBlock = null;
		
		if( theSourceGraphElement != null ){
			
			IRPModelElement theModelObject = theSourceGraphElement.getModelObject();
			
			if( theModelObject != null ){

				if( theModelObject instanceof IRPClass &&
					!GeneralHelpers.hasStereotypeCalled( "TestDriver", theModelObject ) ){

					theBlock = (IRPClass) theModelObject;

				} else if( theModelObject instanceof IRPInstance ){

					IRPInstance thePart = (IRPInstance) theModelObject;

					IRPClassifier theOtherClass = thePart.getOtherClass();

					if( theOtherClass instanceof IRPClass &&
						!GeneralHelpers.hasStereotypeCalled( "TestDriver", theOtherClass ) ){

						theBlock = (IRPClass)theOtherClass;
					}
				}
			}

		} else if( orTheModelElement != null ){

			Logger.writeLine(orTheModelElement.getMetaClass() + "is the MetaClass");
			
			if( orTheModelElement instanceof IRPClass &&
				!GeneralHelpers.hasStereotypeCalled( "TestDriver", orTheModelElement ) ){

				theBlock = (IRPClass) orTheModelElement;

			} else if( orTheModelElement instanceof IRPInstance ){

				IRPInstance thePart = (IRPInstance) orTheModelElement;

				IRPClassifier theOtherClass = thePart.getOtherClass();

				if( theOtherClass instanceof IRPClass &&
					!GeneralHelpers.hasStereotypeCalled( "TestDriver", theOtherClass ) ){
					
					theBlock = (IRPClass)theOtherClass;
				}
				
			} else if( orTheModelElement.getMetaClass().equals("StatechartDiagram") ){
		
				IRPModelElement theOwner = 
						GeneralHelpers.findOwningClassIfOneExistsFor( orTheModelElement );
				
				Logger.writeLine( theOwner, "is the Owner");
				
				if( theOwner instanceof IRPClass &&
					!GeneralHelpers.hasStereotypeCalled( "TestDriver", theOwner )){
					
					theBlock = (IRPClass) theOwner;
				}
			}
		}

		if( theBlock == null ){
			
			IRPModelElement theContextEl = null;
			
			if( orTheModelElement != null ){
				theContextEl = orTheModelElement;
			} else if( theSourceGraphElement != null ){
				theContextEl = theSourceGraphElement.getModelObject();
			}
			
			if( theContextEl != null ){
				
				theBlock = FunctionalAnalysisSettings.getBlockUnderDev( 
						theContextEl, theMsg );
			} else {
				Logger.writeLine("Error in getBlock");
			}

		}
		
		return theBlock;
	}
	
	protected IRPAttribute addAttributeTo( 
			IRPClassifier theClassifier, 
			String withTheName, 
			String andDefaultValue,
			List<IRPRequirement> withTraceabilityReqts ){
		
		IRPAttribute theAttribute = theClassifier.addAttribute( withTheName );				
		
		IRPModelElement theValuePropertyStereotype = 
				GeneralHelpers.findElementWithMetaClassAndName( 
						"Stereotype", "ValueProperty", m_Project );
		
		if( theValuePropertyStereotype != null ){
			
			Logger.writeLine( "Invoking change to from " + Logger.elementInfo( theAttribute ) + 
					" to " + Logger.elementInfo( theValuePropertyStereotype ) );
			
			theAttribute.changeTo( "ValueProperty" );
		}
		
		theAttribute.setDefaultValue( andDefaultValue );
		theAttribute.highLightElement();

		addTraceabilityDependenciesTo( theAttribute, withTraceabilityReqts );

		return theAttribute;
	}
}

/**
 * Copyright (C) 2016-2019  MBSE Training and Consulting Limited (www.executablembse.com)

    Change history:
    #022 30-MAY-2016: Improved handling and validation of event/operation creation by adding new forms (F.J.Chadburn) 
    #032 05-JUN-2016: Populate call operation/event actions on diagram check-box added (F.J.Chadburn)
    #033 05-JUN-2016: Add support for creation of operations and events from raw requirement selection (F.J.Chadburn)
    #034 05-JUN-2016: Re-factored design to move static constructors into appropriate panel class (F.J.Chadburn)
    #040 17-JUN-2016: Extend populate event/ops to work on OMD, i.e., REQ diagrams (F.J.Chadburn)
    #041 29-JUN-2016: Derive downstream requirement menu added for reqts on diagrams (F.J.Chadburn)
    #043 03-JUL-2016: Add Derive downstream reqt for CallOps, InterfaceItems and Event Actions (F.J.Chadburn)
    #058 13-JUL-2016: Dropping CallOp on diagram now gives option to create Op on block (F.J.Chadburn)
    #069 20-JUL-2016: Fix population of events/ops on diagram when creating from a transition (F.J.Chadburn)
    #082 09-AUG-2016: Add a check operation check box added to the create attribute dialog (F.J.Chadburn)
    #083 09-AUG-2016: Add an Update attribute menu option and panel with add check operation option (F.J.Chadburn)
    #089 15-AUG-2016: Add a pull-down list to select Block when adding events/ops in white box (F.J.Chadburn)
    #090 15-AUG-2016: Fix check operation name issue introduced in fixes #083 and #084 (F.J.Chadburn)
    #099 14-SEP-2016: Allow event and operation creation from right-click on AD and RD diagram canvas (F.J.Chadburn)
    #105 03-NOV-2016: Only bleed to requirements checked for coverage (F.J.Chadburn)
    #115 13-NOV-2016: Removed use of isEnableBlockSelectionByUser tag and <<LogicalSystem>> by helper (F.J.Chadburn)
    #125 25-NOV-2016: AutoRipple used in UpdateTracedAttributePanel to keep check and FlowPort name updated (F.J.Chadburn)
    #129 25-NOV-2016: Fixed addTraceabilityDependenciesTo to avoid creation of duplicate dependencies (F.J.Chadburn)
    #130 25-NOV-2016: Improved consistency in handling of isPopulateOptionHidden and isPopulateWantedByDefault tags (F.J.Chadburn)
    #163 05-FEB-2017: Add new menus to Smart link: Start and Smart link: End (F.J.Chadburn)
    #196 05-JUN-2017: Enhanced create traced element dialogs to be context aware for blocks/parts (F.J.Chadburn)
    #197 05-JUN-2017: Fix 8.2 issue in Incoming Event panel, create ValueProperty rather than attribute (F.J.Chadburn)
    #199 05-JUN-2017: Improved create event panel consistency to name event Tbd if no text provided (F.J.Chadburn)
    #200 05-JUN-2017: Hide Populate on diagram check-boxes if context is not valid (F.J.Chadburn)
    #209 04-JUL-2017: Populate requirements for SD(s) based on messages now supported with Dialog (F.J.Chadburn)
    #224 25-AUG-2017: Added new menu to roll up traceability to the transition and populate on STM (F.J.Chadburn)
    #227 06-SEP-2017: Increased robustness to stop smart link panel using non new term version of <<refine>> (F.J.Chadburn)
    #252 29-MAY-2019: Implement generic features for profile/settings loading (F.J.Chadburn)
    #256 29-MAY-2019: Rewrite to Java Swing dialog launching to make thread safe between versions (F.J.Chadburn)

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
