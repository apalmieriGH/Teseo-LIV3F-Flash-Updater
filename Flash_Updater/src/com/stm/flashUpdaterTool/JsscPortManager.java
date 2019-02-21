package com.stm.flashUpdaterTool;

import java.io.IOException;
import java.util.Arrays;

import javax.swing.JOptionPane;
import javax.swing.SwingWorker;

import com.stm.flashUpdaterTool.Common.UpdaterFunction;

import com.fazecast.jSerialComm.*;

public class JsscPortManager {
	private FlashUpdater flashUpdater;
	
	private SerialPort serialPort;
	private VCOMUpdater vUpdater;

	private String versionString;
	private static boolean motherBoardIsChecked;
	
	private UpdaterWorker uw = null;
		
	/** Constructor
	 * 
	 * @param flashUpdater
	 * @param vUpdater
	 */
	public JsscPortManager(FlashUpdater flashUpdater, VCOMUpdater vUpdater) {
		this.flashUpdater = flashUpdater;
		this.vUpdater = vUpdater;
		this.versionString = null;

	}

	/**
	 * Get the serial ports list
	 * @return port names list
	 */
	public String[] portList() {
		String[] portNames;
		SerialPort[] comPorts;
		
		comPorts = SerialPort.getCommPorts();
		portNames = new String[comPorts.length];
		for(int i = 0; i < comPorts.length; i++) {
			portNames[i] = comPorts[i].getSystemPortName()+Common.SERIAL_PORT_NAME_SEPARATOR+comPorts[i].getDescriptivePortName();
			//System.out.println(String.format(portNames[i]));
		}
		return portNames;
	}

	/** Open serial port and set parameters
	 *  
	 * @param descriptivePortName
	 * @throws Exception
	 */
	public void openPort(String descriptivePortName) throws Exception {
		SerialPort[] comPorts;
		String portName = null;
		
		comPorts = SerialPort.getCommPorts();
		for(int i = 0; i < comPorts.length; i++) {
			String [] names = descriptivePortName.split(Common.SERIAL_PORT_NAME_SEPARATOR, 2);
			if(comPorts[i].getDescriptivePortName().equals(names[1])) {
				portName = comPorts[i].getSystemPortName();
			}
		}
		if(portName == null) {
			throw new SerialPortException(Common.SERIAL_PORT_NAME_EX_STR + serialPort.getSystemPortName());
		}
		serialPort = SerialPort.getCommPort(portName);

		serialPort.setComPortParameters(
				Common.BAUDRATE_115200,
				Common.DATABITS_8,
				SerialPort.ONE_STOP_BIT,
				SerialPort.NO_PARITY);
		serialPort.setFlowControl(SerialPort.FLOW_CONTROL_DISABLED);
		
		serialPort.setComPortTimeouts(SerialPort.TIMEOUT_NONBLOCKING, 500, 1000);
		
		if (!serialPort.openPort()) {
	        throw new SerialPortException(Common.SERIAL_PORT_OPEN_EX_STR + serialPort.getSystemPortName());
	    }
		
		System.out.println("Port: "+portName+" opened");

		flashUpdater.dispatchEvent(Common.UpdaterEvent.WAIT, null);

		try {
			
			checkSerialPort();
			
		} catch (Exception ex) {
			throw ex;
		}

	}
	
	/**
	 * Close serial port
	 * @throws SerialPortException 
	 */
	public void closePort() throws SerialPortException {
		if(serialPort == null) return;
		
		if(serialPort.isOpen()) {
			//writeString(Common.FWUPG_CLOSE_OPCODE, Common.FWUPG_CLOSE_CMD);

			System.out.println("Closing port...");
			
			if (!serialPort.closePort()) {
		        throw new SerialPortException(Common.SERIAL_PORT_CLOSE_EX_STR + serialPort.getSystemPortName());
		    }
			serialPort = null;
			//System.out.println("Port closed post wait");

			motherBoardIsChecked = false;
			flashUpdater.dispatchEvent(Common.UpdaterEvent.PORT_CLOSED, null);
				
		}
	}

	/** Create a SwingWorker instance to run a compute-intensive task */
	final class UpdaterWorker extends SwingWorker<Void, String> {

