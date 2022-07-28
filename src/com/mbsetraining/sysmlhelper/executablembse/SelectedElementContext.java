package com.mbsetraining.sysmlhelper.executablembse;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import com.mbsetraining.sysmlhelper.common.UserInterfaceHelper;
import com.telelogic.rhapsody.core.*;

public class SelectedElementContext {

	private static final String tagNameForAssemblyBlockUnderDev = "assemblyBlockUnderDev";
	private static final String tagNameForPackageForEventsAndInterfaces = "packageForEventsAndInterfaces";
	private static final String tagNameForPackageForBlocks = "packageForBlocks";
	private static final String tagNameForPackageForActorsAndTest = "packageForActorsAndTest";
	private static final String tagNameForPackageForFunctionBlocks = "packageForFunctionBlocks";
	private static final String tagNameForBlockForFunctionUsages = "blockForFunctionUsages";

	private List<IRPGraphElement> _selectedGraphEls;
	private IRPModelElement _selectedEl;
	private IRPPackage _rootContextPackage;
	private IRPClass _buildingBlock;
	private IRPClass _chosenBlock;
	private IRPDiagram _sourceGraphElDiagram;
	private Set<IRPRequirement> _selectedReqts;
	private ExecutableMBSE_Context _context;
	private IRPPackage _packageForActorsAndTest;
	private IRPPackage _packageForEventsAndInterfaces;
	private IRPPackage _packageForBlocks;
	private IRPPackage _packageForFunctionBlocks;
	private IRPClass _blockForFunctionUsages;

	public SelectedElementContext(
			ExecutableMBSE_Context context ){

		_context = context;
	}

	public void setContextTo( 
			IRPModelElement theElement ){

		try {
			_selectedEl = theElement;
			_selectedGraphEls = _context.getSelectedGraphElements();
			
			if( _selectedEl instanceof IRPRequirement &&
					!_selectedGraphEls.isEmpty() ){
				
				IRPGraphElement theGraphEl = _selectedGraphEls.get( 0 );
				
				_rootContextPackage = getContextSettingsPackageBasedOn( theGraphEl.getDiagram() );
			} else {
				_rootContextPackage = getContextSettingsPackageBasedOn( _selectedEl );
			}

			if( _rootContextPackage instanceof IRPPackage
					&& _context.hasStereotypeCalled(
							_context.FUNCT_ANALYSIS_SCENARIOS_PACKAGE,
							_rootContextPackage ) ){

				_context.debug( "SelectedElementContext _rootContextPackage is "
						+ _context.elInfo( _rootContextPackage ) );

				_packageForActorsAndTest = getPkgNamedInTagUnder(
						_rootContextPackage, tagNameForPackageForActorsAndTest );

				_buildingBlock = (IRPClass) getElementNamedInFunctionalPackageTag(
						_rootContextPackage, tagNameForAssemblyBlockUnderDev );

				_packageForEventsAndInterfaces = getPkgNamedInTagUnder(
						_rootContextPackage,
						tagNameForPackageForEventsAndInterfaces );

				_packageForBlocks = getPkgNamedInTagUnder(
						_rootContextPackage, tagNameForPackageForBlocks );

			} else if (_rootContextPackage instanceof IRPPackage
					&& _context.hasStereotypeCalled(
							_context.NEW_TERM_FOR_FEATURE_FUNCTION_PACKAGE,
							_rootContextPackage)) {

				_context.debug( "SelectedElementContext _rootContextPackage is "
						+ _context.elInfo( _rootContextPackage ) );

				_packageForFunctionBlocks = getPkgNamedInTagUnder(
						_rootContextPackage, tagNameForPackageForFunctionBlocks );

				_context.debug( "_packageForFunctionBlocks is "
						+ _context.elInfo( _packageForFunctionBlocks ) );

				if( _packageForFunctionBlocks == null ){

					_packageForFunctionBlocks = _rootContextPackage;

					setElementTagValueOn( _rootContextPackage,
							_context.NEW_TERM_FOR_FEATURE_FUNCTION_PACKAGE,
							tagNameForPackageForFunctionBlocks, _rootContextPackage );
				}

				IRPModelElement tagValueResult = getElementNamedInFunctionalPackageTag(
						_rootContextPackage, tagNameForBlockForFunctionUsages);

				if( tagValueResult instanceof IRPClass ){

					_blockForFunctionUsages = (IRPClass) tagValueResult;
				} else {

					List<IRPModelElement> theCandidates = _context.
							findElementsWithMetaClassAndStereotype(
									"Class", _context.NEW_TERM_FOR_FEATURE_BLOCK, _rootContextPackage, 1 );

					if( theCandidates.size() == 1 ){
						_blockForFunctionUsages = (IRPClass) theCandidates.get( 0 );
					} else if( theCandidates.size() > 1 ){

						_blockForFunctionUsages = (IRPClass) _context.
								launchDialogToSelectElement(
										theCandidates, 
										"Which " + _context.FEATURE_BLOCK + " do you want to add usages to?", 
										false );
					} else {
						
						theCandidates = _context.
								findElementsWithMetaClassAndStereotype(
										"Class", _context.NEW_TERM_FOR_FUNCTION_BLOCK, _rootContextPackage, 1 );

						if( theCandidates.size() == 1 ){
							_blockForFunctionUsages = (IRPClass) theCandidates.get( 0 );
						} else if( theCandidates.size() > 1 ){

							_blockForFunctionUsages = (IRPClass) _context.
									launchDialogToSelectElement(
											theCandidates, 
											"Which " + _context.FUNCTION_BLOCK + " do you want to add usages to?", 
											false );
						}			
					}

					if( _blockForFunctionUsages != null ){					
						setElementTagValueOn(_rootContextPackage,
								_context.NEW_TERM_FOR_FEATURE_FUNCTION_PACKAGE,
								tagNameForBlockForFunctionUsages,
								_blockForFunctionUsages );
					}
				}
			}

			_sourceGraphElDiagram = getSourceDiagram();
			
		} catch( Exception e ){
			_context.error( "Exception in SelectedElementContext::SetContextTo, e=" + e.getMessage() );
		}
	}

