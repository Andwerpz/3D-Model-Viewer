package objects;

import java.awt.Color;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.Point;
import java.awt.Polygon;
import java.awt.event.MouseEvent;
import java.awt.image.BufferedImage;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.StringTokenizer;

import javax.imageio.ImageIO;

import main.MainPanel;
import util.GraphicsTools;
import util.MathTools;
import util.Point3D;
import util.Vector3D;

public class Mesh {
	
	public static int customResWidth = 800;
	public static int customResHeight = 600;
	
	public ArrayList<Point3D> vertices;
	
	//this array is not the same as the triangles array used in the render process.
	public ArrayList<int[]> triangles;	//we define triangles as pointers to the vertices array so that when we move a vertice, we don't have to keep track of where it is with other triangles
	public ArrayList<util.Point[]> texturePoints;	//for each triangle, give the 2d coordinates on the texture for each vertice.
	public ArrayList<Integer> whichTexture;	//maps the textures to the triangles
	public HashMap<String, Integer> textureMap;	//gives each texture name a mapping to the textures array list.
	
	public ArrayList<int[][]> textures;	//x, y, rgb
	
	public int[][] errorTexture = new int[][] {
		{Color.pink.getRGB(), Color.black.getRGB(), Color.pink.getRGB()},
		{Color.black.getRGB(), Color.pink.getRGB(), Color.black.getRGB()},
		{Color.pink.getRGB(), Color.black.getRGB(), Color.pink.getRGB()}};
	
	public int[][] pixelColor;
	public double[][] pixelDepth;
	
	public ArrayList<Point3D> projectedVertices;	//used when giving the user the ability to drag around vertices
	public ArrayList<Polygon> projectedTriangles;
	
	public boolean editMode = false;	//makes it so that when you're pressing the mouse to edit, you don't accidentally move the camera
	
	public int selectedTriangle;
	
	public boolean landscapeShrinkMode = false;
	public boolean landscapeEnlargeMode = true;
	public double landscapeEditSpeed = 0.05d;
	
	public boolean extrudeMode = true;	//if a triangle is selected, extrudes the triangle in the direction of its normal
	//for now it will just move all the points corresponding to the triangle in the direction of its normal
	
	public double xRot, yRot, zRot;
	
	public boolean drawVertices = false;
	public int drawnVerticeSize = 4;
	
	public boolean drawWireframe = false;
	
	public boolean drawTextures = true;
	public boolean drawErrorTexture = true;	//if true, textures that fail to load will have a pink and black pixel pattern
	
	public Point mouse = new Point(0, 0);
	public boolean mousePressed = false;
	
	public Point3D camera = new Point3D(0, 0, -20);
	public Vector3D vLookDir = new Vector3D(0, 0.00000001, 1);	//long double is for correcting first frame / by zero errors
	public Vector3D vUp = new Vector3D(0, 1, 0);
	
	public Mesh() {
		this.vertices = new ArrayList<Point3D>();
		this.triangles = new ArrayList<int[]>();
		this.texturePoints = new ArrayList<util.Point[]>();
		this.whichTexture = new ArrayList<Integer>();
		
		this.projectedVertices = new ArrayList<Point3D>();
		this.projectedTriangles = new ArrayList<Polygon>();
		
		xRot = 0; yRot = 0; zRot = 0;
		
		vLookDir.normalize();
		
		selectedTriangle = -1;
		
		textures = new ArrayList<int[][]>();
		textures.add(new int[][] {{255 + (255 << 8) + (255 << 16)}});
	}
	
	public void readMaterialFromFile(String filename, String absolutePath) {
		
		textures = new ArrayList<int[][]>();
		textureMap = new HashMap<String, Integer>();
		
		BufferedReader fin = null;
		File file = null;
		
		try {
			
			file = new File(absolutePath + filename);
			
			fin = new BufferedReader(new FileReader(file));
			
			String line = fin.readLine();
			
			while(line != null) {
				
				boolean noTexture = false;
				
				StringTokenizer st = new StringTokenizer(line);
				if(!st.hasMoreTokens()) {
					line = fin.readLine();
					continue;
				}
				String type = st.nextToken();
				
				if(type.equals("newmtl")) {
					String nextMtl = st.nextToken();
					textureMap.put(nextMtl, textureMap.size());
					
					st = new StringTokenizer(fin.readLine());
					String textureFile = st.nextToken();
					while(!textureFile.equals("map_Kd")) {
						if(textureFile.equals("newmtl")) {
							noTexture = true;
							break;
						}
						while(!st.hasMoreTokens()) {
							st = new StringTokenizer(fin.readLine());
						}
						st = new StringTokenizer(fin.readLine());
						while(!st.hasMoreTokens()) {
							st = new StringTokenizer(fin.readLine());
						}
						textureFile = st.nextToken();
					}
					if(noTexture) {
						textures.add(new int[][] {{255 + (255 << 8) + (255 << 16)}});
						line = textureFile + " " + st.nextToken();
						continue;
					}
					else {
						textureFile = st.nextToken();
						System.out.print("LOADING TEXTURE: " + textureFile);
						this.readTextureFromFile(textureFile, absolutePath);
					}
				}
				line = fin.readLine();
			}
			
		} catch(IOException e) {
			
		}
		
		
	}
	
