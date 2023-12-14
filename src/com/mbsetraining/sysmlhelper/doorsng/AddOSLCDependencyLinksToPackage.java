package com.mbsetraining.sysmlhelper.doorsng;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import com.mbsetraining.sysmlhelper.common.UserInterfaceHelper;
import com.mbsetraining.sysmlhelper.executablembse.ExecutableMBSE_Context;
import com.telelogic.rhapsody.core.*;

public class AddOSLCDependencyLinksToPackage {

	private ExecutableMBSE_Context _context;

	protected List<IRPDiagram> _diagrams;
	protected Set<IRPModelElement> _unloadedLinks = new HashSet<>();
	protected Set<IRPRequirement> _remoteReqts = new HashSet<>();
	protected List<IRPGraphNode> _remoteReqtGraphNodes = new ArrayList<>();

	// testing only
	public static void main(String[] args) {

		IRPApplication theRhpApp = RhapsodyAppServer.getActiveRhapsodyApplication();
		ExecutableMBSE_Context context = new ExecutableMBSE_Context( theRhpApp.getApplicationConnectionString() );

		List<IRPModelElement> theSelectedEls = context.getSelectedElements();
		List<IRPGraphElement> theSelectedGraphEls = context.getSelectedGraphElements();
		
		Set<IRPModelElement> theCombinedSet = 
				context.getSetOfElementsFromCombiningThe(
						theSelectedEls, theSelectedGraphEls );
								
		AddOSLCDependencyLinksToPackage theLinkCreator = new AddOSLCDependencyLinksToPackage( context );
		theLinkCreator.chooseAndAddOSLCLinksToPackageFor( theCombinedSet );
	}

	public AddOSLCDependencyLinksToPackage(
			ExecutableMBSE_Context context ) {

		_context = context;
	}

	public void chooseAndAddOSLCLinksToPackageFor(
			Set<IRPModelElement> theSelectedEls  ) {
		
		_diagrams = _context.getDiagramsBasedOn( theSelectedEls );
		
		for( IRPDiagram theDiagram : _diagrams ){
			addRemoteRequirementsFor( theDiagram );
		}
		
		chooseAndAddOSLCLinksToPackageFor( _remoteReqtGraphNodes );
	}

	protected void addRemoteRequirementsFor(
			IRPDiagram theDiagram ) {

		@SuppressWarnings("unchecked")
		List<IRPGraphElement> theGraphEls = theDiagram.getGraphicalElements().toList();

		for( IRPGraphElement theGraphEl : theGraphEls ){

			IRPModelElement theModelEl = theGraphEl.getModelObject();

			if( theModelEl instanceof IRPHyperLink ){

				IRPHyperLink theUnloadedLink = (IRPHyperLink) theModelEl;
				_context.info( _context.elInfo( theUnloadedLink ) + " on " + _context.elInfo( theDiagram ) + 
						" could be an unloaded remote requirement" );
				_unloadedLinks.add( theUnloadedLink );

			} else if( theModelEl instanceof IRPRequirement && 
					theGraphEl instanceof IRPGraphNode ){

				IRPRequirement theReqt = (IRPRequirement) theModelEl;

				if( theReqt.isRemote() == 1){

					_context.info( _context.elInfo( theReqt ) + " is a remote requirement on " + 
							_context.elInfo( theDiagram ) );
					
					_remoteReqts.add( theReqt );

					// must be a node
					_remoteReqtGraphNodes.add( (IRPGraphNode) theGraphEl );
				}
			}
		}
	}