	public List<IRPGraphElement> get_selectedGraphEls() {
		return _selectedGraphEls;
	}

	@SuppressWarnings("unchecked")
	public Set<IRPRequirement> getSelectedReqts(){

		if( _selectedReqts == null ){

			Set<IRPModelElement> theMatchingEls = 
					_context.findModelElementsIn( 
							_selectedGraphEls, 
							"Requirement" );

			// cast to IRPRequirement
			_selectedReqts = 
					(Set<IRPRequirement>)(Set<?>) theMatchingEls;
		}

		return _selectedReqts;
	}

	public IRPModelElement getSelectedEl() {
		return _selectedEl;
	}

	public IRPGraphElement getSelectedGraphEl() {

		IRPGraphElement theGraphEl = null;

		if( _selectedGraphEls != null &&
				!_selectedGraphEls.isEmpty() ){

			theGraphEl = _selectedGraphEls.get(0);
		} else {
			_context.debug("getSelectedGraphEl is returning null");
		}

		return theGraphEl;
	}

	private IRPPackage getOwningPackageFor(
			IRPModelElement theElement ){

		IRPPackage theOwningPackage = null;

		if (theElement == null) {

			_context.warning("getOwningPackage for was invoked for a null element");

		} else if (theElement instanceof IRPPackage) {
			theOwningPackage = (RPPackage) theElement;

		} else if (theElement instanceof IRPProject) {
			_context.warning( "Unable to find an owning package for " + 
					theElement.getFullPathNameIn() + " as I reached project" );

		} else {
			theOwningPackage = getOwningPackageFor(theElement.getOwner());
		}

		return theOwningPackage;
	}

	public IRPPackage getPackageForSelectedEl() {

		return getOwningPackageFor(_selectedEl);
	}

	public IRPClass getChosenBlock() {
		return _chosenBlock;
	}

