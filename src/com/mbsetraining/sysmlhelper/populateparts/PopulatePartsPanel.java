package com.mbsetraining.sysmlhelper.populateparts;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.GridLayout;
import java.util.Iterator;
import java.util.List;

import javax.swing.BoxLayout;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.border.EmptyBorder;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeModel;
import javax.swing.tree.TreeModel;

import com.mbsetraining.sysmlhelper.common.GraphNodeInfo;
import com.mbsetraining.sysmlhelper.common.UserInterfaceHelper;
import com.mbsetraining.sysmlhelper.executablembse.ExecutableMBSEBasePanel;
import com.telelogic.rhapsody.core.*;

public class PopulatePartsPanel extends ExecutableMBSEBasePanel {

	/**
	 * 
	 */
	private static final long serialVersionUID = 5696361604359498814L;

	private JCheckBoxTree _tree;
	private IRPGraphNode _rootGraphNode;
	private IRPModelElement _rootModelEl;
	private IRPDiagram _diagram;
	private final int _yGap = 10;
	private final int _yTop = 50;
	private final int _yBottom = 30;
	private final int _xLeft = 30;
	private final int _xRight = 30;

	/**
	 * @param args
	 */
	public static void main(String[] args) {

		final String theAppID = 
				RhapsodyAppServer.getActiveRhapsodyApplication().getApplicationConnectionString();

		launchTheDialog( theAppID );		
	}

	public static void launchTheDialog(
			String theAppID ){

		UserInterfaceHelper.setLookAndFeel();

		javax.swing.SwingUtilities.invokeLater(new Runnable() {

			@Override
			public void run() {

				JFrame.setDefaultLookAndFeelDecorated( true );

				JFrame frame = new JFrame( "Populate parts" );

				frame.setDefaultCloseOperation( JFrame.DISPOSE_ON_CLOSE );

				PopulatePartsPanel thePanel = 
						new PopulatePartsPanel(
								theAppID );

				frame.setContentPane( thePanel );
				frame.pack();
				frame.setLocationRelativeTo( null );
				frame.setVisible( true );
			}
		});
	}

	public PopulatePartsPanel( 
			String theAppID ){

		super( theAppID );

		setLayout( new BorderLayout() );
		setBorder( new EmptyBorder(0, 10, 10, 10) );

		List<IRPGraphNode> theSelectedGraphNodes = _context.getSelectedGraphNodes();

		if( theSelectedGraphNodes.size()==1 ){

			_rootGraphNode = theSelectedGraphNodes.get( 0 );
			_diagram = _rootGraphNode.getDiagram();
			_rootModelEl = _rootGraphNode.getModelObject();

			if( _rootModelEl == null ){

				_context.error( "PopulatePartsPanel cannot find a model object for graph node on " + 
						_context.elInfo( _diagram ) );
			}
		} else {
			IRPModelElement theSelectedEl = _context.getSelectedElement( false );

			if( theSelectedEl instanceof IRPStructureDiagram ){

				_diagram = (IRPDiagram) theSelectedEl;
				_rootModelEl = _diagram.getOwner();
			}
		}

		_context.debug( "theSelectedEl is " + _context.elInfo( _rootModelEl ) );
		_context.debug( "the diagram is " + _context.elInfo( _diagram ) );

		JPanel theContentPanel = createContent( _rootModelEl );
		theContentPanel.setAlignmentX( Component.LEFT_ALIGNMENT );

		String introText = 
				"This helper will populate the diagram with parts of the system";

		JPanel theStartPanel = new JPanel();

		theStartPanel.setLayout( new BoxLayout( theStartPanel, BoxLayout.PAGE_AXIS ) );
		theStartPanel.add( new JLabel( " " ) );
		theStartPanel.add( createPanelWithTextCentered( introText ) );

		add( theStartPanel, BorderLayout.PAGE_START );
		add( theContentPanel, BorderLayout.CENTER );
		add( createOKCancelPanel(), BorderLayout.PAGE_END );		
	}

