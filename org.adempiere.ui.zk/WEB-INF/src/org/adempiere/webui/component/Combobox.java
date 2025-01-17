/******************************************************************************
 * Product: Posterita Ajax UI 												  *
 * Copyright (C) 2007 Posterita Ltd.  All Rights Reserved.                    *
 * This program is free software; you can redistribute it and/or modify it    *
 * under the terms version 2 of the GNU General Public License as published   *
 * by the Free Software Foundation. This program is distributed in the hope   *
 * that it will be useful, but WITHOUT ANY WARRANTY; without even the implied *
 * warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.           *
 * See the GNU General Public License for more details.                       *
 * You should have received a copy of the GNU General Public License along    *
 * with this program; if not, write to the Free Software Foundation, Inc.,    *
 * 59 Temple Place, Suite 330, Boston, MA 02111-1307 USA.                     *
 * For the text or an alternative of this public license, you may reach us    *
 * Posterita Ltd., 3, Draper Avenue, Quatre Bornes, Mauritius                 *
 * or via info@posterita.org or http://www.posterita.org/                     *
 *****************************************************************************/

package org.adempiere.webui.component;

import java.util.List;

import org.adempiere.webui.AdempiereIdGenerator;
import org.adempiere.webui.LayoutUtils;
import org.zkoss.zk.au.out.AuScript;
import org.zkoss.zk.ui.IdSpace;
import org.zkoss.zk.ui.Page;
import org.zkoss.zk.ui.WrongValueException;
import org.zkoss.zk.ui.event.Events;
import org.zkoss.zk.ui.util.Clients;
import org.zkoss.zul.Comboitem;

/**
 * Extend {@link org.zkoss.zul.Combobox}
 * @author  <a href="mailto:agramdass@gmail.com">Ashley G Ramdass</a>
 * @date    Feb 25, 2007
 * @version $Revision: 0.10 $
 */
public class Combobox extends org.zkoss.zul.Combobox implements IdSpace
{
	/**
	 * generated serial id
	 */
	private static final long serialVersionUID = -6278632602577424842L;
	
	/**
	 * Default constructor
	 */
    public Combobox() {
		super();
		override();
		init();
	}

    /**
     * @param value
     * @throws WrongValueException
     */
	public Combobox(String value) throws WrongValueException {
		super(value);
		override();
		init();
	}

	/**
	 * Setup initial state of component
	 */
	private void init() {
		//ctrl+down to open combobox dropdown
		this.setCtrlKeys("^#down");
		this.addEventListener(Events.ON_CTRL_KEY, e -> {
			if (this.isEnabled() && LayoutUtils.isReallyVisible(this)) {
				if (!isOpen())
					this.setOpen(true);
				e.stopPropagation();
			}
		});
	}

	/**
	 * override "shallIgnoreClick_"
	 */
	private void override() {
		// idempiere always want to show context ever on disable control
		this.setWidgetOverride("shallIgnoreClick_", "function(evt) {"
				+ " return false; "
				+ "}");
	}
	
	/**
	 * @param enabled
	 */
	public void setEnabled(boolean enabled)
    {
		this.setDisabled(!enabled);
        if (!enabled) {
        	//ensure list is close and not on focus
        	if (this.getPage() != null) 
        	{
        		String script = "(function(){let w=zk('#"+getUuid()+"').$(); if (w){w.close();let j=jq('#" 
        				+getUuid()+" :focus');if(j.get(0)){j.blur();}}"
        				+"})()";
        		Clients.response(new AuScript(script));
        	}
        }
    }
    
	/**
	 * Add new combo item
	 * @param label
	 * @return added Comboitem 
	 */
    public Comboitem appendItem(String label) 
    {
        ComboItem item = new ComboItem(label);
        String id = AdempiereIdGenerator.escapeId(label);
        if (getFellowIfAny(id) == null )
        	item.setId(id);
        item.setParent(this);
        return item;
    }

    /**
     * @return true if enable, false otherwise
     */
	public boolean isEnabled() {
		return !isDisabled();
	}
	
	/**
	 * remove all items, to ease porting of swing form
	 */
	public void removeAllItems() {
		int cnt = getItemCount();
		for (int i = cnt - 1; i >=0; i--) {
			removeItemAt(i);
		}
	}

	/**
	 * add new combo item
	 * @param name
	 * @param value
	 */
	public void appendItem(String name, Object value) {
		ComboItem item = new ComboItem(name, value);
		String id = AdempiereIdGenerator.escapeId(name);
		if (getFellowIfAny(id) == null)
			item.setId(id);
		this.appendChild(item);
	}
	
	 /** 
     * Set selected item for the combo box based on the value of combo item.<br/>
     * Set selected to none if no item found matching the value given or 
     * value is null.
     * @param value Value of ComboItem to set as selected
     */
    public void setValue(Object value)
    {
        setSelectedItem(null);
        
        if (value == null)
        {
            return ;
        }
        
        List<Comboitem> items = getItems();
        for (Comboitem item : items)
        {
        	if (value.getClass() != item.getValue().getClass()) {
        		// if the classes of value and item are different convert both to String
        		String stringValue = value.toString();
        		String stringItem = item.getValue().toString();
                if (stringValue.equals(stringItem))
                {
                    setSelectedItem(item);
                    break;
                }
        	} else {
                if (value.equals(item.getValue()))
                {
                    setSelectedItem(item);
                    break;
                }
        	}
        }
    }
    
    /**
     * @param value
     * @return true if current selected combo item is with value equal to value parameter 
     */
    public boolean isSelected(Object value) 
    {
    	if (value == null)
    		return false;
    	
    	Comboitem item = getSelectedItem();
    	if (item == null)
    		return false;
    	
    	return item.getValue().equals(value);
    }
    
    //http://jira.idempiere.com/browse/IDEMPIERE-443
    //undocumented api hack to ensure onSelect always fire for mouse selection
    public void clearLastSel() {
    	String script = "zk('#"+getUuid()+"').$()._lastsel=null;";
    	AuScript response = new AuScript(script);
    	Clients.response(response);
    }
    
    /**
     * add widget listener to auto scroll selected item to view (i.e make visible)
     */
    public void addScrollSelectedIntoViewListener() {
    	String script = "(function(me){let id='#'+me.uuid+'-pp .z-comboitem-selected';let selected=zk($(id));if(selected.jq.length==1)selected.scrollIntoView();})(this)";
        setWidgetListener("onKeyUp", script);
    }

	@Override
	public void onPageDetached(Page page) {
		setWidgetListener("onKeyUp", null);
		super.onPageDetached(page);
	}        
}