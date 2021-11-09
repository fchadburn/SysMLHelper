package com.mbsetraining.sysmlhelper.executablembse;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import requirementsanalysisplugin.PopulateRelatedRequirementsPanel;
import functionalanalysisplugin.PopulateFunctionalAnalysisPkg.SimulationType;

import com.mbsetraining.sysmlhelper.activitydiagram.ActivityDiagramChecker;
import com.mbsetraining.sysmlhelper.activitydiagram.RenameActions;
import com.mbsetraining.sysmlhelper.common.ConfigurationSettings;
import com.mbsetraining.sysmlhelper.common.DependencySelector;
import com.mbsetraining.sysmlhelper.common.LayoutHelper;
import com.mbsetraining.sysmlhelper.common.NestedActivityDiagram;
import com.mbsetraining.sysmlhelper.common.RequirementsHelper;
import com.mbsetraining.sysmlhelper.common.UserInterfaceHelper;
import com.mbsetraining.sysmlhelper.contextdiagram.CreateContextPackagePanel;
import com.mbsetraining.sysmlhelper.copyactivitydiagram.CopyActivityDiagramsPanel;
import com.mbsetraining.sysmlhelper.createactorpart.CreateNewActorPanel;
import com.mbsetraining.sysmlhelper.createnewblockpart.CreateNewBlockPartPanel;
import com.mbsetraining.sysmlhelper.createtestcase.TestCaseCreator;
import com.mbsetraining.sysmlhelper.doorsng.ExportRequirementsToCSV;
import com.mbsetraining.sysmlhelper.doorsng.SwitchRhapsodyRequirementsToDNG;
import com.mbsetraining.sysmlhelper.eventdeletor.EventDeletor;
import com.mbsetraining.sysmlhelper.executablembse.CreateFunctionalExecutablePackagePanel;
import com.mbsetraining.sysmlhelper.gateway.CreateGatewayProjectPanel;
import com.mbsetraining.sysmlhelper.gateway.MarkedAsDeletedPanel;
import com.mbsetraining.sysmlhelper.gateway.MoveRequirements;
import com.mbsetraining.sysmlhelper.populateparts.PopulatePartsPanel;
import com.mbsetraining.sysmlhelper.pubsubportcreation.PortCreator;
import com.mbsetraining.sysmlhelper.rolluptraceabilitytotransition.RollUpTraceabilityToTheTransitionPanel;
import com.mbsetraining.sysmlhelper.sequencediagram.SequenceDiagramCreator;
import com.mbsetraining.sysmlhelper.sequencediagram.UpdateInferfacesBasedOnSequenceDiagramPanel;
import com.mbsetraining.sysmlhelper.sequencediagram.VerificationDependencyUpdater;
import com.mbsetraining.sysmlhelper.smartlink.EndlinkPanel;
import com.mbsetraining.sysmlhelper.tracedelementpanels.CreateIncomingEventPanel;
import com.mbsetraining.sysmlhelper.tracedelementpanels.CreateOperationPanel;
import com.mbsetraining.sysmlhelper.tracedelementpanels.CreateOutgoingEventPanel;
import com.mbsetraining.sysmlhelper.tracedelementpanels.CreateTracedAttributePanel;
import com.mbsetraining.sysmlhelper.tracedelementpanels.UpdateTracedAttributePanel;
import com.mbsetraining.sysmlhelper.usecasepackage.CreateUseCasesPackagePanel;
import com.telelogic.rhapsody.core.*;

public class ExecutableMBSE_RPUserPlugin extends RPUserPlugin {

	protected ExecutableMBSE_Context _context;
	protected ConfigurationSettings _settings;
	protected ExecutableMBSE_RPApplicationListener _listener = null;
	protected List<String> _startLinkGuids = new ArrayList<>();