	public IRPDiagram getSourceDiagram() {

		if (_sourceGraphElDiagram == null) {

			if (_selectedEl instanceof IRPDiagram) {

				_sourceGraphElDiagram = (IRPDiagram) _selectedEl;
			} else if( _selectedGraphEls != null && !_selectedGraphEls.isEmpty() ){
				_sourceGraphElDiagram = _selectedGraphEls.get(0).getDiagram();
			}
		}

		return _sourceGraphElDiagram;
	}

	public IRPClass getBlockUnderDev(
			String theMsg ){

		if (_chosenBlock == null) {

			if (_selectedEl instanceof IRPClass) {

				if (_context.hasStereotypeCalled("TestDriver", _selectedEl)) {
					UserInterfaceHelper.showWarningDialog( "Sorry, you cannot perform " + theMsg + " with a TestDriver part" );
				} else {
					_chosenBlock = (IRPClass) _selectedEl;
				}

			} else if (_selectedEl instanceof IRPInstance) {

				IRPInstance thePart = (IRPInstance) _selectedEl;
				IRPClassifier theOtherClass = thePart.getOtherClass();

				if (_context.isTestDriver(thePart)) {

					UserInterfaceHelper.showWarningDialog( "Sorry, you cannot perform " + theMsg + " with a TestDriver part" );

				} else if (!(theOtherClass instanceof IRPClass)) {

					UserInterfaceHelper.showWarningDialog( "Sorry, you cannot perform " + theMsg + " with a " + _selectedEl.getUserDefinedMetaClass() );
				} else {
					_chosenBlock = (IRPClass) theOtherClass;
				}

			} else if (_selectedEl.getMetaClass().equals("StatechartDiagram")) {

				IRPModelElement theOwner = _context
						.findOwningClassIfOneExistsFor(_selectedEl);

				// _context.debug( _context.elInfo( theOwner ) +
				// "is the Owner");

				if (_context.hasStereotypeCalled("TestDriver", theOwner)) {

					UserInterfaceHelper.showWarningDialog( "Sorry, you cannot perform " + theMsg + " with a TestDriver part" );

				} else if (!(theOwner instanceof IRPClass)) {

					UserInterfaceHelper.showWarningDialog( "Sorry, you cannot perform " + theMsg + " with a " + _selectedEl.getUserDefinedMetaClass() );
				} else {
					_chosenBlock = (IRPClass) theOwner;
				}
			} else {
				List<IRPModelElement> theCandidates = 
						getNonActorOrTestBlocks( _buildingBlock );

				if (theCandidates.isEmpty()) {

					_context.warning( "No parts typed by Blocks were found underneath " + 
							_context.elInfo( _buildingBlock ) );
				} else {

					if (theCandidates.size() > 1) {

						final IRPModelElement theChosenBlockEl = 
								_context.launchDialogToSelectElement(
										theCandidates, 
										theMsg, 
										true ); 

						if (theChosenBlockEl instanceof IRPClass) {
							_chosenBlock = (IRPClass) theChosenBlockEl;
						}
					} else {
						_chosenBlock = (IRPClass) theCandidates.get(0);
					}
				}
			}
		}

		return _chosenBlock;
	}

	public IRPClass getBuildingBlock() {

		if( _buildingBlock == null && 
				_rootContextPackage != null ){

			try {
				IRPModelElement elementInTag = getElementNamedInFunctionalPackageTag(
						_rootContextPackage, tagNameForAssemblyBlockUnderDev);

				_context.info("Element named in "
						+ tagNameForAssemblyBlockUnderDev + " is "
						+ _context.elInfo(elementInTag));

				if (elementInTag instanceof IRPClass) {
					_buildingBlock = (IRPClass) elementInTag;
				}

			} catch (Exception e) {
				_context.error("Exception in getBuildingBlock, "
						+ "while trying to get "
						+ tagNameForAssemblyBlockUnderDev);
			}
		}

		return _buildingBlock;
	}