    	 /** Schedule a compute-intensive task in a background thread */
        @Override
        protected Void doInBackground() throws Exception {
        	try {
	        	// Sync w/ GNSS device
	        	publish(Common.SYNC_STEP_STR);
				System.out.println("Starting fwupgSync");
				fwupgSync();
				System.out.println("fwupgSync ended.");
				
				publish("Starting communication");
				int numWritten;
				Thread.sleep(10);
				numWritten=serialPort.writeBytes(Common.FWUPG_START_COMM, 1, 0);
				if (numWritten != 1)
					throw new Exception("For some reason it has wrote " + numWritten + " bytes.");
				byte[] ackBuffer = new byte[1];
				while (serialPort.bytesAvailable()<=0);
				int numRead = serialPort.readBytes(ackBuffer, 1);
				
				if ((numRead == 1) && (ackBuffer[0] == Common.FWUPG_DEVICE_ACK[0])) {
					System.out.println("Ack recieved");
				}
				else {
					throw new Exception("Ack not recieved, read " + numRead + " instead.");
				}
				
				publish("Send Image Option");
				System.out.println("Sending image option");
				sendImgOption();
				System.out.println("Acks recieved");
				
				publish("Ready to load!");
				
				// Send buffers containing Img Bin chunks
				System.out.println("Starting fwupgSendBuffer");
				fwupgSendBuffer();
				System.out.println("fwupgSendBuffer ended.");

	        	// Check CRC
	        	publish(Common.CHECK_CRC_STR);
				System.out.println("Checking CRC");
				fwupgCheckCRC();
				System.out.println("CRC checked");
				
	        } catch (Exception ex) {
	        	Common.showMessage(Common.buildExMessage(Common.FWUPG_FAILURE_STR, ex), JOptionPane.ERROR_MESSAGE);
			}

        	System.out.println("doInBackground complete");
        	publish("Done");
        	return null;
        }

		/** Run in event-dispatching thread to process intermediate results
        sent from publish(). */
        @Override
        protected void process(java.util.List<String> strings) {
            // Get the latest result from the list
            String latestResult = strings.get(strings.size() - 1);
            
            flashUpdater.dispatchEvent(Common.UpdaterEvent.UPDATING, latestResult);
         }
        
        /** Run in event-dispatching thread after doInBackground() completes */
        @Override
        protected void done() {
        	endUpdateProcess();
        }

    };

	/** Initialize the Update FW process
	 * @param imgFile
	 * @param uFunction
	 * @throws IOException
	 */
	public void initUpdateProcess(Object imgFile, UpdaterFunction uFunction) throws IOException {
		try {
			/* Read Image File */
			vUpdater.readDataFromFile(imgFile, uFunction);

		} catch (IOException ioe) {
			throw ioe;
		}
		uw = new UpdaterWorker();
		uw.execute();

	}
	
	/** Start the synchronization step
	 *  
	 * @throws Exception
	 */
	private void fwupgSync() throws Exception {		
		try {
			syncGNSSDevice();

		} catch (Exception ex) {
			throw ex;
		}		
	}

