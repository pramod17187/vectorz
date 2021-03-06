package mikera.vectorz.impl;

import java.nio.DoubleBuffer;

import mikera.vectorz.AVector;
import mikera.vectorz.Op;

/**
 * A vector that represents the concatenation of two vectors.
 * 
 * @author Mike
 *
 */
public final class JoinedVector extends AVector {
	private static final long serialVersionUID = -5535850407701653222L;
	
	private final AVector left;
	private final AVector right;
	
	private final int split;
	private final int length;
	
	private JoinedVector(AVector left, AVector right) {
		this.left=left;
		this.right=right;
		this.split=left.length();
		this.length=split+right.length();
	}
	
	/**
	 *  returns a JoinedVector connecting the two vectors
	 * @param left
	 * @param right
	 * @return
	 */
	public static AVector joinVectors(AVector left, AVector right) {
		// balancing in case of nested joined vectors
		while ((left.length()>right.length()*2)&&(left instanceof JoinedVector)) {
			JoinedVector bigLeft=((JoinedVector)left);
			left=bigLeft.left;
			right=joinVectors(bigLeft.right,right);
		}
		while ((left.length()*2<right.length())&&(right instanceof JoinedVector)) {
			JoinedVector bigRight=((JoinedVector)right);
			left=joinVectors(left,bigRight.left);
			right=bigRight.right;
		} 
		return new JoinedVector(left,right);
	}
	
	@Override
	public int length() {
		return length;
	}

	@Override
	public boolean isView() {
		return true;
	}

	@Override
	public boolean isFullyMutable() {
		return left.isFullyMutable() && right.isFullyMutable();
	}
	
	@Override
	public void copyTo(AVector dest, int offset) {
		left.copyTo(dest, offset);
		right.copyTo(dest, offset+split);
	}
	
	@Override
	public void toDoubleBuffer(DoubleBuffer dest) {
		left.toDoubleBuffer(dest);
		right.toDoubleBuffer(dest);
	}
	
	@Override
	public void addToArray(int offset, double[] array, int arrayOffset, int length) {
		assert(arrayOffset+length<=array.length);
		assert(offset+length<=length());
		if (offset>=split) {
			right.addToArray(offset-split, array, arrayOffset, length);
		} else if ((offset+length)<=split) {
			left.addToArray(offset, array, arrayOffset, length);
		} else {
			left.addToArray(offset, array, arrayOffset, (split-offset));
			right.addToArray(0, array, arrayOffset+(split-offset), length-(split-offset));		
		}
	}
	
	@Override
	public void addMultipleToArray(double factor,int offset, double[] array, int arrayOffset, int length) {
		assert(arrayOffset+length<=array.length);
		assert(offset+length<=length());
		if (offset>=split) {
			right.addMultipleToArray(factor,offset-split, array, arrayOffset, length);
		} else if ((offset+length)<=split) {
			left.addMultipleToArray(factor,offset, array, arrayOffset, length);
		} else {
			left.addMultipleToArray(factor,offset, array, arrayOffset, (split-offset));
			right.addMultipleToArray(factor,0, array, arrayOffset+(split-offset), length-(split-offset));		
		}
	}
	
	@Override
	public void addAt(int i, double v) {
		if (i<split) {
			left.addAt(i,v);
		} else {
			right.addAt(i-split,v);
		}
	}
	
	@Override
	public void copyTo(double[] data, int offset) {
		left.copyTo(data, offset);
		right.copyTo(data, offset+split);
	}
	
	@Override
	public void multiplyTo(double[] data, int offset) {
		left.multiplyTo(data, offset);
		right.multiplyTo(data, offset+split);
	}
	
	@Override
	public void divideTo(double[] data, int offset) {
		left.divideTo(data, offset);
		right.divideTo(data, offset+split);
	}
	
	@Override
	public void copyTo(int start, AVector dest, int destOffset, int length) {
		subVector(start,length).copyTo(dest, destOffset);
	}

	
	@Override
	public AVector subVector(int start, int length) {
		assert(start>=0);
		assert((start+length)<=this.length);
		if ((start==0)&&(length==this.length)) return this;
		if (start>=split) return right.subVector(start-split, length);
		if ((start+length)<=split) return left.subVector(start, length);
		
		AVector v1=left.subVector(start, split-start);
		AVector v2=right.subVector(0, length-(split-start));
		return new JoinedVector(v1,v2);
	}
	
	@Override
	public void add(AVector a) {
		assert(length()==a.length());
		if (a instanceof JoinedVector) {
			add((JoinedVector)a);	
		} else {
			add(a,0);
		}
	}
	
	public void add(JoinedVector a) {
		if (split==a.split) {
			left.add(a.left);
			right.add(a.right);
		} else {
			add(a,0);
		}
	}
	
	@Override
	public void scaleAdd(double factor, double constant) {
		left.scaleAdd(factor, constant);
		right.scaleAdd(factor, constant);
	}

	@Override
	public void add(double constant) {
		left.add(constant);
		right.add(constant);
	}
	
	@Override
	public void reciprocal() {
		left.reciprocal();
		right.reciprocal();
	}
	
	@Override
	public void clamp(double min, double max) {
		left.clamp(min, max);
		right.clamp(min, max);
	}
	
	@Override
	public double dotProduct (AVector v) {
		if (v instanceof JoinedVector) {
			JoinedVector jv=(JoinedVector)v;
			return dotProduct(jv);
		}
		return super.dotProduct(v);
	}
	
