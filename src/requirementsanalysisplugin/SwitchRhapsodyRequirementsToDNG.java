package requirementsanalysisplugin;

import generalhelpers.GeneralHelpers;
import generalhelpers.Logger;
import generalhelpers.UserInterfaceHelpers;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import com.telelogic.rhapsody.core.*;

public class SwitchRhapsodyRequirementsToDNG {

	IRPApplication _rhpApp;
	IRPProject _rhpPrj;

	public static void main(String[] args) {
		String theAppID = RhapsodyAppServer.getActiveRhapsodyApplication().getApplicationConnectionString();
		SwitchRhapsodyRequirementsToDNG theSwitcher = new SwitchRhapsodyRequirementsToDNG(theAppID);
		theSwitcher.SwitchRequirements();
	}

	public SwitchRhapsodyRequirementsToDNG(
			String appID ) {

		_rhpApp = RhapsodyAppServer.getActiveRhapsodyApplicationByID( appID );
		_rhpPrj = _rhpApp.activeProject();
	}

	public void SwitchRequirements(){

		IRPModelElement theSelectedEl = _rhpApp.getSelectedElement();

		//Logger.writeLine( "theSelectedEl is " + Logger.elementInfo(theSelectedEl)  );

		Set<IRPRequirement> theRemoteReqts = getLinkedRemoteRequirements( theSelectedEl );

		if( theRemoteReqts.isEmpty() ){

			UserInterfaceHelpers.showWarningDialog( "I was unable to find any remote requirements under " + 
					Logger.elementInfo( theSelectedEl ) + "\n" +
					"Did you log into the Remote Artefacts Package? \n"+ 
					"You also need to establish OSLC links from " + 
					Logger.elementInfo( theSelectedEl ) + " \n" +
					"to the remote requirements you want to switch to.");
		} else {
			Logger.writeLine( "Found " + theRemoteReqts.size() + 
					" remote requirements related to " + 
					Logger.elementInfo( theSelectedEl ) );

			//for (IRPRequirement theRemoteReqt : theRemoteReqts) {
				//				Logger.writeLine( Logger.elementInfo(theRemoteReqt) + 
				//						" with specification " + theRemoteReqt.getSpecification() );
			//}

			@SuppressWarnings("unchecked")
			List<IRPRequirement> theReqts = theSelectedEl.getNestedElementsByMetaClass("Requirement", 1).toList();

			Logger.writeLine( "Found " + theReqts.size() + " Rhapsody-owned requirements under " + Logger.elementInfo( theSelectedEl ) );

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
			
				boolean answer = UserInterfaceHelpers.askAQuestion( 
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

	private void deleteFromModel(
			List<IRPModelElement> theEls ) {
		
		Iterator<IRPModelElement> i = theEls.iterator();

		while( i.hasNext() ){
			
			IRPModelElement theEl = (IRPModelElement) i.next();
			
			Logger.writeLine( "Deleting " + Logger.elementInfo( theEl ) + 
					" owned by " + Logger.elementInfo( theEl.getOwner() ) );
			
			theEl.deleteFromProject();
		}
	}

	private IRPDependency createRemoteDependencyBasedOn(
			IRPRequirement toRemoteReqt,
			IRPDependency thePreviousDependency ){

		IRPDependency theRemoteDependency = null;

		//IRPModelElement theDependsOn = thePreviousDependency.getDependsOn();
		//Logger.writeLine( "theDependsOn is " + Logger.elementInfo(theDependsOn));

		IRPModelElement theDependent = thePreviousDependency.getDependent();
		//Logger.writeLine( "theDependent is " + Logger.elementInfo(theDependent));

		//linkType - one of the link types available with the requirement tool that you are using. 
		// For example, for Doors Next Generation, the possible types are "Derives From", "Refines", "Satisfies", and "Trace".

		if( thePreviousDependency.getUserDefinedMetaClass().equals( "Satisfaction" ) ){

			theRemoteDependency = addRemoteDependency(
					toRemoteReqt, theDependent, "Satisfies" );

		} else if( thePreviousDependency.getUserDefinedMetaClass().equals( "Derivation" ) ){

			theRemoteDependency = addRemoteDependency(
					toRemoteReqt, theDependent, "Derives From" );

		} else if( thePreviousDependency.getUserDefinedMetaClass().equals( "Refinement" ) ){

			theRemoteDependency = addRemoteDependency(
					toRemoteReqt, theDependent, "Refines" );

		} else if( GeneralHelpers.hasStereotypeCalled( "trace", thePreviousDependency ) ){

			theRemoteDependency = addRemoteDependency(
					toRemoteReqt, theDependent, "Trace" );			
		} else {
			Logger.writeLine( "Warning: No stereotype found on " + 
					Logger.elementInfo( thePreviousDependency ) + " to " + 
					Logger.elementInfo( toRemoteReqt ) );

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
			
			Logger.writeLine( "Added remote " + theType + " from " + 
					Logger.elementInfo( theDependent ) + " to " + 
					Logger.elementInfo( toRemoteReqt ) );
			
		} catch( Exception e ){
			Logger.writeLine( "Unable to add remote " + theType + " from " + 
					Logger.elementInfo( theDependent ) + " to " + 
					Logger.elementInfo( toRemoteReqt ) + ", e=" + e.getMessage() );
		}

		return theRemoteDependency;
	}

	private List<IRPDependency> getDependenciesTo(
			IRPRequirement theReqt ){

		List<IRPDependency> theDependencies = new ArrayList<>();

		@SuppressWarnings("unchecked")
		List<IRPModelElement> theReferences = theReqt.getReferences().toList();

		for( IRPModelElement theReference : theReferences ){

			if( theReference instanceof IRPDependency ){
				theDependencies.add( (IRPDependency) theReference );
			}
		}

		return theDependencies;
	}

	private void switchGraphElsFor(
			IRPRequirement theReqt,
			IRPRequirement toRemoteReqt ) {

		//		Logger.writeLine( "switchGraphElsFor from " + Logger.elementInfo( theReqt ) + 
		//				" to " + Logger.elementInfo ( toRemoteReqt ) );

		List<IRPDependency> theExistingDependencies = getDependenciesTo( theReqt );

		Logger.writeLine( "Found " + theExistingDependencies.size() + 
				" dependencies to " + Logger.elementInfo( theReqt ) );

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

				IRPCollection theGraphElsToRemove = _rhpApp.createNewCollection();

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

					Logger.writeLine( "Found " + theOriginalConnectedEdges.size() + " related graph edges" );

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

		Logger.writeLine( "Switching graph element on " + 
				Logger.elementInfo( theDiagram ) + 
				" to " + Logger.elementInfo( toModelEl ) );

		/*
		@SuppressWarnings("unchecked")
		List<IRPGraphicalProperty> theGraphProperties =
				basedOnGraphNode.getAllGraphicalProperties().toList();

		for( IRPGraphicalProperty theGraphProperty : theGraphProperties ){

			String theKey = theGraphProperty.getKey();
			String theValue = theGraphProperty.getValue();

			Logger.writeLine( theKey + " = " + theValue );
		}*/

		String nHeight = basedOnGraphNode.getGraphicalProperty("Height").getValue();
		String nWidth = basedOnGraphNode.getGraphicalProperty("Width").getValue();
		String thePosition = basedOnGraphNode.getGraphicalProperty("Position").getValue();

		String split[] = thePosition.split(",");

		String xPosition = split[0];
		String yPosition = split[1];

		Logger.writeLine( "theHeight = " + nHeight );
		Logger.writeLine( "theWidth = " + nWidth );
		Logger.writeLine( "xPos = " + xPosition );
		Logger.writeLine( "yPos = " + yPosition );

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

		Logger.writeLine( "Switching graph element on " + 
				Logger.elementInfo( theDiagram ) + 
				" to " + Logger.elementInfo( theNewDependency ) );

		/*
		@SuppressWarnings("unchecked")
		List<IRPGraphicalProperty> theGraphProperties =
				basedOnGraphEdge.getAllGraphicalProperties().toList();

		for( IRPGraphicalProperty theGraphProperty : theGraphProperties ){

			String theKey = theGraphProperty.getKey();
			String theValue = theGraphProperty.getValue();

			Logger.writeLine( theKey + " = " + theValue );
		}*/

		String srcPosition = basedOnGraphEdge.getGraphicalProperty("SourcePosition").getValue();
		String trgPosition = basedOnGraphEdge.getGraphicalProperty("TargetPosition").getValue();

		String srcSplit[] = srcPosition.split(",");
		String trgSplit[] = trgPosition.split(",");

		String xSrcPosition = srcSplit[0];
		String ySrcPosition = srcSplit[1];

		String xTrgPosition = trgSplit[0];
		String yTrgPosition = trgSplit[1];

		Logger.writeLine( "xSrcPosition = " + xSrcPosition );
		Logger.writeLine( "ySrcPosition = " + ySrcPosition );
		Logger.writeLine( "xTrgPosition = " + xTrgPosition );
		Logger.writeLine( "yTrgPosition = " + yTrgPosition );

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
			IRPCollection theGraphElements = _rhpApp.createNewCollection();
			
			theGraphElements.addGraphicalItem(theSourceGraphEl);
			theGraphElements.addGraphicalItem(andRequirementGraphNode);
			
			IRPCollection theRelationsCollection = _rhpApp.createNewCollection();
			theRelationsCollection.setSize( 1 );
			theRelationsCollection.setString( 1, "AllRelations" );
			
			Logger.writeLine("Attempting to populate relations to " + 
					Logger.elementInfo( theSourceGraphEl.getModelObject() ) );
			
			theDiagram.populateDiagram( theGraphElements, theRelationsCollection, "among" );
			
			@SuppressWarnings("unchecked")
			List<IRPGraphElement> theCorrespondingGraphEls =
					theDiagram.getCorrespondingGraphicElements(theNewDependency).toList();
			
			if( theCorrespondingGraphEls != null ){
				Logger.writeLine( "there are " + theCorrespondingGraphEls.size() + " graph elements related to " +
						Logger.elementInfo( theNewDependency ) );
			}
		}
		
		return theNewEdge;
	}
	

	public Set<IRPRequirement> getLinkedRemoteRequirements(
			IRPModelElement underEl ){

		Set<IRPRequirement> theRequirements = new HashSet<>();

		@SuppressWarnings("unchecked")
		List<IRPModelElement> theEls = underEl.getNestedElementsRecursive().toList();

		theEls.add( underEl );

		for (IRPModelElement theEl : theEls) {

			@SuppressWarnings("unchecked")
			List<IRPDependency> theRemoteDependencies = theEl.getRemoteDependencies().toList();

			for (IRPDependency theRemoteDependency : theRemoteDependencies) {

				Logger.writeLine( Logger.elementInfo(theRemoteDependency));

				IRPModelElement theDependsOn = theRemoteDependency.getDependsOn();
				Logger.writeLine( "theDependsOn is " + Logger.elementInfo(theDependsOn));

				if( theDependsOn instanceof IRPRequirement ){
					theRequirements.add( (IRPRequirement) theDependsOn );
				}

				IRPModelElement theDependent = theRemoteDependency.getDependent();
				Logger.writeLine( "theDependent is " + Logger.elementInfo(theDependent));
			}
		}

		return theRequirements;

	}

	private Set<IRPRequirement> getRequirementsThatMatch(
			String theSpecificationText,
			Set<IRPRequirement> theCandidates ){

		Set<IRPRequirement> theMatches = new HashSet<>();

		for (IRPRequirement theCandidate : theCandidates) {

			if( theCandidate.getSpecification().equals( theSpecificationText ) ){
				theMatches.add( theCandidate );
			}
		}

		return theMatches;
	}
}

/**
 * Copyright (C) 2020  MBSE Training and Consulting Limited (www.executablembse.com)

    Change history:
    #266 07-DEC-2020: Add initial support for CVS export & switching master of requirements to DOORS NG
    
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