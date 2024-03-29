package com.mbsetraining.sysmlhelper.createactorpart;

import java.util.List;

import javax.swing.JCheckBox;
import javax.swing.JTextField;

import com.mbsetraining.sysmlhelper.common.RhapsodyComboBox;
import com.mbsetraining.sysmlhelper.common.UserInterfaceHelper;
import com.mbsetraining.sysmlhelper.executablembse.ExecutableMBSE_Context;
import com.telelogic.rhapsody.core.*;

public class ActorMappingInfo {

	final public String _actorBlankName = "EnterActorName";
	
	private RhapsodyComboBox _inheritedFromComboBox;
	private JCheckBox _actorCheckBox;
	private JTextField _actorNameTextField;
	private IRPActor _sourceActor = null;
	
	private ExecutableMBSE_Context _context;

	public ActorMappingInfo(
			RhapsodyComboBox theRhapsodyComboBox,
			JCheckBox theActorCheckBox, 
			JTextField theActorName,
			IRPActor theSourceActor,
			ExecutableMBSE_Context context ) {
		
		super();
		
		_context = context;

		this._inheritedFromComboBox = theRhapsodyComboBox;
		this._actorCheckBox = theActorCheckBox;
		this._actorNameTextField = theActorName;
		this._sourceActor = theSourceActor;
	}
	
	public JTextField getTextField(){
		return _actorNameTextField;
	}
	
	public boolean isSelected(){
		return _actorCheckBox.isSelected();
	}
	
	public String getName(){
		return _actorNameTextField.getText();
	}
	
	public String getSourceActorName(){
		return _sourceActor.getName();
	}
	
	public IRPActor getSourceActor(){
		return _sourceActor;
	}
	
