package com.mbsetraining.sysmlhelper.packagediagram;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import com.mbsetraining.sysmlhelper.common.UserInterfaceHelper;
import com.mbsetraining.sysmlhelper.executablembse.ExecutableMBSE_Context;
import com.telelogic.rhapsody.core.*;

public class PackageDiagramIndexCreator {

	protected ExecutableMBSE_Context _context;
	protected IRPPackage _rootPkg;
	protected String _policy;
	protected IRPStereotype _pkgIndexDiagramStereotype;
	protected IRPStereotype _autoDrawnStereotype;
	protected List<String> _packageMetaClasses;
	protected List<String> _diagramMetaClasses;

	public static void main(String[] args) {

		ExecutableMBSE_Context context = new ExecutableMBSE_Context( 
				RhapsodyAppServer.getActiveRhapsodyApplication().getApplicationConnectionString() );

		IRPModelElement theSelectedEl = context.getSelectedElement( false );

		if( theSelectedEl instanceof IRPPackage ) {

			IRPPackage theRootPkg = (IRPPackage) theSelectedEl;

			PackageDiagramIndexCreator theCreator = new PackageDiagramIndexCreator( theRootPkg, context );

			theCreator.createDiagramBasedOnPolicy();

		} else if( theSelectedEl instanceof IRPObjectModelDiagram ) {

			IRPPackage theRootPkg = context.getOwningPackageFor( theSelectedEl );

			PackageDiagramIndexCreator theCreator = new PackageDiagramIndexCreator( theRootPkg, context );
			theCreator.populateContentBasedOnPolicyFor( (IRPDiagram) theSelectedEl );
		}
	}

	public void removeGraphElementsFrom(
			IRPDiagram theDiagram ) {

		IRPCollection theGraphEls = theDiagram.getGraphicalElements();
		theDiagram.removeGraphElements( theGraphEls );
	}

	public PackageDiagramIndexCreator( 
			IRPPackage theRootPkg,
			ExecutableMBSE_Context context ){

		_context = context;
		_rootPkg = theRootPkg;
		_policy = _context.getAutoGenerationOfPackageDiagramContentPolicy( _rootPkg );
		_pkgIndexDiagramStereotype = _context.getNewTermForPackageIndexDiagram();
		_autoDrawnStereotype = _context.getStereotypeForAutoDrawn();
		_packageMetaClasses = _context.getPackageDiagramIndexUserDefinedMetaClasses();
		_diagramMetaClasses = _context.getPackageDiagramIndexDiagramMetaClasses();
	}

	public void createDiagramBasedOnPolicy() {

		if( _policy.equals( "Always" ) ){

			createNewDiagram();

		} else if( _policy.equals( "UserDialog" ) ){

			boolean answer = UserInterfaceHelper.askAQuestion( 
					"Do you want to auto-populate the content based on owned packages of " + 
							_rootPkg.getName() + "?" );

			if( answer ) {
				createNewDiagram();
			} else {
				_context.info( "User chose to cancel" );
			}
		}		
	}

	public IRPDiagram createNewDiagram() {

		String theProposedName = _context.PACKAGE_DIAGRAM_INDEX_PREFIX + _rootPkg.getName();

		String theUniqueName = _context.determineUniqueNameBasedOn( theProposedName, "ObjectModelDiagram", _rootPkg );

		IRPDiagram theDiagram = _rootPkg.addObjectModelDiagram( theUniqueName );
		theDiagram.setStereotype( _pkgIndexDiagramStereotype );

		populateContentFor( theDiagram );

		return theDiagram;
	}

	private void applyAutoDrawnStereotype(
			IRPDiagram theDiagram) {

		if( _autoDrawnStereotype != null ){

			if( _context.hasStereotypeCalled( _autoDrawnStereotype.getName(), theDiagram ) ) {

				_context.debug( "applyAutoDrawnStereotype is skipping " +  _context.elInfo( theDiagram ) + 
						" as " +  _context.elInfo( _autoDrawnStereotype ) + " is already applied." );	
			} else {
				_context.debug( "Applying " + _context.elInfo( _autoDrawnStereotype ) + 
						" to " + _context.elInfo( theDiagram ) );

				theDiagram.addSpecificStereotype( _autoDrawnStereotype );
				theDiagram.highLightElement();

				_rootPkg.highLightElement();
			}
		}
	}

