package com.mbsetraining.sysmlhelper.functionaldesignplugin;

import java.io.File;
import java.util.List;

import javax.swing.JFileChooser;
import javax.swing.filechooser.FileNameExtensionFilter;

import com.mbsetraining.sysmlhelper.common.UserInterfaceHelper;
import com.telelogic.rhapsody.core.*;

public class DesignSpecificationPackageCreatorFromXML {

	IRPObjectModelDiagram m_FunctionHierarchyBDD = null;
	IRPObjectModelDiagram m_SystemContextDiagram = null;

	FunctionalDesign_Context _context;
	
	public void openFunctionHierarchyBDD (){

		if( m_FunctionHierarchyBDD != null ){
			m_FunctionHierarchyBDD.openDiagram();
		}
	}

	public void openSystemContextDiagram (){

		if( m_SystemContextDiagram != null ){
			m_SystemContextDiagram.openDiagram();
		}
	}

	public DesignSpecificationPackageCreatorFromXML(
			IRPProject theRhpPrj,
			List<IRPActor> theMasterActors,
			FunctionalDesign_Context context ){

		_context = context;
		
		String explainStr = 
				"This 'Import from XML' " +
						" helper creates a nested UML model structure " +
						"based on a defining .xml file\n"+
						"and the following " + theMasterActors.size() + 
						" actors defined in the master actor list:\n";
		
		explainStr += _context.buildStringFromModelEls( theMasterActors, 5 );
		explainStr += "\nDo you want to continue with selecting a .xml file?\n\n";

		boolean theAnswer = UserInterfaceHelper.askAQuestion( explainStr );

		if( theAnswer ){

			String theFilename = chooseAFileToImport( theRhpPrj );

			_context.debug( "theFilename is " + theFilename );
			
			DesignSpecificationPackages thePackages =
					new DesignSpecificationPackages( 
							theFilename, 
							theRhpPrj, 
							theMasterActors,
							_context );
			
			thePackages.dumpPackages();
			
			List<String> errorMsgs = thePackages.getErrorMsgs();
			
			if( errorMsgs.isEmpty() ){
								
				String theMsg = "There are " + thePackages.size() + 
						" design package specifications defined in " + theFilename + ":\n";
				
				theMsg += _context.buildStringFrom( 
						thePackages.getPackageNames(), 5 );
				
				theMsg += "\nDo you want to create their corresponding package structures under " + 
						_context.elInfo(theRhpPrj) + "?";
				
				boolean isContinue = UserInterfaceHelper.askAQuestion( theMsg );
				
				if( isContinue ){
					thePackages.createPackages();
				}
			} else {
				String theMsg = "Sorry, unable to proceed as there are at least " + errorMsgs.size() + " problems found with " + theFilename + "\n\n";
				
				theMsg += _context.buildStringFrom( errorMsgs, 5 );
			
				UserInterfaceHelper.showWarningDialog( theMsg );
			}
		}
	}
	
	public String chooseAFileToImport( 
			IRPProject thePrj ){

		String theFilename = null;
		
		JFileChooser fc = new JFileChooser();
		
		fc.addChoosableFileFilter( new XMLFileFilter() );
		fc.setFileFilter( 
				new FileNameExtensionFilter( 
						"xml file", 
						"xml" ) );

		String path = "";

		path = thePrj.getCurrentDirectory();		

		File fp = new File( path );
		fc.setCurrentDirectory( fp );	
		
		int result = fc.showOpenDialog( null );

		if( result == JFileChooser.APPROVE_OPTION ){
			
			File selFile = fc.getSelectedFile();
			
			if( selFile == null || selFile.getName().equals("") ){
				_context.warning( "No file selected" );
			} else {
				theFilename = selFile.getAbsolutePath();

				if (!theFilename.endsWith(".xml")){
					theFilename = theFilename + ".xml";
				}
			}
		}
		
		return theFilename;
	}
	
	public class XMLFileFilter extends javax.swing.filechooser.FileFilter {
		String fileType="xml";
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
 * Copyright (C) 2018-2021  MBSE Training and Consulting Limited (www.executablembse.com)

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