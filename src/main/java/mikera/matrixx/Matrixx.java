package mikera.matrixx;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import us.bpsm.edn.parser.Parseable;
import us.bpsm.edn.parser.Parser;
import us.bpsm.edn.parser.Parsers;
import mikera.indexz.Index;
import mikera.matrixx.impl.ADiagonalMatrix;
import mikera.matrixx.impl.ColumnMatrix;
import mikera.matrixx.impl.DiagonalMatrix;
import mikera.matrixx.impl.IdentityMatrix;
import mikera.matrixx.impl.ScalarMatrix;
import mikera.matrixx.impl.StridedMatrix;
import mikera.matrixx.impl.VectorMatrixMN;
import mikera.matrixx.impl.ZeroMatrix;
import mikera.util.Rand;
import mikera.vectorz.AVector;
import mikera.vectorz.Tools;
import mikera.vectorz.Vector3;
import mikera.vectorz.Vectorz;
import mikera.vectorz.impl.SparseIndexedVector;
import mikera.vectorz.util.VectorzException;

/**
 * Static method class for matrices
 * 
 * @author Mike
 */
public class Matrixx {

	/**
	 * Creates an mutable identity matrix
	 */
	public static AMatrix createIdentityMatrix(int dimensions) {
		AMatrix m = newMatrix(dimensions, dimensions);
		for (int i = 0; i < dimensions; i++) {
			m.set(i, i, 1.0);
		}
		return m;
	}

	/**
	 * Creates an immutable identity matrix
	 */
	public static IdentityMatrix createImmutableIdentityMatrix(int dimensions) {
		return IdentityMatrix.create(dimensions);
	}

	/**
	 * Coerces to a matrix
	 */
	public static AMatrix toMatrix(Object o) {
		if (o instanceof AMatrix) {
			return (AMatrix) o;
		} else if (o instanceof AVector) {
			return ColumnMatrix.wrap((AVector) o);
		} else if (o instanceof Iterable<?>) {
			ArrayList<AVector> al = new ArrayList<AVector>();
			for (Object obj : (Iterable<?>) o) {
				al.add(Vectorz.toVector(obj));
			}
			return createFromVectors(al);
		}
		throw new UnsupportedOperationException("Can't convert to matrix: "
				+ o.getClass());
	}

	/**
	 * Creates a sparse matrix from the given matrix, ignoring zeros
	 */
	public static AMatrix createSparse(AMatrix m) {
		int rc = m.rowCount();
		AVector[] rows = new AVector[rc];
		for (int i = 0; i < rc; i++) {
			rows[i] = SparseIndexedVector.createFromRow(m, i);
		}
		return VectorMatrixMN.wrap(rows);
	}

	/**
	 * Creates an immutable zero-filled matrix
	 */
	public static ZeroMatrix createImmutableZeroMatrix(int rows, int columns) {
		return ZeroMatrix.create(rows, columns);
	}

	public static ADiagonalMatrix createScaleMatrix(int dimensions,
			double factor) {
		DiagonalMatrix im = new DiagonalMatrix(dimensions);
		for (int i = 0; i < dimensions; i++) {
			im.set(i, i, factor);
		}
		return im;
	}

	public static ADiagonalMatrix createScalarMatrix(int dimensions,
			double factor) {
		return (ADiagonalMatrix) ScalarMatrix.create(dimensions, factor);
	}

	public static DiagonalMatrix createScaleMatrix(double... scalingFactors) {
		int dimensions = scalingFactors.length;
		DiagonalMatrix im = new DiagonalMatrix(dimensions);
		for (int i = 0; i < dimensions; i++) {
			im.set(i, i, scalingFactors[i]);
		}
		return im;
	}

	public static Matrix33 createRotationMatrix(Vector3 axis, double angle) {
		return createRotationMatrix(axis.x, axis.y, axis.z, angle);
	}

