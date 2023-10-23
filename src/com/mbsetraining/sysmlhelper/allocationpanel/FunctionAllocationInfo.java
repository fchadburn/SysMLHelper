package com.mbsetraining.sysmlhelper.allocationpanel;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.util.ArrayList;
import java.util.List;

import javax.swing.JLabel;

import com.mbsetraining.sysmlhelper.common.RhapsodyComboBox;
import com.mbsetraining.sysmlhelper.executablembse.ExecutableMBSE_Context;
import com.telelogic.rhapsody.core.*;

public class FunctionAllocationInfo {

	protected ExecutableMBSE_Context _context;
	protected IRPInstance _usageToAllocate;
	protected List<IRPModelElement> _allocationChoices;
	protected RhapsodyComboBox _allocationChoicesComboBox;
	protected JLabel _nameLabel;
	protected JLabel _statusLabel;
	protected List<IRPModelElement> _validAllocationDependencyBlocks;
	protected List<IRPModelElement> _invalidAllocationDependencies;
	protected List<IRPModelElement> _validAllocatedUsageBlocks;
	protected List<IRPModelElement> _invalidAllocatedUsages;
	protected List<IRPInstance> _currentAllocatedUsages;

	public FunctionAllocationInfo(
			IRPInstance usageToAllocate,
			ExecutableMBSE_Context context ) {

		_context = context;
		_usageToAllocate = usageToAllocate;
		_validAllocationDependencyBlocks = new ArrayList<>();
		_validAllocatedUsageBlocks = new ArrayList<>();
		_invalidAllocationDependencies = new ArrayList<>();
		_invalidAllocatedUsages = new ArrayList<>();
		_currentAllocatedUsages = new ArrayList<>();
	}

	public void buildContentFor(
			List<IRPModelElement> allocationChoices ){

		_allocationChoices = allocationChoices;

		_allocationChoicesComboBox = new RhapsodyComboBox( _allocationChoices, false );

		initializeAllocationsDependencyInfo();
		initializeAllocationUsingPartsInfo();

		_allocationChoicesComboBox.addActionListener( new ActionListener(){
			public void actionPerformed( ActionEvent e ){
				updateAllocationsDependencyInfo();
				updateAllocationUsageInfo();
				_statusLabel.setText( getStatusString() );
			}
		});

		_statusLabel = new JLabel( getStatusString() );

		_statusLabel.addMouseListener( new MouseListener() {

			@Override
			public void mouseReleased(MouseEvent e) {
			}

			@Override
			public void mousePressed(MouseEvent e) {
			}

			@Override
			public void mouseExited(MouseEvent e) {
			}

			@Override
			public void mouseEntered(MouseEvent e) {
			}

			@Override
			public void mouseClicked(MouseEvent e) {

				_context.debug( "Highlighting " + _context.elInfo(_usageToAllocate) + " on the diagram." );
				_usageToAllocate.highLightElement();
			}
		});

		_nameLabel = new JLabel( getUsageName() );

		_nameLabel.addMouseListener( new MouseListener() {

			@Override
			public void mouseReleased(MouseEvent e) {
			}

			@Override
			public void mousePressed(MouseEvent e) {
			}

			@Override
			public void mouseExited(MouseEvent e) {
			}

			@Override
			public void mouseEntered(MouseEvent e) {
			}

			@Override
			public void mouseClicked(MouseEvent e) {

				_context.debug( "Highlighting " + _context.elInfo(_usageToAllocate) + " on the diagram." );
				_context.get_rhpApp().highLightElement(_usageToAllocate);
			}
		});
	}

	public String getUsageName() {
		return _usageToAllocate.getName() + ":" + _usageToAllocate.getOtherClass().getName();
	}

	private void initializeAllocationsDependencyInfo() {

		List<IRPModelElement> existingAllocations = _context.
				findElementsWithMetaClassAndStereotype( "Dependency", "Allocation", _usageToAllocate, 0 );

		for( IRPModelElement existingAllocation : existingAllocations ){

			IRPDependency theDependency = (IRPDependency) existingAllocation;
			IRPModelElement theDependsOn = theDependency.getDependsOn();

			//_context.info( _context.elInfo( _usageToAllocate ) + " has allocate to " + _context.elInfo( theDependsOn ) );

			if( _allocationChoices.contains( theDependsOn ) ) {

				_context.info( "Existing Allocation dependency found from " + _context.elInfo( _usageToAllocate ) + " to " + _context.elInfo( theDependsOn ) );

				_validAllocationDependencyBlocks.add( theDependsOn );
				_allocationChoicesComboBox.setSelectedRhapsodyItem( theDependsOn );

			} else {
				_context.info( "Ignoring existing Allocation dependency found from " + _context.elInfo( _usageToAllocate ) + " to " + 
						_context.elInfo( theDependsOn ) + " as not in scope of current system selection" );
			}
		}
	}

