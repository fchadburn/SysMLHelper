package com.mbsetraining.sysmlhelper.contextdiagram;

import java.util.ArrayList;
import java.util.List;

import com.mbsetraining.sysmlhelper.executablembse.ExecutableMBSE_Context;
import com.telelogic.rhapsody.core.IRPActor;
import com.telelogic.rhapsody.core.IRPCollection;
import com.telelogic.rhapsody.core.IRPGraphNode;
import com.telelogic.rhapsody.core.IRPPackage;
import com.telelogic.rhapsody.core.IRPRelation;
import com.telelogic.rhapsody.core.IRPStereotype;
import com.telelogic.rhapsody.core.IRPStructureDiagram;

public class ContextDiagramCreator {
	
	protected ExecutableMBSE_Context _context;

	public static void main(String[] args) {
		// TODO Auto-generated method stub

	}
	
	public ContextDiagramCreator(
			ExecutableMBSE_Context context ){
		
		_context = context;
	}
	
	public IRPStructureDiagram createContextDiagram(
			IRPPackage underOwningPkg,
			List<IRPActor> theActors, 
			String theContextElementName,
			IRPRelation theSystemContextEl ){
		
		List<IRPRelation> theActorUsages = new ArrayList<>();
		
		for( IRPActor theActor : theActors ){

			//_context.debug( "Adding usage for " + _context.elInfo( theActor ) + 
			//		" in " + _context.elInfo( theActor.getOwner() ) );
			
			IRPRelation theObject = underOwningPkg.addImplicitObject( "" );
			theObject.setOtherClass( theActor );
			theObject.setStereotype( _context.getNewTermForActorUsage() );
			
			theActorUsages.add( theObject );
		}
		
		IRPCollection theCollection = _context.get_rhpApp().createNewCollection();

		IRPStructureDiagram theDiagram = (IRPStructureDiagram) underOwningPkg.addNewAggr( 
					"StructureDiagram", _context.CONTEXT_DIAGRAM_PREFIX + theContextElementName );

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
						
		return theDiagram;
	}
	
	public IRPPackage createContextPackage(
			IRPPackage underThePkg,
			String basedOnName ){
		
		String theContextPkgName = _context.determineUniqueNameBasedOn( 
				basedOnName.replace(" ", "") + "_ContextPkg", "Package", underThePkg );
								
		//_context.debug( "The thePkgName is " + thePkgName );
		
		IRPPackage theContextPkg = underThePkg.addNestedPackage( theContextPkgName );
		theContextPkg.changeTo( _context.REQTS_ANALYSIS_CONTEXT_DIAGRAM_PACKAGE );
		_context.setSavedInSeparateDirectoryIfAppropriateFor( theContextPkg );
		
		return theContextPkg;
	}
}

/**
 * Copyright (C) 2023  MBSE Training and Consulting Limited (www.executablembse.com)

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
