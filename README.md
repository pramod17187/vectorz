# vectorz

Fast double-precision vector and matrix maths library for Java.

This library is designed for use in games, simulations, raytracers, machine learning etc. 
where fast vector maths is important. 

Vectorz can do over *1 billion* 3D vector operations per second on a single thread.

Vectorz is reasonably mature, battle tested and being used in production applications. The API is still evolving however as new features get added so you can expect a few minor changes.

[![Build Status](https://secure.travis-ci.org/mikera/vectorz.png)](http://travis-ci.org/mikera/vectorz)

### Documentation

See the [Vectorz Wiki](https://github.com/mikera/vectorz/wiki)

### Example usage

```Java
    Vector3 v=Vector3.of(1.0,2.0,3.0);		
    v.normalise();                       // normalise v to a unit vector
    		
    Vector3 d=Vector3.of(10.0,0.0,0.0);		
    d.addMultiple(v, 5.0);               // d = d + (v * 5)
    
	Matrix33 m=Matrixx.createXAxisRotationMatrix(Math.PI);
	Vector3 rotated=m.transform(d);      // rotate 180 degrees around x axis	    
```

### Key features

 - Supports `double` types vectors of arbitrary size
 - Vector values are mutable, enabling high performance algorithms
 - Support for any size matrices, including higher dimensional (NDArray) matrices
 - Ability to create lightweight "reference" vectors (e.g. to access subranges of other vectors)
 - Library of useful mathematical functions on vectors
 - Vectors have lots of utility functionality implemented - Cloneable, Serializable, Comparable etc.
 - Various specialised vector/matrix types (e.g. identity matrices, diagonal matrices)
 - Support for affine and matrix transformations
 - Operator system provides composable operators that can be applied to array elements
 - Input / output of vectors and matrices in readable edn format

Vectorz is deigned to allow the maximum performance possible for vector maths on the JVM.

This focus has driven a number of important design decisions:

 - Specialised primitive-backed small vectors (1,2,3 and 4 dimensions) and matrices (2x2, 3x3 and M*3)
 - Abstract base classes preferred over interfaces to allow more efficient method dispatch
 - Multiple types of vector are provided for optimised performance in special cases
 - Hard-coded fast paths for most common 2D and 3D operations
 - Vector operations are generally not thread safe, by design
 - Concrete classes are generally final
 
If you have a common case that isn't yet well optimised then please post an issue - the aim is to make all common operations efficient as efficient as they can possibly be on the JVM.