	public void readTextureFromFile(String filename, String absolutePath) {
		
		BufferedReader fin = null;
		File file = null;
		
		try {
			
			file = new File(absolutePath + filename);
			
			BufferedImage image = ImageIO.read(file);
			
			int width = image.getWidth();
			int height = image.getHeight();
			
			int[][] texture = new int[height][width];
			
			for(int i = 0; i < height; i++) {
				for(int j = 0; j < width; j++) {
					int color = image.getRGB(j, height - i - 1);
					texture[i][j] = color;
				}
			}
			
			textures.add(texture);
			System.out.println(" SUCCESS");
			
		} catch(IOException e) {
			System.out.println(" FAILED");
			textures.add(new int[][] {{-1}});
		}
		
	}
	
	public void readMeshFromFile(String filename, boolean loadMaterial) {
		
		int curMtl = -1;
		
		BufferedReader fin = null;
		File file = null;
		
		String absolutePath = filename;
		
		while(absolutePath.charAt(absolutePath.length() - 1) != '\\'){
			absolutePath = absolutePath.substring(0, absolutePath.length() - 1);
		}
		
		try {
			
			file = new File(filename);
			
			fin = new BufferedReader(new FileReader(file));
			this.vertices = new ArrayList<Point3D>();
			this.triangles = new ArrayList<int[]>();
			this.texturePoints = new ArrayList<util.Point[]>();
			this.whichTexture = new ArrayList<Integer>();
			ArrayList<util.Point> readTexturePoints = new ArrayList<util.Point>();
			
			String line = fin.readLine();
			
			while(line != null) {
				
				StringTokenizer st = new StringTokenizer(line);
				
				if(st.hasMoreTokens()) {
					String type = st.nextToken();
					//System.out.println(type);
					
					if(type.equals("v")) {
						double x = Double.parseDouble(st.nextToken()) * -1;
						double y = Double.parseDouble(st.nextToken()) * -1;
						double z = Double.parseDouble(st.nextToken());
						
						vertices.add(new Point3D(x, y, z));
					}
					else if(type.equals("vt")) {
						double u = Double.parseDouble(st.nextToken());
						double v = Double.parseDouble(st.nextToken());
						
						//double w = Double.parseDouble(st.nextToken());
						
						readTexturePoints.add(new util.Point(u, v));
					}
					else if(type.equals("mtllib") && loadMaterial) {
						String mtlLib = "";
						while(st.hasMoreTokens()){
							mtlLib += st.nextToken();
							if(st.hasMoreTokens()) {
								mtlLib += " ";
							}
						}
						System.out.println("LOADING MATERIALS: " + mtlLib);
						this.readMaterialFromFile(mtlLib, absolutePath);
					}
					else if(type.equals("usemtl") && loadMaterial) {
						String mtl = st.nextToken();
						curMtl = this.textureMap.get(mtl);
					}
					else if(type.equals("f")) {
						String nextLine = "";
						while(st.hasMoreTokens()) {
							nextLine += st.nextToken();
							if(st.hasMoreTokens()) {
								nextLine += "/";
							}
						}	
						
						ArrayList<String> nums = new ArrayList<String>();
						nums.add("");
						for(int i = 0; i < nextLine.length(); i++) {
							if(nextLine.charAt(i) == '/') {
								nums.add("");
							}
							else {
								nums.set(nums.size() - 1, nums.get(nums.size() - 1) + nextLine.charAt(i));
							}
						}
						
						int multiple = nums.size() / 3;
						
						int[] nextVertices = new int[3];
						util.Point[] nextTexturePoints = new util.Point[] {new util.Point(0, 0), new util.Point(0, 1), new util.Point(1, 1)};
						
						for(int i = 0; i < nums.size(); i++) {
							if(i % multiple == 0) {
								nextVertices[i / multiple] = Integer.parseInt(nums.get(i)) - 1;
							}
							else if(i % multiple == 1) {
								int nextPoint = Integer.parseInt(nums.get(i)) - 1;
								nextTexturePoints[(i - 1) / multiple] = readTexturePoints.get(nextPoint);	
							}
							//System.out.println(i);
						}
						
						triangles.add(nextVertices);
						texturePoints.add(nextTexturePoints);
						whichTexture.add(curMtl);
					}
				}
				
				line = fin.readLine();
				
			}
			System.out.println("DONE");
			fin.close();
			
		} catch (IOException e) {
			System.out.println("MESH FAILED");
			System.exit(0);
		}
		
	}
	
