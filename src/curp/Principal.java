/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/GUIForms/JFrame.java to edit this template
 */
package curp;

/**
 *
 * @author jesuszuniga
 */
import javax.swing.JFileChooser;
import javax.swing.filechooser.FileNameExtensionFilter;
import java.io.File;

import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import java.io.FileInputStream;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.Timestamp;

import java.awt.Color;
import java.util.ArrayList;
import java.util.List;

public class Principal extends javax.swing.JFrame {

    /**
     * Creates new form Principal
     */
    public Principal() {
        initComponents();
    }
    
    List <String > listaCurps=new ArrayList();
    
    
    public void procesarConsultasRenapo() {
    String sqlInsert = "INSERT INTO public.consulta (session_id, curp, nombres, apellido1, apellido2, "
                     + "status_oper, message, tipo_error, codigo_error, fecha_consulta, "
                     + "hora_consulta, status_curp) "
                     + "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, current_date, current_time, ?)";

    try (Connection con = ConexionPool.getConnection();
         PreparedStatement pstmt = con.prepareStatement(sqlInsert)) {
        
        con.setAutoCommit(false); // Activamos el modo veloz
        int contador = 0;

        for (String curp : listaCurps) {
            pstmt.setString(1, "SES-001");
            pstmt.setString(2, curp.trim()); // Siempre usa trim() por seguridad
            pstmt.setString(3, "NOMBRE_API");
            pstmt.setString(4, "APE1");
            pstmt.setString(5, "APE2");
            pstmt.setString(6, "EXITOSO");
            pstmt.setString(7, "PROCESADO CORRECTAMENTE");
            pstmt.setString(8, "NINGUNO");
            pstmt.setString(9, "0");
            pstmt.setString(10, "A");

            pstmt.addBatch();
            contador++;

            // Mandamos paquetes de 100 en 100
            if (contador % 100 == 0) {
                pstmt.executeBatch();
                con.commit();
            }
        }
        
        // El último empujón para los que sobraron
        pstmt.executeBatch();
        con.commit();
        
        javax.swing.JOptionPane.showMessageDialog(null, "Procesados con éxito: " + contador);

    } catch (SQLException e) {
        // Por si algo más sale mal, esto te dirá la verdad
        SQLException nextE = e.getNextException();
        System.err.println("Causa real: " + (nextE != null ? nextE.getMessage() : e.getMessage()));
    }
}
    

    public void leerExcel(String ruta) {

        int linea = 2;
        String errores = "";
        int incorrectos = 0;
        int correctos = 0;

        try (FileInputStream fis = new FileInputStream(new File(ruta))) {
            Workbook libro = new XSSFWorkbook(fis);
            Sheet hoja = libro.getSheetAt(0);
            
            if(verificarEncabezado(hoja)){

            int primeraFilaALeer = 1;
            int ultimaFila = hoja.getLastRowNum();

            for (int i = primeraFilaALeer; i <= ultimaFila; i++) {
                Row fila = hoja.getRow(i);

                if (fila != null) {

                    for (Cell celda : fila) {
                        if (celda.getCellType() == CellType.STRING) {
                            String valor = celda.getStringCellValue().trim();
                            System.out.println(valor);
                            if (!esCurpValida(valor)) {
                                incorrectos++;
                                errores += "Error en la curp numero " + linea + " " + valor + "\n";
                                
                                //System.out.println("Fila " + (i + 1) + " - CURP encontrada: " + valor);
                            }//fin if que revisa que es un curp valdia
                            listaCurps.add(valor);
                            correctos++;
                        }// fin if de celda vacia
                    }//fin for que recorre celda
                }//fin if de la celda null
                linea++;
            }//fin for
            libro.close();

            System.out.println("correcto " + correctos + " incorrecto " + incorrectos);

            if (incorrectos <= 0) {
                jTextArea1.setForeground(Color.GREEN);
                jTextArea1.append("Se procesaroon correctamente " + correctos + " CURPS");
                jButton3.setEnabled(true);
                
            }// fin if correctos 
            else {
                jTextArea1.setForeground(Color.RED);
                jTextArea1.append(errores);

            }//fin if incorrectos
            
            }//fin if que no es encabezado
            
            else{
                jTextArea1.setForeground(Color.RED);
                jTextArea1.append("EL ARCHIVO NO TIENE EL ENCABEZADO CORRECTO");
            }

        } catch (Exception e) {
            System.out.println("Error al procesar: " + e.getMessage());
        }//fin catch
    }//fin leer excel

