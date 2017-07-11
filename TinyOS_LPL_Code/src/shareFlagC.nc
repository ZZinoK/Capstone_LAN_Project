
configuration shareFlagC {
	      provides interface shareFlag;
}

implementation {
	      components shareFlagP;

	      shareFlag = shareFlagP;
} 