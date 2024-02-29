package com.mbsetraining.sysmlhelper.packagediagram;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import com.mbsetraining.sysmlhelper.common.UserInterfaceHelper;
import com.mbsetraining.sysmlhelper.executablembse.ExecutableMBSE_Context;
import com.telelogic.rhapsody.core.*;

public class PackageDiagramIndexCreator {

	private static final String PARD_LTRPAR_QC_FS18_BULLET = "\\pard\\ltrpar\\qc\\fs18\\bullet  ";
	protected ExecutableMBSE_Context _context;
	protected IRPStereotype _pkgIndexDiagramStereotype;
	protected IRPStereotype _autoDrawnStereotype;

	public static void main(String[] args) {

		ExecutableMBSE_Context context = new ExecutableMBSE_Context( 
				RhapsodyAppServer.getActiveRhapsodyApplication().getApplicationConnectionString() );

		IRPModelElement theSelectedEl = context.getSelectedElement( false );

		if( theSelectedEl instanceof IRPPackage ) {

			PackageDiagramIndexCreator theCreator = new PackageDiagramIndexCreator( context );
			theCreator.populateContentBasedOnPolicyFor( (IRPPackage) theSelectedEl );

		} else if( theSelectedEl instanceof IRPObjectModelDiagram ) {

			PackageDiagramIndexCreator theCreator = new PackageDiagramIndexCreator( context );
			theCreator.populateContentBasedOnPolicyFor( (IRPDiagram) theSelectedEl );
		}
	}

	public void removeGraphElementsFrom(
			IRPDiagram theDiagram ) {

		IRPCollection theGraphEls = theDiagram.getGraphicalElements();
		theDiagram.removeGraphElements( theGraphEls );
	}

	public PackageDiagramIndexCreator( 
			ExecutableMBSE_Context context ){

		_context = context;
		_pkgIndexDiagramStereotype = _context.getNewTermForPackageIndexDiagram();
		_autoDrawnStereotype = _context.getStereotypeForAutoDrawn();
	}

	private void applyAutoDrawnStereotypeTo(
			IRPDiagram theDiagram ){

		if( _autoDrawnStereotype != null ){

			if( _context.hasStereotypeCalled( _autoDrawnStereotype.getName(), theDiagram ) ) {

				_context.debug( "applyAutoDrawnStereotype is skipping " +  _context.elInfo( theDiagram ) + 
						" as " +  _context.elInfo( _autoDrawnStereotype ) + " is already applied." );	
			} else {
				_context.debug( "Applying " + _context.elInfo( _autoDrawnStereotype ) + 
						" to " + _context.elInfo( theDiagram ) );

				theDiagram.addSpecificStereotype( _autoDrawnStereotype );
				theDiagram.highLightElement();
			}
		}
	}

	public void populateContentBasedOnPolicyFor(
			IRPDiagram theDiagram ) {

		IRPPackage theRootPkg = _context.getOwningPackageFor( theDiagram );

		String policy = _context.getAutoGenerationOfPackageDiagramContentPolicy( theDiagram );

		Map<String, List<IRPModelElement>> theElementSetsMap = getNestedPackageMapToPopulateFor( theRootPkg );

		if( policy.equals( "Always" ) ){

			populateDiagramContentFor( theDiagram, theElementSetsMap );

		} else if( policy.equals( "UserDialog" ) ){

			boolean answer = UserInterfaceHelper.askAQuestion( 
					"Do you want to auto-populate the content based on owned packages of " + 
							theRootPkg.getName() + "?" );

			if( answer ) {
				populateDiagramContentFor( theDiagram, theElementSetsMap );
			} else {
				_context.info( "User chose to cancel" );
			}
		}	
	}

	public void populateContentBasedOnPolicyFor(
			IRPPackage thePackage ) {

		populatePackageContentFor( thePackage );

		/*
		String policy = _context.getAutoGenerationOfPackageDiagramContentPolicy( thePackage );

		if( policy.equals( "Always" ) ){

			populateContentFor( theDiagram );

		} else if( policy.equals( "UserDialog" ) ){

			boolean answer = UserInterfaceHelper.askAQuestion( 
					"Do you want to recursively auto-populate the content based on owned packages of " + 
							_rootPkg.getName() + "?" );

			if( answer ) {
				populateContentFor( theDiagram );
			} else {
				_context.info( "User chose to cancel" );
			}
		}*/	
	}

	private List<IRPDiagram> getNestedAutoDrawnPackageDiagrams(
			IRPPackage forPkg ){

		List<IRPDiagram> theAutoDrawnDiagrams = new ArrayList<>();

		@SuppressWarnings("unchecked")
		List<IRPDiagram> theCandidates = forPkg.getObjectModelDiagrams().toList();

		for( IRPDiagram theCandidate : theCandidates ){

			if( _context.hasStereotypeCalled( 
					_context.AUTO_DRAWN_STEREOTYPE, theCandidate ) &&
					_context.hasStereotypeCalled( 
							_context.PACKAGE_DIAGRAM_INDEX, theCandidate ) ) {

				theAutoDrawnDiagrams.add( theCandidate );
			}
		}

		return theAutoDrawnDiagrams;
	}

