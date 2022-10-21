package com.mbsetraining.sysmlhelper.graphelementhelpers;

import com.mbsetraining.sysmlhelper.common.BaseContext;
import com.mbsetraining.sysmlhelper.common.GraphNodeInfo;
import com.telelogic.rhapsody.core.*;

public class GraphNodeResizer {
	
	protected boolean _isValid;
	protected IRPGraphNode _graphNode;
	protected BaseContext _context;
	protected int _newX;
	protected int _newY;
	protected int _newWidth;
	protected int _newHeight;
	
	public GraphNodeResizer(
			IRPGraphNode theGraphNode,
			BaseContext context ) {

		_context = context;
		_graphNode = theGraphNode;
		_isValid = false;

		IRPModelElement theModelObject = _graphNode.getModelObject();
		
		if( theModelObject != null ){			
						
			IRPStereotype theNewTerm = theModelObject.getNewTermStereotype();
			
			String theDefaultSize = null;
			
			// if it's a new term then try and get DefaultSize property for new term
			if( theNewTerm != null ){
				theDefaultSize = getDefaultSize( theNewTerm.getName(), theGraphNode.getDiagram() );
			}
			
			String theBaseClass = theModelObject.getMetaClass();
			
			// if not got a default size yet then use the base metaclass to find it
			if( theDefaultSize == null ){
				theDefaultSize = getDefaultSize( theBaseClass, theGraphNode.getDiagram() );
			}
			
			if( theDefaultSize != null ){
				
				String[] splitSize = theDefaultSize.split(",");		
				
				int defaultXOffset = Integer.parseInt( splitSize[0] );
				int defaultYOffset = Integer.parseInt( splitSize[1] );
				int defaultWidth = Integer.parseInt( splitSize[2] ) - defaultXOffset;
				int defaultHeight = Integer.parseInt( splitSize[3] ) - defaultYOffset;
								
				GraphNodeInfo theDecisionNodeInfo = new GraphNodeInfo( _graphNode, _context );
				
				int x = theDecisionNodeInfo.getTopLeftX();
				int y = theDecisionNodeInfo.getTopLeftY();
				int height = theDecisionNodeInfo.getHeight();
				int width = theDecisionNodeInfo.getWidth();
				
				_newHeight = defaultHeight;
				_newWidth = defaultWidth;
				
				_newX = x + ( ( width - defaultWidth ) / 2 );
				_newY = y + ( ( height - defaultHeight ) / 2 );
				
				_isValid = true;
			}
		}
	}
	
	public void performResizing(){
		
		if( _isValid ){
			_graphNode.setGraphicalProperty( "Position", _newX + "," + _newY );
			_graphNode.setGraphicalProperty( "Height", Integer.toString( _newHeight ) );
			_graphNode.setGraphicalProperty( "Width", Integer.toString( _newWidth ) );			
		} else {
			_context.warning( "Unable to resize graphNode tied to " + _context.elInfo( _graphNode.getModelObject() ) );
		}
	}
	
	private String getDefaultSize( 
			String theMetaClass, 
			IRPModelElement theEl ){
		
		String theDefaultSize = null;
		
		try {
			theDefaultSize = theEl.getPropertyValue( "Format." + theMetaClass + ".DefaultSize" );

		} catch( Exception e ){
			_context.warning( "Warning in GraphNodeResizer related to " + theMetaClass + ", e=" + e.getMessage() );
		}
		
		return theDefaultSize;
	}
	
	public boolean isResizable(){
		return _isValid;
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
