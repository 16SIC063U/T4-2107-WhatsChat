/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package whatschat;

import java.awt.CardLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.GridLayout;
import java.io.IOException;
import java.net.DatagramPacket;
import java.net.InetAddress;
import java.net.MulticastSocket;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import javax.swing.JCheckBox;
import javax.swing.JComponent;
import javax.swing.JTextField;

/**
 *
 * @author Juun
 */
public class WhatsChat extends javax.swing.JFrame {
    static volatile HashMap createdGroup = new HashMap();
    static final int portNo = 6789;
    static volatile Map<String,String> usernameList = new HashMap();
    static volatile Map<String,String> groupList = new HashMap();
    Map<String,String> joinedGroupList = new HashMap();
    //static volatile boolean onHold = false;
    MulticastSocket multicastSocket = null, commonSocket = null;
    InetAddress multicastGroup = null, commonGroup = null;
    String username = "";
    Thread commonThread = null;
    
    /**
     * Creates new form WhatsChat
     */
    public WhatsChat() {
        initComponents();
        postInitComponent();
        try{
            commonGroup = InetAddress.getByName("230.1.1.1");
            commonSocket = new MulticastSocket(portNo);
            commonSocket.joinGroup(commonGroup);
            commonThread = new Thread(new Runnable(){
                        @Override
                        public void run(){
                            byte rcvBuf[] = new byte[1000];
                            DatagramPacket dgpReceived = new DatagramPacket(rcvBuf, rcvBuf.length);
                            try{
                            while(true){
                                    commonSocket.receive(dgpReceived);
                                    byte[] receivedData = dgpReceived.getData();
                                    int length = dgpReceived.getLength();
                                    String msg = new String(receivedData, 0, length);
                                    // TODO: Listen to broadcast
                                    String[] msgArray = msg.split("::");
                                    String action = msgArray[0], parameter = msgArray[1];
                                    if(action.equals("getUser") && !(username.isEmpty())){
                                        String sendUserMessage = "sendUser::"+username;
                                        commonSocket.send(generateMessage(sendUserMessage));
                                    }
                                    else if(action.equals("getGroup") && !(groupList.isEmpty())){
                                        String sendGroupMessage="";
                                        for(String group : groupList.keySet()){
                                            sendGroupMessage = "sendGroup::"+group+"::"+groupList.get(group);
                                            commonSocket.send(generateMessage(sendGroupMessage));
                                        }
                                    }
                                    else if(action.equals("sendUser")){
                                        if(!(usernameList.containsKey(parameter))){
                                            usernameList.put(parameter,"");
                                        }
                                        updateUserList();
                                    }
                                    else if(action.equals("sendGroup")){
                                        if(!(groupList.containsKey(parameter))){
                                            String detail = msgArray[2];
                                            groupList.put(parameter,detail);
                                        }
                                    }
                                    else if(action.equals("inviteUser")){
                                        if(parameter.equals(username)){
                                            joinedGroupList.put(msgArray[2], msgArray[3]);
                                            updateGroupList();
                                        }
                                    }
                                    else if(action.equals("removeUser")){
                                        if(usernameList.containsKey(parameter)){
                                            usernameList.remove(parameter);
                                        }
                                        updateUserList();
                                    }
                                }
                            }
                            catch(IOException ex){
                                ex.printStackTrace();
                            }
                        }
                    });
            commonThread.start();
            String getUserMessage = "getUser:: ";
            commonSocket.send(generateMessage(getUserMessage));
            String getGroupMessage = "getGroup:: ";
            commonSocket.send(generateMessage(getGroupMessage));
        }
        catch(IOException ex){
            ex.printStackTrace();
        }
    }

