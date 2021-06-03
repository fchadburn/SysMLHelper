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
import com.telelogic.rhapsody.core.IRPActivityDiagram;
import com.telelogic.rhapsody.core.IRPActor;
import com.telelogic.rhapsody.core.IRPAttribute;
import com.telelogic.rhapsody.core.IRPClass;
import com.telelogic.rhapsody.core.IRPClassifier;
import com.telelogic.rhapsody.core.IRPComment;
import com.telelogic.rhapsody.core.IRPConstraint;
import com.telelogic.rhapsody.core.IRPDependency;
import com.telelogic.rhapsody.core.IRPEvent;
import com.telelogic.rhapsody.core.IRPEventReception;
import com.telelogic.rhapsody.core.IRPFlowchart;
import com.telelogic.rhapsody.core.IRPGraphElement;
import com.telelogic.rhapsody.core.IRPGuard;
import com.telelogic.rhapsody.core.IRPInstance;
import com.telelogic.rhapsody.core.IRPLink;
import com.telelogic.rhapsody.core.IRPModelElement;
import com.telelogic.rhapsody.core.IRPOperation;
import com.telelogic.rhapsody.core.IRPPackage;
import com.telelogic.rhapsody.core.IRPPin;
import com.telelogic.rhapsody.core.IRPPort;
import com.telelogic.rhapsody.core.IRPProfile;
import com.telelogic.rhapsody.core.IRPProject;
import com.telelogic.rhapsody.core.IRPRequirement;
import com.telelogic.rhapsody.core.IRPSendAction;
import com.telelogic.rhapsody.core.IRPState;
import com.telelogic.rhapsody.core.IRPStateVertex;
import com.telelogic.rhapsody.core.IRPStatechart;
import com.telelogic.rhapsody.core.IRPStereotype;
import com.telelogic.rhapsody.core.IRPSysMLPort;
import com.telelogic.rhapsody.core.IRPTag;
import com.telelogic.rhapsody.core.IRPTransition;
import com.telelogic.rhapsody.core.IRPUnit;

