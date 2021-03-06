package mikera.vectorz.impl;

import mikera.arrayz.impl.IStridedArray;
import mikera.vectorz.AScalar;
import mikera.vectorz.Scalar;
import mikera.vectorz.util.IntArrays;
import mikera.vectorz.util.VectorzException;

public class ArrayIndexScalar extends AScalar implements IStridedArray {
	final double[] array;
	final int index;

	public ArrayIndexScalar(double[] array, int index) {
		this.array = array;
		this.index = index;
	}

	public static ArrayIndexScalar wrap(double[] array, int index) {
		return new ArrayIndexScalar(array, index);
	}

	@Override
	public double get() {
		return array[index];
	}

	@Override
	public void set(double value) {
		array[index] = value;
	}

	@Override
	public boolean isView() {
		return true;
	}

	@Override
	public boolean isFullyMutable() {
		return true;
	}

	@Override
	public Scalar clone() {
		return Scalar.create(array[index]);
	}

	@Override
	public ArrayIndexScalar exactClone() {
		return new ArrayIndexScalar(array.clone(), index);
	}

	@Override
	public void validate() {
		if ((index < 0) || (index >= array.length)) { 
			throw new VectorzException("Index out of bounds"); 
		}
		super.validate();
	}

	@Override
	public double[] getArray() {
		return array;
	}

	@Override
	public int getArrayOffset() {
		return index;
	}

	@Override
	public int[] getStrides() {
		return IntArrays.EMPTY_INT_ARRAY;
	}

	@Override
	public int getStride(int dimension) {
		throw new IndexOutOfBoundsException("Can't access strides for a scalar");
	}

	@Override
	public boolean isPackedArray() {
		return (index==0)&&(array.length==1);
	}
	
	@Override
	public double[] asDoubleArray() {
		return isPackedArray()?array:null;
	}
}
