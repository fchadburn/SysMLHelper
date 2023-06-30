package com.mbsetraining.sysmlhelper.executablembse;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import com.mbsetraining.sysmlhelper.common.BaseContext;
import com.mbsetraining.sysmlhelper.common.RequirementMover;
import com.mbsetraining.sysmlhelper.common.UserInterfaceHelper;
import com.telelogic.rhapsody.core.IRPActivityDiagram;
import com.telelogic.rhapsody.core.IRPActor;
import com.telelogic.rhapsody.core.IRPAttribute;
import com.telelogic.rhapsody.core.IRPClass;
import com.telelogic.rhapsody.core.IRPClassifier;
import com.telelogic.rhapsody.core.IRPDependency;
import com.telelogic.rhapsody.core.IRPDiagram;
import com.telelogic.rhapsody.core.IRPGraphElement;
import com.telelogic.rhapsody.core.IRPInstance;
import com.telelogic.rhapsody.core.IRPLink;
import com.telelogic.rhapsody.core.IRPModelElement;
import com.telelogic.rhapsody.core.IRPOperation;
import com.telelogic.rhapsody.core.IRPPackage;
import com.telelogic.rhapsody.core.IRPPort;
import com.telelogic.rhapsody.core.IRPProject;
import com.telelogic.rhapsody.core.IRPRequirement;
import com.telelogic.rhapsody.core.IRPStereotype;
import com.telelogic.rhapsody.core.IRPSysMLPort;
import com.telelogic.rhapsody.core.IRPUnit;

public class ExecutableMBSE_Context extends BaseContext {

	public final String FLOW_CONNECTOR = "Flow Connector";
	public final String ACTOR_USAGE = "Actor Usage";
	public final String SUBSYSTEM_USAGE = "Subsystem Usage";
	public final String SYSTEM_USAGE = "System Usage";
	public final String FUNCTION_USAGE = "Function Usage";
	public final String FUNCTION_BLOCK = "Function Block";
	public final String ITEM_BLOCK = "Item Block";
	public final String DATA_OBJECT = "Data Object";
	public final String FEATURE_BLOCK = "Feature Block";
	public final String FLOW_OUTPUT = "Flow Output";
	public final String FLOW_INPUT = "Flow Input";
	public final String GUARDED_FLOW_OUTPUT = "Guarded Flow Output";
	public final String OBJECT = "Object";
	public final String SYSTEM_BLOCK = "System Block";
	public final String CONTEXT_DIAGRAM = "Context Diagram";
	public final String BLOCK_DEFINITION_DIAGRAM_SYSTEM = "Block Definition Diagram - System";
	public final String INTERNAL_BLOCK_DIAGRAM_SYSTEM = "Internal Block Diagram - System";
	public final String INTERNAL_BLOCK_DIAGRAM_FUNCTIONAL = "Internal Block Diagram - Functional";
	public final String SIMPLE_REQUIREMENTS_TABLE = "Table View - Simple Requirements Table";
	public final String SUBSYSTEM_TABLE = "Table View - Subsystem Table";
	public final String USE_CASE_TO_REQUIREMENT_TABLE = "Table View - Use Case To Requirement";
	public final String ACTION_TO_REQUIREMENT_TABLE = "Table View - Action To Requirement";
	public final String REQUIREMENT_TO_USE_CASE_TABLE = "Table View - Requirement To Use Case";
	public final String REQUIREMENT_TO_FUNCTION_BLOCK_TABLE = "Table View - Requirement To Function Block";
	public final String REQUIREMENT_TO_ACTION_TABLE = "Table View - Requirement To Action";
	public final String CONTEXT_DIAGRAM_FLOWS_TABLE = "Table View - Context Diagram Flows";
	public final String REQTS_ANALYSIS_CONTEXT_DIAGRAM_PACKAGE = "10 Context Package";
	public final String REQTS_ANALYSIS_ACTOR_PACKAGE = "11 Actor Package";
	public final String REQTS_ANALYSIS_USE_CASE_PACKAGE = "12 Use Case Package";
	public final String REQTS_ANALYSIS_WORKING_COPY_PACKAGE = "12 Working Copy Package";
	public final String REQTS_ANALYSIS_REQUIREMENT_PACKAGE = "13 Requirement Package";
	public final String REQTS_ANALYSIS_EXTERNAL_SIGNALS_PACKAGE = "14 Signals Package";
	public final String FUNCT_ANALYSIS_SCENARIOS_PACKAGE = "21 Funct Analysis - Scenarios Package";
	public final String FUNCT_ANALYSIS_DESIGN_PACKAGE = "22 Funct Analysis - Design Package";
	public final String FUNCT_ANALYSIS_INTERFACES_PACKAGE = "23 Funct Analysis - Interfaces Package";
	public final String FUNCT_ANALYSIS_TEST_PACKAGE = "24 Funct Analysis - Test Package";
	public final String FUNCT_ANALYSIS_FEATURE_FUNCTION_PACKAGE = "22 Feature Function Package";
	public final String DESIGN_SYNTHESIS_SUBSYSTEM_INTERFACES_PACKAGE = "33 Subsystem Interfaces Package";
	public final String DESIGN_SYNTHESIS_LOGICAL_SYSTEM_PACKAGE = "31 System Architecture Package";
	public final String DESIGN_SYNTHESIS_SUBSYSTEM_PACKAGE = "32 Subsystem Package";
	public final String VIEW_AND_VIEWPOINT_PACKAGE = "41 View and Viewpoint Package";
	public final String REQUIREMENTS_DIAGRAM_SYSTEM = "Requirements Diagram - System";
	public final String TIME_ELAPSED_BLOCK_STEREOTYPE = "TimeBlock";
	public final String ELAPSED_TIME_GENERATOR_STEREOTYPE = "TimeGenerator";
	public final String NEW_TERM_FOR_USE_CASE_DIAGRAM = "EnhancedUseCaseDiagram";
	public final String NEW_TERM_FOR_SYSTEM_CONTEXT_DIAGRAM = "ContextDiagram";
	public final String NEW_TERM_FOR_REQUIRMENTS_DIAGRAM_SYSTEM = "RequirementsDiagramSystem";
	public final String NEW_TERM_FOR_TEXTUAL_ACTIVITY_DIAGRAM = "Textual Activity";
	public final String NEW_TERM_FOR_CALL_OPERATION_ACTIVITY_DIAGRAM = "Call Operation Activity";	
	public final String NEW_TERM_FOR_FEATURE_FUNCTION_PACKAGE = "FeatureFunctionPackage";
	public final String NEW_TERM_FOR_WORKING_COPY_PACKAGE = "WorkingCopyPackage";
	public final String NEW_TERM_FOR_ACTOR_USAGE = "ActorUsage";
	public final String NEW_TERM_FOR_SYSTEM_USAGE = "SystemUsage";
	public final String NEW_TERM_FOR_FUNCTION_USAGE = "FunctionUsage";
	public final String NEW_TERM_FOR_DECISION_USAGE = "DecisionUsage";
	public final String NEW_TERM_FOR_FEATURE_BLOCK = "FeatureBlock";
	public final String NEW_TERM_FOR_FUNCTION_BLOCK = "FunctionBlock";
	public final String NEW_TERM_FOR_PARALLEL_GATEWAY_USAGE = "ParallelGatewayUsage";
	public final String NEW_TERM_FOR_START_USAGE = "StartUsage";
	public final String NEW_TERM_FOR_FINAL_USAGE = "FinalUsage";
	public final String NEW_TERM_FOR_FLOW_FINAL_USAGE = "FlowFinalUsage";
	public final String NEW_TERM_FOR_TIME_EVENT_USAGE = "TimeEventUsage";
	public final String NEW_TERM_FOR_DATA_OBJECT = "DataObject";
	public final String NEW_TERM_FOR_SYSTEM_CONTEXT = "SystemUsage";
	public final String NEW_TERM_FOR_ACTOR_PACKAGE = "ActorPackage";
	public final String NEW_TERM_FOR_SYSTEM_ARCHITECTURE_PACKAGE = "SystemArchitecturePackage";
	public final String NEW_TERM_FOR_SCENARIOS_PACKAGE = "ScenariosPackage";
	public final String NEW_TERM_FOR_DESIGN_PACKAGE = "22 Funct Analysis - Design Package";
	public final String TESTBENCH_STEREOTYPE = "Testbench";
	public final String AUTO_RIPPLE_STEREOTYPE = "AutoRipple";
	public final String TABLE_VIEW_PREFIX = "table view - ";
	public final String ACTIVITY_DIAGRAM_PREFIX = "act - ";
	public final String CONTEXT_DIAGRAM_PREFIX = "ctx - ";
	public final String REQUIREMENTS_DIAGRAM_PREFIX = "req - ";
	public final String USE_CASE_DIAGRAM_PREFIX = "uc - ";
	public final String STATE_MACHINE_DIAGRAM_PREFIX = "stm - ";
	public final String BLOCK_DEFINITION_DIAGRAM_PREFIX = "bdd - ";
	public final String INTERNAL_BLOCK_DIAGRAM_PREFIX = "ibd - ";
	public final String SEQUENCE_DIAGRAM_PREFIX = "seq - ";
	public final String VIEW_PREFIX = "view - ";
	public final String VIEWPOINT_PREFIX = "viewpoint - ";
	public final String CUSTOMERVIEW_PREFIX = "customv - ";
	public final String QUERY_PREFIX = "query - ";
	public final String VIEWPOINT_DIAGRAM_PREFIX = "vvd - ";
	public final String VIEW_STRUCTURE_STEREOTYPE = "ViewStructure";
	public final String POST_FIX_FOR_FEATURE_FUNCTION_PKG = "_FeaturePkg";
	public final String POST_FIX_FOR_FEATURE_FUNCTION_WORKING_COPY_PKG = "_WorkingCopyFeaturePkg";
	public final String CUSTOMV_TBD = "customv - TBD";
	public final String VIEW_TBD = "view - TBD";
	public final String CUSTOMV_TBD_EXPLICIT_ONLY = "customv - TBD explicit only";
	public final String VIEWPOINT_TBD = "viewpoint - TBD";
	public final String VIEW_AND_VIEWPOINT_DIAGRAM_TBD = "vvd - TBD";
	public final String QUERY_TBD = "query - TBD";
	public final String QUERYTBD_EXPLICIT_ONLY = "query - TBD explicit only";
	public final String STEREOTYPE_TBD = "TBD";
	public final String USECASEPKG_POSTFIX = "_UseCasePkg";
	public final String REQUIREMENTPKG_POSTFIX = "_RequirementPkg";
	public final String CONTEXTPKG_POSTFIX = "_ContextPkg";
	public final String ACTORPKG_POSTFIX = "_ActorPkg";
	public final String SIGNALPKG_POSTFIX = "_SignalPkg";
	public final String ARCHITECTUREPKG_POSTFIX = "_ArchitecturePkg";
	
