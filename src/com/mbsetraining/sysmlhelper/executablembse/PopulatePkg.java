package com.mbsetraining.sysmlhelper.executablembse;

import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;

import javax.swing.JFileChooser;
import javax.swing.filechooser.FileNameExtensionFilter;

import com.mbsetraining.sysmlhelper.common.ConfigurationSettings;
import com.telelogic.rhapsody.core.*;

public class PopulatePkg {

	protected ConfigurationSettings _context;
	
	public PopulatePkg(
			ConfigurationSettings context ){
		
		_context = context;
	}
	
	final private static String _simpleMenuStereotypeName = "SimpleMenu";

	public IRPProfile addProfileIfNotPresent(
			String theProfileName ){
		
		IRPProfile theProfile = (IRPProfile) _context.get_rhpPrj().
				findNestedElement( theProfileName, "Profile" );
		
		if( theProfile == null ){

			IRPUnit theUnit = _context.get_rhpApp().addProfileToModel( theProfileName );
			
			if( theUnit != null ){
				
				theProfile = (IRPProfile)theUnit;
				_context.info( "Added profile called " + theProfile.getFullPathName() );
				
			} else {
				_context.error( "Error in addProfileIfNotPresent. No profile found with name " + theProfileName );
			}
			
		} else {
			_context.debug( _context.elInfo( theProfile ) + " is already present in the project" );
		}
		
		return theProfile;		
	}
	
	public IRPProfile addProfileIfNotPresentAndMakeItApplied(
			String theProfileName, 
			IRPPackage appliedToPackage ){
				
		IRPProfile theProfile = addProfileIfNotPresent( theProfileName );
		IRPDependency theDependency = appliedToPackage.addDependencyTo( theProfile );
		theDependency.addStereotype("AppliedProfile", "Dependency");
		
		return theProfile;			
	}
	
	public void setProperty(IRPModelElement onTheEl, String withKey, String toValue){
		
		_context.info( "Setting " + withKey + " property on " + _context.elInfo( onTheEl ) + " to " + toValue );
		onTheEl.setPropertyValue( withKey, toValue );
	}	
	
	public void browseAndAddByReferenceIfNotPresent(
			String thePackageName, 
			boolean relative ){
		
    	IRPModelElement theExistingPkg = _context.get_rhpPrj().findElementsByFullName( thePackageName, "Package" );
    	
    	if( theExistingPkg == null ){
    		
    		JFileChooser theFileChooser = new JFileChooser( System.getProperty("user.dir") );
    		theFileChooser.setFileFilter( new FileNameExtensionFilter( "Package", "sbs" ) );
    		
    		int choice = theFileChooser.showDialog( null, "Choose " + thePackageName );
    		
    		if( choice==JFileChooser.CANCEL_OPTION ){
    			_context.debug( "Operation cancelled by user when trying to choose " + thePackageName );
    			
    		} else if( choice==JFileChooser.APPROVE_OPTION ){
    			
    			File theFile = theFileChooser.getSelectedFile();
    			
    			String theTargetPath;
    			
				try {
					theTargetPath = theFile.getCanonicalPath();
					
		  			_context.get_rhpApp().addToModelByReference( theTargetPath );
	    			
	    			if( relative ){
	    				
	        			int trimSize = thePackageName.length()+5;
	        			
	        			Path targetPath = Paths.get( theTargetPath.substring(0, theTargetPath.length()-trimSize) );
	        			Path targetRoot = targetPath.getRoot();
	        			
	        			Path sourcePath = Paths.get( 
	        					_context.get_rhpPrj().getCurrentDirectory().replaceAll(
	        							_context.get_rhpPrj().getName()+"$", "") );
	        			
	        			Path sourceRoot = sourcePath.getRoot();
	        			
	        			if( !targetRoot.equals( sourceRoot ) ){
	        				_context.warning( "Unable to set Unit called " + thePackageName + " to relative, as the drive letters are different" );
	        				_context.warning( "theTargetDir root =" + targetPath.getRoot() );
	        				_context.warning( "theTargetDir=" + targetPath );
	        				_context.warning( "theSourceDir root =" + sourcePath.getRoot() );
	        				_context.warning( "theSourceDir=" + sourcePath );
	        			} else {
		        			Path theRelativePath = sourcePath.relativize( targetPath );
		        		     
		        			IRPModelElement theCandidate = _context.get_rhpPrj().findAllByName( thePackageName, "Package" );
		        			
		        			if( theCandidate != null && theCandidate instanceof IRPPackage ){
		        				
		        				IRPPackage theAddedPackage = (IRPPackage)theCandidate;
		        				
		        				theAddedPackage.setUnitPath( "..\\..\\" + theRelativePath.toString() );
		        				
		        				_context.info( "Unit called " + thePackageName + 
		        						".sbs was changed from absolute path='" + theTargetPath + 
		        						"' to relative path='" + theRelativePath + "'" );
		        			}
	        			}
	    			}
				} catch( IOException e ){
					_context.error("Error, unhandled IOException in PopulatePkg.browseAndAddByReferenceIfNotPresent" );
				}
    		}
    	}
	}
	
	public IRPPackage addPackageFromProfileRpyFolder(
			String withTheName,
			boolean byReference ){
				
		if( byReference ){
			_context.get_rhpApp().addToModelByReference(
					"$OMROOT\\Profiles\\SysMLHelper\\SysMLHelper_rpy\\" + withTheName + ".sbsx" );				
			
		} else {
			_context.get_rhpApp().addToModel(
					"$OMROOT\\Profiles\\SysMLHelper\\SysMLHelper_rpy\\" + withTheName + ".sbsx", 1);				
		}
		
		IRPPackage thePackage = (IRPPackage) _context.get_rhpPrj().findElementsByFullName( withTheName, "Package" );
				
		return thePackage;
	}
	
	protected void applySimpleMenuStereotype() {
		
		IRPModelElement theEl = _context.get_rhpPrj().findAllByName( _simpleMenuStereotypeName, "Stereotype" );
		
		if (theEl != null && theEl instanceof IRPStereotype){
			IRPStereotype theStereotype = (IRPStereotype)theEl;
			
			_context.get_rhpPrj().setStereotype( theStereotype );
			_context.get_rhpPrj().changeTo( "SysML" );
			
			_context.info( _context.elInfo( _context.get_rhpPrj() ) + " was changed to " + _simpleMenuStereotypeName );
			_context.info( "Remove the «" + _simpleMenuStereotypeName + "» stereotype to return the 'Add New' menu" );

		} else {
			_context.error( "Error in applySimpleMenuStereotype, unable to find stereotype called " + _simpleMenuStereotypeName );
		}	
	}
	
	protected void removeSimpleMenuStereotypeIfPresent(){	
		
		IRPModelElement theEl = _context.get_rhpPrj().findAllByName( _simpleMenuStereotypeName, "Stereotype" );
		
		if (theEl != null && theEl instanceof IRPStereotype){
			IRPStereotype theStereotype = (IRPStereotype)theEl;
			
			_context.get_rhpPrj().removeStereotype(theStereotype);
			_context.get_rhpPrj().changeTo( "SysML" );
			
			_context.info( "«" + _simpleMenuStereotypeName + "» stereotype removed from project to return the full 'Add New' menu to Rhapsody" );

		} else {
			_context.error( "Error in removeSimpleMenuStereotypeIfPresent, unable to find stereotype called " + _simpleMenuStereotypeName );
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