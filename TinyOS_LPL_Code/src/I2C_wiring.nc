#include "I2C.h"

configuration I2C_wiring {}
implementation {
	components LPLC;
	components new Msp430I2CC() as I2CBus;
	
	LPLC.I2CPacket -> I2CBus;
	LPLC.Resource -> I2CBus; 
}