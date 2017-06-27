package com.z012.chengdu.sc.tools;

import java.util.Comparator;

import com.z012.chengdu.sc.net.bean.WeatherCityInfo;

public class PinyinComparator implements Comparator<WeatherCityInfo> {

	public int compare(WeatherCityInfo o1, WeatherCityInfo o2) {
		// 这里主要是用来对ListView里面的数据根据ABCDEFG...来排序
		if (o2.fl.equals("#")) {
			return -1;
		} else if (o1.fl.equals("#")) {
			return 1;
		} else {
			return o1.fl.compareTo(o2.fl);
		}
	}
}