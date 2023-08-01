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

	public List<IRPRequirement> getRemoteRequirementsTracedFrom(
			IRPRequirement  theReqt ){

		List<IRPRequirement> theRemoteReqts = new ArrayList<>();

		@SuppressWarnings("unchecked")
		List<IRPDependency> theRemoteDependencies = theReqt.getRemoteDependencies().toList();

		for( IRPDependency theRemoteDependency : theRemoteDependencies ){

			IRPModelElement theDependsOn = theRemoteDependency.getDependsOn();

			if( theDependsOn instanceof IRPRequirement ){

				theRemoteReqts.add( (IRPRequirement) theDependsOn );
			}
		}

		return theRemoteReqts;
	}

	public List<IRPRequirement> getReqtsThatDontTraceOrTraceToChangedRemoteReqtsIn(
			List<IRPRequirement>  theCandidateReqts ){

		List<IRPRequirement> theReqtsThatDontTrace = new ArrayList<>();

		for( IRPRequirement theCandidateReqt : theCandidateReqts ){

			List<IRPRequirement> theRemoteRequirements = getRemoteRequirementsTracedFrom( theCandidateReqt );

			boolean isMatchFound = false;

			if( theRemoteRequirements.size() > 1 ){

				_context.warning( _context.elInfo( theCandidateReqt ) + " traces to " + theRemoteRequirements.size() + 
						" remote requirements, when expecting 0 or 1 hence I'm unable to determine which to use" );

			} else if( theRemoteRequirements.size() == 1 ){

				String theRemoteSpec = theRemoteRequirements.get( 0 ).getSpecification();
				String theSpec = theCandidateReqt.getSpecification();

				if( theSpec.equals( theRemoteSpec ) ){
					isMatchFound = true;
				}
			}	

			if( !isMatchFound ){
				theReqtsThatDontTrace.add( theCandidateReqt );
			}
		}

		return theReqtsThatDontTrace;
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

			List<IRPRequirement> theReqtsThatDontTrace = getReqtsThatDontTraceOrTraceToChangedRemoteReqtsIn( theReqts );

			int diff = theReqts.size() - theReqtsThatDontTrace.size();

			if( diff != 0 ){

				boolean answer = UserInterfaceHelper.askAQuestion( diff + " of the " + theReqts.size() + 
						" requirements under " + _context.elInfo( underEl) + " \nalready trace to remote requirements with matching specification text. \n\n" + 
						"Shall I restrict the csv export to only the " + theReqtsThatDontTrace.size() + " requirements that are new or have changed?");

				if( answer ){
					theReqts = theReqtsThatDontTrace;
				}
			}

			exportToCSV( underEl, theReqts );
		}
	}

	public String getIdentifierFromTracedRemoteRequirement(
			IRPRequirement forReqt ){

		String theIdentifier = "";

		List<IRPRequirement> theRemoteReqts = getRemoteRequirementsTracedFrom( forReqt );

		if( theRemoteReqts.size() > 1 ){

			_context.warning( _context.elInfo( forReqt ) + " has " + theRemoteReqts.size() + " remote requirements when expecting 0 or 1");

		} else if( theRemoteReqts.size() == 1 ){

			IRPRequirement theRemoteReqt = theRemoteReqts.get( 0 );

			theIdentifier= theRemoteReqt.getRequirementID();
		}

		return theIdentifier;
	}

	private void exportToCSV(
			IRPModelElement underEl, 
			List<IRPRequirement> theReqts ){

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
			
			boolean isIncludeArtifactName = _context.getCSVExportIncludeArtifactName( underEl );
			boolean isIncludeColumnsForLinkedAnnotations = _context.getCSVExportIncludeColumnsForLinkedAnnotations( underEl );

			_context.debug( "exportRequirementsToCSV CSVExportSeparator=" + separator + 
					", CSVExportArtifactType=" + artifactTypeForCSVExport + 
					", CVSExportIncludeArtifactName=" + isIncludeArtifactName +
					", CSVExportIncludeColumnsForLinkedAnnotations=" + isIncludeColumnsForLinkedAnnotations );

			String theMsg = "Based on the ExecutableMBSEProfile::RequirementsAnalysis properties for this package this helper will export " + theReqts.size();

			if( theReqts.size() == 1 ) {
				theMsg += " requirement.";
			} else {
				theMsg += " requirements.";
			}

			theMsg += "\n\nSource: " + _context.elInfo( underEl ) + 
					" \n" + "Target: " + theFile.getAbsolutePath() + "\n" +
					"Artifact Type: " + artifactTypeForCSVExport + "\n";

			if( isIncludeArtifactName ) {
				theMsg += "Include Name column: True \n";
			} else {
				theMsg += "Include Name column: False \n";
			}

			if( isIncludeColumnsForLinkedAnnotations ) {
				theMsg += "Include annotation columns, e.g, Rationale: True";
			} else {
				theMsg += "Include annotation columns, e.g, Rationale: False";
			}
			
			if( !theAdditionalHeadings.isEmpty() && 
					!isIncludeColumnsForLinkedAnnotations ) {
				
				theMsg += "\n\nAnnotations such as Rationale were found, but the CSVExportIncludeColumnsForLinkedAnnotations property is set to False";
			}
			
			theMsg += "\n\nOnce exported, prior to import into DOORS Next, first open in Microsoft Excel and check the contents, then Save As to .xlsx \nto create the file to import." + 
					"\n\nDo you want to proceed? ";

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

						if( isIncludeArtifactName ){
							theHeadingLine = 
									"Identifier" + separator + 
									"isHeading" + separator +
									"parentBinding" + separator +
									"Artifact Type" + separator + 
									"Name" + separator + 
									"Primary Text";
						} else {
							theHeadingLine = 
									"Identifier" + separator + 
									"isHeading" + separator +
									"parentBinding" + separator +
									"Artifact Type" + separator + 
									"Primary Text";
						}

						if( isIncludeColumnsForLinkedAnnotations ) {		
							
							for( String theAdditionalHeading : theAdditionalHeadings ){
								theHeadingLine += separator + theAdditionalHeading;
							}							
						}

						theHeadingLine += "\n";

						myWriter.write( theHeadingLine );
						
						boolean isAnnotationInfoNeeded = false;

						for( IRPRequirement theReqt : theReqts ){
							
							String theIdentifier = getIdentifierFromTracedRemoteRequirement( theReqt );
							String theIsHeading = "";
							String theParentBinding = "";
							String theName = theReqt.getName();
							String theLine = "";

							String theSpecification = _context.
									replaceCSVIncompatibleCharsFrom( theReqt.getSpecification() );

							if( isIncludeArtifactName ){
								
								theLine = 
										theIdentifier + separator + 
										theIsHeading + separator + 
										theParentBinding + separator + 
										artifactTypeForCSVExport + separator + 
										theName + separator + 
										theSpecification;
							} else {
								theLine = 
										theIdentifier + separator + 
										theIsHeading + separator + 
										theParentBinding + separator + 
										artifactTypeForCSVExport + separator + 
										theSpecification;
							}

							if( isIncludeColumnsForLinkedAnnotations ) {
								
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
								
							} else if( !isAnnotationInfoNeeded && 
									!theAdditionalHeadings.isEmpty() ) {
								isAnnotationInfoNeeded = true;
							}

							theLine += "\n";

							myWriter.write( theLine );
						}
						
						if( isAnnotationInfoNeeded ){
							_context.info( "Annotations such as Rationale were found but the CSVExportIncludeColumnsForLinkedAnnotations property is set to False" );
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