package com.mbsetraining.sysmlhelper.doorsng;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import com.mbsetraining.sysmlhelper.common.UserInterfaceHelper;
import com.mbsetraining.sysmlhelper.executablembse.ExecutableMBSE_Context;
import com.telelogic.rhapsody.core.IRPApplication;
import com.telelogic.rhapsody.core.IRPModelElement;
import com.telelogic.rhapsody.core.IRPPackage;
import com.telelogic.rhapsody.core.IRPRequirement;
import com.telelogic.rhapsody.core.RhapsodyAppServer;

public class SynchronizeLinksBasedOnSurrogate {

	protected ExecutableMBSE_Context _context;
	protected RemoteRequirementAssessment _assessment;
	protected int _linksToAddCount = 0;
	protected int _linksToDeleteCount = 0;
	protected Map<IRPRequirement, List<LinkInfo>> _linksNotNeededOnRemote = new HashMap<>();
	protected Map<IRPRequirement, List<LinkInfo>> _linksMissingOnRemote = new HashMap<>();
	
	// testing only
	public static void main(String[] args) {
		
		IRPApplication theRhpApp = RhapsodyAppServer.getActiveRhapsodyApplication();
		ExecutableMBSE_Context context = new ExecutableMBSE_Context( theRhpApp.getApplicationConnectionString() );
		
		IRPModelElement theSelectedEl = context.getSelectedElement( false );

		if( theSelectedEl instanceof IRPPackage ) {
			
			SynchronizeLinksBasedOnSurrogate theSwitcher = new SynchronizeLinksBasedOnSurrogate( context );
			theSwitcher.synchronizeLinksFromLocalToRemote( (IRPPackage) theSelectedEl );
		}
	}

	public SynchronizeLinksBasedOnSurrogate(
			ExecutableMBSE_Context context ) {

		_context = context;
	}

	public void synchronizeLinksFromLocalToRemote( 
			IRPPackage theRequirementsPkg ) {

		_assessment = new RemoteRequirementAssessment( _context );
		
		List<IRPModelElement> theSelectedEls = new ArrayList<>();
		theSelectedEls.add( theRequirementsPkg );
		
		_assessment.determineRequirementsToUpdate( theSelectedEls );
				
		boolean isContinue = true;
		
		int tracedCount = _assessment._requirementsThatTrace.size();
		int unloadedReqtsCount = _assessment._requirementsWithUnloadedHyperlinks.size();
		
		String msg = "This helper will synchronize the links so that remote requirement links match the current links to the local surrogate. \n";

		if( tracedCount == 0 ) {
			
			msg += "However, I'm unable to do this as no requirements were found that trace to remote requirements. ";

			if( unloadedReqtsCount > 0 ) {
				
				msg += "\n\n" + unloadedReqtsCount + " unloaded links to remote requirements were detected from local requirements in the package. \n";
				msg += "It is recommended to resolve these first, e.g., have you logged into the remote artifacts package? \n";

				UserInterfaceHelper.showWarningDialog( msg );
			} else {
				
				UserInterfaceHelper.showInformationDialog( msg );
			}
			
			isContinue = false;
			
		} else if( unloadedReqtsCount > 0 ) {
			
			msg += "There are " + unloadedReqtsCount + " unloaded links to remote requirements. \n";
			msg += "Are you sure you want to continue?";
			
			isContinue = UserInterfaceHelper.askAQuestion( msg );
		} else {
			
			msg +=  tracedCount + " local requirements were found that trace to remote remote requirements. \n\n";
			
			//isContinue = UserInterfaceHelper.askAQuestion( msg );
		}
				
		if( isContinue ) {
			
			determineMissingAndToBeDeletedLinks();
			
			if( _linksToAddCount == 0 && _linksToDeleteCount == 0 ) {
				msg += "No links were found that needed to be synchronized. \n";
				
				UserInterfaceHelper.showInformationDialog( msg );
			} else {
				
				if( _linksToAddCount > 0 ) {
					msg += _linksToAddCount + " links to local were found to add that are not present on the remote. \n";
				}
				
				if( _linksToDeleteCount > 0 ) {
					msg += _linksToDeleteCount + " links to remote were found to delete that are not present on the local. \n";
				}
				
				msg += "Do you want to proceed with synchronizing links?";
				
				isContinue = UserInterfaceHelper.askAQuestion( msg );
				
				if( isContinue ) {			
					
					addMissingLinks();
					deleteNotNeededLinks();
					
				} else {
					_context.info( "User chose to cancel" );
				}
			}
		}
	}

