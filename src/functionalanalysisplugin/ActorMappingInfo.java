package functionalanalysisplugin;

import java.util.ArrayList;
import java.util.List;

import javax.swing.JCheckBox;
import javax.swing.JTextField;

import com.mbsetraining.sysmlhelper.common.RhapsodyComboBox;
import com.mbsetraining.sysmlhelper.common.UserInterfaceHelper;
import com.mbsetraining.sysmlhelper.executablembse.ExecutableMBSE_Context;
import com.telelogic.rhapsody.core.*;

public class ActorMappingInfo {

	final public String m_ActorBlankName = "EnterActorName";
	
	private RhapsodyComboBox m_InheritedFromComboBox;
	private JCheckBox m_ActorCheckBox;
	private JTextField m_ActorNameTextField;
	private IRPActor m_SourceActor = null;
	
	private ExecutableMBSE_Context _context;
	FunctionalAnalysisSettings _settings;

	public ActorMappingInfo(
			RhapsodyComboBox theRhapsodyComboBox,
			JCheckBox theActorCheckBox, 
			JTextField theActorName,
			IRPActor theSourceActor,
			ExecutableMBSE_Context context ) {
		
		super();
		
		_context = context;
		_settings = new FunctionalAnalysisSettings(_context);

		this.m_InheritedFromComboBox = theRhapsodyComboBox;
		this.m_ActorCheckBox = theActorCheckBox;
		this.m_ActorNameTextField = theActorName;
		this.m_SourceActor = theSourceActor;
	}
	
	public JTextField getTextField(){
		return m_ActorNameTextField;
	}
	
	public boolean isSelected(){
		return m_ActorCheckBox.isSelected();
	}
	
	public String getName(){
		return m_ActorNameTextField.getText();
	}
	
	public String getSourceActorName(){
		return m_SourceActor.getName();
	}
	
	public IRPActor getSourceActor(){
		return m_SourceActor;
	}
	
	public void updateToBestActorNamesBasedOn(
			String theBlockName ){
		
		String theOriginalActorName;
		
		if( m_SourceActor != null ){
			theOriginalActorName = m_SourceActor.getName();
		} else {
			theOriginalActorName = m_ActorBlankName;
		}
		
		String theDesiredName;
		
		if( theBlockName.isEmpty() ){
			theDesiredName = _context.toLegalClassName( theOriginalActorName );
		} else {
			theDesiredName = _context.toLegalClassName( theOriginalActorName ) + "_" + theBlockName;
		}
		
		String theProposedActorName = _context.determineUniqueNameBasedOn( 
				theDesiredName, 
				"Actor", 
				_context.get_rhpPrj() );
		
		m_ActorNameTextField.setText( theProposedActorName );
	}
	
