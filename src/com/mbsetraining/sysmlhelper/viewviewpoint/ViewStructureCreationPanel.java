package com.mbsetraining.sysmlhelper.viewviewpoint;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Dimension;
import java.util.List;
import javax.swing.BorderFactory;
import javax.swing.BoxLayout;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;

import com.mbsetraining.sysmlhelper.common.UserInterfaceHelper;
import com.mbsetraining.sysmlhelper.executablembse.ExecutableMBSEBasePanel;
import com.mbsetraining.sysmlhelper.graphelementhelpers.GraphNodeResizer;
import com.telelogic.rhapsody.core.*;

public class ViewStructureCreationPanel extends ExecutableMBSEBasePanel {

	/**
	 * 
	 */
	private static final long serialVersionUID = -6527599402965119870L;
	protected JTextField _nameTextField = null;
	protected IRPModelElement _rootPackage;

	public static void main(String[] args) {
		
		IRPApplication theRhpApp = RhapsodyAppServer.getActiveRhapsodyApplication();
		ViewStructureCreationPanel.launchThePanel( theRhpApp.getApplicationConnectionString() );
	}
	
	public static void launchThePanel(
			String theAppID ){

		javax.swing.SwingUtilities.invokeLater(new Runnable() {

			@Override
			public void run() {

				JFrame.setDefaultLookAndFeelDecorated( true );

				JFrame frame = new JFrame( "Create a View/Viewpoint/CustomView structure" );

				frame.setDefaultCloseOperation( JFrame.DISPOSE_ON_CLOSE );

				ViewStructureCreationPanel thePanel = 
						new ViewStructureCreationPanel( 
								theAppID );

				frame.setContentPane( thePanel );
				frame.pack();
				frame.setLocationRelativeTo( null );
				frame.setVisible( true );
			}
		});
	}

	public ViewStructureCreationPanel( 
			String theAppID ){

		super( theAppID );

		_rootPackage = _context.getOwningPackageFor( _context.getSelectedElement( false ) );

		String introText = 
				"This helper will create a View/Viewpoint/CustomView structure underneath \n" + 
						_context.elInfo( _rootPackage ) + ".";
		
		JPanel theStartPanel = new JPanel();

		theStartPanel.setLayout( new BoxLayout( theStartPanel, BoxLayout.PAGE_AXIS ) );
		//theStartPanel.add( new JLabel( " " ) );
		
		theStartPanel.add( createPanelWithTextCentered( introText ) );
		
		setLayout( new BorderLayout(10,10) );
		setBorder( BorderFactory.createEmptyBorder( 10, 10, 10, 10 ) );
		
		add( theStartPanel, BorderLayout.PAGE_START );
		add( createNameChoicePanel( "" ), BorderLayout.CENTER );
		add( createOKCancelPanel(), BorderLayout.PAGE_END );
	}

	private JPanel createNameChoicePanel(String theBlockName){

		JPanel thePanel = new JPanel();
		thePanel.setLayout( new BoxLayout(thePanel, BoxLayout.X_AXIS ) );	
		thePanel.setAlignmentX( Component.LEFT_ALIGNMENT );

		JLabel theLabel = new JLabel( "Identifying name:  " );
		
		_nameTextField = new JTextField();
		_nameTextField.setPreferredSize( new Dimension( 250, 20 ) );

		thePanel.add( theLabel );
		thePanel.add( _nameTextField );

		_nameTextField.requestFocusInWindow();

		return thePanel;
	}

	@Override
	protected boolean checkValidity(
			boolean isMessageEnabled) {

		boolean isValid = true;
		String errorMsg = "";

		String theChosenName = _context.VIEW_PREFIX + _nameTextField.getText();

		boolean isLegalName = _context.isLegalName( theChosenName, _rootPackage );

		if( !isLegalName ){

			errorMsg += theChosenName + " is not legal as an identifier\n";				
			isValid = false;

		} else if (!_context.isElementNameUnique(		
				theChosenName, "Package", _rootPackage, 1 ) ){

			errorMsg += "Unable to proceed as the Event name '" + theChosenName + "' is not unique";
			isValid = false;
		}

		if( isMessageEnabled && 
				!isValid && 
				errorMsg != null ){

			UserInterfaceHelper.showWarningDialog( errorMsg );
		}

		return isValid;
	}

