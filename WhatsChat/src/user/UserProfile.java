package user;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectInputStream;
import java.io.ObjectOutput;
import java.io.ObjectOutputStream;
import java.io.Serializable;

public class UserProfile implements Serializable {

	private static final long serialVersionUID = 1L;

	String username;
	String userImagePath;

	private String textDescription;

	public UserProfile(String username, String userImagePath, String textDescription) {
		this.username = username;
		this.userImagePath = userImagePath;
		this.textDescription = textDescription;
	}

	public void setUserImagePath(String userImagePath) {
		this.userImagePath = userImagePath;
	}

	public String getUsername() {
		return username;
	}

	public String getUserImagePath() {
		return userImagePath;
	}

	public String getTextDescription() {
		return textDescription;
	}

	public UserProfile(byte[] bytes) {

		ByteArrayInputStream bis = new ByteArrayInputStream(bytes);
		ObjectInput in = null;
		try {
			in = new ObjectInputStream(bis);
			UserProfile userImageMap = (UserProfile) in.readObject();
			this.username = userImageMap.getUsername();
			this.userImagePath = userImageMap.getUserImagePath();
			this.textDescription = userImageMap.getTextDescription();
		} catch (IOException | ClassNotFoundException e) {
			System.out.println("error reading user image: " + e);
			e.printStackTrace();
		} finally {
			try {
				if (in != null) {
					in.close();
				}
			} catch (IOException ex) {
				System.out.println("error reading user image: " + ex);
				// ignore close exception
			}
		}

	}

	public String getByteMessage() {
		ByteArrayOutputStream bos = new ByteArrayOutputStream();
		ObjectOutput out = null;
		try {
			try {
				out = new ObjectOutputStream(bos);
				out.writeObject(this);
				out.flush();
				byte[] yourBytes = bos.toByteArray();
				return new String(yourBytes);
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
				return null;
			}

		} finally {
			try {
				bos.close();
			} catch (IOException ex) {
				// ignore close exception
			}
		}
	}

	public void setTextDescription(String textDescription) {
		this.textDescription = textDescription;
	}
}
