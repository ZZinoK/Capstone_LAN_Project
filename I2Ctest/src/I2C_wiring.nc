#define NEW_PRINTF_SEMANTICS
#include "I2C.h"
#include "printf.h"

configuration I2C_wiring {}
implementation {
	components MainC, I2C_Main, LedsC;
	components new Msp430I2CC() as I2CBus;
	components new TimerMilliC() as Timer1;
	
	components PrintfC;
	components SerialStartC;
	
	I2C_Main.ReadTimer -> Timer1;
	I2C_Main.Boot -> MainC;
	I2C_Main.I2CPacket -> I2CBus;
	I2C_Main.Leds -> LedsC;
	I2C_Main.Resource -> I2CBus; 
}