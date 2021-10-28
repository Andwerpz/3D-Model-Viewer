package util;

public class Point {

	public double x, y;
	
	public Point(double x, double y) {
		this.x = x;
		this.y = y;
	}
	
	public Point(Point a) {
		this.x = a.x;
		this.y = a.y;
	}
	
	public Point(Point a, Vector v) {
		this.x = a.x + v.x;
		this.y = a.y + v.y;
	}
	
	public void addVector(Vector v) {
		this.x += v.x;
		this.y += v.y;
	}
	
	public void setLocation(double x, double y) {
		this.x = x;
		this.y = y;
	}
	
}
