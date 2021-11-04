package com.mbsetraining.sysmlhelper.executablembse;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import com.mbsetraining.sysmlhelper.common.ConfigurationSettings;
import com.mbsetraining.sysmlhelper.common.RequirementMover;
import com.mbsetraining.sysmlhelper.common.UserInterfaceHelper;
import com.telelogic.rhapsody.core.IRPActor;
import com.telelogic.rhapsody.core.IRPAttribute;
import com.telelogic.rhapsody.core.IRPClass;
import com.telelogic.rhapsody.core.IRPClassifier;
import com.telelogic.rhapsody.core.IRPDependency;
import com.telelogic.rhapsody.core.IRPEvent;
import com.telelogic.rhapsody.core.IRPEventReception;
import com.telelogic.rhapsody.core.IRPLink;
import com.telelogic.rhapsody.core.IRPModelElement;
import com.telelogic.rhapsody.core.IRPOperation;
import com.telelogic.rhapsody.core.IRPPackage;
import com.telelogic.rhapsody.core.IRPPort;
import com.telelogic.rhapsody.core.IRPProject;
import com.telelogic.rhapsody.core.IRPRequirement;
import com.telelogic.rhapsody.core.IRPState;
import com.telelogic.rhapsody.core.IRPStereotype;
import com.telelogic.rhapsody.core.IRPSysMLPort;
import com.telelogic.rhapsody.core.IRPTransition;
import com.telelogic.rhapsody.core.IRPUnit;

public class ExecutableMBSE_Context extends ConfigurationSettings {

	public final String FLOW_CONNECTOR = "Flow Connector";
	public final String SUBSYSTEM_USAGE = "Subsystem Usage";
	public final String SYSTEM_USAGE = "System Usage";
	public final String FUNCTION_USAGE = "Function Usage";
	public final String FLOW_OUTPUT = "Flow Output";
	public final String FLOW_INPUT = "Flow Input";
	public final String OBJECT = "Object";
	public final String SYSTEM_BLOCK = "System Block";

	public final String REQTS_ANALYSIS_CONTEXT_DIAGRAM_PACKAGE = "10 Reqts Analysis - Context Diagram Package";
	public final String REQTS_ANALYSIS_ACTOR_PACKAGE = "11 Reqts Analysis - Actor Package";
	public final String REQTS_ANALYSIS_USE_CASE_PACKAGE = "12 Reqts Analysis - Use Case Package";
	public final String REQTS_ANALYSIS_WORKING_COPY_PACKAGE = "12 Reqts Analysis - Working Copy Package";
	public final String REQTS_ANALYSIS_REQUIREMENT_PACKAGE = "13 Reqts Analysis - Requirement Package";
	public final String REQTS_ANALYSIS_EXTERNAL_SIGNALS_PACKAGE = "14 Reqts Analysis - External Signals Package";
	public final String FUNCT_ANALYSIS_SCENARIOS_PACKAGE = "21 Funct Analysis - Scenarios Package";
	public final String FUNCT_ANALYSIS_DESIGN_PACKAGE = "22 Funct Analysis - Design Package";
	public final String FUNCT_ANALYSIS_INTERFACES_PACKAGE = "23 Funct Analysis - Interfaces Package";
	public final String FUNCT_ANALYSIS_TEST_PACKAGE = "24 Funct Analysis - Test Package";
	public final String DESIGN_SYNTHESIS_SUBSYSTEM_INTERFACES_PACKAGE = "33 Design Synthesis - Subsystem Interfaces Package";
	public final String TIME_ELAPSED_BLOCK_STEREOTYPE = "ElapsedTimeBlock";
	public final String ELAPSED_TIME_GENERATOR_STEREOTYPE = "ElapsedTimeGenerator";
	public final String NEW_TERM_FOR_USE_CASE_DIAGRAM = "EnhancedUseCaseDiagram";
	public final String NEW_TERM_FOR_SYSTEM_CONTEXT_DIAGRAM = "SystemContextDiagram";
	public final String NEW_TERM_FOR_ACTOR_USAGE = "ActorUsage";
	public final String NEW_TERM_FOR_SYSTEM_CONTEXT = "SystemUsage";
	public final String TESTBENCH_STEREOTYPE = "Testbench";

