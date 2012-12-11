package mikera.transformz.impl;

import mikera.transformz.ATranslation;
import mikera.transformz.Translation3;
import mikera.vectorz.AVector;
import mikera.vectorz.Vector3;

/**
 * Class represnting a transform that returns a 3D constant
 * @author Mike
 *
 */
public final class ConstantTransform3 extends AConstantTransform {
	private double x,y,z;
	
	/**
	 * Creates a new constant transform, using the provided vector as the constant value
	 * Does *not* take a defensive copy
	 * @param inputDimensions
	 * @param value
	 */
	public ConstantTransform3(int inputDimensions, AVector value) {
		super(inputDimensions);
		x=value.get(0);
		y=value.get(1);
		z=value.get(2);
	}

	@Override
	public int outputDimensions() {
		return 3;
	}

	@Override
	public void transform(AVector source, AVector dest) {
		assert(source.length()==inputDimensions());
		dest.set(0,x);
		dest.set(1,y);
		dest.set(2,z);
	}
	
	public void transform(AVector source, Vector3 dest) {
		assert(source.length()==inputDimensions());
		dest.x=x;
		dest.y=y;
		dest.z=z;
	}


	@Override
	public ATranslation getTranslationComponent() {
		return new Translation3(x,y,z);
	}

}