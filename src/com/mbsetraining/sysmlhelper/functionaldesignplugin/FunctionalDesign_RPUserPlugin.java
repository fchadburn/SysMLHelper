package com.mbsetraining.sysmlhelper.functionaldesignplugin;

import java.util.ArrayList;
import java.util.List;

import com.mbsetraining.sysmlhelper.common.UserInterfaceHelper;
import com.telelogic.rhapsody.core.*;

public class FunctionalDesign_RPUserPlugin extends RPUserPlugin {

	protected FunctionalDesign_Context _context;
	protected FunctionalDesign_RPApplicationListener _listener = null;

	// called when plug-in is loaded
	public void RhpPluginInit(
			final IRPApplication theRhapsodyApp ){

		_context = new FunctionalDesign_Context( theRhapsodyApp.getApplicationConnectionString() );

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

		String msg = "The FunctionalDesign component of the SysMLHelperPlugin was loaded successfully.\n" +
				legalNotice +
				"\nNew right-click 'MBSE Method' commands have been added.";		

		_context.info( msg );

		_listener = new FunctionalDesign_RPApplicationListener( 
				"FunctionalDesignProfile",
				_context );

		_listener.connect( theRhapsodyApp );

		_context.info( "The FunctionalDesign profile version is " + _context.getProperty( "PluginVersion" ) );

		_context.checkIfSetupProjectIsNeeded( false, true );
	}

