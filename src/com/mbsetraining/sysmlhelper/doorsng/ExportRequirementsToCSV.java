package com.mbsetraining.sysmlhelper.doorsng;

import java.io.File;
import java.io.FileWriter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import javax.swing.JFileChooser;
import javax.swing.filechooser.FileNameExtensionFilter;

import com.mbsetraining.sysmlhelper.common.AnnotationMap;
import com.mbsetraining.sysmlhelper.common.UserInterfaceHelper;
import com.mbsetraining.sysmlhelper.executablembse.ExecutableMBSE_Context;
import com.telelogic.rhapsody.core.*;

public class ExportRequirementsToCSV {

	protected ExecutableMBSE_Context _context;
	protected RemoteRequirementAssessment _assessment;

	// For testing only
	public static void main(String[] args) {

		ExecutableMBSE_Context theContext = new ExecutableMBSE_Context( 
				RhapsodyAppServer.getActiveRhapsodyApplication().getApplicationConnectionString() );

		ExportRequirementsToCSV theExporter = new ExportRequirementsToCSV( theContext );

		theExporter.exportRequirementsToCSV( theContext.getSelectedElement( false ), 1 );	
	}
	
	public ExportRequirementsToCSV(
			ExecutableMBSE_Context context ) {

		_context = context;
		_assessment = new RemoteRequirementAssessment( _context );
	}

	public void exportRequirementsToCSV(
			IRPModelElement underEl,
			int recursive ){

		List<IRPModelElement> theSelectedEls = new ArrayList<>();
		theSelectedEls.add( underEl );
		_assessment.determineRequirementsToUpdate( theSelectedEls );

		boolean isContinue = true;

		int inScopeCount = 
				_assessment._requirementsInScope.size() +
				_assessment._requirementOwnersInScope.size();

		List<IRPModelElement> theEls = new ArrayList<>();

		if( inScopeCount == 0 ){

			UserInterfaceHelper.showWarningDialog( "There are no requirement artifacts under " + _context.elInfo( underEl ) + " to export" );
			isContinue = false;
		}

		int unloadedCount = _assessment._requirementsWithUnloadedHyperlinks.size();

		if( unloadedCount > 0 ) {

			isContinue = UserInterfaceHelper.askAQuestion( "There are " + unloadedCount + " unloaded requirement links under " + 
					_context.elInfo( underEl ) + ". \nAre you sure you want to continue rather than log into the remote package first?" );
		}

		if( isContinue ) {

			if( !_assessment._requirementOwnersInScope.isEmpty() &&
					_assessment._requirementOwnersThatDontTrace.size() > 0 ) {

				boolean answer = UserInterfaceHelper.askAQuestion( _assessment._requirementOwnersThatDontTrace.size() + 
						" of the " + _assessment._requirementOwnersInScope.size() + " requirement owners under " + 
						_context.elInfo( underEl) + " \ndon't trace to remote requirements. \n\n" + 
						"Do you want to export just the " + _assessment._requirementOwnersThatDontTrace.size() + 
						" requirement owners and " + _assessment._requirementsThatDontTrace.size() + " requirements with no links? \n"+
						"(these will be generated with temporary ids so that structure can be imported with new element creation only)");

				if( answer ){

					theEls.addAll( _assessment._requirementOwnersThatDontTrace );
					theEls.addAll( _assessment._requirementsThatDontTrace );

				} else {

					theEls.addAll( _assessment._requirementsInScope );
				}

			} else {

				// No structure

				int missingOrChangedCount = 
						_assessment._requirementsThatDontTrace.size() + 
						_assessment._requirementsToUpdateSpec.size();

				int diff = inScopeCount - missingOrChangedCount;


				if( diff > 0 ){

					boolean answer = UserInterfaceHelper.askAQuestion( diff + " of the " + inScopeCount + 
							" requirements under " + _context.elInfo( underEl) + 
							" \nalready trace to remote requirements with matching specification text. \n\n" + 
							"Shall I restrict the csv export to only the " + missingOrChangedCount + 
							" that are known to be new or changed (rather than all)?");

					if( answer ){

						theEls.addAll( _assessment._requirementsThatDontTrace );
						theEls.addAll( _assessment._requirementsToUpdateSpec );

					} else {

						theEls.addAll( _assessment._requirementsInScope );
					}

				} else {
					theEls.addAll( _assessment._requirementsInScope );
				}

			}
		}

		if( !theEls.isEmpty() ) {
			exportToCSV( underEl, theEls );
		}
	}

