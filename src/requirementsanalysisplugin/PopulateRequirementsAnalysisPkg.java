package requirementsanalysisplugin;

import generalhelpers.CreateGatewayProjectPanel;

import java.util.List;

import javax.swing.JDialog;
import javax.swing.JOptionPane;

import com.mbsetraining.sysmlhelper.common.PopulatePkg;
import com.mbsetraining.sysmlhelper.common.UserInterfaceHelper;
import com.mbsetraining.sysmlhelper.executablembse.ExecutableMBSE_Context;
import com.telelogic.rhapsody.core.*;

public class PopulateRequirementsAnalysisPkg extends PopulatePkg {
		
	public PopulateRequirementsAnalysisPkg(
			ExecutableMBSE_Context context ) {
		
		super(context);
	}
	
	public void displayGraphicalPropertiesFor(IRPGraphElement theGraphEl){
		
		@SuppressWarnings("unchecked")
		List<IRPGraphicalProperty> theGraphProperties = theGraphEl.getAllGraphicalProperties().toList();
		
		for (IRPGraphicalProperty theGraphicalProperty : theGraphProperties) {
			String thePropertyname = theGraphicalProperty.getKey();
			String theValue = theGraphicalProperty.getValue();
			
			_context.debug(thePropertyname + "=" + theValue);
		}
	}
	
	public void createRequirementsAnalysisPkg(){
		
		final String rootPackageName = "RequirementsAnalysisPkg";
		Boolean ok = true;
		
		IRPModelElement theExistingPkg = _context.get_rhpPrj().findElementsByFullName(rootPackageName, "Package");
		
		if (theExistingPkg != null){
			_context.info("Doing nothing: " + _context.elInfo( _context.get_rhpPrj() ) + " already has package called " + rootPackageName);
			ok = false;
		}
		
		if (ok) {
			
		    JDialog.setDefaultLookAndFeelDecorated(true);
		    
		    int response = JOptionPane.showConfirmDialog(null, 
		    		"This SysML-Toolkit helper is designed to set up a new Rhapsody project for simple activity\n" +
		    		"based use case analysis. It creates a nested package structure and use case diagram, imports\n" +
		    		"the appropriate profiles if not present, and sets default display and other options to \n" +
		    		"appropriate values for the task using Rhapsody profile and property settings.\n\n" +
		    		"Do you want to proceed?", "Confirm",
		        JOptionPane.YES_NO_OPTION, JOptionPane.QUESTION_MESSAGE);
		    
		    if (response == JOptionPane.YES_OPTION) {
		    			
				boolean theAnswer = UserInterfaceHelper.askAQuestion(
						"Do you initially want to simplify the 'Add New' menu for just\n" + 
				        "use case and requirements analysis?");
		    	
		    	populateRequirementsAnalysisPkg();
				
				if (theAnswer==true){
					applySimpleMenuStereotype();
				}
				
				CreateGatewayProjectPanel.launchThePanel( 
						_context.get_rhpAppID(),
						"^RequirementsAnalysisPkg.rqtf$" );
				
		    } else {
		    	_context.debug("Cancelled by user");
		    }
		}
	}
	
	public IRPPackage populateRequirementsAnalysisPkg() {
		
		addProfileIfNotPresent("SysML");		
		addProfileIfNotPresent("GlobalPreferencesProfile");
		addProfileIfNotPresent("RequirementsAnalysisProfile");
		
		_context.get_rhpPrj().changeTo("SysML");
		
		IRPPackage theRequirementsAnalysisPkg = 
				addPackageFromProfileRpyFolder(
						"RequirementsAnalysisPkg", false );
		
		if( theRequirementsAnalysisPkg != null ){
			
			_context.info( _context.elInfo( theRequirementsAnalysisPkg ) + " was successfully copied from the SysMLHelper profile" );
					
			_context.deleteIfPresent( "Structure1", "StructureDiagram", _context.get_rhpPrj() );
			_context.deleteIfPresent( "Default", "Package", _context.get_rhpPrj() );
				    	
	    	_context.setPropertiesValuesRequestedInConfigFile( 
	    			_context.get_rhpPrj(),
	    			"setPropertyForRequirementsAnalysisModel" );
	    			
	    	_context.get_rhpPrj().save();
			
			@SuppressWarnings("unchecked")
			List<IRPUseCaseDiagram> theUCDs = theRequirementsAnalysisPkg.getNestedElementsByMetaClass("UseCaseDiagram", 1).toList();
			
			for (IRPUseCaseDiagram theUCD : theUCDs) {
				_context.debug( _context.elInfo( theUCD ) + " was added to the project");
				
				String oldName = theUCD.getName();
				String newName = oldName.replaceAll("ProjectName", _context.get_rhpPrj().getName());
				
				if (!newName.equals( oldName )){
					_context.debug("Renaming " + oldName + " to " + newName);
					theUCD.setName( newName );
				}
				
				theUCD.highLightElement();
				theUCD.openDiagram();
			}	

		} else {
			_context.error("Error in createRequirementsAnalysisPkg, unable to add RequirementsAnalysisPkg package");
		}
		
		return theRequirementsAnalysisPkg;
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

