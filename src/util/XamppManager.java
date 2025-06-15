package util;

import java.io.IOException;

public class XamppManager {
    private Process apacheProcess;
    private Process mysqlProcess;
    private Process xamppControlProcess;

    public void startXampp() {
        try {
            System.out.println("Menyalakan Apache (httpd.exe)...");
            apacheProcess = new ProcessBuilder("C:\\xampp\\apache\\bin\\httpd.exe").start();

            System.out.println("Menyalakan MySQL (mysqld.exe)...");
            mysqlProcess = new ProcessBuilder("C:\\xampp\\mysql\\bin\\mysqld.exe").start();

            System.out.println("Menjalankan XAMPP Control Panel...");
            xamppControlProcess = new ProcessBuilder("C:\\xampp\\xampp-control.exe").start();
        } catch (IOException e) {
            System.err.println("Gagal menyalakan XAMPP:");
            e.printStackTrace();
        }
    }

    public void stopXampp() {
        try {
            System.out.println("Mematikan XAMPP...");

            if (apacheProcess != null && apacheProcess.isAlive()) {
                apacheProcess.destroy();
                System.out.println("Apache dimatikan.");
            }

            if (mysqlProcess != null && mysqlProcess.isAlive()) {
                mysqlProcess.destroy();
                System.out.println("MySQL dimatikan.");
            }

            if (xamppControlProcess != null && xamppControlProcess.isAlive()) {
                xamppControlProcess.destroy();
                System.out.println("XAMPP Control Panel dimatikan.");
            }
        } catch (Exception e) {
            System.err.println("Gagal mematikan proses XAMPP:");
            e.printStackTrace();
        }
    }
}