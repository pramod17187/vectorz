package mikera.vectorz.util;

import mikera.arrayz.INDArray;
import mikera.indexz.Index;
import mikera.matrixx.AMatrix;
import mikera.vectorz.AVector;

public class ErrorMessages {
	private static String shape(INDArray a) {
		return Index.of(a.getShape()).toString();
	}
	
	private static String shape(int... indexes) {
		return Index.of(indexes).toString();
	}
	
	private static String pos(int... indexes) {
		return Index.of(indexes).toString();
	}
	
	/**
	 * Returns an error message indicating that two arrays have different sizes
	 * @param a
	 * @param b
	 * @return
	 */
	public static String mismatch(INDArray a, INDArray b) {
		return "Mismatched sizes: "+shape(a)+" vs. "+shape(b);
	}
	
	/**
	 * Returns an error message indicating that two arrays have incompatible shapes.
	 * 
	 * e.g. "Incompatible shapes: [3,2] vs. [2,2]"
	 * @param a
	 * @param b
	 * @return
	 */
	public static String incompatibleShapes(INDArray a, INDArray b) {
		return "Incompatible shapes: "+shape(a)+" vs. "+shape(b);
	}
	
	public static String incompatibleShape(INDArray m) {
		return "Incompatible shape: "+shape(m);
	}

	
	/**
	 * Returns an error message indicating that a broadcast is not possible
	 * 
	 * e.g. "Can't broadcast Matrix with shape [2,2] to shape [3,3,3]
	 * @param a
	 * @param b
	 * @return
	 */
	public static String incompatibleBroadcast(INDArray a, int... shape) {
		return "Can't broadcast "+a.getClass()+" with shape "+shape(a)+" to shape: "+shape(shape);
	}

	public static String notFullyMutable(AMatrix m,	int row, int column) {
		return "Can't mutate "+m.getClass()+ " at position: "+pos(row,column);
	}

	public static String wrongDestLength(AVector dest) {
		return "Wrong destination vector size: "+shape(dest);
	}
	
	public static String wrongSourceLength(AVector source) {
		return "Wrong source vector size: "+shape(source);
	}

	public static String squareMatrixRequired(AMatrix m) {
		return "Square matrix required! This matrix has shape: "+shape(m);
	}

	public static String position(int... indexes) {
		return "Invalid index: "+pos(indexes);
	}

	public static String illegalSize(int... shape) {
		return "Illegal shape" +shape(shape);
	}

	public static String immutable(Object a) {
		return a.getClass().toString()+" is immutable!";
	}

	public static String invalidDimension(INDArray a, int dimension) {
		return ""+a.getClass()+" with shape "+shape(a)+" does not have dimension: "+dimension;
	}

	public static String invalidIndex(INDArray a, int... indexes) {
		int[] shape=a.getShape();
		if (shape.length!=indexes.length) {
			return ""+indexes.length+"-D access with index "+pos(indexes)+" not possible for "+a.getClass()+" with shape "+shape(shape);
		} else {
			return "Access at position "+pos(indexes)+" not possible for "+a.getClass()+" with shape "+shape(shape);
		}
	}

	public static String invalidSlice(INDArray a, int slice) {
		return ""+a.getClass()+" with shape "+shape(a)+" does not have slice: "+slice;
	}

	public static String noSlices(INDArray a) {
		return "Cannot access slices of 0-D "+a.getClass();
	}

}
