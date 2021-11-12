package com.mbsetraining.sysmlhelper.sysmlhelperplugin;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import requirementsanalysisplugin.PopulateRelatedRequirementsPanel;
import com.mbsetraining.sysmlhelper.activitydiagram.ActivityDiagramChecker;
import com.mbsetraining.sysmlhelper.activitydiagram.RenameActions;
import com.mbsetraining.sysmlhelper.common.ConfigurationSettings;
import com.mbsetraining.sysmlhelper.common.DependencySelector;
import com.mbsetraining.sysmlhelper.common.LayoutHelper;
import com.mbsetraining.sysmlhelper.common.NestedActivityDiagram;
import com.mbsetraining.sysmlhelper.common.RequirementsHelper;
import com.mbsetraining.sysmlhelper.common.UserInterfaceHelper;
import com.mbsetraining.sysmlhelper.gateway.CreateGatewayProjectPanel;
import com.mbsetraining.sysmlhelper.gateway.MarkedAsDeletedPanel;
import com.mbsetraining.sysmlhelper.gateway.MoveRequirements;
import com.mbsetraining.sysmlhelper.rolluptraceabilitytotransition.RollUpTraceabilityToTheTransitionPanel;
import com.mbsetraining.sysmlhelper.sequencediagram.VerificationDependencyUpdater;
import com.mbsetraining.sysmlhelper.smartlink.EndlinkPanel;
import com.telelogic.rhapsody.core.*;

public class SysMLHelper_RPUserPlugin extends RPUserPlugin {

	protected SysMLHelper_Context _context;
	protected SysMLHelper_RPApplicationListener _listener = null;
	protected List<String> _startLinkGuids = new ArrayList<>();
	protected ConfigurationSettings _settings;

