package org.vinni.servidor.gui;


import org.vinni.dto.MiDatagrama;

import javax.swing.*;
import java.io.IOException;
import java.net.*;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Author: Vinni
 */
public class PrincipalSrv extends JFrame {

    private final int PORT = 12345;

    private final CopyOnWriteArrayList<InetSocketAddress> clientes = new CopyOnWriteArrayList<>();

    /**
     * Creates new form Principal1
     */
    public PrincipalSrv() {
        initComponents();
        this.mensajesTxt.setEditable(false);
    }
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">
    private void initComponents() {
        this.setTitle("Servidor ...");

        bIniciar = new JButton();
        jLabel1 = new JLabel();
        mensajesTxt = new JTextArea();
        jScrollPane1 = new JScrollPane();

        setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
        getContentPane().setLayout(null);

        bIniciar.setFont(new java.awt.Font("Segoe UI", 0, 18)); // NOI18N
        bIniciar.setText("INICIAR SERVIDOR");
        bIniciar.addActionListener(e -> iniciar());
        getContentPane().add(bIniciar);
        bIniciar.setBounds(150, 50, 250, 40);

        jLabel1.setFont(new java.awt.Font("Tahoma", 1, 14)); // NOI18N
        jLabel1.setForeground(new java.awt.Color(204, 0, 0));
        jLabel1.setText("SERVIDOR UDP : FERINK");
        getContentPane().add(jLabel1);
        jLabel1.setBounds(120, 10, 160, 17);

        mensajesTxt.setColumns(25);
        mensajesTxt.setRows(5);

        jScrollPane1.setViewportView(mensajesTxt);

        getContentPane().add(jScrollPane1);
        jScrollPane1.setBounds(20, 150, 500, 120);

        setSize(new java.awt.Dimension(570, 320));
        setLocationRelativeTo(null);
    }// </editor-fold>


    /**
     * @param args the command line arguments
     */

    public void iniciar(){
        mensajesTxt.append("Servidor UDP iniciado en el puerto"+PORT+"\n");
        bIniciar.setEnabled(false);

        new Thread(() -> {
            try(DatagramSocket socketudp = new DatagramSocket(PORT)) {

                byte[] buf = new byte[1024];

                while (true) {
                    // 1. Recibir paquete
                    DatagramPacket dp = new DatagramPacket(buf, buf.length);
                    socketudp.receive(dp);

                    // 2. Registrar cliente si es nuevo
                    InetSocketAddress origen = new InetSocketAddress(
                            dp.getAddress(), dp.getPort());

                    if (!clientes.contains(origen)) {
                        clientes.add(origen);
                        log("Nuevo cliente registrado: " + origen);
                    }

                    // 3. Extraer mensaje (solo los bytes usados)
                    String mensaje = new String(dp.getData(), 0, dp.getLength());
                    log("Recibido de " + origen + ": " + mensaje);

                    // 4. Reenviar a TODOS los clientes registrados
                    String broadcast = "[" + dp.getAddress().getHostAddress()
                            + ":" + dp.getPort() + "] " + mensaje;

                    for (InetSocketAddress cliente : clientes) {
                        DatagramPacket salida = MiDatagrama.crearDataG(
                                cliente.getAddress().getHostAddress(),
                                cliente.getPort(),
                                broadcast);
                        socketudp.send(salida);
                    }

                }

            } catch (SocketException ex) {
                Logger.getLogger(PrincipalSrv.class.getName()).log(Level.SEVERE, null, ex);
            } catch (IOException ex) {
                Logger.getLogger(PrincipalSrv.class.getName()).log(Level.SEVERE, null, ex);
            }
        }).start();
    }
    private void log(String texto) {
        SwingUtilities.invokeLater(() -> mensajesTxt.append(texto + "\n"));
    }

    public static void main(String[] args) {
        java.awt.EventQueue.invokeLater(() -> new PrincipalSrv().setVisible(true));
    }

    private JButton    bIniciar;
    private JLabel     jLabel1;
    private JTextArea  mensajesTxt;
    private JScrollPane jScrollPane1;
}