	@Override
	public double dotProduct(double[] data, int offset) {
		return left.dotProduct(data, offset)+right.dotProduct(data, offset+split);
	}
	
	public double dotProduct (JoinedVector jv) {
		// in likely case of two equally structured JoinedVectors....
		if (jv.left.length()==left.length()) {
			return left.dotProduct(jv.left)+right.dotProduct(jv.right);
		}
		return super.dotProduct(jv);
	}
	
	@Override
	public void add(AVector a,int aOffset) {
		left.add(a,aOffset);
		right.add(a,aOffset+split);
	}
	
	@Override
	public void add(int offset, AVector a) {
		add(offset,a,0,a.length());
	}
	
	@Override
	public void add(int offset, AVector a, int aOffset, int length) {
		if (offset>=split) {
			right.add(offset-split,a,aOffset,length);
		} else {
			if (offset+length<=split) {
				left.add(offset,a,aOffset,length);
			} else {		
				left.add(offset,a,aOffset,split-offset);
				right.add(0,a,aOffset+split-offset,length-(split-offset));
			}
		}
	}
	
	@Override
	public void addMultiple(AVector a, double factor) {
		if (a instanceof JoinedVector) {
			addMultiple((JoinedVector)a,factor);	
		} else {
			left.addMultiple(a, 0, factor);
			right.addMultiple(a, split, factor);
		}
	}
	
	public void addMultiple(JoinedVector a, double factor) {
		if (split==a.split) {
			left.addMultiple(a.left,factor);	
			right.addMultiple(a.right,factor);	
		} else {
			left.addMultiple(a, 0, factor);
			right.addMultiple(a, split, factor);
		}
	}
	
	@Override
	public void addMultiple(AVector a, int aOffset, double factor) {
		left.addMultiple(a, aOffset, factor);
		right.addMultiple(a, aOffset+split, factor);
	}
	
	@Override
	public void addProduct(AVector a, AVector b, double factor) {
		left.addProduct(a, 0, b, 0, factor);
		right.addProduct(a, split, b, split, factor);
	}
	
	@Override
	public void addProduct(AVector a, int aOffset, AVector b, int bOffset, double factor) {
		left.addProduct(a, aOffset,b,bOffset, factor);
		right.addProduct(a, aOffset+split,b,bOffset+split, factor);
	}
	
	
	@Override
	public void signum() {
		left.signum();
		right.signum();
	}
	
	@Override
	public void abs() {
		left.abs();
		right.abs();
	}
	
	@Override
	public void exp() {
		left.exp();
		right.exp();
	}
	
	@Override
	public void log() {
		left.log();
		right.log();
	}
	
	@Override
	public void negate() {
		left.negate();
		right.negate();
	}
	
	@Override
	public void applyOp(Op op) {
		left.applyOp(op);
		right.applyOp(op);
	}
	
	
	@Override
	public double elementSum() {
		return left.elementSum()+right.elementSum();
	}
	
	@Override
	public long nonZeroCount() {
		return left.nonZeroCount()+right.nonZeroCount();
	}
	
	@Override
	public double get(int i) {
		if ((i<0)||(i>=length)) throw new IndexOutOfBoundsException();
		if (i<split) {
			return left.unsafeGet(i);
		}
		return right.unsafeGet(i-split);
	}
	
	@Override
	public void set(AVector src) {
		set(src,0);
	}
	
	@Override
	public double unsafeGet(int i) {
		if (i<split) {
			return left.unsafeGet(i);
		}
		return right.unsafeGet(i-split);
	}

	
	@Override
	public void set(AVector src, int srcOffset) {
		left.set(src,srcOffset);
		right.set(src,srcOffset+split);
	}
	
	@Override
	public void setElements(double[] values, int offset, int length) {
		if (length!=length()) {
			throw new IllegalArgumentException("Incorrect length: "+length);
		}
		left.setElements(values,offset,split);
		right.setElements(values,offset+split,length-split);
	}

	@Override
	public void set(int i, double value) {
		if ((i<0)||(i>=length)) throw new IndexOutOfBoundsException();
		if (i<split) {
			left.set(i,value);
		} else {
			right.set(i-split,value);
		}
	}
	
	@Override
	public void unsafeSet(int i, double value) {
		if (i<split) {
			left.unsafeSet(i,value);
		} else {
			right.unsafeSet(i-split,value);
		}
	}
	
	@Override 
	public void fill(double value) {
		left.fill(value);
		right.fill(value);
	}
	
	@Override
	public void square() {
		left.square();
		right.square();
	}
	
	@Override
	public void sqrt() {
		left.sqrt();
		right.sqrt();
	}
	
	@Override
	public void tanh() {
		left.tanh();
		right.tanh();
	}
	
	@Override
	public void logistic() {
		left.logistic();
		right.logistic();
	}
	
	@Override 
	public void multiply(double value) {
		left.multiply(value);
		right.multiply(value);
	}
	
	public static int depthCalc(AVector v) {
		if (v instanceof JoinedVector) {
			JoinedVector jv=(JoinedVector)v;
			return 1+Math.max(depthCalc(jv.left), depthCalc(jv.right));
		}
		return 1;
	}
	
	public int depth() {
		return depthCalc(this);
	}
	
	@Override 
	public JoinedVector exactClone() {
		return new JoinedVector(left.exactClone(),right.exactClone());
	}

}
