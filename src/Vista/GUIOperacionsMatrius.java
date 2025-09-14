package Vista;

import Controladora.Notificar;

import javax.swing.*;
import java.awt.*;
import java.text.DecimalFormat;

import org.jfree.chart.ChartFactory;
import org.jfree.chart.ChartPanel;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.annotations.XYTextAnnotation;
import org.jfree.chart.axis.LogAxis;
import org.jfree.chart.plot.PlotOrientation;
import org.jfree.chart.plot.XYPlot;
import org.jfree.chart.renderer.xy.XYLineAndShapeRenderer;
import org.jfree.data.xy.XYSeries;
import org.jfree.data.xy.XYSeriesCollection;

public class GUIOperacionsMatrius extends JFrame {
    private JTextField dimensionField;
    private JTextField steppingField;
    private JCheckBox sumCheckBox;
    private JCheckBox multiplicationCheckBox;
    private JCheckBox fixedSeedCheckBox;
    private JButton runButton;
    private JButton stopButton;

    // Existing benchmark datasets and chart
    private XYSeriesCollection dataset;
    private XYSeries additionSeries;
    private XYSeries multiplicationSeries;
    private JFreeChart chart;
    private XYPlot plot;

    // New dataset and chart for constants (both addition and multiplication)
    private XYSeriesCollection constantDataset;
    private XYSeries additionConstantSeries;
    private XYSeries multiplicationConstantSeries;
    private JFreeChart constantChart;

    // Labels for displaying constants in the benchmark chart
    private XYTextAnnotation additionConstantLabel;
    private XYTextAnnotation multiplicationConstantLabel;
    private double lastAdditionX = 0;
    private double lastMultiplicationX = 0;

    private final Notificar controller;
    private final DecimalFormat constantFormat = new DecimalFormat("0.000E0");

    public GUIOperacionsMatrius(Notificar controller) {
        this.controller = controller;
        initializeUI();
    }

