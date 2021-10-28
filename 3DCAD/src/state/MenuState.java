package state;

import java.awt.Color;
import java.awt.Desktop;
import java.awt.FileDialog;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Point;
import java.awt.event.KeyEvent;
import java.awt.event.MouseEvent;
import java.awt.event.MouseWheelEvent;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;

import javax.swing.JFrame;

import button.ButtonManager;
import button.Button;
import button.SliderButton;
import button.ToggleButton;
import objects.Mesh;
import util.Point3D;
import util.TextBox;
import util.Vector;
import util.Vector3D;

public class MenuState extends State{
	
	ButtonManager bm;
	
	Mesh mesh;
	
	public boolean forward = false;
	public boolean backward = false;
	public boolean left = false;
	public boolean right = false;
	public boolean up = false;
	public boolean down = false;
	
	public boolean zoomIn = false;
	public boolean zoomOut = false;
	
	public boolean sprint = false;
	
	public java.awt.Point mouse = new java.awt.Point(0, 0);

	public MenuState(StateManager gsm) {
		super(gsm);
		
		bm = new ButtonManager();
		
		bm.addToggleButton(new ToggleButton(20, 20, 100, 25, "Draw Textures"));
		bm.addToggleButton(new ToggleButton(20, 50, 100, 25, "Draw Wireframe"));
		bm.addToggleButton(new ToggleButton(20, 80, 100, 25, "Draw Vertices"));
		bm.addToggleButton(new ToggleButton(20, 110, 100, 25, "Edit Mode"));
		
		bm.addButton(new Button(20, 140, 100, 25, "Load Mesh"));
		
		bm.toggleButtons.get(0).setToggled(true);
		bm.toggleButtons.get(1).setToggled(false);
		bm.toggleButtons.get(2).setToggled(false);
		bm.toggleButtons.get(3).setToggled(false);
		
		mesh = new Mesh();
		
		ArrayList<Point3D> vertices = new ArrayList<Point3D>(Arrays.asList(
				new Point3D(-1, -1, -1),	//0
				new Point3D(-1, -1, 1),	//1
				new Point3D(-1, 1, -1),	//2
				new Point3D(-1, 1, 1),	//3
				new Point3D(1, -1, -1),	//4
				new Point3D(1, -1, 1),	//5
				new Point3D(1, 1, -1),	//6
				new Point3D(1, 1, 1)));	//7
		
		ArrayList<int[]> triangles = new ArrayList<int[]>(Arrays.asList(
				new int[] {0, 2, 6}, new int[] {0, 6, 4},	//south
				new int[] {4, 6, 7}, new int[] {4, 7, 5},	//east
				new int[] {5, 7, 3}, new int[] {5, 3, 1},	//north
				new int[] {1, 3, 2}, new int[] {1, 2, 0},	//west
				new int[] {2, 3, 7}, new int[] {2, 7, 6},	//top
				new int[] {1, 0, 4}, new int[] {1, 4, 5}));	//bottom
		
		
		
		ArrayList<util.Point[]> textures = new ArrayList<util.Point[]>(Arrays.asList(
				new util.Point[] {new util.Point(0, 1), new util.Point(0, 0), new util.Point(1, 0)}, new util.Point[] {new util.Point(0, 1), new util.Point(1, 0), new util.Point(1, 1)},
				new util.Point[] {new util.Point(0, 1), new util.Point(0, 0), new util.Point(1, 0)}, new util.Point[] {new util.Point(0, 1), new util.Point(1, 0), new util.Point(1, 1)},
				new util.Point[] {new util.Point(0, 1), new util.Point(0, 0), new util.Point(1, 0)}, new util.Point[] {new util.Point(0, 1), new util.Point(1, 0), new util.Point(1, 1)},
				new util.Point[] {new util.Point(0, 1), new util.Point(0, 0), new util.Point(1, 0)}, new util.Point[] {new util.Point(0, 1), new util.Point(1, 0), new util.Point(1, 1)},
				new util.Point[] {new util.Point(0, 1), new util.Point(0, 0), new util.Point(1, 0)}, new util.Point[] {new util.Point(0, 1), new util.Point(1, 0), new util.Point(1, 1)},
				new util.Point[] {new util.Point(0, 1), new util.Point(0, 0), new util.Point(1, 0)}, new util.Point[] {new util.Point(0, 1), new util.Point(1, 0), new util.Point(1, 1)}));
		
		/*
		triangles = new ArrayList<int[]>(Arrays.asList(
				new int[] {0, 6, 4}));
		
		textures = new ArrayList<util.Point[]>(Arrays.asList(
				new util.Point[] {new util.Point(0, 1), new util.Point(1, 0), new util.Point(1, 1)},
				new util.Point[] {new util.Point(0, 1), new util.Point(0, 0), new util.Point(1, 0)}, new util.Point[] {new util.Point(0, 1), new util.Point(1, 0), new util.Point(1, 1)},
				new util.Point[] {new util.Point(0, 1), new util.Point(0, 0), new util.Point(1, 0)}, new util.Point[] {new util.Point(0, 1), new util.Point(1, 0), new util.Point(1, 1)},
				new util.Point[] {new util.Point(0, 1), new util.Point(0, 0), new util.Point(1, 0)}, new util.Point[] {new util.Point(0, 1), new util.Point(1, 0), new util.Point(1, 1)},
				new util.Point[] {new util.Point(0, 1), new util.Point(0, 0), new util.Point(1, 0)}, new util.Point[] {new util.Point(0, 1), new util.Point(1, 0), new util.Point(1, 1)},
				new util.Point[] {new util.Point(0, 1), new util.Point(0, 0), new util.Point(1, 0)}, new util.Point[] {new util.Point(0, 1), new util.Point(1, 0), new util.Point(1, 1)}));
		*/
		
		
		//hard coded cube mesh
        mesh.vertices = vertices;
        mesh.triangles = triangles;
        mesh.texturePoints = textures;

//        String directory = System.getProperty("user.dir");
//        String filepath = directory + "\\res\\Combined\\AK47 Gold Dragon.obj";
//        System.out.println("FILEPATH: " + filepath);
//        System.out.println("Working Directory = " + System.getProperty("user.dir"));
//        mesh.readMeshFromFile(directory + "\\res\\Combined\\AK47 Gold Dragon.obj", true);
		
		
		//old initializer code
//		cube = new Mesh(new ArrayList<Triangle>(Arrays.asList(
//				//south
//				new Triangle(new Point3D(0, 0, 0), new Point3D(0, 1, 0), new Point3D(1, 1, 0)),
//				new Triangle(new Point3D(0, 0, 0), new Point3D(1, 1, 0), new Point3D(1, 0, 0)),
//				//east
//				new Triangle(new Point3D(1, 0, 0), new Point3D(1, 1, 0), new Point3D(1, 1, 1)),
//				new Triangle(new Point3D(1, 0, 0), new Point3D(1, 1, 1), new Point3D(1, 0, 1)),
//				//north
//				new Triangle(new Point3D(1, 0, 1), new Point3D(1, 1, 1), new Point3D(0, 1, 1)),
//				new Triangle(new Point3D(1, 0, 1), new Point3D(0, 1, 1), new Point3D(0, 0, 1)),
//				//west
//				new Triangle(new Point3D(0, 0, 1), new Point3D(0, 1, 1), new Point3D(0, 1, 0)),
//				new Triangle(new Point3D(0, 0, 1), new Point3D(0, 1, 0), new Point3D(0, 0, 0)),
//				//top
//				new Triangle(new Point3D(0, 1, 0), new Point3D(0, 1, 1), new Point3D(1, 1, 1)),
//				new Triangle(new Point3D(0, 1, 0), new Point3D(1, 1, 1), new Point3D(1, 1, 0)),
//				//bottom
//				new Triangle(new Point3D(0, 0, 1), new Point3D(0, 0, 0), new Point3D(1, 0, 0)),
//				new Triangle(new Point3D(0, 0, 1), new Point3D(1, 0, 0), new Point3D(1, 0, 1))
//				)));
		
		//need to specify absolute file path. This is so that we can load any file on the computer
		//mesh.readMeshFromFile("C:\\Users\\aweso\\eclipse-workspace\\3DCAD\\res\\Combined\\AK47 Gold Dragon.obj", true);
	}

