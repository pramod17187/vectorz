package mikera.matrixx.impl;

import mikera.indexz.Index;
import mikera.indexz.Indexz;
import mikera.matrixx.AMatrix;
import mikera.vectorz.AVector;
import mikera.vectorz.impl.IndexedSubVector;
import mikera.vectorz.util.VectorzException;

/**
 * Reference matrix class representing a permutation of a matrix
 * 
 * @author Mike
 */
public class PermutedMatrix extends AMatrix{
	private final AMatrix source;
	private final Index rowPermutations;
	private final Index columnPermutations;
	
	public PermutedMatrix(AMatrix source, Index rowPermutations) {
		this(source,rowPermutations, Indexz.createSequence(source.columnCount()));
	}	
	
	public PermutedMatrix(AMatrix source, Index rowPermutations, Index columnPermutations) {
		if (source instanceof PermutedMatrix) {
			PermutedMatrix pm=(PermutedMatrix)source;
			
			Index rp=pm.rowPermutations.clone();
			rp.permute(rowPermutations);
			rowPermutations=rp;
			
			Index cp=pm.columnPermutations.clone();
			rp.permute(columnPermutations);
			columnPermutations=cp;

			source=pm.source;
		}
		if (source.rowCount()!=rowPermutations.length()) throw new VectorzException("Incorrect row permutation count: "+rowPermutations.length());
		if (source.columnCount()!=columnPermutations.length()) throw new VectorzException("Incorrect column permutation count: "+columnPermutations.length());
		this.rowPermutations=rowPermutations;
		this.columnPermutations=columnPermutations;
		this.source=source;
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
		int sourceRow=rowPermutations.get(row);
		int sourceColumn=columnPermutations.get(column);
		return source.get(sourceRow, sourceColumn);
	}

	@Override
	public void set(int row, int column, double value) {
		int sourceRow=rowPermutations.get(row);
		int sourceColumn=columnPermutations.get(column);
		source.set(sourceRow, sourceColumn,value);
	}
	
	@Override
	public double unsafeGet(int row, int column) {
		int sourceRow=rowPermutations.get(row);
		int sourceColumn=columnPermutations.get(column);
		return source.unsafeGet(sourceRow, sourceColumn);
	}

	@Override
	public void unsafeSet(int row, int column, double value) {
		int sourceRow=rowPermutations.get(row);
		int sourceColumn=columnPermutations.get(column);
		source.unsafeSet(sourceRow, sourceColumn,value);
	}
	
	/**
	 * Returns a row of the permuted matrix as a vector reference
	 */
	@Override
	public AVector getRow(int row) {
		return IndexedSubVector.wrap(source.getRow(rowPermutations.get(row)),columnPermutations.getData());
	}

	/**
	 * Returns a column of the permuted  matrix as a vector reference
	 */
	@Override
	public AVector getColumn(int column) {
		return IndexedSubVector.wrap(source.getColumn(columnPermutations.get(column)),rowPermutations.getData());
	}
	
	@Override
	public PermutedMatrix exactClone() {
		return new PermutedMatrix(source.exactClone(),rowPermutations.clone(),columnPermutations.clone());
	}
}
