package com.mbsetraining.sysmlhelper.requirementpackage;

import java.awt.BorderLayout;
import java.awt.Component;

import javax.swing.BoxLayout;
import javax.swing.GroupLayout;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.GroupLayout.ParallelGroup;
import javax.swing.GroupLayout.SequentialGroup;
import javax.swing.border.EmptyBorder;

import com.mbsetraining.sysmlhelper.executablembse.ExecutableMBSEBasePanel;
import com.telelogic.rhapsody.core.*;

public class CreateRequirementsPkgPanel extends ExecutableMBSEBasePanel {
	
	/**
	 * 
	 */
	private static final long serialVersionUID = 7464178722451860048L;

	public static void main(String[] args) {
		CreateRequirementsPkgPanel.launchTheDialog(
				RhapsodyAppServer.getActiveRhapsodyApplication().getApplicationConnectionString() );
	}

	private IRPPackage _ownerPkg;
	private CreateRequirementsPkgChooser _createRequirementsPkgChooser;

	public static void launchTheDialog(
			String theAppID ){

		javax.swing.SwingUtilities.invokeLater(new Runnable() {

			@Override
			public void run() {

				JFrame.setDefaultLookAndFeelDecorated( true );

				JFrame frame = new JFrame( "Populate requirements package" );

				frame.setDefaultCloseOperation( JFrame.DISPOSE_ON_CLOSE );

				CreateRequirementsPkgPanel thePanel = 
						new CreateRequirementsPkgPanel(
								theAppID );

				frame.setContentPane( thePanel );
				frame.pack();
				frame.setLocationRelativeTo( null );
				frame.setVisible( true );
			}
		});
	}

	public CreateRequirementsPkgPanel( 
			String theAppID ){

		super( theAppID );

		_ownerPkg = _context.get_rhpPrj();

		setLayout( new BorderLayout() );
		setBorder( new EmptyBorder(0, 10, 10, 10) );

		String theUniqueName = 
				_context.determineUniqueNameForPackageBasedOn(
						_context.getDefaultUseCasePackageName( _ownerPkg ),
						_ownerPkg );

		JPanel theReqtsAnalysisPanel = createContent( theUniqueName );
		theReqtsAnalysisPanel.setAlignmentX( Component.LEFT_ALIGNMENT );

		String introText = 
				"This helper will create a package hierarchy for requirements underneath the " + 
						_context.elInfo( _ownerPkg ) + ". \n";

		JPanel theStartPanel = new JPanel();

		theStartPanel.setLayout( new BoxLayout( theStartPanel, BoxLayout.PAGE_AXIS ) );
		theStartPanel.add( new JLabel( " " ) );
		theStartPanel.add( createPanelWithTextCentered( introText ) );

		add( theStartPanel, BorderLayout.PAGE_START );
		add( theReqtsAnalysisPanel, BorderLayout.CENTER );
		add( createOKCancelPanel(), BorderLayout.PAGE_END );		
	}

	private JPanel createContent(
			String theName ){

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

		String requirementPackagePostfix = _context.getDefaultRequirementPackagePostfix();

		_createRequirementsPkgChooser = new CreateRequirementsPkgChooser( 
				_ownerPkg, 
				theName + requirementPackagePostfix,
				true,
				false,
				_context );

		theColumn1ParallelGroup.addComponent( _createRequirementsPkgChooser.getM_UserChoiceComboBox() ); 
		theColumn2ParallelGroup.addComponent( _createRequirementsPkgChooser.getM_NameTextField() );   

		ParallelGroup theVertical1ParallelGroup = 
				theGroupLayout.createParallelGroup( GroupLayout.Alignment.BASELINE );

		ParallelGroup theVertical2ParallelGroup = 
				theGroupLayout.createParallelGroup( GroupLayout.Alignment.BASELINE );

		theVertical2ParallelGroup.addComponent( _createRequirementsPkgChooser.getM_UserChoiceComboBox() );
		theVertical2ParallelGroup.addComponent( _createRequirementsPkgChooser.getM_NameTextField() );

		theVerticalSequenceGroup.addGroup( theVertical1ParallelGroup );		
		theVerticalSequenceGroup.addGroup( theVertical2ParallelGroup );		

		theGroupLayout.setHorizontalGroup( theHorizSequenceGroup );
		theGroupLayout.setVerticalGroup( theVerticalSequenceGroup );

		return thePanel;
	}

	@Override
	protected boolean checkValidity(boolean isMessageEnabled) {
		return true;
	}

	@Override
	protected void performAction() {

		if( checkValidity( false ) ){

			_createRequirementsPkgChooser.createRequirementsPackage(_ownerPkg);
		}
	}
}

/**
 * Copyright (C) 2018-2024  MBSE Training and Consulting Limited (www.executablembse.com)

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