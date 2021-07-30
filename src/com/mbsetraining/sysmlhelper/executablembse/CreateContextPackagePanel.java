package com.mbsetraining.sysmlhelper.executablembse;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.List;

import javax.swing.BoxLayout;
import javax.swing.GroupLayout;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.GroupLayout.ParallelGroup;
import javax.swing.GroupLayout.SequentialGroup;
import javax.swing.border.EmptyBorder;

import com.mbsetraining.sysmlhelper.common.RhapsodyComboBox;
import com.telelogic.rhapsody.core.*;

public class CreateContextPackagePanel extends ExecutableMBSEBasePanel {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	private IRPPackage _ownerPkg;
	private CreateActorPkgChooser _createActorChooser;
	private CreateRequirementsPkgChooser _createRequirementsPkgChooser;
	private CreateExternalSignalsPkgChooser _createExternalSignalsPkgChooser;
	private JTextField _nameTextField;
	protected RhapsodyComboBox _selectClassComboBox = null;

	public static void main(String[] args) {
	
		IRPApplication theRhpApp = RhapsodyAppServer.getActiveRhapsodyApplication();
		
		launchTheDialog( theRhpApp.getApplicationConnectionString() );
	}
	
	public static void launchTheDialog(
			final String theAppID ){

		javax.swing.SwingUtilities.invokeLater(new Runnable() {

			@Override
			public void run() {

				JFrame.setDefaultLookAndFeelDecorated( true );

				JFrame frame = new JFrame( "Populate context diagram package structure" );

				frame.setDefaultCloseOperation( JFrame.DISPOSE_ON_CLOSE );

				CreateContextPackagePanel thePanel = 
						new CreateContextPackagePanel(
								theAppID );

				frame.setContentPane( thePanel );
				frame.pack();
				frame.setLocationRelativeTo( null );
				frame.setVisible( true );
			}
		});
	}

	public CreateContextPackagePanel( 
			String theAppID ){

		super( theAppID );

		_ownerPkg = _context.get_rhpPrj();

		setLayout( new BorderLayout() );
		setBorder( new EmptyBorder( 0, 10, 10, 10 ) );

		String theUniqueName = 
				_context.determineUniqueNameForPackageBasedOn(
						((ExecutableMBSE_Context) _context).getDefaultContextDiagramPackageName(),
						_ownerPkg );

		JPanel theMainPanel = createContent();
		theMainPanel.setAlignmentX( Component.LEFT_ALIGNMENT );

		String introText = 
				"This helper will create a package hierarchy for simple system context diagrams underneath the " + 
						_context.elInfo( _ownerPkg ) + ". \n" +
						"It creates a nested package structure and context diagram, imports the appropriate profiles if not present, and sets default \n" +
						"display and other options to appropriate values for this using Rhapsody profile and property settings.\n";

		JPanel theStartPanel = new JPanel();

		theStartPanel.setLayout( new BoxLayout( theStartPanel, BoxLayout.PAGE_AXIS ) );
		theStartPanel.add( new JLabel( " " ) );
		theStartPanel.add( createContextNameChoicePanel( theUniqueName ) );
		theStartPanel.add( createPanelWithTextCentered( introText ) );

		add( theStartPanel, BorderLayout.PAGE_START );
		add( theMainPanel, BorderLayout.CENTER );
		add( createOKCancelPanel(), BorderLayout.PAGE_END );		
	}

	private JPanel createContent(){

		JPanel thePanel = new JPanel();

		GroupLayout theGroupLayout = new GroupLayout( thePanel );
		thePanel.setLayout( theGroupLayout );
		theGroupLayout.setAutoCreateGaps( true );

		SequentialGroup theHorizSequenceGroup = theGroupLayout.createSequentialGroup();
		SequentialGroup theVerticalSequenceGroup = theGroupLayout.createSequentialGroup();

		ParallelGroup theColumn1ParallelGroup = theGroupLayout.createParallelGroup( GroupLayout.Alignment.LEADING );
		ParallelGroup theColumn2ParallelGroup = theGroupLayout.createParallelGroup( GroupLayout.Alignment.LEADING );

		theHorizSequenceGroup.addGroup( theColumn1ParallelGroup );
		theHorizSequenceGroup.addGroup( theColumn2ParallelGroup );

		_createActorChooser = new CreateActorPkgChooser( 
				_ownerPkg, 
				((ExecutableMBSE_Context) _context) );

		_createRequirementsPkgChooser = new CreateRequirementsPkgChooser( 
				_ownerPkg, 
				((ExecutableMBSE_Context) _context).getDefaultRequirementsPackageName(), 
				false,
				((ExecutableMBSE_Context) _context) );

		_createExternalSignalsPkgChooser = new CreateExternalSignalsPkgChooser( 
				_ownerPkg, 
				((ExecutableMBSE_Context) _context) );
		
		theColumn1ParallelGroup.addComponent( _createExternalSignalsPkgChooser.getM_UserChoiceComboBox() ); 
		theColumn1ParallelGroup.addComponent( _createActorChooser.getM_UserChoiceComboBox() );    
		theColumn1ParallelGroup.addComponent( _createRequirementsPkgChooser.getM_UserChoiceComboBox() ); 
		theColumn2ParallelGroup.addComponent( _createExternalSignalsPkgChooser.getM_NameTextField() );   
		theColumn2ParallelGroup.addComponent( _createActorChooser.getM_NameTextField() );   
		theColumn2ParallelGroup.addComponent( _createRequirementsPkgChooser.getM_NameTextField() );   

		ParallelGroup theVertical1ParallelGroup = 
				theGroupLayout.createParallelGroup( GroupLayout.Alignment.BASELINE );

		theVertical1ParallelGroup.addComponent( _createActorChooser.getM_UserChoiceComboBox() );
		theVertical1ParallelGroup.addComponent( _createActorChooser.getM_NameTextField() );

		ParallelGroup theVertical2ParallelGroup = 
				theGroupLayout.createParallelGroup( GroupLayout.Alignment.BASELINE );

		theVertical2ParallelGroup.addComponent( _createRequirementsPkgChooser.getM_UserChoiceComboBox() );
		theVertical2ParallelGroup.addComponent( _createRequirementsPkgChooser.getM_NameTextField() );

		ParallelGroup theVertical3ParallelGroup = 
				theGroupLayout.createParallelGroup( GroupLayout.Alignment.BASELINE );

		theVertical3ParallelGroup.addComponent( _createExternalSignalsPkgChooser.getM_UserChoiceComboBox() );
		theVertical3ParallelGroup.addComponent( _createExternalSignalsPkgChooser.getM_NameTextField() );

		
		theVerticalSequenceGroup.addGroup( theVertical1ParallelGroup );		
		theVerticalSequenceGroup.addGroup( theVertical2ParallelGroup );		
		theVerticalSequenceGroup.addGroup( theVertical3ParallelGroup );		

		theGroupLayout.setHorizontalGroup( theHorizSequenceGroup );
		theGroupLayout.setVerticalGroup( theVerticalSequenceGroup );

		return thePanel;
	}
	
