package com.mbsetraining.sysmlhelper.doorsng;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import com.mbsetraining.sysmlhelper.executablembse.ExecutableMBSE_Context;
import com.telelogic.rhapsody.core.IRPHyperLink;
import com.telelogic.rhapsody.core.IRPModelElement;
import com.telelogic.rhapsody.core.IRPPackage;
import com.telelogic.rhapsody.core.IRPRequirement;

public class RemoteRequirementAssessment {

	protected List<IRPRequirement> _requirementsInScope = new ArrayList<IRPRequirement>();
	protected List<IRPRequirement> _requirementsToUpdateSpec = new ArrayList<IRPRequirement>();
	protected List<IRPRequirement> _requirementsToUpdateName = new ArrayList<IRPRequirement>();
	protected List<IRPRequirement> _requirementsThatMatch = new ArrayList<IRPRequirement>();
	protected List<IRPRequirement> _requirementsWithNoLinks = new ArrayList<IRPRequirement>();
	protected List<IRPRequirement> _requirementsWithUnloadedHyperlinks = new ArrayList<IRPRequirement>();
	protected Map<IRPRequirement, IRPRequirement> _requirementsThatTrace = new HashMap<IRPRequirement,IRPRequirement>();
	protected List<IRPRequirement> _remoteRequirementsThatTrace = new ArrayList<IRPRequirement>();
	protected List<IRPRequirement> _remoteRequirementsThatDontTrace = new ArrayList<IRPRequirement>();
	protected Map<IRPRequirement, List<IRPRequirement>> _remoteRequirementsToEstablishTraceTo = new HashMap<>();  
	protected List<IRPRequirement> _requirementsWithNoMatchOrLinks = new ArrayList<IRPRequirement>();

	private ExecutableMBSE_Context _context;
	
	public static void main(String[] args) {
		// TODO Auto-generated method stub
	}

	public RemoteRequirementAssessment( 
			ExecutableMBSE_Context context ) {
		
		_context = context;
	}
	
	public void determineRequirementsToUpdate(
			List<IRPModelElement> theSelectedEls ) {

		_requirementsInScope = getNestedRequirementsFor( theSelectedEls );

		for( IRPRequirement theRequirement : _requirementsInScope ){
			determineRequirementsToUpdateBasedOn( theRequirement );
		}
		
		if( theSelectedEls.size()==1 ) {

			IRPModelElement theSelectedEl = theSelectedEls.get(0);

			if( theSelectedEl instanceof IRPPackage ) {
				determineNewRequirementsNeeded( (IRPPackage) theSelectedEl );
			}
		}
	}

	@SuppressWarnings("unchecked")
	private List<IRPRequirement> getNestedRequirementsFor(
			List<IRPModelElement> theSelectedEls ){

		Set<IRPRequirement> theNestedRequirements = new LinkedHashSet<IRPRequirement>();

		for( IRPModelElement theSelectedEl : theSelectedEls ){

			theNestedRequirements.addAll( theSelectedEl.
					getNestedElementsByMetaClass( "Requirement", 1 ).toList() );
		}

		return new ArrayList<>( theNestedRequirements );
	}

