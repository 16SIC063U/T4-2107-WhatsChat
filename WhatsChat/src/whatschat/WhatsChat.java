/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package whatschat;

import java.awt.CardLayout;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.Image;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.net.DatagramPacket;
import java.net.InetAddress;
import java.net.MulticastSocket;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.imageio.ImageIO;
import javax.swing.BoxLayout;
import javax.swing.ButtonGroup;
import javax.swing.GroupLayout;
import javax.swing.ImageIcon;
import javax.swing.GroupLayout.Alignment;
import javax.swing.JCheckBox;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.LayoutStyle.ComponentPlacement;

import user.ImageUtil;
import user.ListProfileDisplay;
import user.ProfilePicture;
import user.UserProfile;
import user.manageGroup;
import javax.swing.JButton;
import java.awt.event.ActionListener;
import java.awt.event.ActionEvent;

/**
 *
 * @author Juun
 */
public class WhatsChat extends javax.swing.JFrame {

	// Variables declaration - do not modify//GEN-BEGIN:variables
	private javax.swing.JButton btnUpdateProfile;
	private javax.swing.JButton btnAddPicture;
	private javax.swing.JButton btnCancel;
	private javax.swing.JButton btnCreate;
	private javax.swing.JButton btnDelete;
	private javax.swing.JButton btnEdit;
	private javax.swing.JButton btnLeave;
	private javax.swing.JButton btnRegister;
	private javax.swing.JButton btnSend;
	private javax.swing.JLabel jLabel1;
	private javax.swing.JLabel jLabel2;
	private javax.swing.JLabel jLabel3;
	private javax.swing.JLabel jLabel4;
	private javax.swing.JLabel labelEditGroupError;
	private javax.swing.JLabel labelGroupError;
	private javax.swing.JLabel labelMessageError;
	private javax.swing.JLabel labelRegisterError;
	private javax.swing.JLabel lblProfilePic;
	private javax.swing.JLabel lbProfileDescription;
	private javax.swing.JPanel jPanel1;
	private javax.swing.JPanel jPanel2;
	private javax.swing.JPanel jPanel3;
	private javax.swing.JPanel jPanel4;
	private javax.swing.JPanel jPanel5;
	private javax.swing.JPanel jPanel6;
	private javax.swing.JPanel jPanel7;
	private javax.swing.JPanel panelGroup;
	private javax.swing.JPanel panelUser;
	private javax.swing.JPanel panelUserCheckbox;
	private javax.swing.JPanel groupPane;
	private javax.swing.JScrollPane jScrollPane3;
	private javax.swing.JScrollPane scrollPane;
	private javax.swing.JScrollPane scrollUser;
	private javax.swing.JScrollPane scrollGroup;
	private javax.swing.JTextArea listConversation;
	private javax.swing.JTextArea textAreaDescription;
	private javax.swing.JTextArea listUser;
	private javax.swing.JTextField textGroup;
	private javax.swing.JTextField textMessage;
	private javax.swing.JTextField textRegister;
	private javax.swing.JTextField textSelectedGroup;
	private ButtonGroup buttonGroup;

	static final int portNo = 6789;
	static volatile List<String> usernameList = new ArrayList<String>();
	static volatile Map<String, String> groupList = new HashMap<String, String>();
	static volatile boolean messageFlag = false;
	Map<String, String> joinedGroupList = new HashMap<String, String>();
	Map<String, String> joinedGroupChats = new HashMap<String, String>();
	Map<String, List<String>> joinedGroupMembers = new HashMap<String, List<String>>();

	MulticastSocket multicastSocket = null;
	InetAddress multicastGroup = null;
	MulticastSocket commonSocket = null;
	InetAddress commonGroup = null;
	public static String username = "";
	String activeGroup = "";
	Thread commonThread = null;

	// To store a mapping of username and its matching profile pic
	List<UserProfile> userImageMapList = new ArrayList<>();

	// Added this for listview. putthis in a JScrollPane by calling new
	// JScrollPane(lvd.getJList());
	ListProfileDisplay lvd = new ListProfileDisplay(this);

