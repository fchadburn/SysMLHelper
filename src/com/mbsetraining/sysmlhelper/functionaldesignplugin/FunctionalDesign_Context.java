package com.mbsetraining.sysmlhelper.functionaldesignplugin;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import com.mbsetraining.sysmlhelper.common.ConfigurationSettings;
import com.mbsetraining.sysmlhelper.common.UserInterfaceHelper;
import com.telelogic.rhapsody.core.IRPActivityDiagram;
import com.telelogic.rhapsody.core.IRPActor;
import com.telelogic.rhapsody.core.IRPDependency;
import com.telelogic.rhapsody.core.IRPFlowchart;
import com.telelogic.rhapsody.core.IRPGraphElement;
import com.telelogic.rhapsody.core.IRPModelElement;
import com.telelogic.rhapsody.core.IRPPackage;
import com.telelogic.rhapsody.core.IRPProfile;
import com.telelogic.rhapsody.core.IRPProject;
import com.telelogic.rhapsody.core.IRPStereotype;
import com.telelogic.rhapsody.core.IRPUnit;

public class FunctionalDesign_Context extends ConfigurationSettings {
	
	public FunctionalDesign_Context(
			String theAppID ){
		
		super( theAppID, 
				"FunctionalDesignProfile.General.EnableErrorLogging", 
				"FunctionalDesignProfile.General.EnableWarningLogging",
				"FunctionalDesignProfile.General.EnableInfoLogging", 
				"FunctionalDesignProfile.General.EnableDebugLogging",
				"FunctionalDesignProfile.General.PluginVersion",
				"FunctionalDesignProfile.General.UserDefinedMetaClassesAsSeparateUnit",
				"FunctionalDesignProfile.General.AllowPluginToControlUnitGranularity",
				"FunctionalDesign.properties", 
				"FunctionalDesign_MessagesBundle",
				"FunctionalDesign" 
				);
	}
	
	public boolean getIsShowProfileVersionCheckDialogs(){
		
		boolean result = getBooleanPropertyValue(
				_rhpPrj,
				"SysMLHelper.General.IsShowProfileVersionCheckDialogs" );
				
		return result;
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
	
	public IRPFlowchart getTemplateForActivityDiagram(
			IRPModelElement basedOnContext,
			String basedOnPropertyKey ){
		
		IRPFlowchart theTemplate = null;
		
		String theTemplateName = 
				basedOnContext.getPropertyValue( 
						basedOnPropertyKey );
		
		if( theTemplateName != null && 
			!theTemplateName.trim().isEmpty() ){
			
			theTemplate = (IRPFlowchart) basedOnContext.getProject().findNestedElementRecursive(
							theTemplateName, 
							"ActivityDiagram" );
			
			if( theTemplate == null ){
				super.warning( "Warning, unable to find template called " + 
						theTemplateName + " named in TemplateForActivityDiagram property" );
			}
		}
		
		super.debug( "getTemplateForActivityDiagram, found " + super.elInfo( theTemplate ) );
		
		return theTemplate;
	}
	
	public String getDefaultActorPackageName(
			IRPModelElement basedOnContext ){
	
		String theValue = basedOnContext.getPropertyValue(
				"SysMLHelper.RequirementsAnalysis.DefaultActorPackageName");
		
		if( theValue == null || theValue.isEmpty() ){
			theValue = "Error";
		}
		
		return theValue;
	}
	
	public String getDefaultUseCasePackageName(
			IRPModelElement basedOnContext ){
	
		String theValue = basedOnContext.getPropertyValue(
				"SysMLHelper.RequirementsAnalysis.DefaultUseCasePackageName" );
		
		if( theValue == null || theValue.isEmpty() ){
			theValue = "Error";
		}
		
		return theValue;
	}
	
	public boolean getIsAutoPopulatePackageDiagram(
			IRPModelElement basedOnContext ){
	
		boolean result = getBooleanPropertyValue(
				basedOnContext,
				"SysMLHelper.General.IsAutoPopulatePackageDiagram" );
		
		return result;		
	}
	
	public String getCSVExportArtifactType(
			IRPModelElement basedOnContext ){
	
		String theValue = basedOnContext.getPropertyValue(
				"SysMLHelper.RequirementsAnalysis.CSVExportArtifactType" );
		
		if( theValue == null || theValue.isEmpty() ){
			theValue = "Error";
		}
		
		return theValue;
	}
	
	public boolean getCVSExportIncludeArtifactName(
			IRPModelElement basedOnContext ){
	
		boolean result = getBooleanPropertyValue(
				basedOnContext,
				"SysMLHelper.RequirementsAnalysis.CVSExportIncludeArtifactName" );
		
		return result;		
	}
	
	public List<String> getStoreUnitInSeparateDirectoryNewTerms(
			IRPModelElement theContextEl ){
		
		return getListFromCommaSeparatedString(
				theContextEl, 
				"SysMLHelper.General.StoreUnitInSeparateDirectoryNewTerms" );
	}
	
	public List<String> getDontCreateSeparateUnitNewTerms(
			IRPModelElement theContextEl ){
		
		return getListFromCommaSeparatedString(
				theContextEl, 
				"SysMLHelper.General.DontCreateSeparateUnitNewTerms" );
	}
	
	public List<String> getDefaultActorsForMasterActorsPackage(
			IRPPackage theContextEl ){
		
		return getDefaultLegalActorNamesFor(
				theContextEl, 
				"SysMLHelper.General.DefaultActorsForMasterActorsPackage");
	}
	
	public List<String> getDefaultActorsForUseCaseDiagram(
			IRPPackage theContextEl ){
		
		return getDefaultLegalActorNamesFor(
				theContextEl, 
				"SysMLHelper.RequirementsAnalysis.DefaultActorsForUseCaseDiagram");
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
				"SysMLHelper.General.RootPackageStereotypes" );
	}

