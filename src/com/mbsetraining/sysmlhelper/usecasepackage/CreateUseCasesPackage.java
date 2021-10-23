package com.mbsetraining.sysmlhelper.usecasepackage;

import java.util.List;

import com.mbsetraining.sysmlhelper.executablembse.AutoPackageDiagram;
import com.mbsetraining.sysmlhelper.executablembse.ExecutableMBSE_Context;
import com.mbsetraining.sysmlhelper.usecasepackage.CreateActorPkg.CreateActorPkgOption;
import com.telelogic.rhapsody.core.IRPActor;
import com.telelogic.rhapsody.core.IRPClassifier;
import com.telelogic.rhapsody.core.IRPCollection;
import com.telelogic.rhapsody.core.IRPGraphNode;
import com.telelogic.rhapsody.core.IRPPackage;
import com.telelogic.rhapsody.core.IRPProject;
import com.telelogic.rhapsody.core.IRPStereotype;
import com.telelogic.rhapsody.core.IRPUseCase;
import com.telelogic.rhapsody.core.IRPUseCaseDiagram;

public class CreateUseCasesPackage {

	protected ExecutableMBSE_Context _context;
	
	public CreateUseCasesPackage(
			String theUseCasesPackageName,
			IRPPackage theOwningPkg,
			CreateRequirementsPkg.CreateRequirementsPkgOption theReqtsPkgChoice,
			String theReqtsPkgOptionalName,
			IRPPackage theExistingReqtsPkgIfChosen,
			CreateActorPkg.CreateActorPkgOption theActorPkgChoice,
			String theActorPkgName,
			IRPPackage theExistingActorPkg,
			String theActorPkgPrefixOption,
			ExecutableMBSE_Context theContext ){
		
		_context = theContext;
		
		IRPProject theProject = _context.get_rhpPrj();
		
		String theAdornedName = theUseCasesPackageName + "Pkg";
		
		_context.debug( "The name is " + theAdornedName );
		
		IRPPackage theUseCasePkg = theOwningPkg.addNestedPackage( theAdornedName );
		theUseCasePkg.changeTo( _context.REQTS_ANALYSIS_USE_CASE_PACKAGE );
		_context.setSavedInSeparateDirectoryIfAppropriateFor( theUseCasePkg );
		
		@SuppressWarnings("unused")
		CreateRequirementsPkg theCreateRequirementsPkg = new CreateRequirementsPkg( 
				theReqtsPkgChoice, 
				theUseCasePkg, 
				theReqtsPkgOptionalName, 
				theExistingReqtsPkgIfChosen,
				_context );

		CreateActorPkg theActorPkgCreator = new CreateActorPkg( _context );
		
		if( theActorPkgChoice == CreateActorPkgOption.CreateNew ){
			
			theActorPkgCreator.createNew( theProject, theActorPkgName, theUseCasePkg );
		
		} else if( theActorPkgChoice == CreateActorPkgOption.CreateNewButEmpty ){
			
			theActorPkgCreator.createNewButEmpty( theProject, theActorPkgName, theUseCasePkg );
			
		} else if( theActorPkgChoice == CreateActorPkgOption.InstantiateFromExisting ){
			
			theActorPkgCreator.instantiateFromExisting( 
					theUseCasePkg, 
					theActorPkgName + theAdornedName, 
					theUseCasePkg, 
					theExistingActorPkg, 
					theActorPkgPrefixOption );
			
		} else if( theActorPkgChoice == CreateActorPkgOption.UseExisting ){
			
			theActorPkgCreator.useExisting( theExistingActorPkg );
		}
		
		List<IRPActor> theActors = theActorPkgCreator.getActors();

		createUseCaseDiagram( theActors, theAdornedName, theUseCasePkg );
		
		_context.deleteIfPresent( "Structure1", "StructureDiagram", theProject );
		_context.deleteIfPresent( "Model1", "ObjectModelDiagram", theProject );
		_context.deleteIfPresent( "Default", "Package", theProject );
		
		if( _context.getIsAutoPopulatePackageDiagram( theProject ) ){
			AutoPackageDiagram theAPD = new AutoPackageDiagram( _context );
			theAPD.drawDiagram();
		}
			    			
		theProject.save();
		
		_context.info( "Package structure construction of " + _context.elInfo( theUseCasePkg ) + " has completed");
	}
	
	private void createUseCaseDiagram(
			List<IRPActor> theActors, 
			String theName,
			IRPPackage theUseCasePkg ) {
		
		IRPUseCaseDiagram theUCD = 
				theUseCasePkg.addUseCaseDiagram( "UCD - " + theName );
		
		IRPStereotype theStereotype = _context.getNewTermForUseCaseDiagram();
		
		if( theStereotype != null ){
			
			_context.debug( "Applying " + _context.elInfo( theStereotype ) + 
					" to " + _context.elInfo( theUCD ) );
			
			theUCD.setStereotype( theStereotype );
		}
		
		IRPUseCase theUC = theUseCasePkg.addUseCase( "UC01 - " );
					
		IRPCollection theCollection = _context.get_rhpApp().createNewCollection();
		
		for( IRPActor theActor : theActors ){
			
			theActor.addRelationTo(
					(IRPClassifier) theUC, 
					"", 
					"Association", 
					"1", 
					"", 
					"Association", 
					"1", 
					"" );
		}
		
		IRPGraphNode theNote =
				theUCD.addNewNodeByType( "Note", 21, 42, 156, 845 );
		
		String theUseCaseNoteText = _context.getUseCaseNoteText( theUCD );
		
		theNote.setGraphicalProperty(
				"Text",
				theUseCaseNoteText );
		
		int x0 = 420;
		int y0 = 270;
		int r = 190;

		int items = theActors.size();
				
		String theDefaultActorSize = theUCD.getPropertyValue("Format.Actor.DefaultSize");
		String[] theActorSplit = theDefaultActorSize.split(",");
		int actorWidth = Integer.parseInt( theActorSplit[2] );
		int actorHeight = Integer.parseInt( theActorSplit[3] );
		
		String theDefaultUseCaseSize = theUCD.getPropertyValue("Format.UseCase.DefaultSize");
		String[] theUseCaseSplit = theDefaultUseCaseSize.split(",");
		int useCaseWidth = Integer.parseInt( theUseCaseSplit[2] );
		int useCaseHeight = Integer.parseInt( theUseCaseSplit[3] );
		
	    IRPGraphNode theUCGraphNode = 
	    		theUCD.addNewNodeForElement( theUC, x0-(useCaseWidth/2), y0-(useCaseHeight/2), useCaseWidth, useCaseHeight );

	    theCollection.addGraphicalItem( theUCGraphNode );
	    
		for(int i = 0; i < items; i++) {

		    int x = (int) (x0 + r * Math.cos(2 * Math.PI * i / items));
		    int y = (int) (y0 + r * Math.sin(2 * Math.PI * i / items));   
		    
		    IRPGraphNode theActorGN = theUCD.addNewNodeForElement( 
		    		theActors.get(i), 
		    		x-(actorWidth/2), 
		    		y-(actorHeight/2), 
		    		actorWidth, 
		    		actorHeight );
		    
		    theCollection.addGraphicalItem( theActorGN );
		}
					
		theUCD.completeRelations(
				theCollection, 
				1);
				
		theUCD.highLightElement();
	}
}

/**
 * Copyright (C) 2018-2021  MBSE Training and Consulting Limited (www.executablembse.com)

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