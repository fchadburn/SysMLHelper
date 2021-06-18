package functionalanalysisplugin;

import javax.swing.JDialog;
import javax.swing.JOptionPane;

import requirementsanalysisplugin.PopulateRequirementsAnalysisPkg;
import sysmlhelperplugin.SysMLHelper_Context;

import com.mbsetraining.sysmlhelper.common.UserInterfaceHelper;
import com.mbsetraining.sysmlhelper.executablembse.PopulatePkg;
import com.mbsetraining.sysmlhelper.gateway.CreateGatewayProjectPanel;
import com.telelogic.rhapsody.core.*;

public class PopulateFunctionalAnalysisPkg extends PopulatePkg {
	
	public enum SimulationType {
	    FullSim, SimpleSim, NoSim
	}
	
	public PopulateFunctionalAnalysisPkg(
			SysMLHelper_Context context ){
		
		super( context );
	}
	
	public void createFunctionalAnalysisPkg(
			final SimulationType withSimulationType ){
		 
		final String rootPackageName = "FunctionalAnalysisPkg";
		Boolean ok = true;
		
		IRPProject forProject = _context.get_rhpPrj();
		
		IRPModelElement theExistingPkg = forProject.findElementsByFullName(rootPackageName, "Package");
		
		if (theExistingPkg != null && theExistingPkg instanceof IRPPackage ){
			
	    	boolean answer = UserInterfaceHelper.askAQuestion( 
	    			"This project already has a " + _context.elInfo( theExistingPkg ) + ". \n" +
	    			"Do you want to create a " + withSimulationType + " package stucture underneath it?");
	    	
	    	if( answer==true ){
		    	createFunctionalBlockPackageHierarchy( (IRPPackage)theExistingPkg, withSimulationType );
	    	}

	    	ok = false;
		}
		
		if (ok) {
			
		    JDialog.setDefaultLookAndFeelDecorated(true);
		    
		    String introText;
		    
		    if( withSimulationType==SimulationType.NoSim){
			    introText = 
			    		"This SysML-Toolkit helper is designed to set up a new Rhapsody project for MBSE. \n" +
			    		"It creates a nested package structure for simple functional analysis, imports the \n" +
			    		"appropriate profiles if not present, and sets default display and other options \n" +
			    		"to appropriate values for the task using Rhapsody profile and property settings. \n" +
			    		"This will remove the SimpleMenu stereotype if applied.\n\n" +
			    		"Do you want to proceed?";		    	
		    } else {
			    introText = 
			    		"This SysML-Toolkit helper is designed to set up a new Rhapsody project for executable MBSE. \n" +
			    		"It creates a nested package structure for executable 'state-based functional analysis',  \n" +
			    		"imports the appropriate profiles if not present, and sets default display and other options \n" +
			    		"to appropriate values for the task using Rhapsody profile and property settings. \n" +
			    		"This will remove the SimpleMenu stereotype if applied.\n\n" +
			    		"Do you want to proceed?";
		    }
		    
		    int response = JOptionPane.showConfirmDialog(
		    		null, 
		    		introText, 
		    		"Confirm",
		    		JOptionPane.YES_NO_OPTION,
		    		JOptionPane.QUESTION_MESSAGE);
		    
		    if (response == JOptionPane.YES_OPTION) {
		    	
		    	IRPModelElement theRequirementsAnalysisPkg = 
		    			forProject.findElementsByFullName("RequirementsAnalysisPkg", "Package");
		    	
		    	if (theRequirementsAnalysisPkg != null){
		    		
			    	populateFunctionalAnalysisPkg(forProject, withSimulationType);
			    	removeSimpleMenuStereotypeIfPresent();
			    	
			    	forProject.save();
			    	
		    	} else { // theRequirementsanalysisPkg == null
		    		
				    int confirm = JOptionPane.showConfirmDialog(null, 
				    		"The project does not contain a root RequirementsAnalysisPkg. This package is used by the\n" +
				    		"plugin/method to populate the actors for functional analysis simulation purposes and/or \n" +
				    	    "higher-level requirements for traceability purposes. \n\n" +
				    		"Do you want to add a RequirementsAnalysisPkg.sbs from another model by reference?\n\n" + 
				    		"NOTE:\n" +
				    		"The recommendation is to create a folder that will contain both this project and its\n" +
				    		"referenced projects to treat them as a consistent project set. If you haven't done this\n" +
				    		"yet then consider cancelling and doing this first. The unit will be added by relative \n"+
				    		"path, hence locating the models in a common root folder is recommended to enable \n"+
				    		"sharing across file systems as a consistent set of projects.\n\n " + 
				    		"Clicking 'Yes' will allow you to select a RequirementsAnalysisPkg by reference.\n\n" +
				    		"Clicking 'No' will create a RequirementsAnalysisPkg structure as the starting point in \n" + 
				    		"this project (so you can import higher-level requirements and define actors). You will \n" +
				    		"then be able to re-run FunctionalAnalysisPkg creation once the actors and use case \n" +
				    		"context have been defined. \n\n"+
				    		"Clicking 'Cancel' will do nothing.\n\n",
				    		"Confirm choice",
				        JOptionPane.YES_NO_CANCEL_OPTION, JOptionPane.QUESTION_MESSAGE);
				    
				    if (confirm == JOptionPane.YES_OPTION){
				    	browseAndAddByReferenceIfNotPresent("RequirementsAnalysisPkg", true);
				    	
				    	populateFunctionalAnalysisPkg(forProject, withSimulationType);
				    	removeSimpleMenuStereotypeIfPresent();
				    	forProject.save();
				    	
				    } else if (confirm == JOptionPane.NO_OPTION){
					    
				    	PopulateRequirementsAnalysisPkg thePopulator = new PopulateRequirementsAnalysisPkg( (SysMLHelper_Context) _context );
				    	thePopulator.populateRequirementsAnalysisPkg();		
						CreateGatewayProjectPanel.launchThePanel( _context.get_rhpAppID(), "^RequirementsAnalysisPkg.rqtf$" );
							    
				    } else {
				    	_context.info("Cancelled by user");
				    }
		    	}

		    } else {
		    	_context.info("Cancelled by user");
		    }
		}
	}
		
