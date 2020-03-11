package edu.escuelaing.spti.cipherchat;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;

import com.google.gson.Gson;

import edu.escuelaing.spti.cipherchat.Client.DeliveryPackage;

public class EchoServer {  

    public static void main(String[] args) throws IOException {
        ServerSocket serverSocket = null;
        BufferedReader in = null;
        Socket clientSocket = null;
        PrintWriter out = null;
        try {
            serverSocket = new ServerSocket(35500);
            while (true) {
                try {
                    System.out.println("Ready to receive ...");

                    clientSocket = serverSocket.accept();
                    in = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));                    
                    String msgreceived;
                    msgreceived = in.readLine();
                    
                    DeliveryPackage msgreceivedobj = new Gson().fromJson(msgreceived, DeliveryPackage.class);
                    System.out.println("msg: " + msgreceived);

                    Socket resendMessage = new Socket(msgreceivedobj.getTo(), 35000);
                    out = new PrintWriter(resendMessage.getOutputStream(), true);
                    out.flush();
                    out.println(msgreceived);
                } catch (IOException e) {
                    System.err.println("server: Accept failed.");
                    System.exit(1);
                } finally {
                    if (in != null)
                        in.close();
                    if (clientSocket != null)
                        clientSocket.close();
                    if (out != null) out.close();
                }
            }
        } catch (IOException e) {
            System.err.println("Could not listen on port: 35000.");
            System.exit(1);
        } finally {
            if (serverSocket != null)
                System.out.println("serversocket closed");
            serverSocket.close();
        }
    }
}