	@Override
	public void init() {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void tick(Point mouse) {
		
		this.mouse = mouse;
		
		mesh.drawTextures = bm.toggleButtons.get(0).getToggled();
		mesh.drawWireframe = bm.toggleButtons.get(1).getToggled();
		mesh.drawVertices = bm.toggleButtons.get(2).getToggled();
		mesh.editMode = bm.toggleButtons.get(3).getToggled();
		
		double moveSpeed = sprint? 3 : 0.5;
		
		double zoomFactor = 1.05;
		
		Vector3D forwardDir = new Vector3D(mesh.vLookDir);
		forwardDir.setMagnitude(moveSpeed);
		
		Vector left = new Vector(forwardDir.x, forwardDir.z);
		left.rotateCounterClockwise(Math.toRadians(90));
		left.setMagnitude(moveSpeed);
		
		if(this.left) {
			mesh.camera.addVector(new Vector3D(left.x, 0, left.y));
		}
		if(this.right) {
			left.setMagnitude(-moveSpeed);
			mesh.camera.addVector(new Vector3D(left.x, 0, left.y));
		}
		if(this.up) {
			mesh.camera.y -= moveSpeed;
		}
		if(this.down) {
			mesh.camera.y += moveSpeed;
		}
		
		if(this.forward) {
			mesh.camera.addVector(forwardDir);
		}
		
		if(this.backward) {
			forwardDir.setMagnitude(-moveSpeed);
			mesh.camera.addVector(forwardDir);
		}
		
		if(this.zoomIn) {
			for(Point3D p : mesh.vertices) {
				p.x *= zoomFactor;
				p.y *= zoomFactor;
				p.z *= zoomFactor;
			}
		}
		if(this.zoomOut) {
			for(Point3D p : mesh.vertices) {
				p.x /= zoomFactor;
				p.y /= zoomFactor;
				p.z /= zoomFactor;
			}
		}

		bm.tick(mouse);
		mesh.tick(mouse);
		
	}

	@Override
	public void draw(Graphics g) {
		
		
		mesh.draw(g, mouse);
		bm.draw(g);

	}

	@Override
	public void keyPressed(int k) {
		if(k == KeyEvent.VK_W) {
			this.forward = true;
		}
		else if(k == KeyEvent.VK_S) {
			this.backward = true;
		}
		else if(k == KeyEvent.VK_A) {
			this.left = true;
		}
		else if(k == KeyEvent.VK_D) {
			this.right = true;
		}
		else if(k == KeyEvent.VK_SHIFT) {
			this.down = true;
		}
		else if(k == KeyEvent.VK_SPACE) {
			this.up = true;
		}
		else if(k == KeyEvent.VK_Z) {
			this.zoomIn = true;
		}
		else if(k == KeyEvent.VK_X) {
			this.zoomOut = true;
		}
		else if(k == KeyEvent.VK_CONTROL) {
			this.sprint = true;
		}
	}

	@Override
	public void keyReleased(int k) {
		if(k == KeyEvent.VK_W) {
			this.forward = false;
		}
		else if(k == KeyEvent.VK_S) {
			this.backward = false;
		}
		else if(k == KeyEvent.VK_A) {
			this.left = false;
		}
		else if(k == KeyEvent.VK_D) {
			this.right = false;
		}
		else if(k == KeyEvent.VK_SHIFT) {
			this.down = false;
		}
		else if(k == KeyEvent.VK_SPACE) {
			this.up = false;
		}
		else if(k == KeyEvent.VK_Z) {
			this.zoomIn = false;
		}
		else if(k == KeyEvent.VK_X) {
			this.zoomOut = false;
		}
		else if(k == KeyEvent.VK_CONTROL) {
			this.sprint = false;
		}
	}

	@Override
	public void keyTyped(int k) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void mouseClicked(MouseEvent arg0) {

		String which = bm.buttonClicked(arg0);
		
		if(which != null) {
			switch(which) {
			case "Load Mesh":
				
				FileDialog fd = new FileDialog(new JFrame());
				fd.setVisible(true);
				File[] f = fd.getFiles();
				if(f.length > 0){
				    System.out.println("LOADING MESH: " + fd.getFiles()[0].getAbsolutePath());
				}
				this.mesh.readMeshFromFile(fd.getFiles()[0].getAbsolutePath(), true);
				this.mesh.camera = new Point3D(0, 0, -20);
				this.mesh.vLookDir = new Vector3D(0, 0, 1);
				break;
			}
		}
		
	}

	@Override
	public void mouseEntered(MouseEvent arg0) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void mouseExited(MouseEvent arg0) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void mousePressed(MouseEvent arg0) {
		
		bm.pressed(arg0);
		mesh.mousePressed(arg0);

	}

	@Override
	public void mouseReleased(MouseEvent arg0) {
		
		bm.mouseReleased();
		mesh.mouseReleased();
	}

	@Override
	public void mouseWheelMoved(MouseWheelEvent arg0) {
		// TODO Auto-generated method stub
		
	}

}
