package whatschat;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.InetAddress;
import java.net.MulticastSocket;
import java.util.List;

public class MulticastBroadcast {
	MulticastSocket multicastSocket = null;
	InetAddress multicastGroup = null;
	WhatsChat whatschat;

	public MulticastBroadcast(WhatsChat whatschat) {
		this.whatschat = whatschat;
	}

	public void joinGroup(String ipStr) {
		try {

			multicastGroup = InetAddress.getByName(ipStr);
			multicastSocket = new MulticastSocket(WhatsChat.portNo);
			multicastSocket.setReceiveBufferSize(65507);
			multicastSocket.joinGroup(multicastGroup);
			// Create a new thread to keep listening for packets from the group
			new Thread(new Runnable() {
				@Override
				public void run() {
					// System.out.println("Start run");
					byte buf1[] = new byte[65507];
					DatagramPacket dgpReceived = new DatagramPacket(buf1, buf1.length);
					while (true) {
						try {
							multicastSocket.receive(dgpReceived);
							byte[] receivedData = dgpReceived.getData();
							int length = dgpReceived.getLength();
							String msg = new String(receivedData, 0, length);
							// System.out.println("Received joingroup packet: "
							// + msg);
							String subString = "";
							if (msg.length() > 11) {
								subString = msg.substring(0, 10);
							}
							if (msg.equals("removeGroup")) {
								// Stop current thread
								// System.out.println("Stop thread: " +
								// Thread.currentThread().getId());
								Thread.currentThread().interrupt();
								// System.out.println("Thread after stopping: "
								// +
								// Thread.currentThread().getId());
							} else if (subString.equals("leaveGroup")) {
								String[] msgArray = msg.split("::");
								List<String> members = whatschat.joinedGroupMembers.get(msgArray[1]);
								if (members.size() > 0) {
									members.remove(msgArray[2]);
									whatschat.joinedGroupMembers.put(msgArray[1], members);
									if (!msgArray[2].equals(whatschat.username)) {
										String message = msgArray[2] + " has left the group...";
										String previousChats = whatschat.joinedGroupChats.get(ipStr);
										whatschat.joinedGroupChats.put(ipStr, previousChats + "\n" + message);
									} else {
										whatschat.activeGroup = "";
									}
									whatschat.updateConversation();
									whatschat.updateGroupList();
									whatschat.updateMemberList();
								}
							} else {
								if (whatschat.joinedGroupChats.get(ipStr) != null) {
									String[] chats = whatschat.joinedGroupChats.get(ipStr).split("\n");
									if (!chats[chats.length - 1].equals(msg)) {
										String previousChats = whatschat.joinedGroupChats.get(ipStr);
										whatschat.joinedGroupChats.put(ipStr, previousChats + "\n" + msg);
									} else {
										whatschat.joinedGroupChats.put(ipStr, msg);
									}
								} else {
									whatschat.joinedGroupChats.put(ipStr, msg);
								}
								whatschat.updateConversation();
								whatschat.updateMemberList();
							}

							// lock.unlock();
						} catch (IOException ex) {
							System.out.println("thread run ex: " + ex);
							ex.printStackTrace();
						}
					}
				}
			}).start();
			String message = whatschat.username + " joined " + ipStr;
			// System.out.println("join group");
			multicastSocket.send(generateMessage(message, multicastGroup));
		} catch (IOException ex) {
			ex.printStackTrace();
		}
	}

	public DatagramPacket generateMessage(String message, InetAddress group) {
		byte[] buf = message.getBytes();
		DatagramPacket dgp = new DatagramPacket(buf, buf.length, group, WhatsChat.portNo);
		return dgp;
	}

	public void sendMessage(String sendUserMessage, InetAddress activeInet) {
		try {
			multicastSocket.send(generateMessage(sendUserMessage, activeInet));
		} catch (IOException e) {
			System.out.println("ERROR SENDING: " + e);
			e.printStackTrace();
		}
	}

	public InetAddress getMulticastGroup() {
		return multicastGroup;
	}
}