	protected SelectedElementContext _selectionContext;

	protected String _defaultExternalSignalsPackageName;
	protected String _defaultContextDiagramPackageName;
	protected String _defaultActorPackageName;
	protected String _defaultRequirementsPackageName;

	protected Boolean _isEnableAutoMoveOfEventsOnAddNewElement;
	protected Boolean _isEnableAutoMoveOfEventsOnFlowCreation;
	protected Boolean _isEnableAutoMoveOfEventsOnFlowConnectorCreation;
	protected Boolean _isEnableAutoMoveOfRequirements;
	protected Boolean _isSendEventViaPanelOptionEnabled;
	protected Boolean _isSendEventViaPanelWantedByDefault;
	protected Boolean _isPopulateWantedByDefault;
	protected Boolean _isPopulateOptionHidden;
	protected Boolean _isCreateParametricSubpackageSelected;
	protected Boolean _isCallOperationSupportEnabled;
	protected Boolean _isCreateSDWithAutoShowApplied;
	protected Boolean _isCreateSDWithTestDriverLifeline;
	protected Boolean _isShowProfileVersionCheckDialogs;
	protected Boolean _isDoubleClickFunctionalityEnabled;
	
	protected List<String> _storeUnitInSeparateDirectoryNewTerms;
	protected List<String> _dontCreateSeparateUnitNewTerms;
	protected List<IRPModelElement> _stereotypesForBlockPartCreation;
	protected String _autoGenerationOfPortsForLinksPolicy;
	protected String _autoGenerationOfPortsForLinksDefaultType;
	protected String _bleedForegroundColor;
	protected String _templateForActivityDiagramValue;

	protected IRPStereotype _stereotypeForTestbench;
	protected IRPStereotype _stereotypeForTimeElapsedActor;
	protected IRPStereotype _stereotypeForTimeElapsedBlock;
	protected IRPStereotype _stereotypeForAutoRipple;
	protected IRPStereotype _newTermForUseCaseDiagram;
	protected IRPStereotype _newTermForSystemContextDiagram;
	protected IRPStereotype _newTermForActorUsage;
	protected IRPStereotype _newTermForSystemContext;
	protected IRPStereotype _stereotypeForDerivation;
	protected IRPStereotype _stereotypeForSatisfaction;
	protected IRPStereotype _stereotypeForVerification;
	protected IRPStereotype _stereotypeForTrace;

	public ExecutableMBSE_Context(
			String theAppID ){

		super(  theAppID, 
				"ExecutableMBSEProfile.General.EnableErrorLogging", 
				"ExecutableMBSEProfile.General.EnableWarningLogging", 
				"ExecutableMBSEProfile.General.EnableInfoLogging", 
				"ExecutableMBSEProfile.General.EnableDebugLogging", 
				"ExecutableMBSEProfile.General.PluginVersion", 
				"ExecutableMBSEProfile.General.UserDefinedMetaClassesAsSeparateUnit", 
				"ExecutableMBSEProfile.General.AllowPluginToControlUnitGranularity" );
	}
	
	// Generally single call per session, so use lazy load
	public IRPStereotype getStereotypeForDerivation(){

		if( _stereotypeForDerivation == null ){

			_stereotypeForDerivation = getExistingStereotype( "derive", _rhpPrj );;
		}

		return _stereotypeForDerivation;
	}
	
	// Generally single call per session, so use lazy load
	public IRPStereotype getStereotypeForSatisfaction(){

		if( _stereotypeForSatisfaction == null ){

			_stereotypeForSatisfaction = getExistingStereotype( "satisfy", _rhpPrj );;
		}

		return _stereotypeForSatisfaction;
	}

	// Generally single call per session, so use lazy load
	public IRPStereotype getStereotypeForVerification(){

		if( _stereotypeForVerification == null ){

			_stereotypeForVerification = getExistingStereotype( "verify", _rhpPrj );;
		}

		return _stereotypeForVerification;
	}
	
	// use lazy load
	public IRPStereotype getStereotypeForTrace(){

		if( _stereotypeForTrace == null ){

			_stereotypeForTrace = getExistingStereotype( "trace", _rhpPrj );;
		}

		return _stereotypeForTrace;
	}
	
