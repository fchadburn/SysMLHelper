package com.mbsetraining.sysmlhelper.tracedelementpanels;

import java.awt.BorderLayout;
import java.awt.Color;
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

import javax.swing.BorderFactory;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.JTextPane;
import javax.swing.SwingUtilities;
import javax.swing.text.SimpleAttributeSet;
import javax.swing.text.StyleConstants;
import javax.swing.text.StyledDocument;

import com.mbsetraining.sysmlhelper.common.GraphEdgeInfo;
import com.mbsetraining.sysmlhelper.common.GraphNodeInfo;
import com.mbsetraining.sysmlhelper.executablembse.ExecutableMBSE_Context;
import com.telelogic.rhapsody.core.*;

public abstract class CreateTracedElementPanel extends JPanel {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	protected RequirementSelectionPanel _requirementSelectionPanel = null;
	protected JTextField _chosenNameTextField = null;
	protected final String _tbd = "Tbd";
	protected IRPApplication _rhpApp;
	
	protected ExecutableMBSE_Context _context;

	public CreateTracedElementPanel(
			String theAppID ){
		
		super();
		
		_context = new ExecutableMBSE_Context( theAppID );
		
		_context.debug( "CreateTracedElementPanel constructor was invoked" );
		
		setupRequirementsPanel();
	}
	
	protected void setupPopulateCheckbox(
			JCheckBox theCheckbox ) {
		
		IRPDiagram theSourceGraphElementDiagram = _context.get_selectedContext().getSourceDiagram();
		
		// is the diagram an AD or RD?
		if( theSourceGraphElementDiagram != null && ( 
				theSourceGraphElementDiagram instanceof IRPActivityDiagram ||
				theSourceGraphElementDiagram instanceof IRPObjectModelDiagram ) ){

			boolean isPopulateOptionHidden = 
					_context.getIsPopulateOptionHidden(
							theSourceGraphElementDiagram );
			
			boolean isPopulate = 
					_context.getIsPopulateWantedByDefault(
							theSourceGraphElementDiagram );
			
			theCheckbox.setVisible( !isPopulateOptionHidden );
			theCheckbox.setSelected( isPopulate );
			
		} else {
			
			// Not supported
			theCheckbox.setVisible( false );
			theCheckbox.setSelected( false );
		}
	}
	
	private void setupRequirementsPanel(){
		
		Set<IRPRequirement> tracedToReqts = 
				_context.getRequirementsThatTraceFrom( 
						_context.get_selectedContext().getSelectedEl(), 
						true );
		
		tracedToReqts.addAll( _context.get_selectedContext().getSelectedReqts() );
		
		if (tracedToReqts.isEmpty()){	
			_requirementSelectionPanel = new RequirementSelectionPanel( 
					"There are no requirements to establish «satisfy» dependencies to",
					tracedToReqts, 
					tracedToReqts,
					_context );
		} else {
			_requirementSelectionPanel = new RequirementSelectionPanel( 
					"With «satisfy» dependencies to:",
					tracedToReqts, 
					tracedToReqts,
					_context );
		}
	}
	
	public JPanel createChosenNamePanelWith(
			String theLabelText,
			String andInitialChosenName ){
		
		JPanel thePanel = new JPanel();
		thePanel.setLayout( new BoxLayout(thePanel, BoxLayout.X_AXIS ) );	
		
		JLabel theLabel =  new JLabel( theLabelText );
		thePanel.add( theLabel );
		
		_chosenNameTextField = new JTextField();
		_chosenNameTextField.setText( andInitialChosenName );
		_chosenNameTextField.setMinimumSize( new Dimension( 350,20 ) );
		_chosenNameTextField.setPreferredSize( new Dimension( 350,20 ) );
		_chosenNameTextField.setMaximumSize( new Dimension( 350,20 ) );
		
		thePanel.add( _chosenNameTextField );
		
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
			
			_context.addAutoRippleDependencyIfOneDoesntExist( 
					theAttribute, theOperation );
			
			IRPModelElement theType = 
					_context.findElementWithMetaClassAndName( 
							"Type", 
							"int", 
							theAttribute.getProject() );
			
			if( theType != null && 
					theType instanceof IRPClassifier ){
				
				theOperation.setReturns( (IRPClassifier) theType );
			} else {
				_context.error( "Error in addCheckOperationFor, unable to find Type called int" );
			}
		} else {
			_context.error( "Error in addCheckOperationFor, owner of " + 
					_context.elInfo( theAttribute ) + " is not a Classifier" );
		}
		
