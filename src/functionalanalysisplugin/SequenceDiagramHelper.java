package functionalanalysisplugin;

import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import com.mbsetraining.sysmlhelper.common.ConfigurationSettings;
import com.mbsetraining.sysmlhelper.common.UserInterfaceHelper;
import com.mbsetraining.sysmlhelper.executablembse.ExecutableMBSE_Context;
import com.telelogic.rhapsody.core.*;

public class SequenceDiagramHelper {	

	protected ConfigurationSettings _context;

	public SequenceDiagramHelper(
			ConfigurationSettings context ) {

		_context = context;
	}

	public void updateVerificationsForSequenceDiagramsBasedOn(
			List<IRPModelElement> theSelectedEls){

		Set<IRPModelElement> theEls = buildSetOfElementsFor(theSelectedEls, "SequenceDiagram", true);

		for (IRPModelElement theEl : theEls) {
			updateVerificationsFor( (IRPSequenceDiagram)theEl );
		}
	}

	private Set<IRPModelElement> buildSetOfElementsFor(
			List<IRPModelElement> theSelectedEls, 
			String withMetaClass, boolean isRecursive) {

		Set<IRPModelElement> theMatchingEls = new HashSet<IRPModelElement>();

		for (IRPModelElement theSelectedEl : theSelectedEls) {

			addElementIfItMatches(withMetaClass, theMatchingEls, theSelectedEl);

			if (isRecursive){

				@SuppressWarnings("unchecked")
				List<IRPModelElement> theCandidates = theSelectedEl.getNestedElementsByMetaClass(withMetaClass, 1).toList();

				for (IRPModelElement theCandidate : theCandidates) {				
					addElementIfItMatches(withMetaClass, theMatchingEls, theCandidate);
				}
			}
		}

		return theMatchingEls;
	}

	private void addElementIfItMatches(
			String withMetaClass,
			Set<IRPModelElement> theMatchingEls, 
			IRPModelElement elementToAdd) {

		if (elementToAdd.getMetaClass().equals( withMetaClass )){

			if (elementToAdd instanceof IRPUnit){

				IRPUnit theUnit = (IRPUnit) elementToAdd;

				if (theUnit.isReadOnly()==0){
					theMatchingEls.add( elementToAdd );
				}
			} else {
				theMatchingEls.add( elementToAdd );
			}
		}
	}

	private void updateVerificationsFor(IRPDiagram theDiagram){

		Set<IRPRequirement> theReqtsOnDiagram = buildSetOfRequirementsAlreadyOn(theDiagram);

		Set<IRPRequirement> theReqtsWithVerificationRelationsToDiagram = 
				_context.getRequirementsThatTraceFromWithStereotype(
						theDiagram, "verify");

		Set<IRPRequirement> theRequirementsToRemove= new HashSet<IRPRequirement>( theReqtsWithVerificationRelationsToDiagram );
		theRequirementsToRemove.removeAll( theReqtsOnDiagram );

		if (!theRequirementsToRemove.isEmpty()){

			@SuppressWarnings("unchecked")
			List<IRPDependency> theDependencies = theDiagram.getNestedElementsByMetaClass("Dependency", 0).toList();

			for (IRPDependency theDependency : theDependencies) {

				String userDefinedMetaClass = theDependency.getUserDefinedMetaClass();

				if (userDefinedMetaClass.equals("Verification")){

					IRPModelElement dependsOn = theDependency.getDependsOn();

					if (dependsOn instanceof IRPRequirement &&
							theRequirementsToRemove.contains(dependsOn)){

						_context.debug( _context.elInfo( dependsOn ) + " removed verification link");
						theDependency.deleteFromProject();
					}

				}
			}
		}

		Set<IRPRequirement> theRequirementsToAdd = new HashSet<IRPRequirement>( theReqtsOnDiagram );
		theRequirementsToAdd.removeAll( theReqtsWithVerificationRelationsToDiagram );

		theDiagram.highLightElement();

		for (IRPRequirement theReq : theRequirementsToAdd) {
			IRPDependency theDep = theDiagram.addDependencyTo( theReq );
			theDep.changeTo("Verification");
			_context.debug( _context.elInfo( theReq ) + " added verification link");
		}
	}

	private Set<IRPRequirement> buildSetOfRequirementsAlreadyOn(IRPDiagram theDiagram){

		Set<IRPRequirement> theReqts = new HashSet<IRPRequirement>();

		@SuppressWarnings("unchecked")
		List<IRPGraphElement> theGraphEls = theDiagram.getGraphicalElements().toList();

		for (IRPGraphElement theGraphEl : theGraphEls) {

			if (theGraphEl instanceof IRPGraphNode){

				IRPModelElement theModelObject = theGraphEl.getModelObject();

				if (theModelObject instanceof IRPRequirement){

					IRPRequirement theReqt = (IRPRequirement)theModelObject;
					theReqts.add( theReqt );
				}
			}
		}

		return theReqts;
	}