	// Generally single call per session, so use lazy load
	public String getDefaultExternalSignalsPackageName(){

		if( _defaultExternalSignalsPackageName == null ){

			_defaultExternalSignalsPackageName = _rhpPrj.getPropertyValue(
					"ExecutableMBSEProfile.RequirementsAnalysis.DefaultExternalSignalsPackageName" );
		}

		return _defaultExternalSignalsPackageName;
	}

	// Generally single call per session, so use lazy load
	public String getDefaultContextDiagramPackageName(){

		if( _defaultContextDiagramPackageName == null ){
			_defaultContextDiagramPackageName = _rhpPrj.getPropertyValue(
					"ExecutableMBSEProfile.RequirementsAnalysis.DefaultContextDiagramPackageName" );
		}

		return _defaultContextDiagramPackageName;
	}

	// Generally single call per session, so use lazy load
	public String getDefaultActorPackageName(){

		if( _defaultActorPackageName == null ){
			_defaultActorPackageName = _rhpPrj.getPropertyValue(
					"ExecutableMBSEProfile.RequirementsAnalysis.DefaultActorPackageName" );
		}

		return _defaultActorPackageName;
	}

	// Generally single call per session, so use lazy load
	public String getDefaultRequirementsPackageName(){

		if( _defaultRequirementsPackageName == null ){
			_defaultRequirementsPackageName = _rhpPrj.getPropertyValue(
					"ExecutableMBSEProfile.RequirementsAnalysis.DefaultRequirementsPackageName" );
		}

		return _defaultRequirementsPackageName;
	}

	// Multiple calls per session, so use lazy load
	public Boolean getIsEnableAutoMoveOfEventsOnFlowCreation(){

		if( _isEnableAutoMoveOfEventsOnFlowCreation == null ){
			_isEnableAutoMoveOfEventsOnFlowCreation = getBooleanPropertyValue(
					_rhpPrj,
					"ExecutableMBSEProfile.RequirementsAnalysis.IsEnableAutoMoveOfEventsOnFlowCreation" );
		}

		return _isEnableAutoMoveOfEventsOnFlowCreation;
	}

	// Multiple calls per session, so use lazy load
	public Boolean getIsEnableAutoMoveOfEventsOnFlowConnectorCreation(){

		if( _isEnableAutoMoveOfEventsOnFlowConnectorCreation == null ){

			_isEnableAutoMoveOfEventsOnFlowConnectorCreation = getBooleanPropertyValue(
					_rhpPrj,
					"ExecutableMBSEProfile.FunctionalAnalysis.IsEnableAutoMoveOfEventsOnFlowConnectorCreation" );
		}

		return _isEnableAutoMoveOfEventsOnFlowConnectorCreation;
	}

	// Multiple calls per session, so use lazy load
	public Boolean getIsEnableAutoMoveOfEventsOnAddNewElement(){

		if( _isEnableAutoMoveOfEventsOnAddNewElement == null ){

			_isEnableAutoMoveOfEventsOnAddNewElement  = getBooleanPropertyValue(
					_rhpPrj,
					"ExecutableMBSEProfile.RequirementsAnalysis.IsEnableAutoMoveOfEventsOnAddNewElement" );
		}

		return _isEnableAutoMoveOfEventsOnAddNewElement;
	}

	// Multiple calls per session, so use lazy load
	public List<String> getStoreUnitInSeparateDirectoryNewTerms(){

		if( _storeUnitInSeparateDirectoryNewTerms == null ){
			_storeUnitInSeparateDirectoryNewTerms = getListFromCommaSeparatedString(
					_rhpPrj, 
					"ExecutableMBSEProfile.General.StoreUnitInSeparateDirectoryNewTerms" );			
		}

		return _storeUnitInSeparateDirectoryNewTerms;
	}

	// Multiple calls per session, so use lazy load
	public List<String> getDontCreateSeparateUnitNewTerms(){

		if( _dontCreateSeparateUnitNewTerms == null ){

			_dontCreateSeparateUnitNewTerms = getListFromCommaSeparatedString(
					_rhpPrj, 
					"ExecutableMBSEProfile.General.DontCreateSeparateUnitNewTerms" );
		}

		return _dontCreateSeparateUnitNewTerms;
	}

	// Multiple calls per session, so use lazy load
	public Boolean getIsEnableAutoMoveOfRequirements(){

		if( _isEnableAutoMoveOfRequirements == null ){

			_isEnableAutoMoveOfRequirements = getBooleanPropertyValue(
					_rhpPrj,
					"ExecutableMBSEProfile.RequirementsAnalysis.IsEnableAutoMoveOfRequirements" );
		}

		return _isEnableAutoMoveOfRequirements;
	}
	
	public Boolean getIsKeepRequirementUnderFunctionBlock(
			IRPModelElement theContextEl ){

		return getBooleanPropertyValue(
					theContextEl,
					"ExecutableMBSEProfile.RequirementsAnalysis.IsKeepRequirementUnderFunctionBlock" );
	}

	// Single call per session, but use lazy load
	public List<IRPModelElement> getStereotypesForBlockPartCreation(){

		if( _stereotypesForBlockPartCreation == null ){			
			_stereotypesForBlockPartCreation = getStereotypesBasedOnProperty(
					_rhpPrj, 
					"ExecutableMBSEProfile.FunctionalAnalysis.StereotypesForBlockCreation" );		
		}

		return _stereotypesForBlockPartCreation;
	}

	public Boolean getIsShowProfileVersionCheckDialogs(){

		if( _isShowProfileVersionCheckDialogs == null ){	
			_isShowProfileVersionCheckDialogs = getBooleanPropertyValue(
				_rhpPrj,
				"ExecutableMBSEProfile.General.IsShowProfileVersionCheckDialogs" );
		}
		
		return _isShowProfileVersionCheckDialogs;
	}
	
	// This is called on double-click 
	public Boolean getIsDoubleClickFunctionalityEnabled(){
		
		_isDoubleClickFunctionalityEnabled = getBooleanPropertyValue(
					_rhpPrj,
					"ExecutableMBSEProfile.General.IsDoubleClickFunctionalityEnabled" );
		
		return _isDoubleClickFunctionalityEnabled;
	}

	public IRPStereotype getStereotypeForTestbench(){

		if( _stereotypeForTestbench == null ){

			_stereotypeForTestbench = getStereotypeWith( 
					TESTBENCH_STEREOTYPE );

			if( _stereotypeForTestbench == null ){
				super.error( "Error in getStereotypeForTestbench, no Stereotyped called " + 
						TESTBENCH_STEREOTYPE + " was found" );
			}	
		}

		return _stereotypeForTestbench;
	}

	public IRPStereotype getStereotypeForTimeElapsedActor(){

		if( _stereotypeForTimeElapsedActor == null ){

			_stereotypeForTimeElapsedActor = getStereotypeWith( 
					ELAPSED_TIME_GENERATOR_STEREOTYPE );

			if( _stereotypeForTimeElapsedActor == null ){
				super.error( "Error in getStereotypeForTimeElapsedActor, no Stereotyped called " + 
						ELAPSED_TIME_GENERATOR_STEREOTYPE + " was found" );
			}	
		}

		return _stereotypeForTimeElapsedActor;
	}