	@SuppressWarnings("unchecked")
	public IRPLink getExistingLinkBetweenBaseClassifiersOf(
			IRPClassifier theClassifier, 
			IRPClassifier andTheClassifier ){
		
		int isLinkFoundCount = 0;
		IRPLink theExistingLink = null;
		
		IRPModelElement theFAPackage = 
				theClassifier.getProject().findNestedElementRecursive(
						"FunctionalAnalysisPkg", "Package" );

		if( theFAPackage != null && theFAPackage instanceof IRPPackage ){
			
			List<IRPClassifier> theOtherEndsBases = new ArrayList<>();
			theOtherEndsBases.add( andTheClassifier );
			theOtherEndsBases.addAll( andTheClassifier.getBaseClassifiers().toList() );
			
			List<IRPClassifier> theSourcesBases = new ArrayList<>();
			theSourcesBases.add( theClassifier );
			theSourcesBases.addAll( theClassifier.getBaseClassifiers().toList() );
			
			
			List<IRPClass> theBuildingBlocks = 
					_settings.getBuildingBlocks( 
							(IRPPackage) theFAPackage );

			for( IRPClass theBuildingBlock : theBuildingBlocks ){
				
				_context.debug("Found theBuildingBlock: " + _context.elInfo( theBuildingBlock ) );
				
				List<IRPLink> theLinks = theBuildingBlock.getLinks().toList();
			
				for( IRPLink theLink : theLinks ){
					
					IRPModelElement fromEl = theLink.getFromElement();
					IRPModelElement toEl = theLink.getToElement();
					
					if( fromEl != null && 
						fromEl instanceof IRPInstance && 
						toEl != null && 
						toEl instanceof IRPInstance ){
					
						IRPClassifier fromClassifier = ((IRPInstance)fromEl).getOtherClass();
						IRPClassifier toClassifier = ((IRPInstance)toEl).getOtherClass();
						
						if( ( theOtherEndsBases.contains( toClassifier ) &&
						      theSourcesBases.contains( fromClassifier ) ) ||
								
							( theSourcesBases.contains( toClassifier ) &&
							  theOtherEndsBases.contains( fromClassifier ) ) ){
							
							_context.debug("Found that " + _context.elInfo( fromClassifier ) 
									+ " is already linked to " + _context.elInfo( toClassifier ) );
							
							theExistingLink = theLink;
							isLinkFoundCount++;
						}						
					}
				}
			}
		}
		
		if( isLinkFoundCount > 1 ){
			_context.warning("Warning in getExistingLinkBetweenBaseClassifiersOf, there are " + isLinkFoundCount );
		}
		
		return theExistingLink;
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

						_context.debug( _context.elInfo( theConnectedToPart ) + " was found to connect the actors to, and is typed by " + 
								_context.elInfo( connectedToBlock ) );

					} else if ( isTestDriver ){

						theTesterPart = thePart;
						theTesterBlock = theOtherClass;

						_context.debug( _context.elInfo( theTesterPart ) + " was found as the test driver, and is typed by " + 
								_context.elInfo( theTesterBlock ) );
					}
				}				
			}

			IRPPackage thePackageForActor = 
					_settings.getPackageForActorsAndTest(
							theProject );

			IRPActor theActor = thePackageForActor.addActor( theLegalActorName );
			theActor.highLightElement();

			String theText = "Create actor called " + m_ActorNameTextField.getText();

			// Make each of the actors a part of the SystemAssembly block
			theActorPart = (IRPInstance) theAssemblyBlock.addNewAggr( "Part", "" );
			theActorPart.highLightElement();
			theActorPart.setOtherClass( theActor );

			if( isInheritanceAllowed ){

				IRPModelElement theInheritedFrom = 
						m_InheritedFromComboBox.getSelectedRhapsodyItem();
				
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
						_context.error("Error: Unable to find Actor with name Testbench");
					}
				}

			} else {

				IRPStereotype theTestBenchStereotype =
						_context.getStereotypeForTestbench( 
								theActor );
						
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

			_context.info( "Finishing adding part connected to actor" );

		} else {
			_context.info( "Not selected" );
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
				getExistingLinkBetweenBaseClassifiersOf(
						theTesterBlock, theActor );
		
		IRPPort theActorToTesterPort = null;
		IRPPort theTesterToActorPort = null;
		
		if( existingLinkConnectingTesterToActor != null ){
			
			_context.debug( "There are existing ports between " + 
					_context.elInfo( theTesterBlock ) + " and " + 
					_context.elInfo( theActor ) );
		
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

			_context.info( "Creating a new connector between " + 
					_context.elInfo( theTesterBlock ) + " and " + 
					_context.elInfo( theActor ) );

			try {
				// and connect actor to the TestDriver block
		    	theActorToTesterPort = 
		    			(IRPPort) theActor.addNewAggr( "Port", "pTester" );
		    	
				theTesterToActorPort = 
						(IRPPort) theTesterBlock.addNewAggr(
								"Port", "p" + theActor.getName() );
			} catch (Exception e) {
				_context.error("Exception while trying to add ports");
			}
		}
		
		IRPLink theTesterLink = 
				(IRPLink) theAssemblyBlock.addLink(
						theActorPart, 
						theTesterPart, 
						null, 
						theActorToTesterPort, 
						theTesterToActorPort );
		
		theTesterLink.changeTo("connector");
	}

	public void connectActorPartWithBlockPartIn(
			IRPClass theAssemblyBlock,
			IRPInstance theConnectedToPart,
			IRPInstance theActorPart ){
		
		IRPClassifier theActor = theActorPart.getOtherClass();
		IRPClassifier connectedToBlock = theConnectedToPart.getOtherClass();
		
		IRPLink existingLinkConnectingBlockToActor = 
				getExistingLinkBetweenBaseClassifiersOf(
						connectedToBlock, theActor );
		
		IRPPort theActorToSystemPort = null;
		IRPPort theSystemToActorPort = null;
		
		if( existingLinkConnectingBlockToActor != null ){
			
			_context.debug( "There is an existing connector between " + 
					_context.elInfo( connectedToBlock ) + " and " + _context.elInfo( theActor ) );
		
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

			_context.info( "Creating a new connector between " + 
					_context.elInfo( connectedToBlock ) + " and " + 
					_context.elInfo( theActor ) );

			String theActorPortName = 
					_context.determineUniqueNameBasedOn(
							"p" + connectedToBlock.getName() , "Port", theActor);

			_context.debug("Attempting to create port called " + 
					theActorPortName + " owned by " + _context.elInfo( theActor ));

			// and connect actor to the LogicalSystem block
			theActorToSystemPort = 
					(IRPPort) theActor.addNewAggr( 
							"Port", theActorPortName);

			String theSystemPortName = 
					_context.determineUniqueNameBasedOn(
							"p" + theActor.getName() , "Port", connectedToBlock);

			_context.debug( "Attempting to create port called " + 
					theSystemPortName + " owned by " + 
					_context.elInfo( connectedToBlock ) );

			try {
				theSystemToActorPort = 
						(IRPPort) connectedToBlock.addNewAggr(
								"Port", theSystemPortName );	
			} catch (Exception e) {
				_context.error("Exception while trying to create system to actor port");
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
			
			theLogicalSystemLink.changeTo("connector");
			
		} catch (Exception e) {
			_context.error("Exception while trying to addLink");
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