	private String getIdentifierFromTracedRemoteRequirement(
			IRPModelElement forModelEl ){

		String theIdentifier = "";

		IRPRequirement theRemoteReqt = _context.getRemoteRequirementFor( forModelEl );

		if( theRemoteReqt != null ){
			theIdentifier= theRemoteReqt.getRequirementID();
		}

		return theIdentifier;
	}

	private void exportToCSV(
			IRPModelElement underEl, 
			List<IRPModelElement> theSourceEls ) {

		_context.debug( "There are " + theSourceEls.size() + " requirements under " + _context.elInfo( underEl ) );

		boolean isFirstTimeExport = isFirstTimeExport( theSourceEls );

		cleanNewLinesFrom( theSourceEls );

		List<String> theAnnotationHeadings = getAnnotationHeadingsFrom( theSourceEls );

		String theFilename = underEl.getName() + ".csv";
		File theFile = chooseAFileToImport( theFilename );

		if( theFile == null ) {

			UserInterfaceHelper.showWarningDialog( 
					"No file was selected to export requirement to" );

		} else {

			// Get controlling properties
			String artifactType = _context.getCSVExportArtifactType( underEl );
			_context.debug( "CSVExportArtifactType = " + artifactType );

			String separator = _context.getCSVExportSeparator( underEl );
			_context.debug( "CSVExportSeparator = " + separator );

			boolean isIncludeArtifactName = _context.getCSVExportIncludeArtifactName( underEl );
			_context.debug( "CVSExportIncludeArtifactName = " + isIncludeArtifactName );

			boolean isIncludeColumnsForLinkedAnnotations = _context.getCSVExportIncludeColumnsForLinkedAnnotations( underEl );
			_context.debug( "CSVExportIncludeColumnsForLinkedAnnotations = " + isIncludeColumnsForLinkedAnnotations );

			String theMsg = "Based on the ExecutableMBSEProfile::RequirementsAnalysis properties for this package this helper will export " + theSourceEls.size();

			if( theSourceEls.size() == 1 ) {
				theMsg += " requirement.";
			} else {
				theMsg += " requirements.";
			}

			theMsg += "\n\nSource: " + _context.elInfo( underEl ) + 
					" \n" + "Target: " + theFile.getAbsolutePath() + "\n" +
					"Artifact Type: " + artifactType + "\n";

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

			if( !theAnnotationHeadings.isEmpty() && 
					!isIncludeColumnsForLinkedAnnotations ) {

				theMsg += "\n\nAnnotations such as Rationale were found but the CSVExportIncludeColumnsForLinkedAnnotations property is set to False";
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

						String theHeadingLine = "id" + separator;

						if( isIncludeArtifactName ){
							theHeadingLine += "Name" + separator;			
						}

						theHeadingLine += "Primary Text" + separator;
						theHeadingLine += "isHeading" + separator;
						theHeadingLine += "parentBinding" + separator;
						theHeadingLine += "Artifact Type";

						if( isIncludeColumnsForLinkedAnnotations ) {		

							for( String theAnnotationHeading : theAnnotationHeadings ){
								theHeadingLine += separator + theAnnotationHeading;
							}							
						}

						theHeadingLine += "\n";

						myWriter.write( theHeadingLine );

						boolean isAnnotationInfoNeeded = false;

						// Used to store Ids when no parent finding found
						Map<IRPModelElement,String> theNewHeadingIdMap = new HashMap<>();

						for( IRPModelElement theSourceEl : theSourceEls ){

							String theIdentifier = getIdentifierFromTracedRemoteRequirement( theSourceEl );
							
							if( theIdentifier.isEmpty() && 
									isFirstTimeExport ) {

								int newId = theNewHeadingIdMap.size() + 1;
								theIdentifier = Integer.toString( newId );
								theNewHeadingIdMap.put( theSourceEl, theIdentifier );
							}

							String theName;

							if( theSourceEl instanceof IRPRequirement ) {

								theName = _context.getNameFromTracedRemoteRequirement( theSourceEl, theIdentifier );
							} else {
								theName = "";
							}

							String theParentBinding;

							IRPRequirement theRemoteParent = _assessment._requirementsRemoteParentMap.get( theSourceEl );

							if( theRemoteParent != null ) {
								
								_context.debug( "The  remote parent for " + _context.elInfo( theSourceEl ) + " is " + _context.elInfo( theRemoteParent ) );
								theParentBinding = theRemoteParent.getRequirementID();
								
							} else if( theNewHeadingIdMap.containsKey( theSourceEl.getOwner() ) ) {
								
								theParentBinding = theNewHeadingIdMap.get( theSourceEl.getOwner() );
								
							} else {
								theParentBinding = "";
							}

							String thePrimaryText;

							if( theSourceEl instanceof IRPRequirement ) {

								IRPRequirement theReqt = (IRPRequirement)theSourceEl;

								thePrimaryText = _context.
										replaceCSVIncompatibleCharsFrom( theReqt.getSpecification() );
							} else {
								// Keep name same as remote if applicable to avoid changing it;
								thePrimaryText = _context.
										getNameFromTracedRemoteRequirement( theSourceEl, theIdentifier ); 
							}

							String theLine = theIdentifier + separator;

							if( isIncludeArtifactName ){
								theLine += theName + separator;
							}

							theLine += thePrimaryText + separator;

							// isHeading
							if( theSourceEl instanceof IRPRequirement ) {
								theLine += "false" + separator;
							} else {
								theLine += "true" + separator;								
							}

							theLine += theParentBinding + separator;
							
							if( theSourceEl instanceof IRPRequirement ) {

								theLine += artifactType;
							} else {
								theLine += "Heading";
							}

							if( isIncludeColumnsForLinkedAnnotations ) {

								AnnotationMap theAnnotationMap = new AnnotationMap( theSourceEl, _context );

								for( String theAdditionalHeading : theAnnotationHeadings ){

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
									!theAnnotationHeadings.isEmpty() ) {
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

	private boolean isFirstTimeExport(
			List<IRPModelElement> theSourceEls ){

		boolean isFirstTimeExport = true;

		if( isFirstTimeExport ) {

			for( IRPModelElement theSourceEl : theSourceEls ){

				String theId = getIdentifierFromTracedRemoteRequirement( theSourceEl );

				if( !theId.isEmpty() ) {
					isFirstTimeExport = false;
					break;
				}
			}
		}

		return isFirstTimeExport;
	}

	private void cleanNewLinesFrom(
			List<IRPModelElement> theEls ){

		List<IRPModelElement> theElsWithNewLines = new ArrayList<>();

		for( IRPModelElement theEl : theEls ){

			if( theEl instanceof IRPRequirement ){

				IRPRequirement theReqt = (IRPRequirement)theEl;

				String theSpec = theReqt.getSpecification();

				if( theSpec.contains( "\r" ) || theSpec.contains( "\n" ) ) {

					theElsWithNewLines.add( theEl );
				}
			}
		}

		if( !theElsWithNewLines.isEmpty() ) {

			boolean answer = UserInterfaceHelper.askAQuestion( theElsWithNewLines.size() + " of the " + 
					theEls.size() + " requirements have newline or linefeed characters: \n" +
					getStringFor( theElsWithNewLines, 1 ) + "\n" +
					"This means that they won't export to csv and roundtrip into DOORS NG correctly.\n\n" +
					"Do you want to fix the model to remove these before proceeding?" );

			if( answer ) {

				for( IRPModelElement theElWithNewLines : theElsWithNewLines ){

					if( theElWithNewLines instanceof IRPRequirement ) {

						IRPRequirement theReqt = (IRPRequirement) theElWithNewLines;

						String theSpec = theReqt.getSpecification().
								replaceAll( "\\r", "" ).replaceAll( "\\n", "" );

						_context.info( "Removing newlines from " + _context.elInfo( theReqt ) );
						theReqt.setSpecification( theSpec  );
					}
				}
			}
		}		
	}

	private List<String> getAnnotationHeadingsFrom(
			List<IRPModelElement> theEls ){

		List<String> theAnnotationHeadings = new ArrayList<>();

		for( IRPModelElement theEl : theEls ){

			AnnotationMap theAnnotationMap = new AnnotationMap( theEl, _context );

			if( !theAnnotationMap.isEmpty() ) {

				String msg = _context.elInfo( theEl ) + " has";

				for( Entry<String, List<IRPAnnotation>>  nodeId : theAnnotationMap.entrySet() ){

					String theKey = nodeId.getKey();

					if( !theAnnotationHeadings.contains( theKey ) ){
						theAnnotationHeadings.add( theKey );
					}

					List<IRPAnnotation> value = nodeId.getValue();

					msg += " " + nodeId.getKey() + " (" + value.size() + ")";		
				}

				_context.info( msg );
			}
		}

		return theAnnotationHeadings;
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
			List<IRPModelElement> theEls,
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