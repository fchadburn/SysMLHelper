package com.mbsetraining.sysmlhelper.packagediagram;

import java.awt.Dimension;
import java.util.ArrayList;
import java.util.List;

import com.mbsetraining.sysmlhelper.executablembse.ExecutableMBSE_Context;
import com.telelogic.rhapsody.core.*;

public class ElementTreeNode {

	protected ExecutableMBSE_Context _context;
	protected IRPModelElement _element = null;
	protected List<ElementTreeNode> _children = new ArrayList<ElementTreeNode>();
	protected ElementTreeNode _parent = null;
	protected IRPDiagram _diagram = null; 
	protected IRPGraphNode _graphNode = null;
	protected Dimension _dimension = null;
	protected Integer _xPos = null;
	protected Integer _yPos = null;
	protected Integer _xChildPos = null;
	protected Integer _yChildPos = null;

    public ElementTreeNode( 
    		IRPModelElement element, 
    		ExecutableMBSE_Context context ) {
        
    	this._element = element;
    	this._context = context;
    }
    
    public ElementTreeNode recursivelyFindLowestGraphNode() {
    	
    	ElementTreeNode lowestNode = null;
    	
    	if( _yPos != null ) {
    		
    		lowestNode = this;
    	        	
        	for( ElementTreeNode childTreeNode : _children ){
    			
        		ElementTreeNode lowestChild = childTreeNode.recursivelyFindLowestGraphNode();
        		
        		if( lowestChild != null &&
        				lowestChild._yPos != null &&
        				lowestNode._yPos < lowestChild._yPos ){
        		
        			lowestNode = lowestChild;
        		}
    		}
    	}

		return lowestNode;
    }
    
    public boolean isLeafInTree() {
    	
    	boolean isDiagramInTree = false;
    	
		List<String> theLeafMetaClasses = _context.getPackageDiagramIndexLeafElementMetaClasses( _element );
		String theElsMetaClass = _element.getUserDefinedMetaClass();		
    
    	if( theLeafMetaClasses.contains( theElsMetaClass ) ){
    		
    		isDiagramInTree = true;
    		
    	} else {
    		
    		for( ElementTreeNode childTreeNode : _children ){
				
    			if( childTreeNode.isLeafInTree() ) {
    				isDiagramInTree = true;
    				break;
    			}
			}
    	}
    	
    	return isDiagramInTree;
    }
    
	public void recursivelyAddTreeNodeToDiagram(
			IRPDiagram theDiagram,
			IRPGraphNode parentGraphNode,
			IRPModelElement parentEl,
			int xPos,
			int yPos ){
		
		this._diagram = theDiagram;
		this._xPos = xPos;
		this._yPos = yPos;
		
		IRPModelElement theModelEl = this.getElement();
		
		_context.debug( "recursivelyAddTreeNodeToDiagram invoked for " + _context.elInfo( theModelEl ) );

		this._dimension = getDimensionFor( theModelEl );

		if( theModelEl instanceof IRPProject ) {
			
			_dimension.height = 0;
			_graphNode = null;
			
		} else if( theModelEl instanceof IRPTableView ){

			IRPHyperLink theHyperLink = _context.createNewOrGetExistingHyperLink( parentEl, theModelEl );
		
			_context.info( _context.elInfo( theHyperLink ) + 
					" owned by " + _context.elInfo( parentEl ) + " has been used as unable to add " + 
					_context.elInfo(theModelEl) + " to " + _context.elInfo( _diagram ) );

			_dimension.height = 0;
			_graphNode = null;
			
		} else {
		
			this._dimension = getDimensionFor( theModelEl );

			try {
				_graphNode = _diagram.addNewNodeForElement( 
						theModelEl, _xPos, _yPos, _dimension.width, _dimension.height );
			
			} catch( Exception e ){
				
				_context.info( "Rhapsody did not allow drawing of " + 
						_context.elInfo( theModelEl ) + " on " + _context.elInfo( _diagram ) );		
				
				_dimension.height = 0;
				_graphNode = null;
			}
		}
		
		List<ElementTreeNode> theChildNodes = this.getChildren();

		if( !theChildNodes.isEmpty() ) {

			int xChildOffset = 40;
			int yChildOffset = 20;
			
			_xChildPos = _xPos + xChildOffset;
			_yChildPos = _yPos + _dimension.height + yChildOffset;

			boolean areIndexDiagramsPresent = arePackageIndexDiagramsPresent( theChildNodes );

			for( ElementTreeNode theChildNode : theChildNodes ){
				
				boolean isPackageIndexDiagram = isPackageIndexDiagram( theChildNode );
				boolean isRootNode = this.getParent() == null;
				
				if( !areIndexDiagramsPresent || 
						( areIndexDiagramsPresent && !isPackageIndexDiagram && isRootNode ) ||
						( areIndexDiagramsPresent && isPackageIndexDiagram && !isRootNode ) ){
					
					IRPModelElement theChildEl = theChildNode.getElement();

					Dimension theChildDimension = getDimensionFor( theChildEl );

					// recursive call
					theChildNode.recursivelyAddTreeNodeToDiagram( 
							theDiagram, parentGraphNode, theModelEl, _xChildPos, _yChildPos );
					
					if( !theChildEl.getOwner().equals( theModelEl ) ){

						_context.warning( _context.elInfo( theChildEl ) + " is not owned by " + _context.elInfo( theModelEl ) );

					} else if ( this._graphNode != null && 
							theChildNode._graphNode != null ){

						try {
							_diagram.addNewEdgeByType(
									"Containment Arrow", 
									theChildNode._graphNode, 
									_xChildPos, 
									_yChildPos + ( theChildDimension.height/2 ),
									this._graphNode, 
									_xPos + ( xChildOffset/2 ), 
									_yPos + theChildDimension.height );
							
						} catch( Exception e ){
							
							_context.info( "Rhapsody did not allow containment arrow to be drawn from " + 
									_context.elInfo( theChildEl ) + " to " + _context.elInfo( theModelEl ) );
						}

					} 
					
					ElementTreeNode lowestTreeNode = this.recursivelyFindLowestGraphNode();
					
					_yChildPos = lowestTreeNode._yPos + lowestTreeNode._dimension.height + yChildOffset;					
				}
			}
		}
	}
	
