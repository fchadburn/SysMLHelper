package generalhelpers;

import java.awt.BorderLayout;
import java.awt.Component;
import java.io.File;
import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.swing.BorderFactory;
import javax.swing.BoxLayout;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;

import com.mbsetraining.sysmlhelper.common.UserInterfaceHelper;
import com.telelogic.rhapsody.core.*;

public class CreateGatewayProjectPanel extends CreateStructuralElementPanel {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	private GatewayFileParser m_ChosenTypesFile;
	private GatewayFileParser m_ChosenProjectFile;
	private List<GatewayDocumentPanel> m_GatewayDocumentPanel = new ArrayList<GatewayDocumentPanel>();

	public static void launchThePanel(
			final String theAppID,
			final String lookingForRqtfTemplatesThatMatchRegEx ){

		javax.swing.SwingUtilities.invokeLater(new Runnable() {

			@Override
			public void run() {				
				JFrame.setDefaultLookAndFeelDecorated( true );

				JFrame frame = new JFrame(
						"Setup the Rhapsody Gateway for the project?");

				frame.setDefaultCloseOperation( JFrame.DISPOSE_ON_CLOSE );

				CreateGatewayProjectPanel thePanel = 
						new CreateGatewayProjectPanel(
								theAppID,
								lookingForRqtfTemplatesThatMatchRegEx );

				frame.setContentPane( thePanel );
				frame.pack();
				frame.setLocationRelativeTo( null );
				frame.setVisible( true );
			}
		});
	}