	public List<IRPModelElement> getStereotypesForMasterActorPackage(
			IRPModelElement forContextEl ){
		
		return getStereotypesBasedOnProperty(
				forContextEl, 
				"SysMLHelper.General.MasterActorPackageStereotypes" );
	}
	
	public List<IRPModelElement> getStereotypesForSystemDesignPackage(
			IRPModelElement forContextEl ){
		
		return getStereotypesBasedOnProperty(
				forContextEl, 
				"SysMLHelper.General.SystemLevelPackageStereotypes" );
	}
	
	public List<IRPModelElement> getStereotypesForBlockPartCreation(
			IRPModelElement forContextEl ){
		
		return getStereotypesBasedOnProperty(
				forContextEl, 
				"SysMLHelper.FunctionalAnalysis.StereotypesForBlockCreation" );
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
	
	protected IRPStereotype getStereotypeBasedOn(
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
						"SysMLHelper.General.ElapsedTimeBlockStereotype" );
		
		return thePropertyValue;
	}
	
	public String getElapsedTimeActorStereotype(
			IRPModelElement basedOnContextEl ) {
		
		String thePropertyValue = 
				basedOnContextEl.getPropertyValue( 
						"SysMLHelper.General.ElapsedTimeActorStereotype" );
		
		return thePropertyValue;
	}
	
	public String getTestbenchStereotype(
			IRPModelElement basedOnContextEl ) {
		
		String thePropertyValue = 
				basedOnContextEl.getPropertyValue( 
						"SysMLHelper.General.TestbenchStereotype" );
		
		return thePropertyValue;
	}
	
	public String getMasterActorPackageStereotype(
			IRPModelElement basedOnContextEl ) {

		return basedOnContextEl.getPropertyValue( 
				"SysMLHelper.General.MasterActorPackageStereotype" );

	}

	public String getActorPackageStereotype(
			IRPModelElement basedOnContextEl ) {
		
			String thePropertyValue = 
					basedOnContextEl.getPropertyValue( 
							"SysMLHelper.General.ActorPackageStereotype" );

			return thePropertyValue;
		}
	
	public String getUseCasePackageStereotype(
			IRPModelElement basedOnContextEl ) {
		
			String thePropertyValue = 
					basedOnContextEl.getPropertyValue( 
							"SysMLHelper.General.UseCasePackageStereotype" );

			return thePropertyValue;
		}

	public String getUseCasePackageWorkingStereotype(
			IRPModelElement basedOnContextEl ) {
		
			String thePropertyValue = 
					basedOnContextEl.getPropertyValue( 
							"SysMLHelper.General.UseCasePackageWorkingStereotype" );

			return thePropertyValue;
		}
	
	public String getRequirementPackageStereotype(
			IRPModelElement basedOnContextEl ) {
		
			String thePropertyValue = 
					basedOnContextEl.getPropertyValue( 
							"SysMLHelper.General.RequirementPackageStereotype" );

			return thePropertyValue;
	}
	
	public String getSimulationPackageStereotype(
			IRPModelElement basedOnContextEl ) {
		
			String thePropertyValue = 
					basedOnContextEl.getPropertyValue( 
							"SysMLHelper.General.SimulationPackageStereotype" );

			return thePropertyValue;
		}
	
	public String getInterfacesPackageStereotype(
		IRPModelElement basedOnContextEl ) {
			
		String thePropertyValue = 
				basedOnContextEl.getPropertyValue( 
						"SysMLHelper.General.InterfacesPackageStereotype" );

		return thePropertyValue;
	}

	public String getDesignPackageStereotype(
			IRPModelElement basedOnContextEl ) {
		
		String thePropertyValue = 
				basedOnContextEl.getPropertyValue( 
						"SysMLHelper.General.DesignPackageStereotype" );
		
		return thePropertyValue;
	}
	
	public String getFunctionsPackageStereotype(
			IRPModelElement basedOnContextEl ) {
		
		String thePropertyValue = 
				basedOnContextEl.getPropertyValue( 
						"SysMLHelper.General.FunctionsPackageStereotype" );
		
		return thePropertyValue;
	}
	
	public String getSystemContextPackageStereotype(
			IRPModelElement basedOnContextEl ) {
		
		String thePropertyValue = 
				basedOnContextEl.getPropertyValue( 
						"SysMLHelper.General.SystemContextPackageStereotype" );
		
		return thePropertyValue;
	}
	
