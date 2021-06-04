package taumigrator;

import java.util.ArrayList;
import java.util.List;

import com.telelogic.rhapsody.core.*;

public class TauMigrator_RPUserPlugin extends RPUserPlugin {

	protected TauMigrator_Context _context = null;
                                                                                                                                                                                      
	// called when plug-in is loaded
	public void RhpPluginInit(
			final IRPApplication theRhapsodyApp ){
		 
		_context = new TauMigrator_Context( theRhapsodyApp.getApplicationConnectionString() );

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

		_context.info( "The TauMigrator profile version is " + _context.getProperty( "PluginVersion" ) );

		_context.checkIfSetupProjectIsNeeded( false, true );
	}
	
	// called when the plug-in pop-up menu  is selected
	public void OnMenuItemSelect(
			String menuItem ){
			
		try {
			IRPModelElement theSelectedEl = _context.getSelectedElement();
			List<IRPModelElement> theSelectedEls = _context.getSelectedElements();
			IRPProject theRhpPrj = _context.get_rhpPrj();

			_context.debug("Starting ("+ theSelectedEls.size() + " elements were selected) ...");

			if( !theSelectedEls.isEmpty() ){

				if( menuItem.equals( _context.getString( 
						"taumigratorplugin.ImportTauModelFromXML" ) ) ){

						if( theSelectedEl instanceof IRPPackage ){
													
							_context.addProfileIfNotPresent( "SysML", theRhpPrj );
							theRhpPrj.changeTo("SysML");
							
							try { 
								ProfileVersionManager.checkAndSetProfileVersion( true, m_configSettings, true );

								CreateRhapsodyModelElementsFromXML theCreator = 
										new CreateRhapsodyModelElementsFromXML(
												theRhpApp );
								
								theCreator.go();
								
								_context.deleteIfPresent( "Structure1", "StructureDiagram", theRhpPrj );
								_context.deleteIfPresent( "Model1", "ObjectModelDiagram", theRhpPrj );
								_context.deleteIfPresent( "Default", "Package", theRhpPrj );
								
								//AutoPackageDiagram theAPD = new AutoPackageDiagram( theRhpPrj );
								//theAPD.drawDiagram();
								
								//PopulateRequirementsAnalysisPkg.createRequirementsAnalysisPkg( (IRPProject) theSelectedEl ); 

							} catch (Exception e) {
								_context.writeLine("Error: Exception in OnMenuItemSelect when invoking PopulateRequirementsAnalysisPkg.createRequirementsAnalysisPkg");
							}
						}					
				} else if( menuItem.equals( _context.getString( 
						"taumigratorplugin.SetupTauMigratorProjectProperties" ) ) ){

						if (theSelectedEl instanceof IRPPackage){
							
							try {
								ProfileVersionManager.checkAndSetProfileVersion( 
										true, m_configSettings, true );

							} catch (Exception e) {
								_context.writeLine("Error: Exception in OnMenuItemSelect when invoking PopulateRequirementsAnalysisPkg.createRequirementsAnalysisPkg");
							}
						}
						
				} else {
					_context.writeLine("Warning in OnMenuItemSelect, " + menuItem + " was not handled.");
				}
			}

			_context.writeLine("... completed");
		} catch( Exception e ){
			_context.error( "Exception in OnMenuItemSelect, e=" + e.getMessage() );
		}
	}

	// if true is returned the plugin will be unloaded
	public boolean RhpPluginCleanup() {

		m_rhpApplication = null;
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
}

/**
 * Copyright (C) 2018-2021  MBSE Training and Consulting Limited (www.executablembse.com)

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