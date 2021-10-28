package util;

import java.awt.AlphaComposite;
import java.awt.Color;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.awt.image.BufferedImage;

import main.MainPanel;

public class GraphicsTools {

	public static AlphaComposite makeComposite(double alpha) {
		int type = AlphaComposite.SRC_OVER;
		return(AlphaComposite.getInstance(type, (float) alpha));
	}
	
	public static void drawCenteredString(int x, int y, Graphics g, Font f, String s, Color c) {
		int width = GraphicsTools.calculateTextWidth(s, f);
		g.setFont(f);
		g.setColor(c);
		g.drawString(s, x - width / 2, y - f.getSize() / 2);
	}
	
	public static int calculateTextWidth(String text, Font font) {
		BufferedImage img = new BufferedImage(1, 1, BufferedImage.TYPE_INT_ARGB);
		FontMetrics fm = img.getGraphics().getFontMetrics(font);
		return fm.stringWidth(text);
	}
	
}
