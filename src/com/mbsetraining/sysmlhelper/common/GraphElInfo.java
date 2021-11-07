package com.mbsetraining.sysmlhelper.common;

import com.telelogic.rhapsody.core.IRPGraphEdge;
import com.telelogic.rhapsody.core.IRPGraphElement;
import com.telelogic.rhapsody.core.IRPGraphNode;

public class GraphElInfo {

	BaseContext _context;
	IRPGraphElement _graphEl;
	
	public GraphElInfo(
			IRPGraphElement theGraphEl,
			BaseContext context ) {
		
		_context = context;
		_graphEl = theGraphEl;
	}
	
	public int getMidX(){
		
		int x = 10;
		
		if( _graphEl != null ){

			if (_graphEl instanceof IRPGraphNode){
				GraphNodeInfo theNodeInfo = new GraphNodeInfo( (IRPGraphNode) _graphEl, _context );
				
				x = theNodeInfo.getMiddleX();
				
			} else if (_graphEl instanceof IRPGraphEdge){
				GraphEdgeInfo theNodeInfo = new GraphEdgeInfo( (IRPGraphEdge) _graphEl, _context );
				
				x = theNodeInfo.getMidX();
			}
		} else {
			x = 20; // default is top right
		}
		
		return x;
	}
	
	public int getMidY( ){
		
		int y = 10;
		
		if( _graphEl != null ){

			if (_graphEl instanceof IRPGraphNode){
				GraphNodeInfo theNodeInfo = new GraphNodeInfo( (IRPGraphNode) _graphEl, _context );
				
				y = theNodeInfo.getMiddleY();
				
			} else if (_graphEl instanceof IRPGraphEdge){
				GraphEdgeInfo theNodeInfo = new GraphEdgeInfo( (IRPGraphEdge) _graphEl, _context );
				
				y = theNodeInfo.getMidY();
			}
		} else {
			y = 20; // default is top right
		}
		
		return y;
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
