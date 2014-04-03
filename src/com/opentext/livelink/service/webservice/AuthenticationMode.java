package com.opentext.livelink.service.webservice;

public enum AuthenticationMode {
	CWSAuthentication {
		public String toString() {
			return "CWS Authentication";
		}
	},
	RCSAuthenticationOTDS {
		public String toString() {
			return "RCS Authentication (OTDS)";
		}
	},
	RCSAuthenticationCAP {
		public String toString() {
			return "RCS Authentication (CAP)";
		}
	};

	public static AuthenticationMode get(String authMode) {
		if (authMode.equals(CWSAuthentication.toString())) {
			return CWSAuthentication;
		} else if (authMode.equals(RCSAuthenticationOTDS.toString())) {
			return RCSAuthenticationOTDS;
		} else if (authMode.equals(RCSAuthenticationCAP.toString())) {
			return RCSAuthenticationCAP;
		}
		return null;
	}
}
