package com.mbsetraining.sysmlhelper.sequencediagram;

import java.util.ArrayList;
import java.util.List;

import com.mbsetraining.sysmlhelper.common.UserInterfaceHelper;
import com.mbsetraining.sysmlhelper.executablembse.ExecutableMBSE_Context;
import com.telelogic.rhapsody.core.*;

public class SequenceDiagramCreator {	

	protected ExecutableMBSE_Context _context;

	public static void main(String[] args) {
		
		IRPApplication theRhpApp = RhapsodyAppServer.getActiveRhapsodyApplication();
		
		IRPModelElement theSelectedEl = theRhpApp.getSelectedElement();

		if( theSelectedEl instanceof IRPClass ){
			
			ExecutableMBSE_Context theContext = new ExecutableMBSE_Context( theRhpApp.getApplicationConnectionString() );
			SequenceDiagramCreator theHelper = new SequenceDiagramCreator( theContext );
			
			IRPPackage theOwningPkg = theContext.getOwningPackageFor( theSelectedEl );
			
			theHelper.createSequenceDiagramFor( 
					(IRPClass) theSelectedEl, 
					theOwningPkg, 
					"SD - " + theSelectedEl.getName(),
					false,
					true,
					false );
			
		} else if( theSelectedEl instanceof IRPSequenceDiagram ){
			
			ExecutableMBSE_Context theContext = new ExecutableMBSE_Context( theRhpApp.getApplicationConnectionString() );
			SequenceDiagramCreator theHelper = new SequenceDiagramCreator( theContext );
			theHelper.updateLifelinesToMatchPartsInActiveBuildingBlock( (IRPSequenceDiagram) theSelectedEl );
		}
	}
	
	public SequenceDiagramCreator(
			ExecutableMBSE_Context context ) {

		_context = context;
	}

	public void updateLifelinesToMatchPartsInActiveBuildingBlock(
			IRPSequenceDiagram theSequenceDiagram ){
		
		_context.get_selectedContext().setContextTo( theSequenceDiagram );
		
		IRPClass theBuildingBlock = 
				_context.get_selectedContext().getBuildingBlock();

		if( theBuildingBlock != null ){

			createSequenceDiagramFor(
					theBuildingBlock, 
					(IRPPackage) theSequenceDiagram.getOwner(), 
					theSequenceDiagram.getName(),
					_context.getIsCreateSDWithAutoShowApplied(),
					false,
					true );

		} else {
			_context.error( "Unable to find building block or tester pkg" );
		}
	}

	public void updateAutoShowSequenceDiagramFor(
			IRPClass theAssemblyBlock) {

		IRPPackage thePackageForSD = 
				_context.get_selectedContext().getScenarioRootContextPackage();
		
		if( thePackageForSD != null ){

			List<IRPModelElement> theSDs = 
					_context.findElementsWithMetaClassAndStereotype(
							"SequenceDiagram", 
							"AutoShow", 
							thePackageForSD, 
							0 );

			if( theSDs.size()==1 ){

				IRPSequenceDiagram theSD = (IRPSequenceDiagram) theSDs.get( 0 );

				createSequenceDiagramFor(
						theAssemblyBlock, 
						thePackageForSD, 
						theSD.getName(),
						_context.getIsCreateSDWithAutoShowApplied(),
						false,
						true );
			} else {
				_context.debug( "Unable to find one sequence diagram with AutoShow stereotype under " +
						_context.elInfo( thePackageForSD ) );
			}
		}
	}

	protected List<IRPInstance> getActorPartsFromLevelAbove(
			IRPClass theAssemblyBlock ){
	
		List<IRPInstance> theActorParts = new ArrayList<IRPInstance>();
		
		IRPClass theDomainBlock = getDomainBlockAbove( theAssemblyBlock );
		
		if( theDomainBlock != null ){			
			//_context.debug( "The DomainBlock for " + _context.elInfo( theAssemblyBlock ) + 
			//		" is " + _context.elInfo( theDomainBlock ) );
			
			@SuppressWarnings("unchecked")
			List<IRPInstance> theCandidates = theDomainBlock.getNestedElementsByMetaClass( "Part", 0 ).toList();
			
			for( IRPInstance theCandidate : theCandidates ){
			
				IRPClassifier theType = theCandidate.getOtherClass();

				if( theType instanceof IRPActor ){
					theActorParts.add( theCandidate );
				}
			}
		}
		
		return theActorParts;
	}