	// called when plug-in is loaded
	public void RhpPluginInit(
			final IRPApplication theRhapsodyApp ){

		String theAppID = theRhapsodyApp.getApplicationConnectionString();

		try {
			_context = new ExecutableMBSE_Context( theAppID );
			
			_settings = new ConfigurationSettings(
					"ExecutableMBSE.properties", 
					"ExecutableMBSE_MessagesBundle",
					"ExecutableMBSE" , 
					_context );

		} catch( Exception e ){
			_context.error( "Exception in RhpPluginInit, e=" + e.getMessage() );
		}

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

		String msg = "The ExecutableMBSE component of the SysMLHelperPlugin was loaded successfully.\n" +
				legalNotice +
				"\nNew right-click 'MBSE Method' commands have been added.";		

		_context.info( msg );

		_listener = new ExecutableMBSE_RPApplicationListener( 
				"ExecutableMBSEProfile",
				_context );

		_listener.connect( theRhapsodyApp );

		_context.info( "The ExecutableMBSE profile version is " + _context.getPluginVersion() );

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
						"executablembseplugin.CreateContextPackageMenu" ) ) ){

					if( theSelectedEl instanceof IRPPackage ){

						boolean isContinue = checkAndPerformProfileSetupIfNeeded();

						if( isContinue ){
							CreateContextPackagePanel.launchTheDialog( theAppID );
						}

					} else {
						_context.error( menuItem + " invoked out of context and only works for packages" );
					}

				} else if( menuItem.equals( _settings.getString(
						"executablembseplugin.CreateRAStructureMenu" ) ) ){

					if( theSelectedEl instanceof IRPPackage ){

						boolean isContinue = checkAndPerformProfileSetupIfNeeded();

						if( isContinue ){
							CreateUseCasesPackagePanel.launchTheDialog( theAppID );
						}	

					} else {
						_context.error( menuItem + " invoked out of context and only works for packages" );
					}

				} else if( menuItem.equals( _settings.getString(
						"executablembseplugin.SetupRAProperties" ) ) ){

					if( theSelectedEl instanceof IRPPackage ){
						_settings.checkIfSetupProjectIsNeeded( true );
						_context.cleanUpModelRemnants();
					} else {
						_context.error( menuItem + " invoked out of context and only works for packages" );
					}

				} else if( menuItem.equals( _settings.getString(
						"executablembseplugin.PopulatePartsMenu" ) ) ){

					if( theSelectedEl instanceof IRPClassifier || 
							theSelectedEl instanceof IRPInstance ||
							theSelectedEl instanceof IRPStructureDiagram ){

						PopulatePartsPanel.launchTheDialog( theAppID );

					} else {
						_context.error( menuItem + " invoked out of context and only works for classes, objects, or structure diagrams/ibds" );
					}
					
				} else if( menuItem.equals( _settings.getString(
						"executablembseplugin.GenerateSequenceDiagramMenu" ) ) ){

					if( theSelectedEl instanceof IRPClass ){

						SequenceDiagramCreator theCreator = new SequenceDiagramCreator( _context );
						
						theCreator.createSequenceDiagramFor( 
								(IRPClass)theSelectedEl, 
								_context.getOwningPackageFor(theSelectedEl), 
								"SD - " + theSelectedEl.getName(), 
								false, 
								true,
								false );

					} else {
						_context.error( menuItem + " invoked out of context and only works for classes" );
					}
					
				} else if( menuItem.equals( _settings.getString(
						"executablembseplugin.CreateFullSimFAStructureMenu" ) ) ){

					if( theSelectedEl instanceof IRPProject ){

						boolean isProceed = UserInterfaceHelper.askAQuestion(
								"The project may need some settings changed. \n" +
								"Do you want to proceed?");

						if( isProceed ){

							_settings.setPropertiesValuesRequestedInConfigFile( 
									_context.get_rhpPrj(),
									"setPropertyForFunctionalAnalysisModel" );

							CreateFunctionalExecutablePackagePanel.launchThePanel( 
									SimulationType.FullSim,
									theAppID );

						} else {
							_context.debug( "User chose not to proceed" );	
						}
					} else {
						_context.error( menuItem + " invoked out of context and only works for packages" );
					} 

				} else if (menuItem.equals(_settings.getString(
						"executablembseplugin.QuickHyperlinkMenu" ) ) ){

					IRPHyperLink theHyperLink = (IRPHyperLink) theSelectedEl.addNewAggr("HyperLink", "");
					theHyperLink.setDisplayOption(HYPNameType.RP_HYP_NAMETEXT, "");
					theHyperLink.highLightElement();
					theHyperLink.openFeaturesDialog(0);

				} else if( menuItem.equals( _settings.getString(
						"executablembseplugin.SelectDependsOnElementsMenu" ) ) ){

					Set<IRPModelElement> theCombinedSet = 
							_context.getSetOfElementsFromCombiningThe(
									theSelectedEls, theSelectedGraphEls );

					DependencySelector theSelector = new DependencySelector( _context );

					theSelector.selectDependsOnElementsFor( 
							theCombinedSet, null );

				} else if( menuItem.equals( _settings.getString(
						"executablembseplugin.SelectDependentElementsMenu" ) ) ){

					Set<IRPModelElement> theCombinedSet = 
							_context.getSetOfElementsFromCombiningThe(
									theSelectedEls, theSelectedGraphEls );

					DependencySelector theSelector = new DependencySelector( _context );

					theSelector.selectDependentElementsFor( 
							theCombinedSet, null );

				} else if( menuItem.equals( _settings.getString(
						"executablembseplugin.SelectDependsOnDeriveOnlyElementsMenu" ) ) ){

					Set<IRPModelElement> theCombinedSet = 
							_context.getSetOfElementsFromCombiningThe(
									theSelectedEls, theSelectedGraphEls );

					DependencySelector theSelector = new DependencySelector( _context );

					theSelector.selectDependsOnElementsFor( 
							theCombinedSet, "derive" );

				} else if( menuItem.equals( _settings.getString(
						"executablembseplugin.SelectDependentDeriveOnlyElementsMenu" ) ) ){

					Set<IRPModelElement> theCombinedSet = 
							_context.getSetOfElementsFromCombiningThe(
									theSelectedEls, theSelectedGraphEls );

					DependencySelector theSelector = new DependencySelector( _context );

					theSelector.selectDependentElementsFor( 
							theCombinedSet, "derive" );

				} else if( menuItem.equals( _settings.getString(
						"executablembseplugin.SelectDependsOnSatisfyOnlyElementsMenu" ) ) ){

					Set<IRPModelElement> theCombinedSet = 
							_context.getSetOfElementsFromCombiningThe(
									theSelectedEls, theSelectedGraphEls );

					DependencySelector theSelector = new DependencySelector( _context );

					theSelector.selectDependsOnElementsFor( 
							theCombinedSet, "satisfy" );

				} else if( menuItem.equals( _settings.getString(
						"executablembseplugin.SelectDependentSatisfyOnlyElementsMenu" ) ) ){

					Set<IRPModelElement> theCombinedSet = 
							_context.getSetOfElementsFromCombiningThe(
									theSelectedEls, theSelectedGraphEls );

					DependencySelector theSelector = new DependencySelector( _context );

					theSelector.selectDependentElementsFor( 
							theCombinedSet, "satisfy" );

				} else if( menuItem.equals( _settings.getString(
						"executablembseplugin.SelectDependsOnVerifyOnlyElementsMenu" ) ) ){

					Set<IRPModelElement> theCombinedSet = 
							_context.getSetOfElementsFromCombiningThe(
									theSelectedEls, theSelectedGraphEls );

					DependencySelector theSelector = new DependencySelector( _context );

					theSelector.selectDependsOnElementsFor( 
							theCombinedSet, "verify" );

				} else if( menuItem.equals( _settings.getString(
						"executablembseplugin.SelectDependentVerifyOnlyElementsMenu" ) ) ){

					Set<IRPModelElement> theCombinedSet = 
							_context.getSetOfElementsFromCombiningThe(
									theSelectedEls, theSelectedGraphEls );

					DependencySelector theSelector = new DependencySelector( _context );

					theSelector.selectDependentElementsFor( 
							theCombinedSet, "verify" );

				} else if( menuItem.equals( _settings.getString(
						"executablembseplugin.SelectDependsOnRefineOnlyElementsMenu" ) ) ){

					Set<IRPModelElement> theCombinedSet = 
							_context.getSetOfElementsFromCombiningThe(
									theSelectedEls, theSelectedGraphEls );

					DependencySelector theSelector = new DependencySelector( _context );

					theSelector.selectDependsOnElementsFor( 
							theCombinedSet, "refine" );

				} else if( menuItem.equals( _settings.getString(
						"executablembseplugin.SelectDependentRefineOnlyElementsMenu" ) ) ){

					Set<IRPModelElement> theCombinedSet = 
							_context.getSetOfElementsFromCombiningThe(
									theSelectedEls, theSelectedGraphEls );

					DependencySelector theSelector = new DependencySelector( _context );

					theSelector.selectDependentElementsFor( 
							theCombinedSet, "refine" );

				} else if( menuItem.equals( _settings.getString(
						"executablembseplugin.SelectDependsOnDeriveReqtOnlyElementsMenu" ) ) ){

					Set<IRPModelElement> theCombinedSet = 
							_context.getSetOfElementsFromCombiningThe(
									theSelectedEls, theSelectedGraphEls );

					DependencySelector theSelector = new DependencySelector( _context );

					theSelector.selectDependsOnElementsFor( 
							theCombinedSet, "deriveReqt" );

				} else if( menuItem.equals( _settings.getString(
						"executablembseplugin.SelectDependentDeriveReqtOnlyElementsMenu" ) ) ){

					Set<IRPModelElement> theCombinedSet = 
							_context.getSetOfElementsFromCombiningThe(
									theSelectedEls, theSelectedGraphEls );

					DependencySelector theSelector = new DependencySelector( _context );

					theSelector.selectDependentElementsFor( 
							theCombinedSet, "deriveReqt" );

				} else if (menuItem.equals(_settings.getString(
						"executablembseplugin.SetupGatewayProjectMenu" ) ) ){

					if (theSelectedEl instanceof IRPProject){
						CreateGatewayProjectPanel.launchThePanel( theAppID, ".*.rqtf$" );				
					}

				} else if (menuItem.equals( _settings.getString("executablembseplugin.AddRelativeUnitMenu"))){

					_context.browseAndAddUnit( theSelectedEl.getProject(), true );

				} else if( menuItem.equals( _settings.getString( 
						"executablembseplugin.CreateNestedADMenu" ) ) ){

					NestedActivityDiagram theHelper = new NestedActivityDiagram(_context);
					theHelper.createNestedActivityDiagramsFor( theSelectedEls );

				} else if( menuItem.equals( _settings.getString( 
						"executablembseplugin.ReportOnNamingAndTraceabilityMenu" ) ) ){

					ActivityDiagramChecker.launchPanelsFor( theSelectedEls, _context );

				} else if( menuItem.equals( _settings.getString( 
						"executablembseplugin.AutoRenameActions" ) ) ){

					RenameActions theRenamer = new RenameActions( _context );
					theRenamer.performRenamesFor( theSelectedEls );
										
				} else if( menuItem.equals( _settings.getString( 
						"executablembseplugin.MoveUnclaimedReqtsMenu" ) ) ){

					MoveRequirements theMover = new MoveRequirements( _context );

					theMover.moveUnclaimedRequirementsReadyForGatewaySync( 
							theSelectedEls, 
							_context.get_rhpPrj() );

				} else if( menuItem.equals( _settings.getString( 
						"executablembseplugin.CreateNewRequirementMenu" ) ) ){

					RequirementsHelper theHelper = new RequirementsHelper( _context );
					theHelper.createNewRequirementsFor( theSelectedGraphEls );

				} else if (menuItem.equals( _settings.getString( 
						"executablembseplugin.PerformRenameInBrowserMenu" ))){

					RenameActions theRenamer = new RenameActions(_context);
					theRenamer.performRenamesFor( theSelectedEls );

				} else if( menuItem.equals( _settings.getString( 
						"executablembseplugin.UpdateNestedADNamesMenu" ) ) ){

					NestedActivityDiagram theHelper = new NestedActivityDiagram(_context);

					theHelper.renameNestedActivityDiagramsFor( 
							theSelectedEls );

				} else if( menuItem.equals( _settings.getString( 
						"executablembseplugin.DeleteTaggedAsDeletedAtHighLevelMenu" ))){

					MarkedAsDeletedPanel.launchThePanel( theAppID );

				} else if( menuItem.equals( _settings.getString( 
						"executablembseplugin.ExportRequirementsToCsvForImportIntoDOORSNG" ))){

					ExportRequirementsToCSV theExporter = new ExportRequirementsToCSV( _context );

					theExporter.exportRequirementsToCSVUnderSelectedEl();

				} else if( menuItem.equals( _settings.getString( 
						"executablembseplugin.SwitchRequirementsToDOORSNG" ))){

					SwitchRhapsodyRequirementsToDNG theSwitcher = 
							new SwitchRhapsodyRequirementsToDNG( _context );

					theSwitcher.SwitchRequirements();

				} else if( menuItem.equals( _settings.getString( 
						"executablembseplugin.StartLinkMenu" ) ) ){

					_startLinkGuids.clear();

					for( IRPModelElement theEl : theSelectedEls ){
						_startLinkGuids.add( theEl.getGUID() );
					}

				} else if( menuItem.equals( _settings.getString( 
						"executablembseplugin.EndLinkMenu" ) ) ){

					if( _startLinkGuids.isEmpty() ){

						UserInterfaceHelper.showWarningDialog( "You need to Start a link before you can end it" );
					} else {
						EndlinkPanel.launchThePanel( 
								theAppID, 
								_startLinkGuids );
					}

				} else if (menuItem.equals( _settings.getString( "executablembseplugin.RollUpTraceabilityUpToTransitionLevel" ))){

					if( theSelectedGraphEls != null ){
						IRPGraphElement theSelectedGraphEl = theSelectedGraphEls.get( 0 );
						RollUpTraceabilityToTheTransitionPanel.launchThePanel( theSelectedGraphEl, _context );
					}

				} else if( menuItem.equals( _settings.getString( 
						"executablembseplugin.layoutDependencies" ) ) ){

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

				} else if (menuItem.equals( _settings.getString(
						"executablembseplugin.PopulateRequirementsForSDsMenu" ) ) ){

					if (theSelectedEl instanceof IRPSequenceDiagram){
						PopulateRelatedRequirementsPanel.launchThePanel( theAppID );
					}

				} else if (menuItem.equals( _settings.getString(
						"executablembseplugin.UpdateVerificationDependenciesForSDsMenu" ) ) ){

					if( !theSelectedEls.isEmpty() ){
						
						VerificationDependencyUpdater theHelper = 
								new VerificationDependencyUpdater( _context );
						
						theHelper.updateVerificationsForSequenceDiagramsBasedOn( theSelectedEls );
					}				

				} else if( menuItem.equals( _settings.getString(
						"executablembseplugin.CreateIncomingEventMenu" ) ) ){

					CreateIncomingEventPanel.launchThePanel( theAppID );

				} else if( menuItem.equals( _settings.getString(
						"executablembseplugin.CreateAnOperationMenu" ) ) ){

					CreateOperationPanel.launchThePanel( theAppID );

				} else if( menuItem.equals( _settings.getString(
						"executablembseplugin.CreateOutgoingEventMenu"))){

					CreateOutgoingEventPanel.launchThePanel( theAppID );

				} else if( menuItem.equals( _settings.getString(
						"executablembseplugin.CreateAttributeMenu" ) ) ){

					CreateTracedAttributePanel.launchThePanel( theAppID );

				} else if( menuItem.equals( _settings.getString(
						"executablembseplugin.UpdateAttributeOrCheckOpMenu" ) ) ){

					if( theSelectedEl instanceof IRPAttribute ){
						UpdateTracedAttributePanel.launchThePanel( theAppID, _context );
					}

				} else if( menuItem.equals( _settings.getString(
						"executablembseplugin.CreateEventForAttributeMenu" ) ) ){

					if( theSelectedEl instanceof IRPAttribute ){
						//								Set<IRPRequirement> theReqts = 
						//										TraceabilityHelper.getRequirementsThatTraceFrom( 
						//												theSelectedEl, false );
						//
						//								CreateIncomingEventPanel.launchThePanel( 
						//										null, 
						//										(IRPAttribute)theSelectedEl, 
						//										theReqts, 
						//										theRhpPrj );


					}				

				} else if( menuItem.equals( _settings.getString("executablembseplugin.DeriveDownstreamRequirementMenu"))){

					if (!theSelectedGraphEls.isEmpty()){
						//CreateDerivedRequirementPanel.deriveDownstreamRequirement( theSelectedGraphEls );
					}

				} else if( menuItem.equals( _settings.getString(
						"executablembseplugin.CreateNewTestCaseForTestDriverMenu" ) ) ){

					TestCaseCreator theCreator = new TestCaseCreator( _context );

					if( theSelectedEl instanceof IRPClass ){
						theCreator.createTestCaseFor( (IRPClass) theSelectedEl );
						
					} else if (theSelectedEl instanceof IRPSequenceDiagram){
						theCreator.createTestCaseFor( (IRPSequenceDiagram) theSelectedEl );
					}

				} else if( menuItem.equals( _settings.getString(
						"executablembseplugin.AddNewActorToPackageMenu" ) ) ){

					if (theSelectedEl instanceof IRPPackage){
						CreateNewActorPanel.launchThePanel( theAppID );
					}

				} else if( menuItem.equals( _settings.getString(
						"executablembseplugin.AddNewBlockPartToPackageMenu" ) ) ){

					if( theSelectedEl instanceof IRPPackage || 
							theSelectedEl instanceof IRPDiagram ){
						CreateNewBlockPartPanel.launchThePanel( theAppID );
					}

				} else if (menuItem.equals( _settings.getString("executablembseplugin.UpdatePortsAndInterfacesMenu"))){

					if( theSelectedEl instanceof IRPSequenceDiagram ||
							theSelectedEl instanceof IRPMessage ){
						
						UpdateInferfacesBasedOnSequenceDiagramPanel.launchThePanel( 
								theAppID, theSelectedEl.getGUID() );
					}
					
				} else if( menuItem.equals( _settings.getString(
						"executablembseplugin.CopyActivityDiagramsMenu" ) ) ){

					if( theSelectedEl instanceof IRPPackage ){

						IRPPackage theSelectedPkg = (IRPPackage)theSelectedEl;

						Set<IRPPackage> theSourceUseCasePkgs = 
								_context.getPullFromPackage( theSelectedPkg );

						if( theSourceUseCasePkgs.isEmpty() ){

							List<IRPModelElement> theUseCasePkgsInProject = 
									_context.findElementsWithMetaClassAndStereotype(
											"Package", 
											_context.REQTS_ANALYSIS_USE_CASE_PACKAGE, 
											theSelectedPkg.getProject(), 
											1 );

							IRPModelElement theChosenPkg =
									UserInterfaceHelper.launchDialogToSelectElement(
											theUseCasePkgsInProject, 
											"Select a Use Cases package to establish dependency to", 
											true );

							if( theChosenPkg != null && 
									theChosenPkg instanceof IRPPackage ){

								theSelectedPkg.addDependencyTo( theChosenPkg );
								theSourceUseCasePkgs.add( (IRPPackage) theChosenPkg );

							}
						}

						if( theSourceUseCasePkgs != null ){
							String theMsg = "This helper has detected that " + _context.elInfo(theSelectedPkg) + " is \n" +
									"pulling from the following packages: \n";

							for (IRPModelElement theSourceUseCasePkg : theSourceUseCasePkgs) {
								theMsg += _context.elInfo(theSourceUseCasePkg) + "\n";
							}

							theMsg += "\nDo you want to proceed with launching the copy dialog?\n\n";

							boolean answer = UserInterfaceHelper.askAQuestion(theMsg);

							if( answer ){
								CopyActivityDiagramsPanel.launchThePanel( _context.get_rhpAppID() );
							}
						}
					}							

				} else if( menuItem.equals( _settings.getString(
						"executablembseplugin.DeleteEventsAndRelatedElementsMenu" ) ) ){

					EventDeletor theDeletor = new EventDeletor( _context );
					theDeletor.deleteEventAndRelatedElementsFor( theSelectedEls );

				} else if (menuItem.equals( _settings.getString(
						"executablembseplugin.RecreateAutoShowSequenceDiagramMenu"))){

					if( theSelectedEl instanceof IRPSequenceDiagram ){

						SequenceDiagramCreator theHelper = new SequenceDiagramCreator( _context );

						theHelper.updateLifelinesToMatchPartsInActiveBuildingBlock(
								(IRPSequenceDiagram) theSelectedEl );
					}	

				} else if( menuItem.equals( _settings.getString(
						"executablembseplugin.MakeAttributeAPublishFlowportMenu" ) ) ){

					if( theSelectedEl instanceof IRPAttribute){

						PortCreator portCreator = new PortCreator( _context );
						portCreator.createPublishFlowportsFor( theSelectedEls );

					}

				} else if( menuItem.equals( _settings.getString(
						"executablembseplugin.MakeAttributeASubscribeFlowportMenu" ) ) ){

					if( theSelectedEl instanceof IRPAttribute ){
						PortCreator portCreator = new PortCreator( _context );
						portCreator.createSubscribeFlowportsFor( theSelectedEls );
					}

				} else if( menuItem.equals(_settings.getString(
						"designsynthesisplugin.DeleteAttributeAndRelatedElementsMenu" ) ) ){

					PortCreator portCreator = new PortCreator( _context );

					if( theSelectedEl instanceof IRPAttribute ){
						portCreator.deleteAttributeAndRelatedEls( (IRPAttribute) theSelectedEl );
					} else if( theSelectedEl instanceof IRPSysMLPort ){
						portCreator.deleteFlowPortAndRelatedEls( (IRPSysMLPort) theSelectedEl );
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

		} catch( Exception e ){
			_context.error( "Unhandled exception in InvokeTooltipFormatter" );
		}

		return theOutput;
	}

	@Override
	public void OnTrigger(String trigger) {
	}

	public void getElementsThatTraceToRequirements(
			IRPModelElement element, 
			IRPCollection result ){

		_context.debug( "getElementsThatTraceToRequirements invoked for " + _context.elInfo( element ) );

		@SuppressWarnings("unchecked")
		List<IRPDependency> theDependencies = 
		element.getNestedElementsByMetaClass( "Dependency", 1 ).toList();

		for (IRPDependency theDependency : theDependencies) {

			IRPModelElement theDependsOn = theDependency.getDependsOn();
			IRPModelElement theDependent= theDependency.getDependent();

			// Don't add itself
			if( !theDependent.equals( element ) &&
					theDependsOn instanceof IRPRequirement ){
				result.addItem( theDependent );
			}
		}		
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

	public void getOwnerOfElementsThatTraceFromRequirements(
			IRPModelElement element, 
			IRPCollection result ){

		_context.debug( "getOwnerOfElementsThatTraceFromRequirements invoked for " + _context.elInfo( element ) );

		@SuppressWarnings("unchecked")
		List<IRPRequirement> theRequirements = 
		element.getNestedElementsByMetaClass( "Requirement", 1 ).toList();

		for (IRPRequirement theRequirement : theRequirements) {

			@SuppressWarnings("unchecked")
			List<IRPModelElement> theReferences = theRequirement.getReferences().toList();

			for (IRPModelElement theReference : theReferences) {

				_context.debug( "theReference invoked for " + _context.elInfo( element ) + 
						" is " + _context.elInfo( theReference ) );

				if( theReference instanceof IRPDependency ){
					IRPDependency theDependency = (IRPDependency)theReference;
					IRPModelElement theDependent = theDependency.getDependent();	

					IRPModelElement theOwner = theDependent.getOwner();

					if( theOwner instanceof IRPState && 
							theOwner.getName().equals( "ROOT" ) ){

						theOwner = theOwner.getOwner();
					}

					result.addItem( theOwner );						
				}
			}
		}		
	}

	public void getElementsThatTraceFromRequirements(
			IRPModelElement element, 
			IRPCollection result ){

		if( element instanceof IRPRequirement ){

			@SuppressWarnings("unchecked")
			List<IRPModelElement> theReferences = element.getReferences().toList();

			for( IRPModelElement theReference : theReferences ){

				if( theReference instanceof IRPDependency ){
					IRPDependency theDependency = (IRPDependency)theReference;
					IRPModelElement theDependent = theDependency.getDependent();
					result.addItem( theDependent );
				}
			}
		}		
	}

	public void getRequirementsThatTraceFromElements(
			IRPModelElement element, 
			IRPCollection result ){

		_context.debug( "getRequirementsThatTraceFromElements invoked for " + _context.elInfo( element ) );

		@SuppressWarnings("unchecked")
		List<IRPDependency> theDependencies = 
		element.getNestedElementsByMetaClass( "Dependency", 1 ).toList();

		for (IRPDependency theDependency : theDependencies) {

			IRPModelElement theDependsOn = theDependency.getDependsOn();

			// Don't add itself
			if( !theDependsOn.equals( element ) &&
					theDependsOn instanceof IRPRequirement ){
				result.addItem( theDependsOn );
			}
		}		
	}

	public void getElementsThatTraceFromRequirements2(
			IRPModelElement element, 
			IRPCollection result ){

		@SuppressWarnings("unchecked")
		List<IRPRequirement> theRequirements = 
		element.getNestedElementsByMetaClass( "Requirement", 1 ).toList();

		for (IRPRequirement theRequirement : theRequirements) {

			@SuppressWarnings("unchecked")
			List<IRPModelElement> theReferences = theRequirement.getReferences().toList();

			for (IRPModelElement theReference : theReferences) {

				if( theReference instanceof IRPDependency ){
					IRPDependency theDependency = (IRPDependency)theReference;
					IRPModelElement theDependent = theDependency.getDependent();	

					result.addItem( theDependent );
				}
			}
		}		
	}

	private static List<IRPModelElement> getElementsThatTraceToRequirements(
			IRPModelElement underTheEl ){

		List<IRPModelElement> theMatchedEls = new ArrayList<>();

		@SuppressWarnings("unchecked")
		List<IRPDependency> theDependencies = 
		underTheEl.getNestedElementsByMetaClass( "Dependency", 1 ).toList();

		for (IRPDependency theDependency : theDependencies) {

			IRPModelElement theDependsOn = theDependency.getDependsOn();
			IRPModelElement theDependent= theDependency.getDependent();

			if( theDependsOn instanceof IRPRequirement ){
				theMatchedEls.add( theDependent );
			}
		}

		return theMatchedEls;
	}

	private static List<IRPModelElement> getOwnerOfElementsThatTraceToRequirements(
			IRPModelElement underTheEl ){

		List<IRPModelElement> theMatchedEls = new ArrayList<>();

		@SuppressWarnings("unchecked")
		List<IRPDependency> theDependencies = 
		underTheEl.getNestedElementsByMetaClass( "Dependency", 1 ).toList();

		for( IRPDependency theDependency : theDependencies ){

			IRPModelElement theDependsOn = theDependency.getDependsOn();
			IRPModelElement theDependent= theDependency.getDependent();

			if( theDependsOn instanceof IRPRequirement ){
				theMatchedEls.add( theDependent.getOwner() );
			}
		}

		return theMatchedEls;
	}

	public static List<IRPRequirement> getTracedRequirementsForUseCase(
			IRPUseCase theUseCase ){

		List<IRPRequirement> theTracedReqts = new ArrayList<>();

		@SuppressWarnings("unchecked")
		List<IRPDependency> theDependencies = 
		theUseCase.getNestedElementsByMetaClass( "Dependency", 1 ).toList();

		for (IRPDependency theDependency : theDependencies) {

			IRPModelElement theDependsOn = theDependency.getDependsOn();

			if( theDependsOn instanceof IRPRequirement ){
				theTracedReqts.add( (IRPRequirement) theDependsOn );
			}
		}

		return theTracedReqts;
	}

	private boolean checkAndPerformProfileSetupIfNeeded() {

		boolean isContinue = true;

		boolean isShowInfoDialog = _context.getIsShowProfileVersionCheckDialogs();
		boolean isSetupNeeded = _settings.checkIfSetupProjectIsNeeded( isShowInfoDialog, false );

		if( isSetupNeeded ){

			String theMsg = "The project needs 'Executable MBSE' profile-based properties and tags values to be applied.\n" + 
					"Do you want me to set these properties and tags on the project in order to continue?";

			isContinue = UserInterfaceHelper.askAQuestion( theMsg );

			if( isContinue ){
				_settings.setupProjectWithProperties();
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