package com.mbsetraining.sysmlhelper.doorsng;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import com.mbsetraining.sysmlhelper.common.UserInterfaceHelper;
import com.mbsetraining.sysmlhelper.executablembse.ExecutableMBSE_Context;
import com.telelogic.rhapsody.core.*;

public class SwitchRhapsodyRequirementsToDNG {

	ExecutableMBSE_Context _context;

	
	public static void main(String[] args) {
		IRPApplication theRhpApp = RhapsodyAppServer.getActiveRhapsodyApplication();
		
		ExecutableMBSE_Context context = new ExecutableMBSE_Context( theRhpApp.getApplicationConnectionString() );
		
		SwitchRhapsodyRequirementsToDNG theSwitcher = new SwitchRhapsodyRequirementsToDNG(context);
		
		theSwitcher.switchRequirements();
	}

	public SwitchRhapsodyRequirementsToDNG(
			ExecutableMBSE_Context context ) {

		_context = context;
	}

	public void switchRequirements(){

		IRPModelElement theSelectedEl = _context.getSelectedElement( false );

		//Logger.debug( "theSelectedEl is " + Logger.elementInfo(theSelectedEl)  );

		Set<IRPRequirement> theRemoteReqts = getLinkedRemoteRequirements( theSelectedEl );

		if( theRemoteReqts.isEmpty() ){

			UserInterfaceHelper.showWarningDialog( "I was unable to find any remote requirements under " + 
					_context.elInfo( theSelectedEl ) + "\n" +
					"Did you log into the Remote Artefacts Package? \n"+ 
					"You also need to establish OSLC links from " + 
					_context.elInfo( theSelectedEl ) + " \n" +
					"to the remote requirements you want to switch to.");
		} else {
			_context.debug( "Found " + theRemoteReqts.size() + 
					" remote requirements related to " + 
					_context.elInfo( theSelectedEl ) );

			@SuppressWarnings("unchecked")
			List<IRPRequirement> theReqts = theSelectedEl.getNestedElementsByMetaClass("Requirement", 1).toList();

			_context.debug( "Found " + theReqts.size() + " Rhapsody-owned requirements under " + _context.elInfo( theSelectedEl ) );

			List<IRPModelElement> theProcessedReqts = new ArrayList<>();
			
			for (IRPRequirement theReqt : theReqts) {

				String theSpec = theReqt.getSpecification();

				Set<IRPRequirement> theMatches = 
						getRequirementsThatMatch(theSpec, theRemoteReqts);

				for (IRPRequirement theRemoteMatch : theMatches) {

					switchGraphElsFor( theReqt, theRemoteMatch );					
					theProcessedReqts.add( theReqt );
				}
			}

			@SuppressWarnings("unchecked")
			List<IRPModelElement> theRemoteDependencies = 
					theSelectedEl.getRemoteDependencies().toList();
			
			if( theProcessedReqts.size() > 0 ){
			
				boolean answer = UserInterfaceHelper.askAQuestion( 
						"Shall I delete the " + theProcessedReqts.size() + 
						" requirements related to the " + theRemoteDependencies.size() + " remote dependencies " +
						" that have been switched?" );
				
				if( answer ){
					deleteFromModel( theProcessedReqts );
					deleteFromModel( theRemoteDependencies );
				}
			}		
		}
	}

	public List<IRPRequirement> getRequirementsThatDontTraceToRemoteRequirements(
			IRPModelElement underTheEl ){
		
		List<IRPRequirement> theMatchingReqts = new ArrayList<>();
	
		@SuppressWarnings("unchecked")
		List<IRPRequirement> theReqts = underTheEl.getNestedElementsByMetaClass( "Requirement", 1 ).toList();

		for (IRPRequirement theReqt : theReqts) {
			
			@SuppressWarnings("unchecked")
			List<IRPDependency> theDependencies = theReqt.getRemoteDependencies().toList();
			
			if( theDependencies.size() > 2 ){
				_context.warning( _context.elInfo( theReqt ) + " has " + theDependencies.size() + 
						" remote dependencies when expecting 0 or 1" );
			} else if( theDependencies.size() == 1 ){
				
				IRPDependency theDependency = theDependencies.get( 0 );
				
				IRPModelElement theRemoteEl = theDependency.getDependent();
				
				_context.info( _context.elInfo( theReqt ) + " already has " + _context.elInfo( theDependency ) + 
						" to " + _context.elInfo( theRemoteEl ) );
				
				
			} else {
				_context.info( _context.elInfo( theReqt ) + " has no remote requirement dependencies" );
				theMatchingReqts.add( theReqt );
			}
		}
		
		return theMatchingReqts;
		
	}
	
