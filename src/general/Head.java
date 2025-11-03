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
 * @author Secret
 */
public class Head extends javax.swing.JFrame {
    
    private static final java.util.logging.Logger logger = java.util.logging.Logger.getLogger(Head.class.getName());
    private final ImageIcon icon = new ImageIcon("src\\img\\world_leader.png");
    
    private void loadHeadData(){
        Connection con = (Connection) DBConnection.getConnection();
        Statement stmt;
        String query = "SELECT " +
                   "Heads.electID, " +
                   "CONCAT(Leader.lname, ', ', Leader.fname, ' ', " +
                   "IF(Leader.mi IS NOT NULL AND Leader.mi != '', CONCAT(Leader.mi, '.'), '')) AS fullname, " +
                   "Nation.name AS nation, " +
                   "Heads.department, " +
                   "Heads.date_from, " +
                   "Heads.date_to " +
                   "FROM Heads " +
                   "JOIN Leader ON Heads.leaderID = Leader.leaderID " +
                   "JOIN Nation ON Heads.nationID = Nation.nationID;";
        try {
            cmbLeader.removeAllItems();
            cmbNation.removeAllItems();
            stmt = con.createStatement();
            ResultSet rs = stmt.executeQuery("SELECT fname, mi, lname FROM Leader");
            
            while(rs.next()){
                String fname = rs.getString("fname");
                String mi = rs.getString("mi");
                String lname = rs.getString("lname");
                String full_name = lname + ", " + fname + " " + (mi.isEmpty()?"":mi+".");
                cmbLeader.addItem(full_name);
            }
            stmt = con.createStatement();
            rs = stmt.executeQuery("SELECT name FROM Nation");
            
            while(rs.next()){
                cmbNation.addItem(rs.getString("name"));
            }
            stmt = con.createStatement();
            rs = stmt.executeQuery(query);
            
            DefaultTableModel model = (DefaultTableModel) tblHead.getModel();
            model.setRowCount(0);
            
            
            while (rs.next()) {
                Object[] row = {
                    rs.getString("electID"),
                    rs.getString("fullname"),
                    rs.getString("nation"),
                    rs.getString("department"),
                    rs.getDate("date_from"),
                    rs.getDate("date_to"),
                };
                model.addRow(row);
            }

            con.close();
            
        } catch (SQLException ex) {
            JOptionPane.showMessageDialog(this, "Connection failed! " + ex.getMessage());
        }
        
    }
    private void addHead(){
        String electID = txtElectID.getText();
        String department = txtDepartment.getText();
        String leader = cmbLeader.getSelectedItem().toString();
        String nation = cmbNation.getSelectedItem().toString();
        SimpleDateFormat date_format = new SimpleDateFormat("yyyy-MM-dd");
        String dateFrom = date_format.format(dtcDateFrom.getDate());
        String dateTo = date_format.format(dtcDateTo.getDate());

        String sql = "INSERT INTO Heads(electID, leaderID, nationID, department, date_from, date_to) values (?, ?, ?, ?, ?, ?)";
        String query = "SELECT nationId from Nation where name=?;";
        String query2 = "Select leaderID FROM Leader WHERE CONCAT(lname, ', ', fname, ' ', IF(mi IS NOT NULL AND mi!='', CONCAT(mi, '.'), ''))=?";
        Connection con = (Connection) DBConnection.getConnection();
        try {
            PreparedStatement pstmt = con.prepareStatement(query);
            pstmt.setString(1, nation);
            ResultSet rs = pstmt.executeQuery();
            rs.next();
            String nID = rs.getString("nationID");
            
            PreparedStatement pstmt2 = con.prepareStatement(query2);
            pstmt2.setString(1, leader);
            ResultSet rs2 = pstmt2.executeQuery();
            rs2.next();
            String lID = rs2.getString("leaderID");
            
            Object[] params = {electID,  lID, nID, department, dateFrom, dateTo};
            
            PreparedStatement pstmtMain = con.prepareStatement(sql);
            
            for (int i = 0; i < params.length; i++) {
                pstmtMain.setObject(i + 1, params[i]);
            }
            pstmtMain.executeUpdate();
            JOptionPane.showMessageDialog(this, "Successfully added!");
            txtElectID.setText("");
            txtDepartment.setText("");
            dtcDateFrom.setCalendar(null);
            dtcDateTo.setCalendar(null);
            loadHeadData();
        } catch (SQLException ex) {
            JOptionPane.showMessageDialog(this, "Database error! " + ex.getMessage());
        }
    }
    