	public boolean determineGatewayProjectAndTypesFile(
			final String lookingForRqtfTemplatesThatMatchRegEx  ){

		boolean success = false;

		String theReqtsPkgExpectedName = "RequirementsPkg";

		final List<IRPModelElement> theRequirementsPkgs = 
				_context.findElementsWithMetaClassAndName(
						"Package", 
						theReqtsPkgExpectedName, 
						_context.get_rhpPrj() );

		String theSysMLHelperProfilePath =
				RhapsodyAppServer.getActiveRhapsodyApplication().getOMROOT() + 
				"\\Profiles\\SysMLHelper\\SysMLHelper_rpy";

		if( theRequirementsPkgs != null && !theRequirementsPkgs.isEmpty() ){

			File theChosenRqtfFile = getFile(
					lookingForRqtfTemplatesThatMatchRegEx,
					theSysMLHelperProfilePath,
					"Which Gateway project template do you want to use?" );

			if( theChosenRqtfFile != null ){

				boolean isLaunchDialog = true;

				String theCandidateTypesFileName = 
						theChosenRqtfFile.getName().substring(
								0, theChosenRqtfFile.getName().length()-5 ) + ".types";

				_context.debug( "The corresponding types file is " + theCandidateTypesFileName );

				final File theChosenTypesFile = getFile(
						theCandidateTypesFileName, 
						theSysMLHelperProfilePath,
						"Which Gateway Types template do you want to use?" );

				if( theChosenTypesFile != null ){

					final GatewayFileParser theTemplateProjectFile = new GatewayFileParser( theChosenRqtfFile, _context );
					final GatewayFileParser theTemplateTypesFile = new GatewayFileParser( theChosenTypesFile, _context );

					File theExistingRqtfFile = getFile(
							"^" + _context.get_rhpPrj().getName() + ".rqtf$",
							_context.get_rhpPrj().getCurrentDirectory() + "\\" + _context.get_rhpPrj().getName() + "_rpy",
							"Which existing Types file do you want to use?");

					// if project has an existing types file then we need to consider how to 
					// merge in its contents					
					if( theExistingRqtfFile != null ){

						int answer = JOptionPane.showConfirmDialog(
								null, 
								"Do you want to setup a Gateway project based on a '" + theChosenRqtfFile.getName() + "' template?\n\n" + 
										"Note: This project already has a Gateway project called " + theExistingRqtfFile.getName() + "\n" +
										"Click 'Yes' to merge and see result, 'No' to delete and re-create from template, or 'Cancel' to do nothing \n\n" +
										"If you click 'Cancel' then this can be done later using the 'Setup Gateway based on rqtf template' menu ", 
										"Question?", 
										JOptionPane.YES_NO_CANCEL_OPTION);

						if( answer == JOptionPane.YES_OPTION  ){

							final GatewayFileParser theExistingProjectFile = 
									new GatewayFileParser( theExistingRqtfFile, _context );

							updateTheRqtfFile(
									theTemplateProjectFile,
									theExistingProjectFile );

						} else if( answer == JOptionPane.NO_OPTION ){

							// don't update
							_context.info("Deleting existing Gateway file called " + theExistingRqtfFile.getAbsolutePath() );
							theExistingRqtfFile.delete();

						} else {
							isLaunchDialog = false;
						}

					} else { // no existing rqtf file

						// check to see if model references external packages 
						for( IRPModelElement theReqtsPkg : theRequirementsPkgs ) {

							IRPPackage thePkg = (IRPPackage)theReqtsPkg;

							if( thePkg.isReferenceUnit()==1 ){

								_context.info("Detected that " + _context.elInfo( thePkg ) + " is added by reference");

								IRPUnit theUnit = thePkg.getSaveUnit();

								String theReferencedDir = theUnit.getCurrentDirectory();

								String theProjectName = extractProjectNameFrom( theReferencedDir );

								theExistingRqtfFile = getFile(
										theProjectName + ".rqtf$",
										theReferencedDir,
										"Which Gateway project template in the referenced project do you want to use?" );

								if( theExistingRqtfFile != null ){

									int answer = JOptionPane.showConfirmDialog(
											null, 
											"Do you want to setup a Gateway project based on a '" + theChosenRqtfFile.getName() + "' template?\n\n" + 
													"Note: This project references a project that has a Gateway project called " + theExistingRqtfFile.getName() + "\n" +
													"Click 'Yes' to merge and see result, 'No' to ignore referenced project, or 'Cancel' to do nothing \n\n" +
													"If you click 'Cancel' then this can be done later using the 'Setup Gateway based on rqtf template' menu ", 
													"Question?", 
													JOptionPane.YES_NO_CANCEL_OPTION);

									if( answer == JOptionPane.YES_OPTION  ){

										final GatewayFileParser theExistingProjectFile = 
												new GatewayFileParser( theExistingRqtfFile, _context );

										updateTheRqtfFile(
												theTemplateProjectFile,
												theExistingProjectFile);	

									} else if( answer == JOptionPane.CANCEL_OPTION ){
										isLaunchDialog = false;
									}
								}
							}

							if( theExistingRqtfFile == null ){

								int answer = JOptionPane.showConfirmDialog(
										null, 
										"Do you want to launch the dialog to setup a Gateway project to synchronize requirements \n" +
												"based on a '" + theChosenRqtfFile.getName() + "' template?\n\n" +
												"If you click 'No' then this can be done later using the 'Setup Gateway based on rqtf template' menu ", 
												"Question?", 
												JOptionPane.YES_NO_OPTION );

								if( answer != JOptionPane.YES_OPTION ){
									isLaunchDialog = false;
								}
							}
						}
					}

					if( isLaunchDialog ){
						m_ChosenProjectFile = theTemplateProjectFile;
						m_ChosenTypesFile = theTemplateTypesFile;
						success = true;
					}

				} else {
					_context.error("Error in CreateGatewayProjectPanel.launchThePanel, no types file matching '" + 
							theCandidateTypesFileName + "' was found in " + theSysMLHelperProfilePath);

				}

			} // no rqtf file candidate was found
		} else {
			_context.error("Error in CreateGatewayProjectPanel.launchThePanel, unable to proceed as no packages called " + theReqtsPkgExpectedName + " were found in the project");
		}

		return success;

	}

	CreateGatewayProjectPanel(
			String theAppID,
			String lookingForRqtfTemplatesThatMatchRegEx ){

		super( theAppID );

		String theReqtsPkgExpectedName = "RequirementsPkg";

		final List<IRPModelElement> forSelectablePackages = 
				_context.findElementsWithMetaClassAndName(
						"Package", theReqtsPkgExpectedName, _context.get_rhpPrj() );

		determineGatewayProjectAndTypesFile( lookingForRqtfTemplatesThatMatchRegEx );

		setLayout( new BorderLayout(10,10) );
		setBorder( BorderFactory.createEmptyBorder( 10, 10, 10, 10 ) );

		add( createPanelWithTextCentered(
				"Set up the Gateway project to import the requirements"), 
				BorderLayout.PAGE_START );

		add( createWestCentrePanel( forSelectablePackages ), BorderLayout.CENTER );
		add( createPageEndPanel(), BorderLayout.PAGE_END );
	}

