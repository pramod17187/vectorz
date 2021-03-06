package mikera.matrixx;

import java.nio.DoubleBuffer;
import java.util.Arrays;

import mikera.arrayz.INDArray;
import mikera.matrixx.impl.ADenseArrayMatrix;
import mikera.matrixx.impl.AStridedMatrix;
import mikera.matrixx.impl.StridedMatrix;
import mikera.matrixx.impl.VectorMatrixMN;
import mikera.vectorz.AVector;
import mikera.vectorz.Op;
import mikera.vectorz.Vector;
import mikera.vectorz.impl.AStridedVector;
import mikera.vectorz.impl.ArraySubVector;
import mikera.vectorz.impl.MatrixBandVector;
import mikera.vectorz.impl.StridedVector;
import mikera.vectorz.util.DoubleArrays;
import mikera.vectorz.util.ErrorMessages;
import mikera.vectorz.util.VectorzException;

/** 
 * Standard MxN matrix class backed by a fully packed double[] array
 * 
 * This is the most efficient Vectorz type for 2D matrices.
 * 
 * @author Mike
 */
public final class Matrix extends ADenseArrayMatrix {
	
	private Matrix(int rowCount, int columnCount) {
		this(rowCount,columnCount,new double[rowCount*columnCount]);
	}
	
	/**
	 * Creates a new zero-filled matrix of the specified shape.
	 */
	public static Matrix create(int rowCount, int columnCount) {
		return new Matrix(rowCount,columnCount);
	}
	
	public static Matrix create(AMatrix m) {
		Matrix nm=new Matrix(m.rowCount(),m.columnCount());
		nm.set(m);
		return nm;
	}
	
	public Matrix(AMatrix m) {
		this(m.rowCount(),m.columnCount());
		set(m);
	}
	
	public static Matrix create(INDArray m) {
		if (m.dimensionality()!=2) throw new IllegalArgumentException("Can only create matrix from 2D array");
		int rows=m.getShape(0);
		int cols=m.getShape(1);
		double[] data=new double[rows*cols];
		m.getElements(data, 0);
		return Matrix.wrap(rows, cols, data);		
	}
	
	public static Matrix create(Object... rowVectors) {
		AMatrix m=VectorMatrixMN.create(rowVectors);
		return create(m);
	}
	
	@Override
	public boolean isView() {
		return false;
	}
	
	@Override
	public boolean isBoolean() {
		return DoubleArrays.isBoolean(data,0,data.length);
	}
	
	@Override
	public boolean isPackedArray() {
		return true;
	}
	
	private Matrix(int rowCount, int columnCount, double[] data) {
		super(data,rowCount,columnCount);
	}
	
	public static Matrix wrap(int rowCount, int columnCount, double[] data) {
		if (data.length!=rowCount*columnCount) throw new VectorzException("data array is of wrong size: "+data.length);
		return new Matrix(rowCount,columnCount,data);
	}
	
	@Override
	public AStridedMatrix subMatrix(int rowStart, int rows, int colStart, int cols) {
		if ((rowStart<0)||(rowStart>=this.rows)||(colStart<0)||(colStart>=this.cols)) throw new IndexOutOfBoundsException("Invalid submatrix start position");
		if ((rowStart+rows>this.rows)||(colStart+cols>this.cols)) throw new IndexOutOfBoundsException("Invalid submatrix end position");
		if ((rows<1)||(cols<1)) throw new IllegalArgumentException("Submatrix has no elements");
		return StridedMatrix.wrap(data, rows, cols, 
				rowStart*rowStride()+colStart*columnStride(), 
				rowStride(), columnStride());
	}
	
	@Override
	public Vector innerProduct(AVector a) {
		if (a instanceof Vector) return innerProduct((Vector)a);
		return transform(a);
	}
	
	@Override
	public Matrix innerProduct(Matrix a) {
		// TODO: detect large matrices and farm off to cache-efficient function
		
		int ic=this.columnCount();
		if ((ic!=a.rowCount())) {
			throw new IllegalArgumentException(ErrorMessages.mismatch(this, a));
		}
		int rc=this.rowCount();
		int cc=a.columnCount();
		Matrix result=Matrix.create(rc,cc);
		for (int i=0; i<rc; i++) {
			int toffset=ic*i;
			for (int j=0; j<cc; j++) {
				double acc=0.0;
				for (int k=0; k<ic; k++) {
					acc+=data[toffset+k]*a.unsafeGet(k, j);
				}
				result.unsafeSet(i,j,acc);
			}
		}
		return result;
	}

	@Override
	public Matrix innerProduct(AMatrix a) {
		// TODO: consider transposing a into packed arrays?
		if (a instanceof Matrix) {
			return innerProduct((Matrix)a);
		}
		if ((this.columnCount()!=a.rowCount())) {
			throw new IllegalArgumentException(ErrorMessages.mismatch(this, a));
		}
		int rc=this.rowCount();
		int cc=a.columnCount();
		int ic=this.columnCount();
		Matrix result=Matrix.create(rc,cc);
		for (int i=0; i<rc; i++) {
			int toffset=ic*i;
			for (int j=0; j<cc; j++) {
				double acc=0.0;
				for (int k=0; k<ic; k++) {
					acc+=data[toffset+k]*a.unsafeGet(k, j);
				}
				result.unsafeSet(i,j,acc);
			}
		}
		return result;
	}
	
