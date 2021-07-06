package com.mbsetraining.sysmlhelper.common;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;

import com.telelogic.rhapsody.core.*;

public class RhpLog {

	private String _enableErrorLoggingProperty;
	private String _enableWarningLoggingProperty;
	private String _enableInfoLoggingProperty;
	private String _enableDebugLoggingProperty;

	protected IRPApplication _rhpApp = null;
	protected IRPProject _rhpPrj = null;

	private Path _file = null;

	RhpLog( String theAppID,
			String enableErrorLoggingProperty,
			String enableWarningLoggingProperty,
			String enableInfoLoggingProperty,
			String enableDebugLoggingProperty ){

		_rhpApp = RhapsodyAppServer.getActiveRhapsodyApplicationByID( theAppID );
		_rhpPrj = _rhpApp.activeProject();
		_enableErrorLoggingProperty = enableErrorLoggingProperty;
		_enableWarningLoggingProperty = enableWarningLoggingProperty;
		_enableInfoLoggingProperty = enableInfoLoggingProperty;
		_enableDebugLoggingProperty = enableDebugLoggingProperty;

		debug( "RhpLog constructor was invoked for " + theAppID );
	}

	public void info( 
			String theStr ){

		writeIfPropertyEnabled( 
				theStr, 
				_enableInfoLoggingProperty,
				"Info   : " );		
	}

	public void debug( 
			String theStr ){

		writeIfPropertyEnabled( 
				theStr, 
				_enableDebugLoggingProperty,
				"Debug  : " );
	}

	public void error( 
			String theStr ){

		writeIfPropertyEnabled( 
				theStr, 
				_enableErrorLoggingProperty,
				"Error  : " );  
	}

	public void warning( 
			String theStr ){

		writeIfPropertyEnabled( 
				 theStr, 
				_enableWarningLoggingProperty,
				"Warning: " );
	}

	protected void writeIfPropertyEnabled( 
			String theStr,
			String theProperty,
			String thePrefix ){

		try {
			String[] lines = theStr.split( "\\n" );
			
			for( String line : lines ){

				String msg = thePrefix + line + "\n";

				writeToFileIfEnabled( msg, StandardOpenOption.APPEND );

				if( _rhpPrj != null ){

					boolean isEnabled = 
							getBoolPropertyValueFromRhp(
									theProperty,
									true );

					if( isEnabled ){
						System.out.println( theStr );
						_rhpApp.writeToOutputWindow( "", msg );
					}
				}
			}

		} catch( Exception e ){
			// fail gracefully
		}
	}

	public void startWritingToFile(
			String thePath,
			String theMsg ){

		_file = Paths.get( thePath );

		if( !Files.exists( _file ) ){

			try {
				Files.createFile( _file );

			} catch( Exception e ){
				System.out.println( "Exception in startWritingToFile, e=" + e.getMessage() );
				error( "Exception while trying to create file: " + thePath  + " e=" + e.getMessage() );
			}
		}

		if( Files.isWritable( _file ) ){

			try {
				Files.write( _file, ( theMsg + "\n").getBytes() );

			} catch( Exception e ){
				System.out.println( "Exception, e=" + e.getMessage() );
				error( "Exception while trying to write to file: " + thePath  + " e=" + e.getMessage() );
			}
		} else {
			error( "Log cannot be written to " + thePath );
		}
	}

	public void stopWritingToFile(){
		_file = null;
	}

	private boolean getBoolPropertyValueFromRhp(
			String propertyKey,
			boolean defaultIfNotSet ) {

		boolean isSet = defaultIfNotSet;

		if( _rhpPrj != null ){

			try {
				String theValue = _rhpPrj.getPropertyValue(
						propertyKey );

				if( theValue != null ){
					isSet = theValue.equals( "True" );
				}

			} catch( Exception e ){
				_rhpApp.writeToOutputWindow( "", e.getMessage() + 
						" has occurred which suggests a problem finding property in the Smartfacts Profile." );
			}
		}

		return isSet;
	}

	protected void writeToFileIfEnabled(
			String theStr,
			StandardOpenOption theOpenOption ){

		try {
			if( _file != null ){

				if( !Files.exists( _file ) ){
					Files.createFile( _file );
				}

				Files.write( _file, theStr.getBytes(), theOpenOption );
			}
		} catch( IOException e ){

			e.printStackTrace();
		}
	}

	public String elInfo( 
			IRPModelElement theEl ){

		String theStr = "";

		if( theEl != null ){
			theStr = theEl.getUserDefinedMetaClass() + " called " + 
					theEl.getName() + " (with base type " + 
					theEl.getMetaClass() + ")";
		}

		return theStr;
	}
}