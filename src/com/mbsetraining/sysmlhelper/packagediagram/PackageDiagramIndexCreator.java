package com.mbsetraining.sysmlhelper.packagediagram;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import com.mbsetraining.sysmlhelper.common.UserInterfaceHelper;
import com.mbsetraining.sysmlhelper.executablembse.ExecutableMBSE_Context;
import com.telelogic.rhapsody.core.*;

public class PackageDiagramIndexCreator {

	private static final String DO_YOU_WANT_CONTINUE_MSG = "Do you want to recursively auto-populate the content based on owned packages of ";
	protected ExecutableMBSE_Context _context;
	protected IRPStereotype _pkgIndexDiagramStereotype;

	public static void main(String[] args) {

		ExecutableMBSE_Context context = new ExecutableMBSE_Context( 
				RhapsodyAppServer.getActiveRhapsodyApplication().getApplicationConnectionString() );

		IRPModelElement theSelectedEl = context.getSelectedElement( false );
		
		PackageDiagramIndexCreator theCreator = new PackageDiagramIndexCreator( context );

		if( theSelectedEl instanceof IRPPackage ) {

			theCreator.populateContentBasedOnPolicyForPackage( (IRPPackage) theSelectedEl );

		} else if( theSelectedEl instanceof IRPObjectModelDiagram ) {

			theCreator.populateContentBasedOnPolicyForDiagram( (IRPDiagram) theSelectedEl );
		}
	}

	public PackageDiagramIndexCreator( 
			ExecutableMBSE_Context context ){

		_context = context;
		_pkgIndexDiagramStereotype = _context.getNewTermForPackageIndexDiagram();
	}

	public void populateContentBasedOnPolicyForDiagram(
			IRPDiagram theDiagram ) {

		IRPPackage theRootPkg = _context.getOwningPackageFor( theDiagram );

		String policy = _context.getAutoGenerationOfPackageDiagramContentPolicy( theDiagram );

		Map<String, List<IRPModelElement>> theElementSetsMap = getNestedPackageMapToPopulateFor( theRootPkg );

		if( policy.equals( "Always" ) ){

			updatePackageDiagramContent( theElementSetsMap, theDiagram );
			
		} else if( policy.equals( "UserDialog" ) ){

			boolean answer = UserInterfaceHelper.askAQuestion( 
					"Do you want to auto-populate " + 
					_context.elInfo( theDiagram ) + " \nbased on owned packages of " + 
					theRootPkg.getName() + "?" );

			if( answer ) {
				updatePackageDiagramContent( theElementSetsMap, theDiagram );
			} else {
				_context.info( "User chose to cancel" );
			}
		}	
	}

	public void populateContentBasedOnPolicyForPackage(
			IRPPackage thePackage ) {

		String policy = _context.getAutoGenerationOfPackageDiagramContentPolicy( thePackage );

		if( policy.equals( "Always" ) ){

			populatePackageContentFor( thePackage );

		} else if( policy.equals( "UserDialog" ) ){

			boolean answer = UserInterfaceHelper.askAQuestion( DO_YOU_WANT_CONTINUE_MSG + 
							thePackage.getName() + "?" );

			if( answer ) {
				populatePackageContentFor( thePackage );
			} else {
				_context.info( "User chose to cancel" );
			}
		}
	}

	private List<IRPDiagram> getNestedPackageDiagrams(
			IRPPackage forPkg ){

		List<IRPDiagram> theAutoDrawnDiagrams = new ArrayList<>();

		@SuppressWarnings("unchecked")
		List<IRPDiagram> theCandidates = forPkg.getObjectModelDiagrams().toList();

		for( IRPDiagram theCandidate : theCandidates ){

			if( _context.hasStereotypeCalled( 
							_context.PACKAGE_DIAGRAM_INDEX, theCandidate ) ) {

				theAutoDrawnDiagrams.add( theCandidate );
			}
		}

		return theAutoDrawnDiagrams;
	}


	protected void populatePackageContentFor( 
			IRPPackage thePackage ){

		_context.debug( "populatePackageContentFor invoked for " + _context.elInfo(thePackage) );

		if( thePackage.isReadOnly() == 1 ){

			_context.warning( "Unable to generate package diagram - index for read-only " + _context.elInfo(thePackage) );

		} else {

			@SuppressWarnings("unchecked")
			List<IRPPackage> thePkgs = thePackage.getPackages().toList();

			// re-curse through nested packages
			for( IRPPackage thePkg : thePkgs ){
				populatePackageContentFor( thePkg );
			}
		
			createNewOrUpdateExistingIndexDiagramFor(  thePackage );
		}
	}

