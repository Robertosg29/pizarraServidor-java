package data;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.List;

/**
 *
 * @author Rober
 */
public class Servidor implements Runnable {

    final static int NUMPUERTO = 50000;
    ServerSocket puertoEscucha = null;
    boolean finServer = false;
    static List<GestionaCliente> gClientes = new ArrayList<>();

    public Servidor() {

    }

    @Override
    public void run() {
        try {
            puertoEscucha = new ServerSocket(NUMPUERTO);

        } catch (IOException ex) {
            System.out.println("No se puede escuchar en este puerto: " + NUMPUERTO + ", " + ex);
            System.exit(1);
        }

        Socket conexion = null;

        while (!finServer) {
            try {
                System.out.println("Esperando conexión");
                puertoEscucha.setSoTimeout(3000);
                conexion = puertoEscucha.accept();
                GestionaCliente gb = new GestionaCliente(conexion);
                synchronized (gClientes) {
                    gClientes.add(gb);
                }
                new Thread(gb).start();
                System.out.println("Conexion completada!");
            } catch (IOException ex) {

            }
        }

        try {
            if (conexion != null) {
                conexion.close();
            }
            puertoEscucha.close();
            System.out.println("Se ha finalizado la comunicación ");
        } catch (IOException ex) {
            System.out.println("Algun flujo no puede cerrarse");
        }
    }

    public void setFinServer(boolean finServer) {
        this.finServer = finServer;
    }

    public void finalizarTodasConexiones() {
        for (GestionaCliente gCliente : Servidor.gClientes) {
            gCliente.finalizarConexionCliente();
        }
        finServer = true;
    }

}
