import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.*;
import java.net.Socket;

public class Cliend {
    private static Socket socket;
    private static DataOutputStream dos;

    public static void main(String[] args) {
        // 创建客户端UI
        JFrame frame = new JFrame("客戶端");
        frame.setSize(400, 300);
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setLayout(new BorderLayout());

        // 创建IP输入框和连接按钮
        JPanel connectionPanel = new JPanel();
        connectionPanel.setLayout(new FlowLayout());
        JLabel ipLabel = new JLabel("伺服器IP：");
        JTextField ipField = new JTextField(15);
        JButton connectButton = new JButton("連接伺服器");

        connectionPanel.add(ipLabel);
        connectionPanel.add(ipField);
        connectionPanel.add(connectButton);
        frame.add(connectionPanel, BorderLayout.NORTH);

        // 创建文本区域显示日志
        JTextArea logArea = new JTextArea();
        logArea.setEditable(false);
        JScrollPane scrollPane = new JScrollPane(logArea);
        frame.add(scrollPane, BorderLayout.CENTER);

        // 创建消息输入框、发送按钮和文件发送按钮
        JPanel panel = new JPanel();
        panel.setLayout(new BorderLayout());

        // +号按钮用于选择文件
        JButton sendFileButton = new JButton("+");
        sendFileButton.setPreferredSize(new Dimension(40, 40)); // 设置大小
        panel.add(sendFileButton, BorderLayout.WEST);

        // 消息输入框
        JTextField messageField = new JTextField();
        panel.add(messageField, BorderLayout.CENTER);

        // 发送消息按钮
        JButton sendButton = new JButton("發送消息");
        panel.add(sendButton, BorderLayout.EAST);

        frame.add(panel, BorderLayout.SOUTH);

        // 禁用消息和文件发送按钮，直到连接成功
        sendButton.setEnabled(false);
        sendFileButton.setEnabled(false);

        // 连接按钮点击事件
        connectButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                String serverIP = ipField.getText();

                try {
                    socket = new Socket(serverIP, 25565);
                    dos = new DataOutputStream(socket.getOutputStream());
                    logArea.append("成功連接到伺服器: " + serverIP + "\n");

                    // 连接成功后启用消息和文件发送按钮
                    sendButton.setEnabled(true);
                    sendFileButton.setEnabled(true);

                    // 禁用连接相关控件
                    connectButton.setEnabled(false);
                    ipField.setEditable(false);
                } catch (IOException ex) {
                    logArea.append("無法連接到伺服器: " + serverIP + "\n");
                    ex.printStackTrace();
                }
            }
        });

        // 发送消息按钮点击事件
        sendButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                try {
                    String message = messageField.getText();
                    dos.writeUTF(message);
                    logArea.append("發送消息: " + message + "\n");
                    messageField.setText("");
                } catch (IOException ex) {
                    logArea.append("消息發送失敗！\n");
                    ex.printStackTrace();
                }
            }
        });

        // +号按钮点击事件，用于选择并发送文件
        sendFileButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                JFileChooser fileChooser = new JFileChooser();
                int returnVal = fileChooser.showOpenDialog(frame);
                if (returnVal == JFileChooser.APPROVE_OPTION) {
                    File file = fileChooser.getSelectedFile();
                    try {
                        dos.writeUTF("sendfile");
                        dos.writeUTF(file.getName());

                        FileInputStream fis = new FileInputStream(file);
                        byte[] buffer = new byte[4096];
                        int bytesRead;
                        logArea.append("正在發送文件: " + file.getName() + "\n");

                        while ((bytesRead = fis.read(buffer)) != -1) {
                            dos.write(buffer, 0, bytesRead);
                        }

                        fis.close();
                        logArea.append("文件發送完成!\n");
                    } catch (IOException ex) {
                        logArea.append("文件發送失敗！\n");
                        ex.printStackTrace();
                    }
                }
            }
        });

        // 显示窗口
        frame.setVisible(true);
    }
}
