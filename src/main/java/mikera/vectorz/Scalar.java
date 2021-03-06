package mikera.vectorz;

/**
 * Basic Scalar class containing a single mutable double value
 * 
 * @author Mike
 */
public final class Scalar extends AScalar {
	public double value;

	public Scalar(double value) {
		this.value = value;
	}

	public static Scalar create(double value) {
		return new Scalar(value);
	}

	public static Scalar create(AScalar a) {
		return create(a.get());
	}

	@Override
	public double get() {
		return value;
	}

	@Override
	public void set(double value) {
		this.value = value;
	}

	@Override
	public void abs() {
		value = Math.abs(value);
	}

	@Override
	public void add(double d) {
		value += d;
	}

	@Override
	public void sub(double d) {
		value -= d;
	}

	@Override
	public void add(AScalar s) {
		value += s.get();
	}

	@Override
	public void multiply(double factor) {
		value *= factor;
	}

	@Override
	public void negate() {
		value = -value;
	}
	
	@Override
	public void scaleAdd(double factor, double constant) {
		value = value*factor + constant;
	}

	@Override
	public boolean isView() {
		return false;
	}
	
	@Override
	public boolean isZero() {
		return value==0.0;
	}

	@Override
	public void getElements(double[] dest, int offset) {
		dest[offset] = value;
	}

	@Override
	public Scalar exactClone() {
		return clone();
	}

	/**
	 * Creates a new Scalar using the elements in the specified vector.
	 * Zero-pads the data as required to define the Scalar
	 * @param data
	 * @param rows
	 * @param columns
	 * @return
	 */
	public static Scalar createFromVector(AVector data) {
		return new Scalar(data.length()>0?data.get(0):0.0);
	}

}
