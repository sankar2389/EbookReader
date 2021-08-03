package com.longluo.zlibrary.core.util;

import org.xml.sax.helpers.DefaultHandler;
import android.util.Xml;

import com.longluo.zlibrary.core.filesystem.ZLFile;

public abstract class XmlUtil {
	public static boolean parseQuietly(ZLFile file, DefaultHandler handler) {
		try {
			Xml.parse(file.getInputStream(), Xml.Encoding.UTF_8, handler);
			return true;
		} catch (Exception e) {
			return false;
		}
	}
}