	public Dimension getDimensionFor(
			IRPModelElement theEl ) {

		Dimension theDimension;

		if( theEl instanceof IRPPackage ) {
			
			theDimension = new Dimension( 300, 70 );
			
		} else if( theEl instanceof IRPUseCase ){
			
			theDimension = new Dimension( 140, 65 );
			
		} else {
			theDimension = new Dimension( 300, 48 );
		}

		return theDimension;
	}

	private boolean arePackageIndexDiagramsPresent(
			List<ElementTreeNode> inElementTreeNodes ) {

		boolean arePackageIndexDiagramsPresent = false;

		for( ElementTreeNode inElementTreeNode : inElementTreeNodes ){

			if( isPackageIndexDiagram( inElementTreeNode ) ) {
				arePackageIndexDiagramsPresent = true;
				break;
			}
		}

		return arePackageIndexDiagramsPresent;
	}
	
	public boolean areNonPackageIndexDiagramChildElementsPresent() {

		boolean areNonPackageIndexDiagramElementsPresent = false;

		for( ElementTreeNode inElementTreeNode : _children ){

			if( !isPackageIndexDiagram( inElementTreeNode ) ) {
				areNonPackageIndexDiagramElementsPresent = true;
				break;
			}
		}

		return areNonPackageIndexDiagramElementsPresent;
	}

	private boolean isPackageIndexDiagram(
			ElementTreeNode theTreeNode ) {

		boolean isPackageIndexDiagram = false;

		IRPModelElement theEl = theTreeNode.getElement();

		if( theEl.getUserDefinedMetaClass().equals( _context.PACKAGE_DIAGRAM_INDEX) ) {
			isPackageIndexDiagram = true;
		}

		return isPackageIndexDiagram;
	}

    public void addChild( ElementTreeNode child ) {
        child.setParent( this );
        this._children.add( child );
    }

    public void addChild( IRPModelElement element ) {
    	ElementTreeNode newChild = new ElementTreeNode( element, _context );
        this.addChild( newChild );
    }

    public void addChildren( List<ElementTreeNode> children ) {
        for( ElementTreeNode t : children ) {
            t.setParent( this );
        }
        this._children.addAll( children );
    }

    public List<ElementTreeNode> getChildren() {
        return _children;
    }

    public IRPModelElement getElement() {
        return _element;
    }

    public void setElement(IRPModelElement element) {
        this._element = element;
    }

    private void setParent(ElementTreeNode parent) {
        this._parent = parent;
    }

    public ElementTreeNode getParent() {
        return _parent;
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
