/**
 * ==================================
 * Copyright (c) 2010 Max-Planck Institute for Informatics
 * Database and Information Systems Department
 * http://www.mpi-inf.mpg.de/departments/d5/index.html
 * 
 * ==================================
 * 
 * This source code is provided with AS IF - it does not guarantee the
 * or compatibilities with older or newer version of third-parties. In any
 * cases, if you have problems regarding using libraries delivered with
 * the project, feel free to write to the above email. Also, we would like
 * to get feedbacks from all of you
 * 
 * Contact: Tuan Tran - ttran@mpi-inf.mpg.de
 *
 */
package tuan.collections;
import java.util.Arrays;
import java.util.BitSet;
import java.util.NoSuchElementException;

/** 
This class is part of the Java Tools (see http://mpii.de/yago-naga/javatools).
It is licensed under the Creative Commons Attribution License 
(see http://creativecommons.org/licenses/by/3.0) by 
the YAGO-NAGA team (see http://mpii.de/yago-naga).

This class implements a space-efficient integer set. All operations are linear time, though...<BR>
 */
public class LongSet {
	/** Holds the integers*/
	protected long[] data;
	/** Tells whether the int is there*/
	protected BitSet isThere;
	/** Number of integers in data*/
	protected int numElements=0;
	/** Index that can be polled next. Handled exclusively by poll() */
	protected int pollIndex=0;
	/** Index pointing to an empty position. Handled exclusively by indexOf()*/
	protected int addIndex=0;
	/** Last index that contains a value*/
	protected int lastIndex=-1;

	/** Adds the element*/
	public boolean add(long v) {
		// Use contains() also to set the addIndex
		if(contains(v)) return(false);  
		if(numElements==data.length) data=Arrays.copyOf(data,data.length+100);
		data[addIndex]=v;
		isThere.set(addIndex,true);
		numElements++;
		if(addIndex>lastIndex) lastIndex=addIndex;
		return(true);
	} 

	/** Adds the element without checking its existence*/
	public boolean forceAdd(long v) {
		// Use setAddIndex() also to set the addIndex
		setAddIndex(); 
		if(numElements==data.length) data=Arrays.copyOf(data,data.length+100);
		data[addIndex]=v;
		isThere.set(addIndex,true);
		numElements++;
		if(addIndex>lastIndex) lastIndex=addIndex;
		return(true);
	} 

	public void clear(int capacity) {
		numElements=0;
		data=new long[capacity];
		isThere=new BitSet(capacity);
		lastIndex=-1;
	}

	public long poll() {
		if(numElements==0) throw new NoSuchElementException();
		shrink();
		while(true) {
			if(pollIndex>lastIndex) pollIndex=0;
			if(isThere.get(pollIndex))  return(removeIndex(pollIndex));
			pollIndex++;
		}
	}

	/** Finds the element, sets addIndex to a free position*/
	protected int indexOf(long v) {
		addIndex=lastIndex+1;
		for(int i=0;i<=lastIndex;i++) {
			if(!isThere.get(i)) addIndex=i;
			if(data[i]==v && isThere.get(i)) return(i);
		}
		return(-1);
	}

	/** Quickly set addIndex to a free position */
	protected void setAddIndex() {
		addIndex=lastIndex+1;
		for(int i=lastIndex;i>=0;i--) {
			if(!isThere.get(i)) {
				addIndex=i;
				break;
			}
		}
	}

	/** Deletes empty space if necessary*/
	protected void shrink() {
		if(numElements<data.length/2 && data.length>300) {    
			LongSet result=new LongSet(numElements+100);
			result.addUnique(this);
			this.data=result.data;
			this.isThere=result.isThere;
			this.lastIndex=result.lastIndex;
			this.numElements=result.numElements;
		}
	}

	/** Force to delete empty space*/
	public void trim() {
		if(numElements<data.length) {
			LongSet result=new LongSet(numElements);
			result.addUnique(this);
			this.data=result.data;
			this.isThere=result.isThere;
			this.lastIndex=result.lastIndex;
			this.numElements=result.numElements;
		}
	}

	/** Force to shrink the data to a specified length.
	 * Return true if the data is modified*/
	public boolean trimToSize(int minSize) {
		if(numElements<=minSize) {
			LongSet result=new LongSet(minSize);
			result.addUnique(this);
			this.data=result.data;
			this.isThere=result.isThere;
			this.lastIndex=result.lastIndex;
			this.numElements=result.numElements;
			return true;
		} else return false;
	}

	/** Removes an item*/
	protected long removeIndex(int index) {
		isThere.set(index, false);
		numElements--; 
		return(data[index]);
	}

	// ----------- Wrapper methods -------------

	/** Creates a new IntSet from initial values.*/
	public static LongSet of(int... initial) {
		LongSet result=new LongSet();
		for(int i : initial) result.add(i);
		return(result);
	}

