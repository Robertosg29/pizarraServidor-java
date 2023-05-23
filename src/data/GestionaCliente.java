package data;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.Socket;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author Rober
 */
public class GestionaCliente implements Runnable {

    Socket s;
    DataInputStream in = null;
    DataOutputStream out = null;
    boolean finServidor = false;
    String nombre;
    static int COD = 1;
    final int cod;

    public GestionaCliente(Socket s) {

        this.s = s;
        this.cod = COD++;
        try {
            in = new DataInputStream(s.getInputStream());
            out = new DataOutputStream(s.getOutputStream());
        } catch (IOException ex) {
            Logger.getLogger(GestionaCliente.class.getName()).log(Level.SEVERE, null, ex);
        }

    }

    @Override
    public void run() {

        while (!finServidor) {
            try {
                s.setSoTimeout(3000);
                String cad = in.readUTF();
                System.out.println("Cliente dice : " + cad);
                recibirCliente(cad);

                } catch (IOException iOException) {
            }

        }

    }

    private void recibirCliente(String cad) {
        try {
            //0|"MENSAJE"
            String[] msj = cad.split(Protocolo.SEPARADOR);
            switch (msj[0]) {
                case "" + Protocolo.PINTAR_C:
                    enviarMensajeAClientes(msj[1]);
                    break;
                case "" + Protocolo.FIN_CLIENTE:
                    desconectarCliente();
                    break;
                case "" + Protocolo.USUARIO_CONECTADO_C:
                    nombre = msj[1];
                    out.writeUTF(Protocolo.USUARIO_CONECTADO_S + Protocolo.SEPARADOR + cod);
                    break;
            }
            if (!finServidor) {
                out.flush();
            }
        } catch (IOException ex) {
            Logger.getLogger(GestionaCliente.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    private void enviarMensajeAClientes(String mensaje) {
        synchronized (Servidor.gClientes) {
            for (GestionaCliente gCliente : Servidor.gClientes) {

                try {
                    gCliente.out.writeUTF(Protocolo.PINTAR_S + Protocolo.SEPARADOR + mensaje);
                    gCliente.out.flush();
                } catch (IOException ex) {
                    Logger.getLogger(GestionaCliente.class.getName()).log(Level.SEVERE, null, ex);
                }

            }

        }

    }

    public void finalizarConexionCliente() {
        try {
            //LE ENVIAMOS UN MENSAJE AL CLIENTE COMO QUE EL SERVIDOR SE DESCONECTA PRIMERO
            out.writeUTF(Protocolo.FIN_SERVIDOR + Protocolo.SEPARADOR);
            out.flush();
            finServidor = true;
        } catch (IOException ex) {
            Logger.getLogger(GestionaCliente.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    private void desconectarCliente() {
        //ESTE METODO SIRVE PARA DESCONECTAR EL GESTIONACLIENTE AL RECIBIR EL MENSAJE DE QUE EL CLIENTE HA FINALIZADO LA CONEXION
        synchronized (Servidor.gClientes) {
            Servidor.gClientes.remove(this);
            finServidor = true;
        }

    }
}