	private IRPDiagram getExistingOrCreateNewIndexDiagramFor(
			IRPPackage thePackage ){

		IRPDiagram theDiagram = null;

		List<IRPDiagram> theDiagrams = getNestedPackageDiagrams( thePackage );
		int size = theDiagrams.size();

		if( size == 1 ) {

			theDiagram = (IRPDiagram) theDiagrams.get(0);
			_context.info( "Re-using existing " + _context.elInfo( theDiagram ) + " under " + _context.elInfo( thePackage ) );

		} else if( size > 1 ) {
			_context.warning( "Found " + size + " under " + _context.elInfo( thePackage ) + " hence don't know which one to choose" );
			theDiagram = (IRPDiagram) theDiagrams.get(0);

		} else {

			String theProposedName = _context.PACKAGE_DIAGRAM_INDEX_PREFIX + thePackage.getName();
			String theUniqueName = _context.determineUniqueNameBasedOn( theProposedName, "ObjectModelDiagram", thePackage );

			theDiagram = thePackage.addObjectModelDiagram( theUniqueName );
			theDiagram.setStereotype( _pkgIndexDiagramStereotype );
		}

		return theDiagram;
	}

	protected void createNewOrUpdateExistingIndexDiagramFor( 
			IRPPackage thePackage ){

		Map<String, List<IRPModelElement>> thePackageMap = getNestedPackageMapToPopulateFor( thePackage );

		boolean isContinue = false;

		for( Map.Entry<String, List<IRPModelElement>> entry : thePackageMap.entrySet() ){

			String thePackageType = entry.getKey();
			List<IRPModelElement> thePkgs = entry.getValue();

			_context.debug( "checking content for  " + thePkgs.size() + " of type '" + thePackageType + "'" );

			for( IRPModelElement thePkg : thePkgs ){

				if( !thePkg.equals( thePackage ) ){
					isContinue = true;
					break;
				} else {
					List<IRPModelElement> theNestedEls = getElementsToAddHyperLinksFor( thePkg );

					if( theNestedEls.size()==1 ){
						IRPModelElement theNestedEl = theNestedEls.get(0);
						if( !theNestedEl.getUserDefinedMetaClass().equals( _context.PACKAGE_DIAGRAM_INDEX ) ){
							isContinue = true;
							break;
						}
					} else if( theNestedEls.size() > 1 ){
						isContinue = true;
						break;
					}
				}
			}
		}

		if( !isContinue ) {
			
			_context.info( "Skipping drawing package diagram for " + _context.elInfo( thePackage ) + " as nothing to show");
			
		} else {
			
			IRPDiagram theDiagram = getExistingOrCreateNewIndexDiagramFor( thePackage );

			theDiagram.highLightElement();
			
			updatePackageDiagramContent( thePackageMap, theDiagram );
		}
		
		thePackage.highLightElement();
	}

