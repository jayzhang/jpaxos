package jay;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.Timer;

public class TestTimer {

	
	public static class MyTimeoutListener implements ActionListener
	{
		Timer timer;
		
		public MyTimeoutListener(Timer timer)
		{
			this.timer = timer;
		}
		
		public void actionPerformed(ActionEvent actionEvent) {
			
			//TODO
			
			boolean allreceived = false;
			
			
			if(allreceived)
			{
				//TODO sendout	
			}
			else 
			{
				
			}
		}
	}
	
	public static void main(String[] args) {

		Timer timer = new Timer(0, null);
		
		ActionListener actionListener = new MyTimeoutListener(timer);
		
		timer.setDelay(500);
		
		timer.addActionListener(actionListener);
		
		timer.start();
		
//		for(int i = 0 ; i < 10; ++ i)
//		{
//			try {
//				Thread.sleep(1000);
//			} catch (InterruptedException e) {
//				// TODO Auto-generated catch block
//				e.printStackTrace();
//			}
//		}
//
//		timer.setDelay( 2000);
		
		for(int i = 0 ; ; ++ i)
		{
			try {
				Thread.sleep(1000);
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
	}
	
}
