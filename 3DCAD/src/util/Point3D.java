package util;

public class Point3D {

	public double x, y, z;
	
	public Point3D(double x, double y, double z) {
		this.x = x;
		this.y = y;
		this.z = z;
	}
	
	public Point3D(Point3D p) {
		this.x = p.x;
		this.y = p.y;
		this.z = p.z;
	}
	
	public void addVector(Vector3D v) {
		this.x += v.x;
		this.y += v.y;
		this.z += v.z;
	}
	
}
