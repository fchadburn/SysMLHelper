package functionalanalysisplugin;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.util.List;

import javax.swing.BorderFactory;
import javax.swing.BoxLayout;
import javax.swing.JCheckBox;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;

import com.mbsetraining.sysmlhelper.common.RhapsodyComboBox;
import com.mbsetraining.sysmlhelper.common.UserInterfaceHelper;
import com.mbsetraining.sysmlhelper.executablembse.ExecutableMBSEBasePanel;
import com.telelogic.rhapsody.core.*;

public class CreateNewBlockPartPanel extends ExecutableMBSEBasePanel {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	private IRPPackage m_RootPackage;
	private IRPClass m_AssemblyBlock;

	protected JTextField m_BlockNameTextField = null;
	protected JTextField m_PartNameTextField = null;

	protected RhapsodyComboBox m_ChosenStereotype;

	// testing only
	public static void main(String[] args) {
		IRPApplication theRhpApp = RhapsodyAppServer.getActiveRhapsodyApplication();
		launchThePanel( theRhpApp.getApplicationConnectionString() );
	}

	public static void launchThePanel(
			final String theAppID ){

		javax.swing.SwingUtilities.invokeLater( new Runnable() {

			@Override
			public void run() {

				JFrame.setDefaultLookAndFeelDecorated( true );

				JFrame frame = new JFrame( "Create new Block/Part" );

				frame.setDefaultCloseOperation( JFrame.DISPOSE_ON_CLOSE );

				CreateNewBlockPartPanel thePanel = 
						new CreateNewBlockPartPanel( theAppID );

				frame.setContentPane( thePanel );
				frame.pack();
				frame.setLocationRelativeTo( null );
				frame.setVisible( true );
			}
		});
	}

	public CreateNewBlockPartPanel(
			String theAppID ){

		super( theAppID );

		IRPClass theBuildingBlock = 
				_selectedContext.getBuildingBlock();

		if( theBuildingBlock == null ){

			buildUnableToRunDialog( 
					"Sorry, this helper is unable to run this command because \n" +
							"there was no execution context or block found in the model. \n " +
					"You need to add the relevant package structure first." );

		} else { // theBuildingBlock != null

			m_RootPackage = _selectedContext.getPackageForBlocks();
			m_AssemblyBlock = _selectedContext.getBuildingBlock();

			setLayout( new BorderLayout(10,10) );
			setBorder( BorderFactory.createEmptyBorder( 10, 10, 10, 10 ) );

			add( createBlockChoicePanel( "" ), BorderLayout.PAGE_START );
			add( createStereotypePanel(), BorderLayout.CENTER );	    
			add( createOKCancelPanel(), BorderLayout.PAGE_END );


		}
	}

	private JPanel createStereotypePanel(){

		JPanel thePanel = new JPanel();
		thePanel.setLayout( new BoxLayout(thePanel, BoxLayout.X_AXIS ) );	

		List<IRPModelElement> theStereotypes = 
				_context.getStereotypesForBlockPartCreation( 
						m_RootPackage.getProject() );

		m_ChosenStereotype = new RhapsodyComboBox( theStereotypes, false );
		m_ChosenStereotype.setMaximumSize( new Dimension( 250, 20 ) );

		if( theStereotypes.size() > 0 ){
			// set to first value in list
			m_ChosenStereotype.setSelectedRhapsodyItem( theStereotypes.get( 0 ) );	
			_context.debug("Setting default stereotype to " + _context.elInfo(theStereotypes.get( 0 )));
		}

		thePanel.add( new JLabel( "  Stereotype as: " ) );
		thePanel.add( m_ChosenStereotype );

		return thePanel;
	}

	private JPanel createBlockChoicePanel(
			String theBlockName ){

		JPanel thePanel = new JPanel();
		thePanel.setLayout( new BoxLayout(thePanel, BoxLayout.X_AXIS ) );	

		m_BlockNameTextField = new JTextField();
		m_BlockNameTextField.setPreferredSize( new Dimension( 150, 20 ) );

		JCheckBox theBlockCheckBox = new JCheckBox( "Create block called:" );

		theBlockCheckBox.setSelected( true );
		thePanel.add( theBlockCheckBox );
		thePanel.add( m_BlockNameTextField );

		thePanel.add( new JLabel(" with part name (leave blank for default): ") );

		m_PartNameTextField = new JTextField();
		m_PartNameTextField.setPreferredSize( new Dimension( 150, 20 ) );

		thePanel.add( m_PartNameTextField );

		return thePanel;
	}

