package general;

import database.DBConnection;
import java.io.File;
import java.io.FileOutputStream;
import java.sql.*;
import java.text.SimpleDateFormat;
import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;


/**
 *
 * @author lenovo
 */
public class Nation extends javax.swing.JFrame {
    private final ImageIcon img = new ImageIcon("src\\img\\world_leader.png");
    /**
     * Creates new form Leader
     */
    
    private void loadNationData(){
        Connection con = (Connection) DBConnection.getConnection();
        Statement stmt;
        try {
            stmt = con.createStatement();
            ResultSet rs = stmt.executeQuery("SELECT * FROM Nation");
            
            DefaultTableModel model = (DefaultTableModel) tblNation.getModel();
            model.setRowCount(0);
            
            while (rs.next()) {
                Object[] row = {
                    rs.getString("nationID"),
                    rs.getString("name"),
                    rs.getString("continent"),
                    rs.getLong("population_count"),
                    rs.getInt("year_established"),
                };
                model.addRow(row);
            }
            nationIDLabel.setText("None");
            txtName.setText("");
            txtNationID.setText("");
            txtName.setText("");
            txtContinent.setText("");
            txtPopulation.setText("");
            yearChooser.setYear(2025);
            con.close();
        } catch (SQLException ex) {
            JOptionPane.showMessageDialog(this, "Connection failed! " + ex.getMessage());
        }
    }
    
    private void addNation(){
        String nationID = txtNationID.getText().trim();
        String name = txtName.getText().trim();
        String continent = txtContinent.getText().trim();
        long population_count = 0;
        int yearEstablished = yearChooser.getYear();
        
        try {
            population_count = Long.parseLong(txtPopulation.getText());
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, "Field must be a number.");
        }