	public void populateFunctionalAnalysisPkg(
			IRPProject forProject, 
			final SimulationType withSimulationType ) {
		
		super.addProfileIfNotPresent("SysML");		
		super.addProfileIfNotPresent("GlobalPreferencesProfile");
		super.addProfileIfNotPresent("RequirementsAnalysisProfile");
		
		IRPPackage theFunctionalAnalysisPkg = forProject.addPackage( "FunctionalAnalysisPkg" );
		
		if( withSimulationType==SimulationType.FullSim ){		

			addProfileIfNotPresentAndMakeItApplied("FunctionalAnalysisProfile", theFunctionalAnalysisPkg);
			addProfileIfNotPresentAndMakeItApplied("DesignSynthesisProfile", theFunctionalAnalysisPkg);
		
		} else { // withSimulationType==SimulationType.SimpleSim || withSimulationType==SimulationType.NoSim
			
			addProfileIfNotPresentAndMakeItApplied("FunctionalAnalysisSimpleProfile", theFunctionalAnalysisPkg);
			addProfileIfNotPresentAndMakeItApplied("DesignSynthesisProfile", theFunctionalAnalysisPkg);
		}
		
		if( theFunctionalAnalysisPkg != null ){
			
			addPackageFromProfileRpyFolder( "BasePkg", true );
		
			_context.deleteIfPresent( "Structure1", "StructureDiagram", forProject );
			_context.deleteIfPresent( "Default", "Package", forProject );
	    		    	
	    	_context.setPropertiesValuesRequestedInConfigFile( 
	    			forProject,
	    			"setPropertyForFunctionalAnalysisModel" );
	    		    	
	    	createFunctionalBlockPackageHierarchy( theFunctionalAnalysisPkg, withSimulationType );
		}
	}
	