	public IRPStereotype getStereotypeForTimeElapsedBlock(){

		if( _stereotypeForTimeElapsedBlock == null ){

			_stereotypeForTimeElapsedBlock = getStereotypeWith( 
					TIME_ELAPSED_BLOCK_STEREOTYPE );

			if( _stereotypeForTimeElapsedBlock == null ){
				super.error( "Error in getStereotypeForTimeElapsedBlock, no Stereotyped called " + 
						TIME_ELAPSED_BLOCK_STEREOTYPE + " was found" );
			}
		}

		return _stereotypeForTimeElapsedBlock;
	}

	public IRPStereotype getStereotypeForAutoRipple(){

		if( _stereotypeForAutoRipple == null ){

			_stereotypeForAutoRipple = getStereotypeWith( 
					AUTO_RIPPLE_STEREOTYPE );

			if( _stereotypeForAutoRipple == null ){
				super.error( "Error in getStereotypeForAutoRipple, no Stereotyped called " + 
						AUTO_RIPPLE_STEREOTYPE + " was found" );
			}
		}

		return _stereotypeForAutoRipple;
	}

	public IRPStereotype getNewTermForUseCaseDiagram(){

		if( _newTermForUseCaseDiagram == null ){

			_newTermForUseCaseDiagram = getStereotypeWith( 
					NEW_TERM_FOR_USE_CASE_DIAGRAM );

			if( _newTermForUseCaseDiagram == null ){
				super.error( "Error in getNewTermForUseCaseDiagram, no Stereotyped called " + 
						NEW_TERM_FOR_USE_CASE_DIAGRAM + " was found" );
			}	
		}

		return _newTermForUseCaseDiagram;
	}

	public IRPStereotype getNewTermForSystemContextDiagram(){

		if( _newTermForSystemContextDiagram == null ){

			_newTermForSystemContextDiagram = getStereotypeWith( 
					NEW_TERM_FOR_SYSTEM_CONTEXT_DIAGRAM );

			if( _newTermForSystemContextDiagram == null ){

				super.error( "Error in getNewTermForSystemContextDiagram, no Stereotyped called " + 
						NEW_TERM_FOR_SYSTEM_CONTEXT_DIAGRAM + " was found" );
			}	
		}

		return _newTermForSystemContextDiagram;
	}

	public IRPStereotype getNewTermForActorUsage(){

		if( _newTermForActorUsage == null ){

			_newTermForActorUsage = getStereotypeWith( 
					NEW_TERM_FOR_ACTOR_USAGE );

			if( _newTermForActorUsage == null ){

				super.error( "Error in getNewTermForActorUsage, no Stereotyped called " + 
						NEW_TERM_FOR_ACTOR_USAGE + " was found" );
			}				
		}

		return _newTermForActorUsage;
	}

	public IRPStereotype getNewTermForSystemContext(){

		if( _newTermForSystemContext == null ){

			_newTermForSystemContext = getStereotypeWith( 
					NEW_TERM_FOR_SYSTEM_CONTEXT );

			if( _newTermForSystemContext == null ){			
				super.error( "Error in getNewTermForSystemContext, no Stereotyped called " + 
						NEW_TERM_FOR_SYSTEM_CONTEXT + " was found" );
			}		
		}

		return _newTermForSystemContext;
	}

	public List<IRPActor> getMasterActorList(
			IRPProject forProject ) {

		List<IRPActor> theMasterActors = new ArrayList<>();

		List<IRPModelElement> theMasterActorStereotypes =
				getStereotypesForMasterActorPackage( 
						forProject );

		for( IRPModelElement theMasterActorStereotype : theMasterActorStereotypes ){

			List<IRPModelElement> theActorPackages = 
					findElementsWithMetaClassAndStereotype(
							"Package", 
							theMasterActorStereotype.getName(), 
							forProject, 
							1 );

			//super.debug( "Looking for packages with metaclass " + 
			//		theMasterActorStereotype.getName() + ", found " + theActorPackages.size() );

			for( IRPModelElement theActorPackage : theActorPackages ){

				@SuppressWarnings("unchecked")
				List<IRPModelElement> theExistingActorEls = 
				theActorPackage.getNestedElementsByMetaClass(
						"Actor", 1 ).toList();

				if( theExistingActorEls.isEmpty() ){

					UserInterfaceHelper.showWarningDialog( "The existing " + 
							super.elInfo( theActorPackage ) + 
							" has no actors in it, hence none will be created" );
				} else {
					for( IRPModelElement theExistingActorEl : theExistingActorEls ){
						theMasterActors.add( (IRPActor) theExistingActorEl );
					}
				}
			}	
		}

		if( theMasterActors.isEmpty() ){

			super.info( "As no master actors were found, " +
					"I'm creating a new master actors package" );

			IRPPackage theMasterActorsPkg = 
					(IRPPackage) forProject.addNewAggr(
							"Package", "MasterActorsPkg" );

			if( !theMasterActorStereotypes.isEmpty() ){

				IRPModelElement theMasterActorPackageStereotype =
						theMasterActorStereotypes.get(0);

				theMasterActorsPkg.changeTo( theMasterActorPackageStereotype.getName() );
			}

			List<String> theDefaultActorNames =
					getDefaultActorsForMasterActorsPackage( forProject );

			for( String theDefaultActorName : theDefaultActorNames ){

				theMasterActors.add( 
						theMasterActorsPkg.addActor( 
								theDefaultActorName ) );
			}
		}

		return theMasterActors;
	}

	public String getDefaultUseCasePackageName(
			IRPModelElement basedOnContext ){

		String theValue = basedOnContext.getPropertyValue(
				"ExecutableMBSEProfile.RequirementsAnalysis.DefaultUseCasePackageName" );

		if( theValue == null || theValue.isEmpty() ){
			theValue = "Error";
		}

		return theValue;
	}

	public boolean getIsAutoPopulatePackageDiagram(
			IRPModelElement basedOnContext ){

		boolean result = getBooleanPropertyValue(
				basedOnContext,
				"ExecutableMBSEProfile.General.IsAutoPopulatePackageDiagram" );

		return result;		
	}

	public String getCSVExportArtifactType(
			IRPModelElement basedOnContext ){

		String theValue = basedOnContext.getPropertyValue(
				"ExecutableMBSEProfile.RequirementsAnalysis.CSVExportArtifactType" );

		if( theValue == null || theValue.isEmpty() ){
			theValue = "Error";
		}

		return theValue;
	}
	
	public String getCSVExportSeparator(
			IRPModelElement basedOnContext ){

		String theValue = basedOnContext.getPropertyValue(
				"ExecutableMBSEProfile.RequirementsAnalysis.CSVExportSeparator" );

		if( theValue == null || theValue.isEmpty() ){
			theValue = "Error";
		}

		return theValue;
	}

	public boolean getCSVExportIncludeArtifactName(
			IRPModelElement basedOnContext ){

		boolean result = getBooleanPropertyValue(
				basedOnContext,
				"ExecutableMBSEProfile.RequirementsAnalysis.CSVExportIncludeArtifactName" );

		return result;		
	}

	public List<String> getDefaultActorsForMasterActorsPackage(
			IRPPackage theContextEl ){

		return getDefaultLegalActorNamesFor(
				theContextEl, 
				"ExecutableMBSEProfile.General.DefaultActorsForMasterActorsPackage");
	}

	public List<String> getDefaultActorsForUseCaseDiagram(
			IRPPackage theContextEl ){

		return getDefaultLegalActorNamesFor(
				theContextEl, 
				"ExecutableMBSEProfile.RequirementsAnalysis.DefaultActorsForUseCaseDiagram");
	}

