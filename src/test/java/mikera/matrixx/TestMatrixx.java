package mikera.matrixx;

import static org.junit.Assert.*;

import mikera.matrixx.AMatrix;
import mikera.matrixx.Matrixx;
import mikera.transformz.ATransform;
import mikera.vectorz.AVector;
import mikera.vectorz.Vector;
import mikera.vectorz.Vector3;
import mikera.vectorz.Vectorz;

import org.junit.Test;

public class TestMatrixx {

	@Test
	public void testIdentity() {
		for (int i=1; i<10; i++) {
			AVector v=Vectorz.createLength(i);
			for (int j=0; j<v.length(); j++) {
				v.set(j,j+1.3);
			}
			
			AVector tv=v.clone();
			
			AMatrix m=Matrixx.createIdentityMatrix(i);
			
			m.transform(v, tv);
			
			assertTrue(v.approxEquals(tv));
		}
	}
	

	@Test
	public void testScale() {
		for (int i=1; i<10; i++) {
			AVector v=Vectorz.createLength(i);
			for (int j=0; j<v.length(); j++) {
				v.set(j,j+1.3);
			}
			
			AVector tv=v.clone();
			
			AMatrix m=Matrixx.createScaleMatrix(i,2.3);
			
			m.transform(v, tv);
			
			assertEquals(v.magnitude()*2.3,tv.magnitude(),0.0001);
		}
	}
	
	@Test
	public void testBasicDeterminant() {
		MatrixMN mmn=new MatrixMN(2,2);
		mmn.getRow(0).set(Vector.of(2,1));
		mmn.getRow(1).set(Vector.of(1,2));
		assertEquals(3.0,mmn.determinant(),0.0);
	}
	
	@Test
	public void testPermuteDeterminant() {
		MatrixMN mmn=new MatrixMN(3,3);
		mmn.set(0,1,1);
		mmn.set(1,0,1);
		mmn.set(2,2,1);
		assertEquals(-1.0,mmn.determinant(),0.0);
	}
	
	@Test
	public void testEquivalentDeterminant() {
		Matrix33 m33=new Matrix33();
		for (int i=0; i<3; i++) for (int j=0; j<3; j++) {
			m33.set(i,j,Math.random());
		}
		
		MatrixMN mmn=new MatrixMN(3,3);
		mmn.set(m33);
		
		for (int i=0; i<3; i++) for (int j=0; j<3; j++) {
			assertEquals(m33.get(i, j),mmn.get(i, j),0.0);
		}
		
		assertEquals(m33.determinant(),mmn.determinant(),0.00001);

	}

	
	@Test
	public void testCompoundTransform() {
		AVector v=Vector.of(1,2,3);
		
		AMatrix m1=Matrixx.createScaleMatrix(3, 2.0);
		AMatrix m2=Matrixx.createScaleMatrix(3, 1.5);
		ATransform ct = m2.compose(m1);
		
		assertTrue(Vector3.of(3,6,9).approxEquals(ct.transform(v)));
	}
	
	void doMutationTest(AMatrix m) {
		m=m.clone();
		AMatrix m2=m.clone();
		int rc=m.rowCount();
		int cc=m.columnCount();
		for (int i=0; i<rc; i++) {
			for (int j=0; j<cc; j++) {
				m2.set(i,j,m2.get(i,j)+1.3);
				assertEquals(m2.get(i,j),m2.getRow(i).get(j),0.0);
				assertNotSame(m.get(i,j),m2.get(i, j));
			}
		}
	}
	
	void doGenericTests(AMatrix m) {
		doMutationTest(m);
	}
	
	@Test public void genericTests() {
		// specialised 3x3 matrix
		Matrix33 m33=new Matrix33();
		doGenericTests(m33);
		
		// specialised Mx3 matrix
		MatrixM3 mm3=new MatrixM3(10);
		doGenericTests(mm3);
	
		// general M*N matrix
		MatrixMN mmn=new MatrixMN(6 ,7);
		doGenericTests(mmn);
		
		// 0x0 matrix should work
		mmn=new MatrixMN(0 ,0);
		doGenericTests(mmn);
	}
}