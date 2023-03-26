package com.mbsetraining.sysmlhelper.doorsng;

import java.io.File;
import java.io.FileWriter;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map.Entry;

import javax.swing.JFileChooser;
import javax.swing.filechooser.FileNameExtensionFilter;

import com.mbsetraining.sysmlhelper.common.AnnotationMap;
import com.mbsetraining.sysmlhelper.common.UserInterfaceHelper;
import com.mbsetraining.sysmlhelper.executablembse.ExecutableMBSE_Context;
import com.telelogic.rhapsody.core.*;

public class ExportRequirementsToCSV {

	ExecutableMBSE_Context _context;
	
	public ExportRequirementsToCSV(
			ExecutableMBSE_Context context ) {

		_context = context;
	}
	
	public static void main(String[] args) {
		
		ExecutableMBSE_Context theContext = new ExecutableMBSE_Context( 
				RhapsodyAppServer.getActiveRhapsodyApplication().getApplicationConnectionString() );
		
		ExportRequirementsToCSV theExporter = new ExportRequirementsToCSV( theContext );
		
		theExporter.exportRequirementsToCSV( theContext.getSelectedElement( false ), 0 );	
	}

	public void exportRequirementsToCSV(
			IRPModelElement underEl,
			int recursive ){

		@SuppressWarnings("unchecked")
		List<IRPRequirement> theReqts = underEl.getNestedElementsByMetaClass( 
				"Requirement", recursive ).toList();

		if( theReqts.isEmpty() ) {
			UserInterfaceHelper.showWarningDialog( "There are no requirements under " + _context.elInfo( underEl ) + " to export" );
		} else {
			_context.debug( "There are " + theReqts.size() + " requirements under " + _context.elInfo( underEl ) );
			
			List<IRPRequirement> theReqtsWithNewLines = new ArrayList<>();
			
			List<String> theAdditionalHeadings = new ArrayList<>();
			
			for( IRPRequirement theReqt : theReqts ){
				
				String theSpec = theReqt.getSpecification();
				
				if( theSpec.contains( "\r" ) || theSpec.contains( "\n" ) ) {
					
					theReqtsWithNewLines.add( theReqt );
				}
				
				AnnotationMap theAnnotationMap = new AnnotationMap( theReqt, _context );
				
				if( !theAnnotationMap.isEmpty() ) {
					
					String msg = _context.elInfo( theReqt ) + " has";
					
					for( Entry<String, List<IRPAnnotation>>  nodeId : theAnnotationMap.entrySet() ){
						
						String theKey = nodeId.getKey();
						
						if( !theAdditionalHeadings.contains( theKey ) ){
							theAdditionalHeadings.add( theKey );
						}
					
						List<IRPAnnotation> value = nodeId.getValue();
								
						msg += " " + nodeId.getKey() + " (" + value.size() + ")";		
					}
					
					_context.info( msg );
				}
			}
			
			if( !theReqtsWithNewLines.isEmpty() ) {
				
				boolean answer = UserInterfaceHelper.askAQuestion( theReqtsWithNewLines.size() + " of the " + 
						theReqts.size() + " requirements have newline or linefeed characters: \n" +
						getStringFor( theReqtsWithNewLines, 1 ) + "\n" +
						"This means that they won't export to csv and roundtrip into DOORS NG correctly.\n\n" +
						"Do you want to fix the model to remove these before proceeding?" );
				
				if( answer ) {
					for( IRPRequirement theReqtWithNewLines : theReqtsWithNewLines ){
						
						String theSpec = theReqtWithNewLines.getSpecification().
								replaceAll( "\\r", "" ).replaceAll( "\\n", "" );
						
						_context.info( "Removing newlines from " + _context.elInfo( theReqtWithNewLines ) );
						theReqtWithNewLines.setSpecification( theSpec  );
					}
				}
			}

			String theFilename = underEl.getName() + ".csv";
			File theFile = chooseAFileToImport( theFilename );
			
			if( theFile == null ) {
				
				UserInterfaceHelper.showWarningDialog( 
						"No file was selected to export requirement to" );
					
			} else {
				
				// Get controlling properties
				String artifactTypeForCSVExport = _context.getCSVExportArtifactType( underEl );
				String separator = _context.getCSVExportSeparator( underEl );
				boolean isNameForCVSExport = _context.getCSVExportIncludeArtifactName( underEl );

				_context.debug( "exportRequirementsToCSV CSVExportSeparator=" + separator + 
						", CSVExportArtifactType=" + artifactTypeForCSVExport + 
						", CVSExportIncludeArtifactName=" + isNameForCVSExport );

				String theMsg = "Based on the ExecutableMBSEProfile::RequirementsAnalysis properties for this package this helper will export " + theReqts.size();
				
				if( theReqts.size() == 1 ) {
					theMsg += " requirement.";
				} else {
					theMsg += " requirements.";
				}
				
				theMsg += "\n\nSource: " + _context.elInfo( underEl ) + 
						" \n" + "Target: " + theFile.getAbsolutePath() + "\n" +
						"Artifact Type: " + artifactTypeForCSVExport + "\n";
				
				if( isNameForCVSExport ) {
					theMsg += "Name column: True";
				} else {
					theMsg += "Name column: False";
				}
				
				theMsg += "\n\nDo you want to proceed? ";
				
				boolean answer = UserInterfaceHelper.askAQuestion( theMsg );
				
				if( answer ) {
					
					_context.info( theFile.getAbsolutePath() + " (Target File)");

					try {

						boolean isContinue = true;
						
						FileWriter myWriter = null;
						
						try {
							
							myWriter = new FileWriter( theFile );

							String info = "sep=" + separator;
							_context.info( "Added " + info + " to export as CSVExportSeparator property" );
							
							myWriter.write( info + "\n" );

						} catch( Exception e ) {
							
							isContinue = false;
							
							UserInterfaceHelper.showWarningDialog( 
									"Unable to export requirements to " + theFile.getAbsolutePath() + " \n" +
									"File is not writable. If it's open in another program, close it and try again." );						
						}
						
						if( isContinue ) {
							
							String theHeadingLine = "";
							
							if( isNameForCVSExport ){
								theHeadingLine = "Artifact Type" + separator + "Name" + separator + "Primary Text";
							} else {
								theHeadingLine = "Artifact Type" + separator + "Primary Text";
							}
							
							for( String theAdditionalHeading : theAdditionalHeadings ){
								theHeadingLine += separator + theAdditionalHeading;
							}
							
							theHeadingLine += "\n";
									
							myWriter.write( theHeadingLine );

							for( IRPRequirement theReqt : theReqts ){

								String theLine = "";
								String theName = theReqt.getName();
								
								String theSpecification = _context.
										replaceCSVIncompatibleCharsFrom( theReqt.getSpecification() );

								if( isNameForCVSExport ){
									theLine = artifactTypeForCSVExport + separator + theName + separator + theSpecification;
								} else {
									theLine = artifactTypeForCSVExport + separator + theSpecification;
								}
								
								AnnotationMap theAnnotationMap = new AnnotationMap( theReqt, _context );

								for( String theAdditionalHeading : theAdditionalHeadings ){
									
									theLine += separator;
									
									List<IRPAnnotation> theSpecificAnnotations = theAnnotationMap.
											getOrDefault( theAdditionalHeading, new ArrayList<>() );
									
									Iterator<IRPAnnotation> iterator = theSpecificAnnotations.iterator();
									
									while( iterator.hasNext() ){
										
										IRPAnnotation theSpecificAnnotation = (IRPAnnotation) iterator.next();
										
										String theDescription = _context.
												replaceCSVIncompatibleCharsFrom( theSpecificAnnotation.getDescription() );
										
										if( theSpecificAnnotations.size() <= 1 ) {
											theLine += theDescription;
										} else {
											theLine += theDescription + "(" + theSpecificAnnotation.getName() + ")";	
										}
									}		
								}
								
								theLine += "\n";
								
								myWriter.write( theLine );
							}
							
							myWriter.close();

						}


					} catch( Exception e ){

						_context.error( "Exception, e=" + e.getMessage() );
						System.out.println( "An error occurred." );
						e.printStackTrace();
					}
				}
			}
		}
	}

