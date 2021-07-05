package designsynthesisplugin;

import javax.swing.JDialog;
import javax.swing.JOptionPane;

import com.mbsetraining.sysmlhelper.executablembse.PopulatePkg;
import com.mbsetraining.sysmlhelper.sysmlhelperplugin.SysMLHelper_Context;
import com.telelogic.rhapsody.core.*;

public class PopulateDesignSynthesisPkg extends PopulatePkg {

	public PopulateDesignSynthesisPkg(
			SysMLHelper_Context context ){

		super( context );
	}

	public void createDesignSynthesisPkg(){
		
		final String rootPackageName = "DesignSynthesisPkg";
		Boolean ok = true;
		
		IRPModelElement theExistingPkg = _context.get_rhpPrj().findElementsByFullName( rootPackageName, "Package" );
		
		if (theExistingPkg != null){
			_context.info("Doing nothing: " + _context.elInfo( _context.get_rhpPrj() ) + " already has package called " + rootPackageName );
			ok = false;
		}
		
		if (ok) {
			
		    JDialog.setDefaultLookAndFeelDecorated(true);
		    
		    int response = JOptionPane.showConfirmDialog(null, 
		    		"This SysML-Toolkit helper is designed to set up a new Rhapsody project for executable MBSE. \n" +
		    		"It creates a nested package structure for executable 'interaction-based' design synthesis,  \n" +
		    		"imports the appropriate profiles if not present, and sets default display and other options \n" +
		    		"to appropriate values for the task using Rhapsody profile and property settings.\n\n" +
		    		"Do you want to proceed?", "Confirm",
		        JOptionPane.YES_NO_OPTION, JOptionPane.QUESTION_MESSAGE);
		    
		    if (response == JOptionPane.YES_OPTION) {
		    	
		    	browseAndAddByReferenceIfNotPresent( "RequirementsAnalysisPkg", true );
		    	browseAndAddByReferenceIfNotPresent( "FunctionalAnalysisPkg", true );
		    	populateDesignSynthesisPkg();
		    	removeSimpleMenuStereotypeIfPresent();
		    	
		    	_context.get_rhpPrj().save();

		    } else {
		    	_context.debug("Cancelled by user");
		    }
		}
	}
	
	public void populateDesignSynthesisPkg(){
		
		addProfileIfNotPresent("SysML");		
		addProfileIfNotPresent("GlobalPreferencesProfile");
		addProfileIfNotPresent("RequirementsAnalysisProfile");
		addProfileIfNotPresent("FunctionalAnalysisProfile");
		addProfileIfNotPresent("DesignSynthesisProfile");
		
		_context.get_rhpPrj().changeTo("SysML");
		
		IRPPackage theDesignSynthesisPkg = 
				addPackageFromProfileRpyFolder(
						"DesignSynthesisPkg", false );
		
		if (theDesignSynthesisPkg != null){
		
			_context.deleteIfPresent( "Structure1", "StructureDiagram", _context.get_rhpPrj() );
			_context.deleteIfPresent( "Default", "Package", _context.get_rhpPrj() );
	    	
	    	setProperty( _context.get_rhpPrj(), "Browser.Settings.ShowPredefinedPackage", "True" );
	    	setProperty( _context.get_rhpPrj(), "General.Model.AutoSaveInterval", "5" );
	    	setProperty( _context.get_rhpPrj(), "General.Model.HighlightElementsInActiveComponentScope", "True" );
	    	setProperty( _context.get_rhpPrj(), "General.Model.ShowModelTooltipInGE", "Simple" );
	    	setProperty( _context.get_rhpPrj(), "General.Model.BackUps", "One" );
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
