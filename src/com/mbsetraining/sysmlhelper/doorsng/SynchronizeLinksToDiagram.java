package com.mbsetraining.sysmlhelper.doorsng;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import com.mbsetraining.sysmlhelper.common.UserInterfaceHelper;
import com.mbsetraining.sysmlhelper.executablembse.ExecutableMBSE_Context;
import com.telelogic.rhapsody.core.IRPApplication;
import com.telelogic.rhapsody.core.IRPDiagram;
import com.telelogic.rhapsody.core.IRPGraphElement;
import com.telelogic.rhapsody.core.IRPHyperLink;
import com.telelogic.rhapsody.core.IRPModelElement;
import com.telelogic.rhapsody.core.IRPRequirement;
import com.telelogic.rhapsody.core.IRPUnit;
import com.telelogic.rhapsody.core.RhapsodyAppServer;

public class SynchronizeLinksToDiagram {

	private List<String> _baseDiagramMetaClasses = Arrays.asList(
			"ObjectModelDiagram", 
			"StructureDiagram", 
			"UseCaseDiagram", 
			"ActivityDiagramGE", 
			"TimingDiagram", 
			"SequenceDiagram",
			"UseCaseDiagram",
			"DeploymentDiagram",
			"ComponentDiagram",
			"CommunicationDiagram",
			"PanelDiagram" );

	protected ExecutableMBSE_Context _context;

	protected LinkInfos _remoteLinksOk;
	protected LinkInfos _remoteLinksToDelete;
	protected LinkInfos _remoteLinksToAdd;
	protected Set<IRPRequirement> _localReqtsMissingLinks;
	protected Set<IRPRequirement> _remoteReqtsWithMultipleLinks;
	protected Set<IRPRequirement> _remoteReqtsWithLinks;
	protected Set<IRPDiagram> _diagramsWithReqts;

	protected Set<IRPHyperLink> _unloadedLinks;

	// testing only
	public static void main(String[] args) {

		IRPApplication theRhpApp = RhapsodyAppServer.getActiveRhapsodyApplication();
		ExecutableMBSE_Context context = new ExecutableMBSE_Context( theRhpApp.getApplicationConnectionString() );

		Set<IRPModelElement> theSelectedEls = new HashSet<>();
		theSelectedEls.add( context.getSelectedElement(false));

		SynchronizeLinksToDiagram theSwitcher = new SynchronizeLinksToDiagram( context );

		theSwitcher.synchronizeLinksToDiagram( theSelectedEls );
	}

	public SynchronizeLinksToDiagram(
			ExecutableMBSE_Context context ) {

		_context = context;

		_remoteLinksOk = new LinkInfos( false, false, _context );
		_remoteLinksToAdd = new LinkInfos( false, false, _context );
		_remoteLinksToDelete = new LinkInfos( false, false, _context );
		_localReqtsMissingLinks = new HashSet<>();
		_remoteReqtsWithMultipleLinks = new HashSet<>();
		_remoteReqtsWithLinks = new HashSet<>();
		_unloadedLinks = new HashSet<>();
		_diagramsWithReqts = new HashSet<>();
	}