	private List<String> getDefaultLegalActorNamesFor(
			IRPPackage theContextEl,
			String basedOnPropertyKey ){

		List<String> theActorNames = new ArrayList<>();

		String theValue = theContextEl.getPropertyValue( 
				basedOnPropertyKey );

		String theRegEx = theContextEl.getPropertyValue( 
				"General.Model.NamesRegExp" );

		if( theValue != null && !theValue.isEmpty() ){

			String[] split = theValue.split(",");

			for (int i = 0; i < split.length; i++) {
				String theActorName = split[ i ].trim();

				if( theActorName.matches( theRegEx ) ){
					theActorNames.add( theActorName );
				}
			}
		}

		return theActorNames;
	}

	public void setSavedInSeparateDirectoryIfAppropriateFor(
			IRPModelElement modelElement) {

		//super.debug("setSavedInSeparateDirectoryIfAppropriateFor was invoked for " + Logger.elementInfo(modelElement) );

		if( modelElement instanceof IRPPackage ){

			if( getStoreUnitInSeparateDirectoryNewTerms().contains( 
					modelElement.getUserDefinedMetaClass() ) ){

				IRPPackage thePackage = (IRPPackage) modelElement;
				thePackage.setSavedInSeperateDirectory( 1 );
			}
		}
	}

	public void setDontSaveAsSeparateUnitIfAppropriateFor(
			IRPModelElement modelElement ){

		//super.debug( "setDontSaveAsSeparateUnitIfAppropriateFor was invoked for " + super.elInfo( modelElement ) );

		if( modelElement != null ){

			if( getDontCreateSeparateUnitNewTerms().contains( 
					modelElement.getUserDefinedMetaClass() ) ){

				((IRPUnit) modelElement).setSeparateSaveUnit( 0 );		
			}
		}
	}

	public List<IRPModelElement> getStereotypesForFunctionalDesignRootPackage(
			IRPModelElement forContextEl ){

		return getStereotypesBasedOnProperty(
				forContextEl, 
				"ExecutableMBSEProfile.General.RootPackageStereotypes" );
	}

	public List<IRPModelElement> getStereotypesForMasterActorPackage(
			IRPModelElement forContextEl ){

		return getStereotypesBasedOnProperty(
				forContextEl, 
				"ExecutableMBSEProfile.General.MasterActorPackageStereotypes" );
	}

	public List<IRPModelElement> getStereotypesForSystemDesignPackage(
			IRPModelElement forContextEl ){

		return getStereotypesBasedOnProperty(
				forContextEl, 
				"ExecutableMBSEProfile.General.SystemLevelPackageStereotypes" );
	}

	public List<IRPModelElement> getStereotypesBasedOnProperty(
			IRPModelElement forContextEl,
			String thePropertyKey ){

		List<IRPModelElement> theStereotypes = new ArrayList<>();

		try {
			String theStereotypeList = forContextEl.getPropertyValue(
					thePropertyKey );

			if( theStereotypeList != null){

				String[] split = theStereotypeList.split(",");

				for( String theString : split ){

					List<IRPModelElement> theStereotypeCandidates = 
							findElementsWithMetaClassAndName( 
									"Stereotype", theString.trim(), forContextEl );

					if( theStereotypeCandidates.size() == 0 ){

						super.warning( "Unable to find " + theString + " specified in " + 
								thePropertyKey + " property" );

					} else if( theStereotypeCandidates.size() == 1 ){

						theStereotypes.add( theStereotypeCandidates.get( 0 ) );
					} else {

						for( IRPModelElement theStereotypeCandidate : theStereotypeCandidates ){

							String theFullName = theStereotypeCandidate.getFullPathName();
							super.debug (theFullName);

							if( theFullName.startsWith( "ExecutableMBSEProfile" ) ){
								theStereotypes.add( theStereotypeCandidate );
								break;
							}
						}
					}
				}

				super.debug( "getStereotypesBasedOnProperty  found " + theStereotypeList + 
						", it is returning a list of x" + theStereotypes.size() + " stereotypes" );
			}
		} catch( Exception e ){
			super.error( "Exception in getStereotypesBasedOnProperty, e=" + e.getMessage() );
		}

		return theStereotypes;
	}

	public String getInterfacesPackageStereotype(
			IRPModelElement basedOnContextEl ) {

		String thePropertyValue = 
				basedOnContextEl.getPropertyValue( 
						"ExecutableMBSEProfile.General.InterfacesPackageStereotype" );

		return thePropertyValue;
	}

	public String getUseCaseNoteText(
			IRPModelElement basedOnContextEl ){

		String thePropertyValue = 
				basedOnContextEl.getPropertyValue( 
						"ExecutableMBSEProfile.RequirementsAnalysis.UseCaseNoteText" );

		if( thePropertyValue == null ){
			thePropertyValue = "Error in getUseCaseNoteText";
		}

		return thePropertyValue;
	}

	public String getBleedForegroundColor(){

		if( _bleedForegroundColor == null ){

			_bleedForegroundColor = _rhpPrj.getPropertyValue( 
					"ExecutableMBSEProfile.RequirementsAnalysis.BleedForegroundColor" );
		}

		return _bleedForegroundColor;
	}

	public boolean getIsApplyAutoShowToSequenceDiagramTemplate(
			IRPModelElement forContextEl ){

		boolean result = getBooleanPropertyValue(
				forContextEl,
				"ExecutableMBSEProfile.FunctionalAnalysis.IsApplyAutoShowToSequenceDiagramTemplate" );

		return result;
	}

	public boolean getIsEnableAutoMoveOfInterfaces(
			IRPModelElement forContextEl ){

		boolean result = getBooleanPropertyValue(
				forContextEl,
				"ExecutableMBSEProfile.FunctionalAnalysis.IsEnableAutoMoveOfInterfaces" );

		return result;
	}

	public boolean getIsEnableGatewayTypes(
			IRPModelElement forContextEl ){

		boolean result = getBooleanPropertyValue(
				forContextEl,
				"ExecutableMBSEProfile.RequirementsAnalysis.IsEnableGatewayTypes" );

		return result;
	}

	public boolean getIsSendEventViaPanelOptionEnabled(){

		if( _isSendEventViaPanelOptionEnabled == null ){

			_isSendEventViaPanelOptionEnabled = getBooleanPropertyValue(
					_rhpPrj,
					"ExecutableMBSEProfile.FunctionalAnalysis.IsSendEventViaPanelOptionEnabled" );
		}

		return _isSendEventViaPanelOptionEnabled;
	}

	public boolean getIsSendEventViaPanelWantedByDefault(){

		if( _isSendEventViaPanelWantedByDefault == null ){

			_isSendEventViaPanelWantedByDefault = getBooleanPropertyValue(
					_rhpPrj,
					"ExecutableMBSEProfile.FunctionalAnalysis.IsSendEventViaPanelWantedByDefault" );
		}

		return _isSendEventViaPanelWantedByDefault;
	}

	public boolean getIsPopulateWantedByDefault(){

		if( _isPopulateWantedByDefault == null ){
			_isPopulateWantedByDefault = getBooleanPropertyValue(
					_rhpPrj, 
					"ExecutableMBSEProfile.FunctionalAnalysis.IsPopulateWantedByDefault" );
		}

		return _isPopulateWantedByDefault;
	}

	public boolean getIsPopulateOptionHidden(){

		if( _isPopulateOptionHidden == null ){			
			_isPopulateOptionHidden = getBooleanPropertyValue(
					_rhpPrj,
					"ExecutableMBSEProfile.FunctionalAnalysis.IsPopulateOptionHidden" );
		}

		return _isPopulateOptionHidden;
	}
	
