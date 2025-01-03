package com.mbsetraining.sysmlhelper.executablembse;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import requirementsanalysisplugin.PopulateRelatedRequirementsPanel;

import com.mbsetraining.sysmlhelper.activitydiagram.ActivityDiagramChecker;
import com.mbsetraining.sysmlhelper.activitydiagram.RenameActions;
import com.mbsetraining.sysmlhelper.allocationpanel.FunctionAllocationPanel;
import com.mbsetraining.sysmlhelper.architecturepkgcreator.CreateArchitecturePkgPanel;
import com.mbsetraining.sysmlhelper.autorealizewithcopy.AutoRealizeWithCopyPanel;
import com.mbsetraining.sysmlhelper.common.ConfigurationSettings;
import com.mbsetraining.sysmlhelper.common.LayoutHelper;
import com.mbsetraining.sysmlhelper.common.NestedActivityDiagram;
import com.mbsetraining.sysmlhelper.common.RequirementsHelper;
import com.mbsetraining.sysmlhelper.common.UserInterfaceHelper;
import com.mbsetraining.sysmlhelper.contextdiagram.CreateContextPackagePanel;
import com.mbsetraining.sysmlhelper.copyactivitydiagram.CopyActivityDiagramsPanel;
import com.mbsetraining.sysmlhelper.createactorpart.CreateNewActorPanel;
import com.mbsetraining.sysmlhelper.createnewblockpart.CreateNewBlockPartPanel;
import com.mbsetraining.sysmlhelper.createtestcase.TestCaseCreator;
import com.mbsetraining.sysmlhelper.dependencyhelper.PopulateClassifiersOfParts;
import com.mbsetraining.sysmlhelper.dependencyhelper.PopulateDependentElements;
import com.mbsetraining.sysmlhelper.dependencyhelper.PopulateDependsOnElements;
import com.mbsetraining.sysmlhelper.doorsng.AddOSLCDependencyLinksToPackage;
import com.mbsetraining.sysmlhelper.doorsng.CleanUpDeadOSLCLinksPanel;
import com.mbsetraining.sysmlhelper.doorsng.DeleteChildOSLCLinksPanel;
import com.mbsetraining.sysmlhelper.doorsng.EstablishTraceRelationsToRemotes;
import com.mbsetraining.sysmlhelper.doorsng.ExportRequirementsToCSV;
import com.mbsetraining.sysmlhelper.doorsng.RepairLinks;
import com.mbsetraining.sysmlhelper.doorsng.SwitchDNGRequirementsToRhapsody;
import com.mbsetraining.sysmlhelper.doorsng.SwitchRhapsodyRequirementsToDNG;
import com.mbsetraining.sysmlhelper.doorsng.SynchronizeLinksBasedOnSurrogate;
import com.mbsetraining.sysmlhelper.doorsng.SynchronizeLinksToDiagram;
import com.mbsetraining.sysmlhelper.doorsng.UpdateSurrogateRequirementsPanel;
import com.mbsetraining.sysmlhelper.eventdeletor.EventDeletor;
import com.mbsetraining.sysmlhelper.executablescenariopackage.CreateFunctionalExecutablePackagePanel;
import com.mbsetraining.sysmlhelper.featurefunctionpkgcreator.CreateFeaturePkgPanel;
import com.mbsetraining.sysmlhelper.featurefunctionpkgcreator.CreateFunctionPkgPanel;
import com.mbsetraining.sysmlhelper.featurefunctionpkgcreator.FeaturePkgCreator;
import com.mbsetraining.sysmlhelper.gateway.CreateGatewayProjectPanel;
import com.mbsetraining.sysmlhelper.gateway.MarkedAsDeletedPanel;
import com.mbsetraining.sysmlhelper.gateway.MoveRequirements;
import com.mbsetraining.sysmlhelper.modelchecks.CheckForRequirementAnchors;
import com.mbsetraining.sysmlhelper.modelchecks.CheckForRequirementCFLRChars;
import com.mbsetraining.sysmlhelper.modelchecks.CheckForRequirementChildren;
import com.mbsetraining.sysmlhelper.modelchecks.CheckForRequirementNameLength;
import com.mbsetraining.sysmlhelper.movetoseparatepackage.MoveToSeparatePackage;
import com.mbsetraining.sysmlhelper.packagediagram.PackageDiagramIndexCreator;
import com.mbsetraining.sysmlhelper.modelchecks.CheckForRemoteRequirementSpecificationMatch;
import com.mbsetraining.sysmlhelper.populateparts.PopulatePartsPanel;
import com.mbsetraining.sysmlhelper.pubsubportcreation.PortCreator;
import com.mbsetraining.sysmlhelper.requirementpackage.CreateRequirementsPkgPanel;
import com.mbsetraining.sysmlhelper.rolluptraceabilitytotransition.RollUpTraceabilityToTheTransitionPanel;
import com.mbsetraining.sysmlhelper.sequencediagram.SequenceDiagramCreator;
import com.mbsetraining.sysmlhelper.sequencediagram.UpdateInferfacesBasedOnSequenceDiagramPanel;
import com.mbsetraining.sysmlhelper.sequencediagram.VerificationDependencyUpdater;
import com.mbsetraining.sysmlhelper.smartlink.EndlinkPanel;
import com.mbsetraining.sysmlhelper.switchanchors.SwitchAnchorsToDependencies;
import com.mbsetraining.sysmlhelper.switchstereotypes.SwitchStereotype;
import com.mbsetraining.sysmlhelper.tracedelementpanels.CreateFunctionBlock;
import com.mbsetraining.sysmlhelper.tracedelementpanels.CreateIncomingEventPanel;
import com.mbsetraining.sysmlhelper.tracedelementpanels.CreateOperationPanel;
import com.mbsetraining.sysmlhelper.tracedelementpanels.CreateOutgoingEventPanel;
import com.mbsetraining.sysmlhelper.tracedelementpanels.CreateTracedAttributePanel;
import com.mbsetraining.sysmlhelper.tracedelementpanels.UpdateTracedAttributePanel;
import com.mbsetraining.sysmlhelper.usecasepackage.CreateUseCasesPackagePanel;
import com.mbsetraining.sysmlhelper.viewviewpoint.AddToViewPanel;
import com.mbsetraining.sysmlhelper.viewviewpoint.RemoveFromViewPanel;
import com.mbsetraining.sysmlhelper.viewviewpoint.ViewStructureCreationPanel;
import com.telelogic.rhapsody.core.*;