	public void synchronizeLinksToDiagram( 
			Set<IRPModelElement> theSelectedEls ) {

		List<IRPDiagram> theDiagrams = getDiagramsBasedOn( theSelectedEls );

		String msg = "This helper will synchronize links to remote requirements based on their presence on the diagram(s). \n";

		if( theDiagrams.isEmpty() ) {
			
			msg += "No diagram(s) were found based on selected elements";

			UserInterfaceHelper.showInformationDialog( msg );

		} else {

			boolean isContinue = true;

			for( IRPDiagram theDiagram : theDiagrams ){
				determineMissingAndToBeDeletedLinksFor( theDiagram );
			}
			
			int linksOkCount = _remoteLinksOk.size();
			int linksToAddCount = _remoteLinksToAdd.size();
			int linksToDeleteCount = _remoteLinksToDelete.size();
			int unloadedReqtsCount = _unloadedLinks.size();

			int diagramsWithLinks = _diagramsWithReqts.size();
			
			if( diagramsWithLinks > 0 ) {
				msg += linksOkCount + " link(s) were found that are present and correct across " + diagramsWithLinks + 
						" of the " + theDiagrams.size() + " diagrams found to have requirements. \n";
				
				String theDiagramString = _context.buildStringFromModelEls( new ArrayList<>( _diagramsWithReqts ), 5 );

				msg += theDiagramString + "\n";
				
				if( linksToAddCount > 0 ) {
					msg += linksToAddCount + " remote link(s) were found to add based on their presence on diagram(s). \n";
				}
			} else {
				msg += linksOkCount + " link(s) were found to be needed on the " + theDiagrams.size() + " diagrams found. \n";
			}

			if( linksToDeleteCount > 0 ) {
				msg += linksToDeleteCount + " remote link(s) were found to delete as no longer present on diagram(s). \n";
			}

			if( unloadedReqtsCount > 0 ) {
				msg += "Warning: There are " + unloadedReqtsCount + " unloaded links to remote requirements (check you've logged into remote artifacts). \n";
			}

			//msg += _linksFoundOnLocal + " link(s) were checked. \n";

			if( linksToAddCount == 0 && linksToDeleteCount == 0 ) {
				
				msg += "No links to remotes were found that need to be synchronized (i.e. added or deleted). \n";
//				msg += linksOkCount + " link(s) were found that are present and correct. \n";

				UserInterfaceHelper.showInformationDialog( msg );
			} else {

				if( unloadedReqtsCount > 0 ) {
					msg += "Are you sure you want to continue with synchronizing links?";
				} else {
					msg += "Do you want to proceed with synchronizing links?";
				}

				isContinue = UserInterfaceHelper.askAQuestion( msg );

				if( isContinue ) {			

					addMissingLinks();
					deleteNotNeededLinks();

				} else {
					_context.info( "User chose to cancel" );

					_context.info( linksToAddCount + " missing remote requirement links were found that need creating:" );	
					_remoteLinksToAdd.dumpInfo();
				}
			}
		}
	}

	private List<IRPDiagram> getDiagramsBasedOn( 
			Set<IRPModelElement> theSelectedEls ){

		Set<IRPDiagram> theDiagrams = new HashSet<>();

		for( IRPModelElement theSelectedEl : theSelectedEls ){

			if( theSelectedEl instanceof IRPDiagram ) {
				theDiagrams.add( (IRPDiagram) theSelectedEl );
			} else {

				for( String baseMetaClass : _baseDiagramMetaClasses ){

					@SuppressWarnings("unchecked")
					List<IRPDiagram> theNestedEls = theSelectedEl.getNestedElementsByMetaClass( baseMetaClass, 1 ).toList();

					for( IRPDiagram theNestedEl : theNestedEls ){
												
						IRPUnit theUnit = theNestedEl.getSaveUnit();
						
						// Only add is read/write, and not added by reference
						if( theUnit.isReadOnly()==0 &&
								theUnit.isReferenceUnit()==0 ) {
							theDiagrams.add( theNestedEl );
						}
					}
				}
			}
		}

		return new ArrayList<>( theDiagrams );
	}

