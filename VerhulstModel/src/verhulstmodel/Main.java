/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package verhulstmodel;

import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.GridLayout;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.image.BufferedImage;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Iterator;
import java.util.List;
import javax.imageio.ImageIO;
import javax.swing.BorderFactory;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSpinner;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.KeyStroke;
import javax.swing.ProgressMonitor;
import javax.swing.SpinnerModel;
import javax.swing.SpinnerNumberModel;
import javax.swing.SwingConstants;
import javax.swing.SwingWorker;
import javax.swing.UIManager;
import javax.swing.table.JTableHeader;
import javax.swing.table.TableColumn;
import javax.swing.table.TableColumnModel;
import javax.swing.table.TableModel;
import org.jfree.chart.ChartFactory;
import org.jfree.chart.JFreeChart;
import org.jfree.data.time.TimeSeries;
import org.jfree.data.time.TimeSeriesCollection;
import org.jfree.data.xy.XYDataset;
import org.jfree.data.time.Day;

/**
 *
 * @author pavel
 */
public class Main extends JFrame implements ActionListener, PropertyChangeListener {
    JFileChooser fch = new JFileChooser();
    BufferedImage lastPlot;
    JComboBox undependentVarCombo;
    JComboBox dependentVarCombo;
    JSpinner minRowSpinner;
    JSpinner maxRowSpinner;
    JSpinner stepSpinner;
    JSpinner modelRowsSpinner;
    JSpinner verhulstInitialPopMinSpinner;
    JSpinner verhulstInitialPopStepSpinner;
    JSpinner verhulstInitialPopMaxSpinner;
    JSpinner verhulstCapacityMinSpinner;
    JSpinner verhulstCapacityStepSpinner;
    JSpinner verhulstCapacityMaxSpinner;
    JSpinner verhulstRateMinSpinner;
    JSpinner verhulstRateStepSpinner;
    JSpinner verhulstRateMaxSpinner;
    JSpinner gompertzDisplacementMinSpinner;
    JSpinner gompertzDisplacementStepSpinner;
    JSpinner gompertzDisplacementMaxSpinner;
    JSpinner gompertzCapacityMinSpinner;
    JSpinner gompertzCapacityStepSpinner;
    JSpinner gompertzCapacityMaxSpinner;
    JSpinner gompertzRateMinSpinner;
    JSpinner gompertzRateStepSpinner;
    JSpinner gompertzRateMaxSpinner;

    JTable table;
    JTextField manualInitialPop;
    JTextField manualCapacity;
    JTextField manualRate;

    JTextField gManualDisplacement;
    JTextField gManualCapacity;
    JTextField gManualRate;


    List<Integer> undepVar;
    List<Integer> depVar;
    JPanel rightPanel;

    ProgressMonitor progressMonitor;

