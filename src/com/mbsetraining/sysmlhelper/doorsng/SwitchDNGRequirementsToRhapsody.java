package com.mbsetraining.sysmlhelper.doorsng;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import com.mbsetraining.sysmlhelper.common.UserInterfaceHelper;
import com.mbsetraining.sysmlhelper.executablembse.ExecutableMBSE_Context;
import com.telelogic.rhapsody.core.*;

public class SwitchDNGRequirementsToRhapsody {

	private ExecutableMBSE_Context _context;
	private RemoteRequirementAssessment _assessment;
	private Map<IRPDependency,IRPDependency> _oldToNewDependencyMap = new HashMap<>();  
	private Map<IRPDiagram, Set<IRPGraphNode>> _existingGraphNodesToDelete = new HashMap<>();

	// testing only
	public static void main(String[] args) {

		IRPApplication theRhpApp = RhapsodyAppServer.getActiveRhapsodyApplication();
		ExecutableMBSE_Context context = new ExecutableMBSE_Context( theRhpApp.getApplicationConnectionString() );
		SwitchDNGRequirementsToRhapsody theSwitcher = new SwitchDNGRequirementsToRhapsody(context);
		IRPModelElement theSelectedEl = context.getSelectedElement( false );

		if( theSelectedEl instanceof IRPPackage ) {

			IRPPackage theSelectedPkg = (IRPPackage) theSelectedEl;

			if( theSelectedPkg.getUserDefinedMetaClass().
					equals( context.REQTS_ANALYSIS_REQUIREMENT_PACKAGE ) ) {

				theSwitcher.switchRequirementsFor( theSelectedPkg );
			}
		}
	}

	public SwitchDNGRequirementsToRhapsody(
			ExecutableMBSE_Context context ) {

		_context = context;
	}

	public void switchRequirementsFor( 
			IRPPackage theRequirementsPkg ) {

		_assessment = new RemoteRequirementAssessment( _context );

		List<IRPModelElement> theEls = new ArrayList<>();
		theEls.add( theRequirementsPkg );

		_assessment.determineRequirementsToUpdate( theEls );

		boolean isContinue = true;

		int unloadedLinkCount = _assessment._requirementsWithUnloadedHyperlinks.size();
		int tracedReqtsCount = _assessment._requirementsThatTrace.size();

		if( unloadedLinkCount > 0 ) {

			if( tracedReqtsCount > 0 ){

				isContinue = UserInterfaceHelper.askAQuestion( 
						"There are " + unloadedLinkCount + " unloaded links under " + theRequirementsPkg.getName() + "\n" + 
								"You should make sure you've logged into the Remote Artefacts Package before proceeding \n\n" +
						"Do you want to proceed anyway?" );

				if( !isContinue ){
					_context.debug( "User chose to cancel." ); 
				}

			} else {

				String msg = "This helper works by looking for local requirements that have OSLC links to remote requirements. \n\n";

				msg += "I was unable to find any requirements under " + theRequirementsPkg.getName() + " needing a switch. \n";

				if( unloadedLinkCount == 1 ) {
					msg += "However, there is " + unloadedLinkCount + " unloaded link related to a requirement under " + theRequirementsPkg.getName() + ". \n\n";
				} else {
					msg += "However, there are " + unloadedLinkCount + " unloaded links related to requirements under " + theRequirementsPkg.getName() + ". \n\n";
				}

				msg += "It's suggested to make sure you've logged into the Remote Artefacts Package and then try again. \n";
				msg += "You should also establish and check the trace relations to remote requirements. \n";

				UserInterfaceHelper.showWarningDialog( msg );

				isContinue = false;				
			}

		} else if( tracedReqtsCount == 0 ){

			UserInterfaceHelper.showWarningDialog( "I was unable to find any requirements under " + 
					theRequirementsPkg.getName() + " needing a switch. \n\n" +
					"Did you run the establish trace relations command first "+ 
					"to establish OSLC links to the remote requirements you want to switch to? ");

			isContinue = false;				
		}

		if( isContinue ){

			_context.debug( "Found " + _assessment._remoteRequirementsThatTrace.size() + 
					" remote requirements to switch under " + 
					_context.elInfo( theRequirementsPkg ) );

			String msg = "";

			if( tracedReqtsCount == 1 ) {
				msg += "There is " + tracedReqtsCount + " local requirement under " + theRequirementsPkg.getName() + " with an OSLC link to a remote requirement. \n\n";
				msg += "Do you want to use the local surrogate requirement on diagrams rather than remote counterpart? \n";
				msg += "This will update all the diagrams it's on. \n";
				msg += "After the switch you'll be given a choice whether to remove the remote requirement(s) links.";

			} else {
				msg += "There are " + tracedReqtsCount + " local requirements under " + theRequirementsPkg.getName() + " with OSLC links to remote requirements. \n\n";
				msg += "Do you want to use the local surrogate requirements on diagrams rather than their remote counterparts? \n";
				msg += "This will update all the diagrams they're on. \n";
				msg += "After the switch you'll be given a choice whether to remove the remote requirement(s) links.";
			}

			isContinue = UserInterfaceHelper.askAQuestion( msg );

			if( !isContinue ){
				_context.debug( "User chose to cancel." ); 				
			}

		}

		if( isContinue ){
			switchGraphElsForRequirementsThatTrace( theRequirementsPkg );	
		}
	}

