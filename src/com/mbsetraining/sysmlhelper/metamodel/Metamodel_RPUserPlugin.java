package com.mbsetraining.sysmlhelper.metamodel;

import com.telelogic.rhapsody.core.*;

public class Metamodel_RPUserPlugin extends RPUserPlugin {

	static protected Metamodel_Context _context = null;
	MetaModelBuilder _metaModelBuilder = null;

	private final String _legalNotice = 
			"Copyright (C) 2020  MBSE Training and Consulting Limited (www.mbsetraining.com)";
	
	String[] _metaModelSingleRelationProperties = { "PartMetaclassName" };
	String[] _metaModelMultipleRelationProperties = { "AllowedTypes", "Sources", "Targets", "HideTabsInFeaturesDialog" };

	public void RhpPluginInit(
			final IRPApplication theRhpApp ){

		// keep the application interface for later use
		_context = new Metamodel_Context( 
				theRhpApp.getApplicationConnectionString() );

		_metaModelBuilder = new MetaModelBuilder( _context );

		_context.info( "The MetamodelProfile plugin (V" + _context.getPluginVersion() + ") was loaded. " + 
						_legalNotice );
	}

	public void OnMenuItemSelect(
			String menuItem ){

		try {			
			_context.debug( menuItem + " was invoked from the right-click menu" );
			
		} catch( Exception e ){
			_context.error( "Exception in OnMenuItemSelect, e=" + e.getMessage() );
		}
	}

	// if true is returned the plugin will be unloaded
	public boolean RhpPluginCleanup() {

		_context = null;
		return true; // plug-in will be unloaded now (on project close)
	}

	@Override
	public void RhpPluginFinalCleanup() {

		/*
		try {
			_listener.finalize();
		} catch (Throwable e) {
			e.printStackTrace();
		}*/
	}

	@Override
	public void RhpPluginInvokeItem() {
	}

	@Override
	public void OnTrigger(String trigger) {
	}
	
	/*	
	User plugin method should be implemented in a Rhapsody plugin
	implementing "RPUserPlugin" interface.
	The method is required to have the following signature:
		public String methodName(String argument);

	In addition, in order to allow in-line cell editing, another method should be implemented:
		public String methodNameSet(String argument);

	Sample plugin "TableExtension" can be found in the samples directory under
	Rhapsody installation.*/

	public String aggregatesList(
			String guid ){
		
		return _metaModelBuilder.aggregatesList( guid );
	}
}

/**
 * Copyright (C) 2020-2022  MBSE Training and Consulting Limited (www.executablembse.com)

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