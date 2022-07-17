package com.mbsetraining.sysmlhelper.businessvalueplugin;

import java.util.List;
import java.util.Set;

import com.mbsetraining.sysmlhelper.common.ConfigurationSettings;
import com.mbsetraining.sysmlhelper.common.DependencySelector;
import com.mbsetraining.sysmlhelper.common.LayoutHelper;
import com.mbsetraining.sysmlhelper.common.UserInterfaceHelper;
import com.telelogic.rhapsody.core.*;

public class BusinessValue_RPUserPlugin extends RPUserPlugin {

	protected BusinessValue_Context _context;
	protected ConfigurationSettings _settings;
	protected BusinessValue_RPApplicationListener _listener = null;

	// called when plug-in is loaded
	public void RhpPluginInit(
			final IRPApplication theRhapsodyApp ){

		String theAppID = theRhapsodyApp.getApplicationConnectionString();

		try {
			_context = new BusinessValue_Context( theAppID );

			_settings = new ConfigurationSettings(
					"BusinessValue.properties", 
					"BusinessValue_MessagesBundle",
					"BusinessValue" , 
					_context );

		} catch( Exception e ){
			_context.error( "Exception in RhpPluginInit, e=" + e.getMessage() );
		}

		final String legalNotice = 
				"Copyright (C) 2022  MBSE Training and Consulting Limited (www.executablembse.com)"
						+ "\n"
						+ "SysMLHelperPlugin is free software: you can redistribute it and/or modify "
						+ "it under the terms of the GNU General Public License as published by "
						+ "the Free Software Foundation, either version 3 of the License, or "
						+ "(at your option) any later version."
						+ "\n"
						+ "SysMLHelperPlugin is distributed in the hope that it will be useful, "
						+ "but WITHOUT ANY WARRANTY; without even the implied warranty of "
						+ "MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the "
						+ "GNU General Public License for more details."
						+ "You should have received a copy of the GNU General Public License "
						+ "along with SysMLHelperPlugin. If not, see <http://www.gnu.org/licenses/>. "
						+ "Source code is made available on https://github.com/fchadburn/mbsetraining";

		String msg = "The BusinessValue component of the SysMLHelperPlugin was loaded successfully.\n" +
				legalNotice +
				"\nNew right-click 'Business Value' commands have been added.";		

		_context.info( msg );

		_listener = new BusinessValue_RPApplicationListener( 
				"BusinessValueProfile",
				_context );

		_listener.connect( theRhapsodyApp );

		_context.info( "The BusinessValue profile version is " + _context.getPluginVersion() );

		_settings.checkIfSetupProjectIsNeeded( false, true );
	}