	private Component createWestCentrePanel(
			List<IRPModelElement> forSelectablePackages) {

		JPanel thePanel = new JPanel();
		thePanel.setLayout( new BoxLayout(thePanel, BoxLayout.Y_AXIS ) );	
		thePanel.setAlignmentX(CENTER_ALIGNMENT);

		List<GatewayFileSection> theDocs = m_ChosenProjectFile.getAllTheFileSections();

		GatewayFileSection theTypesDoc = m_ChosenTypesFile.getFileSectionWith("Types");
		String theTypesNames = theTypesDoc.getValueFor("Names");
		String[] theAnalysisTypes = theTypesNames.split(",");

		String theRhapsodyTypeRegEx = ".*Rhapsody.*";

		GatewayFileSection theRhapsodySection = m_ChosenProjectFile.getFileSectionWith( "UML Model" );

		String theReqtsPackageValue = null;

		if( theRhapsodySection != null ){

			theReqtsPackageValue = theRhapsodySection.getVariableXValue("requirementsPackage");

			if( theReqtsPackageValue != null ){
				_context.debug("Found that requirementsPackage=" + theReqtsPackageValue );
			}
		} else {
			_context.error( "Error in createWestCentrePanel, no section was found that matches the regex=" + theRhapsodyTypeRegEx );
		}

		for (GatewayFileSection gatewayDoc : theDocs) {
			try {

				String theDocName = gatewayDoc.getSectionName();

				_context.debug("Found gatewayDoc=" + theDocName + ", isImmutable=" + gatewayDoc.isImmutable());

				// ignore Files and Rhapsody docs
				if (!theDocName.equals("Files") && 
						!gatewayDoc.getValueFor("Type").contains("Rhapsody")){

					IRPModelElement theSelectedPkg = extractPreselectedPackageFor(
							gatewayDoc.getSectionName(), _context.get_rhpPrj(), theReqtsPackageValue );

					if( theSelectedPkg == null ){
						_context.error("Error in CreateGatewayProjectPanel.createWestCentrePanel, extractPreselectedPackageFor returned null for selected package for " + gatewayDoc.getSectionName() );

					} else if ( !forSelectablePackages.contains( theSelectedPkg ) ){
						_context.error("Error in CreateGatewayProjectPanel.createWestCentrePanel, " + _context.elInfo( theSelectedPkg ) + " was not found in the selectable list");
						theSelectedPkg = forSelectablePackages.get(0);						
					}

					GatewayDocumentPanel theGatewayDocumentPanel = 
							new GatewayDocumentPanel(
									theDocName, 
									theAnalysisTypes, 
									gatewayDoc.getValueFor("Type"),
									gatewayDoc.getValueFor("Path"), 
									gatewayDoc.getVariableXValue("baseline"),
									forSelectablePackages,
									theSelectedPkg,
									gatewayDoc.isImmutable(),
									_context );

					m_GatewayDocumentPanel.add( theGatewayDocumentPanel );

					thePanel.add( theGatewayDocumentPanel );
				}

			} catch (FileNotFoundException e) {
				_context.error("Error in createWestCentrePanel, Unhandled FileNotFoundException detected");
			}
		}

		return thePanel;
	}

	private Component createPageEndPanel() {

		JLabel theLabel = new JLabel( "Do you want to proceed?" );
		theLabel.setAlignmentX(CENTER_ALIGNMENT);
		theLabel.setBorder( BorderFactory.createEmptyBorder(10, 10, 10, 10));

		JPanel thePanel = new JPanel();
		thePanel.setLayout( new BoxLayout(thePanel, BoxLayout.Y_AXIS ) );	
		thePanel.setAlignmentX(CENTER_ALIGNMENT);
		thePanel.add( theLabel );

		thePanel.add( createPanelWithTextCentered(
				"If you click Cancel then this can be done later using the 'Setup Gateway based on rqtf template' menu") );

		thePanel.add( createOKCancelPanel() );

		return thePanel;
	}

