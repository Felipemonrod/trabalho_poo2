package ui;

import exceptions.ArquivoInvalidoException;
import exceptions.ExceptionFormatter;
import model.Regiao;
import service.Entrada;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.io.File;
import java.util.List;

public class GuiApp {
    private final JFrame frame = new JFrame("Dashboards Populacionais - Parte 1");
    private final RegiaoTableModel model = new RegiaoTableModel();

    public GuiApp() {
        initUI();
    }

    private void initUI() {
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setSize(1000, 600);
        frame.setLocationRelativeTo(null);

        JTable table = new JTable(model);
        table.setAutoCreateRowSorter(true);

        JScrollPane scroll = new JScrollPane(table);

        JButton btnOpen = new JButton(new AbstractAction("Abrir CSV...") {
            @Override
            public void actionPerformed(ActionEvent e) {
                JFileChooser fc = new JFileChooser();
                fc.setCurrentDirectory(new File("."));
                int res = fc.showOpenDialog(frame);
                if (res == JFileChooser.APPROVE_OPTION) {
                    File f = fc.getSelectedFile();
                    loadFile(f.getAbsolutePath());
                }
            }
        });

        JPanel top = new JPanel(new FlowLayout(FlowLayout.LEFT));
        top.add(btnOpen);

        frame.getContentPane().setLayout(new BorderLayout());
        frame.getContentPane().add(top, BorderLayout.NORTH);
        frame.getContentPane().add(scroll, BorderLayout.CENTER);
    }

    private void loadFile(String path) {
        try {
            List<Regiao> regs = Entrada.lerArquivo(path);
            model.setData(regs);
            JOptionPane.showMessageDialog(frame, "Arquivo carregado: " + regs.size() + " regiões.", "Sucesso", JOptionPane.INFORMATION_MESSAGE);
        } catch (ArquivoInvalidoException ex) {
            JOptionPane.showMessageDialog(frame, ExceptionFormatter.detalhar(ex), "Erro ao ler arquivo", JOptionPane.ERROR_MESSAGE);
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(frame, ExceptionFormatter.detalhar(ex), "Erro inesperado", JOptionPane.ERROR_MESSAGE);
        }
    }

    public void show() {
        SwingUtilities.invokeLater(() -> frame.setVisible(true));
    }

    public static void main(String[] args) {
        GuiApp g = new GuiApp();
        g.show();
    }
}
