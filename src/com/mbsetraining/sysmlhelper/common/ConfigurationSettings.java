package com.mbsetraining.sysmlhelper.common;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.Date;
import java.util.Locale;
import java.util.Properties;
import java.util.ResourceBundle;

import com.mbsetraining.sysmlhelper.executablembse.ExecutableMBSE_Context;
import com.telelogic.rhapsody.core.*;

public class ConfigurationSettings  {

	protected ResourceBundle _resources;
	protected Properties _properties;
	protected String _propertyFileName;
	protected String _resourceBundleFileName;
	protected ProfileInfo _profileInfo;

	protected BaseContext _context;

	public static void main(String[] args) {
				
		String theAppID = RhapsodyAppServer.getActiveRhapsodyApplication().getApplicationConnectionString();
		
		BaseContext _context = new ExecutableMBSE_Context( theAppID );
		_context.cleanUpModelRemnants();
	}
	
	public ConfigurationSettings(
			String thePropertyFileName,
			String theResourceBundleFileName,
			String theProfileName,
			BaseContext theContext ){

		_context = theContext;
		_propertyFileName = thePropertyFileName;
		_resourceBundleFileName = theResourceBundleFileName;
		_profileInfo = new ProfileInfo( theProfileName, _context );

		InputStream inputStream = null;

		try {
			_properties = new Properties();

			inputStream = getClass().getClassLoader().getResourceAsStream( thePropertyFileName );

			if( inputStream != null ){
				_properties.load( inputStream );
			} else {
				String theMsg = "Exception: Expected property file called '" + 
						thePropertyFileName + "' was not found in the classpath";

				_context.error( theMsg );
				throw new FileNotFoundException( theMsg );
			}

		} catch( Exception e ){
			_context.error( "Exception trying to open '" + thePropertyFileName + "': " + e.getMessage() );

		} finally {
			try {
				inputStream.close();

			} catch( IOException e ){
				_context.error( "Exception trying to close '" + thePropertyFileName + "'" );
			}
		}

		if( _properties != null ){

			String language = _properties.getProperty( "DefaultLanguage", "en" );
			String country = _properties.getProperty( "DefaultCountry", "US" );

			Locale currentLocale = new Locale( language, country );

			try {
				_resources = ResourceBundle.getBundle( 
						theResourceBundleFileName, 
						currentLocale ); 

				_context.debug( "PluginSettingsAndResources has loaded properties from " +
						thePropertyFileName + " and resource bundle from " + 
						theResourceBundleFileName + " for language=" + language + 
						" and county=" + country);

			} catch( Exception e ){
				_context.error( "Exception while trying ResourceBundle.getBundle" );
			}
		}
	}

	public String getProperty(
			String key ){

		String value = _properties.getProperty( key );
		return value;
	}

	public String getProperty(
			String key, 
			String defaultValue ){

		String value = _properties.getProperty( key, defaultValue );
		return value;
	}

	public String getString(
			String key ){

		String value = _resources.getString( key );
		return value;		
	}

	public void setPropertiesValuesRequestedInConfigFile(
			IRPModelElement onTheElement,
			String basedOnContext ){

		for( String key : _properties.stringPropertyNames() ) {

			if( key.startsWith( basedOnContext ) ){

				String thePropertyName = key.replace( basedOnContext + ".", "");
				String thePropertyValue = getProperty( key );

				_context.info( "Setting '" + thePropertyName + 
						"' to '" + thePropertyValue + "' based on '" + 
						_propertyFileName + "'" ); 

				try {
					onTheElement.setPropertyValue( 
							thePropertyName, thePropertyValue );

				} catch( Exception e ){

					_context.error( "Exception in setPropertiesValuesRequestedInConfigFile, " +
							"unable to set " + thePropertyName + " to " + thePropertyValue + 
							" on " + _context.elInfo( onTheElement ) );
				}
			}
		}
	}

	public void checkIfSetupProjectIsNeeded(
			boolean isUserDialogEnabled,
			String withNewTerm ) {

		boolean isSetupNeeded = checkIfSetupProjectIsNeeded( isUserDialogEnabled, false );

		String theMsg;

		if( isSetupNeeded ){

			theMsg = "An update is needed, do you want to proceed?";

		} else {
			theMsg = "An update is not needed, do you want to proceed with update anyway?";
		}

		boolean answer = UserInterfaceHelper.askAQuestion( theMsg );

		if( answer ){
			setupProjectWithProperties( withNewTerm );
		}
	}