	public void updateToBestActorNamesBasedOn(
			String theBlockName ){
		
		String theOriginalActorName;
		
		if( _sourceActor != null ){
			theOriginalActorName = _sourceActor.getName();
		} else {
			theOriginalActorName = _actorBlankName;
		}
		
		String theDesiredName;
		
		if( theBlockName.isEmpty() ){
			theDesiredName = _context.toLegalClassName( theOriginalActorName );
		} else {
			theDesiredName = _context.toLegalClassName( theOriginalActorName ) + "_" + theBlockName;
		}
		
		String theProposedActorName = theDesiredName;
		//_context.determineUniqueNameBasedOn( 
		//		theDesiredName, 
		//		"Actor", 
		//		_context.get_rhpPrj() );
		
		_actorNameTextField.setText( theProposedActorName );
	}
	

	
	public IRPInstance performActorPartCreationIfSelectedIn(
			IRPClass theAssemblyBlock,
			IRPClass connectedToBlock ){
		
		IRPInstance theActorPart = null;
		
		if( isSelected() ){
			
			IRPProject theProject = theAssemblyBlock.getProject();
			
			boolean isInheritanceAllowed = 
					_context.getIsAllowInheritanceChoices( 
							theAssemblyBlock );

			String theLegalActorName = getName().replaceAll(" ", "");
			
			// get the logical system part and block
			@SuppressWarnings("unchecked")
			List<IRPInstance> theParts = 
				theAssemblyBlock.getNestedElementsByMetaClass( 
						"Part", 0 ).toList();
			
			IRPInstance theConnectedToPart = null;
			
			IRPClassifier theTesterBlock = null;
			IRPInstance theTesterPart = null;
			
			for( IRPInstance thePart : theParts ) {
				
				IRPClassifier theOtherClass = thePart.getOtherClass();
				
				if( theOtherClass instanceof IRPClass ){
					
					boolean isTestDriver = 
							_context.hasStereotypeCalled( 
									"TestDriver", theOtherClass );
					
					if( !isTestDriver && 
						(connectedToBlock != null) &&
						theOtherClass.equals( connectedToBlock ) ){

						theConnectedToPart = thePart;

						//_context.debug( _context.elInfo( theConnectedToPart ) + " was found to connect the actors to, and is typed by " + 
						//		_context.elInfo( connectedToBlock ) );

					} else if ( isTestDriver ){

						theTesterPart = thePart;
						theTesterBlock = theOtherClass;

						//_context.debug( _context.elInfo( theTesterPart ) + " was found as the test driver, and is typed by " + 
						//		_context.elInfo( theTesterBlock ) );
					}
				}				
			}

			IRPPackage thePackageForActor = 
					_context.get_selectedContext().getPackageForActorsAndTest();

			IRPActor theActor = thePackageForActor.addActor( theLegalActorName );
			theActor.highLightElement();

			String theText = "Create actor called " + _actorNameTextField.getText();

			// Make each of the actors a part of the SystemAssembly block
			theActorPart = (IRPInstance) theAssemblyBlock.addNewAggr( "Part", "" );
			theActorPart.highLightElement();
			theActorPart.setOtherClass( theActor );

			if( isInheritanceAllowed ){

				IRPModelElement theInheritedFrom = 
						_inheritedFromComboBox.getSelectedRhapsodyItem();
				
				if( theInheritedFrom != null ){
					theText = theText + " inherited from " + theInheritedFrom.getName();

					theActor.addGeneralization( (IRPClassifier) theInheritedFrom );
					theActor.highLightElement();
				} else {
					IRPActor theTestbench = 
							(IRPActor) theProject.findNestedElementRecursive(
									"Testbench", "Actor" );

					if( theTestbench != null ){
						theActor.addGeneralization( theTestbench );
					} else {
						_context.error( "Unable to find Actor with name Testbench" );
					}
				}

			} else {

				IRPStereotype theTestBenchStereotype =
						_context.getStereotypeForTestbench();
						
				if( theTestBenchStereotype != null ){
					theActor.setStereotype( theTestBenchStereotype );
				}
			}

			if( theConnectedToPart != null ){

				connectActorPartWithBlockPartIn(
						theAssemblyBlock,
						theConnectedToPart, 
						theActorPart );					
			}

			if( theTesterBlock == null ){

				UserInterfaceHelper.showWarningDialog(
						"A new Actor part called " + theActorPart.getName() + " was added to " + _context.elInfo( theAssemblyBlock ) + ". \n" +
						"However, no TestDriver part was found hence skipping the creation of links to this. In future, you \n" +
						"may want to consider using the FullSim structure to get the benefits of test driver creation. " );
			} else {

				connectActorPartWithTesterPartIn(
						theAssemblyBlock,
						theTesterPart, 
						theActorPart );
			}

			_context.debug( "Finishing adding part connected to actor" );

		} else {
			//_context.debug( "Not selected" );
		}
		
		return theActorPart;
	}

	private void connectActorPartWithTesterPartIn(
			IRPClass theAssemblyBlock,
			IRPInstance theTesterPart,
			IRPInstance theActorPart ){
		
		IRPClassifier theActor = theActorPart.getOtherClass();
		IRPClassifier theTesterBlock = theTesterPart.getOtherClass();
		
		IRPLink existingLinkConnectingTesterToActor = 
				_context.getExistingLinkBetweenBaseClassifiersOf(
						theTesterBlock, theActor, theAssemblyBlock );
		
		IRPPort theActorToTesterPort = null;
		IRPPort theTesterToActorPort = null;
		
		if( existingLinkConnectingTesterToActor != null ){
			
			//_context.debug( "There are existing ports between " + 
			//		_context.elInfo( theTesterBlock ) + " and " + 
			//		_context.elInfo( theActor ) );
		
			IRPPort fromPort = existingLinkConnectingTesterToActor.getFromPort();
			IRPPort toPort = existingLinkConnectingTesterToActor.getToPort();
			
			if( fromPort.getOwner() instanceof IRPActor ){
				theActorToTesterPort = fromPort;
				theTesterToActorPort = toPort;
			} else {
				theActorToTesterPort = toPort;
				theTesterToActorPort = fromPort;						
			}
			
		} else {

			//_context.debug( "Creating a new connector between " + 
			//		_context.elInfo( theTesterBlock ) + " and " + 
			//		_context.elInfo( theActor ) );

			try {
				// and connect actor to the TestDriver block
		    	theActorToTesterPort = 
		    			(IRPPort) theActor.addNewAggr( "Port", "pTester" );
		    	
				theTesterToActorPort = 
						(IRPPort) theTesterBlock.addNewAggr(
								"Port", "p" + theActor.getName() );
			
			} catch( Exception e ){
				_context.error( "Exception while trying to add ports" );
			}
		}
		
		IRPLink theTesterLink = 
				(IRPLink) theAssemblyBlock.addLink(
						theActorPart, 
						theTesterPart, 
						null, 
						theActorToTesterPort, 
						theTesterToActorPort );
		
		theTesterLink.changeTo( "connector" );
	}