public class ExecutableMBSE_RPUserPlugin extends RPUserPlugin {

	protected ExecutableMBSE_Context _context;
	protected ConfigurationSettings _settings;
	protected ExecutableMBSE_RPApplicationListener _listener = null;
	protected List<String> _startLinkGuids = new ArrayList<>();
	protected CheckForRequirementAnchors _checkForRequirementAnchors;
	protected CheckForRequirementChildren _checkForRequirementChildren;
	protected CheckForRequirementCFLRChars _checkForRequirementCFLRChars;
	protected CheckForRemoteRequirementSpecificationMatch _checkForRemoteRequirementSpecificationMatch;
	protected CheckForRequirementNameLength _checkForRequirementNameLength;

	public static void main(String[] args) {

		IRPApplication theRhpApp = RhapsodyAppServer.getActiveRhapsodyApplication();

		ExecutableMBSE_RPUserPlugin thePlugin = new ExecutableMBSE_RPUserPlugin();
		thePlugin.RhpPluginInit( theRhpApp );
		
		IRPModelElement theSelectedEl = theRhpApp.getSelectedElement();
		
		if( theSelectedEl instanceof IRPTransition ) {

			String theGUID = theSelectedEl.getGUID();
			String theGuard = thePlugin.getGuardOnControlFlow( theGUID );
			
			theRhpApp.writeToOutputWindow("", theGuard);

		}
	}

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
				"Copyright (C) 2015-2024  MBSE Training and Consulting Limited (www.executablembse.com)"
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

		IRPExternalCheckRegistry theCheckRegistry = _context.get_rhpApp().getExternalCheckerRegistry();