    /**
     * This method is called from within the constructor to initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is always
     * regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        btnRegister = new javax.swing.JButton();
        textRegister = new javax.swing.JTextField();
        jPanel1 = new javax.swing.JPanel();
        btnCreate = new javax.swing.JButton();
        btnEdit = new javax.swing.JButton();
        btnDelete = new javax.swing.JButton();
        textGroup = new javax.swing.JTextField();
        labelGroupError = new javax.swing.JLabel();
        jPanel2 = new javax.swing.JPanel();
        panelUser = new javax.swing.JPanel();
        jPanel3 = new javax.swing.JPanel();
        jScrollPane1 = new javax.swing.JScrollPane();
        listGroup = new javax.swing.JTextArea();
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

        javax.swing.GroupLayout jPanel1Layout = new javax.swing.GroupLayout(jPanel1);
        jPanel1.setLayout(jPanel1Layout);
        jPanel1Layout.setHorizontalGroup(
            jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel1Layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(jPanel1Layout.createSequentialGroup()
                        .addComponent(btnCreate)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                        .addComponent(btnEdit)
                        .addGap(41, 41, 41)
                        .addComponent(btnDelete))
                    .addComponent(textGroup)
                    .addGroup(jPanel1Layout.createSequentialGroup()
                        .addComponent(labelGroupError)
                        .addGap(0, 0, Short.MAX_VALUE)))
                .addContainerGap())
        );
        jPanel1Layout.setVerticalGroup(
            jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, jPanel1Layout.createSequentialGroup()
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addComponent(textGroup, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(labelGroupError)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(btnCreate)
                    .addComponent(btnEdit)
                    .addComponent(btnDelete)))
        );

        jPanel2.setBorder(javax.swing.BorderFactory.createTitledBorder("Online Users"));

        javax.swing.GroupLayout panelUserLayout = new javax.swing.GroupLayout(panelUser);
        panelUser.setLayout(panelUserLayout);
        panelUserLayout.setHorizontalGroup(
            panelUserLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 190, Short.MAX_VALUE)
        );
        panelUserLayout.setVerticalGroup(
            panelUserLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 0, Short.MAX_VALUE)
        );

        javax.swing.GroupLayout jPanel2Layout = new javax.swing.GroupLayout(jPanel2);
        jPanel2.setLayout(jPanel2Layout);
        jPanel2Layout.setHorizontalGroup(
            jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(panelUser, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
        );
        jPanel2Layout.setVerticalGroup(
            jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(panelUser, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
        );

        jPanel3.setBorder(javax.swing.BorderFactory.createTitledBorder("Groups"));

        listGroup.setEditable(false);
        listGroup.setColumns(10);
        listGroup.setRows(5);
        jScrollPane1.setViewportView(listGroup);

        javax.swing.GroupLayout jPanel3Layout = new javax.swing.GroupLayout(jPanel3);
        jPanel3.setLayout(jPanel3Layout);
        jPanel3Layout.setHorizontalGroup(
            jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(jScrollPane1, javax.swing.GroupLayout.DEFAULT_SIZE, 190, Short.MAX_VALUE)
        );
        jPanel3Layout.setVerticalGroup(
            jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel3Layout.createSequentialGroup()
                .addComponent(jScrollPane1, javax.swing.GroupLayout.PREFERRED_SIZE, 210, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(0, 0, Short.MAX_VALUE))
        );

        jPanel4.setBorder(javax.swing.BorderFactory.createTitledBorder("Conversation"));

        listConversation.setEditable(false);
        listConversation.setColumns(20);
        listConversation.setRows(5);
        jScrollPane3.setViewportView(listConversation);

        javax.swing.GroupLayout jPanel4Layout = new javax.swing.GroupLayout(jPanel4);
        jPanel4.setLayout(jPanel4Layout);
        jPanel4Layout.setHorizontalGroup(
            jPanel4Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel4Layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(jScrollPane3, javax.swing.GroupLayout.DEFAULT_SIZE, 359, Short.MAX_VALUE)
                .addContainerGap())
        );
        jPanel4Layout.setVerticalGroup(
            jPanel4Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(jScrollPane3)
        );

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

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(getContentPane());
        getContentPane().setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(layout.createSequentialGroup()
                        .addComponent(jPanel2, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addGap(1, 1, 1)
                        .addComponent(jPanel3, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(jPanel4, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                    .addGroup(layout.createSequentialGroup()
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING, false)
                            .addComponent(jPanel1, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                            .addGroup(layout.createSequentialGroup()
                                .addComponent(btnRegister)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addComponent(textRegister, javax.swing.GroupLayout.PREFERRED_SIZE, 200, javax.swing.GroupLayout.PREFERRED_SIZE)))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(labelRegisterError)
                        .addGap(0, 0, Short.MAX_VALUE))
                    .addGroup(layout.createSequentialGroup()
                        .addComponent(btnSend)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(labelMessageError)
                            .addComponent(textMessage))))
                .addContainerGap())
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(btnRegister)
                    .addComponent(textRegister, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(labelRegisterError))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jPanel1, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                    .addComponent(jPanel4, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(jPanel3, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(jPanel2, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(btnSend)
                    .addComponent(textMessage, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(labelMessageError, javax.swing.GroupLayout.PREFERRED_SIZE, 16, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap(56, Short.MAX_VALUE))
        );

        pack();
    }// </editor-fold>//GEN-END:initComponents

    private void postInitComponent(){
        listUser = new javax.swing.JTextArea();
        scrollUser = new javax.swing.JScrollPane();
        panelUserCheckbox = new javax.swing.JPanel();
        panelUserCheckbox.setName("CHECK");
        
        
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
    }
    
    private void btnRegisterMouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_btnRegisterMouseClicked
        // TODO add your handling code here:
        String usernameInput = textRegister.getText();
        if(usernameValid(usernameInput)){
            labelRegisterError.setText("");
            //TODO: Register User
            if(!(usernameList.containsKey(usernameInput))){
                try{
                    username = usernameInput;
                    String sendUserMessage = "sendUser::"+username;
                    commonSocket.send(generateMessage(sendUserMessage));
                }
                catch(IOException ex){
                    ex.printStackTrace();
                }
            }
            else{
                labelRegisterError.setText("Username exist!");  
            }
        }
        else{
            labelRegisterError.setText("Invalid Username !");   
        }
    }//GEN-LAST:event_btnRegisterMouseClicked

    private void btnCreateMouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_btnCreateMouseClicked
        // TODO add your handling code here:
        if(!(panelUser.getComponent(0).isVisible())){
            List<String> users = new ArrayList<String>(usernameList.keySet());
            JCheckBox userCB;
            for(String user : users){
                userCB = new JCheckBox(user);
                userCB.setName(user);
                panelUserCheckbox.add(userCB);
            }
            panelUserCheckbox.setAlignmentY(JComponent.LEFT_ALIGNMENT);
            ((CardLayout) panelUser.getLayout()).show(panelUser, "CHECK");
            btnCreate.setText("Save");
        }
        else{
            String groupInput  = textGroup.getText();
            List<String> checkedUsers = new ArrayList<String>();
            for(Component userCB : panelUserCheckbox.getComponents()){
                if(((JCheckBox)userCB).isSelected()){
                    checkedUsers.add(userCB.getName());
                }
            }
            labelGroupError.setText("");
            if(groupInput.isEmpty()){
                labelGroupError.setText(" Group name cannot be empty !");
            }
            else if(checkedUsers.isEmpty()){
                labelGroupError.setText(" None of the users invited !");
            }
            else{
                int ip = groupInput.hashCode();
                //Limiting the first two group of IP Address to 230.1 for usable Multicast address
                String ipStr = String.format("230.1.%d.%d",(ip & 0xff),(ip >> 8 & 0xff));
                if(!groupList.containsKey(groupInput)){
                    try{
                        String createMessage = "sendGroup::"+groupInput+"::"+ipStr;
                        commonSocket.send(generateMessage(createMessage));
                        for(String checkedUser : checkedUsers){
                            String inviteMessage = "inviteUser::"+checkedUser+"::"+groupInput+"::"+ipStr;
                            commonSocket.send(generateMessage(inviteMessage));
                        }
                        joinedGroupList.put(groupInput, ipStr);
                        groupList.put(groupInput, ipStr);
                    }
                    catch(IOException ex){
                        ex.printStackTrace();
                    }
                }
                else{
                    labelGroupError.setText(" Group already exist !");
                }
            }
        }
    }//GEN-LAST:event_btnCreateMouseClicked

    private void btnEditMouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_btnEditMouseClicked
        // TODO add your handling code here:
    }//GEN-LAST:event_btnEditMouseClicked

    private void btnDeleteMouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_btnDeleteMouseClicked
        // TODO add your handling code here:
    }//GEN-LAST:event_btnDeleteMouseClicked

    private void btnSendMouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_btnSendMouseClicked
        // TODO add your handling code here:
        if(textMessage.getText() != null && !(textMessage.getText().isEmpty())){
            labelMessageError.setText("");
            //TODO: Send Message
            textMessage.setText("");
        }
        else{
            labelMessageError.setText(" Cannot send empty message !");
        }
    }//GEN-LAST:event_btnSendMouseClicked

    private void formWindowClosing(java.awt.event.WindowEvent evt) {//GEN-FIRST:event_formWindowClosing
        // TODO add your handling code here:
        if(!username.isEmpty()){
            try{
                String getUserMessage = "removeUser::"+username;
                commonSocket.send(generateMessage(getUserMessage));
            }
            catch(IOException ex){
                ex.printStackTrace();
            }
        }
    }//GEN-LAST:event_formWindowClosing

    /**
     * @param args the command line arguments
     */
    public static void main(String args[]) {
        /* Set the Nimbus look and feel */
        //<editor-fold defaultstate="collapsed" desc=" Look and feel setting code (optional) ">
        /* If Nimbus (introduced in Java SE 6) is not available, stay with the default look and feel.
         * For details see http://download.oracle.com/javase/tutorial/uiswing/lookandfeel/plaf.html 
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
        //</editor-fold>

        /* Create and display the form */
        java.awt.EventQueue.invokeLater(new Runnable() {
            public void run() {
                new WhatsChat().setVisible(true);
            }
        });
    }
    
