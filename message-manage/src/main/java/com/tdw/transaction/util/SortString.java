package com.tdw.transaction.util;

import java.util.Arrays;

public class SortString {

	// public static void main(String[] args) {
	// /**
	// * 将字符串中的字符进行排序 1.将字符串转成数组 2.对数组进行排序 3.将数组转成字符串
	// */
	// String str = "badcafx";
	// String sortstr = sortString(str);
	// System.out.println(sortstr);
	//
	// }

	/**
	 * 对字符串中的字符进行排序，然后返回排好的字符串
	 * 
	 * @param str
	 * @return
	 */
	public static String sortString(String str) {
		char[] chs = stringToArray(str);
		sort(chs);
		String ch = arrayToString(chs);
		return ch;
	}

	/*
	 * 将数组转成字符串
	 */
	private static String arrayToString(char[] chs) {
		return new String(chs);
	}

	/*
	 * 对数组进行排序
	 */
	private static void sort(char[] chs) {
		Arrays.sort(chs);
	}

	/*
	 * 将字符串转成数组
	 */
	private static char[] stringToArray(String str) {
		return str.toCharArray();
	}

}