package generalhelpers;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import java.util.Properties;
import java.util.ResourceBundle;

import com.telelogic.rhapsody.core.*;

public class ConfigurationSettings {

	protected ResourceBundle _resources;
	protected Properties _properties;
	protected String _propertyFileName;
	protected String _resourceBundleFileName;
	protected String _profileName;
	protected IRPApplication _rhpApp;
	protected IRPProject _rhpPrj;
	protected IRPStereotype _profileInfoStereotype;
	protected IRPTag _profileDateTag;		
	protected IRPTag _profileVersionTag;
	protected IRPTag _profilePropertyTag;
	protected Date _profileDate;

	public static void main(String[] args) {

		IRPApplication theRhpApp = RhapsodyAppServer.getActiveRhapsodyApplication();

		IRPProject theRhpPrj = theRhpApp.activeProject();

		// set the properties
		ConfigurationSettings theConfigSettings = 
				new ConfigurationSettings( 
						theRhpApp,
						theRhpPrj,
						"ExecutableMBSE.properties", 
						"ExecutableMBSE_MessagesBundle",
						"ExecutableMBSE" );

		boolean isSetupNeeded = theConfigSettings.checkIfSetupProjectIsNeeded( true, false );

		if( isSetupNeeded ){
			theConfigSettings.setupProjectWithProperties();
		}
	}

	public ConfigurationSettings(
			IRPApplication theRhpApp,
			IRPProject theRhpPrj,
			String thePropertyFileName,
			String theResourceBundleFileName,
			String theProfileName ){

		_rhpApp = theRhpApp;
		_rhpPrj = theRhpPrj;

		// The stereotype in the profile will give details about current profile date/version
		_profileInfoStereotype = 
				GeneralHelpers.getExistingStereotype( 
						"ProfileInfo", 
						_rhpPrj );

		if( _profileInfoStereotype == null ){
			//Logger.writeLine( "Warning, could not find ProfileInfo stereotype in profile" );	
		} else {
			_profileDateTag = _profileInfoStereotype.getTag( "ProfileDate" );	

			if( _profileDateTag == null ){
				Logger.writeLine( "Warning, could not find ProfileDate tag in profile" );	
			} else {

				String profileDateValue = _profileDateTag.getValue();
				_profileDate = getDate( profileDateValue );

				if( _profileDate == null ){
					Logger.writeLine( "Warning, ProfileDate could not be parsed" );	
				}
			}

			_profileVersionTag = _profileInfoStereotype.getTag( "ProfileVersion" );

			if( _profileVersionTag == null ){
				Logger.writeLine( "Warning, could not find ProfileVersion tag in profile" );	
			}

			_profilePropertyTag = _profileInfoStereotype.getTag( "ProfileProperties" );

			if( _profilePropertyTag == null ){
				Logger.writeLine( "Warning, could not find ProfileProperties tag in profile" );	
			}
		}

		_propertyFileName = thePropertyFileName;
		_resourceBundleFileName = theResourceBundleFileName;
		_profileName = theProfileName;

		if( _properties == null ){

			InputStream inputStream = null;

			try {
				_properties = new Properties();

				inputStream = getClass().getClassLoader().getResourceAsStream( thePropertyFileName );

				if( inputStream != null ){
					_properties.load( inputStream );
				} else {
					String theMsg = "Exception: Expected property file called '" + 
							thePropertyFileName + "' was not found in the classpath";

					Logger.error( theMsg );
					throw new FileNotFoundException( theMsg );
				}

			} catch( Exception e ){
				Logger.error( "Exception trying to open '" + thePropertyFileName + "': " + e.getMessage() );

			} finally {
				try {
					inputStream.close();

				} catch( IOException e ){
					Logger.error( "Exception trying to close '" + thePropertyFileName + "'" );
				}
			}
		}

		if( _properties != null && _resources == null ){

			String language = _properties.getProperty( "DefaultLanguage", "en" );
			String country = _properties.getProperty( "DefaultCountry", "US" );

			Locale currentLocale = new Locale( language, country );

			try {
				_resources = ResourceBundle.getBundle( 
						theResourceBundleFileName, 
						currentLocale ); 

				Logger.info( "PluginSettingsAndResources has loaded properties from " +
						thePropertyFileName + " and resource bundle from " + 
						theResourceBundleFileName + " for language=" + language + 
						" and county=" + country);

			} catch( Exception e ){
				Logger.error( "Exception while trying ResourceBundle.getBundle" );
			}
		}
	}