	private JPanel createContent(
			IRPModelElement basedOnModelEl ){

		JPanel thePanel = new JPanel();

		thePanel.setLayout(new GridLayout(1,0));

		IRPClassifier theClassifier = null;
		IRPInstance thePart = null;

		if( basedOnModelEl instanceof IRPInstance ){

			thePart = (IRPInstance) basedOnModelEl;
			theClassifier = thePart.getOtherClass();

		} else if( basedOnModelEl instanceof IRPClassifier ){

			theClassifier = (IRPClassifier) basedOnModelEl;
		}

		//Create the nodes.
		DefaultMutableTreeNode top =
				new DefaultMutableTreeNode( 
						new ModelElInfo( theClassifier, thePart, false, _context ) );

		createNodes( top );

		TreeModel treeModel = new DefaultTreeModel( top );
		_tree = new JCheckBoxTree( _context );                
		_tree.setModel( treeModel );

		//Create the scroll pane and add the tree to it. 
		JScrollPane treeView = new JScrollPane(_tree);

		//Create the HTML viewing pane.
		//		htmlPane = new JEditorPane();
		//		htmlPane.setEditable(false);

		//		JScrollPane htmlView = new JScrollPane(htmlPane);

		//Add the scroll panes to a split pane.
		//		JSplitPane splitPane = new JSplitPane(JSplitPane.VERTICAL_SPLIT);
		//		splitPane.setTopComponent(treeView);
		//		splitPane.setBottomComponent(htmlView);

		Dimension minimumSize = new Dimension(100, 50);
		//		htmlView.setMinimumSize(minimumSize);
		treeView.setMinimumSize(minimumSize);
		//		splitPane.setDividerLocation(100); 
		treeView.setPreferredSize(new Dimension(500, 300));

		//Add the split pane to this panel.
		thePanel.add( treeView );

		return thePanel;
	}

	private void createNodes(
			DefaultMutableTreeNode underNode ){

		ModelElInfo theModelElInfo = (ModelElInfo) underNode.getUserObject();

		List<ModelElInfo> theChildren = theModelElInfo.getChildren();

		for( ModelElInfo theChild : theChildren ){

			DefaultMutableTreeNode theChildNode = new DefaultMutableTreeNode( theChild );
			underNode.add( theChildNode );
			createNodes( theChildNode );
		}        
	}

	@Override
	protected boolean checkValidity(boolean isMessageEnabled) {
		return true;
	}

	@Override
	protected void performAction() {

		try {
			if( checkValidity( false ) ){

				_context.debug("performAction was invoked");

				TreeModel theModel = _tree.getModel();

				DefaultMutableTreeNode node = (DefaultMutableTreeNode)theModel.getRoot();

				if( _rootGraphNode != null ){
					Dimension theDimension = calculateDimensionOf( node );

					_rootGraphNode.setGraphicalProperty( "Height", Integer.toString( theDimension.height ) );
					_rootGraphNode.setGraphicalProperty( "Width", Integer.toString( theDimension.width ) );									
				}

				populateParts( node, _rootGraphNode );
			}
		} catch (Exception e) {
			_context.error( "Exception in performAction, e=" + e.getMessage() );
		}
	}

