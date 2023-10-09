package com.mbsetraining.sysmlhelper.usecasemover;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import com.mbsetraining.sysmlhelper.common.UserInterfaceHelper;
import com.mbsetraining.sysmlhelper.executablembse.ExecutableMBSE_Context;
import com.telelogic.rhapsody.core.*;

public class UseCaseMover {

	ExecutableMBSE_Context _context;
	
	public static void main(String[] args) {

		IRPApplication theRhpApp = RhapsodyAppServer.getActiveRhapsodyApplication();
		
		ExecutableMBSE_Context theContext = new ExecutableMBSE_Context( theRhpApp.getApplicationConnectionString() );	
		
		List<IRPUseCase> theUseCases = new ArrayList<>();
		
		Set<IRPModelElement> theSelectedEls = 
				theContext.getSetOfElementsFromCombiningThe( 
						theContext.getSelectedElements(), theContext.getSelectedGraphElements() );
		
		for( IRPModelElement theSelectedEl : theSelectedEls ){
			
			if( theSelectedEl instanceof IRPUseCase ){
				theUseCases.add( (IRPUseCase) theSelectedEl );
			}
		}
		
		if( !theUseCases.isEmpty() ){
			UseCaseMover theMover = new UseCaseMover( theContext );
			theMover.moveUseCasesToNewPackages( theUseCases, true );
		}
	}
	
	public UseCaseMover(
			ExecutableMBSE_Context context ) {
		
		_context = context;
	}
	
	public void performMoveUseCasesIfConfirmed() {
		
		List<IRPUseCase> theUseCases = new ArrayList<>();

		List<IRPModelElement> theSelectedEls = _context.getSelectedElements();
		List<IRPGraphElement> theSelectedGraphEls = _context.getSelectedGraphElements();
		
		Set<IRPModelElement> theCandidateEls = 
				_context.getSetOfElementsFromCombiningThe( 
						theSelectedEls, theSelectedGraphEls );

		for( IRPModelElement theCandidateEl : theCandidateEls ){

			if( theCandidateEl instanceof IRPUseCase ){
				theUseCases.add( (IRPUseCase) theCandidateEl );
			}
		}

		if( theUseCases.isEmpty() ){

			_context.warning( "There were no selected use cases. Right-click a use case and try again");

		} else {

			String theMsg = "Do you want to move the " + theUseCases.size() + " selected use cases \n" +
					"into their own packages? \n";

			boolean answer = UserInterfaceHelper.askAQuestion( theMsg );

			if( answer ){
				UseCaseMover theMover = new UseCaseMover( _context );
				theMover.moveUseCasesToNewPackages( theUseCases, true );
			}
		}
	}
	private void moveUseCasesToNewPackages(
			List<IRPUseCase> theUseCases,
			boolean isNestedBelow ){
	
		for( IRPUseCase theUseCase : theUseCases ){
			moveUseCaseToNewPackage( theUseCase, isNestedBelow );
		}
	}
	
	public void moveUseCaseToNewPackage(
			IRPUseCase theUseCase,
			boolean isNestedBelow ){
		
		String theOriginalName = theUseCase.getName();
		String thePostFix = _context.getDefaultUseCasePackagePostfix( theUseCase );
		
		String theCamelCaseName = _context.toLegalClassName( theOriginalName );
		
		IRPPackage theOwningPkg;
		
		if( isNestedBelow ){
			theOwningPkg = (IRPPackage) _context.getOwningPackageFor( theUseCase );
		} else {
			theOwningPkg = (IRPPackage) _context.getOwningPackageFor( theUseCase ).getOwner();
		}

		String theUniqueName = _context.determineUniqueNameForPackageBasedOn( 
				theCamelCaseName, theOwningPkg) + thePostFix;
		
		_context.debug( "Moving " + _context.elInfo( theUseCase ) + " to " + theUniqueName );

		IRPPackage theNewPkg = theOwningPkg.addNestedPackage( theUniqueName );
		theNewPkg.changeTo( _context.REQTS_ANALYSIS_USE_CASE_PACKAGE );
		
		theNewPkg.highLightElement();
		
		theUseCase.setOwner( theNewPkg );	
				
		//addOrModifyNestedRequirementDiagramsIfNeeded( theUseCase, theOriginalName );
		//moveNestedActivityDiagramsToNestedPkg( theUseCase );
	}
	
