configuration LEDAppP
{
}
implementation
{
  components LEDP as PLED;
  components LedsC;

  PLED.CLeds -> LedsC;
}