	@Override
	public double elementSum() {
		return DoubleArrays.elementSum(data);
	}
	
	@Override
	public void abs() {
		DoubleArrays.abs(data);
	}
	
	@Override
	public void signum() {
		DoubleArrays.signum(data);
	}
	
	@Override
	public void square() {
		DoubleArrays.square(data);
	}
	
	@Override
	public void exp() {
		DoubleArrays.exp(data);
	}
	
	@Override
	public void log() {
		DoubleArrays.log(data);
	}
	
	@Override
	public long nonZeroCount() {
		return DoubleArrays.nonZeroCount(data);
	}
	
	@Override
	public Matrix clone() {
		return new Matrix(rows,cols,DoubleArrays.copyOf(data));
	}
	
	@Override
	public Vector cloneRow(int row) {
		int cc = columnCount();
		Vector v = Vector.createLength(cc);
		copyRowTo(row,v.data,0);
		return v;
	}
	
	@Override
	public final void copyRowTo(int row, double[] dest, int destOffset) {
		int srcOffset=row*cols;
		System.arraycopy(data, srcOffset, dest, destOffset, cols);
	}
	
	@Override
	public final void copyColumnTo(int col, double[] dest, int destOffset) {
		int colOffset=col;
		for (int i=0;i<rows; i++) {
			dest[destOffset+i]=data[colOffset+i*cols];
		}
	}

	@Override
	public Vector transform (AVector a) {
		Vector v=Vector.createLength(rows);
		for (int i=0; i<rows; i++) {
			v.data[i]=a.dotProduct(data, i*cols);
		}
		return v;
	}
	
	@Override
	public Vector transform (Vector a) {
		Vector v=Vector.createLength(rows);
		transform(a,v);
		return v;
	}
	
	@Override
	public void transform(AVector source, AVector dest) {
		if ((source instanceof Vector )&&(dest instanceof Vector)) {
			transform ((Vector)source, (Vector)dest);
			return;
		}
		if(rows!=dest.length()) throw new IllegalArgumentException(ErrorMessages.wrongDestLength(dest));
		if(cols!=source.length()) throw new IllegalArgumentException(ErrorMessages.wrongSourceLength(source));
		int index=0;
		for (int i=0; i<rows; i++) {
			double acc=0.0;
			for (int j=0; j<cols; j++) {
				acc+=data[index++]*source.unsafeGet(j);
			}
			dest.unsafeSet(i,acc);
		}
	}
	
	@Override
	public void transform(Vector source, Vector dest) {
		int rc = rowCount();
		int cc = columnCount();
		if (source.length()!=cc) throw new IllegalArgumentException(ErrorMessages.wrongSourceLength(source));
		if (dest.length()!=rc) throw new IllegalArgumentException(ErrorMessages.wrongDestLength(dest));
		int di=0;
		for (int row = 0; row < rc; row++) {
			double total = 0.0;
			for (int column = 0; column < cc; column++) {
				total += data[di+column] * source.data[column];
			}
			di+=cc;
			dest.data[row]=total;
		}
	}
	
	@Override
	public ArraySubVector getRow(int row) {
		return ArraySubVector.wrap(data,row*cols,cols);
	}
	
	@Override
	public AStridedVector getColumn(int col) {
		if (cols==1) {
			if (col!=0) throw new IndexOutOfBoundsException("Column does not exist: "+col);
			return Vector.wrap(data);
		} else {
			return StridedVector.wrap(data,col,rows,cols);
		}
	}

	@Override
	public void swapRows(int i, int j) {
		if (i == j) return;
		int a = i*cols;
		int b = j*cols;
		int cc = columnCount();
		for (int k = 0; k < cc; k++) {
			double t = data[a+k];
			data[a+k]=data[b+k];
			data[b+k]=t;
		}
	}
	
	@Override
	public void swapColumns(int i, int j) {
		if (i == j) return;
		int rc = rowCount();
		int cc = columnCount();
		for (int k = 0; k < rc; k++) {
			int x=k*cc;
			double t = data[i+x];
			data[i+x]=data[j+x];
			data[j+x]=t;
		}
	}
	
	@Override
	public void multiplyRow(int i, double factor) {
		int offset=i*cols;
		for (int j=0; j<cols; j++) {
			data[offset+j]*=factor;
		}
	}
	
	@Override
	public void addRowMultiple(int src, int dst, double factor) {
		int soffset=src*cols;
		int doffset=dst*cols;
		for (int j=0; j<cols; j++) {
			data[doffset+j]+=factor*data[soffset+j];
		}
	}
	
	@Override
	public Vector asVector() {
		return Vector.wrap(data);
	}
	