	private IRPClass getDomainBlockAbove(
			IRPClass theAssemblyBlock ){
		
		IRPClass theDomainBlock = null;
		
		@SuppressWarnings("unchecked")
		List<IRPModelElement> theReferences = theAssemblyBlock.getReferences().toList();
		
		List<IRPClass> theDomainBlockCandidates = new ArrayList<>();
		
		for( IRPModelElement theReference : theReferences ){
			
			//_context.debug( _context.elInfo( theReference ) + " is a candidate" );
			
			if( theReference instanceof IRPInstance ){
				
				IRPInstance theInstance = (IRPInstance)theReference;
				IRPModelElement theInstanceOwner = theInstance.getOwner();
				
				IRPClassifier theOtherClass = theInstance.getOtherClass();
				
				if( theOtherClass instanceof IRPClass && 
						theOtherClass.equals( theAssemblyBlock ) &&
						theInstanceOwner instanceof IRPClass ){
					
					//_context.debug( "getDomainBlockAbove found " + 
					//		_context.elInfo( theInstanceOwner ) + " for " + 
					//		_context.elInfo( theAssemblyBlock ) );
					
					theDomainBlockCandidates.add( (IRPClass) theInstanceOwner );
				}
			}
		}
		
		int size = theDomainBlockCandidates.size();
		
		if( size == 1 ){
			theDomainBlock = theDomainBlockCandidates.get( 0 );
		} else if( size > 1 ){
			_context.debug( "Unable to determine DomainBlock for " + _context.elInfo( theAssemblyBlock ) + 
					" as there are " + size + " candidates not 1" );
		}
		
		return theDomainBlock;
	}
	