    public Main() {
        super("Verhust and Gompertz Models");
        try {
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
        } catch (Exception e) {
            showError(e);
        }
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLayout(new GridLayout(2,1));

        //creating menu
        JMenuBar menuBar = new JMenuBar();
        JMenu fileMenu = new JMenu("File");
        JMenuItem openCSVMenuItem = new JMenuItem("Open CSV");
        openCSVMenuItem.addActionListener(this);
        openCSVMenuItem.setAccelerator(KeyStroke.getKeyStroke('O', java.awt.event.InputEvent.CTRL_DOWN_MASK));
        openCSVMenuItem.setActionCommand("openCSV");
        fileMenu.add(openCSVMenuItem);
        menuBar.add(fileMenu);
        setJMenuBar(menuBar);

        JPanel topPanel = new JPanel(new GridLayout(1,2));
        topPanel.setBorder(BorderFactory.createTitledBorder("Data"));
        table = new JTable(365 * 5, 11); //TODO dynamically add row
        JScrollPane scrollPane = new JScrollPane(table);
        JTable rowTable = new RowNumberTable(table);
        scrollPane.setRowHeaderView(rowTable);
        scrollPane.setCorner(JScrollPane.UPPER_LEFT_CORNER, rowTable.getTableHeader());
        //scrollPane.setSize( (int) Math.rint(0.2 * getScreenWidth()/4), (int) Math.rint( 0.2 * getScreenHeight()/4));
        topPanel.add(scrollPane);

        JPanel panel = new JPanel(new GridLayout(3,1));
        JPanel rowPanel = new JPanel();
        JLabel label = new JLabel("Undependent variable:");
        rowPanel.add(label);
        undependentVarCombo = new JComboBox();
        rowPanel.add(undependentVarCombo);
        label = new JLabel("Dependent variable:");
        rowPanel.add(label);
        dependentVarCombo = new JComboBox();
        rowPanel.add(dependentVarCombo);
        panel.add(rowPanel);

        rowPanel = new JPanel();
        label = new JLabel("Use rows from #:");
        rowPanel.add(label);
        SpinnerModel spinnerModel = new SpinnerNumberModel(1, 1, 100000, 1);
        minRowSpinner = new JSpinner(spinnerModel);
        rowPanel.add(minRowSpinner);
        label = new JLabel("to #:");
        rowPanel.add(label);
        spinnerModel = new SpinnerNumberModel(1, 1, 100000, 1);
        maxRowSpinner = new JSpinner(spinnerModel);
        rowPanel.add(maxRowSpinner);
        label = new JLabel("Step:");
        rowPanel.add(label);
        spinnerModel = new SpinnerNumberModel(1, 1, 10000000, 1);
        stepSpinner = new JSpinner(spinnerModel);
        rowPanel.add(stepSpinner);
        panel.add(rowPanel);

        rowPanel = new JPanel();
        label = new JLabel("Model # of days further:");
        rowPanel.add(label);
        spinnerModel = new SpinnerNumberModel(365, 0, 100000, 1);
        modelRowsSpinner = new JSpinner(spinnerModel);
        rowPanel.add(modelRowsSpinner);
        panel.add(rowPanel);
        topPanel.add(panel);
        add(topPanel);

        //bottomPanel
        JPanel bottomPanel = new JPanel(new GridLayout(1, 2));
        JPanel leftPanel = new JPanel();//new GridLayout(3,1));
        leftPanel.setBorder(BorderFactory.createTitledBorder("Verhulst's model"));

        panel = new JPanel(new GridLayout(4,1));
        rowPanel = new JPanel();
        label = new JLabel("Initial population varies from:");
        rowPanel.add(label);
        spinnerModel = new SpinnerNumberModel(0.000, 0, 10000, 0.001);
        verhulstInitialPopMinSpinner = new JSpinner(spinnerModel);
        JSpinner.NumberEditor editor = (JSpinner.NumberEditor) verhulstInitialPopMinSpinner.getEditor();
        DecimalFormat format = editor.getFormat();
        format.setMinimumFractionDigits(5);
        editor.getTextField().setHorizontalAlignment(SwingConstants.CENTER);
        verhulstInitialPopMinSpinner.setPreferredSize(new Dimension(90, verhulstInitialPopMinSpinner.getPreferredSize().height));
        rowPanel.add(verhulstInitialPopMinSpinner);
        label = new JLabel("to:");
        rowPanel.add(label);
        spinnerModel = new SpinnerNumberModel(500, 0, 10000, 1);
        verhulstInitialPopMaxSpinner = new JSpinner(spinnerModel);
        editor = (JSpinner.NumberEditor) verhulstInitialPopMaxSpinner.getEditor();
        format = editor.getFormat();
        format.setMinimumFractionDigits(5);
        editor.getTextField().setHorizontalAlignment(SwingConstants.CENTER);
        verhulstInitialPopMaxSpinner.setPreferredSize(new Dimension(90, verhulstInitialPopMaxSpinner.getPreferredSize().height));
        rowPanel.add(verhulstInitialPopMaxSpinner);
        label = new JLabel("Step:");
        rowPanel.add(label);
        spinnerModel = new SpinnerNumberModel(1, 0, 100, 0.0001);
        verhulstInitialPopStepSpinner = new JSpinner(spinnerModel);
        editor = (JSpinner.NumberEditor) verhulstInitialPopStepSpinner.getEditor();
        format = editor.getFormat();
        format.setMinimumFractionDigits(5);
        editor.getTextField().setHorizontalAlignment(SwingConstants.CENTER);
        verhulstInitialPopStepSpinner.setPreferredSize(new Dimension(90, verhulstInitialPopStepSpinner.getPreferredSize().height));
        rowPanel.add(verhulstInitialPopStepSpinner);
        panel.add(rowPanel);

        rowPanel = new JPanel();
        label = new JLabel("Capacity varies from:");
        rowPanel.add(label);
        spinnerModel = new SpinnerNumberModel(6000, 100, 100000, 100);
        verhulstCapacityMinSpinner = new JSpinner(spinnerModel);
        rowPanel.add(verhulstCapacityMinSpinner);
        label = new JLabel("to:");
        rowPanel.add(label);
        spinnerModel = new SpinnerNumberModel(15000, 100, 100000, 100);
        verhulstCapacityMaxSpinner = new JSpinner(spinnerModel);
        rowPanel.add(verhulstCapacityMaxSpinner);
        label = new JLabel("Step:");
        rowPanel.add(label);
        spinnerModel = new SpinnerNumberModel(500, 0, 100000, 500);
        verhulstCapacityStepSpinner = new JSpinner(spinnerModel);
        rowPanel.add(verhulstCapacityStepSpinner);
        panel.add(rowPanel);

        rowPanel = new JPanel();
        label = new JLabel("Rate varies from:");
        rowPanel.add(label);
        spinnerModel = new SpinnerNumberModel(0.001, 0, 50, 0.0001);
        verhulstRateMinSpinner = new JSpinner(spinnerModel);
        editor = (JSpinner.NumberEditor) verhulstRateMinSpinner.getEditor();
        format = editor.getFormat();
        format.setMinimumFractionDigits(5);
        editor.getTextField().setHorizontalAlignment(SwingConstants.CENTER);
        verhulstRateMinSpinner.setPreferredSize(new Dimension(90, verhulstRateMinSpinner.getPreferredSize().height));
        rowPanel.add(verhulstRateMinSpinner);
        label = new JLabel("to:");
        rowPanel.add(label);
        spinnerModel = new SpinnerNumberModel(0.01, 0, 50, 0.0001);
        verhulstRateMaxSpinner = new JSpinner(spinnerModel);
        editor = (JSpinner.NumberEditor) verhulstRateMaxSpinner.getEditor();
        format = editor.getFormat();
        format.setMinimumFractionDigits(5);
        editor.getTextField().setHorizontalAlignment(SwingConstants.CENTER);
        verhulstRateMaxSpinner.setPreferredSize(new Dimension(90, verhulstRateMaxSpinner.getPreferredSize().height));
        rowPanel.add(verhulstRateMaxSpinner);
        label = new JLabel("Step:");
        rowPanel.add(label);
        spinnerModel = new SpinnerNumberModel(0.0001, 0, 5, 0.00001);
        verhulstRateStepSpinner = new JSpinner(spinnerModel);
        editor = (JSpinner.NumberEditor) verhulstRateStepSpinner.getEditor();
        format = editor.getFormat();
        format.setMinimumFractionDigits(5);
        editor.getTextField().setHorizontalAlignment(SwingConstants.CENTER);
        verhulstRateStepSpinner.setPreferredSize(new Dimension(90, verhulstRateStepSpinner.getPreferredSize().height));
        rowPanel.add(verhulstRateStepSpinner);
        panel.add(rowPanel);

        JButton verhulst = new JButton("Run!");
        verhulst.setActionCommand("verhulst");
        verhulst.addActionListener(this);
        panel.add(verhulst);
        leftPanel.add(panel);

        rowPanel = new JPanel();
        JButton countButton = new JButton("Count Elementar Operations");
        countButton.setActionCommand("countOperations");
        countButton.addActionListener(this);
        rowPanel.add(countButton);
        leftPanel.add(rowPanel);

        panel = new JPanel();
        rowPanel = new JPanel();
        label = new JLabel("Set rate to:");
        rowPanel.add(label);
        manualRate = new JTextField(5);
        rowPanel.add(manualRate);
        panel.add(rowPanel);
        rowPanel = new JPanel();
        label = new JLabel("Set capacity to:");
        rowPanel.add(label);
        manualCapacity = new JTextField(5);
        rowPanel.add(manualCapacity);
        panel.add(rowPanel);
        rowPanel = new JPanel();
        label = new JLabel("Set initial pop to:");
        rowPanel.add(label);
        manualInitialPop = new JTextField(5);
        rowPanel.add(manualInitialPop);
        panel.add(rowPanel);
        JButton button = new JButton("Plot");
        button.setActionCommand("plot");
        button.addActionListener(this);
        panel.add(button);
        leftPanel.add(panel);

        bottomPanel.add(leftPanel);

        rightPanel = new JPanel();
        rightPanel.setBorder(BorderFactory.createTitledBorder("Gompertz's model"));

        panel = new JPanel(new GridLayout(4,1));
        rowPanel = new JPanel();
        label = new JLabel("Displacement varies from:");
        rowPanel.add(label);
        spinnerModel = new SpinnerNumberModel(-50, -1000, 0, 0.001);
        gompertzDisplacementMinSpinner = new JSpinner(spinnerModel);
        editor = (JSpinner.NumberEditor) gompertzDisplacementMinSpinner.getEditor();
        format = editor.getFormat();
        format.setMinimumFractionDigits(5);
        editor.getTextField().setHorizontalAlignment(SwingConstants.CENTER);
        gompertzDisplacementMinSpinner.setPreferredSize(new Dimension(90, gompertzDisplacementMinSpinner.getPreferredSize().height));
        rowPanel.add(gompertzDisplacementMinSpinner);
        label = new JLabel("to:");
        rowPanel.add(label);
        spinnerModel = new SpinnerNumberModel(0, -10000, 0.000, 0.001);
        gompertzDisplacementMaxSpinner = new JSpinner(spinnerModel);
        editor = (JSpinner.NumberEditor) gompertzDisplacementMaxSpinner.getEditor();
        format = editor.getFormat();
        format.setMinimumFractionDigits(5);
        editor.getTextField().setHorizontalAlignment(SwingConstants.CENTER);
        gompertzDisplacementMaxSpinner.setPreferredSize(new Dimension(90, gompertzDisplacementMaxSpinner.getPreferredSize().height));
        rowPanel.add(gompertzDisplacementMaxSpinner);
        label = new JLabel("Step:");
        rowPanel.add(label);
        spinnerModel = new SpinnerNumberModel(1, 0, 100, 0.0001);
        gompertzDisplacementStepSpinner = new JSpinner(spinnerModel);
        editor = (JSpinner.NumberEditor) gompertzDisplacementStepSpinner.getEditor();
        format = editor.getFormat();
        format.setMinimumFractionDigits(5);
        editor.getTextField().setHorizontalAlignment(SwingConstants.CENTER);
        gompertzDisplacementStepSpinner.setPreferredSize(new Dimension(90, gompertzDisplacementStepSpinner.getPreferredSize().height));
        rowPanel.add(gompertzDisplacementStepSpinner);
        panel.add(rowPanel);

        rowPanel = new JPanel();
        label = new JLabel("Capacity varies from:");
        rowPanel.add(label);
        spinnerModel = new SpinnerNumberModel(6000, 100, 100000, 100);
        gompertzCapacityMinSpinner = new JSpinner(spinnerModel);
        rowPanel.add(gompertzCapacityMinSpinner);
        label = new JLabel("to:");
        rowPanel.add(label);
        spinnerModel = new SpinnerNumberModel(15000, 100, 100000, 100);
        gompertzCapacityMaxSpinner = new JSpinner(spinnerModel);
        rowPanel.add(gompertzCapacityMaxSpinner);
        label = new JLabel("Step:");
        rowPanel.add(label);
        spinnerModel = new SpinnerNumberModel(500, 0, 100000, 500);
        gompertzCapacityStepSpinner = new JSpinner(spinnerModel);
        rowPanel.add(gompertzCapacityStepSpinner);
        panel.add(rowPanel);

        rowPanel = new JPanel();
        label = new JLabel("Rate varies from:");
        rowPanel.add(label);
        spinnerModel = new SpinnerNumberModel(-0.01, -50, 0, 0.0001);
        gompertzRateMinSpinner = new JSpinner(spinnerModel);
        editor = (JSpinner.NumberEditor) gompertzRateMinSpinner.getEditor();
        format = editor.getFormat();
        format.setMinimumFractionDigits(5);
        editor.getTextField().setHorizontalAlignment(SwingConstants.CENTER);
        gompertzRateMinSpinner.setPreferredSize(new Dimension(90, gompertzRateMinSpinner.getPreferredSize().height));
        rowPanel.add(gompertzRateMinSpinner);
        label = new JLabel("to:");
        rowPanel.add(label);
        spinnerModel = new SpinnerNumberModel(0, -50, 0, 0.0001);
        gompertzRateMaxSpinner = new JSpinner(spinnerModel);
        editor = (JSpinner.NumberEditor) gompertzRateMaxSpinner.getEditor();
        format = editor.getFormat();
        format.setMinimumFractionDigits(5);
        editor.getTextField().setHorizontalAlignment(SwingConstants.CENTER);
        gompertzRateMaxSpinner.setPreferredSize(new Dimension(90, gompertzRateMaxSpinner.getPreferredSize().height));
        rowPanel.add(gompertzRateMaxSpinner);
        label = new JLabel("Step:");
        rowPanel.add(label);
        spinnerModel = new SpinnerNumberModel(0.0001, 0, 5, 0.00001);
        gompertzRateStepSpinner = new JSpinner(spinnerModel);
        editor = (JSpinner.NumberEditor) gompertzRateStepSpinner.getEditor();
        format = editor.getFormat();
        format.setMinimumFractionDigits(5);
        editor.getTextField().setHorizontalAlignment(SwingConstants.CENTER);
        gompertzRateStepSpinner.setPreferredSize(new Dimension(90, gompertzRateStepSpinner.getPreferredSize().height));
        rowPanel.add(gompertzRateStepSpinner);
        panel.add(rowPanel);

        JButton gompertz = new JButton("Run!");
        gompertz.setActionCommand("gompertz");
        gompertz.addActionListener(this);
        panel.add(gompertz);
        rightPanel.add(panel);

        rowPanel = new JPanel();
        JButton gCountButton = new JButton("Count Elementar Operations");
        gCountButton.setActionCommand("gCountOperations");
        gCountButton.addActionListener(this);
        rowPanel.add(gCountButton);
        rightPanel.add(rowPanel);

        panel = new JPanel();
        rowPanel = new JPanel();
        label = new JLabel("Set rate to:");
        rowPanel.add(label);
        gManualRate = new JTextField(5);
        rowPanel.add(gManualRate);
        panel.add(rowPanel);
        rowPanel = new JPanel();
        label = new JLabel("Set capacity to:");
        rowPanel.add(label);
        gManualCapacity = new JTextField(5);
        rowPanel.add(gManualCapacity);
        panel.add(rowPanel);
        rowPanel = new JPanel();
        label = new JLabel("Set displacement:");
        rowPanel.add(label);
        gManualDisplacement = new JTextField(5);
        rowPanel.add(gManualDisplacement);
        panel.add(rowPanel);
        JButton gButton = new JButton("Plot");
        gButton.setActionCommand("plotGompertz");
        gButton.addActionListener(this);
        panel.add(gButton);
        rightPanel.add(panel);

        bottomPanel.add(rightPanel);
        add(bottomPanel);
    }
    public static void main(String[] args) {
        Main mainFrame = new Main();
        Toolkit toolkit = Toolkit.getDefaultToolkit();
        mainFrame.setSize(toolkit.getScreenSize());
        mainFrame.setVisible(true);
    }