	private void updateAllocationsDependencyInfo() {

		_invalidAllocationDependencies.clear();
		_validAllocationDependencyBlocks.clear();

		IRPModelElement theChosenEl = _allocationChoicesComboBox.getSelectedRhapsodyItem();

		List<IRPModelElement> existingDependencyEls = _context.
				findElementsWithMetaClassAndStereotype( "Dependency", "Allocation", _usageToAllocate, 0 );

		for( IRPModelElement existingDependencyEl : existingDependencyEls ){

			IRPDependency theDependency = (IRPDependency) existingDependencyEl;
			IRPModelElement theDependsOn = theDependency.getDependsOn();

			if( _allocationChoices.contains( theDependsOn ) ) {

				if( !( theChosenEl instanceof IRPClassifier ) ) {

					_context.info( "Existing allocation dependency invalid from " + _context.elInfo( _usageToAllocate ) + " to " + _context.elInfo( theDependsOn ) );
					_invalidAllocationDependencies.add( existingDependencyEl );

				} else if( theDependsOn.equals( theChosenEl ) ) {

					_context.info( "Existing allocation dependency valid from " + _context.elInfo( _usageToAllocate ) + " to " + _context.elInfo( theDependsOn ) );

					if( !_validAllocatedUsageBlocks.contains( theDependsOn ) ) {

						_validAllocationDependencyBlocks.add( theDependsOn );

					} else {
						_context.info( "Existing allocation dependency a duplicate from " + _context.elInfo( _usageToAllocate ) + " to " + _context.elInfo( theDependsOn ) );
						_invalidAllocationDependencies.add( existingDependencyEl );
					}

				} else {

					_context.info( "Existing allocation dependency invalid from " + _context.elInfo( _usageToAllocate ) + " to " + _context.elInfo( theDependsOn ) );
					_invalidAllocationDependencies.add( existingDependencyEl );
				}

			} else {
				_context.info( "Ignoring existing Allocation dependency found from " + _context.elInfo( _usageToAllocate ) + " to " + 
						_context.elInfo( theDependsOn ) + " as not in scope of current system selection" );
			}	
		}	
	}

	private void updateAllocationUsageInfo() {

		_invalidAllocatedUsages.clear();
		_validAllocatedUsageBlocks.clear();

		IRPModelElement theChosenEl = _allocationChoicesComboBox.getSelectedRhapsodyItem();

		for( IRPInstance currentAllocationUsage : _currentAllocatedUsages ){

			IRPModelElement theOwner = currentAllocationUsage.getOwner();

			if( !( theChosenEl instanceof IRPClassifier ) ) {

				_context.info( "Existing allocation usage invalid for " + _context.elInfo( _usageToAllocate ) + " to " + _context.elInfo( theOwner ) );
				_invalidAllocatedUsages.add( currentAllocationUsage );

			} else if( currentAllocationUsage.isTypelessObject()==0 ) {

				if( theOwner.equals( theChosenEl ) ){

					_context.info( "Existing allocation usage valid for " + _context.elInfo( _usageToAllocate ) + " to " + _context.elInfo( theOwner ) );

					if( !_validAllocatedUsageBlocks.contains( theOwner ) ) {

						_validAllocatedUsageBlocks.add( theOwner );

					} else {
						_context.info( "Existing allocation usage duplicate from " + _context.elInfo( _usageToAllocate ) + " to " + _context.elInfo( theOwner ) );
						_invalidAllocatedUsages.add( currentAllocationUsage );
					}
				} else {
					_context.info( "Existing allocation usage invalid from " + _context.elInfo( _usageToAllocate ) + " to " + _context.elInfo( theOwner ) );
					_invalidAllocatedUsages.add( currentAllocationUsage );
				}

			} else {

				_context.info( "Existing allocation usage is typeless from " + _context.elInfo( _usageToAllocate ) + " to " + _context.elInfo( theOwner ) );
			}
		}
	}