	@Override
	protected boolean checkValidity(
			boolean isMessageEnabled) {

		boolean isValid = true;
		String errorMsg = "";

		String theBlockName = m_BlockNameTextField.getText();

		if ( theBlockName.trim().isEmpty() ){

			errorMsg += "Please choose a valid name for the Block";
			isValid = false;

		} else {
			boolean isLegalBlockName = 
					_context.isLegalName( theBlockName, m_RootPackage );

			if( !isLegalBlockName ){

				errorMsg += theBlockName + " is not legal as an identifier representing an executable Block\n";				
				isValid = false;

			} else if( !_context.isElementNameUnique(
					theBlockName, 
					"Class", 
					m_RootPackage, 
					1 ) ){

				errorMsg += "Unable to proceed as the Block name '" + theBlockName + "' is not unique";
				isValid = false;

			} else {

				String thePartName = m_PartNameTextField.getText();

				if ( !thePartName.trim().isEmpty() ){

					boolean isLegalPartName = 
							_context.isLegalName( thePartName, m_AssemblyBlock );

					if( !isLegalPartName ){

						errorMsg += thePartName + " is not legal as an identifier representing an executable Part\n";				
						isValid = false;

					} else if( !_context.isElementNameUnique(
							thePartName, 
							"Object", 
							m_AssemblyBlock, 
							0 ) ){

						errorMsg += "Unable to proceed as the Part name '" + thePartName + "' is not unique for " + 
								_context.elInfo( m_AssemblyBlock );

						isValid = false;
					}
				}
			}
		}

		if (isMessageEnabled && !isValid && errorMsg != null){

			UserInterfaceHelper.showWarningDialog( errorMsg );
		}

		return isValid;
	}

	private IRPInstance getElapsedTimeActorPartFor(
			IRPClass theAssemblyBlock ){

		IRPInstance theElapsedTimePart = null;

		@SuppressWarnings("unchecked")
		List<IRPInstance> theInstances = 
		theAssemblyBlock.getNestedElementsByMetaClass(
				"Instance", 0 ).toList();

		for( IRPInstance theInstance : theInstances ){

			IRPClassifier theClassifier = theInstance.getOtherClass();

			if( theClassifier != null &&
					theClassifier instanceof IRPActor &&
					theClassifier.getName().equals( "ElapsedTime" ) ){

				theElapsedTimePart = theInstance;
				break;
			}
		}

		return theElapsedTimePart;
	}

	@Override
	protected void performAction() {

		if( checkValidity( false ) ){

			if( m_RootPackage != null ){

				String theName = m_BlockNameTextField.getText();

				IRPClass theClass = m_RootPackage.addClass( theName );
				theClass.highLightElement();				

				IRPProject theProject = theClass.getProject();

				_context.addGeneralization( 
						theClass, 
						"TimeElapsedBlock", 
						theProject );

				String thePartName = m_PartNameTextField.getText().trim();

				IRPInstance thePart = 
						(IRPInstance) m_AssemblyBlock.addNewAggr(
								"Part", thePartName );

				thePart.setOtherClass( theClass );
				thePart.highLightElement();

				IRPModelElement theSelectedStereotype = m_ChosenStereotype.getSelectedRhapsodyItem();

				if( theSelectedStereotype != null && 
						theSelectedStereotype instanceof IRPStereotype ){

					try {
						theClass.setStereotype( (IRPStereotype) theSelectedStereotype );

					} catch (Exception e) {
						_context.error("Exception in CreateNewBlockPartPanel.performAction, unable to apply " + 
								theSelectedStereotype.getName() + " to " + _context.elInfo( theClass ) );	
					}

					try {
						thePart.setStereotype( (IRPStereotype) theSelectedStereotype );

					} catch (Exception e) {
						_context.error("Exception in CreateNewBlockPartPanel.performAction, unable to apply " + 
								theSelectedStereotype.getName() + " to " + _context.elInfo( thePart ) );	
					}
				}

				theClass.changeTo( "Block" );

				// Try and find ElapsedTime actor part 				
				IRPInstance theElapsedTimePart = 
						getElapsedTimeActorPartFor( m_AssemblyBlock );

				if( theElapsedTimePart != null ){

					IRPClassifier theElapsedTimeActor = 
							theElapsedTimePart.getOtherClass();

					IRPSysMLPort theActorsElapsedTimePort = 
							(IRPSysMLPort) _context.findNestedElementUnder( 
									(IRPClassifier) theElapsedTimeActor,
									"elapsedTime",
									"SysMLPort",
									true );

					IRPSysMLPort theBlocksElapsedTimePort = 
							(IRPSysMLPort) _context.findNestedElementUnder( 
									(IRPClassifier) theClass,
									"elapsedTime",
									"SysMLPort",
									true );

					if( theActorsElapsedTimePort != null &&
							theBlocksElapsedTimePort != null ){

						_context.addConnectorBetweenSysMLPortsIfOneDoesntExist(
								theActorsElapsedTimePort, 
								theElapsedTimePart, 
								theBlocksElapsedTimePort, 
								thePart );

					} else {
						_context.error("Error in CreateNewBlockPartPanel.performAction(), unable to find elapsedTime ports") ;
					}

				} else {
					_context.error("Error in CreateNewBlockPartPanel.performAction: Unable to find ElapsedTime actor in project. You may be missing the BasePkg");
				}

				SequenceDiagramHelper theHelper = new SequenceDiagramHelper(_context);

				theHelper.updateAutoShowSequenceDiagramFor( 
						m_AssemblyBlock );

			} else {
				_context.error("Error in CreateNewActorPanel.performAction, unable to find " + _context.elInfo( m_RootPackage ) );
			}

		} else {
			_context.error("Error in CreateNewActorPanel.performAction, checkValidity returned false");
		}		
	}	
}

/**
 * Copyright (C) 2017-2021  MBSE Training and Consulting Limited (www.executablembse.com)

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