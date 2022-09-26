package com.mbsetraining.sysmlhelper.graphtraversal;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.List;

import javax.swing.BorderFactory;
import javax.swing.BoxLayout;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;

import com.mbsetraining.sysmlhelper.businessvalueplugin.BusinessValue_BasePanel;
import com.mbsetraining.sysmlhelper.businessvalueplugin.BusinessValue_Context;
import com.mbsetraining.sysmlhelper.common.RhapsodyComboBox;
import com.mbsetraining.sysmlhelper.common.UserInterfaceHelper;
import com.telelogic.rhapsody.core.*;

public class CreateClassForGraphPathCapturePanel extends BusinessValue_BasePanel {

	/**
	 * 
	 */
	private static final long serialVersionUID = -4947635266699088981L;
	
	protected List<IRPModelElement> _existingClassEls;
	protected JTextField _nameTextField = null;
	protected RhapsodyComboBox _selectClassComboBox = null;
	protected IRPPackage _creationPackage;
	protected IRPDiagram _sourceDiagram;
	protected IRPModelElement _rootElement;

	public static void main(String[] args) {

		IRPApplication theRhpApp = RhapsodyAppServer.getActiveRhapsodyApplication();

		String theAppID = theRhpApp.getApplicationConnectionString();

		BusinessValue_Context context = new BusinessValue_Context( theAppID );

		IRPModelElement theSelectedEl = context.getSelectedElement(true);
		IRPGraphElement theGraphNode = context.getSelectedGraphEl();

		if( theSelectedEl instanceof IRPClass &&
				theGraphNode instanceof IRPGraphNode ){

			IRPDiagram theDiagram = theGraphNode.getDiagram();

			launchThePanel(theAppID, theSelectedEl.getGUID(), theDiagram.getGUID());
		}
	}

	public static void launchThePanel(
			String theAppID,
			String theElementGUID,
			String onDiagramGUID ){

		javax.swing.SwingUtilities.invokeLater(new Runnable() {

			@Override
			public void run() {

				JFrame.setDefaultLookAndFeelDecorated( true );

				JFrame frame = new JFrame( "Perform graphical analysis" );

				frame.setDefaultCloseOperation( JFrame.DISPOSE_ON_CLOSE );

				CreateClassForGraphPathCapturePanel thePanel = 
						new CreateClassForGraphPathCapturePanel( 
								theAppID, 
								theElementGUID,
								onDiagramGUID );

				frame.setContentPane( thePanel );
				frame.pack();
				frame.setLocationRelativeTo( null );
				frame.setVisible( true );
			}
		});
	}

	public CreateClassForGraphPathCapturePanel( 
			String theAppID,
			String theElementGUID,
			String onDiagramGUID ){

		super( theAppID );

		_context.debug( "CreateEventForFlowConnectorPanel constructor was invoked" );

		_sourceDiagram = (IRPDiagram) _context.get_rhpPrj().findElementByGUID( onDiagramGUID );
		
		_rootElement = (IRPModelElement) _context.get_rhpPrj().findElementByGUID( theElementGUID );
		
		String theDefaultName = _context.toLegalClassName( _rootElement.getName() );
		
		_existingClassEls = new ArrayList<IRPModelElement>();

		_creationPackage = _context.getOwningPackageFor( _context.getSelectedElement( false ) );

		_existingClassEls.addAll( determineClassesToChooseFrom() );

		setLayout( new BorderLayout(10,10) );
		setBorder( BorderFactory.createEmptyBorder( 10, 10, 10, 10 ) );
		add( createClassChoicePanel( theDefaultName ), BorderLayout.PAGE_START );
		add( createOKCancelPanel(), BorderLayout.PAGE_END );
	}

	private List<IRPClass> determineClassesToChooseFrom(){

		List<IRPClass> theClassesToChooseFrom = new ArrayList<>();

		@SuppressWarnings("unchecked")
		List<IRPClass> theClasses = _creationPackage.getNestedElementsByMetaClass( 
				"Class", 1 ).toList();

		for( IRPClass theClass : theClasses ){

			if( theClass.getNewTermStereotype() == null ){
				theClassesToChooseFrom.add( theClass );
			}
		}

		return theClassesToChooseFrom;
	}

	private JPanel createClassChoicePanel(
			String theBlockName ){

		JPanel thePanel = new JPanel();
		thePanel.setLayout( new BoxLayout(thePanel, BoxLayout.X_AXIS ) );	

		_nameTextField = new JTextField( theBlockName );
		_nameTextField.setPreferredSize( new Dimension( 250, 20 ) );

		_selectClassComboBox = new RhapsodyComboBox( _existingClassEls, false );

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

		JLabel theLeadText = new JLabel( "Create path analysis class called:" );

		thePanel.add( theLeadText );
		thePanel.add( _nameTextField );

		JLabel theLabel = new JLabel( "  or select existing: " );
		thePanel.add( theLabel );
		thePanel.add( _selectClassComboBox );	    	

		_nameTextField.requestFocusInWindow();

		return thePanel;
	}

	@Override
	protected boolean checkValidity(
			boolean isMessageEnabled) {

		boolean isValid = true;
		String errorMsg = "";

		String theChosenName = _nameTextField.getText();

		boolean isLegalName = _context.isLegalName( theChosenName, _creationPackage );

		if (!isLegalName){

			errorMsg += theChosenName + " is not legal as an identifier representing an Event\n";				
			isValid = false;

		} else if (!_context.isElementNameUnique(		
				theChosenName, "Class", _creationPackage, 1 ) ){

			errorMsg += "Unable to proceed as the Class name '" + theChosenName + "' is not unique";
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

			IRPClass theClass = null;

			IRPModelElement theExistingClass = _selectClassComboBox.getSelectedRhapsodyItem(); 

			if( theExistingClass instanceof IRPClass ){

				_context.debug( "Using existing " + _context.elInfo( theExistingClass ) + 
						" owned by " + _context.elInfo( theExistingClass.getOwner() ) );

				theClass = (IRPClass) theExistingClass;

			} else {

				String theChosenName = _nameTextField.getText();

				_context.debug( "Creating class with name " + theChosenName + 
						" under " + _context.elInfo( _creationPackage ) );

				theClass = (IRPClass) _creationPackage.addNewAggr( "Class", theChosenName );
			}

			theClass.highLightElement();

			Node startNode = new Node( _rootElement, _context );

			_context.info( "root node is " + startNode.toString() );

			GraphPath theCurrentPath = new GraphPath( _context );
			GraphPaths allPaths = new GraphPaths( _context );

			startNode.buildRecursively( theCurrentPath, allPaths );

			allPaths.dumpInfo();

			allPaths.createDependenciesAndPathVisualization( 
					theClass, _creationPackage, _sourceDiagram );

		} else {
			_context.error( "Error in CreateEventForFlowConnectorPanel.performAction, checkValidity returned false" );
		}		
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