	public String getTemplateForActivityDiagramValue(){

		
		if( _templateForActivityDiagramValue == null ){		
			
			_templateForActivityDiagramValue = getStringPropertyValueFromRhp(
					_rhpPrj,
					"ExecutableMBSEProfile.RequirementsAnalysis.TemplateForActivityDiagram" ,
					"template_for_act" );			
		}

		return _templateForActivityDiagramValue;
	}

	public boolean getIsCreateParametricSubpackageSelected(){

		if( _isCreateParametricSubpackageSelected == null ){			

			_isCreateParametricSubpackageSelected = getBooleanPropertyValue(
					_rhpPrj,
					"ExecutableMBSEProfile.General.IsCreateParametricSubpackageSelected" );
		}

		return _isCreateParametricSubpackageSelected;
	}

	public boolean getIsCallOperationSupportEnabled(){

		if( _isCallOperationSupportEnabled == null ){

			_isCallOperationSupportEnabled = getBooleanPropertyValue(
					_rhpPrj,
					"ExecutableMBSEProfile.FunctionalAnalysis.IsCallOperationSupportEnabled" );
		}

		return _isCallOperationSupportEnabled;
	}

	public boolean getIsCreateSDWithAutoShowApplied(){

		if( _isCreateSDWithAutoShowApplied == null ){

			_isCreateSDWithAutoShowApplied = getBooleanPropertyValue(
					_rhpPrj,
					"ExecutableMBSEProfile.FunctionalAnalysis.IsCreateSDWithAutoShowApplied" );
		}


		return _isCreateSDWithAutoShowApplied;
	}

	public boolean getIsCreateSDWithTestDriverLifeline(){

		if( _isCreateSDWithTestDriverLifeline == null ){

			_isCreateSDWithTestDriverLifeline = getBooleanPropertyValue(
					_rhpPrj,
					"ExecutableMBSEProfile.FunctionalAnalysis.IsCreateSDWithTestDriverLifeline" );
		}

		return _isCreateSDWithTestDriverLifeline;
	}

	public boolean getIsConvertToDetailedADOptionEnabled(
			IRPModelElement forContextEl ){

		boolean result = getBooleanPropertyValue(
				forContextEl, 
				"ExecutableMBSEProfile.FunctionalAnalysis.IsConvertToDetailedADOptionEnabled" );

		return result;
	}

	public boolean getIsConvertToDetailedADOptionWantedByDefault(
			IRPModelElement forContextEl ){

		boolean result = getBooleanPropertyValue(
				forContextEl,
				"ExecutableMBSEProfile.FunctionalAnalysis.IsConvertToDetailedADOptionWantedByDefault" );

		return result;
	}

	public boolean getIsAllowInheritanceChoices(
			IRPModelElement forContextEl ){

		boolean result = getBooleanPropertyValue(
				forContextEl,
				"ExecutableMBSEProfile.FunctionalAnalysis.IsAllowInheritanceChoices" );

		return result;
	}

	public boolean getIsShowProfileVersionCheckDialogs(
			IRPModelElement forContextEl ){

		boolean result = getBooleanPropertyValue(
				forContextEl,
				"ExecutableMBSEProfile.General.IsShowProfileVersionCheckDialogs" );

		return result;
	}

	public String getAutoGenerationOfPortsForLinksPolicy(
			IRPModelElement forContextEl ){
		
		_autoGenerationOfPortsForLinksPolicy = getStringPropertyValueFromRhp(
				forContextEl,
				"ExecutableMBSEProfile.DesignSynthesis.AutoGenerationOfPortsForLinksPolicy",
				"Never" );

		return _autoGenerationOfPortsForLinksPolicy;
	}
	
	public String getAutoGenerationOfPortsForLinksDefaultType(
			IRPModelElement forContextEl ){
		
		_autoGenerationOfPortsForLinksDefaultType = getStringPropertyValueFromRhp(
				forContextEl,
				"ExecutableMBSEProfile.DesignSynthesis.AutoGenerationOfPortsForLinksDefaultType",
				"StandardPorts" );

		return _autoGenerationOfPortsForLinksDefaultType;
	}

	@Override
	public IRPPackage addNewTermPackageAndSetUnitProperties( 
			String theName,
			IRPPackage theOwner,
			String theNewTermName ){

		IRPPackage thePackage = theOwner.addNestedPackage( theName );

		thePackage.changeTo( theNewTermName );

		setSavedInSeparateDirectoryIfAppropriateFor( thePackage );
		setDontSaveAsSeparateUnitIfAppropriateFor( thePackage );

		return thePackage;
	}

	public Set<IRPPackage> getPullFromPackage( 
			IRPModelElement basedOnEl ){

		Set<IRPPackage> thePullFromPkgs = new HashSet<>();

		Set<IRPModelElement> theCandidateEls =
				super.getStereotypedElementsThatHaveDependenciesFrom( 
						basedOnEl, 
						REQTS_ANALYSIS_USE_CASE_PACKAGE );

		if( theCandidateEls.size()>0 ){

			for( IRPModelElement theCandidateEl : theCandidateEls ){

				if( theCandidateEl instanceof IRPPackage ){
					thePullFromPkgs.add( (IRPPackage) theCandidateEl );
				}
			}

		} else if( theCandidateEls.size()==0 ){

			IRPModelElement theOwner = basedOnEl.getOwner();

			if( !(theOwner instanceof IRPProject) ){
				thePullFromPkgs = getPullFromPackage( theOwner );

			} else {				
				IRPModelElement theRequirementsAnalysisPkg = 
						basedOnEl.getProject().findElementsByFullName(
								"RequirementsAnalysisPkg", "Package" );

				if( theRequirementsAnalysisPkg != null ){
					thePullFromPkgs.add( (IRPPackage) theRequirementsAnalysisPkg );				
				}
			}
		}

		return thePullFromPkgs;
	}

	public List<IRPModelElement> getNonActorOrTestingClassifiersConnectedTo( 
			IRPClassifier theClassifier,
			IRPClass inTheBuildingBlock ){

		@SuppressWarnings("unchecked")
		List<IRPClassifier> theBaseClassifiers = theClassifier.getBaseClassifiers().toList();

		List<IRPModelElement> theClassifiersConnectedTo = new ArrayList<IRPModelElement>();

		@SuppressWarnings("unchecked")
		List<IRPModelElement> theConnectors = inTheBuildingBlock.getLinks().toList();

		for( IRPModelElement theConnector : theConnectors ) {

			IRPLink theLink = (IRPLink) theConnector;

			IRPPort theFromPort = theLink.getFromPort();
			IRPPort theToPort = theLink.getToPort();

			if( theFromPort != null && theToPort != null ){

				IRPModelElement fromPortOwner = theFromPort.getOwner();
				IRPModelElement toPortOwner = theToPort.getOwner();

				if( fromPortOwner.equals( theClassifier ) || 
						theBaseClassifiers.contains( fromPortOwner ) ){

					if( toPortOwner instanceof IRPClass &&
							!hasStereotypeCalled("TestDriver", toPortOwner) ){
						theClassifiersConnectedTo.add( toPortOwner );
					}

				} else if( toPortOwner.equals( theClassifier ) ||
						theBaseClassifiers.contains( toPortOwner )){

					if( fromPortOwner instanceof IRPClass &&
							!hasStereotypeCalled("TestDriver", fromPortOwner) ){
						theClassifiersConnectedTo.add( fromPortOwner );
					}
				}
			}
		}

		return theClassifiersConnectedTo;
	}

