package mikera.vectorz.impl;

import mikera.arrayz.Arrayz;
import mikera.arrayz.INDArray;
import mikera.arrayz.impl.IStridedArray;
import mikera.matrixx.AMatrix;
import mikera.matrixx.Matrixx;
import mikera.matrixx.impl.StridedMatrix;
import mikera.vectorz.AVector;
import mikera.vectorz.Vector;
import mikera.vectorz.util.ErrorMessages;

/**
 * Abstract base class for vectors backed by a double[] array with a constant stride
 * 
 * The double array can be directly accessed for performance purposes
 * 
 * @author Mike
 */
public abstract class AStridedVector extends AVector implements IStridedArray {
	private static final long serialVersionUID = -7239429584755803950L;

	public abstract double[] getArray();
	public abstract int getArrayOffset();
	public abstract int getStride();
	
	@Override
	public AStridedVector ensureMutable() {
		return clone();
	}
	
	@Override public double dotProduct(double[] data, int offset) {
		double[] array=getArray();
		int thisOffset=getArrayOffset();
		int stride=getStride();
		int length=length();
		double result=0.0;
		for (int i=0; i<length; i++) {
			result+=array[i*stride+thisOffset]*data[i+offset];
		}
		return result;
	}
	
	@Override
	public INDArray broadcast(int... shape) {
		int dims=shape.length;
		if (dims==0) {
			throw new IllegalArgumentException(ErrorMessages.incompatibleBroadcast(this, shape));
		} else if (dims==1) {
			if (shape[0]!=length()) throw new IllegalArgumentException(ErrorMessages.incompatibleBroadcast(this, shape));
			return this;
		} else if (dims==2) {
			int rc=shape[0];
			int cc=shape[1];
			if (cc!=length()) throw new IllegalArgumentException(ErrorMessages.incompatibleBroadcast(this, shape));
			return Matrixx.wrapStrided(getArray(), rc, cc, getArrayOffset(), 0, getStride());
		}
		if (shape[dims-1]!=length()) throw new IllegalArgumentException(ErrorMessages.incompatibleBroadcast(this, shape));
		int[] newStrides=new int[dims];
		newStrides[dims-1]=getStride();
		return Arrayz.wrapStrided(getArray(),getArrayOffset(),shape,newStrides);
	}
	
	@Override
	public INDArray broadcastLike(INDArray target) {
		if (target instanceof AMatrix) {
			return broadcastLike((AMatrix)target);
		}
		return broadcast(target.getShape());
	}
	
	@Override
	public INDArray broadcastLike(AMatrix target) {
		if (length()==target.columnCount()) {
			return StridedMatrix.wrap(getArray(), target.rowCount(), length(), getArrayOffset(), 0, getStride());
		} else {
			throw new IllegalArgumentException(ErrorMessages.incompatibleShapes(this, target));
		}
	}
	
	@Override
	public AStridedVector clone() {
		return Vector.create(this);
	}
	
	public void add(Vector v) {
		int length=length();
		if(length!=v.length()) throw new IllegalArgumentException("Mismatched vector sizes");
		for (int i = 0; i < length; i++) {
			addAt(i,v.data[i]);
		}
	}
	
	@Override
	public double[] asDoubleArray() {
		if (isPackedArray()) return getArray();
		return null;
	}

	@Override
	public boolean isPackedArray() {
		return (getStride()==1)&&(getArrayOffset()==0)&&(getArray().length==length());
	}
	
	@Override
	public int[] getStrides() {
		return new int[] {getStride()};
	}
	
	@Override
	public int getStride(int dimension) {
		switch (dimension) {
		case 0: return getStride();
		default: throw new IllegalArgumentException(ErrorMessages.invalidDimension(this, dimension));
		}
	}
}