	private List<IRPModelElement> getNonActorOrTestBlocks(
			IRPClass withInstancesUnderTheBlock) {

		@SuppressWarnings("unchecked")
		List<IRPModelElement> theCandidateParts = withInstancesUnderTheBlock
		.getNestedElementsByMetaClass("Instance", 1).toList();

		List<IRPModelElement> theNonActorOrTestBlocks = new ArrayList<IRPModelElement>();

		for (IRPModelElement theCandidatePart : theCandidateParts) {

			IRPInstance theInstance = (IRPInstance) theCandidatePart;
			IRPClassifier theClassifier = theInstance.getOtherClass();

			// don't add actors or test driver
			if (theClassifier != null
					&& theClassifier instanceof IRPClass
					&& !_context.hasStereotypeCalled("TestDriver",
							theClassifier)
							&& !theNonActorOrTestBlocks.contains(theClassifier)) {

				theNonActorOrTestBlocks.add(theClassifier);
			}
		}

		return theNonActorOrTestBlocks;
	}

	public IRPModelElement getElementNamedInFunctionalPackageTag(
			String theTagName) {

		IRPModelElement theEl = null;

		IRPTag theTag = _rootContextPackage.getTag(theTagName);

		if (theTag != null) {

			// call getValueSpecifications() to retrieve tag value collection

			IRPCollection valSpecs = theTag.getValueSpecifications();

			@SuppressWarnings("rawtypes")
			Iterator looper = valSpecs.toList().iterator();

			// call getValue() to retrieve each element instance set as the tag
			// value

			while (looper.hasNext()) {

				IRPInstanceValue ins = (IRPInstanceValue) looper.next();
				theEl = ins.getValue();
				break;
			}
		}

		if (theEl == null) {
			_context.error("Error in getElementNamedInFunctionalPackageTag, "
					+ "unable to find value for tag called " + theTagName
					+ " under " + _context.elInfo(_rootContextPackage));
		}

		return theEl;
	}

