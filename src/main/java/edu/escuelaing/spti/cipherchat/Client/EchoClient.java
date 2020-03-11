package edu.escuelaing.spti.cipherchat.Client;

import java.io.*;
import java.net.*;
import java.util.Date;

import com.google.gson.Gson;

import javax.swing.*;
import javax.crypto.BadPaddingException;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;

import java.awt.Dimension;
import java.awt.BorderLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.IOException;
import java.security.NoSuchAlgorithmException;
import java.security.NoSuchProviderException;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.InvalidKeyException;

public class EchoClient {

    public static void main(String[] args)
            throws UnknownHostException, NoSuchAlgorithmException, NoSuchProviderException, NoSuchPaddingException {
        ClientFrame chat = new ClientFrame();
        chat.run();
    } 
    

    static class ClientFrame extends JFrame implements Runnable {

        InetAddress ip = InetAddress.getLocalHost();

        private static final long serialVersionUID = 1L;
        int screenWidth = (int) getToolkit().getScreenSize().getWidth() / 2;
        int screenHeight = (int) getToolkit().getScreenSize().getHeight() / 2;
        JPanel panel;
        JLabel Message;
        JTextField field;
        JTextField sendTo;
        JButton send;
        JButton secureTheChannel;
        JLabel sendToLabel;
        JTextArea messages;
        Boolean securedChannel;
        boolean publicKeyDelivered;
        GenerateKeys gen;
        PublicKey encryptingKey;
        PublicKey myPublicKey;
        PrivateKey myPrivateKey;
        AsymmetricCryptography ac;

        public ClientFrame()
                throws UnknownHostException, NoSuchAlgorithmException, NoSuchProviderException, NoSuchPaddingException {
            prepareFrame();
            buttonPressed();
            secureChannel();
            gen = new GenerateKeys(1024);
            gen.createKeys();
            myPrivateKey = gen.getPrivateKey();
            ac = new AsymmetricCryptography();
        }

        public void clearMessagePanel() {
            field.setText("");
        }

        public void showMessage(String whosthis, String msg) {
            if (!msg.isEmpty())
                messages.append("\n" + "[" + new Date().toString() + "]" + whosthis + ": " + msg);
        }

        public String repackageMessage(DeliveryPackage msg) {
            return new Gson().toJson(msg);
        }

        public void buttonPressed() throws UnknownHostException {
            InetAddress ip = InetAddress.getLocalHost();
            send.addActionListener(new ActionListener() {
                public void actionPerformed(ActionEvent e) {
                    System.out.println("oprime send");
                    try {
                        if (!field.getText().isEmpty() && publicKeyDelivered && securedChannel) {
                            System.out.println("sending message...");
                            sendMessage(repackageMessage(
                                    new DeliveryPackage(ip.getHostAddress(), sendTo.getText(), ac.encryptText(field.getText(),encryptingKey))));
                            showMessage("ME", field.getText());
                            clearMessagePanel();
                        }
                    } catch (IOException e1) {
                        System.out.println(e1);
                        e1.printStackTrace();
                    } catch (NoSuchAlgorithmException e1) {
                        System.out.println(e1);
                        e1.printStackTrace();
                    } catch (NoSuchPaddingException e1) {
                        System.out.println(e1);
                        e1.printStackTrace();
                    } catch (InvalidKeyException e1) {
                        System.out.println(e1);
                        e1.printStackTrace();
                    } catch (IllegalBlockSizeException e1) {
                        System.out.println(e1);
                        e1.printStackTrace();
                    } catch (BadPaddingException e1) {
                        System.out.println(e1);
                        e1.printStackTrace();
                    }
                }
            });
        }

        public void secureChannel() throws UnknownHostException {
            secureTheChannel.addActionListener(new ActionListener() {
                public void actionPerformed(ActionEvent e) {
                    System.out.println("oprime");
                    try {
                        if (!publicKeyDelivered && !sendTo.getText().isEmpty()) {
                            System.out.println("publickey delivered");
                            Socket tempsocket = new Socket(sendTo.getText(), 35550);
                            ObjectOutputStream PublicKeyStream = new ObjectOutputStream(tempsocket.getOutputStream());
                            PublicKeyStream.writeObject(gen.getPublicKey());
                            tempsocket.close();
                            publicKeyDelivered = true;
                        }
                    } catch (IOException e1) {
                        System.out.println(e1);
                        e1.printStackTrace();
                    }
                }
            });
        }

