package Project1;

/**
 * Redes Integradas de Telecomunicações II
 * MIEEC 2013/2014
 *
 * groupDB.java
 *
 * Demonstration class that shows how groupDB can be used.
 *
 */

import java.util.Iterator;


public class test_DB extends javax.swing.JFrame {
    private final String bdname= "grupos.txt";

    private groupDB db;

    
    /** Creates new form test_DB */
    public test_DB() {
        initComponents();
        db= new groupDB(bdname);
        update_table();
    }

    /** This method is called from within the constructor to
     * initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is
     * always regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        jPanel1 = new javax.swing.JPanel();
        jButton1 = new javax.swing.JButton();
        jButton2 = new javax.swing.JButton();
        jPanel2 = new javax.swing.JPanel();
        label1 = new java.awt.Label();
        jTextGrupo = new javax.swing.JTextField();
        jLabel1 = new javax.swing.JLabel();
        jTextNo1 = new javax.swing.JTextField();
        jLabel2 = new javax.swing.JLabel();
        jTextNome1 = new javax.swing.JTextField();
        jPanel3 = new javax.swing.JPanel();
        jLabel3 = new javax.swing.JLabel();
        jTextNo2 = new javax.swing.JTextField();
        jLabel4 = new javax.swing.JLabel();
        jTextNome2 = new javax.swing.JTextField();
        jLabel5 = new javax.swing.JLabel();
        jTextNo3 = new javax.swing.JTextField();
        jLabel6 = new javax.swing.JLabel();
        jTextNome3 = new javax.swing.JTextField();
        jScrollPane1 = new javax.swing.JScrollPane();
        jTable1 = new javax.swing.JTable();

        setDefaultCloseOperation(javax.swing.WindowConstants.EXIT_ON_CLOSE);
        getContentPane().setLayout(new javax.swing.BoxLayout(getContentPane(), javax.swing.BoxLayout.Y_AXIS));

        jButton1.setText("Acrescentar");
        jButton1.setMaximumSize(new java.awt.Dimension(140, 29));
        jButton1.setPreferredSize(new java.awt.Dimension(140, 29));
        jButton1.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButton1ActionPerformed(evt);
            }
        });
        jPanel1.add(jButton1);

        jButton2.setText("Apagar");
        jButton2.setMaximumSize(new java.awt.Dimension(90, 29));
        jButton2.setPreferredSize(new java.awt.Dimension(90, 29));
        jButton2.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButton2ActionPerformed(evt);
            }
        });
        jPanel1.add(jButton2);

        getContentPane().add(jPanel1);

        label1.setText("Grupo");
        jPanel2.add(label1);

        jTextGrupo.setText("1");
        jPanel2.add(jTextGrupo);

        jLabel1.setText("No1");
        jPanel2.add(jLabel1);

        jTextNo1.setText("11111");
        jPanel2.add(jTextNo1);

        jLabel2.setText("Nome1");
        jPanel2.add(jLabel2);

        jTextNome1.setText("José");
        jTextNome1.setPreferredSize(new java.awt.Dimension(50, 27));
        jPanel2.add(jTextNome1);

        getContentPane().add(jPanel2);

        jLabel3.setText("No2");
        jPanel3.add(jLabel3);

        jTextNo2.setText("22222");
        jPanel3.add(jTextNo2);

        jLabel4.setText("Nome2");
        jPanel3.add(jLabel4);

        jTextNome2.setText("João");
        jTextNome2.setPreferredSize(new java.awt.Dimension(50, 27));
        jPanel3.add(jTextNome2);

        jLabel5.setText("No3");
        jPanel3.add(jLabel5);

        jTextNo3.setText("33333");
        jPanel3.add(jTextNo3);

        jLabel6.setText("Nome3");
        jPanel3.add(jLabel6);

        jTextNome3.setText("Gil");
        jTextNome3.setPreferredSize(new java.awt.Dimension(50, 27));
        jPanel3.add(jTextNome3);

        getContentPane().add(jPanel3);

        jTable1.setModel(new javax.swing.table.DefaultTableModel(
            new Object [][] {
                {null, null, null, null, null, null, null},
                {null, null, null, null, null, null, null},
                {null, null, null, null, null, null, null},
                {null, null, null, null, null, null, null},
                {null, null, null, null, null, null, null},
                {null, null, null, null, null, null, null},
                {null, null, null, null, null, null, null},
                {null, null, null, null, null, null, null},
                {null, null, null, null, null, null, null},
                {null, null, null, null, null, null, null},
                {null, null, null, null, null, null, null},
                {null, null, null, null, null, null, null},
                {null, null, null, null, null, null, null},
                {null, null, null, null, null, null, null},
                {null, null, null, null, null, null, null},
                {null, null, null, null, null, null, null},
                {null, null, null, null, null, null, null},
                {null, null, null, null, null, null, null},
                {null, null, null, null, null, null, null},
                {null, null, null, null, null, null, null}
            },
            new String [] {
                "Grupo", "No1", "Nome1", "No 2", "Nome 2", "No 3", "Nome 2"
            }
        ) {
            Class[] types = new Class [] {
                java.lang.String.class, java.lang.String.class, java.lang.String.class, java.lang.String.class, java.lang.String.class, java.lang.String.class, java.lang.String.class
            };
            boolean[] canEdit = new boolean [] {
                false, false, false, false, false, false, false
            };

            public Class getColumnClass(int columnIndex) {
                return types [columnIndex];
            }

            public boolean isCellEditable(int rowIndex, int columnIndex) {
                return canEdit [columnIndex];
            }
        });
        jScrollPane1.setViewportView(jTable1);

        getContentPane().add(jScrollPane1);

        pack();
    }// </editor-fold>//GEN-END:initComponents

    // Writes DB information to the table
    private void update_table() {
        int line= 0;
        for (Iterator it= db.get_sorted_set().iterator(); line < jTable1.getRowCount(); line++) {
            if (it.hasNext ()) {
                String group= (String)(it.next());
                jTable1.setValueAt(group, line, 0);
                jTable1.setValueAt(db.get_group_info(group, "n1"), line, 1);
                jTable1.setValueAt(db.get_group_info(group, "nam1"), line, 2);
                jTable1.setValueAt(db.get_group_info(group, "n2"), line, 3);
                jTable1.setValueAt(db.get_group_info(group, "nam2"), line, 4);
                jTable1.setValueAt(db.get_group_info(group, "n3"), line, 5);
                jTable1.setValueAt(db.get_group_info(group, "nam3"), line, 6);
            } else {
                jTable1.setValueAt("", line, 0);
                jTable1.setValueAt("", line, 1);
                jTable1.setValueAt("", line, 2);
                jTable1.setValueAt("", line, 3);
                jTable1.setValueAt("", line, 4);
                jTable1.setValueAt("", line, 5);
                jTable1.setValueAt("", line, 6);
            }
        }
    }

    // Handles button "Acrescentar"
    private void jButton1ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButton1ActionPerformed
        // Adds a new element to the DB
        db.store_group(jTextGrupo.getText(), false, jTextNo1.getText(),
                jTextNome1.getText(), jTextNo2.getText(), jTextNome2.getText(),
                jTextNo3.getText(), jTextNome3.getText());
        update_table();
    }//GEN-LAST:event_jButton1ActionPerformed

    // Handles button "Apagar"
    private void jButton2ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButton2ActionPerformed
        // Deletes a group from the DB
        db.remove_group(jTextGrupo.getText());
        update_table();
    }//GEN-LAST:event_jButton2ActionPerformed

    /**
    * @param args the command line arguments
    */
    public static void main(String args[]) {
        java.awt.EventQueue.invokeLater(new Runnable() {
            @Override
            public void run() {
                new test_DB().setVisible(true);
            }
        });
    }

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton jButton1;
    private javax.swing.JButton jButton2;
    private javax.swing.JLabel jLabel1;
    private javax.swing.JLabel jLabel2;
    private javax.swing.JLabel jLabel3;
    private javax.swing.JLabel jLabel4;
    private javax.swing.JLabel jLabel5;
    private javax.swing.JLabel jLabel6;
    private javax.swing.JPanel jPanel1;
    private javax.swing.JPanel jPanel2;
    private javax.swing.JPanel jPanel3;
    private javax.swing.JScrollPane jScrollPane1;
    private javax.swing.JTable jTable1;
    private javax.swing.JTextField jTextGrupo;
    private javax.swing.JTextField jTextNo1;
    private javax.swing.JTextField jTextNo2;
    private javax.swing.JTextField jTextNo3;
    private javax.swing.JTextField jTextNome1;
    private javax.swing.JTextField jTextNome2;
    private javax.swing.JTextField jTextNome3;
    private java.awt.Label label1;
    // End of variables declaration//GEN-END:variables

}