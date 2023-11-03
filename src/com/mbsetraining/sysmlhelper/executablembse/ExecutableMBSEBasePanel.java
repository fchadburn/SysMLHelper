package com.mbsetraining.sysmlhelper.executablembse;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.ComponentOrientation;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Window;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.ArrayList;
import java.util.List;

import javax.swing.BorderFactory;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JList;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextPane;
import javax.swing.ListSelectionModel;
import javax.swing.ScrollPaneConstants;
import javax.swing.SwingUtilities;
import javax.swing.text.SimpleAttributeSet;
import javax.swing.text.StyleConstants;
import javax.swing.text.StyledDocument;

import com.mbsetraining.sysmlhelper.common.NamedElementMap;
import com.telelogic.rhapsody.core.*;

public abstract class ExecutableMBSEBasePanel extends JPanel {

	/**
	 * 
	 */
	private static final long serialVersionUID = -6413611438061239075L;
	
	protected ExecutableMBSE_Context _context;

	protected ExecutableMBSEBasePanel(
			String theAppID ){
		
		super();
		
		_context = new ExecutableMBSE_Context( theAppID );
	}
	
	List<IRPUnit> m_UnitsForReadWrite = new ArrayList<IRPUnit>();
	
	// implementation specific provided by parent
	protected abstract boolean checkValidity(boolean isMessageEnabled);
	
	// implementation specific provided by parent
	protected abstract void performAction();
	
	public JPanel createOKCancelPanel(){
		
		JPanel thePanel = new JPanel();
		thePanel.setLayout( new FlowLayout() );
		
		JButton theOKButton = new JButton("OK");
		theOKButton.setPreferredSize( new Dimension( 75,25 ) );
		theOKButton.addActionListener( new ActionListener() {
			
			@Override
			public void actionPerformed( ActionEvent e ) {
				
				try {
					boolean isValid = checkValidity( true );
					
					if( isValid ){
						performAction();
						Window dialog = SwingUtilities.windowForComponent( (Component) e.getSource() );
						dialog.dispose();
					}
												
				} catch( Exception e2 ){
					_context.error( "Unhandled exception in createOKCancelPanel->theOKButton.actionPerformed e2=" + e2.getMessage() );
				}
			}
		});
		
		JButton theCancelButton = new JButton( "Cancel" );
		theCancelButton.setPreferredSize( new Dimension( 75,25 ) );	
		theCancelButton.addActionListener( new ActionListener() {
			
			@Override
			public void actionPerformed( ActionEvent e ){
				
				try {
					Window dialog = SwingUtilities.windowForComponent( (Component) e.getSource() );
					dialog.dispose();
												
				} catch( Exception e2 ){
					_context.error("Unhandled exception in createOKCancelPanel->theCancelButton.actionPerformed, e2=" + e2.getMessage());
				}		
			}	
		});
		
		thePanel.setComponentOrientation( ComponentOrientation.LEFT_TO_RIGHT );
		thePanel.add( theOKButton );
		thePanel.add( theCancelButton );
		
		return thePanel;
	}
	
	public JPanel createUpdateCancelPanelWith( 
			String theUpdateText ){
		
		JPanel thePanel = new JPanel();
		thePanel.setLayout( new FlowLayout() );
		
		JButton theOKButton = new JButton( theUpdateText );
		theOKButton.setPreferredSize(new Dimension( 140,25 ));
		theOKButton.addActionListener( new ActionListener() {
			
			@Override
			public void actionPerformed(ActionEvent e) {
				
				try {
					boolean isValid = checkValidity( true );
					
					if (isValid){
						performAction();
						Window dialog = SwingUtilities.windowForComponent( (Component) e.getSource() );
						dialog.dispose();
					}
												
				} catch (Exception e2) {
					_context.error( "Unhandled exception in createOKCancelPanel->theOKButton.actionPerformed e2=" + e2.getMessage() );
				}
			}
		});
		
		JButton theCancelButton = new JButton("Cancel");
		theCancelButton.setPreferredSize( new Dimension( 140,25 ) );	
		theCancelButton.addActionListener( new ActionListener() {
			
			@Override
			public void actionPerformed(ActionEvent e) {
				
				try {
					Window dialog = SwingUtilities.windowForComponent( (Component) e.getSource() );
					dialog.dispose();
												
				} catch (Exception e2) {
					_context.error("Unhandled exception in createOKCancelPanel->theCancelButton.actionPerformed, e2=" + e2.getMessage());
				}		
			}	
		});
		
		thePanel.setComponentOrientation( ComponentOrientation.LEFT_TO_RIGHT );
		thePanel.add( theOKButton );
		thePanel.add( theCancelButton );
		
		return thePanel;
	}
	