	// called when the plug-in pop-up menu  is selected
	public void OnMenuItemSelect(
			String menuItem ){

		try { 
			IRPModelElement theSelectedEl = _context.getSelectedElement( false );
			IRPProject theRhpPrj = _context.get_rhpPrj();
			List<IRPModelElement> theSelectedEls = _context.getSelectedElements();

			_context.debug( "Starting ("+ theSelectedEls.size() + " elements were selected) ..." );

			if( !theSelectedEls.isEmpty() ){

				if( menuItem.equals( _context.getString(
						"functionaldesignplugin.CreateFunctionalDesignPkgStructure" ) ) ){

					if( theSelectedEl instanceof IRPPackage ){
						CreateFunctionalDesignSpecificationPackage.launchTheDialog( _context.get_rhpAppID() );
					}

				} else if( menuItem.equals( _context.getString( 
						"functionaldesignplugin.CreateSampleProjectStructure" ) ) ){

					boolean isContinue = checkAndPerformProfileSetupIfNeeded();

					if( isContinue ){
						_context.addProfileIfNotPresent( "SysML" );
						theRhpPrj.changeTo("SysML");

						List<IRPActor> theMasterActors = 
								_context.getMasterActorList( 
										theRhpPrj );

						@SuppressWarnings("unused")
						DesignSpecificationPackageCreatorFromXML theDesignSpecCreator = 
						new DesignSpecificationPackageCreatorFromXML(
								theRhpPrj, 
								theMasterActors,
								_context );

						TopLevelSystemDesignCreator theTopLevelCreator = new TopLevelSystemDesignCreator( _context );

						theTopLevelCreator.createSampleModel(
								theRhpPrj, 
								theMasterActors,
								_context );

						_context.deleteIfPresent( "Structure1", "StructureDiagram", theRhpPrj );
						_context.deleteIfPresent( "Model1", "ObjectModelDiagram", theRhpPrj );
						_context.deleteIfPresent( "Default", "Package", theRhpPrj );

						//AutoPackageDiagram theAPD = new AutoPackageDiagram( theRhpPrj );
						//theAPD.drawDiagram();

						//PopulateRequirementsAnalysisPkg.createRequirementsAnalysisPkg( (IRPProject) theSelectedEl ); 
					}

				} else if (menuItem.equals(_context.getString("functionaldesignplugin.SetupFunctionalDesignProjectProperties"))){

					checkAndPerformProfileSetupIfNeeded();

				} else {
					_context.warning("Warning in OnMenuItemSelect, " + menuItem + " was not handled.");
				}

				_context.debug("... completed");
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

	public static List<IRPRequirement> getRequirementsThatTraceFrom(
			IRPModelElement theElement){

		List<IRPRequirement> theReqts = new ArrayList<IRPRequirement>();

		@SuppressWarnings("unchecked")
		List<IRPDependency> theExistingDeps = theElement.getDependencies().toList();

		for (IRPDependency theDependency : theExistingDeps) {

			IRPModelElement theDependsOn = theDependency.getDependsOn();

			if (theDependsOn != null && theDependsOn instanceof IRPRequirement){
				theReqts.add( (IRPRequirement) theDependsOn );
			}
		}

		return theReqts;
	}

	public String traceabilityReportHtml( IRPModelElement theModelEl ) {

		String retval = "";

		if( theModelEl != null ){

			List<IRPRequirement> theTracedReqts;

			if( theModelEl instanceof IRPDependency ){

				IRPDependency theDep = (IRPDependency) theModelEl;
				IRPModelElement theDependsOn = theDep.getDependsOn();

				if( theDependsOn != null && 
						theDependsOn instanceof IRPRequirement ){

					// Display text of the requirement that the dependency traces to
					theTracedReqts = new ArrayList<>();
					theTracedReqts.add( (IRPRequirement) theDependsOn );
				} else {
					theTracedReqts = getRequirementsThatTraceFrom( theModelEl );
				}
			} else {
				theTracedReqts = getRequirementsThatTraceFrom( theModelEl );
			}

			if( theTracedReqts.isEmpty() ){

				retval = "<br>This element has no traceability to requirements<br><br>";
			} else {
				retval = "<br><b>Requirements:</b>";				
				retval += "<table border=\"1\">";			
				retval += "<tr><td><b>ID</b></td><td><b>Specification</b></td></tr>";

				for( IRPRequirement theReqt : theTracedReqts ){
					retval += "<tr><td>" + theReqt.getName() + "</td><td>"+ theReqt.getSpecification() +"</tr>";
				}

				retval += "</table><br>";
			}				
		}

		return retval;
	}

	public String InvokeTooltipFormatter(String html) {

		String theOutput = html;

		try{
			@SuppressWarnings("rawtypes")
			List theAppIDs = RhapsodyAppServer.getActiveRhapsodyApplicationIDList();

			if( theAppIDs.size() == 1 ){

				IRPProject theRhpProject = RhapsodyAppServer.getActiveRhapsodyApplication().activeProject();

				String guidStr = html.substring(1, html.indexOf(']'));

				IRPModelElement theModelEl = theRhpProject.findElementByGUID( guidStr );

				if( theModelEl != null ){
					guidStr = theModelEl.getGUID();
				}

				html = html.substring(html.indexOf(']') + 1);

				String thePart1 =  html.substring(
						0,
						html.indexOf("[[<b>Dependencies:</b>"));

				String thePart2 = traceabilityReportHtml( theModelEl );
				String thePart3 = html.substring(html.lastIndexOf("[[<b>Dependencies:</b>") - 1);

				theOutput = thePart1 + thePart2 + thePart3;
			}

		} catch (Exception e) {
			_context.error("Unhandled exception in InvokeTooltipFormatter");
		}

		return theOutput;
	}

	@Override
	public void OnTrigger(String trigger) {

	}

	private boolean checkAndPerformProfileSetupIfNeeded() {

		boolean isContinue = true;

		boolean isShowInfoDialog = _context.getIsShowProfileVersionCheckDialogs();
		boolean isSetupNeeded = _context.checkIfSetupProjectIsNeeded( isShowInfoDialog, false );

		if( isSetupNeeded ){

			String theMsg = "The project needs 'Functional Design' profile-based properties and tags values to be applied.\n" + 
					"Do you want me to set these properties and tags on the project in order to continue?";

			isContinue = UserInterfaceHelper.askAQuestion( theMsg );

			if( isContinue ){
				_context.setupProjectWithProperties();
			}
		}
		return isContinue;
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