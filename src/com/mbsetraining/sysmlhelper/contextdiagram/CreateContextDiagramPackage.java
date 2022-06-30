package com.mbsetraining.sysmlhelper.contextdiagram;

import java.util.ArrayList;
import java.util.List;

import com.mbsetraining.sysmlhelper.executablembse.AutoPackageDiagram;
import com.mbsetraining.sysmlhelper.executablembse.ExecutableMBSE_Context;
import com.mbsetraining.sysmlhelper.usecasepackage.CreateActorPkg;
import com.mbsetraining.sysmlhelper.usecasepackage.CreateRequirementsPkg;
import com.mbsetraining.sysmlhelper.usecasepackage.CreateActorPkg.CreateActorPkgOption;
import com.telelogic.rhapsody.core.*;

public class CreateContextDiagramPackage {

	protected ExecutableMBSE_Context _context;
	
	public CreateContextDiagramPackage(
			String theContextDiagramPackageName,
			IRPModelElement theSystemBlock,
			IRPPackage theOwningPkg,
			CreateRequirementsPkg.CreateRequirementsPkgOption theReqtsPkgChoice,
			String theReqtsPkgOptionalName,
			IRPPackage theExistingReqtsPkgIfChosen,
			CreateActorPkg.CreateActorPkgOption theActorPkgChoice,
			String theActorPkgName,
			IRPPackage theExistingActorPkg,
			String theActorPkgPrefixOption,
			CreateExternalSignalsPkg.CreateExternalSignalsPkgOption theExternalSignalsPkgChoice,
			String theExternalSignalsPkgOptionalName,
			IRPPackage theExistingExternalSignalsPkgIfChosen,
			ExecutableMBSE_Context theContext ){
		
		_context = theContext;
		
		String thePkgName = _context.determineUniqueNameBasedOn( 
				theContextDiagramPackageName.replace(" ", "") + "_ContextPkg", "Package", theOwningPkg );
		
		IRPProject theProject = _context.get_rhpPrj();
				
		//_context.debug( "The thePkgName is " + thePkgName );
		
		IRPPackage theContextDiagramPkg = theOwningPkg.addNestedPackage( thePkgName );
		theContextDiagramPkg.changeTo( _context.REQTS_ANALYSIS_CONTEXT_DIAGRAM_PACKAGE );
		_context.setSavedInSeparateDirectoryIfAppropriateFor( theContextDiagramPkg );
		
		@SuppressWarnings("unused")
		CreateRequirementsPkg theCreateRequirementsPkg = new CreateRequirementsPkg( 
				theReqtsPkgChoice, 
				theContextDiagramPkg, 
				theReqtsPkgOptionalName, 
				theExistingReqtsPkgIfChosen,
				_context );
		
		CreateActorPkg theActorPkgCreator = new CreateActorPkg( _context );
		
		if( theActorPkgChoice == CreateActorPkgOption.CreateNew ){
			
			theActorPkgCreator.createNew( theProject, theActorPkgName, theContextDiagramPkg );
		
		} else if( theActorPkgChoice == CreateActorPkgOption.CreateNewButEmpty ){
			
			theActorPkgCreator.createNewButEmpty( theProject, theActorPkgName, theContextDiagramPkg );
			
		} else if( theActorPkgChoice == CreateActorPkgOption.InstantiateFromExisting ){
			
			theActorPkgCreator.instantiateFromExisting( 
					theContextDiagramPkg, 
					theActorPkgName + thePkgName, 
					theContextDiagramPkg, 
					theExistingActorPkg, 
					theActorPkgPrefixOption );
			
		} else if( theActorPkgChoice == CreateActorPkgOption.UseExisting ){
			
			theActorPkgCreator.useExisting( theExistingActorPkg );
		}
		
		@SuppressWarnings("unused")
		CreateExternalSignalsPkg theExternalSignalsPkg = new CreateExternalSignalsPkg( 
				theExternalSignalsPkgChoice,
				theProject,
				theExternalSignalsPkgOptionalName,
				theContextDiagramPkg,
				theExistingExternalSignalsPkgIfChosen,
				_context );
		
		List<IRPActor> theActors = theActorPkgCreator.getActors();
		List<IRPRelation> theActorUsages = new ArrayList<>();
		
		for( IRPActor theActor : theActors ){

			//_context.debug( "Adding usage for " + _context.elInfo( theActor ) + 
			//		" in " + _context.elInfo( theActor.getOwner() ) );
			
			IRPRelation theObject = theContextDiagramPkg.addImplicitObject( "" );
			theObject.setOtherClass( theActor );
			theObject.setStereotype( _context.getNewTermForActorUsage() );
			
			theActorUsages.add( theObject );
		}
		
		createContextDiagram( theActorUsages, theContextDiagramPackageName, theSystemBlock, theContextDiagramPkg );
		
		_context.deleteIfPresent( "Structure1", "StructureDiagram", theProject );
		_context.deleteIfPresent( "Model1", "ObjectModelDiagram", theProject );
		_context.deleteIfPresent( "Default", "Package", theProject );
		
		if( _context.getIsAutoPopulatePackageDiagram( theProject ) ){
			AutoPackageDiagram theAPD = new AutoPackageDiagram( _context );
			theAPD.drawDiagram();
		}
			    			
		theProject.save();
	}
	
