package com.mbsetraining.sysmlhelper.doorsng;

import java.io.File;
import java.io.FileWriter;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map.Entry;

import javax.swing.JFileChooser;
import javax.swing.filechooser.FileNameExtensionFilter;

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
	
	private String removeCSVIncompatibleCharsFrom(
			String theString ) {
		
		String theResult = theString.replaceAll( "\\r", "<CR>" );
		theResult = theResult.replaceAll( "\\n", "<LF>" );
		
		if( !theString.equals( theResult ) ) {
		
			_context.warning( "Removed CRLF characters from " + theResult );
		}
		
		return theResult;
	}

	public void exportRequirementsToCSV(
			IRPModelElement underEl,
			int recursive ){

		@SuppressWarnings("unchecked")
		List<IRPRequirement> theReqts = underEl.getNestedElementsByMetaClass( 
				"Requirement", recursive ).toList();

		_context.debug( "There are " + theReqts.size() + " requirements under " + _context.elInfo( underEl ) );
		
		List<String> theAdditionalHeadings = new ArrayList<>();
		
		for( IRPRequirement theReqt : theReqts ){
			
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

		String theFileName = chooseAFileToImport( underEl.getName() + ".csv" );

		if( theFileName != null ){

			_context.info( "User chose " + theFileName + " to export " + theReqts.size() + " requirements" );

			try {

				// Get controlling properties
				String artifactTypeForCSVExport = _context.getCSVExportArtifactType( underEl );
				String separator = _context.getCSVExportSeparator( underEl );
				boolean isNameForCVSExport = _context.getCSVExportIncludeArtifactName( underEl );

				_context.debug( "exportRequirementsToCSV CSVExportSeparator=" + separator + 
						", CSVExportArtifactType=" + artifactTypeForCSVExport + 
						", CVSExportIncludeArtifactName=" + isNameForCVSExport );

				FileWriter myWriter = new FileWriter( theFileName );

				if( !separator.equals("," ) ){
					String info = "sep=" + separator;
					_context.info( "Added " + info + " to export as CSVExportSeparator property is not a comma" );
					myWriter.write( info + "\n" );
				}
				
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
					
					String theSpecification = removeCSVIncompatibleCharsFrom( theReqt.getSpecification() );

					if( isNameForCVSExport ){
						theLine = artifactTypeForCSVExport + separator + theReqt.getName() + separator + theSpecification;
					} else {
						theLine = artifactTypeForCSVExport + separator + theSpecification;
					}
					
					AnnotationMap theAnnotationMap = new AnnotationMap( theReqt, _context );

					for( String theAdditionalHeading : theAdditionalHeadings ){
						
						theLine += separator;

						List<IRPAnnotation> theSpecificAnnotations = theAnnotationMap.get( theAdditionalHeading );
						
						Iterator<IRPAnnotation> iterator = theSpecificAnnotations.iterator();
						
						while( iterator.hasNext() ){
							
							IRPAnnotation theSpecificAnnotation = (IRPAnnotation) iterator.next();
							
							String theDescription = removeCSVIncompatibleCharsFrom( theSpecificAnnotation.getDescription() );
							
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

			} catch( Exception e ){

				_context.error( "Exception, e=" + e.getMessage() );
				System.out.println( "An error occurred." );
				e.printStackTrace();
			}
		}
	}

	private String chooseAFileToImport(
			String theFilename ){

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
			}
		} else {
			_context.debug( "User chose to cancel" );
		}

		return theFilename;
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