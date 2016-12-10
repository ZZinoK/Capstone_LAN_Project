#define NEW_PRINTF_SEMANTICS
#include "printf.h"

configuration LEDAppC
{
}
implementation
{
  	components LEDAppP;
  	components SendReceiveAppC;
  	components I2C_wiring;
  	components LEDP;
  	components MainC;
  	components LPLC;
	components new TimerMilliC() as SensingTimer;
	components new TimerMilliC() as TimeoutTimer;
	components new TimerMilliC() as TimesetTimer;
  	components new TimerMilliC() as PktSendTimer;
  	
  	LPLC.PktSendTimer -> PktSendTimer;
  	LPLC.TimesetTimer -> TimesetTimer;
  	LPLC.SensingTimer -> SensingTimer;
  	LPLC.TimeoutTimer -> TimeoutTimer;
	LPLC.Leds -> LEDP.PLeds;
	LPLC.Boot -> MainC.Boot;
	
    components PrintfC;
    components SerialStartC;
}