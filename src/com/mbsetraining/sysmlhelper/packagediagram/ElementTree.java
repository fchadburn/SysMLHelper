package com.mbsetraining.sysmlhelper.packagediagram;

import java.util.ArrayList;
import java.util.List;
import com.mbsetraining.sysmlhelper.executablembse.ExecutableMBSE_Context;
import com.telelogic.rhapsody.core.*;

public class ElementTree {

	protected ExecutableMBSE_Context _context;
	protected ElementTreeNode _rootNode;
	protected IRPPackage _rootPkg;
	protected IRPDiagram _diagram;

	public ElementTree(
			IRPPackage forPkg,
			ExecutableMBSE_Context context ){

		_context = context;

		_context.debug( "ElementTree constructor invoked for " + _context.elInfo( forPkg ) );

		_rootPkg = forPkg;
		_rootNode = recursivelybuildTree( _rootPkg );

		if( _rootNode != null ) {			
			dumpChildrenFor( _rootNode, 0 );
		}
	}

	public ElementTreeNode recursivelybuildTree(
			IRPModelElement fromEl ){

		_context.debug( "recursivelybuildTree invoked for " + _context.elInfo( fromEl ) );

		ElementTreeNode theTreeNode = new ElementTreeNode( fromEl, _context );

		@SuppressWarnings("unchecked")
		List<IRPModelElement> theChildEls = fromEl.getNestedElements().toList();

		List<String> theLeafMetaClasses = _context.getPackageDiagramIndexLeafElementMetaClasses( _rootPkg );

		String theElsMetaClass = fromEl.getUserDefinedMetaClass();
		boolean isLeafElementType = theLeafMetaClasses.contains( theElsMetaClass );
		
		if( theChildEls.size() > 0 ) {

			List<String> theAcceptedMetaClasses = _context.getPackageDiagramIndexUserDefinedMetaClasses( _rootPkg );

			List<ElementTreeNode> theChildNodes = new ArrayList<>();

			for( String theMetaClass : theAcceptedMetaClasses ){

				for( IRPModelElement theChildEl : theChildEls ){

					String theChildMetaClass = theChildEl.getUserDefinedMetaClass();

					//_context.info( "Check if " +  _context.elInfo( theChildEl ) + " is a " + theMetaClass );

					if( theChildMetaClass.equals( theMetaClass ) ) {

						//_context.info( "- found " + _context.elInfo( theChildEl ) );

						ElementTreeNode theChildNode = recursivelybuildTree( theChildEl );

						if( theChildNode != null ) {

							if( !theChildNodes.contains( theChildNode ) ){
								theChildNodes.add( theChildNode );
							} else {
								_context.info( "already added " + _context.elInfo( theChildEl ) );
							}
						}
					}
				}
			}

			_context.debug( "recursivelybuildTree found " + theChildNodes.size() + " for " + _context.elInfo( fromEl ) );

			if( isLeafElementType ||
					!theChildNodes.isEmpty() ) {

				theTreeNode = new ElementTreeNode( fromEl, _context );

				for( ElementTreeNode theChildNode : theChildNodes ){

					if( theChildNode.isLeafInTree() ) {						
						theTreeNode.addChild( theChildNode );
					}
				}			
			}
		}

		if( !isLeafElementType &&
				theTreeNode._children.isEmpty() ){
			
			// Don't have a leaf node
			theTreeNode = null;
		}

		return theTreeNode;
	}

	public boolean buildPackageDiagram( 
			IRPDiagram theDiagram ){

		_diagram = theDiagram;
		
		boolean isBuilt = false;
		
		if( _rootNode == null ) {
			_context.info( "Skipping package diagram building as nothing to show" );
		} else {
			wipeDiagram( _diagram );
			_rootNode.recursivelyAddTreeNodeToDiagram( _diagram, null, 50, 60 );
			completeRelationsBetweenPackagesIfEnabledOn( _diagram );
			isBuilt = true;
		}

		return isBuilt;
	}
	
	private void completeRelationsBetweenPackagesIfEnabledOn( 
			IRPDiagram theDiagram ){
		
		if( _context.getIsCompleteRelationsWhenAutoDrawingPackageDiagramIndexEnabled( theDiagram ) ) {
			
			@SuppressWarnings("unchecked")
			List<IRPGraphElement> theGraphEls = theDiagram.getGraphicalElements().toList();
			
			IRPCollection theGraphElsToComplete = _context.get_rhpApp().createNewCollection();
			
			for( IRPGraphElement theGraphEl : theGraphEls ){
				
				IRPModelElement theModelEl = theGraphEl.getModelObject();
				
				if( theModelEl instanceof IRPPackage ) {
					theGraphElsToComplete.addGraphicalItem( theGraphEl );
				}
			}
			
			theDiagram.completeRelations( theGraphElsToComplete, 0 );		
		}
	}

	private void wipeDiagram(
			IRPDiagram theDiagram ){

		@SuppressWarnings("unchecked")
		List<IRPGraphElement> theGraphEls = theDiagram.getGraphicalElements().toList();		

		IRPCollection theGraphElsToRemove = _context.createNewCollection();

		for( IRPGraphElement theGraphEl : theGraphEls ){

			IRPGraphicalProperty theTypeProperty = theGraphEl.getGraphicalProperty( "Type" );

			if( theTypeProperty != null ) {
				String theType = theTypeProperty.getValue();

				if( !theType.equals( "DiagramFrame" ) ){
					theGraphElsToRemove.addGraphicalItem( theGraphEl );
				}
			} else {
				_context.warning( "updatePackageDiagramContent found graph el without a Type" );
			}
		}

		theDiagram.removeGraphElements( theGraphElsToRemove );
	}

	public void dumpChildrenFor(
			ElementTreeNode theTreeEl,
			int count ) {

		List<ElementTreeNode> theChildren = theTreeEl.getChildren();

		String msg = "";

		for (int i = 0; i < count; i++) {
			msg = msg + "    ";
		}

		if( theChildren.isEmpty() ) {

			msg += _context.elInfo( theTreeEl.getElement() ) + " has no children.";

			_context.info( msg );

		} else {

			msg += _context.elInfo( theTreeEl.getElement() ) + " has " + theChildren.size() + " children:";

			_context.info( msg );

			for (ElementTreeNode theChild : theChildren) {
				dumpChildrenFor( theChild, count + 1 );
			}
		}
	}

}

/**
 * Copyright (C) 2024  MBSE Training and Consulting Limited (www.executablembse.com)

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