	Dimension calculateDimensionOf( 
			DefaultMutableTreeNode theNode ){

		int width = 0;
		int height = 0;

		ModelElInfo theUserObject = (ModelElInfo) theNode.getUserObject();

		if( _tree.isSelected( theNode ) && theNode.isLeaf() ){

			String theDefaultSize = null;

			if( theUserObject._part != null ){

				IRPStereotype theNewTerm = theUserObject._part.getNewTermStereotype();

				if( theNewTerm != null ){
					theDefaultSize = _diagram.getPropertyValue( 
							"Format." + theNewTerm.getName() + ".DefaultSize" );
				} else {
					_context.debug( "calculateDimensionOf for " + theUserObject.toString() + 
							" is using default size as " + _context.elInfo( theUserObject._part ) + 
							" did not have a new term stereotype" );

					theDefaultSize = _diagram.getPropertyValue( 
							"Format.Object.DefaultSize" );					
				}

			} else if( theUserObject._classifier != null ){

				IRPStereotype theNewTerm = theUserObject._classifier.getNewTermStereotype();

				if( theNewTerm != null ){
					theDefaultSize = _diagram.getPropertyValue( 
							"Format." + theNewTerm.getName() + ".DefaultSize" );
				} else {
					_context.debug( "calculateDimensionOf for " + theUserObject.toString() + 
							" is using default size as " + _context.elInfo( theUserObject._part ) + 
							" did not have a new term stereotype" );

					theDefaultSize = _diagram.getPropertyValue( 
							"Format.Class.DefaultSize" );					
				}
			}

			if( theDefaultSize == null ){
				_context.error( "Unable to find default size for " + _context.elInfo( theUserObject._part ) ); 
			}

			String[] theSplit = theDefaultSize.split(",");
			width = Integer.parseInt( theSplit[2] );
			height = Integer.parseInt( theSplit[3] );

		} else {

			height += _yTop;

			List<DefaultMutableTreeNode> theChildNodes = _tree.getSelectedChildren( theNode );

			for( Iterator<DefaultMutableTreeNode> iterator = theChildNodes.iterator(); iterator.hasNext();) {

				DefaultMutableTreeNode theChildNode = (DefaultMutableTreeNode) iterator.next();

				Dimension theChildsDimension = calculateDimensionOf( theChildNode );					
				height += theChildsDimension.height;

				int proposedWidth = theChildsDimension.width + _xLeft + _xRight;

				if( width < proposedWidth ){
					width = proposedWidth;
				}

				if( iterator.hasNext() ){
					height += _yGap;
				} else {
					height += _yBottom;
				}
			}
		}

		Dimension theDimension = new Dimension( width, height );

		return theDimension;
	}


	public void populateParts(
			DefaultMutableTreeNode theTreeNode,
			IRPGraphNode theGraphNode ) throws Exception{

		List<DefaultMutableTreeNode> theChildNodes = _tree.getSelectedChildren( theTreeNode );

		if( theChildNodes.size() > 0 ){

			int x = _xLeft;
			int y = _yTop;

			if( theGraphNode != null ){

				GraphNodeInfo theParentNodeInfo = new GraphNodeInfo( theGraphNode, _context );

				x += theParentNodeInfo.getTopLeftX();
				y += theParentNodeInfo.getTopLeftY();			

				theGraphNode.setGraphicalProperty( "StructureView", "True" );
				theGraphNode.setGraphicalProperty( "Type", "ImplementationObject" );
			}

			for( Iterator<DefaultMutableTreeNode> iterator = theChildNodes.iterator(); iterator.hasNext();) {

				DefaultMutableTreeNode theChildNode = (DefaultMutableTreeNode) iterator.next();

				ModelElInfo theUserObject = (ModelElInfo) theChildNode.getUserObject();

				// If element is a base class then we can skip drawing and go straight to its children
				if( theUserObject._isSilent ){

					populateParts( theChildNode, theGraphNode );

				} else {
					IRPInstance thePart = theUserObject._part;

					Dimension theChildsDimension = calculateDimensionOf( theChildNode );			

					_context.debug( "Adding graph node for " + theUserObject.toString() + 
							" at x=" + x + ", y=" + y + 
							", height=" + theChildsDimension.height + 
							", width=" + theChildsDimension.width );

					IRPGraphNode theChildsGraphNode = _diagram.addNewNodeForElement(
							thePart, 
							x, 
							y, 
							theChildsDimension.width, 
							theChildsDimension.height );

					IRPCollection theCollection = 
							_context.get_rhpApp().createNewCollection();

					theCollection.addGraphicalItem( theChildsGraphNode );	    

					_context.debug( "Invoking complete relations for " + theCollection.getCount() + " elements" );

					_diagram.completeRelations(
							theCollection, 
							1);

					if( iterator.hasNext() ){
						y += theChildsDimension.getHeight() + _yGap;
					}

					populateParts( theChildNode, theChildsGraphNode );
				}
			}		
		}
	}
}

/**
 * Copyright (C) 2018-2021  MBSE Training and Consulting Limited (www.executablembse.com)

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