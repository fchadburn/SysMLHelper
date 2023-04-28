package com.mbsetraining.sysmlhelper.common;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import javax.swing.JOptionPane;

import com.mbsetraining.sysmlhelper.executablembse.ExecutableMBSE_Context;
import com.mbsetraining.sysmlhelper.graphelementhelpers.GraphNodeResizer;
import com.telelogic.rhapsody.core.*;

public class DependencySelector {

	private static final String ALL_CHOICE = "<all>";
	private static final String NONE_CHOICE = "<none>";
	
	BaseContext _context;

	public DependencySelector(
			BaseContext theContext ) {

		_context = theContext;
	}

	public static void main(String[] args) {

		IRPApplication theRhpApp = RhapsodyAppServer.getActiveRhapsodyApplication();
		ExecutableMBSE_Context context = new ExecutableMBSE_Context( theRhpApp.getApplicationConnectionString() );
		DependencySelector theSelector = new DependencySelector( context );

		List<IRPGraphElement> theGraphEls = context.getSelectedGraphElements();

		for( IRPGraphElement theGraphEl : theGraphEls ){
			theSelector.populateDependsOnElementsFor( theGraphEl );
		}
	}
	
	public void populateDependsOnElementsFor( 
			IRPGraphElement theGraphEl ){
		
		IRPModelElement theModelEl = theGraphEl.getModelObject();
		IRPDiagram theDiagram = theGraphEl.getDiagram();
		List<String> theStereotypeNames = new ArrayList<>();

		@SuppressWarnings("unchecked")
		List<IRPDependency> theDependencies = theModelEl.getNestedElementsByMetaClass( "Dependency", 0 ).toList();
		
		List<IRPDependency> theMissingDependencies = new ArrayList<>();
		
		for( IRPDependency theDependency : theDependencies ){
			
			@SuppressWarnings("unchecked")
			List<IRPGraphElement> theGraphEls = 
					theDiagram.getCorrespondingGraphicElements( theDependency ).toList();
			
			if( theGraphEls.isEmpty() ) {
				
				theMissingDependencies.add( theDependency );

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
			
			if( selection.equals( ALL_CHOICE ) ) {
			
				for( String theStereotypeName : theStereotypeNames ) {
					populateDependsOnElementsFor( theGraphEl, theStereotypeName );
				}
			} else if( selection.equals( NONE_CHOICE ) ){
				
				populateDependsOnElementsFor( theGraphEl, null );
			} else {				
				populateDependsOnElementsFor( theGraphEl, selection );
			}
		}		
	}

	public void populateDependsOnElementsFor(
			IRPGraphElement theGraphEl,
			String theStereotypeName ){

		IRPModelElement theModelEl = theGraphEl.getModelObject();
		IRPDiagram theDiagram = theGraphEl.getDiagram();
		Set<IRPModelElement> theDependsOnEls = getDependsOnElementsFor( theModelEl, theStereotypeName );
	
		GraphElInfo theNodeInfo = new GraphElInfo( theGraphEl, _context );
		
		List<IRPModelElement> theMissingEls = getElsNotOnDiagram( theDiagram, theDependsOnEls );
		
		int xOffset = 200;
		int yOffset = 200;
		int xNudge = 20;
		int yNudge = 20;
		int x = theNodeInfo.getMidX() + xOffset;
		int y = theNodeInfo.getMidY() + yOffset;
		
		IRPCollection theCollection = _context.createNewCollection();
		theCollection.addGraphicalItem( theGraphEl );
		
		for( IRPModelElement theMissingEl : theMissingEls ){
					
			IRPGraphNode theGraphNode = theDiagram.addNewNodeForElement( theMissingEl, x, y, 50, 50 );
			
			GraphNodeResizer theResizer = new GraphNodeResizer( theGraphNode, _context );
			theResizer.performResizing();
			
			theCollection.addGraphicalItem( theGraphNode );
			
			x = x + xNudge;
			y = y + yNudge;
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

	public void selectDependsOnElementsFor(
			Set<IRPModelElement> theCandidateEls,
			String theStereotypeName ){

		Set<IRPModelElement> theElsToHighlight = getDependsOnElementsFor( theCandidateEls, theStereotypeName );

		multiSelectElementsInBrowser(
				theElsToHighlight, true );
	}

	private Set<IRPModelElement> getDependsOnElementsFor(
			Set<IRPModelElement> theCandidateEls,
			String theStereotypeName) {

		Set<IRPModelElement> theEls = 
				new HashSet<IRPModelElement>();

		for( IRPModelElement theCandidateEl : theCandidateEls ){

			Set<IRPModelElement> theDependsOnElements = getDependsOnElementsFor( theCandidateEl, theStereotypeName );
			theEls.addAll( theDependsOnElements );					
		}
		
		return theEls;
	}

	private Set<IRPModelElement> getDependsOnElementsFor(
			IRPModelElement theCandidateEl,
			String theStereotypeName) {

		Set<IRPModelElement> theEls = 
				new HashSet<IRPModelElement>();

		_context.debug( _context.elInfo( theCandidateEl ) + " owned by " + 
				_context.elInfo( theCandidateEl.getOwner() ) + 
				" was selected for DependsOn analysis");

		@SuppressWarnings("unchecked")
		List<IRPModelElement> theNestedElDependencies = 
		theCandidateEl.getNestedElementsByMetaClass( "Dependency", 1 ).toList();

		for( IRPModelElement theNestElDependency : theNestedElDependencies ){

			if( theStereotypeName == null || 
					_context.hasStereotypeCalled(
							theStereotypeName, theNestElDependency ) ){

				IRPModelElement theDependsOn = 
						((IRPDependency) theNestElDependency).getDependsOn();

				if( theDependsOn != null ){
					theEls.add( theDependsOn );
				}
			}
		}

		if( theCandidateEl instanceof IRPDependency ){

			if( theStereotypeName == null || 
					_context.hasStereotypeCalled(
							theStereotypeName, theCandidateEl ) ){

				IRPModelElement theDependsOn = 
						((IRPDependency) theCandidateEl).getDependsOn();

				if( theDependsOn != null ){
					theEls.add( theDependsOn );
				}
			}
		}

		return theEls;
	}

	public void selectDependentElementsFor(
			Set<IRPModelElement> theCandidateEls,
			String theStereotypeName ){

		Set<IRPModelElement> theElsToHighlight = 
				new HashSet<IRPModelElement>();

		for( IRPModelElement theCandidateEl : theCandidateEls ){

			_context.debug( _context.elInfo( theCandidateEl ) + " was selected for Dependent analysis" );

			@SuppressWarnings("unchecked")
			List<IRPModelElement> theReferences = 
			theCandidateEl.getReferences().toList();

			for( IRPModelElement theReference : theReferences ){

				if( theReference instanceof IRPDependency &&
						( theStereotypeName == null || 
						_context.hasStereotypeCalled(
								theStereotypeName, theReference ) ) ){

					IRPModelElement theDependent = 
							((IRPDependency)theReference).getDependent();

					if( theDependent != null ){
						theElsToHighlight.add( theDependent );
					}
				}
			}

			if( theCandidateEl instanceof IRPDependency &&
					( theStereotypeName == null || 
					_context.hasStereotypeCalled(
							theStereotypeName, theCandidateEl ) )){

				IRPModelElement theDependent = 
						((IRPDependency) theCandidateEl).getDependent();

				if( theDependent != null ){
					theElsToHighlight.add( theDependent );
				}
			}
		}

		multiSelectElementsInBrowser(
				theElsToHighlight, true );
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