	protected void chooseAndAddOSLCLinksToPackageFor(
			List<IRPGraphNode> theGraphNodes ) {

		List<IRPRequirement> theRemoteRequirements = getRemoteRequirementsFor( theGraphNodes );

		if( theRemoteRequirements.size() == 0 ) {

			UserInterfaceHelper.showWarningDialog( "Unable to proceed as their are no remote requirements in the selection" );

		} else {

			List<IRPModelElement> theRequirementPkgs = 
					_context.findElementsWithMetaClassAndStereotype(
							"Package", 
							_context.REQTS_ANALYSIS_REQUIREMENT_PACKAGE, 
							_context.get_rhpPrj(), 
							1 );			

			int theReqtPkgCount = theRequirementPkgs.size();

			IRPModelElement theSelectedEl = null;

			if( theReqtPkgCount == 0 ){

				UserInterfaceHelper.showWarningDialog( "Unable to proceed as their are no requirement packages in the project" );

			} else if( theReqtPkgCount == 1 ){

				theSelectedEl = theRequirementPkgs.get( 0 );

			} else {

				theSelectedEl = UserInterfaceHelper.
						launchDialogToSelectElement(
								theRequirementPkgs, 
								"Which requirement package do you want to synch to?", 
								true );
			}	

			if( theSelectedEl instanceof IRPPackage ) {
				
				theSelectedEl.highLightElement();

				List<IRPRequirement> theRemotesWithNoSurrogates = getRequirementsWithNoSurrogateFrom( theRemoteRequirements ); 

				if( theRemotesWithNoSurrogates.size() > 0 ) {

					boolean isContinue = UserInterfaceHelper.askAQuestion( 
							"There are " + theRemotesWithNoSurrogates.size() + " remote requirements selected that don't have a surrogate\n\n" +
									"Do you want to proceed with syncing these into " + _context.elInfo( theSelectedEl ) + "?" );

					if( !isContinue ){

						_context.debug( "User chose to cancel." ); 

					} else {

						for (IRPRequirement theRemoteWithNoSurrogate : theRemotesWithNoSurrogates) {

							_context.info( "Added remote " + "Trace" + " from " + 
									_context.elInfo( theSelectedEl ) + " to " + 
									_context.elInfo( theRemoteWithNoSurrogate ) );

							_context.addRemoteDependency(
									theRemoteWithNoSurrogate, theSelectedEl, "Trace" );
						}
					}
				}
			}
		}
	}

	private List<IRPRequirement> getRemoteRequirementsFor(
			List<IRPGraphNode> theGraphNodes ){

		List<IRPRequirement> theRemoteRequirements = new ArrayList<>();

		for( IRPGraphNode theGraphNode : theGraphNodes ){

			IRPModelElement theModelObject = theGraphNode.getModelObject();

			if( theModelObject instanceof IRPHyperLink ) {

				// Unloaded ?

			} else if( theModelObject instanceof IRPRequirement ) {

				IRPRequirement theRequirement = (IRPRequirement) theModelObject;

				if( theRequirement.isRemote()==1 ) {

					if( !theRemoteRequirements.contains( theRequirement ) ){
						theRemoteRequirements.add( theRequirement );
					}
				}
			}
		}

		return theRemoteRequirements;
	}

	public List<IRPRequirement> getRequirementsWithNoSurrogateFrom( 
			List<IRPRequirement> theCandidateRequirements ){

		List<IRPRequirement> theRequirementsWithNoSurrogate = new ArrayList<>();

		for (IRPRequirement theCandidateRequirement : theCandidateRequirements) {

			@SuppressWarnings("unchecked")
			List<IRPModelElement> theReferences = theCandidateRequirement.getReferences().toList();

			boolean isSurrogateFound = false;

			for( IRPModelElement theReference : theReferences ){

				//_context.info( _context.elInfo( theReference ) );

				if( theReference instanceof IRPDependency ) {

					IRPDependency theDependency = (IRPDependency) theReference;
					IRPModelElement theDependent = theDependency.getDependent();

					if( theDependent instanceof IRPRequirement ) {

						IRPRequirement theSurrogateReqt = (IRPRequirement)theDependent;

						_context.info( _context.elInfo( theCandidateRequirement ) + " is traced from " + 
								_context.elInfo( theSurrogateReqt ) + " owned by " + 
								_context.elInfo( theSurrogateReqt.getOwner() ) );

						isSurrogateFound = true;
						break;
					}
				}
			}

			if( !isSurrogateFound ) {

				_context.info( _context.elInfo( theCandidateRequirement ) + "  has no surrogate " );
				theRequirementsWithNoSurrogate.add( theCandidateRequirement );
			}
		}

		return theRequirementsWithNoSurrogate;
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