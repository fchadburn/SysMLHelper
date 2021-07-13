package com.mbsetraining.sysmlhelper.sequencediagram;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.JButton;

import com.telelogic.rhapsody.core.IRPModelElement;

public class HighlightModelElementButton extends JButton {

	/**
	 * 
	 */
	private static final long serialVersionUID = -1937382683515679103L;

	protected IRPModelElement _theEl;

	public HighlightModelElementButton(
			String theText,
			IRPModelElement theEl ) {

		super( theText );
		
		_theEl = theEl;

		this.addActionListener( new ActionListener(){

			@Override
			public void actionPerformed(ActionEvent e) {
				_theEl.highLightElement();
			}
		});
	}
}
