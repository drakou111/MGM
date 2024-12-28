package mgm;

import com.formdev.flatlaf.FlatDarkLaf;
import com.formdev.flatlaf.FlatIntelliJLaf;
import com.formdev.flatlaf.FlatLightLaf;

import javax.swing.*;
import javax.swing.border.LineBorder;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.plaf.metal.MetalLookAndFeel;
import java.awt.*;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;
import java.util.ArrayList;

public class UI {
    private JFrame frame;
    private JSlider blockHeightSlider;
    private JSlider iconSizeSlider;
    private JSlider textureNoiseSlider;
    private JSlider gradientLengthSlider;
    private JLabel hexLabel, redLabel;
    private JList<String> rgbList;
    private DefaultListModel<String> listModel;
    private JTextField hexInputField;
    private JTextField redField, greenField, blueField;
    private JTextField blockHeightValueField;
    private JTextField iconSizeValueField;
    private JTextField textureNoiseValueField;
    private JTextField gradientLengthValueField;
    private JMenuBar menuBar;
    private JMenu themeMenu;
    private JPanel gradientPanel, bottomPanel, topRightPanel, topLeftPanel;
    private JCheckBox removeRepeatsCheckbox;
    private JPanel colorSquarePanel;


    private BlockGradient blockGradientGenerator = new BlockGradient();

    public static void start() {
        SwingUtilities.invokeLater(() -> new UI().createAndShowGUI());
    }