	public static Matrix33 createRotationMatrix(double x, double y, double z,
			double angle) {
		double d = Math.sqrt(x * x + y * y + z * z);
		double u = x / d;
		double v = y / d;
		double w = z / d;
		double ca = Math.cos(angle);
		double sa = Math.sin(angle);
		return new Matrix33(u * u + (1 - u * u) * ca,
				u * v * (1 - ca) - w * sa, u * w * (1 - ca) + v * sa, u * v
						* (1 - ca) + w * sa, v * v + (1 - v * v) * ca, v * w
						* (1 - ca) - u * sa, u * w * (1 - ca) - v * sa, v * w
						* (1 - ca) + u * sa, w * w + (1 - w * w) * ca);
	}

	public static Matrix33 createRotationMatrix(AVector v, double angle) {
		if (!(v.length() == 3))
			throw new VectorzException(
					"Rotation matrix requires a 3d axis vector");
		return createRotationMatrix(v.unsafeGet(0), v.unsafeGet(1),
				v.unsafeGet(2), angle);
	}

	public static Matrix33 createXAxisRotationMatrix(double angle) {
		return createRotationMatrix(1, 0, 0, angle);
	}

	public static Matrix33 createYAxisRotationMatrix(double angle) {
		return createRotationMatrix(0, 1, 0, angle);
	}

	public static Matrix33 createZAxisRotationMatrix(double angle) {
		return createRotationMatrix(0, 0, 1, angle);
	}

	public static Matrix22 create2DRotationMatrix(double angle) {
		return Matrix22.createRotationMatrix(angle);
	}

	public static Matrix createRandomSquareMatrix(int dimensions) {
		Matrix m = createSquareMatrix(dimensions);
		fillRandomValues(m);
		return m;
	}

	public static AMatrix createRandomMatrix(int rows, int columns) {
		AMatrix m = newMatrix(rows, columns);
		fillRandomValues(m);
		return m;
	}

	static Matrix createInverse(AMatrix m) {
		if (!m.isSquare()) { throw new IllegalArgumentException(
				"Matrix must be square for inverse!"); }

		int dims = m.rowCount();

		Matrix am = new Matrix(m);
		int[] rowPermutations = new int[dims];

		// perform LU-based inverse on matrix
		decomposeLU(am, rowPermutations);
		return backSubstituteLU(am, rowPermutations);
	}

	/**
	 * Computes LU decomposition of a matrix, returns true if successful (i.e.
	 * if matrix is non-singular)
	 */
	private static void decomposeLU(Matrix am, int[] permutations) {
		int dims = permutations.length;
		double[] data = am.data;

		double rowFactors[] = new double[dims];
		calcRowFactors(data, rowFactors);

		for (int col = 0; col < dims; col++) {
			// Scan upper diagonal matrix
			for (int row = 0; row < col; row++) {
				int dataIndex = (dims * row) + col;
				double acc = data[dataIndex];
				for (int i = 0; i < row; i++) {
					acc -= data[(dims * row) + i] * data[(dims * i) + col];
				}
				data[dataIndex] = acc;
			}

			// Find index of largest pivot
			int maxIndex = 0;
			double maxValue = Double.NEGATIVE_INFINITY;
			for (int row = col; row < dims; row++) {
				int dataIndex = (dims * row) + col;
				double acc = data[dataIndex];
				for (int i = 0; i < col; i++) {
					acc -= data[(dims * row) + i] * data[(dims * i) + col];
				}
				data[dataIndex] = acc;

				double value = rowFactors[row] * Math.abs(acc);
				if (value > maxValue) {
					maxValue = value;
					maxIndex = row;
				}
			}

			if (col != maxIndex) {
				am.swapRows(col, maxIndex);
				rowFactors[maxIndex] = rowFactors[col];
			}

			permutations[col] = maxIndex;

			if (data[(dims * col) + col] == 0.0) { throw new VectorzException(
					"Matrix is singular, cannot compute inverse!"); }

			// Scale lower diagonal matrix using values on diagonal
			double diagonalValue = data[(dims * col) + col];
			double factor = 1.0 / diagonalValue;
			int offset = dims * (col + 1) + col;
			for (int i = 0; i < ((dims - 1) - col); i++) {
				data[(dims * i) + offset] *= factor;
			}
		}
	}

