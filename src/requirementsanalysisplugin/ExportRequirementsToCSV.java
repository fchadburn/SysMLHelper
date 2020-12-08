package requirementsanalysisplugin;

import generalhelpers.Logger;
import generalhelpers.StereotypeAndPropertySettings;

import java.io.File;
import java.io.FileWriter;
import java.util.List;

import javax.swing.JFileChooser;
import javax.swing.filechooser.FileNameExtensionFilter;

import com.telelogic.rhapsody.core.*;

public class ExportRequirementsToCSV {

	IRPApplication _rhpApp;
	IRPProject _rhpPrj;
	IRPModelElement _selectedEl;

	public ExportRequirementsToCSV(
			String appID ) {

		_rhpApp = RhapsodyAppServer.getActiveRhapsodyApplicationByID( appID );
		_rhpPrj = _rhpApp.activeProject();
		_selectedEl = _rhpApp.getSelectedElement();
	}

	public void exportRequirementsToCSVUnderSelectedEl(){
		exportRequirementsToCSV( _selectedEl, 1 );
	}

	public void exportRequirementsToCSV(
			IRPModelElement underEl,
			int recursive ){

		@SuppressWarnings("unchecked")
		List<IRPRequirement> theReqts = underEl.getNestedElementsByMetaClass( 
				"Requirement", recursive ).toList();

		Logger.writeLine( "There are " + theReqts.size() + " requirements under " + Logger.elementInfo( underEl ) );

		for (IRPRequirement theReqt : theReqts) {
			Logger.writeLine( Logger.elementInfo( theReqt ) );
		}

		String theFileName = chooseAFileToImport( underEl.getName() + ".csv" );

		if( theFileName == null ){

		} else {
			Logger.writeLine( "User chose " + theFileName + " to export " + theReqts.size() + " requirements" );

			try {

				String artifactTypeForCSVExport = StereotypeAndPropertySettings.getCSVExportArtifactType( underEl );
				boolean isNameForCVSExport = StereotypeAndPropertySettings.getCVSExportIncludeArtifactName( underEl );

				FileWriter myWriter = new FileWriter( theFileName );

				if( isNameForCVSExport ){
					myWriter.write( "Artifact Type,Name,Primary Text\n" );
				} else {
					myWriter.write( "Artifact Type,Primary Text\n" );
				}

				for (IRPRequirement theReqt : theReqts) {

					if( isNameForCVSExport ){
						myWriter.write( artifactTypeForCSVExport + "," + theReqt.getName() + "," + theReqt.getSpecification() + "\n" );
					} else {
						myWriter.write( artifactTypeForCSVExport + "," + theReqt.getSpecification() + "\n" );
					}
				}

				myWriter.close();

			} catch( Exception e ){

				Logger.writeLine( "Exception, e=" + e.getMessage() );
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

		path = _rhpPrj.getCurrentDirectory();		

		File fp = new File( path );
		fc.setCurrentDirectory( fp );	

		int result = fc.showSaveDialog( null );

		if( result == JFileChooser.APPROVE_OPTION ){

			File selFile = fc.getSelectedFile();

			if( selFile == null || selFile.getName().equals("") ){
				Logger.writeLine( "No file selected" );
			} else {
				theFilename = selFile.getAbsolutePath();

				if (!theFilename.endsWith(".csv")){
					theFilename = theFilename + ".csv";
				}
			}
		} else {
			Logger.writeLine( "User chose to cancel" );
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
	
	public static void main(String[] args) {

		IRPApplication theRhpApp = RhapsodyAppServer.getActiveRhapsodyApplication();

		ExportRequirementsToCSV theExporter = new ExportRequirementsToCSV( 
				theRhpApp.getApplicationConnectionString() );

		theExporter.exportRequirementsToCSVUnderSelectedEl();
	}
}

/**
 * Copyright (C) 2020  MBSE Training and Consulting Limited (www.executablembse.com)

    Change history:
    #266 07-DEC-2020: Add initial support for CVS export & switching master of requirements to DOORS NG
    
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