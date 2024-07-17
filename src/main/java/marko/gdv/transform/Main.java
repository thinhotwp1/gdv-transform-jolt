package marko.gdv.transform;

import com.bazaarvoice.jolt.Chainr;
import com.bazaarvoice.jolt.JsonUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.swing.*;
import java.awt.*;
import java.io.*;
import java.util.Arrays;
import java.util.List;

public class Main extends JFrame {
    private static final Logger logger = LoggerFactory.getLogger(Main.class);

    JTextArea logArea;
    private Object inputJSON;
    private List<Object> chainSpecJSON;

    public Main() {
        initGui();
    }

    private void initGui() {
        setTitle("Gdv Json Transformer");
        setSize(600, 400);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);
        setLayout(new BorderLayout());

        logArea = new JTextArea();
        logArea.setEditable(false);
        logArea.append("Welcome to Gdv Json Transform.\n[Step 1] Import Spec Jolt File.\n[Step 2] Import Input File.\n");
        JScrollPane logScrollPane = new JScrollPane(logArea);
        add(logScrollPane, BorderLayout.CENTER);

        JButton importSpecButton = new JButton("Import spec.json");
        importSpecButton.addActionListener(e -> importSpec());

        JButton importInputButton = new JButton("Import input.json");
        importInputButton.addActionListener(e -> importInput());

        // Clear Log
        JButton clearLogButton = new JButton("Clear Log");
        clearLogButton.addActionListener(event -> logArea.replaceRange("Clear log success.\n", 0, logArea.getDocument().getLength()));

        JPanel buttonPanel = new JPanel();
        buttonPanel.setLayout(new FlowLayout());
        buttonPanel.add(importSpecButton);
        buttonPanel.add(importInputButton);
        buttonPanel.add(clearLogButton);

        getContentPane().add(logScrollPane, BorderLayout.CENTER);
        getContentPane().add(buttonPanel, BorderLayout.SOUTH);
    }

    private void importSpec() {
        JFileChooser fileChooser = new JFileChooser();
        int returnValue = fileChooser.showOpenDialog(this);
        if (returnValue == JFileChooser.APPROVE_OPTION) {
            File selectedFile = fileChooser.getSelectedFile();
            try (InputStream inputStream = new FileInputStream(selectedFile)) {
                chainSpecJSON = JsonUtils.jsonToList(inputStream);
                logArea.append("Loaded spec.json\n");
            } catch (Exception ex) {
                logArea.append("Failed to load spec.json: " + ex.getMessage() + "\n");
                ex.printStackTrace();
            }
        }
    }

    private void importInput() {
        JFileChooser fileChooser = new JFileChooser();
        int returnValue = fileChooser.showOpenDialog(this);
        if (returnValue == JFileChooser.APPROVE_OPTION) {
            File selectedFile = fileChooser.getSelectedFile();
            try (InputStream inputStream = new FileInputStream(selectedFile)) {
                inputJSON = JsonUtils.jsonToObject(inputStream);
                logArea.append("Loaded input.json\n");
                exportResult();
            } catch (Exception ex) {
                logArea.append("Failed to load input.json: " + ex.getMessage() + "\n");
                ex.printStackTrace();
            }
        }
    }


    private void logSystem(String logString) {
        logArea.append(logString + "\n");
        logger.debug(logString);
    }

    private void exportResult() {
        try {
            Chainr chainr = Chainr.fromSpec(chainSpecJSON);
            Object transformedOutput = chainr.transform(inputJSON);
            saveJsonToFile(JsonUtils.toJsonString(transformedOutput), "result.json");
        } catch (Exception ex) {
            logSystem("Failed to transform and export JSON: " + ex.getMessage() + ", cause: " + ex.getCause() + ", stack: " + Arrays.toString(ex.getStackTrace()));
        }
    }

    private void saveJsonToFile(String jsonDocument, String nameFile) {
        JFileChooser fileChooser = new JFileChooser();
        fileChooser.setDialogTitle("Save JSON File");
        fileChooser.setSelectedFile(new File(nameFile));
        int userSelection = fileChooser.showSaveDialog(this);
        if (userSelection == JFileChooser.APPROVE_OPTION) {
            File fileToSave = fileChooser.getSelectedFile();
            try (FileWriter fileWriter = new FileWriter(fileToSave)) {
                fileWriter.write(jsonDocument);
                logSystem("Saved result to file: " + fileToSave.getAbsolutePath());
                logger.info("Saved to file: {}", fileToSave.getAbsolutePath());
            } catch (IOException e) {
                logSystem("Error saving JSON to file: " + e.getMessage());
                logger.error("Error saving JSON to file: {}\n{}", e.getMessage(), Arrays.toString(e.getStackTrace()));
            }
        }
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> new Main().setVisible(true));
    }
}