	private void updateTheRqtfFile(
			final GatewayFileParser theProjectFileToUpdate,
			final GatewayFileParser theExistingProjectFile) {

		List<GatewayFileSection> existingGatewayDocs = theExistingProjectFile.getAllTheFileSections();

		for (GatewayFileSection existingGatewayDoc : existingGatewayDocs) {

			String gatewayDocType = existingGatewayDoc.getValueFor( "Type" );

			if( gatewayDocType != null && !gatewayDocType.contains( "Rhapsody" ) ){

				_context.info("===========================");

				_context.info("The existing project has a gatewayDoc called " + existingGatewayDoc.getSectionName() + 
						" with Type=" + gatewayDocType );

				GatewayFileSection theTemplateDoc = theProjectFileToUpdate.getFileSectionWithType( gatewayDocType );

				if (theTemplateDoc != null ){
					_context.info("A match was found between template doc called " + theTemplateDoc.getSectionName() +
							" and the existing doc called " + existingGatewayDoc.getSectionName());

					theProjectFileToUpdate.renameFileSection( theTemplateDoc.getSectionName(), existingGatewayDoc.getSectionName() );
					theTemplateDoc.setIsImmutable( true );
					theProjectFileToUpdate.replaceGatewayDoc( gatewayDocType, existingGatewayDoc );
				}							
			}		
		}
	}

	private IRPModelElement extractPreselectedPackageFor(
			String theGatewayDocName,
			IRPProject inTheProject,
			String basedOnString ){

		IRPModelElement theEl = null;

		String[] split = basedOnString.replaceAll("Packages/","").split("¥");

		String thePath = null;

		for( int i = split.length-1; i>=0; i-- ) {
			if( split[i].equals( theGatewayDocName ) ){
				thePath = split[i-1].replaceFirst("^\\w+/", "").replace("/", "::");
				break;
			}
		}

		if( thePath != null ){

			theEl = inTheProject.findElementsByFullName( thePath, "Package" );

			if( theEl != null ){
				_context.debug( "Successfully found the specified " + theEl.getFullPathName() + " import package in the project");
			} else {
				_context.warning( "Warning in extractPreselectedPackageFor, " + thePath + " was not found" );
			}
		}

		return theEl;	
	}

	public File getFile(
			final String matchingTheRegEx,
			final String inPathToSearch,
			final String withChoiceOfFileMsg ) {

		_context.debug("Looking in " + inPathToSearch + " for a file names matching '" + matchingTheRegEx +"'");

		List<File> theFiles = _context.getFilesMatching( matchingTheRegEx, inPathToSearch );

		int fileCount = theFiles.size();

		File theCandidateRqtfFile = null;

		if (fileCount==0){
			_context.warning("No file matching " + matchingTheRegEx + " was found in the " + inPathToSearch + " folder");

		} else if (fileCount==1){

			// don't bother with selection dialog
			theCandidateRqtfFile = theFiles.get(0);

		} else {
			/// get user to select
			Object[] options = theFiles.toArray();

			JDialog.setDefaultLookAndFeelDecorated(true);

			Object selectedElement = JOptionPane.showInputDialog(
					null,
					withChoiceOfFileMsg,
					"Input",
					JOptionPane.QUESTION_MESSAGE,
					null,
					options,
					options[0]);

			if (selectedElement != null){

				theCandidateRqtfFile = (File)selectedElement;
				_context.debug("The chosen file was " + theCandidateRqtfFile.getAbsolutePath());
			}
		}

		return theCandidateRqtfFile;
	}

	public String extractProjectNameFrom( 
			String theUnitPath ){

		String theProjectName = null;

		String theRegEx = ".*[/\\\\](.*)_rpy.*"; 

		Pattern thePattern = Pattern.compile( theRegEx );

		Matcher theMatcher = thePattern.matcher( theUnitPath );

		if( theMatcher.find() ){
			theProjectName = theMatcher.group( 1 );
		}

		if (theProjectName==null){
			_context.error( "Error in extractProjectNameFrom, project name could not be extracted in theUnitPath=" + theUnitPath );
		}

		return theProjectName;
	}

