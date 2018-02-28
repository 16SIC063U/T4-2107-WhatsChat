package user;

import java.awt.EventQueue;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.border.EmptyBorder;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JScrollPane;
import javax.swing.DefaultListModel;
import javax.swing.JButton;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.awt.event.ActionEvent;
import java.io.IOException;
import java.net.DatagramPacket;
import java.net.InetAddress;
import java.net.MulticastSocket;
import java.util.Collections;
import java.util.List;
import javax.swing.JTextField;

public class manageGroup extends JFrame {

	//Declare tools variable
	private JPanel contentPane;
	private JButton btnRemove;
	private JTextField tb_groupName = new JTextField();
	private JTextField tbGroupName;
	private JScrollPane scrollPaneFriendList;
	private JScrollPane scrollPaneGroupParticipants;
	private JList listBoxFriend;
	private JList listBoxParticipants;
        
	private DefaultListModel onlineList = new DefaultListModel<>();
	private DefaultListModel participantsList = new DefaultListModel<>();
	
	//Declare Variable
        private String groupName;
        private List<String> groupMembers;
        private List<String> editedgroupMembers;
	MulticastSocket multicastSocket = null, commonSocket = null;
	InetAddress multicastGroup = null, commonGroup = null;
	
	ArrayList<String>test = new ArrayList<String>();
        
        public void setGroupName(String groupInput){
            groupName = groupInput;
            tbGroupName.setText(groupInput);
        }
        
	public void setUsers(List<String> onlineUsers, List<String> groupMembers) {
            this.groupMembers = groupMembers;
            for(int i=0; i<onlineUsers.size(); i++){
                if(!groupMembers.contains(onlineUsers.get(i)))
                    onlineList.addElement(onlineUsers.get(i));
            }
            for(int i=0; i<groupMembers.size(); i++){
                participantsList.addElement(groupMembers.get(i));
            }

            listBoxFriend = new JList(onlineList);
            listBoxFriend.setFixedCellWidth(100);
            listBoxFriend.setFixedCellHeight(20);

            listBoxParticipants = new JList(participantsList);
            listBoxParticipants.setFixedCellWidth(100);
            listBoxParticipants.setFixedCellHeight(20);

            scrollPaneFriendList.setViewportView(listBoxFriend);
            scrollPaneGroupParticipants.setViewportView(listBoxParticipants);
        }

	/**
	 * Launch the application.
	 */
	public static void main(String[] args) {
            EventQueue.invokeLater(new Runnable() {
                public void run() {
                    try {
                        manageGroup frame = new manageGroup();
                        frame.setVisible(true);
                        frame.setResizable(false);
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            });
	}

	/**
	 * Create the frame.
	 * 
	 * @param onlineUsers
	 * @param groupName
	 */
	public manageGroup() {
            try{
                commonGroup = InetAddress.getByName("230.1.1.1");
                commonSocket = new MulticastSocket(6789);
                commonSocket.joinGroup(commonGroup);
            }
            catch (IOException ex){
                ex.printStackTrace();
            }
            setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
            setBounds(100, 100, 598, 423);
            contentPane = new JPanel();
            contentPane.setBorder(new EmptyBorder(5, 5, 5, 5));
            setContentPane(contentPane);
            contentPane.setLayout(null);

            JLabel lblGroupName = new JLabel("Group Name:");
            lblGroupName.setBounds(22, 13, 91, 16);
            contentPane.add(lblGroupName);

            scrollPaneFriendList = new JScrollPane();
            scrollPaneFriendList.setBounds(22, 74, 218, 237);
            contentPane.add(scrollPaneFriendList);

            scrollPaneGroupParticipants = new JScrollPane();
            scrollPaneGroupParticipants.setBounds(312, 74, 218, 237);
            contentPane.add(scrollPaneGroupParticipants);

            JButton btnAdd = new JButton(">");
            btnAdd.addActionListener(new ActionListener() {
                public void actionPerformed(ActionEvent arg0) {
                    int i = 0;
                    List<String> from = listBoxFriend.getSelectedValuesList();
                    if (from != null) {
                        for (i = 0; i < from.size(); i++) {
                            participantsList.addElement(from.get(i)); 
                            onlineList.removeElement(from.get(i));
                        }

                        listBoxParticipants = new JList(participantsList);
                        scrollPaneGroupParticipants.setViewportView(listBoxParticipants);
                    }

                }
            });
            btnAdd.setBounds(252, 134, 48, 43);
            contentPane.add(btnAdd);

            btnRemove = new JButton("<");
            btnRemove.addActionListener(new ActionListener() {
                public void actionPerformed(ActionEvent arg0) {
                    List<String> to = listBoxParticipants.getSelectedValuesList();
                    if (to != null) {
                        int i = 0;
                        for (i = 0; i < to.size(); i++) {
                            onlineList.addElement(to.get(i));
                            participantsList.removeElement(to.get(i));
                        }
                    }
                }
            });
            btnRemove.setBounds(252, 192, 48, 43);
            contentPane.add(btnRemove);

            tbGroupName = new JTextField();
            tbGroupName.setBounds(106, 10, 424, 27);
            contentPane.add(tbGroupName);
            tbGroupName.setColumns(10);

            JLabel lblFriendList = new JLabel("Friend List:");
            lblFriendList.setBounds(22, 56, 77, 16);
            contentPane.add(lblFriendList);

            JLabel lblGroupParticipants = new JLabel("Group Participants:");
            lblGroupParticipants.setBounds(310, 56, 112, 16);
            contentPane.add(lblGroupParticipants);

            JButton btnConfirm = new JButton("Confirm");
            btnConfirm.addActionListener(new ActionListener() {
                public void actionPerformed(ActionEvent e) {
                    editedgroupMembers = Collections.list(participantsList.elements());
                    String message="";
                    String groupInput = tbGroupName.getText();
                    for(int i=0; i<editedgroupMembers.size(); i++){
                        if(!groupMembers.contains(editedgroupMembers.get(i))){
                            message = "addMember::"+editedgroupMembers.get(i)+"::"+groupName;
                            sendMessage(message);
                        }
                        else{
                            groupMembers.remove(editedgroupMembers.get(i));
                        }
                    }
                    for(int i=0; i<groupMembers.size(); i++){
                        message = "removeMember::"+groupMembers.get(i)+"::"+groupName;
                        sendMessage(message);
                    }
                    if(!groupName.equals(groupInput)){
                        message = "updateGroupName::"+groupName+"::"+groupInput;
                        sendMessage(message);
                    }
                    manageGroup.this.dispose();
                }
            });
            btnConfirm.setBounds(170, 337, 97, 25);
            contentPane.add(btnConfirm);

            JButton btnCancel = new JButton("Cancel");
            btnCancel.addActionListener(new ActionListener() {
                public void actionPerformed(ActionEvent e) {
                    manageGroup.this.dispose();
                }
            });
            btnCancel.setBounds(294, 337, 97, 25);
            contentPane.add(btnCancel);
	}
        
        public void sendMessage(String message){
            byte[] buf = message.getBytes();
            DatagramPacket dgp = new DatagramPacket(buf, buf.length, commonGroup, 6789);
            try{
                commonSocket.send(dgp);
            }
            catch(IOException ex){
                ex.printStackTrace();
            }
        }
}