	public IRPSequenceDiagram createSequenceDiagramFor(
			IRPClass theAssemblyBlock, 
			IRPPackage inPackage,
			String withName,
			boolean isAutoShow,
			boolean isOpenDiagram,
			boolean isRecreateExisting ){

		boolean isCreateSD = true;
		
		IRPSequenceDiagram theSD = null;

		@SuppressWarnings("unchecked")
		List<IRPInstance> theParts = theAssemblyBlock.getNestedElementsByMetaClass( "Part", 0 ).toList();
		
		theParts.addAll( getActorPartsFromLevelAbove( theAssemblyBlock ) );

		List<IRPInstance> theTestDriverParts = new ArrayList<IRPInstance>();
		List<IRPInstance> theActorParts = new ArrayList<IRPInstance>();
		List<IRPInstance> theBlockParts = new ArrayList<IRPInstance>();

		// Determine actors vs. internal parts vs test drivers
		for( IRPInstance thePart : theParts ) {

			if( _context.isTestDriver( thePart ) ){
				theTestDriverParts.add( thePart );
			} else if( thePart.getOtherClass() instanceof IRPActor ){
				theActorParts.add( thePart );
			} else {
				theBlockParts.add( thePart );
			}
		}
		
		if( isRecreateExisting ){

			IRPModelElement theExistingDiagram = 
					inPackage.findNestedElement( withName, "SequenceDiagram" );

			if( theExistingDiagram != null ){

				String theMsg = _context.elInfo( theExistingDiagram ) + " already exists in \n" + 
						_context.elInfo( inPackage ) + "\n\nDo you want to recreate it with " + 
						theParts.size() + " lifelines for:\n";

				// Do Test Driver first, if enabled in properties
				if( _context.getIsCreateSDWithTestDriverLifeline() ){

					for( IRPInstance thePart : theTestDriverParts ) {
	
						IRPClassifier theType = thePart.getOtherClass();
						theMsg += theType.getName() + 
								" (" + theType.getUserDefinedMetaClass() + ")\n"; 
					}
				}	

				// Then actors
				for( IRPInstance thePart : theActorParts ) {

					if( isPartTheOnlyOneOfItsTypeUnder( theAssemblyBlock, thePart ) ){
						
						IRPClassifier theType = thePart.getOtherClass();
						theMsg += theType.getName() + 
								" (" + theType.getUserDefinedMetaClass() + ")\n"; 
					} else {
						IRPClassifier theType = thePart.getOtherClass();
						theMsg += thePart.getName() + "." + theType.getName() + 
								" (" + theType.getUserDefinedMetaClass() + ")\n"; 
					}
				}

				// Then blocks
				for( IRPInstance thePart : theBlockParts ) {

					if( isPartTheOnlyOneOfItsTypeUnder( theAssemblyBlock, thePart ) ){
						
						IRPClassifier theType = thePart.getOtherClass();
						theMsg += theType.getName() + 
								" (" + theType.getUserDefinedMetaClass() + ")\n"; 
					} else {
						IRPClassifier theType = thePart.getOtherClass();
						theMsg += thePart.getName() + "." + theType.getName() + 
								" (" + theType.getUserDefinedMetaClass() + ")\n"; 
					}
				}
				
				isCreateSD = UserInterfaceHelper.askAQuestion( theMsg );

				if( isCreateSD ){
					theExistingDiagram.deleteFromProject();
				}
			}

		} else {
			
			withName = _context.determineUniqueNameBasedOn( withName, "SequenceDiagram", inPackage );
		}

		if( isCreateSD ){

			theSD = inPackage.addSequenceDiagram( withName );

			int xPos = 30;
			int yPos = 0;
			int nWidth = 100;
			int nHeight = 1000;
			int xGap = 30;

			// Do Test Driver first, if enabled in properties
			if( _context.getIsCreateSDWithTestDriverLifeline() ){

				for( IRPInstance thePart : theTestDriverParts ) {

					theSD.addNewNodeForElement( thePart, xPos, yPos, nWidth, nHeight );
					xPos = xPos + nWidth + xGap;
				}
			}	

			// Then actors
			for( IRPInstance thePart : theActorParts ) {

				if( isPartTheOnlyOneOfItsTypeUnder( theAssemblyBlock, thePart ) ){
					
					IRPClassifier theType = thePart.getOtherClass();
					theSD.addNewNodeForElement( theType, xPos, yPos, nWidth, nHeight );
				} else {
					theSD.addNewNodeForElement( thePart, xPos, yPos, nWidth, nHeight );
				}
					
				xPos = xPos + nWidth + xGap;
			}

			// Then components
			for( IRPInstance thePart : theBlockParts ) {

				IRPClassifier theType = thePart.getOtherClass();

				if( isPartTheOnlyOneOfItsTypeUnder( theAssemblyBlock, thePart ) ){
						
					theSD.addNewNodeForElement( theType, xPos, yPos, nWidth, nHeight );
				} else {
					theSD.addNewNodeForElement( thePart, xPos, yPos, nWidth, nHeight );
				}
					
				xPos = xPos + nWidth + xGap;
			}

			if( isAutoShow ){
				_context.applyExistingStereotype( "AutoShow", theSD );
			}
			
			_context.info( _context.elInfo( theSD ) + " owned by " + _context.elInfo( theSD.getOwner() ) + 
					" was created with " + theParts.size() + " lifelines" );
			
			if( isOpenDiagram ){
				theSD.highLightElement();
			}
		}
		
		return theSD;
	}
	
	private boolean isPartTheOnlyOneOfItsTypeUnder(
			IRPClassifier theClassifier,
			IRPInstance thePart ){
		
		boolean isPartTheOnlyOneOfItsType = true;
		
		if( thePart.isTypelessObject() == 0 ){
			
			IRPClassifier theType = thePart.getOtherClass();
			
			@SuppressWarnings("unchecked")
			List<IRPInstance> theCandidates =
					theClassifier.getNestedElementsByMetaClass( "Part", 0 ).toList();
			
			for (IRPInstance theCandidate : theCandidates) {
				
				if( !theCandidate.equals( thePart ) &&
						theCandidate.isTypelessObject()==0 ){
					
					IRPClassifier theCandidateType = theCandidate.getOtherClass();
					
					if( theCandidateType.equals( theType ) ){
						//_context.debug( "Found that " + _context.elInfo( theCandidate ) + 
						//		" has same type as " + _context.elInfo( thePart ) );
						isPartTheOnlyOneOfItsType = false;
						break;
					}
				}
			}
		}
		
		return isPartTheOnlyOneOfItsType;
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

