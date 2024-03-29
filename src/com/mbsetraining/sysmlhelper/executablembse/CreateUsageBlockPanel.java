package com.mbsetraining.sysmlhelper.executablembse;

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

import com.mbsetraining.sysmlhelper.common.GraphNodeInfo;
import com.mbsetraining.sysmlhelper.common.RhapsodyComboBox;
import com.mbsetraining.sysmlhelper.common.UserInterfaceHelper;
import com.telelogic.rhapsody.core.*;

public class CreateUsageBlockPanel extends ExecutableMBSEBasePanel {

	/**
	 * 
	 */
	private static final long serialVersionUID = 2477351662768784240L;
	protected String _usageUserDefinedMetaClass;
	protected String _blockUserDefinedMetaClass;
	protected List<IRPModelElement> _existingEls;
	protected JTextField _nameTextField = null;
	protected RhapsodyComboBox _selectElComboBox = null;
	protected IRPPackage _owningPackage;
	protected IRPDiagram _sourceDiagram;
	protected IRPInstance _sourceElement;
	protected IRPGraphNode _graphNode;
	final private String _blankName = "<Put Name Here>";

	public static void main(String[] args) {

		IRPApplication theRhpApp = RhapsodyAppServer.getActiveRhapsodyApplication();

		String theAppID = theRhpApp.getApplicationConnectionString();

		ExecutableMBSE_Context context = new ExecutableMBSE_Context( theAppID );

		IRPModelElement theSelectedEl = context.getSelectedElement(true);
		IRPGraphElement theGraphNode = context.getSelectedGraphEl();

		if( theSelectedEl instanceof IRPInstance &&
				theGraphNode instanceof IRPGraphNode ){

			IRPDiagram theDiagram = theGraphNode.getDiagram();

			launchThePanel(
					context.FUNCTION_USAGE, 
					context.FEATURE_BLOCK, 
					theAppID, 
					theSelectedEl.getGUID(), 
					theDiagram.getGUID());
		}
	}

