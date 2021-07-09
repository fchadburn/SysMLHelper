package com.mbsetraining.sysmlhelper.sequencediagram;

import java.util.Iterator;
import java.util.List;
import com.mbsetraining.sysmlhelper.common.ConfigurationSettings;
import com.mbsetraining.sysmlhelper.common.UserInterfaceHelper;
import com.mbsetraining.sysmlhelper.executablembse.ExecutableMBSE_Context;
import com.telelogic.rhapsody.core.*;

import functionalanalysisplugin.FunctionalAnalysisSettings;

public class SequenceDiagramCreator {	

	protected ConfigurationSettings _context;

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
					true );
		}
	}
	
	public SequenceDiagramCreator(
			ConfigurationSettings context ) {

		_context = context;
	}

	public void updateLifelinesToMatchPartsInActiveBuildingBlock(
			IRPSequenceDiagram theSequenceDiagram ){

		FunctionalAnalysisSettings theSettings = new FunctionalAnalysisSettings(_context);
		
		IRPClass theBuildingBlock = 
				theSettings.getBuildingBlock( theSequenceDiagram );

		if( theBuildingBlock != null ){

			createSequenceDiagramFor(
					theBuildingBlock, 
					(IRPPackage) theSequenceDiagram.getOwner(), 
					theSequenceDiagram.getName(),
					_context.getIsCreateSDWithAutoShowApplied( theSequenceDiagram ),
					false );

		} else {
			_context.error("Error, unable to find building block or tester pkg");
		}
	}

	public void updateAutoShowSequenceDiagramFor(
			IRPClass theAssemblyBlock) {

		FunctionalAnalysisSettings theSettings = new FunctionalAnalysisSettings(_context);

		IRPPackage thePackageForSD = 
				theSettings.getPackageForActorsAndTest(
						theAssemblyBlock.getProject() );

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
						_context.getIsCreateSDWithAutoShowApplied( theSD ),
						false );
			}
		}
	}

	public void createSequenceDiagramFor(
			IRPClass theAssemblyBlock, 
			IRPPackage inPackage,
			String withName,
			boolean isAutoShow,
			boolean isOpenDiagram ){

		boolean isCreateSD = true;

		IRPModelElement theExistingDiagram = 
				inPackage.findNestedElement( withName, "SequenceDiagram" );

		@SuppressWarnings("unchecked")
		List<IRPInstance> theParts =
		theAssemblyBlock.getNestedElementsByMetaClass( "Part", 0 ).toList();

		if( theExistingDiagram != null ){

			String theMsg = _context.elInfo( theExistingDiagram ) + " already exists in " + 
					_context.elInfo( inPackage ) + "\nDo you want to recreate it with x" + 
					theParts.size() + " lifelines for:\n";

			for( Iterator<IRPInstance> iterator = theParts.iterator(); iterator.hasNext(); ){

				IRPInstance thePart = (IRPInstance) iterator.next();
				IRPClassifier theType = thePart.getOtherClass();
				theMsg += thePart.getName() + "." + theType.getName() + 
						" (" + theType.getUserDefinedMetaClass() + ")\n"; 
			}

			isCreateSD = UserInterfaceHelper.askAQuestion( theMsg );

			if( isCreateSD ){
				theExistingDiagram.deleteFromProject();
			}
		}

		if( isCreateSD ){

			IRPSequenceDiagram theSD = inPackage.addSequenceDiagram( withName );

			int xPos = 30;
			int yPos = 0;
			int nWidth = 100;
			int nHeight = 1000;
			int xGap = 30;

			// Do Test Driver first
			for( IRPInstance thePart : theParts ) {

				if( _context.hasStereotypeCalled( "TestDriver", thePart.getOtherClass() ) && 
						_context.getIsCreateSDWithTestDriverLifeline( theSD ) ){

					//IRPClassifier theType = thePart.getOtherClass();
					theSD.addNewNodeForElement( thePart, xPos, yPos, nWidth, nHeight );
					xPos = xPos + nWidth + xGap;
				}
			}

			// Then actors
			for( IRPInstance thePart : theParts ) {

				IRPClassifier theType = thePart.getOtherClass();

				if( theType instanceof IRPActor ){
					
					if( isPartTheOnlyOneOfItsTypeUnder( theAssemblyBlock, thePart ) ){
						
						theSD.addNewNodeForElement( theType, xPos, yPos, nWidth, nHeight );
					} else {
						theSD.addNewNodeForElement( thePart, xPos, yPos, nWidth, nHeight );
					}
					
					xPos = xPos + nWidth + xGap;
				}
			}

			// Then components
			for( IRPInstance thePart : theParts ) {

				IRPClassifier theType = thePart.getOtherClass();

				if( !( theType instanceof IRPActor ) &&
						!_context.hasStereotypeCalled( "TestDriver", theType ) ){

					if( isPartTheOnlyOneOfItsTypeUnder( theAssemblyBlock, thePart ) ){
						
						theSD.addNewNodeForElement( theType, xPos, yPos, nWidth, nHeight );
					} else {
						theSD.addNewNodeForElement( thePart, xPos, yPos, nWidth, nHeight );
					}
					
					xPos = xPos + nWidth + xGap;
				}
			}

			if( isAutoShow ){
				_context.applyExistingStereotype( "AutoShow", theSD );
			}
			
			if( isOpenDiagram ){
				theSD.highLightElement();
				theSD.openDiagram();
			}
		}
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
						_context.debug( "Found that " + _context.elInfo( theCandidate ) + 
								" has same type as " + _context.elInfo( thePart ) );
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

