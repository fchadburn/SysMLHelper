package com.mbsetraining.sysmlhelper.common;

import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.Map.Entry;

import com.telelogic.rhapsody.core.*;

public class MergeActors {

	ConfigurationSettings _context;
	
	public static void main(String[] args) {
		

		String theAppID = RhapsodyAppServer.getActiveRhapsodyApplication().getApplicationConnectionString();
			
		ConfigurationSettings context = new ConfigurationSettings
				(theAppID, 
				"ExecutableMBSEProfile.General.EnableErrorLogging", 
				"ExecutableMBSEProfile.General.EnableWarningLogging",
				"ExecutableMBSEProfile.General.EnableInfoLogging", 
				"ExecutableMBSEProfile.General.EnableDebugLogging",
				"ExecutableMBSEProfile.General.PluginVersion",
				"ExecutableMBSEProfile.General.UserDefinedMetaClassesAsSeparateUnit",
				"ExecutableMBSEProfile.General.AllowPluginToControlUnitGranularity",
				"ExecutableMBSE.properties", 
				"ExecutableMBSE_MessagesBundle",
				"ExecutableMBSE" );
		
		try {
					
			MergeActors theMerger = new MergeActors(context);
			
			IRPModelElement theSelectedEl = context.getSelectedElement( false );
			
			if( theSelectedEl instanceof IRPPackage ){
				theMerger.mergeActorsInto( (IRPPackage) theSelectedEl );
			}
			
		} catch (Exception e) {
			context.debug("Exception in main");
		}
	}
	
	public MergeActors(
			ConfigurationSettings context ) {
		
		_context = context;
	}
	
	private IRPRelation getExistingAssociationEnd( 
			IRPClassifier ownedByElement, 
			IRPClassifier toTheElement ){
		
		IRPRelation theExistingAssociationEnd = null;
		
        @SuppressWarnings("unchecked")
		List<IRPModelElement> theRoleEls = ownedByElement.getRelations().toList();
        
        for( IRPModelElement theRoleEl : theRoleEls ){
        
     	   if( theRoleEl.getMetaClass().equals( "AssociationEnd" ) ){
     		
     		   IRPRelation theAssocRole = (IRPRelation) theRoleEl;
     		   
     		   if( !theAssocRole.getOfClass().equals( ownedByElement ) ){
     			   
     			   _context.debug("Error in getExistingAssociationEnd, expected ofClass to be " + 
     					   _context.elInfo( ownedByElement ) + " not " + 
     					   _context.elInfo( theAssocRole.getOfClass() ) );
     		   } else {
     			   
     			   IRPClassifier theOtherClass = theAssocRole.getOtherClass();
     			   
     			   if( theOtherClass.equals( toTheElement ) ){
     				  theExistingAssociationEnd = theAssocRole;
     				  break;
     			   }
     		   }         	   
     	   }
        }
        
		return theExistingAssociationEnd;
	}

	public void mergeActorsInto( 
			IRPPackage thePackage ){

		IRPProject theProject = thePackage.getProject();

		@SuppressWarnings("unchecked")
		List<IRPActor> theActorsToMerge = theProject.getNestedElementsByMetaClass(
				"Actor", 1).toList();

		@SuppressWarnings("unchecked")
		List<IRPActor> theActorsAlreadyInPkg = thePackage.getNestedElementsByMetaClass(
				"Actor", 0).toList();

		for( IRPActor theActorAlreadyInPkg : theActorsAlreadyInPkg ){
			theActorsToMerge.remove( theActorAlreadyInPkg );
		}

		Map<String, Set<IRPActor>> theActorNameToExistingActorsToMergeMap = 
				getActorNameToExistingActorsToMergeMap( 
						theActorsToMerge );

		Map<IRPActor, IRPActor> theOldToNewActorMap = 
				getOldToNewActorMapBasedOn( 
						thePackage, theActorNameToExistingActorsToMergeMap );

		Map<IRPRelation, AssociationInfo> theOldToNewAssocMap = 
				getTheOldToNewAssocMapBasedOn( 
						theOldToNewActorMap );
		
		Map<IRPGraphNode, IRPGraphNode> theOldToNewGraphNode = 
				getOldToNewGraphNodeMap(
						theOldToNewAssocMap );
				
		performMerge( theOldToNewAssocMap, theOldToNewGraphNode );
		
		for( Entry<IRPActor, IRPActor> entry : theOldToNewActorMap.entrySet() ){
			
			IRPActor theOldActor = entry.getKey();
			theOldActor.deleteFromProject();
		}

		theOldToNewAssocMap.size();
	}