	/**
	 * Creates new form WhatsChat
	 */
	public WhatsChat() {
		initComponents();
		postInitComponent();
		try {
			commonGroup = InetAddress.getByName("230.1.1.1");
			commonSocket = new MulticastSocket(portNo);
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

							if (action.equals("getUser") && !(username.isEmpty())) {
								String sendUserMessage = "sendUser::" + username;
								commonSocket.send(generateMessage(sendUserMessage, commonGroup));
							}

							else if (action.equals("sendUser")) {
								if (!(usernameList.contains(parameter))) {
									usernameList.add(parameter);
									// TODO added by edwin
									lvd.addUsername(parameter);
								}
							}

							else if (action.equals("removeUser")) {
								usernameList.remove(parameter);
								lvd.removeUsername(parameter);
							}

							else if (action.equals("getUserImage") && !(userImageMapList.isEmpty())) {
								for (UserProfile userImageMap : userImageMapList) {
									String sendUserImageMessage = "sendUserImage::" + userImageMap.getByteMessage();
									commonSocket.send(generateMessage(sendUserImageMessage, commonGroup));
								}
							}

							else if (action.equals("sendUserImage")) {
								UserProfile map = new UserProfile(parameter.getBytes());
								boolean isInList = false;
								for (UserProfile userImageMap : userImageMapList) {
									if (userImageMap.getUsername().equals(map.getUsername())) {
										userImageMap.setUserImagePath(map.getUserImagePath());
										userImageMap.setTextDescription(map.getTextDescription());
										isInList = true;
									}
								}
								// Add if not in list
								if (!isInList) {
									userImageMapList.add(map);
									// Update list
								}

								lvd.refresh();
							}

							else if (action.equals("getGroup") && !(groupList.isEmpty())) {
								String sendGroupMessage = "";
								for (String group : groupList.keySet()) {
									sendGroupMessage = "sendGroup::" + group + "::" + groupList.get(group);
									commonSocket.send(generateMessage(sendGroupMessage, commonGroup));
								}
							}

							else if (action.equals("sendGroup")) {
								if (!(groupList.containsKey(parameter))) {
									String detail = msgArray[2];
									groupList.put(parameter, detail);
								}
							}

							else if (action.equals("removeGroup")) {
								// TODO remove group done by edwin
								if (groupList.containsKey(parameter)) {
									if (parameter.equals(activeGroup)) {
										activeGroup = "";
									}
									groupList.remove(parameter);
									String ipAddr = joinedGroupList.get(parameter);
									joinedGroupList.remove(parameter);
									joinedGroupChats.remove(ipAddr);
									joinedGroupMembers.remove(parameter);
									updateGroupList();
									updateMemberList();
									updateConversation();
								}
							}

							else if (action.equals("updateGroupName")) {
								int ip = parameter.hashCode();
								String ipStr = String.format("230.1.%d.%d", (ip & 0xff), (ip >> 8 & 0xff));
								if (groupList.containsKey(parameter)) {
									groupList.put(msgArray[2], groupList.get(parameter));
									groupList.remove(parameter);
								}
								if (joinedGroupList.containsKey(parameter)) {
									joinedGroupList.put(msgArray[2], joinedGroupList.get(parameter));
									joinedGroupList.remove(parameter);
								}
								if (joinedGroupMembers.containsKey(parameter)) {
									joinedGroupMembers.put(msgArray[2], joinedGroupMembers.get(parameter));
									joinedGroupMembers.remove(parameter);
								}
								if (joinedGroupChats.containsKey(ipStr)) {
									int newIp = msgArray[2].hashCode();
									String newIpStr = String.format("230.1.%d.%d", (ip & 0xff), (ip >> 8 & 0xff));
									joinedGroupChats.put(newIpStr, joinedGroupChats.get(ipStr));
									joinedGroupChats.remove(ipStr);
								}
								activeGroup = msgArray[2];
								updateGroupList();
							}

							else if (action.equals("inviteUser")) {
								if (parameter.equals(username)) {
									joinedGroupList.put(msgArray[2], msgArray[3]);
									joinedGroupMembers.put(msgArray[2], new ArrayList<>());
									updateGroupMembers(msgArray[2]);
									// System.out.println("1. Invite user");
									joinGroup(msgArray[3]);
									activeGroup = msgArray[2];
									updateGroupList();
									updateConversation();
								}
							}

							else if (action.equals("getMembers")) {
								if (joinedGroupList.containsKey(parameter)) {
									String sendMemberMessage = "sendMember::" + parameter + "::" + username;
									commonSocket.send(generateMessage(sendMemberMessage, commonGroup));
								}
							}

							else if (action.equals("sendMember")) {
								if (joinedGroupList.containsKey(parameter)) {
									List<String> members = joinedGroupMembers.get(parameter);
									if (!members.contains(msgArray[2])) {
										members.add(msgArray[2]);
										joinedGroupMembers.put(parameter, members);
									}
									updateMemberList();
								}
							}

							else if (action.equals("addMember")) {
								int ip = msgArray[2].hashCode();
								String ipStr = String.format("230.1.%d.%d", (ip & 0xff), (ip >> 8 & 0xff));
								if (joinedGroupChats.containsKey(ipStr)) {
									String sendChats = "sendChats::" + msgArray[2] + "::" + joinedGroupChats.get(ipStr);
									commonSocket.send(generateMessage(sendChats, commonGroup));
								}
								if (username.equals(parameter)) {
									joinedGroupList.put(msgArray[2], ipStr);
									joinedGroupMembers.put(msgArray[2], new ArrayList<>());
									activeGroup = msgArray[2];
									updateGroupMembers(msgArray[2]);
									// System.out.println("username join group");
									joinGroup(ipStr);
									updateGroupList();
									updateConversation();
								}
							}

							else if (action.equals("removeMember")) {
								if (username.equals(parameter)) {
									joinedGroupList.remove(msgArray[2]);
									joinedGroupMembers.remove(msgArray[2]);
									updateGroupList();
									updateConversation();
								}
								if (joinedGroupMembers.containsKey(msgArray[2])) {
									List<String> members = joinedGroupMembers.get(msgArray[2]);
									members.remove(parameter);
									joinedGroupMembers.put(msgArray[2], members);
									updateGroupList();
									updateConversation();
									updateMemberList();
								}
							}

							else if (action.equals("sendChats")) {
								int ip = parameter.hashCode();
								String ipStr = String.format("230.1.%d.%d", (ip & 0xff), (ip >> 8 & 0xff));
								if (joinedGroupList.containsKey(parameter)) {
									String[] chatArray = msgArray[2].split("\n");
									if (chatArray.length > 10) {
										String message = "";
										for (int i = chatArray.length - 1; i > chatArray.length - 11; i--) {
											message = chatArray[i] + "\n" + message;
										}
										joinedGroupChats.put(ipStr, message);
									} else {
										joinedGroupChats.put(ipStr, msgArray[2]);
									}
									updateConversation();
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

	/**
	 * This method is called from within the constructor to initialize the form.
	 * WARNING: Do NOT modify this code. The content of this method is always
	 * regenerated by the Form Editor.
	 */
	@SuppressWarnings("unchecked")
	// <editor-fold defaultstate="collapsed" desc="Generated
	// <editor-fold defaultstate="collapsed" desc="Generated
	// Code">//GEN-BEGIN:initComponents
	private void initComponents() {
		lblProfilePic = new JLabel("");
		btnRegister = new javax.swing.JButton();
		textRegister = new javax.swing.JTextField();
		jPanel1 = new javax.swing.JPanel();
		btnCreate = new javax.swing.JButton();
		textGroup = new javax.swing.JTextField();
		labelGroupError = new javax.swing.JLabel();
		btnCancel = new javax.swing.JButton();
		jLabel3 = new javax.swing.JLabel();
		jPanel5 = new javax.swing.JPanel();
		btnDelete = new javax.swing.JButton();
		btnEdit = new javax.swing.JButton();
		btnLeave = new javax.swing.JButton();
		textSelectedGroup = new javax.swing.JTextField();
		labelEditGroupError = new javax.swing.JLabel();
		jLabel4 = new javax.swing.JLabel();
		jPanel2 = new javax.swing.JPanel();
		panelUser = new javax.swing.JPanel();
		jPanel3 = new javax.swing.JPanel();
		panelGroup = new javax.swing.JPanel();
		jPanel4 = new javax.swing.JPanel();
		jScrollPane3 = new javax.swing.JScrollPane();
		listConversation = new javax.swing.JTextArea();
		btnSend = new javax.swing.JButton();
		textMessage = new javax.swing.JTextField();
		labelRegisterError = new javax.swing.JLabel();
		labelMessageError = new javax.swing.JLabel();
		jLabel1 = new javax.swing.JLabel();
		jLabel2 = new javax.swing.JLabel();
		jPanel6 = new javax.swing.JPanel();
		jPanel7 = new javax.swing.JPanel();
		btnAddPicture = new javax.swing.JButton();
		btnAddPicture.setEnabled(false);

		textRegister.addKeyListener(new KeyAdapter() {
			@Override
			public void keyPressed(KeyEvent e) {
				if (e.getKeyCode() == KeyEvent.VK_ENTER) {
					registerName();
				}
			}
		});

		textGroup.addKeyListener(new KeyAdapter() {
			@Override
			public void keyPressed(KeyEvent e) {
				if (e.getKeyCode() == KeyEvent.VK_ENTER) {
					createGroup();
				}
			}
		});

		textMessage.addKeyListener(new KeyAdapter() {
			@Override
			public void keyPressed(KeyEvent e) {
				if (e.getKeyCode() == KeyEvent.VK_ENTER) {
					sendChatMessage();
				}
			}
		});

		setDefaultCloseOperation(javax.swing.WindowConstants.EXIT_ON_CLOSE);
		addWindowListener(new java.awt.event.WindowAdapter() {
			public void windowClosing(java.awt.event.WindowEvent evt) {
				formWindowClosing(evt);
			}
		});

		btnRegister.setFont(new java.awt.Font("Tahoma", 1, 13)); // NOI18N
		btnRegister.setText("Register User");
		btnRegister.setToolTipText("");
		btnRegister.addActionListener(new java.awt.event.ActionListener() {
			public void actionPerformed(java.awt.event.ActionEvent evt) {
				btnRegisterActionPerformed(evt);
			}
		});

		jPanel1.setBorder(javax.swing.BorderFactory.createTitledBorder("Group Management"));

		btnCreate.setFont(new java.awt.Font("Tahoma", 1, 13)); // NOI18N
		btnCreate.setText("Create");
		btnCreate.addMouseListener(new java.awt.event.MouseAdapter() {
			public void mouseClicked(java.awt.event.MouseEvent evt) {
				btnCreateMouseClicked(evt);
			}
		});

		labelGroupError.setForeground(new java.awt.Color(255, 59, 48));
		labelGroupError.setText("                          ");

		btnCancel.setFont(new java.awt.Font("Tahoma", 1, 13)); // NOI18N
		btnCancel.setText("Cancel");
		btnCancel.addMouseListener(new java.awt.event.MouseAdapter() {
			public void mouseClicked(java.awt.event.MouseEvent evt) {
				btnCancelMouseClicked(evt);
			}
		});

		jLabel3.setFont(new java.awt.Font("Tahoma", 1, 13)); // NOI18N
		jLabel3.setText("Group Name");

		jPanel5.setBorder(javax.swing.BorderFactory.createTitledBorder("Manage Selected Group"));

		btnDelete.setFont(new java.awt.Font("Tahoma", 1, 13)); // NOI18N
		btnDelete.setText("Delete");
		btnDelete.setToolTipText("");
		btnDelete.addMouseListener(new java.awt.event.MouseAdapter() {
			public void mouseClicked(java.awt.event.MouseEvent evt) {
				btnDeleteMouseClicked(evt);
			}
		});

		btnEdit.setFont(new java.awt.Font("Tahoma", 1, 13)); // NOI18N
		btnEdit.setText("Edit");
		btnEdit.addMouseListener(new java.awt.event.MouseAdapter() {
			public void mouseClicked(java.awt.event.MouseEvent evt) {
				btnEditMouseClicked(evt);
			}
		});

		btnLeave.setFont(new java.awt.Font("Tahoma", 1, 13)); // NOI18N
		btnLeave.setText("Leave");
		btnLeave.addMouseListener(new java.awt.event.MouseAdapter() {
			public void mouseClicked(java.awt.event.MouseEvent evt) {
				btnLeaveMouseClicked(evt);
			}
		});

		textSelectedGroup.setText("None.");
		textSelectedGroup.setEnabled(false);

		labelEditGroupError.setForeground(new java.awt.Color(255, 59, 48));
		labelEditGroupError.setText("                          ");

		javax.swing.GroupLayout jPanel5Layout = new javax.swing.GroupLayout(jPanel5);
		jPanel5.setLayout(jPanel5Layout);
		jPanel5Layout.setHorizontalGroup(jPanel5Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
				.addGroup(jPanel5Layout.createSequentialGroup().addContainerGap().addGroup(jPanel5Layout
						.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING).addComponent(textSelectedGroup)
						.addGroup(jPanel5Layout.createSequentialGroup()
								.addGroup(jPanel5Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
										.addGroup(jPanel5Layout.createSequentialGroup().addComponent(btnEdit)
												.addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
												.addComponent(btnLeave)
												.addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
												.addComponent(btnDelete))
										.addComponent(labelEditGroupError))
								.addGap(0, 0, Short.MAX_VALUE)))
						.addContainerGap()));
		jPanel5Layout
				.setVerticalGroup(jPanel5Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
						.addGroup(javax.swing.GroupLayout.Alignment.TRAILING, jPanel5Layout.createSequentialGroup()
								.addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
								.addComponent(textSelectedGroup, javax.swing.GroupLayout.PREFERRED_SIZE,
										javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
								.addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
								.addComponent(labelEditGroupError, javax.swing.GroupLayout.PREFERRED_SIZE, 16,
										javax.swing.GroupLayout.PREFERRED_SIZE)
								.addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
								.addGroup(jPanel5Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
										.addComponent(btnEdit).addComponent(btnDelete).addComponent(btnLeave))
								.addContainerGap()));

		jLabel4.setFont(new java.awt.Font("Tahoma", 0, 11)); // NOI18N
		jLabel4.setText("(Group name must be unique and not in used)");

		javax.swing.GroupLayout jPanel1Layout = new javax.swing.GroupLayout(jPanel1);
		jPanel1.setLayout(jPanel1Layout);
		jPanel1Layout.setHorizontalGroup(
				jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING).addGroup(jPanel1Layout
						.createSequentialGroup().addContainerGap().addGroup(jPanel1Layout
								.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING).addComponent(textGroup)
								.addGroup(jPanel1Layout
										.createSequentialGroup().addComponent(jLabel3)
										.addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED,
												javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
										.addComponent(labelGroupError))
								.addGroup(jPanel1Layout.createSequentialGroup().addGroup(jPanel1Layout
										.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
										.addGroup(jPanel1Layout.createSequentialGroup().addComponent(btnCreate)
												.addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
												.addComponent(btnCancel)
												.addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
												.addComponent(jLabel4))
										.addComponent(jPanel5, javax.swing.GroupLayout.PREFERRED_SIZE,
												javax.swing.GroupLayout.DEFAULT_SIZE,
												javax.swing.GroupLayout.PREFERRED_SIZE))
										.addGap(0, 0, Short.MAX_VALUE)))
						.addContainerGap()));
		jPanel1Layout.setVerticalGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
				.addGroup(jPanel1Layout.createSequentialGroup().addContainerGap()
						.addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
								.addComponent(labelGroupError, javax.swing.GroupLayout.PREFERRED_SIZE, 16,
										javax.swing.GroupLayout.PREFERRED_SIZE)
								.addComponent(jLabel3))
						.addGap(10, 10, 10)
						.addComponent(textGroup, javax.swing.GroupLayout.PREFERRED_SIZE,
								javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
						.addGap(18, 18, 18)
						.addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
								.addComponent(btnCreate).addComponent(btnCancel).addComponent(jLabel4))
						.addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED).addComponent(jPanel5,
								javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE,
								Short.MAX_VALUE)));