	public void connectActorPartWithBlockPartIn(
			IRPClass theAssemblyBlock,
			IRPInstance theConnectedToPart,
			IRPInstance theActorPart ){
		
		IRPClassifier theActor = theActorPart.getOtherClass();
		IRPClassifier connectedToBlock = theConnectedToPart.getOtherClass();
		
		IRPLink existingLinkConnectingBlockToActor = 
				_context.getExistingLinkBetweenBaseClassifiersOf(
						connectedToBlock, theActor, theAssemblyBlock );
		
		IRPPort theActorToSystemPort = null;
		IRPPort theSystemToActorPort = null;
		
		if( existingLinkConnectingBlockToActor != null ){
			
			//_context.debug( "There is an existing connector between " + 
			//		_context.elInfo( connectedToBlock ) + " and " + _context.elInfo( theActor ) );
		
			IRPPort fromPort = existingLinkConnectingBlockToActor.getFromPort();
			IRPPort toPort = existingLinkConnectingBlockToActor.getToPort();
			
			if( fromPort.getOwner() instanceof IRPActor ){
				theActorToSystemPort = fromPort;
				theSystemToActorPort = toPort;
			} else {
				theActorToSystemPort = toPort;
				theSystemToActorPort = fromPort;						
			}	
		} else {

			//_context.debug( "Creating a new connector between " + 
			//		_context.elInfo( connectedToBlock ) + " and " + 
			//		_context.elInfo( theActor ) );

			String theActorPortName = 
					_context.determineUniqueNameBasedOn(
							"p" + connectedToBlock.getName() , "Port", theActor);

			//_context.debug("Attempting to create port called " + 
			//		theActorPortName + " owned by " + _context.elInfo( theActor ));

			// and connect actor to the LogicalSystem block
			theActorToSystemPort = 
					(IRPPort) theActor.addNewAggr( 
							"Port", theActorPortName);

			String theSystemPortName = 
					_context.determineUniqueNameBasedOn(
							"p" + theActor.getName() , "Port", connectedToBlock);

			//_context.debug( "Attempting to create port called " + 
			//		theSystemPortName + " owned by " + 
			//		_context.elInfo( connectedToBlock ) );

			try {
				theSystemToActorPort = 
						(IRPPort) connectedToBlock.addNewAggr(
								"Port", theSystemPortName );	
			} catch( Exception e ){
				_context.error( "Exception while trying to create system to actor port, e=" + e.getMessage() );
			}			
		}
		
		try {
			IRPLink theLogicalSystemLink = 
					(IRPLink) theAssemblyBlock.addLink(
							theActorPart, 
							theConnectedToPart, 
							null, 
							theActorToSystemPort, 
							theSystemToActorPort );
			
			theLogicalSystemLink.changeTo( "connector" );
			
		} catch( Exception e ){
			_context.error( "Exception while trying to addLink, e=" + e.getMessage() );
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