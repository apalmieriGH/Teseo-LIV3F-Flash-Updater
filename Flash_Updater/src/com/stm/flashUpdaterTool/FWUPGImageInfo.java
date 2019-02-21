package com.stm.flashUpdaterTool;

public class FWUPGImageInfo {
	
	private int[] uartBaudRate = {
		57600,
		115200,
		230400,
		460800,
		921600
	};
	private byte baudRateIndex = (byte)uartBaudRate[1];
	
	private byte eraseNVM;
	private byte programOnly;
	private byte dump;
	private byte baudRate;
	private int firmwareSize;
	private int firmwareCRC;
	private int nvmAddressOffset;
	private int nvmSize;
	
	public byte getBaudRateIndex() {
		return baudRateIndex;
	}
	
	public void setBaudRateIndex(int baudRate) {
		
		for(short i=0; i<uartBaudRate.length; i++) {
			if(baudRate == uartBaudRate[i]) {
				baudRateIndex = (byte)i;
				break;
			}
		}
		System.out.println("baudRateIndex " + baudRateIndex);
	}
	
	public byte getEraseNVM() {
		return eraseNVM;
	}
	public void setEraseNVM(byte eraseNVM) {
		this.eraseNVM = eraseNVM;
	}
	public byte getProgramOnly() {
		return programOnly;
	}
	public void setProgramOnly(byte programOnly) {
		this.programOnly = programOnly;
	}
	public byte getDump() {
		return dump;
	}
	public void setDump(byte dump) {
		this.dump = dump;
	}
	public byte getBaudRate() {
		return baudRate;
	}
	public void setBaudRate(byte baudRate) {
		this.baudRate = baudRate;
	}
	public int getFirmwareSize() {
		return firmwareSize;
	}
	public void setFirmwareSize(int firmwareSize) {
		this.firmwareSize = firmwareSize;
	}
	public int getFirmwareCRC() {
		return firmwareCRC;
	}
	public void setFirmwareCRC(int firmwareCRC) {
		this.firmwareCRC = firmwareCRC;
	}
	public int getNvmAddressOffset() {
		return nvmAddressOffset;
	}
	public void setNvmAddressOffset(int nvmAddressOffset) {
		this.nvmAddressOffset = nvmAddressOffset;
	}
	public int getNvmSize() {
		return nvmSize;
	}
	public void setNvmSize(int nvmSize) {
		this.nvmSize = nvmSize;
	}

	public FWUPGImageInfo (int firmwareSize, int firmwareCRC) {
		setEraseNVM((byte) 0);
		setProgramOnly((byte) 0);
		setDump((byte) 0);
		setBaudRate(baudRateIndex);
		setFirmwareSize(firmwareSize);
		setFirmwareCRC(firmwareCRC);
		setNvmAddressOffset(0x100000);
		setNvmSize(0x100000);
	}
	
	public int getImgInfoSize() {
		return (4*Byte.BYTES+4*Integer.BYTES);
	}
}