	// called when plug-in is loaded
	public void RhpPluginInit(
			final IRPApplication theRhapsodyApp ){

		String theAppID = theRhapsodyApp.getApplicationConnectionString();

		_context = new SysMLHelper_Context( theAppID );
		
		_settings = new ConfigurationSettings(
				"ExecutableMBSE.properties", 
				"ExecutableMBSE_MessagesBundle",
				"ExecutableMBSE" , 
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

		String msg = "The SysMLHelper component of the SysMLHelperPlugin was loaded successfully.\n" +
				legalNotice +
				"\nNew right-click 'MBSE Method' commands have been added.";		

		_context.info( msg );

		_listener = new SysMLHelper_RPApplicationListener( 
				"SysMLHelperProfile",
				_context );

		_listener.connect( theRhapsodyApp );

		_context.info( "The SysMLHelper profile version is " + _context.getPluginVersion() );

		_settings.checkIfSetupProjectIsNeeded( false, true );
	}

	// called when the plug-in pop-up menu  is selected
	public void OnMenuItemSelect(String menuItem) {

		try { 
			String theAppID = _context.get_rhpAppID();

			IRPModelElement theSelectedEl = _context.getSelectedElement( false );
			List<IRPModelElement> theSelectedEls = _context.getSelectedElements();
			List<IRPGraphElement> theSelectedGraphEls = _context.getSelectedGraphElements();

			_context.debug("Starting ("+ theSelectedEls.size() + " elements were selected) ...");

			if( !theSelectedEls.isEmpty() ){

				if (menuItem.equals(_settings.getString("sysmlhelperplugin.CreateRAStructureMenu"))){

					if (theSelectedEl instanceof IRPProject){

						PopulateRequirementsAnalysisPkg thePopulator = 
								new PopulateRequirementsAnalysisPkg( _context, _settings );
						
						thePopulator.createRequirementsAnalysisPkg(); 
					}

				} else if (menuItem.equals(_settings.getString("sysmlhelperplugin.SetupRAProperties"))){

					if( theSelectedEl instanceof IRPPackage ){
						_settings.checkIfSetupProjectIsNeeded( true );
					} else {
						_context.error( menuItem + " invoked out of context and only works for packages" );
					}

				} else if (menuItem.equals(_settings.getString("sysmlhelperplugin.QuickHyperlinkMenu"))){

					IRPHyperLink theHyperLink = (IRPHyperLink) theSelectedEl.addNewAggr("HyperLink", "");
					theHyperLink.setDisplayOption(HYPNameType.RP_HYP_NAMETEXT, "");
					theHyperLink.highLightElement();
					theHyperLink.openFeaturesDialog(0);

				} else if (menuItem.equals(_settings.getString("sysmlhelperplugin.SelectDependsOnElementsMenu"))){

					Set<IRPModelElement> theCombinedSet = 
							_context.getSetOfElementsFromCombiningThe(
									theSelectedEls, theSelectedGraphEls );

					DependencySelector theSelector = new DependencySelector( _context );

					theSelector.selectDependsOnElementsFor( 
							theCombinedSet, null );

				} else if (menuItem.equals(_settings.getString("sysmlhelperplugin.SelectDependentElementsMenu"))){

					Set<IRPModelElement> theCombinedSet = 
							_context.getSetOfElementsFromCombiningThe(
									theSelectedEls, theSelectedGraphEls );

					DependencySelector theSelector = new DependencySelector( _context );

					theSelector.selectDependentElementsFor( 
							theCombinedSet, null );

				} else if (menuItem.equals(_settings.getString("sysmlhelperplugin.SelectDependsOnDeriveOnlyElementsMenu"))){

					Set<IRPModelElement> theCombinedSet = 
							_context.getSetOfElementsFromCombiningThe(
									theSelectedEls, theSelectedGraphEls );

					DependencySelector theSelector = new DependencySelector( _context );

					theSelector.selectDependsOnElementsFor( 
							theCombinedSet, "derive" );

				} else if (menuItem.equals(_settings.getString("sysmlhelperplugin.SelectDependentDeriveOnlyElementsMenu"))){

					Set<IRPModelElement> theCombinedSet = 
							_context.getSetOfElementsFromCombiningThe(
									theSelectedEls, theSelectedGraphEls );

					DependencySelector theSelector = new DependencySelector( _context );

					theSelector.selectDependentElementsFor( 
							theCombinedSet, "derive" );

				} else if (menuItem.equals(_settings.getString("sysmlhelperplugin.SelectDependsOnSatisfyOnlyElementsMenu"))){

					Set<IRPModelElement> theCombinedSet = 
							_context.getSetOfElementsFromCombiningThe(
									theSelectedEls, theSelectedGraphEls );

					DependencySelector theSelector = new DependencySelector( _context );

					theSelector.selectDependsOnElementsFor( 
							theCombinedSet, "satisfy" );

				} else if (menuItem.equals(_settings.getString("sysmlhelperplugin.SelectDependentSatisfyOnlyElementsMenu"))){

					Set<IRPModelElement> theCombinedSet = 
							_context.getSetOfElementsFromCombiningThe(
									theSelectedEls, theSelectedGraphEls );

					DependencySelector theSelector = new DependencySelector( _context );

					theSelector.selectDependentElementsFor( 
							theCombinedSet, "satisfy" );

				} else if (menuItem.equals(_settings.getString("sysmlhelperplugin.SelectDependsOnVerifyOnlyElementsMenu"))){

					Set<IRPModelElement> theCombinedSet = 
							_context.getSetOfElementsFromCombiningThe(
									theSelectedEls, theSelectedGraphEls );

					DependencySelector theSelector = new DependencySelector( _context );

					theSelector.selectDependsOnElementsFor( 
							theCombinedSet, "verify" );

				} else if (menuItem.equals(_settings.getString("sysmlhelperplugin.SelectDependentVerifyOnlyElementsMenu"))){

					Set<IRPModelElement> theCombinedSet = 
							_context.getSetOfElementsFromCombiningThe(
									theSelectedEls, theSelectedGraphEls );

					DependencySelector theSelector = new DependencySelector( _context );

					theSelector.selectDependentElementsFor( 
							theCombinedSet, "verify" );

				} else if (menuItem.equals(_settings.getString("sysmlhelperplugin.SelectDependsOnRefineOnlyElementsMenu"))){

					Set<IRPModelElement> theCombinedSet = 
							_context.getSetOfElementsFromCombiningThe(
									theSelectedEls, theSelectedGraphEls );

					DependencySelector theSelector = new DependencySelector( _context );

					theSelector.selectDependsOnElementsFor( 
							theCombinedSet, "refine" );

				} else if (menuItem.equals(_settings.getString("sysmlhelperplugin.SelectDependentRefineOnlyElementsMenu"))){

					Set<IRPModelElement> theCombinedSet = 
							_context.getSetOfElementsFromCombiningThe(
									theSelectedEls, theSelectedGraphEls );

					DependencySelector theSelector = new DependencySelector( _context );

					theSelector.selectDependentElementsFor( 
							theCombinedSet, "refine" );

				} else if (menuItem.equals(_settings.getString("sysmlhelperplugin.SelectDependsOnDeriveReqtOnlyElementsMenu"))){

					Set<IRPModelElement> theCombinedSet = 
							_context.getSetOfElementsFromCombiningThe(
									theSelectedEls, theSelectedGraphEls );

					DependencySelector theSelector = new DependencySelector( _context );

					theSelector.selectDependsOnElementsFor( 
							theCombinedSet, "deriveReqt" );

				} else if (menuItem.equals(_settings.getString("sysmlhelperplugin.SelectDependentDeriveReqtOnlyElementsMenu"))){

					Set<IRPModelElement> theCombinedSet = 
							_context.getSetOfElementsFromCombiningThe(
									theSelectedEls, theSelectedGraphEls );

					DependencySelector theSelector = new DependencySelector( _context );

					theSelector.selectDependentElementsFor( 
							theCombinedSet, "deriveReqt" );

				} else if (menuItem.equals(_settings.getString("sysmlhelperplugin.SetupGatewayProjectMenu"))){

					if (theSelectedEl instanceof IRPProject){
						CreateGatewayProjectPanel.launchThePanel( _context.get_rhpAppID(), ".*.rqtf$" );				
					}

				} else if (menuItem.equals(_settings.getString("sysmlhelperplugin.AddRelativeUnitMenu"))){

					_context.browseAndAddUnit( theSelectedEl.getProject(), true );								

					// Requirements Analysis
				} else if (menuItem.equals(_settings.getString( "requirementsanalysisplugin.CreateNestedADMenu" ))){

					NestedActivityDiagram theHelper = new NestedActivityDiagram(_context);
					theHelper.createNestedActivityDiagramsFor( theSelectedEls );

				} else if (menuItem.equals(_settings.getString( "requirementsanalysisplugin.ReportOnNamingAndTraceabilityMenu" ))){

					ActivityDiagramChecker.launchPanelsFor( theSelectedEls, _context );

				} else if (menuItem.equals(_settings.getString( "requirementsanalysisplugin.MoveUnclaimedReqtsMenu" ))){

					MoveRequirements theMover = new MoveRequirements( _context );

					theMover.moveUnclaimedRequirementsReadyForGatewaySync( 
							theSelectedEls, 
							_context.get_rhpPrj() );

				} else if (menuItem.equals(_settings.getString( "requirementsanalysisplugin.CreateNewRequirementMenu" ))){

					RequirementsHelper theHelper = new RequirementsHelper( _context );
					theHelper.createNewRequirementsFor( theSelectedGraphEls );

				} else if (menuItem.equals(_settings.getString( "requirementsanalysisplugin.PerformRenameInBrowserMenu" ))){

					RenameActions theRenamer = new RenameActions(_context);
					theRenamer.performRenamesFor( theSelectedEls );

				} else if (menuItem.equals(_settings.getString( "requirementsanalysisplugin.UpdateNestedADNamesMenu" ))){

					NestedActivityDiagram theHelper = new NestedActivityDiagram(_context);

					theHelper.renameNestedActivityDiagramsFor( 
							theSelectedEls );

				} else if (menuItem.equals(_settings.getString( "requirementsanalysisplugin.DeleteTaggedAsDeletedAtHighLevelMenu" ))){

					MarkedAsDeletedPanel.launchThePanel( theAppID );

				} else if (menuItem.equals(_settings.getString( "requirementsanalysisplugin.StartLinkMenu" ))){

					_startLinkGuids.clear();

					for( IRPModelElement theEl : theSelectedEls ){
						_startLinkGuids.add( theEl.getGUID() );
					}

				} else if (menuItem.equals(_settings.getString( "requirementsanalysisplugin.EndLinkMenu" ))){

					if( _startLinkGuids.isEmpty() ){

						UserInterfaceHelper.showWarningDialog( "You need to Start a link before you can end it" );
					} else {
						EndlinkPanel.launchThePanel( 
								theAppID, 
								_startLinkGuids );
					}

				} else if (menuItem.equals(_settings.getString( "requirementsanalysisplugin.RollUpTraceabilityUpToTransitionLevel" ))){

					if( theSelectedGraphEls != null ){
						IRPGraphElement theSelectedGraphEl = theSelectedGraphEls.get( 0 );
						RollUpTraceabilityToTheTransitionPanel.launchThePanel( theSelectedGraphEl, _context );
					}

				} else if (menuItem.equals(_settings.getString( "requirementsanalysisplugin.layoutDependencies" ))){

					if( theSelectedGraphEls.size() > 0 ){

						LayoutHelper theHelper = new LayoutHelper( _context );

						theHelper.centerDependenciesForTheGraphEls( 
								theSelectedGraphEls );

					} else if( theSelectedEl.getMetaClass().equals( "ActivityDiagramGE" ) ){

						LayoutHelper theHelper = new LayoutHelper( _context );

						theHelper.centerDependenciesForTheDiagram( 
								(IRPDiagram) theSelectedEl );

					} else if( theSelectedEl.getMetaClass().equals( "ActivityDiagram" ) ){

						@SuppressWarnings("unchecked")
						List<IRPModelElement> theDiagrams = 
						theSelectedEl.getNestedElementsByMetaClass( 
								"ActivityDiagramGE", 0 ).toList();

						if( theDiagrams.size()==1 ){

							LayoutHelper theHelper = new LayoutHelper( _context );

							theHelper.centerDependenciesForTheDiagram( 
									(IRPDiagram) theDiagrams.get( 0 ) );
						} else {
							_context.error( "Error in OnMenuItemSelect, unable to find an ActivityDiagramGE" );
						}

					} else if( theSelectedEl instanceof IRPDiagram ){

						LayoutHelper theHelper = new LayoutHelper( _context );

						theHelper.centerDependenciesForTheDiagram( 
								(IRPDiagram) theSelectedEl );

					} else if( theSelectedEl instanceof IRPPackage ){

						LayoutHelper theHelper = new LayoutHelper( _context );

						theHelper.centerDependenciesForThePackage( 
								(IRPPackage) theSelectedEl );
					}				

				} else if (menuItem.equals(_settings.getString("requirementsanalysisplugin.PopulateRequirementsForSDsMenu"))){

					if (theSelectedEl instanceof IRPSequenceDiagram){
						PopulateRelatedRequirementsPanel.launchThePanel( theAppID );
					}

				} else if (menuItem.equals(_settings.getString("requirementsanalysisplugin.UpdateVerificationDependenciesForSDsMenu"))){		

					if( !theSelectedEls.isEmpty() ){

						VerificationDependencyUpdater theHelper = 
								new VerificationDependencyUpdater( _context );

						theHelper.updateVerificationsForSequenceDiagramsBasedOn( theSelectedEls );
					}			

				} else {
					_context.error( _context.elInfo( theSelectedEl ) + " was invoked with menuItem='" + menuItem + "'");
				}
			}

			_context.debug("... completed");


		} catch (Exception e) {
			_context.error("Error: Exception in OnMenuItemSelect, e=" + e.getMessage() );
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

	// For use with Tables
	public String getRequirementSpecificationText(String guid) {

		//Logger.writeLine("Was invoked with guid " + guid);
		String theSpec = "Not found";

		try {
			@SuppressWarnings("rawtypes")
			List theAppIDs = RhapsodyAppServer.getActiveRhapsodyApplicationIDList();

			if( theAppIDs.size() == 1 ){

				IRPProject theRhpProject = RhapsodyAppServer.getActiveRhapsodyApplication().activeProject();
				IRPModelElement theEl = theRhpProject.findElementByGUID(guid);

				if (theEl != null){

					if (theEl instanceof IRPRequirement){

						IRPRequirement theReq = (IRPRequirement)theEl;
						theSpec = theReq.getSpecification();

					} else if (theEl instanceof IRPDependency){

						IRPDependency theDep = (IRPDependency)theEl;
						IRPModelElement theDependsOn = theDep.getDependsOn();

						if (theDependsOn instanceof IRPRequirement){

							IRPRequirement theReq = (IRPRequirement)theDependsOn;
							theSpec = theReq.getSpecification();

							_context.debug(_context.elInfo( theDependsOn )+ "is the depends on with the text '" + theSpec + "'");
						}
					}
				} else {
					_context.error("Error: getRequirementSpecificationText unable to find element with guid=" + guid );
				}
			}
		} catch (Exception e) {
			_context.error("Unhandled exception in getRequirementSpecificationText" );
		}

		return theSpec;
	}
}

/**
 * Copyright (C) 2016-2021  MBSE Training and Consulting Limited (www.executablembse.com)

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