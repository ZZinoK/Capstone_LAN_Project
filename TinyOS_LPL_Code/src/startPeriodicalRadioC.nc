
configuration startPeriodicalRadioC {
	      provides interface startPeriodicalRadio;
}

implementation {
	      components startPeriodicalRadioP;
	      components new TimerMilliC() as Timer0;
	      components new TimerMilliC() as Timer1;
	      components new TimerMilliC() as Timer2;
	      components ActiveMessageC;
	      components shareFlagC;
	      	      
	      startPeriodicalRadio = startPeriodicalRadioP;
	      startPeriodicalRadioP.PeriodicTimer -> Timer0;
	      startPeriodicalRadioP.PeriodicTimer2 -> Timer1;
	      startPeriodicalRadioP.CycleTimer -> Timer2;
	      startPeriodicalRadioP.AMControlSPR -> ActiveMessageC;
	      startPeriodicalRadioP.shareflag -> shareFlagC;
} 