	private void createDesignatedReqtPackagesInTheModel() {

		for( GatewayDocumentPanel gatewayDocumentPanel : m_GatewayDocumentPanel ) {

			IRPPackage theRootPkg = gatewayDocumentPanel.getRootPackage();
			String thePkgName = gatewayDocumentPanel.getReqtsPkgName();
			String theStereotypeName = "from" + gatewayDocumentPanel.getAnalysisTypeName();

			IRPModelElement existingPkg = 
					_context.findElementWithMetaClassAndName(
							"Package", thePkgName, _context.get_rhpPrj() );

			if( existingPkg != null ){

				_context.info( "Skipping creation of " + thePkgName + 
						" as package already exists with the name " + thePkgName );

			} else if( theRootPkg.isReadOnly() == 1 ) { // check to see if root package is writable	 

				_context.info( "Skipping creation of " + thePkgName + 
						" as unable to write to " + _context.elInfo( theRootPkg ) );

			} else {
				_context.info( "Create package called '" + thePkgName + "' with the type of analysis '" + 
						gatewayDocumentPanel.getAnalysisTypeName() + "' in the root " + 
						_context.elInfo( theRootPkg ) );

				IRPPackage theReqtsDocPkg = (IRPPackage) theRootPkg.addNewAggr( "Package", thePkgName );
				theReqtsDocPkg.highLightElement();

				IRPModelElement theFoundStereotype = 
						_context.get_rhpPrj().findAllByName( theStereotypeName , "Stereotype" );

				IRPStereotype theFromStereotype = null;

				if( theFoundStereotype == null ){
					theFromStereotype = theReqtsDocPkg.addStereotype( theStereotypeName, "Package" );

					theFromStereotype.addMetaClass( "Dependency" );
					theFromStereotype.addMetaClass( "HyperLink" );
					theFromStereotype.addMetaClass( "Requirement" );
					theFromStereotype.addMetaClass( "Type" );

					theFromStereotype.setOwner( theReqtsDocPkg );
					theFromStereotype.highLightElement();

				} else {
					theFromStereotype = theReqtsDocPkg.addStereotype( theStereotypeName, "Package" );
				}
			}
		}
	}

	private void updateGatewayDocInfoBasedOnUserSelections() {

		for (GatewayDocumentPanel gatewayDocumentPanel : m_GatewayDocumentPanel) {

			String theOriginalName = gatewayDocumentPanel.getOriginalName();
			String theNewName = gatewayDocumentPanel.getReqtsPkgName();	

			if (!theOriginalName.equals( theNewName )){

				_context.debug("Detected that user changed the name from " + 
						theOriginalName + " to " + theNewName);

				m_ChosenProjectFile.renameFileSection( theOriginalName, theNewName );
			}
		}

		for( GatewayDocumentPanel gatewayDocumentPanel : m_GatewayDocumentPanel ) {

			String theNewName = gatewayDocumentPanel.getReqtsPkgName();	
			GatewayFileSection theGatewayDoc = m_ChosenProjectFile.getFileSectionWith( theNewName );

			theGatewayDoc.setValueFor("Type", gatewayDocumentPanel.getAnalysisTypeName());
			theGatewayDoc.setValueFor("Path", gatewayDocumentPanel.getPathName());
			theGatewayDoc.setVariableXValue("baseline", gatewayDocumentPanel.getBaseline());
		}

		GatewayFileSection theUMLModelDoc = m_ChosenProjectFile.getFileSectionWith("UML Model");

		// change the requirementsPackage variable in the UML Model document
		String theReqtsPkgValue = buildRequirementsPackageValueFor();
		theUMLModelDoc.setVariableXValue( "requirementsPackage", theReqtsPkgValue );
		theUMLModelDoc.setVariableXValue( "previousReqsPackage", theReqtsPkgValue );

		String theDoorsModuleValue = theUMLModelDoc.getVariableXValue( "doorsModule" );

		// do we want to specify export model to DOORS into same module as the requirements?
		if( m_GatewayDocumentPanel.size()==1 && (theDoorsModuleValue != null) ){

			GatewayDocumentPanel theDoorsInfo = m_GatewayDocumentPanel.get(0);

			_context.debug("Setting the doorsModule variable to " + theDoorsInfo.getPathName() + " to get the export of diagrams to work");
			theUMLModelDoc.setVariableXValue( "doorsModule", theDoorsInfo.getPathName() );
		}

		String theUMLModelPath = "..\\" + _context.get_rhpPrj().getName() + ".rpyx";
		theUMLModelDoc.setValueFor( "Path", theUMLModelPath );
	}