    private void showError(Exception e) {
        JOptionPane.showMessageDialog(this, e.getLocalizedMessage(), "Error!", JOptionPane.ERROR_MESSAGE);
        e.printStackTrace();
    }
    private void showError(String message) {
        JOptionPane.showMessageDialog(this, message, "Error!", JOptionPane.ERROR_MESSAGE);
    }


    void openCSV() {
        //TODO csv only
        //fch.addChoosableFileFilter(null);
        int returnVal = fch.showOpenDialog(this);
        if (returnVal == JFileChooser.APPROVE_OPTION) {
            File file = fch.getSelectedFile();
            try {
                parseCSV(file);
            } catch (Exception e) {
                showError(e);
            }
        }
    }

    private void parseCSV(File file) throws FileNotFoundException, IOException {
        BufferedReader br = new BufferedReader(new FileReader(file));
        String line = "";
        String[] fields;
        int row = 0;

        while ((line = br.readLine()) != null) {
            fields = line.split(";");
            for (int column = 0; column < fields.length; column++) {
                if (row == 0) {
                    dependentVarCombo.addItem(fields[column]);
                    undependentVarCombo.addItem(fields[column]);
                    JTableHeader th = table.getTableHeader();
                    TableColumnModel tcm = th.getColumnModel();
                    TableColumn tc = tcm.getColumn(column);
                    tc.setHeaderValue(fields[column]);
                    th.repaint();
                } else {
                    table.getModel().setValueAt(fields[column], row - 1, column);
                }
            }
            row++;
        }
        maxRowSpinner.setValue(row - 1);
        try { dependentVarCombo.setSelectedIndex(2); } catch (Exception e) {}
        br.close();
    }
    private boolean getData() {
        int minRow = (Integer) minRowSpinner.getValue();
        int maxRow = (Integer) maxRowSpinner.getValue();
        int stepRow = (Integer) stepSpinner.getValue();
        int undepColumn = undependentVarCombo.getSelectedIndex();
        int depColumn = dependentVarCombo.getSelectedIndex();
        TableModel tm = table.getModel();


        if (minRow > maxRow || maxRow > tm.getRowCount()) {
            showError("Min row index is greater than max row index!");
            return false;
        } else {
            undepVar = new ArrayList<Integer>();
            depVar = new ArrayList<Integer>();
            try {
                Day day = getDay((String) tm.getValueAt(minRow - 1, undepColumn));
                Day dataDay = day;
                Day lastDay = getDay((String) tm.getValueAt(maxRow - 1, undepColumn));
                int row = minRow - 1;
                int time = 0;
                while (!day.equals(lastDay)) {
                    if (day.equals(dataDay)) {
                        undepVar.add(time);
                        depVar.add(Integer.parseInt((String) tm.getValueAt(row, depColumn)));
                        System.out.println(String.valueOf(time) + "," + (String) tm.getValueAt(row, undepColumn));
                        row += stepRow;
                    }
                    day = (Day) day.next();
                    dataDay = getDay((String) tm.getValueAt(row, undepColumn));
                    time++;
                }
            } catch (Exception e) {
                showError(e);
                return false;
            }
            return true;
        }
    }

