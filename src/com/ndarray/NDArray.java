package com.ndarray;

import com.guard.Guard;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

/**
 * N-Dimensional Array representation class.
 * <p>
 * See README.MD for more info.
 *
 * @author Aren
 */
public class NDArray<T> {
    /*
    PRIVATE MEMBERS-----------------------------------------------------------------
     */


    /**
     * Node representation of this.
     */
    private class Node {
        // the row number
        int row;
        // data to be placed in node - null if "smart" node
        int index;
        T data;
        // Node to the right - "smart" end node if at the end
        Node next;
        // Node to the left - "Smart" front node if at the front
        Node previous;
        // Node to represent the "row" above a "row" node - null if this node is the "top end" node (Top previous)
        Node tPrevious;
        // Node to represent the "row" below a "row" node - null if this is the "bottom end"
        Node bNext;
    }

    /**
     * Front "smart" node - Every node "connected" to this node.
     */
    private Node front;
    /**
     * The number of rows in the NDArray.
     */
    private int rowCount;
    /**
     * The total size of the NDArray (row count + length of individual rows).
     */
    private int size;
    /**
     * The length of each row.
     */
    private int rowLength;
    /**
     * Guard member, used to guard against null errors / out of bounds errors.
     */
    private final Guard GUARD = new Guard();

    /**
     * Creates the entries for this
     *
     * @param n the node to add entries to
     */
    private void createEntries(Node n) {
        // need n to not change, so make a reference
        Node t = n;
        // add entries to the new node
        for (int i = 0; i < this.rowLength; i++) {
            // new node
            Node r = new Node();
            // index range (0, this.rowLength - 1)
            r.index = i;
            // update current nodes next to the new node
            t.next = r;
            // update new node's previous to the current node
            r.previous = t;
            // update current node to the new node
            t = r;
        }
        t.next = null;
    }

    /**
     * Creates the rows for this while also creating the entries
     */
    private void createRows() {
        // reference the front node
        Node n = this.front;
        // create null entries for row 1
        createEntries(this.front);
        // create any new entries if this.rowCount > 1
        for (int i = 1; i < this.rowCount; i++) {
            // node used to create the next row "front" node
            Node p = new Node();
            // update the row number (i + 1 because front is already 1)
            p.row = i + 1;
            // update the index to be -1
            p.index = -1;
            // update current node's next to the newly created node
            n.bNext = p;
            // update the new node's previous to current node
            p.tPrevious = n;
            // move current node to the newly created node
            n = p;
            // now that row has been made, create its entries
            this.createEntries(n);
        }

    }

    /**
     * Helper method that can get the row node "front" to be returned to the method that called.
     *
     * @param row the "front" of the row that needs to be returned
     * @return returns the specific row designated by {@code row}
     */
    private Node getRow(int row) {
        Node n = this.front;
        /*
         * if row > 1 then loop over each row node until the desired row node is found. Preconditions
         * should be checked prior to running this method so n cannot be null.
         */
        while (n.row != row) {
            n = n.bNext;
        }

        return n;
    }

    private void sortRowPrivate(Comparator<T> order, Node row) {
        // list used for sorting purposes
        ArrayList<T> arr = new ArrayList<>();
        // ref to node that move along the row
        Node p = row.next;
        // add all the data to the array
        while (p != null) {
            arr.add(p.data);
            p = p.next;
        }
        // sort the array
        arr.sort(order);
        // refer back to index 0
        p = row.next;
        // replace the current data with the ordered data
        while (p != null) {
            p.data = arr.remove(0);
            p = p.next;
        }

    }


    /*
     * Public methods. ---------------------------------------------------------
     */

    /**
     * Initial construction of empty NDArray
     */
    private void createNewRep(int r, int l) {
        //initial setup of nodes. Note: tPrevious (top) and bNext (bottom) null
        this.front = new Node(); // "smart" front node
        this.front.row = 1; // "smart" node contains the row number
        this.front.index = -1; // index of this row is -1
        this.rowCount = r; // amount of rows
        this.rowLength = l; // length of each row
        this.size = r * l; // the size is total rows * the length of (each) row
        // create the rows
        createRows();
    }

