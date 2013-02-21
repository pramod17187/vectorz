package mikera.vectorz.ops;

import java.util.Arrays;

import mikera.vectorz.AVector;

public class ConstantOp extends ABoundedOp {
	
	private final double value;
	
	public ConstantOp(double value) {
		this.value=value;
	}
	
	@Override
	public double apply(double x) {
		return value;
	}
	
	@Override
	public void applyTo(AVector v) {
		v.fill(value);
	}
	
	@Override
	public void applyTo(double[] data) {
		Arrays.fill(data, value);
	}
	
	@Override
	public void applyTo(double[] data, int start,int length) {
		Arrays.fill(data,start,start+length,value);
	}

	@Override
	public double minValue() {
		return value;
	}

	@Override
	public double maxValue() {
		return value;
	}

}