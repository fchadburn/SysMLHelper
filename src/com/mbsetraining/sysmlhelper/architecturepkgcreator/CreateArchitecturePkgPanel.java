package com.mbsetraining.sysmlhelper.architecturepkgcreator;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Dimension;

import javax.swing.BoxLayout;
import javax.swing.GroupLayout;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.GroupLayout.ParallelGroup;
import javax.swing.GroupLayout.SequentialGroup;
import javax.swing.border.EmptyBorder;

import com.mbsetraining.sysmlhelper.executablembse.AutoPackageDiagram;
import com.mbsetraining.sysmlhelper.executablembse.ExecutableMBSEBasePanel;
import com.telelogic.rhapsody.core.*;

public class CreateArchitecturePkgPanel extends ExecutableMBSEBasePanel {

	/**
	 * 
	 */
	private static final long serialVersionUID = -2230513627271522470L;

	public static void main(String[] args) {
		CreateArchitecturePkgPanel.launchTheDialog(
				RhapsodyAppServer.getActiveRhapsodyApplication().getApplicationConnectionString() );
	}

	private IRPPackage _invokedFromPkg;
	private CreateArchitecturePkgChooser _createPkgChooser;

	public static void launchTheDialog(
			String theAppID ){

		javax.swing.SwingUtilities.invokeLater(new Runnable() {

			@Override
			public void run() {

				JFrame.setDefaultLookAndFeelDecorated( true );

				JFrame frame = new JFrame( "Create an architecture package" );

				frame.setDefaultCloseOperation( JFrame.DISPOSE_ON_CLOSE );

				CreateArchitecturePkgPanel thePanel = 
						new CreateArchitecturePkgPanel(
								theAppID );

				frame.setContentPane( thePanel );
				frame.pack();
				frame.setLocationRelativeTo( null );
				frame.setVisible( true );
			}
		});
	}

	public CreateArchitecturePkgPanel( 
			String theAppID ){

		super( theAppID );

		_invokedFromPkg = (IRPPackage) _context.getSelectedElement( false );
		
		setLayout( new BorderLayout() );
		setBorder( new EmptyBorder(0, 10, 10, 10) );

		JPanel theCenterPanel = createContent();
		theCenterPanel.setAlignmentX( Component.LEFT_ALIGNMENT );

		String introText = 
				"This helper will create an architecture package structure. \n";

		JPanel theStartPanel = new JPanel();

		theStartPanel.setLayout( new BoxLayout( theStartPanel, BoxLayout.PAGE_AXIS ) );
		theStartPanel.add( new JLabel( " " ) );
		theStartPanel.add( createPanelWithTextCentered( introText ) );

		add( theStartPanel, BorderLayout.PAGE_START );
		add( theCenterPanel, BorderLayout.CENTER );
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
		ParallelGroup theColumn3ParallelGroup = theGroupLayout.createParallelGroup( GroupLayout.Alignment.LEADING );

		theHorizSequenceGroup.addGroup( theColumn1ParallelGroup );
		theHorizSequenceGroup.addGroup( theColumn2ParallelGroup );
		theHorizSequenceGroup.addGroup( theColumn3ParallelGroup );
		
		_createPkgChooser = new CreateArchitecturePkgChooser( 
				_invokedFromPkg, 
				_context.getDefaultArchitecturePackageName(),
				_context.getDefaultArchitecturePackagePostfix(),
				_context );
		
		 _createPkgChooser.getM_NameTextField().setMinimumSize( new Dimension( 150, 22 ) );
		 _createPkgChooser.getM_FileNameTextField().setMinimumSize( new Dimension( 250, 22 ) );
 
		theColumn1ParallelGroup.addComponent( _createPkgChooser.getM_UserChoiceComboBox() ); 
		theColumn2ParallelGroup.addComponent( _createPkgChooser.getM_NameTextField() );  
		theColumn3ParallelGroup.addComponent( _createPkgChooser.getM_FileNameTextField() );   
		
		ParallelGroup theVertical1ParallelGroup = 
				theGroupLayout.createParallelGroup( GroupLayout.Alignment.BASELINE );

		ParallelGroup theVertical2ParallelGroup = 
				theGroupLayout.createParallelGroup( GroupLayout.Alignment.BASELINE );

		ParallelGroup theVertical3ParallelGroup = 
				theGroupLayout.createParallelGroup( GroupLayout.Alignment.BASELINE );
		
		theVertical2ParallelGroup.addComponent( _createPkgChooser.getM_UserChoiceComboBox() );
		theVertical2ParallelGroup.addComponent( _createPkgChooser.getM_NameTextField() );
		theVertical2ParallelGroup.addComponent( _createPkgChooser.getM_FileNameTextField() );

		theVerticalSequenceGroup.addGroup( theVertical1ParallelGroup );		
		theVerticalSequenceGroup.addGroup( theVertical2ParallelGroup );		
		theVerticalSequenceGroup.addGroup( theVertical3ParallelGroup );		

		theGroupLayout.setHorizontalGroup( theHorizSequenceGroup );
		theGroupLayout.setVerticalGroup( theVerticalSequenceGroup );

		return thePanel;
	}

	@Override
	protected boolean checkValidity(
			boolean isMessageEnabled ){
		
		return true;
	}

	@Override
	protected void performAction() {

		if( checkValidity( false ) ){

			_createPkgChooser.createArchitecturePackage();

			if( _context.getIsAutoPopulatePackageDiagram( _context.get_rhpPrj() ) ){
				
				AutoPackageDiagram theAPD = new AutoPackageDiagram( _context );
				theAPD.drawDiagram();
			}
		}
	}
}

/**
 * Copyright (C) 2024  MBSE Training and Consulting Limited (www.executablembse.com)

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