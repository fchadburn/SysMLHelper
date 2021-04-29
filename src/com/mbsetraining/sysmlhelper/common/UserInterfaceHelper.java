package com.mbsetraining.sysmlhelper.common;

import java.util.ArrayList;
import java.util.List;

import javax.swing.JOptionPane;
import javax.swing.UIManager;
import javax.swing.UnsupportedLookAndFeelException;

import com.telelogic.rhapsody.core.*;

public class UserInterfaceHelper {

	public static void setLookAndFeel(){
		
		try {
			UIManager.setLookAndFeel("javax.swing.plaf.metal.MetalLookAndFeel");
//					"com.sun.java.swing.plaf.motif.MotifLookAndFeel");
//					"com.sun.java.swing.plaf.windows.WindowsLookAndFeel");
			
		} catch (ClassNotFoundException | InstantiationException | IllegalAccessException
				| UnsupportedLookAndFeelException e) {
			e.printStackTrace();
		}
	}
	
	public static boolean askAQuestion(
			String question ){
		 
		setLookAndFeel();
		
		int answer = JOptionPane.showConfirmDialog(
				null, 
				question, 
				"Question?", 
				JOptionPane.YES_NO_OPTION);
		
		return (answer == JOptionPane.YES_OPTION);
	}
	
	public static void showWarningDialog(
			String theMsg ){
				
		setLookAndFeel();
	    
	    JOptionPane.showMessageDialog(
	    		null,  
	    		theMsg,
	    		"Warning",
	    		JOptionPane.WARNING_MESSAGE);	
	}
	
	public static void showInformationDialog(
			String theMsg ){
				
		
		setLookAndFeel();

	    JOptionPane.showMessageDialog(
	    		null,  
	    		theMsg,
	    		"Information",
	    		JOptionPane.INFORMATION_MESSAGE);
	    	
	}
	
	public static IRPModelElement launchDialogToSelectElement(
			List<IRPModelElement> inList, 
			String messageToDisplay, 
			Boolean isFullPathRequested ){
		
		IRPModelElement theEl = null;
		
		List<String> nameList = new ArrayList<String>();
		
		for (int i = 0; i < inList.size(); i++) {
			if (isFullPathRequested){
				nameList.add(i, inList.get(i).getFullPathName());
			} else {
				nameList.add(i, inList.get(i).getName());
			}
		} 	
		
		Object[] options = nameList.toArray();
		
		setLookAndFeel();
		
		String selectedElementName = (String) JOptionPane.showInputDialog(
				null,
				messageToDisplay,
				"Input",
				JOptionPane.QUESTION_MESSAGE,
				null,
				options,
				options[0]);
		
		if (selectedElementName != null){
			int index = nameList.indexOf(selectedElementName);
			theEl = inList.get(index);
		}
		
		return theEl;
	}
}