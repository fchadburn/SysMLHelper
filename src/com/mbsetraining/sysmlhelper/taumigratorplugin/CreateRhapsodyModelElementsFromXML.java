package com.mbsetraining.sysmlhelper.taumigratorplugin;

import java.io.File;
import java.util.List;

import javax.swing.UIManager;

public class CreateRhapsodyModelElementsFromXML {

	protected TauMigrator_Context _context;

	public CreateRhapsodyModelElementsFromXML( 
			TauMigrator_Context context ) {

		_context = context;
	}

	public void go(){

		try {
			setLookAndFeel();

			_context.info("CreateRhapsodyModelElementsFromXML.go was invoked");

			String thePath = "C:\\Users\\frase\\Documents\\XXX\\";

			RhpEl parentNode = new RhpElProject( _context.get_rhpPrj().getName(), "Project", "xxxxx", _context );

			ModelBuilder theElementStructure = new ModelBuilder( _context );

			theElementStructure.parseXmlFile( thePath + "File1.u2", parentNode );

			performXMLImportFrom( parentNode );	

		} catch (Exception e) {
			_context.info( "Exception in Go, e=" + e.getMessage() );
		}		
	}

	private void performXMLImportFrom(
			RhpEl parentNode ){

		_context.info( "Importing from: " );//+ filename );

		@SuppressWarnings("unused")
		int nodeCount  = parentNode.getNodeCount();

		//		List<String> theWarnings = 
		//				parentNode.getWarnings();

		List<String> theInfos = 
				parentNode.getInfos();

		_context.info("+=================================================");

		_context.info("The tree contains " + theInfos.size() + " elements:");
		for (String theInfo : theInfos) {
			_context.info( theInfo );
		}
		_context.info("... end of tree (" + theInfos.size() + ")");
		_context.info("+=================================================");

		// pass parent node so that element can search full tree
		parentNode.createNodeElementsAndChildrenForJustEvents(
				_context.getSelectedElement(),
				parentNode );

		// pass parent node so that element can search full tree
		parentNode.createNodeElementsAndChildren(
				_context.getSelectedElement(),
				parentNode );

		// pass parent node so that element can search full tree
		parentNode.createRelationshipsAndChildren(
				parentNode );

		parentNode.addMergeNodes(parentNode);
		parentNode.reflowControlNodesFromReceiveEvents(parentNode);
		parentNode.addElseTransitionsIfNeeded(parentNode);

		_context.info( "Saving" );
		_context.get_rhpApp().saveAll();

		_context.info( "Import Complete");
	}


	@SuppressWarnings("unused")
	private boolean isValidFile(String path){
		File f = new File(path);
		if (!f.exists())
			return false;

		if (!f.getAbsolutePath().endsWith(".u2"))
			return false;

		return true;
	}

	public void setLookAndFeel(){
		try{
			UIManager.setLookAndFeel(
					"com.sun.java.swing.plaf.windows.WindowsLookAndFeel");

		} catch (Exception e){
			_context.info("Unhandled exception invoking UIManager.setLookAndFeel");
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