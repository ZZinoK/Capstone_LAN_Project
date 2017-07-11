#include "LPLC.h"

configuration ReceiveAppP
{
}
implementation
{	
	components SendReceiveP;
	components new AMReceiverC(AM_RADIO);
	
	SendReceiveP.CReceive -> AMReceiverC;
	

}