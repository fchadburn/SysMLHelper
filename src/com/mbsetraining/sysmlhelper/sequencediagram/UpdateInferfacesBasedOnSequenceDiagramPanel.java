package com.mbsetraining.sysmlhelper.sequencediagram;

import java.awt.BorderLayout;
import java.awt.Component;

import javax.swing.BoxLayout;
import javax.swing.GroupLayout;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.GroupLayout.ParallelGroup;
import javax.swing.GroupLayout.SequentialGroup;
import javax.swing.border.EmptyBorder;

import com.mbsetraining.sysmlhelper.executablembse.ExecutableMBSEBasePanel;
import com.telelogic.rhapsody.core.*;

public class UpdateInferfacesBasedOnSequenceDiagramPanel extends ExecutableMBSEBasePanel {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	private IRPSequenceDiagram _diagram;
	private MessageInfoList _messageInfoList;
	private InterfaceInfoList _interfaceInfoList;

	public static void main(String[] args) {
		IRPApplication theRhpApp = RhapsodyAppServer.getActiveRhapsodyApplication();
		
		IRPModelElement theSelectedEl = theRhpApp.getSelectedElement();
		
		if( theSelectedEl instanceof IRPSequenceDiagram ){
			
			IRPSequenceDiagram theSD = (IRPSequenceDiagram)theSelectedEl;
			
			launchTheDialog( theRhpApp.getApplicationConnectionString(), theSD.getGUID() );
		}
	}
	
	public static void launchTheDialog(
			String theAppID,
			String theSequenceDiagramGUID ){

		javax.swing.SwingUtilities.invokeLater(new Runnable() {

			@Override
			public void run() {

				JFrame.setDefaultLookAndFeelDecorated( true );

				JFrame frame = new JFrame( "Update interfaces based on sequence diagram" );

				frame.setDefaultCloseOperation( JFrame.DISPOSE_ON_CLOSE );

				UpdateInferfacesBasedOnSequenceDiagramPanel thePanel = 
						new UpdateInferfacesBasedOnSequenceDiagramPanel(
								theAppID,
								theSequenceDiagramGUID );

				frame.setContentPane( thePanel );
				frame.pack();
				frame.setLocationRelativeTo( null );
				frame.setVisible( true );
			}
		});
	}

	public UpdateInferfacesBasedOnSequenceDiagramPanel( 
			String theAppID,
			String theSequenceDiagramGUID ){

		super( theAppID );

		_diagram = (IRPSequenceDiagram) _context.get_rhpPrj().findElementByGUID( theSequenceDiagramGUID );
		
		_context.debug( "UpdateInferfacesBasedOnSequenceDiagramPanel was invoked for " + _context.elInfo( _diagram ) );
		
		_interfaceInfoList = new InterfaceInfoList( _context );
		_interfaceInfoList.dumpInfo();
		
		_messageInfoList = new MessageInfoList( _diagram, _interfaceInfoList, _context );
		
		setLayout( new BorderLayout() );
		setBorder( new EmptyBorder(0, 10, 10, 10) );

		JPanel theReqtsAnalysisPanel = createContent();
		theReqtsAnalysisPanel.setAlignmentX( Component.LEFT_ALIGNMENT );

		String introText = 
				"This helper will realize events and operations on " + 
						_context.elInfo( _diagram ) + " and add them to the interfaces. \n";

		JPanel theStartPanel = new JPanel();

		theStartPanel.setLayout( new BoxLayout( theStartPanel, BoxLayout.PAGE_AXIS ) );
		theStartPanel.add( new JLabel( " " ) );
		theStartPanel.add( createPanelWithTextCentered( introText ) );

		add( theStartPanel, BorderLayout.PAGE_START );
		add( theReqtsAnalysisPanel, BorderLayout.CENTER );
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

		for( MessageInfo messageInfo : _messageInfoList ){
			
			JTextField comp1 = new JTextField( messageInfo.getName() );
			JTextField comp2 = new JTextField( messageInfo.getActionDescription() );
			
			theColumn1ParallelGroup.addComponent( comp1 ); 
			theColumn2ParallelGroup.addComponent( comp2 );   

			ParallelGroup theVertical1ParallelGroup = 
					theGroupLayout.createParallelGroup( GroupLayout.Alignment.BASELINE );

			ParallelGroup theVertical2ParallelGroup = 
					theGroupLayout.createParallelGroup( GroupLayout.Alignment.BASELINE );

			theVertical2ParallelGroup.addComponent( comp1 );
			theVertical2ParallelGroup.addComponent( comp2 );

			theVerticalSequenceGroup.addGroup( theVertical1ParallelGroup );		
			theVerticalSequenceGroup.addGroup( theVertical2ParallelGroup );		
		}
		


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
			
			for( MessageInfo messageInfo : _messageInfoList ){
				messageInfo.performAction();
			}
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