	private void switchGraphElsForRequirementsThatTrace(
			IRPPackage theRequirementsPkg ){

		List<IRPModelElement> theProcessedReqts = new ArrayList<>();

		for( Entry<IRPRequirement, IRPRequirement> entry : _assessment._requirementsThatTrace.entrySet() ){

			IRPRequirement theLocalReqt = entry.getKey();
			IRPRequirement theRemoteReqt = entry.getValue();

			switchGraphElsFor( theRemoteReqt, theLocalReqt );					
			theProcessedReqts.add( theRemoteReqt );
		}

		if( theProcessedReqts.size() > 0 ){

			Set<IRPDependency> theRemoteDependencies = _oldToNewDependencyMap.keySet();

			boolean answer = UserInterfaceHelper.askAQuestion( 
					"Shall I delete the links to the " + theProcessedReqts.size() + 
					" remote requirements that have been switched? \n\n"  + 
					"This will also remove " + _existingGraphNodesToDelete.size() + " elements on diagrams " );

			if( answer ){
				
				_context.deleteAllFromModel( new ArrayList<>( theRemoteDependencies ) );

				removeGraphNodesFromDiagrams();
			}
		}
	}

	private void removeGraphNodesFromDiagrams() {
		
		for( Map.Entry<IRPDiagram, Set<IRPGraphNode>> entry : _existingGraphNodesToDelete.entrySet()){ 
			
			IRPCollection collection = _context.createNewCollection();

			IRPDiagram theDiagram = entry.getKey();
			Set<IRPGraphNode> theGraphNodes = entry.getValue();
				
			for (IRPGraphNode theGraphNode : theGraphNodes ) {
				collection.addGraphicalItem( theGraphNode );
			}
		
			theDiagram.removeGraphElements(collection);
		}
	}

