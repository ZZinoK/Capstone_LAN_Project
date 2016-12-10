
interface startPeriodicalRadio {
	command void startPeriodicRadio(uint16_t start, uint16_t stop);
	command void stopTimer();
	command void startCycleRadio(uint16_t time);
}