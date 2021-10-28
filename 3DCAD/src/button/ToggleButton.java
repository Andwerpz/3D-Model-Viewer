package button;
import java.awt.Color;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.awt.Point;
import java.awt.event.MouseEvent;
import java.awt.image.BufferedImage;
import java.awt.Rectangle;

public class ToggleButton extends Button{
	
    private boolean toggled = false;
    private Color toggledColor;

    public ToggleButton(int x, int y, int width, int height, String text){
        super(x,y,width,height,text);
        this.toggledColor = Color.gray;
    }
    
    public ToggleButton(int x, int y, int width, int height, String text, Color baseColor, Color pressedColor, Color toggledColor) {
    	super(x, y, width, height, text, baseColor, pressedColor);
    	this.toggledColor = toggledColor;
    }
    
    @Override
    public void draw(Graphics g) {
    	
    	g.setColor(getBaseColor());
    	
		if(toggled){
        	g.setColor(this.toggledColor);
        }
		
		if(getPressed()) {
			g.setColor(getPressedColor());
		}
		
		g.fillRect((int)getX(), (int)getY(), getWidth(), getHeight());
		g.setColor(Color.BLACK);
		g.setFont(getFont());
		g.drawRect((int) getX(), (int)getY(), getWidth(), getHeight());
		g.drawString(getText(), (int) getX() + (getWidth() / 2) - (calculateTextWidth() / 2), (int) getY() + (getHeight() / 2) + getFont().getSize() / 2);
		
		//super.draw(g);
        
    } 
    
    @Override
    public boolean isClicked(MouseEvent arg0) {
    	
    	
		Rectangle temp = new Rectangle((int)getX(),(int)getY(),getWidth(),getHeight());
		if(temp.contains(new Point(arg0.getX(), arg0.getY()))) {
			this.toggled = !this.toggled;
            return true;
		}
		return false;
		
		
    }
    
    public void setToggled(boolean bool){
        this.toggled = bool;
    }
    
    public boolean getToggled() {
    	return this.toggled;
    }
}