	protected SelectedElementContext _selectionContext;

	protected String _defaultExternalSignalsPackageName;
	protected String _defaultContextDiagramPackageName;
	protected String _defaultActorPackageName;
	protected String _defaultRequirementsPackageName;

	protected Boolean _isEnableAutoMoveOfEventsOnAddNewElement;
	protected Boolean _isEnableAutoMoveOfEventsOnFlowCreation;
	protected Boolean _isEnableAutoMoveOfEventsOnFlowConnectorCreation;
	protected Boolean _isEnableAutoMoveOfRequirements;
	
	protected List<String> _storeUnitInSeparateDirectoryNewTerms;
	protected List<String> _dontCreateSeparateUnitNewTerms;
	protected List<IRPModelElement> _stereotypesForBlockPartCreation;

	public ExecutableMBSE_Context(
			String theAppID ){

		super( theAppID, 
				"ExecutableMBSEProfile.General.EnableErrorLogging", 
				"ExecutableMBSEProfile.General.EnableWarningLogging",
				"ExecutableMBSEProfile.General.EnableInfoLogging", 
				"ExecutableMBSEProfile.General.EnableDebugLogging",
				"ExecutableMBSEProfile.General.PluginVersion",
				"ExecutableMBSEProfile.General.UserDefinedMetaClassesAsSeparateUnit",
				"ExecutableMBSEProfile.General.AllowPluginToControlUnitGranularity",
				"ExecutableMBSE.properties", 
				"ExecutableMBSE_MessagesBundle",
				"ExecutableMBSE" 
				);

		_selectionContext = new SelectedElementContext( this );
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
	
	// Single call per session, but use lazy load
	public List<IRPModelElement> getStereotypesForBlockPartCreation(){

		if( _stereotypesForBlockPartCreation == null ){			
			_stereotypesForBlockPartCreation = getStereotypesBasedOnProperty(
					_rhpPrj, 
					"ExecutableMBSEProfile.FunctionalAnalysis.StereotypesForBlockCreation" );		
		}
		
		return _stereotypesForBlockPartCreation;
	}

	public boolean getIsShowProfileVersionCheckDialogs(){

		boolean result = getBooleanPropertyValue(
				_rhpPrj,
				"ExecutableMBSEProfile.General.IsShowProfileVersionCheckDialogs" );

		return result;
	}

	public IRPStereotype getStereotypeForTestbench(){

		IRPStereotype theStereotype = getExistingStereotype( 
				TESTBENCH_STEREOTYPE, 
				_rhpPrj );

		if( theStereotype == null ){
			super.error( "Error in getStereotypeForTestbench, no Stereotyped called " + 
					TESTBENCH_STEREOTYPE + " was found" );
		}	

		return theStereotype;
	}

	public IRPStereotype getStereotypeForTimeElapsedActor(){

		IRPStereotype theStereotype = getExistingStereotype( 
				ELAPSED_TIME_GENERATOR_STEREOTYPE, 
				_rhpPrj );

		if( theStereotype == null ){
			super.error( "Error in getStereotypeForTimeElapsedActor, no Stereotyped called " + 
					ELAPSED_TIME_GENERATOR_STEREOTYPE + " was found" );
		}	

		return theStereotype;
	}

	public IRPStereotype getStereotypeForTimeElapsedBlock(){

		IRPStereotype theStereotype = getExistingStereotype( 
				TIME_ELAPSED_BLOCK_STEREOTYPE, 
				_rhpPrj );

		if( theStereotype == null ){
			super.error( "Error in getStereotypeForTimeElapsedBlock, no Stereotyped called " + 
					TIME_ELAPSED_BLOCK_STEREOTYPE + " was found" );
		}	

		return theStereotype;
	}

	public IRPStereotype getNewTermForUseCaseDiagram(){

		IRPStereotype theStereotype = getExistingStereotype( 
				NEW_TERM_FOR_USE_CASE_DIAGRAM, 
				_rhpPrj );

		if( theStereotype == null ){
			super.error( "Error in getNewTermForUseCaseDiagram, no Stereotyped called " + 
					NEW_TERM_FOR_USE_CASE_DIAGRAM + " was found" );
		}	

		return theStereotype;
	}

	public IRPStereotype getNewTermForSystemContextDiagram(){

		IRPStereotype theStereotype = getExistingStereotype( 
				NEW_TERM_FOR_SYSTEM_CONTEXT_DIAGRAM, 
				_rhpPrj );

		if( theStereotype == null ){
			
			super.error( "Error in getNewTermForSystemContextDiagram, no Stereotyped called " + 
					NEW_TERM_FOR_SYSTEM_CONTEXT_DIAGRAM + " was found" );
		}	

		return theStereotype;
	}

	public IRPStereotype getNewTermForActorUsage(){

		IRPStereotype theStereotype = getExistingStereotype( 
				NEW_TERM_FOR_ACTOR_USAGE, 
				_rhpPrj );

		if( theStereotype == null ){
			
			super.error( "Error in getNewTermForActorUsage, no Stereotyped called " + 
					NEW_TERM_FOR_ACTOR_USAGE + " was found" );
		}	

		return theStereotype;
	}

	public IRPStereotype getNewTermForSystemContext(){

		IRPStereotype theStereotype = getExistingStereotype( 
				NEW_TERM_FOR_SYSTEM_CONTEXT, 
				_rhpPrj );

		if( theStereotype == null ){			
			super.error( "Error in getNewTermForSystemContext, no Stereotyped called " + 
					NEW_TERM_FOR_SYSTEM_CONTEXT + " was found" );
		}	

		return theStereotype;
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

	public boolean getCVSExportIncludeArtifactName(
			IRPModelElement basedOnContext ){

		boolean result = getBooleanPropertyValue(
				basedOnContext,
				"ExecutableMBSEProfile.RequirementsAnalysis.CVSExportIncludeArtifactName" );

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

					IRPModelElement theStereotypeElement = 
							findElementWithMetaClassAndName( 
									"Stereotype", theString.trim(), forContextEl );

					if( theStereotypeElement != null ){
						theStereotypes.add( theStereotypeElement );
					}
				}

				super.debug( "getStereotypesForBlockPartCreation was invoked and found " + theStereotypeList + 
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

	public boolean getIsSendEventViaPanelOptionEnabled(
			IRPModelElement forContextEl ){

		boolean result = getBooleanPropertyValue(
				forContextEl,
				"General.Graphics.IsSendEventViaPanelOptionEnabled" );

		return result;
	}

	public boolean getIsSendEventViaPanelWantedByDefault(
			IRPModelElement forContextEl ){

		boolean result = getBooleanPropertyValue(
				forContextEl,
				"General.Graphics.IsSendEventViaPanelWantedByDefault" );

		return result;
	}

	public boolean getIsPopulateWantedByDefault(
			IRPModelElement forContextEl ){

		boolean result = getBooleanPropertyValue(
				forContextEl, 
				"General.Graphics.IsPopulateWantedByDefault" );

		return result;
	}

	public boolean getIsPopulateOptionHidden(
			IRPModelElement forContextEl ){

		boolean result = getBooleanPropertyValue(
				forContextEl,
				"General.Graphics.IsPopulateOptionHidden" );

		return result;
	}

	public boolean getIsCreateParametricSubpackageSelected(
			IRPModelElement forContextEl ){

		boolean result = getBooleanPropertyValue(
				forContextEl,
				"ExecutableMBSEProfile.General.IsCreateParametricSubpackageSelected" );

		return result;
	}

	public boolean getIsCallOperationSupportEnabled(
			IRPModelElement forContextEl ){

		boolean result = getBooleanPropertyValue(
				forContextEl,
				"General.Graphics.IsCallOperationSupportEnabled" );

		return result;
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

	public String getAutoGenerationOfProxyPortsForLinksPolicy(
			IRPModelElement forContextEl ){

		String result = getStringPropertyValueFromRhp(
				forContextEl,
				"ExecutableMBSEProfile.DesignSynthesis.AutoGenerationOfProxyPortsForLinksPolicy",
				"Never" );

		return result;
	}

	public String getAutoGenerationOfFlowPortsForLinksPolicy(
			IRPModelElement forContextEl ){

		String result = getStringPropertyValueFromRhp(
				forContextEl,
				"ExecutableMBSEProfile.FunctionalAnalysis.AutoGenerationOfFlowPortsForLinksPolicy",
				"Never" );

		return result;
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
					super.debug( super.elInfo( theExistingFlowPort ) + " was found based on «AutoRipple» dependency" );					
				} else {
					super.warning( "Warning, in getExistingFlowPort() for " + 
							super.elInfo( forTheAttribute ) + ":" + super.elInfo( theEl ) + 
							"was found based on «AutoRipple» dependency" );	

					super.warning("However, it is incorrectly owned by " + super.elInfo( theElementsOwner ) + 
							" hence relation needs to be deleted");
				}
			}
		}

		// still not found?
		if( theExistingFlowPort == null ){

			theExistingFlowPort = (IRPSysMLPort) forTheAttribute.getOwner().findNestedElement(
					forTheAttribute.getName(), "FlowPort" );

			if( theExistingFlowPort != null ){
				super.debug( super.elInfo( theExistingFlowPort ) + " was found based on name matching" );
			} else {
				super.warning( "Unable to find an existing flow port related to " + super.elInfo( forTheAttribute ) );
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
					super.debug( super.elInfo( theExistingCheckOp ) + " was found based on «AutoRipple» dependency" );					
				} else {
					super.warning( "Warning, in getExistingCheckOp() for " + 
							super.elInfo( forTheAttribute ) + ":" + super.elInfo( theEl ) + 
							" was found based on «AutoRipple» dependency" );	

					super.warning("However, it is incorrectly owned by " + super.elInfo( theElementsOwner ) + 
							" hence relation needs to be deleted");
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
				super.debug( super.elInfo( theExistingCheckOp ) + " was found based on name matching" );
			} else {
				super.warning( "Unable to find an existing check operation called " + theExpectedName );
			}
		}

		return theExistingCheckOp;
	}

	public IRPDependency addAutoRippleDependencyIfOneDoesntExist(
			IRPModelElement fromElement, 
			IRPModelElement toElement ){

		IRPStereotype theAutoRippleStereotype = 
				super.getExistingStereotype( 
						"AutoRipple", fromElement.getProject() );

		IRPDependency theDependency =
				super.addStereotypedDependencyIfOneDoesntExist(
						fromElement, 
						toElement, 
						theAutoRippleStereotype );

		return theDependency;
	}

	public IRPOperation createTestCaseFor( IRPClass theTestDriver ){

		IRPOperation theOp = null;

		if (super.hasStereotypeCalled("TestDriver", theTestDriver)){

			super.debug("createTestCaseFor was invoked for " + super.elInfo(theTestDriver));

			String[] theSplitName = theTestDriver.getName().split("_");

			String thePrefix = theSplitName[0] + "_Test_";

			super.debug("The prefix for TestCase was calculated as '" + thePrefix + "'");

			int count = 0;
			boolean isUniqueNumber = false;
			String nameToTry = null;

			while (isUniqueNumber==false){
				count++;
				nameToTry = thePrefix + String.format("%03d", count);

				if (theTestDriver.findNestedElement(nameToTry, "Operation") == null){
					isUniqueNumber = true;
				}
			}

			if (isUniqueNumber){
				theOp = theTestDriver.addOperation(nameToTry);
				theOp.highLightElement();
				theOp.changeTo("Test Case");

				IRPState theState = getStateCalled("Ready", theTestDriver.getStatechart(), theTestDriver);

				String theEventName = "ev" + nameToTry;

				IRPEventReception theEventReception = theTestDriver.addReception( theEventName );

				if (theEventReception != null){
					IRPEvent theEvent = theEventReception.getEvent();

					super.debug("The state called " + theState.getFullPathName() + " is owned by " + theState.getOwner().getFullPathName());
					IRPTransition theTransition = theState.addInternalTransition( theEvent );
					theTransition.setItsAction( theOp.getName() + "();");
				}
			}		

		} else {
			UserInterfaceHelper.showWarningDialog(
					"This operation only works if you right-click a «TestDriver» block");	    
		}

		return theOp;
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
}

/**
 * Copyright (C) 2021  MBSE Training and Consulting Limited (www.executablembse.com)

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