    /**
     * Default constructor.
     *
     * @param rowCount  the number of rows to be added to this
     * @param rowLength the length of each row
     * @throws IllegalArgumentException if numOfRows or rowLength is outside the allowable range
     * @throws IllegalArgumentException if row && rowLength <= 0 && row && rowLength > 46340
     * @requires 0 < rowCount <= 46340 / 2 && 0 < rowLength <= 46340
     */
    public NDArray(int rowCount, int rowLength) throws IllegalArgumentException {
        // Not sure why anyone would want to do 46340 x 46340, but you never know
        GUARD.againstIndexOutOfBounds(1, 46340, rowCount);
        GUARD.againstIndexOutOfBounds(1, 46340, rowLength);
        this.createNewRep(rowCount, rowLength);
    }

    /**
     * Adds an empty row with no values to the end of this
     */
    public void addEmptyRow() {
        // ref front node
        Node n = this.front;
        // move the bottom node
        while (n.bNext != null) {
            n = n.bNext;
        }
        // new node that will take place of the "front" node for the newest node
        Node p = new Node();
        // connect the current node front to the new end row node
        n.bNext = p;
        // connect the new end row node to the previous row node
        p.tPrevious = n;
        // update row number
        p.row = n.row + 1;
        // create empty entries
        this.createEntries(p);
    }

    /**
     * Adds a new row to the end of this and updates each entry to those of {@code args}
     *
     * @param args the entries to add to this
     * @throws NullPointerException      if args == null
     * @throws IndexOutOfBoundsException if args.size < 0 && args.size >= this.rowLength
     * @updates this
     * @clears args
     * @requires args != null
     * @requires 0 < args.size < this.rowLength
     */
    public void addNonEmptyRow(ArrayList<T> args) throws NullPointerException, IndexOutOfBoundsException {
        GUARD.againstNull(args);
        GUARD.againstIndexOutOfBounds(0, this.rowLength - 1, args.size());
        // ref front node
        Node n = this.front;
        // move the bottom node
        while (n.bNext != null) {
            n = n.bNext;
        }
        // new node that will take place of the "front" node for the newest node
        Node p = new Node();
        // connect the current node front to the new end row node
        n.bNext = p;
        // connect the new end row node to the previous row node
        p.tPrevious = n;
        // update row number
        p.row = n.row + 1;
        for (int i = 0; i < this.rowLength; i++) {
            // new node
            Node r = new Node();
            // update index
            r.index = i;
            // update the data
            r.data = args.remove(0);
            // attach current node to new node
            p.next = r;
            // attach new node to current node
            r.previous = p;
            // change current node to new node
            p = r;
        }
    }


    /**
     * Returns the entry located in {@code row} index {@code i}. None primitive values will be a reference value.
     *
     * @param row the row to return the entry from
     * @param i   the index of the entry
     * @return the desired entry
     * @throws IndexOutOfBoundsException if row <= 0 && row > this.rowCount && i < 0 && i >= this.rowLength
     * @requires 0 < row <= this.rowCount
     * @requires 0 <= i < this.rowLength
     */
    public T getEntry(int row, int i) throws IndexOutOfBoundsException {
        GUARD.againstIndexOutOfBounds(1, this.rowCount, row);
        GUARD.againstIndexOutOfBounds(0, this.rowLength - 1, i);
        Node n = this.getRow(row);
        n = n.next;
        while (n.index != i) {
            n = n.next;
        }
        return n.data;
    }

