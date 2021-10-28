package button;

import java.awt.Graphics;
import java.awt.Point;
import java.awt.event.MouseEvent;
import java.util.ArrayList;

public class ButtonManager {
	
	public ArrayList<Button> buttons;
	public ArrayList<SliderButton> sliderButtons;
	public ArrayList<ToggleButton> toggleButtons;

	public ButtonManager() {
		
		buttons = new ArrayList<Button>();
		sliderButtons = new ArrayList<SliderButton>();
		toggleButtons = new ArrayList<ToggleButton>();
		
	}
	
	public void setText(int buttonType, int index, String text) {
		
		switch(buttonType) {
		
		case 0:
			buttons.get(index).setText(text);
			return;
			
		case 1:
			sliderButtons.get(index).setText(text);
			return;
			
		case 2:
			toggleButtons.get(index).setText(text);
		
		}
		
	}
	
	public void addButton(Button b) {
		buttons.add(b);
	}
	
	public void addSliderButton(SliderButton b) {
		sliderButtons.add(b);
	}
	
	public void addToggleButton(ToggleButton b) {
		toggleButtons.add(b);
	}
	
	public void draw(Graphics g) {
		
		for(Button b : buttons) {
			b.draw(g);
		}
		
		for(SliderButton b : sliderButtons) {
			b.draw(g);
		}
		
		for(ToggleButton b : toggleButtons) {
			b.draw(g);
		}
		
	}
	
	public void tick(Point mouse) {
		
		for(SliderButton b : sliderButtons) {
			b.tick(mouse);
		}
		
	}
	
	public void pressed(MouseEvent arg0) {
		
		for(Button b : buttons) {
			b.pressed(arg0);
		}
		
		for(SliderButton b : sliderButtons) {
			b.pressed(arg0);
		}
		
		for(ToggleButton b : toggleButtons) {
			b.pressed(arg0);
		}
		
	}
	
	public boolean getPressed(MouseEvent arg0) {
		
		for(Button b : buttons) {
			if(b.getPressed()) {
				return true;
			}
		}
		
		for(SliderButton b : sliderButtons) {
			if(b.getHead().getPressed()) {
				return true;
			}
		}
		
		for(ToggleButton b : toggleButtons) {
			if(b.getPressed()) {
				return true;
			}
		}
		
		return false;
		
	}
	
	public String buttonClicked(MouseEvent arg0) {
		
		for(int i = 0; i < buttons.size(); i++) {
			if(buttons.get(i).isClicked(arg0)) {
				return buttons.get(i).getText();
			}
		}
		
		for(int i = 0; i < toggleButtons.size(); i++) {
			if(toggleButtons.get(i).isClicked(arg0)) {
				
			}
		}
		
		return null;
		
	}
	
	public void mouseReleased() {
		
		for(Button b : buttons) {
			b.released();
		}
		
		for(SliderButton b : sliderButtons) {
			b.released();
		}
		
		for(ToggleButton b : toggleButtons) {
			b.released();
		}
		
	}
	
}
