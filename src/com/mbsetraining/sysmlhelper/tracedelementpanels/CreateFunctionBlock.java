package com.mbsetraining.sysmlhelper.tracedelementpanels;

import java.awt.BorderLayout;
import java.util.List;
import java.util.Set;

import javax.swing.BorderFactory;
import javax.swing.BoxLayout;
import javax.swing.JFrame;
import javax.swing.JPanel;

import com.mbsetraining.sysmlhelper.common.UserInterfaceHelper;
import com.telelogic.rhapsody.core.*;

public class CreateFunctionBlock extends CreateTracedElementPanel {
	
	/**
	 * 
	 */
	private static final long serialVersionUID = -6488730484986675104L;
	
	protected IRPPackage _packageForFunctionBlocks;
	protected IRPClassifier _buildingBlock;

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
						"Create a Function Block" );

				frame.setDefaultCloseOperation( JFrame.DISPOSE_ON_CLOSE );

				CreateFunctionBlock thePanel = new CreateFunctionBlock(
						theAppID );

				frame.setContentPane( thePanel );
				frame.pack();
				frame.setLocationRelativeTo( null );
				frame.setVisible( true );
			}
		});
	}
	
	public CreateFunctionBlock(
			String theAppID ){
		
		super( theAppID );
		
		_packageForFunctionBlocks = 
				_context.get_selectedContext().getPackageForFunctionBlocks();

		if( _packageForFunctionBlocks == null ){
			
			buildUnableToRunDialog( 
					"Sorry, this helper is unable to run this command because \n" +
					"there was no package for function blocks found in the model. \n " +
					"You need to add the relevant package structure first." );
			
		} else { // theBuildingBlock != null
			
			_buildingBlock = _context.get_selectedContext().getBlockForFunctionUsages();
			
			if( _buildingBlock != null ){
				_buildingBlock.highLightElement();
			}
			
			createCommonContent(
					_context.get_selectedContext().getSelectedEl(),
					_context.get_selectedContext().getSelectedReqts(), 
					_buildingBlock );
		}
	}
	
	private void createCommonContent(
			IRPModelElement forSourceModelElement,
			Set<IRPRequirement> withReqtsAlsoAdded,
			IRPClassifier onTargetBlock ){
		
		String theSourceText = _context.getActionTextFrom( forSourceModelElement );	
		
		if( theSourceText == null ){
			theSourceText = "function_name";
		}
				
		String theProposedName = _context.determineUniqueNameBasedOn( 
				_context.toCamelCase( theSourceText, 40, true ).trim(), 
				"Class", 
				_packageForFunctionBlocks );					
		
//		_context.debug( "The proposed name is '" + theProposedName + "'" );
		
		setLayout( new BorderLayout(10,10) );
		setBorder( BorderFactory.createEmptyBorder( 10, 10, 10, 10 ) );

		_requirementSelectionPanel.setAlignmentX( LEFT_ALIGNMENT );
		
		JPanel theNamePanel = createChosenNamePanelWith( 
					"Create an " + _context.FUNCTION_BLOCK + " called:  ", theProposedName );

		theNamePanel.setAlignmentX(LEFT_ALIGNMENT);

		JPanel thePageStartPanel = new JPanel();
		thePageStartPanel.setLayout( new BoxLayout( thePageStartPanel, BoxLayout.X_AXIS ) );
		thePageStartPanel.add( theNamePanel );
		
		add( thePageStartPanel, BorderLayout.PAGE_START );
		add( _requirementSelectionPanel, BorderLayout.WEST );
		add( createOKCancelPanel(), BorderLayout.PAGE_END );
	}
	
	@Override
	protected boolean checkValidity(
			boolean isMessageEnabled ){
		
		String errorMessage = null;
		boolean isValid = true;
		
		String theChosenName = _chosenNameTextField.getText().trim();
		
		 if( !_context.isElementNameUnique(
				theChosenName, 
				"Class", 
				_packageForFunctionBlocks, 
				1 ) ){

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

		String theChosenName = _chosenNameTextField.getText().trim();
		
		IRPClass theFunctionBlock = _packageForFunctionBlocks.addClass( theChosenName );
		theFunctionBlock.changeTo( _context.FUNCTION_BLOCK );
		
		if( _buildingBlock != null ){
			
			IRPInstance theFunctionUsage = (IRPInstance) _buildingBlock.addNewAggr( "Object", "" );
			theFunctionUsage.changeTo( _context.FUNCTION_USAGE );
			
			theFunctionUsage.setOtherClass( theFunctionBlock );
			theFunctionUsage.highLightElement();
		}
		
		List<IRPRequirement> theSelectedReqtsList = _requirementSelectionPanel.getSelectedRequirementsList();
		
		IRPStereotype theDependencyStereotype =_context.getStereotypeToUseForActions();		
		addTraceabilityDependenciesTo( theFunctionBlock, theSelectedReqtsList, theDependencyStereotype );
		_context.bleedColorToElementsRelatedTo( theSelectedReqtsList );
		theFunctionBlock.highLightElement();
	}
}

/**
 * Copyright (C) 2022  MBSE Training and Consulting Limited (www.executablembse.com)

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