    private void verhulst() {
        VerhulstTask task = new VerhulstTask(this);
        task.addPropertyChangeListener(this);
        progressMonitor = new ProgressMonitor(this, "Fitting Verhulst model...", null, 0, 100);
        progressMonitor.setProgress(0);
        setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));
        task.execute();
    }

    private void gompertz() {
        GompertzTask task = new GompertzTask(this);
        task.addPropertyChangeListener(this);
        progressMonitor = new ProgressMonitor(this, "Fitting Gompertz model...", null, 0, 100);
        progressMonitor.setProgress(0);
        setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));
        task.execute();
    }


    private Day getDay(String dayCode) {
        int year = Integer.parseInt(dayCode.substring(0, 4));
        int month = Integer.parseInt(dayCode.substring(5,7));
        int day = Integer.parseInt(dayCode.substring(8,10));
        return new Day(day, month, year);
    }

    void plotVerhulst(double rate, int capacity, double initialPopulation) {
        int minRow = (Integer) minRowSpinner.getValue();
        int maxRow = (Integer) maxRowSpinner.getValue();
        int stepRow = (Integer) stepSpinner.getValue();
        int modelRow = (Integer) modelRowsSpinner.getValue();
        String rateStr = String.valueOf(rate);
        String capacityStr = String.valueOf(capacity);
        String initialPopStr = String.valueOf(initialPopulation);
        TimeSeries modelSeries = new TimeSeries("Verhulst Model (initial pop. " + initialPopStr + ", capacity " + capacityStr + ", rate " + rateStr + ")");
        TimeSeries dataSeries = new TimeSeries("Data");
        int row = minRow - 1;
        Day day = getDay((String) table.getValueAt(row, undependentVarCombo.getSelectedIndex()));
        Day dataDay = day;
        Day lastDay = getDay((String) table.getValueAt(maxRow - 1, undependentVarCombo.getSelectedIndex()));
        int time = 0;
        int dataValue;
        while (!day.equals(lastDay)) {
            if (day.equals(dataDay)) {
                dataValue = Integer.parseInt((String) table.getValueAt(row, dependentVarCombo.getSelectedIndex()));
                dataSeries.add(day, dataValue);
                row++;
            }
            modelSeries.add(day, verhulstValue(time, rate, capacity, initialPopulation));
            day = (Day) day.next();
            dataDay = getDay((String) table.getValueAt(row, undependentVarCombo.getSelectedIndex()));
            time++;
        }
        for (int i = 0; i < modelRow; i++) {
            day = (Day) day.next();
            modelSeries.add(day, verhulstValue(time, rate, capacity, initialPopulation));
            time++;
        }
        TimeSeriesCollection collection = new TimeSeriesCollection();
        collection.addSeries(dataSeries);
        collection.addSeries(modelSeries);
        XYDataset data = collection;
        JFreeChart chart = ChartFactory.createTimeSeriesChart("Verhulst model", "time", (String) dependentVarCombo.getSelectedItem(), data, true, true, true);
        BufferedImage chartImage = chart.createBufferedImage(1024, 1024);
        lastPlot = chartImage;
        JLabel label = new JLabel();
        label.setIcon(new ImageIcon(chartImage));
        JScrollPane scrollPane = new JScrollPane(label);
        JFrame frame = new JFrame("Plot");

        JMenuBar menuBar = new JMenuBar();
        JMenu saveMenu = new JMenu("Save");
        JMenuItem saveChartMenuItem = new JMenuItem("Save chart to a png file...");
        saveChartMenuItem.addActionListener(this);
        saveChartMenuItem.setAccelerator(KeyStroke.getKeyStroke('S', java.awt.event.InputEvent.CTRL_DOWN_MASK));
        saveChartMenuItem.setActionCommand("saveChart");
        saveMenu.add(saveChartMenuItem);
        menuBar.add(saveMenu);
        frame.setJMenuBar(menuBar);
        frame.setContentPane(scrollPane);
        frame.setSize(1024, 500);
        frame.setVisible(true);
    }
    double verhulstValue(int time, double rate, int capacity, double initialPopulation) {
        double exprt = Math.exp(time * rate);
        return (capacity * initialPopulation * exprt)/(capacity + initialPopulation * (exprt - 1));
    }
    double verhulstCost(double rate, int capacity, double initialPopulation) {
        double cost = 0;
        int x;
        int y;
        Iterator<Integer> xIt = undepVar.iterator();
        Iterator<Integer> yIt = depVar.iterator();

        while (xIt.hasNext() && yIt.hasNext()) {
            x = xIt.next();
            y = yIt.next();
            cost += Math.pow(y - verhulstValue(x, rate, capacity, initialPopulation), 2);
        }
        return cost;
    }

    double gompertzValue(int time, double rate, int capacity, double displacement) {
        double exprt = Math.exp(time * rate);
        double exprt2 = Math.exp(displacement * exprt);
        return (capacity * exprt2);
    }
    double gompertzCost(double rate, int capacity, double displacement) {
        double cost = 0;
        int x;
        int y;
        Iterator<Integer> xIt = undepVar.iterator();
        Iterator<Integer> yIt = depVar.iterator();
        while (xIt.hasNext() && yIt.hasNext()) {
            x = xIt.next();
            y = yIt.next();
            cost += Math.pow(y - gompertzValue(x, rate, capacity, displacement), 2);
        }
        return cost;
    }
    void plotGompertz(double rate, int capacity, double displacement) {
        int minRow = (Integer) minRowSpinner.getValue();
        int maxRow = (Integer) maxRowSpinner.getValue();
        int modelRow = (Integer) modelRowsSpinner.getValue();
        String rateStr = String.valueOf(rate);
        String capacityStr = String.valueOf(capacity);
        String displacementStr = String.valueOf(displacement);
        TimeSeries modelSeries = new TimeSeries("Gompertz Model (displacement " + displacementStr + ", capacity " + capacityStr + ", rate " + rateStr + ")");
        TimeSeries dataSeries = new TimeSeries("Data");
        int row = minRow - 1;
        Day day = getDay((String) table.getValueAt(row, undependentVarCombo.getSelectedIndex()));
        Day dataDay = day;
        Day lastDay = getDay((String) table.getValueAt(maxRow - 1, undependentVarCombo.getSelectedIndex()));
        int time = 0;
        int dataValue;
        while (!day.equals(lastDay)) {
            if (day.equals(dataDay)) {
                dataValue = Integer.parseInt((String) table.getValueAt(row, dependentVarCombo.getSelectedIndex()));
                dataSeries.add(day, dataValue);
                row++;
            }
            modelSeries.add(day, gompertzValue(time, rate, capacity, displacement));
            day = (Day) day.next();
            dataDay = getDay((String) table.getValueAt(row, undependentVarCombo.getSelectedIndex()));
            time++;
        }
        for (int i = 0; i < modelRow; i++) {
            day = (Day) day.next();
            modelSeries.add(day, gompertzValue(time, rate, capacity, displacement));
            time++;
        }
        TimeSeriesCollection collection = new TimeSeriesCollection();
        collection.addSeries(dataSeries);
        collection.addSeries(modelSeries);
        XYDataset data = collection;
        JFreeChart chart = ChartFactory.createTimeSeriesChart("Gompertz model", "time", (String) dependentVarCombo.getSelectedItem(), data, true, true, true);
        BufferedImage chartImage = chart.createBufferedImage(1024, 1024);
        lastPlot = chartImage;
        JLabel label = new JLabel();
        label.setIcon(new ImageIcon(chartImage));
        JScrollPane scrollPane = new JScrollPane(label);
        JFrame frame = new JFrame("Plot");

        JMenuBar menuBar = new JMenuBar();
        JMenu saveMenu = new JMenu("Save");
        JMenuItem saveChartMenuItem = new JMenuItem("Save chart to a png file...");
        saveChartMenuItem.addActionListener(this);
        saveChartMenuItem.setAccelerator(KeyStroke.getKeyStroke('S', java.awt.event.InputEvent.CTRL_DOWN_MASK));
        saveChartMenuItem.setActionCommand("saveChart");
        saveMenu.add(saveChartMenuItem);
        menuBar.add(saveMenu);
        frame.setJMenuBar(menuBar);
        frame.setContentPane(scrollPane);
        frame.setSize(1024, 500);
        frame.setVisible(true);
    }

    double countVerhulstOperations() {
        double operations = ((Integer) maxRowSpinner.getValue() - (Integer) minRowSpinner.getValue())/((Integer) stepSpinner.getValue());
        operations *= ((Double) verhulstInitialPopMaxSpinner.getValue() - (Double) verhulstInitialPopMinSpinner.getValue())/((Double) verhulstInitialPopStepSpinner.getValue());
        operations *= ((Integer) verhulstCapacityMaxSpinner.getValue() - (Integer) verhulstCapacityMinSpinner.getValue())/((Integer) verhulstCapacityStepSpinner.getValue());
        operations *= ((Double) verhulstRateMaxSpinner.getValue() - (Double) verhulstRateMinSpinner.getValue())/((Double) verhulstRateStepSpinner.getValue());
        return operations;
    }
    double countGompertzOperations() {
        double operations = ((Integer) maxRowSpinner.getValue() - (Integer) minRowSpinner.getValue())/((Integer) stepSpinner.getValue());
        operations *= ((Double) gompertzDisplacementMaxSpinner.getValue() - (Double) gompertzDisplacementMinSpinner.getValue())/((Double) gompertzDisplacementStepSpinner.getValue());
        operations *= ((Integer) gompertzCapacityMaxSpinner.getValue() - (Integer) gompertzCapacityMinSpinner.getValue())/((Integer) gompertzCapacityStepSpinner.getValue());
        operations *= ((Double) gompertzRateMaxSpinner.getValue() - (Double) gompertzRateMinSpinner.getValue())/((Double) gompertzRateStepSpinner.getValue());
        return Math.abs(operations);
    }

    public void actionPerformed(ActionEvent e) {
        if (e.getActionCommand().equals("openCSV")) {
            openCSV();
        }
        if (e.getActionCommand().equals("verhulst")) {
            setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));
            if (getData()) {
                verhulst();
            }
        }
        if (e.getActionCommand().equals("gompertz")) {
            setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));
            if (getData()) {
                gompertz();
            }
        }
        if (e.getActionCommand().equals("plot")) {
            double rate = Double.parseDouble(manualRate.getText());
            int capacity = Integer.parseInt(manualCapacity.getText());
            double initialPop = Double.parseDouble(manualInitialPop.getText());
            plotVerhulst(rate, capacity, initialPop);
        }
        if (e.getActionCommand().equals("plotGompertz")) {
            double rate = Double.parseDouble(gManualRate.getText());
            int capacity = Integer.parseInt(gManualCapacity.getText());
            double displacement = Double.parseDouble(gManualDisplacement.getText());
            plotGompertz(rate, capacity, displacement);
        }
        if (e.getActionCommand().equals("countOperations")) {
            double operations = countVerhulstOperations();
            JOptionPane.showMessageDialog(this, "Number of elementar operations:" + String.valueOf(Math.rint(operations)));
        }
        if (e.getActionCommand().equals("gCountOperations")) {
            double operations = countGompertzOperations();
            JOptionPane.showMessageDialog(this, "Number of elementar operations:" + String.valueOf(Math.rint(operations)));
        }
        if (e.getActionCommand().equals("saveChart")) {
            int returnVal = fch.showSaveDialog(this);
            if (returnVal == JFileChooser.APPROVE_OPTION) {
                try {
                    File file = fch.getSelectedFile();
                    String path = file.getAbsolutePath();
                    if (!path.endsWith(".png")) {
                        file = new File(path + ".png");
                    }
                    ImageIO.write(lastPlot, "png", file);
                } catch (Exception excp) {
                    showError(excp);
                }
            }
        }

    }

    static double getScreenWidth() {
        Toolkit toolkit = Toolkit.getDefaultToolkit();
        return toolkit.getScreenSize().getWidth();
    }
    static double getScreenHeight() {
        Toolkit toolkit = Toolkit.getDefaultToolkit();
        return toolkit.getScreenSize().getHeight();
    }

    public void propertyChange(PropertyChangeEvent evt) {
        if ("progress" == evt.getPropertyName()) {
            int progress = (Integer) evt.getNewValue();
            progressMonitor.setProgress(progress);
            if (progressMonitor.isCanceled()) {
                //code to cancel task!
            }
        }
    }

}

