package functionalanalysisplugin;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import com.mbsetraining.sysmlhelper.common.UserInterfaceHelper;
import com.mbsetraining.sysmlhelper.executablembse.ExecutableMBSE_Context;
import com.telelogic.rhapsody.core.*;

public class FunctionalAnalysisSettings {

	private static final String tagNameForAssemblyBlockUnderDev = "assemblyBlockUnderDev";
	private static final String tagNameForPackageForEventsAndInterfaces = "packageForEventsAndInterfaces";
	private static final String tagNameForPackageForActorsAndTest = "packageForActorsAndTest";
	private static final String tagNameForPackageForBlocks = "packageForBlocks";
	private static final String tagNameForPackageForWorkingCopies = "packageForWorkingCopies";

	private ExecutableMBSE_Context _context;
	
	public FunctionalAnalysisSettings(
			ExecutableMBSE_Context context ) {

		_context = context;
	}

	public List<IRPModelElement> getNonActorOrTestBlocks(
			IRPClass withInstancesUnderTheBlock ){

		@SuppressWarnings("unchecked")
		List<IRPModelElement> theCandidateParts = 
		withInstancesUnderTheBlock.getNestedElementsByMetaClass( "Instance", 1 ).toList();

		List<IRPModelElement> theNonActorOrTestBlocks = new ArrayList<IRPModelElement>();

		for( IRPModelElement theCandidatePart : theCandidateParts ) {

			IRPInstance theInstance = (IRPInstance)theCandidatePart;
			IRPClassifier theClassifier = theInstance.getOtherClass();

			// don't add actors or test driver
			if( theClassifier != null && 
					theClassifier instanceof IRPClass &&
					!_context.hasStereotypeCalled( "TestDriver", theClassifier ) &&
					!theNonActorOrTestBlocks.contains( theClassifier ) ){

				theNonActorOrTestBlocks.add( theClassifier );
			}
		}

		return theNonActorOrTestBlocks;
	}

	public List<IRPClass> getBuildingBlocks(
			IRPPackage underneathThePkg ){

		List<IRPClass> theBuildingBlocks = new ArrayList<IRPClass>();

		@SuppressWarnings("unchecked")
		List<IRPModelElement> theCandidateBlocks = 
		underneathThePkg.getNestedElementsByMetaClass( "Class", 1 ).toList();

		for( IRPModelElement theCandidateBlock : theCandidateBlocks ) {

			@SuppressWarnings("unchecked")
			List<IRPInstance> theInstances = 
			theCandidateBlock.getNestedElementsByMetaClass( "Instance", 0 ).toList();

			for (IRPInstance theInstance : theInstances) {

				if( theInstance.getUserDefinedMetaClass().equals("Object")){
					theBuildingBlocks.add( (IRPClass) theCandidateBlock );
					break;
				}
			}
		}

		return theBuildingBlocks;
	}

	public IRPClass getBuildingBlock( 
			IRPModelElement basedOnContextEl ){

		_context.debug("getBuildingBlock was invoked for " + _context.elInfo( basedOnContextEl ) );
		
		IRPClass theBuildingBlock =
				(IRPClass) getElementNamedInFunctionalPackageTag(
						basedOnContextEl, 
						tagNameForAssemblyBlockUnderDev );

		_context.debug("... getBuildingBlock completed (" + _context.elInfo(theBuildingBlock) + " was found)");

		return theBuildingBlock;
	}

	public IRPPackage getPackageForActorsAndTest(
			IRPModelElement basedOnContextEl ){

		IRPPackage thePackage = getPkgNamedInFunctionalPackageTag(
				basedOnContextEl, 
				tagNameForPackageForActorsAndTest );

		return thePackage;
	}

	public IRPModelElement getElementNamedInFunctionalPackageTag(
			IRPModelElement basedOnContextEl,
			String theTagName ){

		IRPModelElement theEl = null;

		IRPModelElement theSettingsPkg = 
				getSimulationSettingsPackageBasedOn( basedOnContextEl );

		if( theSettingsPkg != null ){
			IRPTag theTag = theSettingsPkg.getTag( theTagName );

			if( theTag != null ){

				// retrieve tag value collection
				IRPCollection valSpecs = theTag.getValueSpecifications();

				@SuppressWarnings("rawtypes")
				Iterator looper = valSpecs.toList().iterator();

				// retrieve each element instance set as the tag value
				while( looper.hasNext() ){

					IRPInstanceValue ins = (IRPInstanceValue)looper.next();
					theEl = ins.getValue();
					break;
				}
			}
		}

		//		if( theEl == null ){
		//			Logger.writeLine( "Error in getElementNamedInFunctionalPackageTag, " + 
		//					"unable to find value for tag called " + theTagName + " under " + 
		//					Logger.elementInfo( basedOnContextEl ) );
		//		}

		return theEl;
	}