	public boolean checkIfSetupProjectIsNeeded(
			boolean isShowInfoDialog,
			boolean isProvideFixingAdvice ){

		String theMsg = null;
		boolean isProfileNeedsUpdate = false;
		IRPProject thePrj = _context.get_rhpPrj();

		if( _profileInfo.isValid() ){

			IRPTag theProjectDateTag = thePrj.getTag( "ProfileDate" );
			IRPTag theProjectVersionTag = thePrj.getTag( "ProfileVersion" );

			String theProfileDateValue = _profileInfo.get_profileDateString();
			String theProfileVersionValue = _profileInfo.get_profileVersionString();

			boolean isProjectsProfileVersionSet = 
					( theProjectDateTag != null && 
					theProjectDateTag.getOwner().equals( thePrj ) ) &&
					( theProjectVersionTag != null &&
					theProjectVersionTag.getOwner().equals( thePrj ) );

			if( !isProjectsProfileVersionSet ){				

				theMsg = "The project called " + thePrj.getName() + " does not have the ProfileDate and ProfileVersion tags set \n" +
						"Your installed " + _profileInfo.get_profileName() + " profile version is " + theProfileVersionValue + 
						" (" + theProfileDateValue + ") \n";

				if( isProvideFixingAdvice ){
					theMsg += "It is recommended to right-click and run the Profile's Setup project properties command \n" +
							"to update the project to the latest profile before proceeding \n";
				}

				isProfileNeedsUpdate = true;

			} else {

				String theProjectsProfileDateValue = theProjectDateTag.getValue();
				String theProjectsProfileVersionValue = theProjectVersionTag.getValue();

				//_context.debug( "Project's required ProfileDate    = " + theProjectsProfileDateValue );
				//_context.debug( "Project's required ProfileVersion = " + theProjectsProfileVersionValue );

				Date profileDate = _profileInfo.get_profileDate();
				String profileName = _profileInfo.get_profileName();
				
				Date theProjectsProfileDate = _context.getDate( theProjectsProfileDateValue );

				if( theProjectsProfileDate == null ){

					theMsg = "Error, theProjectsProfileDate could not be parsed";
					isProfileNeedsUpdate = true;

				} else if( theProjectsProfileDate.after( profileDate ) ){

					theMsg = "An upgraded " + profileName + " profile is needed. \n" +
							"The current profile version you have installed is " + theProfileVersionValue + 
							" (" + theProfileDateValue + ")\n" +
							"The project called " + thePrj.getName() + 
							" is suggesting it was set-up with " + theProjectsProfileVersionValue + 
							" (" + theProjectsProfileDateValue + ") \n";

				} else if( theProjectsProfileDate.equals( profileDate ) ){

					theMsg = "The project's profile date and version check was successfully completed. ";
					isProfileNeedsUpdate = false;

				} else if( theProjectsProfileDate.before( profileDate ) ){

					theMsg = "Your " + profileName + 
							" profile is newer that project's required profile date. \n\n" +
							"The current profile version you have installed is " + theProfileVersionValue + 
							" (" + theProfileDateValue + ") \n" +
							"The project called " + thePrj.getName() + 
							" is suggesting it was set-up with " + theProjectsProfileVersionValue + 
							" (" + theProjectsProfileDateValue + ") \n\n";

					if( isProvideFixingAdvice ){
						theMsg += "It is recommended to right-click and run the profile's Set-up project properties command \n" +
								"to update the project to the latest profile before proceeding \n";
					}

					isProfileNeedsUpdate = true;
				}
			}
		}

		if( isProfileNeedsUpdate ){

			if( isShowInfoDialog ){
				UserInterfaceHelper.showInformationDialog( theMsg );
			} else {
				_context.info( theMsg );
			}

		} else if( theMsg != null ) {

			_context.info( theMsg );
		}

		return isProfileNeedsUpdate;
	}

	public void setupProjectWithProperties(
			String withNewTerm ){

		IRPProject thePrj = _context.get_rhpPrj();

		IRPStereotype theNewTermStereotype = thePrj.getNewTermStereotype();

		if( theNewTermStereotype == null || 
				!theNewTermStereotype.getName().equals( withNewTerm ) ){

			_context.info( "Performing a Change to > '" + withNewTerm + "' on " + _context.elInfo( thePrj ) );
			thePrj.changeTo( withNewTerm );
		}

		if( _profileInfo.isValid() ){

			if( thePrj.isReadOnly()==1 ){

				_context.error( "Error, unable to set tags as project is read-only" );

			} else {

				setPropertiesValuesRequestedInConfigFile( 
						thePrj,
						_profileInfo.get_profilePropertiesString() );
				
				_profileInfo.setProjectToProfileTagValues();
			}
		}
	}
}

/**
 * Copyright (C) 2016-2022  MBSE Training and Consulting Limited (www.executablembse.com)

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
