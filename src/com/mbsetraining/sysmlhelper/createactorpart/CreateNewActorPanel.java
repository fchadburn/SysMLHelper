package com.mbsetraining.sysmlhelper.createactorpart;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.util.ArrayList;
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
import com.mbsetraining.sysmlhelper.executablembse.ActorMappingInfo;
import com.mbsetraining.sysmlhelper.executablembse.ExecutableMBSEBasePanel;
import com.mbsetraining.sysmlhelper.sequencediagram.SequenceDiagramCreator;
import com.telelogic.rhapsody.core.*;

public class CreateNewActorPanel extends ExecutableMBSEBasePanel {

	/**
	 * 
	 */
	private static final long serialVersionUID = 358021036519229322L;

	protected IRPPackage _rootPackage;
	protected JTextField _chosenNameTextField;
	protected ActorMappingInfo _classifierMappingInfo;
	protected IRPClass _blockToConnectTo;
	protected IRPClass _assemblyBlock;

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

				JFrame frame = new JFrame( "Create new Actor" );

				frame.setDefaultCloseOperation( JFrame.DISPOSE_ON_CLOSE );

				CreateNewActorPanel thePanel = 
						new CreateNewActorPanel( theAppID );

				frame.setContentPane( thePanel );
				frame.pack();
				frame.setLocationRelativeTo( null );
				frame.setVisible( true );
			}
		});
	}

	public CreateNewActorPanel( String theAppID ){

		super( theAppID );

		_context.get_selectedContext().setContextTo( 
				_context.getSelectedElement( false ) );

		IRPClass theBuildingBlock = 
				_context.get_selectedContext().getBuildingBlock();

		if( theBuildingBlock == null ){

			buildUnableToRunDialog( 
					"Sorry, this helper is unable to run because \n" +
							"there was no execution context block found in the model. \n " +
					"You need to add the relevant package structure first." );

		} else { // theBuildingBlock != null

			IRPClass theBlock = _context.get_selectedContext().getBlockUnderDev(
					"Which Block/Part do you want to wire the Actor to?" );

			if( theBlock == null ){
				buildUnableToRunDialog( 
						"Sorry, this helper is unable to run because no parts/blocks were found  \n" +
						"under " + _context.elInfo( theBuildingBlock ) + " to wire an actor to. \n " +
						"You need to add a part/block to the system before adding an actor." );
			} else {
				_rootPackage = _context.get_selectedContext().getPackageForActorsAndTest();
				_blockToConnectTo = _context.get_selectedContext().getChosenBlock();
				_assemblyBlock = _context.get_selectedContext().getBuildingBlock();

				setLayout( new BorderLayout(10,10) );
				setBorder( BorderFactory.createEmptyBorder( 10, 10, 10, 10 ) );

				JPanel theStartPanel = new JPanel();

				theStartPanel.setLayout( new BoxLayout( theStartPanel, BoxLayout.PAGE_AXIS ) );
				theStartPanel.add( createPanelWithTextCentered( "Context for part: " + _context.elInfo( _assemblyBlock ) ) );

				add( theStartPanel, BorderLayout.PAGE_START );

				if( _blockToConnectTo != null ){
					add( createActorChoicePanel( _blockToConnectTo.getName() ), BorderLayout.CENTER );

				} else {
					add( createActorChoicePanel( "" ), BorderLayout.CENTER );
				}

				add( createOKCancelPanel(), BorderLayout.PAGE_END );

			}
		}
	}

	@SuppressWarnings("unchecked")
	private JPanel createActorChoicePanel(String theBlockName){

		JPanel thePanel = new JPanel();
		thePanel.setLayout( new BoxLayout(thePanel, BoxLayout.X_AXIS ) );	

		_chosenNameTextField = new JTextField();
		_chosenNameTextField.setPreferredSize( new Dimension( 300, 20 ) );

		List<IRPModelElement> theExistingActors;

		boolean isAllowInheritanceChoices = 
				_context.getIsAllowInheritanceChoices( _rootPackage );

		if( isAllowInheritanceChoices ){

			theExistingActors = _rootPackage.getNestedElementsByMetaClass( 
					"Actor", 1 ).toList();
		} else {
			theExistingActors = new ArrayList<>();
		}

		RhapsodyComboBox theInheritedActorComboBox = 
				new RhapsodyComboBox( theExistingActors, false );

		JCheckBox theActorCheckBox = new JCheckBox( "Create actor called:" );

		theActorCheckBox.setSelected( true );

		_classifierMappingInfo = 
				new ActorMappingInfo(
						theInheritedActorComboBox, 
						theActorCheckBox, 
						_chosenNameTextField, 
						null,
						_context );

		_classifierMappingInfo.updateToBestActorNamesBasedOn( theBlockName );

		thePanel.add( theActorCheckBox );
		thePanel.add( _chosenNameTextField );

		if( isAllowInheritanceChoices ){
			JLabel theLabel = new JLabel( "Inherit from:" );
			thePanel.add( theLabel );
			thePanel.add( theInheritedActorComboBox );	    	
		}

		return thePanel;
	}

	@Override
	protected boolean checkValidity(
			boolean isMessageEnabled) {

		boolean isValid = true;
		String errorMsg = "";

		String theChosenName = _chosenNameTextField.getText();

		if ( theChosenName.contains( _classifierMappingInfo._actorBlankName ) ){

			errorMsg += "Please choose a valid name for the Actor";
			isValid = false;

		} else {
			boolean isLegalBlockName = _context.isLegalName( theChosenName, _blockToConnectTo );

			if (!isLegalBlockName){

				errorMsg += theChosenName + " is not legal as an identifier representing an executable Actor\n";				
				isValid = false;

			} else if (!_context.isElementNameUnique(

					theChosenName, "Actor", _rootPackage, 1)){

				errorMsg += "Unable to proceed as the Actor name '" + theChosenName + "' is not unique";
				isValid = false;
			}
		}

		if (isMessageEnabled && !isValid && errorMsg != null){

			UserInterfaceHelper.showWarningDialog( errorMsg );
		}

		return isValid;
	}

	@Override
	protected void performAction() {

		if( checkValidity( false ) ){

			IRPInstance theActorPart =
					_classifierMappingInfo.performActorPartCreationIfSelectedIn( 
							_assemblyBlock, _blockToConnectTo );

			if( theActorPart != null ){

				SequenceDiagramCreator theHelper = new SequenceDiagramCreator( _context );
				theHelper.updateAutoShowSequenceDiagramFor( _assemblyBlock );
			}

		} else {
			_context.error( "CreateNewActorPanel.performAction, checkValidity returned false" );
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