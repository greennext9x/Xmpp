package ousoftoa.com.xmpp.utils;


import java.util.Comparator;

import ousoftoa.com.xmpp.model.bean.Friend;


public class PinyinComparator implements Comparator {
	@Override
	public int compare(Object o1, Object o2) {
		 String str1 = PinyinUtils.getPingYin(((Friend)o1).getUsername());
	     String str2 = PinyinUtils.getPingYin(((Friend)o2).getUsername());
	     return str1.compareToIgnoreCase(str2);
	}

}
