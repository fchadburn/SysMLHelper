package com.mbsetraining.sysmlhelper.graphtraversal;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import com.mbsetraining.sysmlhelper.common.BaseContext;
import com.telelogic.rhapsody.core.*;

public class GraphPaths extends ArrayList<GraphPath>{

	/**
	 * 
	 */
	private static final long serialVersionUID = 5176021867376333578L;

	BaseContext _context;

	public void createPathVisualizationElements(
			String withTheName,
			IRPPackage underThePackage ){
		
		IRPModelElement theQueryEl = findOrAddElement( withTheName, "TableLayout", underThePackage );
		theQueryEl.changeTo( "Query" );
		
		/*
		IRPStereotype theQueryStereotype = _context.getStereotypeCalled( "Query", theQueryEl );
		_context.info( "Found " + _context.elInfo( theQueryStereotype ) );
		
		IRPTag theRelRef_StereotypeTag = theQueryStereotype.getTag( "RelRef_Stereotype" );
		_context.info( "Found " + _context.elInfo( theRelRef_StereotypeTag ) );
		theQueryEl.setTagElementValue( theRelRef_StereotypeTag, theDependencyStereotypeEl );
		
		IRPTag theRelRef_Metatype = theQueryStereotype.getTag( "RelRef_Metatype" );
		theQueryEl.setTagValue( theRelRef_Metatype, "Dependency" );

		IRPTag theRelRef_How_Related = theQueryStereotype.getTag( "RelRef_How_Related" );
		theQueryEl.setTagValue( theRelRef_How_Related, "IncomingRelations" );
		
		IRPTag theUnresolved = theQueryStereotype.getTag( "Unresolved" );
		theQueryEl.setTagValue( theUnresolved, "ShowUnresolved" );
		*/
		
		IRPModelElement theCustomViewEl = findOrAddElement( withTheName, "Package", underThePackage );
		theCustomViewEl.changeTo( "CustomView" );
				
		/*
		IRPTag theCriteria = theCustomViewEl.getTag( "Criteria" );
		
		if( theCriteria == null ){
			theCriteria = (IRPTag) theCustomViewEl.addNewAggr( "Tag", "Criteria" );
		}
		theCustomViewEl.setTagValue( theCriteria, withTheName );
		
		IRPTag theCriteriaType = theCustomViewEl.getTag( "CriteriaType" );

		if( theCriteriaType == null ){			
			theCriteriaType = (IRPTag) theCustomViewEl.addNewAggr( "Tag", "CriteriaType" );
		}
		
		theCustomViewEl.setTagValue( theCriteriaType, "Queries" );*/
	}

	private IRPModelElement findOrAddElement( 
			String withTheName, 
			String andMetaclass, 
			IRPPackage toThePackage) {
		
		IRPModelElement theEl = toThePackage.findAllByName( withTheName, andMetaclass );
		
		if( theEl != null ){
			
			_context.info( "Found " + _context.elInfo( theEl ) );
		} else {
			
			theEl = toThePackage.addNewAggr( andMetaclass, withTheName );
		}
		
		return theEl;
	}
	
	public GraphPaths(
			BaseContext context ){
		
		_context = context;
	}
	
	public void dumpInfo(){
			
		_context.info( "" );
		_context.info( "***************************************" );
		_context.info( this.size() + " graph paths found:" );
		_context.info( "" );

		if( !this.isEmpty() ){

			Iterator<GraphPath> iterator = this.iterator();
			
			while( iterator.hasNext() ){
				
				GraphPath theGraphPath = iterator.next();
				theGraphPath.dumpInfo();
				
				if( iterator.hasNext() ){
					_context.info("");
				}
			}
		}
		
		_context.info( "" );
		_context.info( "***************************************" );

	}
	
	public void createDependenciesAndPathVisualization(
			String withClassName,
			IRPPackage underThePackage ){
		
		if( this.isEmpty() ){
			_context.warning( "Unable to createDependencies as there are no graph paths " );
		} else {
						
			IRPModelElement theClass = getOrCreateNewDependencyOwningClass( 
					underThePackage, withClassName, true );
			
			theClass.highLightElement();
			
			int count = 0;
			
			Iterator<GraphPath> iterator = this.iterator();
			
			while( iterator.hasNext() ){
				
				count++;

				GraphPath thePath = (GraphPath) iterator.next();
				
				String thePathName = withClassName + String.format( "%03d", count );
							
				_context.info( "Adding dependencies for " + thePathName );

				IRPStereotype theDependencyStereotype = null;
				
				IRPModelElement theDependencyStereotypeEl = findOrAddElement( thePathName, "Stereotype", underThePackage );

				if( theDependencyStereotypeEl instanceof IRPStereotype ){
					theDependencyStereotype = (IRPStereotype)theDependencyStereotypeEl;
					theDependencyStereotype.addMetaClass( "Dependency" );
				}
				
				thePath.createDependencies( theClass, theDependencyStereotype );			
				
				createPathVisualizationElements( thePathName, underThePackage );	
			}
		}
	}

	private IRPModelElement getOrCreateNewDependencyOwningClass(
			IRPPackage underThePackage,
			String withTheName,
			boolean withDependencyCleanupWanted ){
		
		IRPModelElement theClass = underThePackage.findAllByName( withTheName, "Class" );
		
		if( theClass instanceof IRPClass ){
			
			@SuppressWarnings("unchecked")
			List<IRPModelElement> theDependencies = theClass.getDependencies().toList();
			
			if( !theDependencies.isEmpty() ){
				
				if( withDependencyCleanupWanted ){
					deleteFromModel( theDependencies );
				}
			}
		} else {
			theClass = underThePackage.addClass( withTheName );
		}
		
		return theClass;
	}

	private void deleteFromModel( 
			List<IRPModelElement> theModelEls ){
		
		Iterator<IRPModelElement> iterator = theModelEls.iterator();
		
		while( iterator.hasNext() ){
			
			IRPModelElement theEl = (IRPModelElement) iterator.next();
			theEl.deleteFromProject();
		}
	}
}

/**
 * Copyright (C) 2022  MBSE Training and Consulting Limited (www.executablembse.com)

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