	/** Creates a new IntSet by copying the given set*/
	public LongSet(LongSet copy) {
		setTo(copy);
	}

	/** Creates a new IntSet by copying the given bit set*/
	public LongSet(BitSet copy) {
		clear(copy.cardinality());
		addAll(copy);
	}

	/** Creates a new IntSet*/
	public LongSet() {
		clear();
	}

	/** Creates a new IntSet with an initial capacity*/
	public LongSet(int capacity) {
		clear(capacity);
	}

	/** Overwrites the current IntSet with the given one*/
	public void setTo(LongSet copy) {
		if(copy==this) return;
		clear(copy.size());
		addUnique(copy);
	}

	/** Removes one element, returns TRUE if the set was modified*/
	public boolean remove(long i) {
		int pos=indexOf(i);
		if(pos==-1) return false;
		removeIndex(pos);
		shrink();
		return true;    
	} 

	/** Removes all elements in c*/
	public boolean removeAll(LongSet c) {
		boolean result=false;
		for(int i=0;i<=c.lastIndex;i++) {
			if(c.isThere.get(i)) result|=remove(c.data[i]);
		}
		return result;
	}

	/** Returns an array of the elements of this set
	 * NOTE: This will create a new array to protect underlying data*/
	public long[] toArray() {
		long[] result = new long[numElements];
		int j=0;
		for(int i=0;i<=lastIndex;i++) {
			if(isThere.get(i)) result[j++]=data[i]; 
		}
		return result;
	}

	@Override
	public String toString() {
		StringBuilder res=new StringBuilder("[");
		for(int i=0;i<=lastIndex;i++) {
			if(isThere.get(i)) res.append(data[i]).append(", ");
		}
		if(numElements>0) res.setLength(res.length()-2);    
		return res.append("]").toString();
	}    

	public int size() {
		return numElements;
	}

	/** Adds this element*/
	public boolean add(Number e) {
		return add(e.longValue());
	}

	/** Adds all elements*/
	public boolean addAll(long[] c) {
		boolean returnValue=false;
		for(long i : c) returnValue|=add(i);
		return(returnValue);
	}

	/** Adds all elements*/
	public boolean addAll(LongSet s) {
		boolean returnValue=false;
		for(int index=0;index<=s.lastIndex;index++) {
			if(s.isThere.get(index)) returnValue|=add(s.data[index]);
		}
		return(returnValue);
	}

	/** Adds all elements*/
	public boolean addAll(BitSet s) {
		boolean returnValue=false;
		for(int i=s.nextSetBit(0);i>=0;i=s.nextSetBit(i+1)) {      
			returnValue|=add(i);
		}
		return(returnValue);
	}

	/** Adds all elements, skip checking duplicates */
	public boolean addUnique(long[] c) {
		boolean returnValue=false;
		for(long i : c) returnValue|=forceAdd(i);
		return(returnValue);
	}

	/** Adds all elements, skip checking duplicates */
	public boolean addUnique(LongSet s) {
		boolean returnValue=false;
		for(int index=0;index<=s.lastIndex;index++) {
			if(s.isThere.get(index)) returnValue|=forceAdd(s.data[index]);
		}
		return(returnValue);
	}

	/** Adds all elements, skip checking duplicates */
	public boolean addUnique(BitSet s) {
		boolean returnValue=false;
		for(int i=s.nextSetBit(0);i>=0;i=s.nextSetBit(i+1)) {      
			returnValue|=forceAdd(i);
		}
		return(returnValue);
	}

	/** Removes all elements of s from this set*/
	public boolean removeAll(BitSet s) {
		boolean returnValue=false;
		for(int i=s.nextSetBit(0);i>=0;i=s.nextSetBit(i+1)) {      
			returnValue|=remove(i);
		}
		return(returnValue);
	}

	/** Removes all elements in c*/
	public boolean removeAll(long[] c) {
		boolean result=false;
		for(long o : c) result|=remove(o);
		return result;
	}

	/** Adds all elements, returns THIS*/  
	public LongSet enhancedBy(LongSet addMe) {
		addAll(addMe);
		return(this);
	}

	/** Adds addMe, returns THIS*/
	public LongSet enhancedBy(long addMe) {
		add(addMe);
		return(this);
	}

	/** Removes the given elements, returns THIS*/
	public LongSet shrunkBy(LongSet addMe) {
		removeAll(addMe);
		return(this);
	}

	/** Removes the given element, returns THIS*/
	public LongSet shrunkBy(long addMe) {
		remove(addMe);
		return(this);
	}

	/** TRUE if the set contains i*/
	public boolean contains(long i) {
		return(indexOf(i)!=-1);
	}

	/** TRUE if the set is empty*/
	public boolean isEmpty() {
		return numElements==0;
	}

	/** Removes all elements*/
	public void clear() {
		clear(10);
	} 
}