	public IRPPackage getContextSettingsPackageBasedOn(
			IRPModelElement theContextEl) {

		_context.debug("getContextSettingsPackageBasedOn invoked for "
				+ _context.elInfo(theContextEl));

		IRPPackage theSettingsPkg = null;

		if (theContextEl instanceof IRPProject) {

			List<IRPModelElement> thePackageEls = _context
					.findElementsWithMetaClassAndStereotype("Package",
							_context.FUNCT_ANALYSIS_SCENARIOS_PACKAGE,
							theContextEl.getProject(), 1);

			if (thePackageEls.isEmpty()) {

				thePackageEls = _context
						.findElementsWithMetaClassAndStereotype(
								"Package",
								_context.FUNCT_ANALYSIS_FEATURE_FUNCTION_PACKAGE,
								theContextEl.getProject(), 1);

				if (thePackageEls.isEmpty()) {

					_context.warning("Unable to find a "
							+ _context.FUNCT_ANALYSIS_SCENARIOS_PACKAGE
							+ " or "
							+ _context.FUNCT_ANALYSIS_FEATURE_FUNCTION_PACKAGE
							+ " package. Have you added one to the model?");

				} else if (thePackageEls.size() == 1) {

					theSettingsPkg = (IRPPackage) thePackageEls.get(0);

				} else {
					_context.debug("There are " + thePackageEls.size() + " "
							+ _context.FUNCT_ANALYSIS_FEATURE_FUNCTION_PACKAGE
							+ " packages, so you will have to choose one?");

					IRPModelElement theUserSelectedPkg = UserInterfaceHelper
							.launchDialogToSelectElement(thePackageEls,
									"Choose which settings to use", true);

					if (theUserSelectedPkg != null) {
						theSettingsPkg = (IRPPackage) theUserSelectedPkg;
					}
				}
			} else if (thePackageEls.size() == 1) {

				theSettingsPkg = (IRPPackage) thePackageEls.get(0);

			} else {
				_context.debug("There are " + thePackageEls.size() + " "
						+ _context.FUNCT_ANALYSIS_SCENARIOS_PACKAGE
						+ " packages, so you will have to choose one?");

				IRPModelElement theUserSelectedPkg = UserInterfaceHelper
						.launchDialogToSelectElement(thePackageEls,
								"Choose which settings to use", true);

				if (theUserSelectedPkg != null) {
					theSettingsPkg = (IRPPackage) theUserSelectedPkg;
				}
			}

		} else if (theContextEl instanceof IRPPackage
				&& _context
				.hasStereotypeCalled(
						_context.FUNCT_ANALYSIS_SCENARIOS_PACKAGE,
						theContextEl)) {

			_context.debug("getContextSettingsPackageBasedOn is returning "
					+ _context.elInfo(theContextEl));

			theSettingsPkg = (IRPPackage) theContextEl;

		} else if (theContextEl instanceof IRPPackage
				&& _context.hasStereotypeCalled(
						_context.FUNCT_ANALYSIS_FEATURE_FUNCTION_PACKAGE,
						theContextEl)) {

			_context.debug("getContextSettingsPackageBasedOn is returning "
					+ _context.elInfo(theContextEl));

			theSettingsPkg = (IRPPackage) theContextEl;

		} else if (theContextEl instanceof IRPPackage
				&& _context.hasStereotypeCalled(
						_context.REQTS_ANALYSIS_USE_CASE_PACKAGE, 
						theContextEl)) {

			@SuppressWarnings("unchecked")
			List<IRPModelElement> theReferences = theContextEl.getReferences().toList();
			
			List<IRPModelElement> theCandidates = new ArrayList<>();

			for (IRPModelElement theReference : theReferences) {

				if (theReference instanceof IRPDependency) {

					IRPDependency theDependency = (IRPDependency) theReference;
					IRPModelElement theDependent = theDependency.getDependent();

					if (theDependent instanceof IRPPackage
							&& _context.hasStereotypeCalled(
									_context.FUNCT_ANALYSIS_SCENARIOS_PACKAGE,
									theDependent)) {

						theCandidates.add( theDependent );

					} else if (theDependent instanceof IRPPackage
							&& _context
							.hasStereotypeCalled(
									_context.FUNCT_ANALYSIS_FEATURE_FUNCTION_PACKAGE,
									theDependent)) {

						theCandidates.add( theDependent );

					} else if (theDependent instanceof IRPPackage
							&& _context
							.hasStereotypeCalled(
									_context.REQTS_ANALYSIS_WORKING_COPY_PACKAGE,
									theDependent)) {

						IRPModelElement theLinkedPackage =
								getContextSettingsPackageBasedOn( theDependent );
						
						if( theLinkedPackage instanceof IRPPackage ){
							theCandidates.add( theLinkedPackage );

						}
					}	
				}
			}
			
			if( theCandidates.size() == 1 ){
				
				theSettingsPkg = (IRPPackage) theCandidates.get(0);
				
			} else if( theCandidates.size() > 1 ){
				
				_context.debug( "There are " + theCandidates.size() + " "
						+ _context.FUNCT_ANALYSIS_FEATURE_FUNCTION_PACKAGE + " or " 
						+ _context.FUNCT_ANALYSIS_SCENARIOS_PACKAGE 	
						+ " packages, so you will have to choose one?");

				IRPModelElement theUserSelectedPkg = UserInterfaceHelper
						.launchDialogToSelectElement(theCandidates,
								"Choose which settings to use", true);

				if (theUserSelectedPkg != null) {
					theSettingsPkg = (IRPPackage) theUserSelectedPkg;
				}
			}
			


		} else {

			//_context.debug( "Recursing to owner of " + _context.elInfo( theContextEl ) );

			// recurse
			theSettingsPkg = getContextSettingsPackageBasedOn(theContextEl
					.getOwner());
		}

		return theSettingsPkg;
	}