	@Override
	public Vector toVector() {
		return Vector.create(data);
	}
	
	@Override
	public void toDoubleBuffer(DoubleBuffer dest) {
		dest.put(data);
	}
	
	@Override
	public double[] asDoubleArray() {
		return data;
	}

	@Override
	public double get(int row, int column) {
		if ((column<0)||(column>=cols)) throw new IndexOutOfBoundsException();
		return data[(row*cols)+column];
	}

	@Override
	public void unsafeSet(int row, int column, double value) {
		data[(row*cols)+column]=value;
	}
	
	@Override
	public double unsafeGet(int row, int column) {
		return data[(row*cols)+column];
	}

	@Override
	public void set(int row, int column, double value) {
		if ((column<0)||(column>=cols)) throw new IndexOutOfBoundsException();
		data[(row*cols)+column]=value;
	}
	
	@Override
	public void applyOp(Op op) {
		op.applyTo(data);
	}
	
	public void addMultiple(Matrix m,double factor) {
		assert(rowCount()==m.rowCount());
		assert(columnCount()==m.columnCount());
		for (int i=0; i<data.length; i++) {
			data[i]+=m.data[i]*factor;
		}
	}
	
	public void add(Matrix m) {
		assert(rowCount()==m.rowCount());
		assert(columnCount()==m.columnCount());
		for (int i=0; i<data.length; i++) {
			data[i]+=m.data[i];
		}
	}

	@Override
	public void addMultiple(AMatrix m,double factor) {
		if (m instanceof Matrix) {addMultiple((Matrix)m,factor); return;}
		int rc=rowCount();
		int cc=columnCount();
		if (!((rc==m.rowCount())&&(cc==m.columnCount()))) throw new IllegalArgumentException(ErrorMessages.mismatch(this, m));

		int di=0;
		for (int i=0; i<rc; i++) {
			for (int j=0; j<cc; j++) {
				data[di++]+=m.unsafeGet(i, j)*factor;
			}
		}
	}
	
	@Override
	public void add(double d) {
		DoubleArrays.add(data, d);
	}
	
	@Override
	public void add(AMatrix m) {
		if (m instanceof Matrix) {add((Matrix)m); return;}
		int rc=rowCount();
		int cc=columnCount();
		if (!((rc==m.rowCount())&&(cc==m.columnCount()))) throw new IllegalArgumentException(ErrorMessages.mismatch(this, m));

		int di=0;
		for (int i=0; i<rc; i++) {
			for (int j=0; j<cc; j++) {
				data[di++]+=m.unsafeGet(i, j);
			}
		}
	}
	
	@Override
	public void multiply(double factor) {
		for (int i=0; i<data.length; i++) {
			data[i]*=factor;
		}
	}
	
	@Override
	public void set(AMatrix a) {
		int rc = rowCount();
		if (!(rc==a.rowCount())) throw new IllegalArgumentException(ErrorMessages.mismatch(this, a));
		int cc = columnCount();
		if (!(cc==a.columnCount())) throw new IllegalArgumentException(ErrorMessages.mismatch(this, a));
		a.getElements(this.data, 0);
	}
	
	@Override
	public void getElements(double[] dest, int offset) {
		System.arraycopy(data, 0, dest, offset, data.length);
	}
	
	@Override
	public StridedMatrix getTranspose() {
		return StridedMatrix.wrap(data,cols,rows,0,1,cols);
	}
	
	@Override
	public StridedMatrix getTransposeView() {
		return StridedMatrix.wrap(data,cols,rows,0,1,cols);
	}
	
	@Override 
	public void set(double value) {
		Arrays.fill(data,value);
	}
	
	@Override
	public void reciprocal() {
		DoubleArrays.reciprocal(data,0,data.length);
	}
	
	@Override
	public void clamp(double min, double max) {
		DoubleArrays.clamp(data,0,data.length,min,max);
	}
	
	@Override
	public Matrix exactClone() {
		return new Matrix(this);
	}

	@Override
	public void setRow(int i, AVector row) {
		int cc=columnCount();
		if (row.length()!=cc) throw new IllegalArgumentException(ErrorMessages.mismatch(this.getRow(i), row));
		row.getElements(data, i*cc);
	}
	
	@Override
	public void setColumn(int j, AVector col) {
		int rc=rowCount();
		if (col.length()!=rc) throw new IllegalArgumentException(ErrorMessages.mismatch(this.getColumn(j), col));
		for (int i=0; i<rc; i++) {
			data[index(i,j)]=col.unsafeGet(j);
		}
	}
	
	@Override
	public StridedVector getBand(int band) {
		int cc=columnCount();
		int rc=rowCount();
		if ((band>=cc)||(band<=-rc)) return null;
		return StridedVector.wrap(data, (band>=0)?band:(-band)*cc, bandLength(band), cc+1);
	}
	
	@Override
	protected final int index(int row, int col) {
		return row*cols+col;
	}

	@Override
	public int getArrayOffset() {
		return 0;
	}

	@Override
	public double[] getArray() {
		return data;
	}

}
