package general;

import database.DBConnection;
import java.io.File;
import java.io.FileOutputStream;
import java.sql.*;
import java.text.SimpleDateFormat;
import javax.swing.*;
import javax.swing.table.*;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;

/**
 *
 * @author lenovo
 */
public class Leader extends javax.swing.JFrame {
    private final ImageIcon icon = new ImageIcon("src\\img\\world_leader.png");
    
    
    private void loadLeaderData(){
        Connection con = (Connection) DBConnection.getConnection();
        Statement stmt;
        try {
            stmt = con.createStatement();
            ResultSet rs = stmt.executeQuery("SELECT * FROM Leader");
            DefaultTableModel model = (DefaultTableModel) tblLeader.getModel();
            model.setRowCount(0);
            
            
            while (rs.next()) {
                String id = rs.getString("leaderID");
                String fname = rs.getString("fname");
                String mi = rs.getString("mi");
                String lname = rs.getString("lname");
                Date date = rs.getDate("bdate");
                int age = rs.getInt("age");
                String gender = rs.getString("gender");
                
                String full_name = lname + ", " + fname + " " + (mi.isEmpty()?"":mi+".");
                Object[] row = {id, full_name, age, gender, date};
                model.addRow(row);
            }

            con.close();
        } catch (SQLException ex) {
            JOptionPane.showMessageDialog(this, "Connection failed! " + ex.getMessage());
        }
    }
    
    private void addLeader(){
        String leaderID = txtLID.getText();
        String fname = txtFN.getText();
        String mi = txtMI.getText();
        String lname = txtLN.getText();
        int age = 0;
        String gender = cmbGender.getSelectedItem().toString();
        SimpleDateFormat date_format = new SimpleDateFormat("yyyy-MM-dd");
        String date = date_format.format(dcBirthdate.getDate());
        
        try {
            age = Integer.parseInt(txtAge.getText());
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, "Field must be a number.");
        }

