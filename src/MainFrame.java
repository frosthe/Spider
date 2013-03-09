import java.awt.BorderLayout;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.Calendar;
import javax.swing.JButton;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JProgressBar;
import javax.swing.JTextField;

public class MainFrame extends JFrame {
	public static void main(String[] args) {
		Calendar calendar = Calendar.getInstance();
		final int yearNow = calendar.get(Calendar.YEAR);
		final int monthNow = calendar.get(Calendar.MONTH) + 1;
		final int dayNow = calendar.get(Calendar.DATE);
		final JFrame frame = new JFrame("人民日报新闻下载器");
		final JTextField yearField = new JTextField(4);
		yearField.setHorizontalAlignment(JTextField.RIGHT);
		yearField.setText(yearNow+"");
		final JTextField monthField = new JTextField(4);
		monthField.setHorizontalAlignment(JTextField.RIGHT);
		monthField.setText(monthNow+"");
		final JTextField dayField = new JTextField(4);
		dayField.setHorizontalAlignment(JTextField.RIGHT);
		dayField.setText(""+dayNow);
		final JButton start = new JButton("下载");
		final JTextField dirField = new JTextField(22);
		dirField.setEditable(false);
		dirField.setText("C:\\");
		final JButton browse = new JButton("浏览");
		final JProgressBar bar = new JProgressBar(JProgressBar.HORIZONTAL);
		bar.setValue(0);
		bar.setMaximum(100);
		
		
		
		
		/*next set the layout*/
		frame.setLayout(new GridLayout(3,1));
		
		//the panel above
		//year
		JPanel year = new JPanel();
		JLabel yearLabel = new JLabel("年");
		year.add(yearField);
		year.add(yearLabel);
		
		//month
		JPanel month = new JPanel();
		JLabel monthLabel = new JLabel("月");
		month.add(monthField);
		month.add(monthLabel);
		
		//day
		JPanel day = new JPanel();
		JLabel dayLabel = new JLabel("日");
		day.add(dayField);
		day.add(dayLabel);
		
		//date
		JPanel date = new JPanel();
		date.add(year);
		date.add(month);
		date.add(day);
		
		//panel above
		JPanel above = new JPanel(new BorderLayout());
		above.add(date, BorderLayout.CENTER);
		/*add actionListen here*/
		start.addActionListener(new ActionListener() {
			boolean checkDate(int yearNow,int monthNow,int dayNow,int yearInput,int monthInput,int dayInput){
				if(yearInput > yearNow)
					return false;
				else if(yearInput == yearNow){
					if(monthInput > monthNow)
						return false;
					else if(monthInput == monthNow){
						if(dayInput > dayNow){
							return false;
						}
					}
				}
				if(monthInput > 12 || monthInput < 1)
					return false;
				
				switch (monthInput) {
				case 1:case 3:case 5:case 7:case 8:case 10:case 12:{
					if(dayInput > 31 || dayInput < 1)
						return false;
					break;
				}
				case 4:case 6:case 9:case 11:{
					if(dayInput > 30 || dayInput < 1)
						return false;
					break;
				}
				default:break;
				}
				
				if(monthInput == 2 ){
					if(yearInput % 400 == 0 || (yearInput % 4 == 0 && yearInput % 100 != 0)){
						//闰年
						if(dayInput > 29 || dayInput < 1)
							return false;
					}
					else{
						//不是闰年
						if(dayInput > 28 || dayInput < 1)
							return false;
					}
				}
				
				return true;
			}
			public void actionPerformed(ActionEvent arg0) {
				if(dirField.getText().equals(""))
					JOptionPane.showMessageDialog(null,"请选择下载路径","Warning!",JOptionPane.ERROR_MESSAGE);
				else if(yearField.getText().equals("")|| monthField.getText().equals("")|| dayField.getText().equals(""))
					JOptionPane.showMessageDialog(null,"请输入日期","Warning!",JOptionPane.ERROR_MESSAGE);
				else{
					int yearInput = Integer.parseInt(yearField.getText());
					int monthInput = Integer.parseInt(monthField.getText());
					int dayInput = Integer.parseInt(dayField.getText());
					if(!checkDate(yearNow, monthNow, dayNow, yearInput, monthInput, dayInput))
						JOptionPane.showMessageDialog(null,"日期输入有误，请重新输入","Warning!",JOptionPane.ERROR_MESSAGE);
					else {
						Spider spider = new Spider(dirField.getText(), yearInput, monthInput, dayInput,bar);
						spider.addPropertyChangeListener(new PropertyChangeListener() {
							public void propertyChange(PropertyChangeEvent e) {
								if("progress".equals(e.getPropertyName()))
									bar.setValue((Integer)e.getNewValue());
							}
						});
						spider.execute();
					}
				}
			}
		});
		
		
		JPanel Panel1 = new JPanel();
		Panel1.add(start);
		above.add(Panel1,BorderLayout.EAST);
		frame.add(above);
		
		//the panel below
		//dir
		JLabel dirLabel = new JLabel("目录");
		JPanel dir = new JPanel();
		dir.add(dirLabel);
		dir.add(dirField);
		
		//panel middle
		JPanel panel2 = new JPanel();
		/*add actionListen here*/
		browse.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {
				/*浏览下载目录，默认在C盘下*/
				JFileChooser fileChooser = new JFileChooser("C:\\");
		    	fileChooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
		        int returnVal = fileChooser.showOpenDialog(fileChooser);
		        if(returnVal == JFileChooser.APPROVE_OPTION){       
		        	String filePath= fileChooser.getSelectedFile().getAbsolutePath();//the download directory you choose
		        	dirField.setText(filePath);
		    	}
			}
		});
		panel2.add(browse);
		JPanel middle = new JPanel(new BorderLayout());
		middle.add(dir, BorderLayout.CENTER);
		middle.add(panel2,BorderLayout.EAST);
		frame.add(middle);
		
		//panel below
		JPanel below = new JPanel(new BorderLayout());
		below.add(bar,BorderLayout.CENTER);
		frame.add(below);
		
		//show the window
		frame.setSize(400,140);
		frame.setLocationRelativeTo(null);
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		frame.setVisible(true);
	}

}
