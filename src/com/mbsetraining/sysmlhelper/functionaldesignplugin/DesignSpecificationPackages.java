package com.mbsetraining.sysmlhelper.functionaldesignplugin;

import java.util.ArrayList;
import java.util.List;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import com.telelogic.rhapsody.core.IRPActor;
import com.telelogic.rhapsody.core.IRPPackage;
import com.telelogic.rhapsody.core.IRPProject;

public class DesignSpecificationPackages extends ArrayList<DesignSpecificationPackage>{

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	
	IRPPackage m_OwnerPkg = null;
	IRPProject m_RhpPrj = null;
	List<IRPActor> m_MasterActors = null;
	FunctionalDesign_Context _context;
	
	DesignSpecificationPackages(
			String theFilename,
			IRPPackage theOwnerPkg,
			List<IRPActor> theMasterActors,
			FunctionalDesign_Context context ){
		
		_context = context;
		
		m_OwnerPkg = theOwnerPkg;
		m_RhpPrj = m_OwnerPkg.getProject();
		m_MasterActors = theMasterActors;
		
		parseXmlFile( theFilename );
	}
	
	private void parseXmlFile(
			String theFilename ){
		
		Document doc;

		//get the factory
		DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();

		try {
			//Using factory get an instance of document builder
			DocumentBuilder db = dbf.newDocumentBuilder();

			//parse using builder to get DOM representation of the XML file
			doc = db.parse( theFilename );
			doc.getDocumentElement().normalize();
			_context.debug( "Root element :" + doc.getDocumentElement().getNodeName() );
			NodeList nList = doc.getElementsByTagName( "package_structure" );
			_context.debug( "----------------------------" );

			for( int i = 0; i < nList.getLength(); i++ ){
				
				Node nNode = nList.item(i);
				_context.debug("\nCurrent Element :" + nNode.getNodeName());

				if (nNode.getNodeType() == Node.ELEMENT_NODE) {
					
					Element eElement = (Element) nNode;
					
					String thePackageName = 							
							eElement.getAttribute( "package_name" );
					
					String theNewTermStereotype = 
							eElement
							.getElementsByTagName("new_term_stereotype")
							.item(0)
							.getTextContent();
					
					String theShortName = 
							eElement
							.getElementsByTagName("short_name")
							.item(0)
							.getTextContent();
					
					String theDescription =
							eElement
							.getElementsByTagName("description")
							.item(0)
							.getTextContent();
					
					String theFunctionName =
							eElement
							.getElementsByTagName("function_name")
							.item(0)
							.getTextContent();
					
					String theFunctionDescription =
							eElement
							.getElementsByTagName("function_description")
							.item(0)
							.getTextContent();
					
					boolean isCreateParametricPackage = eElement
							.getElementsByTagName("create_parametric_package")
							.item(0)
							.getTextContent().equals("true");
					
					DesignSpecificationPackage thePackage =
							new DesignSpecificationPackage( 
									m_OwnerPkg,
									m_MasterActors,
									thePackageName, 
									theNewTermStereotype, 
									theShortName, 
									theDescription, 
									theFunctionName, 
									theFunctionDescription, 
									isCreateParametricPackage,
									_context );
					
					this.add( thePackage );
				}
			}
		} catch( Exception e ){
			_context.error( "Exception in parseXmlFile, e=" + e.getMessage() );
			e.printStackTrace();
		}
	}
	
	public void dumpPackages(){
		
		_context.debug("---------------------------------------------------------");
		_context.debug("There are " + this.size() + " design package definitions:");
		
		for( DesignSpecificationPackage thePackage : this ){
			_context.debug("---------------------------------------------------------");
			thePackage.dumpPackage();
		}
		
		_context.debug("---------------------------------------------------------");
	}
	
	public List<String> getPackageNames(){
		
		List<String> theRootPackages = new ArrayList<>();
		
		for( DesignSpecificationPackage thePackage : this ){
			theRootPackages.add( thePackage.get_packageName() );
		}
		
		return theRootPackages;
	}
	
	public void createPackages(){
		
		for( DesignSpecificationPackage thePackage : this ){
			thePackage.createPackage();
		}
	}
	
	public List<String> getErrorMsgs(){
				
		List<String> errorMsgs = new ArrayList<>();
		
		for( DesignSpecificationPackage thePackage : this ){
			
			String errorMsg = thePackage.getErrorMsg();
			
			if( !errorMsg.isEmpty() ){
				errorMsgs.add( errorMsg );
			}
		}
		
		return errorMsgs;
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