    private void deleteHead(){
        int selectedRow = tblHead.getSelectedRow();
        String id = (String) tblHead.getValueAt(selectedRow, 0);
        System.out.println(id);
        
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
                String sql = "DELETE FROM Heads WHERE electID = ?";
                PreparedStatement pstmt = con.prepareStatement(sql);
                pstmt.setString(1, id);
                int rowsDeleted = pstmt.executeUpdate();

                if (rowsDeleted > 0) {
                    JOptionPane.showMessageDialog(this, "Record deleted successfully!");
                    con.close();
                    loadHeadData();
                    electIDLabel.setText("None");
                } else {
                    JOptionPane.showMessageDialog(this, "Failed to delete record.");
                }
                
            } catch (Exception e) {
                JOptionPane.showMessageDialog(this, "Database error! " + e.getMessage());
            }
        }
    }
        
    private void updateHead(){
        String electID = txtElectID.getText();
        String department = txtDepartment.getText();
        String leader = cmbLeader.getSelectedItem().toString();
        String nation = cmbNation.getSelectedItem().toString();
        SimpleDateFormat date_format = new SimpleDateFormat("yyyy-MM-dd");
        String dateFrom = date_format.format(dtcDateFrom.getDate());
        String dateTo = date_format.format(dtcDateTo.getDate());
        int selectedRow = tblHead.getSelectedRow();
        String id = (String) tblHead.getValueAt(selectedRow, 0);
        
        String sql = "UPDATE Heads SET electID=?, leaderID=?, nationID=?, department=?, date_from=?, date_to=?";
        String query = "SELECT nationId from Nation where name=?;";
        String query2 = "Select leaderID FROM Leader WHERE CONCAT(lname, ', ', fname, ' ', IF(mi IS NOT NULL AND mi!='', CONCAT(mi, '.'), ''))=?";
        Connection con = (Connection) DBConnection.getConnection();
        try {
            PreparedStatement pstmt = con.prepareStatement(query);
            pstmt.setString(1, nation);
            ResultSet rs = pstmt.executeQuery();
            rs.next();
            String nID = rs.getString("nationID");
            
            PreparedStatement pstmt2 = con.prepareStatement(query2);
            pstmt2.setString(1, leader);
            ResultSet rs2 = pstmt2.executeQuery();
            rs2.next();
            String lID = rs2.getString("leaderID");
            
            Object[] params = {electID,  lID, nID, department, dateFrom, dateTo};
            
            PreparedStatement pstmtMain = con.prepareStatement(sql);
            
            for (int i = 0; i < params.length; i++) {
                pstmtMain.setObject(i + 1, params[i]);
            }
            pstmtMain.executeUpdate();
            JOptionPane.showMessageDialog(this, "Successfully added!");
            txtElectID.setText("");
            txtDepartment.setText("");
            dtcDateFrom.setCalendar(null);
            dtcDateTo.setCalendar(null);
            loadHeadData();
        } catch (SQLException ex) {
            JOptionPane.showMessageDialog(this, "Database error! " + ex.getMessage());
        }
        
    }
    
    private void exportData() {
        JFileChooser chooser = new JFileChooser();
        chooser.setDialogTitle("Save Excel File");

        String timestamp = new SimpleDateFormat("yyyy-MM-dd_HHmmss").format(new java.util.Date());
        chooser.setSelectedFile(new java.io.File("head_list_" + timestamp + ".xlsx"));
        int userSelection = chooser.showSaveDialog(this);

        if (userSelection == JFileChooser.APPROVE_OPTION) {
            File fileToSave = chooser.getSelectedFile();
            if (!fileToSave.getAbsolutePath().toLowerCase().endsWith(".xlsx")) {
                fileToSave = new File(fileToSave.getAbsolutePath() + ".xlsx");
            }

            String query = "SELECT " +
                   "Heads.electID, " +
                   "CONCAT(Leader.lname, ', ', Leader.fname, ' ', " +
                   "IF(Leader.mi IS NOT NULL AND Leader.mi != '', CONCAT(Leader.mi, '.'), '')) AS fullname, " +
                   "Nation.name AS nation, " +
                   "Heads.department, " +
                   "Heads.date_from, " +
                   "Heads.date_to " +
                   "FROM Heads " +
                   "JOIN Leader ON Heads.leaderID = Leader.leaderID " +
                   "JOIN Nation ON Heads.nationID = Nation.nationID;";

            try (
                Connection con = DBConnection.getConnection();
                Statement stmt = con.createStatement();
                ResultSet rs = stmt.executeQuery(query);
                Workbook workbook = new org.apache.poi.xssf.usermodel.XSSFWorkbook()
            ) {
                Sheet sheet = (Sheet) workbook.createSheet("Head List");

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
     * Creates new form Head
     */
    public Head() {
        initComponents();
        this.setIconImage(icon.getImage());
        this.setLocationRelativeTo(null);
        loadHeadData();
    }

    /**
     * This method is called from within the constructor to initialize the form. WARNING: Do NOT modify this code. The content of this method is always regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        jLabel2 = new javax.swing.JLabel();
        Dashboard = new javax.swing.JPanel();
        Fields = new javax.swing.JPanel();
        txtElectID = new javax.swing.JTextField();
        jLabel3 = new javax.swing.JLabel();
        jLabel4 = new javax.swing.JLabel();
        jLabel5 = new javax.swing.JLabel();
        jLabel6 = new javax.swing.JLabel();
        txtDepartment = new javax.swing.JTextField();
        jLabel7 = new javax.swing.JLabel();
        jLabel8 = new javax.swing.JLabel();
        jLabel9 = new javax.swing.JLabel();
        electIDLabel = new javax.swing.JLabel();
        btnSave = new javax.swing.JButton();
        btnUpdate = new javax.swing.JButton();
        btnDelete = new javax.swing.JButton();
        cmbLeader = new javax.swing.JComboBox<>();
        cmbNation = new javax.swing.JComboBox<>();
        dtcDateFrom = new com.toedter.calendar.JDateChooser();
        dtcDateTo = new com.toedter.calendar.JDateChooser();
        List = new javax.swing.JPanel();
        jScrollPane1 = new javax.swing.JScrollPane();
        tblHead = new javax.swing.JTable();
        btnNationList = new javax.swing.JButton();
        jLabel1 = new javax.swing.JLabel();
        btnExport = new javax.swing.JButton();
        btnAssignLeader = new javax.swing.JButton();
        jLabel11 = new javax.swing.JLabel();

        jLabel2.setText("jLabel2");

        setDefaultCloseOperation(javax.swing.WindowConstants.EXIT_ON_CLOSE);
        setTitle("World Leaders Tracker");
        setResizable(false);
        setSize(new java.awt.Dimension(1152, 648));

        Dashboard.setBorder(javax.swing.BorderFactory.createTitledBorder("Dashboard"));
        Dashboard.setForeground(new java.awt.Color(255, 255, 255));

        Fields.setBorder(javax.swing.BorderFactory.createTitledBorder("Fields"));

        txtElectID.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                txtElectIDActionPerformed(evt);
            }
        });

        jLabel3.setText("Elect ID");

        jLabel4.setText("Leader");

        jLabel5.setText("Nation");

        jLabel6.setText("Department");

        txtDepartment.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                txtDepartmentActionPerformed(evt);
            }
        });

        jLabel7.setText("Date from");

        jLabel8.setText("Date to");

        jLabel9.setText("Selected Elected ID:");

        electIDLabel.setFont(new java.awt.Font("Segoe UI", 1, 18)); // NOI18N
        electIDLabel.setText("None");

        btnSave.setText("Save Record");
        btnSave.setFocusable(false);
        btnSave.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnSaveActionPerformed(evt);
            }
        });

        btnUpdate.setText("Update Record");
        btnUpdate.setFocusable(false);
        btnUpdate.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnUpdateActionPerformed(evt);
            }
        });

        btnDelete.setText("Delete Record");
        btnDelete.setFocusable(false);
        btnDelete.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnDeleteActionPerformed(evt);
            }
        });

        cmbLeader.setFocusable(false);
        cmbLeader.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                cmbLeaderActionPerformed(evt);
            }
        });

        cmbNation.setCursor(new java.awt.Cursor(java.awt.Cursor.DEFAULT_CURSOR));
        cmbNation.setFocusable(false);

        javax.swing.GroupLayout FieldsLayout = new javax.swing.GroupLayout(Fields);
        Fields.setLayout(FieldsLayout);
        FieldsLayout.setHorizontalGroup(
            FieldsLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(FieldsLayout.createSequentialGroup()
                .addContainerGap()
                .addGroup(FieldsLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(FieldsLayout.createSequentialGroup()
                        .addComponent(dtcDateFrom, javax.swing.GroupLayout.DEFAULT_SIZE, 157, Short.MAX_VALUE)
                        .addGap(18, 18, 18)
                        .addComponent(dtcDateTo, javax.swing.GroupLayout.PREFERRED_SIZE, 155, javax.swing.GroupLayout.PREFERRED_SIZE))
                    .addGroup(FieldsLayout.createSequentialGroup()
                        .addGroup(FieldsLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addGroup(FieldsLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                                .addComponent(txtElectID)
                                .addComponent(jLabel3, javax.swing.GroupLayout.PREFERRED_SIZE, 47, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addComponent(txtDepartment)
                                .addGroup(FieldsLayout.createSequentialGroup()
                                    .addComponent(jLabel7, javax.swing.GroupLayout.PREFERRED_SIZE, 64, javax.swing.GroupLayout.PREFERRED_SIZE)
                                    .addGap(111, 111, 111)
                                    .addComponent(jLabel8, javax.swing.GroupLayout.PREFERRED_SIZE, 55, javax.swing.GroupLayout.PREFERRED_SIZE))
                                .addComponent(btnSave, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                                .addComponent(btnUpdate, javax.swing.GroupLayout.DEFAULT_SIZE, 330, Short.MAX_VALUE)
                                .addComponent(btnDelete, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                                .addComponent(cmbLeader, 0, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                                .addComponent(cmbNation, 0, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                                .addComponent(jLabel4, javax.swing.GroupLayout.PREFERRED_SIZE, 52, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addComponent(jLabel5, javax.swing.GroupLayout.PREFERRED_SIZE, 58, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addComponent(jLabel6, javax.swing.GroupLayout.PREFERRED_SIZE, 73, javax.swing.GroupLayout.PREFERRED_SIZE))
                            .addGroup(FieldsLayout.createSequentialGroup()
                                .addComponent(jLabel9)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addComponent(electIDLabel, javax.swing.GroupLayout.PREFERRED_SIZE, 175, javax.swing.GroupLayout.PREFERRED_SIZE)))
                        .addGap(0, 0, Short.MAX_VALUE)))
                .addContainerGap())
        );
        FieldsLayout.setVerticalGroup(
            FieldsLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(FieldsLayout.createSequentialGroup()
                .addContainerGap()
                .addComponent(jLabel3)
                .addGap(4, 4, 4)
                .addComponent(txtElectID, javax.swing.GroupLayout.PREFERRED_SIZE, 40, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addComponent(jLabel4)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(cmbLeader, javax.swing.GroupLayout.PREFERRED_SIZE, 40, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addComponent(jLabel5)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(cmbNation, javax.swing.GroupLayout.PREFERRED_SIZE, 40, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addComponent(jLabel6)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(txtDepartment, javax.swing.GroupLayout.PREFERRED_SIZE, 40, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addGroup(FieldsLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel7)
                    .addComponent(jLabel8, javax.swing.GroupLayout.PREFERRED_SIZE, 16, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(FieldsLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                    .addComponent(dtcDateTo, javax.swing.GroupLayout.DEFAULT_SIZE, 40, Short.MAX_VALUE)
                    .addComponent(dtcDateFrom, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                .addGap(19, 19, 19)
                .addGroup(FieldsLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel9)
                    .addComponent(electIDLabel))
                .addGap(18, 18, 18)
                .addComponent(btnSave, javax.swing.GroupLayout.PREFERRED_SIZE, 40, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(btnUpdate, javax.swing.GroupLayout.PREFERRED_SIZE, 40, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(btnDelete, javax.swing.GroupLayout.PREFERRED_SIZE, 40, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );

        List.setBorder(javax.swing.BorderFactory.createTitledBorder("List"));

        tblHead.setModel(new javax.swing.table.DefaultTableModel(
            new Object [][] {

            },
            new String [] {
                "ElectID", "Leader", "Nation", "Department", "Date to", "Date from"
            }
        ) {
            boolean[] canEdit = new boolean [] {
                false, false, false, false, false, false
            };

            public boolean isCellEditable(int rowIndex, int columnIndex) {
                return canEdit [columnIndex];
            }
        });
        tblHead.setFocusable(false);
        tblHead.getTableHeader().setReorderingAllowed(false);
        tblHead.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                tblHeadMouseClicked(evt);
            }
        });
        jScrollPane1.setViewportView(tblHead);
        if (tblHead.getColumnModel().getColumnCount() > 0) {
            tblHead.getColumnModel().getColumn(0).setPreferredWidth(40);
            tblHead.getColumnModel().getColumn(1).setPreferredWidth(100);
            tblHead.getColumnModel().getColumn(2).setPreferredWidth(75);
            tblHead.getColumnModel().getColumn(3).setPreferredWidth(75);
        }

        javax.swing.GroupLayout ListLayout = new javax.swing.GroupLayout(List);
        List.setLayout(ListLayout);
        ListLayout.setHorizontalGroup(
            ListLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(ListLayout.createSequentialGroup()
                .addContainerGap()
                .addComponent(jScrollPane1, javax.swing.GroupLayout.DEFAULT_SIZE, 802, Short.MAX_VALUE)
                .addContainerGap())
        );
        ListLayout.setVerticalGroup(
            ListLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(ListLayout.createSequentialGroup()
                .addComponent(jScrollPane1, javax.swing.GroupLayout.DEFAULT_SIZE, 551, Short.MAX_VALUE)
                .addContainerGap())
        );

        btnNationList.setText("Go to Nation List");
        btnNationList.setFocusable(false);
        btnNationList.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnNationListActionPerformed(evt);
            }
        });

        jLabel1.setFont(new java.awt.Font("Times New Roman", 1, 24)); // NOI18N
        jLabel1.setForeground(new java.awt.Color(0, 0, 0));
        jLabel1.setText("World Leaders Tracker");

        btnExport.setText("Export Data");
        btnExport.setFocusable(false);
        btnExport.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnExportActionPerformed(evt);
            }
        });

        btnAssignLeader.setText("Go to Assign Leader");
        btnAssignLeader.setFocusable(false);
        btnAssignLeader.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnAssignLeaderActionPerformed(evt);
            }
        });

        jLabel11.setFont(new java.awt.Font("Times New Roman", 0, 24)); // NOI18N
        jLabel11.setForeground(new java.awt.Color(0, 0, 0));
        jLabel11.setText("/  Department");

        javax.swing.GroupLayout DashboardLayout = new javax.swing.GroupLayout(Dashboard);
        Dashboard.setLayout(DashboardLayout);
        DashboardLayout.setHorizontalGroup(
            DashboardLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(DashboardLayout.createSequentialGroup()
                .addContainerGap()
                .addGroup(DashboardLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                    .addGroup(DashboardLayout.createSequentialGroup()
                        .addComponent(Fields, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                        .addComponent(List, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                    .addGroup(DashboardLayout.createSequentialGroup()
                        .addComponent(jLabel1, javax.swing.GroupLayout.PREFERRED_SIZE, 245, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(jLabel11, javax.swing.GroupLayout.PREFERRED_SIZE, 246, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                        .addComponent(btnAssignLeader, javax.swing.GroupLayout.PREFERRED_SIZE, 150, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addGap(20, 20, 20)
                        .addComponent(btnNationList, javax.swing.GroupLayout.PREFERRED_SIZE, 150, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addGap(20, 20, 20)
                        .addComponent(btnExport, javax.swing.GroupLayout.PREFERRED_SIZE, 150, javax.swing.GroupLayout.PREFERRED_SIZE)))
                .addContainerGap())
        );
        DashboardLayout.setVerticalGroup(
            DashboardLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, DashboardLayout.createSequentialGroup()
                .addGap(23, 23, 23)
                .addGroup(DashboardLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jLabel1, javax.swing.GroupLayout.PREFERRED_SIZE, 30, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addGroup(DashboardLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                        .addComponent(btnExport, javax.swing.GroupLayout.PREFERRED_SIZE, 30, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addComponent(btnNationList, javax.swing.GroupLayout.PREFERRED_SIZE, 30, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addComponent(btnAssignLeader, javax.swing.GroupLayout.PREFERRED_SIZE, 30, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addComponent(jLabel11, javax.swing.GroupLayout.PREFERRED_SIZE, 30, javax.swing.GroupLayout.PREFERRED_SIZE)))
                .addGap(18, 18, 18)
                .addGroup(DashboardLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                    .addComponent(List, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(Fields, javax.swing.GroupLayout.PREFERRED_SIZE, 580, Short.MAX_VALUE))
                .addContainerGap(11, Short.MAX_VALUE))
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
                .addComponent(Dashboard, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addContainerGap())
        );

        pack();
    }// </editor-fold>//GEN-END:initComponents

    private void btnNationListActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnNationListActionPerformed
        Nation nation = new Nation();
        nation.setVisible(true);
        this.dispose();
    }//GEN-LAST:event_btnNationListActionPerformed

    private void txtElectIDActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_txtElectIDActionPerformed
        // TODO add your handling code here:
    }//GEN-LAST:event_txtElectIDActionPerformed

    private void txtDepartmentActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_txtDepartmentActionPerformed
        // TODO add your handling code here:
    }//GEN-LAST:event_txtDepartmentActionPerformed

    private void cmbLeaderActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_cmbLeaderActionPerformed
        // TODO add your handling code here:
    }//GEN-LAST:event_cmbLeaderActionPerformed

    private void btnAssignLeaderActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnAssignLeaderActionPerformed
        Leader leader = new Leader();
        leader.setVisible(true);
        this.dispose();
    }//GEN-LAST:event_btnAssignLeaderActionPerformed

    private void btnDeleteActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnDeleteActionPerformed
        deleteHead();
    }//GEN-LAST:event_btnDeleteActionPerformed

    private void btnSaveActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnSaveActionPerformed
        addHead();
    }//GEN-LAST:event_btnSaveActionPerformed

    private void btnUpdateActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnUpdateActionPerformed
        updateHead();
    }//GEN-LAST:event_btnUpdateActionPerformed

    private void btnExportActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnExportActionPerformed
        exportData();
    }//GEN-LAST:event_btnExportActionPerformed

    private void tblHeadMouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_tblHeadMouseClicked
        int selectedRow = tblHead.getSelectedRow();
        String id = (String) tblHead.getValueAt(selectedRow, 0);
        electIDLabel.setText(id);
    }//GEN-LAST:event_tblHeadMouseClicked

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
        } catch (ReflectiveOperationException | javax.swing.UnsupportedLookAndFeelException ex) {
            logger.log(java.util.logging.Level.SEVERE, null, ex);
        }
        //</editor-fold>

        /* Create and display the form */
        java.awt.EventQueue.invokeLater(() -> new Head().setVisible(true));
    }
    
    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JPanel Dashboard;
    private javax.swing.JPanel Fields;
    private javax.swing.JPanel List;
    private javax.swing.JButton btnAssignLeader;
    private javax.swing.JButton btnDelete;
    private javax.swing.JButton btnExport;
    private javax.swing.JButton btnNationList;
    private javax.swing.JButton btnSave;
    private javax.swing.JButton btnUpdate;
    private javax.swing.JComboBox<String> cmbLeader;
    private javax.swing.JComboBox<String> cmbNation;
    private com.toedter.calendar.JDateChooser dtcDateFrom;
    private com.toedter.calendar.JDateChooser dtcDateTo;
    private javax.swing.JLabel electIDLabel;
    private javax.swing.JLabel jLabel1;
    private javax.swing.JLabel jLabel11;
    private javax.swing.JLabel jLabel2;
    private javax.swing.JLabel jLabel3;
    private javax.swing.JLabel jLabel4;
    private javax.swing.JLabel jLabel5;
    private javax.swing.JLabel jLabel6;
    private javax.swing.JLabel jLabel7;
    private javax.swing.JLabel jLabel8;
    private javax.swing.JLabel jLabel9;
    private javax.swing.JScrollPane jScrollPane1;
    private javax.swing.JTable tblHead;
    private javax.swing.JTextField txtDepartment;
    private javax.swing.JTextField txtElectID;
    // End of variables declaration//GEN-END:variables
}