	public boolean isTestDriver( 
			IRPInstance thePart ){

		IRPClassifier theType = thePart.getOtherClass();

		boolean isTestDriver = hasStereotypeCalled( "TestDriver", theType );

		return isTestDriver;
	}

	public IRPPort getPortThatConnects(
			IRPClassifier theSourceClassifier,
			IRPClassifier withTheTargetClassifier, 
			IRPClass inTheBuildingBlock ) {

		IRPPort thePort = null;

		@SuppressWarnings("unchecked")
		List<IRPModelElement> theConnectors = inTheBuildingBlock.getLinks().toList();

		@SuppressWarnings("unchecked")
		List<IRPClassifier> theTargetClassifiers = 
		withTheTargetClassifier.getBaseClassifiers().toList();

		theTargetClassifiers.add( withTheTargetClassifier );

		@SuppressWarnings("unchecked")
		List<IRPClassifier> theSourceClassifiers = 
		theSourceClassifier.getBaseClassifiers().toList();

		theSourceClassifiers.add( theSourceClassifier );

		for( IRPModelElement theConnector : theConnectors ){

			IRPLink theLink = (IRPLink) theConnector;

			IRPPort theFromPort = theLink.getFromPort();
			IRPPort theToPort = theLink.getToPort();

			if( theFromPort != null && theToPort != null ){

				IRPModelElement fromPortOwner = theFromPort.getOwner();
				IRPModelElement toPortOwner = theToPort.getOwner();

				if( theTargetClassifiers.contains( fromPortOwner ) && 
						theSourceClassifiers.contains( toPortOwner ) ){

					super.debug( "Found " + super.elInfo(theConnector) + " owned by " + super.elInfo( inTheBuildingBlock ) + 
							"that goes from " + super.elInfo(theFromPort) + " to " + super.elInfo(theToPort));

					thePort = theToPort;

				} else if( theTargetClassifiers.contains( toPortOwner ) && 
						theSourceClassifiers.contains( fromPortOwner ) ){

					super.debug( "Found " + super.elInfo(theConnector) + " owned by " + super.elInfo( inTheBuildingBlock ) + 
							"that goes from " + super.elInfo(theFromPort) + " to " + super.elInfo(theToPort));

					thePort = theFromPort;
				}
			}
		}

		super.debug("getPortThatConnects is returning " + super.elInfo(thePort));

		return thePort;
	}

	public String toCamelCase(
			String theInput,
			int max,
			boolean areSpacesAllowed ){
		
		StringBuilder nameBuilder = new StringBuilder(theInput.length());    

		boolean capitalizeNextChar = true;

		int n = 1;

		for (char c:theInput.toCharArray()) {
			if (Character.isJavaIdentifierPart(c)){
				if (capitalizeNextChar) {
					nameBuilder.append(Character.toUpperCase(c));
				} else {
					if (n==1 && !areSpacesAllowed){
						nameBuilder.append(Character.toUpperCase(c));
					} else {
						nameBuilder.append(c);
					}
				}
				capitalizeNextChar = false;
			} else if (Character.isSpaceChar(c)){

				if( areSpacesAllowed ){
					nameBuilder.append(c);
				}
				
				if (n<max){
					capitalizeNextChar = true;
					continue;
				} else {
					break;
				}
			}
			n++;
		}

		return nameBuilder.toString();
	}
	
	public String determineBestCheckOperationNameFor(
			IRPClassifier onTargetBlock,
			String theAttributeName,
			int max ){

		String theProposedName = determineUniqueNameBasedOn( 
				toMethodName( "check" + capitalize( theAttributeName ), max ), 
				"Attribute", 
				onTargetBlock );

		return theProposedName;
	}

	public IRPSysMLPort getExistingFlowPort( 
			IRPAttribute forTheAttribute ){

		IRPSysMLPort theExistingFlowPort = null;

		Set<IRPModelElement> theEls = 
				super.getElementsThatHaveStereotypedDependenciesFrom(
						forTheAttribute, "AutoRipple" );

		IRPModelElement theAttributeOwner = forTheAttribute.getOwner();

		for( IRPModelElement theEl : theEls ) {

			if( theEl instanceof IRPSysMLPort ){

				IRPModelElement theElementsOwner = theEl.getOwner();

				if( theElementsOwner.equals( theAttributeOwner )){
					theExistingFlowPort = (IRPSysMLPort)theEl;
					//super.debug( super.elInfo( theExistingFlowPort ) + " was found based on �AutoRipple� dependency" );					
				} else {
					//super.debug( "Warning, in getExistingFlowPort() for " + 
					//		super.elInfo( forTheAttribute ) + ":" + super.elInfo( theEl ) + 
					//		" was found based on �AutoRipple� dependency" );	

					//super.debug("However, it is incorrectly owned by " + super.elInfo( theElementsOwner ) + 
					//		" hence relation needs to be deleted");
				}
			}
		}

		// still not found?
		if( theExistingFlowPort == null ){

			theExistingFlowPort = (IRPSysMLPort) forTheAttribute.getOwner().findNestedElement(
					forTheAttribute.getName(), "FlowPort" );

			if( theExistingFlowPort != null ){
				//super.debug( super.elInfo( theExistingFlowPort ) + " was found based on name matching" );
			} else {
				//super.debug( "Unable to find an existing flow port related to " + super.elInfo( forTheAttribute ) );
			}
		}

		return theExistingFlowPort;
	}

	public IRPOperation getExistingCheckOp( 
			IRPAttribute forTheAttribute ){

		IRPOperation theExistingCheckOp = null;

		Set<IRPModelElement> theEls = 
				super.getElementsThatHaveStereotypedDependenciesFrom(
						forTheAttribute, "AutoRipple" );

		IRPModelElement theAttributeOwner = forTheAttribute.getOwner();

		for( IRPModelElement theEl : theEls ) {

			if( theEl instanceof IRPOperation && 
					theEl.getName().contains( "check" ) ){

				IRPModelElement theElementsOwner = theEl.getOwner();

				if( theElementsOwner.equals( theAttributeOwner )){
					theExistingCheckOp = (IRPOperation)theEl;
					//super.debug( super.elInfo( theExistingCheckOp ) + " was found based on �AutoRipple� dependency" );					
				} else {
					//super.debug( "Warning, in getExistingCheckOp() for " + 
					//		super.elInfo( forTheAttribute ) + ":" + super.elInfo( theEl ) + 
					//		" was found based on �AutoRipple� dependency" );	

					//super.debug( "However, it is incorrectly owned by " + super.elInfo( theElementsOwner ) + 
					//		" hence relation needs to be deleted");
				}
			}
		}

		// still not found?
		if( theExistingCheckOp == null ){

			String theExpectedName = determineBestCheckOperationNameFor(
					(IRPClassifier)theAttributeOwner, 
					forTheAttribute.getName(), 
					40 );

			theExistingCheckOp = 
					(IRPOperation) forTheAttribute.getOwner().findNestedElement(
							theExpectedName, "Operation" );

			if( theExistingCheckOp != null ){
				//super.debug( super.elInfo( theExistingCheckOp ) + " was found based on name matching" );
			} else {
				//super.debug( "Unable to find an existing check operation called " + theExpectedName );
			}
		}

		return theExistingCheckOp;
	}

	public IRPDependency addAutoRippleDependencyIfOneDoesntExist(
			IRPModelElement fromElement, 
			IRPModelElement toElement ){

		IRPStereotype theAutoRippleStereotype = getStereotypeForAutoRipple();

		IRPDependency theDependency =
				super.addStereotypedDependencyIfOneDoesntExist(
						fromElement, 
						toElement, 
						theAutoRippleStereotype );

		return theDependency;
	}

