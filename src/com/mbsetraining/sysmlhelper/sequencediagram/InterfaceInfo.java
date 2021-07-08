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
		
		_context.info( _context.elInfo( _interfaceClass ) + 
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
