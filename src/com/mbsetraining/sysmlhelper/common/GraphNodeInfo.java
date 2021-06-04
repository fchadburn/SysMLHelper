package com.mbsetraining.sysmlhelper.common;

import com.telelogic.rhapsody.core.*;

import functionalanalysisplugin.PolygonInfo;

public class GraphNodeInfo {
	
	private IRPGraphNode m_GraphNode = null;
	protected PolygonInfo _polygonInfo;
	protected ConfigurationSettings _context;
	
	public GraphNodeInfo(
			IRPGraphNode theGraphNode,
			ConfigurationSettings context ){
		
		_context = context;
		m_GraphNode = theGraphNode;
		_polygonInfo = new PolygonInfo( m_GraphNode, context );
	}
	
	public int getWidth(){
	
		return getBottomRightX() - getTopLeftX();
	}
	
	public int getHeight(){
		
		return getBottomRightY() - getTopLeftY();
	}
	
	public int getTopLeftX(){
		
		return _polygonInfo.getValueAt( 1 );
	}
	
	public int getTopLeftY(){
		
		return _polygonInfo.getValueAt( 2 );
	}

	public int getTopRightX(){
		
		return _polygonInfo.getValueAt( 3 );
	}
	
	public int getTopRightY(){
		
		return _polygonInfo.getValueAt( 4 );
	}
	
	public int getBottomRightX(){
		
		return _polygonInfo.getValueAt( 5 );
	}
	
	public int getBottomRightY(){
		
		return _polygonInfo.getValueAt( 6 );
	}
	
	public int getBottomLeftX(){
		
		return _polygonInfo.getValueAt( 7 );
	}
	
	public int getBottomLeftY(){
		
		return _polygonInfo.getValueAt( 8 );
	}
	
	public int getMiddleX(){
		
		int val = _polygonInfo.getValueAt( 1 ) + ( ( _polygonInfo.getValueAt( 3 ) - _polygonInfo.getValueAt( 1 ) ) / 2 );
		return val;
	}
	
	public int getMiddleY(){
		
		int val = _polygonInfo.getValueAt( 2 ) + ( ( _polygonInfo.getValueAt(8 ) - _polygonInfo.getValueAt( 2 ) ) / 2 );
		return val;
	}
}

/**
 * Copyright (C) 2016-2021  MBSE Training and Consulting Limited (www.executablembse.com)
    
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