	@Override
	public void moveRequirementIfNeeded(
			IRPRequirement theReqt) {

		// only do move if property is set
		if( getIsEnableAutoMoveOfRequirements() ){
			RequirementMover theElementMover = new RequirementMover( 
					theReqt, 
					REQTS_ANALYSIS_REQUIREMENT_PACKAGE, 
					this );

			theElementMover.performMove( theReqt );					
		}
	}

	@Override
	public void autoPopulateProjectPackageDiagramIfNeeded() {

		if( getIsAutoPopulatePackageDiagram( _rhpPrj ) ){
			AutoPackageDiagram theAPD = new AutoPackageDiagram( this );
			theAPD.drawDiagram();
		}
	}

	public SelectedElementContext get_selectedContext(){

		if( _selectionContext == null ){
			_selectionContext = new SelectedElementContext( this );
		}

		return _selectionContext;
	}

	@SuppressWarnings("unchecked")
	public IRPLink getExistingLinkBetweenBaseClassifiersOf(
			IRPClassifier theClassifier, 
			IRPClassifier andTheClassifier,
			IRPClassifier underTheAssemblyBlock ){

		int isLinkFoundCount = 0;
		IRPLink theExistingLink = null;

		List<IRPClassifier> theOtherEndsBases = new ArrayList<>();
		theOtherEndsBases.add( andTheClassifier );
		theOtherEndsBases.addAll( andTheClassifier.getBaseClassifiers().toList() );

		List<IRPClassifier> theSourcesBases = new ArrayList<>();
		theSourcesBases.add( theClassifier );
		theSourcesBases.addAll( theClassifier.getBaseClassifiers().toList() );

		//super.debug( "getExistingLinkBetweenBaseClassifiersOf is looking under " + super.elInfo( underTheAssemblyBlock ) );

		List<IRPLink> theLinks = underTheAssemblyBlock.getLinks().toList();

		for( IRPLink theLink : theLinks ){

			IRPModelElement fromEl = theLink.getFromElement();
			IRPModelElement toEl = theLink.getToElement();

			if( fromEl instanceof IRPInstance && 
					toEl instanceof IRPInstance ){

				IRPClassifier fromClassifier = ((IRPInstance)fromEl).getOtherClass();
				IRPClassifier toClassifier = ((IRPInstance)toEl).getOtherClass();

				if( ( theOtherEndsBases.contains( toClassifier ) &&
						theSourcesBases.contains( fromClassifier ) ) ||

						( theSourcesBases.contains( toClassifier ) &&
								theOtherEndsBases.contains( fromClassifier ) ) ){

					//super.debug( "Found that " + super.elInfo( fromClassifier ) 
					//		+ " is already linked to " + super.elInfo( toClassifier ) );

					theExistingLink = theLink;
					isLinkFoundCount++;
				}						
			}

		}

		if( isLinkFoundCount > 1 ){
			super.warning( "Warning in getExistingLinkBetweenBaseClassifiersOf, there are " + isLinkFoundCount );
		}

		return theExistingLink;
	}

	public IRPInstance getElapsedTimeActorPartFor(
			IRPClass theAssemblyBlock ){

		IRPInstance theElapsedTimePart = null;

		@SuppressWarnings("unchecked")
		List<IRPInstance> theInstances = 
		theAssemblyBlock.getNestedElementsByMetaClass(
				"Instance", 0 ).toList();

		for( IRPInstance theInstance : theInstances ){

			IRPClassifier theClassifier = theInstance.getOtherClass();

			if( theClassifier instanceof IRPActor &&
					hasStereotypeCalled( ELAPSED_TIME_GENERATOR_STEREOTYPE, theClassifier ) ){

				theElapsedTimePart = theInstance;
				break;
			}
		}

		return theElapsedTimePart;
	}
	
	public boolean isOwnedUnderPackageHierarchy( 
			String withPackageStereotypeName, 
			IRPModelElement theElement ){
		
		boolean isOwnedUnderHierarchy = false;
		
		IRPModelElement theOwner = theElement.getOwner();
		
		if( theOwner != null ){
			
			IRPPackage theOwningPkg = getOwningPackageFor( theOwner );
			
			if( theOwningPkg != null ){
				
				if( hasStereotypeCalled(
						withPackageStereotypeName, 
						theOwningPkg ) ){
					isOwnedUnderHierarchy = true;
				} else {
					isOwnedUnderHierarchy = isOwnedUnderPackageHierarchy(
							withPackageStereotypeName,  
							theOwningPkg );
				}
			}
		}
		
		return isOwnedUnderHierarchy;
	}
	
	public void bleedColorToElementsRelatedTo(
			List<IRPRequirement> theSelectedReqts ){

		IRPGraphElement theSelectedGraphEl = get_selectedContext().getSelectedGraphEl();

		// only bleed on activity diagrams		
		if( theSelectedGraphEl != null &&
				theSelectedGraphEl.getDiagram() instanceof IRPActivityDiagram ){

			for( IRPGraphElement theGraphEl : get_selectedContext().get_selectedGraphEls() ) {
				
				bleedColorToElementsRelatedTo( theGraphEl, theSelectedReqts );
			}
		}
	}
	
	private void bleedColorToElementsRelatedTo(
			IRPGraphElement theGraphEl,
			List<IRPRequirement> theSelectedReqts ){

		String theForegroundColor = getBleedForegroundColor();
		IRPDiagram theDiagram = theGraphEl.getDiagram();
		IRPModelElement theEl = theGraphEl.getModelObject();

		if( theEl != null ){

			//_context.debug("Setting color to red for " + theEl.getName());
			theGraphEl.setGraphicalProperty( "ForegroundColor", theForegroundColor );

			@SuppressWarnings("unchecked")
			List<IRPDependency> theExistingDeps = theEl.getDependencies().toList();

			for (IRPDependency theDependency : theExistingDeps) {

				IRPModelElement theDependsOn = theDependency.getDependsOn();

				if (theDependsOn != null && 
						theDependsOn instanceof IRPRequirement && 
						theSelectedReqts.contains( theDependsOn )){	

					bleedColorToGraphElsRelatedTo( theDependsOn, theForegroundColor, theDiagram );
					bleedColorToGraphElsRelatedTo( theDependency, theForegroundColor, theDiagram );
				}
			}
		}
	}

	public void bleedColorToGraphElsRelatedTo(
			IRPModelElement theEl, 
			String theForegroundColor, 
			IRPDiagram onDiagram ){

		@SuppressWarnings("unchecked")
		List<IRPGraphElement> theGraphElsRelatedToElement = 
		onDiagram.getCorrespondingGraphicElements( theEl ).toList();

		for (IRPGraphElement theGraphEl : theGraphElsRelatedToElement) {
			theGraphEl.setGraphicalProperty( "ForegroundColor", theForegroundColor );
		}
	}
	
	public String replaceCSVIncompatibleCharsFrom(
			String theString ) {
		
		String theResult = theString.replaceAll( "\\r", "<CR>" );
		theResult = theResult.replaceAll( "\\n", "<LF>" );
		
		if( !theString.equals( theResult ) ) {
		
			warning( "Removed CRLF characters from " + theResult );
		}
		
		return theResult;
	}
}

/**
 * Copyright (C) 2021-2023  MBSE Training and Consulting Limited (www.executablembse.com)

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