	private String buildRequirementsPackageValueFor(){

		StringBuilder sb = new StringBuilder();

		for (int i = m_GatewayDocumentPanel.size() - 1; i >= 0; i--) {

			String targetPkgName = m_GatewayDocumentPanel.get(i).getReqtsPkgName();	
			IRPPackage theRootPkg = m_GatewayDocumentPanel.get(i).getRootPackage();

			sb.append( _context.get_rhpPrj().getName() );

			String[] split = theRootPkg.getFullPathName().split("::");

			for (String section : split) {
				sb.append( "/Packages/" );
				sb.append( section );
			}

			sb.append( "¥" );
			sb.append( targetPkgName );

			if(i>0){
				sb.append( "¥" );
			}
		}

		_context.debug("buildRequirementsPackageValueFor returning " + sb.toString() );
		return sb.toString();
	}

	@Override
	protected boolean checkValidity(boolean isMessageEnabled) {

		String errorMsg = null;

		boolean isValid = true;

		for (GatewayDocumentPanel gatewayDocumentPanel : m_GatewayDocumentPanel) {

			String theChosenName = gatewayDocumentPanel.getReqtsPkgName();

			boolean isLegalName = _context.isLegalName( theChosenName, _context.get_rhpPrj() );

			if (!isLegalName){

				if (errorMsg==null){
					errorMsg = theChosenName + " is not legal as a package name\n";
				} else {
					errorMsg += theChosenName + " is not legal as a package name\n";				
				}

				isValid = false;
			}
		}

		if (isMessageEnabled && !isValid && errorMsg != null){

			UserInterfaceHelper.showWarningDialog( errorMsg );
		}

		return isValid;		
	}

	@Override
	protected void performAction() {

		try {
			// do silent check first
			if( checkValidity( false ) ){

				updateGatewayDocInfoBasedOnUserSelections();

				String theProjectPath = _context.get_rhpPrj().getCurrentDirectory() + "/" + _context.get_rhpPrj().getName() + "_rpy" + "/";

				final String theGatewayPkgName = "GatewayProjectFiles";

				IRPModelElement theExistingGWPackage = 
						_context.findElementWithMetaClassAndName(
								"Package", theGatewayPkgName, _context.get_rhpPrj());

				IRPPackage theGatewayProjectFilesPkg = null;

				if( theExistingGWPackage != null && theExistingGWPackage instanceof IRPPackage ){
					theGatewayProjectFilesPkg = (IRPPackage)theExistingGWPackage;
				} else {
					// mimic Rhapsody by adding the GatewayProjectFiles package containing the GW files
					theGatewayProjectFilesPkg = _context.get_rhpPrj().addNestedPackage("GatewayProjectFiles");
				}

				String theRqtfFileName = _context.get_rhpPrj().getName() + ".rqtf";
				String theRqtfFullFilePathForProject = theProjectPath + theRqtfFileName;

				m_ChosenProjectFile.writeGatewayFileTo( theRqtfFullFilePathForProject );

				if( _context.findElementWithMetaClassAndName(
						"ControlledFile", theRqtfFileName, theGatewayProjectFilesPkg ) == null ){

					theGatewayProjectFilesPkg.addNewAggr(
							"ControlledFile",  theRqtfFileName);
				}

				String theTypesFileName = _context.get_rhpPrj().getName() + ".types";
				String theTypesFullFilePathForProject = theProjectPath + theTypesFileName;
				m_ChosenTypesFile.writeGatewayFileTo( theTypesFullFilePathForProject );

				if( _context.findElementWithMetaClassAndName(
						"ControlledFile", theTypesFileName, theGatewayProjectFilesPkg ) == null ){

					theGatewayProjectFilesPkg.addNewAggr(
							"ControlledFile",  theTypesFileName);
				}

				createDesignatedReqtPackagesInTheModel();

			} else {
				_context.error("Error in CreateGatewayProjectPanel.performAction, checkValidity returned false");
			}	
		} catch (Exception e) {
			_context.error("Error, unhandled exception in CreateGatewayProjectPanel.performAction");
		}

	}
}

/**
 * Copyright (C) 2016-2021  MBSE Training and Consulting Limited (www.executablembse.com)

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