    public boolean usernameValid(String username){
        if(username == null || username.isEmpty() || username.length() > 8){
            return false;
        }
        else if(username.contains(" ") || username.matches("^\\d.+")){
            return false;
        }
        else{
            return true;
        }
    }
    
    public void updateUserList(){
        List<String> users = new ArrayList<String>(usernameList.keySet());
        String userList = "";
        for(String user : users){
            userList+=user+"\n";
        }
        listUser.setText(userList);
    }
    
    public void updateGroupList(){
        List<String> groups = new ArrayList<String>(joinedGroupList.keySet());
        String groupNameList = "";
        for(String group : groups){
            groupNameList+=group+"\t"+groupList.get(group)+"\n";
        }
        listGroup.setText(groupNameList);
    }
    
    public DatagramPacket generateMessage(String message){
        byte[] buf = message.getBytes();
        DatagramPacket dgp = new DatagramPacket(buf, buf.length, commonGroup, portNo);
        return dgp;
    }

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton btnCreate;
    private javax.swing.JButton btnDelete;
    private javax.swing.JButton btnEdit;
    private javax.swing.JButton btnRegister;
    private javax.swing.JButton btnSend;
    private javax.swing.JPanel jPanel1;
    private javax.swing.JPanel jPanel2;
    private javax.swing.JPanel jPanel3;
    private javax.swing.JPanel jPanel4;
    private javax.swing.JScrollPane jScrollPane1;
    private javax.swing.JScrollPane jScrollPane3;
    private javax.swing.JLabel labelGroupError;
    private javax.swing.JLabel labelMessageError;
    private javax.swing.JLabel labelRegisterError;
    private javax.swing.JTextArea listConversation;
    private javax.swing.JTextArea listGroup;
    private javax.swing.JPanel panelUser;
    private javax.swing.JTextField textGroup;
    private javax.swing.JTextField textMessage;
    private javax.swing.JTextField textRegister;
    // End of variables declaration//GEN-END:variables
    private javax.swing.JTextArea listUser;
    private javax.swing.JScrollPane scrollUser;
    private javax.swing.JPanel panelUserCheckbox;
}
