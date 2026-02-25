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


                    String aviso = "CLIENTE_NUEVO:" + dp.getAddress().getHostAddress()
                            + ":" + dp.getPort();
                    notificarATodos(socketudp, aviso);

                    // Enviar al nuevo cliente la lista de clientes existentes
                    for (InetSocketAddress c : clientes) {
                        if (!c.equals(origen)) {
                            String existente = "CLIENTE_NUEVO:" + c.getAddress().getHostAddress() + ":" + c.getPort();
                            DatagramPacket p = MiDatagrama.crearDataG(dp.getAddress().getHostAddress(), dp.getPort(), existente);
                            socketudp.send(p);
                        }
                    }
                }


                // 3. Extraer mensaje (solo los bytes usados)
                    String mensaje = new String(dp.getData(), 0, dp.getLength());

                if (mensaje.startsWith("CLIENTE_")) continue;

                log("Recibido de " + origen + ": " + mensaje);

                    // 4. Reenviar a TODOS los clientes registrados
                if (mensaje.startsWith("@")) {
                    // ── MENSAJE PRIVADO
                    int espacioIdx = mensaje.indexOf(' ');
                    if (espacioIdx > 1) {
                        String destStr = mensaje.substring(1, espacioIdx);
                        String textoPrivado = mensaje.substring(espacioIdx + 1);
                        String[] partes = destStr.split(":");

                        if (partes.length == 2) {
                            try {
                                InetAddress destIp = InetAddress.getByName(partes[0]);
                                int destPort = Integer.parseInt(partes[1].trim());
                                InetSocketAddress destAddr = new InetSocketAddress(destIp, destPort);

                                if (clientes.contains(destAddr)) {
                                    String msgPrivado = "[PRIVADO de " + dp.getAddress().getHostAddress() + ":" + dp.getPort() + "] " + textoPrivado;

                                    DatagramPacket salida = MiDatagrama.crearDataG(destIp.getHostAddress(), destPort, msgPrivado);
                                    socketudp.send(salida);

                                    // Confirmacion al emisor
                                    DatagramPacket confirm = MiDatagrama.crearDataG(dp.getAddress().getHostAddress(), dp.getPort(),
                                            "[Al privado entregado a " + destStr + "]");
                                    socketudp.send(confirm);

                                    log("Privado enrutado: " + origen + " → " + destAddr);

                                } else {
                                    DatagramPacket err = MiDatagrama.crearDataG(dp.getAddress().getHostAddress(), dp.getPort(),
                                            "[ERROR] El cliente " + destStr + " no esta conectado.");
                                    socketudp.send(err);
                                }

                            } catch (NumberFormatException | UnknownHostException ex) {
                                log("Direccion invalida: " + destStr);
                            }
                        }
                    }

                } else {
                    String broadcast = "[" + dp.getAddress().getHostAddress() + ":" + dp.getPort() + "] " + mensaje;notificarATodos(socketudp, broadcast);
                }
            }

        } catch (SocketException ex) {
                Logger.getLogger(PrincipalSrv.class.getName()).log(Level.SEVERE, null, ex);
            } catch (IOException ex) {
                Logger.getLogger(PrincipalSrv.class.getName()).log(Level.SEVERE, null, ex);
            }
        }).start();
    }

private void notificarATodos(DatagramSocket socket, String mensaje) throws IOException {
    for (InetSocketAddress cliente : clientes) {
        DatagramPacket p = MiDatagrama.crearDataG(
                cliente.getAddress().getHostAddress(),
                cliente.getPort(),
                mensaje);
        socket.send(p);
    }
}

    private void log(String texto) {
        SwingUtilities.invokeLater(() -> mensajesTxt.append(texto + "\n"));
    }

    public static void main(String[] args) {
        java.awt.EventQueue.invokeLater(() -> new PrincipalSrv().setVisible(true));
    }

    private JButton bIniciar;
    private JLabel jLabel1;
    private JTextArea mensajesTxt;
    private JScrollPane jScrollPane1;
}
