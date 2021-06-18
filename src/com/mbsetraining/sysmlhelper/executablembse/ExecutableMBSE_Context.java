package com.mbsetraining.sysmlhelper.executablembse;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JTextField;

import com.mbsetraining.sysmlhelper.common.ConfigurationSettings;
import com.mbsetraining.sysmlhelper.common.UserInterfaceHelper;
import com.telelogic.rhapsody.core.IRPAcceptEventAction;
import com.telelogic.rhapsody.core.IRPAcceptTimeEvent;
import com.telelogic.rhapsody.core.IRPActor;
import com.telelogic.rhapsody.core.IRPAttribute;
import com.telelogic.rhapsody.core.IRPClass;
import com.telelogic.rhapsody.core.IRPClassifier;
import com.telelogic.rhapsody.core.IRPComment;
import com.telelogic.rhapsody.core.IRPConstraint;
import com.telelogic.rhapsody.core.IRPDependency;
import com.telelogic.rhapsody.core.IRPEvent;
import com.telelogic.rhapsody.core.IRPEventReception;
import com.telelogic.rhapsody.core.IRPGraphElement;
import com.telelogic.rhapsody.core.IRPGuard;
import com.telelogic.rhapsody.core.IRPInstance;
import com.telelogic.rhapsody.core.IRPLink;
import com.telelogic.rhapsody.core.IRPModelElement;
import com.telelogic.rhapsody.core.IRPOperation;
import com.telelogic.rhapsody.core.IRPPackage;
import com.telelogic.rhapsody.core.IRPPort;
import com.telelogic.rhapsody.core.IRPProfile;
import com.telelogic.rhapsody.core.IRPProject;
import com.telelogic.rhapsody.core.IRPRequirement;
import com.telelogic.rhapsody.core.IRPSendAction;
import com.telelogic.rhapsody.core.IRPState;
import com.telelogic.rhapsody.core.IRPStereotype;
import com.telelogic.rhapsody.core.IRPSysMLPort;
import com.telelogic.rhapsody.core.IRPTransition;
import com.telelogic.rhapsody.core.IRPUnit;

public class ExecutableMBSE_Context extends ConfigurationSettings {

	final protected String _defaultExternalSignalsPackageName;
	final protected String _defaultContextDiagramPackageName;
	final protected String _defaultActorPackageName;
	final protected String _defaultRequirementsPackageName;
	final protected String _externalSignalsPackageStereotype;
	final protected String _contextDiagramPackageStereotype;
	final protected String _requirementPackageStereotype;
	final protected boolean _isEnableAutoMoveOfEventsOnAddNewElement;
	final protected boolean _isEnableAutoMoveOfEventsOnFlowCreation;

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

		_defaultExternalSignalsPackageName = _rhpPrj.getPropertyValue(
				"ExecutableMBSEProfile.RequirementsAnalysis.DefaultExternalSignalsPackageName" );

		_defaultContextDiagramPackageName = _rhpPrj.getPropertyValue(
				"ExecutableMBSEProfile.RequirementsAnalysis.DefaultContextDiagramPackageName" );

		_defaultActorPackageName = _rhpPrj.getPropertyValue(
				"ExecutableMBSEProfile.RequirementsAnalysis.DefaultActorPackageName" );

		_defaultRequirementsPackageName = _rhpPrj.getPropertyValue(
				"ExecutableMBSEProfile.RequirementsAnalysis.DefaultRequirementsPackageName" );

		_externalSignalsPackageStereotype = _rhpPrj.getPropertyValue(
				"ExecutableMBSEProfile.General.ExternalSignalsPackageStereotype" );

		_isEnableAutoMoveOfEventsOnFlowCreation = getBooleanPropertyValue(
				_rhpPrj,
				"ExecutableMBSEProfile.RequirementsAnalysis.IsEnableAutoMoveOfEventsOnFlowCreation" );

		_isEnableAutoMoveOfEventsOnAddNewElement  = getBooleanPropertyValue(
				_rhpPrj,
				"ExecutableMBSEProfile.RequirementsAnalysis.IsEnableAutoMoveOfEventsOnAddNewElement" );

		_contextDiagramPackageStereotype = _rhpPrj.getPropertyValue(
				"ExecutableMBSEProfile.General.ContextDiagramPackageStereotype" );