		_checkForRequirementAnchors = new CheckForRequirementAnchors( theCheckRegistry );
		_checkForRequirementChildren = new CheckForRequirementChildren( theCheckRegistry );	
		_checkForRequirementCFLRChars = new CheckForRequirementCFLRChars( theCheckRegistry, _context );
		_checkForRemoteRequirementSpecificationMatch = new CheckForRemoteRequirementSpecificationMatch( theCheckRegistry, _context );
		_checkForRequirementNameLength = new CheckForRequirementNameLength( theCheckRegistry, _context );
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
						"executablembseplugin.CreateUseCaseStructureMenu" ) ) ){

					if( theSelectedEl instanceof IRPPackage ){

						boolean isContinue = checkAndPerformProfileSetupIfNeeded();

						if( isContinue ){
							CreateUseCasesPackagePanel.launchTheDialog( theAppID );
						}	

					} else {
						_context.error( menuItem + " invoked out of context and only works for packages" );
					}

				} else if( menuItem.equals( _settings.getString(
						"executablembseplugin.CreateRequirementPackageMenu" ) ) ){

					if( theSelectedEl instanceof IRPPackage ){

						boolean isContinue = checkAndPerformProfileSetupIfNeeded();

						if( isContinue ){
							CreateRequirementsPkgPanel.launchTheDialog( theAppID );
						}	

					} else {
						_context.error( menuItem + " invoked out of context and only works for packages" );
					}

				} else if( menuItem.equals( _settings.getString(
						"executablembseplugin.SetupRAProperties" ) ) ){

					if( theSelectedEl instanceof IRPPackage ){
						_settings.checkIfSetupProjectIsNeeded( true, "SysML" );
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
								_context.SEQUENCE_DIAGRAM_PREFIX + theSelectedEl.getName(), 
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

					PopulateDependsOnElements theSelector = new PopulateDependsOnElements( _context );
					theSelector.selectDependsOnElementsFor( new ArrayList<>( theCombinedSet ) );

				} else if( menuItem.equals( _settings.getString(
						"executablembseplugin.SelectDependentElementsMenu" ) ) ){

					Set<IRPModelElement> theCombinedSet = 
							_context.getSetOfElementsFromCombiningThe(
									theSelectedEls, theSelectedGraphEls );

					PopulateDependentElements theSelector = new PopulateDependentElements( _context );
					theSelector.selectDependentElementsFor( new ArrayList<>( theCombinedSet ) );

				} else if( menuItem.equals( _settings.getString(
						"executablembseplugin.PopulateDependsOnElementsMenu" ) ) ){

					PopulateDependsOnElements theSelector = new PopulateDependsOnElements( _context );
					theSelector.populateDependsOnElementsFor( _context.getSelectedGraphEl() );

				} else if( menuItem.equals( _settings.getString(
						"executablembseplugin.PopulateDependentElementsMenu" ) ) ){

					PopulateDependentElements theSelector = new PopulateDependentElements( _context );
					theSelector.populateDependentElementsFor( _context.getSelectedGraphEl() );

				} else if( menuItem.equals( _settings.getString(
						"executablembseplugin.PopulateClassifiersOfPartsMenu" ) ) ){

					PopulateClassifiersOfParts theSelector = new PopulateClassifiersOfParts( _context );
					theSelector.populateClassifiersFor( _context.getSelectedGraphEl() );

				} else if( menuItem.equals( _settings.getString(
						"executablembseplugin.SelectChildClassifiersMenu" ) ) ){

					if( ( theSelectedEl instanceof IRPObjectModelDiagram ||
							theSelectedEl instanceof IRPStructureDiagram ) &&
							theSelectedEl.getOwner() instanceof IRPClassifier ){

						theSelectedEls.add( theSelectedEl.getOwner() );
					}

					Set<IRPModelElement> theCombinedSet = 
							_context.getSetOfElementsFromCombiningThe(
									theSelectedEls, theSelectedGraphEls );

					PopulateClassifiersOfParts theSelector = new PopulateClassifiersOfParts( _context );
					theSelector.selectClassifiersFor( new ArrayList<>( theCombinedSet ) );

				} else if (menuItem.equals(_settings.getString(
						"executablembseplugin.SetupGatewayProjectMenu" ) ) ){

					if (theSelectedEl instanceof IRPProject){
						CreateGatewayProjectPanel.launchThePanel( theAppID, ".*.rqtf$" );				
					}

				} else if (menuItem.equals( _settings.getString(
						"executablembseplugin.AddRelativeUnitMenu" ) ) ){

					_context.browseAndAddUnit( theSelectedEl.getProject(), true );

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

				} else if( menuItem.equals( _settings.getString( 
						"executablembseplugin.UpdateNestedADNamesMenu" ) ) ){

					NestedActivityDiagram theHelper = new NestedActivityDiagram(_context);

					theHelper.renameNestedActivityDiagramsFor( 
							theSelectedEls );

				} else if( menuItem.equals( _settings.getString( 
						"executablembseplugin.DeleteTaggedAsDeletedAtHighLevelMenu" ) ) ){

					MarkedAsDeletedPanel.launchThePanel( theAppID );

				} else if( menuItem.equals( _settings.getString( 
						"executablembseplugin.ExportRequirementsToCsvForImportIntoDOORSNG" ) ) ){

					ExportRequirementsToCSV theExporter = new ExportRequirementsToCSV( _context );

					theExporter.exportRequirementsToCSV( 
							_context.getSelectedElement( false ), 1 );

				} else if( menuItem.equals( _settings.getString( 
						"executablembseplugin.SwitchRequirementsToDOORSNG" ) ) ){

					if( theSelectedEl instanceof IRPPackage ) {

						SwitchRhapsodyRequirementsToDNG theSwitcher = 
								new SwitchRhapsodyRequirementsToDNG( _context );

						theSwitcher.switchRequirementsFor( (IRPPackage) theSelectedEl );
					}

				} else if( menuItem.equals( _settings.getString( 
						"executablembseplugin.EstablishTraceRelationsToDNG" ) ) ){

					EstablishTraceRelationsToRemotes theTracer = 
							new EstablishTraceRelationsToRemotes( _context );

					theTracer.establishTraceRelationsToRemoteReqts();

				} else if( menuItem.equals( _settings.getString( 
						"executablembseplugin.UpdateSurrogateBasedOnRemoteRequirementChanges" ) ) ){

					UpdateSurrogateRequirementsPanel.launchThePanel( theAppID );

				} else if( menuItem.equals( _settings.getString( 
						"executablembseplugin.SynchronizeLinksBasedOnSurrogate" ) ) ){

					if( theSelectedEl instanceof IRPPackage ) {

						SynchronizeLinksBasedOnSurrogate theSynchronizer = 
								new SynchronizeLinksBasedOnSurrogate( _context );

						theSynchronizer.synchronizeLinksFromLocalToRemote( (IRPPackage) theSelectedEl );	
					}

				} else if( menuItem.equals( _settings.getString( 
						"executablembseplugin.SynchronizeDiagramLinksToRemotes" ) ) ){

					Set<IRPModelElement> theCombinedSet = 
							_context.getSetOfElementsFromCombiningThe(
									theSelectedEls, theSelectedGraphEls );

					SynchronizeLinksToDiagram theSynchronizer = 
							new SynchronizeLinksToDiagram( _context );

					theSynchronizer.synchronizeLinksToDiagram( theCombinedSet );

				} else if( menuItem.equals( _settings.getString( 
						"executablembseplugin.StartLinkMenu" ) ) ){

					_startLinkGuids.clear();

					for( IRPModelElement theEl : theSelectedEls ){
						_startLinkGuids.add( theEl.getGUID() );
					}

				} else if( menuItem.equals( _settings.getString( 
						"executablembseplugin.EndLinkMenu" ) ) ){

					if( _startLinkGuids.isEmpty() ){

						UserInterfaceHelper.showWarningDialog( 
								"You need to Start a link before you can end it" );
					} else {
						EndlinkPanel.launchThePanel( 
								theAppID, 
								_startLinkGuids );
					}

				} else if( menuItem.equals( _settings.getString( 
						"executablembseplugin.AddToView" ) ) ){

					AddToViewPanel.launchThePanel( theAppID, _context );

				} else if( menuItem.equals( _settings.getString( 
						"executablembseplugin.RemoveFromView" ) ) ){

					RemoveFromViewPanel.launchThePanel( theAppID, _context );

				} else if (menuItem.equals( _settings.getString( 
						"executablembseplugin.RollUpTraceabilityUpToTransitionLevel" ) ) ){

					if( theSelectedGraphEls != null ){
						IRPGraphElement theSelectedGraphEl = theSelectedGraphEls.get( 0 );
						RollUpTraceabilityToTheTransitionPanel.launchThePanel( theSelectedGraphEl, _context );
					}

				} else if( menuItem.equals( _settings.getString( 
						"executablembseplugin.CenterStraightLinesMenu" ) ) ){

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

				} else if( menuItem.equals( _settings.getString(
						"executablembseplugin.CreateFunctionBlockMenu" ) ) ){

					CreateFunctionBlock.launchThePanel( theAppID );

				} else if( menuItem.equals( _settings.getString(
						"executablembseplugin.DeriveDownstreamRequirementMenu" ) ) ){

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

				} else if (menuItem.equals( _settings.getString(
						"executablembseplugin.UpdatePortsAndInterfacesMenu"))){

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
						"executablembseplugin.DeleteAttributeAndRelatedElementsMenu" ) ) ){

					PortCreator portCreator = new PortCreator( _context );

					if( theSelectedEl instanceof IRPAttribute ){
						portCreator.deleteAttributeAndRelatedEls( (IRPAttribute) theSelectedEl );
					} else if( theSelectedEl instanceof IRPSysMLPort ){
						portCreator.deleteFlowPortAndRelatedEls( (IRPSysMLPort) theSelectedEl );
					}

				} else if( menuItem.equals( _settings.getString( 
						"executablembseplugin.AutoRealizeWithCopy" ) ) ){

					//if( theSelectedEl instanceof IRPSequenceDiagram ||
					//		theSelectedEl instanceof IRPMessage ){

					AutoRealizeWithCopyPanel.launchThePanel( 
							theAppID, theSelectedEl.getGUID() );
					//}

				} else if( menuItem.equals( _settings.getString( 
						"executablembseplugin.RepairOSLCLinks" ) ) ){

					RepairLinks theRepairer = new RepairLinks( _context );
					theRepairer.repairAllDiagrams( theSelectedEl );

				} else if( menuItem.equals( _settings.getString( 
						"executablembseplugin.SwitchStereotype" ) ) ){

					if( theSelectedEl instanceof IRPStereotype ){

						SwitchStereotype theSwitcher = new SwitchStereotype( _context );
						theSwitcher.switchStereotypeFrom( (IRPStereotype) theSelectedEl );
					}

				} else if( menuItem.equals( _settings.getString( 
						"executablembseplugin.MoveUseCaseIntoSeparatePkg" ) ) ){

					MoveToSeparatePackage theMover = new MoveToSeparatePackage( 
							_context, 
							"UseCase", 
							_context.REQTS_ANALYSIS_USE_CASE_PACKAGE, 
							_context.getDefaultUseCasePackagePostfix( _context.get_rhpPrj() ) );

					theMover.performMoveIfConfirmed();

				} else if( menuItem.equals( _settings.getString( 
						"executablembseplugin.MoveFunctionIntoSeparatePkg" ) ) ){

					MoveToSeparatePackage theMover = new MoveToSeparatePackage( 
							_context, 
							_context.FUNCTION_BLOCK, 
							_context.FUNCT_ANALYSIS_FEATURE_FUNCTION_PACKAGE, 
							"_FunctionPkg" );

					theMover.performMoveIfConfirmed();

				} else if( menuItem.equals( _settings.getString( 
						"executablembseplugin.MoveSubsystemIntoSeparatePkg" ) ) ){

					MoveToSeparatePackage theMover = new MoveToSeparatePackage( 
							_context, 
							_context.SUBSYSTEM_BLOCK, 
							_context.DESIGN_SYNTHESIS_SUBSYSTEM_PACKAGE, 
							"_SubsystemPkg" );

					theMover.performMoveIfConfirmed();

				} else if( menuItem.equals( _settings.getString( 
						"executablembseplugin.CreateFeaturePackageStructureMenu" ) ) ){

					if( theSelectedEl instanceof IRPPackage ){

						boolean isContinue = checkAndPerformProfileSetupIfNeeded();

						if( isContinue ){
							CreateFeaturePkgPanel.launchTheDialog( theAppID );
						}
					}

				} else if( menuItem.equals( _settings.getString( 
						"executablembseplugin.CreateFunctionPackageStructureMenu" ) ) ){

					if( theSelectedEl instanceof IRPPackage ){

						boolean isContinue = checkAndPerformProfileSetupIfNeeded();

						if( isContinue ){
							CreateFunctionPkgPanel.launchTheDialog( theAppID );
						}
					}
					
				} else if( menuItem.equals( _settings.getString( 
						"executablembseplugin.CreateArchitecturePackageStructureMenu" ) ) ){

					if( theSelectedEl instanceof IRPPackage ){

						boolean isContinue = checkAndPerformProfileSetupIfNeeded();

						if( isContinue ){
							CreateArchitecturePkgPanel.launchTheDialog( theAppID );
						}
					}

				} else if( menuItem.equals( _settings.getString( 
						"executablembseplugin.CreateFeatureFunctionPkg" ) ) ){

					List<IRPUseCase> theUseCases = new ArrayList<>();

					Set<IRPModelElement> theCandidateEls = 
							_context.getSetOfElementsFromCombiningThe( 
									theSelectedEls, theSelectedGraphEls );

					for( IRPModelElement theCandidateEl : theCandidateEls ){

						if( theCandidateEl instanceof IRPUseCase ){
							theUseCases.add( (IRPUseCase) theCandidateEl );
						}
					}

					if( theUseCases.isEmpty() ){

						_context.warning( "There were no selected use cases. Right-click a use case and try again");

					} else {

						String theMsg = "Do you want to create feature function packages for the " + 
								theUseCases.size() + " selected use cases? \n";

						boolean answer = UserInterfaceHelper.askAQuestion( theMsg );

						if( answer ){
							FeaturePkgCreator theCreator = new FeaturePkgCreator( _context );
							theCreator.createFeatureFunctionPkgs( theUseCases );
						}
					}

				} else if( menuItem.equals( _settings.getString(
						"executablembseplugin.CreateViewStructureMenu" ) ) ){

					if( theSelectedEl instanceof IRPPackage ){

						ViewStructureCreationPanel.launchThePanel( theAppID );

					} else {
						_context.error( menuItem + " invoked out of context and only works for packages" );
					}
					
				} else if( menuItem.equals( _settings.getString(
						"executablembseplugin.SwitchAnchorsToDependencies" ) ) ){

					if( theSelectedEl instanceof IRPPackage ){

						SwitchAnchorsToDependencies theSwitcher = new SwitchAnchorsToDependencies( _context );
						theSwitcher.performSwitch( theSelectedEl, 1 );

					} else {
						_context.error( menuItem + " invoked out of context and only works for packages" );
					}
					
				} else if( menuItem.equals( _settings.getString(
						"executablembseplugin.CleanUpDeadOSLCLinks" ) ) ){

					CleanUpDeadOSLCLinksPanel.launchThePanel( theAppID );

				} else if( menuItem.equals( _settings.getString(
						"executablembseplugin.DeleteChildOSLCLinks" ) ) ){

					DeleteChildOSLCLinksPanel.launchThePanel( theAppID );

				} else if( menuItem.equals( _settings.getString(
						"executablembseplugin.AllocateFunctionBlocksMenu" ) ) ){

					FunctionAllocationPanel.launchThePanel( theAppID );

				} else if( menuItem.equals( _settings.getString(
						"executablembseplugin.AddTraceLinksToRequirementsPackageMenu" ) ) ){

					Set<IRPModelElement> theCombinedSet = 
							_context.getSetOfElementsFromCombiningThe(
									theSelectedEls, theSelectedGraphEls );

					AddOSLCDependencyLinksToPackage theLinkCreator = new AddOSLCDependencyLinksToPackage( _context );
					theLinkCreator.chooseAndAddOSLCLinksToPackageFor( theCombinedSet );

				} else if( menuItem.equals( _settings.getString(
						"executablembseplugin.SwitchDOORSNGRequirementsToRhapsodyMenu" ) ) ){

					if( theSelectedEl instanceof IRPPackage ) {
						SwitchDNGRequirementsToRhapsody theSwitcher = new SwitchDNGRequirementsToRhapsody( _context );
						theSwitcher.switchRequirementsFor( (IRPPackage) theSelectedEl );
					}

				} else if( menuItem.equals( _settings.getString(
						"executablembseplugin.AutoCreatePackageDiagramMenu" ) ) ){

					if( theSelectedEl instanceof IRPPackage ) {

						PackageDiagramIndexCreator theCreator = new PackageDiagramIndexCreator( _context );
						theCreator.populateContentBasedOnPolicyForPackage( (IRPPackage) theSelectedEl );
					}

				} else if( menuItem.equals( _settings.getString(
						"executablembseplugin.AutoUpdatePackageDiagramMenu" ) ) ){

					if( theSelectedEl instanceof IRPDiagram ){

						PackageDiagramIndexCreator theCreator = new PackageDiagramIndexCreator( _context );
						theCreator.populateContentBasedOnPolicyForDiagram( (IRPDiagram) theSelectedEl );
					}
					
				} else if( menuItem.equals( _settings.getString(
						"executablembseplugin.ReopenDiagramMenu" ) ) ){

					if( theSelectedEl instanceof IRPDiagram ){
					
						// Close and reopen diagram to refresh remote requirement text
						IRPDiagram theDiagram = (IRPDiagram)theSelectedEl;
						theDiagram.closeDiagram();
						theDiagram.openDiagram();
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

	// Used in context pattern tables
	public static void getObjectNodeRepresents(
			IRPModelElement element, 
			IRPCollection result ){

		if( element instanceof IRPObjectNode ){

			IRPObjectNode theObjectNode = (IRPObjectNode) element;
			
			IRPModelElement theRepresents = theObjectNode.getRepresents();

			if( theRepresents != null ) {
				result.addItem( theRepresents );
			}
		}
	}
	
	// Used in context pattern tables
	public static void getIncomingFlowSources(
			IRPModelElement element, 
			IRPCollection result ){
		
		if( element instanceof IRPStateVertex ){

			IRPStateVertex theStateVertex = (IRPStateVertex) element;

			List<IRPModelElement> theFlowSources = getIncomingFlowSources( theStateVertex );

			if( !theFlowSources.isEmpty() ){
				
				for( IRPModelElement theFlowSource : theFlowSources ){
					result.addItem( theFlowSource );
				}
			}
		}
	}
	
	// Used in context pattern tables
	public static void getOutgoingFlowsFromStateVertex(
			IRPModelElement element, 
			IRPCollection result ){
		
		if( element instanceof IRPStateVertex ){

			IRPStateVertex theStateVertex = (IRPStateVertex) element;
			
			@SuppressWarnings("unchecked")
			List<IRPTransition> theTransitions = theStateVertex.getOutTransitions().toList();

			for( IRPTransition theTransition : theTransitions ){
				result.addItem( theTransition );
			}
		}
	}
	
	// Used in context pattern tables
	public static void getOutgoingFlowTargets(
			IRPModelElement element, 
			IRPCollection result ){
		
		if( element instanceof IRPStateVertex ){

			IRPStateVertex theStateVertex = (IRPStateVertex) element;

			List<IRPModelElement> theFlowTargets = getOutgoingFlowTargets( theStateVertex );

			if( !theFlowTargets.isEmpty() ){
				
				for( IRPModelElement theFlowTarget : theFlowTargets ){
					result.addItem( theFlowTarget );
				}
			}
		}
	}
	
	public String getGuardOnControlFlow( String guid ) {
	
		String result = "";
		
		if( _context != null ) {			
			
			IRPProject theRhp = _context.get_rhpPrj();
			
			if( theRhp != null ) {
				
				IRPModelElement theEl = theRhp.findElementByGUID( guid );
				
				if( theEl instanceof IRPTransition) {
					
					IRPTransition theTransition = (IRPTransition)theEl;
					
					IRPGuard theGuard = theTransition.getItsGuard();
					
					if( theGuard != null ) {						
						result = theGuard.getBody();
					}
				}
			}
		}
		
		return result;
	}
	
	// use on context pattern tables
	public String getStereotypesByString(String guid) {
		
		String result = "";
				
		if( _context != null ) {			
			
			IRPProject theRhp = _context.get_rhpPrj();
			
			if( theRhp != null ) {
				
				IRPModelElement theEl = theRhp.findElementByGUID( guid );
				
				if( theEl != null ) {
					
					@SuppressWarnings("unchecked")
					List<IRPStereotype> theStereotypes = theEl.getStereotypes().toList();
					
					for (Iterator<IRPStereotype> iterator = theStereotypes.iterator(); iterator.hasNext();) {
						
						IRPStereotype theStereotype = (IRPStereotype) iterator.next();
						
						result += theStereotype.getName();
						
						if( iterator.hasNext() ) {
							result += ", ";
						}
					}
				}
			}
		}
		
		return result;
	}
		
	void getLeafClassifierUsagesUnderPkg(
			IRPModelElement element,
			IRPCollection result ) {
		
//		_context.debug( "getLeafClassifierUsagesUnderPkg" );
		
		if( element instanceof IRPPackage ) {
			
			@SuppressWarnings("unchecked")
			List<IRPModelElement> theClasses = element.getNestedElementsByMetaClass( "Class", 0 ).toList();
			
//			_context.debug( "found " + theClasses.size() + " classes" );
			
			for( IRPModelElement theClass : theClasses ){
				
				String theUserDefinedMetaClass = theClass.getUserDefinedMetaClass();
				
//				_context.debug( "found " + _context.elInfo( theClass ) );

				if( theUserDefinedMetaClass.equals( "Function Block" ) ) {

					@SuppressWarnings("unchecked")
					List<IRPModelElement> theObjects = theClass.getNestedElementsByMetaClass( "Object", 0 ).toList();

					if( theObjects.isEmpty() ) {						
						getElementsDirectlyUsingClassifier( theClass, result );
					}
				}
			}
		}
	}
		
	void getElementsDirectlyUsingClassifier(
			IRPModelElement element,
			IRPCollection result ) {

		if( element instanceof IRPClassifier ){
			
			//_context.debug( "getElementsWithUsagesOfThisClassifier invoked for " + _context.elInfo( element ) );

			IRPClassifier theClassifier = (IRPClassifier) element;
		
			@SuppressWarnings("unchecked")
			List<IRPModelElement> theReferences = theClassifier.getReferences().toList();
			
			for( IRPModelElement theReference : theReferences ){
			
				if( theReference instanceof IRPInstance ) {					
					result.addItem( theReference );			
				}
			}			
		}
	}
	
	// Used in context pattern tables
	void getRootElementsForClassifier(
			IRPModelElement element, 
			IRPCollection result ){

		if( element instanceof IRPClassifier ){
							
			//_context.debug( "getRootClassifiersRecursively invoked for " + _context.elInfo( element ) );

			IRPClassifier theClassifier = (IRPClassifier) element;

			boolean isRootClassifier = true;
				
				@SuppressWarnings("unchecked")
			List<IRPModelElement> theReferences = theClassifier.getReferences().toList();
				
			for( IRPModelElement theReference : theReferences ){
				
				if( theReference instanceof IRPInstance ) {
						
					isRootClassifier = false;
						
					IRPInstance theInstance = (IRPInstance)theReference;
					IRPClassifier theOfClass = theInstance.getOfClass();
						
					getRootElementsForClassifier( theOfClass, result );				
				}
			}
				
			if( isRootClassifier ) {
				result.addItem( theClassifier );
			}
		}
	}
	
	// Used in context pattern tables
	void getUntracedToRemoteRequirement(
			IRPModelElement element, 
			IRPCollection result ){

		if( element instanceof IRPRequirement ){

			IRPRequirement req = (IRPRequirement) element;

			@SuppressWarnings("unchecked")
			List<IRPDependency> theRemoteDependencies = req.getRemoteDependencies().toList();

			if( theRemoteDependencies.isEmpty() ){
				result.addItem( element );
			}
		}
	}

	// Used in context pattern tables
	void getMatchingRemoteRequirements(
			IRPModelElement element, 
			IRPCollection result ){

		if( element instanceof IRPRequirement ){

			IRPRequirement req = (IRPRequirement) element;

			@SuppressWarnings("unchecked")
			List<IRPDependency> theRemoteDependencies = req.getRemoteDependencies().toList();

			for( IRPDependency theRemoteDependency : theRemoteDependencies ){

				IRPModelElement theDependsOn = theRemoteDependency.getDependsOn();

				if( theDependsOn instanceof IRPRequirement ){

					IRPRequirement theOSLCRequirement = (IRPRequirement)theDependsOn;

					if( _context.isRequirementSpecificationMatchingFor(
							req, theOSLCRequirement ) ) {

						result.addItem( theDependsOn );
					}
				}
			}
		}
	}

	// Used in context pattern tables
	void getNonMatchingRemoteRequirements(
			IRPModelElement element, 
			IRPCollection result ){

		if( element instanceof IRPRequirement ){

			IRPRequirement req = (IRPRequirement) element;

			@SuppressWarnings("unchecked")
			List<IRPDependency> theRemoteDependencies = req.getRemoteDependencies().toList();

			for( IRPDependency theRemoteDependency : theRemoteDependencies ){

				IRPModelElement theDependsOn = theRemoteDependency.getDependsOn();

				if( theDependsOn instanceof IRPHyperLink ) {
					result.addItem( theDependsOn );

				} else if( theDependsOn instanceof IRPRequirement ){

					IRPRequirement theOSLCRequirement = (IRPRequirement)theDependsOn;

					if( !_context.isRequirementSpecificationMatchingFor(
							req, theOSLCRequirement ) ){

						result.addItem( theDependsOn );
					}
				}
			}
		}
	}
	
	// Used in context pattern tables
	void getRelatedAnnotations(
			IRPModelElement element,
			IRPCollection result ) {
		
		@SuppressWarnings("unchecked")
		List<IRPModelElement> theReferences = element.getReferences().toList();
		
		for( IRPModelElement theReference : theReferences ){
			
			if( theReference instanceof IRPAnnotation ) {
				
				result.addItem( theReference );
								
			} else if( theReference instanceof IRPDependency ) {
				
				IRPDependency theDependency = (IRPDependency)theReference;
				IRPModelElement theDependent = theDependency.getDependent();
				
				if( theDependent instanceof IRPAnnotation ) {
					result.addItem( theDependent );
				}
			}
		}		
	}

	// Used in context pattern tables
	void getRequirementsOnDiagram(
			IRPModelElement element, 
			IRPCollection result ){

		//RhapsodyAppServer.getActiveRhapsodyApplication().writeToOutputWindow("", "invoked for " + element.getMetaClass() + " called (2) " + element.getName() + "\n" );

		if( element instanceof IRPFlowchart ) { // ActivityDiagram
			IRPFlowchart theAD = (IRPFlowchart)element;
			element = theAD.getStatechartDiagram(); // ActivityDiagramGE
		} else if( element instanceof IRPStatechart ) {
			IRPStatechart theStatechart = (IRPStatechart)element;
			element = theStatechart.getStatechartDiagram();
		}

		// We elevate requirements on nested ADs or stms to the owning use case/classifier to work around issues with context patterns and statecharts/activity diagranms
		if( element instanceof IRPClassifier ){

			// This will harvest both stms and activity diagrams as Rhapsody's AD is a statechart diagram
			@SuppressWarnings("unchecked")
			List<IRPModelElement> theActivityDiagramGEs = element.getNestedElementsByMetaClass( "StatechartDiagram", 1 ).toList();

			for( IRPModelElement irpModelElement : theActivityDiagramGEs ){
				getRequirementsOnDiagram(
						irpModelElement, 
						result );
			}

		} else if( element instanceof IRPDiagram ){

			//RhapsodyAppServer.getActiveRhapsodyApplication().writeToOutputWindow("", element.getMetaClass() + " is a diagram\n" );

			IRPDiagram theDiagram = (IRPDiagram) element;

			@SuppressWarnings("unchecked")
			List<IRPModelElement> theEls = theDiagram.getElementsInDiagram().toList();

			for( IRPModelElement theEl : theEls ){

				if( theEl instanceof IRPRequirement ) {
					//RhapsodyAppServer.getActiveRhapsodyApplication().writeToOutputWindow("", "Adding " + theEl.getName() + " as on diagram\n" );
					result.addItem( theEl );
				}
			}
		} else {
			//RhapsodyAppServer.getActiveRhapsodyApplication().writeToOutputWindow("", element.getMetaClass() + " is not a diagram\n" );
		}
	}

	// Used in context pattern tables
	void getDiagramsWithRequirementOn(
			IRPModelElement element, 
			IRPCollection result ){

		//RhapsodyAppServer.getActiveRhapsodyApplication().writeToOutputWindow("", "invoked for " + element.getMetaClass() + " called (2) " + element.getName() + "\n" );

		if( element instanceof IRPRequirement ){

			@SuppressWarnings("unchecked")
			List<IRPModelElement> theReferences = element.getReferences().toList();

			for (IRPModelElement theEl : theReferences) {

				if( theEl instanceof IRPActivityDiagram ) { // ActivityDiagramGE
					result.addItem( theEl.getOwner() ); // Activity Diagram

				} else if( theEl instanceof IRPStatechartDiagram ) { // StatechartDiagram
					result.addItem( theEl.getOwner() ); // Statechart

				} else if( theEl instanceof IRPDiagram ) {
					result.addItem( theEl );
				}
			}
		}
	}

	// Used in context pattern tables
	void getDiagramsWithObjectsOn(
			IRPModelElement element, 
			IRPCollection result ){

		//RhapsodyAppServer.getActiveRhapsodyApplication().writeToOutputWindow("", "invoked for " + element.getMetaClass() + " called (2) " + element.getName() + "\n" );

		if( element instanceof IRPInstance ){

			@SuppressWarnings("unchecked")
			List<IRPModelElement> theReferences = element.getReferences().toList();

			for( IRPModelElement theEl : theReferences ){

				if( theEl instanceof IRPActivityDiagram ) { // ActivityDiagramGE
					result.addItem( theEl.getOwner() ); // Activity Diagram

				} else if( theEl instanceof IRPStatechartDiagram ) { // StatechartDiagram
					result.addItem( theEl.getOwner() ); // Statechart

				} else if( theEl instanceof IRPDiagram ) {
					result.addItem( theEl );
				}
			}
		}
	}

	// Used in context pattern tables
	void getChildActivityDiagrams(
			IRPModelElement element, 
			IRPCollection result ){

		//RhapsodyAppServer.getActiveRhapsodyApplication().writeToOutputWindow("", "invoked for " + element.getMetaClass() + " called (2) " + element.getName() + "\n" );

		@SuppressWarnings("unchecked")
		List<IRPFlowchart> theReferences = element.getNestedElementsByMetaClass( "ActivityDiagram", 0 ).toList();

		for( IRPFlowchart theEl : theReferences ){	
			//RhapsodyAppServer.getActiveRhapsodyApplication().writeToOutputWindow("", "getChildActivityDiagrams invoked for " + element.getMetaClass() + " called  " + element.getName() + 
			//		" has nested " + theEl.getMetaClass() + " called " + theEl.getName() +"\n" );
			result.addItem( theEl.getFlowchartDiagram() ); // Activity Diagram
		}
	}

	// Used in context pattern tables
	void getChildStatechartDiagrams(
			IRPModelElement element, 
			IRPCollection result ){

		//RhapsodyAppServer.getActiveRhapsodyApplication().writeToOutputWindow("", " getChildStatechartDiagrams invoked for " + element.getMetaClass() + " called (2) " + element.getName() + "\n" );

		@SuppressWarnings("unchecked")
		List<IRPFlowchart> theReferences = element.getNestedElementsByMetaClass( "Statechart", 0 ).toList();

		//RhapsodyAppServer.getActiveRhapsodyApplication().writeToOutputWindow("", theReferences.size() +  " nested els doung for " + element.getName() + "\n" );

		for( IRPStatechart theEl : theReferences ){	

			//RhapsodyAppServer.getActiveRhapsodyApplication().writeToOutputWindow("",  theEl.getMetaClass() + " called " + theEl.getName() + "\n" );
			result.addItem( theEl.getStatechartDiagram() ); // Activity Diagram
		}
	}

	// Used in context pattern tables
	void filterToDiagramsWithRequirements(
			IRPModelElement element, 
			IRPCollection result ){

		//RhapsodyAppServer.getActiveRhapsodyApplication().writeToOutputWindow("", "invoked for " + element.getMetaClass() + " called (2) " + element.getName() + "\n" );

		if( element instanceof IRPFlowchart ) { // ActivityDiagram
			IRPFlowchart theAD = (IRPFlowchart)element;
			element = theAD.getStatechartDiagram(); // ActivityDiagramGE
		} else if( element instanceof IRPStatechart ) {
			IRPStatechart theStatechart = (IRPStatechart)element;
			element = theStatechart.getStatechartDiagram();
		}

		if( element instanceof IRPDiagram ){

			IRPDiagram theDiagram = (IRPDiagram) element;

			@SuppressWarnings("unchecked")
			List<IRPModelElement> theEls = theDiagram.getElementsInDiagram().toList();

			for( IRPModelElement theEl : theEls ){

				if( theEl instanceof IRPRequirement ) {
					result.addItem( element );
					break;
				}
			}
		}
	}

	// Used in context pattern tables
	void getObjectNodesWithFlowTargets(
			IRPModelElement element, 
			IRPCollection result ){

		if( element instanceof IRPClass ){

			IRPClass theClass = (IRPClass) element;

			@SuppressWarnings("unchecked")
			List<IRPModelElement> theReferences = theClass.getReferences().toList();

			for( IRPModelElement theReference : theReferences ){

				if( theReference instanceof IRPObjectNode ){

					IRPObjectNode theObjectNode = (IRPObjectNode)theReference;

					List<IRPModelElement> theFlowTargets = getOutgoingFlowTargets( theObjectNode );

					if( !theFlowTargets.isEmpty() ){
						result.addItem( theObjectNode );
					}
				}
			}	
		}
	}

	// Used in context pattern tables
	void getObjectNodesWithFlowSources(
			IRPModelElement element, 
			IRPCollection result ){

		if( element instanceof IRPClass ){

			IRPClass theClass = (IRPClass) element;

			@SuppressWarnings("unchecked")
			List<IRPModelElement> theReferences = theClass.getReferences().toList();

			for( IRPModelElement theReference : theReferences ){

				if( theReference instanceof IRPObjectNode ){

					IRPObjectNode theObjectNode = (IRPObjectNode)theReference;

					List<IRPModelElement> theFlowSources = getIncomingFlowSources( theObjectNode );

					if( !theFlowSources.isEmpty() ){
						result.addItem( theObjectNode );
					}
				}
			}	
		}
	}

	// Used in context pattern tables
	void getObjectNodesWithNoFlowTargetsOrSources(
			IRPModelElement element, 
			IRPCollection result ){

		if( element instanceof IRPClass ){

			IRPClass theClass = (IRPClass) element;

			@SuppressWarnings("unchecked")
			List<IRPModelElement> theReferences = theClass.getReferences().toList();

			for( IRPModelElement theReference : theReferences ){

				if( theReference instanceof IRPObjectNode ){

					IRPObjectNode theObjectNode = (IRPObjectNode)theReference;

					List<IRPModelElement> theFlowTargets = getOutgoingFlowTargets( theObjectNode );
					List<IRPModelElement> theFlowSources = getIncomingFlowSources( theObjectNode );

					if( theFlowTargets.isEmpty() && theFlowSources.isEmpty() ) {
						result.addItem( theObjectNode );
					}
				}
			}	
		}
	}

	private static List<IRPModelElement> getOutgoingFlowTargets( 
			IRPStateVertex forVertex ){

		//RhapsodyAppServer.getActiveRhapsodyApplication().writeToOutputWindow("", "addOutgoingFlowTargets invoked for " + forVertex.getName() + "/n" );

		List<IRPModelElement> theFlowTargets = new ArrayList<>();

		@SuppressWarnings("unchecked")
		List<IRPTransition> theTransitions = forVertex.getOutTransitions().toList();

		for( IRPTransition theTransition : theTransitions ){

			IRPStateVertex theTarget = theTransition.getItsTarget();

			//RhapsodyAppServer.getActiveRhapsodyApplication().writeToOutputWindow("", "Transition found to " + theTarget.getName() + "/n" );

			if( theTarget instanceof IRPState ){

				theFlowTargets.add( theTarget );

			} else if( theTarget instanceof IRPPin ){

				IRPPin thePin = (IRPPin) theTarget;
				theFlowTargets.add( thePin.getParent() );

			} else if( theTarget instanceof IRPConnector ){

				// recursively call
				theFlowTargets.addAll( getOutgoingFlowTargets( theTarget ) );
			}
		}

		return theFlowTargets;
	}

	private static List<IRPModelElement> getIncomingFlowSources( 
			IRPStateVertex forVertex ){

		//RhapsodyAppServer.getActiveRhapsodyApplication().writeToOutputWindow("", "addOutgoingFlowTargets invoked for " + forVertex.getName() + "/n" );

		List<IRPModelElement> theFlowTargets = new ArrayList<>();

		@SuppressWarnings("unchecked")
		List<IRPTransition> theTransitions = forVertex.getInTransitions().toList();

		for( IRPTransition theTransition : theTransitions ){

			IRPStateVertex theTarget = theTransition.getItsSource();

			//RhapsodyAppServer.getActiveRhapsodyApplication().writeToOutputWindow("", "Transition found to " + theTarget.getName() + "/n" );

			if( theTarget instanceof IRPState ){

				theFlowTargets.add( theTarget );

			} else if( theTarget instanceof IRPPin ){

				IRPPin thePin = (IRPPin) theTarget;
				theFlowTargets.add( thePin.getParent() );

			} else if( theTarget instanceof IRPConnector ){

				// recursively call
				theFlowTargets.addAll( getIncomingFlowSources( theTarget ) );
			}			
		}

		return theFlowTargets;
	}

	// Used in context pattern tables
	void getOutgoingObjectFlowTargets(
			IRPModelElement element, 
			IRPCollection result ){

		if( element instanceof IRPStateVertex ){

			List<IRPModelElement> theFlowTargets = getOutgoingFlowTargets( (IRPStateVertex) element );	

			for( IRPModelElement theOutgoingFlowTarget : theFlowTargets ){
				result.addItem( theOutgoingFlowTarget );
			}		
		}
	}

	// Used in context pattern tables
	void getIncomingObjectFlowSources(
			IRPModelElement element, 
			IRPCollection result ){

		if( element instanceof IRPStateVertex ){

			List<IRPModelElement> theIncomingFlowSources = getIncomingFlowSources( (IRPStateVertex) element );

			for( IRPModelElement theIncomingFlowSource : theIncomingFlowSources ){
				result.addItem( theIncomingFlowSource );
			}		
		}
	}

	// Used in context pattern tables
	void getTracesToUnloadedRemoteRequirements(
			IRPModelElement element, 
			IRPCollection result ){

		if( element instanceof IRPRequirement ){

			IRPRequirement req = (IRPRequirement) element;

			@SuppressWarnings("unchecked")
			List<IRPDependency> theRemoteDependencies = req.getRemoteDependencies().toList();

			for( IRPDependency theRemoteDependency : theRemoteDependencies ){

				IRPModelElement theDependsOn = theRemoteDependency.getDependsOn();

				// If depends on is hyperlink rather than requirement then it's unloaded
				if( theDependsOn instanceof IRPHyperLink ){					
					result.addItem( theDependsOn );
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
				_settings.setupProjectWithProperties( "SysML" );
			}
		}
		return isContinue;
	}
}

/**
 * Copyright (C) 2018-2024  MBSE Training and Consulting Limited (www.executablembse.com)

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