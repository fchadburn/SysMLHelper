package com.mbsetraining.sysmlhelper.graphtraversal;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import com.mbsetraining.sysmlhelper.businessvalueplugin.BusinessValue_Context;
import com.telelogic.rhapsody.core.*;

public class GraphPaths extends ArrayList<GraphPath>{

	/**
	 * 
	 */
	private static final long serialVersionUID = 5176021867376333578L;

	protected BusinessValue_Context _context;
	protected String _name;
	protected IRPDiagram _sourceDiagram;
	
	public GraphPaths(
			BusinessValue_Context context,
			String name,
			IRPDiagram sourceDiagram ){
		
		_context = context;
		_name = name;
		_sourceDiagram = sourceDiagram;
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
	
	public IRPPackage createViewStructure(
			String theName,
			IRPPackage rootPackage ) {
		
		String theViewName = _context.VIEW_PREFIX + theName;
		String theViewpointName = _context.VIEWPOINT_PREFIX + theName;
		String theCustomViewExplicitOnlyName = _context.CUSTOMERVIEW_PREFIX + theName;
		String theViewpointDiagramName = _context.VIEWPOINT_DIAGRAM_PREFIX + theName;
		String theQueryExplicitOnlyName = _context.QUERY_PREFIX + theName;

		_context.info( "Creating View with name " + theViewName + 
				" under " + _context.elInfo( rootPackage ) );

		IRPPackage theView = (IRPPackage) rootPackage.addNewAggr( "View", theViewName );
		
		// A standard content ViewStructure stereotype will create the structure with an expected naming to be able to 
		// set up the query and custom view settings (as API doesn't seem to allow this). We will then modify this
		
		_context.applyExistingStereotype( _context.VIEW_STRUCTURE_STEREOTYPE, theView );
		
		IRPModelElement theEl = theView.findNestedElement( _context.QUERYTBD_EXPLICIT_ONLY, "Query" );
		
		if( theEl != null ) {
			// The standard context query has this turned off so that the template doesn't affect model
			//_context.setBoolPropertyValueInRhp(theEl, "Model.Query.ShowInBrowserFilterList", false );
		}
				
		_context.renameNestedElement( _context.VIEWPOINT_TBD, "Class", theView, theViewpointName );
		_context.renameNestedElement( _context.CUSTOMV_TBD_EXPLICIT_ONLY, "Package", theView, theCustomViewExplicitOnlyName );
		_context.renameNestedElement( _context.VIEW_AND_VIEWPOINT_DIAGRAM_TBD, "ObjectModelDiagram", theView, theViewpointDiagramName );
		_context.renameNestedElement( _context.QUERYTBD_EXPLICIT_ONLY, "Query", theView, theQueryExplicitOnlyName );
		_context.renameNestedElement( _context.STEREOTYPE_TBD, "Stereotype", theView, theName );
			
		createDiagramView(theView, (IRPObjectModelDiagram) _sourceDiagram);
		
		return theView;
	}

	private void createDiagramView(
			IRPPackage theView, 
			IRPObjectModelDiagram theSourceDiagram ) {
		
		//IRPModelElement theCustomV = theView.findNestedElement( _context.CUSTOMV_TBD_EXPLICIT_ONLY, "Package" );
		
		//IRPCollection theCollection = _context.createNewCollection();
		//theCollection.setSize(1);
		//theCollection.setModelElement( 0,theCustomV );
		
		//IRPDiagram theDiagramView = theSourceDiagram.createDiagramView( theSourceDiagram.getOwner(), null);
		//theDiagramView.setCustomViews(theCollection);
	}
	
	public IRPPackage createSingleViewStructureUnder(
			IRPPackage theRootPkg ) {
		
		IRPPackage theViewStructure = null;
		
		if( this.isEmpty() ){
			
			_context.warning( "Unable to createSingleViewStructureUnder as there are no graph paths " );
			
		} else {
								
			theViewStructure = createViewStructure( _name, theRootPkg );

			IRPStereotype theDependencyStereotype = null;

			IRPModelElement theDependencyStereotypeEl = _context.findOrAddElement( _name, "Stereotype", theViewStructure );

			if( theDependencyStereotypeEl instanceof IRPStereotype ){
				
				theDependencyStereotype = (IRPStereotype)theDependencyStereotypeEl;
				theDependencyStereotype.addMetaClass( "Dependency" );
			} else {
				_context.error( "Unable to find stereotype with name " + _name );
			}
						
			IRPStereotype theImportStereotype = _context.getStereotypeWith( "import" );

			Iterator<GraphPath> iterator = this.iterator();
			
			while( iterator.hasNext() ){
				
				GraphPath theGraphPath = (GraphPath) iterator.next();
				
				_context.info( "Adding dependencies for " + theGraphPath.toString() );

				if( theImportStereotype == null ) {
					_context.error( "Unable to find <<import>> stereotype. Is SysML profile present?");
				} else {
					
					theGraphPath.createDependencies( theViewStructure, theImportStereotype );		
				}
						
				if( theDependencyStereotype == null ) {
					_context.error( "Unable to find stereotype.");
				} else {
					theGraphPath.createDependencies( theViewStructure, theDependencyStereotype );		
				}
			}
		}
		
		return theViewStructure;
	}
	
	/*
	public void createViewStructuresUnder(
			IRPPackage theRootPkg ) {
		
		if( this.isEmpty() ){
			_context.warning( "Unable to createDependencies as there are no graph paths " );
		} else {
												
			int count = 0;
			
			Iterator<GraphPath> iterator = this.iterator();
			
			while( iterator.hasNext() ){
				
				count++;

				GraphPath theGraphPath = (GraphPath) iterator.next();
				
				String thePathName = theGraphPath.getLastNodeName() + String.format( "%03d", count );

				_context.info( "Adding dependencies for " + thePathName );

				IRPPackage fromView = createViewStructure( thePathName, theRootPkg );

				IRPStereotype theConformStereotype = _context.getStereotypeWith( "Conform" );
				
				if( theConformStereotype == null ) {
					_context.error( "Unable to find conform stereotype. Is SysML profile present?");
				}
						
				IRPStereotype theDependencyStereotype = null;
				
				IRPModelElement theDependencyStereotypeEl = _context.findOrAddElement( thePathName, "Stereotype", fromView );

				if( theDependencyStereotypeEl instanceof IRPStereotype ){
					theDependencyStereotype = (IRPStereotype)theDependencyStereotypeEl;
					theDependencyStereotype.addMetaClass( "Dependency" );
				}
				
				theGraphPath.createDependencies( fromView, theDependencyStereotype );		
			}
		}
	}*/
	
	/*
	public void createDependenciesAndPathVisualization(
			IRPClass theClass,
			IRPPackage underThePackage,
			IRPDiagram basedOnDiagram ){
		
		if( this.isEmpty() ){
			_context.warning( "Unable to createDependencies as there are no graph paths " );
		} else {
						
			deleteAllDependenciesOwnedBy( theClass );
						
			int count = 0;
			
			Iterator<GraphPath> iterator = this.iterator();
			
			while( iterator.hasNext() ){
				
				count++;

				GraphPath thePath = (GraphPath) iterator.next();
				
				String thePathName = theClass.getName() + String.format( "%03d", count );
							
				_context.info( "Adding dependencies for " + thePathName );

				IRPStereotype theDependencyStereotype = null;
				
				IRPModelElement theDependencyStereotypeEl = _context.findOrAddElement( thePathName, "Stereotype", underThePackage );

				if( theDependencyStereotypeEl instanceof IRPStereotype ){
					theDependencyStereotype = (IRPStereotype)theDependencyStereotypeEl;
					theDependencyStereotype.addMetaClass( "Dependency" );
				}
				
				thePath.createDependencies( theClass, theDependencyStereotype );			
				
				createPathVisualizationElements( thePathName, underThePackage, theDependencyStereotypeEl, basedOnDiagram, thePath );	
			}
		}
	}*/

	public void deleteAllDependenciesOwnedBy(
			IRPModelElement theClass ){
		
		@SuppressWarnings("unchecked")
		List<IRPModelElement> theDependencies = theClass.getDependencies().toList();
		
		if( !theDependencies.isEmpty() ){
			
			deleteFromModel( theDependencies );
		}
	}

	private void deleteFromModel( 
			List<IRPModelElement> theModelEls ){
		
		Iterator<IRPModelElement> iterator = theModelEls.iterator();
		
		while( iterator.hasNext() ){
			
			IRPModelElement theEl = (IRPModelElement) iterator.next();
			theEl.deleteFromProject();
		}
	}
	
	public GraphPaths getGraphPathsThatInclude(
			IRPModelElement theModelEl,
			String withTheAssignedName ) {
		
		GraphPaths theGraphPaths = new GraphPaths( _context, withTheAssignedName, _sourceDiagram );
		
		int origSize = this.size();
		
		Iterator<GraphPath> iterator = this.iterator();
		
		while( iterator.hasNext() ){

			GraphPath theGraphPath = (GraphPath) iterator.next();

			if( theGraphPath.doesPathInclude( theModelEl ) ) {
				theGraphPaths.add( theGraphPath );
			}
		}
		
		int finalSize = theGraphPaths.size();
		
		_context.info( "getGraphPathsThatInclude has removed " + 
				(origSize - finalSize) + " paths leaving " + finalSize );
		
		return theGraphPaths;
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
