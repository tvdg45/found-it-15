import java.awt.BorderLayout;
import java.awt.EventQueue;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.border.EmptyBorder;
import javax.swing.JTextField;
import javax.swing.JLabel;
import javax.swing.JButton;
import javax.swing.JPasswordField;

public class LoginFrame extends JFrame {

	private JPanel contentPane;
	private JTextField idField;
	private JPasswordField passwordField;
	private JButton loginButton;
	
	/**
	 * Launch the application.
	 */
	public static void main(String[] args) {
		EventQueue.invokeLater(new Runnable() {
			public void run() {
				try {
					LoginFrame frame = new LoginFrame();
					frame.setVisible(true);
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		});
	}

	/**
	 * Create the frame.
	 */
	public LoginFrame() {
		setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		setBounds(100, 100, 450, 300);
		contentPane = new JPanel();
		contentPane.setBorder(new EmptyBorder(5, 5, 5, 5));
		setContentPane(contentPane);
		contentPane.setLayout(null);
		
		idField = new JTextField();
		idField.setBounds(111, 82, 244, 21);
		contentPane.add(idField);
		idField.setColumns(10);
		
		passwordField = new JPasswordField();
		passwordField.setBounds(111, 148, 244, 21);
		contentPane.add(passwordField);
		
		JLabel lblNewLabel = new JLabel("ID");
		lblNewLabel.setBounds(12, 82, 57, 24);
		contentPane.add(lblNewLabel);
		
		JLabel lblNewLabel_1 = new JLabel("Password");
		lblNewLabel_1.setBounds(12, 148, 57, 21);
		contentPane.add(lblNewLabel_1);
		
		loginButton = new JButton("Log In");
		loginButton.setBounds(220, 195, 135, 43);
		
		loginButton.addActionListener(new ActionListener() {
			// TODO: use MVC pattern to remove this function
			public void actionPerformed(ActionEvent e) {
				String id = idField.getText();
				char[] password = passwordField.getPassword();
			}
		});
		
		contentPane.add(loginButton);
		
		
	}
}