	private void performMerge(
			Map<IRPRelation, AssociationInfo> theOldToNewAssocMap,
			Map<IRPGraphNode, IRPGraphNode> theOldToNewGraphNode ){
		
		for( Entry<IRPRelation, AssociationInfo> entry : theOldToNewAssocMap.entrySet() ){

			@SuppressWarnings("unused")
			IRPRelation theOldRelation = entry.getKey();
			AssociationInfo theAssocInfo = entry.getValue();
			
			IRPRelation theNewRelation = theAssocInfo.getM_NewRelation();
			Set<IRPGraphEdge> theGraphEdges = theAssocInfo.getM_GraphEdges();
			
			for( IRPGraphEdge theGraphEdge : theGraphEdges ){
				
				GraphEdgeInfo theEdgeInfo = new GraphEdgeInfo( theGraphEdge, _context );
				
				IRPGraphNode theSrcGraphNode = (IRPGraphNode)theGraphEdge.getSource();
				IRPGraphNode theTgtGraphNode = (IRPGraphNode)theGraphEdge.getTarget();
				
				IRPModelElement theSrcEl = theSrcGraphNode.getModelObject();
				IRPModelElement theTgtEl = theTgtGraphNode.getModelObject();

				_context.debug( 
						"start x=" + theEdgeInfo.getEndX() + ", y=" + theEdgeInfo.getEndY() + 
						" is a " + _context.elInfo( theSrcEl ) +
						" end x=" + theEdgeInfo.getEndX() + ", y=" +theEdgeInfo.getEndY() +
						" is a " + _context.elInfo( theTgtEl ) );
									
				GraphNodeInfo theSrcNodeInfo = new GraphNodeInfo( theSrcGraphNode, _context );
				GraphNodeInfo theTgtNodeInfo = new GraphNodeInfo( theTgtGraphNode, _context );

				_context.debug("There is a source " + _context.elInfo( theSrcEl ) + 
						" at x=" + theSrcNodeInfo.getTopLeftX() + ", y=" + theSrcNodeInfo.getTopLeftY() );
				
				_context.debug("There is a target " + _context.elInfo( theTgtEl ) + 
						" at x=" + theTgtNodeInfo.getTopLeftX() + ", y=" + theTgtNodeInfo.getTopLeftY() );

				IRPDiagram theDiagram = theGraphEdge.getDiagram();
				
				IRPGraphNode theNewNode = theOldToNewGraphNode.get( theSrcGraphNode );
				
				_context.debug("Adding " + _context.elInfo( theNewRelation ) + " to " + _context.elInfo( theDiagram ) ); 
				
				@SuppressWarnings("unused")
				IRPGraphEdge theNewEdge = theDiagram.addNewEdgeForElement(
						theNewRelation, 
						theNewNode, 
						theEdgeInfo.getStartX(), 
						theEdgeInfo.getStartY(), 
						(IRPGraphNode) theGraphEdge.getTarget(), 
						theEdgeInfo.getEndX(), 
						theEdgeInfo.getEndY() );
			}
		}
	}

	private Map<IRPGraphNode, IRPGraphNode> getOldToNewGraphNodeMap(
			Map<IRPRelation, AssociationInfo> theOldToNewAssocMap) {
		
		Map<IRPGraphNode, IRPGraphNode> theOldToNewGraphNode = new HashMap<>();
		
		for( Entry<IRPRelation, AssociationInfo> entry : theOldToNewAssocMap.entrySet() ){
		
			AssociationInfo theAssocInfo = entry.getValue();
			
			Set<IRPGraphEdge> theGraphEdges = theAssocInfo.getM_GraphEdges();

			for( IRPGraphEdge theGraphEdge : theGraphEdges ){
				
				IRPDiagram theDiagram = theGraphEdge.getDiagram();
				
				theDiagram.openDiagram();
				
				IRPGraphElement theSourceGraphElement = theGraphEdge.getSource();
				IRPModelElement theSourceModelObject = theSourceGraphElement.getModelObject();
				
				IRPGraphElement theTargetGraphElement = theGraphEdge.getTarget();
				@SuppressWarnings("unused")
				IRPModelElement theTargetModelObject = theTargetGraphElement.getModelObject();

				IRPClassifier theNewActor = theAssocInfo.getM_NewActor();
				
				if( theSourceGraphElement instanceof IRPGraphNode &&
					theSourceModelObject != null && 
					theSourceModelObject instanceof IRPActor ){
					
					if( !theOldToNewGraphNode.containsKey( theSourceGraphElement ) ){
						
						IRPGraphNode theOldGraphNode = (IRPGraphNode) theSourceGraphElement;
						
						GraphNodeInfo theNodeInfo = new GraphNodeInfo( 
								theOldGraphNode, _context );

						_context.debug("There is a source " + _context.elInfo( theSourceModelObject ) + 
								" at x=" + theNodeInfo.getTopLeftX() + ", y=" + theNodeInfo.getTopLeftY() +
								" which will be replaced with a graph node for " + theNewActor.getFullPathName() );
						
						IRPGraphNode theNewGraphNode = theDiagram.addNewNodeForElement(
								theNewActor, 
								theNodeInfo.getTopLeftX(), 
								theNodeInfo.getTopLeftY(), 
								theNodeInfo.getWidth(), 
								theNodeInfo.getHeight() );
						
						theOldToNewGraphNode.put( theOldGraphNode, theNewGraphNode );
					}
				}
			}
		}
		return theOldToNewGraphNode;
	}

