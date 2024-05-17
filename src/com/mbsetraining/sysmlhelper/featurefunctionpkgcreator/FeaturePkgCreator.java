package com.mbsetraining.sysmlhelper.featurefunctionpkgcreator;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import com.mbsetraining.sysmlhelper.executablembse.ExecutableMBSE_Context;
import com.mbsetraining.sysmlhelper.graphelementhelpers.GraphNodeResizer;
import com.telelogic.rhapsody.core.*;

public class FeaturePkgCreator {

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
			FeaturePkgCreator theCreator = new FeaturePkgCreator( theContext );
			theCreator.createFeatureFunctionPkgs( theUseCases );
		}
	}
	
	public FeaturePkgCreator(
			ExecutableMBSE_Context context ) {
		
		_context = context;
	}
	
	public void createFeatureFunctionPkgs(
			List<IRPUseCase> theUseCases ){
	
		for( IRPUseCase theUseCase : theUseCases ){
			createFeatureFunctionPkg( 
					theUseCase, 
					_context.POST_FIX_FOR_FEATURE_PKG, 
					_context.POST_FIX_FOR_FEATURE_WORKING_COPY_PKG );
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
		
		IRPStructureDiagram ibd = addInternalBlockDiagram( theFeature );
		ibd.openDiagram();
		ibd.highLightElement();
		
		IRPObjectModelDiagram bdd = addBlockDefinitionDiagram( theUseCase, theFeature );
		bdd.openDiagram();
		bdd.highLightElement();
	}
	
	public void createFeatureFunctionPkg(
			IRPPackage underPkg,
			String withPackageName,
			String andFeatureName,
			IRPStereotype withStereotypeIfApplicable ){
								
		String theUniqueName = _context.determineUniqueNameBasedOn( withPackageName, "Package", underPkg );
		
		_context.debug( "Creating package called " + theUniqueName + " under " + _context.elInfo( underPkg ) );

		IRPPackage theNewPkg = underPkg.addNestedPackage( theUniqueName );
		theNewPkg.changeTo( _context.FUNCT_ANALYSIS_FEATURE_FUNCTION_PACKAGE );
		
		IRPClass theFeature = theNewPkg.addClass( andFeatureName );
		theFeature.changeTo( _context.FEATURE_BLOCK );
		
		IRPStructureDiagram ibd = addInternalBlockDiagram( theFeature );
		ibd.openDiagram();
		ibd.highLightElement();
		
		IRPObjectModelDiagram bdd = addBlockDefinitionDiagram( null, theFeature );
		bdd.openDiagram();
		bdd.highLightElement();
		
		if( withStereotypeIfApplicable instanceof IRPStereotype ) {
			
			theNewPkg.addSpecificStereotype( withStereotypeIfApplicable );
			theFeature.addSpecificStereotype( withStereotypeIfApplicable );
		}		
	}

	private IRPObjectModelDiagram addBlockDefinitionDiagram(
			IRPUseCase theUseCase, 
			IRPClass underTheFeature ){
		
		IRPObjectModelDiagram theDiagram = 
				(IRPObjectModelDiagram) underTheFeature.addNewAggr( "ObjectModelDiagram", "" );
		
		theDiagram.changeTo( _context.BLOCK_DEFINITION_DIAGRAM_SYSTEM );
		
		_context.setDiagramNameToOwningClassifierIfAppropriate( theDiagram, underTheFeature );

		// The re-sizer will deal with width and height, hence just use 50,50 to start with
		IRPGraphNode theFeatureGraphNode = theDiagram.addNewNodeForElement( underTheFeature, 450, 150, 50, 50 );
		GraphNodeResizer theFeatureResizer = new GraphNodeResizer( theFeatureGraphNode, _context );
		theFeatureResizer.performResizing();
		
		if( theUseCase != null ) {
			// The re-sizer will deal with width and height, hence just use 50,50 to start with
			IRPGraphNode theUseCaseGraphNode = theDiagram.addNewNodeForElement( theUseCase, 150, 100, 50, 50 );
			GraphNodeResizer theUseCaseResizer = new GraphNodeResizer( theUseCaseGraphNode, _context );
			theUseCaseResizer.performResizing();
		}

		IRPCollection theGraphElsToComplete = _context.get_rhpApp().createNewCollection();
		theGraphElsToComplete.addGraphicalItem( theFeatureGraphNode );
		theDiagram.completeRelations( theGraphElsToComplete, 1 );
		
		return theDiagram;
	}
	
	private IRPStructureDiagram addInternalBlockDiagram(
			IRPClass underTheFeature ){
		
		IRPStructureDiagram theDiagram = 
				(IRPStructureDiagram) underTheFeature.addNewAggr( "StructureDiagram", "" );
		
		theDiagram.changeTo( _context.INTERNAL_BLOCK_DIAGRAM_FUNCTIONAL );
		
		_context.setDiagramNameToOwningClassifierIfAppropriate( theDiagram, underTheFeature );

		return theDiagram;
	}
}

/**
 * Copyright (C) 2022-2024  MBSE Training and Consulting Limited (www.executablembse.com)

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