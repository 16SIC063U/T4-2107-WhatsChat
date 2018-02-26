package user;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectInputStream;
import java.io.ObjectOutput;
import java.io.ObjectOutputStream;
import java.io.Serializable;

import javax.swing.JList;

public class UserImageMap implements Serializable{
	String username;
	File userImage;
	
	public UserImageMap(String username, File userImage){
		this.username = username;
		this.userImage = userImage;
	}

	public File getUserImage(){
		return userImage;
	}
	public String getUsername(){
		return username;
	}
	public UserImageMap(byte[] bytes) {
		ByteArrayInputStream bis = new ByteArrayInputStream(bytes);
		ObjectInput in = null;
		try {
		  in = new ObjectInputStream(bis);
		  UserImageMap userImageMap = (UserImageMap) in.readObject(); 
		  this.username = userImageMap.getUsername();
		  this.userImage = userImageMap.getUserImage();
		}catch (IOException | ClassNotFoundException e){
			 System.out.println("error reading user image: " + e);
		}
			finally {
		
		  try {
		    if (in != null) {
		      in.close();
		    }
		  } catch (IOException ex) {
			  System.out.println("error reading user image");
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
				String s = new String(bos.toByteArray());
				return s;
			} catch (IOException e) {
				// TODO Auto-generated catch block
				System.out.println("error sending");
				e.printStackTrace();
			}
		} finally {
			try {
				bos.close();
			} catch (IOException ex) {
				System.out.println("error parsing");
				// ignore close exception
			}
		}
		return null;
	}
	
	@Override
	public boolean equals(Object obj) {
		System.out.println("Check if equal");
		UserImageMap userImageMap =  (UserImageMap) obj;
		if(username == userImageMap.getUsername()){
			return true;
		}else{
			return false;
		}
	}
	
	@Override
	public int hashCode() {
		return this.username.hashCode();
	}
}

