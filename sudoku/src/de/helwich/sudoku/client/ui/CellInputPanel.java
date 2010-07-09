package de.helwich.sudoku.client.ui;

import com.google.gwt.event.dom.client.BlurEvent;
import com.google.gwt.event.dom.client.BlurHandler;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.event.dom.client.KeyPressEvent;
import com.google.gwt.event.dom.client.KeyPressHandler;
import com.google.gwt.user.client.ui.AbsolutePanel;
import com.google.gwt.user.client.ui.FocusPanel;
import com.google.gwt.user.client.ui.Widget;

/**
 * Can be used as a {@link ClickHandler} for a sudoku cell.
 * If the cell is clicked a {@link FocusPanel} is put around it to be able to
 * read keyboard inputs and pipe them to the cell.
 * This will make the content of the cell editable.
 * 
 * @author Hendrik Helwich
 */
class CellInputPanel extends FocusPanel implements BlurHandler, KeyPressHandler, ClickHandler {
	
	private AbsolutePanel mainPanel;
	private Widget widget;
	
	public CellInputPanel(AbsolutePanel mainPanel) {
		setStylePrimaryName("sudonski-cellinput");
		this.mainPanel = mainPanel;
		addBlurHandler(this);
		addKeyPressHandler(this);
	}
	
	private void addWidgetWithCorrectPosition(Widget widget, int left, int top) {
		mainPanel.add(widget, left, top);
		// correct position (position changes after adding widget)
		//TODO why do i need to do this ? find nice solution
		left += left-mainPanel.getWidgetLeft(widget);
		top += top-mainPanel.getWidgetTop(widget);
		mainPanel.setWidgetPosition(widget, left, top);
	}

	@Override
	public void onBlur(BlurEvent event) {
		if (widget != null) {
			int left = mainPanel.getWidgetLeft(this);
			int top = mainPanel.getWidgetTop(this);
			mainPanel.remove(this);
			addWidgetWithCorrectPosition(widget, left, top);
			widget = null;
		}
	}

	@Override
	public void onKeyPress(KeyPressEvent event) {
		//TODO pipe input to cell
	}

	@Override
	public void onClick(ClickEvent event) {
		if (this.widget == null) {
			this.widget = (Widget) event.getSource();
			int left = mainPanel.getWidgetLeft(widget);
			int top = mainPanel.getWidgetTop(widget);
			mainPanel.remove(widget);
			add(widget);
			addWidgetWithCorrectPosition(this, left, top);
			setFocus(true);
		}
	}

}
