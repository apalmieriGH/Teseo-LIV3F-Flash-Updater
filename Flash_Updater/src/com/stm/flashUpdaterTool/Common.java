package com.stm.flashUpdaterTool;


import javax.swing.JFrame;
import javax.swing.JOptionPane;

public class Common {

	final static int UART_TIMEOUT = 6000; //ms
	final static int CLOSE_TIMEOUT = 100; //ms
	final static int VERSION_TIMEOUT = 12000; //ms
	final static int SYNC_ACK_TIMEOUT = 12000; //ms
	final static int CHUNK_DELAY = 100; //ms
	final static int CHUNK_ACK_TIMEOUT = 6000; //ms
	final static int CRC_ACK_TIMEOUT = 30000; //ms
	
	final static int PAYLOAD_OFFSET = 2;
	final static int CHUNK_SAMPLE_LEN = 5;
	final static int LATEST_FW_VERSION_NUMBER = 8;

	final static String SPACE_STR = " ";
	final static String TRAILER_STR = "...";
	final static String LINK_ERROR_STR = "Link error";
	final static String INVALID_LINK_STR = "Invalid link";
	final static String SERIAL_PORT_OPEN_EX_STR = "Unable to open port ";
	final static String SERIAL_PORT_CLOSE_EX_STR = "Unable to close port ";
	final static String SERIAL_PORT_NAME_SEPARATOR = ": ";
	final static String SERIAL_PORT_NAME_EX_STR = "Invalid port name";
	final static String SERIAL_PORT_WRITE_EX_STR = "Unable to write command";
	final static String FW_VERSION_WARN_STR = "GNSS FW already updated!";
	final static String SYNC_STEP_STR = "Syncing with GNSS, please reset the board";
	final static String SEND_CHUNK_STR = "Sending chunk:";
	final static String CHECK_CRC_STR = "Checking CRC";
	final static String FWUPG_FAILURE_STR = "Upgrade procedure failed\n\n";
	final static String FWUPG_NO_VERSION_STR = "No version got!";
	final static String GNSS_NOT_SYNCED_EX_STR = "GNSS device not synced";
	final static String CHUNK_SENDING_EX_STR = "Chunk sending failed!";
	final static String CRC_EX_STR = "Check CRC failed!";
	final static String NUCLEO_SERIAL_PORT_EX = "No motherboard detected\n\n"
												+ "Please check that:\n"
												+ "- the Nucleo board is plugged correctly\n"
												+ "- the VCOM application has been flashed\n"
												+ "- the ST-Link on Nucleo board has been upgraded to the latest version";

	final static String FWUPG_START_CMD = "START";
	final static String FWUPG_START_ACK = "FWUPGACK";
	final static String FWUPG_CLOSE_CMD = "CLOSE";
	final static String SERIAL_PORT_BORDER_STR = "New connection";
	final static String FWUPG_BUTTON_STR = "Action";

	final static String FWUPG_FW_VER = "PSTMVER,BINIMG_X.Y.Z.W_RCX_ARM";
	final static String FWUPG_NMEACMD = "$PSTMFWUPGRADE\n\r";
	final static String FWUPG_NMEACMDOK = "$PSTMFWUPGRADEOK";
	final static String FWUPG_NMEACMDKO = "$PSTMFWUPGRADEERROR";
	final static byte[] FWUPGID = {(byte)0xF4, (byte)0x01, (byte)0xD5, (byte)0xBC};
	final static byte[] FWUPGSYNC = {(byte)0x73, (byte)0x40, (byte)0x98, (byte)0x83};
	final static byte[] FWUPG_START_COMM = {(byte)0xA3};
	final static byte[] FWUPG_FLASHER_READY = {(byte)0x4A};
	final static byte[] FWUPG_DEVICE_ACK = {(byte)0xCC};

	final static byte FWUPG_START_OPCODE = 0x00;
	final static byte FWUPG_NMEACMD_OPCODE = 0x01;
	final static byte FWUPG_SYNC_OPCODE = 0x02;
	final static byte FWUPG_COMM_OPCODE = 0x03;
	final static byte FWUPG_IMGINFO_OPCODE = 0x04;
	final static byte FWUPG_FLASHER_READY_OPCODE = 0x05; 
	final static byte FWUPG_SEND_BUF_OPCODE = 0x06;
	final static byte FWUPG_EOF_OPCODE = 0x07;
	final static byte FWUPG_NEW_SYNC_OPCODE = 0x08;
	final static byte FWUPG_START_COM_OPCODE = 0x09;
	final static byte FWUPG_CLOSE_OPCODE = (byte)0xFF;
	final static short FWUPG_PKT_LENTGH = 2;
	
