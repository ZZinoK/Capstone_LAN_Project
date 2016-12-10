#include "LPLC.h"
configuration SendAppP
{
}
implementation
{	
	components SendReceiveP;
	components new AMSenderC(AM_RADIO);
	components ActiveMessageC;
	components new TimerMilliC() as Timer1;
	components new TimerMilliC() as Timer2;
	
	components startPeriodicalRadioC;
	components shareFlagC;
  	components LedsC;
	components CC2420ControlC;
	
	SendReceiveP.CC2420Config -> CC2420ControlC;
  	SendReceiveP.Leds -> LedsC;

	SendReceiveP.waitForData -> Timer1;  	
	SendReceiveP.OffTimer -> Timer2;
	
	SendReceiveP.Packet -> AMSenderC;
	SendReceiveP.AMControl -> ActiveMessageC;
	SendReceiveP.AMPacket -> AMSenderC;
	SendReceiveP.AMSendC -> AMSenderC;
	SendReceiveP.Ack -> ActiveMessageC;
	
	SendReceiveP.SPradio -> startPeriodicalRadioC;
	SendReceiveP.shareflag -> shareFlagC;
}