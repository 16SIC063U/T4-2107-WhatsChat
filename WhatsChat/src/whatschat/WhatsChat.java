/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package whatschat;

import java.awt.BorderLayout;
import java.awt.CardLayout;
import java.awt.Checkbox;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.GridLayout;
import java.awt.ScrollPane;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectOutput;
import java.io.ObjectOutputStream;
import java.net.DatagramPacket;
import java.net.InetAddress;
import java.net.MulticastSocket;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import javax.swing.BoxLayout;
import javax.swing.ButtonGroup;
import javax.swing.JCheckBox;
import javax.swing.JComponent;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.GroupLayout.Alignment;
import javax.swing.GroupLayout;
import javax.swing.ImageIcon;
import javax.swing.LayoutStyle.ComponentPlacement;

import user.ProfilePicture;
import user.UserImageMap;
import user.manageGroup;

import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.SwingConstants;

/**
 *
 * @author Juun
 */
public class WhatsChat extends javax.swing.JFrame {
	static volatile HashMap createdGroup = new HashMap();
	static final int portNo = 6789;
	static volatile List<String> usernameList = new ArrayList();
	static volatile Map<String, String> groupList = new HashMap();
	static volatile boolean messageFlag = false;
	Map<String, String> joinedGroupList = new HashMap();
	Map<String, String> joinedGroupChats = new HashMap();
        Map<String, List<String>> joinedGroupMembers = new HashMap();
	// static volatile boolean onHold = false;
	MulticastSocket multicastSocket = null, commonSocket = null;
	InetAddress multicastGroup = null, commonGroup = null;
	String username = "", activeGroup = "";
	Thread commonThread = null;

	// To store a mapping of username and its matching profile pic
	List<UserImageMap> userImageMapList = new ArrayList<>();
	
