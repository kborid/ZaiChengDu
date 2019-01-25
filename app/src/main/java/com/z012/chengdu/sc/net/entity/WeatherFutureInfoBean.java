package com.z012.chengdu.sc.net.entity;

public class WeatherFutureInfoBean {
	public ASTRO astro;
	public COND cond;
	public String date;
	public String hum;
	public String pcpn;
	public String pop;
	public String pres;
	public TMP tmp;
	public String vis;

	public class ASTRO {
		public String sr;
		public String ss;
	}

	public class COND {
		public String code_d;
		public String code_n;
		public String txt_d;
		public String txt_n;
	}

	public class TMP {
		public String max;
		public String min;
	}

	@Override
	public String toString() {
		return "[" + "astro1 = " + astro.sr + "astro2 = " + astro.ss
				+ "cond1 = " + cond.code_d + "cond2 = " + cond.code_n
				+ "cond3 = " + cond.txt_d + "cond4 = " + cond.txt_n + "date = "
				+ date + "hum = " + hum + "pcpn = " + pcpn + "pop = " + pop
				+ "pres = " + pres + "tmp1 = " + tmp.max + "tmp2 = " + tmp.min
				+ "vis = " + vis + "]";
	}
}
