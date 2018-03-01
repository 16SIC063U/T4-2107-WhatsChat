package whatschat;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.InetAddress;
import java.net.MulticastSocket;
import java.util.ArrayList;
import java.util.List;

import user.UserProfile;

public class MulticastCommon {
	
	private MulticastSocket commonSocket = null;
	private InetAddress commonGroup = null;
	private Thread commonThread = null;
	private WhatsChat whatsChat;

	public DatagramPacket generateMessage(String message, InetAddress group) {
		byte[] buf = message.getBytes();
		DatagramPacket dgp = new DatagramPacket(buf, buf.length, group, WhatsChat.portNo);
		return dgp;
	}

	public MulticastCommon(WhatsChat whatsChat) {
		this.whatsChat = whatsChat;
		try {
			commonGroup = InetAddress.getByName("230.1.1.1");
			commonSocket = new MulticastSocket(WhatsChat.portNo);
			commonSocket.setReceiveBufferSize(65507);
			commonSocket.joinGroup(commonGroup);
			commonThread = new Thread(new Runnable() {
				@Override
				public void run() {

					byte rcvBuf[] = new byte[65507];
					DatagramPacket dgpReceived = new DatagramPacket(rcvBuf, rcvBuf.length);
					try {
						while (true) {
							commonSocket.receive(dgpReceived);
							byte[] receivedData = dgpReceived.getData();
							int length = dgpReceived.getLength();
							String msg = new String(receivedData, 0, length);
							// TODO: Listen to broadcast
							String[] msgArray = msg.split("::");
							String action = msgArray[0], parameter = msgArray[1];

							if (action.equals("getUser") && !(whatsChat.username.isEmpty())) {
								String sendUserMessage = "sendUser::" + whatsChat.username;
								commonSocket.send(generateMessage(sendUserMessage, commonGroup));
							}

							else if (action.equals("sendUser")) {
								if (!(whatsChat.usernameList.contains(parameter))) {
									whatsChat.usernameList.add(parameter);
									// TODO added by edwin
									whatsChat.lvd.addUsername(parameter);
								}
							}

							else if (action.equals("removeUser")) {
								whatsChat.usernameList.remove(parameter);
								whatsChat.lvd.removeUsername(parameter);
							}

							else if (action.equals("getUserImage") && !(whatsChat.userProfileList.isEmpty())) {
								for (UserProfile userImageMap : whatsChat.userProfileList) {
									String sendUserImageMessage = "sendUserImage::" + userImageMap.getByteMessage();
									commonSocket.send(generateMessage(sendUserImageMessage, commonGroup));
								}
							}

							else if (action.equals("sendUserImage")) {
								// Receive sent user info
								UserProfile map = new UserProfile(parameter.getBytes());
								boolean isInList = false;
								for (UserProfile userImageMap : whatsChat.userProfileList) {
									if (userImageMap.getUsername().equals(map.getUsername())) {
										userImageMap.setUserImagePath(map.getUserImagePath());
										userImageMap.setTextDescription(map.getTextDescription());
										isInList = true;
									}
								}
								// Add if not in list
								if (!isInList) {
									whatsChat.userProfileList.add(map);
									// Update list
								}

								whatsChat.lvd.refresh();
							}

							else if (action.equals("getGroup") && !(whatsChat.groupList.isEmpty())) {
								String sendGroupMessage = "";
								for (String group : whatsChat.groupList.keySet()) {
									sendGroupMessage = "sendGroup::" + group + "::" + whatsChat.groupList.get(group);
									commonSocket.send(generateMessage(sendGroupMessage, commonGroup));
								}
							}

							else if (action.equals("sendGroup")) {
								if (!(whatsChat.groupList.containsKey(parameter))) {
									String detail = msgArray[2];
									whatsChat.groupList.put(parameter, detail);
								}
							}

							else if (action.equals("removeGroup")) {
								// TODO remove group done by edwin
								if (whatsChat.groupList.containsKey(parameter)) {
									if (parameter.equals(whatsChat.activeGroup)) {
										whatsChat.activeGroup = "";
									}
									whatsChat.groupList.remove(parameter);
									String ipAddr = whatsChat.joinedGroupList.get(parameter);
									whatsChat.joinedGroupList.remove(parameter);
									whatsChat.joinedGroupChats.remove(ipAddr);
									whatsChat.joinedGroupMembers.remove(parameter);
									whatsChat.updateGroupList();
									whatsChat.updateMemberList();
									whatsChat.updateConversation();
								}
							}

							else if (action.equals("updateGroupName")) {
								int ip = parameter.hashCode();
								String ipStr = String.format("230.1.%d.%d", (ip & 0xff), (ip >> 8 & 0xff));
								if (whatsChat.groupList.containsKey(parameter)) {
									whatsChat.groupList.put(msgArray[2], whatsChat.groupList.get(parameter));
									whatsChat.groupList.remove(parameter);
								}
								if (whatsChat.joinedGroupList.containsKey(parameter)) {
									whatsChat.joinedGroupList.put(msgArray[2],
											whatsChat.joinedGroupList.get(parameter));
									whatsChat.joinedGroupList.remove(parameter);
								}
								if (whatsChat.joinedGroupMembers.containsKey(parameter)) {
									whatsChat.joinedGroupMembers.put(msgArray[2],
											whatsChat.joinedGroupMembers.get(parameter));
									whatsChat.joinedGroupMembers.remove(parameter);
								}
								if (whatsChat.joinedGroupChats.containsKey(ipStr)) {
									int newIp = msgArray[2].hashCode();
									String newIpStr = String.format("230.1.%d.%d", (ip & 0xff), (ip >> 8 & 0xff));
									whatsChat.joinedGroupChats.put(newIpStr, whatsChat.joinedGroupChats.get(ipStr));
									whatsChat.joinedGroupChats.remove(ipStr);
								}
								whatsChat.activeGroup = msgArray[2];
								whatsChat.updateGroupList();
							}

							else if (action.equals("inviteUser")) {
								if (parameter.equals(whatsChat.username)) {
									whatsChat.joinedGroupList.put(msgArray[2], msgArray[3]);
									whatsChat.joinedGroupMembers.put(msgArray[2], new ArrayList<>());
									whatsChat.updateGroupMembers(msgArray[2]);
									// System.out.println("1. Invite user");
									whatsChat.joinGroup(msgArray[3]);
									whatsChat.activeGroup = msgArray[2];
									whatsChat.updateGroupList();
									whatsChat.updateConversation();
								}
							}

							else if (action.equals("getMembers")) {
								if (whatsChat.joinedGroupList.containsKey(parameter)) {
									String sendMemberMessage = "sendMember::" + parameter + "::" + whatsChat.username;
									commonSocket.send(generateMessage(sendMemberMessage, commonGroup));
								}
							}

							else if (action.equals("sendMember")) {
								if (whatsChat.joinedGroupList.containsKey(parameter)) {
									List<String> members = whatsChat.joinedGroupMembers.get(parameter);
									if (!members.contains(msgArray[2])) {
										members.add(msgArray[2]);
										whatsChat.joinedGroupMembers.put(parameter, members);
									}
									whatsChat.updateMemberList();
								}
							}

							else if (action.equals("addMember")) {
								int ip = msgArray[2].hashCode();
								String ipStr = String.format("230.1.%d.%d", (ip & 0xff), (ip >> 8 & 0xff));
								if (whatsChat.joinedGroupChats.containsKey(ipStr)) {
									String sendChats = "sendChats::" + msgArray[2] + "::"
											+ whatsChat.joinedGroupChats.get(ipStr);
									commonSocket.send(generateMessage(sendChats, commonGroup));
								}
								if (whatsChat.username.equals(parameter)) {
									whatsChat.joinedGroupList.put(msgArray[2], ipStr);
									whatsChat.joinedGroupMembers.put(msgArray[2], new ArrayList<>());
									whatsChat.activeGroup = msgArray[2];
									whatsChat.updateGroupMembers(msgArray[2]);
									// System.out.println("username join
									// group");
									whatsChat.joinGroup(ipStr);
									whatsChat.updateGroupList();
									whatsChat.updateConversation();
								}
							}

							else if (action.equals("removeMember")) {
								if (whatsChat.username.equals(parameter)) {
									whatsChat.joinedGroupList.remove(msgArray[2]);
									whatsChat.joinedGroupMembers.remove(msgArray[2]);
									whatsChat.updateGroupList();
									whatsChat.updateConversation();
								}
								if (whatsChat.joinedGroupMembers.containsKey(msgArray[2])) {
									List<String> members = whatsChat.joinedGroupMembers.get(msgArray[2]);
									members.remove(parameter);
									whatsChat.joinedGroupMembers.put(msgArray[2], members);
									whatsChat.updateGroupList();
									whatsChat.updateConversation();
									whatsChat.updateMemberList();
								}
							}

							else if (action.equals("sendChats")) {
								int ip = parameter.hashCode();
								String ipStr = String.format("230.1.%d.%d", (ip & 0xff), (ip >> 8 & 0xff));
								if (whatsChat.joinedGroupList.containsKey(parameter)) {
									String[] chatArray = msgArray[2].split("\n");
									if (chatArray.length > 10) {
										String message = "";
										for (int i = chatArray.length - 1; i > chatArray.length - 11; i--) {
											message = chatArray[i] + "\n" + message;
										}
										whatsChat.joinedGroupChats.put(ipStr, message);
									} else {
										whatsChat.joinedGroupChats.put(ipStr, msgArray[2]);
									}
									whatsChat.updateConversation();
								}
							}
						}
					} catch (IOException ex) {
						ex.printStackTrace();
					}
				}
			});
			commonThread.start();
			String getUserMessage = "getUser:: ";
			commonSocket.send(generateMessage(getUserMessage, commonGroup));
			String getGroupMessage = "getGroup:: ";
			commonSocket.send(generateMessage(getGroupMessage, commonGroup));
			String getUserImageMessage = "getUserImage:: ";
			commonSocket.send(generateMessage(getUserImageMessage, commonGroup));
		} catch (IOException ex) {
			ex.printStackTrace();
		}
	}

	public void sendMessage(String sendUserMessage) {
		try {
			commonSocket.send(generateMessage(sendUserMessage, commonGroup));

		} catch (IOException e) {
			System.out.println("ERROR SENDING: " + e);
			e.printStackTrace();
		}
	}
}