	public IRPPackage getPkgNamedInFunctionalPackageTag(
			IRPModelElement basedOnContextEl,
			String theTagName ){

		IRPPackage thePackage = null;

		IRPModelElement theSettingsPkg = 
				getSimulationSettingsPackageBasedOn( basedOnContextEl );

		if( theSettingsPkg != null ){
			IRPTag theTag = theSettingsPkg.getTag( theTagName );

			if( theTag != null ){
				String thePackageName = theTag.getValue();

				thePackage = (IRPPackage) basedOnContextEl.getProject().findNestedElementRecursive(
						thePackageName, "Package");

				if( thePackage == null ){
					_context.debug( "getPkgNamedInFunctionalPackageTag was unable to find package called " + 
							thePackageName );
				}
			} else {
				_context.debug( "getPkgNamedInFunctionalPackageTag was unable to find tag called " + 
						theTagName + " underneath " + _context.elInfo( theSettingsPkg ) );
			}
		} else {
			_context.debug("getPkgNamedInFunctionalPackageTag was unable to find a functional analysis pkg based on " + 
					_context.elInfo( basedOnContextEl ) );
		}

		if( thePackage == null ){

			IRPClass theLogicalBlock = getBlockUnderDev( 
					basedOnContextEl, 
					"Unable to determine Logical Block, please pick one" );

			// old projects may not have an InterfacesPkg hence use the package the block is in
			IRPModelElement theOwner = theLogicalBlock.getOwner();

			if( theOwner instanceof IRPPackage ){
				thePackage = (IRPPackage)theOwner;
			} else {
				_context.error( "Error in getPkgThatOwnsEventsAndInterfaces: Can't find event pkg for " + _context.elInfo( theLogicalBlock ) );
			}
		}

		return thePackage;
	}

	public IRPPackage getPkgThatOwnsEventsAndInterfaces(
			IRPModelElement basedOnContextEl ){

		IRPPackage thePackage = 
				(IRPPackage) getElementNamedInFunctionalPackageTag(
						basedOnContextEl, 
						tagNameForPackageForEventsAndInterfaces );

		return thePackage;
	}

	public IRPPackage getWorkingPkgUnderDev(
			IRPModelElement basedOnContextEl ){

		IRPPackage theWorkingPkg = 
				(IRPPackage) getElementNamedInFunctionalPackageTag(
						basedOnContextEl, 
						tagNameForPackageForWorkingCopies );

		return theWorkingPkg;
	}

	public IRPClass getBlockUnderDev(
			IRPModelElement basedOnContextEl,
			String theMsg ){

		IRPClass theBlockUnderDev = null;

		IRPClass theBuildingBlock = 
				getBuildingBlock( basedOnContextEl );

		if( theBuildingBlock == null ){

			_context.error( "Error in getBlockUnderDev, no building block was found underneath " + 
					_context.elInfo( basedOnContextEl ) );

		} else {

			List<IRPModelElement> theCandidates = 
					getNonActorOrTestBlocks( theBuildingBlock );

			if( theCandidates.isEmpty() ){

				_context.error("Error in getBlockUnderDev, no parts typed by Blocks were found underneath " + 
						_context.elInfo( theBuildingBlock ) );
			} else {

				if( theCandidates.size() > 1 ){
					final IRPModelElement theChosenBlockEl = 
							_context.launchDialogToSelectElement(
									theCandidates, theMsg, true ); 

					if( theChosenBlockEl != null && theChosenBlockEl instanceof IRPClass ){
						theBlockUnderDev = (IRPClass) theChosenBlockEl;
					}
				} else {
					theBlockUnderDev = (IRPClass) theCandidates.get( 0 );
				}
			}
		}

		return theBlockUnderDev;
	}