	private void initializeAllocationUsingPartsInfo() {

		IRPClassifier theClassifierForAllocation = _usageToAllocate.getOtherClass();

		if( theClassifierForAllocation instanceof IRPClassifier ) {

			@SuppressWarnings("unchecked")
			List<IRPModelElement> theReferences = theClassifierForAllocation.getReferences().toList();

			for( IRPModelElement theReference : theReferences ){

				//_context.info( _context.elInfo( theReference ) + " is a reference for " + _context.elInfo( _usageToAllocate ) );

				if( theReference instanceof IRPInstance ){

					IRPInstance theInstance = (IRPInstance)theReference;
					IRPModelElement theOwner = theInstance.getOwner();

					if( _allocationChoices.contains( theOwner ) ) {

						_context.info( _context.elInfo( _usageToAllocate ) + " is allocated as a part of " + _context.elInfo( theOwner ) );

						_currentAllocatedUsages.add( theInstance );
						_validAllocatedUsageBlocks.add( theOwner );

						_allocationChoicesComboBox.setSelectedRhapsodyItem( theOwner );					
					}
				}
			}
		}
	}

	public String getStatusString() {

		String theMsg = "";

		IRPModelElement theChosenEl = _allocationChoicesComboBox.getSelectedRhapsodyItem();

		//_context.info( "getStatusString invoked for " + _context.elInfo( theChosenEl ) );

		int invalidAllocationDependenciesCount =  _invalidAllocationDependencies.size();
		int invalidAllocatedPartsCount = _invalidAllocatedUsages.size();

		if( theChosenEl instanceof IRPClassifier ) {

			if( invalidAllocatedPartsCount == 0 && 
					invalidAllocationDependenciesCount == 0 &&
					_currentAllocatedUsages.isEmpty() ){

				theMsg += "New. ";

			} else if( _validAllocatedUsageBlocks.contains( theChosenEl ) ) {

				theMsg += "Already allocated. ";

			} else {
				theMsg += "Switch. ";
			}
		}

		if( invalidAllocationDependenciesCount > 0 && invalidAllocatedPartsCount > 0 ) {
			theMsg += invalidAllocatedPartsCount + " allocation relations and " + invalidAllocatedPartsCount + " usages to delete.";
		} else if( invalidAllocationDependenciesCount > 0 ) {
			theMsg += invalidAllocatedPartsCount + " allocation relations and " + invalidAllocatedPartsCount + " usages to delete.";
		} else if( invalidAllocatedPartsCount > 0 ) {
			theMsg += invalidAllocatedPartsCount + " usages to delete.";
		}

		return theMsg;
	}

	public void performAllocationOfUsages() {

		IRPModelElement theChosenEl = _allocationChoicesComboBox.getSelectedRhapsodyItem();

		_context.info( "performAllocationOfUsages with '" + _context.elInfo( theChosenEl ) + "' chosen for " + _context.elInfo( _usageToAllocate ) );

		if( theChosenEl instanceof IRPClassifier ) {

			if( _validAllocatedUsageBlocks.contains( theChosenEl ) ){

				_context.info( "No action necessary as " + _context.elInfo( theChosenEl ) + 
						" already has a usage typed by " + _context.elInfo( _usageToAllocate.getOtherClass() ) );
			} else {

				IRPClassifier allocatedSubsystem = (IRPClassifier) theChosenEl;

				IRPInstance thePart = (IRPInstance) allocatedSubsystem.addNewAggr( "Part", "" ); 

				if( _usageToAllocate.isTypelessObject() == 1 ) {

					String theUniqueName = _context.determineUniqueNameBasedOn( 
							_usageToAllocate.getName(), 
							"Instance", 
							allocatedSubsystem );

					thePart.setName( theUniqueName );

				} else {
					thePart.setOtherClass( _usageToAllocate.getOtherClass() );
				}

				thePart.changeTo( _usageToAllocate.getUserDefinedMetaClass() );

				_validAllocatedUsageBlocks.add( allocatedSubsystem );

				_context.info( "Created " + _context.elInfo( thePart ) + " under " + _context.elInfo( allocatedSubsystem ) );

				_context.deleteAllFromModel( _invalidAllocationDependencies );
				_context.deleteAllFromModel( _invalidAllocatedUsages );

				_context.addStereotypedDependencyIfOneDoesntExist(
						_usageToAllocate, 
						theChosenEl, 
						_context.getStereotypeForAllocation() );			}

		} else {
			_context.deleteAllFromModel( _invalidAllocationDependencies );
			_context.deleteAllFromModel( _invalidAllocatedUsages );
		}
	}
}

/**
 * Copyright (C) 2023  MBSE Training and Consulting Limited (www.executablembse.com)

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
