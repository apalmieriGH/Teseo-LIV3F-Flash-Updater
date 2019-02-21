package com.stm.flashUpdaterTool;

import java.util.Arrays;

import javax.swing.SwingUtilities;

public class FlashUpdater {

	private static UIManager uiManager;
	private static VCOMUpdater vUpdater;
	private static JsscPortManager jsscPortManager;

	private NewSerialPortTask newSerialPortTask;
	public boolean running;
	
	String[] portList = null;
	
	public FlashUpdater() {

		vUpdater = new VCOMUpdater();
		jsscPortManager = new JsscPortManager(this, vUpdater);
		uiManager = new UIManager(jsscPortManager);
		newSerialPortTask = new NewSerialPortTask();
    }

    public void showUI() {
    	uiManager.setVisible(true);
    	
    	running = true;
		startNewSerialPortTask();
    }
    
    public void dispatchEvent (Common.UpdaterEvent uEvent, Object message) {
    	uiManager.handleEvent(uEvent, message);
    }

    private void startNewSerialPortTask() {
		if(newSerialPortTask != null) {
			(new Thread(newSerialPortTask)).start();
		}
	}
    
    // Check the serial port list every 1 secs 
    private class NewSerialPortTask implements Runnable {

		@Override
		public void run() {
			
			while(running){
				
				String[] currentPortList = jsscPortManager.portList();
				if(!Arrays.equals(portList, currentPortList) && currentPortList!= null) {
					uiManager.refreshPortList(currentPortList);
					portList = currentPortList;
				}
			
				try{
					Thread.sleep(1000);
				} catch (InterruptedException ie) {
					//FIXME!!!
				}
			}
		}
		
	}
    
    public static void main(String[] args) {

        SwingUtilities.invokeLater(new Runnable() {
            @Override
            public void run() {
            	FlashUpdater flashUpdater = new FlashUpdater();
            	flashUpdater.showUI();
            }
        });
    }

}
