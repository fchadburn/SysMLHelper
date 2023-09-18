package com.mbsetraining.sysmlhelper.doorsng;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.mbsetraining.sysmlhelper.common.UserInterfaceHelper;
import com.mbsetraining.sysmlhelper.executablembse.ExecutableMBSE_Context;
import com.telelogic.rhapsody.core.IRPApplication;
import com.telelogic.rhapsody.core.IRPModelElement;
import com.telelogic.rhapsody.core.IRPRequirement;
import com.telelogic.rhapsody.core.RhapsodyAppServer;

public class EstablishTraceRelationsToRemotes {

	protected ExecutableMBSE_Context _context;
	protected RemoteRequirementAssessment _assessment;
	

	// testing only
	public static void main(String[] args) {
		
		IRPApplication theRhpApp = RhapsodyAppServer.getActiveRhapsodyApplication();
		ExecutableMBSE_Context context = new ExecutableMBSE_Context( theRhpApp.getApplicationConnectionString() );
		EstablishTraceRelationsToRemotes theSwitcher = new EstablishTraceRelationsToRemotes(context);
		theSwitcher.establishTraceRelationsToRemoteReqts();
	}

	public EstablishTraceRelationsToRemotes(
			ExecutableMBSE_Context context ) {

		_context = context;
		_assessment = new RemoteRequirementAssessment( _context );
	}
	
	public void establishTraceRelationsToRemoteReqts(){

		IRPModelElement theSelectedEl = _context.getSelectedElement( false );
		List<IRPModelElement> theSelectedEls = new ArrayList<>();
		theSelectedEls.add( theSelectedEl );
		_assessment.determineRequirementsToUpdate( theSelectedEls );
		
		//Logger.debug( "theSelectedEl is " + Logger.elementInfo(theSelectedEl)  );
		
		boolean isContinue = true;
		
		if( _assessment._remoteRequirementsOwnedByPackage.isEmpty() ){

			UserInterfaceHelper.showWarningDialog( "I was unable to find any remote requirements under " + 
					_context.elInfo( theSelectedEl ) + "\n" +
					"Did you log into the Remote Artefacts Package? \n"+ 
					"You also need to establish OSLC links from " + 
					_context.elInfo( theSelectedEl ) + " \n" +
					"to the remote requirements you want to switch to.");
			
		} else {
			
			_context.info( "Found " + _assessment._requirementsThatDontTrace.size() + " Rhapsody-owned requirements under " + 
					_context.elInfo( theSelectedEl ) + " with no remote reqts");

			int matchesCount = _assessment._remoteRequirementsToEstablishTraceTo.keySet().size();
			
			if( matchesCount > 0 ) {
				
				String msg  = matchesCount + " matches were found. ";			
				msg += "\n\nDo you want to proceed with adding trace relations?\n";
				
				boolean answer = UserInterfaceHelper.askAQuestion( msg );

				if( answer ){

					for( Map.Entry<IRPModelElement, List<IRPRequirement>> entry : 
						_assessment._remoteRequirementsToEstablishTraceTo.entrySet() ){
						
						IRPModelElement theEl = entry.getKey();
						List<IRPRequirement> theRemoteMatches = entry.getValue();

						for( IRPRequirement theRemoteReqt : theRemoteMatches ){
							_context.establishTraceRelationFrom( theEl, theRemoteReqt );					
						}
					}		
				}
			} else {
				
				String msg = "There are " + _assessment._remoteRequirementsOwnedByPackage.size() + 
						" remote requirement links owned by " + _context.elInfo( theSelectedEl ) + "\n" + 
						"No matches were found to establish trace relations to. ";
				
				UserInterfaceHelper.showInformationDialog( msg );
			}
			
			/*

			int size = theDependencyMap.keySet().size();

			if( size == 0 ){
				
				if( _assessment._requirementsThatDontTrace.isEmpty() ) {
					
					UserInterfaceHelper.showInformationDialog( "No requirements were found needing trace relations" );

				} else if( _assessment._requirementsThatDontTrace.size() == 1 ){					
					UserInterfaceHelper.showInformationDialog( "No matches were found for the 1 requirement needing a trace relation" );
				} else {
					UserInterfaceHelper.showInformationDialog( "No matches were found for the " + _assessment._requirementsThatDontTrace.size() + " requirements needing trace relations" );
				}
			} else { 
				
				String msg;
				
				if( duplicateCount > 0 ) {
					msg = theDependencyMap.keySet().size() + " matches were found. ";
					msg += "Warning: " + duplicateCount + " have multiple matches \nwhich may cause issues. ";
				} else {
					msg = theDependencyMap.keySet().size() + " matches were found. ";
				}
				
				msg += "\n\nDo you want to proceed with adding trace relations?\n";
				
				boolean answer = UserInterfaceHelper.askAQuestion( msg );

				if( answer ){

					for (Map.Entry<IRPRequirement, List<IRPRequirement>> entry : theDependencyMap.entrySet()) {
						IRPRequirement theReqt = entry.getKey();
						List<IRPRequirement> theRemoteMatches = entry.getValue();

						for (IRPRequirement theRemoteReqt : theRemoteMatches) {
							_context.establishTraceRelationFrom( theReqt, theRemoteReqt );					
						}
					}		
				}
			}*/
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
