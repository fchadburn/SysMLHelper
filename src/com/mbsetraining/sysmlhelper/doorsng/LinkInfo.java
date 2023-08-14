package com.mbsetraining.sysmlhelper.doorsng;

import java.util.List;

import com.mbsetraining.sysmlhelper.common.BaseContext;
import com.telelogic.rhapsody.core.*;

public class LinkInfo {

	BaseContext _context;
	IRPModelElement _sourceEl;
	IRPRequirement _targetEl;
	String _type;
	
	public LinkInfo(
			IRPModelElement sourceEl,
			IRPRequirement targetEl,
			String type,
			BaseContext context ){
		
		_sourceEl = sourceEl;
		_targetEl = targetEl;
		_type = type;
		_context = context;
	}
	
	public String getInfo(){
		
		return _type + " from " + 
				_context.elInfo( _sourceEl ) + 
				" to " + _context.elInfo( _targetEl );
	}
	
	public void createLink(){
		
		addRemoteDependency( _targetEl, _sourceEl, _type );
	}
	
	public void deleteLink(){
		
		List<IRPDependency> theExistingDependencies = _context.
				getExistingStereotypedDependencies( _sourceEl, _targetEl, _type );

		for( IRPDependency theDependency : theExistingDependencies ){
			theDependency.deleteFromProject();
		}
	}
	
	// "Derives From", "Refines", "Satisfies", and "Trace".
	private IRPDependency addRemoteDependency(
			IRPRequirement toRemoteReqt,
			IRPModelElement theDependent,
			String theType ){

		IRPDependency theRemoteDependency = null;

		try {
			theRemoteDependency = theDependent.addRemoteDependencyTo( 
					toRemoteReqt, theType );

			_context.debug( "Added remote " + theType + " from " + 
					_context.elInfo( theDependent ) + " to " + 
					_context.elInfo( toRemoteReqt ) );

		} catch( Exception e ){
			_context.debug( "Unable to add remote " + theType + " from " + 
					_context.elInfo( theDependent ) + " to " + 
					_context.elInfo( toRemoteReqt ) + ", e=" + e.getMessage() );
		}

		return theRemoteDependency;
	}
	
	public void dumpInfo() {
		
		_context.info( getInfo() );
	}
	
	public boolean isEquivalentTo( 
			LinkInfo theLinkInfo ) {
		
		boolean isEquivalentTo = false;
		
		if( _sourceEl.equals( theLinkInfo._sourceEl ) &&
				_type.equals( theLinkInfo._type ) ){
			
			isEquivalentTo = true;
		}
		
		return isEquivalentTo;
	}
}

/**
 * Copyright (C) 2022-2023  MBSE Training and Consulting Limited (www.executablembse.com)

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