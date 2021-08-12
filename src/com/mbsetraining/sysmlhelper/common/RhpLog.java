package com.mbsetraining.sysmlhelper.common;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;

import com.telelogic.rhapsody.core.*;

public class RhpLog {
	
	private boolean _isEnableWarningLogging = false;
	private boolean _isEnableErrorLogging = false;
	private boolean _isEnableDebugLogging = false;
	private boolean _isEnableInfoLogging = false;

	protected IRPApplication _rhpApp = null;
	protected IRPProject _rhpPrj = null;
	
	private Path _file = null;
	
	RhpLog( IRPApplication theRhpApp,
			IRPProject theRhpPrj,
			boolean isEnableWarningLogging,
			boolean isEnableErrorLogging,
			boolean isEnableDebugLogging,
			boolean isEnableInfoLogging ){
		
		_rhpApp = theRhpApp;
		_rhpPrj = theRhpPrj;
		_isEnableWarningLogging = isEnableWarningLogging;
		_isEnableErrorLogging = isEnableErrorLogging;
		_isEnableDebugLogging = isEnableDebugLogging;
		_isEnableInfoLogging = isEnableInfoLogging;
		
		
		String msg = "RhpLog constructor was invoked for " + 
				_rhpApp.getApplicationConnectionString() +
				"(";
		
		if( _isEnableWarningLogging ){
			msg += "Warning logging is enabled | ";
		} else {
			msg += " | ";
		}
		
		if( _isEnableInfoLogging ){
			msg += "Info logging is enabled | ";
		} else {
			msg += " | ";
		}	
		
		if( _isEnableErrorLogging ){
			msg += "Error logging is enabled | ";
		} else {
			msg += " | ";
		}
		
		if( _isEnableDebugLogging ){
			msg += "Debug logging is enabled";
		} else {

		}
		
		debug( "RhpLog constructor was invoked for " + 
				_rhpApp.getApplicationConnectionString() +
				msg + ")" );
	}

	public void info( 
			String theStr ){

		if( _isEnableInfoLogging ){
			writeLine( "Info   : " + theStr );
		}		
	}

	public void debug( 
			String theStr ){

		if( _isEnableDebugLogging ){
			writeLine( "Debug  : " + theStr );
		}
	}
	
	public void error( 
			String theStr ){

		if( _isEnableErrorLogging ){
			writeLine( "Error  : " + theStr );  
		}
	}

	public void warning( 
			String theStr ){

		if( _isEnableWarningLogging ){
			writeLine( "Warning: " + theStr );
		}
	}
	
	protected void writeLine( 
			String theStr ){

		String msg = theStr + "\n";		

		try {
			System.out.println( theStr );
			_rhpApp.writeToOutputWindow( "", msg );
			writeToFileIfEnabled( msg, StandardOpenOption.APPEND );
			
		} catch (Exception e) {
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

	public boolean is_isEnableWarningLogging() {
		return _isEnableWarningLogging;
	}

	public void set_isEnableWarningLogging(boolean _isEnableWarningLogging) {
		this._isEnableWarningLogging = _isEnableWarningLogging;
	}

	public boolean is_isEnableErrorLogging() {
		return _isEnableErrorLogging;
	}

	public void set_isEnableErrorLogging(boolean _isEnableErrorLogging) {
		this._isEnableErrorLogging = _isEnableErrorLogging;
	}

	public boolean is_isEnableDebugLogging() {
		return _isEnableDebugLogging;
	}

	public void set_isEnableDebugLogging(boolean _isEnableDebugLogging) {
		this._isEnableDebugLogging = _isEnableDebugLogging;
	}

	public boolean is_isEnableInfoLogging() {
		return _isEnableInfoLogging;
	}

	public void set_isEnableInfoLogging(boolean _isEnableInfoLogging) {
		this._isEnableInfoLogging = _isEnableInfoLogging;
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