	public void establishTraceRelationsToRemoteReqts(){

		IRPModelElement theSelectedEl = _context.getSelectedElement( false );

		//Logger.debug( "theSelectedEl is " + Logger.elementInfo(theSelectedEl)  );

		Set<IRPRequirement> theRemoteReqts = getLinkedRemoteRequirements( theSelectedEl );

		if( theRemoteReqts.isEmpty() ){

			UserInterfaceHelper.showWarningDialog( "I was unable to find any remote requirements under " + 
					_context.elInfo( theSelectedEl ) + "\n" +
					"Did you log into the Remote Artefacts Package? \n"+ 
					"You also need to establish OSLC links from " + 
					_context.elInfo( theSelectedEl ) + " \n" +
					"to the remote requirements you want to switch to.");
		} else {
			_context.debug( "Found " + theRemoteReqts.size() + 
					" remote requirements related to " + 
					_context.elInfo( theSelectedEl ) );

			List<IRPRequirement> theReqts = getRequirementsThatDontTraceToRemoteRequirements( theSelectedEl );

			_context.debug( "Found " + theReqts.size() + " Rhapsody-owned requirements under " + _context.elInfo( theSelectedEl ) );

			Map<IRPRequirement,List<IRPRequirement>> theDependencyMap = new HashMap<>();  
			
			for( IRPRequirement theReqt : theReqts ){
				String theSpec = theReqt.getSpecification();
				List<IRPRequirement> theMatchedReqts = new ArrayList<>(); 
				theMatchedReqts.addAll( getRequirementsThatMatch( theSpec, theRemoteReqts ) );

				if( !theMatchedReqts.isEmpty() ){					
					theDependencyMap.put(theReqt,  theMatchedReqts );			
				}
			}

			int size = theDependencyMap.keySet().size();

			if( size == 0 ){
				UserInterfaceHelper.showInformationDialog( "No matches were found." );
			} else {
				boolean answer = UserInterfaceHelper.askAQuestion( theDependencyMap.keySet().size() + " matches were found. Do you want to proceed?");

				if( answer ){
			
				    for (Map.Entry<IRPRequirement, List<IRPRequirement>> entry : theDependencyMap.entrySet()) {
				        IRPRequirement theReqt = entry.getKey();
				       List<IRPRequirement> theRemoteMatches = entry.getValue();
			
				       for (IRPRequirement theRemoteReqt : theRemoteMatches) {
				
							establishTraceRelationFrom( theReqt, theRemoteReqt );					
				}
			}		
		}
				
			}
			
	}
	}
	
	private void deleteFromModel(
			List<IRPModelElement> theEls ) {
		
		Iterator<IRPModelElement> i = theEls.iterator();

		while( i.hasNext() ){
			
			IRPModelElement theEl = (IRPModelElement) i.next();
			
			_context.debug( "Deleting " + _context.elInfo( theEl ) + 
					" owned by " + _context.elInfo( theEl.getOwner() ) );
			
			theEl.deleteFromProject();
		}
	}

	private IRPDependency createRemoteDependencyBasedOn(
			IRPRequirement toRemoteReqt,
			IRPDependency thePreviousDependency ){

		IRPDependency theRemoteDependency = null;

		String theUserDefinedMetaClass = thePreviousDependency.getUserDefinedMetaClass();
		
		//IRPModelElement theDependsOn = thePreviousDependency.getDependsOn();
		//_context.debug( "theDependsOn is " + _context.elInfo(theDependsOn));

		IRPModelElement theDependent = thePreviousDependency.getDependent();
		//_context.debug( "theDependent is " + _context.elInfo(theDependent));

		//linkType - one of the link types available with the requirement tool that you are using. 
		// For example, for Doors Next Generation, the possible types are "Derives From", "Refines", "Satisfies", and "Trace".

		if (theUserDefinedMetaClass.equals( "Satisfaction" ) ){

			theRemoteDependency = addRemoteDependency(
					toRemoteReqt, theDependent, "Satisfies" );

		} else if( theUserDefinedMetaClass.equals( "Derivation" ) ){

			theRemoteDependency = addRemoteDependency(
					toRemoteReqt, theDependent, "Derives From" );

		} else if( theUserDefinedMetaClass.equals( "Refinement" ) ){

			theRemoteDependency = addRemoteDependency(
					toRemoteReqt, theDependent, "Refines" );

		} else if( _context.hasStereotypeCalled( "trace", thePreviousDependency ) ){

			theRemoteDependency = addRemoteDependency(
					toRemoteReqt, theDependent, "Trace" );			
		} else {
			_context.debug( "Warning: No stereotype found on " + 
					_context.elInfo( thePreviousDependency ) + " to " + 
					_context.elInfo( toRemoteReqt ) );

			theRemoteDependency = addRemoteDependency(
					toRemoteReqt, theDependent, "Trace" );		
		}

		return theRemoteDependency;
	}