	public IRPPackage getFunctionBlockRootPackageBasedOn(
			IRPModelElement theContextEl ){

		//_context.debug( "getFunctionBlockRootPackageBasedOn invoked for "
		//		+ _context.elInfo( theContextEl ) );

		IRPPackage theSettingsPkg = null;

		if( theContextEl instanceof IRPProject ){

			List<IRPModelElement> thePackageEls = _context
					.findElementsWithMetaClassAndStereotype( "Package",
							_context.FUNCT_ANALYSIS_FEATURE_FUNCTION_PACKAGE,
							theContextEl.getProject(), 1 );

			if( thePackageEls.isEmpty() ){
				_context.warning("Unable to find a "
						+ _context.FUNCT_ANALYSIS_FEATURE_FUNCTION_PACKAGE
						+ " package. Have you added one to the model?" );

			} else if (thePackageEls.size() == 1) {

				theSettingsPkg = (IRPPackage) thePackageEls.get(0);

			} else {
				_context.debug("There are " + thePackageEls.size() + " "
						+ _context.FUNCT_ANALYSIS_FEATURE_FUNCTION_PACKAGE
						+ " packages, so you will have to choose one?");

				IRPModelElement theUserSelectedPkg = UserInterfaceHelper
						.launchDialogToSelectElement(thePackageEls,
								"Choose which settings to use", true);

				if (theUserSelectedPkg != null) {
					theSettingsPkg = (IRPPackage) theUserSelectedPkg;
				}
			}

		} else if( theContextEl instanceof IRPPackage
				&& _context.hasStereotypeCalled(
						_context.FUNCT_ANALYSIS_FEATURE_FUNCTION_PACKAGE,
						theContextEl ) ){

			//_context.debug( "getFunctionBlockRootPackageBasedOn is returning "
			//		+ _context.elInfo( theContextEl ) );

			theSettingsPkg = (IRPPackage) theContextEl;

		} else if( theContextEl instanceof IRPPackage
				&& _context.hasStereotypeCalled(
						_context.REQTS_ANALYSIS_USE_CASE_PACKAGE, theContextEl ) ){

			@SuppressWarnings("unchecked")
			List<IRPModelElement> theReferences = theContextEl.getReferences().toList();

			for( IRPModelElement theReference : theReferences ){

				if (theReference instanceof IRPDependency) {

					IRPDependency theDependency = (IRPDependency) theReference;
					IRPModelElement theDependent = theDependency.getDependent();

					if (theDependent instanceof IRPPackage
							&& _context.hasStereotypeCalled(
									_context.FUNCT_ANALYSIS_FEATURE_FUNCTION_PACKAGE,
									theDependent)) {

						theSettingsPkg = (IRPPackage) theDependent;

					} else if (theDependent instanceof IRPPackage
							&& _context.hasStereotypeCalled(
									_context.REQTS_ANALYSIS_WORKING_COPY_PACKAGE,
									theDependent)) {

						theSettingsPkg = getFunctionBlockRootPackageBasedOn( theDependent );
					}
				}
			}

		} else {

			//_context.debug( "Recursing to look at owner of " + _context.elInfo( theContextEl ) );

			// recurse
			theSettingsPkg = getFunctionBlockRootPackageBasedOn( theContextEl.getOwner() );
		}

		return theSettingsPkg;
	}

	public IRPPackage getPackageForBlocks() {
		return _packageForBlocks;
	}

	public IRPPackage getPackageForFunctionBlocks() {
		return _packageForFunctionBlocks;
	}

	public IRPClass getBlockForFunctionUsages() {
		return _blockForFunctionUsages;
	}
	
	public IRPPackage getScenarioRootContextPackage() {
		return _rootContextPackage;
	}

	public List<IRPClass> getBuildingBlocks(IRPPackage underneathThePkg) {

		List<IRPClass> theBuildingBlocks = new ArrayList<IRPClass>();

		@SuppressWarnings("unchecked")
		List<IRPModelElement> theCandidateBlocks = underneathThePkg
		.getNestedElementsByMetaClass("Class", 1).toList();

		for (IRPModelElement theCandidateBlock : theCandidateBlocks) {

			@SuppressWarnings("unchecked")
			List<IRPInstance> theInstances = theCandidateBlock
			.getNestedElementsByMetaClass("Instance", 0).toList();

			for (IRPInstance theInstance : theInstances) {

				if (theInstance.getUserDefinedMetaClass().equals("Object")) {
					theBuildingBlocks.add((IRPClass) theCandidateBlock);
					break;
				}
			}
		}

		return theBuildingBlocks;
	}

	public IRPPackage getPackageForActorsAndTest() {
		return _packageForActorsAndTest;
	}