	private Map<IRPRelation, AssociationInfo> getTheOldToNewAssocMapBasedOn(
			Map<IRPActor, IRPActor> theOldToNewActorMap ){
		
		Map<IRPRelation, AssociationInfo> theOldToNewAssocMap = new HashMap<>();
		
		for( Entry<IRPActor, IRPActor> entry : theOldToNewActorMap.entrySet() ){
		
			IRPActor theOldActor = entry.getKey();
			IRPActor theNewActor = entry.getValue();
			
			@SuppressWarnings("unchecked")
			List<IRPModelElement> theRelations = theOldActor.getRelations().toList();

			for( IRPModelElement theRelationEl : theRelations ){

				if( theRelationEl.getMetaClass().equals( "AssociationEnd" ) ){
					
					IRPRelation theOldRelation = (IRPRelation)theRelationEl;
					
					IRPClassifier theOfClass = theOldRelation.getOfClass();
					IRPClassifier theOtherClass = theOldRelation.getOtherClass();

					if( theOfClass.equals( theNewActor ) ){
						
						_context.debug("Error, Ignoring " + _context.elInfo( theOldRelation ) + 
								" as it involves the  " + theNewActor.getFullPathName() + " as theOfClass ");
						
					} else if( theOtherClass.equals( theNewActor )){

						_context.debug("Error, Ignoring " + _context.elInfo( theOldRelation ) + 
								" as it involves the  " + theNewActor.getFullPathName() + " as theOtherClass ");

					} else {
						
						IRPRelation theNewRelation = 
								getExistingOrCreateNewAssocationEndTo(
										theNewActor, (IRPRelation) theOldRelation );
						
						AssociationInfo theAssociationInfo = 
								new AssociationInfo( theOldRelation, theNewRelation, _context );
						
						theOldToNewAssocMap.put( 
								theOldRelation, theAssociationInfo );
					}
				}
			}
		}
		
		return theOldToNewAssocMap;
	}

	private IRPRelation getExistingOrCreateNewAssocationEndTo(
			IRPClassifier theNewActor, 
			IRPRelation basedOnOldRelation ){
		
		_context.debug("getExistingOrCreateNewAssocationEndTo was invoked for actor '" + theNewActor.getFullPathName() + 
				"' based on " + _context.elInfo( basedOnOldRelation ) + " owned by " + 
				basedOnOldRelation.getOwner().getFullPathName() );

		IRPRelation theNewAssociationEnd = null;

		IRPClassifier theOfClass = basedOnOldRelation.getOfClass();
		
		if( !theOfClass.getName().equals( theNewActor.getName() ) ){
			
			_context.debug( "Error in getExistingOrCreateNewAssocationEndTo, " + theOfClass.getName() + " does not equal " + theNewActor.getName() );
		
		} else {
			IRPClassifier theOtherClass = basedOnOldRelation.getOtherClass();

			// does it already exist?
			theNewAssociationEnd = 
					getExistingAssociationEnd( theNewActor, theOtherClass );

			if( theNewAssociationEnd == null ){

				theNewAssociationEnd = theNewActor.addRelationTo(
						theOtherClass, 
						"", 
						"Association", 
						"1", 
						"", 
						"Association", 
						"1", 
						"");
			}
		}
		
		return theNewAssociationEnd;
	}

	private Map<IRPActor, IRPActor> getOldToNewActorMapBasedOn(
			IRPPackage thePackage,
			Map<String, Set<IRPActor>> theActorNameMap ){
		
		Map<IRPActor, IRPActor> theOldToNewActorMap = new HashMap<>();

		for( Entry<String, Set<IRPActor>> entry : theActorNameMap.entrySet() ){
        	           
           String theActorName = entry.getKey();
           Set<IRPActor> theExistingActors = entry.getValue();
           
           _context.debug("Actors with name '" + theActorName + "' are:");
           
           IRPActor theNewActor = thePackage.findActor( theActorName );
           
           if( theNewActor == null ){
        	   theNewActor = thePackage.addActor( theActorName );
           }
           
           for( IRPActor theCurrentActor : theExistingActors ){
        	   theOldToNewActorMap.put( theCurrentActor, theNewActor );
           }
		}
		return theOldToNewActorMap;
	}

	private Map<String, Set<IRPActor>> getActorNameToExistingActorsToMergeMap(
			List<IRPActor> theCandidateActorsToMerge ){
		
		Map<String, Set<IRPActor>> theActorNameMap = new HashMap<>();
		
		for( IRPActor theActor : theCandidateActorsToMerge ){
			
			 Set<IRPActor> theCurrentList = theActorNameMap.get( theActor.getName() );
			 
			 if( theCurrentList == null ){
				 theCurrentList = new HashSet<IRPActor>();
			 }
			 
			 theCurrentList.add( theActor ); 
			 theActorNameMap.put( theActor.getName(), theCurrentList );
		}
		
		return theActorNameMap;
	}
}

/**
 * Copyright (C) 2017-2021  MBSE Training and Consulting Limited (www.executablembse.com)

    Change history:
    #212 04-JUL-2017: Added a MergeActors helper, currently only invoked via Eclipse (F.J.Chadburn) 

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