	/**
	 * Creates new form WhatsChat
	 */
	public WhatsChat() {
            initComponents();
            postInitComponent();
            try {
                commonGroup = InetAddress.getByName("230.1.1.1");
                commonSocket = new MulticastSocket(portNo);
                commonSocket.joinGroup(commonGroup);
                commonThread = new Thread(new Runnable() {
                    @Override
                    public void run() {
                        byte rcvBuf[] = new byte[1000];
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
                                else if (action.equals("getUserImage") && !(userImageMapList.isEmpty())) {
                                    for (UserImageMap userImageMap : userImageMapList) {
                                        String sendUserImageMessage = "sendUserImage::" + userImageMap.getByteMessage();
                                        commonSocket.send(generateMessage(sendUserImageMessage, commonGroup));
                                    }
                                }
                                else if (action.equals("getGroup") && !(groupList.isEmpty())) {
                                    String sendGroupMessage = "";
                                    for (String group : groupList.keySet()) {
                                        sendGroupMessage = "sendGroup::" + group + "::" + groupList.get(group);
                                        commonSocket.send(generateMessage(sendGroupMessage, commonGroup));
                                    }
                                } else if (action.equals("sendUser")) {
                                    if (!(usernameList.contains(parameter))) {
                                        usernameList.add(parameter);
                                    }
                                    updateUserList();
                                }else if (action.equals("sendUserImage")) {
                                    System.out.println("Send user image");
                                    UserImageMap map = new UserImageMap(parameter.getBytes());

                                    boolean isInList = false;
                                    for(UserImageMap userImageMap: userImageMapList){
                                        if(userImageMap.getUsername().equals(map.getUsername())){
                                            isInList = true;
                                        }
                                    }
                                    //Add if not in list
                                    if(!isInList){
                                        System.out.println("not in list");
                                        userImageMapList.add(map);
                                    }
                                    for(UserImageMap u: userImageMapList){
                                        System.out.println("UN: " + u.getUsername());
                                    }
                                } 
                                else if (action.equals("sendGroup")) {
                                    if (!(groupList.containsKey(parameter))) {
                                        String detail = msgArray[2];
                                        groupList.put(parameter, detail);
                                    }
                                } else if (action.equals("inviteUser")) {
                                    if (parameter.equals(username)) {
                                        joinedGroupList.put(msgArray[2], msgArray[3]);
                                        joinedGroupMembers.put(msgArray[2], new ArrayList<>());
                                        updateGroupMembers(msgArray[2]);
                                        joinGroup(msgArray[3]);
                                        updateGroupList();
                                        updateConversation();
                                    }
                                } else if (action.equals("removeUser")) {
                                    if (usernameList.contains(parameter)) {
                                        usernameList.remove(parameter);
                                    }
                                    updateUserList();
                                } else if (action.equals("getMembers")){
                                    if(joinedGroupList.containsKey(parameter)){
                                        String sendMemberMessage = "sendMember::"+parameter+"::"+username;
                                        commonSocket.send(generateMessage(sendMemberMessage, commonGroup));
                                    }
                                } else if (action.equals("sendMember")){
                                    if(joinedGroupList.containsKey(parameter)){
                                        List<String> members = joinedGroupMembers.get(parameter);
                                        if(!members.contains(msgArray[2])){
                                            members.add(msgArray[2]);
                                            joinedGroupMembers.put(parameter, members);
                                        }
                                    }
                                } else if(action.equals("addMember")){
                                    int ip = msgArray[2].hashCode();
                                    String ipStr = String.format("230.1.%d.%d", (ip & 0xff), (ip >> 8 & 0xff));
                                    if(joinedGroupChats.containsKey(ipStr)){
                                        System.out.println("addMember "+ipStr);
                                        String sendChats = "sendChats::"+msgArray[2]+"::"+joinedGroupChats.get(ipStr);
                                        commonSocket.send(generateMessage(sendChats,commonGroup));
                                    }
                                    if(username.equals(parameter)){
                                        joinedGroupList.put(msgArray[2], ipStr);
                                        joinedGroupMembers.put(msgArray[2], new ArrayList<>());
                                        updateGroupMembers(msgArray[2]);
                                        joinGroup(ipStr);
                                        updateGroupList();
                                        updateConversation();
                                    }
                                } else if(action.equals("removeMember")){
                                    if(username.equals(parameter)){
                                        joinedGroupList.remove(msgArray[2]);
                                        joinedGroupMembers.remove(msgArray[2]);
                                        updateGroupList();
                                        updateConversation();
                                    }
                                    if(joinedGroupMembers.containsKey(msgArray[2])){
                                        List<String> members = joinedGroupMembers.get(msgArray[2]);
                                        members.remove(parameter);
                                        joinedGroupMembers.put(msgArray[2],members);
                                        updateGroupList();
                                        updateConversation();
                                    }
                                } else if(action.equals("sendChats")){
                                    int ip = parameter.hashCode();
                                    String ipStr = String.format("230.1.%d.%d", (ip & 0xff), (ip >> 8 & 0xff));
                                    if(joinedGroupList.containsKey(parameter)){
                                        joinedGroupChats.put(ipStr, msgArray[2]);
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
                commonSocket.send(generateMessage(getUserMessage, commonGroup));
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
	// Code">//GEN-BEGIN:initComponents
	private void initComponents() {

		btnRegister = new javax.swing.JButton();
		textRegister = new javax.swing.JTextField();
		jPanel1 = new javax.swing.JPanel();
		btnCreate = new javax.swing.JButton();
		btnEdit = new javax.swing.JButton();
		btnDelete = new javax.swing.JButton();
		textGroup = new javax.swing.JTextField();
		labelGroupError = new javax.swing.JLabel();
		btnCancel = new javax.swing.JButton();
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

		setDefaultCloseOperation(javax.swing.WindowConstants.EXIT_ON_CLOSE);
		addWindowListener(new java.awt.event.WindowAdapter() {
			public void windowClosing(java.awt.event.WindowEvent evt) {
				formWindowClosing(evt);
			}
		});

		btnRegister.setText("Register User");
		btnRegister.setToolTipText("");
		btnRegister.addMouseListener(new java.awt.event.MouseAdapter() {
			public void mouseClicked(java.awt.event.MouseEvent evt) {
				btnRegisterMouseClicked(evt);
			}
		});

		jPanel1.setBorder(javax.swing.BorderFactory.createTitledBorder("Group Management"));

		btnCreate.setText("Create");
		btnCreate.addMouseListener(new java.awt.event.MouseAdapter() {
			public void mouseClicked(java.awt.event.MouseEvent evt) {
				btnCreateMouseClicked(evt);
			}
		});

		btnEdit.setText("Edit");
		btnEdit.addMouseListener(new java.awt.event.MouseAdapter() {
			public void mouseClicked(java.awt.event.MouseEvent evt) {
				btnEditMouseClicked(evt);
			}
		});

		btnDelete.setText("Delete");
		btnDelete.setToolTipText("");
		btnDelete.addMouseListener(new java.awt.event.MouseAdapter() {
			public void mouseClicked(java.awt.event.MouseEvent evt) {
				btnDeleteMouseClicked(evt);
			}
		});

		labelGroupError.setForeground(new java.awt.Color(255, 59, 48));
		labelGroupError.setText("                          ");

		btnCancel.setText("Cancel");
		btnCancel.addMouseListener(new java.awt.event.MouseAdapter() {
			public void mouseClicked(java.awt.event.MouseEvent evt) {
				btnCancelMouseClicked(evt);
			}
		});

		javax.swing.GroupLayout jPanel1Layout = new javax.swing.GroupLayout(jPanel1);
		jPanel1.setLayout(jPanel1Layout);
		jPanel1Layout.setHorizontalGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
				.addGroup(jPanel1Layout.createSequentialGroup().addContainerGap().addGroup(jPanel1Layout
						.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
						.addGroup(jPanel1Layout.createSequentialGroup().addComponent(btnCreate)
								.addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
								.addComponent(btnCancel)
								.addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED,
										javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
								.addComponent(btnEdit)
								.addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
								.addComponent(btnDelete).addGap(5, 5, 5))
						.addComponent(textGroup).addGroup(jPanel1Layout.createSequentialGroup()
								.addComponent(labelGroupError).addGap(0, 0, Short.MAX_VALUE)))
						.addContainerGap()));
		jPanel1Layout
				.setVerticalGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
						.addGroup(javax.swing.GroupLayout.Alignment.TRAILING, jPanel1Layout.createSequentialGroup()
								.addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
								.addComponent(textGroup, javax.swing.GroupLayout.PREFERRED_SIZE,
										javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
								.addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
								.addComponent(labelGroupError)
								.addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
								.addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
										.addComponent(btnCreate).addComponent(btnEdit).addComponent(btnDelete)
										.addComponent(btnCancel))));

		jPanel2.setBorder(javax.swing.BorderFactory.createTitledBorder("Online Users"));

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

		jPanel3.setBorder(javax.swing.BorderFactory.createTitledBorder("Groups"));

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

		jPanel4.setBorder(javax.swing.BorderFactory.createTitledBorder("Conversation"));

		listConversation.setEditable(false);
		listConversation.setColumns(20);
		listConversation.setRows(5);
		jScrollPane3.setViewportView(listConversation);

		javax.swing.GroupLayout jPanel4Layout = new javax.swing.GroupLayout(jPanel4);
		jPanel4.setLayout(jPanel4Layout);
		jPanel4Layout.setHorizontalGroup(jPanel4Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
				.addGroup(jPanel4Layout.createSequentialGroup().addContainerGap()
						.addComponent(jScrollPane3, javax.swing.GroupLayout.DEFAULT_SIZE, 359, Short.MAX_VALUE)
						.addContainerGap()));
		jPanel4Layout.setVerticalGroup(jPanel4Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
				.addComponent(jScrollPane3));

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

		btnAddPicture = new JButton("Add Pic");
		btnAddPicture.setVisible(false);
		btnAddPicture.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {
				// Add profile pic
				
				ProfilePicture pp = new ProfilePicture();
				
				if (pp.selectProfilePic(username)) {
					// Set profile on panel
					lblProfilePic.setIcon(pp.getImageIconProfilePic(lblProfilePic));
					UserImageMap userImageMap = new UserImageMap(username, pp.getProfilePic());
					// Update other user of profile pic
					try {
						String sendUserMessage = "sendUserImage::" + userImageMap.getByteMessage();
						commonSocket.send(generateMessage(sendUserMessage, commonGroup));
					} catch (IOException e) {
						System.out.println("ERROR SENDING user image");
						e.printStackTrace();
					}
				}
			}
		});

		lblProfilePic = new JLabel("");

		javax.swing.GroupLayout layout = new javax.swing.GroupLayout(getContentPane());
		layout.setHorizontalGroup(
			layout.createParallelGroup(Alignment.LEADING)
				.addGroup(layout.createSequentialGroup()
					.addContainerGap()
					.addGroup(layout.createParallelGroup(Alignment.LEADING)
						.addGroup(layout.createSequentialGroup()
							.addGroup(layout.createParallelGroup(Alignment.TRAILING, false)
								.addComponent(jPanel1, GroupLayout.DEFAULT_SIZE, GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
								.addGroup(layout.createSequentialGroup()
									.addComponent(btnRegister)
									.addPreferredGap(ComponentPlacement.RELATED)
									.addComponent(textRegister, GroupLayout.PREFERRED_SIZE, 200, GroupLayout.PREFERRED_SIZE)))
							.addPreferredGap(ComponentPlacement.RELATED)
							.addComponent(labelRegisterError)
							.addPreferredGap(ComponentPlacement.RELATED, 250, Short.MAX_VALUE)
							.addGroup(layout.createParallelGroup(Alignment.LEADING)
								.addGroup(layout.createSequentialGroup()
									.addGap(10)
									.addComponent(btnAddPicture, GroupLayout.PREFERRED_SIZE, 88, GroupLayout.PREFERRED_SIZE))
								.addComponent(lblProfilePic, Alignment.TRAILING, GroupLayout.PREFERRED_SIZE, 107, GroupLayout.PREFERRED_SIZE)))
						.addGroup(layout.createSequentialGroup()
							.addComponent(btnSend)
							.addPreferredGap(ComponentPlacement.UNRELATED)
							.addGroup(layout.createParallelGroup(Alignment.LEADING)
								.addComponent(labelMessageError)
								.addComponent(textMessage, 691, 691, 691)))
						.addGroup(layout.createSequentialGroup()
							.addComponent(jPanel2, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE)
							.addGap(1)
							.addComponent(jPanel3, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE)
							.addPreferredGap(ComponentPlacement.RELATED)
							.addComponent(jPanel4, GroupLayout.DEFAULT_SIZE, GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)))
					.addContainerGap())
		);
		layout.setVerticalGroup(
			layout.createParallelGroup(Alignment.LEADING)
				.addGroup(layout.createSequentialGroup()
					.addContainerGap()
					.addGroup(layout.createParallelGroup(Alignment.LEADING)
						.addGroup(layout.createSequentialGroup()
							.addGroup(layout.createParallelGroup(Alignment.BASELINE)
								.addComponent(btnRegister)
								.addComponent(textRegister, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE)
								.addComponent(labelRegisterError))
							.addPreferredGap(ComponentPlacement.RELATED)
							.addComponent(jPanel1, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE))
						.addGroup(layout.createSequentialGroup()
							.addComponent(lblProfilePic, GroupLayout.PREFERRED_SIZE, 100, GroupLayout.PREFERRED_SIZE)
							.addPreferredGap(ComponentPlacement.RELATED)
							.addComponent(btnAddPicture)))
					.addPreferredGap(ComponentPlacement.UNRELATED)
					.addGroup(layout.createParallelGroup(Alignment.LEADING, false)
						.addComponent(jPanel4, GroupLayout.DEFAULT_SIZE, GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
						.addComponent(jPanel3, GroupLayout.DEFAULT_SIZE, GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
						.addComponent(jPanel2, GroupLayout.DEFAULT_SIZE, GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
					.addPreferredGap(ComponentPlacement.RELATED)
					.addGroup(layout.createParallelGroup(Alignment.BASELINE)
						.addComponent(btnSend)
						.addComponent(textMessage, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE))
					.addPreferredGap(ComponentPlacement.RELATED)
					.addComponent(labelMessageError, GroupLayout.PREFERRED_SIZE, 16, GroupLayout.PREFERRED_SIZE)
					.addContainerGap(GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
		);
		getContentPane().setLayout(layout);

		pack();
	}// </editor-fold>//GEN-END:initComponents

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
	}

	private void btnRegisterMouseClicked(java.awt.event.MouseEvent evt) {// GEN-FIRST:event_btnRegisterMouseClicked
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
                        btnAddPicture.setVisible(true);
                    } catch (IOException ex) {
                        ex.printStackTrace();
                    }
                } else {
                    labelRegisterError.setText("Username exist!");
                }
            } else {
                labelRegisterError.setText("Invalid Username !");
            }
	}// GEN-LAST:event_btnRegisterMouseClicked

	private void btnCreateMouseClicked(java.awt.event.MouseEvent evt) {// GEN-FIRST:event_btnCreateMouseClicked
            // TODO add your handling code here:
            if (!(panelUser.getComponent(0).isVisible())) {
                JCheckBox userCB;
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
                        } catch (IOException ex) {
                            ex.printStackTrace();
                        }
                    } else {
                            labelGroupError.setText(" Group already exist !");
                    }
                }
            }
	}// GEN-LAST:event_btnCreateMouseClicked

	private void btnEditMouseClicked(java.awt.event.MouseEvent evt) {// GEN-FIRST:event_btnEditMouseClicked
		// TODO add your handling code here:
                String groupInput = textGroup.getText();
                if (groupInput != null && joinedGroupList.containsKey(groupInput)) {
                    manageGroup newFrame = new manageGroup();
                    newFrame.setGroupName(textGroup.getText());
                    newFrame.setUsers(usernameList,joinedGroupMembers.get(groupInput));
                    newFrame.setVisible(true);
                    //TODO: set the list
		}
                else if(textGroup.getText() == null || textGroup.getText().isEmpty()){
                    labelGroupError.setText(" Group name to be edited cannot be empty !");
                }
                else if(!joinedGroupList.containsKey(textGroup.getText())){
                    labelGroupError.setText(" Not authorised to edit the group !");
                }
	}// GEN-LAST:event_btnEditMouseClicked

	private void btnDeleteMouseClicked(java.awt.event.MouseEvent evt) {// GEN-FIRST:event_btnDeleteMouseClicked
		// TODO add your handling code here:
                System.out.println(joinedGroupChats.toString());
	}// GEN-LAST:event_btnDeleteMouseClicked

	private void btnSendMouseClicked(java.awt.event.MouseEvent evt) {// GEN-FIRST:event_btnSendMouseClicked
            // TODO add your handling code here:
            String messageInput = textMessage.getText();
            if (messageInput != null && !(messageInput.isEmpty())) {
                labelMessageError.setText("");
                // TODO: Send Message
                String message = username + " : " + messageInput;
                if(activeGroup != ""){
                    try {
                        multicastSocket.send(generateMessage(message, getActiveInet()));
                    } catch (IOException ex) {
                        ex.printStackTrace();
                    }
                    textMessage.setText("");
                }
                else{
                    labelMessageError.setText(" No active group to send message !");
                }
            } else {
                labelMessageError.setText(" Cannot send empty message !");
            }
	}// GEN-LAST:event_btnSendMouseClicked

	private void formWindowClosing(java.awt.event.WindowEvent evt) {// GEN-FIRST:event_formWindowClosing
            // TODO add your handling code here:
            if (!username.isEmpty()) {
                try {
                    String getUserMessage = "removeUser::" + username;
                    commonSocket.send(generateMessage(getUserMessage, commonGroup));
                } catch (IOException ex) {
                    ex.printStackTrace();
                }
            }
	}// GEN-LAST:event_formWindowClosing

	private void btnCancelMouseClicked(java.awt.event.MouseEvent evt) {// GEN-FIRST:event_btnCancelMouseClicked
            // TODO add your handling code here:
            ((CardLayout) panelUser.getLayout()).show(panelUser, "LIST");
            panelUserCheckbox.removeAll();
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
             * If Nimbus (introduced in Java SE 6) is not available, stay with the
             * default look and feel. For details see
             * http://download.oracle.com/javase/tutorial/uiswing/lookandfeel/plaf.
             * html
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

	public boolean usernameValid(String username) {
            if (username == null || username.isEmpty() || username.length() > 8) {
                return false;
            } else if (username.contains(" ") || username.matches("^\\d.+")) {
                return false;
            } else {
                return true;
            }
	}

	public void updateUserList() {
            String userList = "";
            for (String user : usernameList) {
                userList += user + "\n";
            }
            listUser.setText(userList);
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
                }
                button.addItemListener(new ItemListener() {
                    @Override
                    public void itemStateChanged(ItemEvent event) {
                        if (event.getStateChange() == ItemEvent.SELECTED) {
                            activeGroup = ((JRadioButton) event.getItem()).getName();
                            updateConversation();
                        }
                    }
                });
                button.setVisible(true);
                buttonGroup.add(button);
                groupPane.add(button);
            }
            panelGroup.add(scrollGroup);
            panelGroup.repaint();
            panelGroup.revalidate();
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
        
        public void updateGroupMembers(String groupInput){
            String getMembersMessage = "getMembers::" + groupInput;
            try{
                commonSocket.send(generateMessage(getMembersMessage, commonGroup));
            }
            catch(IOException ex){
                ex.printStackTrace();
            }
        }

	public void joinGroup(String ipStr) {
            try {
                multicastGroup = InetAddress.getByName(ipStr);
                multicastSocket = new MulticastSocket(portNo);
                multicastSocket.joinGroup(multicastGroup);
                // Create a new thread to keep listening for packets from the group
                new Thread(new Runnable() {
                    @Override
                    public void run() {
                        byte buf1[] = new byte[1000];
                        DatagramPacket dgpReceived = new DatagramPacket(buf1, buf1.length);
                        while (true) {
                            try {
                                multicastSocket.receive(dgpReceived);
                                byte[] receivedData = dgpReceived.getData();
                                int length = dgpReceived.getLength();
                                String msg = new String(receivedData, 0, length);
                                if (joinedGroupChats.get(ipStr) != null) {
                                    String previousChats = joinedGroupChats.get(ipStr);
                                    joinedGroupChats.put(ipStr, previousChats + "\n" + msg);
                                } else {
                                    System.out.println(username+" newchat");
                                    joinedGroupChats.put(ipStr, msg);
                                }
                                updateConversation();
                            } catch (IOException ex) {
                                ex.printStackTrace();
                            }
                        }
                    }
                }).start();
                String message = username + " joined " + ipStr;
                multicastSocket.send(generateMessage(message, multicastGroup));
            } catch (IOException ex) {
                ex.printStackTrace();
            }
	}

	// Variables declaration - do not modify//GEN-BEGIN:variables
	private javax.swing.JButton btnCancel;
	private javax.swing.JButton btnCreate;
	private javax.swing.JButton btnDelete;
	private javax.swing.JButton btnAddPicture;
	private javax.swing.JButton btnEdit;
	private javax.swing.JButton btnRegister;
	private javax.swing.JButton btnSend;
	private javax.swing.JPanel jPanel1;
	private javax.swing.JPanel jPanel2;
	private javax.swing.JPanel jPanel3;
	private javax.swing.JPanel jPanel4;
	private javax.swing.JScrollPane jScrollPane3;
	private javax.swing.JLabel labelGroupError;
	private javax.swing.JLabel labelMessageError;
	private javax.swing.JLabel labelRegisterError;
	private javax.swing.JTextArea listConversation;
	private javax.swing.JPanel panelGroup;
	private javax.swing.JPanel panelUser;
	private javax.swing.JTextField textGroup;
	private javax.swing.JTextField textMessage;
	private javax.swing.JTextField textRegister;
	// End of variables declaration//GEN-END:variables
	private JTextArea listUser;
	private JScrollPane scrollUser;
	private JPanel panelUserCheckbox;
	private ButtonGroup buttonGroup;
	private JScrollPane scrollGroup;
	private JPanel groupPane;
	private JLabel lblProfilePic;
}