	public IRPClass getTestBlock(
			IRPClass withInstanceUnderTheBlock ){

		@SuppressWarnings("unchecked")
		List<IRPModelElement> theCandidateParts = 
		withInstanceUnderTheBlock.getNestedElementsByMetaClass( "Object", 0 ).toList();

		IRPClass theTestBlock = null;

		for( IRPModelElement theCandidatePart : theCandidateParts ) {

			IRPInstance theInstance = (IRPInstance)theCandidatePart;
			IRPClassifier theClassifier = theInstance.getOtherClass();

			_context.debug( "The instance is " + _context.elInfo( theInstance) + 
					" typed by " + _context.elInfo( theClassifier ) );

			// don't add actors or test driver
			if( theClassifier != null && 
					theClassifier instanceof IRPClass &&
					_context.hasStereotypeCalled( "TestDriver", theClassifier ) ){

				_context.debug("Found " + _context.elInfo( theClassifier ) );
				theTestBlock = (IRPClass) theClassifier;
			}
		}

		return theTestBlock;
	}

	public List<IRPActor> getActors(
			IRPClass withInstancesUnderTheBlock ){

		@SuppressWarnings("unchecked")
		List<IRPModelElement> theCandidateParts = 
		withInstancesUnderTheBlock.getNestedElementsByMetaClass( "Instance", 1 ).toList();

		List<IRPActor> theActors = new ArrayList<IRPActor>();

		for( IRPModelElement theCandidatePart : theCandidateParts ) {

			IRPInstance theInstance = (IRPInstance)theCandidatePart;
			IRPClassifier theClassifier = theInstance.getOtherClass();

			// only add actors
			if( theClassifier != null && 
					theClassifier instanceof IRPActor ){

				theActors.add( (IRPActor) theClassifier );
			}
		}

		return theActors;
	}

	public void setupFunctionalAnalysisTagsFor(
			IRPPackage theRootPackage,
			IRPClass theAssemblyBlockUnderDev,
			IRPPackage thePackageForEventsAndInterfaces, 
			IRPPackage thePackageForActorsAndTest,
			IRPPackage thePackageForBlocks ){

		if( theRootPackage != null ){

			setModelElementTagValueOn( 
					theRootPackage, 
					tagNameForAssemblyBlockUnderDev, 
					"Class",
					theAssemblyBlockUnderDev );

			setModelElementTagValueOn( 
					theRootPackage, 
					tagNameForPackageForActorsAndTest,
					"Package",
					thePackageForActorsAndTest );

			setModelElementTagValueOn( 
					theRootPackage, 
					tagNameForPackageForEventsAndInterfaces, 
					"Package",
					thePackageForEventsAndInterfaces );

			setModelElementTagValueOn( 
					theRootPackage, 
					tagNameForPackageForBlocks, 
					"Package",
					thePackageForBlocks );	
		}
	}


	public void setupFunctionalAnalysisTagsFor2(
			IRPPackage theRootPackage,
			IRPClass theAssemblyBlockUnderDev,
			IRPPackage thePackageForEventsAndInterfaces, 
			IRPPackage thePackageForActorsAndTest,
			IRPPackage thePackageForBlocks ){

		if( theRootPackage != null ){

			String theStereotypeName = 
					_context.getSimulationPackageStereotype( theRootPackage );

			setElementTagValueOn( 
					theRootPackage, 
					theStereotypeName, 
					tagNameForAssemblyBlockUnderDev, 
					theAssemblyBlockUnderDev );

			setElementTagValueOn( 
					theRootPackage, 
					theStereotypeName, 
					tagNameForPackageForActorsAndTest, 
					thePackageForActorsAndTest );

			setElementTagValueOn( 
					theRootPackage, 
					theStereotypeName, 
					tagNameForPackageForEventsAndInterfaces, 
					thePackageForEventsAndInterfaces );

			setElementTagValueOn( 
					theRootPackage, 
					theStereotypeName, 
					tagNameForPackageForBlocks, 
					thePackageForBlocks );	
		}	
	}

	private void setElementTagValueOn( 
			IRPModelElement theOwner, 
			String theStereotypeName,
			String theTagName, 
			IRPModelElement theValue ){

		// In order to set a value for a tag that comes from a stereotype, you need to quote 
		// the stereotype(stereotype_0)'s tag(tag_0) as its "base" tag

		IRPStereotype theStereotype = 
				_context.getExistingStereotype(
						theStereotypeName, theOwner.getProject() );

		IRPTag baseTag = theStereotype.getTag( theTagName );

		// Return the newly created tag with value "value_A" and set it to class_0
		IRPTag newTag = theOwner.setTagValue( baseTag, "" );

		// Add other tags with different value to class_0, if the multiplicity > 1
		newTag.addElementDefaultValue( theValue );
	}