        try {
            
            String sql = "INSERT INTO Nation(nationID, name, continent, population_count, year_established) values (?, ?, ?, ?, ?)";
            Connection con = (Connection) DBConnection.getConnection();
            PreparedStatement pstmt = con.prepareStatement(sql);
            
            Object[] params = {nationID, name, continent, population_count, yearEstablished};
            
            for (int i = 0; i < params.length; i++) {
                pstmt.setObject(i + 1, params[i]);
            }
            pstmt.executeUpdate();
            
            JOptionPane.showMessageDialog(this, "Record added successfully!");
            
            loadNationData();
            con.close();
        } catch (SQLException ex) {
            JOptionPane.showMessageDialog(this, "Database error! " + ex.getMessage());
        }
    }
    
    private void deleteNation(){
        int selectedRow = tblNation.getSelectedRow();

        if (selectedRow == -1) {
            JOptionPane.showMessageDialog(this, "Please select a record to delete.");
        }
        
        String id = (String) tblNation.getValueAt(selectedRow, 0);
        
        int confirm = JOptionPane.showConfirmDialog(
            this,
            "Are you sure you want to delete this student?",
            "Confirm Delete",
            JOptionPane.YES_NO_OPTION
        );
        
        if(confirm == JOptionPane.YES_OPTION) {
            Connection con = (Connection) DBConnection.getConnection();
            try {
                String sql = "DELETE FROM Nation WHERE nationID = ?";
                PreparedStatement pstmt = con.prepareStatement(sql);
                pstmt.setString(1, id);
                int rowsDeleted = pstmt.executeUpdate();

                if (rowsDeleted > 0) {
                    JOptionPane.showMessageDialog(this, "Record deleted successfully!");
                    con.close();
                    loadNationData();
                } else {
                    JOptionPane.showMessageDialog(this, "Failed to delete record.");
                }
            } catch (Exception e) {
                JOptionPane.showMessageDialog(this, "Database error! " + e.getMessage());
            }
        }
    }
    
    private void updateNation(){
        String nationID = txtNationID.getText();
        String name = txtName.getText();
        String continent = txtContinent.getText();
        long population_count = 0;
        int yearEstablished = yearChooser.getYear();
        int selectedRow = tblNation.getSelectedRow();
        String id = (String) tblNation.getValueAt(selectedRow, 0);
        
        if (selectedRow == -1) {
            JOptionPane.showMessageDialog(this, "Please select a record to delete.");
        }
        
        try {
            population_count = Long.parseLong(txtPopulation.getText());
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, "Field must be a number.");
        }
        Connection con = (Connection) DBConnection.getConnection();
        try {
            String sql = "UPDATE Nation SET nationID=?, name=?, continent=?, population_count=?, year_established=? WHERE nationID =?";
            PreparedStatement pstmt = con.prepareStatement(sql);
            Object[] params = {nationID, name, continent, population_count, yearEstablished, id};
            for (int i = 0; i < params.length; i++) {
                pstmt.setObject(i + 1, params[i]);
            }
            pstmt.executeUpdate();
            loadNationData();
            JOptionPane.showMessageDialog(this, "Record updated successfully!");
            con.close();
        } catch (SQLException ex) {
            JOptionPane.showMessageDialog(this, "Database error! " + ex.getMessage());
        }
    }
    
    private void exportData() {
        JFileChooser chooser = new JFileChooser();
        chooser.setDialogTitle("Save Excel File");

        String timestamp = new SimpleDateFormat("yyyy-MM-dd_HHmmss").format(new java.util.Date());
        chooser.setSelectedFile(new java.io.File("nation_list_" + timestamp + ".xlsx"));
        int userSelection = chooser.showSaveDialog(this);

        if (userSelection == JFileChooser.APPROVE_OPTION) {
            File fileToSave = chooser.getSelectedFile();
            if (!fileToSave.getAbsolutePath().toLowerCase().endsWith(".xlsx")) {
                fileToSave = new File(fileToSave.getAbsolutePath() + ".xlsx");
            }

            String query = "SELECT * FROM Nation";

            try (
                Connection con = DBConnection.getConnection();
                Statement stmt = con.createStatement();
                ResultSet rs = stmt.executeQuery(query);
                Workbook workbook = new org.apache.poi.xssf.usermodel.XSSFWorkbook()
            ) {
                Sheet sheet = (Sheet) workbook.createSheet("Nation List");

                ResultSetMetaData meta = rs.getMetaData();
                int columnCount = meta.getColumnCount();
                org.apache.poi.ss.usermodel.Row headerRow = sheet.createRow(0);
                for (int i = 1; i <= columnCount; i++) {
                    headerRow.createCell(i - 1).setCellValue(meta.getColumnName(i));
                }

                int rowIndex = 1;
                while (rs.next()) {
                    org.apache.poi.ss.usermodel.Row row = sheet.createRow(rowIndex++);
                    for (int i = 1; i <= columnCount; i++) {
                        Object value = rs.getObject(i);
                        row.createCell(i - 1).setCellValue(value != null ? value.toString() : "");
                    }
                }

                for (int i = 0; i < columnCount; i++) {
                    sheet.autoSizeColumn(i);
                }

                try (FileOutputStream fileOut = new FileOutputStream(fileToSave)) {
                    workbook.write(fileOut);
                }

                JOptionPane.showMessageDialog(this,
                    "Data exported successfully to:\n" + fileToSave.getAbsolutePath());

            } catch (Exception ex) {
                JOptionPane.showMessageDialog(this, "Error exporting data: " + ex.getMessage());
                ex.printStackTrace();
            }
        }
    }
    
    public Nation() {
        initComponents();
        this.setIconImage(img.getImage());
        this.setLocationRelativeTo(null);
        loadNationData();
    }

    /**
     * This method is called from within the constructor to initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is always
     * regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        Dashboard = new javax.swing.JPanel();
        MainTitle = new javax.swing.JLabel();
        Fields = new javax.swing.JPanel();
        jLabel1 = new javax.swing.JLabel();
        txtNationID = new javax.swing.JTextField();
        jLabel2 = new javax.swing.JLabel();
        txtName = new javax.swing.JTextField();
        jLabel3 = new javax.swing.JLabel();
        btnSaveRecord = new javax.swing.JButton();
        btnDeleteRecord = new javax.swing.JButton();
        btnUpdateRecord = new javax.swing.JButton();
        jLabel4 = new javax.swing.JLabel();
        nationIDLabel = new javax.swing.JLabel();
        txtPopulation = new javax.swing.JTextField();
        txtContinent = new javax.swing.JTextField();
        jLabel6 = new javax.swing.JLabel();
        jLabel7 = new javax.swing.JLabel();
        yearChooser = new com.toedter.calendar.JYearChooser();
        List = new javax.swing.JPanel();
        jScrollPane1 = new javax.swing.JScrollPane();
        tblNation = new javax.swing.JTable();
        btnDepartment = new javax.swing.JButton();
        btnLeader = new javax.swing.JButton();
        btnExport = new javax.swing.JButton();
        jLabel8 = new javax.swing.JLabel();

        setDefaultCloseOperation(javax.swing.WindowConstants.EXIT_ON_CLOSE);
        setTitle("World Leaders Tracker");
        setResizable(false);

        Dashboard.setBorder(javax.swing.BorderFactory.createTitledBorder("Dashboard"));

        MainTitle.setBackground(new java.awt.Color(255, 255, 255));
        MainTitle.setFont(new java.awt.Font("Times New Roman", 1, 24)); // NOI18N
        MainTitle.setForeground(new java.awt.Color(0, 0, 0));
        MainTitle.setText("World Leaders Tracker");

        Fields.setBorder(javax.swing.BorderFactory.createTitledBorder("Fields"));

        jLabel1.setText("Nation ID");

        txtNationID.setPreferredSize(new java.awt.Dimension(65, 25));
        txtNationID.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                txtNationIDActionPerformed(evt);
            }
        });

        jLabel2.setText("Name");

        txtName.setPreferredSize(new java.awt.Dimension(65, 25));
        txtName.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                txtNameActionPerformed(evt);
            }
        });

        jLabel3.setText("Continent");

        btnSaveRecord.setText("Save Record");
        btnSaveRecord.setFocusable(false);
        btnSaveRecord.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnSaveRecordActionPerformed(evt);
            }
        });

        btnDeleteRecord.setText("Delete Record");
        btnDeleteRecord.setFocusable(false);
        btnDeleteRecord.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnDeleteRecordActionPerformed(evt);
            }
        });

        btnUpdateRecord.setText("Update Record");
        btnUpdateRecord.setFocusable(false);
        btnUpdateRecord.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnUpdateRecordActionPerformed(evt);
            }
        });

        jLabel4.setText("Select Nation ID:  ");

        nationIDLabel.setFont(new java.awt.Font("Segoe UI", 1, 18)); // NOI18N
        nationIDLabel.setText("None");

        txtPopulation.setPreferredSize(new java.awt.Dimension(65, 25));
        txtPopulation.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                txtPopulationActionPerformed(evt);
            }
        });

        txtContinent.setPreferredSize(new java.awt.Dimension(65, 25));
        txtContinent.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                txtContinentActionPerformed(evt);
            }
        });

        jLabel6.setText("Year Established");

        jLabel7.setText("Population Count");

        yearChooser.setFont(new java.awt.Font("Segoe UI", 0, 12)); // NOI18N
        yearChooser.setHorizontalAlignment(0);

        javax.swing.GroupLayout FieldsLayout = new javax.swing.GroupLayout(Fields);
        Fields.setLayout(FieldsLayout);
        FieldsLayout.setHorizontalGroup(
            FieldsLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(FieldsLayout.createSequentialGroup()
                .addContainerGap()
                .addGroup(FieldsLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(yearChooser, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.DEFAULT_SIZE, 330, Short.MAX_VALUE)
                    .addComponent(btnSaveRecord, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.DEFAULT_SIZE, 330, Short.MAX_VALUE)
                    .addComponent(txtName, javax.swing.GroupLayout.DEFAULT_SIZE, 330, Short.MAX_VALUE)
                    .addComponent(txtNationID, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.DEFAULT_SIZE, 330, Short.MAX_VALUE)
                    .addComponent(btnUpdateRecord, javax.swing.GroupLayout.DEFAULT_SIZE, 330, Short.MAX_VALUE)
                    .addComponent(txtPopulation, javax.swing.GroupLayout.DEFAULT_SIZE, 330, Short.MAX_VALUE)
                    .addComponent(txtContinent, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.DEFAULT_SIZE, 330, Short.MAX_VALUE)
                    .addComponent(btnDeleteRecord, javax.swing.GroupLayout.DEFAULT_SIZE, 330, Short.MAX_VALUE)
                    .addGroup(FieldsLayout.createSequentialGroup()
                        .addGroup(FieldsLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(jLabel1, javax.swing.GroupLayout.PREFERRED_SIZE, 62, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(jLabel2, javax.swing.GroupLayout.PREFERRED_SIZE, 62, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(jLabel3, javax.swing.GroupLayout.PREFERRED_SIZE, 62, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(jLabel7, javax.swing.GroupLayout.PREFERRED_SIZE, 102, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(jLabel6, javax.swing.GroupLayout.PREFERRED_SIZE, 94, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addGroup(FieldsLayout.createSequentialGroup()
                                .addComponent(jLabel4)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addComponent(nationIDLabel, javax.swing.GroupLayout.PREFERRED_SIZE, 162, javax.swing.GroupLayout.PREFERRED_SIZE)))
                        .addGap(0, 0, Short.MAX_VALUE)))
                .addContainerGap())
        );
        FieldsLayout.setVerticalGroup(
            FieldsLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(FieldsLayout.createSequentialGroup()
                .addComponent(jLabel1)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(txtNationID, javax.swing.GroupLayout.PREFERRED_SIZE, 40, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(12, 12, 12)
                .addComponent(jLabel2)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(txtName, javax.swing.GroupLayout.PREFERRED_SIZE, 40, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addComponent(jLabel3)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(txtContinent, javax.swing.GroupLayout.PREFERRED_SIZE, 40, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addComponent(jLabel7)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(txtPopulation, javax.swing.GroupLayout.PREFERRED_SIZE, 40, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addComponent(jLabel6)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(yearChooser, javax.swing.GroupLayout.PREFERRED_SIZE, 40, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, 21, Short.MAX_VALUE)
                .addGroup(FieldsLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel4, javax.swing.GroupLayout.PREFERRED_SIZE, 34, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(nationIDLabel))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addComponent(btnSaveRecord, javax.swing.GroupLayout.PREFERRED_SIZE, 40, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(btnUpdateRecord, javax.swing.GroupLayout.PREFERRED_SIZE, 40, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(btnDeleteRecord, javax.swing.GroupLayout.PREFERRED_SIZE, 40, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap())
        );

        List.setBorder(javax.swing.BorderFactory.createTitledBorder("List"));

        tblNation.setModel(new javax.swing.table.DefaultTableModel(
            new Object [][] {

            },
            new String [] {
                "Nation ID", "Name", "Continent", "Population Count", "Year Established"
            }
        ) {
            boolean[] canEdit = new boolean [] {
                false, false, false, false, false
            };

            public boolean isCellEditable(int rowIndex, int columnIndex) {
                return canEdit [columnIndex];
            }
        });
        tblNation.setFocusable(false);
        tblNation.getTableHeader().setReorderingAllowed(false);
        tblNation.setUpdateSelectionOnSort(false);
        tblNation.addFocusListener(new java.awt.event.FocusAdapter() {
            public void focusGained(java.awt.event.FocusEvent evt) {
                tblNationFocusGained(evt);
            }
        });
        tblNation.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                tblNationMouseClicked(evt);
            }
        });
        jScrollPane1.setViewportView(tblNation);
        if (tblNation.getColumnModel().getColumnCount() > 0) {
            tblNation.getColumnModel().getColumn(1).setPreferredWidth(170);
        }

        javax.swing.GroupLayout ListLayout = new javax.swing.GroupLayout(List);
        List.setLayout(ListLayout);
        ListLayout.setHorizontalGroup(
            ListLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(ListLayout.createSequentialGroup()
                .addContainerGap()
                .addComponent(jScrollPane1, javax.swing.GroupLayout.DEFAULT_SIZE, 738, Short.MAX_VALUE)
                .addContainerGap())
        );
        ListLayout.setVerticalGroup(
            ListLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(ListLayout.createSequentialGroup()
                .addComponent(jScrollPane1)
                .addContainerGap())
        );

        btnDepartment.setText("Go to Assign Department");
        btnDepartment.setActionCommand("Go to Leader");
        btnDepartment.setFocusable(false);
        btnDepartment.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnDepartmentActionPerformed(evt);
            }
        });

        btnLeader.setText("Go to Assign Leader");
        btnLeader.setFocusable(false);
        btnLeader.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnLeaderActionPerformed(evt);
            }
        });

        btnExport.setText("Export Data");
        btnExport.setFocusable(false);
        btnExport.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnExportActionPerformed(evt);
            }
        });

        jLabel8.setFont(new java.awt.Font("Times New Roman", 0, 24)); // NOI18N
        jLabel8.setForeground(new java.awt.Color(0, 0, 0));
        jLabel8.setText("/  Nation List");

        javax.swing.GroupLayout DashboardLayout = new javax.swing.GroupLayout(Dashboard);
        Dashboard.setLayout(DashboardLayout);
        DashboardLayout.setHorizontalGroup(
            DashboardLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(DashboardLayout.createSequentialGroup()
                .addContainerGap()
                .addGroup(DashboardLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(DashboardLayout.createSequentialGroup()
                        .addComponent(MainTitle, javax.swing.GroupLayout.PREFERRED_SIZE, 246, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(jLabel8, javax.swing.GroupLayout.PREFERRED_SIZE, 174, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                        .addComponent(btnDepartment)
                        .addGap(20, 20, 20)
                        .addComponent(btnLeader, javax.swing.GroupLayout.PREFERRED_SIZE, 150, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addGap(20, 20, 20)
                        .addComponent(btnExport, javax.swing.GroupLayout.PREFERRED_SIZE, 150, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addGap(15, 15, 15))
                    .addGroup(DashboardLayout.createSequentialGroup()
                        .addComponent(Fields, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(List, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                        .addContainerGap())))
        );
        DashboardLayout.setVerticalGroup(
            DashboardLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(DashboardLayout.createSequentialGroup()
                .addGroup(DashboardLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(DashboardLayout.createSequentialGroup()
                        .addGap(15, 15, 15)
                        .addGroup(DashboardLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                            .addComponent(btnLeader, javax.swing.GroupLayout.PREFERRED_SIZE, 30, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(btnExport, javax.swing.GroupLayout.PREFERRED_SIZE, 30, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(btnDepartment, javax.swing.GroupLayout.PREFERRED_SIZE, 30, javax.swing.GroupLayout.PREFERRED_SIZE)))
                    .addGroup(DashboardLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                        .addComponent(MainTitle, javax.swing.GroupLayout.PREFERRED_SIZE, 63, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addComponent(jLabel8)))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(DashboardLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(Fields, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(List, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                .addContainerGap())
        );

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(getContentPane());
        getContentPane().setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(Dashboard, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addContainerGap())
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(Dashboard, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );

        pack();
    }// </editor-fold>//GEN-END:initComponents

    private void btnDepartmentActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnDepartmentActionPerformed
        Head head = new Head();
        head.setVisible(true);
        this.dispose();
    }//GEN-LAST:event_btnDepartmentActionPerformed

    private void btnLeaderActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnLeaderActionPerformed
        Leader leader = new Leader();
        leader.setVisible(true);
        this.dispose();
    }//GEN-LAST:event_btnLeaderActionPerformed

    private void txtNationIDActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_txtNationIDActionPerformed
        // TODO add your handling code here:
    }//GEN-LAST:event_txtNationIDActionPerformed

    private void txtNameActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_txtNameActionPerformed
        // TODO add your handling code here:
    }//GEN-LAST:event_txtNameActionPerformed

    private void btnUpdateRecordActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnUpdateRecordActionPerformed
        updateNation();
    }//GEN-LAST:event_btnUpdateRecordActionPerformed

    private void btnDeleteRecordActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnDeleteRecordActionPerformed
        deleteNation();
    }//GEN-LAST:event_btnDeleteRecordActionPerformed

    private void txtPopulationActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_txtPopulationActionPerformed
        // TODO add your handling code here:
    }//GEN-LAST:event_txtPopulationActionPerformed

    private void txtContinentActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_txtContinentActionPerformed
        // TODO add your handling code here:
    }//GEN-LAST:event_txtContinentActionPerformed

    private void btnExportActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnExportActionPerformed
        exportData();
    }//GEN-LAST:event_btnExportActionPerformed

    private void btnSaveRecordActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnSaveRecordActionPerformed
        addNation();
    }//GEN-LAST:event_btnSaveRecordActionPerformed

    private void tblNationFocusGained(java.awt.event.FocusEvent evt) {//GEN-FIRST:event_tblNationFocusGained
        
    }//GEN-LAST:event_tblNationFocusGained

    private void tblNationMouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_tblNationMouseClicked
        int selectedRow = tblNation.getSelectedRow();
        String id = (String) tblNation.getValueAt(selectedRow, 0);
        nationIDLabel.setText(id);
    }//GEN-LAST:event_tblNationMouseClicked

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
                if ("Windows".equals(info.getName())) {
                    javax.swing.UIManager.setLookAndFeel(info.getClassName());
                    break;
                }
            }
        } catch (ClassNotFoundException ex) {
            java.util.logging.Logger.getLogger(Nation.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        } catch (InstantiationException ex) {
            java.util.logging.Logger.getLogger(Nation.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        } catch (IllegalAccessException ex) {
            java.util.logging.Logger.getLogger(Nation.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        } catch (javax.swing.UnsupportedLookAndFeelException ex) {
            java.util.logging.Logger.getLogger(Nation.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        }
        //</editor-fold>
        //</editor-fold>
        //</editor-fold>
        //</editor-fold>

        /* Create and display the form */
        java.awt.EventQueue.invokeLater(new Runnable() {
            public void run() {
                new Nation().setVisible(true);
            }
        });
    }

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JPanel Dashboard;
    private javax.swing.JPanel Fields;
    private javax.swing.JPanel List;
    private javax.swing.JLabel MainTitle;
    private javax.swing.JButton btnDeleteRecord;
    private javax.swing.JButton btnDepartment;
    private javax.swing.JButton btnExport;
    private javax.swing.JButton btnLeader;
    private javax.swing.JButton btnSaveRecord;
    private javax.swing.JButton btnUpdateRecord;
    private javax.swing.JLabel jLabel1;
    private javax.swing.JLabel jLabel2;
    private javax.swing.JLabel jLabel3;
    private javax.swing.JLabel jLabel4;
    private javax.swing.JLabel jLabel6;
    private javax.swing.JLabel jLabel7;
    private javax.swing.JLabel jLabel8;
    private javax.swing.JScrollPane jScrollPane1;
    private javax.swing.JLabel nationIDLabel;
    private javax.swing.JTable tblNation;
    private javax.swing.JTextField txtContinent;
    private javax.swing.JTextField txtName;
    private javax.swing.JTextField txtNationID;
    private javax.swing.JTextField txtPopulation;
    private com.toedter.calendar.JYearChooser yearChooser;
    // End of variables declaration//GEN-END:variables
}
