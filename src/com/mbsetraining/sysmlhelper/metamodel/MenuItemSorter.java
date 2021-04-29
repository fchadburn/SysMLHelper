package com.mbsetraining.sysmlhelper.metamodel;

import java.util.Arrays;
import java.util.Comparator;
import java.util.List;

public class MenuItemSorter implements Comparator<MenuItem> {
 
    private List<Comparator<MenuItem>> listComparators;
 
    public MenuItemSorter(
    		@SuppressWarnings("unchecked") Comparator<MenuItem>... comparators ){
    	
        this.listComparators = Arrays.asList( comparators );
    }
 
    public int compare(
    		MenuItem left, 
    		MenuItem right ){
    	
        for( Comparator<MenuItem> comparator : listComparators ){
        	
            int result = comparator.compare( left, right );
            if( result != 0 ){
                return result;
            }
        }
        return 0;
    }
}