	/** Synchronize with the GNSS
	 * 
	 *  Due to timing constraints a single packet is sent
	 *  including the following fields:
	 *  
	 *  1. Sync Identifier bytes
	 *  2. Start Communication byte
	 *  3. Image Info bytes
	 *  4. Flasher Ready byte
	 *  
	 * @throws Exception
	 */
	private void syncGNSSDevice() throws Exception {
		//boolean ready = false;
		Thread tr = new Thread() {
			public void run() {
				boolean sync = false;
				while (!sync) {
					try {
						sync = fwupgWaitsync(10);
					} catch (Exception e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
				}
			}
		};
		tr.start();
		while (tr.isAlive()) {
			writeBytes(Common.FWUPG_NEW_SYNC_OPCODE, null);
			Thread.sleep(5);
		}
		vUpdater.setUpdaterStatus(Common.UpdaterStatus.SYNC_COMPLETE);
	}
	
	@SuppressWarnings("unused")
	private void startCommunication() throws Exception {
		serialPort.writeBytes(Common.FWUPG_START_COMM, 1);
		try {
			fwupgWaitack(Common.SYNC_ACK_TIMEOUT);
			vUpdater.setUpdaterStatus(Common.UpdaterStatus.COMM_STARTED);

		} catch (Exception ex) {
			ex.printStackTrace();
			throw new Exception(Common.GNSS_NOT_SYNCED_EX_STR);
		}
	}
	
	private void sendImgOption() throws Exception {
		boolean ready = false;
		writeBytes(Common.FWUPG_IMGINFO_OPCODE, null);
		Thread.sleep(100);

		serialPort.writeBytes(Common.FWUPG_FLASHER_READY, 1);
		for (int i = 0; i < 3; i++) {
			while (!ready) {
				try {
					ready = fwupgWaitack(Common.SYNC_ACK_TIMEOUT);
					System.out.println(i);

				} catch (Exception ex) {
					throw ex;
				}
			}
			ready = false;
		}

		vUpdater.setUpdaterStatus(Common.UpdaterStatus.SYNC_COMPLETE);
	}
	

	@SuppressWarnings("unused")
	private void fwupgStartComm() throws Exception {
		boolean acked = false;
		int trials = 0;
		while ((!acked) && (trials < 6)) {
			writeBytes(Common.FWUPG_COMM_OPCODE, Common.FWUPG_START_COMM);
			try {
				fwupgWaitack(6000/*FIXME: replace w/ MACRO */);
			} catch (Exception ex) {							
				if (trials == 5) {
					throw new Exception("Communication not started");
				}
				else {
					trials++;
					continue;
				}
			}
			
			try {
				fwupgWaitack(6000/*FIXME: replace w/ MACRO */);
			} catch (Exception ex) {							
				if (trials == 5) {
					throw new Exception("Communication not started");
				}
				else {
					trials++;
					continue;
				}
			}
			
			try {
				fwupgWaitack(6000/*FIXME: replace w/ MACRO */);
			} catch (Exception ex) {							
				if (trials == 5) {
					throw new Exception("Communication not started");
				}
				else {
					trials++;
					continue;
				}
			}
			
			try {
				fwupgWaitack(6000/*FIXME: replace w/ MACRO */);
			} catch (Exception ex) {							
				if (trials == 5) {
					throw new Exception("Communication not started");
				}
				else {
					trials++;
					continue;
				}
			}
			acked = true;
		}
	}

	@SuppressWarnings("unused")
	private void fwupgImgInfo() throws Exception {
		writeBytes(Common.FWUPG_IMGINFO_OPCODE, null);
	}

	@SuppressWarnings("unused")
	private void fwupgFlasherReady() throws Exception {
		writeBytes(Common.FWUPG_FLASHER_READY_OPCODE, Common.FWUPG_FLASHER_READY);
		try {
			fwupgWaitack(61500/*FIXME: replace w/ MACRO */);

		} catch (Exception ex) {
			throw new Exception("Flasher Ready, Device Init or Program flash erase failed!");
		}
	}

	/** Send buffers containing Img Bin chunks
	 *  Wait a while (1000 ms) before sending each chunk
	 *  
	 * @throws Exception
	 */
	private void fwupgSendBuffer() throws Exception {
		Thread.sleep(Common.CHUNK_DELAY);
		
		while(vUpdater.getUpdaterStatus() != Common.UpdaterStatus.PROGRAM_COMPLETE) {

			// Send the chunk
			sendChunk(Common.FWUPG_SEND_BUF_OPCODE, null);

			try {
				fwupgWaitack(Common.CHUNK_ACK_TIMEOUT);
				Thread.sleep(Common.CHUNK_DELAY);
				
			} catch (Exception ex) {
				throw new Exception(Common.CHUNK_SENDING_EX_STR);
			}
		}
	}
	
	/**
	 * Finalize the Upgrading process sending an EOF
	 * and waiting for CRC check completion
	 * 
	 * @throws Exception
	 */
	private void fwupgCheckCRC() throws Exception {
		//writeBytes(Common.FWUPG_EOF_OPCODE, null);
		try {
			// Check CRC
			fwupgWaitack(Common.CRC_ACK_TIMEOUT);		
		} catch (Exception ex) {
			throw new Exception(Common.CRC_EX_STR);
		}
	}

	/**
	 * At the end of the Upgrading process
	 * wait for the new FW version string
	 */
	public void endUpdateProcess() {

        flashUpdater.dispatchEvent(Common.UpdaterEvent.UPDATE_DONE, "Update Done");
        
	}

	/**
	 * Wait for an ack from GNSS device
	 * @param timeout
	 * @throws Exception
	 */
	private boolean fwupgWaitack(int timeout) throws Exception {
		byte[] ackBuffer = new byte[Common.FWUPG_DEVICE_ACK.length];
		boolean acked = false;

		long startTime = System.currentTimeMillis(); //fetch starting time
		
		while(serialPort.bytesAvailable()<=0) {
			if((System.currentTimeMillis()-startTime)>10000) {
				throw new Exception("Timed out");
			}
		}
		
		int numRead = serialPort.readBytes(ackBuffer, Common.FWUPG_DEVICE_ACK.length);
		if (numRead > 0) {

			for(int i = 0; i < ackBuffer.length; i++){
				System.out.println(String.format("ackBuffer: %02X ", ackBuffer[i]));
			}
			if(Arrays.equals(ackBuffer, Common.FWUPG_DEVICE_ACK)) {
				acked = true;
			}
		}

		return acked;
		
	}
	
	private boolean fwupgWaitsync(int timeout) throws Exception {
		byte[] syncBuffer = new byte[1];
		byte[] trashBuffer = new byte[10000];
		boolean synced = false;
		
		if (serialPort.bytesAvailable() <= 0)
			return synced;

		int numRead = 0;
		System.out.println("Bytes available: " + serialPort.bytesAvailable());
		
		numRead = serialPort.readBytes(syncBuffer, 1);
		
		if (numRead > 0) {

			for(int i = 0; i < syncBuffer.length; i++){
				System.out.println(String.format("syncBuffer: %02X ", syncBuffer[i]));
			}
			if((syncBuffer[0] == Common.FWUPGSYNC[0])) {
				serialPort.readBytes(trashBuffer, 3);
				synced = true;
				for (int i = 0; i<3; i++)
					synced = (synced && (trashBuffer[i] == Common.FWUPGSYNC[i+1]));
			}
		}

		return synced;
		
	}

	@SuppressWarnings("unused")
	private void sendFwupgCmd() throws Exception {
		writeString(Common.FWUPG_NMEACMD_OPCODE, Common.FWUPG_NMEACMD);

		try {
			checkGNSSDevice();

		} catch (Exception ex) {
			throw ex;
		}
	}

	/**
	 * Wrapper to build the string to be written to serial port
	 * @param opcode
	 * @param params
	 */
	private void writeString(int opcode, String params) {
		if(serialPort == null) return;
		
		byte[] command = vUpdater.buildCmdPkt(opcode, params);
		
		if(command!=null) {
			write(command);
		}
	}
	
	/**
	 * Wrapper to build the bytes composing a chunk
	 * and update the user info box accordingly 
	 * @param opcode
	 * @param params
	 */
	private void sendChunk(int opcode, byte[] params) {
		if(serialPort == null) return;
		 
		StringBuffer str = new StringBuffer(Common.SEND_CHUNK_STR+Common.SPACE_STR);
		byte[] command = vUpdater.buildCmdPkt(opcode, params);
		int cSize = 100;
		int index = command.length / cSize;
		
		
		if(command!=null) {
			int bytesWritten=0;
			for (int i = 0; i < index; i++) {
				bytesWritten+=serialPort.writeBytes(command, cSize, cSize*i);
				try {
					Thread.sleep(20);
				} catch (InterruptedException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
			bytesWritten+=serialPort.writeBytes(command, command.length % cSize, cSize*index);
			
			if(bytesWritten != command.length) {
				System.out.println("Num written: " + bytesWritten);
				Common.showMessage(Common.SERIAL_PORT_WRITE_EX_STR, JOptionPane.ERROR_MESSAGE);
			}
			
			for (int i=Common.PAYLOAD_OFFSET; i<(Common.PAYLOAD_OFFSET+Common.CHUNK_SAMPLE_LEN); i++) {
				str.append(String.format("%02X ", command[i]));
			}
			str.append(Common.TRAILER_STR);
			flashUpdater.dispatchEvent(Common.UpdaterEvent.UPDATING, str.toString());
		}
		
	}
	
	/**
	 * Wrapper to build the bytes to be written to serial port
	 * @param opcode
	 * @param params
	 */
	private void writeBytes(int opcode, byte[] params) {
		if(serialPort == null) return;
		
		byte[] command = vUpdater.buildCmdPkt(opcode, params);
		
		if(command!=null) {
			write(command);			
		}
	}

	/**
	 * Write bytes to serial port
	 * @param command
	 */
	private void write(byte[] command) {
		int numWritten = 0;
		/*for (int i = 0; i < command.length; i++) {
			numWritten += serialPort.writeBytes(command, 1, i);
		}*/
		numWritten = serialPort.writeBytes(command, command.length);
		//System.out.println("Command length :" + command.length);
		if(numWritten != command.length) {
			System.out.println("Num written: " + numWritten);
			Common.showMessage(Common.SERIAL_PORT_WRITE_EX_STR, JOptionPane.ERROR_MESSAGE);
		}
	}

	/**
	 * Check Serial Port
	 * @throws Exception
	 */
	private void checkSerialPort() throws Exception {
		
    	motherBoardIsChecked = true;
    	flashUpdater.dispatchEvent(Common.UpdaterEvent.PORT_OPENED, "Ready for update");
    	
        if(!motherBoardIsChecked) {
        	throw new Exception(Common.NUCLEO_SERIAL_PORT_EX);
        }

	}

	public boolean fwNeedsUpdate() {
		int versionChar = Character.getNumericValue(versionString.charAt(19));

		if(versionChar<=Common.LATEST_FW_VERSION_NUMBER) {
			return false;
		}
		return true;
	}
	
	/** Not used by now */
	private void checkGNSSDevice() throws Exception {
		throw new Exception("checkGNSSDevice Not used by now!!");
	}

}