        public void sendMessage(String message) throws IOException, NoSuchAlgorithmException, NoSuchPaddingException,
                InvalidKeyException, IllegalBlockSizeException, BadPaddingException {
            Socket echoSocket = null;
            PrintWriter out = null;
            BufferedReader in = null;

            try {
                echoSocket = new Socket(ip.getHostAddress(), 35500);
                out = new PrintWriter(echoSocket.getOutputStream(), true);
                out.flush();
                out.println(message);
            } catch (UnknownHostException e) {
                System.err.println("Don’t know about host!.");
                System.exit(1);
            } catch (IOException e) {
                System.err.println("Couldn’t get I/O for " + "the connection to: localhost.");
                System.out.println(e);
                System.exit(1);
            } finally {
                if (echoSocket != null) {
                    System.out.println("echosocket closed");
                    echoSocket.close();
                }
                if (in != null) {
                    System.out.println("in closed");
                    in.close();
                }
                if (out != null) {
                    System.out.println("out closed");
                    out.close();
                }
            }
        }

        @Override
        public void run() {
            ServerSocket serverSocket = null;
            ServerSocket serverSocketKeys = null;
            BufferedReader in = null;
            Socket clientSocket = null;
            Socket forkeys = null;
            try {
                serverSocket = new ServerSocket(35000);
                serverSocketKeys = new ServerSocket(35550);
                forkeys = serverSocketKeys.accept();
                ObjectInputStream OISpublickey = new ObjectInputStream(forkeys.getInputStream());
                encryptingKey = (PublicKey) OISpublickey.readObject();
                System.out.println(encryptingKey.toString());
                securedChannel = true;
                forkeys.close();
                while (true) {
                    try {
                        clientSocket = serverSocket.accept();
                        in = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
                        String receivedmsg = in.readLine();
                        DeliveryPackage msgreceivedobj = new Gson().fromJson(receivedmsg, DeliveryPackage.class);
                        showMessage(msgreceivedobj.getFrom(),
                                ac.decryptText(msgreceivedobj.getMessage(), myPrivateKey));
                    } catch (IOException e) {
                        System.err.println("client: Accept failed.");
                        System.out.println(e);
                        System.exit(1);
                    } catch (InvalidKeyException e) {
                        System.out.println(e);
                        e.printStackTrace();
                    } catch (IllegalBlockSizeException e) {
                        System.out.println(e);
                        e.printStackTrace();
                    } catch (BadPaddingException e) {
                        System.out.println(e);
                        e.printStackTrace();
                    } finally {
                        if (in != null)
                            in.close();
                        if (clientSocket != null)
                            clientSocket.close();
                    }
                }
            } catch (IOException e) {
                System.err.println("Could not listen on port: 35000.");
                System.exit(1);
            } catch (ClassNotFoundException e1) {
                e1.printStackTrace();
            } finally {
                if (serverSocket != null)
                    System.out.println("serversocket closed");
                try {
                    serverSocket.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }

        public void prepareFrame() {
            securedChannel = false;
            publicKeyDelivered = false;
            setLayout(new BorderLayout());
            setBounds(screenWidth / 2, screenHeight / 2, screenWidth, screenHeight);
            panel = new JPanel();
            Message = new JLabel("Message");
            panel.add(Message);
            field = new JTextField();
            field.setPreferredSize(new Dimension(screenWidth - 100, 40));
            panel.add(field);
            send = new JButton("SEND");
            send.setPreferredSize(new Dimension(screenWidth / 3, 40));
            secureTheChannel = new JButton("Secure channel");
            secureTheChannel.setPreferredSize(new Dimension(screenWidth / 3, 40));
            messages = new JTextArea();
            messages.setPreferredSize(new Dimension(screenWidth - 50, screenHeight - 300));
            sendTo = new JTextField();
            sendTo.setPreferredSize(new Dimension(160, 40));
            sendToLabel = new JLabel("Send to");
            panel.add(sendToLabel);
            panel.add(sendTo);
            panel.add(secureTheChannel);
            panel.add(send);
            panel.add(messages);
            add(panel);
            setVisible(true);
        }
    }
}