	public String getParametricsPackageStereotype(
			IRPModelElement basedOnContextEl ) {
		
		String thePropertyValue = 
				basedOnContextEl.getPropertyValue( 
						"SysMLHelper.General.ParametricsPackageStereotype" );
		
		return thePropertyValue;
	}
	
	public String getTestPackageStereotype(
			IRPModelElement basedOnContextEl ) {
		
		String thePropertyValue = 
				basedOnContextEl.getPropertyValue( 
						"SysMLHelper.General.TestPackageStereotype" );
		
		return thePropertyValue;
	}
	
	public String getUseCaseNoteText(
			IRPModelElement basedOnContextEl ){
		
		String thePropertyValue = 
				basedOnContextEl.getPropertyValue( 
						"SysMLHelper.RequirementsAnalysis.UseCaseNoteText" );
		
		if( thePropertyValue == null ){
			thePropertyValue = "Error in getUseCaseNoteText";
		}
		
		return thePropertyValue;
	}
	
	public String getCreateRequirementTextForPrefixing(
			IRPModelElement basedOnContextEl ){
				
		String thePropertyValue = 
				basedOnContextEl.getPropertyValue( 
						"SysMLHelper.RequirementsAnalysis.CreateRequirementTextForPrefixing" );
	
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
				"SysMLHelper.RequirementsAnalysis.IsEnableAutoMoveOfRequirements" );
		
		return result;
	}
	
	public boolean getIsApplyAutoShowToSequenceDiagramTemplate(
			IRPModelElement forContextEl ){
		
		boolean result = getBooleanPropertyValue(
				forContextEl,
				"SysMLHelper.FunctionalAnalysis.IsApplyAutoShowToSequenceDiagramTemplate" );
		
		return result;
	}
	
	public boolean getIsCreateSDWithAutoShowApplied(
			IRPModelElement forContextEl ){
		
		boolean result = getBooleanPropertyValue(
				forContextEl,
				"SysMLHelper.FunctionalAnalysis.IsCreateSDWithAutoShowApplied" );
		
		return result;
	}
	
	public boolean getIsCreateSDWithTestDriverLifeline(
			IRPModelElement forContextEl ){
		
		boolean result = getBooleanPropertyValue(
				forContextEl,
				"SysMLHelper.FunctionalAnalysis.IsCreateSDWithTestDriverLifeline" );
		
		return result;
	}
	
	public boolean getIsEnableAutoMoveOfInterfaces(
			IRPModelElement forContextEl ){
		
		boolean result = getBooleanPropertyValue(
				forContextEl,
				"SysMLHelper.FunctionalAnalysis.IsEnableAutoMoveOfInterfaces" );
		
		return result;
	}
	
	public boolean getIsEnableGatewayTypes(
			IRPModelElement forContextEl ){
		
		boolean result = getBooleanPropertyValue(
				forContextEl,
				"SysMLHelper.RequirementsAnalysis.IsEnableGatewayTypes" );
		
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
				"SysMLHelper.General.IsCreateParametricSubpackageSelected" );
				
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
				"SysMLHelper.FunctionalAnalysis.IsConvertToDetailedADOptionEnabled" );

		return result;
	}

	public boolean getIsConvertToDetailedADOptionWantedByDefault(
			IRPModelElement forContextEl ){
		
		boolean result = getBooleanPropertyValue(
				forContextEl,
				"SysMLHelper.FunctionalAnalysis.IsConvertToDetailedADOptionWantedByDefault" );

		return result;
	}
	
	public boolean getIsAllowInheritanceChoices(
			IRPModelElement forContextEl ){
		
		boolean result = getBooleanPropertyValue(
				forContextEl,
				"SysMLHelper.FunctionalAnalysis.IsAllowInheritanceChoices" );
				
		return result;
	}
	
	public boolean getIsShowProfileVersionCheckDialogs(
			IRPModelElement forContextEl ){
		
		boolean result = getBooleanPropertyValue(
				forContextEl,
				"SysMLHelper.General.IsShowProfileVersionCheckDialogs" );
				
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
	
	public List<IRPActivityDiagram> buildListOfActivityDiagramsFor(
			List<IRPModelElement> theSelectedEls) {
		
		List<IRPActivityDiagram> theADs = new ArrayList<IRPActivityDiagram>();
		
		for (IRPModelElement theSelectedEl : theSelectedEls) {
			
			@SuppressWarnings("unchecked")
			List<IRPActivityDiagram> theCandidates = theSelectedEl.getNestedElementsByMetaClass("ActivityDiagramGE", 1).toList();
			
			for (IRPActivityDiagram theCandidate : theCandidates) {
				if (!theADs.contains(theCandidate)){
					
					if (theCandidate.isReadOnly()==0){
						theADs.add(theCandidate);			
						super.debug("Adding " + super.elInfo(theCandidate.getOwner()) + " to the list");
					} else {
						super.debug("Skipping " + super.elInfo(theCandidate.getOwner()) + " as it is read-only");
					}
				}
			}
		}
		return theADs;
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
}
