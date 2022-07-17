package com.mbsetraining.sysmlhelper.taumigratorplugin;

import java.util.List;

import com.mbsetraining.sysmlhelper.common.ConfigurationSettings;
import com.mbsetraining.sysmlhelper.common.UserInterfaceHelper;
import com.telelogic.rhapsody.core.*;

public class TauMigrator_RPUserPlugin extends RPUserPlugin {

	protected TauMigrator_Context _context = null;
	protected ConfigurationSettings _settings = null;

	// called when plug-in is loaded
	public void RhpPluginInit(
			final IRPApplication theRhapsodyApp ){

		_context = new TauMigrator_Context( theRhapsodyApp.getApplicationConnectionString() );

		_settings = new ConfigurationSettings(
				"TauMigrator.properties", 
				"TauMigrator_MessagesBundle",
				"TauMigrator",
				_context );

		final String legalNotice = 
				"Copyright (C) 2015-2021  MBSE Training and Consulting Limited (www.executablembse.com)"
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

		String msg = "The TauMigrator component of the SysMLHelperPlugin was loaded successfully.\n" +
				legalNotice +
				"\nNew right-click 'MBSE Method' commands have been added.";		

		_context.info( msg );

		_context.info( "The TauMigrator profile version is " + _context.getPluginVersion() );

		_settings.checkIfSetupProjectIsNeeded( false, true );
	}

	// called when the plug-in pop-up menu  is selected
	public void OnMenuItemSelect(
			String menuItem ){

		try {
			List<IRPModelElement> theSelectedEls = _context.getSelectedElements();
			IRPProject theRhpPrj = _context.get_rhpPrj();

			_context.debug("Starting ("+ theSelectedEls.size() + " elements were selected) ...");

			if( !theSelectedEls.isEmpty() ){

				if( menuItem.equals( _settings.getString( 
						"taumigratorplugin.ImportTauModelFromXML" ) ) ){

					boolean isContinue = checkAndPerformProfileSetupIfNeeded();

					if( isContinue ){

						CreateRhapsodyModelElementsFromXML theCreator = 
								new CreateRhapsodyModelElementsFromXML( _context );

						theCreator.go();

						_context.deleteIfPresent( "Structure1", "StructureDiagram", theRhpPrj );
						_context.deleteIfPresent( "Model1", "ObjectModelDiagram", theRhpPrj );
						_context.deleteIfPresent( "Default", "Package", theRhpPrj );
					}					

				} else if( menuItem.equals( _settings.getString( 
						"taumigratorplugin.SetupTauMigratorProjectProperties" ) ) ){

					checkAndPerformProfileSetupIfNeeded();

				} else {
					_context.warning("Warning in OnMenuItemSelect, " + menuItem + " was not handled.");
				}
			}

			_context.debug("... completed");

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
	}

	@Override
	public void RhpPluginInvokeItem() {
	}

	@Override
	public void OnTrigger(String trigger) {
	}

	private boolean checkAndPerformProfileSetupIfNeeded() {

		boolean isContinue = true;

		boolean isShowInfoDialog = _context.getIsShowProfileVersionCheckDialogs();
		boolean isSetupNeeded = _settings.checkIfSetupProjectIsNeeded( isShowInfoDialog, false );

		if( isSetupNeeded ){

			String theMsg = "The project needs 'Tau Migrator' profile-based properties and tags values to be applied.\n" + 
					"Do you want me to set these properties and tags on the project in order to continue?";

			isContinue = UserInterfaceHelper.askAQuestion( theMsg );

			if( isContinue ){
				_settings.setupProjectWithProperties( "SysML" );
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