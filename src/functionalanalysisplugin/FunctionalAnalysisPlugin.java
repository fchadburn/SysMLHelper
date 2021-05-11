package functionalanalysisplugin;

import functionalanalysisplugin.PopulateFunctionalAnalysisPkg.SimulationType;
import generalhelpers.ConfigurationSettings;
import generalhelpers.Logger;
import generalhelpers.TraceabilityHelper;
import generalhelpers.UserInterfaceHelpers;

import java.util.List;
import java.util.Set;

import com.telelogic.rhapsody.core.*;
 
public class FunctionalAnalysisPlugin extends RPUserPlugin {
  
	protected static IRPApplication m_rhpApplication = null;
	protected static ConfigurationSettings m_configSettings = null;
	
	// plug-in is loaded
	public void RhpPluginInit(final IRPApplication theRhapsodyApp) {
		
		m_rhpApplication = theRhapsodyApp;
		
		m_configSettings = new ConfigurationSettings(
				m_rhpApplication,
				m_rhpApplication.activeProject(),
				"SysMLHelper.properties", 
				"SysMLHelper_MessagesBundle",
				"SysMLHelper" );		
		
		String msg = "The FunctionalAnalysisPlugin component of the SysMLHelperPlugin V" + m_configSettings.getProperty("PluginVersion") 
				+ " was loaded successfully. New right-click 'MBSE Method' commands have been added.";		
		
		Logger.writeLine(msg); 
	}

	public static IRPApplication getRhapsodyApp(){
		
		if (m_rhpApplication==null){
			m_rhpApplication = RhapsodyAppServer.getActiveRhapsodyApplication();
		}
		
		return m_rhpApplication;
	}
	
	public static IRPProject getActiveProject(){
		
		return getRhapsodyApp().activeProject();
	}
	