	private IRPDependency createLocalDependencyBasedOn(
			IRPRequirement toLocalReqt,
			IRPDependency thePreviousDependency ){

		IRPDependency theLocalDependency = null;

		//_context.info( "createRemoteDependencyBasedOn invoked for " + _context.elInfo (thePreviousDependency) + 
		//		" with stereotype " + _context.elInfo( thePreviousDependency.getStereotype() ) );

		String theUserDefinedMetaClass = thePreviousDependency.getUserDefinedMetaClass();

		IRPModelElement theDependsOn = thePreviousDependency.getDependsOn();
		//_context.debug( "theDependsOn is " + _context.elInfo(theDependsOn));

		IRPModelElement theDependent = thePreviousDependency.getDependent();
		//_context.debug( "theDependent is  " + _context.elInfo(theDependent));

		//linkType - one of the link types available with the requirement tool that you are using. 
		// For example, for Doors Next Generation, the possible types are "Derives From", "Refines", "Satisfies", and "Trace".

		if( theDependent instanceof IRPPackage && 
				theDependent.getUserDefinedMetaClass().
				equals( _context.REQTS_ANALYSIS_REQUIREMENT_PACKAGE ) ){

			_context.debug( "Skipping " + _context.elInfo( thePreviousDependency ) + " from " + _context.elInfo( theDependent ) );

		} else if( theDependsOn instanceof IRPRequirement ){
			
			if( theDependent.equals( toLocalReqt ) ){
				_context.debug( "Skipping " + _context.elInfo( thePreviousDependency ) + " from " + _context.elInfo( theDependent ) );

			} else if (theUserDefinedMetaClass.equals( "Satisfaction" ) ){

					theLocalDependency = _context.
							getExistingOrAddNewDependency( 
									theDependent, 
									toLocalReqt, 
									_context.getStereotypeForSatisfaction() );

				} else if( theUserDefinedMetaClass.equals( "Derivation" ) ){

					theLocalDependency = _context.
							getExistingOrAddNewDependency( 
									theDependent, 
									toLocalReqt, 
									_context.getStereotypeForDerivation() );

				} else if( theUserDefinedMetaClass.equals( "Refinement" ) ){

					theLocalDependency = _context.
							getExistingOrAddNewDependency( 
									theDependent, 
									toLocalReqt, 
									_context.getStereotypeForRefinement() );

				} else if( _context.hasStereotypeCalled( "trace", thePreviousDependency ) ){

					theLocalDependency = _context.
							getExistingOrAddNewDependency( 
									theDependent, 
									toLocalReqt, 
									_context.getStereotypeForTrace() );	

				} else {
					_context.debug( "Warning: No stereotype found on " + 
							_context.elInfo( thePreviousDependency ) + " to " + 
							_context.elInfo( toLocalReqt ) );

					theLocalDependency = _context.
							getExistingOrAddNewDependency( 
									theDependent, 
									toLocalReqt, 
									_context.getStereotypeForTrace() );	
				}			
				
		} 

		return theLocalDependency;
	}

