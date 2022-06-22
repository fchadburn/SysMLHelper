package com.mbsetraining.sysmlhelper.tracedelementpanels;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.List;

import javax.swing.AbstractButton;
import javax.swing.BorderFactory;
import javax.swing.BoxLayout;
import javax.swing.ButtonGroup;
import javax.swing.JCheckBox;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.JTextField;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;

import com.mbsetraining.sysmlhelper.common.UserInterfaceHelper;
import com.mbsetraining.sysmlhelper.pubsubportcreation.PortCreator;
import com.telelogic.rhapsody.core.*;

public class CreateTracedAttributePanel extends CreateTracedElementPanel {
	
	/**
	 * 
	 */
	private static final long serialVersionUID = -5130829523852064061L;
	protected JTextField _initialValueTextField = null;
	private JCheckBox _checkOperationCheckBox;
	private String _checkOpName;
	private JCheckBox _callOperationIsNeededCheckBox;
	private JRadioButton _noFlowPortRadioButton;
	private JRadioButton _pubFlowPortRadioButton;
	private JRadioButton _subFlowPortRadioButton;
	
	public static void main(String[] args) {
		IRPApplication theRhpApp = RhapsodyAppServer.getActiveRhapsodyApplication();
		launchThePanel( theRhpApp.getApplicationConnectionString() );
	}
	
	public static void launchThePanel(
			String theAppID ){

		javax.swing.SwingUtilities.invokeLater( new Runnable(){

			@Override
			public void run() {

				JFrame.setDefaultLookAndFeelDecorated( true );

				UserInterfaceHelper.setLookAndFeel();

				JFrame frame = new JFrame(
						"Create attribute/flow-port/check operation" );

				frame.setDefaultCloseOperation( JFrame.DISPOSE_ON_CLOSE );

				CreateTracedAttributePanel thePanel = new CreateTracedAttributePanel(
						theAppID );

				frame.setContentPane( thePanel );
				frame.pack();
				frame.setLocationRelativeTo( null );
				frame.setVisible( true );
			}
		});
	}
	
	public CreateTracedAttributePanel(
			String theAppID ){
		
		super( theAppID );
			
		IRPClass theBuildingBlock = 
				_context.get_selectedContext().getBuildingBlock();

		if( theBuildingBlock == null ){
			
			buildUnableToRunDialog( 
					"Sorry, this helper is unable to run this command because \n" +
					"there was no execution context or block found in the model. \n " +
					"You need to add the relevant package structure first." );
			
		} else { // theBuildingBlock != null
			
			IRPClass theBlock = _context.get_selectedContext().getBlockUnderDev(
					"Which Block do you want to add the Operation to?" );
			
			if( theBlock == null ){
				buildUnableToRunDialog( 
						"Sorry, this helper is unable to run this command because \n" +
						"there was no execution context or block found in the model. \n " +
						"You need to add the relevant package structure first." );
			} else {
				
				IRPModelElement theModelObject = _context.getSelectedElement( true );
				
				String theSourceText = "attributeName";
				
				if( theModelObject instanceof IRPState ){
					String theActionText = _context.getActionTextFrom( theModelObject );
					
					if( theActionText != null ){
						theSourceText = theActionText;
					}					
				}
				
				createCommonContent( _context.get_selectedContext().getChosenBlock(), theSourceText );
			}
		}
	}
	