		_requirementPackageStereotype = _rhpPrj.getPropertyValue(
				"ExecutableMBSEProfile.General.RequirementPackageStereotype" );
	}

	public boolean getIsShowProfileVersionCheckDialogs(){

		boolean result = getBooleanPropertyValue(
				_rhpPrj,
				"ExecutableMBSEProfile.General.IsShowProfileVersionCheckDialogs" );

		return result;
	}

	public IRPStereotype getStereotypeForTestbench(
			IRPModelElement basedOnContextEl ){

		String theTestbenchSterotype = getTestbenchStereotype( basedOnContextEl );

		IRPStereotype theStereotype = getExistingStereotype( 
				theTestbenchSterotype, 
				basedOnContextEl.getProject() );

		if( theStereotype == null ){
			super.error( "Error in getStereotypeForTestbench, no Stereotyped called " + 
					theTestbenchSterotype + " was found" );
		}	

		return theStereotype;
	}

	public IRPStereotype getStereotypeForTimeElapsedActor(
			IRPModelElement basedOnContextEl ){

		String theElapsedTimeActorStereotype = getElapsedTimeActorStereotype( basedOnContextEl );

		IRPStereotype theStereotype = getExistingStereotype( 
				theElapsedTimeActorStereotype, 
				basedOnContextEl.getProject() );

		if( theStereotype == null ){
			super.error( "Error in getStereotypeForTimeElapsedActor, no Stereotyped called " + 
					theElapsedTimeActorStereotype + " was found" );
		}	

		return theStereotype;
	}

	public IRPStereotype getStereotypeForTimeElapsedBlock(
			IRPModelElement basedOnContextEl ){

		String theElapsedTimeBlockStereotype = getElapsedTimeBlockStereotype( basedOnContextEl );

		IRPStereotype theStereotype = getExistingStereotype( 
				theElapsedTimeBlockStereotype, 
				basedOnContextEl.getProject() );

		if( theStereotype == null ){
			super.error( "Error in getStereotypeForTimeElapsedBlock, no Stereotyped called " + 
					theElapsedTimeBlockStereotype + " was found" );
		}	

		return theStereotype;
	}

	public IRPStereotype getStereotypeToUseForFunctions(
			IRPModelElement basedOnContextEl ){

		IRPStereotype theStereotype = getStereotypeBasedOn(
				basedOnContextEl, 
				"ExecutableMBSEProfile.FunctionalAnalysis.TraceabilityTypeToUseForFunctions" );

		return theStereotype;
	}

	public IRPStereotype getStereotypeToUseForActions(
			IRPModelElement basedOnContext ){

		IRPStereotype theStereotype = getStereotypeBasedOn(
				basedOnContext, 
				"ExecutableMBSEProfile.RequirementsAnalysis.TraceabilityTypeToUseForActions" );

		return theStereotype;
	}

	public IRPStereotype getStereotypeToUseForUseCases(
			IRPModelElement basedOnContext ){

		IRPStereotype theStereotype = getStereotypeBasedOn(
				basedOnContext, 
				"ExecutableMBSEProfile.RequirementsAnalysis.TraceabilityTypeToUseForUseCases" );

		return theStereotype;
	}

	public IRPStereotype getNewTermForUseCaseDiagram(){
		
		IRPStereotype newTermForUseCaseDiagram = getStereotypeBasedOn(
				_rhpPrj, 
				"ExecutableMBSEProfile.RequirementsAnalysis.NewTermForUseCaseDiagram" );
		
		return newTermForUseCaseDiagram;
	}

	public IRPStereotype getNewTermForSystemContextDiagram(){
		
		IRPStereotype newTermForSystemContextDiagram = getStereotypeBasedOn(
				_rhpPrj, 
				"ExecutableMBSEProfile.RequirementsAnalysis.NewTermForSystemContextDiagram" );
		
		return newTermForSystemContextDiagram;
	}

	public IRPStereotype getNewTermForActorUsage(){
		
		IRPStereotype newTermForActorUsage = getStereotypeBasedOn(
				_rhpPrj, 
				"ExecutableMBSEProfile.RequirementsAnalysis.NewTermForActorUsage" );
		
		return newTermForActorUsage;
	}

	public IRPStereotype getNewTermForSystemContext(){
		
		IRPStereotype newTermForSystemContext = getStereotypeBasedOn(
				_rhpPrj, 
				"ExecutableMBSEProfile.RequirementsAnalysis.NewTermForSystemContext" );
		
		return newTermForSystemContext;
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

			super.debug( "Looking for packages with metaclass " + 
					theMasterActorStereotype.getName() + ", found " + theActorPackages.size() );

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

	public String getDefaultActorPackageName(){
		return _defaultActorPackageName;
	}

	public String getDefaultRequirementsPackageName(){
		return _defaultRequirementsPackageName;
	}

	public String getDefaultExternalSignalsPackageName(){
		return _defaultExternalSignalsPackageName;
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

	public String getDefaultContextDiagramPackageName(){

		return _defaultContextDiagramPackageName;
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

	public List<String> getStoreUnitInSeparateDirectoryNewTerms(
			IRPModelElement theContextEl ){

		return getListFromCommaSeparatedString(
				theContextEl, 
				"ExecutableMBSEProfile.General.StoreUnitInSeparateDirectoryNewTerms" );
	}

	public List<String> getDontCreateSeparateUnitNewTerms(
			IRPModelElement theContextEl ){

		return getListFromCommaSeparatedString(
				theContextEl, 
				"ExecutableMBSEProfile.General.DontCreateSeparateUnitNewTerms" );
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

		//		Logger.info("setSavedInSeparateDirectoryIfAppropriateFor was invoked for " + Logger.elementInfo(modelElement) );

		if( modelElement != null && 
				modelElement instanceof IRPPackage ){

			List<String> theNewTerms = 
					getStoreUnitInSeparateDirectoryNewTerms( 
							modelElement );

			if( theNewTerms.contains( modelElement.getUserDefinedMetaClass() ) ){

				IRPPackage thePackage = (IRPPackage) modelElement;
				thePackage.setSavedInSeperateDirectory( 1 );
			}
		}
	}

	public void setDontSaveAsSeparateUnitIfAppropriateFor(
			IRPModelElement modelElement ){

		super.debug( "setDontSaveAsSeparateUnitIfAppropriateFor was invoked for " + super.elInfo( modelElement ) );

		if( modelElement != null ){

			List<String> theNewTerms = getDontCreateSeparateUnitNewTerms( modelElement );

			if( theNewTerms.contains( modelElement.getUserDefinedMetaClass() ) ){
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

	public List<IRPModelElement> getStereotypesForBlockPartCreation(
			IRPModelElement forContextEl ){

		return getStereotypesBasedOnProperty(
				forContextEl, 
				"ExecutableMBSEProfile.FunctionalAnalysis.StereotypesForBlockCreation" );
	}

	public List<IRPModelElement> getStereotypesBasedOnProperty(
			IRPModelElement forContextEl,
			String thePropertyKey ){

		List<IRPModelElement> theStereotypes = new ArrayList<>();

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

		return theStereotypes;
	}

	private IRPStereotype getStereotypeBasedOn(
			IRPModelElement theContextEl,
			String andPropertyValue ){

		IRPStereotype theStereotype = null;

		String theStereotypeName = 
				theContextEl.getPropertyValue( 
						andPropertyValue );

		if( theStereotypeName != null && 
				!theStereotypeName.trim().isEmpty() ){

			theStereotype = getExistingStereotype( 
					theStereotypeName, 
					theContextEl.getProject() );

			if( theStereotype == null ){
				super.error( "Error in getStereotypeBasedOn, no Stereotyped called " + theStereotypeName + " was found" );

				//theStereotype = selectAndPersistStereotype( basedOnContext.getProject(), thePkg, theTag );

			} else {				
				super.debug( "Using " + super.elInfo( theStereotype ) + " for " + andPropertyValue );
			}		
		}

		return theStereotype;
	}

	public String getElapsedTimeBlockStereotype(
			IRPModelElement basedOnContextEl ) {

		String thePropertyValue = 
				basedOnContextEl.getPropertyValue( 
						"ExecutableMBSEProfile.General.ElapsedTimeBlockStereotype" );

		return thePropertyValue;
	}

	public String getElapsedTimeActorStereotype(
			IRPModelElement basedOnContextEl ) {

		String thePropertyValue = 
				basedOnContextEl.getPropertyValue( 
						"ExecutableMBSEProfile.General.ElapsedTimeActorStereotype" );

		return thePropertyValue;
	}

	public String getTestbenchStereotype(
			IRPModelElement basedOnContextEl ) {

		String thePropertyValue = 
				basedOnContextEl.getPropertyValue( 
						"ExecutableMBSEProfile.General.TestbenchStereotype" );

		return thePropertyValue;
	}

	public String getMasterActorPackageStereotype(
			IRPModelElement basedOnContextEl ) {

		return basedOnContextEl.getPropertyValue( 
				"ExecutableMBSEProfile.General.MasterActorPackageStereotype" );

	}

	public String getActorPackageStereotype(
			IRPModelElement basedOnContextEl ) {

		String thePropertyValue = 
				basedOnContextEl.getPropertyValue( 
						"ExecutableMBSEProfile.General.ActorPackageStereotype" );

		return thePropertyValue;
	}

	public String getUseCasePackageStereotype(
			IRPModelElement basedOnContextEl ) {

		String thePropertyValue = 
				basedOnContextEl.getPropertyValue( 
						"ExecutableMBSEProfile.General.UseCasePackageStereotype" );

		return thePropertyValue;
	}

	public String getUseCasePackageWorkingStereotype(
			IRPModelElement basedOnContextEl ) {

		String thePropertyValue = 
				basedOnContextEl.getPropertyValue( 
						"ExecutableMBSEProfile.General.UseCasePackageWorkingStereotype" );

		return thePropertyValue;
	}

	public String getRequirementPackageStereotype() {
		return _requirementPackageStereotype;
	}

	public String getSimulationPackageStereotype(
			IRPModelElement basedOnContextEl ) {

		String thePropertyValue = 
				basedOnContextEl.getPropertyValue( 
						"ExecutableMBSEProfile.General.SimulationPackageStereotype" );

		return thePropertyValue;
	}

	public String getInterfacesPackageStereotype(
			IRPModelElement basedOnContextEl ) {

		String thePropertyValue = 
				basedOnContextEl.getPropertyValue( 
						"ExecutableMBSEProfile.General.InterfacesPackageStereotype" );

		return thePropertyValue;
	}

	public String getExternalSignalsPackageStereotype() {

		return _externalSignalsPackageStereotype;
	}

	public String getDesignPackageStereotype(
			IRPModelElement basedOnContextEl ) {

		String thePropertyValue = 
				basedOnContextEl.getPropertyValue( 
						"ExecutableMBSEProfile.General.DesignPackageStereotype" );

		return thePropertyValue;
	}

	public String getFunctionsPackageStereotype(
			IRPModelElement basedOnContextEl ) {

		String thePropertyValue = 
				basedOnContextEl.getPropertyValue( 
						"ExecutableMBSEProfile.General.FunctionsPackageStereotype" );

		return thePropertyValue;
	}

	public String getContextDiagramPackageStereotype() {
		return _contextDiagramPackageStereotype;
	}

	public String getParametricsPackageStereotype(
			IRPModelElement basedOnContextEl ) {

		String thePropertyValue = 
				basedOnContextEl.getPropertyValue( 
						"ExecutableMBSEProfile.General.ParametricsPackageStereotype" );

		return thePropertyValue;
	}

	public String getTestPackageStereotype(
			IRPModelElement basedOnContextEl ) {

		String thePropertyValue = 
				basedOnContextEl.getPropertyValue( 
						"ExecutableMBSEProfile.General.TestPackageStereotype" );

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

	public String getCreateRequirementTextForPrefixing(
			IRPModelElement basedOnContextEl ){

		String thePropertyValue = 
				basedOnContextEl.getPropertyValue( 
						"ExecutableMBSEProfile.RequirementsAnalysis.CreateRequirementTextForPrefixing" );

		if( thePropertyValue == null ){
			thePropertyValue = "Error in getCreateRequirementTextForPrefixing";
		}

		//#005 10-APR-2016: Support ProductName substitution in reqt text tag (F.J.Chadburn)
		thePropertyValue = thePropertyValue.replaceAll(
				"ProjectName", basedOnContextEl.getProject().getName() );

		return thePropertyValue;
	}

	public boolean getIsEnableAutoMoveOfRequirements(
			IRPModelElement forContextEl ){

		boolean result = getBooleanPropertyValue(
				forContextEl,
				"ExecutableMBSEProfile.RequirementsAnalysis.IsEnableAutoMoveOfRequirements" );

		return result;
	}

	public boolean getIsApplyAutoShowToSequenceDiagramTemplate(
			IRPModelElement forContextEl ){

		boolean result = getBooleanPropertyValue(
				forContextEl,
				"ExecutableMBSEProfile.FunctionalAnalysis.IsApplyAutoShowToSequenceDiagramTemplate" );

		return result;
	}

	public boolean getIsCreateSDWithAutoShowApplied(
			IRPModelElement forContextEl ){

		boolean result = getBooleanPropertyValue(
				forContextEl,
				"ExecutableMBSEProfile.FunctionalAnalysis.IsCreateSDWithAutoShowApplied" );

		return result;
	}

	public boolean getIsCreateSDWithTestDriverLifeline(
			IRPModelElement forContextEl ){

		boolean result = getBooleanPropertyValue(
				forContextEl,
				"ExecutableMBSEProfile.FunctionalAnalysis.IsCreateSDWithTestDriverLifeline" );

		return result;
	}

	public boolean getIsEnableAutoMoveOfInterfaces(
			IRPModelElement forContextEl ){

		boolean result = getBooleanPropertyValue(
				forContextEl,
				"ExecutableMBSEProfile.FunctionalAnalysis.IsEnableAutoMoveOfInterfaces" );

		return result;
	}

	public boolean getIsEnableAutoMoveOfEventsOnAddNewElement(){
		return _isEnableAutoMoveOfEventsOnAddNewElement;
	}

	public boolean getIsEnableAutoMoveOfEventsOnFlowCreation(){
		return _isEnableAutoMoveOfEventsOnFlowCreation;
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
						getUseCasePackageStereotype( basedOnEl ) );

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

	public Set<IRPModelElement> findModelElementsIn(
			List<IRPGraphElement> theGraphElementList, 
			String withMetaClass){

		Set<IRPModelElement> theFilteredSet = new HashSet<IRPModelElement>();

		for (IRPGraphElement theGraphEl : theGraphElementList) {

			IRPModelElement theEl = theGraphEl.getModelObject();

			if (theEl != null && theEl.getMetaClass().equals( withMetaClass )){
				theFilteredSet.add( theEl );
			}
		}

		return theFilteredSet;
	}

	public List<IRPModelElement> findElementsIn(
			List<IRPModelElement> theModelElementList, 
			String withMetaClass){

		List<IRPModelElement> theFilteredList = new ArrayList<IRPModelElement>();

		for (IRPModelElement theEl : theModelElementList) {

			if (theEl.getMetaClass().equals( withMetaClass )){
				theFilteredList.add( theEl );
			}
		}

		return theFilteredList;
	}

	public boolean doUnderlyingModelElementsIn(
			List<IRPGraphElement> theGraphElementList, 
			String haveTheMetaClass){

		boolean result = true;

		for (IRPGraphElement theGraphEl : theGraphElementList) {
			IRPModelElement theEl = theGraphEl.getModelObject();

			if (theEl != null && !theEl.getMetaClass().equals( haveTheMetaClass )){
				result = false;
			}
		}

		return result;
	}

	public Set<IRPModelElement> findModelElementsNestedUnder(
			IRPModelElement rootEl, 
			String ofMetaClass, 
			String withStereotypeMatchingRegEx){

		@SuppressWarnings("unchecked")
		List<IRPModelElement> theCandidateEls = rootEl.getNestedElementsByMetaClass(ofMetaClass, 1).toList();

		Set<IRPModelElement> theFound = new LinkedHashSet<IRPModelElement>();

		for (IRPModelElement theEl : theCandidateEls) {

			IRPStereotype theStereotype = getStereotypeAppliedTo( theEl, withStereotypeMatchingRegEx );

			if( theStereotype != null ){
				// don't add if element is under the profile.
				if (!checkIsNestedUnderAProfile( theEl )){
					theFound.add( theEl );
				}
			}			
		}

		return theFound;
	}

	public List<IRPModelElement> findModelElementsWithoutStereotypeNestedUnder(
			IRPModelElement rootEl, 
			String ofMetaClass, 
			String withStereotypeMatchingRegEx){

		@SuppressWarnings("unchecked")
		List<IRPModelElement> theCandidateEls = rootEl.getNestedElementsByMetaClass(ofMetaClass, 1).toList();
		List<IRPModelElement> theFound = new ArrayList<IRPModelElement>();

		for (IRPModelElement theEl : theCandidateEls) {

			IRPStereotype theStereotype = getStereotypeAppliedTo(theEl, withStereotypeMatchingRegEx);

			if (theStereotype==null){
				theFound.add(theEl);
			}			
		}

		return theFound;
	}

	public void applyStereotypeToDeriveReqtDependenciesOriginatingFrom( 
			IRPModelElement theReqt, 
			IRPStereotype theStereotypeToApply ) {

		@SuppressWarnings("unchecked")
		List<IRPDependency> theDependencies = theReqt.getDependencies().toList();

		for (IRPDependency theDependency : theDependencies) {

			IRPStereotype theExistingGatewayStereotype = 
					getStereotypeAppliedTo( theDependency, "from.*" );

			if (theExistingGatewayStereotype == null && 
					hasStereotypeCalled("deriveReqt", theDependency)){

				super.debug("Applying " + super.elInfo(theStereotypeToApply) + " to " + super.elInfo(theDependency));
				theDependency.setStereotype(theStereotypeToApply);
				theDependency.changeTo("Derive Requirement");
			}
		}
	}

	public boolean checkIsNestedUnderAProfile(
			IRPModelElement theElementToCheck){

		boolean isUnderAProfile = false;

		IRPModelElement theOwner = theElementToCheck.getOwner();

		if (theOwner!=null){

			if (theOwner instanceof IRPProfile){
				isUnderAProfile = true;
			} else {
				isUnderAProfile = checkIsNestedUnderAProfile( theOwner );
			}
		}

		return isUnderAProfile;
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

	public List<IRPLink> getLinksBetween(
			IRPSysMLPort thePort,
			IRPInstance ownedByPart,
			IRPSysMLPort andThePort,
			IRPInstance whichIsOwnedByPart,
			IRPClassifier inBuildingBlock ){

		List<IRPLink> theLinksBetween = 
				new ArrayList<IRPLink>();

		@SuppressWarnings("unchecked")
		List<IRPLink> theExistingLinks = 
		inBuildingBlock.getLinks().toList();

		for( IRPLink theExistingLink : theExistingLinks ){

			IRPSysMLPort fromSysMLPort = theExistingLink.getFromSysMLPort();
			IRPModelElement fromSysMLElement = theExistingLink.getFromElement();

			IRPSysMLPort toSysMLPort = theExistingLink.getToSysMLPort();
			IRPModelElement toSysMLElement = theExistingLink.getToElement();

			if( fromSysMLPort != null && 
					fromSysMLElement != null && fromSysMLElement instanceof IRPInstance &&
					toSysMLPort != null &&
					toSysMLElement != null && toSysMLElement instanceof IRPInstance ){

				if( thePort.equals( fromSysMLPort ) && 
						ownedByPart.equals( fromSysMLElement ) &&
						andThePort.equals( toSysMLPort ) &&
						whichIsOwnedByPart.equals( toSysMLElement ) ){

					super.debug("Check for links between " + super.elInfo(fromSysMLPort) + " and " + 
							super.elInfo( toSysMLPort ) + " successfully found " + 
							super.elInfo( theExistingLink ) );

					theLinksBetween.add( theExistingLink );

				} else if( thePort.equals( toSysMLPort ) && 
						ownedByPart.equals( fromSysMLElement ) &&
						andThePort.equals( fromSysMLPort ) &&
						whichIsOwnedByPart.equals( toSysMLElement ) ){

					super.debug("Check for links between " + super.elInfo(toSysMLPort) + " and " + 
							super.elInfo( fromSysMLPort ) + " successfully found " + 
							super.elInfo( theExistingLink ) );

					theLinksBetween.add( theExistingLink );

				} else {
					//					Logger.writeLine("Check for links between " + Logger.elementInfo(toSysMLPort) + " and " + 
					//							Logger.elementInfo( fromSysMLPort ) + " found no match to " + 
					//							Logger.elementInfo( theExistingLink ) );
				}

			} else {
				// we're only interested in flow ports
			}
		}

		super.debug("getLinksBetween " + super.elInfo( thePort ) + " and " +
				super.elInfo( andThePort ) + " has found " + 
				theLinksBetween.size() + " matches");

		return theLinksBetween;
	}

	public IRPLink addConnectorBetweenSysMLPortsIfOneDoesntExist(
			IRPSysMLPort theSrcPort,
			IRPInstance theSrcPart, 
			IRPSysMLPort theTgtPort,
			IRPInstance theTgtPart) {

		IRPLink theLink = null;

		IRPClass theAssemblyBlock = (IRPClass) theSrcPart.getOwner();

		super.debug( "addConnectorBetweenSysMLPortsIfOneDoesntExist has determined that " + 
				super.elInfo( theAssemblyBlock ) + " is the assembly block" );

		List<IRPLink> theLinks = getLinksBetween(
				theSrcPort, 
				theSrcPart,
				theTgtPort, 
				theTgtPart,
				theAssemblyBlock );

		// only add if one does not already exist
		if( theLinks.size() == 0 ){

			super.debug( "Adding a new connector between " + super.elInfo( theSrcPort ) + 
					" and " + super.elInfo( theTgtPort ) + " as one does not exist" ); 

			IRPPackage thePkg = (IRPPackage) theAssemblyBlock.getOwner();

			theLink = thePkg.addLinkBetweenSYSMLPorts(
					theSrcPart, 
					theTgtPart, 
					null, 
					theSrcPort, 
					theTgtPort );

			theLink.changeTo("connector");

			String theUniqueName = determineUniqueNameBasedOn(
					theSrcPart.getName() + "_" + theSrcPort.getName() + "__" + 
							theTgtPart.getName() + "_" + theTgtPort.getName(), 
							"Link", 
							theAssemblyBlock );

			theLink.setName( theUniqueName );		
			theLink.setOwner( theAssemblyBlock );

			super.debug("Added " + super.elInfo( theLink ) + 
					" to " + super.elInfo( theAssemblyBlock ));
		}

		return theLink;
	}

	public Set<IRPModelElement> getSetOfElementsFromCombiningThe(
			List<IRPModelElement> theSelectedEls,
			List<IRPGraphElement> theSelectedGraphEls ){

		Set<IRPModelElement> theSetOfElements = 
				new HashSet<IRPModelElement>( theSelectedEls );

		for( IRPGraphElement theGraphEl : theSelectedGraphEls ){

			IRPModelElement theEl = theGraphEl.getModelObject();

			if( theEl != null ){
				theSetOfElements.add( theEl );
			}
		}

		return theSetOfElements;
	}

	public void addGeneralization(
			IRPClassifier fromElement, 
			String toBlockWithName, 
			IRPPackage underneathTheRootPackage ){

		IRPModelElement theBlock = 
				underneathTheRootPackage.findNestedElementRecursive( 
						toBlockWithName, "Block" );

		if( theBlock != null ){
			fromElement.addGeneralization( (IRPClassifier) theBlock );
		} else {
			super.error( "Error: Unable to find element with name " + toBlockWithName );
		}
	}

	public IRPClass findOwningClassIfOneExistsFor( 
			IRPModelElement theModelEl ){

		IRPModelElement theOwner = theModelEl.getOwner();
		IRPClass theResult = null;

		if( ( theOwner != null ) &&
				!( theOwner instanceof IRPProject ) ){

			if( theOwner.getMetaClass().equals("Class") ){

				theResult = (IRPClass) theOwner;
			} else {
				theResult = findOwningClassIfOneExistsFor( theOwner );
			}
		}

		return theResult;
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

	public IRPModelElement getOwningClassifierFor(IRPModelElement theState){

		IRPModelElement theOwner = theState.getOwner();

		while (theOwner.getMetaClass().equals("State") || theOwner.getMetaClass().equals("Statechart")){
			theOwner = theOwner.getOwner();
		}

		super.debug("The owner for " + super.elInfo(theState) + " is " + super.elInfo(theOwner));

		return theOwner;
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