	private void switchGraphElsFor(
			IRPRequirement theRemoteReqt,
			IRPRequirement toLocalReqt ) {

		_context.debug( "switchGraphElsFor from " + _context.elInfo( theRemoteReqt ) + 
				" to " + _context.elInfo ( toLocalReqt ) );

		List<IRPDependency> theExistingDependencies = _context.getDependenciesTo( theRemoteReqt );

		_context.debug( "Found " + theExistingDependencies.size() + 
				" dependencies to " + _context.elInfo( theRemoteReqt ) );

		List<IRPDependency> unmappedDependencies = new ArrayList<>();  

		for( IRPDependency theExistingDependency : theExistingDependencies ){

			IRPDependency theLocalDependency = 
					createLocalDependencyBasedOn( 
							toLocalReqt, theExistingDependency );

			if( theLocalDependency != null ){
				
				if( _oldToNewDependencyMap.containsKey( theExistingDependency ) ) {
				
					_context.debug( _context.elInfo( theExistingDependency ) + " is already in map" );
					
				} else {
					_context.debug( _context.elInfo( theExistingDependency ) + " from " + 
							_context.elInfo( theExistingDependency.getDependent() ) + " is mapped to " + 
							_context.elInfo( theLocalDependency ) );

					_oldToNewDependencyMap.put( theExistingDependency, theLocalDependency );				
				}
			} else {
				unmappedDependencies.add( theExistingDependency );
			}
		}

		@SuppressWarnings("unchecked")
		List<IRPModelElement> theReferences = theRemoteReqt.getReferences().toList();

		for( IRPModelElement theReference : theReferences ){

			if( theReference instanceof IRPDiagram ){

				IRPDiagram theDiagram = (IRPDiagram)theReference;
				theDiagram.openDiagram();

				IRPCollection theGraphElsToRemove = _context.get_rhpApp().createNewCollection();

				@SuppressWarnings("unchecked")
				List<IRPGraphElement> theOriginalGraphEls = theDiagram.
				getCorrespondingGraphicElements( theRemoteReqt ).toList();

				Iterator<IRPGraphElement> i = theOriginalGraphEls.iterator();

				while( i.hasNext() ){
					
					IRPGraphElement theOriginalGraphEl = (IRPGraphElement) i.next();

					IRPGraphNode theNewGraphNode = createGraphNodeFor(
							toLocalReqt, 
							(IRPGraphNode) theOriginalGraphEl );

					List<IRPGraphEdge> theOriginalConnectedEdges = 
							getGraphEdgesConnectorTo( (IRPGraphNode) theOriginalGraphEl );

					_context.debug( "Found " + theOriginalConnectedEdges.size() + " related graph edges" );

					for (IRPGraphEdge theOriginalConnectedEdge : theOriginalConnectedEdges) {

						IRPModelElement theOldDependency = 
								theOriginalConnectedEdge.getModelObject();

						if( theOldDependency instanceof IRPDependency ){

							IRPDependency theNewDependency = _oldToNewDependencyMap.get( theOldDependency );

							if( theNewDependency != null ){

								@SuppressWarnings("unused")
								IRPGraphEdge theNewGraphEdge = createGraphEdgeFor(
										(IRPDependency) theNewDependency, 
										(IRPGraphEdge) theOriginalConnectedEdge,
										theNewGraphNode );
								
								Set<IRPGraphNode> theGraphNodes = _existingGraphNodesToDelete.getOrDefault( theDiagram, new HashSet<>() );
								
								theGraphNodes.add( (IRPGraphNode) theOriginalGraphEl );
								
								_existingGraphNodesToDelete.put(theDiagram, theGraphNodes);
								
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

		//_context.debug( "theHeight = " + nHeight );
		//_context.debug( "theWidth = " + nWidth );
		//_context.debug( "xPos = " + xPosition );
		//_context.debug( "yPos = " + yPosition );

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
		 * @SuppressWarnings("unchecked") List<IRPGraphicalProperty> theGraphProperties
		 * = basedOnGraphEdge.getAllGraphicalProperties().toList();
		 * 
		 * for( IRPGraphicalProperty theGraphProperty : theGraphProperties ){
		 * 
		 * String theKey = theGraphProperty.getKey(); String theValue =
		 * theGraphProperty.getValue();
		 * 
		 * _context.debug( theKey + " = " + theValue ); }
		 */

		String srcPosition = basedOnGraphEdge.getGraphicalProperty( "SourcePosition" ).getValue();
		String trgPosition = basedOnGraphEdge.getGraphicalProperty( "TargetPosition" ).getValue();

		String srcSplit[] = srcPosition.split(",");
		String trgSplit[] = trgPosition.split(",");

		String xSrcPosition = srcSplit[0];
		String ySrcPosition = srcSplit[1];

		String xTrgPosition = trgSplit[0];
		String yTrgPosition = trgSplit[1];

		//_context.debug( "xSrcPosition = " + xSrcPosition );
		//_context.debug( "ySrcPosition = " + ySrcPosition );
		//_context.debug( "xTrgPosition = " + xTrgPosition );
		//_context.debug( "yTrgPosition = " + yTrgPosition );

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

		} else { // graph edge, hence no direct to populate

			_context.debug( "Attempting to complete relations to " + 
					_context.elInfo( theSourceGraphEl.getModelObject() ) );

			IRPCollection theGraphElements = _context.get_rhpApp().createNewCollection();
			theGraphElements.addGraphicalItem( theSourceGraphEl );

			theDiagram.completeRelations( theGraphElements, 1 );

			@SuppressWarnings("unchecked")
			List<IRPGraphElement> theCorrespondingGraphEls = theDiagram.getCorrespondingGraphicElements( theNewDependency ).toList();

			if( theCorrespondingGraphEls != null ){
				_context.debug( theCorrespondingGraphEls.size() + " graph elements related to " +
						_context.elInfo( theNewDependency ) + " were populated on " + _context.elInfo( theDiagram ) );
			}
		}

		return theNewEdge;
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