	public void updateLifelinesToMatchPartsInActiveBuildingBlock(
			IRPSequenceDiagram theSequenceDiagram ){

		FunctionalAnalysisSettings theSettings = new FunctionalAnalysisSettings(_context);
		
		IRPClass theBuildingBlock = 
				theSettings.getBuildingBlock( theSequenceDiagram );

		if( theBuildingBlock != null ){

			createSequenceDiagramFor(
					theBuildingBlock, 
					(IRPPackage) theSequenceDiagram.getOwner(), 
					theSequenceDiagram.getName() );

		} else {
			_context.error("Error, unable to find building block or tester pkg");
		}
	}

	public void updateAutoShowSequenceDiagramFor(
			IRPClass theAssemblyBlock) {

		FunctionalAnalysisSettings theSettings = new FunctionalAnalysisSettings(_context);

		IRPPackage thePackageForSD = 
				theSettings.getPackageForActorsAndTest(
						theAssemblyBlock.getProject() );

		if( thePackageForSD != null ){

			List<IRPModelElement> theSDs = 
					_context.findElementsWithMetaClassAndStereotype(
							"SequenceDiagram", 
							"AutoShow", 
							thePackageForSD, 
							0 );

			if( theSDs.size()==1 ){

				IRPSequenceDiagram theSD = (IRPSequenceDiagram) theSDs.get( 0 );

				createSequenceDiagramFor(
						theAssemblyBlock, 
						thePackageForSD, 
						theSD.getName() );
			}
		}
	}

	public void createSequenceDiagramFor(
			IRPClass theAssemblyBlock, 
			IRPPackage inPackage,
			String withName ){

		boolean isCreateSD = true;

		IRPModelElement theExistingDiagram = 
				inPackage.findNestedElement( withName, "SequenceDiagram" );

		@SuppressWarnings("unchecked")
		List<IRPInstance> theParts =
		theAssemblyBlock.getNestedElementsByMetaClass( "Part", 0 ).toList();

		if( theExistingDiagram != null ){

			String theMsg = _context.elInfo( theExistingDiagram ) + " already exists in " + 
					_context.elInfo( inPackage ) + "\nDo you want to recreate it with x" + 
					theParts.size() + " lifelines for:\n";

			for( Iterator<IRPInstance> iterator = theParts.iterator(); iterator.hasNext(); ){

				IRPInstance thePart = (IRPInstance) iterator.next();
				IRPClassifier theType = thePart.getOtherClass();
				theMsg += thePart.getName() + "." + theType.getName() + 
						" (" + theType.getUserDefinedMetaClass() + ")\n"; 
			}

			isCreateSD = UserInterfaceHelper.askAQuestion( theMsg );

			if( isCreateSD ){
				theExistingDiagram.deleteFromProject();
			}
		}

		if( isCreateSD ){

			IRPSequenceDiagram theSD = inPackage.addSequenceDiagram( withName );

			int xPos = 30;
			int yPos = 0;
			int nWidth = 100;
			int nHeight = 1000;
			int xGap = 30;

			// Do Test Driver first
			for( IRPInstance thePart : theParts ) {

				if( _context.hasStereotypeCalled( "TestDriver", thePart.getOtherClass() ) && 
						_context.getIsCreateSDWithTestDriverLifeline( theSD ) ){

					//IRPClassifier theType = thePart.getOtherClass();
					theSD.addNewNodeForElement( thePart, xPos, yPos, nWidth, nHeight );
					xPos = xPos + nWidth + xGap;
				}
			}

			// Then actors
			for( IRPInstance thePart : theParts ) {

				IRPClassifier theType = thePart.getOtherClass();

				if( theType instanceof IRPActor ){
					theSD.addNewNodeForElement( thePart, xPos, yPos, nWidth, nHeight );
					xPos = xPos + nWidth + xGap;
				}
			}

			// Then components
			for( IRPInstance thePart : theParts ) {

				IRPClassifier theType = thePart.getOtherClass();

				if( !( theType instanceof IRPActor ) &&
						!_context.hasStereotypeCalled( "TestDriver", theType ) ){

					theSD.addNewNodeForElement( thePart, xPos, yPos, nWidth, nHeight );
					xPos = xPos + nWidth + xGap;
				}
			}

			if( _context.getIsCreateSDWithAutoShowApplied( theSD ) ){
				_context.applyExistingStereotype( "AutoShow", theSD );
			}
		}
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

