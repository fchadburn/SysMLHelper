package com.mbsetraining.sysmlhelper.dependencyhelper;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import javax.swing.JOptionPane;

import com.mbsetraining.sysmlhelper.common.BaseContext;
import com.mbsetraining.sysmlhelper.common.GraphElInfo;
import com.mbsetraining.sysmlhelper.common.UserInterfaceHelper;
import com.mbsetraining.sysmlhelper.executablembse.ExecutableMBSE_Context;
import com.mbsetraining.sysmlhelper.graphelementhelpers.GraphNodeResizer;
import com.telelogic.rhapsody.core.*;

public class DependencySelector {

	private static final String ALL_CHOICE = "<all>";
	private static final String NONE_CHOICE = "<no stereotype>";

	BaseContext _context;

	public DependencySelector(
			BaseContext theContext ) {

		_context = theContext;
	}

	// For testing only
	public static void main(String[] args) {

		IRPApplication theRhpApp = RhapsodyAppServer.getActiveRhapsodyApplication();
		ExecutableMBSE_Context context = new ExecutableMBSE_Context( theRhpApp.getApplicationConnectionString() );
		DependencySelector theSelector = new DependencySelector( context );
		List<IRPModelElement> theModelEls = context.getSelectedElements();
		theSelector.selectDependentElementsFor( theModelEls );
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

	public void selectDependsOnElementsFor( 
			List<IRPModelElement> theModelEls ){

		List<String> theStereotypeNames = getStereotypeNamesForOutgoingDependencies( theModelEls );

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

	private List<String> getStereotypeNamesForOutgoingDependencies(
			List<IRPModelElement> theModelEls ){

		List<String> theStereotypeNames = new ArrayList<>();

		for( IRPModelElement theModelEl : theModelEls ){

			@SuppressWarnings("unchecked")
			List<IRPDependency> theModelElDependencies = 
			theModelEl.getNestedElementsByMetaClass( "Dependency", 0 ).toList();

			for( IRPDependency theModelElDependency : theModelElDependencies ){
				harvestStereotypeNamesFor( theModelElDependency, theStereotypeNames );
			}
		}
		return theStereotypeNames;
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

	private void addToDependsOnElementsFor(
			List<IRPModelElement> theModelEls, 
			Set<IRPModelElement> theDependsOnEls,
			String theStereotypeName ){

		for( IRPModelElement theModelEl : theModelEls ){
			theDependsOnEls.addAll( getDependsOnElementsFor( theModelEl, theStereotypeName ) );
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

	private void harvestStereotypeNamesFor(
			IRPDependency theDependency,
			List<String> theStereotypeNames ){

		@SuppressWarnings("unchecked")
		List<IRPStereotype> theStereotypes = theDependency.getStereotypes().toList();

		if( theStereotypes.isEmpty() && 
				!theStereotypeNames.contains( NONE_CHOICE ) ) {
			theStereotypeNames.add( NONE_CHOICE );
		}

		for( IRPStereotype theStereotype : theStereotypes ){

			String theStereotypeName = theStereotype.getName();

			if( !theStereotypeNames.contains( theStereotypeName ) ){
				theStereotypeNames.add( theStereotypeName );
			}
		}
	}

	private void populateDependsOnElementsFor(
			IRPGraphElement theGraphEl,
			String theStereotypeName ){

		IRPModelElement theModelEl = theGraphEl.getModelObject();
		Set<IRPModelElement> theDependsOnEls = getDependsOnElementsFor( theModelEl, theStereotypeName );

		populateElsNotOnDiagram( theGraphEl, theDependsOnEls );
	}

	private void populateDependentElementsFor(
			IRPGraphElement theGraphEl,
			String theStereotypeName ){

		IRPModelElement theModelEl = theGraphEl.getModelObject();
		Set<IRPModelElement> theDependentEls = getDependentElementsFor( theModelEl, theStereotypeName );

		populateElsNotOnDiagram( theGraphEl, theDependentEls );
	}

	private void populateElsNotOnDiagram(
			IRPGraphElement theGraphEl, 
			Set<IRPModelElement> theCandidateEls ){

		IRPDiagram theDiagram = theGraphEl.getDiagram();

		GraphElInfo theNodeInfo = new GraphElInfo( theGraphEl, _context );

		List<IRPModelElement> theMissingEls = getElsNotOnDiagram( theDiagram, theCandidateEls );

		int xOffset = 200;
		int yOffset = 200;
		int xNudge = 20;
		int yNudge = 20;
		int x = theNodeInfo.getMidX() + xOffset;
		int y = theNodeInfo.getMidY() + yOffset;

		IRPCollection theCollection = _context.createNewCollection();
		theCollection.addGraphicalItem( theGraphEl );

		for( IRPModelElement theMissingEl : theMissingEls ){

			IRPGraphNode theGraphNode = null;

			try {
				theGraphNode = theDiagram.addNewNodeForElement( theMissingEl, x, y, 50, 50 );

			} catch (Exception e) {
				_context.warning( "Rhapsody is unable to populate " + _context.elInfo( theMissingEl ) + 
						" on " + _context.elInfo( theDiagram ) );
			}

			if( theGraphNode != null ){

				GraphNodeResizer theResizer = new GraphNodeResizer( theGraphNode, _context );
				theResizer.performResizing();

				theCollection.addGraphicalItem( theGraphNode );

				x = x + xNudge;
				y = y + yNudge;
			}
		}

		theDiagram.completeRelations( theCollection, 1 );
	}

	private List<IRPModelElement> getElsNotOnDiagram(
			IRPDiagram theDiagram, 
			Set<IRPModelElement> theDependsOnEls ){

		List<IRPModelElement> theMissingEls = new ArrayList<>();

		for( IRPModelElement theDependsOnEl : theDependsOnEls ){

			@SuppressWarnings("unchecked")
			List<IRPGraphElement> theGraphEls = theDiagram.getCorrespondingGraphicElements( theDependsOnEl ).toList();

			if( theGraphEls.isEmpty() ) {
				theMissingEls.add( theDependsOnEl );
			}
		}

		return theMissingEls;
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

	private void multiSelectElementsInBrowser(
			Set<IRPModelElement> theEls,
			boolean withInfoDialog ){

		IRPApplication theRhpApp = _context.get_rhpApp();
		theRhpApp.refreshAllViews();

		IRPCollection theEmptyCollection = theRhpApp.createNewCollection();
		theRhpApp.selectGraphElements( theEmptyCollection );

		IRPCollection theRhpCollection = theRhpApp.createNewCollection();

		for( IRPModelElement theEl : theEls ){
			theEl.highLightElement();
			theRhpCollection.addItem( theEl );
		}

		theRhpApp.refreshAllViews();

		int theCount = theRhpCollection.getCount();

		if( theCount > 0 ){

			theRhpApp.selectModelElements( 
					theRhpCollection );

			if( withInfoDialog ){

				String theMsg = theCount + " elements will be selected in the browser: \n";

				int count = 0;

				for( Iterator<IRPModelElement> iterator = theEls.iterator(); iterator.hasNext(); ){

					IRPModelElement theEl = (IRPModelElement) iterator.next();

					String theElementInfo = _context.elInfo( theEl );

					int length = theElementInfo.length();

					if( length > 70 ){
						theMsg += theElementInfo.substring(0, 70) + "... ";

					} else {
						theMsg += theElementInfo + " ";
					}

					if( iterator.hasNext() ){
						theMsg += "\n";
					}

					count++;

					if( count > 10 ){
						theMsg += "(... more)";
						break;
					}
				}

				theMsg += "\n";

				UserInterfaceHelper.showInformationDialog( theMsg );
			}

		} else if( withInfoDialog ) {

			String theMsg = 
					"There were no elements selected.";

			UserInterfaceHelper.showInformationDialog( theMsg );
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