	private void updatePackageDiagramContent(
			Map<String, List<IRPModelElement>> thePackageMap, 
			IRPDiagram theDiagram ){
		
		int xLeftMargin = 100;
		int yTopMargin = 100;

		int pkgXPos = xLeftMargin;
		int pkgYPos = yTopMargin;
		int pkgHeight = 70;
		int pkgWidth = 300;
		int pkgHorizGap = 50;
		int pkgVertGap = 50;

		int elXOffset = 40;
		int elYOffset = 20;

		int elHeight = 48;
		int elWidth = 300;
		
		@SuppressWarnings("unchecked")
		List<IRPGraphElement> theGraphEls = theDiagram.getGraphicalElements().toList();		
		
		IRPCollection theGraphElsToRemove = _context.createNewCollection();
		
		for( IRPGraphElement theGraphEl : theGraphEls ){
			
			_context.dumpGraphicalPropertiesFor( theGraphEl );

			IRPGraphicalProperty theTypeProperty = theGraphEl.getGraphicalProperty("Type");
			
			if( theTypeProperty != null ) {
				String theType = theTypeProperty.getValue();
				
				if( !theType.equals( "DiagramFrame" ) ){
					theGraphElsToRemove.addGraphicalItem( theGraphEl );
				}
			} else {
				_context.warning( "updatePackageDiagramContent found graph el without a Type" );
			}
		}
		
		theDiagram.removeGraphElements( theGraphElsToRemove );

		for( Map.Entry<String, List<IRPModelElement>> entry : thePackageMap.entrySet() ){

			String thePackageType = entry.getKey();
			List<IRPModelElement> theNestedPkgs = entry.getValue();

			_context.debug( "populateContentFor found  " + theNestedPkgs.size() + " " + thePackageType );

			int elYMax = pkgYPos;

			for( IRPModelElement theNestedPkg : theNestedPkgs ) {

				_context.debug( "Adding graph node for " + _context.elInfo( theNestedPkg ) );
				IRPGraphNode thePkgGraphNode = theDiagram.addNewNodeForElement( theNestedPkg, pkgXPos, pkgYPos, pkgWidth, pkgHeight );

				int elXPos = pkgXPos + elXOffset;
				int elYPos = pkgYPos + pkgHeight + elYOffset;

				List<IRPModelElement> theEls = getElementsToAddHyperLinksFor(theNestedPkg);
				
				for( IRPModelElement theEl : theEls ){
					
					if( theEl.equals( theDiagram ) ) {
						_context.info( "Skipping adding graph node for " + _context.elInfo( theEl ) + " as it's on its own diagram");

					} else {
						_context.debug( "Adding graph node for " + _context.elInfo( theEl ) );

						IRPGraphNode theNode;
						
						try {
							theNode = theDiagram.addNewNodeForElement( theEl, elXPos, elYPos, elWidth, elHeight );
							
						} catch (Exception e) {
							
							_context.warning( "Adding hyperlink as Rhapsody api failed to add " + _context.elInfo( theEl ) + 
									" to " + _context.elInfo( theDiagram ) );
							
							IRPHyperLink theHyperLink = _context.createNewOrGetExistingHyperLink( theNestedPkg, theEl );
							theNode = theDiagram.addNewNodeForElement( theHyperLink, elXPos, elYPos, elWidth, elHeight );
							theEl = theHyperLink;
							
							// TODO Auto-generated catch block
							//e.printStackTrace();
						}

						if( !theEl.getOwner().equals( theNestedPkg ) ){

							_context.info( _context.elInfo(theEl) + " is not owned by " + _context.elInfo( theNestedPkg ));

						} else {

							theDiagram.addNewEdgeByType(
									"Containment Arrow", 
									theNode, 
									elXPos, 
									elYPos + (elHeight/2),
									thePkgGraphNode, 
									pkgXPos + (elXOffset/2), 
									pkgYPos + pkgHeight );
						} 

						if( elYMax < elYPos) {
							elYMax = elYPos;
						}

						elYPos = elYPos + elHeight + elYOffset;
						
						if( theEl.getUserDefinedMetaClass().equals(_context.PACKAGE_DIAGRAM_INDEX)) {
							break;
						}
					}
				}

				pkgXPos += pkgWidth + pkgHorizGap;
			}

			pkgXPos = xLeftMargin;
			pkgYPos = elYMax + elHeight + pkgVertGap;
		}
	}

	private Map<String, List<IRPModelElement>> getNestedPackageMapToPopulateFor(
			IRPPackage theRootPkg ){

		//_context.info( "getNestedPackageMapToPopulateFor invoked for " + _context.elInfo( theRootPkg ) );

		LinkedHashMap<String, List<IRPModelElement>> theElementSetsMap = new LinkedHashMap<>();

		List<IRPModelElement> theSelfList = new ArrayList<>();

		if( !( theRootPkg instanceof IRPProject ) ) {	
			theSelfList.add( theRootPkg );
			theElementSetsMap.put( "", theSelfList );
		}

		List<String> thePackageMetaClasses = _context.getPackageDiagramIndexUserDefinedMetaClasses( theRootPkg );

		for( String thePackageType : thePackageMetaClasses ){

			List<IRPModelElement> theNestedPkgs = _context.
					findElementsWithMetaClassAndStereotype( "Package", thePackageType, theRootPkg, 0 );

			if( !theNestedPkgs.isEmpty() ) {				
				theElementSetsMap.put( thePackageType, theNestedPkgs );
			}
		}

		//List<IRPModelElement> theEls = getElementsToAddHyperLinksFor( theRootPkg );

		return theElementSetsMap;
	}

	@SuppressWarnings("unchecked")
	private List<IRPModelElement> getAllDiagramsUnder(
			IRPPackage thePkg ){

		List<IRPModelElement> theDiagrams = new ArrayList<>();

		theDiagrams.addAll( thePkg.getObjectModelDiagrams().toList() );
		theDiagrams.addAll( thePkg.getStructureDiagrams().toList() );
		theDiagrams.addAll( thePkg.getUseCaseDiagrams().toList() );
		theDiagrams.addAll( thePkg.getCollaborationDiagrams().toList() );
		theDiagrams.addAll( thePkg.getPanelDiagrams().toList() );
		theDiagrams.addAll( thePkg.getSequenceDiagrams().toList() );
		theDiagrams.addAll( thePkg.getComponentDiagrams().toList() );
		theDiagrams.addAll( thePkg.getDeploymentDiagrams().toList() );
		theDiagrams.addAll( getActivityDiagramsOwnedByUseCasesUnder( thePkg ) );
		theDiagrams.addAll( getDiagramsOwnedByClassesUnder( thePkg ) );
		theDiagrams.addAll( thePkg.getNestedElementsByMetaClass( "TableView", 0).toList() );

		return theDiagrams;
	}