	@Override
	protected void performAction() {

		if( checkValidity( false ) ){

			String theName = _nameTextField.getText();
			String theViewName = _context.VIEW_PREFIX + theName;
			String theViewpointName = _context.VIEWPOINT_PREFIX + theName;
			String theCustomViewName = _context.CUSTOMERVIEW_PREFIX + theName;
			String theViewpointDiagramName = _context.VIEWPOINT_DIAGRAM_PREFIX + theName;
			String theQueryName = _context.QUERY_PREFIX + theName;

			_context.debug( "Creating View with name " + theViewName + 
					" under " + _context.elInfo( _rootPackage ) );

			IRPPackage theView = (IRPPackage) _rootPackage.addNewAggr( "View", theViewName );
			
			// A standard content ViewStructure stereotype will create the structure with an expected naming to be able to 
			// set up the query and custom view settings (as API doesn't seem to allow this). We will then modify this
			
			_context.applyExistingStereotype( _context.VIEW_STRUCTURE_STEREOTYPE, theView );
			
			IRPModelElement theEl = theView.findNestedElement( _context.QUERY_VIEW_STRUCTURE, "Query" );
			
			if( theEl != null ) {
				// The standard context query has this turned off so that the template doesn't affect model
				_context.setBoolPropertyValueInRhp(theEl, "Model.Query.ShowInBrowserFilterList", true );
			}
			
			IRPModelElement theCustomViewEl = theView.findNestedElement( _context.CUSTOMV_VIEW_STRUCTURE, "Package" );
			
			if( theCustomViewEl != null ) {
				theCustomViewEl.highLightElement();
			}
			
			_context.renameNestedElement( _context.VIEWPOINT_VIEW_STRUCTURE, "Class", theView, theViewpointName );
			_context.renameNestedElement( _context.CUSTOMV_VIEW_STRUCTURE, "Package", theView, theCustomViewName );
			_context.renameNestedElement( _context.VIEW_AND_VIEWPOINT_DIAGRAM_VIEW_STRUCTURE, "ObjectModelDiagram", theView, theViewpointDiagramName );
			_context.renameNestedElement( _context.QUERY_VIEW_STRUCTURE, "Query", theView, theQueryName );
						
		} else {
			_context.error( "Error in ViewStructureCreationPanel.performAction, checkValidity returned false" );
		}		
	}
	
	protected IRPObjectModelDiagram createViewViewpointDiagramBasedOn(
			IRPPackage theView,
			IRPModelElement underRootEl,
			String withName ) {
		
		String theUniqueName = _context.determineUniqueNameBasedOn( withName, withName, underRootEl );
		
		IRPCollection theElsToDrawRelationsFor = _context.createNewCollection();

		IRPObjectModelDiagram theDiagram = (IRPObjectModelDiagram) underRootEl.addNewAggr( "ObjectModelDiagram", theUniqueName );
		theDiagram.changeTo( "View and Viewpoint Diagram" );
		
		IRPGraphNode theViewNode = theDiagram.addNewNodeForElement( theView, 100, 100, 50, 50 );
		GraphNodeResizer theViewNodeResizer = new GraphNodeResizer( theViewNode, _context );
		theViewNodeResizer.performResizing();
		
		theElsToDrawRelationsFor.addGraphicalItem( theViewNode );
		
		@SuppressWarnings("unchecked")
		List<IRPDependency> theDependencies = theView.getDependencies().toList();
		
		int x = 300;
		int y = 300;
		int nudgeX = 50;
		int nudgeY = 50;
		
		for (IRPDependency theDependency : theDependencies) {
			
			IRPModelElement theViewpointEl = theDependency.getDependsOn();
						
			if( theDependency.getUserDefinedMetaClass().equals( "Conform") && 
					theViewpointEl.getUserDefinedMetaClass().equals( "Viewpoint" ) ) {
				
				IRPGraphNode theViewpointNode = theDiagram.addNewNodeForElement( theViewpointEl, x, y, 50, 50 );
				GraphNodeResizer theViewpointNodeResizer = new GraphNodeResizer( theViewpointNode, _context );
				theViewpointNodeResizer.performResizing();		
				
				theElsToDrawRelationsFor.addGraphicalItem( theViewpointNode );
				
				x += nudgeX;
				y += nudgeY;
			} else {
				_context.warning( "Unexpected " + _context.elInfo( theViewpointEl ) + " in createViewViewpointDiagramBasedOn with conforms relation");
			}
		}
		
		theDiagram.completeRelations( theElsToDrawRelationsFor, 1 );
		
		return theDiagram;
	}
}

/**
 * Copyright (C) 2022  MBSE Training and Consulting Limited (www.executablembse.com)

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