	public void tick(Point mouse) {
		
		//tick code moved to draw loop

	}
	
	public void draw(Graphics g, Point mouse) {
		
		//I put the tick code in the draw loop
		//this is to avoid tears between the vertices of the mesh due to asynchronous updates of the camera.
		double xDiff = mouse.x - this.mouse.x;
		double yDiff = mouse.y - this.mouse.y;
		
		if(mousePressed) {
			if(editMode) {
				if(this.selectedTriangle != -1) {
				
					int[] t = triangles.get(selectedTriangle);
					Vector3D normal = this.calculateNormal(t);
					
					if(this.landscapeEnlargeMode) {
						normal.setMagnitude(-this.landscapeEditSpeed);
						for(int i = 0; i < t.length; i++) {
							int next = t[i];
							vertices.get(next).addVector(normal);
						}
					}
					else if(this.landscapeShrinkMode) {
						normal.setMagnitude(this.landscapeEditSpeed);
						for(int i = 0; i < t.length; i++) {
							int next = t[i];
							vertices.get(next).addVector(normal);
						}
					}
					else if(this.extrudeMode){
						normal.setMagnitude(xDiff / 20);
						for(int i = 0; i < t.length; i++) {
							int next = t[i];
							vertices.get(next).addVector(normal);
						}
					}
				}
			}
			
			else {
				this.xRot += Math.toRadians(yDiff / 3);
				this.yRot += Math.toRadians(xDiff / 3);
			}
		}
		
		this.mouse = new Point(mouse.x, mouse.y);
	
		
		projectedVertices = new ArrayList<Point3D>();
		
		//preparing the camera transforms
		//we do the camera transforms before we go into projection space
		
		Vector3D vUp = new Vector3D(0, 1, 0);
		
		vLookDir = new Vector3D(0, 0, 1);
		vLookDir.rotateY(yRot);
		
		Vector3D vTarget = new Vector3D(camera.x, camera.y, camera.z);
		vTarget.addVector(vLookDir);
		
		double[][] matCamera = MathTools.matrixPointAt(vTarget, camera, vUp);
		
		//view matrix from camera
		double[][] matView = MathTools.invertTransformMatrix(matCamera);
		
		
		//initializing the lighting vector
		Vector3D light = new Vector3D(0, 0, 1);
//		light.rotateX(-xRot);
//		light.rotateY(yRot);
		light.normalize();
		
		
		//prepping the render list
		ArrayList<Triangle> toRender = new ArrayList<Triangle>();
		ArrayList<Point3D> viewedPoints = new ArrayList<Point3D>();
		ArrayList<Point3D> translatedPoints = new ArrayList<Point3D>();
		
		//rotate, translate, and project the vertices
		
		for(int i = 0; i < vertices.size(); i++) {
			
			//pretty much deprecated. Rotates points around the origin
			Point3D rotatedPoint = vertices.get(i);
			
			Point3D translatedPoint = new Point3D(rotatedPoint);
			translatedPoint.z += 20;	//got the camera set up :))
			
			translatedPoints.add(translatedPoint);
			
			//transform vertices based on the view matrix
			
			Point3D viewedPoint = MathTools.multiplyMatrixVector(matView, translatedPoint, new double[] {0});
			viewedPoint = MathTools.rotatePoint(viewedPoint, xRot, 0, 0);
			
			viewedPoints.add(viewedPoint);
			
			projectedVertices.add(MathTools.projectPoint(viewedPoint, new double[] {0}));
		}
		
		//figure out which triangles are visible from the camera. If they are, put them into the rendering list
		
		for(int i = 0; i < triangles.size(); i++) {
			int[] t = triangles.get(i);
			int a = t[0]; int b = t[1]; int c = t[2];
			
			//first calculate the dot product between the normal of the triangle, and the vector from the center of the triangle to the camera.
			//if the dot product is greater than 0, 
			
			Point3D pa = new Point3D(translatedPoints.get(t[0]));
			Point3D pb = new Point3D(translatedPoints.get(t[1]));
			Point3D pc = new Point3D(translatedPoints.get(t[2]));
			
			Point3D avg = new Point3D((pa.x + pb.x + pc.x) / 3d, (pa.y + pb.y + pc.y) / 3d, (pa.z + pb.z + pc.z) / 3d);
			
			Vector3D v1 = new Vector3D(pa, pb);
			Vector3D v2 = new Vector3D(pb, pc);
			
			Vector3D normal = MathTools.crossProduct(v1, v2);
			Vector3D triToCamera = new Vector3D(avg, camera);
			
			normal.normalize();
			triToCamera.normalize();
			
			if(MathTools.dotProduct3D(normal, triToCamera) > 0d) {	//just checking if the normal is facing the camera.
				
				//add triangle to the render pipeline
				Triangle next = new Triangle();
				next.vertices[0] = new Point3D(viewedPoints.get(a));
				next.vertices[1] = new Point3D(viewedPoints.get(b));
				next.vertices[2] = new Point3D(viewedPoints.get(c));
				
				//saving depth information before it's crushed in projection.
				next.w[0] = next.vertices[0].z;
				next.w[1] = next.vertices[1].z;
				next.w[2] = next.vertices[2].z;
				
				next.texturePoints[0] = new util.Point(this.texturePoints.get(i)[0]);
				next.texturePoints[1] = new util.Point(this.texturePoints.get(i)[1]);
				next.texturePoints[2] = new util.Point(this.texturePoints.get(i)[2]);
				
				//calculating shading based on normal
				
				//now we calculate the real space normal. We use this one for lighting.
				//since we don't want to rotate the actual saved vertices, we just make a copy.
				
				Vector3D realNormal = this.calculateNormal(t);
				
				//now that we have the normal, we can calculate the shading by using the cosine rule.
				
				realNormal.normalize();
				
				double dot = MathTools.dotProduct3D(realNormal, light);
				
				int color = (int) (255d * dot);
				color = Math.min(color, 255); color = Math.max(color, 51);
				
				next.color = color;	//light based coloring
				next.index = i;	//important for highlighting the selected triangle
				
				//saving which texture belongs to which triangle
				if(i >= this.whichTexture.size()) {
					next.whichTexture = -1;
				}
				else {
					next.whichTexture = this.whichTexture.get(i);
				}
				
				
				toRender.add(next);
			}
			
		}
		
		
		//clipping the to be rendered triangles
		ArrayList<Triangle> clippedTriangles = new ArrayList<Triangle>();
		clippedTriangles.addAll(toRender);
		
		//first, clip triangles that are behind the camera. This must be done in real space.
		ArrayList<Triangle> nextTriangles = new ArrayList<Triangle>();
		for(int i = 0; i < clippedTriangles.size(); i++) {
			Triangle cur = clippedTriangles.get(i);
			ArrayList<util.Point[]> nextTex = new ArrayList<util.Point[]>();
			ArrayList<Point3D[]> next = new ArrayList<Point3D[]>();
			
			next = MathTools.triangleClipAgainstPlane(new Point3D(0, 0, 0.1), new Vector3D(0, 0, 1), cur.vertices, cur.texturePoints, nextTex, new double[] {0, 0, 0}, new ArrayList<double[]>());
			
			for(int j = 0; j < next.size(); j++) {
				Triangle add = new Triangle();
				add.vertices = next.get(j);
				add.texturePoints = nextTex.get(j);
				add.color = cur.color;
				add.index = cur.index;
				add.whichTexture = cur.whichTexture;
				
//				add.w[0] = next.get(j)[0].z;
//				add.w[1] = next.get(j)[1].z;
//				add.w[2] = next.get(j)[2].z;
				
				nextTriangles.add(add);
			}
		}
		clippedTriangles.clear();
		clippedTriangles.addAll(nextTriangles);
		
		//projecting the triangles onto the x, y plane
		for(int tri = 0; tri < clippedTriangles.size(); tri++) {
			Triangle t = clippedTriangles.get(tri);
			
			//projecting and scaling the clipped triangles, as well as saving the depth information
			for(int i = 0; i < 3; i++) {
				double[] wOut = new double[] {0};
				t.vertices[i] = MathTools.projectPoint(t.vertices[i], wOut);
				t.vertices[i] = MathTools.scalePoint(t.vertices[i]);
				t.w[i] = wOut[0];
			}
			
			//correcting texture information for perspective
			t.texturePoints[0].x /= t.w[0];
			t.texturePoints[1].x /= t.w[1];
			t.texturePoints[2].x /= t.w[2];
			
			t.texturePoints[0].y /= t.w[0];
			t.texturePoints[1].y /= t.w[1];
			t.texturePoints[2].y /= t.w[2];
			
			t.w[0] = 1d / t.w[0];
			t.w[1] = 1d / t.w[1];
			t.w[2] = 1d / t.w[2];
		}
		
		
		//next, clip triangles that go off screen. This is done in projected and scaled space.
		//since we already projected and scaled the triangles, all the triangles have effectively the same z value. We can treat them as 2d triangles now
		
		for(int p = 0; p < 4; p++) {
			nextTriangles.clear();
			for(int i = 0; i < clippedTriangles.size(); i++) {
				Triangle cur = clippedTriangles.get(i);
				ArrayList<util.Point[]> nextTex = new ArrayList<util.Point[]>();
				ArrayList<Point3D[]> next = new ArrayList<Point3D[]>();
				ArrayList<double[]> outW = new ArrayList<double[]>();
				
				switch(p) {
				case 0:
					next = MathTools.triangleClipAgainstPlane(new Point3D(0, Mesh.customResHeight + 10, 0), new Vector3D(0, -1, 0), cur.vertices, cur.texturePoints, nextTex, cur.w, outW);
					break;
					
				case 1:
					next = MathTools.triangleClipAgainstPlane(new Point3D(0, 0, 0), new Vector3D(0, 1, 0), cur.vertices, cur.texturePoints, nextTex, cur.w, outW);
					break;
					
				case 2:
					next = MathTools.triangleClipAgainstPlane(new Point3D(Mesh.customResWidth + 10, 0, 0), new Vector3D(-1, 0, 0), cur.vertices, cur.texturePoints, nextTex, cur.w, outW);
					break;
					
				case 3:
					next = MathTools.triangleClipAgainstPlane(new Point3D(0, 0, 0), new Vector3D(1, 0, 0), cur.vertices, cur.texturePoints, nextTex, cur.w, outW);
					break;
				}
				for(int j = 0; j < next.size(); j++) {
					Triangle add = new Triangle();
					add.vertices = next.get(j);
					add.texturePoints = nextTex.get(j);
					add.color = cur.color;
					add.index = cur.index;
					add.w = outW.get(j);
					add.whichTexture = cur.whichTexture;
					nextTriangles.add(add);
				}
			}
			clippedTriangles.clear();
			clippedTriangles.addAll(nextTriangles);
		}
		
		//setting the z buffer for each triangle
		for(Triangle t : clippedTriangles) {
			t.zBuffer = (t.w[0] + t.w[1] + t.w[2]) / 3;
		}
	
		
		//sorting projected triangles based on zBuffer
		//this is also where we would return the triangles to the mesh manager
		clippedTriangles.sort((a, b) -> Double.compare(a.zBuffer, b.zBuffer));
		
		
		int curSelectedTriangle = -1;
		this.projectedTriangles = new ArrayList<Polygon>();
		
		//per pixel depth buffer
		pixelColor = new int[customResHeight][customResWidth];
		pixelDepth = new double[customResHeight][customResWidth];
		
		//rendering triangles
		for(Triangle t : clippedTriangles) {
			
			int[] x = new int[3];
			int[] y = new int[3];
			for(int i = 0; i < 3; i++) {

				double scaledX = t.vertices[i].x;
				double scaledY = t.vertices[i].y;
				
				x[i] = (int) scaledX;
				y[i] = (int) scaledY;

			}
			
			Polygon p = new Polygon(x, y, x.length);
			
			if(p.contains(mouse)) {
				curSelectedTriangle = t.index;
			}
			
			if(this.drawTextures) {
				this.drawTexturedTriangle(g, t);
				if(p.contains(mouse)) {
					Graphics2D g2 = (Graphics2D) g;
					g2.setComposite(GraphicsTools.makeComposite(0.25));
					g2.setColor(Color.white);
					g2.fillPolygon(p);
					g2.setComposite(GraphicsTools.makeComposite(1));
					g2.drawPolygon(p);
				}
			}
			else {
				
				g.setColor(new Color(t.color, t.color, t.color));
				g.fillPolygon(new java.awt.Polygon(x, y, x.length));
				
				//draws a green hue on the polygon if selected
				if(p.contains(mouse)) {
					
					Graphics2D g2 = (Graphics2D) g;
					g2.setComposite(GraphicsTools.makeComposite(0.25));
					g2.setColor(Color.green);
					g2.fillPolygon(p);
					g2.setComposite(GraphicsTools.makeComposite(1));
					g2.drawPolygon(p);
					
				}
			}
			
			projectedTriangles.add(p);
			
		}
		
		if(this.drawTextures) {
			//image that textures will render on
			//we want to set the pixels on an image since drawing all of them at once is faster than doing them one by one
			BufferedImage img = new BufferedImage(customResWidth, customResHeight, 1);
	
			//setting image pixel colors
			if(this.drawTextures) {
				for(int i = 0; i < customResHeight; i++) {
					for(int j = 0; j < customResWidth; j++) {
						
						
						if(this.pixelDepth[i][j] != 0) {
							img.setRGB(j, i, pixelColor[i][j]);
						}
						else {
							//background color
							img.setRGB(j, i, (255 << 16) + (255 << 8) + 255);
						}
					}
				}
			}
			
			//drawing image
			g.drawImage(img, 0, 0, MainPanel.WIDTH, MainPanel.HEIGHT, null);
		}
		
		if(this.drawWireframe) {
			for(Triangle t : clippedTriangles) {

				int[] x = new int[3];
				int[] y = new int[3];
				for(int i = 0; i < 3; i++) {
					double scaledX = t.vertices[i].x;
					double scaledY = t.vertices[i].y;
					
					x[i] = (int) scaledX;
					y[i] = (int) scaledY;
				}
				
				Polygon p = new Polygon(x, y, x.length);

				g.setColor(Color.GREEN);
				
				if(p.contains(mouse)) {
					g.fillPolygon(p);
				}
				else {
					g.drawPolygon(new java.awt.Polygon(x, y, x.length));
				}	
				
			}
		}
		
		
		if(editMode) {
			if(this.landscapeEnlargeMode || this.landscapeShrinkMode) {
				this.selectedTriangle = curSelectedTriangle;
			}
			else if(extrudeMode) {
				//don't do anything
			}
		}
		else {
			this.selectedTriangle = curSelectedTriangle;
		}
		
		if(drawVertices) {
			g.setColor(Color.green);
			for(int i = 0; i < projectedVertices.size(); i++) {
				
				if(viewedPoints.get(i).z < 0.1) {
					continue;
				}
				
				Point3D p = projectedVertices.get(i);
				
				if(p.z < 0.1) {
					continue;
				}
				
				double scaledX = p.x;
				double scaledY = p.y;
				
				scaledX += 1; scaledY += 1;
				
				scaledX *= (double) (0.5 * (double) Mesh.customResWidth);
				scaledY *= (double) (0.5 * (double) Mesh.customResHeight);
				
				int xLow = (int) (scaledX - drawnVerticeSize / 2);
				int yLow = (int) (scaledY - drawnVerticeSize / 2);
				
				if(xLow <= mouse.x && mouse.x <= xLow + drawnVerticeSize && yLow <= mouse.y && mouse.y <= yLow + drawnVerticeSize) {
					g.setColor(Color.blue);
				}
				else {
					g.setColor(Color.green);
				}
				
				g.drawRect((int) (scaledX - drawnVerticeSize / 2), (int) (scaledY - drawnVerticeSize / 2), drawnVerticeSize, drawnVerticeSize);
			}
		}
		
	}
	