	public void setModelElementTagValueOn( 
			IRPModelElement theOwner, 
			String theTagName, 
			String theTagTypeDeclaration,
			IRPModelElement theValue ){

		IRPTag theTag = theOwner.getTag( theTagName );

		if( theTag != null ){
			String theExistingTagValue = theTag.getValue();
			theTag.deleteFromProject();
			IRPTag theNewTag = (IRPTag)theOwner.addNewAggr( "Tag", theTagName );
			theNewTag.setDeclaration( theTagTypeDeclaration );
			theOwner.setTagElementValue( theNewTag, theValue );

			_context.debug( _context.elInfo( theOwner ) + " already has a tag called " + theTagName + 
					", changing it from '" + theExistingTagValue + "'" + " to '" + theNewTag.getValue() + "'");

		} else {
			IRPTag theNewTag = (IRPTag)theOwner.addNewAggr( "Tag", theTagName );
			theNewTag.setDeclaration( theTagTypeDeclaration );
			theOwner.setTagElementValue( theNewTag, theValue );
			_context.debug( _context.elInfo( theNewTag ) + " has been added to " + 
					_context.elInfo(theOwner) + " and set to '" + theNewTag.getValue() + "'");
		}
	}

	public IRPPackage getSimulationSettingsPackageBasedOn(
			IRPModelElement theContextEl ){

		IRPPackage theSettingsPkg = null;

		if( theContextEl instanceof IRPProject ){

			List<IRPModelElement> thePackageEls = 
					_context.findElementsWithMetaClassAndStereotype(
							"Package", 
							_context.getSimulationPackageStereotype( theContextEl ), 
							theContextEl.getProject(), 
							1 );

			if( thePackageEls.isEmpty() ){

				IRPModelElement theFunctionalAnalysisPkg = 
						theContextEl.findElementsByFullName( "FunctionalAnalysisPkg", "Package" );

				if( theFunctionalAnalysisPkg == null ){
					_context.warning( "Warning in getSimulationSettingsPackageBasedOn, unable to find use case settings package");

				} else {
					theSettingsPkg = (IRPPackage) theFunctionalAnalysisPkg;
				}

			} else if( thePackageEls.size()==1){

				theSettingsPkg = (IRPPackage) thePackageEls.get(0);

			} else {
				_context.error( "Error in getSimulationSettingsPackageBasedOn, unable to find use case settings package");

				IRPModelElement theUserSelectedPkg = 
						UserInterfaceHelper.launchDialogToSelectElement(
								thePackageEls, 
								"Choose which settings to use", 
								true );

				if( theUserSelectedPkg != null ){
					theSettingsPkg = (IRPPackage) theUserSelectedPkg;
				}
			}

		} else if( theContextEl instanceof IRPPackage &&
				_context.hasStereotypeCalled(
						_context.getSimulationPackageStereotype( theContextEl ), 
						theContextEl ) ){

			_context.debug( "getSimulationSettingsPackageBasedOn, is returning " + _context.elInfo( theContextEl ) );

			theSettingsPkg = (IRPPackage) theContextEl;

		} else if( theContextEl instanceof IRPPackage &&
				_context.hasStereotypeCalled(
						_context.getUseCasePackageStereotype( theContextEl ), 
						theContextEl ) ){

			@SuppressWarnings("unchecked")
			List<IRPModelElement> theReferences = theContextEl.getReferences().toList();

			for( IRPModelElement theReference : theReferences ){

				if( theReference instanceof IRPDependency ){

					IRPDependency theDependency = (IRPDependency)theReference;
					IRPModelElement theDependent = theDependency.getDependent();

					if( theDependent instanceof IRPPackage &&
							_context.hasStereotypeCalled(
									_context.getSimulationPackageStereotype( theContextEl ), 
									theDependent ) ){

						theSettingsPkg = (IRPPackage) theDependent;
					}
				}
			}

		} else {

			// recurse
			theSettingsPkg = getSimulationSettingsPackageBasedOn(
					theContextEl.getOwner() );
		}

		return theSettingsPkg;
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
