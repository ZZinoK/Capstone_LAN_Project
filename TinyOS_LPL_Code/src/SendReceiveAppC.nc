#include "LPLC.h"
configuration SendReceiveAppC
{
}
implementation
{
	components SendAppP;
	components LPLC;
	components SendReceiveP;
	components shareFlagC;
	
	LPLC.AMPacket -> SendReceiveP.AMPacketP;
	LPLC.AMSend -> SendReceiveP.AMSendP;
	LPLC.AMControl -> SendReceiveP.AMControlP;
	LPLC.Packet -> SendReceiveP.PacketP;
	LPLC.SPradio -> SendReceiveP.SPradioP;
	LPLC.Ack -> SendReceiveP.AckP;
	components ReceiveAppP;
	
	LPLC.Receive -> SendReceiveP.PReceive;
	LPLC.shareflag -> shareFlagC;
}