	private IRPDependency addRemoteDependency(
			IRPRequirement toRemoteReqt,
			IRPModelElement theDependent,
			String theType ){

		IRPDependency theRemoteDependency = null;

		try {
			theRemoteDependency = theDependent.addRemoteDependencyTo( 
					toRemoteReqt, theType );
			
			_context.debug( "Added remote " + theType + " from " + 
					_context.elInfo( theDependent ) + " to " + 
					_context.elInfo( toRemoteReqt ) );
			
		} catch( Exception e ){
			_context.debug( "Unable to add remote " + theType + " from " + 
					_context.elInfo( theDependent ) + " to " + 
					_context.elInfo( toRemoteReqt ) + ", e=" + e.getMessage() );
		}

		return theRemoteDependency;
	}

	private void establishTraceRelationFrom(
			IRPRequirement theReqt,
			IRPRequirement toRemoteReqt ) {

		//		_context.debug( "switchGraphElsFor from " + _context.elementInfo( theReqt ) + 
		//				" to " + _context.elementInfo ( toRemoteReqt ) );

		//List<IRPDependency> theExistingDependencies = _context.getDependenciesTo( toRemoteReqt );

		@SuppressWarnings("unchecked")
		List<IRPDependency> theExistingDependencies = theReqt.getDependencies().toList();
		
		List<IRPDependency> theMatchingDependencies = new ArrayList<>();
		List<IRPDependency> theUnmatchedDependencies = new ArrayList<>();
		
		for( IRPDependency theDependency : theExistingDependencies ){
			
			IRPModelElement theDependsOn = theDependency.getDependsOn();
			
			if( theDependsOn.equals( toRemoteReqt ) ) {
				theMatchingDependencies.add( theDependency );
			} else {
				theUnmatchedDependencies.add( theDependency );
			}
		}
		
		if( !theMatchingDependencies.isEmpty() ) {
			
			_context.info( theMatchingDependencies.toString() + " dependencies were found already from " + 
					_context.elInfo( theReqt ) + " to " + _context.elInfo( toRemoteReqt ) );
			
			for( IRPDependency theDependency : theMatchingDependencies ){
				_context.info( _context.elInfo( theDependency ) );
			}
		}

		addRemoteDependency( toRemoteReqt, theReqt, "Trace" );	
		
		_context.info( "Established OSLC trace relation from local " + _context.elInfo( theReqt ) + 
						" to remote " + _context.elInfo ( toRemoteReqt ) );
	}
	