	public String get_profileName() {
		return _profileName;
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

				Logger.info( "Setting '" + thePropertyName + 
						"' to '" + thePropertyValue + "' based on '" + 
						_propertyFileName + "'" ); 

				try {
					onTheElement.setPropertyValue( 
							thePropertyName, thePropertyValue );

				} catch (Exception e) {

					Logger.error( "Exception in setPropertiesValuesRequestedInConfigFile, " +
							"unable to set " + thePropertyName + " to " + thePropertyValue + 
							" on " + Logger.elementInfo( onTheElement ) );
				}
			}
		}
	}
	
	public boolean checkIfSetupProjectIsNeeded(
			boolean isShowInfoDialog,
			boolean isProvideFixingAdvice ){

		String theMsg = null;
		boolean isProfileNeedsUpdate = false;

		if( _profileDateTag != null &&
				_profileVersionTag != null &&
				_profilePropertyTag != null ){

			IRPTag theProjectDateTag = _rhpPrj.getTag( "ProfileDate" );
			IRPTag theProjectVersionTag = _rhpPrj.getTag( "ProfileVersion" );

			String theProfileDateValue = _profileDateTag.getValue();
			String theProfileVersionValue = _profileVersionTag.getValue();

			boolean isProjectsProfileVersionSet = 
					( theProjectDateTag != null && 
					theProjectDateTag.getOwner().equals( _rhpPrj ) ) &&
					( theProjectVersionTag != null &&
					theProjectVersionTag.getOwner().equals( _rhpPrj ) );

			if( !isProjectsProfileVersionSet ){				

				theMsg = "The project called " + _rhpPrj.getName() + " does not have the ProfileDate and ProfileVersion tags set \n" +
						"Your installed " + _profileName + " profile version is " + theProfileVersionValue + 
						" (" + theProfileDateValue + ") \n";
				
				if( isProvideFixingAdvice ){
					theMsg += "It is recommended to right-click and run MBSE Method > Set-up project properties command \n" +
							"to update the project to the latest profile before proceeding \n";
				}

				isProfileNeedsUpdate = true;

			} else {

				String theProjectsProfileDateValue = theProjectDateTag.getValue();
				//String theProjectsProfileVersionValue = theProjectVersionTag.getValue();

				// Logger.writeLine( "Project's required ProfileDate    = " + theProjectsProfileDateValue );
				//Logger.writeLine( "Project's required ProfileVersion = " + theProjectsProfileVersionValue );

				Date theProjectsProfileDate = getDate( theProjectsProfileDateValue );
				
				if( theProjectsProfileDate == null ){

					theMsg = "Error, theProjectsProfileDate could not be parsed";
					isProfileNeedsUpdate = true;

				} else if( theProjectsProfileDate.after( _profileDate ) ){

					theMsg = "An upgraded " + _profileName + " profile is needed. \n" +
							"The current profile version you have installed is " + theProfileVersionValue + 
							" (" + theProfileDateValue + ")\n" +
							"The project called " + _rhpPrj.getName() + 
							" is suggesting it needs " + theProfileVersionValue + 
							" (" + theProjectsProfileDateValue + ") \n";

				} else if( theProjectsProfileDate.equals( _profileDate ) ){

					theMsg = "The project's profile date and version check was successfully completed. \n";
					isProfileNeedsUpdate = false;

				} else if( theProjectsProfileDate.before( _profileDate ) ){

					theMsg = "Your " + _profileName + 
							" profile is newer that project's required profile date. \n\n" +
							"The current profile version you have installed is " + theProfileVersionValue + 
							" (" + theProfileDateValue + ") \n" +
							"The project called " + _rhpPrj.getName() + 
							" is suggesting it needs " + theProfileVersionValue + 
							" (" + theProjectsProfileDateValue + ") \n\n";
					
					if( isProvideFixingAdvice ){
						theMsg += "It is recommended to right-click and run MBSE Method > Set-up project properties command \n" +
								"to update the project to the latest profile before proceeding \n";
					}

					isProfileNeedsUpdate = true;
				}
			}
		}	

		// Only show upgrade request dialog if profile needs updating
		if( isProfileNeedsUpdate ){		
			
			if( isShowInfoDialog ){
				UserInterfaceHelpers.showInformationDialog( theMsg );
			} else {
				Logger.writeLine( theMsg );
			}
			
		} else if( theMsg != null ) {
			
			Logger.writeLine( theMsg );
		}

		return isProfileNeedsUpdate;
	}
	
	public void setupProjectWithProperties(){

		IRPStereotype theNewTermStereotype = _rhpPrj.getNewTermStereotype();
		
		if( theNewTermStereotype == null || 
				!theNewTermStereotype.getName().equals( "SysML" ) ){
			
			Logger.writeLine( "Performing a Change to > SysML on " + Logger.elementInfo( _rhpPrj ) );
			_rhpPrj.changeTo( "SysML" );
		}

		if( _profileDateTag != null &&
				_profileVersionTag != null &&
				_profilePropertyTag != null ){

			String theProfileDateValue = _profileDateTag.getValue();
			String theProfileVersionValue = _profileVersionTag.getValue();
			String theProfilePropertyTagValue = _profilePropertyTag.getValue();
			
			if( _rhpPrj.isReadOnly()==1 ){
			
				Logger.writeLine("Error, unable to set tags as project is read-only");
				
			} else {

				setPropertiesValuesRequestedInConfigFile( 
						_rhpPrj,
						theProfilePropertyTagValue );
				
				Logger.writeLine( "Setting " + Logger.elementInfo( _profileDateTag ) + " on " + 
						Logger.elementInfo( _profileInfoStereotype ) + " to " + theProfileDateValue );

				_rhpPrj.setTagValue( _profileDateTag, theProfileDateValue );

				Logger.writeLine( "Setting " + Logger.elementInfo( _profileVersionTag ) + " on " + 
						Logger.elementInfo( _profileInfoStereotype ) + " to " + theProfileVersionValue );

				_rhpPrj.setTagValue( _profileVersionTag, theProfileVersionValue );
			
				Logger.writeLine( "Setting " + Logger.elementInfo( _profilePropertyTag ) + " on " + 
						Logger.elementInfo( _profileInfoStereotype ) + " to " + theProfilePropertyTagValue );
				
				_rhpPrj.setTagValue( _profilePropertyTag, theProfilePropertyTagValue );
			}
		}
	}
	
	/**
	 * @param args
	 */
	private static Date getDate( 
			String fromString ){

		SimpleDateFormat parser = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss");

		Date date = null;

		try {
			date = parser.parse( fromString );

		} catch( ParseException e ){
			Logger.writeLine( "Exception in getDate trying to parse date" );
		}

		return date;
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
