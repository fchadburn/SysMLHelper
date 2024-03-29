package com.mbsetraining.sysmlhelper.common;

import com.telelogic.rhapsody.core.*;

public class PolygonInfo {

	protected IRPGraphElement _graphElement;
	protected String[] _component; 
	protected BaseContext _context;
	
	public PolygonInfo( 
			IRPGraphElement theGraphEl,
			BaseContext context ){
		
		_context = context;
		_graphElement = theGraphEl;
		
		IRPGraphicalProperty theGraphicalProperty = 
				_graphElement.getGraphicalProperty( "Polygon" );
		
		String theValue = theGraphicalProperty.getValue();		
		_component = theValue.split(",");		
	}
	
	public int getValueAt( int theIndex ){
				
		int theResult = -999;
		
		try {
			theResult = Integer.parseInt( _component[ theIndex ] );
			
		} catch( Exception e ){
			_context.error( "Warning, unable to find polgyon value for getValueAt(" + theIndex + ")" );
		}	
		
		return theResult;
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