    public boolean verificarEncabezado(Sheet hoja) {

        boolean flag = true;

        // Acceder a la primera fila (índice 0)
        Row filaTitulo = hoja.getRow(0);

        if (filaTitulo != null) {

            Cell celdaTitulo = filaTitulo.getCell(0);

            if (celdaTitulo != null && celdaTitulo.getCellType() == CellType.STRING) {
                String textoCabecera = celdaTitulo.getStringCellValue().trim();
                    System.out.println("cabecera "+ textoCabecera);
                // Verificar si contiene la frase exacta
                if (!textoCabecera.equals("SUPERVIVENCIA CURP OPEO")) {
                    flag = false;
                }
            }
        }
        
        else{flag=false;}
        return flag;
    }//fin verificarEncabezado

    public boolean esCurpValida(String curp) {
        // Expresión regular oficial para la CURP en México
        String regex = "^[A-Z]{1}[AEIOU]{1}[A-Z]{2}[0-9]{2}(0[1-9]|1[0-2])(0[1-9]|1[0-9]|2[0-9]|3[0-1])[HM]{1}(AS|BC|BS|CC|CH|CL|CM|CS|DF|DG|GR|GT|HG|JC|MC|MN|MS|NT|NL|OC|PL|QT|QR|SP|SL|SR|TC|TS|TL|VZ|YN|ZS|NE)[B-DF-HJ-NP-TV-Z]{3}[0-9A-Z]{1}[0-9]{1}$";

        Pattern patron = Pattern.compile(regex);
        Matcher emparejador = patron.matcher(curp.toUpperCase()); // Convertimos a mayúsculas por si acaso

        return emparejador.matches();
    }