	protected void populatePackageContentFor( 
			IRPPackage thePackage ){

		if( thePackage.isReadOnly() == 0 ){				

			@SuppressWarnings("unchecked")
			List<IRPPackage> thePkgs = thePackage.getPackages().toList();

			for( IRPPackage thePkg : thePkgs ){
				populatePackageContentFor( thePkg );
			}

			List<IRPDiagram> theDiagrams = getNestedAutoDrawnPackageDiagrams( thePackage );
			int size = theDiagrams.size();

			Map<String, List<IRPModelElement>> theElementSetsMap = getNestedPackageMapToPopulateFor( thePackage );

			IRPDiagram theDiagram = null;

			if( size == 1 ) {

				theDiagram = (IRPDiagram) theDiagrams.get(0);
				_context.info( "Found " + _context.elInfo( theDiagram ) + " under " + _context.elInfo( thePackage ) );

			} else if( size > 1 ) {
				_context.warning( "Found " + size + " under " + _context.elInfo( thePackage ) + " hence don't know which one to choose" );
				theDiagram = (IRPDiagram) theDiagrams.get(0);

			} else {

				String theProposedName = _context.PACKAGE_DIAGRAM_INDEX_PREFIX + thePackage.getName();

				String theUniqueName = _context.determineUniqueNameBasedOn( theProposedName, "ObjectModelDiagram", thePackage );

				theDiagram = thePackage.addObjectModelDiagram( theUniqueName );
				theDiagram.setStereotype( _pkgIndexDiagramStereotype );
			}

			if( theDiagram != null ) {				
				populateDiagramContentFor( theDiagram, theElementSetsMap );
			}
		}
	}

	protected void populateDiagramContentFor( 
			IRPDiagram theDiagram,
			Map<String, List<IRPModelElement>> theElementSetsMap ){

		int xLeftMargin = 100;
		int yTopMargin = 100;

		int xPkgPos = xLeftMargin;
		int yPkgPos = yTopMargin;
		int pkgHeight = 200;
		int pkgWidth = 300;
		int pkgHorizGap = 50;
		int pkgVertGap = 50;

		applyAutoDrawnStereotypeTo( theDiagram );		

		IRPPackage theRootPkg = _context.getOwningPackageFor( theDiagram ); 

		IRPCollection theCompleteRelationsEls = _context.createNewCollection();

		for( Map.Entry<String, List<IRPModelElement>> entry : theElementSetsMap.entrySet() ){

			String thePackageType = entry.getKey();
			List<IRPModelElement> theNestedPkgs = entry.getValue();

			_context.debug( "populateContentFor found  " + theNestedPkgs.size() + " " + 
					thePackageType + " owned by " + _context.elInfo( theRootPkg ) );

			for( IRPModelElement theNestedPkg : theNestedPkgs ) {

				if( !( theNestedPkg instanceof IRPProject ) ) {
					
					@SuppressWarnings("unchecked")
					List<IRPGraphElement> theGraphEls = theDiagram.getCorrespondingGraphicElements( theNestedPkg ).toList();

					if( theGraphEls.size() != 0 ){

						_context.info( "Auto-populate is skipping " + _context.elInfo( theNestedPkg ) + 
								" as already on " + _context.elInfo( theDiagram ) );

						for( IRPGraphElement theGraphEl : theGraphEls ){
							theCompleteRelationsEls.addGraphicalItem( theGraphEl );
						}

					} else {
											
						_context.debug( "Graph node added for " + _context.elInfo( theNestedPkg ) );

						IRPGraphNode thePkgGraphNode = theDiagram.addNewNodeForElement( theNestedPkg, xPkgPos, yPkgPos, pkgWidth, pkgHeight );
						theCompleteRelationsEls.addGraphicalItem( thePkgGraphNode );

						xPkgPos += pkgWidth + pkgHorizGap;
					}

					List<IRPModelElement> theRTFLinks = getElementsToAddHyperLinksFor( theNestedPkg );

					updateRTFDescriptionFor( theNestedPkg, theRTFLinks );
				}
			}

			xPkgPos = xLeftMargin;
			yPkgPos += pkgHeight + pkgVertGap;
		}

		theDiagram.completeRelations( theCompleteRelationsEls, 0 );
	}

	private Map<String, List<IRPModelElement>> getNestedPackageMapToPopulateFor(
			IRPPackage theRootPkg ){

		//_context.info( "getNestedPackageMapToPopulateFor invoked for " + _context.elInfo( theRootPkg ) );

		Map<String, List<IRPModelElement>> theElementSetsMap = new LinkedHashMap<>();

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

		return theDiagrams;
	}

	private List<IRPModelElement> getElementsToAddHyperLinksFor(
			IRPModelElement thePkg ){

		//_context.info( "getDiagramsAndTableViewsFor invoked for " + _context.elInfo( thePkg ) );

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

		return theViews;
	}

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
	}

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