		return theOperation;
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
			public void actionPerformed( ActionEvent e ) {
				
				try {
					boolean isValid = checkValidity( true );
					
					if( isValid ){
						performAction();
						Window dialog = SwingUtilities.windowForComponent( (Component) e.getSource() );
						dialog.dispose();
					}		
												
				} catch( Exception e2 ){
					_context.error( "Unhandled exception in CreateTracedElementPanel.createOKCancelPanel on OK button action listener, e2=" + e2.getMessage() );
				}
			}
		});
		
		JButton theCancelButton = new JButton( "Cancel" );
		theCancelButton.setPreferredSize(new Dimension(75,25));
		
		theCancelButton.addActionListener( new ActionListener() {
			
			@Override
			public void actionPerformed( ActionEvent e ) {
				
				try {
					Window dialog = SwingUtilities.windowForComponent( (Component) e.getSource() );
					dialog.dispose();
												
				} catch( Exception e2 ){
					_context.error( "Unhandled exception in CreateTracedElementPanel.createOKCancelPanel on Cancel button action listener, e2=" + e2.getMessage() );
				}
			}	
		});
		
		thePanel.setComponentOrientation(ComponentOrientation.LEFT_TO_RIGHT);
		thePanel.add( theOKButton );
		thePanel.add( theCancelButton );
		
		return thePanel;
	}
	
	public JPanel createCancelPanel(){
		
		JPanel thePanel = new JPanel();
		thePanel.setLayout( new FlowLayout() );
				
		JButton theCancelButton = new JButton( "Cancel" );
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
		thePanel.add( theCancelButton );
		
		return thePanel;
	}
	
	protected Component createPanelWithTextCentered(
			String theText){
		
		JTextPane theTextPane = new JTextPane();
		theTextPane.setBorder( BorderFactory.createEmptyBorder( 10, 10, 10, 10 ) );
		theTextPane.setBackground( new Color( 238, 238, 238 ) );
		theTextPane.setEditable( false );
		theTextPane.setText( theText );
		
		StyledDocument theStyledDoc = theTextPane.getStyledDocument();
		SimpleAttributeSet center = new SimpleAttributeSet();
		StyleConstants.setAlignment( center, StyleConstants.ALIGN_CENTER );

		theStyledDoc.setParagraphAttributes( 0, theStyledDoc.getLength(), center, false );

		JPanel thePanel = new JPanel();
		thePanel.add( theTextPane );
		
		return thePanel;
	}
	
	protected void buildUnableToRunDialog(
			String withMsg ){
		
		setLayout( new BorderLayout(10,10) );
		setBorder( BorderFactory.createEmptyBorder( 10, 10, 10, 10 ) );

		JPanel thePageStartPanel = new JPanel();
		thePageStartPanel.setLayout( new BoxLayout( thePageStartPanel, BoxLayout.X_AXIS ) );
		thePageStartPanel.add( createPanelWithTextCentered( withMsg ) );

		add( thePageStartPanel, BorderLayout.PAGE_START );
		
		add( createCancelPanel(), BorderLayout.PAGE_END );

	}
	
	protected void bleedColorToElementsRelatedTo( 
			IRPGraphElement theGraphEl ){
		
		// only bleed on activity diagrams		
		if( theGraphEl.getDiagram() instanceof IRPActivityDiagram ){
			
			String theColorSetting = "255,0,0";
			IRPDiagram theDiagram = theGraphEl.getDiagram();
			IRPModelElement theEl = theGraphEl.getModelObject();
			
			if (theEl != null){
				
				List<IRPRequirement> theSelectedReqts = _requirementSelectionPanel.getSelectedRequirementsList();
				
				_context.debug("Setting color to red for " + theEl.getName());
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

	private void bleedColorToGraphElsRelatedTo(
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
				_context.debug("Setting color to red for " + theModelObject.getName());
			}
		}
	}
	
	protected void addTraceabilityDependenciesTo(
			IRPModelElement theElement, 
			List<IRPRequirement> theReqtsToAdd ){

		IRPStereotype theDependencyStereotype =
				_context.getStereotypeToUseForFunctions( 
						_context.get_selectedContext().getChosenBlock() );
				
		if( theDependencyStereotype != null ){
			
			String theStereotypeName = theDependencyStereotype.getName();
			
			Set<IRPModelElement> theExistingTracedReqts = 
					_context.getElementsThatHaveStereotypedDependenciesFrom( 
							theElement, theStereotypeName );
			
			for( IRPRequirement theReqt : theReqtsToAdd ) {
				
				if( theExistingTracedReqts.contains( theReqt ) ){
					_context.info( _context.elInfo( theElement ) + " already has a «" + theStereotypeName + 
							"» dependency to " + _context.elInfo( theReqt ) + 
							", so doing nothing" );
					
				} else if( theReqt.isRemote()== 1 ){
					
					_context.info( "Add remote " +
							_context.elInfo( theDependencyStereotype) + " from " +
							_context.elInfo(theElement) + " \n" +
							"to " + _context.elInfo(theReqt) );
					
					if( theDependencyStereotype.getName().equals( "satisfy" ) ){
						
						// "Derives From", "Refines", "Satisfies", and "Trace".
						theElement.addRemoteDependencyTo( theReqt, "Satisfies" );
					}
					
				} else {					
					_context.info( _context.elInfo( theElement ) + " does not have a «" + theStereotypeName + 
							"» dependency to " + _context.elInfo( theReqt ) + 
							", so adding one" );
					
					IRPDependency theDep = theElement.addDependencyTo( theReqt );
					theDep.setStereotype( theDependencyStereotype );						
				}
			}
			
		} else {
			_context.error("Error in addTraceabilityDependenciesTo, unable to find stereotype to apply to dependencies");
		}
	}

	protected List<IRPModelElement> getNonElapsedTimeActorsRelatedTo(
			 IRPClassifier theBuildingBlock ){
		
		List<IRPModelElement> theActors = new ArrayList<IRPModelElement>();
		
		// get the logical system part and block
		@SuppressWarnings("unchecked")
		List<IRPInstance> theParts = 
 			theBuildingBlock.getNestedElementsByMetaClass("Part", 0).toList();
		
		IRPStereotype theTestbenchStereotype =
				_context.getStereotypeForTestbench(
						theBuildingBlock );

		for (IRPInstance thePart : theParts) {
			
			IRPClassifier theOtherClass = thePart.getOtherClass();
			
			if (theOtherClass instanceof IRPActor && 
					_context.hasStereotypeCalled( 
							theTestbenchStereotype.getName(), 
							theOtherClass ) ){
//					!theOtherClass.getName().equals("ElapsedTime") ){
				
				theActors.add((IRPActor) theOtherClass);					
			}
		}
		
		return theActors;
	}
	
	protected int getSourceElementX(){
		
		int x = 10;
		
		IRPGraphElement theSourceGraphEl = _context.get_selectedContext().getSelectedGraphEl();
		
		if( theSourceGraphEl != null ){

			if( theSourceGraphEl instanceof IRPGraphNode ){
				
				GraphNodeInfo theNodeInfo = 
						new GraphNodeInfo( (IRPGraphNode) theSourceGraphEl, _context );
				
				x = theNodeInfo.getTopLeftX() + 20;
				
			} else if( theSourceGraphEl instanceof IRPGraphEdge ){
				
				GraphEdgeInfo theNodeInfo = 
						new GraphEdgeInfo( (IRPGraphEdge) theSourceGraphEl, _context );
				
				x = theNodeInfo.getMidX();
			}
		} else {
			x = 20; // default is top right
		}
		
		return x;
	}
	
	protected int getSourceElementY(){
		
		int y = 10;

		IRPGraphElement theSourceGraphEl = _context.get_selectedContext().getSelectedGraphEl();

		if( theSourceGraphEl != null ){

			if( theSourceGraphEl instanceof IRPGraphNode ){
				GraphNodeInfo theNodeInfo = 
						new GraphNodeInfo( (IRPGraphNode) theSourceGraphEl, _context );
				
				y = theNodeInfo.getTopLeftY() + 20;
				
			} else if( theSourceGraphEl instanceof IRPGraphEdge ){
				GraphEdgeInfo theNodeInfo = 
						new GraphEdgeInfo( (IRPGraphEdge) theSourceGraphEl, _context );
				
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
			IRPApplication theRhpApp = _context.get_rhpApp();

			IRPDiagram theDiagram = _context.get_selectedContext().getSourceDiagram();
			IRPGraphElement theGraphEl = _context.get_selectedContext().getSelectedGraphEl();
			
			if( theDiagram != null ){

				if( theDiagram instanceof IRPActivityDiagram ){
					
					IRPActivityDiagram theAD = (IRPActivityDiagram)theDiagram;

					IRPFlowchart theFlowchart = theAD.getFlowchart();

					if( theGraphEl != null && 
							theGraphEl.getModelObject() instanceof IRPCallOperation ){

						IRPCallOperation theCallOp = (IRPCallOperation) theGraphEl.getModelObject();
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

				} else if( theDiagram instanceof IRPObjectModelDiagram ){

					IRPObjectModelDiagram theOMD = (IRPObjectModelDiagram)theDiagram;

					IRPGraphNode theEventNode = theOMD.addNewNodeForElement( 
							theOperation, getSourceElementX() + 50, getSourceElementY() + 50, 300, 40 );	

					if( theGraphEl != null ){
						IRPCollection theGraphElsToDraw = theRhpApp.createNewCollection();
						theGraphElsToDraw.addGraphicalItem( theGraphEl );
						theGraphElsToDraw.addGraphicalItem( theEventNode );

						theOMD.completeRelations( theGraphElsToDraw, 1 );
					}
					
					theOperation.highLightElement();

				} else {
					_context.error( "Error in populateCallOperationActionOnDiagram " + _context.elInfo( theDiagram ) + 
							" is not supported for populating on");
				}

			} else {	
				_context.error( "Error in populateCallOperationActionOnDiagram, m_SourceGraphElementDiagram is null when value was expected" );
			}

		} catch (Exception e) {
			_context.error( "Error in populateCallOperationActionOnDiagram, unhandled exception was detected ");
		}
	}
	
	protected IRPClass getBlock(
			final IRPGraphElement theSourceGraphElement,
			final IRPModelElement orTheModelElement, 
			final String theMsg ){
		
		IRPClass theBlock = null;
		
		if( theSourceGraphElement != null ){
			
			IRPModelElement theModelObject = theSourceGraphElement.getModelObject();
			
			if( theModelObject != null ){

				if( theModelObject instanceof IRPClass &&
					!_context.hasStereotypeCalled( "TestDriver", theModelObject ) ){

					theBlock = (IRPClass) theModelObject;

				} else if( theModelObject instanceof IRPInstance ){

					IRPInstance thePart = (IRPInstance) theModelObject;

					IRPClassifier theOtherClass = thePart.getOtherClass();

					if( theOtherClass instanceof IRPClass &&
						!_context.hasStereotypeCalled( "TestDriver", theOtherClass ) ){

						theBlock = (IRPClass)theOtherClass;
					}
				}
			}

		} else if( orTheModelElement != null ){

			_context.debug(orTheModelElement.getMetaClass() + "is the MetaClass");
			
			if( orTheModelElement instanceof IRPClass &&
				!_context.hasStereotypeCalled( "TestDriver", orTheModelElement ) ){

				theBlock = (IRPClass) orTheModelElement;

			} else if( orTheModelElement instanceof IRPInstance ){

				IRPInstance thePart = (IRPInstance) orTheModelElement;

				IRPClassifier theOtherClass = thePart.getOtherClass();

				if( theOtherClass instanceof IRPClass &&
					!_context.hasStereotypeCalled( "TestDriver", theOtherClass ) ){
					
					theBlock = (IRPClass)theOtherClass;
				}
				
			} else if( orTheModelElement.getMetaClass().equals("StatechartDiagram") ){
		
				IRPModelElement theOwner = 
						_context.findOwningClassIfOneExistsFor( orTheModelElement );
				
				_context.debug( _context.elInfo( theOwner ) + "is the Owner");
				
				if( theOwner instanceof IRPClass &&
					!_context.hasStereotypeCalled( "TestDriver", theOwner )){
					
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
				
				theBlock = _context.get_selectedContext().getBlockUnderDev( 
						theContextEl, theMsg );
			} else {
				_context.error("Error in getBlock");
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
				_context.findElementWithMetaClassAndName( 
						"Stereotype", 
						"ValueProperty", 
						theClassifier.getProject() );
		
		if( theValuePropertyStereotype != null ){
			
			_context.debug( "Invoking change to from " + _context.elInfo( theAttribute ) + 
					" to " + _context.elInfo( theValuePropertyStereotype ) );
			
			theAttribute.changeTo( "ValueProperty" );
		}
		
		theAttribute.setDefaultValue( andDefaultValue );
		theAttribute.highLightElement();

		addTraceabilityDependenciesTo( theAttribute, withTraceabilityReqts );

		return theAttribute;
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