	private void switchGraphElsFor(
			IRPRequirement theReqt,
			IRPRequirement toRemoteReqt ) {

		//		_context.debug( "switchGraphElsFor from " + _context.elementInfo( theReqt ) + 
		//				" to " + _context.elementInfo ( toRemoteReqt ) );

		List<IRPDependency> theExistingDependencies = _context.getDependenciesTo( theReqt );

		_context.debug( "Found " + theExistingDependencies.size() + 
				" dependencies to " + _context.elInfo( theReqt ) );

		Map<IRPDependency,IRPDependency> theDependencyMap = new HashMap<>();  
		List<IRPDependency> unmappedDependencies = new ArrayList<>();  

		for (IRPDependency theExistingDependency : theExistingDependencies) {

			IRPDependency theRemoteDependency = 
					createRemoteDependencyBasedOn( 
							toRemoteReqt, theExistingDependency );

			if( theRemoteDependency != null ){
				theDependencyMap.put( theExistingDependency, theRemoteDependency );				
			} else {
				unmappedDependencies.add( theExistingDependency );
			}
		}

		@SuppressWarnings("unchecked")
		List<IRPModelElement> theReferences = theReqt.getReferences().toList();

		for( IRPModelElement theReference : theReferences ){

			if( theReference instanceof IRPDiagram ){

				IRPDiagram theDiagram = (IRPDiagram)theReference;
				theDiagram.openDiagram();

				IRPCollection theGraphElsToRemove = _context.get_rhpApp().createNewCollection();

				@SuppressWarnings("unchecked")
				List<IRPGraphElement> theOriginalGraphEls = 
					theDiagram.getCorrespondingGraphicElements( theReqt ).toList();

				Iterator<IRPGraphElement> i = theOriginalGraphEls.iterator();

				while( i.hasNext() ){
					
					IRPGraphElement theOriginalGraphEl = (IRPGraphElement) i.next();

					IRPGraphNode theNewGraphNode = createGraphNodeFor(
							toRemoteReqt, 
							(IRPGraphNode) theOriginalGraphEl );

					List<IRPGraphEdge> theOriginalConnectedEdges = 
							getGraphEdgesConnectorTo( (IRPGraphNode) theOriginalGraphEl );

					_context.debug( "Found " + theOriginalConnectedEdges.size() + " related graph edges" );

					for (IRPGraphEdge theOriginalConnectedEdge : theOriginalConnectedEdges) {

						IRPModelElement theOldDependency = 
								theOriginalConnectedEdge.getModelObject();

						if( theOldDependency != null && 
								theOldDependency instanceof IRPDependency ){

							IRPDependency theNewDependency = theDependencyMap.get( theOldDependency );

							if( theNewDependency != null ){

								@SuppressWarnings("unused")
								IRPGraphEdge theNewGraphEdge = createGraphEdgeFor(
										(IRPDependency) theNewDependency, 
										(IRPGraphEdge) theOriginalConnectedEdge,
										theNewGraphNode );
								
								//theGraphElsToRemove.addGraphicalItem( theOriginalConnectedEdge );
							}
						}
					}
					
					theGraphElsToRemove.addGraphicalItem( theOriginalGraphEl );
				}

				//theDiagram.removeGraphElements( theGraphElsToRemove );
			}
		}
	}

	private List<IRPGraphEdge> getGraphEdgesConnectorTo(
			IRPGraphNode theGraphNode ){

		List<IRPGraphEdge> theGraphEdges = new ArrayList<>();

		@SuppressWarnings("unchecked")
		List<IRPGraphElement> theGraphEls =
		theGraphNode.getDiagram().getGraphicalElements().toList();

		for (IRPGraphElement theGraphEl : theGraphEls) {

			if( theGraphEl instanceof IRPGraphEdge ){
				IRPGraphEdge theGraphEdge = (IRPGraphEdge)theGraphEl;

				IRPGraphElement theTarget = theGraphEdge.getTarget();
				IRPGraphElement theSource = theGraphEdge.getSource();

				if( theTarget != null &&
						theGraphNode.equals( theTarget ) ){

					theGraphEdges.add( theGraphEdge );

				} else if( theSource != null &&
						theGraphNode.equals( theSource ) ){

					theGraphEdges.add( theGraphEdge );
				}
			}
		}

		return theGraphEdges;
	}

	private IRPGraphNode createGraphNodeFor(
			IRPModelElement toModelEl, 
			IRPGraphNode basedOnGraphNode ){

		IRPDiagram theDiagram = basedOnGraphNode.getDiagram();

		_context.debug( "Switching graph element on " + 
				_context.elInfo( theDiagram ) + 
				" to " + _context.elInfo( toModelEl ) );

		/*
		@SuppressWarnings("unchecked")
		List<IRPGraphicalProperty> theGraphProperties =
				basedOnGraphNode.getAllGraphicalProperties().toList();

		for( IRPGraphicalProperty theGraphProperty : theGraphProperties ){

			String theKey = theGraphProperty.getKey();
			String theValue = theGraphProperty.getValue();

			_context.debug( theKey + " = " + theValue );
		}*/

		String nHeight = basedOnGraphNode.getGraphicalProperty("Height").getValue();
		String nWidth = basedOnGraphNode.getGraphicalProperty("Width").getValue();
		String thePosition = basedOnGraphNode.getGraphicalProperty("Position").getValue();

		String split[] = thePosition.split(",");

		String xPosition = split[0];
		String yPosition = split[1];

		_context.debug( "theHeight = " + nHeight );
		_context.debug( "theWidth = " + nWidth );
		_context.debug( "xPos = " + xPosition );
		_context.debug( "yPos = " + yPosition );

		IRPGraphNode theNode = theDiagram.addNewNodeForElement(
				toModelEl, 
				Integer.parseInt(xPosition), 
				Integer.parseInt(yPosition), 
				Integer.parseInt(nWidth), 
				Integer.parseInt(nHeight) );

		toModelEl.highLightElement();

		return theNode;
	}

