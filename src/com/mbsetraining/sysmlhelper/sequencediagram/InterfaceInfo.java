package com.mbsetraining.sysmlhelper.sequencediagram;

import com.mbsetraining.sysmlhelper.common.ConfigurationSettings;
import com.telelogic.rhapsody.core.*;

public class InterfaceInfo {

	protected IRPClass _interfaceClass;
	protected IRPClassifier _fromClassifier;
	protected IRPClassifier _toClassifier;
	protected ConfigurationSettings _context;

	public InterfaceInfo(
		IRPClass interfaceClass,
		IRPClassifier fromClassifier,
		IRPClassifier toClassifier,
		ConfigurationSettings context
			) {

		_interfaceClass = interfaceClass;
		_fromClassifier = fromClassifier;
		_toClassifier = toClassifier;
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