    /**
     * This method is called from within the constructor to initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is always
     * regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        jPanel1 = new javax.swing.JPanel();
        jButton1 = new javax.swing.JButton();
        jTextField1 = new javax.swing.JTextField();
        jLabel2 = new javax.swing.JLabel();
        jLabel3 = new javax.swing.JLabel();
        jButton2 = new javax.swing.JButton();
        jPanel2 = new javax.swing.JPanel();
        jScrollPane1 = new javax.swing.JScrollPane();
        jTextArea1 = new javax.swing.JTextArea();
        jButton3 = new javax.swing.JButton();

        setDefaultCloseOperation(javax.swing.WindowConstants.EXIT_ON_CLOSE);

        jPanel1.setBorder(javax.swing.BorderFactory.createEtchedBorder());

        jButton1.setText("Buscar");
        jButton1.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButton1ActionPerformed(evt);
            }
        });

        jTextField1.setEditable(false);

        jLabel2.setText("1.- Buscar el archivo de excel que contiene las CURP de consulta");

        jLabel3.setHorizontalAlignment(javax.swing.SwingConstants.LEFT);
        jLabel3.setText("<html>2.- Procesa el archivo para verificar la intergridad de datos <br> y buscar errores en las CURP</html>");
        jLabel3.setVerticalAlignment(javax.swing.SwingConstants.TOP);

        jButton2.setText("Verificar archivo");
        jButton2.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButton2ActionPerformed(evt);
            }
        });

        jPanel2.setBorder(javax.swing.BorderFactory.createTitledBorder("Errores"));

        jTextArea1.setColumns(20);
        jTextArea1.setFont(new java.awt.Font("Helvetica Neue", 0, 14)); // NOI18N
        jTextArea1.setRows(5);
        jScrollPane1.setViewportView(jTextArea1);

        javax.swing.GroupLayout jPanel2Layout = new javax.swing.GroupLayout(jPanel2);
        jPanel2.setLayout(jPanel2Layout);
        jPanel2Layout.setHorizontalGroup(
            jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel2Layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(jScrollPane1)
                .addContainerGap())
        );
        jPanel2Layout.setVerticalGroup(
            jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel2Layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(jScrollPane1, javax.swing.GroupLayout.DEFAULT_SIZE, 178, Short.MAX_VALUE)
                .addContainerGap())
        );

        javax.swing.GroupLayout jPanel1Layout = new javax.swing.GroupLayout(jPanel1);
        jPanel1.setLayout(jPanel1Layout);
        jPanel1Layout.setHorizontalGroup(
            jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel1Layout.createSequentialGroup()
                .addGap(17, 17, 17)
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, jPanel1Layout.createSequentialGroup()
                        .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                            .addComponent(jLabel3, javax.swing.GroupLayout.DEFAULT_SIZE, 433, Short.MAX_VALUE)
                            .addComponent(jTextField1))
                        .addGap(18, 18, 18)
                        .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                            .addComponent(jButton1, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                            .addComponent(jButton2, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)))
                    .addGroup(jPanel1Layout.createSequentialGroup()
                        .addComponent(jLabel2)
                        .addGap(0, 0, Short.MAX_VALUE))
                    .addComponent(jPanel2, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                .addContainerGap())
        );
        jPanel1Layout.setVerticalGroup(
            jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel1Layout.createSequentialGroup()
                .addGap(7, 7, 7)
                .addComponent(jLabel2)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jButton1)
                    .addComponent(jTextField1, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(jPanel1Layout.createSequentialGroup()
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                        .addComponent(jLabel3, javax.swing.GroupLayout.PREFERRED_SIZE, 55, javax.swing.GroupLayout.PREFERRED_SIZE))
                    .addGroup(jPanel1Layout.createSequentialGroup()
                        .addGap(20, 20, 20)
                        .addComponent(jButton2)))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jPanel2, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap(27, Short.MAX_VALUE))
        );

        jButton3.setText("Procesar");
        jButton3.setEnabled(false);
        jButton3.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButton3ActionPerformed(evt);
            }
        });

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(getContentPane());
        getContentPane().setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                    .addGroup(layout.createSequentialGroup()
                        .addContainerGap()
                        .addComponent(jPanel1, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                    .addGroup(layout.createSequentialGroup()
                        .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                        .addComponent(jButton3, javax.swing.GroupLayout.PREFERRED_SIZE, 111, javax.swing.GroupLayout.PREFERRED_SIZE)))
                .addContainerGap())
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addGap(14, 14, 14)
                .addComponent(jPanel1, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(18, 18, 18)
                .addComponent(jButton3, javax.swing.GroupLayout.PREFERRED_SIZE, 42, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap(18, Short.MAX_VALUE))
        );

        pack();
    }// </editor-fold>//GEN-END:initComponents

    private void jButton1ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButton1ActionPerformed

        JFileChooser archivo = new JFileChooser();

        FileNameExtensionFilter filtro = new FileNameExtensionFilter("Archivos de Excel (.xlsx)", "xlsx");
        archivo.setFileFilter(filtro);

        archivo.setAcceptAllFileFilterUsed(false);

        int resultado = archivo.showOpenDialog(this);

        if (resultado == JFileChooser.APPROVE_OPTION) {
            File archivoSeleccionado = archivo.getSelectedFile();

            // Aquí ya tienes la ruta completa del archivo
            String ruta = archivoSeleccionado.getAbsolutePath();
            jTextField1.setText(ruta);
            System.out.println("Archivo elegido: " + ruta);

            // TIP: Puedes poner esta ruta en un JTextField para que el usuario vea qué eligió
            // txtRuta.setText(ruta);
        }        // TODO add your handling code here:
    }//GEN-LAST:event_jButton1ActionPerformed

    private void jButton2ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButton2ActionPerformed
        leerExcel(jTextField1.getText());        // TODO add your handling code here:
    }//GEN-LAST:event_jButton2ActionPerformed

    private void jButton3ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButton3ActionPerformed
                
                    
                    procesarConsultasRenapo();
                
    }//GEN-LAST:event_jButton3ActionPerformed

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
            java.util.logging.Logger.getLogger(Principal.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        } catch (InstantiationException ex) {
            java.util.logging.Logger.getLogger(Principal.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        } catch (IllegalAccessException ex) {
            java.util.logging.Logger.getLogger(Principal.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        } catch (javax.swing.UnsupportedLookAndFeelException ex) {
            java.util.logging.Logger.getLogger(Principal.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        }
        //</editor-fold>

        /* Create and display the form */
        java.awt.EventQueue.invokeLater(new Runnable() {
            public void run() {
                new Principal().setVisible(true);
            }
        });
    }

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton jButton1;
    private javax.swing.JButton jButton2;
    private javax.swing.JButton jButton3;
    private javax.swing.JLabel jLabel2;
    private javax.swing.JLabel jLabel3;
    private javax.swing.JPanel jPanel1;
    private javax.swing.JPanel jPanel2;
    private javax.swing.JScrollPane jScrollPane1;
    private javax.swing.JTextArea jTextArea1;
    private javax.swing.JTextField jTextField1;
    // End of variables declaration//GEN-END:variables
}
