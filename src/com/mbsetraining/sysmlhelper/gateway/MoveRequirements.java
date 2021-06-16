package com.mbsetraining.sysmlhelper.gateway;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import javax.swing.JDialog;
import javax.swing.JOptionPane;

import com.mbsetraining.sysmlhelper.common.UserInterfaceHelper;
import com.mbsetraining.sysmlhelper.executablembse.ExecutableMBSE_Context;
import com.telelogic.rhapsody.core.*;
 
public class MoveRequirements {
	
	ExecutableMBSE_Context _context;
	
	public MoveRequirements(
			ExecutableMBSE_Context context ) {
		_context = context;
	}
	
	public Set<IRPModelElement> buildSetOfUnclaimedRequirementsBasedOn(
			List<IRPModelElement> theSelectedEls, 
			String theGatewayStereotypeName) {
		
		Set<IRPModelElement> theUnclaimedReqts = new HashSet<IRPModelElement>();
		
		for (IRPModelElement theSelectedEl : theSelectedEls) {
			
			IRPModelElement theElementToSearchUnder = theSelectedEl;
			
			if( theSelectedEl instanceof IRPActivityDiagram ){	
				theElementToSearchUnder = theElementToSearchUnder.getOwner().getOwner();
			}
			
			List<IRPModelElement> theReqtsToAdd = 
					_context.findModelElementsWithoutStereotypeNestedUnder( 
							theElementToSearchUnder, "Requirement", theGatewayStereotypeName );
			
			theUnclaimedReqts.addAll( theReqtsToAdd );	
		}

		return theUnclaimedReqts;
	}
	
	public void moveUnclaimedRequirementsReadyForGatewaySync(
			List<IRPModelElement> theSelectedEls, 
			IRPProject theProject ){
		
		String theGatewayStereotypeName = "from.*";
		
		Set<IRPModelElement> theUnclaimedReqts = 
				buildSetOfUnclaimedRequirementsBasedOn( 
						theSelectedEls, 
						theGatewayStereotypeName );
		
		_context.info( theUnclaimedReqts.size() + 
				" requirements unclaimed by the Gateway were found" );
		
		if( theUnclaimedReqts.isEmpty() ){
			
			String theMsg = "Nothing to do as there were no unclaimed requirements found";
		
			UserInterfaceHelper.showInformationDialog( theMsg );
			
		} else {
			
			List<IRPModelElement> thePackageEls = 
					new ArrayList<IRPModelElement>(
							_context.findModelElementsNestedUnder(
									theProject, "Package", theGatewayStereotypeName) );
			
			List<IRPModelElement> theWritablePackages = new ArrayList<>();
			
			for( IRPModelElement thePackageEl : thePackageEls ){
				
				IRPPackage thePackage = (IRPPackage)thePackageEl;
				
				if( thePackage.getIsUnresolved()==0 && 
					thePackage.isReadOnly()==0 ){
				
					theWritablePackages.add( thePackage );
				}
			}
					
			Object[] options = new Object[theWritablePackages.size()];
			
			for (int i = 0; i < options.length; i++) {
				
				IRPPackage thePackage = (IRPPackage) theWritablePackages.get(i);
				_context.debug("thePackage = " + thePackage.getFullPathNameIn());
				
//				String theOptionName =  theWritablePackages.get(i).getName() + " in " + 
//						theWritablePackages.get(i).getOwner().getFullPathName();
				options[i] = thePackage.getFullPathNameIn();
			}
			
			JDialog.setDefaultLookAndFeelDecorated(true);
			
			if (theWritablePackages.isEmpty()){
				
				String theMsg = "Nothing to do as no writeable Gateway imported packages were found.\n" +
						"Recommendation is to either:\n" +
						"a) Add high-level requirements to the model using the Gateway to create the package(s), or\n" +
						"b) Create your own package with a from<X> stereotype to minimic the Gateway, or\n" + 
						"c) Assess whether there are existing from<X> stereotyped packages that are present but not writable and correct the situation.\n";
				
				_context.info( theMsg );
				
				JOptionPane.showMessageDialog(null, theMsg);

			} else {
				JDialog.setDefaultLookAndFeelDecorated(true);
				
				Object theChoice = JOptionPane.showInputDialog(
						null,
						"Which package do you want to move the " + theUnclaimedReqts.size() + " unclaimed requirement(s) to?",
						"Based on " + theSelectedEls.size() + " selected elements",
						JOptionPane.QUESTION_MESSAGE,
						null,
						options,
						options[0]);
				
				if (theChoice == null){
					
					_context.info( "Operation was cancelled by user with no changes made." );
					
				} else {			
					
					IRPModelElement thePackage = theProject.findElementsByFullName(theChoice.toString(), "Package");
					
					IRPStereotype theStereotypeToApply = 
							_context.getStereotypeAppliedTo( thePackage, theGatewayStereotypeName );
					
					int dialogResult = JOptionPane.showConfirmDialog (
							null, "Would you like to move the " + theUnclaimedReqts.size() + " unclaimed requirements into the Package \n" 
							   + "called " + thePackage.getName() + " which has the Gateway imported stereotype \n" 
							   + "«" + theStereotypeToApply.getName() + "» applied? ",
							"Confirm", JOptionPane.YES_NO_OPTION);
					
					if( dialogResult == JOptionPane.YES_OPTION ){
						
						int count = 0;
						
						for (IRPModelElement theReqt : theUnclaimedReqts) {
							
							// check if already element of same name
							IRPModelElement alreadyExistingEl = thePackage.findNestedElement(theReqt.getName(), "Requirement");
							
							if (alreadyExistingEl != null){
								
								String uniqueName = _context.determineUniqueNameBasedOn( 
										theReqt.getName(), "Requirement", thePackage );
								
								_context.warning( "Warning: Same name as " + _context.elInfo( theReqt ) 
										+ " already exists under " + _context.elInfo(thePackage) + 
										", hence element was renamed to " + uniqueName );
								
								theReqt.setName( uniqueName );

							}

							_context.info( "Moving " + _context.elInfo( theReqt ) + " from " 
									+ _context.elInfo( theReqt.getOwner() ) + " to " + _context.elInfo( thePackage ) 
									+ " and applying " + _context.elInfo( theStereotypeToApply ) );
							
							theReqt.setOwner( thePackage );
							theReqt.addStereotype(theStereotypeToApply.getName(), "Requirement");
							count++;
							theReqt.highLightElement();

							_context.applyStereotypeToDeriveReqtDependenciesOriginatingFrom( 
									theReqt, theStereotypeToApply );
						}
						
						_context.info( "Finished (" + count + 
								" requirements were moved out of " + theUnclaimedReqts.size() + ")" );
						
					} else {
						_context.info( "Cancelled due to user choice not to continue with the move." );
					}
				}			
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

