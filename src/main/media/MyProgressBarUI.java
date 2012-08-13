package media;


import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;

import javax.swing.*;


import javax.swing.plaf.basic.*;

public class MyProgressBarUI extends BasicProgressBarUI
{

	@Override
	public void paintDeterminate(Graphics g, JComponent c) {
		
		Graphics2D g2d = (Graphics2D) g;
		JProgressBar bar = (JProgressBar) c;
		Dimension vSize = c.getSize();
		int value = bar.getValue();
		
		float newValue =((float) vSize.width /(float)10000) * (float) value;		
		g2d.setColor(Color.BLACK);
		g2d.fillRect(0,0, vSize.width, vSize.height );
		g2d.setColor(Color.white);
		g2d.fillRect(1,1, (int) newValue, vSize.height-2 );

	}

}