	protected Component createPanelWithTextCentered(
			String theText){
		
		JTextPane theTextPane = new JTextPane();
		theTextPane.setBorder( BorderFactory.createEmptyBorder( 10, 10, 10, 10 ) );
		theTextPane.setBackground( new Color( 238, 238, 238 ) );
		theTextPane.setEditable( false );
		theTextPane.setText( theText );
		
		StyledDocument theStyledDoc = theTextPane.getStyledDocument();
		SimpleAttributeSet center = new SimpleAttributeSet();
		StyleConstants.setAlignment( center, StyleConstants.ALIGN_CENTER );

		theStyledDoc.setParagraphAttributes( 0, theStyledDoc.getLength(), center, false );

		JPanel thePanel = new JPanel();
		thePanel.add( theTextPane );
		
		return thePanel;
	}
	
	protected void notifyReadWriteNeededFor(
			IRPModelElement theEl ){
				
		IRPUnit theUnit = theEl.getSaveUnit();
		
		_context.debug("notifyReadWriteNeededFor has determined that the Unit for " + _context.elInfo(theEl) + " is " + _context.elInfo(theUnit));
		
		if( !m_UnitsForReadWrite.contains( theUnit ) ){
			m_UnitsForReadWrite.add( theUnit );
		}
	}
	
	protected List<IRPUnit> getLockedUnits(){
	
		List<IRPUnit> theLockedUnits = new ArrayList<>();
		
		for( IRPUnit theUnit : m_UnitsForReadWrite ){
			
			int isReadyOnly = theUnit.isReadOnly();
			
			if( isReadyOnly==1 ){
				theLockedUnits.add( theUnit );
			}
		}

		return theLockedUnits;
	}
	
	protected void buildUnableToRunDialog(
			String withMsg ){
		
		setLayout( new BorderLayout(10,10) );
		setBorder( BorderFactory.createEmptyBorder( 10, 10, 10, 10 ) );

		JPanel thePageStartPanel = new JPanel();
		thePageStartPanel.setLayout( new BoxLayout( thePageStartPanel, BoxLayout.X_AXIS ) );
		thePageStartPanel.add( createPanelWithTextCentered( withMsg ) );

		add( thePageStartPanel, BorderLayout.PAGE_START );
		
		add( createCancelPanel(), BorderLayout.PAGE_END );
	}
	
	protected JScrollPane createDoubleClickableScrollPaneFor( 
			List<IRPModelElement> theEls ){
		
		List<IRPModelElement> theFoundEls = new ArrayList<>( theEls );			
		final NamedElementMap theNamedElMap = new NamedElementMap( theFoundEls );

		Object[] dataList = theNamedElMap.getFullNamesIn();

		JList<Object> list = new JList<>( dataList );
		list.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);

		list.addMouseListener(new MouseAdapter() {
			public void mouseClicked(MouseEvent evt) {
				@SuppressWarnings("rawtypes")
				JList list = (JList)evt.getSource();

				if (evt.getClickCount() >= 2) {

					// Double-click detected
					int index = list.locationToIndex(evt.getPoint());

					IRPModelElement theElement = theNamedElMap.getElementAt(index);

					if( theElement == null ){

						JDialog.setDefaultLookAndFeelDecorated(true);

						JOptionPane.showMessageDialog(
								null, 
								"Element no longer exists", 
								"Warning",
								JOptionPane.WARNING_MESSAGE);

					} else {
						theElement.highLightElement();
					}   

					_context.debug( _context.elInfo( theElement ) + " was double-clicked" );
				}
			}
		});

		JScrollPane theScrollPane = new JScrollPane(list);
		theScrollPane.setBounds(1,1,16,58);

		theScrollPane.setVerticalScrollBarPolicy( ScrollPaneConstants.VERTICAL_SCROLLBAR_ALWAYS);
		return theScrollPane;
	}
	
	public JPanel createCancelPanel(){
		
		JPanel thePanel = new JPanel();
		thePanel.setLayout( new FlowLayout() );
				
		JButton theCancelButton = new JButton("Cancel");
		theCancelButton.setPreferredSize(new Dimension(75,25));
		
		theCancelButton.addActionListener( new ActionListener() {
			
			@Override
			public void actionPerformed(ActionEvent e) {
				
				try {
					Window dialog = SwingUtilities.windowForComponent( (Component) e.getSource() );
					dialog.dispose();
												
				} catch (Exception e2) {
					_context.error("Error, unhandled exception in CreateOperationPanel.createOKCancelPanel on Cancel button action listener, e2=" + e2.getMessage() );
				}
			}	
		});
		
		thePanel.setComponentOrientation(ComponentOrientation.LEFT_TO_RIGHT);
		thePanel.add( theCancelButton );
		
		return thePanel;
	}
}

/**
 * Copyright (C) 2016-2023  MBSE Training and Consulting Limited (www.executablembse.com)

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