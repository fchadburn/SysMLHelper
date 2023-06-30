package com.mbsetraining.sysmlhelper.tracedelementpanels;

import java.awt.BorderLayout;
import java.util.List;
import java.util.Set;

import javax.swing.BorderFactory;
import javax.swing.BoxLayout;
import javax.swing.JCheckBox;
import javax.swing.JFrame;
import javax.swing.JPanel;

import com.mbsetraining.sysmlhelper.common.UserInterfaceHelper;
import com.telelogic.rhapsody.core.*;

public class CreateOperationPanel extends CreateTracedElementPanel {

	/**
	 * 
	 */
	private static final long serialVersionUID = -3759980130721314325L;
	
	private JCheckBox _callOperationIsNeededCheckBox;

	// for testing only
	public static void main(String[] args) {
		IRPApplication theRhpApp = RhapsodyAppServer.getActiveRhapsodyApplication();
		launchThePanel( theRhpApp.getApplicationConnectionString() );
	}
	
	public static void launchThePanel(
			final String theAppID ){

		javax.swing.SwingUtilities.invokeLater(new Runnable() {

			@Override
			public void run() {

				JFrame.setDefaultLookAndFeelDecorated( true );

				UserInterfaceHelper.setLookAndFeel();

				JFrame frame = new JFrame(
						"Create an operation" );

				frame.setDefaultCloseOperation( JFrame.DISPOSE_ON_CLOSE );

				CreateOperationPanel thePanel = new CreateOperationPanel(
						theAppID );

				frame.setContentPane( thePanel );
				frame.pack();
				frame.setLocationRelativeTo( null );
				frame.setVisible( true );
			}
		});
	}
	
	public CreateOperationPanel(
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
				createCommonContent(
						_context.get_selectedContext().getSelectedEl(),
						_context.get_selectedContext().getSelectedReqts(), 
						theBlock );
			}
		}

	}
	
	private void createCommonContent(
			IRPModelElement forSourceModelElement,
			Set<IRPRequirement> withReqtsAlsoAdded,
			IRPClassifier onTargetBlock ){
		
		String theSourceText = _context.getTextToFeedToReqtFrom( forSourceModelElement );	
		
		if( theSourceText == null ){
			theSourceText = "function_name";
		}
		
		_context.debug( "CreateOperationPanel constructor called with text '" + theSourceText + "'" );
		
		String theProposedName = _context.determineUniqueNameBasedOn( 
				_context.toMethodName( theSourceText, 40 ), 
				"Operation", 
				onTargetBlock );					
		
		_context.debug( "The proposed name is '" + theProposedName + "'" );
		
		setLayout( new BorderLayout(10,10) );
		setBorder( BorderFactory.createEmptyBorder( 10, 10, 10, 10 ) );

		_requirementSelectionPanel.setAlignmentX( LEFT_ALIGNMENT );
		
		JPanel theNamePanel = createChosenNamePanelWith( "Create an operation called:  ", theProposedName );
		theNamePanel.setAlignmentX(LEFT_ALIGNMENT);
		
		_callOperationIsNeededCheckBox = new JCheckBox("Populate on diagram?");
		setupPopulateCheckbox( _callOperationIsNeededCheckBox );
				
		JPanel thePageStartPanel = new JPanel();
		thePageStartPanel.setLayout( new BoxLayout( thePageStartPanel, BoxLayout.X_AXIS ) );
		thePageStartPanel.add( theNamePanel );
		thePageStartPanel.add( _callOperationIsNeededCheckBox );
		
		add( thePageStartPanel, BorderLayout.PAGE_START );
		add( _requirementSelectionPanel, BorderLayout.WEST );
		add( createOKCancelPanel(), BorderLayout.PAGE_END );
	}
	
	@Override
	protected boolean checkValidity(
			boolean isMessageEnabled ){
		
		String errorMessage = null;
		boolean isValid = true;
		
		String theChosenName = _chosenNameTextField.getText();
		IRPClass theChosenBlock = _context.get_selectedContext().getChosenBlock();
		
		boolean isLegalName = _context.isLegalName( theChosenName, theChosenBlock );
		
		if( !isLegalName ){
			
			errorMessage = theChosenName + " is not legal as an identifier representing an operation\n";				
			isValid = false;
			
		} else if (!_context.isElementNameUnique(
				theChosenName, 
				"Operation", 
				_context.get_selectedContext().getChosenBlock(),//m_TargetOwningElement, 
				1)){

			errorMessage = "Unable to proceed as the name '" + theChosenName + "' is not unique";
			isValid = false;
		}

		if (isMessageEnabled && !isValid && errorMessage != null){

			UserInterfaceHelper.showWarningDialog( errorMessage );
		}
		
		return isValid;
	}

	@Override
	protected void performAction() {

		// it is assumed that checkValidity has returned true

		IRPOperation theOperation = 
				_context.get_selectedContext().getChosenBlock().addOperation(
						_chosenNameTextField.getText() );	

		IRPGraphElement theSourceGraphElement = _context.get_selectedContext().getSelectedGraphEl();

		if( theSourceGraphElement != null ){

			if( theSourceGraphElement.getModelObject() instanceof IRPCallOperation ){

				IRPCallOperation theCallOp = (IRPCallOperation) theSourceGraphElement.getModelObject();

				IRPInterfaceItem theExistingOp = theCallOp.getOperation();

				if( theExistingOp == null ){

					_context.debug( "Setting the " + _context.elInfo( theCallOp ) + 
							" to " + _context.elInfo(theOperation) );

					theCallOp.setOperation( theOperation );

					String theProposedName = 
							_context.determineUniqueNameBasedOn( 
									_context.toMethodName( theOperation.getName(), 40 ), 
									"CallOperation", 
									theCallOp.getOwner() );

					theCallOp.setName( theProposedName );
				}
			} else {
				List<IRPRequirement> theSelectedReqtsList = _requirementSelectionPanel.getSelectedRequirementsList();
				
				IRPStereotype theDependencyStereotype =_context.getStereotypeToUseForFunctions();		
				addTraceabilityDependenciesTo( theOperation, theSelectedReqtsList, theDependencyStereotype );
				_context.bleedColorToElementsRelatedTo( theSelectedReqtsList );
			}
		}

		if( _callOperationIsNeededCheckBox.isSelected() ){
			populateCallOperationActionOnDiagram( theOperation );
		}

		theOperation.highLightElement();
	}
}

/**
 * Copyright (C) 2016-2023  MBSE Training and Consulting Limited (www.executablembse.com)

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
