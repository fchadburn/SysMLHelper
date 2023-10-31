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
	protected List<IRPDependency> _validAllocationDependencies;
	protected List<IRPModelElement> _invalidAllocationDependencies;
	protected List<IRPInstance> _validAllocatedUsages;
	protected List<IRPModelElement> _invalidAllocatedUsages;
	protected List<IRPInstance> _currentAllocatedUsages;

	public FunctionAllocationInfo(
			IRPInstance usageToAllocate,
			ExecutableMBSE_Context context ) {

		_context = context;
		_usageToAllocate = usageToAllocate;
		_validAllocationDependencies = new ArrayList<>();
		_validAllocatedUsages = new ArrayList<>();
		_invalidAllocationDependencies = new ArrayList<>();
		_invalidAllocatedUsages = new ArrayList<>();
		_currentAllocatedUsages = new ArrayList<>();
	}

	public void buildContentFor(
			List<IRPModelElement> allocationChoices ){

		_allocationChoices = allocationChoices;
		_allocationChoicesComboBox = new RhapsodyComboBox( _allocationChoices, false );
		_currentAllocatedUsages = getUsagesInSystemScopeFor( _usageToAllocate );

		updateAssessmentOfAllocations( true );

		_allocationChoicesComboBox.addActionListener( new ActionListener(){
			public void actionPerformed( ActionEvent e ){
				updateAssessmentOfAllocations( false );
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

				if( !_currentAllocatedUsages.isEmpty() ) {
					
					for( IRPInstance currentAllocatedUsage : _currentAllocatedUsages ) {
						currentAllocatedUsage.highLightElement();
					}
					
				} else {
					_usageToAllocate.highLightElement();
				}
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

				_usageToAllocate.highLightElement();
			}
		});
	}

	public String getUsageName() {
		return _usageToAllocate.getName() + ":" + _usageToAllocate.getOtherClass().getName();
	}

	private void updateAssessmentOfAllocations(
			boolean isSetInitialValue ) {

		_context.debug( "updateAssessmentOfAllocations invoked for " + _context.elInfo( _usageToAllocate ) );
		
		_invalidAllocatedUsages.clear();
		_validAllocatedUsages.clear();
		
		_invalidAllocationDependencies.clear();
		_validAllocationDependencies.clear();
		
		List<IRPModelElement> allocationDependencyEls = _context.
				findElementsWithMetaClassAndStereotype( "Dependency", "Allocation", _usageToAllocate, 0 );

		for( IRPModelElement allocationDependencyEl: allocationDependencyEls ){

			IRPDependency theDependency = (IRPDependency) allocationDependencyEl;
			IRPModelElement theDependsOn = theDependency.getDependsOn();

			//_context.debug( _context.elInfo( _usageToAllocate ) + " has allocate to " + _context.elInfo( theDependsOn ) );

			if( _usageToAllocate.isTypelessObject()==1 ){

				// Only consider direct dependencies to instances, i.e. usages, rather than subsystem blocks

				if( theDependsOn instanceof IRPInstance ) {

					IRPModelElement theOwner = theDependsOn.getOwner();
					IRPInstance theInstance = (IRPInstance) theDependsOn;

					if(  _allocationChoices.contains( theOwner ) ) {

						_context.debug( "Existing inferred Allocation dependency found from " + 
								_context.elInfo( _usageToAllocate ) + " to " + _context.elInfo( theDependsOn ) + 
								" owned by " + _context.elInfo( theOwner ) );

						if( isSetInitialValue ) {
							_allocationChoicesComboBox.setSelectedRhapsodyItem( theOwner );
							
							if( !_currentAllocatedUsages.contains( theInstance ) ) {
								_currentAllocatedUsages.add( theInstance );
							} else {
								_context.debug( "Detected that currentAllocationUsages list of " + _currentAllocatedUsages.size() +
										" already contains " + _context.elInfo( theInstance) + " hence not adding again" );
							}
						}
						
						IRPModelElement theChosenEl = _allocationChoicesComboBox.getSelectedRhapsodyItem();

						if( theChosenEl == null || 
								!theChosenEl.equals( theOwner ) ) {

							_invalidAllocationDependencies.add( theDependency );
							_invalidAllocatedUsages.add( theInstance );
						} else {
							_validAllocationDependencies.add( theDependency );
							_validAllocatedUsages.add( theInstance );
						} 


					} else {
						_context.debug( "Ignoring existing Allocation dependency found from " + _context.elInfo( _usageToAllocate ) + " to " + 
								_context.elInfo( theDependsOn ) + " as not in scope of current system selection" );
					}

				} else {
					_context.debug( "Ignoring existing Allocation dependency found from " + _context.elInfo( _usageToAllocate ) + " to " + 
							_context.elInfo( theDependsOn ) + " as not to a usage/instance");					
				}

			} else if( theDependsOn instanceof IRPClassifier &&
					_allocationChoices.contains( theDependsOn ) ) {

				if( isSetInitialValue ) {

					_context.debug( "Existing allocation dependency valid from " + _context.elInfo( _usageToAllocate ) + " to " + _context.elInfo( theDependsOn ) );
					_validAllocationDependencies.add( theDependency );
					
					if( isSetInitialValue ) {
						_allocationChoicesComboBox.setSelectedRhapsodyItem( theDependsOn );
					}	
					
				} else {
					
					IRPModelElement theChosenEl = _allocationChoicesComboBox.getSelectedRhapsodyItem();

					if( theChosenEl == null || !theChosenEl.equals( theDependsOn ) ) {
						
						_context.debug( "Existing allocation dependency invalid from " + _context.elInfo( _usageToAllocate ) + " to " + _context.elInfo( theDependsOn ) );
						_invalidAllocationDependencies.add( theDependency );
					} else {
						_context.debug( "Existing allocation dependency valid from  " + _context.elInfo( _usageToAllocate ) + " to " + _context.elInfo( theDependsOn ) );
						_validAllocationDependencies.add( theDependency );
					}

					if( isSetInitialValue ) {
						_allocationChoicesComboBox.setSelectedRhapsodyItem( theDependsOn );
					}			
				}
						

			} else {
				_context.debug( "Ignoring existing Allocation dependency found from  " + _context.elInfo( _usageToAllocate ) + " to " + 
						_context.elInfo( theDependsOn ) + " as not in scope of current system selection" );
			}
		}
		

		for( IRPInstance currentAllocationUsage : _currentAllocatedUsages ){

			IRPModelElement theOwner = currentAllocationUsage.getOwner();

			if( currentAllocationUsage.isTypelessObject()==0 ) {

				IRPModelElement theChosenEl = _allocationChoicesComboBox.getSelectedRhapsodyItem();

				if( theOwner.equals( theChosenEl ) ){

					_context.debug( "Existing allocation usage valid for " + _context.elInfo( _usageToAllocate ) + " to " + _context.elInfo( theOwner ) );

					if( !_validAllocatedUsages.contains( currentAllocationUsage ) ) {
						_validAllocatedUsages.add( currentAllocationUsage );

					} else {
						_context.debug( "Existing allocation usage duplicate from " + _context.elInfo( _usageToAllocate ) + " to " + _context.elInfo( theOwner ) );
						_invalidAllocatedUsages.add( currentAllocationUsage );
					}
				} else {
					_context.debug( "Existing allocation usage invalid from " + _context.elInfo( _usageToAllocate ) + " to " + _context.elInfo( theOwner ) );
					_invalidAllocatedUsages.add( currentAllocationUsage );
				}

			} else {

				_context.debug( "Existing allocation usage is typeless from  " + _context.elInfo( _usageToAllocate ) + " to " + _context.elInfo( theOwner ) );
			}
		}
	}

	public List<IRPInstance> getUsagesInSystemScopeFor(
			IRPInstance theUsageToAllocate ){

		//_context.info( "getUsagesInSystemScopeFor invoked for " + _context.elInfo( theUsageToAllocate ) );
		
		List<IRPInstance> theInstances = new ArrayList<>();

		if( theUsageToAllocate.isTypelessObject() == 1 ){
			
			
		} else {
			// Check for usages with same class if the usage is not typeless, e.g., a function block

			IRPClassifier theClassifier = theUsageToAllocate.getOtherClass();

			@SuppressWarnings("unchecked")
			List<IRPModelElement> theReferences = theClassifier.getReferences().toList();

			for( IRPModelElement theReference : theReferences ){

				//_context.info( _context.elInfo( theReference ) + " owned by " + _context.elInfo( theReference.getOwner() ) + 
				//		" is a reference for " + _context.elInfo( _usageToAllocate ) );

				if( theReference instanceof IRPInstance ){

					IRPInstance theInstance = (IRPInstance)theReference;
					IRPModelElement theOwner = theInstance.getOwner();

					if( _allocationChoices.contains( theOwner ) ) {

						//_context.info( _context.elInfo( theReference ) + " owned by " + theReference.getOwner() + 
						//		" is a reference for " + _context.elInfo( _usageToAllocate ) );
						
						theInstances.add( theInstance );				
					}
				}
			}
		}
		
		return theInstances;
	}

	public String getStatusString() {

		String theMsg = "";

		IRPModelElement theChosenEl = _allocationChoicesComboBox.getSelectedRhapsodyItem();

		//_context.debug( "getStatusString invoked for " + _context.elInfo( _usageToAllocate ) + " with choice " + _context.elInfo( theChosenEl ) );

		//for (IRPInstance _currentAllocatedUsage : _currentAllocatedUsages) {
		//	_context.info( _context.elInfo( _currentAllocatedUsage ) + " is owned by " + _context.elInfo( _currentAllocatedUsage.getOwner() ) );
		//}

		int validDependenciesCount =  _validAllocationDependencies.size();
		int invalidDependenciesCount =  _invalidAllocationDependencies.size();
		int invalidUsagesCount = _invalidAllocatedUsages.size();

		if( theChosenEl != null ) {

			if( invalidUsagesCount == 0 && 
					invalidDependenciesCount == 0 &&
					_currentAllocatedUsages.isEmpty() ){

				if( validDependenciesCount == 1 ) {
					theMsg += "New (Inferred by Dependency)";

				} else {
					theMsg += "New. ";

				}

			} else if( !_validAllocatedUsages.isEmpty() ) {

				theMsg += "Already allocated. ";

			} else {
				theMsg += "Switch. ";
			}
		}

		if( invalidDependenciesCount > 0 && invalidUsagesCount > 0 ) {
			theMsg += invalidUsagesCount + " allocation relations and " + invalidUsagesCount + " usages to delete.";
		} else if( invalidDependenciesCount > 0 ) {
			theMsg += invalidUsagesCount + " allocation relations and " + invalidUsagesCount + " usages to delete.";
		} else if( invalidUsagesCount > 0 ) {
			theMsg += invalidUsagesCount + " usages to delete.";
		}

		return theMsg;
	}

	public void performAllocationOfUsages() {

		IRPModelElement theChosenEl = _allocationChoicesComboBox.getSelectedRhapsodyItem();

		_context.debug( "performAllocationOfUsages of " + _context.elInfo( _usageToAllocate ) + " => " + _context.elInfo( theChosenEl ) );

		if( theChosenEl instanceof IRPClassifier ) {

			if( !_validAllocatedUsages.isEmpty() ){

				_context.debug( "No action necessary as " + _context.elInfo( theChosenEl ) + 
						" already has a usage typed by " + _context.elInfo( _usageToAllocate.getOtherClass() ) );
			} else {

				IRPClassifier allocatedSubsystem = (IRPClassifier) theChosenEl;

				IRPInstance thePart;

				if( _usageToAllocate.isTypelessObject() == 1 ) {

					// Clone the element so that the ports it owns are also transferred, if they exist
					String theUniqueName = _context.determineUniqueNameBasedOn( 
							_usageToAllocate.getName(), 
							"Instance", 
							allocatedSubsystem );

					thePart = (IRPInstance) _usageToAllocate.clone( theUniqueName, allocatedSubsystem );

				} else {
					thePart = (IRPInstance) allocatedSubsystem.addNewAggr( "Part", "" ); 
					thePart.setOtherClass( _usageToAllocate.getOtherClass() );
					IRPStereotype theStereotype = _usageToAllocate.getNewTermStereotype();

					if( theStereotype != null ) {						
						thePart.setStereotype( theStereotype );
					}
				}
				
				_context.info( "Created " + _context.elInfo( thePart ) + " under " + _context.elInfo( allocatedSubsystem ) );

				cleanUpPreviousAllocationsIfNeeded();

				if( _usageToAllocate.isTypelessObject()== 1 ) {

					_context.addStereotypedDependencyIfOneDoesntExist(
							_usageToAllocate, 
							thePart, 
							_context.getStereotypeForAllocation() );
				} else {

					_context.addStereotypedDependencyIfOneDoesntExist(
							_usageToAllocate, 
							theChosenEl, 
							_context.getStereotypeForAllocation() );	
				}
				
				_validAllocatedUsages.add( thePart );
			}

		} else if( theChosenEl instanceof IRPInstance ) {

			_context.addStereotypedDependencyIfOneDoesntExist(
					_usageToAllocate, 
					theChosenEl, 
					_context.getStereotypeForAllocation() );

		} else {

			cleanUpPreviousAllocationsIfNeeded();
		}
	}

	private void cleanUpPreviousAllocationsIfNeeded() {

		if( !_invalidAllocationDependencies.isEmpty() ) {
			_context.debug( "Deleting " + _invalidAllocationDependencies.size() + " dependencies that are no longer relevant");
			_context.deleteAllFromModel( _invalidAllocationDependencies );
		}

		if( !_invalidAllocatedUsages.isEmpty() ) {		
			_context.debug( "Deleting " + _invalidAllocatedUsages.size() + " part usages that are no longer relevant");
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
