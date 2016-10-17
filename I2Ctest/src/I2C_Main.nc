#include "printf.h"
#include "I2C.h"
#define SLAVEADDRESS 0x68

module I2C_Main{
	uses {
		interface Boot;
		interface Leds;
		interface Resource;
		interface I2CPacket<TI2CBasicAddr>;
		interface Timer<TMilli> as ReadTimer;
	}
}
implementation{
	uint8_t ActiveSensor[2] = { 0x6B, 0 };
	uint8_t ActiveSensorData[1] = { 0 };
	uint8_t ActiveRead[1] = { 0x3B };
	uint8_t ReadData[14];
	uint16_t acX, acY, acZ, tmp, gyX, gyY, gyZ;
	
	event void Boot.booted(){
		if(call Resource.request() == SUCCESS){
		 	call Leds.led0On();
		 }
	}
	
	event void Resource.granted()  {
	
	
		if(call I2CPacket.write(I2C_START|I2C_STOP, SLAVEADDRESS, 2, ActiveSensor) == SUCCESS){
			call Leds.led1On();
		}
		//if(call I2CPacket.write(I2C_STOP, SLAVEADDRESS, 1, ActiveSensorData) == SUCCESS){
			//call Leds.led1On();
		//}
		call ReadTimer.startPeriodic(1000);
 		//call Resource.release();
  	}

	async event void I2CPacket.readDone(error_t error, uint16_t addr, uint8_t length, uint8_t *data){
		// TODO Auto-generated method stub
	}

	async event void I2CPacket.writeDone(error_t error, uint16_t addr, uint8_t length, uint8_t *data){
		// TODO Auto-generated method stub
	}

	event void ReadTimer.fired(){
		if(call I2CPacket.write(I2C_START, SLAVEADDRESS, 1, ActiveRead) == SUCCESS){
			//call Leds.led2On();
		}
		if(call I2CPacket.read(I2C_START|I2C_ACK_END, SLAVEADDRESS, 14, ReadData) == SUCCESS){
			//call Leds.led0Off();
		}
		
		acX = ReadData[0] << 8 | ReadData[1]; // 0x3B (ACCEL_XOUT_H) & 0x3C (ACCEL_XOUT_L)
		acY = ReadData[2] << 8 | ReadData[3]; // 0x3D (ACCEL_YOUT_H) & 0x3E (ACCEL_YOUT_L)
		acZ = ReadData[4] << 8 | ReadData[5]; // 0x3F (ACCEL_ZOUT_H) & 0x40 (ACCEL_ZOUT_L)
		tmp = ReadData[6] << 8 | ReadData[7]; // 0x41 (TEMP_OUT_H) & 0x42 (TEMP_OUT_L)
		gyX = ReadData[8] << 8 | ReadData[9]; // 0x43 (GYRO_XOUT_H) & 0x44 (GYRO_XOUT_L)
		gyY = ReadData[10] << 8 | ReadData[11]; // 0x45 (GYRO_YOUT_H) & 0x46 (GYRO_YOUT_L)
		gyZ = ReadData[12] << 8 | ReadData[13]; // 0x47 (GYRO_ZOUT_H) & 0x48 (GYRO_ZOUT_L)
		printf("%d, %d, %d, %d, %d, %d, %d \r\n",acX, acY, acZ, tmp, gyX, gyY, gyZ);
		printfflush();	
	}
}