	// called when the plug-in pop-up menu (if applicable) is selected
	public void OnMenuItemSelect(String menuItem) {
	
		if( UserInterfaceHelpers.checkOKToRunAndWarnUserIfNot() ){
			IRPModelElement theSelectedEl = getRhapsodyApp().getSelectedElement();
			IRPProject theActiveProject = getRhapsodyApp().activeProject();
			
			@SuppressWarnings("unchecked")
			List<IRPGraphElement> theSelectedGraphEls = getRhapsodyApp().getSelectedGraphElements().toList();
			
			@SuppressWarnings("unchecked")
			List<IRPModelElement> theSelectedEls = getRhapsodyApp().getListOfSelectedElements().toList();

			Logger.writeLine("Starting ("+ theSelectedEls.size() + " elements were selected) ...");
			
			if( !theSelectedEls.isEmpty() ){
				
				if (menuItem.equals(m_configSettings.getString("functionalanalysisplugin.PopulateFullSimPackageHierarchyForAnalysisBlockMenu"))){

					try {
						if (theSelectedEl instanceof IRPPackage){
							PopulateFunctionalAnalysisPkg.createFunctionalBlockPackageHierarchy( (IRPPackage)theSelectedEl, SimulationType.FullSim, m_configSettings );
						}

					} catch (Exception e) {
						Logger.writeLine("Error: Exception in OnMenuItemSelect when invoking PopulateFunctionalAnalysisPkg.createFunctionalBlockPackageHierarchy (FullSim)");
					}

				} else if (menuItem.equals(m_configSettings.getString("functionalanalysisplugin.PopulateSimpleSimPackageHierarchyForAnalysisBlockMenu"))){

					try {
						if (theSelectedEl instanceof IRPPackage){
							PopulateFunctionalAnalysisPkg.createFunctionalBlockPackageHierarchy( (IRPPackage)theSelectedEl, SimulationType.SimpleSim, m_configSettings );
						}

					} catch (Exception e) {
						Logger.writeLine("Error: Exception in OnMenuItemSelect when invoking PopulateFunctionalAnalysisPkg.createFunctionalBlockPackageHierarchy (SimpleSim)");
					}

				} else if (menuItem.equals(m_configSettings.getString("functionalanalysisplugin.PopulateNoSimPackageHierarchyForAnalysisBlockMenu"))){

					try {
						if (theSelectedEl instanceof IRPPackage){
							PopulateFunctionalAnalysisPkg.createFunctionalBlockPackageHierarchy( (IRPPackage)theSelectedEl, SimulationType.NoSim, m_configSettings );
						}

					} catch (Exception e) {
						Logger.writeLine("Error: Exception in OnMenuItemSelect when invoking PopulateFunctionalAnalysisPkg.createFunctionalBlockPackageHierarchy (NoSim)");
					}

				} else if( menuItem.equals( m_configSettings.getString(
						"functionalanalysisplugin.CreateIncomingEventMenu" ) ) ){

					try{
						CreateIncomingEventPanel.launchThePanel();

					} catch (Exception e) {
						Logger.writeLine( "Error: Exception in OnMenuItemSelect when invoking " +
								"CreateIncomingEventPanel.launchThePanel e=" + e.getMessage() );
					}

				} else if( menuItem.equals( m_configSettings.getString(
						"functionalanalysisplugin.CreateAnOperationMenu" ) ) ){

					try{
						CreateOperationPanel.launchThePanel();

					} catch( Exception e ){
						Logger.writeLine( "Error: Exception in OnMenuItemSelect when invoking " +
								"CreateOperationPanel.launchThePanel e=" + e.getMessage() );
					}
					
				} else if( menuItem.equals( m_configSettings.getString(
						"functionalanalysisplugin.CreateOutgoingEventMenu" ) ) ){

					try{
						CreateOutgoingEventPanel.launchThePanel( m_configSettings );

					} catch( Exception e ){
						Logger.writeLine( "Error: Exception in OnMenuItemSelect when invoking " +
								"CreateOutgoingEventPanel.launchThePanel e=" + e.getMessage() );
					}

				} else if( menuItem.equals( m_configSettings.getString(
						"functionalanalysisplugin.CreateAttributeMenu" ) ) ){

					try{
						CreateTracedAttributePanel.launchThePanel( m_configSettings );

					} catch( Exception e ){
						Logger.writeLine( "Error: Exception in OnMenuItemSelect when invoking " +
								"CreateOperationPanel.launchThePanel e=" + e.getMessage() );
					}
					
//					if( theSelectedGraphEls.isEmpty() && ( 
//							theSelectedEl instanceof IRPClass ||
//							theSelectedEl instanceof IRPInstance ||
//							theSelectedEl instanceof IRPDiagram ) ){
//						
//						Set<IRPRequirement> theReqts = new HashSet<IRPRequirement>();
//						
//						CreateTracedAttributePanel.launchThePanel(
//								null,
//								theSelectedEl, 
//								theReqts, 
//								theActiveProject );
//						
//					} else if (!theSelectedGraphEls.isEmpty()){
//						try {
//							CreateTracedAttributePanel.createSystemAttributesFor( theActiveProject, theSelectedGraphEls );
//							
//						} catch (Exception e) {
//							Logger.writeLine("Error: Exception in OnMenuItemSelect when invoking CreateTracedAttributePanel.createSystemAttributeFor");
//						}
//					}

				} else if (menuItem.equals(m_configSettings.getString("functionalanalysisplugin.UpdateAttributeOrCheckOpMenu"))){

					if ( theSelectedEl instanceof IRPAttribute ){
						try {
							Set<IRPRequirement> theReqts = 
									TraceabilityHelper.getRequirementsThatTraceFrom( theSelectedEl, false );
							
							UpdateTracedAttributePanel.launchThePanel( 
									(IRPAttribute)theSelectedEl, theReqts, theActiveProject );
							
						} catch (Exception e) {
							Logger.writeLine("Error: Exception in OnMenuItemSelect when invoking UpdateTracedAttributePanel.launchThePanel");
						}
					}

				} else if( menuItem.equals( m_configSettings.getString(
						"functionalanalysisplugin.CreateEventForAttributeMenu" ) ) ){

					try{
						CreateIncomingEventPanel.launchThePanel();

					} catch( Exception e ){
						Logger.writeLine( "Error: Exception in OnMenuItemSelect when invoking " +
								"CreateIncomingEventPanel.launchThePanel, e=" + e.getMessage() );
					}		

				} else if (menuItem.equals(m_configSettings.getString("functionalanalysisplugin.DeriveDownstreamRequirementMenu"))){
					
					if (!theSelectedGraphEls.isEmpty()){
						try {
							CreateDerivedRequirementPanel.deriveDownstreamRequirement( theSelectedGraphEls );

						} catch (Exception e) {
							Logger.writeLine("Error: Exception in OnMenuItemSelect when invoking CreateDerivedRequirementPanel.launchThePanel");
						}
					}

				} else if (menuItem.equals(m_configSettings.getString("functionalanalysisplugin.CreateNewTestCaseForTestDriverMenu"))){

					if (theSelectedEl instanceof IRPClass){
						try {
							OperationCreator.createTestCaseFor( (IRPClass) theSelectedEl );

						} catch (Exception e) {
							Logger.writeLine("Error: Exception in OnMenuItemSelect when invoking OperationCreator.createTestCaseFor");
						}
					} else if (theSelectedEl instanceof IRPSequenceDiagram){
						
						try {
							TestCaseCreator.createTestCaseFor( (IRPSequenceDiagram) theSelectedEl );
							
						} catch (Exception e) {
							Logger.writeLine("Error: Exception in OnMenuItemSelect when invoking TestCaseCreator.createTestCaseFor");
						}
					}

				} else if (menuItem.equals(m_configSettings.getString("functionalanalysisplugin.AddNewActorToPackageMenu"))){

					if (theSelectedEl instanceof IRPPackage){
						try {
							CreateNewActorPanel.launchThePanel();

						} catch (Exception e) {
							Logger.writeLine("Error: Exception in OnMenuItemSelect when invoking PopulateFunctionalAnalysisPkg.addNewActorToPackageUnderDevelopement");
						}
					}

				} else if (menuItem.equals(m_configSettings.getString("functionalanalysisplugin.AddNewBlockPartToPackageMenu"))){

					if (theSelectedEl instanceof IRPPackage || theSelectedEl instanceof IRPDiagram ){
						try {
							CreateNewBlockPartPanel.launchThePanel();

						} catch (Exception e) {
							Logger.writeLine("Error: Exception in OnMenuItemSelect when invoking PopulateFunctionalAnalysisPkg.AddNewBlockPartToPackageMenu");
						}
					}
					
				} else if (menuItem.equals(m_configSettings.getString("functionalanalysisplugin.CopyActivityDiagramsMenu"))){

					if (theSelectedEl instanceof IRPPackage){
						try {
							PopulateFunctionalAnalysisPkg.copyActivityDiagrams( (IRPProject) theSelectedEl ); 

						} catch (Exception e) {
							Logger.writeLine("Error: Exception in OnMenuItemSelect when invoking PopulateFunctionalAnalysisPkg.CopyActivityDiagramsMenu");
						}
					}							

				} else if (menuItem.equals(m_configSettings.getString("functionalanalysisplugin.DeleteEventsAndRelatedElementsMenu"))){

					try {
						EventDeletion.deleteEventAndRelatedElementsFor( theSelectedEls );
						
					} catch (Exception e) {
						Logger.writeLine("Error: Exception in OnMenuItemSelect when invoking EventDeletion.deleteEventAndRelatedElementsFor");
					}

//				} else if (menuItem.equals(m_configSettings.getString("functionalanalysisplugin.SwitchMenusToMoreDetailedADMenu"))){
//
//					try {
//						if( theSelectedEl instanceof IRPActivityDiagram ){
//							
//							IRPActivityDiagram theAD = (IRPActivityDiagram)theSelectedEl;
//							
//							int isOpen = theAD.isOpen();
//							
//							PopulateFunctionalAnalysisPkg.switchToMoreDetailedAD( 
//									(IRPActivityDiagram)theSelectedEl );
//							
//							if( isOpen==1 ){
//								theAD.highLightElement();
//							}
//						}
//						
//					} catch (Exception e) {
//						Logger.writeLine("Error: Exception in OnMenuItemSelect when invoking MBSE Method: Functional Analysis\\Switch menus to �MoreDetailedAD�");
//					}
					
				} else if (menuItem.equals(m_configSettings.getString("functionalanalysisplugin.SwitchMenusToFullSim"))){

					try {
						PopulateFunctionalAnalysisPkg.switchFunctionalAnalysisPkgProfileFrom(
								"FunctionalAnalysisSimpleProfile", "FunctionalAnalysisProfile", theActiveProject );
						
					} catch (Exception e) {
						Logger.writeLine("Error: Exception in OnMenuItemSelect when invoking MBSE Method: Functional Analysis\\Switch menus to full sim");
					}
					
				} else if (menuItem.equals(m_configSettings.getString("functionalanalysisplugin.SwitchMenusToSimpleSim"))){

					try {
						PopulateFunctionalAnalysisPkg.switchFunctionalAnalysisPkgProfileFrom(
								"FunctionalAnalysisProfile", "FunctionalAnalysisSimpleProfile", theActiveProject );
						
					} catch (Exception e) {
						Logger.writeLine("Error: Exception in OnMenuItemSelect when invoking MBSE Method: Functional Analysis\\Switch menus to full sim");
					}

				} else if (menuItem.equals(m_configSettings.getString("functionalanalysisplugin.RecreateAutoShowSequenceDiagramMenu"))){

					try {
						if( theSelectedEl instanceof IRPSequenceDiagram ){

							SequenceDiagramHelper.updateLifelinesToMatchPartsInActiveBuildingBlock(
									(IRPSequenceDiagram) theSelectedEl );
							
						}	
											
					} catch (Exception e) {
						Logger.writeLine("Error: Exception in OnMenuItemSelect when invoking MBSE Method: Functional Analysis\\Re-create �AutoShow� sequence diagram");
					}
					

				} else {
					Logger.writeLine(theSelectedEl, " was invoked with menuItem='" + menuItem + "'");
				}
			} // else No selected element

			Logger.writeLine("... completed");
		}
	}
	
