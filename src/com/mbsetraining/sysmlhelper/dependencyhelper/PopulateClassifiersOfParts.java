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

public class PopulateClassifiersOfParts extends PopulateRelationsHelper{

	public PopulateClassifiersOfParts(
			BaseContext theContext ) {

		_context = theContext;
	}

	// For testing only
	public static void main(String[] args) {

		IRPApplication theRhpApp = RhapsodyAppServer.getActiveRhapsodyApplication();
		ExecutableMBSE_Context context = new ExecutableMBSE_Context( theRhpApp.getApplicationConnectionString() );
		PopulateClassifiersOfParts theSelector = new PopulateClassifiersOfParts( context );

		IRPModelElement theSelectedEl = context.getSelectedElement( true );
		List<IRPModelElement> theSelectedEls = context.getSelectedElements();
		List<IRPGraphElement> theSelectedGraphEls = context.getSelectedGraphElements();

		if( ( theSelectedEl instanceof IRPObjectModelDiagram ||
				theSelectedEl instanceof IRPStructureDiagram ) &&
				theSelectedEl.getOwner() instanceof IRPClassifier ){

			theSelectedEls.add( theSelectedEl.getOwner() );
		}

		Set<IRPModelElement> theCombinedSet = 
				context.getSetOfElementsFromCombiningThe(
						theSelectedEls, theSelectedGraphEls );

		theSelector.selectClassifiersFor( new ArrayList<>( theCombinedSet ) );
	}

	public void populateClassifiersFor( 
			IRPGraphElement theGraphEl ){

		if( theGraphEl == null ) {

			UserInterfaceHelper.showWarningDialog( 
					"This only works when you select a single graph element on a diagram." );

		} else {
			
			IRPModelElement theModelEl = theGraphEl.getModelObject();
			
			List<String> theStereotypeNames = getStereotypeNamesFor( theGraphEl );

			if( theStereotypeNames.isEmpty() ) {

				UserInterfaceHelper.showInformationDialog( 
						"No unpopulated classifiers found for \n" + _context.elInfo( theModelEl ) );

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

				populateClassifierElementsFor( theGraphEl, selection );
			}
		}
	}

	private List<String> getStereotypeNamesFor(
			IRPGraphElement theGraphEl ){
		
		IRPModelElement theModelEl = theGraphEl.getModelObject();
		IRPDiagram theDiagram = theGraphEl.getDiagram();
		List<String> theStereotypeNames = new ArrayList<>();

		@SuppressWarnings("unchecked")
		List<IRPInstance> theObjects = theModelEl.getNestedElementsByMetaClass( "Object", 0 ).toList();

		for( IRPInstance theObject : theObjects ){
			
			// Ignore typeless parts, e.g., StartUsage
			if( theObject.isTypelessObject() == 0 &&
					theObject.getMetaClass().equals( "Object" ) ){
				
				@SuppressWarnings("unchecked")
				List<IRPGraphElement> theExistingGraphEls = theDiagram.getCorrespondingGraphicElements( theObject ).toList();

				if( theExistingGraphEls.isEmpty() ) {
					harvestStereotypeNamesFor( theObject, theStereotypeNames );
				}	
			}
		}
		
		return theStereotypeNames;
	}

	public void selectClassifiersFor( 
			List<IRPModelElement> theModelEls ){

		List<String> theStereotypeNames = getStereotypeNamesFor( theModelEls, "Object" );

		if( theStereotypeNames.isEmpty() ) {

			UserInterfaceHelper.showInformationDialog( 
					"No parts typed by classifier elements were found for the " + theModelEls.size() + " selected elements" );
		} else {	
			theStereotypeNames.add( ALL_CHOICE );

			Object[] options = theStereotypeNames.toArray();

			String selection = (String) JOptionPane.showInputDialog(
					null,
					"Choose relation",
					"Select classifiers of parts in browser",
					JOptionPane.QUESTION_MESSAGE,
					null,
					options,
					options[0]);

			Set<IRPModelElement> theDependsOnEls = new HashSet<>();
			addToClassifierElementsFor( theModelEls, theDependsOnEls, selection );
			multiSelectElementsInBrowser( theDependsOnEls, true );
		}		
	}

	private void addToClassifierElementsFor(
			List<IRPModelElement> theModelEls, 
			Set<IRPModelElement> theClassifierEls,
			String theStereotypeName ){

		for( IRPModelElement theModelEl : theModelEls ){
			theClassifierEls.addAll( getClassifierElementsFor( theModelEl, theStereotypeName ) );
		}
	}

	private void populateClassifierElementsFor(
			IRPGraphElement theGraphEl,
			String theStereotypeName ){

		IRPModelElement theModelEl = theGraphEl.getModelObject();
		Set<IRPModelElement> theClassifierEls = getClassifierElementsFor( theModelEl, theStereotypeName );

		populateElsNotOnDiagram( theGraphEl, theClassifierEls );
	}

	private Set<IRPModelElement> getClassifierElementsFor(
			IRPModelElement theCandidateEl,
			String theStereotypeName ){

		Set<IRPModelElement> theEls = new HashSet<IRPModelElement>();

		//_context.debug( _context.elInfo( theCandidateEl ) + " owned by " + 
		//		_context.elInfo( theCandidateEl.getOwner() ) + 
		//		" was selected for DependsOn analysis");

		@SuppressWarnings("unchecked")
		List<IRPInstance> theInstances = theCandidateEl.getNestedElementsByMetaClass( "Object", 0 ).toList();

		for( IRPInstance theInstance : theInstances ){
			addClassifierEls( theEls, (IRPInstance) theInstance, theStereotypeName );
		}

		if( theCandidateEl instanceof IRPInstance ){
			addClassifierEls( theEls, (IRPInstance) theCandidateEl, theStereotypeName );
		}

		return theEls;
	}

	private void addClassifierEls( 
			Set<IRPModelElement> toTheEls, 
			IRPInstance forInstance,
			String theStereotypeName ){

		if( forInstance.isTypelessObject() == 0 &&
				forInstance.getMetaClass().equals("Object" ) ) {
			
			if( theStereotypeName.equals( ALL_CHOICE ) ||
					( theStereotypeName.equals( NONE_CHOICE ) && 
							forInstance.getStereotypes().toList().isEmpty() ) || 
					
					_context.hasStereotypeCalled( theStereotypeName, forInstance ) ){

				IRPModelElement theClassifier = forInstance.getOtherClass();

				if( theClassifier != null ){
					toTheEls.add( theClassifier );
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
