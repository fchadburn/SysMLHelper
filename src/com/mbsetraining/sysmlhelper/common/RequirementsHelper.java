package com.mbsetraining.sysmlhelper.common;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import com.telelogic.rhapsody.core.*;
   
public class RequirementsHelper {
 	
	protected ConfigurationSettings _context;
	
	public RequirementsHelper(
			ConfigurationSettings context ){
		
		_context = context;
	}
	
	public void createNewRequirementsFor(
			List<IRPGraphElement> theGraphEls ){
		
		for( IRPGraphElement theGraphEl : theGraphEls ){
			createNewRequirementFor( theGraphEl );
		}
	}
	
	private void createNewRequirementFor(
			IRPGraphElement theGraphEl ){
		
		IRPModelElement theModelObject = theGraphEl.getModelObject();
		IRPDiagram theDiagram = theGraphEl.getDiagram();
		
		if( theModelObject != null ){
			
			String theActionText = _context.getActionTextFrom( theModelObject );
			
			if( theActionText != null ){
				List<IRPModelElement> theRelations = getElementsThatFlowInto( theModelObject, theDiagram );

				String theText = null;
				
				if( theRelations.isEmpty() ){
						
					String preFix = _context.getCreateRequirementTextForPrefixing( 
							theModelObject,
							"ExecutableMBSEProfile.RequirementsAnalysis.CreateRequirementTextForPrefixing" );
					
					theText = preFix + theActionText;				
					
				} else {
					
					theText = "When ";
					
					Iterator<IRPModelElement> theRelatedModelElIter = theRelations.iterator();
					
					while( theRelatedModelElIter.hasNext() ) {
						
						IRPModelElement theRelatedModelEl = theRelatedModelElIter.next();
						
						if( theRelatedModelEl instanceof IRPTransition ){
							IRPTransition theTransition = (IRPTransition)theRelatedModelEl;
							String theGuardBody = theTransition.getItsGuard().getBody();
							
							theText+= theGuardBody;
						
						} else if( theRelatedModelEl instanceof IRPAcceptEventAction ){
							
							theText+= _context.decapitalize( 
								_context.getActionTextFrom( theRelatedModelEl ) );
						}
						
						if( theRelatedModelElIter.hasNext() ){
							theText+= " or ";
						}		
					}
					
					theText += " the feature shall " + theActionText;
				}
				
				IRPModelElement theReqtOwner = theDiagram;

				if( theReqtOwner instanceof IRPActivityDiagram ){
					theReqtOwner = theDiagram.getOwner();
				}

				IRPDependency theDependency = 
						addNewRequirementTracedTo( theModelObject, theReqtOwner, theText );	

				IRPRequirement theReqt = (IRPRequirement) theDependency.getDependsOn();

				GraphElInfo theInfo = new GraphElInfo(theGraphEl, _context);
				int x = theInfo.getMidX();
				int y = theInfo.getMidY();

				IRPGraphNode theGraphNode = theDiagram.addNewNodeForElement(
						theReqt, x+100, y+70, 300, 100 );

				if( theGraphEl instanceof IRPGraphNode ){

					IRPGraphNode theStartNode = (IRPGraphNode)theGraphEl;

					GraphElInfo theGraphNodeInfo = new GraphElInfo(theGraphNode, _context);
					
					theDiagram.addNewEdgeForElement(
							theDependency, 
							theStartNode, 
							x, 
							y, 
							theGraphNode, 
							theGraphNodeInfo.getMidX(), 
							theGraphNodeInfo.getMidY() );

				} else if( theGraphEl instanceof IRPGraphEdge ){

					IRPCollection theGraphEls = 
							_context.get_rhpApp().createNewCollection();

					theGraphEls.addGraphicalItem( theGraphEl );
					theGraphEls.addGraphicalItem( theGraphNode );

					theDiagram.completeRelations( theGraphEls, 0);	

				} else {
					_context.warning( "Warning in populateDependencyOnDiagram, " +
							"the graphEls are not handled types for drawing relations" );
				}
				
				_context.moveRequirementIfNeeded( theReqt );

			} // theActionText == null
		} else { // theModelObject == null
			_context.error( "theModelObject == null" );
		}
	}
	
	private List<IRPModelElement> getElementsThatFlowInto(
			IRPModelElement theElement, 
			IRPDiagram onTheDiagram){
		
		List<IRPModelElement> theElementsFound = new ArrayList<IRPModelElement>();
		 
		@SuppressWarnings("unchecked")
		List<IRPGraphElement> theGraphEls = onTheDiagram.getGraphicalElements().toList();
		
		for (IRPGraphElement irpGraphElement : theGraphEls) {
			
			if (irpGraphElement instanceof IRPGraphEdge){
				
				IRPModelElement theModelEl = (IRPModelElement) irpGraphElement.getModelObject();
				
				if (theModelEl instanceof IRPTransition){
					IRPTransition theTrans = (IRPTransition)theModelEl;
					IRPModelElement theTarget = theTrans.getItsTarget();
					
					if (theTarget != null && theTarget.getGUID().equals(theElement.getGUID())){
						
						IRPGuard theGuard = theTrans.getItsGuard();
						
						if (theGuard!=null){
							String theBody = theGuard.getBody();
							
							if (!theBody.isEmpty()){
								theElementsFound.add(theModelEl);
							} 
						} else { // theGuard==null
							
							//  does the transition come from an event?
							IRPModelElement theSource = theTrans.getItsSource();
							
							if( theSource instanceof IRPAcceptEventAction ){
								theElementsFound.add( theSource );
							}
						}
					}				
				}				
			}
		}
		
		return theElementsFound;	
	}


	private IRPDependency addNewRequirementTracedTo(
			IRPModelElement theModelObject, 
			IRPModelElement toOwner,
			String theText) {
		
		IRPRequirement theReqt = (IRPRequirement) toOwner.addNewAggr("Requirement", "");
		theReqt.setSpecification(theText);
		theReqt.highLightElement();	

		IRPDependency theDep = theModelObject.addDependencyTo( theReqt );

		IRPStereotype theDependencyStereotype = 
				_context.getStereotypeToUseForActions();
		
		if( theDependencyStereotype != null ){
			
			theDep.addSpecificStereotype( theDependencyStereotype );
		} else {
			theDep.addStereotype("derive", "Dependency");				
		}
		
		_context.info( "Created a Requirement called " + theReqt.getName() + 
				" with the text '" + theText + "' related to " + 
				_context.elInfo( theModelObject ) + " with a " + 
				_context.elInfo( theDep ) );
		
		return theDep;
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

