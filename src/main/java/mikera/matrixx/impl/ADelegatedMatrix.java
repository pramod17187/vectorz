package mikera.matrixx.impl;

import mikera.matrixx.AMatrix;
import mikera.vectorz.AVector;
import mikera.vectorz.IOp;
import mikera.vectorz.Op;

/**
 * Abstract base class that delegates certain methods to a source matrix
 * 
 * @author Mike
 */
abstract class ADelegatedMatrix extends AMatrix {

	protected final AMatrix source;
	
	protected ADelegatedMatrix(AMatrix source) {
		this.source=source;
	}
	
	@Override
	public boolean isView() {
		return true;
	}
	
	@Override
	public int rowCount() {
		return source.rowCount();
	}

	@Override
	public int columnCount() {
		return source.columnCount();
	}

	@Override
	public double get(int row, int column) {
		return source.get(row,column);
	}

	@Override
	public void set(int row, int column, double value) {
		source.set(row,column,value);
	}
	
	@Override
	public void applyOp(Op op) {
		source.applyOp(op);
	}
	
	@Override
	public void applyOp(IOp op) {
		source.applyOp(op);
	}
	

	@Override
	public void multiply(double factor) {
		source.multiply(factor);
	}
	
	@Override
	public void set(double value) {
		source.set(value);
	}
	
	@Override
	public AVector getLeadingDiagonal() {
		return source.getLeadingDiagonal();
	}
	
	@Override
	public long elementCount() {
		return source.elementCount();
	}
	
	@Override
	public double elementSum() {
		return source.elementSum();
	}
	
	@Override
	public long nonZeroCount() {
		return source.nonZeroCount();
	}


}