    /**
     * Returns the index of the first occurrence of {@code x} in {@code row}
     *
     * @param row the row to check
     * @param x   the entry to find
     * @return true if this contains {@code x}, else false
     * @throws IndexOutOfBoundsException if row <= 0 && row >this.rowCount
     * @throws NullPointerException      if x == null
     * @requires 0 < row <= this.rowCount
     * @requires x != null
     */
    public boolean rowContains(int row, T x) throws IndexOutOfBoundsException, NullPointerException {
        GUARD.againstNull(x);
        GUARD.againstIndexOutOfBounds(1, this.rowCount, row);
        // get the row
        Node p = this.getRow(row);
        // update reference to the node at index 0
        p = p.next;
        while (p != null) {
            if (p.data.equals(x)) {
                // return the index of the entry
                return true;
            }
            // move to next
            p = p.next;
        }
        // if value is not in this then returns -1
        return false;
    }

    /**
     * Returns the index of the first occurrence of {@code x} in {@code row}
     *
     * @param row the row to check
     * @param x   the entry to find
     * @return the index of the entry, else -1
     * @throws NullPointerException      if x == null
     * @throws IndexOutOfBoundsException if row <= 0 && row >this.rowCount
     * @requires 0 < row <= this.rowCount
     * @requires 0 < row <= this.rowCount
     * @requires x != null
     */
    public int rowIndexOf(int row, T x) throws IndexOutOfBoundsException {
        GUARD.againstNull(x);
        GUARD.againstIndexOutOfBounds(1, this.rowCount, row);
        // get the row
        Node p = this.getRow(row);
        // update reference to the node at index 0
        p = p.next;
        while (p != null) {
            if (p.data.equals(x)) {
                // return the index of the entry
                return p.index;
            }
            // move to next
            p = p.next;
        }
        // if value is not in this then returns -1
        return -1;
    }

    /**
     * Returns an ArrayList of the entries located at {@code row}
     *
     * @param row the row with the entries to be returned
     * @return result = row.entries
     * @throws IndexOutOfBoundsException if row <= 0 && row > this.rowCount
     * @requires 0 < row <= this.rowCount
     */
    public ArrayList<T> getRowEntries(int row) throws IndexOutOfBoundsException {
        GUARD.againstIndexOutOfBounds(1, this.rowCount, row);
        Node n = this.getRow(row);
        n = n.next;
        ArrayList<T> result = new ArrayList<>();
        while (n != null) {
            result.add(n.data);
            n = n.next;
        }
        return result;
    }

    /**
     * Removes the row designated by {@code row}
     *
     * @param row the row to be removed
     * @throws IndexOutOfBoundsException if 0 < row <= this.rowCount
     * @requires 0 < row <= this.rowCount
     */
    public void removeRow(int row) throws IndexOutOfBoundsException {
        GUARD.againstIndexOutOfBounds(1, this.rowCount, row);
        if (row == 1 && this.rowCount == 1) {
            // reset this to initial state
            this.createNewRep(this.rowCount, this.rowLength);
        } else if (row == 1) {
            // update the first row to the next row
            this.front = this.front.next;
        } else {
            Node n = this.getRow(row);
            Node p = n.previous;
            Node r = n.next;
            p.next = r;
            r.previous = p;
        }
        this.rowCount--;
        this.size = this.rowCount * this.rowLength;
    }

    /**
     * Add a single entry to the desired row in the
     *
     * @param row the desired row to add the entry
     * @param i   the specific index of the row to add the entry
     * @throws IndexOutOfBoundsException if row <= 0 && row > this.rorCount && i < 0 && i >= this.rowLength
     * @throws NullPointerException      if x is null
     * @updates this
     * @requires 0 < row <= this.rowCount && 0 <= i < this.rowLength
     * @requires x is not null
     */
    public void replaceEntry(int row, int i, T x) {
        GUARD.againstIndexOutOfBounds(1, this.rowCount, rowCount);
        GUARD.againstIndexOutOfBounds(0, this.rowLength - 1, i);
        GUARD.againstNull(x);
        // call method to get the front node row;
        Node n = this.getRow(row);
        // Move to index 0
        n = n.next;
        // if index > 0 then enters loop
        while (n.index != i) {
            n = n.next;
        }
        // changes the data to x
        n.data = x;
    }