	//takes in a projected and scaled triangle and renders it using a texture
	
	public void drawTexturedTriangle(Graphics g, Triangle tri) {
		
		int wt = tri.whichTexture;	//wt short for which texture
		
		Point3D a = tri.vertices[0];	util.Point at = tri.texturePoints[0];
		Point3D b = tri.vertices[1];	util.Point bt = tri.texturePoints[1];
		Point3D c = tri.vertices[2];	util.Point ct = tri.texturePoints[2];
		
		double w1 = tri.w[0];
		double w2 = tri.w[1];
		double w3 = tri.w[2];
		
		//sorting points based on their y position
		
		if(b.y < a.y) {
			//System.out.println("SAWP");
			Point3D temp = new Point3D(b);
			b = new Point3D(a);
			a = new Point3D(temp);
			
			util.Point temp2 = new util.Point(bt);
			bt = new util.Point(at);
			at = new util.Point(temp2);
			
			double temp3 = w2;
			w2 = w1;
			w1 = temp3;
		}
		if(c.y < a.y) {
			//System.out.println("APWA");
			Point3D temp = new Point3D(c);
			c = new Point3D(a);
			a = new Point3D(temp);
			
			util.Point temp2 = new util.Point(ct);
			ct = new util.Point(at);
			at = new util.Point(temp2);
			
			double temp3 = w3;
			w3 = w1;
			w1 = temp3;
		}
		if(c.y < b.y) {
			//System.out.println("SOAW");
			Point3D temp = new Point3D(c);
			c = new Point3D(b);
			b = new Point3D(temp);
			
			util.Point temp2 = new util.Point(ct);
			ct = new util.Point(bt);
			bt = new util.Point(temp2);
			
			double temp3 = w3;
			w3 = w2;
			w2 = temp3;
		}
		
		
		//calculating slopes needed for rendering
		
		double dy1 = b.y - a.y;
		double dx1 = b.x - a.x;
		double dv1 = bt.y - at.y;
		double du1 = bt.x - at.x;
		double dw1 = w2 - w1;
		
		double dy2 = c.y - a.y;
		double dx2 = c.x - a.x;
		double dv2 = ct.y - at.y;
		double du2 = ct.x - at.x;
		double dw2 = w3 - w1;
		
		double daxStep = 0;	//screen space steps
		double dbxStep = 0;
		double du1Step = 0;	double dv1Step = 0;	//texture space steps
		double du2Step = 0; double dv2Step = 0;
		double dw1Step = 0; double dw2Step = 0; //perspective correction
		
		if(dy1 != 0) {
			daxStep = dx1 / Math.abs(dy1);
			du1Step = du1 / Math.abs(dy1);
			dv1Step = dv1 / Math.abs(dy1);
			dw1Step = dw1 / Math.abs(dy1);
		}
		if(dy2 != 0) {
			dbxStep = dx2 / Math.abs(dy2);
			du2Step = du2 / Math.abs(dy2);
			dv2Step = dv2 / Math.abs(dy2);
			dw2Step = dw2 / Math.abs(dy2);
		}
		
		//initializing variables now so we don't have to reinitialize them later in the draw loop
		//very small optimization
		double tex_u;
		double tex_v;
		double tex_w;
		
		double tex_su;	double tex_eu;	//starting and ending coordinates for each row in texture space
		double tex_sv;	double tex_ev;
		double tex_sw; 	double tex_ew;
		
		int ax;	int bx;
		
		double tStep;	double t;
		
		//starting to draw
		
		//top half of the triangle
		//we only draw the top half if it exists
		if(dy1 != 0) {
			
			for(int i = (int) a.y + 1; i <= (int) b.y; i++) {
				
				//calculating for each row the starting and ending coordinates
				ax = (int) (a.x + (double)(i - a.y) * daxStep);
				bx = (int) (a.x + (double)(i - a.y) * dbxStep); 
				
				
				//doing the same for the texture space
				//starting coordinates
				tex_su = at.x + ((double) i - a.y) * du1Step; 
				tex_sv = at.y + ((double) i - a.y) * dv1Step; 
				tex_sw = w1 + ((double) i - a.y) * dw1Step; 
				
				//ending coordinates
				tex_eu = at.x + ((double) i - a.y) * du2Step;
				tex_ev = at.y + ((double) i - a.y) * dv2Step;
				tex_ew = w1 + ((double) i - a.y) * dw2Step; 
				
				//making sure the texture coordinates are properly ordered
				if(ax > bx) {
					double temp = ax;
					ax = bx;
					bx = (int) temp;
					
					temp = tex_su;
					tex_su = tex_eu;
					tex_eu = temp;
					
					temp = tex_sv;
					tex_sv = tex_ev;
					tex_ev = temp;
					
					temp = tex_sw;
					tex_sw = tex_ew;
					tex_ew = temp;
				}
				
				
				tex_u = tex_su;
				tex_v = tex_sv;
				tex_w = tex_sw;
				
				tStep = 1d / ((double) (bx - ax));
				t = 0;
				
				//drawing the triangle
				for(int j = ax; j <= bx; j++) {
					
					int color = 0;
					
					tex_u = (1d - t) * tex_su + t * tex_eu;
					tex_v = (1d - t) * tex_sv + t * tex_ev;
					tex_w = (1d - t) * tex_sw + t * tex_ew;

					if(tri.whichTexture == -1 || this.textures.get(wt).length == 1 && this.textures.get(wt)[0].length == 1) {
						if(this.drawErrorTexture) {
							int texScaledX = (int) (mod(tex_u / tex_w) * this.errorTexture[0].length);
							int texScaledY = (int) (mod(tex_v / tex_w) * this.errorTexture.length);
							
							texScaledX = Math.min(texScaledX, this.errorTexture[0].length - 1);
							texScaledY = Math.min(texScaledY, this.errorTexture.length - 1);
							
							color = this.errorTexture[texScaledY][texScaledX];
						}
					}
					
					else {		
						int texScaledX = (int) (mod(tex_u / tex_w) * this.textures.get(wt)[0].length);
						int texScaledY = (int) (mod(tex_v / tex_w) * this.textures.get(wt).length);
						
						texScaledX = Math.min(texScaledX, this.textures.get(wt)[0].length - 1);
						texScaledY = Math.min(texScaledY, this.textures.get(wt).length - 1);
						
						color = this.textures.get(wt)[texScaledY][texScaledX];
					}
					
					if(j >= 0 && j < customResWidth && i >= 0 && i < customResHeight && tex_w > this.pixelDepth[i][j]) {
						this.pixelDepth[i][j] = tex_w;
						this.pixelColor[i][j] = color;
					}
					
					t += tStep;
				}
				
			}
		}
			
		//updating slope values for the bottom half of the triangle
		//so we can reuse the code above 
		dy1 = c.y - b.y;
		dx1 = c.x - b.x;
		dv1 = ct.y - bt.y;
		du1 = ct.x - bt.x;
		dw1 = w3 - w2;
		
		du1Step = 0;
		dv1Step = 0;
		dw1Step = 0;
		
		if(dy1 != 0) {
			daxStep = dx1 / Math.abs(dy1);
			du1Step = du1 / Math.abs(dy1);
			dv1Step = dv1 / Math.abs(dy1);
			dw1Step = dw1 / Math.abs(dy1);
		}
		if(dy2 != 0) {
			dbxStep = dx2 / Math.abs(dy2);
		} 
		
		
		//drawing bottom triangle
		for(int i = (int) b.y + 1; i <= (int) c.y; i++) {
			
			//calculating for each row the starting and ending coordinates
			ax = (int) (b.x + (double)(i - b.y) * daxStep);
			bx = (int) (a.x + (double)(i - a.y) * dbxStep); 
			
			
			//doing the same for the texture space
			//starting coordinates
			tex_su = bt.x + ((double) i - b.y) * du1Step; 
			tex_sv = bt.y + ((double) i - b.y) * dv1Step; 
			tex_sw = w2 + ((double) i - b.y) * dw1Step; 
			
			//ending coordinates
			tex_eu = at.x + ((double) i - a.y) * du2Step;
			tex_ev = at.y + ((double) i - a.y) * dv2Step;
			tex_ew = w1 + ((double) i - a.y) * dw2Step; 
			
			//making sure the texture coordinates are properly ordered
			if(ax > bx) {
				double temp = ax;
				ax = bx;
				bx = (int) temp;
				
				temp = tex_su;
				tex_su = tex_eu;
				tex_eu = temp;
				
				temp = tex_sv;
				tex_sv = tex_ev;
				tex_ev = temp;
				
				temp = tex_sw;
				tex_sw = tex_ew;
				tex_ew = temp;
			}
			
			
			tex_u = tex_su;
			tex_v = tex_sv;
			tex_w = tex_sw;
			
			tStep = 1d / ((double) (bx - ax));
			t = 0;
			
			//drawing the triangle
			for(int j = ax; j <= bx; j++) {
				
				int color = 0;
				
				tex_u = (1d - t) * tex_su + t * tex_eu;
				tex_v = (1d - t) * tex_sv + t * tex_ev;
				tex_w = (1d - t) * tex_sw + t * tex_ew;

				if(tri.whichTexture == -1 || this.textures.get(wt).length == 1 && this.textures.get(wt)[0].length == 1) {
					if(this.drawErrorTexture) {
						int texScaledX = (int) (mod(tex_u / tex_w) * this.errorTexture[0].length);
						int texScaledY = (int) (mod(tex_v / tex_w) * this.errorTexture.length);
						
						texScaledX = Math.min(texScaledX, this.errorTexture[0].length - 1);
						texScaledY = Math.min(texScaledY, this.errorTexture.length - 1);
						
						color = this.errorTexture[texScaledY][texScaledX];
					}
				}
				
				else {		
					int texScaledX = (int) (mod(tex_u / tex_w) * this.textures.get(wt)[0].length);
					int texScaledY = (int) (mod(tex_v / tex_w) * this.textures.get(wt).length);
					
					texScaledX = Math.min(texScaledX, this.textures.get(wt)[0].length - 1);
					texScaledY = Math.min(texScaledY, this.textures.get(wt).length - 1);
					
					color = this.textures.get(wt)[texScaledY][texScaledX];
				}
				
				if(j >= 0 && j < customResWidth && i >= 0 && i < customResHeight && tex_w > this.pixelDepth[i][j]) {
					this.pixelDepth[i][j] = tex_w;
					this.pixelColor[i][j] = color;
				}
				
				t += tStep;
			}
			
		}
			
	}
	