    private void initializeUI() {
        setTitle("Matrix Operations Benchmark");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLayout(new BorderLayout());

        JPanel mainPanel = new JPanel(new BorderLayout());
        mainPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        JPanel controlPanel = createControlPanel();

        // ========== Benchmark chart setup (unchanged) ==========
        dataset = new XYSeriesCollection();
        additionSeries = new XYSeries("Addition");
        multiplicationSeries = new XYSeries("Multiplication");
        dataset.addSeries(additionSeries);
        dataset.addSeries(multiplicationSeries);

        chart = ChartFactory.createXYLineChart(
                "Matrix Operations Benchmark",
                "Matrix Dimension",
                "Time (ms)",
                dataset,
                PlotOrientation.VERTICAL,
                true,
                true,
                false
        );

        plot = chart.getXYPlot();
        XYLineAndShapeRenderer renderer = new XYLineAndShapeRenderer();
        renderer.setSeriesPaint(0, Color.BLUE);
        renderer.setSeriesPaint(1, Color.RED);
        renderer.setSeriesStroke(0, new BasicStroke(2.0f));
        renderer.setSeriesStroke(1, new BasicStroke(2.0f));
        plot.setRenderer(renderer);

        additionConstantLabel = new XYTextAnnotation("", 0, 0);
        additionConstantLabel.setPaint(Color.BLUE);
        additionConstantLabel.setFont(new Font("SansSerif", Font.BOLD, 12));
        multiplicationConstantLabel = new XYTextAnnotation("", 0, 0);
        multiplicationConstantLabel.setPaint(Color.RED);
        multiplicationConstantLabel.setFont(new Font("SansSerif", Font.BOLD, 12));
        plot.addAnnotation(additionConstantLabel);
        plot.addAnnotation(multiplicationConstantLabel);

        ChartPanel benchmarkChartPanel = new ChartPanel(chart);
        benchmarkChartPanel.setPreferredSize(new Dimension(800, 500));

        // ========== Constants chart setup (modified) ==========
        constantDataset = new XYSeriesCollection();
        additionConstantSeries = new XYSeries("Addition Constant");
        multiplicationConstantSeries = new XYSeries("Multiplication Constant");
        constantDataset.addSeries(additionConstantSeries);
        constantDataset.addSeries(multiplicationConstantSeries);

        constantChart = ChartFactory.createXYLineChart(
                "Constant Graph",
                "Matrix Dimension",
                "Constant",
                constantDataset,
                PlotOrientation.VERTICAL,
                true,
                true,
                false
        );

        XYPlot constantPlot = constantChart.getXYPlot();
        XYLineAndShapeRenderer constantRenderer = new XYLineAndShapeRenderer();
        constantRenderer.setSeriesPaint(0, Color.BLUE);
        constantRenderer.setSeriesStroke(0, new BasicStroke(2.0f));
        constantRenderer.setSeriesPaint(1, Color.RED);
        constantRenderer.setSeriesStroke(1, new BasicStroke(2.0f));
        constantPlot.setRenderer(constantRenderer);

        // ----- NEW CODE: Use a log axis on the Y-axis -----
        LogAxis logYAxis = new LogAxis("Constant (Log scale)");
        logYAxis.setBase(10);  // Base-10 logarithm
        // Optionally use standard log tick units (depending on your locale):
        // logYAxis.setStandardTickUnits(NumberAxis.createLogTickUnits(Locale.getDefault()));
        constantPlot.setRangeAxis(logYAxis);
        // ---------------------------------------------------

        ChartPanel constantChartPanel = new ChartPanel(constantChart);
        constantChartPanel.setPreferredSize(new Dimension(800, 500));

        // Create a tabbed pane to hold both charts
        JTabbedPane tabbedPane = new JTabbedPane();
        tabbedPane.addTab("Benchmark", benchmarkChartPanel);
        tabbedPane.addTab("Constants", constantChartPanel);

        mainPanel.add(controlPanel, BorderLayout.NORTH);
        mainPanel.add(tabbedPane, BorderLayout.CENTER);

        add(mainPanel);
        pack();
        setLocationRelativeTo(null);
        setVisible(true);

        // Default values for controls
        dimensionField.setText("100");
        steppingField.setText("50");
        sumCheckBox.setSelected(true);
        multiplicationCheckBox.setSelected(true);
    }

    private JPanel createControlPanel() {
        JPanel controlPanel = new JPanel(new GridLayout(2, 4, 10, 10));
        controlPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        // First row
        controlPanel.add(new JLabel("Initial Dimension:"));
        dimensionField = new JTextField(5);
        controlPanel.add(dimensionField);

        controlPanel.add(new JLabel("Step Size:"));
        steppingField = new JTextField(5);
        controlPanel.add(steppingField);

        // Second row
        sumCheckBox = new JCheckBox("Addition");
        controlPanel.add(sumCheckBox);

        multiplicationCheckBox = new JCheckBox("Multiplication");
        controlPanel.add(multiplicationCheckBox);

        fixedSeedCheckBox = new JCheckBox("Fixed Seed");
        controlPanel.add(fixedSeedCheckBox);

        JPanel buttonPanel = new JPanel(new GridLayout(1, 2, 5, 5));
        runButton = new JButton("Run");
        runButton.setBackground(new Color(200, 255, 200));
        stopButton = new JButton("Stop");
        stopButton.setBackground(new Color(255, 200, 200));
        stopButton.setEnabled(false);
        buttonPanel.add(runButton);
        buttonPanel.add(stopButton);
        controlPanel.add(buttonPanel);

        // Action listeners
        runButton.addActionListener(e -> startCalculation());
        stopButton.addActionListener(e -> controller.onCalculationStopped());

        return controlPanel;
    }

