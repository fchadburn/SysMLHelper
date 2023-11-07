package com.mbsetraining.sysmlhelper.dependencyhelper;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import javax.swing.JOptionPane;

import com.mbsetraining.sysmlhelper.common.BaseContext;
import com.mbsetraining.sysmlhelper.common.UserInterfaceHelper;
import com.mbsetraining.sysmlhelper.executablembse.ExecutableMBSE_Context;
import com.telelogic.rhapsody.core.*;

public class PopulateDependsOnElements extends PopulateRelationsHelper{

	public PopulateDependsOnElements(
			BaseContext theContext ) {

		_context = theContext;
	}

	// For testing only
	public static void main(String[] args) {

		IRPApplication theRhpApp = RhapsodyAppServer.getActiveRhapsodyApplication();
		ExecutableMBSE_Context context = new ExecutableMBSE_Context( theRhpApp.getApplicationConnectionString() );
		PopulateDependsOnElements theSelector = new PopulateDependsOnElements( context );
		List<IRPModelElement> theModelEls = context.getSelectedElements();
		theSelector.selectDependsOnElementsFor( theModelEls );
	}

	public void populateDependsOnElementsFor( 
			IRPGraphElement theGraphEl ){

		if( theGraphEl == null ) {

			UserInterfaceHelper.showWarningDialog( 
					"This only works when you select a graph element on a diagram." );
		} else {
			IRPModelElement theModelEl = theGraphEl.getModelObject();
			IRPDiagram theDiagram = theGraphEl.getDiagram();
			List<String> theStereotypeNames = new ArrayList<>();

			@SuppressWarnings("unchecked")
			List<IRPDependency> theDependencies = theModelEl.getNestedElementsByMetaClass( "Dependency", 0 ).toList();

			for( IRPDependency theDependency : theDependencies ){

				@SuppressWarnings("unchecked")
				List<IRPGraphElement> theGraphEls = 
				theDiagram.getCorrespondingGraphicElements( theDependency ).toList();

				if( theGraphEls.isEmpty() ) {
					harvestStereotypeNamesFor( theDependency, theStereotypeNames );
				}			
			}

			if( theStereotypeNames.isEmpty() ) {

				UserInterfaceHelper.showInformationDialog( 
						"No unpopulated dependencies found for \n" + _context.elInfo( theModelEl ) );

			} else {	
				theStereotypeNames.add( ALL_CHOICE );

				Object[] options = theStereotypeNames.toArray();

				String selection = (String) JOptionPane.showInputDialog(
						null,
						"Choose relation",
						"Populate on diagram, if not present",
						JOptionPane.QUESTION_MESSAGE,
						null,
						options,
						options[0]);

				populateDependsOnElementsFor( theGraphEl, selection );
			}
		}
	}

	public void selectDependsOnElementsFor( 
			List<IRPModelElement> theModelEls ){

		List<String> theStereotypeNames = getStereotypeNamesFor( theModelEls, "Dependency" );

		if( theStereotypeNames.isEmpty() ) {

			UserInterfaceHelper.showInformationDialog( 
					"No depends on elements found for the " + theModelEls.size() + " selected elements" );
		} else {	
			theStereotypeNames.add( ALL_CHOICE );

			Object[] options = theStereotypeNames.toArray();

			String selection = (String) JOptionPane.showInputDialog(
					null,
					"Choose relation",
					"Select depends on in browser",
					JOptionPane.QUESTION_MESSAGE,
					null,
					options,
					options[0]);

			Set<IRPModelElement> theDependsOnEls = new HashSet<>();
			addToDependsOnElementsFor( theModelEls, theDependsOnEls, selection );
			multiSelectElementsInBrowser( theDependsOnEls, true );
		}		
	}

	private void addToDependsOnElementsFor(
			List<IRPModelElement> theModelEls, 
			Set<IRPModelElement> theDependsOnEls,
			String theStereotypeName ){

		for( IRPModelElement theModelEl : theModelEls ){
			theDependsOnEls.addAll( getDependsOnElementsFor( theModelEl, theStereotypeName ) );
		}
	}

	private void populateDependsOnElementsFor(
			IRPGraphElement theGraphEl,
			String theStereotypeName ){

		IRPModelElement theModelEl = theGraphEl.getModelObject();
		Set<IRPModelElement> theDependsOnEls = getDependsOnElementsFor( theModelEl, theStereotypeName );

		populateElsNotOnDiagram( theGraphEl, theDependsOnEls );
	}
	
	private Set<IRPModelElement> getDependsOnElementsFor(
			IRPModelElement theCandidateEl,
			String theStereotypeName) {

		Set<IRPModelElement> theEls = new HashSet<IRPModelElement>();

		//_context.debug( _context.elInfo( theCandidateEl ) + " owned by " + 
		//		_context.elInfo( theCandidateEl.getOwner() ) + 
		//		" was selected for DependsOn analysis");

		@SuppressWarnings("unchecked")
		List<IRPDependency> theDependencies = theCandidateEl.getNestedElementsByMetaClass( "Dependency", 0 ).toList();

		for( IRPDependency theDependency : theDependencies ){
			addDependsOnEls( theEls, (IRPDependency) theDependency, theStereotypeName );
		}

		if( theCandidateEl instanceof IRPDependency ){
			addDependsOnEls( theEls, (IRPDependency) theCandidateEl, theStereotypeName );
		}

		return theEls;
	}

	private void addDependsOnEls( 
			Set<IRPModelElement> toTheEls, 
			IRPDependency forDependency,
			String theStereotypeName ){

		if( theStereotypeName.equals( ALL_CHOICE ) ||
				( theStereotypeName.equals( NONE_CHOICE ) && 
						forDependency.getStereotypes().toList().isEmpty() ) || 
				_context.hasStereotypeCalled( theStereotypeName, forDependency ) ){

			IRPModelElement theDependsOn = forDependency.getDependsOn();

			if( theDependsOn != null ){
				toTheEls.add( theDependsOn );
			}
		}
	}
}

/**
 * Copyright (C) 2017-2023  MBSE Training and Consulting Limited (www.executablembse.com)

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