	//for handling tiled textures.
	//sometimes the uv specified in the obj file are not within the normalized space. This means that the textures are tiled.
	
	public double mod(double a) {
		a += ((int) Math.abs(a)) + 1;
		a %= 1;
		return a;
	}
	
	//calculates normal of given triangle
	
	public Vector3D calculateNormal(int[] t) {
		Point3D a = vertices.get(t[0]);
		Point3D b = vertices.get(t[1]);
		Point3D c = vertices.get(t[2]);
		
		Vector3D v1 = new Vector3D(a, c);
		Vector3D v2 = new Vector3D(a, b);
		
		Vector3D realNormal = MathTools.crossProduct(v1, v2);
		
		return realNormal;
	}
	
	//increases the rotation around the x, y, z axis. 
	
	//all rotations are applied at once during drawing. The order is x, y, then z.
	//not used anymore
	
	public void rotate(double xRad, double yRad, double zRad) {
		this.xRot += xRad;
		this.yRot += yRad;
		this.zRot += zRad;
	}
	
	//rotate around x then y. 
	
	//for use when you're rotating an object with the mouse
	//not used anymore
	
	public Point3D rotatePoint(Point3D p) {
		Point3D ans = new Point3D(p);
		
		ans = MathTools.rotatePoint(ans, 0, yRot, 0);
		ans = MathTools.rotatePoint(ans, xRot, 0, 0);
		
		return ans;
	}
	
	public void mousePressed(MouseEvent arg0) {
		this.mousePressed = true;
		for(Polygon p : this.projectedTriangles) {
			if(p.contains(this.mouse)) {
				this.editMode = true;
				break;
			}
		}
	}
	
	public void mouseReleased() {
		this.mousePressed = false;
		this.editMode = false;
	}

	public ArrayList<Triangle> getTriangles(Vector3D translate, int i, int j, Point3D camera2, Vector3D vLookDir2) {
		// TODO Auto-generated method stub
		return null;
	}
	
}

class Triangle {
	
	//this class is going to be used exclusively for the drawing loop
	
	public Point3D[] vertices;
	public util.Point[] texturePoints;
	public int index = -1;
	public int color;
	public double zBuffer = 0;
	
	public double[] w;
	
	public int whichTexture = 0;
	
	public Triangle() {
		vertices = new Point3D[3];
		texturePoints = new util.Point[3];
		color = 0;
		w = new double[3];
	}
	
}
