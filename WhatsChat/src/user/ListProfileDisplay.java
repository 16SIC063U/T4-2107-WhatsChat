package user;

import java.awt.Font;
import java.awt.Image;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.List;

import javax.imageio.ImageIO;
import javax.swing.DefaultListCellRenderer;
import javax.swing.DefaultListModel;
import javax.swing.ImageIcon;
import javax.swing.JLabel;
import javax.swing.JList;

public class ListProfileDisplay {

	private DefaultListModel model = new DefaultListModel();
	private JList list;

	public void refresh() {
		Object o = new Object();
		model.addElement(o);
		model.removeElement(o);
	}
	public void addUsername(String username) {
		model.addElement(username);
	}

	public JList getJList() {
		return list;
	}

	public ListProfileDisplay() {
		list = new JList(model);
		list.setCellRenderer(new ProfileRenderer());
	}

	class ProfileRenderer extends DefaultListCellRenderer {
		Font font = new Font("helvitica", Font.LAYOUT_LEFT_TO_RIGHT, 12);

		@Override
		public java.awt.Component getListCellRendererComponent(JList list, Object value, int index, boolean isSelected,
				boolean cellHasFocus) {

			JLabel label = (JLabel) super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);

			// Get file from images

			try {
				File file = new File("images/" + model.get(index) + ".jpg");
				if(!file.exists()) {
					file = new File("no_img.png");
				}
				BufferedImage image;
				image = ImageIO.read(file);
				Image dimg = image.getScaledInstance(30, 30, Image.SCALE_SMOOTH);
				label.setIcon(new ImageIcon(dimg));
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}

			
			label.setHorizontalTextPosition(JLabel.RIGHT);
			label.setFont(font);
			return label;
		}

	}

}