	public static void launchThePanel(
			String theUsageUserDefinedMetaClass,
			String theBlockUserDefinedMetaClass,
			String theAppID,
			String theElementGUID,
			String onDiagramGUID ){

		javax.swing.SwingUtilities.invokeLater(new Runnable() {

			@Override
			public void run() {

				JFrame.setDefaultLookAndFeelDecorated( true );

				JFrame frame = new JFrame( "Create or select a " + theUsageUserDefinedMetaClass);

				frame.setDefaultCloseOperation( JFrame.DISPOSE_ON_CLOSE );

				CreateUsageBlockPanel thePanel = 
						new CreateUsageBlockPanel( 
								theUsageUserDefinedMetaClass,
								theBlockUserDefinedMetaClass,
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

	public CreateUsageBlockPanel( 
			String theUsageUserDefinedMetaClass,
			String theBlockUserDefinedMetaClass,
			String theAppID,
			String theElementGUID,
			String onDiagramGUID ){

		super( theAppID );

		_context.debug( "CreateFunctionUsagePanel constructor was invoked" );

		_usageUserDefinedMetaClass = theUsageUserDefinedMetaClass;
		_blockUserDefinedMetaClass = theBlockUserDefinedMetaClass;	
		_sourceDiagram = (IRPDiagram) _context.get_rhpPrj().findElementByGUID( onDiagramGUID );
		_sourceElement = (IRPInstance) _context.get_rhpPrj().findElementByGUID( theElementGUID );

		IRPCollection theGraphEls = _sourceDiagram.getCorrespondingGraphicElements( _context.getSelectedElement( true ) );

		_graphNode = (IRPGraphNode) theGraphEls.getItem(1);
		_existingEls = new ArrayList<IRPModelElement>();
		_owningPackage = _context.getOwningPackageFor( _context.getSelectedElement( false ) );

		//_context.info( "_sourceDiagram: "  + _context.elInfo( _sourceDiagram ) );
		//_context.info( "_graphNode.ModelObject: "  + _context.elInfo( _graphNode.getModelObject() ) );
		//_context.info( "_creationPackage: "  + _context.elInfo( _creationPackage ) );

		_existingEls.addAll( determineElsToChooseFrom() );

		setLayout( new BorderLayout(10,10) );
		setBorder( BorderFactory.createEmptyBorder( 10, 10, 10, 10 ) );
		add( createClassChoicePanel( _blankName ), BorderLayout.PAGE_START );
		add( createOKCancelPanel(), BorderLayout.PAGE_END );
	}

	private List<IRPModelElement> determineElsToChooseFrom(){

		IRPModelElement theOwner = _sourceElement.getOwner();

		List<IRPModelElement> theElsToChooseFrom = _context.getExistingElementsBasedOn(
				theOwner,
				_context.FUNCT_ANALYSIS_FEATURE_FUNCTION_PACKAGE,
				"Object",
				_usageUserDefinedMetaClass );

		theElsToChooseFrom.addAll( _context.getExistingElementsBasedOn(
				_owningPackage,
				_context.FUNCT_ANALYSIS_FEATURE_FUNCTION_PACKAGE,
				"Class",
				_blockUserDefinedMetaClass ) );

		// We want to make it hard to add elements already on diagram, hence we remove that choice
		@SuppressWarnings("unchecked")
		List<IRPModelElement> theElsOnDiagram = _sourceDiagram.getElementsInDiagram().toList();
		theElsToChooseFrom.removeAll( theElsOnDiagram );

		return theElsToChooseFrom;
	}

	private JPanel createClassChoicePanel(
			String theBlockName ){

		JPanel thePanel = new JPanel();
		thePanel.setLayout( new BoxLayout(thePanel, BoxLayout.X_AXIS ) );	

		_nameTextField = new JTextField( theBlockName );
		_nameTextField.setPreferredSize( new Dimension( 250, 20 ) );

		_selectElComboBox = new RhapsodyComboBox( _existingEls, false );

		_selectElComboBox.addActionListener( new ActionListener () {
			public void actionPerformed( ActionEvent e ){

				IRPModelElement theItem = _selectElComboBox.getSelectedRhapsodyItem();

				if( theItem instanceof IRPClass ){
					_nameTextField.setEnabled( false );
				} else {
					_nameTextField.setEnabled( true );
				}
			}
		});

		JLabel theLeadText = new JLabel( "Create " + _blockUserDefinedMetaClass + " called:  " );

		thePanel.add( theLeadText );
		thePanel.add( _nameTextField );

		JLabel theLabel = new JLabel( "  or select existing: " );
		thePanel.add( theLabel );
		thePanel.add( _selectElComboBox );	    	

		_nameTextField.requestFocusInWindow();

		return thePanel;
	}

	@Override
	protected boolean checkValidity(
			boolean isMessageEnabled) {

		boolean isValid = true;
		String errorMsg = "";

		String theChosenName = _nameTextField.getText();

		boolean isLegalName = _context.isLegalName( theChosenName, _owningPackage );

		IRPModelElement theSelectedEl = _selectElComboBox.getSelectedRhapsodyItem(); 
		
		if (!isLegalName){

			errorMsg += theChosenName + " is not legal as an identifier representing an " + _blockUserDefinedMetaClass + "\n";				
			isValid = false;

		} else if ( theSelectedEl == null && theChosenName.contains( _blankName ) ){

			errorMsg += "Please choose a valid name for the Block";
			isValid = false;

		} else if( theSelectedEl == null && !_context.isElementNameUnique(		
				theChosenName, "Class", _owningPackage, 1 ) ){

			List<IRPModelElement> theExistingEls = _context.
					findElementsWithMetaClassStereotypeAndName(
							"Class", 
							 _context.FUNCTION_BLOCK, 
							theChosenName, 
							_owningPackage );
			
			if( theExistingEls.size() == 1 ) {
				
				IRPModelElement theExistingEl = theExistingEls.get(0);
				boolean answer = UserInterfaceHelper.
						askAQuestion( _context.elInfo( theExistingEl ) + " already exists. Do you want to use this instead?" );
			
				if( answer ) {
					_selectElComboBox.setSelectedRhapsodyItem( theExistingEl );
				} else {
					
					errorMsg += "Unable to proceed as the name '" + theChosenName + "' is not unique";
					isValid = false;
				}
			}

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

			IRPModelElement theSelectedEl = _selectElComboBox.getSelectedRhapsodyItem(); 

			if( theSelectedEl instanceof IRPClass ){

				_context.debug( "Using existing " + _context.elInfo( theSelectedEl ) + 
						" owned by " + _context.elInfo( theSelectedEl.getOwner() ) );

				_sourceElement.setOtherClass( (IRPClassifier) theSelectedEl );

				theSelectedEl.highLightElement();

			} else if( theSelectedEl instanceof IRPInstance ){

				_context.debug( "Using existing " + _context.elInfo( theSelectedEl ) + 
						" owned by " + _context.elInfo( theSelectedEl.getOwner() ) );

				switchElementOnDiagramTo( theSelectedEl );

			} else {

				String theChosenName = _nameTextField.getText();

				_context.debug( "Creating class with name " + theChosenName + 
						" under " + _context.elInfo( _owningPackage ) );

				IRPClassifier theClassifier = (IRPClass) _owningPackage.addNewAggr( "Class", theChosenName );
				theClassifier.changeTo( _blockUserDefinedMetaClass );

				IRPModelElement theOwner = _sourceElement.getOwner();
				IRPInstance theInstance = (IRPInstance) theOwner.addNewAggr( "Object", "" );
				theInstance.changeTo( _usageUserDefinedMetaClass );
				theInstance.setOtherClass( theClassifier );

				switchElementOnDiagramTo( theInstance );				
			}

		} else {
			_context.error( "Error in CreateEventForFlowConnectorPanel.performAction, checkValidity returned false" );
		}		
	}

	private void switchElementOnDiagramTo(
			IRPModelElement theSelectedEl ) {

		GraphNodeInfo theInfo = new GraphNodeInfo( _graphNode, _context );
		IRPDiagram theDiagram = _graphNode.getDiagram();	

		int h = theInfo.getHeight();
		int w = theInfo.getWidth();

		//_context.info( "Adding graph node for " + _context.elInfo( theSelectedEl ) );
		//_context.info( "h = " + h + ", w = " + w );

		IRPGraphNode theGraphNode = theDiagram.addNewNodeForElement( 
				theSelectedEl, 
				theInfo.getTopLeftX()-1, 
				theInfo.getTopLeftY()-1, 
				40, 
				40);

		IRPModelElement theEl = _graphNode.getModelObject();

		theGraphNode.setGraphicalProperty("Height", Integer.toString( h ) );
		theGraphNode.setGraphicalProperty("Width", Integer.toString( w ) );

		IRPCollection theCollection = _context.createNewCollection();
		theCollection.addGraphicalItem(_graphNode);

		theDiagram.removeGraphElements(theCollection);

		if( theEl != null ) {
			theEl.deleteFromProject();
		}

		IRPInstance theInstance = (IRPInstance) theSelectedEl;
		theInstance.highLightElement();

		IRPClassifier theClassifier = (IRPClass) theInstance.getOtherClass();
		theClassifier.highLightElement();
	}
}

/**
 * Copyright (C) 2022-2023  MBSE Training and Consulting Limited (www.executablembse.com)

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