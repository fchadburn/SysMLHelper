package com.mbsetraining.sysmlhelper.createnewblockpart;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.util.List;

import javax.swing.BorderFactory;
import javax.swing.BoxLayout;
import javax.swing.JCheckBox;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;

import com.mbsetraining.sysmlhelper.common.RhapsodyComboBox;
import com.mbsetraining.sysmlhelper.common.UserInterfaceHelper;
import com.mbsetraining.sysmlhelper.executablembse.ExecutableMBSEBasePanel;
import com.mbsetraining.sysmlhelper.sequencediagram.SequenceDiagramCreator;
import com.telelogic.rhapsody.core.*;

public class CreateNewBlockPartPanel extends ExecutableMBSEBasePanel {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	private IRPPackage _rootPackage;
	private IRPClass _assemblyBlock;

	protected JTextField _blockNameTextField = null;
	protected JTextField _partNameTextField = null;

	protected RhapsodyComboBox _chosenStereotype;

	// testing only
	public static void main(String[] args) {
		IRPApplication theRhpApp = RhapsodyAppServer.getActiveRhapsodyApplication();
		launchThePanel( theRhpApp.getApplicationConnectionString() );
	}

	public static void launchThePanel(
			final String theAppID ){

		javax.swing.SwingUtilities.invokeLater( new Runnable() {

			@Override
			public void run() {

				JFrame.setDefaultLookAndFeelDecorated( true );

				JFrame frame = new JFrame( "Create new Block/Part" );

				frame.setDefaultCloseOperation( JFrame.DISPOSE_ON_CLOSE );

				CreateNewBlockPartPanel thePanel = 
						new CreateNewBlockPartPanel( theAppID );

				frame.setContentPane( thePanel );
				frame.pack();
				frame.setLocationRelativeTo( null );
				frame.setVisible( true );
			}
		});
	}

	public CreateNewBlockPartPanel(
			String theAppID ){

		super( theAppID );

		_context.get_selectedContext().setContextTo( _context.getSelectedElement( true ) );
		
		IRPClass theBuildingBlock = 
				_context.get_selectedContext().getBuildingBlock();

		if( theBuildingBlock == null ){

			buildUnableToRunDialog( 
					"Sorry, this helper is unable to run this command because \n" +
							"there was no execution context or block found in the model. \n " +
					"You need to add the relevant package structure first." );

		} else { // theBuildingBlock != null

			_rootPackage = _context.get_selectedContext().getPackageForBlocks();
			_assemblyBlock = _context.get_selectedContext().getBuildingBlock();

			setLayout( new BorderLayout(10,10) );
			setBorder( BorderFactory.createEmptyBorder( 10, 10, 10, 10 ) );

			JPanel theStartPanel = new JPanel();

			theStartPanel.setLayout( new BoxLayout( theStartPanel, BoxLayout.PAGE_AXIS ) );
			theStartPanel.add( createPanelWithTextCentered( "Context for part: " + _context.elInfo( _assemblyBlock ) ) );
			theStartPanel.add( createBlockChoicePanel( "" ) );
			
			add( theStartPanel, BorderLayout.PAGE_START );
			add( createStereotypePanel(), BorderLayout.CENTER );	    
			add( createOKCancelPanel(), BorderLayout.PAGE_END );
		}
	}

	private JPanel createStereotypePanel(){

		JPanel thePanel = new JPanel();
		thePanel.setLayout( new BoxLayout(thePanel, BoxLayout.X_AXIS ) );	

		List<IRPModelElement> theStereotypes = 
				_context.getStereotypesForBlockPartCreation();

		_chosenStereotype = new RhapsodyComboBox( theStereotypes, false );
		_chosenStereotype.setMaximumSize( new Dimension( 250, 20 ) );

		if( theStereotypes.size() > 0 ){
			// set to first value in list
			_chosenStereotype.setSelectedRhapsodyItem( theStereotypes.get( 0 ) );	
			_context.debug("Setting default stereotype to " + _context.elInfo(theStereotypes.get( 0 )));
		}

		thePanel.add( new JLabel( "  Stereotype as: " ) );
		thePanel.add( _chosenStereotype );

		return thePanel;
	}

	private JPanel createBlockChoicePanel(
			String theBlockName ){

		JPanel thePanel = new JPanel();
		thePanel.setLayout( new BoxLayout(thePanel, BoxLayout.X_AXIS ) );	
		
		_blockNameTextField = new JTextField();
		_blockNameTextField.setPreferredSize( new Dimension( 150, 20 ) );

		JCheckBox theBlockCheckBox = new JCheckBox( "Create block called:" );
		theBlockCheckBox.setSelected( true );
				
		thePanel.add( theBlockCheckBox );
		thePanel.add( _blockNameTextField );

		thePanel.add( new JLabel(" with part name (leave blank for default): ") );

		_partNameTextField = new JTextField();
		_partNameTextField.setPreferredSize( new Dimension( 150, 20 ) );

		thePanel.add( _partNameTextField );

		return thePanel;
	}