	private void determineMissingAndToBeDeletedLinks() {

		for( Entry<IRPRequirement, IRPRequirement> entry : _assessment._requirementsThatTrace.entrySet() ){
			
			IRPRequirement theLocalReqt = entry.getKey();
			IRPRequirement theRemoteReqt = entry.getValue();
			
			//_context.info( _context.elInfo( theLocalReqt ) + " maps to " + _context.elInfo( theRemoteReqt ) );	

			LinkInfos theLocalReqtLinkInfos = new LinkInfos( theLocalReqt, false, false, _context );
			LinkInfos theRemoteReqtLinkInfos = new LinkInfos( theRemoteReqt, false, false, _context );
			
			LinkInfos theMissingRemoteLinkInfos = new LinkInfos( false, false, _context );
			
			for( LinkInfo theLocalReqtLinkInfo : theLocalReqtLinkInfos ){
				
				if( !theRemoteReqtLinkInfos.isEquivalentPresentFor( theLocalReqtLinkInfo ) ) {
					
					LinkInfo theMissingInfo = new LinkInfo( 
							theLocalReqtLinkInfo._sourceEl, theRemoteReqt, theLocalReqtLinkInfo._type, _context );
					
					theMissingRemoteLinkInfos.add( theMissingInfo );
				}
			}
			
			if( !theMissingRemoteLinkInfos.isEmpty() ) {	
				
				//_context.info( theMissingRemoteLinkInfos.size() + " missing remote requirement links were found that need creating:" );	
				//theMissingRemoteLinkInfos.dumpInfo();
				
				_linksMissingOnRemote.put( theRemoteReqt, theMissingRemoteLinkInfos );
				_linksToAddCount += theMissingRemoteLinkInfos.size();
			}
			
			LinkInfos theRemoteLinkInfosNotNeeded = new LinkInfos( false, false, _context );
			
			for( LinkInfo theRemoteReqtLinkInfo : theRemoteReqtLinkInfos ){
				
				if( !theLocalReqtLinkInfos.isEquivalentPresentFor( theRemoteReqtLinkInfo ) ) {
					
					LinkInfo theUnnecessaryInfo = new LinkInfo( 
							theRemoteReqtLinkInfo._sourceEl, theRemoteReqt, theRemoteReqtLinkInfo._type, _context );
					
					theRemoteLinkInfosNotNeeded.add( theUnnecessaryInfo );
				}
			}
			
			if( !theRemoteLinkInfosNotNeeded.isEmpty() ) {

				//_context.info( theRemoteLinkInfosNotNeeded.size() + " unnecessary remote requirement links were found that need deleting:" );	
				//theRemoteLinkInfosNotNeeded.dumpInfo();
				
				_linksNotNeededOnRemote.put( theRemoteReqt, theRemoteLinkInfosNotNeeded );
				_linksToDeleteCount += theRemoteLinkInfosNotNeeded.size();
			}
		}
	}
	
	private void addMissingLinks() {

		_context.info( _linksToAddCount + " missing remote requirement links were found that need creating" );	

		for( Entry<IRPRequirement, List<LinkInfo>> entry : _linksMissingOnRemote.entrySet() ){
			
			List<LinkInfo> theLinkInfos = entry.getValue();
			
			for (LinkInfo linkInfo : theLinkInfos) {
				
				_context.info( "Creating " + linkInfo.getInfo() );
				linkInfo.createLink();
			}
		}
	}
	
	private void deleteNotNeededLinks() {

		_context.info( _linksToDeleteCount + " unnecessary remote requirement links were found that need deleting" );	

		for( Entry<IRPRequirement, List<LinkInfo>> entry : _linksNotNeededOnRemote.entrySet() ){
			
			List<LinkInfo> theLinkInfos = entry.getValue();
			
			for( LinkInfo linkInfo : theLinkInfos ){
				
				_context.info( "Deleting " + linkInfo.getInfo() );
				linkInfo.deleteLink();
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