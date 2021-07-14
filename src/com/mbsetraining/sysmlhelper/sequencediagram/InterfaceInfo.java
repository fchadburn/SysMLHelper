package com.mbsetraining.sysmlhelper.sequencediagram;

import com.mbsetraining.sysmlhelper.common.ConfigurationSettings;
import com.telelogic.rhapsody.core.*;

public class InterfaceInfo {

	protected IRPClass _interfaceClass;
	protected IRPClassifier _fromClassifier;
	protected IRPClassifier _toClassifier;
	protected IRPPort _fromPort;
	protected IRPPort _toPort;
	protected boolean _isFromPortNeedsToBeBehavioural;
	protected boolean _isToPortNeedsToBeBehavioural;
	protected ConfigurationSettings _context;

	public InterfaceInfo(
		IRPClass interfaceClass,
		IRPPort fromPort,
		IRPPort toPort,
		ConfigurationSettings context ){

		_interfaceClass = interfaceClass;
		_fromPort = fromPort;
		_toPort = toPort;
		_fromClassifier = (IRPClassifier) _fromPort.getOwner();
		_toClassifier = (IRPClassifier) _toPort.getOwner();
		_context = context;
	}
	
	public void dumpInfo(){
		
		_context.debug( _context.elInfo( _interfaceClass ) + 
				" is an interface contracted from " + 
				_context.elInfo( _fromClassifier ) + " to " + 
				_context.elInfo( _toClassifier ) );
	}

	public IRPClass get_interfaceClass() {
		return _interfaceClass;
	}

	public IRPClassifier get_fromClassifier() {
		return _fromClassifier;
	}

	public IRPClassifier get_toClassifier() {
		return _toClassifier;
	}
	
	public void set_isToPortNeedsToBeBehavioural(
			boolean _isToPortNeedsToBeBehavioural ){
		
		this._isToPortNeedsToBeBehavioural = _isToPortNeedsToBeBehavioural;
	}
	
	public void set_isFromPortNeedsToBeBehavioural(
			boolean _isFromPortNeedsToBeBehavioural ){
		
		this._isFromPortNeedsToBeBehavioural = _isFromPortNeedsToBeBehavioural;
	}
	
	public void setPortsAsBehaviouralIfNeeded(){
		
		if( _isFromPortNeedsToBeBehavioural && 
				_fromPort.getIsBehavioral() != 1 ){
			
			_context.debug( "Setting " + _context.elInfo( _fromPort ) + " as behavioural" );
			_fromPort.setIsBehavioral( 1 );
		}
		
		if( _isToPortNeedsToBeBehavioural && 
				_toPort.getIsBehavioral() != 1 ){
			
			_context.debug( "Setting " + _context.elInfo( _toPort ) + " as behavioural" );
			_toPort.setIsBehavioral( 1 );
		}
	}
	
	public IRPPort get_fromPort() {
		return _fromPort;
	}

	public IRPPort get_toPort() {
		return _toPort;
	}
}

/**
 * Copyright (C) 2021  MBSE Training and Consulting Limited (www.executablembse.com)

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