package com.mbsetraining.sysmlhelper.common;

import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import com.telelogic.rhapsody.core.*;

public class DependencySelector {
	
	ConfigurationSettings _context;
	
	public DependencySelector(
			ConfigurationSettings theContext ) {
		
		_context = theContext;
	}
	
	public void selectDependsOnElementsFor(
			Set<IRPModelElement> theCandidateEls,
			String theStereotypeName ){
		
		Set<IRPModelElement> theElsToHighlight = 
				new HashSet<IRPModelElement>();
		
		for( IRPModelElement theCandidateEl : theCandidateEls ){
			
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
						theElsToHighlight.add( theDependsOn );
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
						theElsToHighlight.add( theDependsOn );
					}
				}
			}
		}

		multiSelectElementsInBrowser(
				theElsToHighlight, true );
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
 * Copyright (C) 2017-2021  MBSE Training and Consulting Limited (www.executablembse.com)

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