class VerhulstTask extends SwingWorker<Void, Void> {
    Main main;
    double cost;
    int minCapacity;
    int maxCapacity;
    int stepCapacity;
    double minInPop;
    double maxInPop;
    double stepInPop;
    double minRate;
    double maxRate;
    double stepRate;
    int idealCapacity;
    double idealInPop;
    double idealRate;
    double minCost;

    VerhulstTask(Main main) {
        this.main = main;
        minCapacity = (Integer) main.verhulstCapacityMinSpinner.getValue();
        maxCapacity = (Integer) main.verhulstCapacityMaxSpinner.getValue();
        stepCapacity = (Integer) main.verhulstCapacityStepSpinner.getValue();
        minInPop = (Double) main.verhulstInitialPopMinSpinner.getValue();
        maxInPop = (Double) main.verhulstInitialPopMaxSpinner.getValue();
        stepInPop = (Double) main.verhulstInitialPopStepSpinner.getValue();
        minRate = (Double) main.verhulstRateMinSpinner.getValue();
        maxRate = (Double) main.verhulstRateMaxSpinner.getValue();
        stepRate = (Double) main.verhulstRateStepSpinner.getValue();
        idealCapacity = 0;
        idealInPop = 0;
        idealRate = 0;
        minCost = 0;
    }