	public void populateContentBasedOnPolicyFor(
			IRPDiagram theDiagram ) {

		if( _policy.equals( "Always" ) ){

			populateContentFor( theDiagram );

		} else if( _policy.equals( "UserDialog" ) ){

			boolean answer = UserInterfaceHelper.askAQuestion( 
					"Do you want to auto-populate the content based on owned packages of " + 
							_rootPkg.getName() + "?" );

			if( answer ) {
				populateContentFor( theDiagram );
			} else {
				_context.info( "User chose to cancel" );
			}
		}	
	}

	protected void populateContentFor( 
			IRPDiagram theDiagram ){

		int xLeftMargin = 100;
		int yTopMargin = 100;

		int xPkgPos = xLeftMargin;
		int yPkgPos = yTopMargin;
		int pkgHeight = 200;
		int pkgWidth = 300;
		int pkgHorizGap = 50;
		int pkgVertGap = 50;

		applyAutoDrawnStereotype( theDiagram );		

		Map<String, List<IRPModelElement>> theElementSetsMap = new LinkedHashMap<>();

		for( String thePackageType : _packageMetaClasses ){

			List<IRPModelElement> theNestedPkgs = _context.
					findElementsWithMetaClassAndStereotype( "Package", thePackageType, _rootPkg, 0 );

			if( !theNestedPkgs.isEmpty() ) {				
				theElementSetsMap.put( thePackageType, theNestedPkgs );
			}
		}

		IRPCollection theCompleteRelationsEls = _context.createNewCollection();

		for( Map.Entry<String, List<IRPModelElement>> entry : theElementSetsMap.entrySet() ){

			String thePackageType = entry.getKey();
			List<IRPModelElement> theNestedPkgs = entry.getValue();

			_context.debug( "populateContentFor found " + theNestedPkgs.size() + " " + 
					thePackageType + " owned by " + _context.elInfo( _rootPkg ) );

			for( IRPModelElement theNestedPkg : theNestedPkgs ) {

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

					IRPGraphNode theGraphNode = theDiagram.addNewNodeForElement( theNestedPkg, xPkgPos, yPkgPos, pkgWidth, pkgHeight );
					theCompleteRelationsEls.addGraphicalItem( theGraphNode );

					xPkgPos += pkgWidth + pkgHorizGap;
				}

				updateRTFDescriptionFor( theNestedPkg );
			}

			xPkgPos = xLeftMargin;
			yPkgPos += pkgHeight + pkgVertGap;
		}

		theDiagram.completeRelations( theCompleteRelationsEls, 0 );
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

	private void updateRTFDescriptionFor(
			IRPModelElement thePkg ){

		//_context.debug( "updateRTFDescriptionFor invoked for " + _context.elInfo( thePkg ) );
		
		List<IRPModelElement> theRTFLinks = new ArrayList<>();
		
		List<IRPModelElement> candidateDiagrams = getAllDiagramsUnder( (IRPPackage) thePkg );

		for( String theMetaClass : _diagramMetaClasses ){

			for( IRPModelElement theCandidate : candidateDiagrams ){
				
				String theUserDefinedMetaClass = theCandidate.getUserDefinedMetaClass();

				//_context.info( "Does " +  _context.elInfo( theCandidate ) + " = " + theMetaClass );
				
				if( theUserDefinedMetaClass.equals( theMetaClass ) &&
						_diagramMetaClasses.contains( theUserDefinedMetaClass ) ) {
					
					if( !theRTFLinks.contains( theCandidate ) ) {
						theRTFLinks.add( theCandidate );
					}
				}
			}
		}

		if( !theRTFLinks.isEmpty() ) {

			IRPCollection targets = _context.get_rhpApp().createNewCollection();
			targets.setSize( theRTFLinks.size() );

			String rtfText = "{\\rtf1\\fbidis\\ansi\\ansicpg1255\\deff0\\deflang1037{\\fonttbl{\\f0\\fnil\\fcharset0 Arial;}}\n{\\colortbl;\\red0\\green0\\blue255;}\n\\viewkind4\\uc1 ";

			int count = 1;

			for( IRPModelElement theRTFLink : theRTFLinks ){

				targets.setModelElement( count, theRTFLink );

				rtfText += "\\pard\\ltrpar\\qc\\fs18\\bullet  " + theRTFLink.getUserDefinedMetaClass() + 
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
