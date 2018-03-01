package user;

import java.awt.Font;
import java.awt.Image;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;

import javax.imageio.ImageIO;
import javax.swing.DefaultListCellRenderer;
import javax.swing.DefaultListModel;
import javax.swing.ImageIcon;
import javax.swing.JLabel;
import javax.swing.JList;

import whatschat.WhatsChat;

public class ListProfileDisplay {

	private DefaultListModel<String> model = new DefaultListModel<String>();
	private JList<String> list;

	public ListProfileDisplay(WhatsChat whatsChat) {
		list = new JList<String>(model);
		list.setCellRenderer(new ProfileRenderer());
		list.addMouseListener(new MouseAdapter() {
			public void mouseClicked(MouseEvent evt) {
				if (evt.getClickCount() == 2) {
					JList list = (JList) evt.getSource();
					int index = list.locationToIndex(evt.getPoint());
					String usernameClicked = model.get(index);
					// Display profile
					whatsChat.showUserProfile(usernameClicked);
				}
			}
		});
	}

	public JList<String> getJList() {
		return list;
	}

	public void refresh() {
		String o = new String();
		model.addElement(o);
		model.removeElement(o);
	}

	public void addUsername(String username) {
		model.addElement(username);
	}

	public void removeUsername(String username) {
		model.removeElement(username);
	}

	class ProfileRenderer extends DefaultListCellRenderer {

		Font font = new Font("helvitica", Font.LAYOUT_LEFT_TO_RIGHT, 12);

		@Override
		public java.awt.Component getListCellRendererComponent(JList list, Object value, int index, boolean isSelected,
				boolean cellHasFocus) {

			JLabel label = (JLabel) super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);
			// Get file from images
			try {
				File file = new File(ImageUtil.getUserFolderPath(model.get(index)) + model.get(index) + ".jpg");
				if (!file.exists()) {
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
