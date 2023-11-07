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

public class PopulateDependentElements extends PopulateRelationsHelper{

	public PopulateDependentElements(
			BaseContext theContext ) {

		_context = theContext;
	}

	// For testing only
	public static void main(String[] args) {

		IRPApplication theRhpApp = RhapsodyAppServer.getActiveRhapsodyApplication();
		ExecutableMBSE_Context context = new ExecutableMBSE_Context( theRhpApp.getApplicationConnectionString() );
		PopulateDependentElements theSelector = new PopulateDependentElements( context );
		List<IRPModelElement> theModelEls = context.getSelectedElements();
		theSelector.selectDependentElementsFor( theModelEls );
	}

	public void populateDependentElementsFor( 
			IRPGraphElement theGraphEl ){

		if( theGraphEl == null ) {
			
			UserInterfaceHelper.showWarningDialog( 
					"This only works when you select a graph element on a diagram." );
			
		} else {
			
			IRPModelElement theModelEl = theGraphEl.getModelObject();
			IRPDiagram theDiagram = theGraphEl.getDiagram();
			List<String> theStereotypeNames = new ArrayList<>();

			@SuppressWarnings("unchecked")
			List<IRPModelElement> theReferences = theModelEl.getReferences().toList();

			for( IRPModelElement theReference : theReferences ){

				if( theReference instanceof IRPDependency ) {

					IRPDependency theDependency = (IRPDependency) theReference;

					@SuppressWarnings("unchecked")
					List<IRPGraphElement> theGraphEls = 
					theDiagram.getCorrespondingGraphicElements( theReference ).toList();

					if( theGraphEls.isEmpty() ) {
						harvestStereotypeNamesFor( theDependency, theStereotypeNames );					
					}
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

				populateDependentElementsFor( theGraphEl, selection );
			}
		}
	}

	public void selectDependentElementsFor( 
			List<IRPModelElement> theModelEls ){

		List<String> theStereotypeNames = new ArrayList<>();

		for( IRPModelElement theModelEl : theModelEls ){

			@SuppressWarnings("unchecked")
			List<IRPModelElement> theReferences = theModelEl.getReferences().toList();

			for( IRPModelElement theReference : theReferences ){

				if( theReference instanceof IRPDependency ) {
					harvestStereotypeNamesFor( (IRPDependency) theReference, theStereotypeNames );					
				}
			}
		}

		if( theStereotypeNames.isEmpty() ) {

			UserInterfaceHelper.showInformationDialog( 
					"No dependent elements found for the " + theModelEls.size() + " selected elements" );
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

			Set<IRPModelElement> theDependentEls = new HashSet<>();
			addToDependentElementsFor( theModelEls, theDependentEls, selection );
			multiSelectElementsInBrowser( theDependentEls, true );
		}		
	}

	private void addToDependentElementsFor(
			List<IRPModelElement> theModelEls, 
			Set<IRPModelElement> theDependentEls,
			String theStereotypeName ){

		for( IRPModelElement theModelEl : theModelEls ){
			theDependentEls.addAll( getDependentElementsFor( theModelEl, theStereotypeName ) );
		}
	}

	private void populateDependentElementsFor(
			IRPGraphElement theGraphEl,
			String theStereotypeName ){

		IRPModelElement theModelEl = theGraphEl.getModelObject();
		Set<IRPModelElement> theDependentEls = getDependentElementsFor( theModelEl, theStereotypeName );

		populateElsNotOnDiagram( theGraphEl, theDependentEls );
	}

	private Set<IRPModelElement> getDependentElementsFor(
			IRPModelElement theCandidateEl,
			String theStereotypeName) {

		Set<IRPModelElement> theEls = new HashSet<IRPModelElement>();

		//_context.debug( _context.elInfo( theCandidateEl ) + " owned by " + 
		//		_context.elInfo( theCandidateEl.getOwner() ) + 
		//		" was selected for Dependent analysis");

		@SuppressWarnings("unchecked")
		List<IRPModelElement> theReferences = theCandidateEl.getReferences().toList();

		for( IRPModelElement theReference : theReferences ){

			if( theReference instanceof IRPDependency ){
				addDependentEls( theEls, (IRPDependency) theReference, theStereotypeName );
			}
		}

		if( theCandidateEl instanceof IRPDependency ){
			addDependentEls( theEls, (IRPDependency) theCandidateEl, theStereotypeName );
		}

		return theEls;
	}

	private void addDependentEls( 
			Set<IRPModelElement> toTheEls, 
			IRPDependency forDependency,
			String theStereotypeName ){

		if( theStereotypeName.equals( ALL_CHOICE ) ||
				( theStereotypeName.equals( NONE_CHOICE ) && 
						forDependency.getStereotypes().toList().isEmpty() ) || 
				_context.hasStereotypeCalled( theStereotypeName, forDependency ) ){

			IRPModelElement theDependent = forDependency.getDependent();

			if( theDependent != null ){
				toTheEls.add( theDependent );
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

