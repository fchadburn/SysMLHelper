package com.mbsetraining.sysmlhelper.doorsng;

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

	ExecutableMBSE_Context _context;

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
	}
	
	public void establishTraceRelationsToRemoteReqts(){

		IRPModelElement theSelectedEl = _context.getSelectedElement( false );

		//Logger.debug( "theSelectedEl is " + Logger.elementInfo(theSelectedEl)  );

		List<IRPRequirement> theRemoteReqts = _context.getRemoteRequirementsFor( theSelectedEl );

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

			List<IRPRequirement> theReqts = _context.getRequirementsThatDontTraceToRemoteRequirements( theSelectedEl );

			_context.debug( "Found " + theReqts.size() + " Rhapsody-owned requirements under " + _context.elInfo( theSelectedEl ) + " with no remote reqts");

			Map<IRPRequirement,List<IRPRequirement>> theDependencyMap = new HashMap<>();  
			
			int duplicateCount = 0;

			for( IRPRequirement theReqt : theReqts ){
							
				List<IRPRequirement> theMatchedReqts = _context.getRequirementsThatMatch( theReqt, theRemoteReqts );
				
				int count = theMatchedReqts.size();

				if( count == 1 ){			
					
					_context.debug( "Found 1 match for local " + _context.elInfo( theReqt ) );
					theDependencyMap.put( theReqt, theMatchedReqts );		
					
				} else if ( count > 0 ) {
					
					_context.warning( "Found " + count + " matches for local " + _context.elInfo( theReqt ) );
					theDependencyMap.put( theReqt, theMatchedReqts );	
					duplicateCount++;
				}
			}

			int size = theDependencyMap.keySet().size();

			if( size == 0 ){
				UserInterfaceHelper.showInformationDialog( "No matches were found." );
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