    /**
     * Replaces the entries of the given row with the entries in {@code args}
     *
     * @param args the entries to add to the row
     * @throws NullPointerException      if args = null
     * @throws IndexOutOfBoundsException if row <= 0 && row > this.rowsLength
     * @requires 0 < |args| < this.rowsLength
     * @requires args != null
     * @requires 0 < row <= this.rowsLength
     * @updates this
     */
    public void replaceEntries(int row, List<T> args) throws NullPointerException, IndexOutOfBoundsException {
        GUARD.againstNull(args);
        GUARD.againstIndexOutOfBounds(1, row, this.rowCount);
        GUARD.againstIndexOutOfBounds(0, this.rowLength - 1, args.size());
        // get the row node to add entries to
        Node n = this.getRow(row);
        // move n to the 'first' entry location
        n = n.next;
        // n should not be null, but checking anyway, add args to data
        for (int i = 0; n != null && i < args.size(); i++) {
            n.data = args.get(i);
            n = n.next;
        }
    }

    /**
     * Returns the row count
     *
     * @return the row count
     */
    public int rowCount() {
        return this.rowCount;
    }

    /**
     * Returns the length of the row(s)
     *
     * @return the length of the row(s)
     */
    public int rowLength() {
        return this.rowLength;
    }

    /**
     * Returns the total size of this
     *
     * @return the total size of this
     */
    public int size() {
        return this.size;
    }

    /**
     * Sorts the specified row given {@code rode} in the specified order given by {@code order}
     *
     * @param order the order which the row will be sorted
     * @param row   the row to be sorted
     * @throws NullPointerException      if order = null
     * @throws IndexOutOfBoundsException if row <= 0 && row >= this.rowCount
     * @requires order != null
     * @requires 0 < row <= this.rowCount
     */
    public void sortRow(Comparator<T> order, int row) throws NullPointerException, IndexOutOfBoundsException {
        GUARD.againstNull(order);
        GUARD.againstIndexOutOfBounds(1, this.rowCount, row);
        Node n = this.getRow(row);
        this.sortRowPrivate(order, n);
    }

    /**
     * Sorts the specified row given {@code rode} in the specified order given by {@code order}
     *
     * @param order the order which the row will be sorted
     * @throws NullPointerException if order = null
     * @requires order != null
     */
    public void sortAll(Comparator<T> order) throws NullPointerException {
        GUARD.againstNull(order);
        Node n = this.front;
        while (n != null) {
            this.sortRowPrivate(order, n);
            n = n.bNext;
        }
    }

    /**
     * String representation of this
     *
     * @return the string representation of this.
     * @ensures toString = [ [rows[x] = [entry 1, entry 2, ...., entry n] * \n ]
     */
    @Override
    public String toString() {
        // building a string with while loops, so using a string builder
        StringBuilder sb = new StringBuilder();
        // append the [ to signify where the NDArray starts
        sb.append("[ ");
        // node to move along entries
        Node nextEntry = this.front.next;
        // node to move along the rows
        Node nextRow = this.front;
        // Loop over each row's entries and output them to console - if this is a reference value then uses their toString
        while (nextRow != null) {
            // append [ to signify the start of a row
            sb.append("[");
            // loop over the entries
            while (nextEntry != null) {
                // append each entry to the string builder
                sb.append(nextEntry.data).append(", ");
                // move nextEntry to the next value
                nextEntry = nextEntry.next;
            }
            // replace the value at the ", " with ]
            sb.replace(sb.length() - 2, sb.length(), "]");
            // update nextRow
            nextRow = nextRow.bNext;
            // need to make sure next row is not empty before we reference the next entry of that row
            if (nextRow != null) {
                // update node to next entry
                nextEntry = nextRow.next;
                // append a new line and spaces for formatting
                sb.append("\n  ");
            }
        }
        // append closing ] to sb
        sb.append(" ]");
        return sb.toString();
    }
}