    // Helper method to create the gradient visualization panel
    private JPanel createGradientPanel() {
        gradientPanel = new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                Graphics2D g2d = (Graphics2D) g;

                // Get the colors from the list model
                java.util.List<Color> colors = new java.util.ArrayList<>();
                for (int i = 0; i < listModel.size(); i++) {
                    String rgbValue = listModel.getElementAt(i);
                    Color color = parseRGB(rgbValue);
                    if (color != null) {
                        colors.add(color);
                    }
                }

                // If there are no colors, return
                if (colors.isEmpty()) return;

                // If only one color is in the list, create a solid color gradient
                if (colors.size() == 1) {
                    g2d.setColor(colors.get(0));
                    g2d.fillRect(0, 0, getWidth(), getHeight());
                } else {
                    // Create an array of float positions for the colors (evenly spaced)
                    float[] positions = new float[colors.size()];
                    for (int i = 0; i < positions.length; i++) {
                        positions[i] = (float) i / (positions.length - 1); // Evenly spaced between 0 and 1
                    }

                    // Create a LinearGradientPaint with the colors and positions
                    LinearGradientPaint gradient = new LinearGradientPaint(
                            0, 0, getWidth(), 0, positions, colors.toArray(new Color[0])
                    );

                    // Set the gradient and fill the panel with it
                    g2d.setPaint(gradient);
                    g2d.fillRect(0, 0, getWidth(), getHeight());
                }
            }
        };
        gradientPanel.setPreferredSize(new Dimension(0, 200)); // Adjust the size as needed
        return gradientPanel;
    }

    public void createAndShowGUI() {
        frame = new JFrame("Image Configuration");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setSize(1000, 800); // Initial size of the window
        frame.setLayout(new BorderLayout());

        // Initialize sliders and value fields
        textureNoiseSlider = createSlider(0, 256);
        gradientLengthSlider = createSlider(0, 1024);
        iconSizeSlider = createSlider(1, 128);
        blockHeightSlider = createSlider(1, 64);
        textureNoiseValueField = createValueField(textureNoiseSlider, 0, 256);
        gradientLengthValueField = createValueField(gradientLengthSlider,  0, 1024);
        iconSizeValueField = createValueField(iconSizeSlider, 1, 128);
        blockHeightValueField = createValueField(blockHeightSlider, 1, 64);

        textureNoiseSlider.setValue(128);
        gradientLengthSlider.setValue(64);
        iconSizeSlider.setValue(32);
        blockHeightSlider.setValue(3);

        textureNoiseValueField.setText("128");
        gradientLengthValueField.setText("64");
        iconSizeValueField.setText("32");
        blockHeightValueField.setText("3");

        // Initialize listModel and rgbList
        listModel = new DefaultListModel<>();
        rgbList = new JList<>(listModel); // Associate listModel with rgbList

        // Create menu bar
        createMenuBar();
        frame.setJMenuBar(menuBar);

        // Create the Top Left panel (parameters area)
        topLeftPanel = createTopLeftPanel();
        topLeftPanel.setLayout(new BoxLayout(topLeftPanel, BoxLayout.Y_AXIS));
        topLeftPanel.setBorder(new LineBorder(UIManager.getColor("Panel.borderColor"), 1));  // Black outline with thickness of 1

        // Create the Top Right panel (gradient area)
        topRightPanel = createTopRightPanel();
        topRightPanel.setLayout(new BoxLayout(topRightPanel, BoxLayout.Y_AXIS));
        topRightPanel.setBorder(new LineBorder(UIManager.getColor("Panel.borderColor"), 1));  // Black outline with thickness of 1

        bottomPanel = createBottomPanel();
        bottomPanel.setLayout(new BorderLayout());
        bottomPanel.setBorder(new LineBorder(UIManager.getColor("Panel.borderColor"), 1));  // Black outline with thickness of 1

        // Create JSplitPane for dividing top left and top right sections
        JSplitPane splitPaneTop = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, topLeftPanel, topRightPanel);
        splitPaneTop.setDividerLocation(frame.getWidth() / 2); // Initial split location
        splitPaneTop.setDividerSize(20);


        // Create JSplitPane for dividing top and bottom sections
        JSplitPane splitPaneMain = new JSplitPane(JSplitPane.VERTICAL_SPLIT, splitPaneTop, bottomPanel);
        splitPaneMain.setDividerLocation(frame.getHeight() / 2); // Initial split location
        splitPaneMain.setDividerSize(20);

        // Add the splitPaneMain to the frame
        frame.add(splitPaneMain, BorderLayout.CENTER);

        // Use a component listener to update the sizes on window resize
        frame.addComponentListener(new ComponentAdapter() {
            @Override
            public void componentResized(ComponentEvent e) {
                // Update the split pane divider locations on window resize
                Dimension size = frame.getSize();
                splitPaneTop.setDividerLocation(size.width / 2);
                splitPaneMain.setDividerLocation(size.height / 2);
            }
        });

        setupEventListeners();

        frame.setVisible(true);
    }


    private JPanel createTopLeftPanel() {
        JPanel panel = new JPanel();
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));

        panel.add(createSliderPanel(textureNoiseSlider, textureNoiseValueField, "Texture Noise Tolerance: "));
        panel.add(createSliderPanel(gradientLengthSlider, gradientLengthValueField, "Gradient Length: "));
        panel.add(createSliderPanel(iconSizeSlider, iconSizeValueField, "Block Display Size: "));
        panel.add(createSliderPanel(blockHeightSlider, blockHeightValueField, "Gradient Block Height: "));

        removeRepeatsCheckbox = new JCheckBox("Remove Repeats");
        removeRepeatsCheckbox.addActionListener(e -> updateBlockGradient());
        panel.add(removeRepeatsCheckbox);

        return panel;
    }

    private JPanel createTopRightPanel() {
        JPanel panel = new JPanel();
        panel.setLayout(new BorderLayout(10, 10));

        // RGB list and Hex input panel
        JPanel inputPanel = new JPanel();
        inputPanel.setLayout(new BoxLayout(inputPanel, BoxLayout.Y_AXIS));

        inputPanel.add(new JLabel("Gradient Colors:"));
        inputPanel.add(new JScrollPane(rgbList)); // Scrollable list of colors
        inputPanel.add(createColorInputPanel());

        // Gradient Display Panel (visual representation of gradient)
        gradientPanel = createGradientPanel(); // Corrected line to use the proper gradient panel creation

        // Add components to top right panel
        panel.add(inputPanel, BorderLayout.NORTH);
        panel.add(gradientPanel, BorderLayout.CENTER);

        return panel;
    }

    // Update the bottom panel to display images in a grid-like layout.
    private JPanel createBottomPanel() {
        bottomPanel = new JPanel(); // Initialize the bottom panel
        bottomPanel.setLayout(new FlowLayout(FlowLayout.LEFT, 0, 0)); // Horizontal layout with padding

        return bottomPanel;
    }

    // Add a method to display the block gradient images.
    private void displayBlockGradient(ArrayList<ImageInfo> blockGradient) {
        // Clear the current bottom panel
        bottomPanel.removeAll();

        // Set the layout manager for bottomPanel to FlowLayout with LEFT alignment
        bottomPanel.setLayout(new FlowLayout(FlowLayout.LEFT, 0, 2));

        int iconSize = iconSizeSlider.getValue();
        int repeatAmount = blockHeightSlider.getValue();

        // Iterate over the blockGradient and add each image to the bottomPanel
        for (ImageInfo imageInfo : blockGradient) {
            if (imageInfo == null)
                continue;

            JPanel verticalStack = new JPanel();
            verticalStack.setLayout(new GridLayout(repeatAmount, 1, 0, 0)); // Vertically stack repeated icons

            ImageIcon icon = new ImageIcon(
                    imageInfo.image.getScaledInstance(iconSize, iconSize, Image.SCALE_FAST)
            );

            for (int i = 0; i < repeatAmount; i++) {
                JLabel imageLabel = new JLabel(icon); // Create a new label for each repetition
                verticalStack.add(imageLabel); // Add to the vertical stack
            }

            bottomPanel.add(verticalStack); // Add the stack to the bottom panel
        }

        // Refresh the bottom panel to show the new images
        bottomPanel.revalidate();
        bottomPanel.repaint();
    }

    private void setupEventListeners() {
        gradientLengthSlider.addChangeListener(e -> updateBlockGradient());
        textureNoiseSlider.addChangeListener(e -> updateBlockGradient());
        iconSizeSlider.addChangeListener(e -> updateBlockGradient());
        blockHeightSlider.addChangeListener(e -> updateBlockGradient());
        removeRepeatsCheckbox.addChangeListener(e -> updateBlockGradient());
    }

    public void updateBlockGradient() {
        int textureNoise = textureNoiseSlider.getValue();
        int gradientLength = gradientLengthSlider.getValue();
        boolean removeRepeats = removeRepeatsCheckbox.isSelected();

        // Get the list of colors from the listModel
        java.util.List<Color> colors = new java.util.ArrayList<>();
        for (int i = 0; i < listModel.size(); i++) {
            String rgbValue = listModel.getElementAt(i);
            Color color = parseRGB(rgbValue);
            if (color != null) {
                colors.add(color);
            }
        }

        if (colors.isEmpty()) {
            bottomPanel.removeAll();
            bottomPanel.revalidate();
            bottomPanel.repaint();
            return;
        }

        ArrayList<ImageInfo> blockGradient = blockGradientGenerator.getGradient(textureNoise, gradientLength, removeRepeats, colors);

        // Display the block gradient in the bottom panel
        displayBlockGradient(blockGradient);
    }

    private Color parseRGB(String rgbValue) {
        try {
            String[] parts = rgbValue.replace("RGB(", "").replace(")", "").split(", ");
            int red = Integer.parseInt(parts[0]);
            int green = Integer.parseInt(parts[1]);
            int blue = Integer.parseInt(parts[2]);
            return new Color(red, green, blue);
        } catch (Exception e) {
            return null; // Invalid RGB format
        }
    }

    // Helper method to create sliders
    private JSlider createSlider(int minValue, int maxValue) {
        JSlider slider = new JSlider(minValue, maxValue);
        slider.setMajorTickSpacing((maxValue-minValue)/8);
        slider.setMinorTickSpacing((maxValue-minValue)/64);
        slider.setPaintTicks(true);
        slider.setPaintLabels(true);
        slider.setMaximumSize(new Dimension(Integer.MAX_VALUE, 100));

        // Update the text field value when slider value changes (real-time)
        slider.addChangeListener(e -> {
            if (slider == textureNoiseSlider) {
                textureNoiseValueField.setText(String.valueOf(slider.getValue()));
            } else if (slider == gradientLengthSlider) {
                gradientLengthValueField.setText(String.valueOf(slider.getValue()));
            } else if (slider == iconSizeSlider) {
                iconSizeValueField.setText(String.valueOf(slider.getValue()));
            } else if (slider == blockHeightSlider) {
                blockHeightValueField.setText(String.valueOf(slider.getValue()));
            }
        });

        return slider;
    }

    // Helper method to create the editable value fields for sliders
    private JTextField createValueField(JSlider slider, int minValue, int maxValue) {
        JTextField valueField = new JTextField(String.valueOf(slider.getValue()),3);
        valueField.setMaximumSize(new Dimension(40, 20));

        //TODO: fix

        // Update slider value when field loses focus (not on enter)
        valueField.addActionListener(e -> {
            try {
                int value = Integer.parseInt(valueField.getText().trim());
                if (value >= minValue && value <= maxValue) {
                    slider.setValue(value);
                } else {
                    valueField.setText(String.valueOf(slider.getValue()));
                }
            } catch (NumberFormatException ex) {
                valueField.setText(String.valueOf(slider.getValue()));
            }
        });

        return valueField;
    }

    // Helper method to create a panel for sliders with value fields
    private JPanel createSliderPanel(JSlider slider, JTextField valueField, String label) {
        JPanel panel = new JPanel();
        // Set the layout to BoxLayout.X_AXIS for horizontal alignment of label and value field
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));

        // Create a sub-panel for label and value field with horizontal layout
        JPanel inlinePanel = new JPanel();
        inlinePanel.setLayout(new BoxLayout(inlinePanel, BoxLayout.X_AXIS));
        inlinePanel.add(new JLabel(label));
        inlinePanel.add(valueField);


        // Add the inline panel (label + value field) and slider to the main panel
        panel.add(inlinePanel);
        panel.add(slider);
        panel.add(Box.createVerticalStrut(20)); // Optional strut to add some space below slider

        return panel;
    }

    private JPanel createColorInputPanel() {
        JPanel panel = new JPanel();
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS)); // Stack components vertically

        // First row: RGB input fields
        JPanel firstRowPanel = new JPanel();
        firstRowPanel.setLayout(new BoxLayout(firstRowPanel , BoxLayout.X_AXIS));

        // RGB input labels and fields
        redLabel = new JLabel("RGB:");
        firstRowPanel.add(redLabel);
        firstRowPanel.add(Box.createHorizontalStrut(2));
        redField = new JTextField(3);
        redField.setText("0"); // Default value
        redField.setMaximumSize(new Dimension(40, 30));
        redField.getDocument().addDocumentListener(new DocumentListener() {
            @Override
            public void insertUpdate(DocumentEvent e) {
                if (redField.hasFocus()) {
                    SwingUtilities.invokeLater(() -> updateColorFromRGB());
                }
            }

            @Override
            public void removeUpdate(DocumentEvent e) {
                if (redField.hasFocus()) {
                    SwingUtilities.invokeLater(() -> updateColorFromRGB());
                }
            }

            @Override
            public void changedUpdate(DocumentEvent e) {
                if (redField.hasFocus()) {
                    SwingUtilities.invokeLater(() -> updateColorFromRGB());
                }
            }
        });
        firstRowPanel.add(redField);
        firstRowPanel.add(Box.createHorizontalStrut(2));
        greenField = new JTextField(3);
        greenField.setText("0"); // Default value
        greenField.setMaximumSize(new Dimension(40, 30));
        greenField.getDocument().addDocumentListener(new DocumentListener() {
            @Override
            public void insertUpdate(DocumentEvent e) {
                if (greenField.hasFocus()) {
                    SwingUtilities.invokeLater(() -> updateColorFromRGB());
                }
            }

            @Override
            public void removeUpdate(DocumentEvent e) {
                if (greenField.hasFocus()) {
                    SwingUtilities.invokeLater(() -> updateColorFromRGB());
                }
            }

            @Override
            public void changedUpdate(DocumentEvent e) {
                if (greenField.hasFocus()) {
                    SwingUtilities.invokeLater(() -> updateColorFromRGB());
                }
            }
        });
        firstRowPanel.add(greenField);
        firstRowPanel.add(Box.createHorizontalStrut(2));
        blueField = new JTextField(3);
        blueField.setText("0"); // Default value
        blueField.setMaximumSize(new Dimension(40, 30));
        blueField.getDocument().addDocumentListener(new DocumentListener() {
            @Override
            public void insertUpdate(DocumentEvent e) {
                if (blueField.hasFocus()) {
                    SwingUtilities.invokeLater(() -> updateColorFromRGB());
                }
            }

            @Override
            public void removeUpdate(DocumentEvent e) {
                if (blueField.hasFocus()) {
                    SwingUtilities.invokeLater(() -> updateColorFromRGB());
                }
            }

            @Override
            public void changedUpdate(DocumentEvent e) {
                if (blueField.hasFocus()) {
                    SwingUtilities.invokeLater(() -> updateColorFromRGB());
                }
            }
        });
        firstRowPanel.add(blueField);

        firstRowPanel.add(Box.createHorizontalStrut(20)); // Optional strut to add some space below slider

        // Hex input label and field
        hexLabel = new JLabel("Hex:");
        firstRowPanel.add(hexLabel);
        firstRowPanel.add(Box.createHorizontalStrut(2));
        hexInputField = new JTextField(7);
        hexInputField.setText("000000"); // Default value
        hexInputField.setMaximumSize(new Dimension(80, 30));
        hexInputField.getDocument().addDocumentListener(new DocumentListener() {
            @Override
            public void insertUpdate(DocumentEvent e) {
                if (hexInputField.hasFocus()) {
                    SwingUtilities.invokeLater(() -> updateColorFromHex());
                }
            }

            @Override
            public void removeUpdate(DocumentEvent e) {
                if (hexInputField.hasFocus()) {
                    SwingUtilities.invokeLater(() -> updateColorFromHex());
                }
            }

            @Override
            public void changedUpdate(DocumentEvent e) {
                if (hexInputField.hasFocus()) {
                    SwingUtilities.invokeLater(() -> updateColorFromHex());
                }
            }
        });
        firstRowPanel.add(hexInputField);

        firstRowPanel.add(Box.createHorizontalStrut(20)); // Optional strut to add some space below slider

        JButton colorPickerButton = new JButton("Choose Color");
        colorPickerButton.addActionListener(e -> openColorPicker());
        firstRowPanel.add(colorPickerButton);

        // Create the color display square
        colorSquarePanel = new JPanel();
        colorSquarePanel.setMaximumSize(new Dimension(50, 50)); // Square size (40x40)
        colorSquarePanel.setBackground(Color.BLACK); // Default color

        firstRowPanel.add(Box.createHorizontalStrut(20)); // Optional strut to add some space
        firstRowPanel.add(colorSquarePanel); // Add the color square to the first row


        // Add the first row panel to the main panel
        panel.add(firstRowPanel);

        // Fourth row panel with buttons
        JPanel fourthRowPanel = new JPanel();
        JButton addColorButton = new JButton("Add Color");
        addColorButton.addActionListener(e -> {
            addColor();
            updateBlockGradient();
        });

        JButton removeColorButton = new JButton("Remove Color");
        removeColorButton.addActionListener(e -> {
            removeSelectedColor();
            updateBlockGradient();
        });

        JButton clearColorButton = new JButton("Clear Colors");
        clearColorButton.addActionListener(e -> {
            resetColors();
            updateBlockGradient();
        });

        fourthRowPanel.add(addColorButton);
        fourthRowPanel.add(removeColorButton);
        fourthRowPanel.add(clearColorButton);

        panel.add(fourthRowPanel);

        return panel;
    }



    // Method to update the color based on RGB input
    private void updateColorFromRGB() {
        try {
            int r = Integer.parseInt(redField.getText().trim());
            int g = Integer.parseInt(greenField.getText().trim());
            int b = Integer.parseInt(blueField.getText().trim());

            // Ensure valid RGB range
            if (isValidRGBValue(r) && isValidRGBValue(g) && isValidRGBValue(b)) {
                Color color = new Color(r, g, b);
                colorSquarePanel.setBackground(new Color(r, g, b));
                colorSquarePanel.revalidate();
                colorSquarePanel.repaint();
                updateHexField(color);
            }
        } catch (NumberFormatException e) {
            updateColorFromHex();
        }
    }

    // Method to update the color based on Hex input
    private void updateColorFromHex() {
        String hex = hexInputField.getText().trim();
        if (isValidHex(hex)) {
            Color color = Color.decode("#" + hex);
            int r = color.getRed();
            int g = color.getGreen();
            int b = color.getBlue();
            colorSquarePanel.setBackground(new Color(r, g, b));
            colorSquarePanel.revalidate();
            colorSquarePanel.repaint();
            updateRGBFields(r, g, b);
        }
    }

    // Method to update RGB fields from a Color object
    private void updateRGBFields(int r, int g, int b) {
        redField.setText(String.valueOf(r));
        greenField.setText(String.valueOf(g));
        blueField.setText(String.valueOf(b));
    }

    // Method to update the Hex field from a Color object
    private void updateHexField(Color color) {
        String hex = String.format("%02x%02x%02x", color.getRed(), color.getGreen(), color.getBlue());
        hexInputField.setText(hex);
    }

    // Method to open a color picker dialog
    private void openColorPicker() {
        Color initialColor = new Color(Integer.parseInt(redField.getText()), Integer.parseInt(greenField.getText()), Integer.parseInt(blueField.getText()));
        Color color = JColorChooser.showDialog(frame, "Choose Color", initialColor);
        if (color != null) {
            colorSquarePanel.setBackground(new Color(color.getRed(), color.getGreen(), color.getBlue()));
            colorSquarePanel.revalidate();
            colorSquarePanel.repaint();
            updateRGBFields(color.getRed(), color.getGreen(), color.getBlue());
            updateHexField(color);
        }
    }

    private void resetColors() {
        listModel.clear();
        gradientPanel.repaint();
    }

    // Method to add a color (Hex or RGB)
    private void addColor() {

        updateColorFromRGB();

        // Add RGB color
        try {
            int red = Integer.parseInt(redField.getText().trim());
            int green = Integer.parseInt(greenField.getText().trim());
            int blue = Integer.parseInt(blueField.getText().trim());

            if (isValidRGBValue(red) && isValidRGBValue(green) && isValidRGBValue(blue)) {
                String rgbValue = "RGB(" + red + ", " + green + ", " + blue + ")";
                listModel.addElement(rgbValue);
            } else {
                JOptionPane.showMessageDialog(frame, "Invalid RGB Values (must be between 0 and 255)", "Error", JOptionPane.ERROR_MESSAGE);
            }
        } catch (NumberFormatException e) {
            JOptionPane.showMessageDialog(frame, "Invalid RGB Values (must be integers)", "Error", JOptionPane.ERROR_MESSAGE);
        }
        // Update gradient display
        gradientPanel.repaint();
    }

    // Method to remove the selected color from the list
    private void removeSelectedColor() {
        int selectedIndex = rgbList.getSelectedIndex();
        if (selectedIndex != -1) {
            listModel.remove(selectedIndex);
        } else {
            JOptionPane.showMessageDialog(frame, "No color selected", "Error", JOptionPane.ERROR_MESSAGE);
        }
        // Update gradient display
        gradientPanel.repaint();
    }

    // Helper method to check if a Hex value is valid
    private boolean isValidHex(String hex) {
        if (hex == null || hex.isEmpty()) return false;
        try {
            Color.decode("#" + hex); // Will throw an exception if invalid
            return true;
        } catch (NumberFormatException e) {
            return false;
        }
    }

    // Helper method to validate RGB values
    private boolean isValidRGBValue(int value) {
        return value >= 0 && value <= 255;
    }

    // Create the theme menu for selecting different themes
    private void createMenuBar() {
        menuBar = new JMenuBar();
        themeMenu = new JMenu("Themes");
        JMenu blocksMenu = new JMenu("Blocks");

        JMenuItem lightThemeItem = new JMenuItem("Light Theme");
        lightThemeItem.addActionListener(e -> applyTheme("Light"));
        themeMenu.add(lightThemeItem);

        JMenuItem darkThemeItem = new JMenuItem("Dark Theme");
        darkThemeItem.addActionListener(e -> applyTheme("Dark"));
        themeMenu.add(darkThemeItem);

        JMenuItem intelliJThemeItem = new JMenuItem("IntelliJ Theme");
        intelliJThemeItem.addActionListener(e -> applyTheme("IntelliJ"));
        themeMenu.add(intelliJThemeItem);

        JMenuItem darkPurpleThemeItem = new JMenuItem("Metal Theme");
        darkPurpleThemeItem.addActionListener(e -> applyTheme("Metal"));
        themeMenu.add(darkPurpleThemeItem);

        menuBar.add(themeMenu);




        JMenuItem blocksMenuItem = new JMenuItem("Blocks");
        blocksMenuItem.addActionListener(e -> openBlocksWindow()); // Action when clicked
        blocksMenu.add(blocksMenuItem);

        menuBar.add(blocksMenu);
    }

    private void openBlocksWindow() {
        JFrame blocksWindow = new JFrame("Manage Blocks");
        blocksWindow.setSize(400, 600);
        blocksWindow.setLocationRelativeTo(null); // Center the window
        blocksWindow.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);

        // Panel for the whole content
        JPanel blocksPanel = new JPanel();
        blocksPanel.setLayout(new BoxLayout(blocksPanel, BoxLayout.Y_AXIS));

        // Create a panel for the search field
        JPanel searchPanel = new JPanel();
        JTextField searchField = new JTextField();
        searchField.setPreferredSize(new Dimension(350, 30));
        searchField.setMaximumSize(new Dimension(350, 30));

        // Add listener to update block list based on search text
        searchField.getDocument().addDocumentListener(new DocumentListener() {
            @Override
            public void insertUpdate(DocumentEvent e) {
                SwingUtilities.invokeLater(() -> filterBlocks(searchField.getText().trim(), blocksPanel));
            }

            @Override
            public void removeUpdate(DocumentEvent e) {
                SwingUtilities.invokeLater(() -> filterBlocks(searchField.getText().trim(), blocksPanel));
            }

            @Override
            public void changedUpdate(DocumentEvent e) {
                SwingUtilities.invokeLater(() -> filterBlocks(searchField.getText().trim(), blocksPanel));
            }
        });

        // Add the search field to its own panel
        searchPanel.add(searchField);
        blocksPanel.add(searchPanel);

        // Create a scrollable container for the block list (initially with all blocks)
        JPanel blockListPanel = new JPanel();
        blockListPanel.setLayout(new BoxLayout(blockListPanel, BoxLayout.Y_AXIS));

        // Add blocks to the panel initially (before search filtering)
        for (ImageInfo block : blockGradientGenerator.allBlocks) {
            JPanel blockPanel = createBlockPanel(block);
            blockListPanel.add(blockPanel);
        }

        // Add block list to a scroll pane
        JScrollPane scrollPane = new JScrollPane(blockListPanel);
        blocksPanel.add(scrollPane);

        blocksWindow.add(blocksPanel);
        blocksWindow.setVisible(true);
    }

    private void filterBlocks(String query, JPanel blocksPanel) {
        // Get the JScrollPane that contains the block list panel
        JScrollPane scrollPane = (JScrollPane) blocksPanel.getComponent(1);
        JPanel blockListPanel = (JPanel) scrollPane.getViewport().getView();

        // Remove all existing blocks
        blockListPanel.removeAll();

        // Filter and add only matching blocks to the list
        for (ImageInfo block : blockGradientGenerator.allBlocks) {
            if (block.name.toLowerCase().contains(query.toLowerCase())) {
                JPanel blockPanel = createBlockPanel(block);
                blockListPanel.add(blockPanel);
            }
        }

        // Refresh the UI
        blockListPanel.revalidate();
        blockListPanel.repaint();
    }

    // Create the block panel with checkbox and icon
    private JPanel createBlockPanel(ImageInfo block) {
        JPanel blockPanel = new JPanel();
        blockPanel.setLayout(new BoxLayout(blockPanel, BoxLayout.X_AXIS));

        // Create the checkbox for the block
        JCheckBox checkBox = new JCheckBox(block.name, block.allowed);
        checkBox.addActionListener(e -> {
            block.allowed = checkBox.isSelected();
        });

        // Create an icon from the block's image
        ImageIcon blockIcon = new ImageIcon(block.image); // Assuming ImageInfo has an Image
        JLabel iconLabel = new JLabel(blockIcon);

        // Add the icon and checkbox to the block panel
        blockPanel.add(iconLabel);
        blockPanel.add(checkBox);

        return blockPanel;
    }


    // Apply selected theme
    private void applyTheme(String theme) {
        try {
            if ("Light".equals(theme)) {
                UIManager.setLookAndFeel(new FlatLightLaf());
            } else if ("Dark".equals(theme)) {
                UIManager.setLookAndFeel(new FlatDarkLaf());
            } else if ("IntelliJ".equals(theme)) {
                UIManager.setLookAndFeel(new FlatIntelliJLaf());
            } else if ("Metal".equals(theme)) {
                UIManager.setLookAndFeel(new MetalLookAndFeel());
            }
            SwingUtilities.updateComponentTreeUI(frame);
        } catch (UnsupportedLookAndFeelException e) {
            e.printStackTrace();
        }
    }
}
