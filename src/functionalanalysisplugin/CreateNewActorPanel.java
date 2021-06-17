package functionalanalysisplugin;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.util.ArrayList;
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
import com.mbsetraining.sysmlhelper.executablembse.ExecutableMBSE_Context;
import com.telelogic.rhapsody.core.*;

public class CreateNewActorPanel extends ExecutableMBSEBasePanel {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	private IRPPackage m_RootPackage;
	protected JTextField m_ChosenNameTextField = null;
	private ActorMappingInfo m_ClassifierMappingInfo;
	private IRPClass m_BlockToConnectTo = null;
	protected FunctionalAnalysisSettings _settings;
	protected SelectedElementContext _selectedContext;
	protected ExecutableMBSE_Context _context;

	public static void main(String[] args) {	
		IRPApplication theRhpApp = RhapsodyAppServer.getActiveRhapsodyApplication();
		launchThePanel( theRhpApp.getApplicationConnectionString() );
	}
	
	public static void launchThePanel(
			final String theAppID ){
				
		javax.swing.SwingUtilities.invokeLater(new Runnable() {

			@Override
			public void run() {
				
				JFrame.setDefaultLookAndFeelDecorated( true );
				
				JFrame frame = new JFrame( "Create new Actor" );
				
				frame.setDefaultCloseOperation( JFrame.DISPOSE_ON_CLOSE );

				CreateNewActorPanel thePanel = 
						new CreateNewActorPanel( theAppID );

				frame.setContentPane( thePanel );
				frame.pack();
				frame.setLocationRelativeTo( null );
				frame.setVisible( true );
			}
		});
	}
	
	public CreateNewActorPanel( String theAppID ){
		
		super( theAppID );
		
		_settings = new FunctionalAnalysisSettings( (ExecutableMBSE_Context) _context );
		
		IRPClass theBuildingBlock = 
				_selectedContext.getBuildingBlock();

		if( theBuildingBlock == null ){
			
			buildUnableToRunDialog( 
					"Sorry, this helper is unable to run this command because \n" +
					"there was no execution context or block found in the model. \n " +
					"You need to add the relevant package structure first." );
			
		} else { // theBuildingBlock != null
			
			IRPClass theBlock = _selectedContext.getBlockUnderDev(
					"Which Block/Part do you want to wire the Actor to?" );
			
			if( theBlock == null ){
				buildUnableToRunDialog( 
						"Sorry, this helper is unable to run this command because \n" +
						"there was no execution context or block found in the model. \n " +
						"You need to add the relevant package structure first." );
			} else {
				m_RootPackage = _selectedContext.getSimulationSettingsPackageBasedOn( theBlock );
				m_BlockToConnectTo = _selectedContext.getChosenBlock();
				
				setLayout( new BorderLayout(10,10) );
				setBorder( BorderFactory.createEmptyBorder( 10, 10, 10, 10 ) );
				
				if( m_BlockToConnectTo != null ){
					add( createActorChoicePanel( m_BlockToConnectTo.getName() ), BorderLayout.PAGE_START );

				} else {
					add( createActorChoicePanel( "" ), BorderLayout.PAGE_START );

				}
				
				add( createOKCancelPanel(), BorderLayout.PAGE_END );

			}
		}
	}

	@SuppressWarnings("unchecked")
	private JPanel createActorChoicePanel(String theBlockName){
		
		JPanel thePanel = new JPanel();
		thePanel.setLayout( new BoxLayout(thePanel, BoxLayout.X_AXIS ) );	
		
		m_ChosenNameTextField = new JTextField();
		m_ChosenNameTextField.setPreferredSize( new Dimension( 300, 20 ) );

		List<IRPModelElement> theExistingActors;
		
		boolean isAllowInheritanceChoices = 
				_context.getIsAllowInheritanceChoices( m_RootPackage );
		
		if( isAllowInheritanceChoices ){
			
			theExistingActors = m_RootPackage.getNestedElementsByMetaClass( 
					"Actor", 1 ).toList();
		} else {
			theExistingActors = new ArrayList<>();
		}
				
		RhapsodyComboBox theInheritedActorComboBox = 
				new RhapsodyComboBox( theExistingActors, false );
		
		JCheckBox theActorCheckBox = new JCheckBox( "Create actor called:" );
		    
		theActorCheckBox.setSelected( true );
			
		m_ClassifierMappingInfo = 
				new ActorMappingInfo(
						theInheritedActorComboBox, 
						theActorCheckBox, 
						m_ChosenNameTextField, 
						null,
						_context );
		
		m_ClassifierMappingInfo.updateToBestActorNamesBasedOn( theBlockName );
		
	    thePanel.add( theActorCheckBox );
	    thePanel.add( m_ChosenNameTextField );
	    
	    if( isAllowInheritanceChoices ){
		    JLabel theLabel = new JLabel( "Inherit from:" );
		    thePanel.add( theLabel );
		    thePanel.add( theInheritedActorComboBox );	    	
	    }
	    
		return thePanel;
	}
	
	@Override
	protected boolean checkValidity(
			boolean isMessageEnabled) {
		
		boolean isValid = true;
		String errorMsg = "";
		
		String theChosenName = m_ChosenNameTextField.getText();
		
		if ( theChosenName.contains( m_ClassifierMappingInfo.m_ActorBlankName ) ){
			
			errorMsg += "Please choose a valid name for the Actor";
			isValid = false;
			
		} else {
			boolean isLegalBlockName = _context.isLegalName( theChosenName, m_BlockToConnectTo );
			
			if (!isLegalBlockName){
				
				errorMsg += theChosenName + " is not legal as an identifier representing an executable Actor\n";				
				isValid = false;
				
			} else if (!_context.isElementNameUnique(
					
				theChosenName, "Actor", m_RootPackage, 1)){

				errorMsg += "Unable to proceed as the Actor name '" + theChosenName + "' is not unique";
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
		
		if( checkValidity( false ) ){
			
			IRPClass theAssemblyBlock = 
					_settings.getBuildingBlock( m_RootPackage );
			
			if( m_RootPackage != null ){
				
				IRPInstance theActorPart =
						m_ClassifierMappingInfo.performActorPartCreationIfSelectedIn( 
								theAssemblyBlock, m_BlockToConnectTo );
				
				if( theActorPart != null ){
					
					SequenceDiagramHelper theHelper = new SequenceDiagramHelper( _context );
					theHelper.updateAutoShowSequenceDiagramFor( theAssemblyBlock );
				}
			
			} else {
				_context.error("Error in CreateNewActorPanel.performAction, unable to find " + _context.elInfo( m_RootPackage ) );
			}
						
		} else {
			_context.error("Error in CreateNewActorPanel.performAction, checkValidity returned false");
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