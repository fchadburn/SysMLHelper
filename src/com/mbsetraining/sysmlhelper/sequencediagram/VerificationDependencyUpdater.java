package com.mbsetraining.sysmlhelper.sequencediagram;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

import com.mbsetraining.sysmlhelper.common.BaseContext;
import com.telelogic.rhapsody.core.*;

public class VerificationDependencyUpdater {	

	protected BaseContext _context;
	
	public VerificationDependencyUpdater(
			BaseContext context ) {

		_context = context;
	}

	public void updateVerificationsForSequenceDiagramsBasedOn(
			List<IRPModelElement> theSelectedEls ){

		Set<IRPModelElement> theEls = 
				buildSetOfElementsFor( theSelectedEls, "SequenceDiagram", true );

		for( IRPModelElement theEl : theEls ){
			updateVerificationsFor( (IRPSequenceDiagram)theEl );
		}
	}
	
	protected void updateVerificationsFor(
			IRPDiagram theDiagram ){

		Set<IRPRequirement> theReqtsOnDiagram = buildSetOfRequirementsAlreadyOn( theDiagram );

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

	private Set<IRPModelElement> buildSetOfElementsFor(
			List<IRPModelElement> theSelectedEls, 
			String withMetaClass, 
			boolean isRecursive) {

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

	private Set<IRPRequirement> buildSetOfRequirementsAlreadyOn(
			IRPDiagram theDiagram){

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