	/**
	 * Utility function to calculate scale factors for each row
	 */
	private static void calcRowFactors(double[] data, double[] factorsOut) {
		int dims = factorsOut.length;
		for (int row = 0; row < dims; row++) {
			double maxValue = 0.0;

			// find maximum value in the row
			for (int col = 0; col < dims; col++) {
				maxValue = Math.max(maxValue, Math.abs(data[row * dims + col]));
			}

			if (maxValue == 0.0) { throw new VectorzException(
					"Matrix is singular!"); }

			// scale factor for row should reduce maximum absolute value to 1.0
			factorsOut[row] = 1.0 / maxValue;
		}
	}

	private static Matrix backSubstituteLU(Matrix am, int[] permutations) {
		int dims = permutations.length;
		double[] dataIn = am.data;

		// create identity matrix in output
		Matrix result = new Matrix(Matrixx.createImmutableIdentityMatrix(dims));
		double[] dataOut = result.data;

		for (int col = 0; col < dims; col++) {
			int rowIndex = -1;

			// Forward substitution phase
			for (int row = 0; row < dims; row++) {
				int pRow = permutations[row];
				double acc = dataOut[(dims * pRow) + col];
				dataOut[(dims * pRow) + col] = dataOut[(dims * row) + col];
				if (rowIndex >= 0) {
					for (int i = rowIndex; i <= row - 1; i++) {
						acc -= dataIn[(row * dims) + i]
								* dataOut[(dims * i) + col];
					}
				} else if (acc != 0.0) {
					rowIndex = row;
				}
				dataOut[(dims * row) + col] = acc;
			}

			// Back substitution phase
			for (int row = 0; row < dims; row++) {
				int irow = (dims - 1 - row);
				int offset = dims * irow;
				double total = 0.0;
				for (int i = 0; i < row; i++) {
					total += dataIn[offset + ((dims - 1) - i)]
							* dataOut[(dims * ((dims - 1) - i)) + col];
				}
				double diagonalValue = dataIn[offset + irow];
				dataOut[(dims * irow) + col] = (dataOut[(dims * irow) + col] - total)
						/ diagonalValue;
			}
		}

		return result;
	}

	/**
	 * Creates an empty (zero-filled) mutable matrix of the specified size
	 * 
	 * @param rows
	 * @param columns
	 * @return
	 */
	public static AMatrix newMatrix(int rows, int columns) {
		if (rows == 2 && columns == 2) return new Matrix22();
		if (rows == 3 && columns == 3) return new Matrix33();
		return Matrix.create(rows, columns);
	}

	/**
	 * Creates a new matrix using the elements in the specified vector.
	 * Truncates or zero-pads the data as required to fill the new matrix
	 * @param data
	 * @param rows
	 * @param columns
	 * @return
	 */
	public static Matrix createFromVector(AVector data, int rows, int columns) {
		Matrix m = Matrix.create(rows, columns);
		int n=Math.min(rows*columns, data.length());
		data.copyTo(0, m.data, 0, n);
		return m;
	}

	/**
	 * Creates a zero-filled matrix with the specified number of dimensions for both rows and columns
	 * @param dimensions
	 * @return
	 */
	private static Matrix createSquareMatrix(int dimensions) {
		return Matrix.create(dimensions, dimensions);
	}

	/**
	 * Creates a mutable deep copy of a matrix
	 */
	public static Matrix create(AMatrix m) {
		return new Matrix(m);
	}

	/**
	 * Create a matrix from a list of rows
	 * 
	 * @param rows
	 * @return
	 */
	public static Matrix create(List<Object> rows) {
		int rc = rows.size();
		AVector firstRow = Vectorz.create(rows.get(0));
		int cc = firstRow.length();

		Matrix m = Matrix.create(rc, cc);
		m.setRow(0, firstRow);

		for (int i = 1; i < rc; i++) {
			m.setRow(i, Vectorz.create(rows.get(i)));
		}
		return m;
	}