	final static int FWUPG_FLASHER_CHUNKSIZE = (16*1024);
	//FIXME: 53 = 1(SYNC STEP)+51(800K/16k)+1(CRC STEP) --- Compute at run time
	//By now File size = ~800K; Chunk size = ~16k
	final static int FW_UPDATE_CYCLES = 57;

	final static String UI_TITLE = "GNSS FW Upgrader";
	final static String UI_TITLE_VER_STR = UI_TITLE/*+" v2.0.0"*/;
	final static String SERIAL_PORT_LABEL_STR = "Port:";
	final static String SERIAL_PORT_OPEN_STR = "Open";
	final static String SERIAL_PORT_CLOSE_STR = "Close";
	final static String USER_INFO_DEFAULT_STR = "Ready";
	final static String FW_UPDATE_ADV_STR = "Advanced...";
	final static String FW_UPDATE_BUTTON_STR = "Update FW >>>";
	final static String FW_UPDATE_LABEL_STR = "Updating...";
	final static String FW_UPDATE_CONFIRM_STR = "FW will be updated. Continue?";
	final static String FILE_READ_FAILURE_STR = "Image file coudn't be read";
	final static String FW_UPDATE_DONE_STR = "Job completed";
	final static String GNSS_HW = " GNSS HW ";
	final static String GNSS_FW = " GNSS FW ";
	
	final static String ST_LOGO_PATH = "/st_logo.png";
	final static String ST_ICON_PATH = "/st_icon.png";
	final static String TOOLS_ICON_PATH = "/tools.jpg";
	//final static String GNSS_FW_FILE_PATH = "/STA8090_4_5_5_LIV_UPG26.bin";
	//final static String GNSS_FW_FILE_PATH = "/STA8090_4_5_7_LIV_UPG26.bin";
	//final static String GNSS_FW_FILE_PATH = "/STA8090_4_6_8_LIV_CFG_v6_SMPS_UPG26.bin";
	//final static String GNSS_FW_FILE_PATH = "/STA8090_4_6_8_1_UPG26.bin";
	final static String GNSS_FW_FILE_PATH = "/STA8090_4_6_8_2_RC3_UPG26.bin";
	//final static String GNSS_FW_FILE_PATH = "/sta8090_SQI_AGNSS_UPG.bin";
	final static String GNSS_FW_UPDATE_WARNING = "Please, don't unplug the motherboard while updating!";
	
	public static final int BAUDRATE_115200 = 115200;
	public static final int DATABITS_8 = 8;
	
	public enum Status {
		SUCCESS,
		FAILURE
	}

	public enum ControlStatus {
		P_OPENED,
		P_CLOSED
	}

	public enum UpdaterStatus {
		INIT,
		SYNC_COMPLETE,
		COMM_STARTED,
		PROGRAMMING,
		PROGRAM_COMPLETE,
		CRC_COMPLETE,
		ERROR
	}
	
	public enum UpdaterEvent {
		PORT_OPENED,
		PORT_CLOSED,
		WAIT,
		UPDATING,
		UPDATE_DONE
	}
	
	public enum UpdaterFunction {
		STACK
	}
	
	/**
	 * Utility to build error message string
	 * @param str
	 * @param ex
	 * @return
	 */
	public static String buildExMessage(String str, Exception ex) {
		return str + "("+ex.getMessage()+")";
	}
	
	/**
	 * Utility for message pop-up
	 * @param message
	 * @param type
	 */
	public static void showMessage(Object message, int type) {
		JOptionPane.showMessageDialog(new JFrame(),
									message,
									UI_TITLE,
									type);
	}
	
	/**
	 * Utility for confirmation message pop-up
	 * @param message
	 * @param type
	 * @return
	 */
	public static int showConfirm(Object message, int type) {
	
		int option = JOptionPane.showConfirmDialog(new JFrame(),
											message,
											UI_TITLE,
											type);
		
		return option;
	}
}