	// called when the plug-in pop-up menu  is selected
	public void OnMenuItemSelect(
			String menuItem ){

		try {
			final String theAppID = _context.get_rhpAppID();

			IRPModelElement theSelectedEl = _context.getSelectedElement( false );
			List<IRPModelElement> theSelectedEls = _context.getSelectedElements();
			List<IRPGraphElement> theSelectedGraphEls = _context.getSelectedGraphElements();

			_context.debug( "Right-click menu item '" + menuItem + "' was called with " + 
					theSelectedEls.size() + " selected elements..." );

			if( !theSelectedEls.isEmpty() ){

				if( menuItem.equals( _settings.getString(
						"businessvalueplugin.SetupProjectProperties" ) ) ){

					if( theSelectedEl instanceof IRPPackage ){
						_settings.checkIfSetupProjectIsNeeded( true, _context.BUSINESS_VALUE_NEW_TERM );
						_context.cleanUpModelRemnants();
					} else {
						_context.error( menuItem + " invoked out of context and only works for packages" );
					}
					
				} else if( menuItem.equals( _settings.getString(
						"businessvalueplugin.SelectDependsOnElementsMenu" ) ) ){

					Set<IRPModelElement> theCombinedSet = 
							_context.getSetOfElementsFromCombiningThe(
									theSelectedEls, theSelectedGraphEls );

					DependencySelector theSelector = new DependencySelector( _context );

					theSelector.selectDependsOnElementsFor( 
							theCombinedSet, null );

				} else if( menuItem.equals( _settings.getString(
						"businessvalueplugin.SelectDependentElementsMenu" ) ) ){

					Set<IRPModelElement> theCombinedSet = 
							_context.getSetOfElementsFromCombiningThe(
									theSelectedEls, theSelectedGraphEls );

					DependencySelector theSelector = new DependencySelector( _context );

					theSelector.selectDependentElementsFor( 
							theCombinedSet, null );

				} else if( menuItem.equals( _settings.getString( 
						"businessvalueplugin.CenterStraightLinesMenu" ) ) ){

					if( theSelectedGraphEls.size() > 0 ){

						LayoutHelper theHelper = new LayoutHelper( _context );

						theHelper.centerStraightLinesForTheGraphEls( 
								theSelectedGraphEls );

					} else if( theSelectedEl.getMetaClass().equals( "ActivityDiagramGE" ) ){

						LayoutHelper theHelper = new LayoutHelper( _context );

						theHelper.centerLinesForTheDiagram( 
								(IRPDiagram) theSelectedEl );

					} else if( theSelectedEl.getMetaClass().equals( "ActivityDiagram" ) ){

						@SuppressWarnings("unchecked")
						List<IRPModelElement> theDiagrams = theSelectedEl.getNestedElementsByMetaClass( 
								"ActivityDiagramGE", 0 ).toList();

						if( theDiagrams.size()==1 ){

							LayoutHelper theHelper = new LayoutHelper( _context );

							theHelper.centerLinesForTheDiagram( 
									(IRPDiagram) theDiagrams.get( 0 ) );
						} else {
							_context.error( "Error in OnMenuItemSelect, unable to find an ActivityDiagramGE" );
						}

					} else if( theSelectedEl instanceof IRPDiagram ){

						LayoutHelper theHelper = new LayoutHelper( _context );

						theHelper.centerLinesForTheDiagram( 
								(IRPDiagram) theSelectedEl );

					} else if( theSelectedEl instanceof IRPPackage ){

						LayoutHelper theHelper = new LayoutHelper( _context );

						theHelper.centerLinesForThePackage( 
								(IRPPackage) theSelectedEl );
					}
					
				} else {
					_context.warning( "Unhandled menu: " + _context.elInfo( theSelectedEl ) + " was invoked with menuItem='" + menuItem + "'");
				}

				_context.debug( "'" + menuItem + "' completed.");
			}

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

		try {
			_listener.finalize();
			_listener = null;

		} catch( Throwable e ){
			e.printStackTrace();
		}
	}

	@Override
	public void RhpPluginInvokeItem() {
	}

	@Override
	public void OnTrigger(String trigger) {
	}

	public IRPPackage getOwningPackage(
			IRPModelElement forEl ){

		IRPPackage theOwningPackage = null;

		if( forEl instanceof IRPPackage ){
			theOwningPackage = (RPPackage)forEl;

		} else if( forEl instanceof IRPProject ){

		} else if( forEl.getOwner() != null ){
			theOwningPackage = getOwningPackage( forEl.getOwner() );
		} else {
		}

		return theOwningPackage;
	}


	private boolean checkAndPerformProfileSetupIfNeeded() {

		boolean isContinue = true;

		boolean isShowInfoDialog = _context.getIsShowProfileVersionCheckDialogs();
		boolean isSetupNeeded = _settings.checkIfSetupProjectIsNeeded( isShowInfoDialog, false );

		if( isSetupNeeded ){

			String theMsg = "The project needs 'Business Value' profile-based properties and tags values to be applied.\n" + 
					"Do you want me to set these properties and tags on the project in order to continue?";

			isContinue = UserInterfaceHelper.askAQuestion( theMsg );

			if( isContinue ){
				_settings.setupProjectWithProperties( _context.BUSINESS_VALUE_NEW_TERM );
			}
		}
		return isContinue;
	}
}

/**
 * Copyright (C) 2018-2022  MBSE Training and Consulting Limited (www.executablembse.com)

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