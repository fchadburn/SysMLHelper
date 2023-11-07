package com.mbsetraining.sysmlhelper.dependencyhelper;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import com.mbsetraining.sysmlhelper.common.BaseContext;
import com.mbsetraining.sysmlhelper.common.GraphElInfo;
import com.mbsetraining.sysmlhelper.common.UserInterfaceHelper;
import com.mbsetraining.sysmlhelper.graphelementhelpers.GraphNodeResizer;
import com.telelogic.rhapsody.core.IRPApplication;
import com.telelogic.rhapsody.core.IRPCollection;
import com.telelogic.rhapsody.core.IRPDependency;
import com.telelogic.rhapsody.core.IRPDiagram;
import com.telelogic.rhapsody.core.IRPGraphElement;
import com.telelogic.rhapsody.core.IRPGraphNode;
import com.telelogic.rhapsody.core.IRPModelElement;
import com.telelogic.rhapsody.core.IRPStereotype;

public class PopulateRelationsHelper {

	protected static final String ALL_CHOICE = "<all>";
	protected static final String NONE_CHOICE = "<no stereotype>";

	protected BaseContext _context;
	
	protected List<String> getStereotypeNamesFor(
			List<IRPModelElement> theModelEls,
			String withMetaClass ){

		List<String> theStereotypeNames = new ArrayList<>();

		for( IRPModelElement theModelEl : theModelEls ){

			@SuppressWarnings("unchecked")
			List<IRPModelElement> theNestedEls = 
			theModelEl.getNestedElementsByMetaClass( withMetaClass, 0 ).toList();

			for( IRPModelElement theNestedEl : theNestedEls ){
				harvestStereotypeNamesFor( theNestedEl, theStereotypeNames );
			}
		}
		
		return theStereotypeNames;
	}
	
	protected void harvestStereotypeNamesFor(
			IRPModelElement theEl,
			List<String> theStereotypeNames ){

		@SuppressWarnings("unchecked")
		List<IRPStereotype> theStereotypes = theEl.getStereotypes().toList();

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
	
	protected void multiSelectElementsInBrowser(
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
	
	protected void populateElsNotOnDiagram(
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