	public IRPModelElement getElementNamedInFunctionalPackageTag(
			IRPPackage theSettingsPkg, String theTagName) {

		IRPModelElement theEl = null;

		IRPTag theTag = theSettingsPkg.getTag(theTagName);

		if (theTag != null) {

			// retrieve tag value collection
			IRPCollection valSpecs = theTag.getValueSpecifications();

			@SuppressWarnings("rawtypes")
			Iterator looper = valSpecs.toList().iterator();

			// retrieve each element instance set as the tag value
			while (looper.hasNext()) {

				IRPInstanceValue ins = (IRPInstanceValue) looper.next();
				theEl = ins.getValue();
				break;
			}
		}

		if (theEl == null) {
			_context.debug("getElementNamedInFunctionalPackageTag was "
					+ "unable to find value for tag called " + theTagName
					+ " under " + _context.elInfo(theSettingsPkg));
		}

		return theEl;
	}

	public IRPPackage getPkgNamedInFunctionalPackageTag(
			IRPPackage theSettingsPkg, String theTagName) {

		IRPPackage thePackage = getPkgNamedInTagUnder(theSettingsPkg,
				theTagName);

		if (thePackage == null) {

			IRPClass theLogicalBlock = getBlockUnderDev(theSettingsPkg,
					"Unable to determine Logical Block, please pick one");

			if (theLogicalBlock != null) {

				// old projects may not have an InterfacesPkg hence use the
				// package the block is in
				IRPModelElement theOwner = theLogicalBlock.getOwner();

				if (theOwner instanceof IRPPackage) {
					thePackage = (IRPPackage) theOwner;
				} else {
					_context.error("Error in getPkgThatOwnsEventsAndInterfaces: Can't find event pkg for "
							+ _context.elInfo(theLogicalBlock));
				}
			}
		}

		return thePackage;
	}

	private IRPPackage getPkgNamedInTagUnder(
			IRPPackage theRootPackage,
			String theTagName) {

		IRPPackage thePackage = null;

		IRPTag theTag = theRootPackage.getTag( theTagName );

		if( theTag != null ){
			String thePackageName = theTag.getValue();

			thePackage = (IRPPackage) theRootPackage.getProject()
					.findNestedElementRecursive(thePackageName, "Package");

			if (thePackage == null) {
				_context.debug( "getPkgNamedInTagUnder was unable to find package called " + thePackageName );
			}
		} else {
			_context.debug( "getPkgNamedInTagUnder was unable to find tag called "
					+ theTagName
					+ " underneath "
					+ _context.elInfo( theRootPackage ) );
		}

		return thePackage;
	}

	public IRPClass getBlockUnderDev(IRPModelElement basedOnContextEl,
			String theMsg) {

		IRPClass theBlockUnderDev = null;

		if (_buildingBlock == null) {

			_context.error("Error in getBlockUnderDev, no building block was found underneath "
					+ _context.elInfo(basedOnContextEl));

		} else {

			List<IRPModelElement> theCandidates = getNonActorOrTestBlocks(_buildingBlock);

			if (theCandidates.isEmpty()) {

				_context.error("No parts typed by Blocks were found underneath "
						+ _context.elInfo(_buildingBlock));
			} else {

				if (theCandidates.size() > 1) {
					final IRPModelElement theChosenBlockEl = _context
							.launchDialogToSelectElement(theCandidates, theMsg,
									true);

					if (theChosenBlockEl != null
							&& theChosenBlockEl instanceof IRPClass) {
						theBlockUnderDev = (IRPClass) theChosenBlockEl;
					}
				} else {
					theBlockUnderDev = (IRPClass) theCandidates.get(0);
				}
			}
		}

		return theBlockUnderDev;
	}

	public IRPClass getTestBlock(IRPClass withInstanceUnderTheBlock) {

		@SuppressWarnings("unchecked")
		List<IRPModelElement> theCandidateParts = withInstanceUnderTheBlock
		.getNestedElementsByMetaClass("Object", 0).toList();

		IRPClass theTestBlock = null;

		for (IRPModelElement theCandidatePart : theCandidateParts) {

			IRPInstance theInstance = (IRPInstance) theCandidatePart;
			IRPClassifier theClassifier = theInstance.getOtherClass();

			// _context.debug( "The instance is " + _context.elInfo(
			// theInstance) +
			// " typed by " + _context.elInfo( theClassifier ) );

			// don't add actors or test driver
			if (theClassifier != null
					&& theClassifier instanceof IRPClass
					&& _context
					.hasStereotypeCalled("TestDriver", theClassifier)) {

				// _context.debug("Found " + _context.elInfo( theClassifier ) );
				theTestBlock = (IRPClass) theClassifier;
			}
		}

		return theTestBlock;
	}

