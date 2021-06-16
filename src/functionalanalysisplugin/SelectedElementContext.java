package functionalanalysisplugin;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import com.mbsetraining.sysmlhelper.common.UserInterfaceHelper;
import com.mbsetraining.sysmlhelper.executablembse.ExecutableMBSE_Context;
import com.telelogic.rhapsody.core.*;

public class SelectedElementContext {

	private static final String tagNameForAssemblyBlockUnderDev = "assemblyBlockUnderDev";
	private static final String tagNameForPackageForEventsAndInterfaces = "packageForEventsAndInterfaces";
	private static final String tagNameForPackageForBlocks = "packageForBlocks";

	private List<IRPGraphElement> _selectedGraphEls;
	private IRPModelElement _selectedEl = null;
	private IRPModelElement _contextEl = null;
	private IRPClass _buildingBlock = null;
	private IRPClass _chosenBlock = null;
	private IRPDiagram _sourceGraphElDiagram = null;
	private Set<IRPRequirement> _selectedReqts = null;
	private ExecutableMBSE_Context _context;

	private FunctionalAnalysisSettings _settings;
	
	public SelectedElementContext(
			ExecutableMBSE_Context context ){
		
		_context = context;
		_selectedGraphEls = _context.getSelectedGraphElements();
		_selectedEl = _context.getSelectedElement();
		_contextEl = getContextEl();
		_sourceGraphElDiagram = getSourceDiagram();
		_settings = new FunctionalAnalysisSettings( _context );
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

	public IRPModelElement getSelectedEl(){
		return _selectedEl;
	}
	
	public IRPGraphElement getSelectedGraphEl(){
		
		IRPGraphElement theGraphEl = null;
		
		if( _selectedGraphEls != null &&
				!_selectedGraphEls.isEmpty() ){
			
			theGraphEl = _selectedGraphEls.get( 0 );
		} else {
			_context.warning( "getSelectedGraphEl is returning null");
		}
		
		return theGraphEl;
	}
	
	private IRPPackage getOwningPackageFor(
			IRPModelElement theElement ){

		IRPPackage theOwningPackage = null;

		if( theElement == null ){

			_context.warning( "getOwningPackage for was invoked for a null element" );

		} else if( theElement instanceof IRPPackage ){
			theOwningPackage = (RPPackage)theElement;

		} else if( theElement instanceof IRPProject ){
			_context.warning( "Unable to find an owning package for " + theElement.getFullPathNameIn() + " as I reached project" );

		} else {
			theOwningPackage = getOwningPackageFor( theElement.getOwner() );
		}

		return theOwningPackage;
	}
	
	public IRPPackage getPackageForSelectedEl(){
		
		return getOwningPackageFor( _selectedEl );
	}
	
	public IRPClass getChosenBlock(){
		return _chosenBlock;
	}
	
	public IRPDiagram getSourceDiagram(){

		if( _sourceGraphElDiagram == null ){
			
			if( _selectedEl instanceof IRPDiagram ){

				_sourceGraphElDiagram = (IRPDiagram) _selectedEl;
			} else if( _selectedGraphEls != null && !_selectedGraphEls.isEmpty() ){
				_sourceGraphElDiagram = _selectedGraphEls.get( 0 ).getDiagram();
			}
		}
		
		return _sourceGraphElDiagram;
	}
	
	private IRPModelElement getContextEl(){

		IRPModelElement theContextEl = _selectedEl;

		if( theContextEl == null ){

			if( _selectedGraphEls != null && !_selectedGraphEls.isEmpty() ){

				IRPGraphElement selectedGraphEl = _selectedGraphEls.get( 0 );
				theContextEl = selectedGraphEl.getModelObject();
			}
		}

		return theContextEl;
	}

	public IRPClass getBlockUnderDev(
			String theMsg ){

		if( _chosenBlock == null ){

			if( _selectedEl instanceof IRPClass ){

				if( _context.hasStereotypeCalled( "TestDriver", _selectedEl ) ){
					UserInterfaceHelper.showWarningDialog( "Sorry, you cannot perform " + theMsg + " with a TestDriver part" );
				} else {
					_chosenBlock = (IRPClass) _selectedEl;
				}

			} else if( _selectedEl instanceof IRPInstance ){

				IRPInstance thePart = (IRPInstance) _selectedEl;
				IRPClassifier theOtherClass = thePart.getOtherClass();

				if( _context.hasStereotypeCalled( "TestDriver", theOtherClass ) ){

					UserInterfaceHelper.showWarningDialog( "Sorry, you cannot perform " + theMsg + " with a TestDriver part" );

				} else if( !(theOtherClass instanceof IRPClass) ){

					UserInterfaceHelper.showWarningDialog( "Sorry, you cannot perform " + theMsg + " with a " + _selectedEl.getUserDefinedMetaClass() );
				} else {
					_chosenBlock = (IRPClass) theOtherClass;
				}

			} else if( _selectedEl.getMetaClass().equals( "StatechartDiagram" ) ){

				IRPModelElement theOwner = 
						_context.findOwningClassIfOneExistsFor( _selectedEl );

				_context.debug( _context.elInfo( theOwner ) + "is the Owner");

				if( _context.hasStereotypeCalled( "TestDriver", theOwner ) ){

					UserInterfaceHelper.showWarningDialog( "Sorry, you cannot perform " + theMsg + " with a TestDriver part" );

				} else if( !(theOwner instanceof IRPClass) ){

					UserInterfaceHelper.showWarningDialog( "Sorry, you cannot perform " + theMsg + " with a " + _selectedEl.getUserDefinedMetaClass() );
				} else {
					_chosenBlock = (IRPClass) theOwner;
				}
			} else {
				List<IRPModelElement> theCandidates = 
						getNonActorOrTestBlocks( _buildingBlock );

				if( theCandidates.isEmpty() ){

					_context.error("Error in getBlockUnderDev, no parts typed by Blocks were found underneath " + 
							_context.elInfo( _buildingBlock ) );
				} else {

					if( theCandidates.size() > 1 ){

						final IRPModelElement theChosenBlockEl = 
								_context.launchDialogToSelectElement(
										theCandidates, 
										theMsg, 
										true ); 

						if( theChosenBlockEl != null && 
								theChosenBlockEl instanceof IRPClass ){

							_chosenBlock = (IRPClass) theChosenBlockEl;
						}
					} else {
						_chosenBlock = (IRPClass) theCandidates.get( 0 );
					}
				}
			}
		}

		return _chosenBlock;
	}
	
	public IRPClass getBuildingBlock(){
		
		if( _buildingBlock == null && _contextEl != null ){
			
			try {
				
				IRPModelElement elementInTag = _settings.getElementNamedInFunctionalPackageTag(
						_contextEl, 
						tagNameForAssemblyBlockUnderDev );
				
				_context.info( "Element named in " + tagNameForAssemblyBlockUnderDev + " is " + _context.elInfo( elementInTag ) );

				if( elementInTag != null && elementInTag instanceof IRPClass ){
					_buildingBlock = (IRPClass)elementInTag;
				}

			} catch (Exception e) {
				_context.error( "Exception in getBuildingBlock, " +
						"while trying to get " + tagNameForAssemblyBlockUnderDev );
			}
		}

		return _buildingBlock;
	}

	private List<IRPModelElement> getNonActorOrTestBlocks(
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

	public IRPPackage getPkgThatOwnsEventsAndInterfaces(){

		IRPPackage thePackage = 
				(IRPPackage) getElementNamedInFunctionalPackageTag(
						tagNameForPackageForEventsAndInterfaces );

		return thePackage;
	}

	public IRPModelElement getElementNamedInFunctionalPackageTag(
			String theTagName ){

		IRPModelElement theEl = null;

		IRPModelElement theSettingsPkg = 
				getSimulationSettingsPackageBasedOn( _contextEl );

		if( theSettingsPkg != null ){
			IRPTag theTag = theSettingsPkg.getTag( theTagName );

			if( theTag != null ){

				//call getValueSpecifications() to retrieve tag value collection

				IRPCollection valSpecs = theTag.getValueSpecifications();

				@SuppressWarnings("rawtypes")
				Iterator looper = valSpecs.toList().iterator();

				//call getValue() to retrieve each element instance set as the tag value

				while( looper.hasNext() ){

					IRPInstanceValue ins = (IRPInstanceValue)looper.next();
					theEl = ins.getValue();
					break;
				}
			}
		}

		if( theEl == null ){
			_context.error( "Error in getElementNamedInFunctionalPackageTag, " + 
					"unable to find value for tag called " + theTagName + " under " + 
					_context.elInfo( _contextEl ) );
		}

		return theEl;
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
				_context.warning( "Warning in getSimulationSettingsPackageBasedOn, unable to find use case settings package");

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

			_context.debug("Recursing to look at owner of " + _context.elInfo( theContextEl ) );

			// recurse
			theSettingsPkg = getSimulationSettingsPackageBasedOn(
					theContextEl.getOwner() );
		}

		return theSettingsPkg;
	}
	
	public void bleedColorToElementsRelatedTo(
			List<IRPRequirement> theSelectedReqts ){
		
		IRPGraphElement theSelectedGraphEl = getSelectedGraphEl();
		
		// only bleed on activity diagrams		
		if( theSelectedGraphEl != null &&
				theSelectedGraphEl.getDiagram() instanceof IRPActivityDiagram ){
			
			for( IRPGraphElement theGraphEl : _selectedGraphEls ) {
				bleedColorToElementsRelatedTo( theGraphEl, theSelectedReqts );
			}
		}
	}

	private void bleedColorToElementsRelatedTo(
			IRPGraphElement theGraphEl,
			List<IRPRequirement> theSelectedReqts ){
		
		String theColorSetting = "255,0,0";
		IRPDiagram theDiagram = theGraphEl.getDiagram();
		IRPModelElement theEl = theGraphEl.getModelObject();
		
		if( theEl != null ){
								
			_context.debug("Setting color to red for " + theEl.getName());
			theGraphEl.setGraphicalProperty("ForegroundColor", theColorSetting);
			
			@SuppressWarnings("unchecked")
			List<IRPDependency> theExistingDeps = theEl.getDependencies().toList();
			
			for (IRPDependency theDependency : theExistingDeps) {
				
				IRPModelElement theDependsOn = theDependency.getDependsOn();
				
				if (theDependsOn != null && 
					theDependsOn instanceof IRPRequirement && 
					theSelectedReqts.contains( theDependsOn )){	
					
					bleedColorToGraphElsRelatedTo( theDependsOn, theColorSetting, theDiagram );
					bleedColorToGraphElsRelatedTo( theDependency, theColorSetting, theDiagram );
				}
			}
		}
	}

	private void bleedColorToGraphElsRelatedTo(
			IRPModelElement theEl, 
			String theColorSetting, 
			IRPDiagram onDiagram){

		@SuppressWarnings("unchecked")
		List<IRPGraphElement> theGraphElsRelatedToElement = 
				onDiagram.getCorrespondingGraphicElements( theEl ).toList();
		
		for (IRPGraphElement irpGraphElement : theGraphElsRelatedToElement) {
			
			irpGraphElement.setGraphicalProperty("ForegroundColor", theColorSetting);
			
			IRPModelElement theModelObject = irpGraphElement.getModelObject();
			
			if (theModelObject != null){
				_context.debug("Setting color to red for " + theModelObject.getName());
			}
		}
	}
	
	public IRPPackage getPackageForBlocks(){

		IRPPackage thePackage = _settings.getPkgNamedInFunctionalPackageTag(
				_contextEl, 
				tagNameForPackageForBlocks );
		
		return thePackage;
	}
}

/**
 * Copyright (C) 2019-2021  MBSE Training and Consulting Limited (www.executablembse.com)

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