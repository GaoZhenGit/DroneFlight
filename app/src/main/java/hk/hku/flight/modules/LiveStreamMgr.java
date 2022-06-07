package hk.hku.flight.modules;

public class LiveStreamMgr {
    private static final String TAG = "LiveStreamMgr";

    private static class InstanceHolder {
        public volatile static LiveStreamMgr instance = new LiveStreamMgr();
    }

    public static LiveStreamMgr getInstance() {
        return LiveStreamMgr.InstanceHolder.instance;
    }
}