    @Override
    public Void doInBackground() {
        setProgress(0);
        int operationsDone = 0;
        double operationsMax = ((maxInPop - minInPop)/stepInPop) * ((maxCapacity - minCapacity)/stepCapacity) * ((maxRate - minRate)/stepRate);
        for (double initialPop = minInPop; initialPop <= maxInPop; initialPop += stepInPop) {
            for (int capacity = minCapacity; capacity <= maxCapacity; capacity += stepCapacity) {
                for (double rate = minRate; rate <= maxRate; rate += stepRate) {
                    cost = main.verhulstCost(rate, capacity, initialPop);
                    if (minCost == 0 || minCost > cost) {
                            minCost = cost;
                            idealCapacity = capacity;
                            idealInPop = initialPop;
                            idealRate = rate;
                    }
                    operationsDone++;
                }
                setProgress((int) Math.rint(100 * operationsDone/operationsMax));
            }
        }
        return null;
    }
    @Override
    public void done() {
        JOptionPane.showMessageDialog(main, "Optimal parameters:" +
                "Capacity = " + String.valueOf(idealCapacity) +
                ", Initial Population = " + String.valueOf(idealInPop) +
                ", Rate  = " + String.valueOf(idealRate) +
                "; Cost function = " + String.valueOf(minCost));
        main.plotVerhulst(idealRate, idealCapacity, idealInPop);
        main.setCursor(Cursor.getDefaultCursor());
    }
}