	/*
	private void addOrModifyNestedRequirementDiagramsIfNeeded(
			IRPUseCase theUseCase, 
			String theOriginalName ){
		
		List<IRPModelElement> theRequirementDiagrams = 
				_context.findElementsWithMetaClassAndStereotype(
						"ObjectModelDiagram", 
						_context.NEW_TERM_FOR_REQUIRMENTS_DIAGRAM_SYSTEM, 
						theUseCase, 
						1 );
		
		if( theRequirementDiagrams.isEmpty() ){
			
			theRequirementDiagrams = 
					_context.findElementsWithMetaClassAndStereotype(
							"ObjectModelDiagram", 
							"Requirements Diagram", 
							theUseCase, 
							1 );
			
			if( theRequirementDiagrams.isEmpty() ){
				
				IRPDiagram theRD = (IRPDiagram) theUseCase.addNewAggr( 
						"ObjectModelDiagram", _context.REQUIREMENTS_DIAGRAM_PREFIX + theOriginalName );
				
				theRD.changeTo( _context.REQUIREMENTS_DIAGRAM_SYSTEM );

				String theUseCaseSize = theRD.getPropertyValue( 
						"Format.UseCase.DefaultSize" );
				
				String[] theSizeSplit = theUseCaseSize.split(",");
				int theWidth = Integer.parseInt( theSizeSplit[2] );
				int theHeight = Integer.parseInt( theSizeSplit[3] );
				
				int x = 420;
				int y = 350;
				
				theRD.addNewNodeForElement( theUseCase, x, y, theWidth, theHeight );
				
				theUseCase.highLightElement();
				
			} else {
				
				for( IRPModelElement theRequirementDiagram : theRequirementDiagrams ){
					
					_context.debug( "Changing type of " + _context.elInfo( theRequirementDiagram ) + 
							" to " + _context.REQUIREMENTS_DIAGRAM_SYSTEM );
					
					theRequirementDiagram.changeTo( _context.REQUIREMENTS_DIAGRAM_SYSTEM );
				}
			}
		}
	}
	
	public void moveNestedActivityDiagramsToNestedPkg(
			IRPUseCase theUseCase ){
		
		@SuppressWarnings("unchecked")
		List<IRPModelElement> theNestedDiagrams = 
				theUseCase.getNestedElementsByMetaClass( "ActivityDiagram", 1 ).toList();
		
		if( !theNestedDiagrams.isEmpty() ){
			
			String theOriginalName = theUseCase.getName();
			
			String theCamelCaseName = _context.toLegalClassName( theOriginalName );
			
			IRPPackage theOwningPkg = (IRPPackage) _context.getOwningPackageFor( theUseCase );
			
			String theUniqueName = _context.determineUniqueNameForPackageBasedOn( theCamelCaseName + "_Activity", theOwningPkg) + "Pkg";
			
			IRPPackage theNewPkg = theOwningPkg.addNestedPackage( theUniqueName );
			theNewPkg.changeTo( _context.REQTS_ANALYSIS_USE_CASE_PACKAGE );	
			
			for (IRPModelElement theNestedDiagram : theNestedDiagrams) {
				
				theNestedDiagram.setOwner( theNewPkg );
				
				IRPHyperLink theHyperLink = (IRPHyperLink) theUseCase.addNewAggr("HyperLink", "");
				theHyperLink.setDisplayOption(HYPNameType.RP_HYP_NAMETEXT, "");		
				theHyperLink.setTarget( theNestedDiagram );
				
				_context.debug( "Added" + _context.elInfo( theHyperLink ) + " to " + _context.elInfo( theUseCase ) );
			}
		}
	}*/
}

/**
 * Copyright (C) 2022-2023  MBSE Training and Consulting Limited (www.executablembse.com)

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