	private JPanel createContextNameChoicePanel(
			String theUniqueName ){
		
		JPanel thePanel = new JPanel();
		thePanel.setLayout( new BoxLayout(thePanel, BoxLayout.X_AXIS ) );	
		
		_nameTextField = new JTextField( theUniqueName );
		_nameTextField.setPreferredSize( new Dimension( 200, 20 ) );
		
		List<IRPModelElement> theSystemBlocks = 
				_context.findElementsWithMetaClassAndStereotype( 
						"Class", 
						ExecutableMBSE_ProfileConstants.SYSTEM_BLOCK, 
						_context.get_rhpPrj(), 
						1 );
		
		_selectClassComboBox = new RhapsodyComboBox( theSystemBlocks, false );
		
		_selectClassComboBox.addActionListener( new ActionListener () {
		    public void actionPerformed( ActionEvent e ){
		        
		    	IRPModelElement theItem = _selectClassComboBox.getSelectedRhapsodyItem();
		    	
		    	if( theItem instanceof IRPClass ){
		    		_nameTextField.setEnabled( false );
		    	} else {
		    		_nameTextField.setEnabled( true );
		    	}
		    }
		});
		
		thePanel.add( new JLabel( "Choose a unique name: " ) );
	    thePanel.add( _nameTextField );
	    
		JLabel theLabel = new JLabel( "  or select existing " + ExecutableMBSE_ProfileConstants.SYSTEM_BLOCK + ":  " );
		thePanel.add( theLabel );
		thePanel.add( _selectClassComboBox );	    	
	    
		_nameTextField.requestFocusInWindow();
		
		return thePanel;
	}

	@Override
	protected boolean checkValidity(
			boolean isMessageEnabled ){
		// TODO 	
		return true;
	}

	@Override
	protected void performAction(){

		if( checkValidity( false ) ){

			IRPModelElement theSystemBlock = _selectClassComboBox.getSelectedRhapsodyItem();
			
			String theUnadornedName;
			
			if( theSystemBlock instanceof IRPClassifier ){
				
				theUnadornedName = theSystemBlock.getName();
			} else {
				theUnadornedName = _nameTextField.getText(); 
			}
			
			@SuppressWarnings("unused")
			CreateContextDiagramPackage theCreator = new CreateContextDiagramPackage(
					theUnadornedName, // theContextDiagramPackageName
					theSystemBlock,
					_ownerPkg, // theOwningPkg
					_createRequirementsPkgChooser.getReqtsPkgChoice(), // theReqtsPkgChoice
					_createRequirementsPkgChooser.getReqtsPkgOptionalName(), // theReqtsPkgOptionalName
					_createRequirementsPkgChooser.getExistingReqtsPkgIfChosen(), // theExistingReqtsPkgIfChosen
					_createActorChooser.getCreateActorPkgOption(), // theActorPkgChoice
					_createActorChooser.getActorsPkgNameIfChosen(), // theActorsPkgNameOption
					_createActorChooser.getExistingActorPkgIfChosen(), // theExistingActorsPkgOption
					theUnadornedName, // theActorPkgPrefixOption
					_createExternalSignalsPkgChooser.getCreateExternalSignalsPkgOption(),
					_createExternalSignalsPkgChooser.getExternalSignalsPkgNameIfChosen(),
					_createExternalSignalsPkgChooser.getExistingExternalSignalsPkgIfChosen(),
					((ExecutableMBSE_Context) _context) );
		}
	}
}

/**
 * Copyright (C) 2021  MBSE Training and Consulting Limited (www.executablembse.com)

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