class GompertzTask extends SwingWorker<Void, Void> {
    Main main;
    double cost;
    int minCapacity;
    int maxCapacity;
    int stepCapacity;
    double minDisplacement;
    double maxDisplacement;
    double stepDisplacement;
    double minRate;
    double maxRate;
    double stepRate;
    int idealCapacity;
    double idealDisplacement;
    double idealRate;
    double minCost;

    GompertzTask(Main main) {
        this.main = main;
        minCapacity = (Integer) main.gompertzCapacityMinSpinner.getValue();
        maxCapacity = (Integer) main.gompertzCapacityMaxSpinner.getValue();
        stepCapacity = (Integer) main.gompertzCapacityStepSpinner.getValue();
        minDisplacement = (Double) main.gompertzDisplacementMinSpinner.getValue();
        maxDisplacement = (Double) main.gompertzDisplacementMaxSpinner.getValue();
        stepDisplacement = (Double) main.gompertzDisplacementStepSpinner.getValue();
        minRate = (Double) main.gompertzRateMinSpinner.getValue();
        maxRate = (Double) main.gompertzRateMaxSpinner.getValue();
        stepRate = (Double) main.gompertzRateStepSpinner.getValue();
        idealCapacity = 0;
        idealDisplacement = 0;
        idealRate = 0;
        minCost = 0;
    }

