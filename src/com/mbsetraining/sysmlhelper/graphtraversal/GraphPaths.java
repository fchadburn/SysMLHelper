package com.mbsetraining.sysmlhelper.graphtraversal;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import com.mbsetraining.sysmlhelper.businessvalueplugin.BusinessValue_Context;
import com.mbsetraining.sysmlhelper.common.BaseContext;
import com.telelogic.rhapsody.core.*;

public class GraphPaths extends ArrayList<GraphPath>{

	/**
	 * 
	 */
	private static final long serialVersionUID = 5176021867376333578L;

	BaseContext _context;

	
	public static void main(String[] args) {
		
    	IRPApplication theRhpApp = RhapsodyAppServer.getActiveRhapsodyApplication();
    	BusinessValue_Context context = new BusinessValue_Context( theRhpApp.getApplicationConnectionString() );
    	
    	IRPModelElement theSelectedEl = context.getSelectedElement(false);
    	
		context.info( "theSelectedEl is " + context.elInfo( theSelectedEl ) );

    	if( theSelectedEl instanceof IRPTableLayout ){
    		IRPTableLayout theLayout = (IRPTableLayout)theSelectedEl;
    		IRPTableLayout theQueryToUse = theLayout.getToElementTypesQueryToUse();
    		
    		context.info( context.elInfo( theQueryToUse ) );
    		
    		@SuppressWarnings("unchecked")
			List<Object> theProperties = theLayout.getOverriddenProperties(0).toList();
    		
    		for (Object object : theProperties) {
				context.info( object.toString() );
			}

    		
    	} else if( theSelectedEl instanceof IRPObjectModelDiagram ){
    		
    		IRPObjectModelDiagram basedOnDiagram = (IRPObjectModelDiagram)theSelectedEl;
    		
    		//theDiagram.createDiagramView(owner, customViews)
    		context.info( "theSelectedEl is not a table view" );
    		
    		int min = 1;

    		int max = 5;
    		    		
			IRPPackage thePkg = context.getOwningPackageFor( basedOnDiagram );

    		for (int i = min; i <= max; i++) {
        		String theName = "MaxShldrVal" + String.format( "%03d", i);

    			IRPModelElement theCustomView = context.findElementWithMetaClassAndName("Package", theName, thePkg);
    			
    			if( theCustomView != null ){
    				
    				IRPCollection customViews = context.createNewCollection();
    				customViews.setSize(1);
    				customViews.addItem(theCustomView);
    				
        			IRPDiagram theDiagramEl = basedOnDiagram.createDiagramView( basedOnDiagram.getOwner(), customViews );
        			theDiagramEl.setName( theName );
        			
    			}
    			

  

			}
 
    	}
	}
	
	public void createPathVisualizationElements(
			String withTheName,
			IRPPackage underThePackage,
			IRPModelElement theDependencyStereotypeEl,
			IRPDiagram basedOnDiagram,
			GraphPath thePath ){
		
		IRPModelElement theQueryEl = findOrAddElement( withTheName, "TableLayout", underThePackage );
		theQueryEl.changeTo( "Query" );
		
		String theEnableCriteriaCheckValue = null;
		
		try {
			theEnableCriteriaCheckValue = theQueryEl.getPropertyValue( "Model.TableLayout.EnableCriteriaCheck" );

		} catch (Exception e) {
			// TODO: handle exception
		}
		
		if( theEnableCriteriaCheckValue == null ){
			theQueryEl.addProperty( "Model.TableLayout.EnableCriteriaCheck", "Bool", "True" );
		} else if( !theEnableCriteriaCheckValue.equals( "True" ) ){
			theQueryEl.setPropertyValue( "Model.TableLayout.EnableCriteriaCheck", "True" );
		}
		
		String theQueryContextPatternValue = null;
		
		try {
			theQueryContextPatternValue = theQueryEl.getPropertyValue( "Model.TableLayout.QueryContextPattern" );

		} catch (Exception e) {
			// TODO: handle exception
		}
		
		if( theQueryContextPatternValue == null ){

			theQueryEl.addProperty( "Model.TableLayout.QueryContextPattern", "String", "" );
		} else if( !theQueryContextPatternValue.equals( "" ) ){
			theQueryEl.setPropertyValue( "Model.TableLayout.QueryContextPattern", "" );
		}

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
		
		IRPModelElement theCustomViewEl = findOrAddElement( withTheName, "Package", underThePackage );
		theCustomViewEl.changeTo( "CustomView" );
/*		
		IRPCollection customViews = _context.createNewCollection();
		customViews.setSize(1);
		customViews.addItem( theCustomViewEl );*/
		/*		
		IRPModelElement theDiagramEl = _context.findElementWithMetaClassAndName( "ObjectModelDiagram", withTheName, basedOnDiagram.getOwner() );
		
		if( theDiagramEl instanceof IRPDiagram ){
			
			_context.info( "Found " + _context.elInfo( theDiagramEl ) );

			IRPDiagram theDiagram = (IRPDiagram) theDiagramEl;
			theDiagram.setCustomViews( customViews );
			
		} else {
			
			theDiagramEl = basedOnDiagram.createDiagramView( basedOnDiagram.getOwner(), customViews );
			theDiagramEl.setName( withTheName );
		}*/
		

		IRPTag theCriteriaType = theCustomViewEl.getTag( "CriteriaType" );

		if( theCriteriaType == null ){			
			theCriteriaType = (IRPTag) theCustomViewEl.addNewAggr( "Tag", "CriteriaType" );
		}
		
		theCustomViewEl.setTagValue( theCriteriaType, "Queries" );
		
		/* Doesn't work

		IRPTag theCriteria = theCustomViewEl.getTag( "Criteria" );
		
		if( theCriteria == null ){
			theCriteria = (IRPTag) theCustomViewEl.addNewAggr( "Tag", "Criteria" );
		}
		theCustomViewEl.setTagValue( theCriteria, withTheName );*/
		

				
		if( theCustomViewEl != null ){
			
			IRPDiagram theDiagramEl = basedOnDiagram.createDiagramView( basedOnDiagram.getOwner(), null );
			theDiagramEl.setName( withTheName + " - " + _context.toLegalClassName( thePath.getLastNodeName() ) );
			
		}
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
				
				IRPModelElement theDependencyStereotypeEl = findOrAddElement( thePathName, "Stereotype", underThePackage );

				if( theDependencyStereotypeEl instanceof IRPStereotype ){
					theDependencyStereotype = (IRPStereotype)theDependencyStereotypeEl;
					theDependencyStereotype.addMetaClass( "Dependency" );
				}
				
				thePath.createDependencies( theClass, theDependencyStereotype );			
				
				createPathVisualizationElements( thePathName, underThePackage, theDependencyStereotypeEl, basedOnDiagram, thePath );	
			}
		}
	}

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