	private void createContextDiagram(
			List<IRPRelation> theActorUsages, 
			String theName,
			IRPModelElement theSystemBlock,
			IRPPackage theOwningPkg ) {
		
		IRPCollection theCollection = _context.get_rhpApp().createNewCollection();

		IRPStructureDiagram theDiagram;
		
		IRPRelation theSystemContextEl;
		
		if( theSystemBlock instanceof IRPClassifier ){
			
			theDiagram = (IRPStructureDiagram) theOwningPkg.addNewAggr( 
					"StructureDiagram", _context.CONTEXT_DIAGRAM_PREFIX + theSystemBlock.getName() );
			
			theSystemContextEl = theOwningPkg.addImplicitObject( "" );
			theSystemContextEl.setOtherClass( (IRPClassifier) theSystemBlock );
		} else {
			
			theDiagram = (IRPStructureDiagram) theOwningPkg.addNewAggr( 
					"StructureDiagram", _context.CONTEXT_DIAGRAM_PREFIX + theName );
			
			theSystemContextEl = theOwningPkg.addImplicitObject( theName );
		}
		
		IRPStereotype theStereotype = _context.getNewTermForSystemContextDiagram();
		
		if( theStereotype != null ){
			
			//_context.debug( "Applying " + _context.elInfo( theStereotype ) + 
			//		" to " + _context.elInfo( theDiagram ) );
			
			theDiagram.setStereotype( theStereotype );
		}
		
		theSystemContextEl.setStereotype( _context.getNewTermForSystemContext() );
		
		String theDefaultSystemContextSize = theDiagram.getPropertyValue( 
				"Format." + _context.getNewTermForSystemContext().getName() + ".DefaultSize");
		
		String[] theSystemContextSizeSplit = theDefaultSystemContextSize.split(",");
		int theSystemContextWidth = Integer.parseInt( theSystemContextSizeSplit[2] );
		int theSystemContextHeight = Integer.parseInt( theSystemContextSizeSplit[3] );
		
		int x0 = 420;
		int y0 = 380;
		int r = 270;
		
		IRPGraphNode theSystemGraphNode = 
	    		theDiagram.addNewNodeForElement( 
	    				theSystemContextEl, 
	    				x0-(theSystemContextWidth/2), 
	    				y0-(theSystemContextHeight/2), 
	    				theSystemContextWidth, 
	    				theSystemContextHeight );

		theSystemGraphNode.setGraphicalProperty( "StructureView", "True" );
		
	    theCollection.addGraphicalItem( theSystemGraphNode );
		
		if( !theActorUsages.isEmpty() ){

			int items = theActorUsages.size();
			
			String theDefaultActorSize = theDiagram.getPropertyValue( 
					"Format." + _context.getNewTermForActorUsage().getName() + ".DefaultSize" );
			
			String[] theActorSplit = theDefaultActorSize.split(",");
			int actorWidth = Integer.parseInt( theActorSplit[2] );
			int actorHeight = Integer.parseInt( theActorSplit[3] );
		    
			for(int i = 0; i < items; i++) {

			    int x = (int) (x0 + r * Math.cos(2 * Math.PI * i / items));
			    int y = (int) (y0 + r * Math.sin(2 * Math.PI * i / items));   
			    
			    IRPGraphNode theActorGN = theDiagram.addNewNodeForElement( 
			    		theActorUsages.get(i), 
			    		x-(actorWidth/2), 
			    		y-(actorHeight/2), 
			    		actorWidth, 
			    		actorHeight );

			    theActorGN.setGraphicalProperty( "StructureView", "True" );
				
				theCollection.addGraphicalItem( theActorGN );
			}
						
			theDiagram.completeRelations(
					theCollection, 
					1);
		}
				
		theDiagram.highLightElement();
	}
}

/**
 * Copyright (C) 2021-2022  MBSE Training and Consulting Limited (www.executablembse.com)

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