    @Override
    public Void doInBackground() {
        setProgress(0);
        int operationsDone = 0;
        double operationsMax = Math.abs(((maxDisplacement - minDisplacement)/stepDisplacement) * ((maxCapacity - minCapacity)/stepCapacity) * ((maxRate - minRate)/stepRate));
        for (double displacement = minDisplacement; displacement <= maxDisplacement; displacement += stepDisplacement) {
            for (int capacity = minCapacity; capacity <= maxCapacity; capacity += stepCapacity) {
                for (double rate = minRate; rate <= maxRate; rate += stepRate) {
                    cost = main.gompertzCost(rate, capacity, displacement);
                    if (minCost == 0 || minCost > cost) {
                            minCost = cost;
                            idealCapacity = capacity;
                            idealDisplacement = displacement;
                            idealRate = rate;
                    }
                    operationsDone++;
                }
                setProgress((int) Math.rint(100 * operationsDone/operationsMax));
            }
        }
        setProgress(100);
        return null;
    }
    @Override
    public void done() {
        JOptionPane.showMessageDialog(main, "Optimal parameters:" +
                "Capacity = " + String.valueOf(idealCapacity) +
                ", Displacement = " + String.valueOf(idealDisplacement) +
                ", Rate = " + String.valueOf(idealRate) +
                "; Cost function = " + String.valueOf(minCost));
        main.plotGompertz(idealRate, idealCapacity, idealDisplacement);
        main.setCursor(Cursor.getDefaultCursor());

    }
}
