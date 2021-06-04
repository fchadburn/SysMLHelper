package com.mbsetraining.sysmlhelper.common;

import com.telelogic.rhapsody.core.*;

import functionalanalysisplugin.PolygonInfo;

public class GraphEdgeInfo {
	
	protected IRPGraphEdge _graphEdge;
	protected ConfigurationSettings _context;
	protected PolygonInfo _polygonInfo;
			
	public GraphEdgeInfo(
			IRPGraphEdge theGraphEdge,
			ConfigurationSettings context ) {	
		
		_context = context;
		_graphEdge = theGraphEdge;
		_polygonInfo = new PolygonInfo( _graphEdge, _context );
	}
	
	public int getStartX(){
		
		return _polygonInfo.getValueAt( 1 );
	}
	
	public int getStartY(){
		
		return _polygonInfo.getValueAt( 2 );
	}

	public int getEndX(){
		
		return _polygonInfo.getValueAt( 3 );
	}
	
	public int getEndY(){
		
		return _polygonInfo.getValueAt( 4 );
	}
	
	public int getMidX(){
		int xOffset = (getBiggestX()-getSmallestX()) / 2;
		int x = getSmallestX()+xOffset;
		return x;
	}
	
	public int getMidY(){
		int yOffset = (getBiggestY()-getSmallestY()) / 2;
		int y = getSmallestY()+yOffset;
		return y;
	}
	
	private int getBiggestX(){
		
		int n = _polygonInfo.getValueAt( 0 );
		int x = 0;
		
		for (int i = 0; i < n; i++) {
			int val = _polygonInfo.getValueAt( i*2+1 );
			if( val > x ){
				x = val;
			}
		}
		
		return x;
	}
	
	private int getSmallestX(){
		
		int n = _polygonInfo.getValueAt( 0 );
		int x = Integer.MAX_VALUE;
		
		for (int i = 0; i < n; i++) {
			int val = _polygonInfo.getValueAt( i*2+1 );
			if( val < x ){
				x = val;
			}
		}
		
		return x;
	}
	
	private int getBiggestY(){
		
		int n = _polygonInfo.getValueAt( 0 );
		int y = 0;
		
		for (int i = 0; i < n; i++) {
			int val = _polygonInfo.getValueAt( i*2+2 );
			if( val > y ){
				y = val;
			}
		}
		
		return y;
	}
	
	private int getSmallestY(){
		
		int n = _polygonInfo.getValueAt( 0 );
		int y = Integer.MAX_VALUE;
		
		for (int i = 0; i < n; i++) {
			int val = _polygonInfo.getValueAt( i*2+2 );
			if( val < y ){
				y = val;
			}
		}
		
		return y;
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