	private void determineMissingAndToBeDeletedLinksFor( 
			IRPDiagram theDiagram ){

		_context.info( "Checking " + _context.elInfo( theDiagram ) );

		Set<IRPRequirement> theExpectedRemoteReqts = new HashSet<>();

		@SuppressWarnings("unchecked")
		List<IRPGraphElement> theGraphEls = theDiagram.getGraphicalElements().toList();

		for( IRPGraphElement theGraphEl : theGraphEls ){

			IRPModelElement theModelEl = theGraphEl.getModelObject();

			if( theModelEl instanceof IRPHyperLink ){

				IRPHyperLink theUnloadedLink = (IRPHyperLink) theModelEl;
				_context.info( _context.elInfo( theUnloadedLink ) + " could be an unloaded remote requirement" );
				_unloadedLinks.add( theUnloadedLink );
				_diagramsWithReqts.add( theDiagram );

			} else if( theModelEl instanceof IRPRequirement ){

				IRPRequirement theReqt = (IRPRequirement) theModelEl;

				if( theReqt.isRemote() == 1){

					_context.info( _context.elInfo( theReqt ) + " is a remote requirement directly on diagram" );
					theExpectedRemoteReqts.add( theReqt );
					_diagramsWithReqts.add( theDiagram );

				} else {

					List<IRPModelElement> theRemoteDependsOns = _context.getRemoteDependsOnFor( theReqt );

					int remoteDependsOnCount = theRemoteDependsOns.size();

					if( remoteDependsOnCount == 1 ) {

						IRPModelElement theRemoteDependsOn = theRemoteDependsOns.get( 0 );

						if( theRemoteDependsOn instanceof IRPRequirement ) {

							IRPRequirement theRemoteReqt = (IRPRequirement) theRemoteDependsOn;
							_context.info( _context.elInfo( theRemoteReqt ) + " is a remote requirement inferred by requirement on diagram" );
							theExpectedRemoteReqts.add( theRemoteReqt );
							_diagramsWithReqts.add( theDiagram );

						} else if( theRemoteDependsOn instanceof IRPHyperLink ) {

							IRPHyperLink theUnloadedLink = (IRPHyperLink) theRemoteDependsOn;
							_context.info( _context.elInfo( theUnloadedLink ) + " is inferred by requirement but is unloaded" );
							_unloadedLinks.add( theUnloadedLink );
							_diagramsWithReqts.add( theDiagram );
						}

					} else if( remoteDependsOnCount == 0 ) {

						_context.info( _context.elInfo( theReqt ) + " is a local requirement without a remote link" );
						_localReqtsMissingLinks.add( theReqt );

					} else {

						_context.warning( "Skipping " + _context.elInfo( theReqt ) + " as it is a local requirement with " + 
								remoteDependsOnCount + " inferred remote links, when expecting 1" );

						_remoteReqtsWithMultipleLinks.add( theReqt );						
					}
				}
			}
		}

		LinkInfos theExpectedLinkInfos = new LinkInfos( false, false, _context );

		for( IRPRequirement remoteReqt : theExpectedRemoteReqts ){

			LinkInfos theExistingLinkInfos = new LinkInfos( remoteReqt, false, false, _context );

			LinkInfo theExpectedLinkInfo = new LinkInfo( theDiagram, remoteReqt, "Trace", _context );
			theExpectedLinkInfos.add( theExpectedLinkInfo );

			if( theExistingLinkInfos.isEquivalentPresentFor( theExpectedLinkInfo ) ){

				_remoteLinksOk.add( theExpectedLinkInfo );
			} else {
				_context.info( theExpectedLinkInfo.getInfo() + " is missing" );
				_remoteLinksToAdd.add( theExpectedLinkInfo );
			}
		}

		List<IRPRequirement> theExistingRemoteReqts = _context.getRemoteRequirementsFor( theDiagram );

		for( IRPRequirement remoteReqt : theExistingRemoteReqts ){

			if( !theExpectedRemoteReqts.contains( remoteReqt ) ){

				LinkInfo theExistingLinkInfo = new LinkInfo( theDiagram, remoteReqt, "trace", _context );
				_context.info( theExistingLinkInfo.getInfo() + " needs deleting" );
				_remoteLinksToDelete.add( theExistingLinkInfo );
			}
		}
	}

	private void addMissingLinks() {

		if( _remoteLinksToAdd.size() == 1 ) {
			_context.info( "1 remote link was found to be missing from diagram(s)" );	

		} else if( _remoteLinksToAdd.size() > 1 ) {
			_context.info( _remoteLinksToAdd.size() + " remote links from diagrams were found to be missing" );	
		}

		for( LinkInfo linkInfo : _remoteLinksToAdd ){

			_context.info( "Creating " + linkInfo.getInfo() );
			linkInfo.createLink();
		}
	}

	private void deleteNotNeededLinks() {

		if( _remoteLinksToDelete.size() == 1 ) {
			_context.info( "1 remote link from diagram was found to delete" );	

		} else if( _remoteLinksToDelete.size() > 1 ) {
			_context.info( _remoteLinksToDelete.size() + " remote links from diagrams were found to be not needed" );
		}

		for( LinkInfo linkInfo : _remoteLinksToDelete ){

			_context.info( "Deleting " + linkInfo.getInfo() );
			linkInfo.deleteLink();
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