	private IRPGraphEdge createGraphEdgeFor(
			IRPDependency theNewDependency, 
			IRPGraphEdge basedOnGraphEdge,
			IRPGraphNode andRequirementGraphNode ){

		IRPGraphEdge theNewEdge = null;

		IRPDiagram theDiagram = basedOnGraphEdge.getDiagram();

		_context.debug( "Switching graph element on " + 
				_context.elInfo( theDiagram ) + 
				" to " + _context.elInfo( theNewDependency ) );

		/*
		@SuppressWarnings("unchecked")
		List<IRPGraphicalProperty> theGraphProperties =
				basedOnGraphEdge.getAllGraphicalProperties().toList();

		for( IRPGraphicalProperty theGraphProperty : theGraphProperties ){

			String theKey = theGraphProperty.getKey();
			String theValue = theGraphProperty.getValue();

			_context.debug( theKey + " = " + theValue );
		}*/

		String srcPosition = basedOnGraphEdge.getGraphicalProperty("SourcePosition").getValue();
		String trgPosition = basedOnGraphEdge.getGraphicalProperty("TargetPosition").getValue();

		String srcSplit[] = srcPosition.split(",");
		String trgSplit[] = trgPosition.split(",");

		String xSrcPosition = srcSplit[0];
		String ySrcPosition = srcSplit[1];

		String xTrgPosition = trgSplit[0];
		String yTrgPosition = trgSplit[1];

		_context.debug( "xSrcPosition = " + xSrcPosition );
		_context.debug( "ySrcPosition = " + ySrcPosition );
		_context.debug( "xTrgPosition = " + xTrgPosition );
		_context.debug( "yTrgPosition = " + yTrgPosition );

		IRPGraphElement theSourceGraphEl = basedOnGraphEdge.getSource();
		
		if( theSourceGraphEl instanceof IRPGraphNode ){

			theNewEdge = theDiagram.addNewEdgeForElement(
					theNewDependency, 
					(IRPGraphNode) basedOnGraphEdge.getSource(), 
					Integer.parseInt( xSrcPosition ), 
					Integer.parseInt( ySrcPosition ), 
					andRequirementGraphNode, 
					Integer.parseInt( xTrgPosition ), 
					Integer.parseInt( yTrgPosition ) );

		} else {
			IRPCollection theGraphElements = _context.get_rhpApp().createNewCollection();
			
			theGraphElements.addGraphicalItem(theSourceGraphEl);
			theGraphElements.addGraphicalItem(andRequirementGraphNode);
			
			IRPCollection theRelationsCollection = _context.get_rhpApp().createNewCollection();
			theRelationsCollection.setSize( 1 );
			theRelationsCollection.setString( 1, "AllRelations" );
			
			_context.debug("Attempting to populate relations to " + 
					_context.elInfo( theSourceGraphEl.getModelObject() ) );
			
			theDiagram.populateDiagram( theGraphElements, theRelationsCollection, "among" );
			
			@SuppressWarnings("unchecked")
			List<IRPGraphElement> theCorrespondingGraphEls =
					theDiagram.getCorrespondingGraphicElements(theNewDependency).toList();
			
			if( theCorrespondingGraphEls != null ){
				_context.debug( "there are " + theCorrespondingGraphEls.size() + " graph elements related to " +
						_context.elInfo( theNewDependency ) );
			}
		}
		
		return theNewEdge;
	}
	

	public Set<IRPRequirement> getLinkedRemoteRequirements(
			IRPModelElement underEl ){

		Set<IRPRequirement> theRequirements = new HashSet<>();

		@SuppressWarnings("unchecked")
		List<IRPDependency> theRemoteDependencies = underEl.getRemoteDependencies().toList();

			for (IRPDependency theRemoteDependency : theRemoteDependencies) {

				IRPModelElement theDependsOn = theRemoteDependency.getDependsOn();

				if( theDependsOn instanceof IRPRequirement ){
					theRequirements.add( (IRPRequirement) theDependsOn );
			}
		}

		return theRequirements;

	}

	private Set<IRPRequirement> getRequirementsThatMatch(
			String theSpecificationText,
			Set<IRPRequirement> theCandidates ){

		Set<IRPRequirement> theMatches = new HashSet<>();

		for (IRPRequirement theCandidate : theCandidates) {

			_context.debug( "theCandidate         = '" + theCandidate.getSpecification() + "'" );
			_context.debug( "theSpecificationText = '" + theSpecificationText + "'" );

			if( theCandidate.getSpecification().matches( theSpecificationText ) ){
				theMatches.add( theCandidate );
			}
		}

		return theMatches;
	}
}

/**
 * Copyright (C) 2020-2023  MBSE Training and Consulting Limited (www.executablembse.com)

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