    private void startCalculation() {
        try {
            int initialDimension = Integer.parseInt(dimensionField.getText().trim());
            int stepping = Integer.parseInt(steppingField.getText().trim());

            if (initialDimension <= 0 || stepping <= 0) {
                showError("Please enter positive integers for dimension and step size.");
                return;
            }

            // Clear data and annotations for benchmark chart
            additionSeries.clear();
            multiplicationSeries.clear();
            plot.removeAnnotation(additionConstantLabel);
            plot.removeAnnotation(multiplicationConstantLabel);
            additionConstantLabel = new XYTextAnnotation("", 0, 0);
            additionConstantLabel.setPaint(Color.BLUE);
            additionConstantLabel.setFont(new Font("SansSerif", Font.BOLD, 12));
            multiplicationConstantLabel = new XYTextAnnotation("", 0, 0);
            multiplicationConstantLabel.setPaint(Color.RED);
            multiplicationConstantLabel.setFont(new Font("SansSerif", Font.BOLD, 12));
            plot.addAnnotation(additionConstantLabel);
            plot.addAnnotation(multiplicationConstantLabel);
            lastAdditionX = 0;
            lastMultiplicationX = 0;

            // Clear data for constants chart
            additionConstantSeries.clear();
            multiplicationConstantSeries.clear();

            boolean sumEnabled = sumCheckBox.isSelected();
            boolean multiplicationEnabled = multiplicationCheckBox.isSelected();

            if (!sumEnabled && !multiplicationEnabled) {
                showError("Please select at least one operation (addition or multiplication).");
                return;
            }

            controller.onCalculationStarted(
                    initialDimension,
                    stepping,
                    sumEnabled,
                    multiplicationEnabled,
                    fixedSeedCheckBox.isSelected()
            );

        } catch (NumberFormatException e) {
            showError("Please enter valid integers for dimension and step size.");
        }
    }

    public void setCalculationRunning(boolean running) {
        SwingUtilities.invokeLater(() -> {
            runButton.setEnabled(!running);
            stopButton.setEnabled(running);
            dimensionField.setEnabled(!running);
            steppingField.setEnabled(!running);
            sumCheckBox.setEnabled(!running);
            multiplicationCheckBox.setEnabled(!running);
            fixedSeedCheckBox.setEnabled(!running);
        });
    }

    public void addDataPoint(String operationType, int dimension, double executionTime, double constant) {
        SwingUtilities.invokeLater(() -> {
            if ("Addition".equals(operationType)) {
                additionSeries.add(dimension, executionTime);
                lastAdditionX = dimension;

                // Update constant label for Addition in benchmark chart
                String constantText = "Addition constant: " + constantFormat.format(constant);
                plot.removeAnnotation(additionConstantLabel);
                additionConstantLabel = new XYTextAnnotation(constantText, dimension, executionTime * 0.9);
                additionConstantLabel.setPaint(Color.BLUE);
                additionConstantLabel.setFont(new Font("SansSerif", Font.BOLD, 12));
                plot.addAnnotation(additionConstantLabel);

                // Also add data point to the constants chart for addition constant
                additionConstantSeries.add(dimension, constant);

            } else if ("Multiplication".equals(operationType)) {
                multiplicationSeries.add(dimension, executionTime);
                lastMultiplicationX = dimension;

                // Update constant label for Multiplication in benchmark chart (placed at top right)
                String constantText = "Multiplication constant: " + constantFormat.format(constant);
                plot.removeAnnotation(multiplicationConstantLabel);
                double xPos = plot.getDomainAxis().getUpperBound() - 50;  // adjust offset as needed
                double yPos = plot.getRangeAxis().getUpperBound() - 10;   // adjust offset as needed
                multiplicationConstantLabel = new XYTextAnnotation(constantText, xPos, yPos);
                multiplicationConstantLabel.setPaint(Color.RED);
                multiplicationConstantLabel.setFont(new Font("SansSerif", Font.BOLD, 12));
                plot.addAnnotation(multiplicationConstantLabel);

                // Also add data point to the constants chart for multiplication constant
                multiplicationConstantSeries.add(dimension, constant);
            }
        });
    }

    public void showError(String message) {
        SwingUtilities.invokeLater(() -> {
            JOptionPane.showMessageDialog(this, message, "Error", JOptionPane.ERROR_MESSAGE);
        });
    }
}
