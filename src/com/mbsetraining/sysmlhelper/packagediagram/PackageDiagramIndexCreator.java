package com.mbsetraining.sysmlhelper.packagediagram;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import com.mbsetraining.sysmlhelper.common.UserInterfaceHelper;
import com.mbsetraining.sysmlhelper.executablembse.ExecutableMBSE_Context;
import com.telelogic.rhapsody.core.*;

public class PackageDiagramIndexCreator {

	private static final String AUTO_CREATED = "AutoCreated";
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

		if( policy.equals( "Always" ) ){

			ElementTree theElementTree = new ElementTree( theRootPkg, _context );
			theElementTree.buildPackageDiagram( theDiagram );	
			theDiagram.openDiagram();
			
		} else if( policy.equals( "UserDialog" ) ){

			boolean answer = UserInterfaceHelper.askAQuestion( 
					"Do you want to auto-populate " + 
					_context.elInfo( theDiagram ) + " \nbased on owned packages of " + 
					theRootPkg.getName() + "?" );

			if( answer ) {
				
				ElementTree theElementTree = new ElementTree( theRootPkg, _context );
				theElementTree.buildPackageDiagram( theDiagram );
				theDiagram.openDiagram();
				
			} else {
				_context.info( "User chose to cancel" );
			}
		}	
	}

	public void populateContentBasedOnPolicyForPackage(
			IRPPackage thePackage ) {

		String policy = _context.getAutoGenerationOfPackageDiagramContentPolicy( thePackage );

		if( policy.equals( "Always" ) ){

			populatePackageContentFor( thePackage, false );
			
		} else if( policy.equals( "AlwaysRecurse" ) ){

			populatePackageContentFor( thePackage, true );
				
		} else if( policy.equals( "UserDialog" ) ){

			boolean answer = UserInterfaceHelper.askAQuestion( 
					"Do you want to auto-populate a " + _context.PACKAGE_DIAGRAM_INDEX + " based on \nowned packages of " + 
					thePackage.getName() + "?" );

			if( answer ){
								
				@SuppressWarnings("unchecked")
				List<IRPPackage> thePkgs = thePackage.getPackages().toList();
				
				int count = thePkgs.size();
				
				if( count > 0 ) {
					
					boolean recurse = UserInterfaceHelper.askAQuestion( 
							"There are " + count + " nested packages for " +
							thePackage.getName() + "\nDo you want to recursively build up diagrams for these (bottom-up)?" );
					
					populatePackageContentFor( thePackage, recurse );

				} else {
					populatePackageContentFor( thePackage, false );
				}
				
			} else {
				_context.info( "User chose to cancel" );
			}
		}
	}

	protected void populatePackageContentFor( 
			IRPPackage thePackage,
			boolean recurse ){

		_context.debug( "populatePackageContentFor invoked for " + _context.elInfo(thePackage) );

		if( thePackage.isReadOnly() == 1 ){

			_context.warning( "Unable to generate package diagram - index for read-only " + _context.elInfo(thePackage) );

		} else {

			if( recurse ) {
				
				@SuppressWarnings("unchecked")
				List<IRPPackage> thePkgs = thePackage.getPackages().toList();

				// re-curse through nested packages
				for( IRPPackage thePkg : thePkgs ){
					populatePackageContentFor( thePkg, recurse );
				}
			}
		
			ElementTree theElementTree = new ElementTree( thePackage, _context );
			
			if( theElementTree._rootNode != null &&
				!theElementTree._rootNode._children.isEmpty() ){
				
				if( theElementTree._rootNode.areNonPackageIndexDiagramChildElementsPresent() ) {
					
					IRPDiagram theDiagram = getExistingOrCreateNewPackageIndexDiagramFor( thePackage );
					theElementTree.buildPackageDiagram( theDiagram );
					theDiagram.openDiagram();
					
				} else {
					_context.info( "Skipping drawing of " + _context.PACKAGE_DIAGRAM_INDEX + " as nothing to draw for " + _context.elInfo( thePackage ) );
				}
				
			} else {
				_context.info( "Skipping drawing of " + _context.PACKAGE_DIAGRAM_INDEX + " as nothing to draw for " + _context.elInfo( thePackage ) );
			}			
		}
	}

	private IRPDiagram getExistingOrCreateNewPackageIndexDiagramFor(
			IRPPackage theDiagramOwner ){

		IRPDiagram theDiagram = getExistingPackageIndexDiagramFor( theDiagramOwner );

		if( theDiagram == null ) {

			String theProposedName = _context.PACKAGE_DIAGRAM_INDEX_PREFIX + theDiagramOwner.getName();
			String theUniqueName = _context.determineUniqueNameBasedOn( theProposedName, "ObjectModelDiagram", theDiagramOwner );

			theDiagram = theDiagramOwner.addObjectModelDiagram( theUniqueName );
			theDiagram.setStereotype( _pkgIndexDiagramStereotype );
			
			IRPTag theTag = (IRPTag) theDiagram.addNewAggr( "Tag", AUTO_CREATED );
			
			DateFormat dateFormat = new SimpleDateFormat( "yyyy/MM/dd HH:mm:ss" );
			Date date = new Date();
			theTag.setValue( dateFormat.format( date ) );
		}

		return theDiagram;
	}
	
	private IRPDiagram getExistingPackageIndexDiagramFor(
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
		}

		return theDiagram;
	}
	
	private List<IRPDiagram> getNestedPackageDiagrams(
			IRPPackage forPkg ){

		List<IRPDiagram> theAutoDrawnDiagrams = new ArrayList<>();

		@SuppressWarnings("unchecked")
		List<IRPDiagram> theCandidates = forPkg.getObjectModelDiagrams().toList();

		for( IRPDiagram theCandidate : theCandidates ){
			
			if( _context.hasStereotypeCalled( 
					_context.PACKAGE_DIAGRAM_INDEX, theCandidate ) ) {
				
				IRPTag theTag = theCandidate.getTag( AUTO_CREATED );
				
				if( theTag != null ) {
					_context.info( "Found existing " + _context.elInfo( theCandidate ) + " with " + 
							_context.elInfo( theTag ) + " " + theTag.getValue() );
					theAutoDrawnDiagrams.add( theCandidate );
				}				
			}
		}

		return theAutoDrawnDiagrams;
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