	private void createCommonContent(
			IRPClassifier onTargetBlock,
			String theSourceText) {
		
		String theProposedName = _context.determineUniqueNameBasedOn( 
				_context.toMethodName( theSourceText, 40 ), 
				"Attribute", 
				onTargetBlock );

		//_context.debug("The proposed name is '" + theProposedName + "'");
		
		JPanel thePageStartPanel = new JPanel();
		thePageStartPanel.setLayout( new BoxLayout( thePageStartPanel, BoxLayout.X_AXIS ) );
		
		JPanel theNamePanel = createChosenNamePanelWith( "Create an attribute called:  ", theProposedName );
		theNamePanel.setAlignmentX(LEFT_ALIGNMENT);
		thePageStartPanel.add( theNamePanel );

		JPanel theInitialValuePanel = createInitialValuePanel( "0" );
		theInitialValuePanel.setAlignmentX( LEFT_ALIGNMENT );
		thePageStartPanel.add( theInitialValuePanel );

		JPanel theCenterPanel = new JPanel();
		theCenterPanel.setLayout( new BoxLayout( theCenterPanel, BoxLayout.Y_AXIS ) );

		_checkOperationCheckBox = new JCheckBox();
		_checkOperationCheckBox.setBorder( BorderFactory.createEmptyBorder( 0, 0, 10, 0 ) );

		_checkOperationCheckBox.addActionListener( new ActionListener() {
			
		      public void actionPerformed(ActionEvent actionEvent) {
		    	  
			        AbstractButton abstractButton = (AbstractButton) actionEvent.getSource();
			        
			        boolean selected = abstractButton.getModel().isSelected();
					
			        IRPDiagram theSourceDiagram = _context.get_selectedContext().getSourceDiagram();
			        
					boolean isPopulate = false;
					
					if( theSourceDiagram != null ){
						
						isPopulate = _context.getIsPopulateWantedByDefault();
					}
					
			        _callOperationIsNeededCheckBox.setEnabled( selected );
			        _callOperationIsNeededCheckBox.setSelected( selected && isPopulate );
			        			        
			      }} );
		
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
		
		_checkOperationCheckBox.setSelected(true);
		_checkOperationCheckBox.setEnabled(true);

		_callOperationIsNeededCheckBox = new JCheckBox("Populate the '" + _checkOpName + "' operation on diagram?");
		_callOperationIsNeededCheckBox.setBorder( BorderFactory.createEmptyBorder( 0, 0, 10, 0 ) );
		setupPopulateCheckbox( _callOperationIsNeededCheckBox );
		
		updateNames();
		
		theCenterPanel.add( _checkOperationCheckBox );
		theCenterPanel.add( _callOperationIsNeededCheckBox );

		_noFlowPortRadioButton  = new JRadioButton( "None", true );
		_pubFlowPortRadioButton = new JRadioButton( "«publish»" );
		_subFlowPortRadioButton = new JRadioButton( "«subscribe»" );
		
		ButtonGroup group = new ButtonGroup();
		group.add( _noFlowPortRadioButton );
		group.add( _pubFlowPortRadioButton );
		group.add( _subFlowPortRadioButton );

		JPanel theFlowPortOptions = new JPanel();
		theFlowPortOptions.setLayout( new BoxLayout( theFlowPortOptions, BoxLayout.LINE_AXIS ) );
		theFlowPortOptions.setAlignmentX( LEFT_ALIGNMENT );
		theFlowPortOptions.add ( new JLabel("Create a FlowPort: ") );
		theFlowPortOptions.add( _noFlowPortRadioButton );
		theFlowPortOptions.add( _pubFlowPortRadioButton );
		theFlowPortOptions.add( _subFlowPortRadioButton );
	    
		theCenterPanel.add( theFlowPortOptions );
		
		setLayout( new BorderLayout(10,10) );
		setBorder( BorderFactory.createEmptyBorder( 10, 10, 10, 10 ) );
		
		add( theCenterPanel, BorderLayout.WEST );
		_requirementSelectionPanel.setAlignmentX(LEFT_ALIGNMENT);
		theCenterPanel.add( _requirementSelectionPanel );
				
		add( thePageStartPanel, BorderLayout.PAGE_START );
		add( theCenterPanel, BorderLayout.WEST );
		add( createOKCancelPanel(), BorderLayout.PAGE_END );
	}

	private JPanel createInitialValuePanel(String withValue){
		
		JLabel theLabel =  new JLabel(" with the initial value:  ");
		
		_initialValueTextField = new JTextField();
		_initialValueTextField.setText( withValue );
		_initialValueTextField.setPreferredSize( new Dimension( 100, 20 ) );
		_initialValueTextField.setMaximumSize( new Dimension( 100, 20 ) );
		
		JPanel thePanel = new JPanel();
		thePanel.setLayout( new BoxLayout(thePanel, BoxLayout.X_AXIS ) );	
		thePanel.add( theLabel );
		thePanel.add( _initialValueTextField );
		
		return thePanel;
	}
	
