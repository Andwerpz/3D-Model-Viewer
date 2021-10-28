package state;

import java.util.Stack;
import java.awt.Graphics;
import java.awt.MouseInfo;
import java.awt.Point;
import java.awt.event.MouseEvent;
import java.awt.event.MouseWheelEvent;


public class StateManager {
	
	public Stack<State> states;
	
	private Point mouse;
	
	public StateManager() {
		
		states = new Stack<State>();
		states.push(new MenuState(this));
		
	}
	
	public void tick(Point mouse2) {
		this.mouse = mouse2;
		states.peek().tick(mouse2);
	}
	
	public void draw(Graphics g) {
		
		g.drawString((int) (mouse.x) + "", (int) (mouse.x - 30), (int) (mouse.y - 10));
		g.drawString((int) (mouse.y) + "", (int) (mouse.x), (int) (mouse.y - 10));
		
		states.peek().draw(g);
		
	}
	
	public void keyPressed(int k) {
		states.peek().keyPressed(k);
	}
	
	public void keyReleased(int k) {
		states.peek().keyReleased(k);
	}
	
	public void keyTyped(int k) {
		states.peek().keyTyped(k);
	}
	
	public void mouseClicked(MouseEvent arg0) {
		states.peek().mouseClicked(arg0);
	}

	public void mouseEntered(MouseEvent arg0) {
		states.peek().mouseEntered(arg0);
	}

	public void mouseExited(MouseEvent arg0) {
		states.peek().mouseExited(arg0);
	}

	public void mousePressed(MouseEvent arg0) {
		states.peek().mousePressed(arg0);
	}

	public void mouseReleased(MouseEvent arg0) {
		states.peek().mouseReleased(arg0);
	}
	
	public void mouseWheelMoved(MouseWheelEvent arg0) {
		states.peek().mouseWheelMoved(arg0);
	}
	
}