	public List<IRPActor> getActors(IRPClass withInstancesUnderTheBlock) {

		@SuppressWarnings("unchecked")
		List<IRPModelElement> theCandidateParts = withInstancesUnderTheBlock
		.getNestedElementsByMetaClass("Instance", 1).toList();

		List<IRPActor> theActors = new ArrayList<IRPActor>();

		for (IRPModelElement theCandidatePart : theCandidateParts) {

			IRPInstance theInstance = (IRPInstance) theCandidatePart;
			IRPClassifier theClassifier = theInstance.getOtherClass();

			// only add actors
			if (theClassifier != null && theClassifier instanceof IRPActor) {

				theActors.add((IRPActor) theClassifier);
			}
		}

		return theActors;
	}

	public IRPPackage getPkgThatOwnsEventsAndInterfaces() {

		return _packageForEventsAndInterfaces;
	}

	public void setupFunctionalAnalysisTagsFor(IRPPackage theRootPackage,
			IRPClass theAssemblyBlockUnderDev,
			IRPPackage thePackageForEventsAndInterfaces,
			IRPPackage thePackageForActorsAndTest,
			IRPPackage thePackageForBlocks) {

		if (theRootPackage != null) {

			setElementTagValueOn(theRootPackage,
					_context.FUNCT_ANALYSIS_SCENARIOS_PACKAGE,
					tagNameForAssemblyBlockUnderDev, theAssemblyBlockUnderDev);

			setElementTagValueOn(theRootPackage,
					_context.FUNCT_ANALYSIS_SCENARIOS_PACKAGE,
					tagNameForPackageForActorsAndTest,
					thePackageForActorsAndTest);

			setElementTagValueOn(theRootPackage,
					_context.FUNCT_ANALYSIS_SCENARIOS_PACKAGE,
					tagNameForPackageForEventsAndInterfaces,
					thePackageForEventsAndInterfaces);

			setElementTagValueOn(theRootPackage,
					_context.FUNCT_ANALYSIS_SCENARIOS_PACKAGE,
					tagNameForPackageForBlocks, thePackageForBlocks);
		}
	}

	protected void setElementTagValueOn(IRPModelElement theOwner,
			String theStereotypeName, 
			String theTagName,
			IRPModelElement theValue ){

		// In order to set a value for a tag that comes from a stereotype, you
		// need to quote
		// the stereotype(stereotype_0)'s tag(tag_0) as its "base" tag

		IRPStereotype theStereotype = _context.getStereotypeWith( theStereotypeName );

		IRPTag baseTag = theStereotype.getTag(theTagName);

		// Return the newly created tag with value "value_A" and set it to
		// class_0
		IRPTag newTag = theOwner.setTagValue(baseTag, "");

		// Add other tags with different value to class_0, if the multiplicity >
		// 1
		newTag.addElementDefaultValue(theValue);
	}
}

/**
 * Copyright (C) 2019-2022 MBSE Training and Consulting Limited
 * (www.executablembse.com)
 * 
 * This file is part of SysMLHelperPlugin.
 * 
 * SysMLHelperPlugin is free software: you can redistribute it and/or modify it
 * under the terms of the GNU General Public License as published by the Free
 * Software Foundation, either version 3 of the License, or (at your option) any
 * later version.
 * 
 * SysMLHelperPlugin is distributed in the hope that it will be useful, but
 * WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or
 * FITNESS FOR A PARTICULAR PURPOSE. See the GNU General Public License for more
 * details.
 * 
 * You should have received a copy of the GNU General Public License along with
 * SysMLHelperPlugin. If not, see <http://www.gnu.org/licenses/>.
 */