        try {
            
            String sql = "INSERT INTO Leader(leaderID, fname, mi, lname, bdate, age, gender) values (?, ?, ?, ?, ?, ?, ?)";
            
            Connection con = (Connection) DBConnection.getConnection();
            PreparedStatement pstmt = con.prepareStatement(sql);
            
            Object[] params = {leaderID, fname, mi, lname, date, age, gender};
            
            for (int i = 0; i < params.length; i++) {
                pstmt.setObject(i + 1, params[i]);
            }
            pstmt.executeUpdate();
            
            JOptionPane.showMessageDialog(this, "Record added successfully!");
            txtLID.setText("");
            txtFN.setText("");
            txtMI.setText("");
            txtLN.setText("");
            txtAge.setText("");
            
            loadLeaderData();
            con.close();
        } catch (SQLException ex) {
            JOptionPane.showMessageDialog(this, "Database error! " + ex.getMessage());
        }
    }
    private void deleteLeader(){
        int selectedRow = tblLeader.getSelectedRow();
        String id = (String) tblLeader.getValueAt(selectedRow, 0);
        
        if (selectedRow == -1) {
            JOptionPane.showMessageDialog(this, "Please select a record to delete.");
        }
        int confirm = JOptionPane.showConfirmDialog(
            this,
            "Are you sure you want to delete this student?",
            "Confirm Delete",
            JOptionPane.YES_NO_OPTION
        );
        if(confirm == JOptionPane.YES_OPTION) {
            Connection con = (Connection) DBConnection.getConnection();
            try {
                String sql = "DELETE FROM Leader WHERE leaderID = ?";
                PreparedStatement pstmt = con.prepareStatement(sql);
                pstmt.setString(1, id);
                int rowsDeleted = pstmt.executeUpdate();

                if (rowsDeleted > 0) {
                    JOptionPane.showMessageDialog(this, "Record deleted successfully!");
                    con.close();
                    loadLeaderData();
                } else {
                    JOptionPane.showMessageDialog(this, "Failed to delete record.");
                }
                
            } catch (Exception e) {
                JOptionPane.showMessageDialog(this, "Database error! " + e.getMessage());
            }
        }
    }
    private void updateLeader(){
        if (txtFN.getText().trim().isEmpty() || 
            txtMI.getText().trim().isEmpty() ||
            txtLN.getText().trim().isEmpty() ||
            txtLID.getText().trim().isEmpty() ||
            txtAge.getText().trim().isEmpty()
        ){
            JOptionPane.showMessageDialog(null, "Please fill all fields!");
            return;
        }
        String leaderID = txtLID.getText();
        String fname = txtFN.getText();
        String mi = txtMI.getText();
        String lname = txtLN.getText();
        int age = 0;
        String gender = cmbGender.getSelectedItem().toString();
        SimpleDateFormat date_format = new SimpleDateFormat("yyyy-MM-dd");
        String date = date_format.format(dcBirthdate.getDate());
        int selectedRow = tblLeader.getSelectedRow();
        String id = (String) tblLeader.getValueAt(selectedRow, 0);
        System.out.println(selectedRow);
        System.out.println(id);
        
        if (selectedRow == -1) {
            JOptionPane.showMessageDialog(this, "Please select a record to delete.");
        }
        try {
            age = Integer.parseInt(txtAge.getText());
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, "Field must be a number.");
        }
        
        Connection con = (Connection) DBConnection.getConnection();
        try {
            String sql = "UPDATE Leader SET leaderID=?, fname=?, mi=?,lname=?, bdate=?, age=?, gender=? WHERE leaderID=?";
            PreparedStatement pstmt = con.prepareStatement(sql);
            Object[] params = {leaderID, fname, mi, lname, date, age, gender, id};

            for (int i = 0; i < params.length; i++) {
                pstmt.setObject(i + 1, params[i]);
            }
            pstmt.executeUpdate();
            JOptionPane.showMessageDialog(this, "Record deleted successfully!");
            txtLID.setText("");
            txtFN.setText("");
            txtMI.setText("");
            txtLN.setText("");
            txtAge.setText("");
            con.close();
            loadLeaderData();
        } catch (SQLException ex) {
            JOptionPane.showMessageDialog(this, "Database error! " + ex.getMessage());
        }
        
    }
    
    private void exportData() {
        JFileChooser chooser = new JFileChooser();
        chooser.setDialogTitle("Save Excel File");

        String timestamp = new SimpleDateFormat("yyyy-MM-dd_HHmmss").format(new java.util.Date());
        chooser.setSelectedFile(new java.io.File("leader_list_" + timestamp + ".xlsx"));
        int userSelection = chooser.showSaveDialog(this);

        if (userSelection == JFileChooser.APPROVE_OPTION) {
            File fileToSave = chooser.getSelectedFile();
            if (!fileToSave.getAbsolutePath().toLowerCase().endsWith(".xlsx")) {
                fileToSave = new File(fileToSave.getAbsolutePath() + ".xlsx");
            }

            String query = "SELECT * FROM Leader";

            try (
                Connection con = DBConnection.getConnection();
                Statement stmt = con.createStatement();
                ResultSet rs = stmt.executeQuery(query);
                Workbook workbook = new org.apache.poi.xssf.usermodel.XSSFWorkbook()
            ) {
                Sheet sheet = (Sheet) workbook.createSheet("Leader List");

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
    
    /**
     * Creates new form MainFrame
     */
    public Leader() {
        initComponents();
        this.setIconImage(icon.getImage());

        this.setLocationRelativeTo(null);
        loadLeaderData();
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
        btnDepartment = new javax.swing.JButton();
        MainTitle = new javax.swing.JLabel();
        LeaderList = new javax.swing.JLabel();
        btnNation = new javax.swing.JButton();
        btnExport = new javax.swing.JButton();
        Fields = new javax.swing.JPanel();
        txtLID = new javax.swing.JTextField();
        lLeaderID = new javax.swing.JLabel();
        lFirstName = new javax.swing.JLabel();
        lMiddleIni = new javax.swing.JLabel();
        lLastName = new javax.swing.JLabel();
        txtFN = new javax.swing.JTextField();
        txtMI = new javax.swing.JTextField();
        txtLN = new javax.swing.JTextField();
        lAge = new javax.swing.JLabel();
        lBirthdate = new javax.swing.JLabel();
        lGender = new javax.swing.JLabel();
        cmbGender = new javax.swing.JComboBox<>();
        txtAge = new javax.swing.JTextField();
        btnSaveRecord = new javax.swing.JButton();
        btnUpdateRecord = new javax.swing.JButton();
        btnDeleteRecord = new javax.swing.JButton();
        lSelectedLeadID = new javax.swing.JLabel();
        lStatus = new javax.swing.JLabel();
        dcBirthdate = new com.toedter.calendar.JDateChooser();
        List = new javax.swing.JPanel();
        theTable = new javax.swing.JScrollPane();
        tblLeader = new javax.swing.JTable();

        setDefaultCloseOperation(javax.swing.WindowConstants.EXIT_ON_CLOSE);
        setTitle("World Leaders Tracker");
        setResizable(false);

        Dashboard.setBorder(javax.swing.BorderFactory.createTitledBorder("Dashboard"));

        btnDepartment.setText("Go to Assign Department");
        btnDepartment.setActionCommand("Go to Leader");
        btnDepartment.setFocusable(false);
        btnDepartment.setPreferredSize(new java.awt.Dimension(135, 20));
        btnDepartment.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnDepartmentActionPerformed(evt);
            }
        });

        MainTitle.setBackground(new java.awt.Color(255, 255, 255));
        MainTitle.setFont(new java.awt.Font("Times New Roman", 1, 24)); // NOI18N
        MainTitle.setForeground(new java.awt.Color(0, 0, 0));
        MainTitle.setText("World Leaders Tracker");

        LeaderList.setFont(new java.awt.Font("Times New Roman", 0, 24)); // NOI18N
        LeaderList.setForeground(new java.awt.Color(0, 0, 0));
        LeaderList.setText("/  Leader");

        btnNation.setText("Go to Nation List");
        btnNation.setFocusable(false);
        btnNation.setPreferredSize(new java.awt.Dimension(135, 20));
        btnNation.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnNationActionPerformed(evt);
            }
        });

        btnExport.setText("Export Data");
        btnExport.setFocusable(false);
        btnExport.setPreferredSize(new java.awt.Dimension(135, 20));
        btnExport.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnExportActionPerformed(evt);
            }
        });

        Fields.setBorder(javax.swing.BorderFactory.createTitledBorder("Fields"));

        txtLID.setPreferredSize(new java.awt.Dimension(65, 25));
        txtLID.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                txtLIDActionPerformed(evt);
            }
        });

        lLeaderID.setText("Leader ID");

        lFirstName.setText("First Name");

        lMiddleIni.setText("Middle Initial");

        lLastName.setText("Last Name");

        txtFN.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                txtFNActionPerformed(evt);
            }
        });

        txtMI.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                txtMIActionPerformed(evt);
            }
        });

        txtLN.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                txtLNActionPerformed(evt);
            }
        });

        lAge.setText("Age");

        lBirthdate.setText("Birthdate");

        lGender.setText("Gender");

        cmbGender.setModel(new javax.swing.DefaultComboBoxModel<>(new String[] { "Male", "Female" }));
        cmbGender.setFocusable(false);
        cmbGender.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                cmbGenderActionPerformed(evt);
            }
        });

        txtAge.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                txtAgeActionPerformed(evt);
            }
        });

        btnSaveRecord.setText("Save Record");
        btnSaveRecord.setFocusable(false);
        btnSaveRecord.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnSaveRecordActionPerformed(evt);
            }
        });

        btnUpdateRecord.setText("Update Record");
        btnUpdateRecord.setFocusable(false);
        btnUpdateRecord.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnUpdateRecordActionPerformed(evt);
            }
        });

        btnDeleteRecord.setText("Delete Record");
        btnDeleteRecord.setFocusable(false);
        btnDeleteRecord.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnDeleteRecordActionPerformed(evt);
            }
        });

        lSelectedLeadID.setText("Selected Leader ID:");

        lStatus.setFont(new java.awt.Font("Segoe UI", 1, 18)); // NOI18N
        lStatus.setText("None");

        dcBirthdate.setAutoscrolls(true);
        dcBirthdate.setFocusable(false);

        javax.swing.GroupLayout FieldsLayout = new javax.swing.GroupLayout(Fields);
        Fields.setLayout(FieldsLayout);
        FieldsLayout.setHorizontalGroup(
            FieldsLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(FieldsLayout.createSequentialGroup()
                .addGroup(FieldsLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(FieldsLayout.createSequentialGroup()
                        .addContainerGap()
                        .addGroup(FieldsLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(txtAge, javax.swing.GroupLayout.Alignment.TRAILING)
                            .addComponent(txtLID, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                            .addComponent(txtMI)
                            .addComponent(txtFN)
                            .addComponent(txtLN)
                            .addComponent(lLeaderID, javax.swing.GroupLayout.PREFERRED_SIZE, 62, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(lFirstName, javax.swing.GroupLayout.PREFERRED_SIZE, 67, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(lMiddleIni)
                            .addComponent(lLastName)
                            .addComponent(lAge)
                            .addGroup(FieldsLayout.createSequentialGroup()
                                .addGroup(FieldsLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                    .addComponent(lGender)
                                    .addComponent(cmbGender, javax.swing.GroupLayout.PREFERRED_SIZE, 84, javax.swing.GroupLayout.PREFERRED_SIZE))
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addGroup(FieldsLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                    .addGroup(FieldsLayout.createSequentialGroup()
                                        .addComponent(lBirthdate)
                                        .addGap(0, 0, Short.MAX_VALUE))
                                    .addComponent(dcBirthdate, javax.swing.GroupLayout.DEFAULT_SIZE, 240, Short.MAX_VALUE)))))
                    .addGroup(FieldsLayout.createSequentialGroup()
                        .addGap(15, 15, 15)
                        .addComponent(lSelectedLeadID)
                        .addGap(18, 18, 18)
                        .addComponent(lStatus, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                    .addGroup(FieldsLayout.createSequentialGroup()
                        .addContainerGap()
                        .addGroup(FieldsLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(btnSaveRecord, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                            .addComponent(btnUpdateRecord, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                            .addComponent(btnDeleteRecord, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))))
                .addGap(14, 14, 14))
        );
        FieldsLayout.setVerticalGroup(
            FieldsLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(FieldsLayout.createSequentialGroup()
                .addComponent(lLeaderID)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(txtLID, javax.swing.GroupLayout.PREFERRED_SIZE, 40, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addComponent(lFirstName)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(txtFN, javax.swing.GroupLayout.PREFERRED_SIZE, 40, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addComponent(lMiddleIni)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(txtMI, javax.swing.GroupLayout.PREFERRED_SIZE, 40, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addComponent(lLastName)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(txtLN, javax.swing.GroupLayout.PREFERRED_SIZE, 40, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addComponent(lAge)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(txtAge, javax.swing.GroupLayout.PREFERRED_SIZE, 40, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addGroup(FieldsLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(lGender)
                    .addComponent(lBirthdate))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(FieldsLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(cmbGender, javax.swing.GroupLayout.PREFERRED_SIZE, 40, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(dcBirthdate, javax.swing.GroupLayout.PREFERRED_SIZE, 40, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, 22, Short.MAX_VALUE)
                .addGroup(FieldsLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(lSelectedLeadID)
                    .addComponent(lStatus))
                .addGap(18, 18, 18)
                .addComponent(btnSaveRecord, javax.swing.GroupLayout.PREFERRED_SIZE, 40, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(btnUpdateRecord, javax.swing.GroupLayout.PREFERRED_SIZE, 40, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(btnDeleteRecord, javax.swing.GroupLayout.PREFERRED_SIZE, 40, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(7, 7, 7))
        );

        List.setBorder(javax.swing.BorderFactory.createTitledBorder("List"));

        tblLeader.setModel(new javax.swing.table.DefaultTableModel(
            new Object [][] {

            },
            new String [] {
                "Leader ID", "Fullname", "Age", "Gender", "Birthdate"
            }
        ));
        tblLeader.setFocusable(false);
        tblLeader.getTableHeader().setReorderingAllowed(false);
        tblLeader.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                tblLeaderMouseClicked(evt);
            }
        });
        theTable.setViewportView(tblLeader);
        if (tblLeader.getColumnModel().getColumnCount() > 0) {
            tblLeader.getColumnModel().getColumn(0).setPreferredWidth(10);
            tblLeader.getColumnModel().getColumn(1).setPreferredWidth(130);
            tblLeader.getColumnModel().getColumn(2).setPreferredWidth(25);
            tblLeader.getColumnModel().getColumn(3).setPreferredWidth(5);
            tblLeader.getColumnModel().getColumn(4).setPreferredWidth(50);
        }

        javax.swing.GroupLayout ListLayout = new javax.swing.GroupLayout(List);
        List.setLayout(ListLayout);
        ListLayout.setHorizontalGroup(
            ListLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, ListLayout.createSequentialGroup()
                .addContainerGap()
                .addComponent(theTable, javax.swing.GroupLayout.DEFAULT_SIZE, 695, Short.MAX_VALUE)
                .addContainerGap())
        );
        ListLayout.setVerticalGroup(
            ListLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(ListLayout.createSequentialGroup()
                .addGap(5, 5, 5)
                .addComponent(theTable)
                .addContainerGap())
        );

        javax.swing.GroupLayout DashboardLayout = new javax.swing.GroupLayout(Dashboard);
        Dashboard.setLayout(DashboardLayout);
        DashboardLayout.setHorizontalGroup(
            DashboardLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(DashboardLayout.createSequentialGroup()
                .addContainerGap()
                .addGroup(DashboardLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(DashboardLayout.createSequentialGroup()
                        .addComponent(Fields, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                        .addComponent(List, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                    .addGroup(DashboardLayout.createSequentialGroup()
                        .addComponent(MainTitle)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                        .addComponent(LeaderList)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                        .addComponent(btnDepartment, javax.swing.GroupLayout.PREFERRED_SIZE, 165, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addGap(18, 18, 18)
                        .addComponent(btnNation, javax.swing.GroupLayout.PREFERRED_SIZE, 150, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addGap(20, 20, 20)
                        .addComponent(btnExport, javax.swing.GroupLayout.PREFERRED_SIZE, 150, javax.swing.GroupLayout.PREFERRED_SIZE)))
                .addContainerGap())
        );
        DashboardLayout.setVerticalGroup(
            DashboardLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(DashboardLayout.createSequentialGroup()
                .addGroup(DashboardLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(DashboardLayout.createSequentialGroup()
                        .addGap(17, 17, 17)
                        .addGroup(DashboardLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                            .addComponent(MainTitle)
                            .addComponent(LeaderList)))
                    .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, DashboardLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                        .addComponent(btnExport, javax.swing.GroupLayout.PREFERRED_SIZE, 30, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addComponent(btnNation, javax.swing.GroupLayout.PREFERRED_SIZE, 30, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addComponent(btnDepartment, javax.swing.GroupLayout.PREFERRED_SIZE, 30, javax.swing.GroupLayout.PREFERRED_SIZE)))
                .addGap(18, 18, 18)
                .addGroup(DashboardLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                    .addComponent(Fields, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(List, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                .addContainerGap(10, Short.MAX_VALUE))
        );

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(getContentPane());
        getContentPane().setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(Dashboard, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addContainerGap())
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(Dashboard, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addContainerGap())
        );

        pack();
    }// </editor-fold>//GEN-END:initComponents

    private void btnDepartmentActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnDepartmentActionPerformed
        // TODO add your handling code here:
        Head head = new Head();
        head.setVisible(true);
        this.dispose();
    }//GEN-LAST:event_btnDepartmentActionPerformed

    private void btnNationActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnNationActionPerformed
        // TODO add your handling code here:
        Nation nation = new Nation();
        nation.setVisible(true);
        this.dispose();
    }//GEN-LAST:event_btnNationActionPerformed

    private void txtLIDActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_txtLIDActionPerformed
        // TODO add your handling code here:
    }//GEN-LAST:event_txtLIDActionPerformed

    private void btnExportActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnExportActionPerformed
        exportData();
    }//GEN-LAST:event_btnExportActionPerformed

    private void txtLNActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_txtLNActionPerformed
        // TODO add your handling code here:
    }//GEN-LAST:event_txtLNActionPerformed

    private void txtMIActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_txtMIActionPerformed
        // TODO add your handling code here:
    }//GEN-LAST:event_txtMIActionPerformed

    private void txtFNActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_txtFNActionPerformed
        // TODO add your handling code here:
    }//GEN-LAST:event_txtFNActionPerformed

    private void txtAgeActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_txtAgeActionPerformed
        // TODO add your handling code here:
    }//GEN-LAST:event_txtAgeActionPerformed

    private void cmbGenderActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_cmbGenderActionPerformed
        // TODO add your handling code here:
    }//GEN-LAST:event_cmbGenderActionPerformed

    private void btnSaveRecordActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnSaveRecordActionPerformed
        addLeader();
    }//GEN-LAST:event_btnSaveRecordActionPerformed

    private void btnDeleteRecordActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnDeleteRecordActionPerformed
        deleteLeader();
    }//GEN-LAST:event_btnDeleteRecordActionPerformed

    private void btnUpdateRecordActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnUpdateRecordActionPerformed
        updateLeader();
    }//GEN-LAST:event_btnUpdateRecordActionPerformed

    private void tblLeaderMouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_tblLeaderMouseClicked
        int selectedRow = tblLeader.getSelectedRow();
        String id = (String) tblLeader.getValueAt(selectedRow, 0);
        lStatus.setText(id);
    }//GEN-LAST:event_tblLeaderMouseClicked

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
            java.util.logging.Logger.getLogger(Leader.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        } catch (InstantiationException ex) {
            java.util.logging.Logger.getLogger(Leader.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        } catch (IllegalAccessException ex) {
            java.util.logging.Logger.getLogger(Leader.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        } catch (javax.swing.UnsupportedLookAndFeelException ex) {
            java.util.logging.Logger.getLogger(Leader.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        }
        //</editor-fold>
        //</editor-fold>

        /* Create and display the form */
        java.awt.EventQueue.invokeLater(new Runnable() {
            public void run() {
                new Leader().setVisible(true);
            }
        });
    }

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JPanel Dashboard;
    private javax.swing.JPanel Fields;
    private javax.swing.JLabel LeaderList;
    private javax.swing.JPanel List;
    private javax.swing.JLabel MainTitle;
    private javax.swing.JButton btnDeleteRecord;
    private javax.swing.JButton btnDepartment;
    private javax.swing.JButton btnExport;
    private javax.swing.JButton btnNation;
    private javax.swing.JButton btnSaveRecord;
    private javax.swing.JButton btnUpdateRecord;
    private javax.swing.JComboBox<String> cmbGender;
    private com.toedter.calendar.JDateChooser dcBirthdate;
    private javax.swing.JLabel lAge;
    private javax.swing.JLabel lBirthdate;
    private javax.swing.JLabel lFirstName;
    private javax.swing.JLabel lGender;
    private javax.swing.JLabel lLastName;
    private javax.swing.JLabel lLeaderID;
    private javax.swing.JLabel lMiddleIni;
    private javax.swing.JLabel lSelectedLeadID;
    private javax.swing.JLabel lStatus;
    private javax.swing.JTable tblLeader;
    private javax.swing.JScrollPane theTable;
    private javax.swing.JTextField txtAge;
    private javax.swing.JTextField txtFN;
    private javax.swing.JTextField txtLID;
    private javax.swing.JTextField txtLN;
    private javax.swing.JTextField txtMI;
    // End of variables declaration//GEN-END:variables
}
