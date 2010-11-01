package de.helwich.sudoku.gwt.client;

import java.util.LinkedList;
import java.util.List;

import com.google.gwt.core.client.EntryPoint;
import com.google.gwt.core.client.GWT;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.Grid;
import com.google.gwt.user.client.ui.RootPanel;
import com.google.gwt.user.client.ui.VerticalPanel;

import de.helwich.sudoku.solve.BMatrixChangeHandler;
import de.helwich.sudoku.solve.XorMatrix;
import de.helwich.sudoku.solve.XorMatrixFactory;



/**
 * Entry point classes define <code>onModuleLoad()</code>.
 */
public class Application
    implements EntryPoint, BMatrixChangeHandler
{

	private XorMatrix matrix;
	private Button[] buttons;
	private Button undoB;
	private Button undoA;
	private List<Integer> undoStore = new LinkedList<Integer>();
	
	/**
	 * This is the entry point method.
	 */
	public void onModuleLoad() {
		
		Grid grid = new Grid(9,9);
		
		buttons = new Button[81];
		for (int i = 0; i < 81; i++) {
			buttons[i] = new Button(""+i);
			final int j = i;
			buttons[i].addClickHandler(new ClickHandler() {
				@Override
				public void onClick(ClickEvent event) {
					updateUndo();
					matrix.removeRow(j);
				}
			});
			grid.setWidget(i/9, i%9, buttons[i]);
		}
		
		XorMatrixFactory fac = new XorMatrixFactory();
		for (int i = 0; i <= 72; i+=9)
			fac.addXorColumn(0+i,1+i,2+i,3+i,4+i,5+i,6+i,7+i,8+i);
		for (int i = 0; i < 9; i++)
			fac.addXorColumn(0+i,9+i,18+i,27+i,36+i,45+i,54+i,63+i,72+i);
		for (int i = 0; i <= 6; i+=3)
			for (int j = 0; j <= 54; j+=27)
				fac.addXorColumn(0+i+j,1+i+j,2+i+j,9+i+j,10+i+j,11+i+j,18+i+j,19+i+j,20+i+j);
		matrix = fac.createXorMatrix();
		matrix.addChangeHandler(this);

		undoB = new Button("Undo");
		undoB.addClickHandler(new ClickHandler() {
			@Override
			public void onClick(ClickEvent event) {
				int count = undoStore.remove(undoStore.size()-1);
				matrix.undoRemove(count);
				undoB.setEnabled(!undoStore.isEmpty());
				undoA.setEnabled(undoB.isEnabled());
			}
		});
		undoB.setEnabled(false);
		
		undoA = new Button("Undo All");
		undoA.addClickHandler(new ClickHandler() {
			@Override
			public void onClick(ClickEvent event) {
				undoStore.clear();
				matrix.undoRemove(0);
				undoB.setEnabled(false);
				undoA.setEnabled(undoB.isEnabled());
			}
		});
		undoA.setEnabled(false);
		
		VerticalPanel vp = new VerticalPanel();
		vp.add(grid);
		vp.add(undoB);
		vp.add(undoA);
		
		RootPanel.get().add(vp);
	}

	private void updateUndo() {
		int count = matrix.getRemovedNodesCount();
		undoStore.add(count);
		undoB.setEnabled(true);
		undoA.setEnabled(true);
	}
	
	@Override
	public void onInsertRow(int row) {
		buttons[row].setVisible(true);
	}

	@Override
	public void onRemoveRow(int row) {
		buttons[row].setVisible(false);
	}
}
