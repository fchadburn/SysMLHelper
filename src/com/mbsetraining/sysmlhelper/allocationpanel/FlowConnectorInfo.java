package com.mbsetraining.sysmlhelper.allocationpanel;

import com.mbsetraining.sysmlhelper.executablembse.ExecutableMBSE_Context;
import com.telelogic.rhapsody.core.*;

public class FlowConnectorInfo {
	
	protected ExecutableMBSE_Context _context;
	protected FunctionAllocationMap _functionAllocationMap;
	protected IRPLink _srcLink;
	protected IRPInstance _srcFromInstance;
	protected IRPInstance _srcToInstance;
	protected IRPSysMLPort _srcFromPort;
	protected IRPSysMLPort _srcToPort;

	protected IRPLink _tgtLink;
	protected IRPInstance _tgtFromInstance;
	protected IRPInstance _tgtToInstance;

	public FlowConnectorInfo(
			IRPLink theLink,
			FunctionAllocationMap functionAllocationMap,
			ExecutableMBSE_Context context ){
		
		_context = context;
		_functionAllocationMap = functionAllocationMap;
		_srcLink = theLink;
		_srcFromInstance = _srcLink.getFrom();
		_srcToInstance = _srcLink.getTo();
		_srcFromPort = _srcLink.getFromSysMLPort();
		_srcToPort = _srcLink.getToSysMLPort();
	}
	
	public void dumpInfo() {
		
		_context.info( "_srcLink         = " + _context.elInfo( _srcLink ) );
		_context.info( "_srcFromInstance = " + _context.elInfo( _srcFromInstance ) );
		_context.info( "_srcToInstance   = " + _context.elInfo( _srcToInstance ) );
		_context.info( "_srcFromPort     = " + _context.elInfo( _srcFromPort ) );
		_context.info( "_srcToPort       = " + _context.elInfo( _srcToPort ) );
	}
	
	public boolean isMappable() {
		
		boolean isMappable = true;
		
		if( !_functionAllocationMap.containsKey( _srcFromInstance ) ) {
			_context.info( "Unable to map " + _context.elInfo( _srcLink ) + " as " + _context.elInfo( _srcFromInstance ) + " is not in mapping" );
			isMappable = false;
		}
		
		if( !_functionAllocationMap.containsKey( _srcToInstance ) ) {
			_context.info( "Unable to map " + _context.elInfo( _srcLink ) + " as " + _context.elInfo( _srcToInstance ) + " is not in mapping" );
			isMappable = false;
		}
		
		return isMappable;
	}
	
	public boolean performMapping() {
		
		boolean isMappable = true;
		
		FunctionAllocationInfo fromInstanceInfo = _functionAllocationMap.get( _srcFromInstance );
		
		if( fromInstanceInfo == null ) {
			_context.info( "Unable to map " + _context.elInfo( _srcLink ) + " as " + _context.elInfo( _srcFromInstance ) + " is not in mapping" );
			isMappable = false;
		}
		
		FunctionAllocationInfo toInstanceInfo = _functionAllocationMap.get( _srcToInstance );

		if( toInstanceInfo == null ) {
			_context.info( "Unable to map " + _context.elInfo( _srcLink ) + " as " + _context.elInfo( _srcToInstance ) + " is not in mapping" );
			isMappable = false;
		}
		
		return isMappable;
	}
}

/**
 * Copyright (C) 2023  MBSE Training and Consulting Limited (www.executablembse.com)

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
