package store.progress;

import com.displee.cache.ProgressListener;

public interface IAbstractProgressListener extends ProgressListener {
    void finish(String title, String message);

}
