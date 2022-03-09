package com.mbsetraining.sysmlhelper.common;

import java.util.ArrayList;
import java.util.List;

import com.telelogic.rhapsody.core.*;

public class LayoutHelper {

	BaseContext _context;
	
	public LayoutHelper(
			BaseContext context ) {
		_context = context;
	}
	
	private void centerAll( 
			List<IRPGraphEdge> theGraphEdges ){
				
		IRPDiagram theDiagram = theGraphEdges.get(0).getDiagram();
		
		for( IRPGraphEdge theEdgeToRedraw : theGraphEdges ){
			
			IRPGraphElement theSourceGraphEl = theEdgeToRedraw.getSource();
			IRPGraphElement theTargetGraphEl = theEdgeToRedraw.getTarget();
			
			IRPModelElement theModelObject = theEdgeToRedraw.getModelObject();
			
			if( theSourceGraphEl != null && 
				theTargetGraphEl != null ){
				
				IRPCollection theCollection = _context.createNewCollection();
				theCollection.addGraphicalItem( theEdgeToRedraw );
				theDiagram.removeGraphElements( theCollection );
				
				drawLineToMidPointsFor(
						theModelObject, 
						theSourceGraphEl, 
						theTargetGraphEl, 
						theDiagram );
			}
		}
	}
	
	private List<IRPGraphEdge> getAllStraightGraphEdges( 
			List<IRPGraphElement> inTheGraphEls ){
		
		List<IRPGraphEdge> theMatchingGraphEdges = new ArrayList<>();

		for( IRPGraphElement theGraphEl : inTheGraphEls ){
			
			IRPModelElement theModelEl = theGraphEl.getModelObject();

			if( theGraphEl instanceof IRPGraphEdge &&
				( theModelEl instanceof IRPDependency ||
						theModelEl instanceof IRPRelation ||
						theModelEl instanceof IRPGeneralization ) ){
				
				try {
					IRPGraphicalProperty theGraphProperty = theGraphEl.getGraphicalProperty( "LineStyle" );
					
					if( theGraphProperty != null && 
							theGraphProperty.getValue().equals( "Straight" ) ){
						
						theMatchingGraphEdges.add( (IRPGraphEdge) theGraphEl );
					}

				} catch( Exception e ){
					_context.error( "Exception in getAllStraightGraphEdges for " + 
							_context.elInfo( theGraphEl.getModelObject() ) ); 
				}
			}
		}
		
		return theMatchingGraphEdges;
	}
	
	public void centerStraightLinesForTheGraphEls( 
			List<IRPGraphElement> theGraphEls ){
		
		List<IRPGraphEdge> theEdgesToRedraw = 
				getAllStraightGraphEdges( theGraphEls );
		
		boolean answer = UserInterfaceHelper.askAQuestion( "There are " + 
				theEdgesToRedraw.size() + " lines selected.\n" +
				"Do you want to redraw them to the centers?");
		
		if( answer==true ){
			
			centerAll( theEdgesToRedraw );
		}
	}
	
	public void centerLinesForThePackage( 
			IRPPackage thePackage ){

		@SuppressWarnings("unchecked")
		List<IRPModelElement> theADs = 
				thePackage.getNestedElementsByMetaClass( 
						"ActivityDiagramGE", 1 ).toList();
	
		for( IRPModelElement theAD : theADs ){
			centerLinesForTheDiagram( (IRPDiagram) theAD );
		}
	}
	
	public void centerLinesForTheDiagram( 
			IRPDiagram theDiagram ){
				
		@SuppressWarnings("unchecked")
		List<IRPGraphElement> theGraphEls = 
				theDiagram.getGraphicalElements().toList();
		
		List<IRPGraphEdge> theEdgesToRedraw = 
				getAllStraightGraphEdges( theGraphEls );
		
		if( theEdgesToRedraw.size()== 0 ){
			
			UserInterfaceHelper.showInformationDialog(
					"There are no straight lines on the diagram" );
		
		} else {	
			
			String theDiagramName;
			
			if( theDiagram instanceof IRPActivityDiagram ){
				theDiagramName = _context.elInfo( theDiagram.getOwner() );
			} else {
				theDiagramName = _context.elInfo( theDiagram );
			}
			
			boolean answer = UserInterfaceHelper.askAQuestion( 
					"There are " + theEdgesToRedraw.size() + 
					" straight lines on the " + theDiagramName + ".\n" +
					"Do you want to redraw them to the centers?" );
			
			if( answer==true ){
				centerAll( theEdgesToRedraw );
			}
		}
	}
	
	public void drawLineToMidPointsFor(
			IRPModelElement existingModelEl, 
			IRPGraphElement theStartGraphEl,
			IRPGraphElement theEndGraphEl, 
			IRPDiagram theDiagram ){
		
		if( theStartGraphEl instanceof IRPGraphNode && 
			theEndGraphEl instanceof IRPGraphNode ){

			IRPGraphNode theStartNode = (IRPGraphNode)theStartGraphEl;
			IRPGraphNode theEndNode = (IRPGraphNode)theEndGraphEl;

			GraphElInfo theStartNodeInfo = new GraphElInfo( theStartNode, _context );
			GraphElInfo theEndNodeInfo = new GraphElInfo( theEndNode, _context );
			
			theDiagram.addNewEdgeForElement(
					existingModelEl, 
					theStartNode, 
					theStartNodeInfo.getMidX(), 
					theStartNodeInfo.getMidY(), 
					theEndNode, 
					theEndNodeInfo.getMidX(), 
					theEndNodeInfo.getMidY());

		} else if( theStartGraphEl instanceof IRPGraphEdge || 
				   theEndGraphEl instanceof IRPGraphEdge ){

			IRPCollection theGraphEls = 
					_context.get_rhpApp().createNewCollection();

			theGraphEls.addGraphicalItem( theStartGraphEl );
			theGraphEls.addGraphicalItem( theEndGraphEl );

			theDiagram.completeRelations( theGraphEls, 0);	

		} else {
			_context.warning("Warning in drawLineToMidPointsFor, the graphEls are not handled types for drawing relations");
		}
	}
}

/**
 * Copyright (C) 2017-2022  MBSE Training and Consulting Limited (www.executablembse.com)

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