	/**
	 * Creates a mutable copy of a matrix
	 */
	public static AMatrix create(IMatrix m) {
		int rows = m.rowCount();
		int columns = m.columnCount();
		AMatrix result = newMatrix(rows, columns);
		for (int i = 0; i < rows; i++) {
			for (int j = 0; j < columns; j++) {
				result.set(i, j, m.get(i, j));
			}
		}
		return result;
	}

	public static void fillRandomValues(AMatrix m) {
		int rows = m.rowCount();
		int columns = m.columnCount();
		for (int i = 0; i < rows; i++) {
			for (int j = 0; j < columns; j++) {
				m.unsafeSet(i, j, Rand.nextDouble());
			}
		}
	}

	public static AMatrix createFromVectors(AVector... data) {
		int rc = data.length;
		int cc = (rc == 0) ? 0 : data[0].length();
		AMatrix m = newMatrix(rc, cc);
		for (int i = 0; i < rc; i++) {
			m.getRow(i).set(data[i]);
		}
		return m;
	}

	public static AMatrix createFromVectors(List<AVector> data) {
		int rc = data.size();
		int cc = (rc == 0) ? 0 : data.get(0).length();
		AMatrix m = newMatrix(rc, cc);
		for (int i = 0; i < rc; i++) {
			m.getRow(i).set(data.get(i));
		}
		return m;
	}

	// ====================================
	// Edn formatting and parsing functions

	private static Parser.Config getMatrixParserConfig() {
		return Parsers.defaultConfiguration();
	}

	/**
	 * Parse a matrix in edn format
	 * 
	 * @param ednString
	 * @return
	 */
	@SuppressWarnings("unchecked")
	public static AMatrix parse(String ednString) {
		Parser p = Parsers.newParser(getMatrixParserConfig());
		Parseable ps = Parsers.newParseable(ednString);
		List<List<Object>> data = (List<List<Object>>) p.nextValue(ps);
		int rc = data.size();
		int cc = (rc == 0) ? 0 : data.get(0).size();
		AMatrix m = newMatrix(rc, cc);
		for (int i = 0; i < rc; i++) {
			for (int j = 0; j < cc; j++) {
				m.set(i, j, Tools.toDouble(data.get(i).get(j)));
			}
		}
		return m;
	}

	public static Matrix deepCopy(AMatrix m) {
		return create(m);
	}

	public static AMatrix createSparse(int inputDims, Index[] indexes,
			AVector[] weights) {
		int len = indexes.length;
		if (len != weights.length)
			throw new VectorzException("Length mismatch!" + len + " vs. "
					+ weights.length);
		AVector[] svs = new AVector[len];
		for (int i = 0; i < len; i++) {
			svs[i] = SparseIndexedVector.create(inputDims, indexes[i],
					weights[i]);
		}
		return VectorMatrixMN.wrap(svs);
	}

	public static AMatrix create(Object... vs) {
		return create(Arrays.asList(vs));
	}

	public static Matrix create(double[][] data) {
		int rows = data.length;
		int cols = data[0].length;
		Matrix m = Matrix.create(rows, cols);
		for (int i = 0; i < rows; i++) {
			double[] ds=data[i];
			if (ds.length!=cols) throw new IllegalArgumentException("Array shape is not rectangular!");
			System.arraycopy(ds, 0, m.data, i * cols, cols);
		}
		return m;
	}

	/**
	 * Wraps double[] data in a strided matrix
	 * @param array
	 * @param arrayOffset
	 * @param reverse
	 * @param reverse2
	 * @return
	 */
	public static AMatrix wrapStrided(double[] data, int rows, int cols, int offset, int rowStride, int colStride) {
		if (offset==0) {
			if ((cols==rowStride)&&(colStride==1)&&(data.length==rows*cols)) {
				return Matrix.wrap(rows, cols, data);
			} 
		}
		return StridedMatrix.wrap(data, rows, cols, offset, rowStride, colStride);
	}

}