	public void createFunctionalBlockPackageHierarchy(
			IRPPackage theRootPackage, 
			final SimulationType withSimulationType ){
		
		if (theRootPackage.getName().equals("FunctionalAnalysisPkg")){
			
			IRPPackage theRequirementsAnalysisPkg = 
					(IRPPackage) theRootPackage.getProject().findElementsByFullName(
							"RequirementsAnalysisPkg", "Package");
			
			if (theRequirementsAnalysisPkg == null && 
					withSimulationType==SimulationType.FullSim){
				
				JDialog.setDefaultLookAndFeelDecorated(true);
				
				JOptionPane.showMessageDialog(
						null,  
			    		"Unable to do functional block creation as this only works if the project contains a RequirementsAnalysisPkg.",
			    		"Information",
			    		JOptionPane.INFORMATION_MESSAGE);
			} else {
				
				CreateFunctionalBlockPackagePanel.launchThePanel(
						theRootPackage, 
						theRequirementsAnalysisPkg, 
						withSimulationType,
						_context );
			}
		    
		} else {
		    JDialog.setDefaultLookAndFeelDecorated(true);
		    
		    JOptionPane.showMessageDialog(
		    		null,  
		    		"This operation only works if you right-click the FunctionalAnalysisPkg.",
		    		"Warning",
		    		JOptionPane.WARNING_MESSAGE);	    
		}
	}
	
	public void addNewActorToPackageUnderDevelopement(
			IRPModelElement theSelectedEl ){
		
		final String rootPackageName = "FunctionalAnalysisPkg";
		
		IRPProject theProject = theSelectedEl.getProject();
		
		final IRPModelElement theRootPackage = 
				theProject.findElementsByFullName( rootPackageName, "Package" );
		
		if( theRootPackage != null ){
			CreateNewActorPanel.launchThePanel( _context.get_rhpAppID() );
		}
	}
	
	public void switchFunctionalAnalysisPkgProfileFrom(
			String theProfileName,
			String toTheProfileName ) {
		
		final String rootPackageName = "FunctionalAnalysisPkg";

		IRPModelElement theFunctionalAnalysisPkg = 
				_context.get_rhpPrj().findElementsByFullName( rootPackageName, "Package" );
		
		if( theFunctionalAnalysisPkg==null ){
			
			_context.info( "Doing nothing: " + _context.elInfo( _context.get_rhpPrj() ) + " does not have a " + rootPackageName );
		} else {

			String infoMsg =  "Do you want to change the menus by replacing the profile called '" + theProfileName + "' \n " +
					 		  "with the profile called '" + toTheProfileName + "'?" + "\n\n" +
					          "Note: After running this you will need to close Rhapsody completely and re-open the project for the new menus to appear.";
			
			boolean result = UserInterfaceHelper.askAQuestion( infoMsg );
			
			if( result==true ){
				
				addProfileIfNotPresentAndMakeItApplied( toTheProfileName, (IRPPackage)theFunctionalAnalysisPkg );
				
				IRPModelElement theProfileToDelete = _context.get_rhpPrj().findAllByName( theProfileName, "Profile" );
				
				if( theProfileToDelete==null ){
					
					_context.warning("Unable to find a profile called " + theProfileName + " to delete");
				} else {
					theProfileToDelete.deleteFromProject();
				}
			}
		}
	}
	
	public void switchToMoreDetailedAD(
			IRPActivityDiagram theDiagram ) {
		
		final String theStereotypeName = "MoreDetailedAD";
		
		if( _context.hasStereotypeCalled( theStereotypeName, theDiagram ) ){
			
			_context.info( "Doing nothing as diagram already has the stereotype «" + theStereotypeName + "» applied." );
		
		} else {
			
			theDiagram.addStereotype( theStereotypeName, "ActivityDiagramGE" );
			IRPFlowchart theFC = theDiagram.getFlowchart();
			theFC.setIsAnalysisOnly( 1 );
			
			if( theDiagram.isOpen()==1 ){
				theDiagram.closeDiagram();
			}
			
			_context.info( "Applied stereotype «" + theStereotypeName + "» to " + 
					_context.elInfo( theDiagram ) + " to add additional tools to the toolbar" );
			
			setProperty( theDiagram.getFlowchart(), "Activity_diagram.AcceptEventAction.ShowNotation", "Event" );
			setProperty( theDiagram.getFlowchart(), "Activity_diagram.SendAction.ShowNotation", "Event" );		
		}
	}

	public void addNewBlockPartToPackageUnderDevelopement(
			IRPModelElement theSelectedEl ){
		
		final String rootPackageName = "FunctionalAnalysisPkg";
		
		IRPProject theProject = theSelectedEl.getProject();
		
		final IRPModelElement theRootPackage = 
				theProject.findElementsByFullName( rootPackageName, "Package" );
		
		if( theRootPackage != null ){
			CreateNewBlockPartPanel.launchThePanel( _context.get_rhpAppID() );
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