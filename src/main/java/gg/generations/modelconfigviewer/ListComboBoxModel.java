//package gg.generations.modelconfigviewer;
//
//import javax.swing.*;
//import java.io.Serializable;
//import java.util.ArrayList;
//import java.util.Collection;
//import java.util.List;
//
//public class ListComboBoxModel<E> extends AbstractListModel<E> implements MutableComboBoxModel<E>, Serializable {
//    List<E> objects;
//    E selectedObject;
//
//    /**
//     * Constructs an empty DefaultComboBoxModel object.
//     */
//    public ListComboBoxModel() {
//        objects = new ArrayList<>();
//    }
//
//    /**
//     * Constructs a DefaultComboBoxModel object initialized with
//     * an array of objects.
//     *
//     * @param items  an array of Object objects
//     */
//    public ListComboBoxModel(List<E> list) {
//        objects = list;;
//
//        if (getSize() > 0 ) {
//            selectedObject = getElementAt( 0 );
//        }
//    }
//
//    // implements javax.swing.ComboBoxModel
//    /**
//     * Set the value of the selected item. The selected item may be null.
//     *
//     * @param anObject The combo box value or null for no selection.
//     */
//    public void setSelectedItem(Object anObject) {
//        if ((selectedObject != null && !selectedObject.equals( anObject )) ||
//            selectedObject == null && anObject != null) {
//            selectedObject = (E) anObject;
//            fireContentsChanged(this, -1, -1);
//        }
//    }
//
//    // implements javax.swing.ComboBoxModel
//    public Object getSelectedItem() {
//        return selectedObject;
//    }
//
//    // implements javax.swing.ListModel
//    public int getSize() {
//        return objects.size();
//    }
//
//    // implements javax.swing.ListModel
//    public E getElementAt(int index) {
//        if ( index >= 0 && index < objects.size() )
//            return objects.get(index);
//        else
//            return null;
//    }
//
//    /**
//     * Returns the index-position of the specified object in the list.
//     *
//     * @param anObject the object to return the index of
//     * @return an int representing the index position, where 0 is
//     *         the first position
//     */
//    public int getIndexOf(Object anObject) {
//        return objects.indexOf(anObject);
//    }
//
//    // implements javax.swing.MutableComboBoxModel
//    public void addElement(E anObject) {
//        objects.add(anObject);
//        fireIntervalAdded(this,objects.size()-1, objects.size()-1);
//        if ( objects.size() == 1 && selectedObject == null && anObject != null ) {
//            setSelectedItem( anObject );
//        }
//    }
//
//    // implements javax.swing.MutableComboBoxModel
//    public void insertElementAt(E anObject,int index) {
//        objects.add(index, anObject);
//        fireIntervalAdded(this, index, index);
//    }
//
//    // implements javax.swing.MutableComboBoxModel
//    public void removeElementAt(int index) {
//        if ( getElementAt( index ) == selectedObject ) {
//            if ( index == 0 ) {
//                setSelectedItem( getSize() == 1 ? null : getElementAt( index + 1 ) );
//            }
//            else {
//                setSelectedItem( getElementAt( index - 1 ) );
//            }
//        }
//
//        objects.remove(index);
//
//        fireIntervalRemoved(this, index, index);
//    }
//
//    // implements javax.swing.MutableComboBoxModel
//    public void removeElement(Object anObject) {
//        objects.remove(anObject);
//    }
//
//    /**
//     * Empties the list.
//     */
//    public void removeAllElements() {
//        if ( objects.size() > 0 ) {
//            int firstIndex = 0;
//            int lastIndex = objects.size() - 1;
//            objects.clear();
//            selectedObject = null;
//            fireIntervalRemoved(this, firstIndex, lastIndex);
//        } else {
//            selectedObject = null;
//        }
//    }
//
//    /**
//     * Adds all of the elements present in the collection.
//     *
//     * @param c the collection which contains the elements to add
//     * @throws NullPointerException if {@code c} is null
//     */
//    public void addAll(Collection<? extends E> c) {
//        if (c.isEmpty()) {
//            return;
//        }
//
//        int startIndex = getSize();
//
//        objects.addAll(c);
//        fireIntervalAdded(this, startIndex, getSize() - 1);
//    }
//
//    /**
//     * Adds all of the elements present in the collection, starting
//     * from the specified index.
//     *
//     * @param index index at which to insert the first element from the
//     * specified collection
//     * @param c the collection which contains the elements to add
//     * @throws ArrayIndexOutOfBoundsException if {@code index} does not
//     * fall within the range of number of elements currently held
//     * @throws NullPointerException if {@code c} is null
//     */
//    public void addAll(int index, Collection<? extends E> c) {
//        if (index < 0 || index > getSize()) {
//            throw new ArrayIndexOutOfBoundsException("index out of range: " +
//                                                                       index);
//        }
//
//        if (c.isEmpty()) {
//            return;
//        }
//
//        objects.addAll(index, c);
//        fireIntervalAdded(this, index, index + c.size() - 1);
//    }
//}
