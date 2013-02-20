package mikera.vectorz;


public interface IOp {

	public void applyTo(AVector v);
	
	public double apply(double x);
	
	public void applyTo(double[] data, int start, int length);

}