public class ExecutableMBSE_Context extends ConfigurationSettings {
	
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
	}
	
	public boolean getIsShowProfileVersionCheckDialogs(){
		
		boolean result = getBooleanPropertyValue(
				_rhpPrj,
				"SysMLHelper.General.IsShowProfileVersionCheckDialogs" );
				
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
				"SysMLHelper.FunctionalAnalysis.TraceabilityTypeToUseForFunctions" );
		
		return theStereotype;
	}
	
	public IRPStereotype getStereotypeToUseForActions(
			IRPModelElement basedOnContext ){
		
		IRPStereotype theStereotype = getStereotypeBasedOn(
				basedOnContext, 
				"SysMLHelper.RequirementsAnalysis.TraceabilityTypeToUseForActions" );
		
		return theStereotype;
	}
	
	public IRPStereotype getStereotypeToUseForUseCases(
			IRPModelElement basedOnContext ){
		
		IRPStereotype theStereotype = getStereotypeBasedOn(
				basedOnContext, 
				"SysMLHelper.RequirementsAnalysis.TraceabilityTypeToUseForUseCases" );
		
		return theStereotype;
	}
	
	public IRPStereotype getStereotypeForUseCaseDiagram(
			IRPModelElement basedOnContext ){
		
		IRPStereotype theStereotype = getStereotypeBasedOn(
				basedOnContext, 
				"SysMLHelper.RequirementsAnalysis.NewTermForUseCaseDiagram" );
		
		if( theStereotype == null ){
			super.warning( "Warning, no stereotype was set for NewTermForUseCaseDiagram" );
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
	
	public String getActionTextFrom(
			IRPModelElement theEl) {
		
		String theSourceInfo = null;
		
		if (theEl instanceof IRPState){
			IRPState theState = (IRPState)theEl;
			String theStateType = theState.getStateType();
			
			if (theStateType.equals("Action")){
				theSourceInfo = theState.getEntryAction();
				
			} else if (theStateType.equals("AcceptEventAction")){ // receive event
				
				IRPAcceptEventAction theAcceptEventAction = (IRPAcceptEventAction)theEl;
				IRPEvent theEvent = theAcceptEventAction.getEvent();
				
				if (theEvent==null){
					super.debug( "Event has no name so using Name" );
					theSourceInfo = theState.getName();
				} else {
					theSourceInfo = theEvent.getName();
				}
				
			} else if (theStateType.equals("EventState")){ // send event
				
				IRPSendAction theSendAction = theState.getSendAction();
				
				if (theSendAction != null){
					IRPEvent theEvent = theSendAction.getEvent();
					
					if (theEvent != null){
						theSourceInfo = theEvent.getName();
					} else {
						super.debug("SendAction has no Event so using Name of action");
						theSourceInfo = theState.getName();
					}
				} else {
					super.warning( "Warning in deriveDownstreamRequirement, theSendAction is null" );
				}	
				
			} else if (theStateType.equals("TimeEvent")){
				
				IRPAcceptTimeEvent theAcceptTimeEvent = (IRPAcceptTimeEvent)theEl;
				String theDuration = theAcceptTimeEvent.getDurationTime();
				
				if (theDuration.isEmpty()){
					theSourceInfo = theAcceptTimeEvent.getName();
				} else {
					theSourceInfo = theDuration;
				}
				
			} else {
				super.warning("Warning in getActionTextFrom, " + theStateType + " was not handled");
			}
			
		} else if (theEl instanceof IRPTransition){
			
			IRPTransition theTrans = (IRPTransition)theEl;
			IRPGuard theGuard = theTrans.getItsGuard();
			
			// check that transition has a guard before trying to use it
			if( theGuard != null ){
				theSourceInfo = ((IRPTransition) theEl).getItsGuard().getBody();
			} else {
				theSourceInfo = "TBD"; // no source info available
			}
			
		} else if (theEl instanceof IRPComment){
			
			theSourceInfo = theEl.getDescription();

		} else if (theEl instanceof IRPRequirement){
			
			IRPRequirement theReqt = (IRPRequirement)theEl;
			theSourceInfo = theReqt.getSpecification();

		} else if (theEl instanceof IRPConstraint){
			
			IRPConstraint theConstraint = (IRPConstraint)theEl;
			theSourceInfo = theConstraint.getSpecification();		

		} else {
			super.error("Error in getActionTextFrom, " + super.elInfo(theEl) + " was not handled as of an unexpected type");
			theSourceInfo = ""; // default
		}
		
		if( theSourceInfo != null ){
			
			if( theSourceInfo.isEmpty() ){
				
				super.warning( "Warning, " + super.elInfo( theEl ) + " has no text" );
			} else {
				theSourceInfo = decapitalize( theSourceInfo );
			}
		}
		
		return theSourceInfo;
	}
	
	public String promptUserForTextEntry(
			String withTitle, 
			String andQuestion, 
			String andDefault, 
			int size ){
		
		String theEntry = andDefault;
		
		JPanel panel = new JPanel();
		
		panel.add( new JLabel( andQuestion ) );
		
		JTextField theTextField = new JTextField( size );
		panel.add( theTextField );
		
		if (!andDefault.isEmpty())
			theTextField.setText(andDefault);
		
		int choice = JOptionPane.showConfirmDialog(
				null, 
				panel, 
				withTitle, 
				JOptionPane.OK_CANCEL_OPTION );
		
		if( choice==JOptionPane.OK_OPTION ){
			String theTextEntered = theTextField.getText(); 
			
			if (!theTextEntered.isEmpty()){
				theEntry = theTextField.getText();
			} else {
				super.debug("No text was entered, using default response of '" + andDefault + "'");
			}
		}
		
		return theEntry;
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
	
	public boolean isStateUnique(
			String theProposedName, 
			IRPState underneathTheEl ){
				
		int count = 0;
		
		@SuppressWarnings("unchecked")
		List<IRPState> theExistingEls = 
				underneathTheEl.getSubStates().toList();
		
		for (IRPModelElement theExistingEl : theExistingEls) {
			
			if (theExistingEl.getName().equals( theProposedName )){
				count++;
				break;
			}
		}
		
		if (count > 1){
			super.warning("Warning in isStateUnique, there are " + count + " elements called " + 
					theProposedName + ". This may cause issues.");
		}
				
		boolean isUnique = (count == 0);

		return isUnique;
	}
	
	public String determineUniqueStateBasedOn(
			String theProposedName,
			IRPState underElement ){
		
		int count = 0;
		
		String theUniqueName = theProposedName;
		
		while( !isStateUnique(
				theUniqueName, underElement ) ){
			
			count++;
			theUniqueName = theProposedName + count;
		}
		
		return theUniqueName;
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
	
	public void setStringTagValueOn( 
			IRPModelElement theOwner, 
			String theTagName, 
			String theValue ){
		
		IRPTag theTag = theOwner.getTag( theTagName );
		
		if( theTag != null ){
			theOwner.setTagValue( theTag, theValue );
		} else {
			
			super.error( "Error in GeneralHelpers.setStringTagValueOn for " + 
					super.elInfo( theOwner) + ", unable to find tag called " + theTagName );
		}
	}
	
	public String getStringForTagCalled( 
			String theTagName,
			IRPModelElement onElement,
			String defaultIfNotSet ){
	
		String theValue = defaultIfNotSet;
		
		IRPTag theTag = onElement.getTag( theTagName );
		
		if( theTag != null ){
			theValue = theTag.getValue();
			
			if( theValue.isEmpty() ){
				theValue = defaultIfNotSet;
			}
		}
		
		return theValue;
	}
	
	public IRPPackage getExistingOrCreateNewPackageWith( 
			String theName, 
			IRPModelElement underneathTheEl ){
		
		IRPModelElement thePackage = findElementWithMetaClassAndName(
				"Package", theName, underneathTheEl );
		
		if( thePackage == null ){

			super.info( "Create a package called " + theName );
			thePackage = underneathTheEl.addNewAggr( "Package", theName );
		}
		
		return (IRPPackage) thePackage;
	}

	public IRPModelElement getExistingOrCreateNewElementWith( 
			String theName, 
			String andMetaClass,
			IRPModelElement underneathTheEl ){
		
		IRPModelElement theElement =
				findElementWithMetaClassAndName(
						andMetaClass, theName, underneathTheEl );
		
		try {
			if( theElement == null ){
				theElement = underneathTheEl.addNewAggr( andMetaClass, theName );
			}
			
		} catch (Exception e) {
			super.error("Exception in getExistingOrCreateNewElementWith( theName " + theName + 
					", andMetaClass=" + andMetaClass + ", underneath=" + super.elInfo(underneathTheEl));
		}
		
		return theElement;
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
	
	public void cleanUpModelRemnants( 
			IRPProject inProject ){
		
		deleteIfPresent( "Structure1", "StructureDiagram", inProject );
		deleteIfPresent( "Model1", "ObjectModelDiagram", inProject );
		deleteIfPresent( "Default", "Package", inProject );
		
		IRPModelElement theDefaultComponent = 
				inProject.findElementsByFullName("DefaultComponent", "Component");
		
		if( theDefaultComponent != null ){
			theDefaultComponent.setName( "NotUsedComp" );
			
			IRPModelElement theDefaultConfig = 
					theDefaultComponent.findNestedElement("DefaultConfig", "Configuration");
			
			if( theDefaultConfig != null ){
				theDefaultConfig.setName( "NotUsedComp" );
			}
		}
	}
	
	public IRPGraphElement getGraphElement(
			IRPModelElement element,
			IRPFlowchart fc) {

		IRPGraphElement ret = null;

		@SuppressWarnings("unchecked")
		List<IRPGraphElement> gList = fc.getGraphicalElements().toList();
		
		if (!gList.isEmpty()) {
			for (IRPGraphElement g : gList) {
				if (g.getModelObject() != null) {
					if(g.getModelObject().getGUID().equals(element.getGUID())) {

						ret = g;
						break;
					}
				}
			}
		}
		
		return ret;
	}
	
	public IRPGraphElement getCorrespondingGraphElement(
			IRPModelElement forElement,
			IRPActivityDiagram theAD ) throws Exception {

		IRPGraphElement ret = null;

		@SuppressWarnings("unchecked")
		List<IRPGraphElement> theGraphEls = 
				theAD.getCorrespondingGraphicElements( forElement ).toList();
		
		if( theGraphEls.size() > 1 ){
			throw new Exception("There is more than one graph element for " + super.elInfo(forElement));
		} else if( theGraphEls.size() == 1 ){
			ret = theGraphEls.get( 0 );
		} else {
			super.warning("Warning, getCorrespondingGraphElement dif not find a graph element corresponding to " + 
					super.elInfo(forElement) + " on " + super.elInfo(theAD) );
		}
		
		return ret;
	}
	
	
	public IRPPin getPin(
			String withName,
			IRPAcceptEventAction onAcceptEventAction ){
		
		IRPPin thePin = null;
		
		List<IRPPin> theCandidates = getPins( onAcceptEventAction );
		
		for( IRPPin theCandidate : theCandidates ){
			
			if( theCandidate.getName().equals( withName ) ){
				thePin = theCandidate;
			}
		}
		
		return thePin;
	}
	
	public List<IRPPin> getPins(
			IRPAcceptEventAction onAcceptEventAction ){
		
		List<IRPPin> thePins = new ArrayList<>();
		
		for( Object o: onAcceptEventAction.getSubStateVertices().toList()){
			if( o instanceof IRPPin ){
			
				thePins.add( (IRPPin) o );
			}
		}
		
		return thePins;
	}
	
	public IRPStateVertex getTargetOfOutTransitionIfSingleOneExisting(
			IRPStateVertex theStateVertex ){
		
		IRPStateVertex theTarget = null;
		
		@SuppressWarnings("unchecked")
		List<IRPTransition> theOutTransitions = theStateVertex.getOutTransitions().toList();
		
		if( theOutTransitions.size() == 1 ){
			
			IRPTransition theTransition = theOutTransitions.get( 0 );
			theTarget = theTransition.getItsTarget();
		}
		
		return theTarget;
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
	
	private IRPModelElement getOwningClassifierFor(IRPModelElement theState){
		
		IRPModelElement theOwner = theState.getOwner();
		
		while (theOwner.getMetaClass().equals("State") || theOwner.getMetaClass().equals("Statechart")){
			theOwner = theOwner.getOwner();
		}
		
		super.debug("The owner for " + super.elInfo(theState) + " is " + super.elInfo(theOwner));
			
		return theOwner;
	}
	
	private IRPState getStateCalled(
			String theName, 
			IRPStatechart inTheDiagram, 
			IRPModelElement ownedByEl){
		
		@SuppressWarnings("unchecked")
		List<IRPModelElement> theElsInDiagram = inTheDiagram.getElementsInDiagram().toList();
		
		IRPState theState = null;
		
		int count = 0;
		
		for (IRPModelElement theEl : theElsInDiagram) {
			
			if (theEl instanceof IRPState 
					&& theEl.getName().equals(theName)
					&& getOwningClassifierFor(theEl).equals(ownedByEl)){
				
				super.debug("Found state called " + theEl.getName() + " owned by " + theEl.getOwner().getFullPathName());
				
				theState = (IRPState) theEl;
				count++;
			}
		}
		
		if (count != 1){
			super.debug("Warning in getStateCalled (" + count + ") states called " + theName + " were found");
		}
		
		return theState;
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
