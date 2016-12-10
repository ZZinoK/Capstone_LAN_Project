#include "Timer.h"
#include "Leds.h"
#include "LPLC.h"
#include "I2C.h"
#include "math.h"
#define SLAVEADDRESS	0x68
#define PI       		3.14159265358979323846
#define Target_Node		4

module LPLC {
	uses interface AMPacket;
	uses interface Leds;
	uses interface Boot;
	uses interface Timer<TMilli> as SensingTimer;
	uses interface Timer<TMilli> as TimeoutTimer;
	uses interface Timer<TMilli> as TimesetTimer;
	uses interface Timer<TMilli> as PktSendTimer;
	uses interface AMSend;
	uses interface Receive;
	uses interface Packet;
	uses interface SplitControl as AMControl;
	uses interface startPeriodicalRadio as SPradio; // Adaptive(Dynamic) period Radio
	uses interface PacketAcknowledgements as Ack;
	uses interface shareFlag as shareflag;
	uses interface I2CPacket<TI2CBasicAddr>;
	uses interface Resource;
}
implementation
{
	///////////// I2C Communication & Complementary Filter /////////////////
	uint16_t ActiveSensor = 0x006B;
	uint8_t ActiveRead = 0x3B;
	
	uint8_t ReadData[14];
	int16_t acX, acY, acZ, tmp, gyX, gyY, gyZ;
	double pitch = 0, roll = 0, yaw = 0;
	double before_pitch = 0, before_roll = 0, before_yaw = 0;
	int16_t pkt_p = 0, pkt_r=0, pkt_y = 0;
	double deg_x, deg_y, deg_z, ac_X, ac_Y, ac_Z, gy_X, gy_Y, gy_Z;
	double dgy_x, dgy_y, dgy_z;
	
	bool Wake_up = FALSE; 
	uint8_t Variation = 1;
	int8_t SELECT = 0;
	enum {
		INIT = 0,
		READ_ACTIVE = 1,
		READ = 2,
	};
	
	////////////////////With Reference to Send-Operation///////////////////
	uint8_t pkt_counter = 0;
	bool SendDone_flag = FALSE;
	message_t pkt;
	
	////////////////////Function Statement///////////////////////////
	void Send();
	void printfFloat(double toBePrinted);
	void comparisonOperation(double p, double r, double y, double before_pitch, double before_roll, double before_yaw);
	void I2C_Init();
	
	event void Boot.booted() {
		I2C_Init();
		call SPradio.startPeriodicRadio(1000,20);
		call TimeoutTimer.startOneShot(1000);
		call SensingTimer.startPeriodic(50);
		call PktSendTimer.startPeriodic(5000);
	}

	event void SensingTimer.fired() {		
		call Resource.request();
	}
	
	event void PktSendTimer.fired(){
		Send();	
	}
	
	
	event void TimeoutTimer.fired(){
		call SensingTimer.stop();
		call TimesetTimer.startOneShot(1000);
	}
	
	event void TimesetTimer.fired(){
		call SensingTimer.startPeriodic(50);
		call TimeoutTimer.startOneShot(1000);
	}
	
	void Send() {
		LPLMsg* LPLmsg = (LPLMsg*)(call Packet.getPayload(&pkt, sizeof(LPLMsg)));
		if(call shareflag.What_is_myPeriod() > 900)
			call shareflag.This_is_myPeriod(call shareflag.What_is_myPeriod()-500);
		else
			call shareflag.This_is_myPeriod(500);
		
		if(LPLmsg == NULL) { return; }
		
		printf("Sendpkt Info. Node ID:%d, Route:%d, Pitch:%d, Roll:%d, Yaw:%d, Variation:%d\n",TOS_NODE_ID,TOS_NODE_ID,(int16_t)pitch,(int16_t)roll,(int16_t)yaw,Variation);
    	printfflush();
    	
		LPLmsg -> nodeid = (uint8_t)TOS_NODE_ID;
		LPLmsg -> route = (uint8_t)TOS_NODE_ID;
		LPLmsg -> pitch = (int16_t)pitch;
		LPLmsg -> roll = (int16_t)roll;
		LPLmsg -> yaw = (int16_t)yaw;
		LPLmsg -> variation = Variation; 
		
		call AMSend.send(Target_Node, &pkt, sizeof(LPLMsg));
		pkt_counter++;
		
		Variation = 1;
		return;		
	}
	
	void I2C_Init(){
		SELECT = INIT;
		call Resource.request();
	}	
	
	event void Resource.granted()  {
		switch(SELECT) {
			case INIT :
				if(call I2CPacket.write(I2C_START|I2C_STOP, SLAVEADDRESS, 2, (uint8_t*)(&ActiveSensor)) == FAIL)
					call Resource.request();
			
				atomic SELECT = READ_ACTIVE;	
				break;
					
			case READ_ACTIVE :
				if(call I2CPacket.write(I2C_START|I2C_STOP, SLAVEADDRESS, 1, &ActiveRead) == FAIL){
					call Resource.request();
				}
				
				atomic SELECT = READ;
				break;
					
			case READ :
				if(call I2CPacket.read(I2C_START|I2C_STOP, SLAVEADDRESS, 14, ReadData) == FAIL)
					call Resource.request();

				atomic SELECT = READ_ACTIVE;
				break;				
			
			default :
				call Resource.release();
				
		}
  	}
  	
  	async event void I2CPacket.readDone(error_t error, uint16_t addr, uint8_t length, uint8_t *data){
		int i;
				
		
			for(i=0; i<14; i++)
				ReadData[i] = *(data+i);
				
			acX = ReadData[0] << 8 | ReadData[1]; // 0x3B (ACCEL_XOUT_H) & 0x3C (ACCEL_XOUT_L)
			acY = ReadData[2] << 8 | ReadData[3]; // 0x3D (ACCEL_YOUT_H) & 0x3E (ACCEL_YOUT_L)
			acZ = ReadData[4] << 8 | ReadData[5]; // 0x3F (ACCEL_ZOUT_H) & 0x40 (ACCEL_ZOUT_L)
			tmp = ReadData[6] << 8 | ReadData[7]; // 0x41 (TEMP_OUT_H) & 0x42 (TEMP_OUT_L)
			gyX = ReadData[8] << 8 | ReadData[9]; // 0x43 (GYRO_XOUT_H) & 0x44 (GYRO_XOUT_L)
			gyY = ReadData[10] << 8 | ReadData[11]; // 0x45 (GYRO_YOUT_H) & 0x46 (GYRO_YOUT_L)
			gyZ = ReadData[12] << 8 | ReadData[13]; // 0x47 (GYRO_ZOUT_H) & 0x48 (GYRO_ZOUT_L)
				
		call Resource.release();
		
		deg_x = atan2(acX, acZ) * 180 / PI ;  //rad to deg
		deg_y = atan2(acY, acZ) * 180 / PI ;
		deg_z = atan2(acX, acY) * 180 / PI ;
		
		dgy_x = gyY / 131.;  //16-bit data to 250 deg/sec
		dgy_y = gyZ / 131.;
		dgy_z = gyX / 131.;
		
		before_pitch = pitch, before_roll = roll, before_yaw = yaw;
		pitch = (0.9 * (pitch + (dgy_x * 0.001))) + (0.1 * deg_x) ; //complementary filter
		roll = (0.9 * (roll + (dgy_y * 0.001))) + (0.1 * deg_y);
		yaw = (0.9 * (yaw + (dgy_z * 0.001))) + (0.1 * deg_z);
		
		printfFloat(pitch);printfFloat(roll);printfFloat(yaw);
		printf("\n");
		printfflush();
		comparisonOperation(pitch, roll, yaw, before_pitch, before_roll, before_yaw);
	}
	
	void comparisonOperation(double p, double r, double y, double bp, double br, double by) {
		int16_t comp_p = 0, comp_r = 0, comp_y = 0;
		if((p != 0) && (r != 0) && (y != 0) && (bp != 0) && (br != 0) && (by != 0)){
			comp_p = (int16_t)(bp - p); comp_r = (int16_t)(br - r); comp_y = (int16_t)(by - y);
			
			if((abs(comp_p) >= 3) || (abs(comp_r) >= 3) || (abs(comp_y) >= 3)){
				Variation = 3;
				Send();	
			}
			else if((abs(comp_p) >= 2) || (abs(comp_r) >= 2) || (abs(comp_y) >= 2)){
				Variation = 2;
				Send();
			}
			else
				Variation = 1;
		}
	}
	
	void printfFloat(double toBePrinted) {
    	uint32_t fi, f0, f1, f2;
    	char c;
    	double f = toBePrinted;

    	if (f<0){
    		c = '-'; f = -f;
    	} else {
       		c = ' ';
       	}

     	// integer portion.
     	fi = (uint32_t) f;

     	// decimal portion...get index for up to 3 decimal places.
     	f = f - ((float) fi);
     	f0 = f*10;   f0 %= 10;
     	f1 = f*100;  f1 %= 10;
     	f2 = f*1000; f2 %= 10;
    	
    	printf("%c%ld.%d%d%d ", c, fi, (uint8_t) f0, (uint8_t) f1, (uint8_t) f2);
    	printfflush();
   	}
	
	async event void I2CPacket.writeDone(error_t error, uint16_t addr, uint8_t length, uint8_t *data){
		call Resource.release();
	}
	
	event void AMSend.sendDone(message_t *msg, error_t error){
	}
	
	event message_t * Receive.receive(message_t *msg, void *payload, uint8_t len){
		/*LPLMsg *data_pkt = (LPLMsg*)payload;
		call AMSend.send(Target_Node,msg,len);
    	printf("pkt    Info. Node ID : %d,   Sequence : %d,    Perioid : %d\n", data_pkt -> nodeid, data_pkt -> pktSequence, data_pkt -> myPeriod);
    	printfflush();
    	printf("Sensor Info. Pitch   : %d,   Roll     : %d,    Yaw     : %d    Variation : %s\n", data_pkt -> pitch, data_pkt -> roll, data_pkt -> yaw, data_pkt->variation?"TRUE":"FALSE");
		printfflush();*/
		return msg;
	}

	event void AMControl.startDone(error_t error){
		
	}

	event void AMControl.stopDone(error_t error){
	}
}

