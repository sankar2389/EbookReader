package com.longluo.ebookreader.fbreader;

import java.io.InputStream;
import java.io.IOException;
import java.util.*;

import com.longluo.zlibrary.core.network.*;
import com.longluo.zlibrary.text.view.ExtensionElementManager;

import com.longluo.ebookreader.network.NetworkLibrary;
import com.longluo.ebookreader.network.opds.*;

class BookElementManager extends ExtensionElementManager {
	private final FBView myView;
	private final Runnable myScreenRefresher;
	private final Map<Map<String,String>,List<BookElement>> myCache =
		new HashMap<Map<String,String>,List<BookElement>>();
	private Timer myTimer;

	BookElementManager(final FBView view) {
		myView = view;
		myScreenRefresher = new Runnable() {
			public void run() {
				view.Application.getViewWidget().reset();
				view.Application.getViewWidget().repaint();
			}
		};
	}

	@Override
	protected synchronized List<BookElement> getElements(String type, Map<String,String> data) {
		if (!"opds".equals(type)) {
			return Collections.emptyList();
		}

		List<BookElement> elements = myCache.get(data);
		if (elements == null) {
			try {
				final int count = Integer.valueOf(data.get("size"));
				elements = new ArrayList<BookElement>(count);
				for (int i = 0; i < count; ++i) {
					elements.add(new BookElement(myView));
				}
				startLoading(data.get("src"), elements);
			} catch (Throwable t) {
				return Collections.emptyList();
			}
			myCache.put(data, elements);
		}
		return Collections.unmodifiableList(elements);
	}

	private void startLoading(final String url, final List<BookElement> elements) {
		final NetworkLibrary library = NetworkLibrary.Instance(myView.Application.SystemInfo);

		new Thread() {
			public void run() {
				final SimpleOPDSFeedHandler handler = new SimpleOPDSFeedHandler(library, url);
				try {
					new QuietNetworkContext().perform(new ZLNetworkRequest.Get(url, true) {
						@Override
						public void handleStream(InputStream inputStream, int length) throws IOException, ZLNetworkException {
							new OPDSXMLReader(library, handler, false).read(inputStream);
						}
					});
					if (handler.books().isEmpty()) {
						throw new RuntimeException();
					}
					myTimer = null;
					final List<OPDSBookItem> items = handler.books();
					int index = 0;
					for (BookElement book : elements) {
						book.setData(items.get(index));
						index = (index + 1) % items.size();
						myScreenRefresher.run();
					}
				} catch (Exception e) {
					if (myTimer == null) {
						myTimer = new Timer();
					}
					myTimer.schedule(new TimerTask() {
						@Override
						public void run() {
							startLoading(url, elements);
						}
					}, 10000);
					e.printStackTrace();
				}
			}
		}.start();
	}
}
