package com.mbsetraining.sysmlhelper.featurefunctionpkgcreator;

import com.mbsetraining.sysmlhelper.executablembse.ExecutableMBSE_Context;
import com.mbsetraining.sysmlhelper.graphelementhelpers.GraphNodeResizer;
import com.telelogic.rhapsody.core.*;

public class FunctionPkgCreator {

	ExecutableMBSE_Context _context;
	
	public FunctionPkgCreator(
			ExecutableMBSE_Context context ) {
		
		_context = context;
	}
	
	public void createFunctionPkg(
			IRPPackage underPkg,
			String withPackageName,
			String andFunctionName,
			IRPStereotype withStereotypeIfApplicable ){
								
		String theUniqueName = _context.determineUniqueNameBasedOn( withPackageName, "Package", underPkg );
		
		_context.debug( "Creating package called " + theUniqueName + " under " + _context.elInfo( underPkg ) );

		IRPPackage theNewPkg = underPkg.addNestedPackage( theUniqueName );
		theNewPkg.changeTo( _context.FUNCT_ANALYSIS_FEATURE_FUNCTION_PACKAGE );
		
		IRPClass theFunction = theNewPkg.addClass( andFunctionName );
		theFunction.changeTo( _context.FUNCTION_BLOCK );
		
		IRPStructureDiagram ibd = addInternalBlockDiagram( theFunction );
		ibd.openDiagram();
		ibd.highLightElement();
		
		IRPObjectModelDiagram bdd = addBlockDefinitionDiagram( theFunction );
		bdd.openDiagram();
		bdd.highLightElement();
		
		if( withStereotypeIfApplicable instanceof IRPStereotype ) {
			
			theNewPkg.addSpecificStereotype( withStereotypeIfApplicable );
			theFunction.addSpecificStereotype( withStereotypeIfApplicable );
		}		
	}

	private IRPObjectModelDiagram addBlockDefinitionDiagram(
			IRPClass underTheClass ){
		
		IRPObjectModelDiagram theDiagram = 
				(IRPObjectModelDiagram) underTheClass.addNewAggr( "ObjectModelDiagram", "" );
		
		theDiagram.changeTo( _context.BLOCK_DEFINITION_DIAGRAM_SYSTEM );
		
		_context.setDiagramNameToOwningClassifierIfAppropriate( theDiagram, underTheClass );

		// The re-sizer will deal with width and height, hence just use 50,50 to start with
		IRPGraphNode theGraphNode = theDiagram.addNewNodeForElement( underTheClass, 450, 150, 50, 50 );
		GraphNodeResizer theFeatureResizer = new GraphNodeResizer( theGraphNode, _context );
		theFeatureResizer.performResizing();
		
		return theDiagram;
	}
	
	private IRPStructureDiagram addInternalBlockDiagram(
			IRPClass underTheClass ){
		
		IRPStructureDiagram theDiagram = 
				(IRPStructureDiagram) underTheClass.addNewAggr( "StructureDiagram", "" );
		
		theDiagram.changeTo( _context.INTERNAL_BLOCK_DIAGRAM_FUNCTIONAL );
		
		_context.setDiagramNameToOwningClassifierIfAppropriate( theDiagram, underTheClass );

		return theDiagram;
	}
}

/**
 * Copyright (C) 2024  MBSE Training and Consulting Limited (www.executablembse.com)

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