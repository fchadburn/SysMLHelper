package com.mbsetraining.sysmlhelper.featurefunctionpkgcreator;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import com.mbsetraining.sysmlhelper.executablembse.ExecutableMBSE_Context;
import com.mbsetraining.sysmlhelper.graphelementhelpers.GraphNodeResizer;
import com.telelogic.rhapsody.core.*;

public class FeatureFunctionPkgCreator {

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
			FeatureFunctionPkgCreator theCreator = new FeatureFunctionPkgCreator( theContext );
			theCreator.createFeatureFunctionPkgs( theUseCases );
		}
	}
	
	public FeatureFunctionPkgCreator(
			ExecutableMBSE_Context context ) {
		
		_context = context;
	}
	
	public void createFeatureFunctionPkgs(
			List<IRPUseCase> theUseCases ){
	
		for( IRPUseCase theUseCase : theUseCases ){
			createFeatureFunctionPkg( 
					theUseCase, 
					_context.POST_FIX_FOR_FEATURE_FUNCTION_PKG, 
					_context.POST_FIX_FOR_FEATURE_FUNCTION_WORKING_COPY_PKG );
		}
	}
	
	public void createFeatureFunctionPkg(
			IRPUseCase theUseCase,
			String usingPostFix,
			String andWorkingCopyPkgPostFix ){
		
		String theOriginalName = theUseCase.getName();
		
		String theCamelCaseName = _context.toLegalClassName( theOriginalName ) + usingPostFix;
		
		IRPPackage theUseCaseOwnerPkg = (IRPPackage) _context.getOwningPackageFor( theUseCase );
		IRPPackage theOwningPkg = (IRPPackage) theUseCase.getProject();
		
		String theUniqueName = _context.determineUniqueNameBasedOn( theCamelCaseName, "Package", theOwningPkg);
		
		_context.debug( "Creating package for " + _context.elInfo( theUseCase ) + " called " + theUniqueName );

		IRPPackage theNewPkg = theOwningPkg.addNestedPackage( theUniqueName );
		theNewPkg.changeTo( _context.FUNCT_ANALYSIS_FEATURE_FUNCTION_PACKAGE );
		
		IRPClass theFeature = theNewPkg.addClass( theOriginalName );
		theFeature.changeTo( _context.FEATURE_BLOCK );
		
		IRPDependency theUCDependency = theFeature.addDependencyTo( theUseCase );
		
		theUCDependency.highLightElement();
		
		IRPPackage theWorkingCopyPkg = theNewPkg.
				addNestedPackage( theUniqueName.replace( usingPostFix, andWorkingCopyPkgPostFix ) );
		
		theWorkingCopyPkg.changeTo( _context.REQTS_ANALYSIS_WORKING_COPY_PACKAGE );
		
		IRPDependency theDependency = theWorkingCopyPkg.
				addDependencyBetween( theWorkingCopyPkg, theUseCaseOwnerPkg );
		
		theDependency.highLightElement();
		
		IRPObjectModelDiagram theDiagram = 
				(IRPObjectModelDiagram) theFeature.addNewAggr( "ObjectModelDiagram", "" );
		
		theDiagram.changeTo( _context.BLOCK_DEFINITION_DIAGRAM_SYSTEM );
		
		_context.setDiagramNameToOwningClassifierIfAppropriate( theDiagram, theFeature );

		// The resizer will deal with width and height, hence just use 50,50 to start with
		IRPGraphNode theGraphNode = theDiagram.addNewNodeForElement( theFeature, 450, 150, 50, 50 );
		GraphNodeResizer theResizer = new GraphNodeResizer( theGraphNode, _context );
		theResizer.performResizing();
		
		theFeature.highLightElement();
	}
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