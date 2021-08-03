package com.longluo.zlibrary.core.xml;

import java.util.*;

public interface ZLXMLReader {
	public boolean dontCacheAttributeValues();

	public void startDocumentHandler();
	public void endDocumentHandler();

	// returns true iff xml processing should be interrupted
	public boolean startElementHandler(String tag, ZLStringMap attributes);
	public boolean endElementHandler(String tag);
	public void characterDataHandler(char[] ch, int start, int length);
	public void characterDataHandlerFinal(char[] ch, int start, int length);

	boolean processNamespaces();
	void namespaceMapChangedHandler(Map<String,String> namespaces);

	void collectExternalEntities(HashMap<String,char[]> entityMap);
	List<String> externalDTDs();
}
