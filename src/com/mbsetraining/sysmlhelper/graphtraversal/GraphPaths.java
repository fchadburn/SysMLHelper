package com.mbsetraining.sysmlhelper.graphtraversal;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import com.mbsetraining.sysmlhelper.businessvalueplugin.BusinessValue_Context;
import com.mbsetraining.sysmlhelper.common.BaseContext;
import com.mbsetraining.sysmlhelper.common.UserInterfaceHelper;
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
    	
    	IRPModelElement theSelectedEl = context.getSelectedElement(true);
    	
    	if( theSelectedEl instanceof IRPModelElement ){
    		
    		IRPPackage thePackage = context.getOwningPackageFor(theSelectedEl);
    		
    		GraphPaths theGraphPaths = new GraphPaths(context);
    		
    		theGraphPaths.createPathVisualizationElements( "P1", thePackage );
    	}
	}

	public void createPathVisualizationElements(
			String withTheName,
			IRPPackage underThePackage ){
		
		IRPModelElement theQueryEl = findOrAddElement( withTheName, "TableLayout", underThePackage );
		theQueryEl.changeTo( "Query" );
		
		IRPStereotype theQueryStereotype = _context.getStereotypeCalled( "Query", theQueryEl );
		
		_context.info( "Found " + _context.elInfo( theQueryStereotype ) );
		
		IRPModelElement theDependencyStereotypeEl = findOrAddElement( withTheName, "Stereotype", underThePackage );

		if( theDependencyStereotypeEl instanceof IRPStereotype ){
			IRPStereotype theDependencyStereotype = (IRPStereotype)theDependencyStereotypeEl;
			theDependencyStereotype.addMetaClass( "Dependency" );
		}
		
		IRPTag theRelRef_StereotypeTag = theQueryStereotype.getTag( "RelRef_Stereotype" );
		_context.info( "Found " + _context.elInfo( theRelRef_StereotypeTag ) );
		theQueryEl.setTagElementValue( theRelRef_StereotypeTag, theDependencyStereotypeEl );
		
		IRPTag theRelRef_Metatype = theQueryStereotype.getTag( "RelRef_Metatype" );
		theQueryEl.setTagValue( theRelRef_Metatype, "Dependency" );

		IRPTag theRelRef_How_Related = theQueryStereotype.getTag( "RelRef_How_Related" );
		theQueryEl.setTagValue( theRelRef_How_Related, "IncomingRelations" );
		
		IRPTag theUnresolved = theQueryStereotype.getTag( "Unresolved" );
		theQueryEl.setTagValue( theUnresolved, "ShowUnresolved" );
		
		IRPModelElement theCustomViewEl = findOrAddElement( withTheName + "View", "Package", underThePackage );
		theCustomViewEl.changeTo( "CustomView" );
				
		IRPTag theCriteria = theCustomViewEl.getTag( "Criteria" );
		
		if( theCriteria == null ){
			theCriteria = (IRPTag) theCustomViewEl.addNewAggr( "Tag", "Criteria" );
		}
		theCustomViewEl.setTagValue( theCriteria, withTheName );
		
		IRPTag theCriteriaType = theCustomViewEl.getTag( "CriteriaType" );

		if( theCriteriaType == null ){			
			theCriteriaType = (IRPTag) theCustomViewEl.addNewAggr( "Tag", "CriteriaType" );
		}
		
		theCustomViewEl.setTagValue( theCriteriaType, "Queries" );
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
			IRPPackage underThePackage ){
		
		if( this.isEmpty() ){
			_context.warning( "Unable to createDependencies as there are no graph paths" );
		} else {
			
			int count = 1;
			Iterator<GraphPath> iterator = this.iterator();
			
			while( iterator.hasNext() ){
				
				GraphPath thePath = (GraphPath) iterator.next();
				
				String theName = "P" + count;
				
				IRPModelElement theClass = underThePackage.findAllByName( theName, "Class" );
				
				if( theClass instanceof IRPClass ){
					
					@SuppressWarnings("unchecked")
					List<IRPModelElement> theDependencies = theClass.getDependencies().toList();
					
					if( !theDependencies.isEmpty() ){
						
						boolean answer = true;
						//UserInterfaceHelper.askAQuestion( 
						//		"Do you want to clean the existing dependencies for " + 
						//		_context.elInfo( theClass ) );
						
						if( answer ){
							deleteFromModel( theDependencies );
						}
					}
				}
				
				if( theClass == null ){
					theClass = underThePackage.addClass( theName );
				}
								
				thePath.createDependencies( theClass );			
				
				createPathVisualizationElements( theName, underThePackage );
				
				count++;
			}
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