		jPanel2.setBorder(javax.swing.BorderFactory.createTitledBorder(null, "Group Members",
				javax.swing.border.TitledBorder.DEFAULT_JUSTIFICATION, javax.swing.border.TitledBorder.DEFAULT_POSITION,
				new java.awt.Font("Tahoma", 1, 13))); // NOI18N

		javax.swing.GroupLayout panelUserLayout = new javax.swing.GroupLayout(panelUser);
		panelUser.setLayout(panelUserLayout);
		panelUserLayout.setHorizontalGroup(panelUserLayout
				.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING).addGap(0, 190, Short.MAX_VALUE));
		panelUserLayout.setVerticalGroup(panelUserLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
				.addGap(0, 0, Short.MAX_VALUE));

		javax.swing.GroupLayout jPanel2Layout = new javax.swing.GroupLayout(jPanel2);
		jPanel2.setLayout(jPanel2Layout);
		jPanel2Layout.setHorizontalGroup(
				jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING).addComponent(panelUser,
						javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE));
		jPanel2Layout.setVerticalGroup(
				jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING).addComponent(panelUser,
						javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE));

		jPanel3.setBorder(javax.swing.BorderFactory.createTitledBorder(null, "Groups",
				javax.swing.border.TitledBorder.DEFAULT_JUSTIFICATION, javax.swing.border.TitledBorder.DEFAULT_POSITION,
				new java.awt.Font("Tahoma", 1, 13))); // NOI18N
		jPanel3.setToolTipText("");

		javax.swing.GroupLayout panelGroupLayout = new javax.swing.GroupLayout(panelGroup);
		panelGroup.setLayout(panelGroupLayout);
		panelGroupLayout.setHorizontalGroup(panelGroupLayout
				.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING).addGap(0, 190, Short.MAX_VALUE));
		panelGroupLayout.setVerticalGroup(panelGroupLayout
				.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING).addGap(0, 200, Short.MAX_VALUE));

		javax.swing.GroupLayout jPanel3Layout = new javax.swing.GroupLayout(jPanel3);
		jPanel3.setLayout(jPanel3Layout);
		jPanel3Layout.setHorizontalGroup(
				jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING).addComponent(panelGroup,
						javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE));
		jPanel3Layout.setVerticalGroup(jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
				.addGroup(jPanel3Layout.createSequentialGroup()
						.addComponent(panelGroup, javax.swing.GroupLayout.PREFERRED_SIZE,
								javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
						.addGap(0, 0, Short.MAX_VALUE)));

		jPanel4.setBorder(javax.swing.BorderFactory.createTitledBorder(null, "Conversation",
				javax.swing.border.TitledBorder.DEFAULT_JUSTIFICATION, javax.swing.border.TitledBorder.DEFAULT_POSITION,
				new java.awt.Font("Tahoma", 1, 13))); // NOI18N

		listConversation.setEditable(false);
		listConversation.setColumns(20);
		listConversation.setRows(5);
		jScrollPane3.setViewportView(listConversation);

		javax.swing.GroupLayout jPanel4Layout = new javax.swing.GroupLayout(jPanel4);
		jPanel4.setLayout(jPanel4Layout);
		jPanel4Layout.setHorizontalGroup(jPanel4Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
				.addGroup(jPanel4Layout.createSequentialGroup().addContainerGap()
						.addComponent(jScrollPane3, javax.swing.GroupLayout.DEFAULT_SIZE, 409, Short.MAX_VALUE)
						.addContainerGap()));
		jPanel4Layout.setVerticalGroup(jPanel4Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
				.addComponent(jScrollPane3));

		btnSend.setFont(new java.awt.Font("Tahoma", 1, 13)); // NOI18N
		btnSend.setText("Send Message");
		btnSend.addMouseListener(new java.awt.event.MouseAdapter() {
			public void mouseClicked(java.awt.event.MouseEvent evt) {
				btnSendMouseClicked(evt);
			}
		});

		labelRegisterError.setForeground(new java.awt.Color(255, 59, 48));
		labelRegisterError.setText("                                ");

		labelMessageError.setForeground(new java.awt.Color(255, 59, 48));
		labelMessageError.setText("                             ");

		jLabel1.setFont(new java.awt.Font("Tahoma", 1, 13)); // NOI18N
		jLabel1.setText("User ID");

		jLabel2.setFont(new java.awt.Font("Tahoma", 0, 11)); // NOI18N
		jLabel2.setText("(User ID must not contain spaces, begin with number or more than 8 characters) ");

		jPanel6.setBorder(javax.swing.BorderFactory.createTitledBorder(null, "Online User",
				javax.swing.border.TitledBorder.DEFAULT_JUSTIFICATION, javax.swing.border.TitledBorder.DEFAULT_POSITION,
				new java.awt.Font("Tahoma", 1, 13))); // NOI18N

		scrollPane = new JScrollPane(lvd.getJList());

		javax.swing.GroupLayout jPanel6Layout = new javax.swing.GroupLayout(jPanel6);
		jPanel6Layout.setHorizontalGroup(jPanel6Layout.createParallelGroup(Alignment.LEADING).addComponent(scrollPane,
				GroupLayout.DEFAULT_SIZE, 171, Short.MAX_VALUE));
		jPanel6Layout.setVerticalGroup(jPanel6Layout.createParallelGroup(Alignment.LEADING).addComponent(scrollPane,
				GroupLayout.DEFAULT_SIZE, 306, Short.MAX_VALUE));
		jPanel6.setLayout(jPanel6Layout);

		jPanel7.setBorder(javax.swing.BorderFactory.createTitledBorder(null, "User Profile",
				javax.swing.border.TitledBorder.DEFAULT_JUSTIFICATION, javax.swing.border.TitledBorder.DEFAULT_POSITION,
				new java.awt.Font("Tahoma", 1, 13))); // NOI18N

		btnAddPicture.setText("Select Profile Pic");
		btnAddPicture.addMouseListener(new java.awt.event.MouseAdapter() {
			public void mouseClicked(java.awt.event.MouseEvent evt) {
				btnAddPictureMouseClicked(evt);
			}
		});

		textAreaDescription = new JTextArea();
		textAreaDescription.setEnabled(false);

		btnUpdateProfile = new JButton("Update Profile");
		btnUpdateProfile.setEnabled(false);
		btnUpdateProfile.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {
				sendUserProfile();
			}
		});

		lbProfileDescription = new JLabel("Profile Description");

		javax.swing.GroupLayout jPanel7Layout = new javax.swing.GroupLayout(jPanel7);
		jPanel7Layout.setHorizontalGroup(jPanel7Layout.createParallelGroup(Alignment.TRAILING).addGroup(jPanel7Layout
				.createSequentialGroup()
				.addGroup(jPanel7Layout.createParallelGroup(Alignment.LEADING)
						.addGroup(Alignment.TRAILING,
								jPanel7Layout.createSequentialGroup().addContainerGap().addComponent(
										textAreaDescription, GroupLayout.DEFAULT_SIZE, 233, Short.MAX_VALUE))
						.addGroup(Alignment.TRAILING,
								jPanel7Layout.createSequentialGroup().addContainerGap().addComponent(btnUpdateProfile,
										GroupLayout.DEFAULT_SIZE, 233, Short.MAX_VALUE))
						.addGroup(jPanel7Layout.createSequentialGroup().addContainerGap()
								.addComponent(lbProfileDescription))
						.addGroup(jPanel7Layout.createSequentialGroup().addContainerGap()
								.addGroup(jPanel7Layout.createParallelGroup(Alignment.LEADING)
										.addComponent(btnAddPicture, Alignment.TRAILING, GroupLayout.DEFAULT_SIZE, 233,
												Short.MAX_VALUE)
										.addComponent(lblProfilePic, GroupLayout.DEFAULT_SIZE, 233, Short.MAX_VALUE))))
				.addContainerGap()));
		jPanel7Layout.setVerticalGroup(jPanel7Layout.createParallelGroup(Alignment.LEADING)
				.addGroup(jPanel7Layout.createSequentialGroup().addComponent(btnAddPicture)
						.addPreferredGap(ComponentPlacement.RELATED)
						.addComponent(lblProfilePic, GroupLayout.PREFERRED_SIZE, 218, GroupLayout.PREFERRED_SIZE)
						.addPreferredGap(ComponentPlacement.RELATED).addComponent(lbProfileDescription)
						.addPreferredGap(ComponentPlacement.RELATED, GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
						.addComponent(textAreaDescription, GroupLayout.PREFERRED_SIZE, 55, GroupLayout.PREFERRED_SIZE)
						.addPreferredGap(ComponentPlacement.RELATED).addComponent(btnUpdateProfile).addContainerGap()));
		jPanel7.setLayout(jPanel7Layout);

		javax.swing.GroupLayout layout = new javax.swing.GroupLayout(getContentPane());
		layout.setHorizontalGroup(layout.createParallelGroup(Alignment.LEADING)
				.addGroup(layout.createSequentialGroup().addContainerGap()
						.addGroup(layout.createParallelGroup(Alignment.LEADING).addGroup(layout.createSequentialGroup()
								.addComponent(btnSend).addPreferredGap(ComponentPlacement.UNRELATED)
								.addGroup(layout.createParallelGroup(Alignment.LEADING).addComponent(labelMessageError)
										.addComponent(textMessage, 731, 731, 731)))
								.addGroup(layout.createSequentialGroup()
										.addComponent(jPanel2, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE,
												GroupLayout.PREFERRED_SIZE)
										.addGap(1)
										.addComponent(jPanel3, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE,
												GroupLayout.PREFERRED_SIZE)
										.addPreferredGap(ComponentPlacement.RELATED)
										.addComponent(jPanel4, GroupLayout.DEFAULT_SIZE, 474, Short.MAX_VALUE))
								.addGroup(layout.createSequentialGroup().addGroup(layout
										.createParallelGroup(Alignment.LEADING, false)
										.addGroup(layout.createSequentialGroup()
												.addGroup(layout.createParallelGroup(Alignment.LEADING)
														.addGroup(layout.createSequentialGroup().addComponent(jLabel1)
																.addPreferredGap(ComponentPlacement.RELATED,
																		GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
																.addComponent(labelRegisterError))
														.addComponent(textRegister))
												.addGap(18).addComponent(btnRegister))
										.addComponent(jLabel2, GroupLayout.DEFAULT_SIZE, GroupLayout.DEFAULT_SIZE,
												Short.MAX_VALUE)
										.addComponent(jPanel1, GroupLayout.DEFAULT_SIZE, GroupLayout.DEFAULT_SIZE,
												Short.MAX_VALUE))
										.addGap(18)
										.addComponent(jPanel6, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE,
												GroupLayout.PREFERRED_SIZE)
										.addPreferredGap(ComponentPlacement.RELATED).addComponent(jPanel7,
												GroupLayout.DEFAULT_SIZE, GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)))
						.addContainerGap()));
		layout.setVerticalGroup(layout.createParallelGroup(Alignment.LEADING)
				.addGroup(layout.createSequentialGroup().addGap(10)
						.addGroup(layout.createParallelGroup(Alignment.LEADING, false)
								.addGroup(layout.createSequentialGroup()
										.addGroup(layout.createParallelGroup(Alignment.BASELINE)
												.addComponent(labelRegisterError).addComponent(jLabel1))
										.addPreferredGap(ComponentPlacement.RELATED)
										.addGroup(layout.createParallelGroup(Alignment.BASELINE)
												.addComponent(textRegister, GroupLayout.PREFERRED_SIZE,
														GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE)
												.addComponent(btnRegister))
										.addPreferredGap(ComponentPlacement.RELATED).addComponent(jLabel2)
										.addPreferredGap(ComponentPlacement.UNRELATED)
										.addComponent(jPanel1, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE,
												GroupLayout.PREFERRED_SIZE))
								.addComponent(jPanel7, GroupLayout.DEFAULT_SIZE, 391, Short.MAX_VALUE).addComponent(
										jPanel6, GroupLayout.DEFAULT_SIZE, GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
						.addPreferredGap(ComponentPlacement.RELATED)
						.addGroup(layout.createParallelGroup(Alignment.LEADING, false)
								.addComponent(jPanel4, GroupLayout.DEFAULT_SIZE, GroupLayout.DEFAULT_SIZE,
										Short.MAX_VALUE)
								.addComponent(jPanel3, GroupLayout.DEFAULT_SIZE, GroupLayout.DEFAULT_SIZE,
										Short.MAX_VALUE)
								.addComponent(jPanel2, GroupLayout.DEFAULT_SIZE, GroupLayout.DEFAULT_SIZE,
										Short.MAX_VALUE))
						.addPreferredGap(ComponentPlacement.RELATED)
						.addGroup(layout.createParallelGroup(Alignment.BASELINE).addComponent(btnSend).addComponent(
								textMessage, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE,
								GroupLayout.PREFERRED_SIZE))
						.addPreferredGap(ComponentPlacement.RELATED)
						.addComponent(labelMessageError, GroupLayout.PREFERRED_SIZE, 16, GroupLayout.PREFERRED_SIZE)
						.addContainerGap(GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)));
		getContentPane().setLayout(layout);

		pack();
	}// </editor-fold>//GEN-END:initComponents

	protected void sendUserProfile() {
		try {
			UserProfile map = new UserProfile(username, ImageUtil.getUserFolderPath(username) + username + ".jpg",
					textAreaDescription.getText());
			String sendUserMessage = "sendUserImage::" + map.getByteMessage();
			commonSocket.send(generateMessage(sendUserMessage, commonGroup));

		} catch (IOException e) {
			System.out.println("ERROR SENDING user image");
			e.printStackTrace();
		}
	}

	private void btnLeaveMouseClicked(java.awt.event.MouseEvent evt) {// GEN-FIRST:event_btnLeaveMouseClicked
		// TODO add your handling code here:
		if (activeGroup.isEmpty()) {
			return;
		}
		// TODO delete here
		// Remove active group
		try {
			multicastSocket.send(generateMessage("leaveGroup::" + activeGroup + "::" + username, multicastGroup));
			String ipAddr = joinedGroupList.get(activeGroup);
			joinedGroupChats.remove(ipAddr);
			if (joinedGroupMembers.get(activeGroup).size() == 1) {
				String message = "removeGroup::" + activeGroup;
				commonSocket.send(generateMessage(message, commonGroup));
			}
			joinedGroupList.remove(activeGroup);
			textSelectedGroup.setText("None.");
			updateMemberList();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}// GEN-LAST:event_btnLeaveMouseClicked

	private void btnAddPictureMouseClicked(java.awt.event.MouseEvent evt) {// GEN-FIRST:event_btnAddPictureMouseClicked
		// Create new profile picture
		final ProfilePicture pp = new ProfilePicture(username);
		if (pp.selectProfilePic()) {
			// Set profile on label
			lblProfilePic.setIcon(pp.getImageIconProfilePic(lblProfilePic));
		}
	}// GEN-LAST:event_btnAddPictureMouseClicked

	private void btnRegisterActionPerformed(java.awt.event.ActionEvent evt) {// GEN-FIRST:event_btnRegisterActionPerformed
		// TODO add your handling code here:
		registerName();
	}// GEN-LAST:event_btnRegisterActionPerformed

	private void postInitComponent() {
		listUser = new JTextArea();
		scrollUser = new JScrollPane();
		panelUserCheckbox = new JPanel();
		panelUserCheckbox.setName("CHECK");

		buttonGroup = new ButtonGroup();
		scrollGroup = new JScrollPane();
		groupPane = new JPanel();

		groupPane.setLayout(new BoxLayout(groupPane, BoxLayout.Y_AXIS));
		groupPane.setBounds(10, 20, 200, 200);
		scrollGroup.setViewportView(groupPane);
		scrollGroup.setSize(190, 200);

		listUser.setEditable(false);
		listUser.setColumns(10);
		listUser.setRows(5);
		scrollUser.setViewportView(listUser);
		scrollUser.setName("LIST");

		panelUser.setLayout(new CardLayout());
		panelUser.add(panelUserCheckbox, "CHECK");
		panelUser.add(scrollUser, "LIST");
		panelUser.setPreferredSize(new Dimension(190, 200));
		((CardLayout) panelUser.getLayout()).show(panelUser, "LIST");

		btnCancel.setVisible(false);
		btnCancel.setEnabled(false);

		textRegister.addKeyListener(new KeyAdapter() {
			@Override
			public void keyPressed(KeyEvent e) {
				if (e.getKeyCode() == KeyEvent.VK_ENTER) {
					registerName();
				}
			}
		});

		textGroup.addKeyListener(new KeyAdapter() {
			@Override
			public void keyPressed(KeyEvent e) {
				if (e.getKeyCode() == KeyEvent.VK_ENTER) {
					createGroup();
				}
			}
		});

		textMessage.addKeyListener(new KeyAdapter() {
			@Override
			public void keyPressed(KeyEvent e) {
				if (e.getKeyCode() == KeyEvent.VK_ENTER) {
					sendChatMessage();
				}
			}
		});

	}

	private void btnCreateMouseClicked(java.awt.event.MouseEvent evt) {// GEN-FIRST:event_btnCreateMouseClicked
		// TODO add your handling code here:
		createGroup();
	}// GEN-LAST:event_btnCreateMouseClicked

	private void btnEditMouseClicked(java.awt.event.MouseEvent evt) {// GEN-FIRST:event_btnEditMouseClicked
		// TODO add your handling code here:
		labelEditGroupError.setText("");
		String groupInput = textSelectedGroup.getText();
		if (groupInput != null && joinedGroupList.containsKey(groupInput)) {
			manageGroup newFrame = new manageGroup();
			newFrame.setGroupName(activeGroup);
			newFrame.setUsers(usernameList, joinedGroupMembers.get(groupInput));
			newFrame.setVisible(true);
			// TODO: set the list
		} else if (groupInput.equals("None.")) {
			labelEditGroupError.setText(" No group is selected yet !");
		} else if (!joinedGroupList.containsKey(groupInput)) {
			labelEditGroupError.setText(" Not authorised to edit the group !");
		}
	}// GEN-LAST:event_btnEditMouseClicked

	private void btnDeleteMouseClicked(java.awt.event.MouseEvent evt) {// GEN-FIRST:event_btnDeleteMouseClicked
		// TODO add your handling code here:
		if (activeGroup.isEmpty()) {
			return;
		}
		try {
			multicastSocket.send(generateMessage("removeGroup", multicastGroup));
			String message = "removeGroup::" + activeGroup;
			commonSocket.send(generateMessage(message, commonGroup));
			activeGroup = "";
			textSelectedGroup.setText("None.");
		} catch (IOException e) {
			System.out.println("Delete group error: " + e);
			e.printStackTrace();
		}
	}// GEN-LAST:event_btnDeleteMouseClicked

	private void btnSendMouseClicked(java.awt.event.MouseEvent evt) {// GEN-FIRST:event_btnSendMouseClicked
		// TODO add your handling code here:
		sendChatMessage();
	}// GEN-LAST:event_btnSendMouseClicked

	private void formWindowClosing(java.awt.event.WindowEvent evt) {// GEN-FIRST:event_formWindowClosing
		// TODO add your handling code here:
		try {
			for (String group : joinedGroupList.keySet()) {
				activeGroup = group;
				multicastSocket.send(generateMessage("leaveGroup::" + group + "::" + username, getActiveInet()));
				if (joinedGroupMembers.get(group) != null && joinedGroupMembers.get(group).size() == 1) {
					String message = "removeGroup::" + group;
					commonSocket.send(generateMessage(message, commonGroup));
				}
			}
			if (!username.isEmpty()) {
				String getUserMessage = "removeUser::" + username;
				commonSocket.send(generateMessage(getUserMessage, commonGroup));
			}
			// Clear user and its associated images
			ImageUtil.deleteUserFolder(username);
		} catch (IOException ex) {
			ex.printStackTrace();
		}
	}// GEN-LAST:event_formWindowClosing

	private void btnCancelMouseClicked(java.awt.event.MouseEvent evt) {// GEN-FIRST:event_btnCancelMouseClicked
		// TODO add your handling code here:
		((CardLayout) panelUser.getLayout()).show(panelUser, "LIST");
		panelUserCheckbox.removeAll();
		labelGroupError.setText("");
		btnCreate.setText("Create");
		btnCancel.setVisible(false);
		btnCancel.setEnabled(false);
	}// GEN-LAST:event_btnCancelMouseClicked

	/**
	 * @param args
	 *            the command line arguments
	 */
	public static void main(String args[]) {
		/* Set the Nimbus look and feel */
		// <editor-fold defaultstate="collapsed" desc=" Look and feel setting
		// code (optional) ">
		/*
		 * If Nimbus (introduced in Java SE 6) is not available, stay with the default
		 * look and feel. For details see
		 * http://download.oracle.com/javase/tutorial/uiswing/lookandfeel/plaf. html
		 */
		try {
			for (javax.swing.UIManager.LookAndFeelInfo info : javax.swing.UIManager.getInstalledLookAndFeels()) {
				if ("Nimbus".equals(info.getName())) {
					javax.swing.UIManager.setLookAndFeel(info.getClassName());
					break;
				}
			}
		} catch (ClassNotFoundException ex) {
			java.util.logging.Logger.getLogger(WhatsChat.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
		} catch (InstantiationException ex) {
			java.util.logging.Logger.getLogger(WhatsChat.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
		} catch (IllegalAccessException ex) {
			java.util.logging.Logger.getLogger(WhatsChat.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
		} catch (javax.swing.UnsupportedLookAndFeelException ex) {
			java.util.logging.Logger.getLogger(WhatsChat.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
		}
		// </editor-fold>

		/* Create and display the form */
		java.awt.EventQueue.invokeLater(new Runnable() {
			public void run() {
				new WhatsChat().setVisible(true);
			}
		});
	}

	public void sendChatMessage() {
		String messageInput = textMessage.getText();
		if (messageInput != null && !(messageInput.isEmpty())) {
			labelMessageError.setText("");
			// TODO: Send Message
			String message = username + " : " + messageInput;
			if (activeGroup != "") {
				try {
					multicastSocket.send(generateMessage(message, getActiveInet()));
				} catch (IOException ex) {
					ex.printStackTrace();
				}
				textMessage.setText("");
			} else {
				labelMessageError.setText(" No active group to send message !");
			}
		} else {
			labelMessageError.setText(" Cannot send empty message !");
		}
	}

	public void createGroup() {
		if (!(panelUser.getComponent(0).isVisible())) {
			JCheckBox userCB;
			panelUserCheckbox.removeAll();
			for (String user : usernameList) {
				userCB = new JCheckBox(user);
				userCB.setName(user);
				panelUserCheckbox.add(userCB);
			}
			panelUserCheckbox.setAlignmentY(JComponent.LEFT_ALIGNMENT);
			((CardLayout) panelUser.getLayout()).show(panelUser, "CHECK");
			btnCreate.setText("Save");
			btnCancel.setVisible(true);
			btnCancel.setEnabled(true);
		} else {
			String groupInput = textGroup.getText();
			List<String> checkedUsers = new ArrayList<String>();
			for (Component userCB : panelUserCheckbox.getComponents()) {
				if (((JCheckBox) userCB).isSelected()) {
					checkedUsers.add(userCB.getName());
				}
			}
			labelGroupError.setText("");
			if (groupInput.isEmpty()) {
				labelGroupError.setText(" Group name cannot be empty !");
			} else if (checkedUsers.isEmpty()) {
				labelGroupError.setText(" None of the users invited !");
			} else {
				int ip = groupInput.hashCode();
				// Limiting the first two group of IP Address to 230.1 for
				// usable Multicast address
				String ipStr = String.format("230.1.%d.%d", (ip & 0xff), (ip >> 8 & 0xff));
				if (!groupList.containsKey(groupInput)) {
					try {
						String createMessage = "sendGroup::" + groupInput + "::" + ipStr;
						commonSocket.send(generateMessage(createMessage, commonGroup));
						joinedGroupList.put(groupInput, ipStr);
						groupList.put(groupInput, ipStr);
						activeGroup = groupInput;
						for (String checkedUser : checkedUsers) {
							String inviteMessage = "inviteUser::" + checkedUser + "::" + groupInput + "::" + ipStr;
							commonSocket.send(generateMessage(inviteMessage, commonGroup));
						}
						((CardLayout) panelUser.getLayout()).show(panelUser, "LIST");
						textGroup.setText("");
						btnCreate.setText("Create");
						btnCancel.setVisible(false);
						btnCancel.setEnabled(false);
					} catch (IOException ex) {
						ex.printStackTrace();
					}
				} else {
					labelGroupError.setText(" Group already exist !");
				}
			}
		}
	}

	public void registerName() {
		// TODO add your handling code here:
		String usernameInput = textRegister.getText();
		if (usernameValid(usernameInput)) {
			labelRegisterError.setText("");
			// TODO: Register User
			if (!(usernameList.contains(usernameInput))) {
				try {
					username = usernameInput;
					String sendUserMessage = "sendUser::" + username;
					commonSocket.send(generateMessage(sendUserMessage, commonGroup));

					File file = new File("no_img.png");
					BufferedImage image;
					image = ImageIO.read(file);
					Image dimg = image.getScaledInstance(lblProfilePic.getWidth(), lblProfilePic.getHeight(),
							Image.SCALE_SMOOTH);
					lblProfilePic.setIcon(new ImageIcon(dimg));

				} catch (IOException ex) {
					ex.printStackTrace();
				}
				btnAddPicture.setEnabled(true);
				textAreaDescription.setEnabled(true);
				btnUpdateProfile.setEnabled(true);
				btnAddPicture.setVisible(true);
				btnAddPicture.setVisible(true);
				btnRegister.setEnabled(false);
				textRegister.setEnabled(false);
			} else {
				labelRegisterError.setText("Username exist!");
			}
		} else {
			labelRegisterError.setText("Invalid Username !");
		}
	}

	public boolean usernameValid(String username) {
		if (username == null || username.isEmpty() || username.length() > 8) {
			return false;
		} else if (username.contains(" ") || username.matches("^\\d.+")) {
			return false;
		} else {
			return true;
		}
	}

	public void updateMemberList() {
		String memberList = "";
		if (!activeGroup.isEmpty()) {
			for (String member : joinedGroupMembers.get(activeGroup)) {
				memberList += member + "\n";
			}
		}
		listUser.setText(memberList);
		listUser.revalidate();
	}

	public void updateGroupList() {
		List<String> groups = new ArrayList<String>(joinedGroupList.keySet());
		String groupNameList = "";
		JRadioButton button;
		groupPane.removeAll();
		for (String group : groups) {
			groupNameList = group + " : " + groupList.get(group);
			button = new JRadioButton(groupNameList);
			button.setName(group);
			if (activeGroup.equals(group)) {
				button.setSelected(true);
				textSelectedGroup.setText(activeGroup);
			}
			button.addItemListener(new ItemListener() {
				@Override
				public void itemStateChanged(ItemEvent event) {
					if (event.getStateChange() == ItemEvent.SELECTED) {
						activeGroup = ((JRadioButton) event.getItem()).getName();
						textSelectedGroup.setText(activeGroup);
						updateConversation();
					}
				}
			});
			button.setVisible(true);
			buttonGroup.add(button);
			groupPane.add(button);
		}
		panelGroup.add(scrollGroup);
		panelGroup.revalidate();
		panelGroup.repaint();
	}

	public DatagramPacket generateMessage(String message, InetAddress group) {
		byte[] buf = message.getBytes();
		DatagramPacket dgp = new DatagramPacket(buf, buf.length, group, portNo);
		return dgp;
	}

	public InetAddress getActiveInet() {
		try {
			return InetAddress.getByName(joinedGroupList.get(activeGroup));
		} catch (UnknownHostException ex) {
			ex.printStackTrace();
		}
		return null;
	}

	public void updateConversation() {
		listConversation.setText(joinedGroupChats.get(joinedGroupList.get(activeGroup)));
		listConversation.repaint();
	}

	public void updateGroupMembers(String groupInput) {
		String getMembersMessage = "getMembers::" + groupInput;
		try {
			commonSocket.send(generateMessage(getMembersMessage, commonGroup));
		} catch (IOException ex) {
			ex.printStackTrace();
		}
	}

	// private Lock lock = new Lock();
	// TODO THREADNOT CLOSED PROPERLY WHEN CLOSING GROUP
	public void joinGroup(String ipStr) {
		try {

			multicastGroup = InetAddress.getByName(ipStr);
			multicastSocket = new MulticastSocket(portNo);
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
							// System.out.println("Received joingroup packet: " + msg);
							String subString = "";
							if (msg.length() > 11) {
								subString = msg.substring(0, 10);
							}
							if (msg.equals("removeGroup")) {
								// Stop current thread
								// System.out.println("Stop thread: " + Thread.currentThread().getId());
								Thread.currentThread().interrupt();
								// System.out.println("Thread after stopping: " +
								// Thread.currentThread().getId());
							} else if (subString.equals("leaveGroup")) {
								String[] msgArray = msg.split("::");
								List<String> members = joinedGroupMembers.get(msgArray[1]);
								if (members.size() > 0) {
									members.remove(msgArray[2]);
									joinedGroupMembers.put(msgArray[1], members);
									if (!msgArray[2].equals(username)) {
										String message = msgArray[2] + " has left the group...";
										String previousChats = joinedGroupChats.get(ipStr);
										joinedGroupChats.put(ipStr, previousChats + "\n" + message);
									} else {
										activeGroup = "";
									}
									updateConversation();
									updateGroupList();
									updateMemberList();
								}
							} else {
								if (joinedGroupChats.get(ipStr) != null) {
									String[] chats = joinedGroupChats.get(ipStr).split("\n");
									if (!chats[chats.length - 1].equals(msg)) {
										String previousChats = joinedGroupChats.get(ipStr);
										joinedGroupChats.put(ipStr, previousChats + "\n" + msg);
									} else {
										joinedGroupChats.put(ipStr, msg);
									}
								} else {
									joinedGroupChats.put(ipStr, msg);
								}
								updateConversation();
								updateMemberList();
							}

							// lock.unlock();
						} catch (IOException ex) {
							System.out.println("thread run ex: " + ex);
							ex.printStackTrace();
						}
					}
				}
			}).start();
			String message = username + " joined " + ipStr;
			// System.out.println("join group");
			multicastSocket.send(generateMessage(message, multicastGroup));
		} catch (IOException ex) {
			ex.printStackTrace();
		}
	}

	public void showUserProfile(String usernameClicked) {

		ProfilePicture pp = new ProfilePicture(usernameClicked);
		lblProfilePic.setIcon(pp.getImageIconProfilePic(lblProfilePic));

		if (usernameClicked.equals(username)) {
			// Allow edit
			textAreaDescription.setEnabled(true);
			btnAddPicture.setEnabled(true);
			btnUpdateProfile.setEnabled(true);
		} else {
			// Disallow editing
			textAreaDescription.setEnabled(false);
			btnAddPicture.setEnabled(false);
			btnUpdateProfile.setEnabled(false);
		}

		textAreaDescription.setText("");
		for (UserProfile profile : userImageMapList) {
			if (profile.getUsername().equals(usernameClicked)) {
				textAreaDescription.setText(profile.getTextDescription());
			}
		}

	}
}