	private List<IRPModelElement> getElementsToAddHyperLinksFor(
			IRPModelElement thePkg ){


		List<IRPModelElement> theViews = new ArrayList<>();

		List<IRPModelElement> candidateDiagrams = getAllDiagramsUnder( (IRPPackage) thePkg );

		List<String> theDiagramMetaClasses = _context.getPackageDiagramIndexDiagramMetaClasses( thePkg );

		for( String theMetaClass : theDiagramMetaClasses ){

			for( IRPModelElement theCandidate : candidateDiagrams ){

				String theUserDefinedMetaClass = theCandidate.getUserDefinedMetaClass();

				//_context.info( "Does " +  _context.elInfo( theCandidate ) + " = " + theMetaClass );

				if( theUserDefinedMetaClass.equals( theMetaClass ) &&
						theDiagramMetaClasses.contains( theUserDefinedMetaClass ) ) {

					if( !theViews.contains( theCandidate ) ) {
						theViews.add( theCandidate );
					}
				}
			}
		}

		_context.info( "getDiagramsAndTableViewsFor found there are " + theViews.size() + " for " + _context.elInfo( thePkg ) );

		return theViews;
	}

	/*

	private void updateRTFDescriptionFor(
			IRPModelElement thePkg,
			List<IRPModelElement> theRTFLinks ){

		//_context.info( "updateRTFDescriptionFor invoked for " + _context.elInfo( thePkg ) );

		if( theRTFLinks.isEmpty() ) {

			String theExistingRTF = thePkg.getDescriptionRTF();

			//_context.info( theExistingRTF );

			if( theExistingRTF.contains(PARD_LTRPAR_QC_FS18_BULLET)) {
				thePkg.setDescription("");
			}

		} else {

			IRPCollection targets = _context.get_rhpApp().createNewCollection();
			targets.setSize( theRTFLinks.size() );

			String rtfText = "{\\rtf1\\fbidis\\ansi\\ansicpg1255\\deff0\\deflang1037{\\fonttbl{\\f0\\fnil\\fcharset0 Arial;}}\n{\\colortbl;\\red0\\green0\\blue255;}\n\\viewkind4\\uc1 ";

			int count = 1;

			for( IRPModelElement theRTFLink : theRTFLinks ){

				targets.setModelElement( count, theRTFLink );

				rtfText += PARD_LTRPAR_QC_FS18_BULLET + theRTFLink.getUserDefinedMetaClass() + 
						": \\cf1\\ul\\protect " + theRTFLink.getName() + "\\cf0\\ulnone\\protect0\\par";

				count++;
			}

			thePkg.setDescriptionAndHyperlinks( rtfText, targets );
		}
	}*/

	private List<IRPModelElement> getActivityDiagramsOwnedByUseCasesUnder(
			IRPPackage thePkg ){

		List<IRPModelElement> theDiagrams = new ArrayList<>();

		@SuppressWarnings("unchecked")
		List<IRPModelElement> theUseCases = thePkg.getNestedElementsByMetaClass( "UseCase", 0 ).toList();

		for( IRPModelElement theUseCase : theUseCases ){

			@SuppressWarnings("unchecked")
			List<IRPModelElement> theActivityDiagrams = theUseCase.getNestedElementsByMetaClass( "ActivityDiagram", 0 ).toList();
			theDiagrams.addAll( theActivityDiagrams );
		}

		return theDiagrams;
	}

	private List<IRPModelElement> getDiagramsOwnedByClassesUnder( 
			IRPPackage thePkg ){

		//_context.info( "getDiagramsOwnedByClassesUnder invoked for " + _context.elInfo( thePkg ) );

		List<IRPModelElement> theDiagrams = new ArrayList<>();

		@SuppressWarnings("unchecked")
		List<IRPModelElement> theClasses = thePkg.getNestedElementsByMetaClass( "Class", 0 ).toList();

		for( IRPModelElement theClass : theClasses ){

			//_context.info( "checking under " + _context.elInfo( theClass ) );

			@SuppressWarnings("unchecked")
			List<IRPModelElement> theOMDs = theClass.getNestedElementsByMetaClass( "ObjectModelDiagram", 0 ).toList();
			theDiagrams.addAll( theOMDs );
		}

		return theDiagrams;
	}
}

/**
 * Copyright (C) 2024  MBSE Training and Consulting Limited (www.executablembse.com)

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
