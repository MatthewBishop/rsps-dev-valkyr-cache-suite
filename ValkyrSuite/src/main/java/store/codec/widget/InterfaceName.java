package store.codec.widget;

import java.io.IOException;

import com.displee.cache.CacheLibrary;
import store.ValkyrCacheLibrary;
import store.codec.util.Utils;

public class InterfaceName {

	public static final char[] VALID_CHARS = new char[] { 'a', 'b', 'c', 'd', 'e', 'f', 'g', 'h', 'i', 'j', 'k', 'l',
			'm', 'n', 'o', 'p', 'q', 'r', 's', 't', 'u', 'v', 'w', 'x', 'y', 'z' };

	public static void printAllCombinations4Letters() {
	}

	public static void main(String[] args) throws IOException {
		CacheLibrary rscache = new CacheLibrary("cache/", false, null);
		ValkyrCacheLibrary.singleton = rscache;
		int hash = rscache.index(3).archive(884).getHashName();
		char[] arr$ = VALID_CHARS;
		int len$ = arr$.length;

		for (int i$ = 0; i$ < len$; ++i$) {
			char l1 = arr$[i$];
			System.out.println(l1);
			char[] arr$1 = VALID_CHARS;
			int len$1 = arr$1.length;

			for (int i$1 = 0; i$1 < len$1; ++i$1) {
				char l2 = arr$1[i$1];
				char[] arr$2 = VALID_CHARS;
				int len$2 = arr$2.length;

				for (int i$2 = 0; i$2 < len$2; ++i$2) {
					char l3 = arr$2[i$2];
					char[] arr$3 = VALID_CHARS;
					int len$3 = arr$3.length;

					for (int i$3 = 0; i$3 < len$3; ++i$3) {
						char l4 = arr$3[i$3];
						char[] arr$4 = VALID_CHARS;
						int len$4 = arr$4.length;

						for (int i$4 = 0; i$4 < len$4; ++i$4) {
							char l5 = arr$4[i$4];
							char[] arr$5 = VALID_CHARS;
							int len$5 = arr$5.length;

							for (int i$5 = 0; i$5 < len$5; ++i$5) {
								char l6 = arr$5[i$5];
								String name = new String(new char[] { l1, l2, l3, l4, l5, l6 });
								if (Utils.getNameHash(name) == hash) {
									System.out.println(name);
								}
							}
						}
					}
				}
			}
		}

	}
}
