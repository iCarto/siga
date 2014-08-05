package es.icarto.gvsig.extgia.consultas;

/*
 * Based on:
 * http://www.java2s.com/Code/Java/Swing-JFC/DualJListwithbuttonsinbetween.htm
 */

import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.SortedSet;
import java.util.TreeSet;

import javax.swing.AbstractListModel;
import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.ListModel;
import javax.swing.event.ListDataListener;

@SuppressWarnings("serial")
public class DualListBox<E> extends JPanel {

    private static final Insets EMPTY_INSETS = new Insets(0, 0, 0, 0);
    public static final String prototypeCellValue = "XXXXXXXXXXXXXXX";
    private static final String ADD_BUTTON_LABEL = " >";
    private static final String REMOVE_BUTTON_LABEL = "< ";
    private static final String ADD_ALL_BUTTON_LABEL = ">>";
    private static final String REMOVE_ALL_BUTTON_LABEL = "<<";

    private static final String DEFAULT_SOURCE_CHOICE_LABEL = "Campos disponibles";
    private static final String DEFAULT_DEST_CHOICE_LABEL = "Campos escogidos";

    private JLabel sourceLabel;
    private JList sourceList;
    private SortedListModel<E> sourceListModel;

    private JList destList;
    private SortedListModel<E> destListModel;
    private JLabel destLabel;

    private JButton addButton;
    private JButton removeButton;
    private JButton addAllButton;
    private JButton removeAllButton;

    public DualListBox() {
	initScreen();
    }

    private void clearSourceListModel() {
	sourceListModel.clear();
    }

    public void addSourceElements(ListModel newValue) {
	fillListModel(sourceListModel, newValue);
    }

    private void fillListModel(SortedListModel<E> model, ListModel newValues) {
	int size = newValues.getSize();
	for (int i = 0; i < size; i++) {
	    model.add((E) newValues.getElementAt(i));
	}
    }

    public void addSourceElements(E newValue[]) {
	fillListModel(sourceListModel, newValue);
    }

    private void addDestinationElements(E newValue[]) {
	fillListModel(destListModel, newValue);
    }

    private void fillListModel(SortedListModel<E> model, E newValues[]) {
	model.addAll(newValues);
    }

    private void clearSourceSelected() {
	E selected[] = (E[]) sourceList.getSelectedValues();
	for (int i = selected.length - 1; i >= 0; --i) {
	    sourceListModel.removeElement(selected[i]);
	}
	sourceList.getSelectionModel().clearSelection();
    }

    private void clearDestinationSelected() {
	E selected[] = (E[]) destList.getSelectedValues();
	for (int i = selected.length - 1; i >= 0; --i) {
	    destListModel.removeElement(selected[i]);
	}
	destList.getSelectionModel().clearSelection();
    }

    private void initScreen() {
	setBorder(BorderFactory.createEtchedBorder());
	setLayout(new GridBagLayout());
	sourceLabel = new JLabel(DEFAULT_SOURCE_CHOICE_LABEL);
	sourceListModel = new SortedListModel<E>();
	sourceList = new JList(sourceListModel);
	sourceList.setPrototypeCellValue(prototypeCellValue);
	add(sourceLabel, new GridBagConstraints(0, 0, 1, 1, 0, 0,
		GridBagConstraints.CENTER, GridBagConstraints.NONE,
		EMPTY_INSETS, 0, 0));
	add(new JScrollPane(sourceList, JScrollPane.VERTICAL_SCROLLBAR_ALWAYS,
		JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED),
		new GridBagConstraints(0, 1, 1, 5, .25, 1,
			GridBagConstraints.CENTER, GridBagConstraints.BOTH,
			EMPTY_INSETS, 0, 0));

	addButton = new JButton(ADD_BUTTON_LABEL);
	add(addButton, new GridBagConstraints(1, 1, 1, 1, 0, .25,
		GridBagConstraints.CENTER, GridBagConstraints.HORIZONTAL,
		EMPTY_INSETS, 0, 0));
	addButton.addActionListener(new AddListener());

	addAllButton = new JButton(ADD_ALL_BUTTON_LABEL);
	add(addAllButton, new GridBagConstraints(1, 2, 1, 1, 0, .25,
		GridBagConstraints.CENTER, GridBagConstraints.HORIZONTAL,
		EMPTY_INSETS, 0, 0));
	addAllButton.addActionListener(new AddAllListener());

	removeButton = new JButton(REMOVE_BUTTON_LABEL);
	add(removeButton, new GridBagConstraints(1, 3, 1, 1, 0, .25,
		GridBagConstraints.CENTER, GridBagConstraints.HORIZONTAL,
		EMPTY_INSETS, 0, 0));
	removeButton.addActionListener(new RemoveListener());

	removeAllButton = new JButton(REMOVE_ALL_BUTTON_LABEL);
	add(removeAllButton, new GridBagConstraints(1, 4, 1, 1, 0, .25,
		GridBagConstraints.CENTER, GridBagConstraints.HORIZONTAL,
		EMPTY_INSETS, 0, 0));
	removeAllButton.addActionListener(new RemoveAllListener());

	destLabel = new JLabel(DEFAULT_DEST_CHOICE_LABEL);
	destListModel = new SortedListModel<E>();
	destList = new JList(destListModel);
	destList.setPrototypeCellValue(prototypeCellValue);
	add(destLabel, new GridBagConstraints(2, 0, 1, 1, 0, 0,
		GridBagConstraints.CENTER, GridBagConstraints.NONE,
		EMPTY_INSETS, 0, 0));
	add(new JScrollPane(destList, JScrollPane.VERTICAL_SCROLLBAR_ALWAYS,
		JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED),
		new GridBagConstraints(2, 1, 1, 5, .25, 1,
			GridBagConstraints.CENTER, GridBagConstraints.BOTH,
			EMPTY_INSETS, 0, 0));
    }