	private void updateNames(){
		
		_checkOpName = _context.determineBestCheckOperationNameFor(
				_context.get_selectedContext().getChosenBlock(),
				_chosenNameTextField.getText(),
				40 );
		
		_checkOperationCheckBox.setText(
				"Add a '" + _checkOpName + 
				"' operation to the block that returns the attribute value" );
		
		_callOperationIsNeededCheckBox.setText(
				"Populate the '" + _checkOpName + "' operation on diagram?");
	}
	
	@Override
	protected boolean checkValidity(
			boolean isMessageEnabled ){
		
		String errorMessage = "";
		boolean isValid = true;
		
		String theChosenName = _chosenNameTextField.getText();
		
		IRPClass theChosenBlock = _context.get_selectedContext().getChosenBlock();
		
		boolean isLegalName = _context.isLegalName( theChosenName, theChosenBlock );
		
		if( !isLegalName ){
			
			errorMessage += theChosenName + " is not legal as an identifier representing an executable attribute\n";				
			isValid = false;
			
		} else if( !_context.isElementNameUnique(
				_chosenNameTextField.getText(), 
				"Attribute", 
				theChosenBlock, 
				1 ) ){

			errorMessage = "Unable to proceed as the name '" + _chosenNameTextField.getText() + "' is not unique";
			isValid = false;

		} else if( _checkOperationCheckBox.isSelected() && 
				   !_context.isElementNameUnique(
						   _checkOpName,
						   "Operation",
						   theChosenBlock, 
						   1 ) ){

			errorMessage = "Unable to proceed as the derived check operation name '" + 
					_checkOpName + "' is not unique";
			
			isValid = false;
			
		} else if (!isInteger( _initialValueTextField.getText() )){
			
			errorMessage = "Unable to proceed as the initial value '" + _chosenNameTextField.getText() + "' is not an integer";
			isValid = false;
		}

		if (isMessageEnabled && !isValid && errorMessage != null){

			UserInterfaceHelper.showWarningDialog( errorMessage );
		}
		
		return isValid;
	}
	
	public static boolean isInteger(String s) {
	    try { 
	        Integer.parseInt(s); 
	        
	    } catch(NumberFormatException e) { 
	        return false; 
	        
	    } catch(NullPointerException e) {
	        return false;
	    }
	    
	    return true;
	}

	
	@Override
	protected void performAction() {
		
		// do silent check first
		if( checkValidity( false ) ){

			List<IRPRequirement> selectedReqtsList = _requirementSelectionPanel.getSelectedRequirementsList();

			IRPAttribute theAttribute = addAttributeTo( 
					_context.get_selectedContext().getChosenBlock(), 
					_chosenNameTextField.getText(), 
					_initialValueTextField.getText(),
					selectedReqtsList );
												
			if( _checkOperationCheckBox.isSelected() ){
				
				IRPOperation theCheckOp = addCheckOperationFor( theAttribute, _checkOpName );
				
				IRPStereotype theDependencyStereotype =_context.getStereotypeToUseForFunctions();		
				addTraceabilityDependenciesTo( theCheckOp, selectedReqtsList, theDependencyStereotype );	
				
				if( _callOperationIsNeededCheckBox.isSelected() ){
					populateCallOperationActionOnDiagram( theCheckOp );
				}
				
				theCheckOp.highLightElement();
			}
			
			if( _context.get_selectedContext().getSelectedGraphEl() != null ){
				bleedColorToElementsRelatedTo( _context.get_selectedContext().getSelectedGraphEl() );
			}
			
			PortCreator theCreator = new PortCreator(_context);
			
			if( _pubFlowPortRadioButton.isSelected() ){
				theCreator.createPublishFlowportFor( theAttribute );
			}

			if( _subFlowPortRadioButton.isSelected() ){
				theCreator.createSubscribeFlowportFor( theAttribute );
			}
			
			theAttribute.highLightElement();
			
		} else {
			_context.error("Error in CreateOperationPanel.performAction, checkValidity returned false");
		}	
	}
}

/**
 * Copyright (C) 2016-2022  MBSE Training and Consulting Limited (www.executablembse.com)

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