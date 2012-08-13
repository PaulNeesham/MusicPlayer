package media;

import java.awt.Component;

import javax.swing.ImageIcon;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.ListCellRenderer;

public class MyListCellRenderer extends JLabel implements ListCellRenderer {

		public  MyListCellRenderer () {
			// no need to read the icon from file at every call
			// let's do it only once
			super(( new ImageIcon ("h:/my/plane.jpeg")));
		}



		public Component getListCellRendererComponent ( JList list , Object value , int index , boolean a , boolean b ) {
			if(value instanceof String)
				this.setText((String) value);
			else if(value instanceof JLabel)
				this.setText(((JLabel) value).getText());
			return (this) ;

		}

	}