    public void addDestListDataListener(ListDataListener l) {
	destListModel.addListDataListener(l);
    }

    public List<E> getDestList() {
	List<E> list = new ArrayList<E>(destListModel.getSize());
	Iterator<E> iterator = destListModel.iterator();
	while (iterator.hasNext()) {
	    list.add(iterator.next());
	}
	return list;
    }

    private class AddListener implements ActionListener {
	@Override
	public void actionPerformed(ActionEvent e) {
	    E selected[] = (E[]) sourceList.getSelectedValues();
	    addDestinationElements(selected);
	    clearSourceSelected();
	}
    }

    private class AddAllListener implements ActionListener {
	@Override
	public void actionPerformed(ActionEvent e) {
	    int start = 0;
	    int end = sourceList.getModel().getSize() - 1;
	    if (end >= 0) {
		sourceList.setSelectionInterval(start, end);
		E selected[] = (E[]) sourceList.getSelectedValues();
		addDestinationElements(selected);
		clearSourceSelected();
	    }
	}
    }

    private class RemoveAllListener implements ActionListener {
	@Override
	public void actionPerformed(ActionEvent e) {
	    int start = 0;
	    int end = destList.getModel().getSize() - 1;
	    if (end >= 0) {
		destList.setSelectionInterval(start, end);
		E selected[] = (E[]) destList.getSelectedValues();
		addSourceElements(selected);
		clearDestinationSelected();
	    }
	}
    }

    private class RemoveListener implements ActionListener {
	@Override
	public void actionPerformed(ActionEvent e) {
	    E selected[] = (E[]) destList.getSelectedValues();
	    addSourceElements(selected);
	    clearDestinationSelected();
	}
    }
}

@SuppressWarnings("serial")
final class SortedListModel<E> extends AbstractListModel {

    SortedSet<E> model;

    public SortedListModel() {
	model = new TreeSet<E>();
    }

    @Override
    public int getSize() {
	return model.size();
    }

    @Override
    public Object getElementAt(int index) {
	return model.toArray()[index];
    }

    public void add(E element) {
	if (model.add(element)) {
	    fireContentsChanged(this, 0, getSize());
	}
    }

    public void addAll(E elements[]) {
	Collection<E> c = Arrays.asList(elements);
	model.addAll(c);
	fireContentsChanged(this, 0, getSize());
    }

    public void clear() {
	model.clear();
	fireContentsChanged(this, 0, getSize());
    }

    public boolean contains(E element) {
	return model.contains(element);
    }

    public E firstElement() {
	return model.first();
    }

    public Iterator<E> iterator() {
	return model.iterator();
    }

    public E lastElement() {
	return model.last();
    }

    public boolean removeElement(E element) {
	boolean removed = model.remove(element);
	if (removed) {
	    fireContentsChanged(this, 0, getSize());
	}
	return removed;
    }
}