	public boolean RhpPluginCleanup() {
		m_rhpApplication = null;
		return true; // true=unload plugin
	}

	@Override
	public void RhpPluginInvokeItem() {		
	}

	@Override
	public void RhpPluginFinalCleanup() {		
	}

	@Override
	public void OnTrigger(String trigger) {		
	}
}

/**
 * Copyright (C) 2016-2019  MBSE Training and Consulting Limited (www.executablembse.com)

    Change history:
    #006 02-MAY-2016: Add FunctionalAnalysisPkg helper support (F.J.Chadburn)
    #011 08-MAY-2016: Simplify version numbering mechanism (F.J.Chadburn)
    #013 10-MAY-2016: Add support for sequence diagram req't and verification relation population (F.J.Chadburn)
    #016 11-MAY-2016: Add GPL advisory to the Log window (F.J.Chadburn)
    #022 30-MAY-2016: Improved handling and validation of event/operation creation by adding new forms (F.J.Chadburn)
    #025 31-MAY-2016: Add new menu and dialog to add a new actor to package under development (F.J.Chadburn)
    #027 31-MAY-2016: Add new menu to launch dialog to copy Activity Diagrams (F.J.Chadburn)
    #028 01-JUN-2016: Add new menu to create a stand-alone attribute owned by the system (F.J.Chadburn)
    #034 05-JUN-2016: Re-factored design to move static constructors into appropriate panel class (F.J.Chadburn)
    #047 06-JUL-2016: Tweaked properties and added options to switch to MoreDetailedAD automatically (F.J.Chadburn)
    #083 09-AUG-2016: Add an Update attribute menu option and panel with add check operation option (F.J.Chadburn)
    #084 09-AUG-2016: Add new right-click menu to create an incoming event for an attribute (F.J.Chadburn)
    #099 14-SEP-2016: Allow event and operation creation from right-click on AD and RD diagram canvas (F.J.Chadburn)
    #109 06-NOV-2016: Added .properties support for localisation of menus (F.J.Chadburn)
    #110 06-NOV-2016: PluginVersion now comes from Config.properties file, rather than hard wired (F.J.Chadburn)
    #111 13-NOV-2016: Added new Simple Sim (Guard only) functional analysis structure option (F.J.Chadburn)
    #112 13-NOV-2016: Added new No Sim functional analysis structure option (F.J.Chadburn)
    #117 13-NOV-2016: Get incoming and outgoing event dialogs to work without actors in the context (F.J.Chadburn)
    #128 25-NOV-2016: Improved usability/speed of Copy AD dialog by providing user choice to open diagrams (F.J.Chadburn)
    #137 02-DEC-2016: Allow 'create attribute' menu command on AD/RD canvas right-click (F.J.Chadburn)
    #179 29-MAY-2017: Add new Functional Analysis menu to Re-create �AutoShow� sequence diagram (F.J.Chadburn)
    #196 05-JUN-2017: Enhanced create traced element dialogs to be context aware for blocks/parts (F.J.Chadburn)
    #209 04-JUL-2017: Populate requirements for SD(s) based on messages now supported with Dialog (F.J.Chadburn)
    #216 09-JUL-2017: Added a new Add Block/Part command added to the Functional Analysis menus (F.J.Chadburn)
    #222 12-JUL-2017: Allow AddNewBlockPartToPackageMenu to work when right-clicking IBDs and BDDs (F.J.Chadburn) 			
    #230 20-SEP-2017: Initial alpha trial for create test case script from a sequence diagram (F.J.Chadburn)
    #239 04-OCT-2017: Improve warning/behaviour if multiple Rhapsodys are open or user switches app (F.J.Chadburn)
    #256 11-SEP-2018: Move populate requirements/update verifications for SD(s) menus to Reqts menu (F.J.Chadburn)
    #252 29-MAY-2019: Implement generic features for profile/settings loading (F.J.Chadburn)
    #256 29-MAY-2019: Rewrite to Java Swing dialog launching to make thread safe between versions (F.J.Chadburn)

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

