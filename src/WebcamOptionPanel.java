import javax.swing.JPanel;
import javax.swing.JLabel;
import javax.swing.JTextField;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;

import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.DefaultListModel;
import javax.swing.JButton;
import javax.swing.JList;
import javax.swing.JOptionPane;
import javax.swing.JComboBox;

public class WebcamOptionPanel extends JPanel {
	private JTextField textField_MinRadius;
	private JTextField textField_MaxRadius;
	private JButton btnApply;
	private JLabel lblNewLabel_2;
	private JComboBox<String> comboBox;
	private JComboBox<Integer> comboBox2;
	private JTextField textField_PosX;
	private JTextField textField_PosY;
	private JTextField textField_Width;
	private JTextField textField_Height;
	private JTextField textField_ScanResult;
	private JButton btnStartDetect;
	private JButton btnTrainfalse;
	private JButton btnStopdetect;
	private JButton btnTrainblack;
	private JButton btnTrainwhite;
	private JLabel lblCameraList;
	private JTextField minDistance;
	private JTextField maxDistance;
	
	/**
	 * Create the panel.
	 */
	public WebcamOptionPanel() {
		setLayout(null);
		
		// TODO: use MVC pattern to remove this function
		Dimension size = new Dimension(240, 170);
        setPreferredSize(new Dimension(240, 571));
        setMinimumSize(size);
        setMaximumSize(size);
        setSize(size);
        setLayout(null);
        setAutoscrolls(true);
		
		JLabel lblNewLabel = new JLabel("Min Radius");
		lblNewLabel.setBounds(38, 30, 64, 15);
		add(lblNewLabel);
		
		JLabel lblNewLabel_1 = new JLabel("Max Radius");
		lblNewLabel_1.setBounds(38, 55, 68, 15);
		add(lblNewLabel_1);
		
		textField_MinRadius = new JTextField();
		textField_MinRadius.setBounds(137, 27, 65, 21);
		add(textField_MinRadius);
		textField_MinRadius.setColumns(10);
		
		textField_MaxRadius = new JTextField();
		textField_MaxRadius.setBounds(137, 52, 65, 21);
		add(textField_MaxRadius);
		textField_MaxRadius.setColumns(10);
		
		textField_MinRadius.setText(Integer.toString(WebcamVariables.MinRadius));
		textField_MaxRadius.setText(Integer.toString(WebcamVariables.MaxRadius));
		
		btnApply = new JButton("Apply");
		btnApply.setBounds(38, 346, 164, 23);
		
		btnApply.addActionListener(new ActionListener() {
			// TODO: use MVC pattern to remove this function
			public void actionPerformed(ActionEvent e) {
				try {
					
					
					int min = Integer.parseInt(textField_MinRadius.getText());
					int max = Integer.parseInt(textField_MaxRadius.getText());
					
					if(min < 0 || max < 0) {
						
						// Revert data on text fields
						textField_MinRadius.setText(Integer.toString(WebcamVariables.MinRadius));
						textField_MaxRadius.setText(Integer.toString(WebcamVariables.MaxRadius));
					} else {
						if(min >= max) {
							max = min + 5;
							
							// update max radius text field
							textField_MaxRadius.setText(Integer.toString(max));
						}
						
						WebcamVariables.MinRadius = min;
						WebcamVariables.MaxRadius = max;
					}
					
					min = Integer.parseInt(minDistance.getText());
					max = Integer.parseInt(maxDistance.getText());
					
					if(min < 0 || max < 0) {
						
						// Revert data on text fields
						minDistance.setText(Integer.toString(WebcamVariables.MinDistance));
						maxDistance.setText(Integer.toString(WebcamVariables.MaxDistance));
					} else {
						if(min >= max) {
							max = min + 5;
							
							// update max radius text field
							maxDistance.setText(Integer.toString(max));
						}
						
						WebcamVariables.MinDistance = min;
						WebcamVariables.MaxDistance = max;
					}
					
					int posX = Integer.parseInt(textField_PosX.getText());
					int posY = Integer.parseInt(textField_PosY.getText());
					int width = Integer.parseInt(textField_Width.getText());
					int height = Integer.parseInt(textField_Height.getText());
					
					if(posX < 0 || posY < 0 || width < 0 || height < 0) {
						// Revert data on text fields
						textField_PosX.setText(Integer.toString(WebcamVariables.SubAreaPosX));
						textField_PosY.setText(Integer.toString(WebcamVariables.SubAreaPosY));
						textField_Width.setText(Integer.toString(WebcamVariables.SubAreaWidth));
						textField_Height.setText(Integer.toString(WebcamVariables.SubAreaHeight));
					} else {
						if(posX > 0 || posY > 0) {
							if(width == 0) {
								width += 5;
							}
							if(height == 0) {
								height += 5;
							}
						}
						
						WebcamVariables.SubAreaPosX = posX;
						WebcamVariables.SubAreaPosY = posY;
						WebcamVariables.SubAreaWidth = width;
						WebcamVariables.SubAreaHeight = height;
					}
				} catch (Exception ex) {
					// If error occurs, revert data on text fields
					// TODO: Prohibit text on text field
					textField_MinRadius.setText(Integer.toString(WebcamVariables.MinRadius));
					textField_MaxRadius.setText(Integer.toString(WebcamVariables.MaxRadius));
					
					textField_PosX.setText(Integer.toString(WebcamVariables.SubAreaPosX));
					textField_PosY.setText(Integer.toString(WebcamVariables.SubAreaPosY));
					textField_Width.setText(Integer.toString(WebcamVariables.SubAreaWidth));
					textField_Height.setText(Integer.toString(WebcamVariables.SubAreaHeight));
				}
			}
		});
		
		add(btnApply);
		
		lblNewLabel_2 = new JLabel("Options");
		lblNewLabel_2.setBounds(38, 10, 164, 15);
		add(lblNewLabel_2);
		
		comboBox = new JComboBox<String>();
		comboBox.setBounds(38, 137, 164, 21);
		for(int index = 0; index < DetectType.values().length; index++) {
			comboBox.addItem(DetectType.values()[index].toString());
		}
		comboBox.setSelectedItem(WebcamVariables.DetectMethod.toString());
		comboBox.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
		        JComboBox cb = (JComboBox)e.getSource();
		        // TODO: use conversion from enum to int to compare index
		        if(cb.getSelectedIndex() == 0 /*DetectType.Hough*/) {
		        	/*JOptionPane.showMessageDialog(null, "Hough method is currently deprecated.\n"
		        			+ "Use Contour or Barcode method.", "InfoBox", JOptionPane.INFORMATION_MESSAGE);
		        	
		        	comboBox.setSelectedItem(WebcamVariables.DetectMethod.toString()); // Revert to last selected item
		        	*/
		        	WebcamVariables.DetectMethod = DetectType.Hough; 
		        } else if(cb.getSelectedIndex() == 1 /*DetectType.Contour*/) {
		        	WebcamVariables.DetectMethod = DetectType.Contour;
		        } else if(cb.getSelectedIndex() == 2 /*DetectType.Contour2*/) {
		        	WebcamVariables.DetectMethod = DetectType.Contour2;
		        } else if(cb.getSelectedIndex() == 3 /*DetectType.Triangle*/) {
		        	WebcamVariables.DetectMethod = DetectType.Triangle;
		        } else if(cb.getSelectedIndex() == 4 /*DetectType.Rectangle*/) {
		        	WebcamVariables.DetectMethod = DetectType.Rectangle;
		        } else if(cb.getSelectedIndex() == 5 /*DetectType.Square*/) {
		        	WebcamVariables.DetectMethod = DetectType.Square;
		        } else if(cb.getSelectedIndex() == 6 /*DetectType.Barocde*/) {
		        	WebcamVariables.DetectMethod = DetectType.Barcode;
		        }
		    }
		});
		add(comboBox);
		
		comboBox2 = new JComboBox<Integer>();
		comboBox2.setBounds(38, 186, 164, 21);
		for(int index = 0; index < WebcamVariables.TotalCameraCount; index++) {
			comboBox2.addItem(index);
		}
		comboBox2.setSelectedItem(WebcamVariables.CurrentCameraID);
		comboBox2.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
		        JComboBox cb = (JComboBox)e.getSource();
		        WebcamVariables.CurrentCameraID = (int)cb.getSelectedItem();
		    }
		});
		add(comboBox2);
		
		JLabel lblNewLabel_3 = new JLabel("Sub Area");
		lblNewLabel_3.setBounds(38, 214, 57, 15);
		add(lblNewLabel_3);
		
		JLabel lblNewLabel_4 = new JLabel("Pos X");
		lblNewLabel_4.setBounds(38, 239, 57, 15);
		add(lblNewLabel_4);
		
		JLabel lblNewLabel_5 = new JLabel("Pos Y");
		lblNewLabel_5.setBounds(137, 239, 57, 15);
		add(lblNewLabel_5);
		
		JLabel lblNewLabel_6 = new JLabel("Width");
		lblNewLabel_6.setBounds(38, 294, 57, 15);
		add(lblNewLabel_6);
		
		JLabel lblNewLabel_7 = new JLabel("Height");
		lblNewLabel_7.setBounds(137, 294, 65, 15);
		add(lblNewLabel_7);
		
		textField_PosX = new JTextField();
		textField_PosX.setBounds(38, 264, 64, 21);
		add(textField_PosX);
		textField_PosX.setColumns(10);
		
		textField_PosY = new JTextField();
		textField_PosY.setColumns(10);
		textField_PosY.setBounds(137, 264, 64, 21);
		add(textField_PosY);
		
		textField_Width = new JTextField();
		textField_Width.setColumns(10);
		textField_Width.setBounds(38, 315, 64, 21);
		add(textField_Width);
		
		textField_Height = new JTextField();
		textField_Height.setColumns(10);
		textField_Height.setBounds(137, 315, 64, 21);
		add(textField_Height);
		
		textField_PosX.setText(Integer.toString(WebcamVariables.SubAreaPosX));
		textField_PosY.setText(Integer.toString(WebcamVariables.SubAreaPosY));
		textField_Width.setText(Integer.toString(WebcamVariables.SubAreaWidth));
		textField_Height.setText(Integer.toString(WebcamVariables.SubAreaHeight));
		
		textField_ScanResult = new JTextField();
		textField_ScanResult.setBounds(38, 373, 164, 21);
		add(textField_ScanResult);
		textField_ScanResult.setColumns(10);
		
		btnTrainblack = new JButton("Train_Black");
		btnTrainblack.setBounds(38, 404, 164, 23);
		btnTrainblack.addActionListener(new ActionListener() {
			// TODO: use MVC pattern to remove this function
			public void actionPerformed(ActionEvent e) {
				WebcamVariables.Train = TrainImage.Black;
			}
		});
		add(btnTrainblack);
		
		btnTrainwhite = new JButton("Train_White");
		btnTrainwhite.setBounds(38, 439, 164, 23);
		btnTrainwhite.addActionListener(new ActionListener() {
			// TODO: use MVC pattern to remove this function
			public void actionPerformed(ActionEvent e) {
				WebcamVariables.Train = TrainImage.White;
			}
		});
		add(btnTrainwhite);
		
		btnTrainfalse = new JButton("Train_False");
		btnTrainfalse.setBounds(38, 472, 164, 23);
		btnTrainfalse.addActionListener(new ActionListener() {
			// TODO: use MVC pattern to remove this function
			public void actionPerformed(ActionEvent e) {
				WebcamVariables.Train = TrainImage.False;
			}
		});
		add(btnTrainfalse);
		
		lblCameraList = new JLabel("Camera list");
		lblCameraList.setBounds(38, 164, 88, 15);
		add(lblCameraList);
		
		btnStartDetect = new JButton("Start Detect");
		btnStartDetect.setBounds(38, 505, 165, 23);
		btnStartDetect.addActionListener(new ActionListener() {
			// TODO: use MVC pattern to remove this function
			public void actionPerformed(ActionEvent e) {
				WebcamVariables.Train = TrainImage.Start;
				System.out.println("Scan started");
				setButtonsEnalbed(false);
			}
		});
		add(btnStartDetect);
		
		btnStopdetect = new JButton("Stop_Detect");
		btnStopdetect.setBounds(38, 538, 164, 23);
		btnStopdetect.addActionListener(new ActionListener() {
			// TODO: use MVC pattern to remove this function
			public void actionPerformed(ActionEvent e) {
				WebcamVariables.Train = TrainImage.Training;
				System.out.println("Scan stopped");
				setButtonsEnalbed(true);
			}
		});
		add(btnStopdetect);
		
		JLabel lblMinDistance = new JLabel("Min Dist");
		lblMinDistance.setBounds(38, 86, 64, 15);
		add(lblMinDistance);
		
		JLabel lblMaxDist = new JLabel("Max Dist");
		lblMaxDist.setBounds(38, 112, 68, 15);
		add(lblMaxDist);
		
		minDistance = new JTextField();
		minDistance.setBounds(137, 80, 65, 21);
		add(minDistance);
		minDistance.setColumns(10);
		
		maxDistance = new JTextField();
		maxDistance.setBounds(137, 111, 65, 21);
		add(maxDistance);
		maxDistance.setColumns(10);
		
		minDistance.setText(Integer.toString(WebcamVariables.MinDistance));
		maxDistance.setText(Integer.toString(WebcamVariables.MaxDistance));
		
		if(WebcamVariables.Train == TrainImage.Start) {

			setButtonsEnalbed(false);
		} else {

			setButtonsEnalbed(true);
		}
		
		DefaultListModel<String> model = new DefaultListModel<>();
		for ( int i = 0; i < DetectType.values().length; i++ ){
			model.addElement( DetectType.values()[i].toString() );
		}
	}
	
	public void setButtonsEnalbed(boolean isEnabled) {
		
		if(WebcamVariables.CurrentPermission == Permission.Manager) {
			
			btnApply.setEnabled(isEnabled);
			comboBox.setEnabled(isEnabled);
			comboBox2.setEnabled(isEnabled);
			btnTrainblack.setEnabled(isEnabled);
			btnTrainwhite.setEnabled(isEnabled);
			btnTrainfalse.setEnabled(isEnabled);
			
			textField_MinRadius.setEnabled(isEnabled);
			textField_MaxRadius.setEnabled(isEnabled);
			minDistance.setEnabled(isEnabled);
			maxDistance.setEnabled(isEnabled);
			textField_PosX.setEnabled(isEnabled);
			textField_PosY.setEnabled(isEnabled);
			textField_Width.setEnabled(isEnabled);
			textField_Height.setEnabled(isEnabled);
		} else if(WebcamVariables.CurrentPermission == Permission.Operator) {
			
			btnApply.setEnabled(false);
			comboBox.setEnabled(false);
			comboBox2.setEnabled(false);
			btnTrainblack.setEnabled(false);
			btnTrainwhite.setEnabled(false);
			btnTrainfalse.setEnabled(false);
			
			textField_MinRadius.setEnabled(false);
			textField_MaxRadius.setEnabled(false);
			minDistance.setEnabled(false);
			maxDistance.setEnabled(false);
			textField_PosX.setEnabled(false);
			textField_PosY.setEnabled(false);
			textField_Width.setEnabled(false);
			textField_Height.setEnabled(false);
		}
		
		btnStartDetect.setEnabled(isEnabled);
		btnStopdetect.setEnabled(!isEnabled);
		
		textField_ScanResult.setText("");
	}
	
	public void setScanResultText(String text) {
		textField_ScanResult.setText(text);
	}
	
	public String getScanResultText() {
		return textField_ScanResult.getText();
	}
	
	public void refreshUI() {
		comboBox2.setSelectedItem(WebcamVariables.CurrentCameraID);	
	}
	
	// TODO: use MVC pattern to use this function
	public void setMinRadius(int minRadius) {
		textField_MinRadius.setText(Integer.toString(minRadius));
	}
	
	// TODO: use MVC pattern to use this function
	public void setMaxRadius(int maxRadius) {
		textField_MaxRadius.setText(Integer.toString(maxRadius));
	}
}