	@Override
	protected boolean checkValidity(
			boolean isMessageEnabled) {

		boolean isValid = true;
		String errorMsg = "";

		String theBlockName = _blockNameTextField.getText();

		if ( theBlockName.trim().isEmpty() ){

			errorMsg += "Please choose a valid name for the Block";
			isValid = false;

		} else {
			boolean isLegalBlockName = 
					_context.isLegalName( theBlockName, _rootPackage );

			if( !isLegalBlockName ){

				errorMsg += theBlockName + " is not legal as an identifier representing an executable Block\n";				
				isValid = false;

			} else if( !_context.isElementNameUnique(
					theBlockName, 
					"Class", 
					_rootPackage, 
					1 ) ){

				errorMsg += "Unable to proceed as the Block name '" + theBlockName + "' is not unique";
				isValid = false;

			} else {

				String thePartName = _partNameTextField.getText();

				if ( !thePartName.trim().isEmpty() ){

					boolean isLegalPartName = 
							_context.isLegalName( thePartName, _assemblyBlock );

					if( !isLegalPartName ){

						errorMsg += thePartName + " is not legal as an identifier representing an executable Part\n";				
						isValid = false;

					} else if( !_context.isElementNameUnique(
							thePartName, 
							"Object", 
							_assemblyBlock, 
							0 ) ){

						errorMsg += "Unable to proceed as the Part name '" + thePartName + "' is not unique for " + 
								_context.elInfo( _assemblyBlock );

						isValid = false;
					}
				}
			}
		}

		if (isMessageEnabled && !isValid && errorMsg != null){

			UserInterfaceHelper.showWarningDialog( errorMsg );
		}

		return isValid;
	}

	private IRPInstance getElapsedTimeActorPartFor(
			IRPClass theAssemblyBlock ){

		IRPInstance theElapsedTimePart = null;

		@SuppressWarnings("unchecked")
		List<IRPInstance> theInstances = 
		theAssemblyBlock.getNestedElementsByMetaClass(
				"Instance", 0 ).toList();

		for( IRPInstance theInstance : theInstances ){

			IRPClassifier theClassifier = theInstance.getOtherClass();

			if( theClassifier != null &&
					theClassifier instanceof IRPActor &&
					_context.hasStereotypeCalled( "ElapsedTimeGenerator", theClassifier ) ){

				theElapsedTimePart = theInstance;
				break;
			}
		}

		return theElapsedTimePart;
	}

	@Override
	protected void performAction() {

		if( checkValidity( false ) ){

			if( _rootPackage != null ){

				String theName = _blockNameTextField.getText();

				IRPClass theClass = _rootPackage.addClass( theName );
				theClass.highLightElement();				

				IRPStereotype theTimeElapsedBlockStereotype = 
						_context.getStereotypeForTimeElapsedBlock();

				theClass.setStereotype( theTimeElapsedBlockStereotype );
				
				String thePartName = _partNameTextField.getText().trim();

				IRPInstance thePart = 
						(IRPInstance) _assemblyBlock.addNewAggr(
								"Part", thePartName );

				thePart.setOtherClass( theClass );
				thePart.highLightElement();

				IRPModelElement theSelectedStereotype = _chosenStereotype.getSelectedRhapsodyItem();

				if( theSelectedStereotype instanceof IRPStereotype ){

					try {
						theClass.setStereotype( (IRPStereotype) theSelectedStereotype );

					} catch( Exception e ){
						_context.error( "Exception in CreateNewBlockPartPanel.performAction, unable to apply " + 
								theSelectedStereotype.getName() + " to " + _context.elInfo( theClass ) );	
					}

					try {
						thePart.setStereotype( (IRPStereotype) theSelectedStereotype );

					} catch( Exception e ){
						_context.error( "Exception in CreateNewBlockPartPanel.performAction, unable to apply " + 
								theSelectedStereotype.getName() + " to " + _context.elInfo( thePart ) );	
					}
				}

				theClass.changeTo( "Block" );

				// Try and find ElapsedTime actor part 				
				IRPInstance theElapsedTimePart = 
						getElapsedTimeActorPartFor( _assemblyBlock );

				if( theElapsedTimePart != null ){

					IRPClassifier theElapsedTimeActor = 
							theElapsedTimePart.getOtherClass();

					IRPSysMLPort theActorsElapsedTimePort = 
							(IRPSysMLPort) _context.findNestedElementUnder( 
									(IRPClassifier) theElapsedTimeActor,
									"elapsedTime",
									"SysMLPort",
									true );

					IRPSysMLPort theBlocksElapsedTimePort = 
							(IRPSysMLPort) _context.findNestedElementUnder( 
									(IRPClassifier) theClass,
									"elapsedTime",
									"SysMLPort",
									true );

					if( theActorsElapsedTimePort != null &&
							theBlocksElapsedTimePort != null ){

						_context.addConnectorBetweenSysMLPortsIfOneDoesntExist(
								theActorsElapsedTimePort, 
								theElapsedTimePart, 
								theBlocksElapsedTimePort, 
								thePart );

					} else {
						_context.error( "CreateNewBlockPartPanel.performAction was unable to find elapsedTime ports") ;
					}

				} else {
					_context.error( "CreateNewBlockPartPanel.performAction was unable to find ElapsedTime actor in project. You may be missing the BasePkg");
				}

				SequenceDiagramCreator theHelper = new SequenceDiagramCreator(_context);

				theHelper.updateAutoShowSequenceDiagramFor( 
						_assemblyBlock );

			} else {
				_context.error( "CreateNewBlockPartPanel.performAction was unable to find " + _context.elInfo( _rootPackage ) );
			}

		} else {
			_context.error( "CreateNewBlockPartPanel.performAction, checkValidity returned false" );
		}		
	}	
}

/**
 * Copyright (C) 2017-2021  MBSE Training and Consulting Limited (www.executablembse.com)

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