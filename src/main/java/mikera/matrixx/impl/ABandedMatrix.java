package mikera.matrixx.impl;

import mikera.matrixx.AMatrix;
import mikera.matrixx.Matrix;
import mikera.vectorz.AVector;
import mikera.vectorz.Vector;
import mikera.vectorz.util.VectorzException;

/** 
 * Abstract base class for banded matrices
 * 
 * Banded matrix implementations are assumed to store their data efficiently in diagonal bands,
 * so functions on banded matrices are designed to exploit this fact.
 * 
 * May be either square or rectangular
 * 
 * @author Mike
 *
 */
public abstract class ABandedMatrix extends AMatrix {
	
	@Override
	public abstract int upperBandwidthLimit();
	
	@Override
	public abstract int lowerBandwidthLimit();
	
	@Override
	public abstract AVector getBand(int band);
	
	@Override
	public int upperBandwidth() {
		for (int i=upperBandwidthLimit(); i>0; i--) {
			if (!(getBand(i).isZero())) return i;
		}
		return 0;
	}
	
	@Override
	public int lowerBandwidth() {
		for (int i=-lowerBandwidthLimit(); i<0; i++) {
			if (!(getBand(i).isZero())) return i;
		}
		return 0;
	}
	
	@Override
	public boolean isFullyMutable() {
		return false;
	}
	
	@Override
	public boolean isSymmetric() {
		if (rowCount()!=columnCount()) return false;
		int bs=Math.max(upperBandwidthLimit(), lowerBandwidthLimit());
		for (int i=1; i<=bs; i++) {
			if (!getBand(i).equals(getBand(-i))) return false;
		}
		return true;
	}
	
	@Override
	public boolean isUpperTriangular() {
		return (lowerBandwidthLimit()==0)||(lowerBandwidth()==0);
	}
	
	@Override
	public boolean isLowerTriangular() {
		return (upperBandwidthLimit()==0)||(upperBandwidth()==0);
	}
	
	@Override
	public AVector getRow(int row) {
		return new BandedMatrixRow(row);
	}
	
	@Override 
	public long nonZeroCount() {
		long t=0;
		for (int i=-lowerBandwidthLimit(); i<=upperBandwidthLimit(); i++) {
			t+=getBand(i).nonZeroCount();
		}
		return t;
	}
	
	@Override 
	public double elementSum() {
		double t=0;
		for (int i=-lowerBandwidthLimit(); i<=upperBandwidthLimit(); i++) {
			t+=getBand(i).elementSum();
		}
		return t;
	}
	
	@Override 
	public double elementSquaredSum() {
		double t=0;
		for (int i=-lowerBandwidthLimit(); i<=upperBandwidthLimit(); i++) {
			t+=getBand(i).elementSquaredSum();
		}
		return t;
	}
	
	@Override 
	public void fill(double value) {
		for (int i=-rowCount()+1; i<columnCount(); i++) {
			getBand(i).fill(value);
		}
	}
	
	@Override
	public Matrix toMatrix() {
		int rc = rowCount();
		int cc = columnCount();
		Matrix m = Matrix.create(rc, cc);
		for (int i=-lowerBandwidthLimit(); i<=upperBandwidthLimit(); i++) {
			m.getBand(i).set(this.getBand(i));
		}
		return m;
	}
	
	@Override
	public Matrix toMatrixTranspose() {
		int rc = rowCount();
		int cc = columnCount();
		Matrix m = Matrix.create(cc, rc);
		for (int i=-lowerBandwidthLimit(); i<=upperBandwidthLimit(); i++) {
			m.getBand(-i).set(this.getBand(i));
		}
		return m;
	}
	
	/**
	 * Inner class for generic banded matrix rows
	 * @author Mike
	 *
	 */
	private final class BandedMatrixRow extends AVector {
		final int row;
		final int length;
		final int lower;
		final int upper;
		public BandedMatrixRow(int row) {
			this.row=row;
			this.length=columnCount();
			this.lower=-lowerBandwidthLimit();
			this.upper=upperBandwidthLimit();
		}

		@Override
		public int length() {
			return length;
		}

		@Override
		public double get(int i) {
			if ((i<0)||(i>=length)) throw new IndexOutOfBoundsException("Index: "+i);
			return unsafeGet(i);
		}
		
		@Override
		public double unsafeGet(int i) {
			int b=i-row;
			if ((b<lower)||(b>upper)) return 0;
			return getBand(b).unsafeGet(Math.min(i, row));
		}
		
		@Override 
		public double dotProduct(AVector v) {
			double result=0.0;
			for (int i=Math.max(0,lower+row); i<=Math.min(length-1, row+upper);i++) {
				result+=getBand(i-row).unsafeGet(Math.min(i, row))*v.unsafeGet(i);
			}
			return result;
		}
		
		@Override 
		public double dotProduct(Vector v) {
			double result=0.0;
			for (int i=Math.max(0,lower+row); i<=Math.min(length-1, row+upper);i++) {
				result+=getBand(i-row).unsafeGet(Math.min(i, row))*v.unsafeGet(i);
			}
			return result;
		}

		@Override
		public void set(int i, double value) {
			if ((i<0)||(i>=length)) throw new IndexOutOfBoundsException("Index: "+i);
			unsafeSet(i,value);
		}
		
		@Override
		public void unsafeSet(int i, double value) {
			int b=i-row;
			getBand(b).unsafeSet(Math.min(i, row),value);
		}

		@Override
		public AVector exactClone() {
			return ABandedMatrix.this.exactClone().getRow(row);
		}
	
		@Override
		public boolean isFullyMutable() {
			return ABandedMatrix.this.isFullyMutable();
		}
	}
	
	
	@Override public void validate() {
		super.validate();
		if (lowerBandwidthLimit()<0) throw new VectorzException("Negative lower bandwidth limit?!?");
		int minBand=-lowerBandwidthLimit();
		int maxBand=upperBandwidthLimit();
		if (minBand<=-rowCount()) throw new VectorzException("Invalid lower limit: "+minBand);
		if (maxBand>=columnCount()) throw new VectorzException("Invalid upper limit: "+maxBand);
		for (int i=minBand; i<=maxBand; i++) {
			AVector v=getBand(i);
			if (bandLength(i)!=v.length()) throw new VectorzException("Invalid band length: "+i);
		}
	}
}