	private File chooseAFileToImport(
			String theFilename ){

		File theFile = null;

		JFileChooser fc = new JFileChooser();

		fc.addChoosableFileFilter( new CSVFileFilter() );
		fc.setFileFilter( 
				new FileNameExtensionFilter( 
						"csv file", 
						"csv" ) );

		fc.setSelectedFile( new File( theFilename ) );

		String path = "";

		path = _context.get_rhpPrj().getCurrentDirectory();		

		File fp = new File( path );
		fc.setCurrentDirectory( fp );	

		int result = fc.showSaveDialog( null );

		if( result == JFileChooser.APPROVE_OPTION ){

			File selFile = fc.getSelectedFile();

			if( selFile == null || selFile.getName().equals("") ){
				_context.warning( "No file selected" );
			} else {
				theFilename = selFile.getAbsolutePath();

				if (!theFilename.endsWith(".csv")){
					theFilename = theFilename + ".csv";
				}

				theFile = new File( theFilename );	
			}
		} else {
			_context.debug( "User chose to cancel" );
		}

		return theFile;
	}

	public class CSVFileFilter extends javax.swing.filechooser.FileFilter {
		
		String fileType="csv";
		
		public boolean accept(File f) {
			String name = f.getName();
			if (f.isDirectory())
				return true;
			return name.endsWith(fileType);
		}

		public String getDescription() {
			return "*" + fileType;
		}
	}
	
	
	public String getStringFor( 
			List<IRPRequirement> theEls,
			int max ) {
		
		int count = 0;
		
		String theString = "";
		
		for( IRPModelElement theEl : theEls ){
			count++;
			theString += theEl.getName() + "\n";
			
			if( count == max && 
					count != theEls.size() ){
				
				theString += "...\n";
				break;
			}
		}
		
		return theString;
	}
}

/**
 * Copyright (C) 2020-2023  MBSE Training and Consulting Limited (www.executablembse.com)

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