	public void determineNewRequirementsNeeded(
			IRPPackage theRequirementPkg ) {

		List<IRPRequirement> theMissingRemoteReqts = new ArrayList<>();

		List<IRPRequirement> theExpectedRemoteReqts = _context.getRemoteRequirementsFor( theRequirementPkg );

		for( IRPRequirement theExpectedRemoteReqt : theExpectedRemoteReqts ){

			if( !_remoteRequirementsThatTrace.contains( theExpectedRemoteReqt )) {
				//_context.debug( "Found that remote " + _context.elInfo( theExpectedRemoteReqt ) + " doesn't have traceability yet" );
				theMissingRemoteReqts.add( theExpectedRemoteReqt );
			}
		}

		for( IRPRequirement theReqt : _requirementsWithNoLinks ){
			
			List<IRPRequirement> theMatchedReqts = _context.getRequirementsThatMatch( theReqt, theExpectedRemoteReqts );

			int matchCount = theMatchedReqts.size();

			if( matchCount > 0 ) {

				for (IRPRequirement theMatchedReqt : theMatchedReqts) {
					_context.debug( "Found that spec of unlinked " + _context.elInfo( theReqt ) + " matches remote " + _context.elInfo( theMatchedReqt ) );
				}

				if( matchCount > 1 ) {
					_context.warning( "Match found " + _context.elInfo( theReqt ) + " has match to " + matchCount + " remote requirements hence don't know which one to choose" );
				}
				
				_remoteRequirementsToEstablishTraceTo.put( theReqt, theMatchedReqts );		

			} else {
				_requirementsWithNoMatchOrLinks.add( theReqt );
			}
		}

		for( IRPRequirement theMissingRemoteReqt : theMissingRemoteReqts ){
			
			boolean isFound = false;
			
			for (Map.Entry<IRPRequirement, List<IRPRequirement>> entry : _remoteRequirementsToEstablishTraceTo.entrySet()) {
				
				List<IRPRequirement> theValue = entry.getValue();
				
				if( theValue.contains( theMissingRemoteReqt ) ) {
					isFound = true;
					break;
				}
			}

			if( !isFound ) {
				_remoteRequirementsThatDontTrace.add( theMissingRemoteReqt );
			}
		}

		//int size = _remoteRequirementsToEstablishTraceTo.keySet().size();

		//_context.debug ( "determineNewRequirementsNeeded found " + size + " unlinked remote reqts have matches " + 
		//		"to requirements with same spec text and " +  _remoteRequirementsThatDontTrace.size() + " requirements do not" );
	}

	private void determineRequirementsToUpdateBasedOn(
			IRPRequirement theRequirement ){

		List<IRPModelElement> theRemoteDependsOns = _context.getRemoteDependsOnFor( theRequirement );

		if( theRemoteDependsOns.isEmpty() ) {

			if( !_requirementsWithNoLinks.contains( theRequirement ) ) {	
				//_context.debug( "Found " + _context.elInfo( theRequirement ) + "'s got no oslc links");
				_requirementsWithNoLinks.add( theRequirement );
			}
		} else {

			for( IRPModelElement theRemoteDependsOn : theRemoteDependsOns ){

				//_context.debug( _context.elInfo ( theRequirement ) + " traces to " + _context.elInfo( theRemoteDependsOn ) + 
				//		" owned by " + _context.elInfo( theRemoteDependsOn.getOwner() ) );

				if( theRemoteDependsOn instanceof IRPRequirement ){

					IRPRequirement theRemoteReqt = (IRPRequirement) theRemoteDependsOn;
					
					_remoteRequirementsThatTrace.add( theRemoteReqt );
					_requirementsThatTrace.put( theRequirement, theRemoteReqt );

					IRPRequirement theOSLCRequirement = (IRPRequirement)theRemoteDependsOn;

					String theRemoteSpec = theOSLCRequirement.getSpecification();
					String theSpec = theRequirement.getSpecification();

					if( theSpec.equals( theRemoteSpec ) ){

						if( !_requirementsThatMatch.contains( theRequirement ) ) {	
							_context.debug( "Found " + _context.elInfo( theRequirement ) + "'s spec matches");
							_requirementsThatMatch.add( theRequirement );
						}
					} else {

						if( !_requirementsToUpdateSpec.contains( theRequirement ) ) {		
							_context.debug( "Found " + _context.elInfo( theRequirement ) + "'s spec needs updating");
							_requirementsToUpdateSpec.add( theRequirement );
						}
					}
					
					String theRemoteName = theOSLCRequirement.getName();
					String theRemoteID = theOSLCRequirement.getRequirementID();
					
					String theProposedName = _context.determineRequirementNameBasedOn( theRemoteName, theRemoteID );
					
					String theName = theRequirement.getName();
					
					if( !theName.equals( theProposedName ) ){

						if( !_requirementsToUpdateName.contains( theRequirement ) ) {		
							_context.debug( "Found " + _context.elInfo( theRequirement ) + "'s name needs updating");
							_requirementsToUpdateName.add( theRequirement );
						}
					}

				} else if( theRemoteDependsOn instanceof IRPHyperLink ) {

					if( !_requirementsWithUnloadedHyperlinks.contains( theRequirement ) ) {					
						_context.debug( "Found " + _context.elInfo( theRequirement ) + "'s oslc link is unloaded");
						_requirementsWithUnloadedHyperlinks.add( theRequirement );
					}
				}
			}
		}		
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
